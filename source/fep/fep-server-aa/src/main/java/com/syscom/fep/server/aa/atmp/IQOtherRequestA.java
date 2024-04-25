package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APN;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * @author vincent
 */
public class IQOtherRequestA extends INBKAABase {
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
	private String tita ;
	private String atmno;

	public IQOtherRequestA(ATMData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() throws Exception {
		String rtnMessage = "";
		try {
			// 記錄FEPLOG內容
			tita = EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage());
			this.logContext.setProgramFlowType(ProgramFlow.AAIn);
			this.logContext.setMessageFlowType(MessageFlow.Request);
			this.logContext.setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
			this.logContext.setMessage("ASCII TITA:" + tita);
			this.logContext.setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
			logMessage(this.logContext);

			// 1. Prepare : 交易記錄初始資料
			rtnCode = getATMBusiness().prepareFEPTXN();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
				getLogContext().setMessage("FEPTXN PREPARE ERROR");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 2. AddTxData: 新增交易記錄(FEPTXN)
				this.addTxData();
				if (this.rtnCode != FEPReturnCode.Normal) {
					// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
					getLogContext().setProgramName(ProgramName + ".AddTxData");
					getLogContext().setMessage("FEPTXN ADD ERROR");
					sendEMS(getLogContext());
					return rtnMessage; // RETUEN 空字串，不回覆ATM
				}
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 3. CheckBusinessRule: 商業邏輯檢核
				this.checkBusinessRule();
			}

			FISC fiscBusiness = getFiscBusiness();
			if (rtnCode == FEPReturnCode.Normal) {
				// 4. 組送往 FISC 之 Request 電文並等待財金之 Response
				rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 5. CheckResponseFromFISC:檢核回應電文是否正確
				rtnCode = fiscBusiness.checkResponseMessage();
			}

			// 6. 更新交易紀錄(FEPTXN)
			this.updateTxData();

			// 7. 組 ATM 回應電文 & 回 ATMMsgHandler
			rtnMessage = this.response();
			if ("EAT".equals(feptxn.getFeptxnChannel())) {
				rtnMessage = this.eatmResponse(rtnMessage);
			}

		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		}
		try {
			getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getATMtxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
			getATMtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
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
			/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".addTxData"));
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
	 * 3. CheckBusinessRule: 商業邏輯檢核
	 *
	 * @return
	 * @throws Exception
	 */
	private void checkBusinessRule() throws Exception {
		//3.1 檢核ATM電文
		rtnCode = checkRequestFromATM(getATMtxData());
		
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO  6       /* 更新交易紀錄 */
		}
		
		//3.2 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError;
			return; // GO TO  6       /* 更新交易紀錄 */
		}
		
		String newMac = ATM_REQ_PICCMACD;

		this.logContext.setMessage("Begin checkAtmMac mac:" + newMac);

		// CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
		atmno = feptxn.getFeptxnAtmno();
		
		rtnCode = new ENCHelper(getATMtxData()).checkAtmMacNew(atmno, 
				StringUtils.substring(this.getATMtxData().getTxRequestMessage(), 36, 742), 
				newMac);

		this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
	}

	/**
	 * 7. 組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @throws Exception
	 */
	private String response() throws Exception {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			ATMGeneralRequest atmReq = this.getATMRequest();
			ENCHelper atmEncHelper = new ENCHelper(this.getATMtxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				ATM_FAA_CC1APC atm_faa_cc1apc = new ATM_FAA_CC1APC();
				// 組Header(OUTPUT-1)
				atm_faa_cc1apc.setWSID(atmReq.getWSID());
				atm_faa_cc1apc.setRECFMT("1");
				atm_faa_cc1apc.setMSGCAT("F");
				atm_faa_cc1apc.setMSGTYP("PC"); // - response
				atm_faa_cc1apc.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1apc.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1apc.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				atm_faa_cc1apc.setDATATYPE("D0");
				atm_faa_cc1apc.setDATALEN("004");
				atm_faa_cc1apc.setACKNOW("0");
				// 未上送CBS，給固定值：交易不能處理
				String pageNo = "";
				if("2999".equals(feptxn.getFeptxnReplyCode())) {
					pageNo = "226";
				}else if("    ".equals(feptxn.getFeptxnReplyCode())) {// 4個 SPACES
					pageNo = "000";
				}else {
					 //以FEPTXN_REPLY_CODE 轉換財金回應代碼
					String rc = TxHelper.getRCFromErrorCode(feptxn.getFeptxnReplyCode(), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext());
					if("2999".equals(rc)) {
						pageNo = "226";
					}else {
						pageNo = rc;
					}
				}
				
				atm_faa_cc1apc.setPAGENO(pageNo);
				
				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1apc.setPTYPE("S0");
				atm_faa_cc1apc.setPLEN("191");
				atm_faa_cc1apc.setPBMPNO("010000"); // FPC
				// 西元年轉民國年
				atm_faa_cc1apc.setPDATE(new SimpleDateFormat("yyy/MM/dd").format(new SimpleDateFormat("yyyMMdd").parse(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()))));
				atm_faa_cc1apc.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));

				atm_faa_cc1apc.setPTXTYPE("59"); // 不須上送主機，固定給值
				atm_faa_cc1apc.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1apc.setPTXAMT(FormatUtil.decimalFormat(BigDecimal.ZERO, "$#,##0")); // 不須上送主機，固定給值
				// 格式:$999 ex :$0
				atm_faa_cc1apc.setPFEE(FormatUtil.decimalFormat(BigDecimal.ZERO, "$#,##0")); // 不須上送主機，固定給值

				BigDecimal feptxnBalb = feptxn.getFeptxnBalb();
				// 格式 :正值放$,負值放-,$99,999,999,999.00(共18位) // 已確認是單純只總長度，後面轉字串會補滿18位，這裡不用處理
				// ex:$99,355,329;-33,123.00
				if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
					atm_faa_cc1apc.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
				} else {
					atm_faa_cc1apc.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
				}

				// 他行帳號:16位,第10~12位隱碼
				String feptxnTroutActno = this.setRangeCharAtStar(StringUtils.leftPad(feptxn.getFeptxnTroutActno(), 16), 9, 11);
				atm_faa_cc1apc.setPACCNO(feptxnTroutActno);
				atm_faa_cc1apc.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1apc.setPSTAN(feptxn.getFeptxnStan());
				//轉出行
				atm_faa_cc1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				atm_faa_cc1apc.setPRCCODE(feptxn.getFeptxnReplyCode());
				
				//取得原存行的促銷應用訊息
				atm_faa_cc1apc.setPARPC(feptxn.getFeptxnLuckyno());
				
				/* CALL ENC 取得MAC 資料 */
				rfs.set("");
				rtnMessage = atm_faa_cc1apc.makeMessage();
				
				 if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	                	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	                		+ atmReq.getPICCBI11() +  atmReq.getTRANSEQ()
	                		+ atmReq.getTDRSEG() + feptxn.getFeptxnRepRc()
	                		;
	                	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	                	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	                	logMessage(this.logContext);
				 }
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1apc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
					atm_faa_cc1apc.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
				rtnMessage = atm_faa_cc1apc.makeMessage();
			} else {
				ATM_FAA_CC1APN atm_faa_cc1apn = new ATM_FAA_CC1APN();
				// 組Header(OUTPUT-1)
				atm_faa_cc1apn.setWSID(atmReq.getWSID());
				atm_faa_cc1apn.setRECFMT("1");
				atm_faa_cc1apn.setMSGCAT("F");
				atm_faa_cc1apn.setMSGTYP("PN"); // + response
				atm_faa_cc1apn.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1apn.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1apn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1apn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1apn.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(024)
				atm_faa_cc1apn.setDATATYPE("D0");
				atm_faa_cc1apn.setDATALEN("024");
				atm_faa_cc1apn.setACKNOW("0");
				atm_faa_cc1apn.setPAGENO("030");
				BigDecimal feptxnBalb = feptxn.getFeptxnBalb();
				// 格式 :正值放$,負值放-,$99,999,999,999.00(共18位) // 已確認是單純只總長度，後面轉字串會補滿18位，這裡不用處理
				// ex:$99,355,329;-33,123.00
				if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
					atm_faa_cc1apn.setBALANCE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
					atm_faa_cc1apn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
				} else {
					atm_faa_cc1apn.setBALANCE(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
					atm_faa_cc1apn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
				}

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1apn.setPTYPE("S0");
				atm_faa_cc1apn.setPLEN("191");
				atm_faa_cc1apn.setPBMPNO("000010"); // FPN
				// 西元年轉民國年
				atm_faa_cc1apn.setPDATE(new SimpleDateFormat("yyy/MM/dd").format(new SimpleDateFormat("yyyMMdd").parse(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()))));
				atm_faa_cc1apn.setPTIME(new SimpleDateFormat(FormatUtil.FORMAT_TIME_HH_MM_SS).format(new SimpleDateFormat("HHmmss").parse(feptxn.getFeptxnTxTime())));

				atm_faa_cc1apn.setPTXTYPE("59"); // 不須上送主機，固定給值
				atm_faa_cc1apn.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1apn.setPTXAMT(FormatUtil.decimalFormat(BigDecimal.ZERO, "$#,##0")); // 不須上送主機，固定給值
				// 格式:$999 ex :$0
				atm_faa_cc1apn.setPFEE(FormatUtil.decimalFormat(BigDecimal.ZERO, "$#,##0")); // 不須上送主機，固定給值


				// 他行帳號:16位,第10~12位隱碼 ex：123456789***3456 (明細表顯示內容)
				String feptxnTroutActno = this.setRangeCharAtStar(StringUtils.leftPad(feptxn.getFeptxnTroutActno(), 16), 9, 11);
				atm_faa_cc1apn.setPACCNO(feptxnTroutActno);
				atm_faa_cc1apn.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1apn.setPSTAN(feptxn.getFeptxnStan());
				//轉出行
				atm_faa_cc1apn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				//取得原存行的促銷應用訊息
				atm_faa_cc1apn.setPARPC(feptxn.getFeptxnLuckyno());
				
				/* CALL ENC 取得MAC 資料 */
				rfs.set("");
				rtnMessage = atm_faa_cc1apn.makeMessage();
				
				 if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	                	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	                		+ atmReq.getPICCBI11() +  atmReq.getTRANSEQ()
	                		+ atmReq.getTDRSEG() + feptxn.getFeptxnRepRc()
	                		;
	                	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	                	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	                	logMessage(this.logContext);
				 }
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno,rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1apn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getATMtxData().getLogContext()));
					atm_faa_cc1apn.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1apn.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
				rtnMessage = atm_faa_cc1apn.makeMessage();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}

	/**
	 * 6. 更新交易紀錄(FEPTXN)
	 */
	private void updateTxData() {
		// ATM 4WAY，FISC 2WAY的交易
		if (rtnCode == FEPReturnCode.Normal  && NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnReplyCode())) {
			feptxn.setFeptxnReplyCode("    "); // 4個SPACES
			feptxn.setFeptxnTxrust("B");  /*Pending*/
		} else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {	
			feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
			feptxn.setFeptxnTxrust("R"); /*Reject-normal*/
			
			if ("4001".equals(feptxn.getFeptxnRepRc())) { /* +REP */
					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP,  FEPChannel.FISC, getATMtxData().getLogContext()));
			} else { /* -REP */
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP, FEPChannel.FISC, getATMtxData().getLogContext()));
			}
		} else {
			/* FEPReturnCode <> Normal */
			/* 2020/3/6 修改，主機有回應錯誤時，修改交易結果 */
			feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
			
			if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getATMtxData().getLogContext()));
			}
		}
		
		feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
		if (rtnCode != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode.getValue());
		} else {
			feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}
		
		feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
		rtnCode2 = this.updateFeptxn();
		
		if (rtnCode2 != FEPReturnCode.Normal) {
			// 回寫檔案 (FEPTxn) 發生錯誤
			this.feptxn.setFeptxnReplyCode("L013");
			sendEMS(getLogContext());
		}
	}


	/**
	 * 更新feptxn
	 *
	 * @return
	 */
	private FEPReturnCode updateFeptxn() {
		FEPReturnCode rtn = FEPReturnCode.Normal;
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFeptxn"));
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFeptxn");
			rtn = FEPReturnCode.FEPTXNUpdateError;
			sendEMS(getLogContext());
		}
		
		return rtn;
	}

	/**
	 * 字串指定區間的每個字元皆取代為'*'
	 *
	 * @param value
	 * @param starIndex 開始位置索引
	 * @param endIndex  結束位置索引
	 * @return
	 */
	private String setRangeCharAtStar(String value, int starIndex, int endIndex) {
		StringBuilder sb = new StringBuilder(value);
		for (int i = 9; i <= 11; i++) {
			sb.setCharAt(i, '*');
		}
		return sb.toString();
	}
	
	private String eatmResponse(String outdata) throws Exception {
		String rtnMessage = "" ;
		try {
			/* 組 ATM Response OUT-TEXT */
			RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getATMtxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();

			if (rtnCode != FEPReturnCode.Normal) {
				SEND_EATM_FAA_CC1APC rs = new SEND_EATM_FAA_CC1APC();
				SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body rsbody = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body();
				SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs();
				SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header();
				SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs();
				msgrs.setSvcRq(msgbody);
				msgrs.setHeader(header);
				rsbody.setRs(msgrs);
				rs.setBody(rsbody);
				header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
				header.setCHANNEL(feptxn.getFeptxnChannel());
				header.setMSGID(atmReqheader.getMSGID());
				header.setCLIENTDT(atmReqheader.getCLIENTDT());
				header.setSYSTEMID("FEP");
				if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
					header.setSTATUSCODE("4001");
				} else {
					header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
				}
				header.setSEVERITY("ERROR");
				msgbody.setOUTDATA(outdata);
				rtnMessage = XmlUtil.toXML(rs);
			} else {
				SEND_EATM_FAA_CC1APN rs = new SEND_EATM_FAA_CC1APN();
				SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body rsbody = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body();
				SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_NS1MsgRs();
				SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_Header();
				SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APN.SEND_EATM_FAA_CC1APN_Body_MsgRs_SvcRs();
				msgrs.setSvcRq(msgbody);
				msgrs.setHeader(header);
				rsbody.setRs(msgrs);
				rs.setBody(rsbody);
				header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
				header.setCHANNEL(feptxn.getFeptxnChannel());
				header.setMSGID(atmReqheader.getMSGID());
				header.setCLIENTDT(atmReqheader.getCLIENTDT());
				header.setSYSTEMID("FEP");
				if(StringUtils.isBlank(feptxn.getFeptxnReplyCode())){
					header.setSTATUSCODE("4001");
				} else {
					header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
				}
				header.setSEVERITY("INFO");
				msgbody.setOUTDATA(outdata);
				rtnMessage = XmlUtil.toXML(rs);
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "" ;
		}
		return rtnMessage;
	}
}
