package com.syscom.fep.server.aa.ims;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;

public class CBSRequestI extends INBKAABase{
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private String rtnMessage = StringUtils.EMPTY;
	private boolean isPrepareFEPTXN = true;
	
	public CBSRequestI(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData(){
		try {
			//記錄LOG
			this.logContext.setProgramFlowType(ProgramFlow.AAIn);
			this.logContext.setMessageFlowType(MessageFlow.Request);
			this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
			this.logContext.setMessage(this.getTxData().getTxRequestMessage());
			this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
			logMessage(this.logContext);
			
			// 1. 	判斷FEPTXN記錄是否存在
			rtnCode = checkPrepareFEPTXN();

			// 2.AddTxData:新增交易記錄(FEPTXN)
			if (isPrepareFEPTXN && rtnCode == FEPReturnCode.Normal) {
				//(1) 	Prepare() 交易記錄初始資料
				rtnCode = getFiscBusiness().prepareFEPTXN();
				
				if (rtnCode == FEPReturnCode.Normal) {
					//(2) 	新增交易記錄
					getFiscBusiness().getFeptxn().setFeptxnMsgflow("CR");
					getFiscBusiness().getFeptxn().setFeptxnCbsProc("Y");
					rtnCode = getFiscBusiness().insertFEPTxn();
					
					if (rtnCode == CommonReturnCode.Normal) {
						//(3) 	產生 MAC
						String mac = getFiscReq().getMAC();
						RefString refMac = new RefString(mac);
						rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(getFiscReq().getMessageType(), refMac);
						getFiscReq().setMAC(refMac.get());
						if (rtnCode != CommonReturnCode.Normal) {
			                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			            }
						
					}
					
				}
				
			}
			
			// 3. 	更新交易記錄(FEPTXN)
			if (rtnCode == FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);
				getFiscBusiness().getFeptxn().setFeptxnFiscTimeout((short)1);
				getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
				getFiscBusiness().getFeptxn().setFeptxnPending((short)1);
				getFiscBusiness().getFeptxn().setFeptxnTxrust("S");
				rtnCode = getFiscBusiness().updateTxData();
			}
			
			// 4. SendToFISC送電文到財金
			if (rtnCode == FEPReturnCode.Normal) {
				rtnCode = getFiscBusiness().sendToFISCFromCBS();
				
				if (rtnCode == FEPReturnCode.Normal) {
					//拆回傳電文
					rtnCode = getFiscBusiness().checkBitmapFromCBS(getFiscRes(), getFiscRes().getAPData());
					
					this.logContext.setMessage("after checkBitmapFromCBS RC:" + rtnCode.toString());
			        logMessage(this.logContext);
			        
					// 5. 將財金回覆電文傳回主機
					if (rtnCode == FEPReturnCode.Normal) {
						rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
								.checkFiscMac(getFiscRes().getMessageType(), getFiscRes().getMAC());
						
						this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
				        logMessage(this.logContext);
					}
					char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(this.getFiscRes().getBitMapConfiguration(), 16, 2, 64).toCharArray();
					if (bitMapFromFisc[63] == '1') {
							String mMac;
							if (rtnCode == FEPReturnCode.Normal) {
								mMac = "0000";
							} else {
								mMac = "0302";
							}
							
							String mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
							String apData = this.getFiscRes().getAPData();
							apData = apData.substring(0, (apData.length() - 8)) + mac;
							this.getFiscRes().setAPData(apData);
							getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscRes().getResponseCode());
							this.getFiscRes().makeFISCMsg();
					}
					rtnMessage = this.getFiscRes().getFISCMessage();
				}
				
			}

			//6. 更新 FEPTXN
			rtnCode = updateFEPTXN();
			
		} catch (Exception e) {
			this.rtnCode = CommonReturnCode.ProgramException;
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(this.logContext);
		} finally {
			this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			this.getTxData().getLogContext().setMessage(rtnMessage);
			this.getTxData().getLogContext().setProgramName(this.aaName);
			this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, this.logContext);
		}
		
		return rtnMessage;
	}
	
	private FEPReturnCode checkPrepareFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		Calendar txDate = CalendarUtil.rocStringToADDate("0" + getFiscReq().getTxnInitiateDateAndTime().substring(0, 6)); // 民國轉成西元年
		if (txDate == null) {
			rtnCode = getFiscBusiness().checkBitmapFromCBS(getFiscReq(), getFiscReq().getAPData());
		}else {
			String date = FormatUtil.dateTimeFormat(txDate, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
			String bkno = getFiscReq().getTxnSourceInstituteId().substring(0, 3);
			String stan = getFiscReq().getSystemTraceAuditNo();
			feptxn = feptxnDao.getFEPTXNByReqDateAndStan(date, bkno, stan);
			if(feptxn != null) {
				getFiscBusiness().setFeptxn(feptxn);
				isPrepareFEPTXN = false;
				this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkPrepareFEPTXN"));
				this.logContext.setMessage("The Feptxn Exist, date = " + date + ", bkno = " + bkno + ", stan = " + stan);
				logMessage(this.logContext);
			}else {
				rtnCode = getFiscBusiness().checkBitmapFromCBS(getFiscReq(), getFiscReq().getAPData());
			}
		}
		
		return rtnCode;
	}

	//7. 更新 FEPTXN
	private FEPReturnCode updateFEPTXN() {
		getFiscBusiness().getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
		getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			if (rtnCode == FEPReturnCode.Normal ) {
				if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); /* 拒絕-正常 */
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
				}
			}
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /* AA Close */
			rtnCode = getFiscBusiness().updateTxData(); 
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateTxData");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
