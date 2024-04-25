package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.IctltxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author ZhaoKai
 */
@Deprecated
public class ICCommonI extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
	private boolean isIC = false;
	private Ictltxn defICTLTXN = new Ictltxn();
	private Ictltxn oriICTLTXN = new Ictltxn();
	private IctltxnExtMapper dbICTLTXN = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");

	private boolean isEC = false;

	public ICCommonI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * <summary>
	 * ''' 程式進入點
	 * ''' </summary>
	 * ''' <returns>Response電文</returns>
	 * ''' <remarks></remarks>
	 */
	@Override
	public String processRequestData() {
		try {
			// 拆解並檢核財金電文
			_rtnCode = processRequestHeader();

			// 換日後該筆交易應重抓DBFepTxn在INSERT FEPTXN時才會寫入換日後的FEPTXNXX
			if (!SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8).equals(this.feptxnDao.getTableNameSuffix())) {
				this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "processRequestData"));
				this.getTxData().setFeptxnDao(this.feptxnDao);
				getFiscBusiness().setFeptxnDao(this.feptxnDao);
			}

			// PrepareFEPTXN
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().prepareFEPTXN_IC();
			}
			if (isIC) {
				if (_rtnCode == CommonReturnCode.Normal) {
					RefBase<Ictltxn> ictltxnRefBase = new RefBase<>(defICTLTXN);
					RefBase<Ictltxn> oriictltxnRefBase = new RefBase<>(oriICTLTXN);
					_rtnCode = getFiscBusiness().prepareIctltxn(ictltxnRefBase, oriictltxnRefBase, MessageFlow.Request);
					defICTLTXN = ictltxnRefBase.get();
					oriICTLTXN = oriictltxnRefBase.get();
				}
				// spec change 20101109
				if (_rtnCode != CommonReturnCode.Normal) {
					if (_rtnCode == FISCReturnCode.OriginalMessageDataError) {// MAPPING 欄位資料不符
						_rtnCode = CommonReturnCode.Normal;
						strFISCRc = FISCReturnCode.OriginalMessageDataError;
						getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					}
				}
			}

			// 新增交易記錄
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = addTxData();
			}

			if (strFISCRc == CommonReturnCode.Normal) {
				strFISCRc = _rtnCode;
			}

			// 商業邏輯檢核 & 電文Body檢核
			if (_rtnCode == CommonReturnCode.Normal && strFISCRc == CommonReturnCode.Normal) {
				strFISCRc = checkBusinessRule();
				// SendToCBS/ASC
				if (strFISCRc == CommonReturnCode.Normal) {
					_rtnCode = sendToCBS();
				}
			}

			// 組回傳財金Response電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = prepareForFISC();
			}

			// 更新交易記錄(FEPTXN & ICTLTXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = updateTxData();
			}

			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processAPTOT();
			}

			// 將組好的財金電文送給財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			}

		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}

		return "";

	}

	/**
	 * 拆解並檢核由財金發動的Request電文
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 檢核財金電文 Header
		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

		if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
				|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
			return rtnCode;
		}

		// 判斷是否為晶片金融卡跨國交易
		// 避免OPC(3106)做2000-ExceptionCheckOut時，回的rtnCode=ReceiverBankServiceStop會被蓋掉的問題
		if (StringUtils.isNotBlank(getFiscReq().getIcData())) {
			isIC = true;
		}

		if (rtnCode != CommonReturnCode.Normal) {
			strFISCRc = rtnCode;
			rtnCode = CommonReturnCode.Normal;
		}

		return rtnCode;
	}

	/**
	 * 新增交易記錄
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			rtnCode = getFiscBusiness().insertFEPTxn();
			if (rtnCode != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				return rtnCode;
			}
			if (isIC) {
				if (dbICTLTXN.insertSelective(defICTLTXN) < 1) {
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}
			// 2010-11-01 by kyo for Business 屬性為Assign因此造成例外
			// fiscBusiness.IntlTxn = defICTLTXN
			transactionManager.commit(txStatus);

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 商業邏輯檢核
	 * 
	 * @return
	 * 
	 */

	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.ProgramException;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		try {
			// 檢核特約商店代號
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnMerchantId())) {
				rtnCode = getFiscBusiness().checkMerchant();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}

			// 檢核提款金額及單筆限額
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}

			// 檢核MAC & IC TAC
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnIcTac())) {

				if (getFiscReq().getTAC().length() < 16) {// 長度小於16
					return FISCReturnCode.LengthError;
				}

				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnIcmark())
						|| StringUtils.leftPad("0", getTxData().getTxObject().getINBKRequest().getICMARK().length(), '0').equals(getTxData().getTxObject().getINBKRequest().getICMARK())) {
					getLogContext().setRemark("財金電文ICMARK為空白=" + getTxData().getTxObject().getINBKRequest().getICMARK());
					logMessage(Level.INFO, getLogContext());
					return ATMReturnCode.ICMARKError;
				}

				rtnCode =
						encHelper.checkFISCMACICTAC(getFiscReq().getMAC(), getFiscReq().getICDATA().getTxCur(), getFiscReq().getICDATA().getAcqCntry(), (int) getTxData().getMsgCtl().getMsgctlTacType());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				// 拒絕預借現金交易-原存交易轉出帳號不得為信用卡號
				if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnTroutKind())) {
					getLogContext().setRemark("轉出帳號不得為信用卡號");
					logMessage(Level.INFO, getLogContext());
					return FISCReturnCode.CCardServiceNotAllowed;
				}

				// 檢核 Card Status
				if (!FISCPCode.PCode2572.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
						&& !FISCPCode.PCode2546.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
					if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnMajorActno())) {
						getLogContext().setRemark("卡片帳號(FEPTXN_MAJOR_ACTNO)為空白=" + getFiscBusiness().getFeptxn().getFeptxnMajorActno());
						logMessage(Level.INFO, getLogContext());
						return ATMReturnCode.NotICCard; // 4401-失效卡片
					}

					// 一般金融卡
					rtnCode = getFiscBusiness().checkCardStatus();
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}

					// 海外卡不提供跨國晶片卡交易
					if (!ZoneCode.TWN.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode())) {
						getLogContext().setRemark(StringUtils.join("海外卡不提供跨國晶片卡交易, FEPTXN_ZONE_CODE=", getFiscBusiness().getFeptxn().getFeptxnZoneCode()));
						logMessage(Level.INFO, getLogContext());
						return FISCReturnCode.CCardServiceNotAllowed; // 4501:該卡片之帳戶為問題帳戶
					}

					// mma悠遊Debit卡/金融儲值卡不提供跨國晶片卡交易
					if (getFiscBusiness().getCard().getCardType() == 27 || getFiscBusiness().getCard().getCardType() == 53) {
						getLogContext().setRemark(StringUtils.join("mma悠遊Debit卡/金融儲值卡不提供跨國晶片卡交易, CARD_TYPE=", getFiscBusiness().getCard().getCardType()));
						logMessage(Level.INFO, getLogContext());
						return FISCReturnCode.CCardServiceNotAllowed; // 4501:該卡片之帳戶為問題帳戶
					}

					// 被代理不可執行信用卡交易
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnTroutKind())) {
						getLogContext().setRemark(StringUtils.join("被代理不可執行信用卡交易, FEPTXN_TROUT_KIND=", getFiscBusiness().getFeptxn().getFeptxnTroutKind()));
						logMessage(Level.INFO, getLogContext());
						return FISCReturnCode.CCardServiceNotAllowed; // 4501:該卡片之帳戶為問題帳戶
					}
				}
			}

			// 檢核和更新原始交易狀態(for 2572/2546)
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				rtnCode = checkoriFEPTXN();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 檢核和更新原始交易狀態
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode checkoriFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String I_TX_DATE = "";

		try {
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
			// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 本營業日檔
			getFiscBusiness().setOriginalFEPTxn(oriDBFEPTXN.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8); // for 2572/2546

				getLogContext().setRemark(StringUtils.join("SearchFeptxn 以FEPTXN_TX_DATETIME_FISC[1,8]=", getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8), " FEPTXN_BKNO=",
						getFiscBusiness().getFeptxn().getFeptxnBkno(), " FEPTXN_ORI_STAN=", getFiscBusiness().getFeptxn().getFeptxnOriStan(), " 找原交易"));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());

				rtnCode = searchOriginalFEPTxn(I_TX_DATE, getFiscBusiness().getFeptxn().getFeptxnBkno(), getFiscBusiness().getFeptxn().getFeptxnOriStan());
				if (rtnCode != CommonReturnCode.Normal) {
					rtnCode = FISCReturnCode.TransactionNotFound;
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					getLogContext().setRemark("SearchFeptxn 找不到原交易");
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
			}

			// 檢核原交易是否成功
			if (!"A".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()) && !"B".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {
				rtnCode = FISCReturnCode.StatusNotMatch; // 交易狀態有誤
				getFiscBusiness().getFeptxn().setFeptxnTxrust("I"); // 原交易已拒絕
				getLogContext().setRemark(StringUtils.join("原交易不成功, FEPTXN_TXRUST=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				return rtnCode;
			}

			// 檢核原交易之 MAPPING 欄位是否相同
			if (!getFiscBusiness().getOriginalFEPTxn().getFeptxnDesBkno().equals(getFiscBusiness().getFeptxn().getFeptxnDesBkno())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().equals(getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc())
					|| getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtAct().doubleValue() != getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().doubleValue()
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnAtmno().trim())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim())) {
				rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
				getLogContext()
						.setRemark(StringUtils.join("與原交易欄位不相同, DES_BKNO=", getFiscBusiness().getFeptxn().getFeptxnDesBkno(), " 原DES_BKNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnDesBkno(),
								", TX_DATETIME_FISC=", getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc(), " 原TX_DATETIME_FISC=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc(),
								", TX_AMT_ACT=", getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString(), " 原TX_AMT_ACT=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtAct().toString(),
								", ATMNO=", getFiscBusiness().getFeptxn().getFeptxnAtmno(), " 原ATMNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno(), ", TROUT_ACTNO=",
								getFiscBusiness().getFeptxn().getFeptxnTroutActno(), " 原TROUT_ACTNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				return rtnCode;
			}
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("T"); // 沖銷或授權完成進行中
			oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());

			getFiscBusiness().getFeptxn().setFeptxnTroutActno(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno());
			getFiscBusiness().getFeptxn().setFeptxnTroutKind(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutKind());
			getFiscBusiness().getFeptxn().setFeptxnMajorActno(getFiscBusiness().getOriginalFEPTxn().getFeptxnMajorActno());
			getFiscBusiness().getFeptxn().setFeptxnCardSeq(getFiscBusiness().getOriginalFEPTxn().getFeptxnCardSeq());
			getFiscBusiness().getFeptxn().setFeptxnDueDate(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().substring(0, 8));

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkoriFEPTXN"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 以日期搜尋 FEPTXN
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode searchOriginalFEPTxn(String txDate, String bkno, String stan) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
		Bsdays aBSDAYS = new Bsdays();
		BsdaysMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
		String wk_TBSDY = null;
		String wk_NBSDY = "";
		// Dim i As Int32
		try {
			db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
			getFiscBusiness().setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				aBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
				aBSDAYS.setBsdaysDate(txDate);
				aBSDAYS = dbBSDAYS.selectByPrimaryKey(aBSDAYS.getBsdaysZoneCode(), aBSDAYS.getBsdaysDate());
				if (aBSDAYS == null) {
					return IOReturnCode.BSDAYSNotFound;
				}
				// ASK CONNIE
				if (DbHelper.toBoolean(aBSDAYS.getBsdaysWorkday())) {// 工作日
					wk_TBSDY = aBSDAYS.getBsdaysDate();
					wk_NBSDY = aBSDAYS.getBsdaysNbsdy();
				} else {
					wk_TBSDY = aBSDAYS.getBsdaysNbsdy();
				}
				if (wk_TBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
					db.setTableNameSuffix(wk_TBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
					getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
					getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
					getFiscBusiness().setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
					if (getFiscBusiness().getOriginalFEPTxn() == null) {
						if (StringUtils.isNotBlank(wk_NBSDY) && wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
							db.setTableNameSuffix(wk_NBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
							getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
							getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
							getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
							getFiscBusiness()
									.setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
							if (getFiscBusiness().getOriginalFEPTxn() == null) {
								rtnCode = IOReturnCode.FEPTXNNotFound;
							}
						} else {
							rtnCode = IOReturnCode.FEPTXNNotFound;
						}
					}
				} else {
					rtnCode = IOReturnCode.FEPTXNNotFound;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".searchOriginalFEPTxn"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 6. SendToCBS/ASC(if need): 帳務主機處理
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		T24 hostT24 = new T24(getTxData());

		try {
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
				boolean ProcessTag = false;
				byte TxType = 0;

				if (getTxData().getMsgCtl().getMsgctlCbsFlag() != 0) {
					// 轉出交易
					TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().byteValue();
					ProcessTag = true;
				} else {
					// 轉入交易
					TxType = 1;
					ProcessTag = false;
				}

				rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag);
				if (rtnCode != CommonReturnCode.Normal) {
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
						strFISCRc = rtnCode;
						return CommonReturnCode.Normal;
					} else {
						getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
						getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
						getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
						getFiscBusiness().updateTxData();

						if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 成功
							oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
						}
						return rtnCode;
					}
				}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBS"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 組回傳財金Response電文
	 * 
	 * @return
	 * 
	 */
	@SuppressWarnings("unused")
	private FEPReturnCode PrepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		if (strFISCRc != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getLogContext().setProgramName(ProgramName);
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
		}

		rtnCode = getFiscBusiness().prepareHeader("0210");
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		}

		rtnCode = prepareBody();

		rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			getFiscRes().setBitMapConfiguration("0000000000000000");
		}

		rtnCode = getFiscRes().makeFISCMsg();

		return rtnCode;
	}

	/**
	 * 組財金電文Body部份
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_BITMAP = null;

		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				// +REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else {
				// -REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}

			// 依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				if (wk_BITMAP.charAt(i) == '1') {
					switch (i) {
						case 2: // 交易金額
							getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString());
							break;
						case 5: // 代付單位 CD/ATM 代號
							getFiscRes().setATMNO(getFiscBusiness().getFeptxn().getFeptxnAtmno());
							break;
						case 6: // 可用餘額
							getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						case 14: // 跨行手續費
							getFiscRes().setFeeAmt(String.valueOf(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().doubleValue()));
							break;
						case 21: // 促銷訊息
							getFiscRes().setPromMsg("");
							Sysconf defSYSCONF = new Sysconf();
							SysconfExtMapper dbSYSCONF = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
							defSYSCONF.setSysconfSubsysno((short) 3);
							defSYSCONF.setSysconfName("AD");
							defSYSCONF = dbSYSCONF.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
							if (defSYSCONF != null) {
								if (StringUtils.isNotBlank(defSYSCONF.getSysconfValue())) {
									getFiscRes().setPromMsg(defSYSCONF.getSysconfValue());
								}
							}
							break;
					}
				}
			}

			// 產生 MAC
			RefString mac = new RefString(getFiscRes().getMAC());
			rtnCode = encHelper.makeFISCICMAC(getFiscRes().getMessageType(), mac);
			getFiscRes().setMAC(mac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscRes().setMAC("00000000");
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 組回傳財金Response電文
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		if (strFISCRc != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getLogContext().setProgramName(ProgramName);
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
		}

		rtnCode = getFiscBusiness().prepareHeader("0210");
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		}

		rtnCode = prepareBody();

		rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			getFiscRes().setBitMapConfiguration("0000000000000000");
		}

		rtnCode = getFiscRes().makeFISCMsg();

		return rtnCode;
	}

	/**
	 * UpdateTxData部份
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
				} else {// (2 way)
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
				}

				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					isEC = false;
					getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
				}
			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R");// 拒絕
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response

			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文，人工處理
				transactionManager.rollback(txStatus);
				return rtnCode;
			}

			// 判斷是否需更新 ICTLTXN
			if (isIC) {
				// defICTLTXN.ICTLTXN_TX_CUR_ACT = fiscBusiness.FepTxn.FEPTXN_TX_CUR_ACT
				// defICTLTXN.ICTLTXN_TX_AMT_ACT = fiscBusiness.FepTxn.FEPTXN_TX_AMT_ACT
				// defICTLTXN.ICTLTXN_EXRATE = fiscBusiness.FepTxn.FEPTXN_EXRATE
				defICTLTXN.setIctltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
				defICTLTXN.setIctltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
				defICTLTXN.setIctltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defICTLTXN.setIctltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				defICTLTXN.setIctltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
				if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
					defICTLTXN.setIctltxnOriStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
				}
				if (dbICTLTXN.updateByPrimaryKeySelective(defICTLTXN) < 1) {// 若更新失敗則不送回應電文，人工處理
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}

			// (3) 判斷是否需更新原始交易 for 2572/2546
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				if (getFiscBusiness().getOriginalFEPTxn() != null) {
					oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData"));
					if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("D");
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTraceEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());

						if (isIC) {// 國際卡交易需同時更新ICTLTXN
							oriICTLTXN.setIctltxnTxrust("D");
							if (dbICTLTXN.updateByPrimaryKeySelective(oriICTLTXN) < 1) {// 若更新失敗則不送回應電文，人工處理
								transactionManager.rollback(txStatus);
								return IOReturnCode.UpdateFail;
							}
						}

						if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
							// 若更新失敗則不送回應電文, 人工處理
							transactionManager.rollback(txStatus);
							return IOReturnCode.FEPTXNUpdateError;
						}
					} else {// -REP
							// 授權交易需先上主機解圏, 若解圏成功則 TXRUST =“C”
							// 所以若TXRUST =“T”進行中, 即可將原交易之狀態改回 Active
						if ("T".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// 進行中for沖銷
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 將原始交易之狀態改為Active
							if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
								// 若更新失敗則不送回應電文，人工處理
								transactionManager.rollback(txStatus);
								return IOReturnCode.FEPTXNUpdateError;
							}
						}
					}
				}
			}

			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 更新跨行代收付
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processICAptot(isEC);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}
}
