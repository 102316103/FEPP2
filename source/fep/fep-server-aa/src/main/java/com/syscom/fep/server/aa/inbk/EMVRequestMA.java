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
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.mapper.HotbinMapper;
import com.syscom.fep.mybatis.mapper.HotcardMapper;
import com.syscom.fep.mybatis.model.Hotbin;
import com.syscom.fep.mybatis.model.Hotcard;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.ATMAdapter;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.TXCUR;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.T24TxType;

/**
 * @author Richard
 */
public class EMVRequestMA extends INBKAABase {
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	String rtnMessage = StringUtils.EMPTY;
	private Intltxn intlTxn = new Intltxn(); // 國際卡檔
	private Intltxn oriintlTxn = new Intltxn(); // 國際卡檔
	@SuppressWarnings("unused")
	private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);
	@SuppressWarnings("unused")
	private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
	private boolean needResponseMsg = true;
	
	// AA的建構式,在這邊初始化及設定其他相關變數
	// @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	// 初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	// ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	// FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	public EMVRequestMA(ATMData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			getFiscBusiness().getFISCTxData().setFiscTeleType(FISCSubSystem.EMVIC);
			// 1. 準備FEP交易記錄檔
			_rtnCode = getATMBusiness().prepareFEPTXN();

			// 2. 新增FEP交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getATMBusiness().addTXData();
			}

			// 3. 商業邏輯檢核(ATM電文)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = checkBusinessRule();
			}

			// 4. 組送 FISC 之 Request 電文並等待財金之 Response 電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendEMVRequestToFISC(getATMRequest());
			}

			// 5. 檢核 FISC 之 Response電文是否正確
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().checkEMVResponseMessage();
			}

			if (_rtnCode == CommonReturnCode.Normal && (
					NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc())
					)){
				// 2016/07/20 Modify by Ruling for 財金回應電文銀聯要檢查IC_CHECKDATA；VISA、MASTER要檢查IC_CHECKRESULT
				if (FISCPCode.PCode2600.getValueStr().equals(getFeptxn().getFeptxnPcode())
				|| FISCPCode.PCode2601.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
					// 銀聯檢查BITMAP63
					if (StringUtils.isBlank(getFiscEMVICRes().getIcCheckdata())){
						logContext.setRemark("財金REP未包含BITMAP63(IC_CHECKDATA)!!");
						logMessage(Level.INFO,logContext);
					}
				}
				else{
					// VISA、MASTER檢查BITMAP60
					if (StringUtils.isBlank(getFiscEMVICRes().getIcCheckresult())){
						logContext.setRemark("財金REP未包含BITMAP60(IC_CHECKRESULT)!!");
						logMessage(Level.INFO,logContext);
					}
				}
			}

			// 6. Prepare國際卡交易(INTLTXN)記錄(if need)
			// 當財金回-response時，ORI_DATA的值為０，為避免電文內的日期轉換有誤，多加fisc reprc=4001的條件成立才組Prepare Intltxn
			if ((NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))
			&& StringUtils.isNotBlank(getFiscEMVICRes().getOriData())) {
				RefBase<Intltxn> intltxnRefBase = new RefBase<>(intlTxn);
				RefBase<Intltxn> oriintltxnRefBase = new RefBase<>(oriintlTxn);
				_rtnCode = getFiscBusiness().prepareIntltxnEMV(intltxnRefBase, oriintltxnRefBase, MessageFlow.Response);
				intlTxn = intltxnRefBase.get();
				oriintlTxn = oriintltxnRefBase.get();
			}

			// 7. Fly 2017/5/16 (週二) 上午 11:28 修改 for  拒絕 EMV Mastero卡交易
			if (FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode())
					|| FISCPCode.PCode2632.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
				if ((NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))
						&& getFiscEMVICRes().getNetworkCode().trim().length() >= 2
						&& "MS".equals(getFiscEMVICRes().getNetworkCode().substring(0,2)))
				{
					logContext.setRemark( "此筆交易為MAESTRO交易，要回財金(-)CON，RC=0601");
					logMessage(Level.INFO,logContext);
					_rtnCode = FEPReturnCode.FISCTimeout;
				}
			}

			// 8. ProcessAPTOT:更新跨行代收付
			if (_rtnCode == CommonReturnCode.Normal
					&& (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))
					&& DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())){
				_rtnCode = getFiscBusiness().processAptot(false);

			}

			// 9. 帳務主機處理：代理提款-進帳務主機掛現金帳
			if (_rtnCode == CommonReturnCode.Normal
					&& (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))){
				_rtnCode = sendToCBS();
			}

			// 10. 組回應電文回給 ATM&判斷是否需組 CON 電文回財金/本行轉入交易掛帳
			_rtnCode2 = sendConfirm();

			// 11.新增國際卡交易(INTLTXN)記錄(if need)
			if ((NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))
					&& StringUtils.isNotBlank(getFiscEMVICRes().getOriData())){
				intlTxn.setIntltxnTxrust(getFeptxn().getFeptxnTxrust());
				_rtnCode2 = getFiscBusiness().insertINTLTxn(intlTxn);
			}

		} catch (Exception ex) {
			rtnMessage = "";
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "processRequestData");
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
			getLogContext().setProgramName(this.aaName);
			logMessage(Level.DEBUG, getLogContext());
		}
		// 先組回應ATM 故最後return空字串
		return rtnMessage;
	}

	// 商業邏輯檢核
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtn = FEPReturnCode.Normal;
		try {
			rtn =  checkRequestFromATM(getATMtxData());
			if (rtn != FEPReturnCode.Normal){
				return rtn;
			}

			// Fly 2016/08/26 修改 for 檢核ATM電文 POSENTRYMODE
			//--ben-20220922-//if (StringUtils.isBlank(getATMRequest().getPOSENTRYMOD())){
			//--ben-20220922-//	logContext.setRemark( "ATM上來的POSENTRYMOD值為空白");
			//--ben-20220922-//	logMessage(Level.INFO,logContext);
			//--ben-20220922-//	return FEPReturnCode.OtherCheckError;
			//--ben-20220922-//}

			// Fly 2017/4/14 (週五) 下午 04:43 修改 for ATM防呆處理
			//--ben-20220922-//logContext.setRemark( "ATM_EMV[" + getATMBusiness().getAtmStr().getAtmEmv() + "]" + "POSENTRYMOD[" + getATMRequest().getPOSENTRYMOD() + "]");
			logMessage(Level.INFO,logContext);

			//--ben-20220922-//if (DbHelper.toBoolean(getATMBusiness().getAtmStr().getAtmEmv()) && "2901".equals(getATMRequest().getPOSENTRYMOD())){
			//--ben-20220922-//	logContext.setRemark( "已升級EMV機台, POSENTRYMOD不能送 2901");
			//--ben-20220922-//	logMessage(Level.INFO,logContext);
			//--ben-20220922-//	return FEPReturnCode.OtherCheckError;
			//--ben-20220922-//}

			if (!SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(getATMtxData().getTbsdyFISC())){
				rtn = CommonReturnCode.FISCBusinessDateChanged;
				logContext.setRemark( "EMVIssueRequestA-Main：AA起始讀進的SYSSTAT_TBSDY_FISC <> 現在的SYSSTAT_TBSDY_FISC，" +
						"AA起始讀進的SYSSTAT_TBSDY_FISC=" + getATMtxData().getTbsdyFISC() + "，" +
						"現在的SYSSTAT_TBSDY_FISC=" + SysStatus.getPropertyValue().getSysstatTbsdyFisc());
				logMessage(Level.INFO,logContext);
			}else{
				rtn = checkHotBin();
				if (rtn == ATMReturnCode.Track2Error){
					return rtn;
				}
				// 2017/12/15 Fly 增加檢核CheckHotBin的ReturnCode
				if (rtn == CommonReturnCode.Normal){
					rtn = checkEMV();
				}
				if (rtn == CommonReturnCode.Normal){
					rtn = checkEMVMLimit();
				}
			}
			return rtn;
		} catch (Exception ex) {
			// 異常時要Return ProgramException
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	// 檢核偽卡資料
	private FEPReturnCode checkHotBin() {
		String pan = "";
		String bin = "";
		@SuppressWarnings("unused")
		Hotbin defHOTBIN = new Hotbin();
		HotbinMapper dbHOTBIN = SpringBeanFactoryUtil.getBean(HotbinMapper.class);
		@SuppressWarnings("unused")
		Hotcard defHOTCARD = new Hotcard();
		HotcardMapper dbHOTCARD = SpringBeanFactoryUtil.getBean(HotcardMapper.class);
		try{
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrk2())){
				if (!getFeptxn().getFeptxnTrk2().contains("=")){
					return ATMReturnCode.Track2Error;
				}
				pan = getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")).trim();
				if (pan.length() < 6){
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					logContext.setRemark( "TRK2取=前的長度不能小於6位，值為=" + pan);
					logMessage(Level.INFO,logContext);
					return ATMReturnCode.Track2Error;
				}

				bin = pan.substring(0,6);

				// 檢核HOTCARD
				if (dbHOTCARD.selectByPrimaryKey(pan) != null){
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					logContext.setRemark( "在HOTCARD檔有找到資料，PAN_NO=" + pan);
					logMessage(Level.INFO,logContext);
					return ATMReturnCode.Track2Error;
				}

				// 檢核HOTBIN
				if (dbHOTBIN.selectByPrimaryKey(bin) != null){
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					logContext.setRemark( "在HOTBIN檔有找到資料，BIN_NO=" + bin);
					logMessage(Level.INFO,logContext);
					return ATMReturnCode.Track2Error;
				}

				// Fly 2017/11/27 增加防呆處理, 避免手續費記號不相符, 影響帳務
				if (FISCPCode.PCode2620.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
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
					//--ben-20220922-//if (("N".equals(CHARGEFG) && "Y".equals(getATMRequest().getACFEE()))
					//--ben-20220922-//		|| ("Y".equals(CHARGEFG) && !"Y".equals(getATMRequest().getACFEE()) && !"N".equals(getATMRequest().getACFEE()))) {
					//--ben-20220922-//	getLogContext().setRemark("ATM送來的手續費記號[" + getATMRequest().getACFEE() + "] 與FEP查詢的手續費記號[" + CHARGEFG + "] 不一致");
					//--ben-20220922-//	logMessage(getLogContext());
					//--ben-20220922-//	return ATMReturnCode.OtherCheckError;
					//--ben-20220922-//}
				}

			}
			return FEPReturnCode.Normal;
		}catch (Exception e){
			// 2014/10/06 Modify by Ruling for 前端的ErrorCode仍是Normal，要改用Return
			// _rtnCode = CommonReturnCode.ProgramException
			getLogContext().setProgramException(e);
			getLogContext().setProgramName(ProgramName + ".CheckHotBin");
			sendEMS(getLogContext());
			// 2014/10/06 Modify by Ruling for 前端的ErrorCode仍是Normal，要改用Return
			return CommonReturnCode.ProgramException;
		}
	}

	// 檢核EMV晶片卡拒絶磁條卡提款或預借現金交易
	private FEPReturnCode checkEMV() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try{
			if (FISCPCode.PCode2620.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
				//--ben-20220922-//if ("N".equals(getATMRequest().getACFEE())){
					// 客戶不同意收取處理費，交易拒絕
				//--ben-20220922-//	getLogContext().setRemark("CheckEMV-客戶不同意收取處理費交易拒絕(E928)，ATMRequest.ACFEE=" + getATMRequest().getACFEE());
				//--ben-20220922-//	logMessage(getLogContext());
				//--ben-20220922-//	return ATMReturnCode.ACFeeReject;
				//--ben-20220922-//}
				//--ben-20220922-//else if ("Y".equals(getATMRequest().getACFEE())){
					// 客戶已同意收取處理費
				//--ben-20220922-//	rtnCode = checkACFEE();
				//--ben-20220922-//	if (rtnCode != CommonReturnCode.Normal){
				//--ben-20220922-//		return rtnCode;
				//--ben-20220922-//	}
				//--ben-20220922-//}
			}
			return rtnCode;
		}catch (Exception ex){
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 收取EMV晶片卡處理費
	private FEPReturnCode checkACFEE() {
		SysconfExtMapper dbSYSCONF = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Sysconf defSYSCONF = dbSYSCONF.selectByPrimaryKey((short)1,  "EMVACFeeFlag");
		try{
			if (defSYSCONF == null){
				rtnCode = ATMReturnCode.OtherCheckError;
				getLogContext().setRemark("CheckACFEE-在SYSCONF檔找不到EMVACFeeFlag的值");
				logMessage(getLogContext());
				return rtnCode;
			}
			else{
				if ("Y".equals(defSYSCONF.getSysconfValue())) {
					getFeptxn().setFeptxnRsCode("Y");
					getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(INBKConfig.getInstance().getEMVAccessFee()));
				} else {
					rtnCode = ATMReturnCode.OtherCheckError;
					getLogContext().setRemark("CheckACFEE-在SYSCONF檔的EMVACFeeFlag其值不等於Y");
					logMessage(getLogContext());
					return rtnCode;
				}
			}
			return rtnCode;

		}catch (Exception ex){
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// EMV檢核限額
	private FEPReturnCode checkEMVMLimit() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		@SuppressWarnings("unused")
		String trk2SCode = "";

		try{
			// Fly 2016/04/07 應永豐要求取消銀聯卡(2600)月限額
			if (FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode())
			|| FISCPCode.PCode2620.getValueStr().equals(getFeptxn().getFeptxnPcode())
			|| FISCPCode.PCode2632.getValueStr().equals(getFeptxn().getFeptxnPcode())
			|| FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode())){
				String serviceCode = StringUtils.rightPad(getATMBusiness().getFeptxn().getFeptxnTrk2(),40," ")
						.substring(getATMBusiness().getFeptxn().getFeptxnTrk2().indexOf("=")+5,getATMBusiness().getFeptxn().getFeptxnTrk2().indexOf("=")+6);
				if (!DbHelper.toBoolean(getATMBusiness().getATMMSTR().getAtmEmv()) && ("2".equals(serviceCode) || "6".equals(serviceCode))){
					getLogContext().setRemark("CheckEMVMLimit(EMV檢核限額)-需檢核  ATM_EMV:[" + getATMBusiness().getATMMSTR().getAtmEmv() + "] serviceCode:[" + serviceCode + "]");
					logMessage(getLogContext());
					rtnCode = getFiscBusiness().checkEMVMLimit();
					if (rtnCode != CommonReturnCode.Normal){
						return rtnCode;
					}
				}
				else{
					getLogContext().setRemark("CheckEMVMLimit(EMV檢核限額)-不需檢核  ATM_EMV:[" + getATMBusiness().getATMMSTR().getAtmEmv() + "] serviceCode:[" + serviceCode + "]");
					logMessage(getLogContext());
					return rtnCode;
				}
			}
			return rtnCode;
		}catch (Exception ex){
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 帳務主機處理：代理提款-進帳務主機掛現金帳
	private FEPReturnCode sendToCBS() {
		T24 hostT24 = new T24(getATMtxData());
		try {
			if (getATMtxData().getMsgCtl().getMsgctlCbsFlag() == 1 && StringUtils.isBlank(getFeptxn().getFeptxnBkno())){
				_rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(),T24TxType.Accounting.getValue(),true);
				if (_rtnCode != CommonReturnCode.Normal){
					if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())){
						_rtnCode2 = getFiscBusiness().processAptot(true);
					}
					return _rtnCode;
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName);
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	// 判斷是否需組 CON 電文回財金
	// 判斷欄位是否有值,必須用Not IsNullorEmpty(Item)判斷,之後用Not IsNullorEmpty(Item.Trim)
	private FEPReturnCode sendConfirm() {
		@SuppressWarnings("unused")
		T24 hostT24 = new T24(getATMtxData());
		@SuppressWarnings("unused")
		Credit hostCredit = new Credit(getATMtxData());
		try {
			if (_rtnCode == FEPReturnCode.Normal && (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc()))) {
				getFeptxn().setFeptxnReplyCode("    ");
				if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlAtm2way())){
					// 3WAY(代理提款-ATM)
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending);
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
					getATMBusiness().updateTxData();
					sendToATM();
				}
				else{
					// 2WAY
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);// 成功
					getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
					if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlFisc2way())){
						getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
						getATMBusiness().updateTxData();
						sendToATM();
					}
				}

				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())){
					// 寫入 ATM 清算資料
					_rtnCode2 = getATMBusiness().insertATMC(1);

					// 持符合EMV規格晶片卡跨國交易寫入EMV限額檔
					// Fly 2016/04/07 應永豐要求取消銀聯卡(2600)月限額
					if ((FISCPCode.PCode2620.getValueStr().equals(getFeptxn().getFeptxnPcode())
							|| FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode())
							|| FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode())
							|| FISCPCode.PCode2622.getValueStr().equals(getFeptxn().getFeptxnPcode()))
							&& !TXCUR.TWD.equals(getATMBusiness().getFeptxn().getFeptxnTxrust())){
						String serviceCode = StringUtils.rightPad(getATMBusiness().getFeptxn().getFeptxnTrk2(),40," ")
								.substring(getATMBusiness().getFeptxn().getFeptxnTrk2().indexOf("=")+5,getATMBusiness().getFeptxn().getFeptxnTrk2().indexOf("=")+6);
						if (!DbHelper.toBoolean(getATMBusiness().getATMMSTR().getAtmEmv()) && ("2".equals(serviceCode) || "6".equals(serviceCode))){
							_rtnCode = getATMBusiness().insertEMVC(1);
							if (_rtnCode != CommonReturnCode.Normal){
								getATMBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
							}
						}
					}
				}

				// 更新 ATM 鈔匣資
				if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAtmcash())){
					_rtnCode2 = getATMBusiness().updateATMCash(1);
				}
			} else {
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())
						&& StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc().trim())) {
					getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
					if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) || "4007".equals(getFeptxn().getFeptxnRepRc())){
						// +REP
						if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlFisc2way())){ // 3WAY
							getLogContext().setProgramName(ProgramName);
							getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()),FEPChannel.FEP, FEPChannel.FISC,getLogContext()));
							getLogContext().setProgramName(ProgramName);
							// 將ReturnCode轉成前端對應通道,但不可以使用Feptxn_Channel,因為forT24使用會有變動,所以要改用AData資料(ATMtxData)
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()),FEPChannel.FEP, getATMtxData().getTxChannel(),getLogContext()));
							// Accept-Reverse
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse);
							// FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
							_rtnCode2 = getFiscBusiness().sendConfirmToFISCEMV();
						}
						else{ // 2WAY
							// 財金2WAY交易(IIQ)，如檢核 KEY SYNC 錯誤，仍要回給 ATM 錯誤代碼
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
							getLogContext().setProgramName(ProgramName);
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()),FEPChannel.FEP, getATMtxData().getTxChannel(),getLogContext()));

						}
					}
					else{ // -REP
						// 將ReturnCode轉成前端對應通道,但不可以使用Feptxn_Channel,因為forT24使用會有變動,所以要改用AData資料(ATMtxData)
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(),FEPChannel.FISC, getATMtxData().getTxChannel(),getLogContext()));
					}
				} else {// fepReturnCode <> Normal
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
					if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())){
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()),FEPChannel.FEP, getATMtxData().getTxChannel(),getLogContext()));
					}
				}
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
				getFiscBusiness().updateTxData();
				sendToATM();

			}
			return FEPReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "sendToConfirm");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	// 更新交易記錄檔
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = null;

		if (_rtnCode != CommonReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		} else if (_rtnCode2 != CommonReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
		} else {
			getFeptxn().setFeptxnAaRc(CommonReturnCode.Normal.getValue());
		}

		getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

		rtnCode = getFiscBusiness().updateTxData();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		return CommonReturnCode.Normal;
	}

	// 組回應電文
	private FEPReturnCode sendToATM() {
		ATMAdapter oatmAdapter = new ATMAdapter(getATMtxData());
		@SuppressWarnings("unused")
		FEPReturnCode rtncode = null;
		try {
			// 先送給ATM主機
			//--ben-20220922-//oatmAdapter.setAtmNo(getATMRequest().getBRNO() + getATMRequest().getWSNO());
			if (StringUtils.isBlank(getATMtxData().getTxResponseMessage()) && needResponseMsg) {
				rtnMessage = prepareATMResponseData();
			} else {
				rtnMessage = getATMtxData().getTxResponseMessage();
			}

			if (FEPChannel.ATM.toString().equals(getFeptxn().getFeptxnChannel())
					|| FEPChannel.WEBATM.toString().equals(getFeptxn().getFeptxnChannel())) {
				if (StringUtils.isNotBlank(rtnMessage.trim())) {
					oatmAdapter.setMessageToATM(rtnMessage);
					rtncode = oatmAdapter.sendReceive();
				} else {
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
			getLogContext().setProgramName(ProgramName + "." + "sendToATM");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}
}
