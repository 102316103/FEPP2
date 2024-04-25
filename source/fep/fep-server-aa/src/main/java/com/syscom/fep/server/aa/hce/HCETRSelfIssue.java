package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

/**
 * @author Jaime
 */
public class HCETRSelfIssue extends ATMPAABase {
	private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;

	public HCETRSelfIssue(HCEData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() throws Exception {
		String rtnMessage = "";
		try {
			// 1. Prepare():記錄MessageText & 準備回覆電文資料
			rtnCode = getATMBusiness().hce_PrepareFEPTxn();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				sendEMS(getLogContext());
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 2. AddTxData: 新增交易記錄(FEPTxn)
				addTxData(); // 新增交易記錄(FEPTxn)
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 3. CheckBusinessRule: 商業邏輯檢核
				RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
				rtnCode = getATMBusiness().checkRequestFromOtherChannel(getmHCEtxData(), tita.getINTIME());
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 4. SendToCBS:送往CBS主機處理
				this.sendToCBS();
			}

			// 5. UpdateTxData: 更新交易記錄(FEPTxn)
			this.updateTxData();

		} catch (Exception ex) {
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		} finally {
			if (StringUtils.isBlank(getmHCEtxData().getTxResponseMessage())) {
//				6. 	組HCE回應電文 & 回 HCEMsgHandler
//			       電文內容格式請參照: SEND_HCE_GeneralTrans_RS 
				rtnMessage = getATMBusiness().prepareHCEResponseData(tota);
			} else {
				rtnMessage = getmHCEtxData().getTxResponseMessage();
			}
			// 7. 交易通知 (if need)
			this.sendToMailHunter();

			//7.	寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
			if("Y".equals(feptxn.getFeptxnSend2160()) && "000".equals(feptxn.getFeptxnCbsRc())) {
				 /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
				rtnCode = getATMBusiness().prepareInbk2160();
			}else if("A".equals(feptxn.getFeptxnSend2160())) {
				/*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
		        rtnCode = getATMBusiness().prepareInbk2160();
			}
			// else if("000".equals(feptxn.getFeptxnCbsRc()) ) {
			// 	/*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
			// 	/*該判斷僅在開發套測試，佈版至其它套需拿掉*/
			// 	LogData logData = new LogData();
			// 	logData.setProgramFlowType(ProgramFlow.AAOut);
			// 	logData.setMessageFlowType(MessageFlow.Response);
			// 	logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
			// 	logData.setMessage("Do INBK2160 Strat");
			// 	logMessage(Level.DEBUG, logData);
			//
			// 	rtnCode = getATMBusiness().prepareInbk2160();
			//
			// 	logData.setProgramFlowType(ProgramFlow.AAOut);
			// 	logData.setMessageFlowType(MessageFlow.Response);
			// 	logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
			// 	logData.setMessage("Do INBK2160 End");
			// 	logMessage(Level.DEBUG, logData);
			// }
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				sendEMS(getLogContext());
			}
			getmHCEtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getmHCEtxData().getLogContext().setMessage(rtnMessage);
			getmHCEtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getmHCEtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		}
		return rtnMessage;
	}
	private void sendToMailHunter() {
		try {
			String noticeType = feptxn.getFeptxnNoticeType();
			if (StringUtils.isNotBlank(noticeType) && "4001".equals(feptxn.getFeptxnRepRc())&& "4001".equals(feptxn.getFeptxnConRc())) {
				switch (noticeType) {
					case "P": /* 送推播 */
						getATMBusiness().preparePush(this.feptxn);
						break;
					case "M": /* 簡訊 */
						getATMBusiness().prepareSms(this.feptxn);
						break;
					case "E": /* Email */
						getATMBusiness().prepareMail(this.feptxn);
						break;
				}
			}
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
			sendEMS(this.logContext);
		}
	}
	/**
	 * 2. AddTxData: 新增交易記錄( FEPTxn)
	 * 
	 * @return
	 * @throws Exception
	 */
	private void addTxData() throws Exception {
		try {
			// 新增交易記錄(FEPTxn) Returning FEPReturnCode
			/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
			if (StringUtils.isNotBlank(this.feptxn.getFeptxnTelephone())){
				String tel = this.feptxn.getFeptxnTelephone().substring(this.feptxn.getFeptxnTelephone().length() - 10);
				this.feptxn.setFeptxnTelephone(tel);
			}
			int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
			if (insertCount <= 0) { // 新增失敗
				rtnCode = FEPReturnCode.FEPTXNInsertError;
			}
		} catch (Exception ex) { // 新增失敗
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".addTxData");
			sendEMS(getLogContext());
			rtnCode = FEPReturnCode.FEPTXNInsertError;
		}
	}

	/**
	 * 4. SendToCBS:送往CBS主機處理
	 * 
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
		/* 交易記帳處理 */
		String AATxTYPE = "1"; // 上CBS入扣帳
		String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
		feptxn.setFeptxnCbsTxCode(AA);
		ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
		rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
		tota = hostAA.getTota();
	}

	/**
	 * 5. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void updateTxData() {
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
		if(rtnCode != FEPReturnCode.Normal){
			feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
					getmHCEtxData().getLogContext()));
			feptxn.setFeptxnErrMsg(msgfileExtMapper.selectByMsgfileErrorcode(feptxn.getFeptxnReplyCode()).get(0).getMsgfileShortmsg());
		}else{
			feptxn.setFeptxnReplyCode("    ");
		}

		feptxn.setFeptxnAaRc(rtnCode.getValue());
		feptxn.setFeptxnAaComplete((short) 1); /* AA Complete */
		/* For報表, 寫入處理結果 */
		if (rtnCode == FEPReturnCode.Normal) {
			if (feptxn.getFeptxnWay() == 3) {
				feptxn.setFeptxnTxrust("B"); /* 處理結果=Pending */
			} else {
				feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
			}
		} else {
			feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
		}

		// 回寫 FEPTXN
		/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
		FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
			String tel = this.feptxn.getFeptxnTelephone().substring(this.feptxn.getFeptxnTelephone().length() - 10);
			this.feptxn.setFeptxnTelephone(tel);
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateTxData");
			sendEMS(getLogContext());
		}

		if (rtnCode2 != FEPReturnCode.Normal) {
			// 回寫檔案 (FEPTxn) 發生錯誤
			this.feptxn.setFeptxnReplyCode("L013");
			sendEMS(getLogContext());
		}
	}
}
