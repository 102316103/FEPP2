package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Objects;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
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
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.FeptxnMapper;
import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.mapper.QrptxnMapper;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.mybatis.model.Qrptxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.Dapp;
import com.syscom.fep.server.common.business.host.HK;
import com.syscom.fep.server.common.business.host.MO;
import com.syscom.fep.server.common.business.host.Ncnb;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.CBSHostType;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.DAPPAppMsg;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動的跨行轉帳確認電文
 * 
 * @author vincent
 *
 */
public class IFTConfirmI extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode1 = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal; //for主流程第三點更新交易記錄(FEPTXN & VATXN)，儲存更新失敗的FEPReturnCode
	private boolean isExitProgram = false;

	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 * @throws Exception
	 */
	public IFTConfirmI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.拆解並檢核財金電文
			_rtnCode = processRequest();
			if(_rtnCode != CommonReturnCode.Normal){
				return StringUtils.EMPTY;
			}

			//2.商業邏輯檢核＆電文Body檢核
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode= checkBusinessRule();
				if(isExitProgram){
					return StringUtils.EMPTY;
				}
			}

			//3.更新交易記錄 (FEPTXN & VATXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = updateTxData();
				if(_rtnCode != CommonReturnCode.Normal){
					return StringUtils.EMPTY;
				}
			}

			// 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processAPTOTSendToCBSASC();
			}

			// 6.更新交易記錄
			if (getFiscBusiness().getFeptxn() != null) {
				_rtnCode = updateFEPTXN();
			}

			// 7.發送推播或簡訊或Email
			_rtnCode =sendToMailHunter();

			//8.判斷是否需傳送2160電文給財金
			if(StringUtils.isNotBlank(feptxn.getFeptxnSend2160()) && "Y".equals(feptxn.getFeptxnSend2160())){
				_rtnCode=insertINBK2160();
			}
			//9.FEP通知主機交易結束
			_rtnCode = SendToCBS();
		} catch (Exception e) {
			this._rtnCode = CommonReturnCode.ProgramException;
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(this.logContext);
		}
		return StringUtils.EMPTY;
	}
	private FEPReturnCode SendToCBS() throws Exception {
		FEPReturnCode rc2 = CommonReturnCode.Normal;
		/*沖轉主機帳務*/
		String AATxTYPE = "";
		String AATxRs = "N";
		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
		ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
		rc2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE,AATxRs);
		return rc2;
	}
	private FEPReturnCode insertINBK2160() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			//檢核Header
			rtnCode = getFiscBusiness().prepareInbk2160();
			if(rtnCode != CommonReturnCode.Normal){
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setRemark("寫入檔案發生錯誤!!");
				logMessage(Level.INFO, getLogContext());
				return FEPReturnCode.INBK2160InsertError;
			}else{
				return FEPReturnCode.Normal;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequest");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
	// ''' <summary>
	// ''' "拆解並檢核由財金發動的Request電文"
	// ''' </summary>
	// ''' <modify>
	// ''' <modifier>HusanYin</modifier>
	// ''' <reason>修正txData的feptxn與fiscbusiness同步問題</reason>
	// ''' <date>2010/12/07</date>
	// ''' </modify>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode processRequest() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 檢核Header
			rtnCode = getFiscBusiness().checkHeader(getFiscCon(), true);
			if (rtnCode == FEPReturnCode.MessageTypeError
					|| rtnCode == FEPReturnCode.TraceNumberDuplicate
					|| rtnCode == FEPReturnCode.OriginalMessageError
					|| rtnCode == FEPReturnCode.STANError
					|| rtnCode == FEPReturnCode.CheckBitMapError
					|| rtnCode == FEPReturnCode.SenderIdError) {
				// 'modified by Maxine on 2011/09/19 for 查不到原始資料則不更新FEPTXN
				getFiscBusiness().setFeptxn(null);
				getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
				return rtnCode;
			}
			getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
			getTxData().setFeptxn(getFiscBusiness().getFeptxn());
		} catch (Exception e) {
			this._rtnCode = CommonReturnCode.ProgramException;
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequest"));
			sendEMS(this.logContext);
		}
		return CommonReturnCode.Normal;
	}

	// ''' <summary>
	// ''' "商業邏輯檢核,電文Body檢核"
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_TX_DATE = "";

		try {
			//檢核 Mapping 欄位
			if (Integer.parseInt(this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 2)) < 90) {
				wk_TX_DATE = CalendarUtil.rocStringToADString("20110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
			} else {
				wk_TX_DATE = CalendarUtil.rocStringToADString("19110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
			}
			/* 9/9 修改, 改抓 FEPTXN_REQ_DATETIME欄位 */
			String aa = (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6), 7, "0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6, 12);
			if (!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
					(!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0, 3))) ||
					(StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
					|| (StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmt()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
				return FEPReturnCode.OriginalMessageDataError;
			}

			//檢核交易是否未完成
			if (!getFiscBusiness().getFeptxn().getFeptxnTxrust().equals("B")) {
				/* 10/20 修改, 財金錯誤代碼改為 ‘0101’ */
				return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
			}

			/* 9/22 修改 for CON 送2次 */
			if (!getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().equals(0)) {
				this.getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
				isExitProgram = true;
				return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
			}

			//檢核 MAC
			getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscCon().getResponseCode());
			/* 11/16 修改, 收到財金確認電文時間寫入FEPTXN */
			getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			//註解合庫開發環境暫時不測試
			//			rtnCode = encHelper.checkFiscMac(this.getFiscCon().getMessageType(), getFiscCon().getMAC());
//			this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
//			logMessage(this.logContext);
//			if (rtnCode != FEPReturnCode.Normal) {
//				this.getFeptxn().setFeptxnConRc(null);
//				return FEPReturnCode.ENCCheckMACError;//**訊息押碼錯誤
//			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// '''<summary>
	// ''' UpdateTxData部份
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <modify>
	// ''' <modifier>Husan</modifier>
	// ''' <reason>connie spec change
	// ''' 刪除欄位
	// ''' 1. FEPTXN_REQ_TIME
	// ''' 2. FEPTXN_REP_TIME
	// ''' 3. FEPTXN_CON_TIME
	// ''' </reason>
	// ''' <date>2010/11/05</date>
	// ''' <modifier>HusanYin</modifier>
	// ''' <reason>修正Const RC</reason>
	// ''' <date>2010/11/25</date>
	// ''' </modify>
	private FEPReturnCode updateTxData() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (NormalRC.FISC_ATM_OK.equals(getFiscCon().getResponseCode())) {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
				} else {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); // 'Accept-Reverse
				}
				getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // 'F3
			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());

			this.feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData")); // 2021-06-16 Richard add

			// 'add by maxine on 2012/03/05 for 換日後fiscBusiness.DBFEPTXN,以及txData.FEPTXN都應該使用實際交易日期,避免後面更新不到FEPTXN
			getTxData().setFeptxnDao(this.feptxnDao);
			getFiscBusiness().setFeptxnDao(this.feptxnDao);

			int i = this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn());
			if (i <= 0) {
				transactionManager.rollback(txStatus);
				_rtnCode = IOReturnCode.FEPTXNUpdateError;
				return _rtnCode;
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			// '若失敗則復原
			transactionManager.rollback(txStatus);
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode processAPTOTSendToCBSASC() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String AATxTYPE = "";
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /* +REP */
			if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) { /* -CON */
				/*沖轉跨行代收付*/
				rtnCode = getFiscBusiness().processAptot(true);
				this.logContext.setProgramName(ProgramName);
				/*沖轉主機帳務*/
				if(getFiscBusiness().getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno()))
				{
					AATxTYPE="2";
					String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
					ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
					_rtnCode2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE);
				}
			} else {  /* +CON */
				/* 轉入行為本行, 送主機入帳 */
				//20220927 Add 2522,2524 + Confirm 也要送CBS
				if(!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())
						&& SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())){
					AATxTYPE="1";
					String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
					ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
					_rtnCode2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE);
				}
				if(SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())
						&&("2522".equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) || "2524".equals(getFiscBusiness().getFeptxn().getFeptxnPcode()))){
					AATxTYPE="6";
					String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
					ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
					_rtnCode2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE);
				}
			}
		}
		return rtnCode;
	}

	// 更新FEPTXN
	private FEPReturnCode updateFEPTXN() throws Exception {
		if (getFiscBusiness().getFeptxn().getFeptxnAaRc().equals(FEPReturnCode.Normal.getValue())) {
			if (this._rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode.getValue());
			} else if (this._rtnCode2 != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode2.getValue());
			}
		}
		getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
		if (this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn()) > 0) {
			return FEPReturnCode.Normal;
		}
		return FEPReturnCode.UpdateFail;
	}

	/**
	 * 發送推播或簡訊或Email
	 */
	private FEPReturnCode sendToMailHunter() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			if (getFiscBusiness().getFeptxn() != null
					&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
					&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
				switch(getFiscBusiness().getFeptxn().getFeptxnNoticeType()){
					case "P" : /* 送推播 */
						getFiscBusiness().preparePush(getFiscBusiness().getFeptxn());
						break;
					case "M": /* 簡訊 */
						getFiscBusiness().prepareSms(getFiscBusiness().getFeptxn());
						break;
					case "E": /* Email */
						getFiscBusiness().prepareMail(getFiscBusiness().getFeptxn());
						break;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
			sendEMS(this.logContext);
			return FEPReturnCode.Abnormal;
		}
	}
}
