package com.syscom.fep.server.aa.ims;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;

public class CBSConfirmI extends INBKAABase{
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private String rtnMessage = StringUtils.EMPTY;
	
	public CBSConfirmI(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData(){

		try {

			// 3.產生財金 MAC
			getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscCon().getResponseCode());
			getFiscBusiness().getFeptxn().setFeptxnCbsRc(this.getFiscCon().getResponseCode());
			/* 11/16 修改, 收到財金確認電文時間寫入FEPTXN */
			getFiscBusiness().getFeptxn().setFeptxnConTxTime(
					FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			String conMac = getFiscCon().getMAC();
			RefString refMac = new RefString(conMac);
			rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(getFiscCon().getMessageType(), refMac);
			getFiscReq().setMAC(refMac.get());
			if (rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			}

			// 4.SendToFISC送電文到財金
			if (rtnCode == FEPReturnCode.Normal) {
				rtnCode = getFiscBusiness().sendToFISCFromCBS();
			}

			// 5.UpdateTxData:更新交易記錄 (FEPTXN)
			if (rtnCode == FEPReturnCode.Normal) {
				rtnCode = this.updateTxData();
				if (rtnCode != FEPReturnCode.Normal) {
					return rtnMessage;
				}
			}

			// 6.label_END_OF_FUNC:
			rtnCode = this.updateFEPTXN();

		} catch (Exception e) {
			this.rtnCode = CommonReturnCode.ProgramException;
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(this.logContext);
		} finally {
			this.logContext.setProgramFlowType(ProgramFlow.AAOut);
			this.logContext.setMessage(rtnMessage);
			this.logContext.setProgramName(this.aaName);
			this.logContext.setMessageFlowType(MessageFlow.Response);
			this.logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this.rtnCode, this.logContext));
			logMessage(Level.DEBUG, this.logContext);
		}
		
		return rtnMessage;
	}

	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		
		//(1) 	檢核 Mapping 欄位 
		String aa = (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6), 7, "0")))
					+ this.getFiscCon().getTxnInitiateDateAndTime().substring(6, 12);//轉西元年
		if (!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
			(!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0, 3))) ||
				(StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))
				|| (StringUtils.isNotBlank(this.getFiscCon().getTxAmt()) && !new DecimalFormat("0.00").format(getFiscBusiness().getFeptxn().getFeptxnTxAmt()).equals(new DecimalFormat("0.00").format(new BigDecimal(this.getFiscCon().getTxAmt()))))) {
			return FEPReturnCode.OriginalMessageDataError;
		}
		
		// (2) 檢核交易是否未完成
		if (!getFiscBusiness().getFeptxn().getFeptxnTxrust().equals("B")) {
			/* 10/20 修改, 財金錯誤代碼改為 ‘0101’ */
			return FEPReturnCode.MessageFormatError; // 11011 **相關欄位檢查錯誤
		}
		/* 9/22 修改 for CON 送2次 */
		if (!getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().equals(0)) {
			this.getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
			return FEPReturnCode.MessageFormatError; // 11011 **相關欄位檢查錯誤
		}
		
		return rtnCode;
	}

	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)0);
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
				if (NormalRC.FISC_ATM_OK.equals(getFiscCon().getResponseCode())) {  /*+CON*/
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); /*成功*/
					String balb = getFiscCon().getBALB();
					String bala = getFiscCon().getBALA();
					if(StringUtils.isNoneBlank(balb)) {
						getFiscBusiness().getFeptxn().setFeptxnBalb(new BigDecimal(balb));
					}
					if(StringUtils.isNoneBlank(bala)) {
						getFiscBusiness().getFeptxn().setFeptxnBala(new BigDecimal(bala));
					}
				} else {
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse);  /*Accept-Reverse*/
				}
				getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); /*解除 PENDING */
			}
			
			getFiscBusiness().getFeptxn().setFeptxnMsgflow("CF");  /*CBS CONFIRM TO FISC*/
			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());
			
			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

		} catch (Exception ex) {
			logContext.setProgramException(ex);
			logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(logContext);
			return CommonReturnCode.ProgramException;
		}
		
		return rtnCode;
	}
	
	private FEPReturnCode updateFEPTXN() throws Exception {
		if(getFiscBusiness().getFeptxn().getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
			if (this.rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(this.rtnCode.getValue());
			}
		}
		
		getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)1); /*AA Close*/
		if (this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn()) > 0) {
			return FEPReturnCode.Normal;
		}
		
		return FEPReturnCode.UpdateFail;
	}
}
