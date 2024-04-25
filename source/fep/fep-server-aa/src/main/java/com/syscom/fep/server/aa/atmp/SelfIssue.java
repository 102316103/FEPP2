package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.*;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Objects;

/**
 * 負責處理 ATM 傳送的自行前置/申請建置/結帳交易電文
 *
 * @author Jie
 */
public class SelfIssue extends ATMPAABase {
	private FEPReturnCode rtnCode = CommonReturnCode.Normal;
	private String rtnMessage = StringUtils.EMPTY;
	private Object tota = null;
	private String atmno;
	private boolean needCheckMac;
	private boolean isGarbageR = false;

	public SelfIssue(ATMData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() {
		try {
			// 記錄FEPLOG內容
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage()));
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);
			
			// 1. 記錄MessageText & 準備回覆電文資料
			this.prepare();
			if (this.rtnCode != CommonReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepare");
				getLogContext().setMessage("FEPTXN PREPARE ERROR");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}

			// 2. AddTxData: 新增交易記錄(FEPTXN)
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.addTxData();
				if (this.rtnCode != CommonReturnCode.Normal) {
					// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
					getLogContext().setProgramName(ProgramName + ".AddTxData");
					getLogContext().setMessage("FEPTXN ADD ERROR");
					sendEMS(getLogContext());
					return rtnMessage; // RETUEN 空字串，不回覆ATM
				}
			}

			// 3. CheckBusinessRule: 商業邏輯檢核
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.checkBusinessRule();
			}
			//4. FEP檢核錯誤處理
			if (this.rtnCode != CommonReturnCode.Normal) {
				setFeptxnErrorCode(FEPChannel.FEP, FEPChannel.ATM);
			}
			
			// 5. SendToCBS:送往CBS主機處理
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.sendToCBS();
			}

			// 6. UpdateTxData: 更新交易記錄(FEPTxn)
			this.UpdateTxData();
			if (feptxn.getFeptxnCbsTimeout() == 1) { // HostResponseTimeout
				getLogContext().setProgramName(ProgramName + ".updateTxData");
				getLogContext().setMessage("HostResponseTimeout");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}
		} catch (Exception e) {
			this.rtnCode = CommonReturnCode.ProgramException;
			this.getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
		}

		try {
			if(!isGarbageR) {
				// 7. Response:組ATM回應電文 & 回 ATMMsgHandler
				this.rtnMessage = this.prepareATMResponseData();
	
				if("EAT".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = this.eatmResponse(rtnMessage);
				}
			}else{
				//8. 	GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler 
				this.rtnMessage = this.garbageResponse();
				
				if("EAT".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = this.eatmGarbageResponse(rtnMessage);
				}
			}
			this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			this.getTxData().getLogContext().setMessage("MessageToATM:" + this.rtnMessage);
			this.getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			this.logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this.rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		} catch (Exception e) {
			this.rtnCode = CommonReturnCode.ProgramException;
			this.getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
		}

		return this.rtnMessage;
	}

	/**
	 * 1. Prepare():記錄MessageText & 準備回覆電文資料
	 *
	 * @return
	 */
	public void prepare() {
		//查詢企業名稱(C6)、指靜脈建置(FV)、磁條密碼變更(P1)，為特殊電文另以PrepareOtherFEPTXN處理
		String msgtyp = this.getATMRequest().getMSGTYP();
		String fscode = this.getATMRequest().getFSCODE();
		String piccdid = this.getATMRequest().getPICCDID();
		String piccdlth = this.getATMRequest().getPICCDLTH();
		if (StringUtils.equals("C6", msgtyp) ||
				StringUtils.equals("FV", fscode)
				|| (StringUtils.equals("P1", fscode) && !StringUtils.equals("K", piccdid)
						&&!StringUtils.equals("200", piccdlth))) {
			// C6 電文請參考 ATM_D9Trans
			// FV 電文請參考 ATM_FVTrans
			// P1 電文請參考 ATM_TRK2ChangeSSCode(含C1)
			this.rtnCode = this.getATMBusiness().prepareOtherFEPTXN();
		} else {
			// 電文內容格式請參照: RCV_ATM-GeneralTrans
			this.rtnCode = this.getATMBusiness().prepareFEPTXN();
		}
		if (this.rtnCode != CommonReturnCode.Normal) {
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			sendEMS(logContext);
		}
	}

	/**
	 * 2. AddTxData: 新增交易記錄(FEPTXN)
	 *
	 * @throws Exception
	 */
	private void addTxData() throws Exception {
		try {
			/* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
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
	 * 3. 商業邏輯檢核 相關程式
	 */
	private void checkBusinessRule() {
		String FSCODE = this.getATMRequest().getFSCODE();
		String MSGTYP  = this.getATMRequest().getMSGTYP();
		atmno = feptxn.getFeptxnAtmno();
		if("FV".equals(FSCODE)) {
			needCheckMac = false;
		
		}else if("C6".equals(MSGTYP)) {
			needCheckMac = false;
			return ;
		}else if("I5".equals(FSCODE)) {
			//上送不驗但下送要押MAC
			needCheckMac = true;
			return ;
		}else {
			needCheckMac = true;
		}
		
		String txrequestMessage = this.getTxData().getTxRequestMessage();
		String tmAscii = EbcdicConverter.fromHex(CCSID.English, txrequestMessage);
		
		if(needCheckMac) {
			//檢核ATM電文訊息押碼(MAC)
			String PICCMACD = this.getATMRequest().getPICCMACD();
			if (StringUtils.isBlank(PICCMACD)) {
				this.rtnCode =  FEPReturnCode.ENCCheckMACError;
				return ;
			}
			
			String wkMAC = StringUtils.EMPTY;
			
			// CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
			String txCode = feptxn.getFeptxnTxCode();
			
			
			
			if ((StringUtils.equals("P1", txCode) && !StringUtils.equals("K 200", tmAscii.substring(179, 184))) ||
					StringUtils.equals("FC1", tmAscii.substring(17,20))) {
				// 磁條密碼變更&前置
				int maxtxLength = txrequestMessage.length();
				int txLength = maxtxLength - 16;
				// 最後16碼轉 ASCII
				String ATMMAC = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(txrequestMessage, txLength));
				
				this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
				logMessage(this.logContext);
				
				wkMAC = txrequestMessage.substring(36, txLength);//從第36碼取到電文總長度-16
				
				this.rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, wkMAC, ATMMAC);
				this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		        logMessage(this.logContext);
				
				
			} else if (StringUtils.equals("D9", txCode)) {
				// 查詢企業名稱(FC6)
				String ATMMAC = PICCMACD;
				
				this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
				logMessage(this.logContext);
				
				wkMAC = txrequestMessage.substring(36, 492);
				
				this.rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, wkMAC, ATMMAC);
				this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		        logMessage(this.logContext);
			} else {
				String ATMMAC = PICCMACD;
				
				this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
				logMessage(this.logContext);
				
				wkMAC = txrequestMessage.substring(36, 742);
				
				this.rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, wkMAC, ATMMAC);
				this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		        logMessage(this.logContext);
			}
			
			if (this.rtnCode != CommonReturnCode.Normal) {
				return ; // GO TO  4    /* FEP檢核錯誤處理*/
			}
		}
		
		// 3.2 轉換檢核交易PINBLOCK
		if (("FV".equals(FSCODE) || "FP".equals(FSCODE) || "P4".equals(FSCODE))
				|| (StringUtils.equals("P1", FSCODE) && !StringUtils.equals("K 200", tmAscii.substring(179, 184)))
				|| ("WF".equals(FSCODE) && "C8".equals(MSGTYP))) {
			RefString rfs = new RefString();
			String pin = "";
			String mode = ""; // 00 for ANSI9.8 , 01 for 3624
			String accno = ""; // 帳號
			String atmSeqNo = "";
			ENCKeyType keyType = ENCKeyType.None;

			switch (FSCODE) {
			case "FP": // 指靜脈密碼設定
				keyType = ENCKeyType.T3;
				mode = "00";
				break;
			case "FV":
				keyType = ENCKeyType.T3;
				mode = "00";
				pin = txrequestMessage.substring(214, 230);
				break;
			case "WF": //指靜脈提款查詢約定帳號(FC8-WF)
				keyType = ENCKeyType.T3;
				mode = "00";
				break;
			case "P4": //指靜脈舊密碼檢核(FC4-P4)、指靜脈密碼變更(P4)
				keyType = ENCKeyType.T3;
				mode = "00";
				break;
			case "P1": //磁條密碼檢核舊密碼(FC1-P1)及密碼變更(P1)
				keyType = ENCKeyType.S1;
				mode = "01";
				int car_maxLen = this.getATMRequest().getCARDPART1().length();
				if (car_maxLen >= 12) {
					int dl = car_maxLen - 12;
					accno = this.getATMRequest().getCARDPART1().substring(dl);
				}
				pin = this.getATMRequest().getPINCODE();
				break;
			}

			if (StringUtils.isBlank(pin)) {
				pin = this.getATMRequest().getIPYDATA().substring(2, 18); // 取ASCII 的值
			}
			// ATM交易序號XOR方式
			if("01".equals(mode)) {
				//取EBCDIC 值
				atmSeqNo = StringUtils.rightPad(txrequestMessage.substring(64, 72).trim(), 16, "F0");//用0補滿 8 碼
				
			}else if("FV".equals(FSCODE)) {
				//取EBCDIC 值
				atmSeqNo = txrequestMessage.substring(64, 72);
				atmSeqNo += atmSeqNo;
			}else {
				atmSeqNo = this.getATMRequest().getTRANSEQ(); //取EBCDIC 值
			}
			
			rfs.set("");
			
			try {
				this.rtnCode = new ENCHelper(getTxData()).ConvertATMPinToIMS(keyType, mode, atmno, atmSeqNo, accno, pin, rfs);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				sendEMS(getLogContext());
				this.rtnCode = ENCReturnCode.ENCPINBlockConvertError;
			}
			
			if (this.rtnCode != CommonReturnCode.Normal) {
				return ; // GO TO  4    /* FEP檢核錯誤處理*/
			}else {
				this.logContext.setMessage("new pin:" + rfs.get());
		        logMessage(this.logContext);
				feptxn.setFeptxnPinblock(rfs.get());
				if("FV".equals(FSCODE)) {
					String oldFVDATA = this.getATMRequest().getFVDATA();
					this.getATMRequest().setFVDATA(oldFVDATA.substring(0, 16) + rfs.get() + oldFVDATA.substring(32));
					this.logContext.setMessage("new FVDATA :" + this.getATMRequest().getFVDATA());
					logMessage(this.logContext);
				}
			}

		}
		
	}

	/**
	 * 5. SendToCBS:送往CBS主機處理
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		/* 交易前置處理查詢處理 */
		String AATxTYPE = "0"; // 上CBS查詢、檢核、申請、建置
		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
		feptxn.setFeptxnCbsTxCode(AA);
		ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
		this.rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
		tota = hostAA.getTota();
		if (rtnCode != FEPReturnCode.Normal) {
			if(feptxn.getFeptxnCbsTimeout() == 0) { // HostResponse無Timeout
				// 回前端主機的處理結果
            	feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, getTxData().getLogContext()));
            	if(feptxn.getFeptxnReplyCode().equals("2999")){ //無對應error code
                    feptxn.setFeptxnReplyCode("226"); //交易不能處理(IMS錯誤編碼)
                }
			} else {
				// HostResponseTimeout
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getTxData().getLogContext()));
			}
            feptxn.setFeptxnAaRc(rtnCode.getValue());
		}
	}

	/**
	 * 6. UpdateTxData: 更新交易記錄(FEPTxn)
	 */
	private void UpdateTxData() {
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
		feptxn.setFeptxnAaRc(rtnCode.getValue());
		feptxn.setFeptxnAaComplete(DbHelper.toShort(true)); /* AA Complete */
		/* For報表, 寫入處理結果 */
		if (rtnCode == FEPReturnCode.Normal) {
			if (this.getFeptxn().getFeptxnWay() == 3) {
				feptxn.setFeptxnTxrust("B"); /* 處理結果=Pending */
			} else {
				feptxn.setFeptxnTxrust("A"); /* 處理結果=成功 */
			}
		} else {
			feptxn.setFeptxnTxrust("R"); /* 處理結果=Reject */
		}

		// 回寫 FEPTXN
		/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			getATMBusiness().getFeptxn().setFeptxnReplyCode("L013");
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateTxData");
			getLogContext().setMessage("FEPTXN UPDATE ERROR");
			sendEMS(getLogContext());
		}
		
		// 電文被主機視為garbage時(所有電文)，只傳送HEAD 給ATM
		String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
		String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());

		if ("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO 7 /*組GarbageResponse回覆ATM */
		}
	}

	/**
	 * 7. Response:組ATM回應電文 & 回 ATMMsgHandler 電文內容格式請參照:
	 * C1_下送ATM電文(FPN_FPC)晶片卡格式v1.7或自行主機電文規格
	 *
	 * @return
	 */
	private String prepareATMResponseData() {
		String rtnMessage = StringUtils.EMPTY;
		/* 組 ATM Response OUT-TEXT */
		// 前置/結帳交易(以電文的MSGTYP判斷)
		String msgtyp = this.getATMRequest().getMSGTYP();

		if (StringUtils.equals("C1", msgtyp) || StringUtils.equals("C4", msgtyp)) {
			// 檢核舊密碼
			if (this.rtnCode != CommonReturnCode.Normal) {
				rtnMessage = this.getATM_FC1_FPC(); // 回覆ATM_FC1_FPC電文
			} else {
				rtnMessage = this.getATM_FSN_HEAD2(); // 回覆ATM_FSN_HEAD2電文
			}
		} else if (StringUtils.equals("C2", msgtyp)) {
			// 約定帳號查詢(舊)
			if (this.rtnCode != CommonReturnCode.Normal) {
				rtnMessage = this.getATM_FC2_FPC(); // 回覆ATM_FC2_FPC電文
			} else {
				rtnMessage = this.getATM_FC2_FR2(); // 回覆ATM_FC2_FR2電文
			}
		} else if (StringUtils.equals("CA", msgtyp)) {
			// 約定帳號查詢(新)
			if (this.rtnCode != CommonReturnCode.Normal) {
				rtnMessage = this.getATM_FCA_FPC(); // 回覆ATM_FCA_FPC電文
			} else {
				rtnMessage = this.getATM_FCA_FRA(); // 回覆ATM_FCA_FRA電文
			}
		} else if (StringUtils.equals("C5", msgtyp)) {
			// 查詢外幣匯率
			rtnMessage = this.getATM_FR5(); // 回覆ATM_FR5電文
		} else if (StringUtils.equals("C6", msgtyp)) {
			// 查詢企業名稱
			rtnMessage = this.getATM_FR6(); // 回覆ATM_FR6電文
		} else if (StringUtils.equals("C7", msgtyp) || StringUtils.equals("C8", msgtyp)) {
			// C7：指靜脈建置前置處理～檢核指靜脈申請
			// C8：指靜脈提款查詢～查詢指靜脈提款帳號
			rtnMessage = this.getATM_FR7_8(); // 回覆ATM_FR7_8電文
		} else if (StringUtils.equals("C9", msgtyp)) {
			// 指靜脈資料下傳
			rtnMessage = this.getATM_FR9(); // 回覆ATM_FR9電文
		} else if (StringUtils.equals("CC", msgtyp)) {
			// 舊卡啟用新卡查詢
			rtnMessage = this.getATM_FRC(); // 回覆ATM_FRC電文
		} else if (StringUtils.equals("CZ", msgtyp)) {
			// 無卡提款申請前置
			rtnMessage = this.getATM_FRZ(); // 回覆ATM_FAA_CC1B1PN電文
		}
		
		if(StringUtils.isNotBlank(rtnMessage)) {
			return rtnMessage;
		}

		// 申請/建置交易(以電文的FSCODE判斷)
		if (this.rtnCode != CommonReturnCode.Normal) {
			switch (this.getFeptxn().getFeptxnTxCode()) {
			// 啟用新卡、無卡提款申請、重設網銀
			case "SS":
			case "AW":
			case "NP":
				rtnMessage = this.getATM_FAA_CC1APC(); // 回覆ATM_FAA_CC1APC電文
				break;
			// 指靜脈建置、指靜脈密碼設定、指靜脈相關申請
			case "FV":
			case "FP":
			case "FA":
			case "FD":
			case "FS":
			case "FX":
				rtnMessage = this.getATM_FAA_CC1APC(); // 回覆ATM_FAA_CC1APC電文
				break;
			// 晶片卡/磁條卡密碼變更
			case "P1":
				String cardfmt = this.getATMRequest().getCARDFMT();
				// 晶片卡
				if (StringUtils.equals("9", cardfmt)) {
					rtnMessage = this.getATM_FAA_CCP1PC(); // 回覆ATM_FAA_CCP1PC電文
				}
				// 磁條卡
				if (StringUtils.equals("0", cardfmt)) {
					rtnMessage = this.getATM_FAA_TRK3PC(); // 回覆ATM_FAA_TRK3PC電文
				}
				break;
			// 指靜脈密碼變更
			case "P4":
				rtnMessage = this.getATM_FAA_CCP1PC(); // 回覆ATM_FAA_CCP1PC電文
				break;
			// 無卡存款前置~傳送簡訊驗證碼
			case "DX":
				rtnMessage = this.getATM_FAA_CC1DPC(); // 回覆ATM_FAA_CC1DPC電文
				break;
			// 存款帳號檢核、企業授權存入查詢
			case "I4":
			case "I5":
				rtnMessage = this.getATM_FAA_CC1CPC(); // 回覆ATM_FAA_CC1CPC電文
			default:
				break;
			}
		} else {
			switch (this.getFeptxn().getFeptxnTxCode()) {
			// 啟用新卡、無卡提款申請、重設網銀
			case "SS":
			case "AW":
			case "NP":
				rtnMessage = this.getATM_FAA_CC1B1PN(); // 回覆ATM_FAA_CC1B1PN電文
				break;
			// 指靜脈建置、指靜脈密碼設定、指靜脈相關申請
			case "FV":
			case "FP":
			case "FA":
			case "FD":
			case "FS":
			case "FX":
				rtnMessage = this.getATM_FAA_CC1APN(); // 回覆ATM_FAA_CC1APN電文
				break;
			// 晶片卡/磁條卡密碼變更
			case "P1":
				String cardfmt = this.getATMRequest().getCARDFMT();
				// 晶片卡
				if (StringUtils.equals("9", cardfmt)) {
					rtnMessage = this.getATM_FAA_CCP1PN(); // 回覆ATM_FAA_CCP1PN電文
				}
				// 磁條卡
				if (StringUtils.equals("0", cardfmt)) {
					rtnMessage = this.getATM_FAA_TRK3PN(); // 回覆ATM_FAA_TRK3PN電文
				}
				break;
			// 指靜脈密碼變更
			case "P4":
				rtnMessage = this.getATM_FAA_CCP1PN(); // 回覆ATM_FAA_CCP1PN電文
				break;
			// 無卡存款前置~傳送簡訊驗證碼
			case "DX":
				rtnMessage = this.getATM_FAA_CC1DPN(); // 回覆ATM_FAA_CC1DPN電文
				break;
			// 存款帳號檢核、企業授權存入查詢
			case "I4":
			case "I5":
				rtnMessage = this.getATM_FAA_CC1CPN(); // 回覆ATM_FAA_CC1CPN電文
				break;
			default:
				break;
			}
		}
		return rtnMessage;
	}

	private String getATM_FR5() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FR5 response = new ATM_FR5();
		try {
			response.setWSID(request.getWSID());
			response.setRECFMT("1"); // 變動長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2));
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setPRCRDACT("0"); // 固定值
			// 組PRINT
			if(tota != null){
				response.setRATEDATA(this.getImsPropertiesValue(tota,ImsMethodName.FR5DATA.getValue()));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FR6() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FR6 response = new ATM_FR6();
		try {
			response.setWSID(request.getWSID());
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2));
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setPRCRDACT("0"); // 固定值
			// 組PRINT
			if(tota != null){
				response.setPOFUNAEL(this.getImsPropertiesValue(tota, ImsMethodName.POFUNAEL.getValue()));// 交易種類
				response.setPOATMID(this.getImsPropertiesValue(tota, ImsMethodName.POATMID.getValue()));// 機器代號
				response.setPOFADATAA(this.getImsPropertiesValue(tota, ImsMethodName.POFADATAA.getValue()));// 企業代碼
				response.setPOFADATAB(this.getImsPropertiesValue(tota, ImsMethodName.POFADATAB.getValue()));// 授權代碼
				response.setPOTADATA(this.getImsPropertiesValue(tota, ImsMethodName.POTADATA.getValue()));// 企業戶名
				response.setPOSEQ(this.getImsPropertiesValue(tota, ImsMethodName.POSEQ.getValue()));// 交易序號
				response.setPOBUSINE(this.getImsPropertiesValue(tota, ImsMethodName.POBUSINE.getValue()));// 記帳日
				response.setPORTN(this.getImsPropertiesValue(tota, ImsMethodName.PORTN.getValue()));// 回應代碼
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FR7_8() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FR7_8 response = new ATM_FR7_8();
		try {
			response.setWSID(request.getWSID());
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2));
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setPRCRDACT("0"); // 固定值
			// 組PRINT
			if(tota != null){
				if (StringUtils.equals("C7", request.getMSGTYP())) {
					response.setDATA(this.getImsPropertiesValue(tota, ImsMethodName.FR7DATA.getValue()));
				} else if (StringUtils.equals("C8", request.getMSGTYP())) {
					response.setDATA(this.getImsPropertiesValue(tota, ImsMethodName.FR8DATA.getValue()));
				}
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FR9() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FR9 response = new ATM_FR9();
		try {
			response.setWSID(request.getWSID());
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2));
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setPRCRDACT("0"); // 固定值
			// 組PRINT
			if(tota != null){
				response.setD0DATA(this.getImsPropertiesValue(tota, ImsMethodName.D0DATA.getValue()));
				//轉換PINBLOCK更新FR9DATA中的值再下送ATM
				RefString rfs = new RefString();
				rfs.set("");
				String pin = feptxn.getFeptxnPinblock();
				String mode = "02";
				String accno = ""; // 帳號
				String atmSeqNo = this.getTxData().getTxRequestMessage().substring(64, 72);
				atmSeqNo += atmSeqNo;
				ENCKeyType keyType = ENCKeyType.T3;
				rtnCode = new ENCHelper(getTxData()).ConvertATMPinToIMS(keyType, mode, atmno, atmSeqNo, accno, pin, rfs);
				String FR9DATA = this.getImsPropertiesValue(tota, ImsMethodName.FR9DATA.getValue());
				if (rtnCode == FEPReturnCode.Normal) {
					feptxn.setFeptxnPinblock(rfs.get());
					FR9DATA = (FR9DATA.substring(0, 16) + rfs.get() + FR9DATA.substring(32));
				}
				response.setFR9DATA(FR9DATA);
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FRC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FRC response = new ATM_FRC();
		try {
			response.setWSID(request.getWSID());
			response.setRECFMT("1"); // 變動長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2));
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setCARDACT("0"); // 固定值
			// 組PRINT
			if(tota != null){
				response.setDATA(this.getImsPropertiesValue(tota, ImsMethodName.D0_FCC.getValue()));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FRZ() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FRZ response = new ATM_FRZ();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PC");
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 不吃卡
			// 組PRINT(189)
			response.setPTYPE("S0");
			response.setPLEN("190");
			response.setPBMPNO("010000"); // 前置交易給定值
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			// 代理行
			response.setPATXBKNO(feptxn.getFeptxnBkno()); // 代理行
			response.setPSTAN(feptxn.getFeptxnStan());

			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));

			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CC1APN() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CC1APN response = new ATM_FAA_CC1APN();
		try {
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PN"); // + response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); //不吃卡
			// 組PRINT(024)
			response.setDATATYPE("D0");
			response.setDATALEN("024");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			response.setPAGENO(TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
					this.getTxData().getLogContext()));
			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			// 組PRINT(189)
			response.setPTYPE("S0");
			response.setPLEN("190");
			response.setPBMPNO("000010"); // FPN
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			// 代理行
			response.setPATXBKNO(feptxn.getFeptxnBkno());
			response.setPSTAN(feptxn.getFeptxnStan());
			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}
			
			if(tota != null){
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉入帳號(明細表顯示內容)
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));
				//CBS下送ATM的促銷應用訊息
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CC1APC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CC1APC response = new ATM_FAA_CC1APC();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PC"); // + response

			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 晶片卡不留置:固定放”0”
			// 組PRINT
			response.setDATATYPE("D0");
			response.setDATALEN("004");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			String imsrcTcb = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());
			response.setPAGENO(TxHelper.getRCFromErrorCode(imsrcTcb, FEPChannel.CBS, FEPChannel.ATM,
					this.getTxData().getLogContext()));
			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			// 組PRINT(191)
			response.setPTYPE("S0");
			response.setPLEN("191");
			response.setPBMPNO("010000"); // 前置交易給定值

			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			// 代理行
			response.setPATXBKNO(feptxn.getFeptxnBkno());
			response.setPSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));

			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CC1B1PN() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CC1B1PN response = new ATM_FAA_CC1B1PN();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGTYP("PN"); // + response
			response.setMSGCAT("F");
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 不吃卡
			// 組PRINT(191)
			response.setPTYPE("S0");
			response.setPLEN("191");
			response.setPBMPNO("000010"); // FPN
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			response.setPATXBKNO(feptxn.getFeptxnBkno()); // 代理行
			response.setPSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_TRK3PN() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_TRK3PN response = new ATM_FAA_TRK3PN();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PN"); // + response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 不吃卡
			// 組T3DATA(108)
			response.setPTYPE("I0");
			response.setPLEN("108");
			response.setT3MAP(StringUtils.substring(request.getCARDDATA(), 43, 47));
			response.setTMT3(getATMBusiness().getAtmTxData().getTxRequestMessage().substring(191, 295));
			// 組PRINT(157)
			response.setPTYPE("S0");
			response.setPLEN("159");
			response.setPBMPNO("000010"); // FPN
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			// CBS下送的轉出帳號(明細表顯示內容)
			response.setPATXBKNO(feptxn.getFeptxnBkno()); // 代理行
			response.setPSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));

			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_TRK3PC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_TRK3PC response = new ATM_FAA_TRK3PC();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PN"); // + response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 不吃卡
			// 組DATA(004)
			response.setPTYPE("D0");
			response.setPLEN("004");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			response.setPAGENO(
					TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(),
							FEPChannel.CBS, FEPChannel.ATM, getTxData().getLogContext()));
			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			// 組PRINT(157)
			response.setPTYPE("S0");
			response.setPLEN("159");
			response.setPBMPNO("000010"); // FPN
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			response.setPATXBKNO(feptxn.getFeptxnBkno()); // 代理行
			response.setPSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));

			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CCP1PN() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CCP1PN response = new ATM_FAA_CCP1PN();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PN"); // + response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); //不吃卡
			// 組PRINT(159)
			response.setPTYPE("S0");
			response.setPLEN("159");
			response.setPBMPNO("000010"); // FPN
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			response.setPATXBKNO(feptxn.getFeptxnBkno()); // 代理行
			response.setPSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));

			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CCP1PC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CCP1PC response = new ATM_FAA_CCP1PC();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PN"); // + response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
			// 組DATA(004)
			response.setPTYPE("D0");
			response.setPLEN("004");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			response.setPAGENO(
					TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(),
							FEPChannel.CBS, FEPChannel.ATM, getTxData().getLogContext()));
			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			// 組PRINT(159)
			response.setPTYPE("S0");
			response.setPLEN("159");
			response.setPBMPNO("000010"); // FPN
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			// ATM代號
			response.setPTID(feptxn.getFeptxnAtmno());

			// 代理行
			response.setPATXBKNO(feptxn.getFeptxnBkno());
			response.setPSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota,ImsMethodName.FROMBANK.getValue()));

			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CC1CPN() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CC1CPN response = new ATM_FAA_CC1CPN();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PN"); // + response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 不吃卡
			// 組 D0(079-格式四)
			response.setDATATYPE("D0");
			response.setDATALEN("079");
			response.setACKNOW("1");
			response.setSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				response.setFSCODE(this.getImsPropertiesValue(tota, ImsMethodName.FSCODE.getValue()));
				response.setCARDACT(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				response.setTOACT(this.getImsPropertiesValue(tota,ImsMethodName.TOBANK.getValue()) + "-"
						+ this.getImsPropertiesValue(tota,ImsMethodName.TOACT.getValue()));
				response.setACTNAME(this.getImsPropertiesValue(tota,ImsMethodName.ACTNAME.getValue()));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CC1CPC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CC1CPC response = new ATM_FAA_CC1CPC();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PC"); // - response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 不吃卡
			// 組DATA(004)
			response.setDATATYPE("D0");
			response.setDATALEN("004");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			response.setPAGENO(TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
					getTxData().getLogContext()));
			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			// 組 DATA(079)
			response.setDATATYPE("D0");
			response.setDATALEN("079");
			response.setACKNOW("1");
			response.setSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				response.setFSCODE(this.getImsPropertiesValue(tota, ImsMethodName.FSCODE.getValue()));
				response.setCARDACT(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				response.setTOACT(this.getImsPropertiesValue(tota,ImsMethodName.TOBANK.getValue()) + "-"
						+ this.getImsPropertiesValue(tota,ImsMethodName.TOACT.getValue()));
				response.setACTNAME(this.getImsPropertiesValue(tota,ImsMethodName.ACTNAME.getValue()));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CC1DPN() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CC1DPN response = new ATM_FAA_CC1DPN();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PN"); // + response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("4"); // 不吃卡
			// 組DATA(079)
			response.setDATATYPE("D0");
			response.setDATALEN("079");
			response.setACKNOW("1"); // 客戶確認
			response.setFSCODE(request.getFSCODE());
			response.setSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				response.setDXTVC(this.getImsPropertiesValue(tota, ImsMethodName.DXTVC.getValue()));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FAA_CC1DPC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FAA_CC1DPC response = new ATM_FAA_CC1DPC();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1");
			response.setMSGCAT("F");
			response.setMSGTYP("PC"); // - response
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
			// 組DATA(004)
			response.setDATATYPE("D0");
			response.setDATALEN("004");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			response.setPAGENO(
					TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(),
							FEPChannel.CBS, FEPChannel.ATM, getTxData().getLogContext()));
			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			// 組DATA1(079)
			response.setDATATYPE1("D0");
			response.setDATALEN1("079");
			response.setACKNOW1("1"); // 客戶確認
			response.setFSCODE(request.getFSCODE());
			response.setTELNO(feptxn.getFeptxnTelephone());
			response.setSTAN(feptxn.getFeptxnStan());

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				// 驗證碼
				response.setDXTVC(this.getImsPropertiesValue(tota, ImsMethodName.DXTVC.getValue()));

			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FSN_HEAD2() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FSN_HEAD2 response = new ATM_FSN_HEAD2();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("0"); // 固定長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2));
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG()); // 回覆FAA
			response.setPRCRDACT("0"); // 固定值

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FC1_FPC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FC1_FPC response = new ATM_FC1_FPC();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1"); // 變動長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("PC"); // 問題交易
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setPRCRDACT("4");

			// 組DATA(004)
			response.setDATATYPE("D0");
			response.setDATALEN("004");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			response.setPAGENO(
					TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(),
							FEPChannel.CBS, FEPChannel.ATM, getTxData().getLogContext()));

			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}

			// 組PRINT(158)
			response.setPTYPE("S0");
			response.setPLEN("159");
			response.setPBMPNO("010000"); // FPC
			// 西元年(格式：YYYYMMDD)轉民國年(格式：YYY/MM/DD)
			response.setPDATE(this.dateStrToYYYMMDD(feptxn.getFeptxnTxDateAtm()));
			// 時間(格式：HHMMSS，格式化 ：HH :MM :SS)
			response.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
			response.setPTID(feptxn.getFeptxnAtmno());
			response.setPATXBKNO(feptxn.getFeptxnBkno());
			if (StringUtils.equals("C4", request.getMSGTYP())) { // 檢核指靜脈舊密碼
				response.setPSTAN("FPINCHK");
			}

			//ATM回應代碼(CBSProcess已依主機下送規則處理)
			if(rtnCode != FEPReturnCode.Normal){
				response.setPRCCODE(feptxn.getFeptxnReplyCode());
			}else{
				response.setPRCCODE(feptxn.getFeptxnCbsRc());
			}

			if(tota != null){
				//留置卡片註記及STAN，FC1(檢核磁條卡舊密碼)由主機下送取得
				if("C1".equals(request.getMSGTYP())){
					response.setPRCRDACT(this.getImsPropertiesValue(tota, ImsMethodName.PBMCRD.getValue()));
					response.setPSTAN(this.getImsPropertiesValue(tota, ImsMethodName.ERRPGM.getValue()));
				}
				//交易種類
				response.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
				//轉出帳號(明細表顯示內容)
				response.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
				//轉出銀行別
				response.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
				ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					response.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
	
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FC2_FR2() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FC2_FR2 response = new ATM_FC2_FR2();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1"); // 變動長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2));
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setCARDACT("0"); // 固定值
			// 組PRINT(變動長度：總長度=R0LEN)
			response.setR0("R0");
			response.setS0("S0");
			response.setS0LEN("0101N"); // 固定值

			if(tota != null){
				response.setR0LEN(this.getImsPropertiesValue(tota, ImsMethodName.R0_LENTH.getValue()));
				response.setACT_COUNT(this.getImsPropertiesValue(tota, ImsMethodName.ACT_COUNT.getValue()));
				response.setACT_LENGTH(this.getImsPropertiesValue(tota, ImsMethodName.ACT_LENGTH.getValue()));
				int i = Integer.valueOf(this.getImsPropertiesValue(tota,ImsMethodName.FR2_ACTDAT_LEN.getValue()));
				response.setACTDATA(this.getImsPropertiesValue(tota, ImsMethodName.FR2_ACTDATA.getValue()).substring(0,i));
			}

			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
	            
				rtnCode = new ENCHelper(this.getTxData()).makeAtmMacP3(atmno, rtnMessage, rfs);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	private String getATM_FC2_FPC() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FC2_FPC response = new ATM_FC2_FPC();
		try {
			// 組Header
			response.setWSID(request.getWSID());
			response.setRECFMT("1"); // 變動長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("PC"); // 問題交易
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setPRCRDACT("0"); // 固定值
			// 組DATA(004)
			response.setDATATYPE("D0");
			response.setDATALEN("004");
			response.setACKNOW("0");
			// 取主機下送三碼代碼，需轉換為ATM代碼
			response.setPAGENO(
					TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(),
							FEPChannel.CBS, FEPChannel.ATM, getTxData().getLogContext()));

            // 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			
			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
				
				rtnCode = new ATMENCHelper(this.getTxData()).makeAtmMacP3(atmno, rtnMessage, rfs);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	/**
	 *  回覆ATM_FCA_FRA電文
	 * @return
	 */
	private String getATM_FCA_FRA() {
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FCA_FRA response = new ATM_FCA_FRA();
		try {
			//組Header(IMS實際下送FCA電文如下)
			response.setWSID(request.getWSID());
			response.setRECFMT("1"); // 變動長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("R" + StringUtils.substring(request.getMSGTYP(), 1, 2)); // "R"+ ATM.REQ.MSGTYP[ 2 :1]
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setCARDACT("0"); // 固定值
			
			// 處理有收到CBS Response的欄位值
			if(tota != null){
				response.setACTDATA(this.getImsPropertiesValue(tota, ImsMethodName.FRADATA.getValue())); //帳號資料
			}
			
			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
				
				rtnCode = new ATMENCHelper(this.getTxData()).makeAtmMacP3(atmno, rtnMessage, rfs);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	/**
	 * 回覆ATM_FCA_FPC電文
	 * @return
	 */
	private String getATM_FCA_FPC() {
		//下送 ClassName：ATM_FCA_FPC
		String rtnMessage = StringUtils.EMPTY;
		ATMGeneralRequest request = this.getATMRequest();
		ATM_FCA_FPC response = new ATM_FCA_FPC();
		try {
			//組Header r(IMS實際下送FCA電文如下)
			response.setWSID(request.getWSID());
			response.setRECFMT("1"); // 變動長度
			response.setMSGCAT(request.getMSGCAT());
			response.setMSGTYP("PC"); //問題交易
			response.setTRANDATE(request.getTRANDATE());
			response.setTRANTIME(request.getTRANTIME());
			response.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
			response.setTDRSEG(request.getTDRSEG());
			response.setPRCRDACT("0"); // 固定值
			// 組DATA(004)
			response.setDATATYPE("D0");
			response.setDATALEN("004");
			response.setACKNOW("0");
			// 以CBS_RC取得轉換後的PBMDPO編號
			String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM, getTxData().getLogContext());
			response.setPAGENO(pageNo);

			// 其他未列入的代碼，一律回 226
			if (StringUtils.isBlank(response.getPAGENO())) {
				response.setPAGENO("226"); // 交易不能處理
			}
			
			/* CALL ENC 取得MAC 資料 */
			if(needCheckMac) {
				RefString rfs = new RefString();
	
				rfs.set("");
				rtnMessage = response.makeMessage();
				
	            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
	            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
	            		+ request.getPICCBI11() +  request.getTRANSEQ()
	            		+ request.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
	            		+ " ";
	            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
	            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
	            	logMessage(this.logContext);
	            }
				
				rtnCode = new ATMENCHelper(this.getTxData()).makeAtmMacP3(atmno, rtnMessage, rfs);
				
				if (rtnCode != FEPReturnCode.Normal) {
					response.setMACCODE(""); /* 訊息押碼 */
				} else {
					response.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
			}
			
			rtnMessage = response.makeMessage();
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		return rtnMessage;
	}

	/**
	 * 轉民國年,格式化為YYY/MM/DD,年度取後碼,ex :112/06/29
	 *
	 * @param dateStr
	 * @return
	 */
	private String dateStrToYYYMMDD(String dateStr) {
		String rtnDate;
		if ("00000000".equals(dateStr) || dateStr.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			rtnDate = "000/00/00";
		} else {
			dateStr = CalendarUtil.adStringToROCString(dateStr);
			String yearStr = dateStr.substring(0, 3);
			String monthStr = dateStr.substring(3, 5);
			String dayStr = dateStr.substring(5);
			rtnDate = String.format("%s/%s/%s", yearStr, monthStr, dayStr);
		}
		return rtnDate;
	}

	/**
	 * 時間格式字串：HHmmss轉為HH:mm:ss
	 *
	 * @param time
	 * @return
	 */
	private String formatTime(String time) {
		if (StringUtils.isBlank(time)) {
			return time;
		} else if (time.length() != 6) {
			time = StringUtils.leftPad(StringUtils.right(time, 6), 6, '0');
		}
		StringBuilder sb = new StringBuilder();
		boolean addColon = true;
		for (int i = 0; i < time.length(); i++) {
			if (addColon) {
				sb.append(':');
			}
			sb.append(time.charAt(i));
			addColon = !addColon;
		}
		return sb.substring(1);
	}

	private String eatmResponse(String outdata) throws Exception {
		String rtnMessage = "" ;
		try {
			/* 組 ATM Response OUT-TEXT */
			RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();

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

	private String garbageResponse() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
            // 組 Header
            ATMGeneralRequest atmReq = this.getATMRequest();
            
            ATM_FSN_HEAD2 atm_fsn_head2 = new ATM_FSN_HEAD2();
            atm_fsn_head2.setWSID(atmReq.getWSID());
            atm_fsn_head2.setRECFMT("1");
            atm_fsn_head2.setMSGCAT("F");
            atm_fsn_head2.setMSGTYP("PC");
            atm_fsn_head2.setTRANDATE(atmReq.getTRANDATE());
            atm_fsn_head2.setTRANTIME(atmReq.getTRANTIME());
            atm_fsn_head2.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
            atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
            // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
            // (FC1、P1)主機才有可能依據狀況要求吃卡
            atm_fsn_head2.setPRCRDACT("0");

            /* CALL ENC 取得MAC 資料 */
            ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
            RefString rfs = new RefString();
            
            rfs.set("");
            rtnMessage = atm_fsn_head2.makeMessage();
            
            if(!"ATM".equals(feptxn.getFeptxnChannel())) {
            	rtnMessage = rtnMessage.substring(8, 23) + feptxn.getFeptxnStan()
            		+ atmReq.getPICCBI11() +  atmReq.getTRANSEQ()
            		+ atmReq.getTDRSEG() + this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())
            		+ " ";
            	rtnMessage = EbcdicConverter.toHex(CCSID.English, rtnMessage.length(), rtnMessage);
            	this.logContext.setMessage("Channel:" + feptxn.getFeptxnChannel()+ ", new inputData:" + rtnMessage);
            	logMessage(this.logContext);
            }
            
			rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
			
			if (rtnCode != FEPReturnCode.Normal) {
				atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
			} else {
				atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
			
            this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
            
            rtnMessage = atm_fsn_head2.makeMessage();
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}
	
	private String eatmGarbageResponse(String outdata) throws Exception {
		String rtnMessage = "" ;
		try {
			/* 組 ATM Response OUT-TEXT */
			RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
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
			header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
			header.setSEVERITY("Garbage");
			msgbody.setOUTDATA(outdata);
			rtnMessage = XmlUtil.toXML(rs);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "" ;
		}
		return rtnMessage;
	}
	
	//4. 	FEP檢核錯誤處理
	private void setFeptxnErrorCode(FEPChannel channel_1, FEPChannel channel_2) {
		setFeptxnErrorCode(Objects.toString(rtnCode.getValue()), channel_1, channel_2);
	}
	
	private void setFeptxnErrorCode(String errCode, FEPChannel channel_1, FEPChannel channel_2) {
		feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(errCode, channel_1, channel_2, getTxData().getLogContext()));
		feptxn.setFeptxnAaRc(rtnCode.getValue());
	}
}
