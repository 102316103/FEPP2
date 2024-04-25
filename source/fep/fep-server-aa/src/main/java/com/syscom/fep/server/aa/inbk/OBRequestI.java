package com.syscom.fep.server.aa.inbk;

import java.util.Calendar;

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
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.ObtltxnExtMapper;
import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.T24Version;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.T24TxType;

/**
 * 負責處理財金發動的跨境電子支付交易Req電文
 * @author Richard	--> Ben 
 */
public class OBRequestI extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode rc1 = CommonReturnCode.Normal;
	private FEPReturnCode rc2 = CommonReturnCode.Normal;
	private FEPReturnCode rc3 = CommonReturnCode.Normal;
	private FEPReturnCode rc4 = CommonReturnCode.Normal;
	private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
	private boolean isExitProgram = false;
	private boolean isOB = false;
	private boolean isEC = false;
	private Obtltxn defOBTLTXN = new Obtltxn();
	private Obtltxn oriOBTLTXN = new Obtltxn();
	private ObtltxnExtMapper dbOBTLTXN = SpringBeanFactoryUtil.getBean(ObtltxnExtMapper.class);;
	private String oriTXRUST; // for 2556 保留原OBTLTXN的交易狀態
	private boolean isQRPMain = false; // 豐錢包主掃交易
	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");

	public OBRequestI(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			//1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
			rc1 = ProcessRequestHeader();
			//程式結束
			if(isExitProgram) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc1, getFiscReq());
				getLogContext().setMessage(rc1.toString());
				getLogContext().setRemark("檢核財金電文Header有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	
			}
			//2.AddTxData:新增交易記錄(FEPTXN & OBTLTXN)
			//2.1 Prepare() 交易記錄初始資料
			rc2 = this.prePare();	
			//程式結束
			if(isExitProgram) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc2, getFiscReq());
				getLogContext().setMessage(rc2.toString());
				getLogContext().setRemark("交易記錄初始資料有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	
			}
			//2.2 以 TRANSACTION 新增交易記錄
			rc3 = addTxData();
			//程式結束
			if(isExitProgram) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc3, getFiscReq());
				getLogContext().setMessage(rc3.toString());
				getLogContext().setRemark("新增交易記錄有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	
			}
			
			//2.3 判斷 CheckHeader 之 RC, 若有誤則不繼續執行
			if (CommonReturnCode.Normal.equals(rc1) && CommonReturnCode.Normal.equals(rc2)) {
				//3.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核 
				rc1 = this.checkBusinessRule();
				if (CommonReturnCode.Normal.equals(rc1)) {
					//4.SendToCBS: 帳務主機處理
				    /*若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗
						則仍需組回應電文給財金 */
					rc1 = this.sendToCBSAndAsc();
					//程式結束
					if(isExitProgram) {
						getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc1, getFiscReq());
						getLogContext().setMessage(rc1.toString());
						getLogContext().setRemark("sendToCBS有誤!!");
						logMessage(Level.INFO, getLogContext());
						return StringUtils.EMPTY;	
					}
				}
			}
			//6.PrepareFISC:準備回財金的相關資料
			//6.1 判斷 RC1,RC2,RC3
			if (rc1 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc1.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc1, FEPChannel.FISC, getLogContext()));
				}
			}else if (rc2 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc2.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc2, FEPChannel.FISC, getLogContext()));
				}
			}else if (rc3 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc3.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc3, FEPChannel.FISC, getLogContext()));
				}
			}else {
				getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);		 /*+REP*/
			}
			//8/17 退款交易之原帳戶已銷戶/關閉，仍應回覆財金交易成功
			if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())
					&& INBKConfig.getInstance().getOBRT24ErrCode().equals(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
				getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
				rc1 = CommonReturnCode.Normal;
			}
			//6.2 產生 Response 電文Header:
			rc4 = getFiscBusiness().prepareHeader("0210");
			if (rc4 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc4.getValue());
			}
			//6.3 產生 Response 電文Body:
			//6.4 產生 MAC
			this.prepareBody();
			//6.5 產生Bit Map
			rc4 = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
			this.logContext.setMessage("after makeBitmap RC:" + rc4.toString());
			logMessage(this.logContext);
			if (rc4 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc4.getValue());
				getFiscRes().setBitMapConfiguration("0000000000000000");
			}
			this.getFiscRes().makeFISCMsg();

			//7.UpdateTxData: 更新交易記錄(FEPTXN & OBTLTXN)
			rc1 = this.updateTxData();
			//程式結束
			if(isExitProgram) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc1, getFiscReq());
				getLogContext().setMessage(rc1.toString());
				getLogContext().setRemark("更新交易記錄有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	
			}
			//8.ProcessAPTOT:更新跨行代收付
			this.processAPTOT();

			//9.SendToFISC送回覆電文到財金
			getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

			//10.判斷是否需傳送2160電文給財金
        	if(StringUtils.isNotBlank(feptxn.getFeptxnSend2160())
				&& ("A".equals(feptxn.getFeptxnSend2160())|| "Y".equals(feptxn.getFeptxnSend2160()))) {
				/* 寫入2160發送資料檔 */
				this.insertINBK2160();
			}

		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("FiscResponse:"+getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		} 

		return "";
	}

	/**
	 * 1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
	 * @return
	 */
	private FEPReturnCode ProcessRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
		//1.1 檢核財金電文 Header
		if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
				|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
			getFiscBusiness().setFeptxn(null);
			isExitProgram = true;
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
			return rtnCode;
		}
		//1.2 判斷是否為晶片金融卡跨境電子支付交易
		//OB_DATA(Bitmap 36) 必須有值
		if (StringUtils.isNotBlank(getFiscReq().getOriData())) {
			isOB = true;
		}
		return rtnCode;
	}
	
	/**
	 * 2.1 	Prepare() 交易記錄初始資料
	 */
	private FEPReturnCode prePare() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		rtnCode = getFiscBusiness().prepareFEPTXN();
		if (rtnCode != CommonReturnCode.Normal) {
			isExitProgram = true;
			return rtnCode;
		}
		RefBase<Obtltxn> defOBTLTXNBase = new RefBase<>(defOBTLTXN);
		RefBase<Obtltxn> oriOBTLTXNBase = new RefBase<>(oriOBTLTXN);
		rtnCode = getFiscBusiness().prepareObtltxn(defOBTLTXNBase, oriOBTLTXNBase, MessageFlow.Request);
		defOBTLTXN = defOBTLTXNBase.get();
		oriOBTLTXN = oriOBTLTXNBase.get();
		if (rtnCode != CommonReturnCode.Normal) {
			getLogContext().setRemark("PrepareObtltxn-收到財金發動之跨境電子支付在準備OBTLTXN發生異常");
			logMessage(Level.INFO, getLogContext());
			getFiscBusiness().getFeptxn().setFeptxnRepRc(FISCReturnCode.MessageFormatError.toString());		//0101 
			getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // R 拒絶
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
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {

			rtnCode = getFiscBusiness().insertFEPTxn();
			if (rtnCode != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				isExitProgram = true;
				return rtnCode;
			}
			//if (isOB) {
			if (dbOBTLTXN.insertSelective(defOBTLTXN) < 1) {
				transactionManager.rollback(txStatus);
				isExitProgram = true;
				return IOReturnCode.UpdateFail;
			}
			//}

			transactionManager.commit(txStatus);
			return rtnCode;
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
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			//3.1 檢核單筆限額
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if (rtnCode != CommonReturnCode.Normal) {
					getLogContext().setRemark("超過單筆限額");
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
			}
			//3.2 檢核財金MAC
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
			if (CommonReturnCode.Normal.equals(rtnCode)) {
				//3.3 檢核&更新原始交易狀態 FOR 2556
				if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
					rtnCode = checkoriOBTLTXN();
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}
			}

			return rtnCode;
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
	private FEPReturnCode checkoriOBTLTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			oriOBTLTXN = new Obtltxn();
			oriOBTLTXN.setObtltxnTbsdyFisc(getFiscBusiness().getFeptxn().getFeptxnDueDate());
			oriOBTLTXN.setObtltxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
			oriOBTLTXN.setObtltxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
			Obtltxn obtltxn = dbOBTLTXN.queryOBTLXNByStan(oriOBTLTXN.getObtltxnTbsdyFisc(), oriOBTLTXN.getObtltxnBkno(), oriOBTLTXN.getObtltxnStan());
			if (obtltxn == null) {
				rtnCode = FEPReturnCode.CheckFieldError;
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
				getLogContext().setRemark(StringUtils.join(
						"QueryOBTLXNByStan 找不到原交易, OBTLTXN_TBSDY_FISC=", oriOBTLTXN.getObtltxnTbsdyFisc(),
						", OBTLTXN_BKNO=", oriOBTLTXN.getObtltxnBkno(),
						",  FEPTXN_ORI_STAN=", oriOBTLTXN.getObtltxnStan()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				transactionManager.commit(txStatus);
				return rtnCode;
			}
			oriOBTLTXN = obtltxn;
			// 檢核原交易是否成功
			if (!FeptxnTxrust.Successed.equals(oriOBTLTXN.getObtltxnTxrust())
					&& !FeptxnTxrust.Pending.equals(oriOBTLTXN.getObtltxnTxrust())
					&& !FeptxnTxrust.ReverseSuccessed.equals(oriOBTLTXN.getObtltxnTxrust())) {
				rtnCode = FEPReturnCode.CheckFieldError; // 交易狀態有誤
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.OriTxnRejected); // I：原交易已拒絕
				// 2019/06/24 Modify by Ruling for 修改Log說明避免發生Exception
				getLogContext().setRemark(StringUtils.join("原交易不成功(A or B or D), FEPTXN_TXRUST=", oriOBTLTXN.getObtltxnTxrust()));
				// LogContext.Remark = String.Format("原交易不成功(A or B or D), FEPTXN_TXRUST={0}", fiscBusiness.OriginalFEPTxn.FEPTXN_TXRUST)
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				transactionManager.commit(txStatus);
				return rtnCode;
			} else {
				oriTXRUST = oriOBTLTXN.getObtltxnTxrust(); // 保留原交易狀態
			}
			// 比對原交易資料
			if (!oriOBTLTXN.getObtltxnMerchantId().equals(defOBTLTXN.getObtltxnMerchantId())
					|| !oriOBTLTXN.getObtltxnSetCur().equals(defOBTLTXN.getObtltxnSetCur())
					|| !oriOBTLTXN.getObtltxnSetExrate().equals(defOBTLTXN.getObtltxnSetExrate())
					|| !oriOBTLTXN.getObtltxnTbsdyFisc().equals(defOBTLTXN.getObtltxnOriTxDate())
					|| !oriOBTLTXN.getObtltxnOrderNo().equals(defOBTLTXN.getObtltxnOriOrderNo())
					|| !FISCPCode.PCode2555.getValueStr().equals(oriOBTLTXN.getObtltxnPcode())) {
				rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
				getLogContext().setRemark(StringUtils.join(
						"與原交易欄位不相同, MERCHANT_ID=", defOBTLTXN.getObtltxnMerchantId(),
						" 原MERCHANT_ID=", oriOBTLTXN.getObtltxnMerchantId(),
						", SET_CUR=", defOBTLTXN.getObtltxnSetCur(),
						" 原SET_CUR=", oriOBTLTXN.getObtltxnSetCur(),
						", SET_EXRATE=", defOBTLTXN.getObtltxnSetExrate(),
						" 原SET_EXRATE=", oriOBTLTXN.getObtltxnSetExrate(),
						", TX_DATE=", defOBTLTXN.getObtltxnOriTxDate(),
						" 原TX_DATE=", oriOBTLTXN.getObtltxnTbsdyFisc(),
						", ORDER_NO=", defOBTLTXN.getObtltxnOriOrderNo(),
						" 原ORDER_NO=", oriOBTLTXN.getObtltxnOrderNo(),
						", 原PCODE <> 2555, 原PCODE=", oriOBTLTXN.getObtltxnPcode()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				transactionManager.commit(txStatus);
				return rtnCode;
			}
			// 比對退貨日期-交易日期 > 360天
			RefBase<Calendar> txdate = new RefBase<>(null);
			RefBase<Calendar> orTxdate = new RefBase<>(null);
			if (!CalendarUtil.validateDateTime(defOBTLTXN.getObtltxnTxDatetimeFisc().substring(0,8), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN, txdate)) {
				rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
				getLogContext().setRemark(StringUtils.join("比對退貨期限-交易日，轉換日期格式有誤, txdate:", defOBTLTXN.getObtltxnTbsdyFisc()));
				logMessage(Level.INFO, getLogContext());
				transactionManager.commit(txStatus);
				return rtnCode;
			}
			if (!CalendarUtil.validateDateTime(defOBTLTXN.getObtltxnTxDatetimeFisc().substring(0,8), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN, orTxdate)) {
				rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
				getLogContext().setRemark(StringUtils.join("比對退貨期限-原交易日，轉換日期格式有誤, orTxdate:", oriOBTLTXN.getObtltxnTbsdyFisc()));
				logMessage(Level.INFO, getLogContext());
				transactionManager.commit(txStatus);
				return rtnCode;
			}
			if (CalendarUtil.getDayPeriod(txdate.get(), orTxdate.get()) > 360) {
				rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
				getLogContext().setRemark("退貨日期-交易日期 > 360天");
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				transactionManager.commit(txStatus);
				return rtnCode;
			}
			// 比對退貨金額是否大於原交易金額
			if (defOBTLTXN.getObtltxnTotTwdAmt().doubleValue() > (oriOBTLTXN.getObtltxnTotTwdAmt().doubleValue() - oriOBTLTXN.getObtltxnTotRetAmt().doubleValue())) {
				rtnCode = FEPReturnCode.CheckFieldError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // N：無帳務沖正
				getLogContext().setRemark("退貨金額大於原交易金額");
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				transactionManager.commit(txStatus);
				return rtnCode;
			}
			oriOBTLTXN.setObtltxnTxrust(FeptxnTxrust.Processing); // T：沖銷或授權完成進行中
			dbOBTLTXN.updateByPrimaryKeySelective(oriOBTLTXN);
			transactionManager.commit(txStatus);
			getFiscBusiness().getFeptxn().setFeptxnTroutActno(oriOBTLTXN.getObtltxnTroutActno());
			getFiscBusiness().getFeptxn().setFeptxnTroutKind(oriOBTLTXN.getObtltxnTroutKind());
			getFiscBusiness().getFeptxn().setFeptxnMajorActno(oriOBTLTXN.getObtltxnMajorActno());
			getFiscBusiness().getFeptxn().setFeptxnCardSeq(oriOBTLTXN.getObtltxnCardSeq());
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkoriOBTLTXN"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 帳務主機處理(SendToCBS/ASC(if need))
	 */
	private FEPReturnCode sendToCBSAndAsc() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String TxType = "";		//上CBS入扣帳
		try {
			switch(getFiscBusiness().getFeptxn().getFeptxnPcode()){
				case "2555":
					TxType="1";
					break;
				case "2556":
					TxType="0";
					break;
				default:
					break;
			}
			//20221028一律使用MSGCTL_TWCBSTXID
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			this.getTxData().setObtlTxn(defOBTLTXN);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
			if (rtnCode != CommonReturnCode.Normal) {
				if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
					return rtnCode;
				} else {
					getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
					getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
					getFiscBusiness().updateTxData();
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 成功
						oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
					}
					isExitProgram = true;
					return rtnCode;
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBSAndAsc"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
	
	/**
	 * 帳務主機處理(SendToCBS/ASC(if need))
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode sendToCBSAndAscOLD() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
	
		T24 hostT24 = new T24(getTxData());
		byte TxType = 0;
		boolean ProcessTag = false;
		try {
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
				if (getTxData().getMsgCtl().getMsgctlCbsFlag() != 0) {
					TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().byteValue();
					ProcessTag = true;
				} else {
					TxType = 1;
					ProcessTag = false;
				}
				rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag);
				
				
				if (rtnCode != CommonReturnCode.Normal) {
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
						//strFISCRc = rtnCode;
						return rtnCode;
					} else {
						getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
						getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); // S Reject-abnormal
						getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
						getFiscBusiness().updateTxData();
						if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 成功
							oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
							// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
						}
						isExitProgram = true;
						return rtnCode;
					}
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBSAndAsc"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 首次身分驗證狀況(P33)
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode sendToCBSAndP33() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		T24 hostT24 = new T24(getTxData());
		try {
			// 2019/08/28 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：豐錢包APP主掃交易於商業邏輯檢核已有送B0001查詢ID及數位記號，不需重複送，非豐錢包APP主掃交易才要送主機查詢ID
			if (!isQRPMain) {
				// 送至T24主機取得身份證號
				rtnCode = hostT24.sendToT24(T24Version.B0001, (byte) T24TxType.Accounting.getValue(), true);
				if (rtnCode == CommonReturnCode.CBSResponseError) {
					rtnCode = CommonReturnCode.P33IssuerReject;
				}
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				if (getTxData().getT24Response() != null && getTxData().getT24Response().getTotaEnquiryContents().size() > 0) {
					if (getTxData().getT24Response().getTotaEnquiryContents().get(0).containsKey("IDNO")
							&& StringUtils.isNotBlank(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("IDNO"))) {
						getFiscBusiness().getFeptxn().setFeptxnIdno(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("IDNO"));
					} else {
						getLogContext().setRemark("SendToCBSAndP33-AA拆解T24下行電文, IDNO無此欄位或為空值");
						logMessage(Level.INFO, getLogContext());
						return CommonReturnCode.P33IssuerReject;
					}
				} else {
					getLogContext().setRemark("SendToCBSAndP33-AA收到NULL的T24下行電文");
					logMessage(Level.INFO, getLogContext());
					return CommonReturnCode.P33IssuerReject;
				}
			}

			// 執行首次身分驗證狀況(P33)
			rtnCode = getFiscBusiness().sendToP33();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBSAndP33"));
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
	 * 組財金電文Body部份
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String wk_BITMAP = null;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {// +REP
				// 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else {// -REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}

			for (int i = 2; i <= 63; i++) {
				if (wk_BITMAP.charAt(i) == '1') {
					switch (i) {
						case 2: // 交易金額
							getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString());
							break;
						case 5: // 代付單位 CD/ATM 代號
							getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
							break;
					}
				}
			}

			// 產生 MAC
			RefString refMac = new RefString(getFiscRes().getMAC());
			rtnCode = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
			getFiscRes().setMAC(refMac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscRes().setMAC("00000000");
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 7. 	UpdateTxData: 更新交易記錄(FEPTXN & OBTLTXN)
	 * (1) 	更新 FEPTXN
	 * (2) 	判斷是否需更新 OBTLTXN
	 * (3) 	判斷是否需更新原始交易  for 2556
	 */
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			// (1) 更新 FEPTXN
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); // B Pending
				} else {// (2 way)
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // A 成功
				}

				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					isEC = false;
					getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
				}
			} else if (FeptxnTxrust.Initial.equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // R 拒絕
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /// *AA Close*/

			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != CommonReturnCode.Normal) {
				// 若更新失敗則不送回應電文, 人工處理
				transactionManager.rollback(txStatus);
				isExitProgram = true;
				return rtnCode;
			}

			// (2) 判斷是否需更新 OBTLTXN
			if (isOB) {
				defOBTLTXN.setObtltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
				defOBTLTXN.setObtltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
				defOBTLTXN.setObtltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defOBTLTXN.setObtltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				defOBTLTXN.setObtltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
				defOBTLTXN.setObtltxnMajorActno(getFiscBusiness().getFeptxn().getFeptxnMajorActno());
				defOBTLTXN.setObtltxnCardSeq(getFiscBusiness().getFeptxn().getFeptxnCardSeq());
				defOBTLTXN.setObtltxnOriStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());

				if (dbOBTLTXN.updateByPrimaryKeySelective(defOBTLTXN) < 1) {
					// 若更新失敗則不送回應電文, 人工處理
					transactionManager.rollback(txStatus);
					getLogContext().setRemark("updateTxData-更新OBTLTXN失敗");
					logMessage(Level.INFO, getLogContext());
					isExitProgram = true;
					return IOReturnCode.UpdateFail;
				}
			}

			// (3) 判斷是否需更新原始交易 for 2556
			if (FISCPCode.PCode2556.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				if (oriOBTLTXN != null) {
					if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
						oriOBTLTXN.setObtltxnTxrust(FeptxnTxrust.ReverseSuccessed); // D 已沖正成功

						if (dbOBTLTXN.updateByPrimaryKeySelective(oriOBTLTXN) < 1) {
							// 若更新失敗則不送回應電文, 人工處理
							transactionManager.rollback(txStatus);
							getLogContext().setRemark("updateTxData-2556+Rep更新oriOBTLTXN失敗");
							logMessage(Level.INFO, getLogContext());
							isExitProgram = true;
							return IOReturnCode.UpdateFail;
						}
					} else {// -REP
						//授權交易需先上主機解圏, 若解圏成功則 TXRUST = "C",
				        //所以若TXRUST = "T"進行中, 即可將原交易之狀態改回 Active
						if (FeptxnTxrust.Processing.equals(oriOBTLTXN.getObtltxnTxrust())) {// T 進行中for沖銷
							oriOBTLTXN.setObtltxnTxrust(oriTXRUST); // 將原始交易之狀態還原
							if (dbOBTLTXN.updateByPrimaryKeySelective(oriOBTLTXN) < 1) {
								// 若更新失敗則不送回應電文，人工處理
								transactionManager.rollback(txStatus);
								getLogContext().setRemark("updateTxData-2556-Rep更新oriOBTLTXN失敗");
								logMessage(Level.INFO, getLogContext());
								isExitProgram = true;
								return IOReturnCode.UpdateFail;
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
	 */
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processOBAptot(isEC);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}
	
	  /**
     * 判斷是否需傳送2160電文給財金
     * @return
     */
    public FEPReturnCode insertINBK2160() {
    	FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			rtnCode = getATMBusiness().prepareInbk2160();
			if (rtnCode != CommonReturnCode.Normal) {
				if (rtnCode != CommonReturnCode.Normal) {
					getLogContext().setMessage(rtnCode.toString());
					getLogContext().setRemark("寫入檔案(INBK2160)發生錯誤!!");
					logMessage(Level.INFO, getLogContext());
					return FEPReturnCode.FEPTXNInsertError;
				} else {
					return FEPReturnCode.Normal;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".AddTXData");
			sendEMS(getLogContext());
		}
		return FEPReturnCode.Normal;
	}
}
