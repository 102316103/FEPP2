package com.syscom.fep.server.aa.atmp;

import java.util.Objects;

import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.BinMapper;
import com.syscom.fep.mybatis.model.Bin;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_P6WW1BPN;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_WW1BPC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_WW1BPN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;

/**
 * @author vincent
 */
public class WPSelfIssue extends ATMPAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private BinMapper binMapper = SpringBeanFactoryUtil.getBean(BinMapper.class);
	private String atmno;
	private boolean isGarbageR = false;

	public WPSelfIssue(ATMData txnData) throws Exception {
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
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage()));
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);
            
			// 1. Prepare():記錄MessageText & 準備回覆電文資料
			rtnCode = getATMBusiness().PrepareFEPTXN_EMV();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".PrepareFEPTXN_EMV");
				getLogContext().setMessage("FEPTXN PREPARE ERROR");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 2. AddTxData: 新增交易記錄(FEPTxn)
				this.addTxData(); // 新增交易記錄(FEPTxn)
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
			
			//4. 	FEP檢核錯誤處理
			if (rtnCode != FEPReturnCode.Normal) {
				setFeptxnErrorCode(FEPChannel.FEP, FEPChannel.ATM);
			}
			
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. SendToCBS:送往CBS主機處理
				this.sendToCBS();
			}

			// 6. UpdateTxData: 更新交易記錄(FEPTxn)
			this.updateTxData();
			if (feptxn.getFeptxnCbsTimeout() == 1) { // HostResponseTimeout
				getLogContext().setProgramName(ProgramName + ".updateTxData");
				getLogContext().setMessage("HostResponseTimeout");
				sendEMS(getLogContext());
				return rtnMessage; // RETUEN 空字串，不回覆ATM
			}

		} catch (Exception ex) {
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		}

		try {
			// 7.Response:組ATM回應電文 & 回 ATMMsgHandler
			if(!isGarbageR) {
				// 7.Response:組ATM回應電文 & 回 ATMMsgHandler
				if (StringUtils.isBlank(getTxData().getTxResponseMessage())) {
					rtnMessage = this.response();
				} else {
					rtnMessage = getTxData().getTxResponseMessage();
				}
			}else {
				//8. 	GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler 
				rtnMessage = this.garbageResponse();
			}
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		} catch (Exception ex) {
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
	 * @throws Exceptionx
	 */
	private void addTxData() throws Exception {
		try {
			// 新增交易記錄(FEPTxn) Returning FEPReturnCode
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
		// 3.1 檢核本行發卡信用卡BIN
		String trk2 = getATMRequest().getTRK2();
		String BIN_NO = StringUtils.substring(trk2, 0, 6);
		String BIN_BKNO = SysStatus.getPropertyValue().getSysstatHbkno();
		// 檢核BIN資料是否存在
		// 以BIN_NO及BIN_BKNO 為key讀取 Table
		Bin bin = binMapper.selectByPrimaryKey(BIN_NO, BIN_BKNO);
		if (bin == null) {
			rtnCode = FEPReturnCode.BINNotFound; // BIN資料不存在
			return; // GO TO 4   /* FEP檢核錯誤處理 */ 
		}

		// 3.2 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		/* 檢核 ATM 電文 MAC */
		String ATM_TITA_PICCMACD = getTxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
			return; // GO TO 4 /* FEP檢核錯誤處理 */
		}

		String ATMMAC = ATM_TITA_PICCMACD;
		this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
		logMessage(this.logContext);

		atmno = feptxn.getFeptxnAtmno();
		String wkMAC = StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 360);

		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, wkMAC, ATMMAC);

		this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
		
		if (rtnCode != FEPReturnCode.Normal) {
			return ;
		}
		
		//3.3	ATM電文PIN換成新 PIN 給主機
		String ssCode = getATMRequest().getSSCODE();
		if(StringUtils.isBlank(ssCode)) {
			//轉換PIN BLOCK失敗
			rtnCode = FEPReturnCode.ENCPINBlockConvertError;
			return; // GO TO  4   /* FEP檢核錯誤處理 */
		}
		
		ENCKeyType keytype;
		String mode = "01";
		String accno;
		String pin = "";
		String atmSeqNo = "";
		RefString rfs = new RefString();
		rfs.set("");
		
		if("3".equals(getATMRequest().getAPPLUSE())) {
			keytype = ENCKeyType.T3;
		}else {
			keytype = ENCKeyType.S1;
		}
		
		int eqInt = trk2.indexOf("D");
		int eqInt2 = trk2.indexOf("=");
		if((eqInt > -1 && eqInt <= 12) || (eqInt2 > -1 && eqInt2 <= 12)) {
			if(eqInt > -1) {
				accno = StringUtils.leftPad(StringUtils.substring(trk2, 0, eqInt - 1), 12, "0");
			}else {
				accno = StringUtils.leftPad(StringUtils.substring(trk2, 0, eqInt2 - 1), 12, "0");
			}
		}else if(eqInt > 12 || eqInt2 > 12) {
			int start;
			if(eqInt > 12) {
				start = eqInt - 13;
				accno = StringUtils.substring(trk2, start, start + 12);
			}else {
				start = eqInt2 - 13;
				accno = StringUtils.substring(trk2, start, start + 12);
			}
			
		}else {
			accno = StringUtils.substring(trk2, 3, 15);//ATM.TRK2[4 :12]
		}
		
		pin = getATMRequest().getIPYDATA().substring(2, 18);
		atmSeqNo = getATMRequest().getTRANSEQ();
		
		rtnCode = new ENCHelper(getTxData()).ConvertATMPinToIMS(keytype, mode, atmno, atmSeqNo, accno, pin, rfs);
		String newPin = rfs.get();
		
		if (rtnCode == FEPReturnCode.Normal) {
			this.logContext.setMessage("newPin:" + newPin);
			logMessage(this.logContext);
			feptxn.setFeptxnPinblock(newPin); // 更新 pin 
		} else {
			return ; // GO TO  4    /* FEP檢核錯誤處理 */
		}
	}

	/**
	 * 5. SendToCBS:送往CBS主機處理
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		/* 交易前置處理查詢處理 */
		String AATxTYPE = "3"; // 上CBS授權(變更密碼)
		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
		feptxn.setFeptxnCbsTxCode(AA);
		ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
		rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
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
	private void updateTxData() {
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
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
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateTxData"));
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
	 * 7. Response:組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @throws Exception
	 */
	private String response() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			ATMGeneralRequest atmReq = this.getATMRequest();
			ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				ATM_FAA_WW1BPC atm_faa_ww1bpc = new ATM_FAA_WW1BPC();
				// 組Header(OUTPUT-1)
				atm_faa_ww1bpc.setWSID(atmReq.getWSID());
				atm_faa_ww1bpc.setRECFMT("1");
				atm_faa_ww1bpc.setMSGCAT("F");
				atm_faa_ww1bpc.setMSGTYP("PC"); // - response
				atm_faa_ww1bpc.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_ww1bpc.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_ww1bpc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_ww1bpc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_ww1bpc.setPRCRDACT("4"); // 自行不處理留置:固定放”4”

				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				atm_faa_ww1bpc.setDATATYPE("D0");
				atm_faa_ww1bpc.setDATALEN("004");
				atm_faa_ww1bpc.setACKNOW("0");
				// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
				String pageNo;
				// 此欄給主機回應的代碼，尚未走到主機就給空值
				if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
					pageNo = feptxn.getFeptxnCbsRc();
				} else {
					pageNo = feptxn.getFeptxnReplyCode();
				}

				atm_faa_ww1bpc.setPAGENO(pageNo);

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_ww1bpc.setPTYPE("S0");
				atm_faa_ww1bpc.setPLEN("158");
				atm_faa_ww1bpc.setPBMPNO("010000"); // FPC
				// 日期格式 :YYYYMMDD(不用轉換)
				atm_faa_ww1bpc.setPDATE(feptxn.getFeptxnTxDateAtm());
				// 時間格式：HHMMSS 轉為HH :MM :SS
				atm_faa_ww1bpc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				atm_faa_ww1bpc.setPTID(feptxn.getFeptxnAtmno());
				// 代理銀行別
				atm_faa_ww1bpc.setPATXBKNO(feptxn.getFeptxnBkno());
				// 跨行交易序號
				atm_faa_ww1bpc.setPSTAN(feptxn.getFeptxnStan());
				//ATM回應代碼(CBSProcess已依主機下送規則處理)
				atm_faa_ww1bpc.setPRCCODE(feptxn.getFeptxnReplyCode());

				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_ww1bpc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)
					atm_faa_ww1bpc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
					//轉出行
					atm_faa_ww1bpc.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
				}
				
				rfs.set("");
				rtnMessage = atm_faa_ww1bpc.makeMessage();
				
				rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
				
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_ww1bpc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getTxData().getLogContext()));
					atm_faa_ww1bpc.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_ww1bpc.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
				
				rtnMessage = atm_faa_ww1bpc.makeMessage();
			} else {
				if("P6".equals(feptxn.getFeptxnTxCode()) ) {
					ATM_FAA_P6WW1BPN atm_faa_p6ww1bpn = new ATM_FAA_P6WW1BPN();
					// 組Header(OUTPUT-1)
					atm_faa_p6ww1bpn.setWSID(atmReq.getWSID());
					atm_faa_p6ww1bpn.setRECFMT("1");
					atm_faa_p6ww1bpn.setMSGCAT("F");
					atm_faa_p6ww1bpn.setMSGTYP("PN"); // + response
					atm_faa_p6ww1bpn.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_p6ww1bpn.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_p6ww1bpn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_p6ww1bpn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_p6ww1bpn.setPRCRDACT("4"); // 自行不處理留置:固定放”4”
					
					// 組 H0(037)
					atm_faa_p6ww1bpn.setDATATYPE("H0");
					atm_faa_p6ww1bpn.setDATALEN("037");
					atm_faa_p6ww1bpn.setTRK2(atmReq.getTRK2());

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_p6ww1bpn.setPTYPE("S0");
					atm_faa_p6ww1bpn.setPLEN("158");
					atm_faa_p6ww1bpn.setPBMPNO("000010"); // FPN
					// 日期格式 :YYYYMMDD(不用轉換)
					atm_faa_p6ww1bpn.setPDATE(feptxn.getFeptxnTxDateAtm());
					// 時間格式：HHMMSS 轉為HH :MM :SS
					atm_faa_p6ww1bpn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_p6ww1bpn.setPTID(feptxn.getFeptxnAtmno());
					// 代理銀行別
					atm_faa_p6ww1bpn.setPATXBKNO(feptxn.getFeptxnBkno());
					// 跨行交易序號
					atm_faa_p6ww1bpn.setPSTAN(feptxn.getFeptxnStan());
					//ATM回應代碼(CBSProcess已依主機下送規則處理)
					atm_faa_p6ww1bpn.setPRCCODE(feptxn.getFeptxnCbsRc());

					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_p6ww1bpn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)
						atm_faa_p6ww1bpn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
						//轉出行
						atm_faa_p6ww1bpn.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
					}
					
					rfs.set("");
					rtnMessage = atm_faa_p6ww1bpn.makeMessage();
					
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
					
					if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_p6ww1bpn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
						atm_faa_p6ww1bpn.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_p6ww1bpn.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
					
					rtnMessage = atm_faa_p6ww1bpn.makeMessage();
				}else {
					ATM_FAA_WW1BPN atm_faa_ww1bpn = new ATM_FAA_WW1BPN();
					// 組Header(OUTPUT-1)
					atm_faa_ww1bpn.setWSID(atmReq.getWSID());
					atm_faa_ww1bpn.setRECFMT("1");
					atm_faa_ww1bpn.setMSGCAT("F");
					atm_faa_ww1bpn.setMSGTYP("PN"); // + response
					atm_faa_ww1bpn.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_ww1bpn.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_ww1bpn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_ww1bpn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_ww1bpn.setPRCRDACT("4"); // 自行不處理留置:固定放”4”

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_ww1bpn.setPTYPE("S0");
					atm_faa_ww1bpn.setPLEN("158");
					atm_faa_ww1bpn.setPBMPNO("000010"); // FPN
					// 日期格式 :YYYYMMDD(不用轉換)
					atm_faa_ww1bpn.setPDATE(feptxn.getFeptxnTxDateAtm());
					// 時間格式：HHMMSS 轉為HH :MM :SS
					atm_faa_ww1bpn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_ww1bpn.setPTID(feptxn.getFeptxnAtmno());
					// 代理銀行別
					atm_faa_ww1bpn.setPATXBKNO(feptxn.getFeptxnBkno());
					// 跨行交易序號
					atm_faa_ww1bpn.setPSTAN(feptxn.getFeptxnStan());
					//ATM回應代碼(CBSProcess已依主機下送規則處理)
					atm_faa_ww1bpn.setPRCCODE(feptxn.getFeptxnCbsRc());

					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_ww1bpn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)
						atm_faa_ww1bpn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
						//轉出行
						atm_faa_ww1bpn.setPOTXBKNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue()));
					}
					
					rfs.set("");
					rtnMessage = atm_faa_ww1bpn.makeMessage();
					
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
					
					if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_ww1bpn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
						atm_faa_ww1bpn.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_ww1bpn.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
					
					rtnMessage = atm_faa_ww1bpn.makeMessage();
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
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
	
	/**
	 * 時間格式：HHmmss 轉為HH:mm:ss
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
	
	private void setFeptxnErrorCode(FEPChannel channel_1, FEPChannel channel_2) {
		setFeptxnErrorCode(Objects.toString(rtnCode.getValue()), channel_1, channel_2);
	}
	
	private void setFeptxnErrorCode(String errCode, FEPChannel channel_1, FEPChannel channel_2) {
		feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(errCode, channel_1, channel_2, getTxData().getLogContext()));
		feptxn.setFeptxnAaRc(rtnCode.getValue());
	}
}
