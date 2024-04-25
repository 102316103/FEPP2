package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.HotbinMapper;
import com.syscom.fep.mybatis.mapper.HotcardMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.mapper.SysconfMapper;
import com.syscom.fep.mybatis.model.Hotbin;
import com.syscom.fep.mybatis.model.Hotcard;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.ATMAdapter;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.TXCUR;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.CreditTxType;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.T24TxType;

/**
 * @author Richard
 */
public class OtherIssueRequestA extends INBKAABase {
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	private Intltxn intlTxn = new Intltxn(); // 國際卡檔
	private Intltxn oriintlTxn = new Intltxn(); // 國際卡檔
	@SuppressWarnings("unused")
	private ATMTXCD txCode; // ATM交易代號
	String rtnMessage = "";
	Nwdtxn defNWDTXN = null;
	boolean needResponseMsg = true;
	private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
	private HotbinMapper hotbinMapper = SpringBeanFactoryUtil.getBean(HotbinMapper.class);
	private HotcardMapper hotcardMapper = SpringBeanFactoryUtil.getBean(HotcardMapper.class);
	private SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfMapper.class);

	public OtherIssueRequestA(ATMData txnData) throws Exception {
		super(txnData);
		//--ben-20220922-//this.txCode = ATMTXCD.parse(this.getATMRequest().getTXCD());
	}

	@Override
	public String processRequestData() {

		try {
			// 1.準備FEP交易記錄檔
			_rtnCode = getATMBusiness().prepareFEPTXN();

			// 無卡提款交易寫入NWDTXN
			if (ATMTXCD.NWD.toString().equals(getFeptxn().getFeptxnTxCode())) {
				getFeptxn().setFeptxnAtmType("6071");// 端末設備型態=無實體卡片
				getFeptxn().setFeptxnTroutActno("0000000000000000");
				//--ben-20220922-//getFeptxn().setFeptxnMajorActno(getATMRequest().getNWDSEQ()); // 提款序號
				RefBase<Nwdtxn> nwdtxnRefBase = new RefBase<>(new Nwdtxn());
				_rtnCode = getATMBusiness().prepareNWDTXN(nwdtxnRefBase);
				defNWDTXN = nwdtxnRefBase.get();
			}
			// 2. 新增FEP交易記錄檔
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue()) {
				_rtnCode = getATMBusiness().addTXData();
			}
			// 3. 商業邏輯檢核(ATM電文)
			// 2014/04/21 Modify by Ruling for 整批轉即時
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue()) {
				_rtnCode = checkBusinessRule();
			}
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue() && "24".equals(getFeptxn().getFeptxnPcode().substring(0, 2))) {
				// 2015/02/04 Modify by Ruling for 避免寫入換日前的FEPTXN，但FEPTXN_TBSDY_FISC卻為換日後，導致GL沒抓到該筆交易而帳務不合的問題
				if (!SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(getATMtxData().getTbsdyFISC())) {
					_rtnCode = FEPReturnCode.FISCBusinessDateChanged;
					getLogContext().setRemark("OtherIssueRequestA-Main：AA起始讀進的SYSSTAT_TBSDY_FISC <> 現在的SYSSTAT_TBSDY_FISC，AA起始讀進的SYSSTAT_TBSDY_FISC=" + getATMtxData().getTbsdyFISC()
							+ "，現在的SYSSTAT_TBSDY_FISC=" + SysStatus.getPropertyValue().getSysstatTbsdyFisc());
					logMessage(Level.INFO, getLogContext());
				} else {
					_rtnCode = checkHotBin();
					if (_rtnCode.getValue() == FEPReturnCode.Track2Error.getValue()) {

					}
					// 2015/08/24 Modify by Ruling for EMV拒絶磁條卡交易
					// 2017/12/15 Fly 增加檢核CheckHotBin的ReturnCode
					if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue()) {
						_rtnCode = checkEMV();
					}
					// 2015/11/04 Modify by Ruling for EMV檢核限額
					if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue()) {
						_rtnCode = checkEMVMLimit();
					}
				}
			}

			// 4. 帳務主機處理：本行轉入-進帳務主機查詢帳號
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue()) {
				_rtnCode = sendToCBS();
			}

			// 2018/02/12 Modify by Ruling for 跨行無卡提款
			// 5. 組送 FISC 之 Request 電文並等待財金之 Response 電文
			if (ATMTXCD.NWD.name().equals(getFeptxn().getFeptxnTxCode())) {
				if (_rtnCode == CommonReturnCode.Normal) {
					_rtnCode = getFiscBusiness().sendNCRequestToFISC(getATMRequest());
				}
			} else {
				if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue()) {
					_rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
				}
			}

			// 6. 檢核 FISC 之 Response電文是否正確
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue()) {
				_rtnCode = getFiscBusiness().checkResponseMessage();
			}

			// 7. Prepare國際卡交易(INTLTXN)記錄(if need)
			// 2012/12/26 Modify by Ruling for 當財金回-response時，ORI_DATA的值為０，為避免電文內的日期轉換有誤，多加fisc reprc=4001的條件成立才組Prepare Intltxn
			if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) && StringUtils.isNotBlank(getFiscRes().getOriData())) {
				RefBase<Intltxn> intltxnRefBase = new RefBase<>(intlTxn);
				RefBase<Intltxn> oriintltxnRefBase = new RefBase<>(oriintlTxn);
				_rtnCode = getFiscBusiness().prepareIntltxn(intltxnRefBase, oriintltxnRefBase, MessageFlow.Response);
				intlTxn = intltxnRefBase.get();
				oriintlTxn = oriintltxnRefBase.get();
			}
			// 2014/04/30 Modify by Ruling for 永豐要求拒絕 MASTRO 連線交易
			if (FISCPCode.PCode2450.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2460.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
				if ("4001".equals(getFeptxn().getFeptxnRepRc()) && getFiscBusiness().getFISCTxData().getTxObject().getINBKResponse().getNetwkData().replace(" ", "").length() > 2
						&& "MS".equals(getFiscBusiness().getFISCTxData().getTxObject().getINBKResponse().getNetwkData().replace(" ", "").substring(0, 2))) {
					getLogContext().setRemark("此筆交易為MASTRO交易，要回財金(-)CON，RC=0601");
					logMessage(Level.INFO, getLogContext());
					_rtnCode = FEPReturnCode.FISCTimeout;
				}
			}

			// 8. ProcessAPTOT:更新跨行代收付
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue() && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())
					&& DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
				_rtnCode = getFiscBusiness().processAptot(false);
			}
			// 9. 帳務主機處理：代理提款-進帳務主機掛現金帳
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue() && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				_rtnCode = sendToCBS2();
			}

			// 10. 組回應電文回給 ATM&判斷是否需組 CON 電文回財金/本行轉入交易掛帳
			_rtnCode2 = sendConfirm();
			// 11. 準備國際卡交易(INTLTXN)記錄(if need)
			// 2012/12/26 Modify by Ruling Intltxn多Prepare一次，Mark起來
			// If Not String.IsNullOrEmpty(fiscRes.ORI_DATA) Then _rtnCode2 = fiscBusiness.PrepareIntltxn(intlTxn, oriintlTxn, MessageFlow.Response)

			// 12.新增國際卡交易(INTLTXN)記錄(if need)
			// 2012/12/26 Modify by Ruling for 當財金回-response時，ORI_DATA的值為０，為避免電文內的日期轉換有誤，多加fisc reprc=4001的條件成立才更新Intltxn
			// 2013/10/31 Modify by Ruling for 代理國際卡餘額查詢(IQV2451/IQM2451) 2 Way 需先將交易結果回寫INTLTXN
			if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) && StringUtils.isNotBlank(getFiscRes().getOriData())) {
				intlTxn.setIntltxnTxrust(getFeptxn().getFeptxnTxrust());
				_rtnCode2 = getFiscBusiness().insertINTLTxn(intlTxn);
			}

		} catch (Exception ex) {
			rtnMessage = "";
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		} finally {
			// 13.更新交易記錄(FEPTXN)
			updateFEPTXN();

			// 14.組回應電文(當海外主機有給TOTA或DES發生例外或主機逾時不組回應)
			getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getATMtxData().getLogContext().setMessage(rtnMessage);
			getATMtxData().getLogContext().setProgramName(this.aaName);
			getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			getATMtxData().getLogContext().setProgramName(this.aaName);
			logMessage(Level.DEBUG, getLogContext());
		}

		return rtnMessage;
	}

	/**
	 * <summary>
	 * 商業邏輯檢核
	 * </summary>
	 * <returns>FEPReturnCode</returns>
	 * <remarks></remarks>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason>整批轉即時</reason>
	 * <date>2014/04/21</date>
	 * </modify>
	 */
	private FEPReturnCode checkBusinessRule() {

		try {
			if (FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel())
					|| FEPChannel.WEBATM.name().equals(getFeptxn().getFeptxnChannel())) {
				_rtnCode = checkRequestFromATM(getATMtxData());
				if (!_rtnCode.equals(FEPReturnCode.Normal)) {
					return _rtnCode;
				}
				// 2017/03/25 Modify by Ruling for WebATM晶片卡繳信用卡款
				if (FEPChannel.WEBATM.name().equals(getFeptxn().getFeptxnChannel())) {
					if (ATMTXCD.EFT.name().equals(getFeptxn().getFeptxnTxCode()) && "03".equals(getFeptxn().getFeptxnPbtype())) {
						// WEBATM電文未帶銷帳編號，將轉入帳號填入
						if (StringUtils.isNotBlank(getFeptxn().getFeptxnReconSeqno())) {
							getFeptxn().setFeptxnReconSeqno(getFeptxn().getFeptxnTrinActno());
						}
						// 檢核是否為繳本行信用卡費
						_rtnCode = getATMBusiness().checkEFTCCard(getFeptxn().getFeptxnReconSeqno());
						if (_rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
							return _rtnCode;
						}
					}
				}
			} else {
				// 外圍 Channel
				_rtnCode = checkRequestFromOtherChannel(getATMtxData());
				if (_rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
					return _rtnCode;
				}
			}
			return _rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".CheckBusinessRule");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}

	}

	private FEPReturnCode checkRequestFromOtherChannel(ATMData txnData) {
		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;

		// (2)檢核財金及參加單位之系統狀態
		rtnCode = getFiscBusiness().checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
		if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
			return rtnCode;
		}

		// (3)檢核交易之連線狀態
		if (!getATMtxData().isTxStatus()) {
			rtnCode = FEPReturnCode.InterBankServiceStop;
			return rtnCode;
		}

		// (4)檢核委託單位代號或繳款類別
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnBusinessUnit())) {
			// 檢核委託單位代號
			rtnCode = getFiscBusiness().checkNpsunit(getFeptxn());
			if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
				return rtnCode;
			}
		} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnPaytype())) {
			// 檢核繳款類別
			rtnCode = getFiscBusiness().checkPAYTYPE();
			if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
				return rtnCode;
			}
		}

		// (5)身份證號/統一編號檢核
		if ("226".equals(getFeptxn().getFeptxnPcode().substring(0, 3))) {
			rtnCode = getFiscBusiness().checkIDNO(getFeptxn().getFeptxnIdno());
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}
		}

		// (6)檢核全國繳費ID+Account限額
		if (getFeptxn().getFeptxnTxAmt().intValue() > INBKConfig.getInstance().getINBKIDLimit()) {
			return FEPReturnCode.OverLimit;
		}

		return rtnCode;
	}

	/**
	 * <summary>
	 * 檢核偽卡資料
	 * </summary>
	 * <returns>FEPReturnCode</returns>
	 * <remarks></remarks>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason>加檢核偽卡資料的Function</reason>
	 * <date>2014/03/19</date>
	 * </modify>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason>加檢核偽卡資料檔(HOTCARD)</reason>
	 * <date>2014/03/31</date>
	 * </modify>
	 */
	private FEPReturnCode checkHotBin() {
		String pan = "";
		String bin = "";
		Hotbin defHOTBIN = new Hotbin();
		Hotcard defHOTCARD = new Hotcard();
		try {
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrk2())) {
				// 2016/12/20 Modify by Ruling for 避免TRK2無"="造成Substring從0取到-1導致程式發生例外錯誤
				if (!getFeptxn().getFeptxnTrk2().contains("=")) {
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); // S
					getLogContext().setRemark("TRK2沒找到等於的符號，值為=" + getFeptxn().getFeptxnTrk2());
					logMessage(Level.INFO, getLogContext());
					return ATMReturnCode.Track2Error;
				}

				pan = StringUtils.trim(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")));
				// 2014/10/06 Modify by Ruling for 加判斷:pan的Length必需>=6，否則下一行做Substring會發生Exception
				if (pan.length() < 6) {
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); // S
					getLogContext().setRemark("TRK2取=前的長度不能小於6位，值為=" + pan);
					logMessage(Level.INFO, getLogContext());
					return ATMReturnCode.Track2Error;
				}

				bin = pan.substring(0, 6);

				// 檢核HOTCARD
				defHOTCARD.setPanNo(pan);
				defHOTCARD = hotcardMapper.selectByPrimaryKey(defHOTCARD.getPanNo());
				if (defHOTCARD != null) {
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); // S
					getLogContext().setRemark("在HOTCARD檔有找到資料，PAN_NO=" + defHOTCARD.getPanNo());
					logMessage(Level.INFO, getLogContext());
					return ATMReturnCode.Track2Error;
				}

				// 檢核HOTBIN
				defHOTBIN.setBinNo(bin);
				defHOTBIN = hotbinMapper.selectByPrimaryKey(defHOTBIN.getBinNo());
				if (defHOTBIN != null) {
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); // S
					getLogContext().setRemark("在HOTBIN檔有找到資料，BIN_NO=" + defHOTBIN.getBinNo());
					logMessage(Level.INFO, getLogContext());
					return ATMReturnCode.Track2Error;
				}

				// Fly 2017/11/27 增加防呆處理, 避免手續費記號不相符, 影響帳務
				if (FISCPCode.PCode2410.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2420.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
					String CHARGEFG = "";
					BigDecimal ACFEE = new BigDecimal(0);
					RefBase<BigDecimal> bigDecimalRefBase = new RefBase<>(ACFEE);
					RefString chargefg = new RefString(CHARGEFG);
					FEPReturnCode rtn = getFiscBusiness().checkEUVISABIN(bin, bigDecimalRefBase, chargefg);
					CHARGEFG = chargefg.get();
					ACFEE = bigDecimalRefBase.get();
					if (rtn.getValue() != FEPReturnCode.Normal.getValue()) {
						return rtn;
					}
					//--ben-20220922-//if ((CHARGEFG.equals("N") && getATMRequest().getACFEE().equals("Y"))
					//--ben-20220922-//		|| (CHARGEFG.equals("Y") && !getATMRequest().getACFEE().equals("Y") && !getATMRequest().getACFEE().equals("N"))) {
					//--ben-20220922-//	getLogContext().setRemark("ATM送來的手續費記號[" + getATMRequest().getACFEE() + "] 與FEP查詢的手續費記號[" + CHARGEFG + "] 不一致");
					//--ben-20220922-//	logMessage(Level.INFO, getLogContext());
					//--ben-20220922-//	return ATMReturnCode.OtherCheckError;
					//--ben-20220922-//}
				}
			}

			// 2014/10/06 Modify by Ruling for 檢核正確時回Normal
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			// 2014/10/06 Modify by Ruling for 前端的ErrorCode仍是Normal，要改用Return
			// _rtnCode = CommonReturnCode.ProgramException
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".CheckHotBin");
			sendEMS(getLogContext());
			// 2014/10/06 Modify by Ruling for 前端的ErrorCode仍是Normal，要改用Return
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * <summary>
	 * 檢核EMV晶片卡拒絶磁條卡提款或預借現金交易
	 * </summary>
	 * <returns>FEPReturnCode</returns>
	 * <remarks></remarks>
	 * <History>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason>New Function for EMV拒絶磁條卡交易</reason>
	 * <date>2015/08/24</date>
	 * </modify>
	 * </History>
	 */
	private FEPReturnCode checkEMV() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			if (FISCPCode.PCode2400.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2410.getValueStr().equals(getFeptxn().getFeptxnPcode())
					|| FISCPCode.PCode2420.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2450.getValueStr().equals(getFeptxn().getFeptxnPcode())
					|| FISCPCode.PCode2460.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
				if (DbHelper.toBoolean(getATMBusiness().getATMMSTR().getAtmEmv())) {
					// 已升級 EMV 機台，不得送EMV磁條卡電文
					// 2016/05/09 Modify by Ruling for 已升級EMV機台，只拒絕銀聯磁條交易(CUP)
					if (FISCPCode.PCode2400.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
						rtnCode = FISCReturnCode.CCardServiceNotAllowed;
						getLogContext().setRemark("CheckEMV-ATM 已升級 EMV 機台，不得送 EMV 磁條卡電文");
						// LogContext.Remark = "CheckEMV-ATM不允許信用卡交易"
						logMessage(Level.INFO, getLogContext());
						return rtnCode;
					}
				}
				// Fly 2017/11/27 調整流程，一律檢核是否冋意收取處理費
				//--ben-20220922-//switch (getATMRequest().getICEMV()) {
				//--ben-20220922-//	case "N": // 未過初步檢核
				//--ben-20220922-//		getLogContext().setRemark("CheckEMV-ATM無此交易，IC卡讀寫設備未支援(E926)，getATMRequest().ICEMV=" + getATMRequest().getICEMV());
				//--ben-20220922-//		logMessage(Level.INFO, getLogContext());
				//--ben-20220922-//		return FEPReturnCode.ICEMVError;
				//--ben-20220922-//	case "Y": // 已過初步檢核
						// CWV2410/CAV2420 交易收手續費
				//--ben-20220922-//		if (FISCPCode.PCode2410.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2420.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
				//--ben-20220922-//			if ("N".equals(getATMRequest().getACFEE())) {
								// 客戶不同意收取處理費，交易拒絕
				//--ben-20220922-//				getLogContext().setRemark("CheckEMV-客戶不同意收取處理費交易拒絕(E928)，getATMRequest().ACFEE=" + getATMRequest().getACFEE());
				//--ben-20220922-//				logMessage(Level.INFO, getLogContext());
				//--ben-20220922-//				return FEPReturnCode.ACFeeReject;
				//--ben-20220922-//			} else if ("Y".equals(getATMRequest().getACFEE())) {
								// 客戶已同意收取處理費
				//--ben-20220922-//				rtnCode = checkACFEE();
				//--ben-20220922-//				if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
				//--ben-20220922-//					return rtnCode;
				//--ben-20220922-//				}
				//--ben-20220922-//			}
				//--ben-20220922-//		}
				//--ben-20220922-//		break;
				//--ben-20220922-//}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "CheckEMV");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 收取EMV晶片卡處理費
	 * 
	 * @return FEPReturnCode
	 * 
	 *         <History>
	 *         <modify>
	 *         <modifier>Ruling</modifier>
	 *         <reason>New Function for EMV拒絶磁條卡交易</reason>
	 *         <date>2015/09/09</date>
	 *         </modify>
	 *         </History>
	 */
	private FEPReturnCode checkACFEE() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Sysconf defSYSCONF = new Sysconf();

		try {
			defSYSCONF.setSysconfSubsysno((short) 1);
			defSYSCONF.setSysconfName("EMVACFeeFlag");
			defSYSCONF = sysconfMapper.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
			if (defSYSCONF == null) {
				rtnCode = ATMReturnCode.OtherCheckError;
				getLogContext().setRemark("CheckACFEE-在SYSCONF檔找不到EMVACFeeFlag的值");
				logMessage(Level.INFO, getLogContext());
				return rtnCode;
			} else {
				if ("Y".equals(defSYSCONF.getSysconfValue())) {
					getFeptxn().setFeptxnRsCode("Y");
					getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(INBKConfig.getInstance().getEMVAccessFee()));
				} else {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckACFEE-在SYSCONF檔的EMVACFeeFlag其值不等於Y");
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".CheckACFEE");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * <summary>
	 * EMV檢核限額
	 * </summary>
	 * <returns>FEPReturnCode</returns>
	 * <remarks></remarks>
	 * <History>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason>New Function for EMV檢核限額</reason>
	 * <date>2015/11/04</date>
	 * </modify>
	 * </History>
	 */
	private FEPReturnCode checkEMVMLimit() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String trk2SCode = "";

		try {
			// 2016/05/31 Modify by Ruling for 應永豐要求取消銀聯卡(2400)月限額
			if (FISCPCode.PCode2410.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2420.getValueStr().equals(getFeptxn().getFeptxnPcode())
					|| FISCPCode.PCode2450.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2460.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrk2())) {
					if (getFeptxn().getFeptxnTrk2().indexOf("=") > -1) {
						trk2SCode = StringUtils.rightPad(getFeptxn().getFeptxnTrk2(), 40, " ").substring(getFeptxn().getFeptxnTrk2().indexOf("=") + 5, getFeptxn().getFeptxnTrk2().indexOf("=") + 5 + 1); // 取TRK2的Service Code(2或6開頭表示卡片為晶片卡)
						if (!DbHelper.toBoolean(getATMBusiness().getATMMSTR().getAtmEmv()) && (trk2SCode.equals("2") || trk2SCode.equals("6"))) {
							// ATM未升級EMV機台且Service Code為2或6要檢查限額
							rtnCode = getFiscBusiness().checkEMVMLimit();
							if (rtnCode != CommonReturnCode.Normal) {
								return rtnCode;
							}
						}
					} else {
						rtnCode = FEPReturnCode.Track2Error;
						getLogContext().setRemark("CheckEMVMLimit(EMV檢核限額)-Feptxn.FEPTXN_TRK2的值找不到'='，無法取Service Code");
						logMessage(Level.INFO, getLogContext());
						return rtnCode;
					}
				} else {
					rtnCode = FEPReturnCode.Track2Error;
					getLogContext().setRemark("CheckEMVMLimit(EMV檢核限額)-Feptxn.FEPTXN_TRK2的值為NULL或空白");
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "CheckEMVMLimit");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * <summary>
	 * 帳務主機處理：本行轉入-進帳務主機查詢帳號
	 * </summary>
	 * <returns></returns>
	 * <history>
	 * <modify>
	 * <modifier>Husan</modifier>
	 * <reason> 使用 MsgCtl.MSGCTL_ATM_2WAY 判斷是否為2WAY</reason>
	 * <reason>connie spec異動 </reason>
	 * <date>2010/10/08</date>
	 * </modify>
	 * <modify>
	 * <modifier>Husan</modifier>
	 * <reason> SendToCBS 多傳參數</reason>
	 * <reason>connie spec change </reason>
	 * <date>2010/10/21</date>
	 * </modify>
	 * </history>
	 * <remarks></remarks>
	 */
	private FEPReturnCode sendToCBS() {
		T24 hostT24 = new T24(getATMtxData());
		Credit hostCredit = new Credit(getATMtxData());

		try {
			// 轉入行為本行
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
				if (!"G".equals(getFeptxn().getFeptxnTrinKind())) {
					// 非GIFT卡，即一般交易進CBS主機查詢
					// connie spec異動 modify by Husan 2010-10-21
					_rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), false);
					if (_rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
						return _rtnCode;
					}
				} else {
					// 為GIFT卡，進信用卡主機檢核額度
					_rtnCode = hostCredit.sendToCredit(getATMtxData().getMsgCtl().getMsgctlAsctxid(), Byte.valueOf(CreditTxType.Accounting.getValue() + ""));
					if (_rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
						return _rtnCode;
					}
					// add by Maxine 3/20 修改 for 轉帳加值 GIFT 卡, 上T24主機作 Validate
					_rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid1(), T24TxType.Accounting.getValue(), false);
					if (_rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
						return _rtnCode;
					}

				}
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".SendToCBS");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * <summary>
	 * 帳務主機處理：代理提款-進帳務主機掛現金帳
	 * </summary>
	 * <returns></returns>
	 * <modify>
	 * <modifier>Husan</modifier>
	 * <reason> SendToCBS 多傳參數</reason>
	 * <reason>connie spec change </reason>
	 * <date>2010/10/21</date>
	 * <reason>國際卡交易上主機前需先 GET清算幣別 </reason>
	 * <date>2010/12/09</date>
	 * <reason>for 清算檔 ,spec change </reason>
	 * <date>2011/2/18</date>
	 * </modify>
	 * <remarks></remarks>
	 */
	private FEPReturnCode sendToCBS2() {
		T24 hostT24 = new T24(getATMtxData());

		try {
			// 提款
			if (getATMtxData().getMsgCtl().getMsgctlCbsFlag() == 1 && StringUtils.isBlank(getFeptxn().getFeptxnTrinBkno())) {
				// modify 20110218
				// modify 2010/12/09 國際卡交易上主機前需先 GET清算幣別
				// connie spec異動 modify by Husan 2010-10-21
				_rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), true);
				if (_rtnCode != CommonReturnCode.Normal) {
					// modify 20110218
					if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot(), false)) {
						_rtnCode2 = getFiscBusiness().processAptot(true);
					}
					return _rtnCode;
				}
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".SendToCBS2");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * <summary>
	 * 判斷是否需組 CON 電文回財金
	 * </summary>
	 * <returns></returns>
	 * <remarks>判斷欄位是否有值,必須用Not IsNullorEmpty(Item)判斷,之後用Not IsNullorEmpty(Item.Trim)</remarks>
	 * <history>
	 * <modify>
	 * <modifier>Husan</modifier>
	 * <reason>GetRCFromErrorCode 傳入第一個參數為 error code 要先轉出成error code</reason>
	 * <reason>一開始要先判斷rtncode是否為normal同時rep_rc為"4001"</reason>
	 * <date>2010/10/05</date>
	 * <reason>MSGCTL Schema修改 MSGCTL_2WAY變MSGCTL_FISC_2WAY</reason>
	 * <date>2010/10/07</date>
	 * <reason>connie spec異動</reason>
	 * <date>2010/10/08</date>
	 * <reason>connie spec異動 sendtoCBS 新增參數</reason>
	 * <date>2010/10/21</date>
	 * <reason>connie spec異動 </reason>
	 * <date>2010/10/29</date>
	 * <reason>connie spec異動 GetRCfromErrorCode </reason>
	 * <date>2010/11/10</date>
	 * <reason>connie spec異動 增加組回應電文回給ATM-- SendToATM() </reason>
	 * <date>2010/11/19</date>
	 * <reason>connie spec異動 回應ATM之前都要UPDATE-Table </reason>
	 * <date>2010/11/23</date>
	 * <reason>connie spec異動 增加Feptxn_Pending =2 </reason>
	 * <date>2010/11/24</date>
	 * <reason>修正Const RC</reason>
	 * <date>2010/11/25</date>
	 * <reason>一開始channel不需要再判斷,在TXhelper裡面會把WEBATM當成ATM</reason>
	 * <date>2011/01/13</date>
	 * <reason>修正更新ATM CASH</reason>
	 * <date>2011/01/28</date>
	 * </modify>
	 * </history>
	 */
	private FEPReturnCode sendConfirm() {
		T24 hostT24 = new T24(getATMtxData());
		Credit hostCredit = new Credit(getATMtxData());
		try {
			// modify 20110113
			// Dim sorChannel As FEPChannel
			if (_rtnCode.getValue() == FEPReturnCode.Normal.getValue() && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				// +REP
				// getFeptxn().FEPTXN_REPLY_CODE = "0000"
				// connie spec異動 modify by Husan 2010-10-21
				getFeptxn().setFeptxnReplyCode("    ");
				// connie spec異動 modify by Husan 2010-10-08
				if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlAtm2way())) {
					// 3WAY(代理提款-ATM)
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); // Pending
					// connie spec add 2010/11/23
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
					getATMBusiness().updateTxData();
					// connie spec add 2010/11/19
					sendToATM();
				} else {
					// 2WAY
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
					// connie spec change 2010/11/24
					getFeptxn().setFeptxnPending((short) 2); /// *解除 PENDING*/
					// connie spec change 2010/11/19
					if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlFisc2way())) {// /*for餘額查詢*/
						// connie spec add 2010/11/23
						getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
						getATMBusiness().updateTxData();
						sendToATM();
					} else {
						/// *轉帳類交易直接送confirm to FISC*/
						// getFeptxn().FEPTXN_PENDING = 2 '解除 PENDING
						getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK); // +CON
						_rtnCode2 = getFiscBusiness().sendConfirmToFISC(); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
						// connie spec add 2010/11/23
						getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
						getATMBusiness().updateTxData();
						sendToATM();
						// 上主機入帳
						if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
							// connie spec異動 modify by Husan 2010-10-21
							_rtnCode2 = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), true); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
							// Gift卡
							if ("G".equals(getFeptxn().getFeptxnTrinKind())) {
								// Gift卡進信用卡主機加值
								_rtnCode2 = hostCredit.sendToCredit(getATMtxData().getMsgCtl().getMsgctlAsctxid1(), (byte) CreditTxType.Accounting.getValue()); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
							}
							// 2014/05/06 Modify by Ruling for 整批轉即時:因送T24前已先組好ATM回應電文，當時的FEPTXN_CBS_RRN為空白，送完T24之後再將值塞給ATMTOTA.CBS_TXID
							if (FEPChannel.FCS.name().equals(getFeptxn().getFeptxnChannel())) {
								//ben20221118  getATMtxData().getTxObject().getResponse().setCbsTxid(getFeptxn().getFeptxnCbsRrn());
							}
						}
					}
				}

				// 2014/04/21 Modify by Ruling for 整批轉即時：Channel為ATM、WEBATM，才寫入ATM清算資料
				// 寫入 ATM 清算資料
				if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAtmc())
						&& (FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel()) || FEPChannel.WEBATM.name().equals(getFeptxn().getFeptxnChannel()))) {
					// If ATMtxData.MsgCtl.MSGCTL_UPDATE_ATMC Then
					_rtnCode2 = getATMBusiness().insertATMC(1);

					// 2015/08/25 Modify by Ruling for EMV拒絶磁條卡交易：ATM未升級EMV機台時，寫入EMV限額檔
					// 2016/05/31 Modify by Ruling for 應永豐要求取消銀聯卡(2400)月限額
					if ((FISCPCode.PCode2410.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2420.getValueStr().equals(getFeptxn().getFeptxnPcode())
							|| FISCPCode.PCode2450.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2460.getValueStr().equals(getFeptxn().getFeptxnPcode()))
							&& !TXCUR.TWD.equals(getFeptxn().getFeptxnTxCurSet())) {
						String serviceCode = StringUtils.leftPad(getFeptxn().getFeptxnTrk2(), 40, ' ').substring(getFeptxn().getFeptxnTrk2().indexOf("=") + 5, 1); // Service Code為2或6開頭，表示卡片符合EMV晶片卡
						if (!DbHelper.toBoolean(getATMBusiness().getAtmStr().getAtmEmv()) && ("2".equals(serviceCode) || "6".equals(serviceCode))) {
							_rtnCode2 = getATMBusiness().insertEMVC(1);
						}
					}
				}
				// 2014/04/21 Modify by Ruling for 整批轉即時：實體ATM才更新鈔匣資料
				// 更新 ATM 鈔匣資
				if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAtmcash()) && FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel())) {
					_rtnCode2 = getATMBusiness().updateATMCash(1);
				}
			} else {
				// connie spec change modify by husan 2010/11/10
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && StringUtils.isNotBlank(StringUtils.trim(getFeptxn().getFeptxnRepRc()))) {// 代表Feptxn.FEPTXN_REP_RC有值包含要去空白
					getFeptxn().setFeptxnPending((short) 2); // 解除 Pending
					if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {// +REP
						if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlFisc2way())) {// 3WAY
							getLogContext().setProgramName(ProgramName);
							getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
							getLogContext().setProgramName(ProgramName);
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但不可以使用Feptxn_Channel,因為forT24使用會有變動,所以要改用AData資料(ATMtxData)
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
							_rtnCode2 = getFiscBusiness().sendConfirmToFISC(); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
						} else {// 2WAY
							// 2012/05/29 Modifry by Ruling for 財金2WAY交易(IIQ)，如檢核 KEY SYNC 錯誤，仍要回給 ATM 錯誤代碼
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
							getLogContext().setProgramName(ProgramName);
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext()));
						}
					} else {// -REP

						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但不可以使用Feptxn_Channel,因為forT24使用會有變動,所以要改用AData資料(ATMtxData)
					}
				} else {// FEPReturnCode <> Normal
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但不可以使用Feptxn_Channel,因為forT24使用會有變動,所以要改用AData資料(ATMtxData)
					}
				}
				// connie spec add 2010/11/23
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
				getATMBusiness().updateTxData();
				// connie spec add 2010/11/19
				sendToATM();
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".SendConfirm");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 組回應電文
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Husan</modifier>
	 * <reason>Function design</reason>
	 * <date>2010/11/19</date>
	 * </modify>
	 * </history>
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode sendToATM() {
		ATMAdapter oatmAdapter = new ATMAdapter(getATMtxData());
		FEPReturnCode rtncode = null;
		try {
			// 先送給ATM主機
			// 2010-04-21 modified by kyo for 跟著明祥的atmadapter更新一起更新，若不塞ATMNo會發生例外
			// BugReport(001B0672): by Jim 取 ATMNO 透過電文欄位
			//--ben-20220922-//oatmAdapter.setAtmNo(getATMRequest().getBRNO() + getATMRequest().getWSNO());
			if (StringUtils.isBlank(getATMtxData().getTxResponseMessage()) && needResponseMsg) {
				rtnMessage = prepareATMResponseData();
			} else {
				rtnMessage = getATMtxData().getTxResponseMessage();
			}

			// 2014/04/23 Modify by Ruling for 整批轉即時：ATM和WebATM才能透過ATMAdapter先送回給ATM，FCS由原路回去
			if (FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel()) || FEPChannel.WEBATM.name().equals(getFeptxn().getFeptxnChannel())) {
				if (StringUtils.isNotBlank(StringUtils.trim(rtnMessage))) {
					oatmAdapter.setMessageToATM(rtnMessage);
					rtncode = oatmAdapter.sendReceive();
				} else {
					// 若需要回ATM電文，但rtnMessage是空的表示有問題需alert
					getLogContext().setRemark("ATM組出來的回應電文為空字串");
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
				}
				// 因為先送給ATM了所以要將回應字串清成空字串
				rtnMessage = "";
			}
			return _rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToATM"));
			sendEMS(getLogContext());
			_rtnCode = CommonReturnCode.ProgramException;
		}
		return rtncode;
	}

	/// #Region "13.更新交易記錄檔"
	/**
	 * 更新FEPTXN
	 * 
	 * @return
	 * 
	 *         <modify>
	 *         <modifier>Husan</modifier>
	 *         <reason>connie spec change</reason>
	 *         <date>2010/11/23</date>
	 *         </modify>
	 */
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = null;

		if (_rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
			getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		} else if (_rtnCode2.getValue() != FEPReturnCode.Normal.getValue()) {
			getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
		} else {
			getFeptxn().setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}

		getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

		rtnCode = getFiscBusiness().updateTxData();
		/* Trace.WriteLine("return code in updatefeptxn:  " + rtnCode.toString()); */
		if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
			return rtnCode;
		}

		// 2017/02/22 Modify by Ruling for 跨行無卡提款
		if (ATMTXCD.NWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
			defNWDTXN.setNwdtxnTxCurAct(getATMBusiness().getFeptxn().getFeptxnTxCurAct());
			defNWDTXN.setNwdtxnTxAmtAct(getATMBusiness().getFeptxn().getFeptxnTxAmtAct());
			defNWDTXN.setNwdtxnBrno(getATMBusiness().getFeptxn().getFeptxnBrno());
			defNWDTXN.setNwdtxnZoneCode(getATMBusiness().getFeptxn().getFeptxnZoneCode());
			defNWDTXN.setNwdtxnRepRc(getATMBusiness().getFeptxn().getFeptxnReplyCode());
			defNWDTXN.setNwdtxnTxrust(getATMBusiness().getFeptxn().getFeptxnTxrust());
			defNWDTXN.setNwdtxnTroutActno(getATMBusiness().getFeptxn().getFeptxnTroutActno());
			defNWDTXN.setNwdtxnIdno(getATMBusiness().getFeptxn().getFeptxnIdno());
			try {
				nwdtxnMapper.insertSelective(defNWDTXN);
			} catch (Exception ex) {
				getLogContext().setProgramException(ex);
				sendEMS(getLogContext());
			}
		}

		return FEPReturnCode.Normal;

	}
}
