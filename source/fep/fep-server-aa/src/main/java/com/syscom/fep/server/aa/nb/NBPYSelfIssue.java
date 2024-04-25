package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;

/**
 * @author Jaime
 */
public class NBPYSelfIssue extends ATMPAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;

	public NBPYSelfIssue(NBData txnData) throws Exception {
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
			rtnCode = getATMBusiness().nb_PrepareFEPTxn();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				sendEMS(getLogContext());
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 2. AddTxData: 新增交易記錄(FEPTxn)
				this.addTxData(); // 新增交易記錄(FEPTxn)
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 3. CheckBusinessRule: 商業邏輯檢核
                RCV_NB_GeneralTrans_RQ tita = this.getmNBReq();
                rtnCode = getATMBusiness().checkRequestFromOtherChannel(getmNBtxData(), tita.getBody().getRq().getSvcRq().getINTIME());
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 4. SendToCBS:送往CBS主機處理
				this.sendToCBS();
			}

			// 5. UpdateTxData: 更新交易記錄(FEPTxn)
			this.updateTxData();

			// 6.Response:組ATM回應電文 & 回 ATMMsgHandler
			rtnMessage = getATMBusiness().prepareNBResponseData(tota);

			// 7. 交易通知 (if need)
			this.sendToMailHunter();

			//8. 交易結束通知主機(By PCODE)
			this.transactionCloseConnect();

		} catch (Exception ex) {
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		} finally {

			getmNBtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getmNBtxData().getLogContext().setMessage(rtnMessage);
			getmNBtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getmNBtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		}
		return rtnMessage;
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
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
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
        String AA = getmNBtxData().getMsgCtl().getMsgctlTwcbstxid();
		feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmNBtxData());
        rtnCode = new CBS(hostAA, getmNBtxData()).sendToCBS(AATxTYPE);
		tota = hostAA.getTota();
		String charge = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
		String brch =  this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
		String chargeFlag =  this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
		if(StringUtils.isNotBlank(charge)) {
        	BigDecimal hostCharge = new BigDecimal(charge);
			getmNBtxData().setCharge(hostCharge);
        }
		getmNBtxData().setBrch(brch);
		getmNBtxData().setChargeFlag(chargeFlag);
	}

	/**
	 * 5. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void updateTxData() {
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
		feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FEP,
				getmNBtxData().getLogContext()));
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
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
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

	/**
	 * 7. 交易通知 (if need)
	 */
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
	 * 8. 交易結束通知主機(By PCODE)
	 */
	public void transactionCloseConnect() {
		try {
			String AATxTYPE = ""; //不需提供此值
			String AATxRs = "N";//不需等待主機回應
			String AA = getmNBtxData().getMsgCtl().getMsgctlTwcbstxid1();
			writeLog("AA Namee : " + AA, ProgramFlow.AAIn);
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmNBtxData());
			rtnCode = new CBS(hostAA, getmNBtxData()).sendToCBS(AATxTYPE, AATxRs);
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".transactionCloseConnect"));
			sendEMS(this.logContext);
		}
	}

	public void writeLog(String msg, ProgramFlow flow) {
		LogData logData = new LogData();
		logData.setProgramFlowType(flow);
		logData.setMessageFlowType(MessageFlow.Request);
		logData.setProgramName(ProgramName);
		logData.setMessage(msg);
		// logData.setRemark("MBService Receive Request");
		logData.setServiceUrl("/mb/recv");
		logData.setEj(TxHelper.generateEj());
		this.logMessage(logData);
	}
}
