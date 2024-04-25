package com.syscom.fep.server.aa.atmp;

import java.util.Calendar;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B2PC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B2PN;

/**
 * @author vincent
 */
public class PYSelfRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

	public PYSelfRequestA(ATMData txnData) throws Exception {
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
            this.logContext.setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage("ASCII TITA:" + EbcdicConverter.fromHex(CCSID.English, this.getATMtxData().getTxRequestMessage()));
            this.logContext.setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
            logMessage(this.logContext);
            
			// 1. Prepare : 交易記錄初始資料
			rtnCode = getATMBusiness().prepareFEPTXN();

			if (rtnCode == FEPReturnCode.Normal) {
				// 2. AddTxData: 新增交易記錄(FEPTXN)
				this.addTxData();
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 3. CheckBusinessRule: 商業邏輯檢核
				this.checkBusinessRule();
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 4. SendToCBS/ASC: 送主機檢核帳戶資料
				this.sendToCBS();
			}

			FISC fiscBusiness = getFiscBusiness();
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. 組送往 FISC 之 Request 電文並等待財金之 Response
				rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
			}

			boolean repRcEq4001 = true;
			if (rtnCode == FEPReturnCode.Normal) {
				// 6. CheckResponseFromFISC:檢核回應電文是否正確
				rtnCode = fiscBusiness.checkResponseMessage();
				repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
			}

			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				//7. SendToCBS/ASC: 送主機處理帳務
				this.sendToCBS2();
				if (rtnCode == FEPReturnCode.Normal) {
					if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
						/* 沖回跨行代收付(APTOT) */
						rtnCode = fiscBusiness.processAptot(true);
					}
				}
			}

			// 9. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
			if(!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
				rtnMessage = this.labelEndOfFunc();
			}
			// 10. 判斷是否需組 CON 電文回財金
			this.updateTxData();

		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		}

		try {
			// 11. 更新交易記錄(FEPTXN)
			this.updateTxData2();

			getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getATMtxData().getLogContext().setMessage("MessageToATM:" + rtnMessage);
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

		/* 檢核 ATM 電文 */
		rtnCode = checkRequestFromATM(getATMtxData());
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
		}

		/* 檢核委託單位代號 或繳款類別 */
		if (StringUtils.isNotBlank(feptxn.getFeptxnBusinessUnit())) {
			/* 檢核委託單位代號 */
			rtnCode = getFiscBusiness().checkNpsunit(feptxn);
			if (rtnCode != FEPReturnCode.Normal) {
				return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
			}
		} else if (StringUtils.isNotBlank(feptxn.getFeptxnPaytype())) {
			/* 檢核繳款類別 */
			rtnCode = getFiscBusiness().checkPAYTYPE();
			if (rtnCode != FEPReturnCode.Normal) {
				return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
			}
		}

		/* 檢核單筆限額 */
		/*
		 * 自行繳費: MSGCTL_CHECK_LIMIT=2 1. 全國繳費單筆交易限額: 單筆限額 200萬 2. 「繳費移轉計畫」視為約定轉帳:
		 * 單筆限額200萬
		 */
		rtnCode = getFiscBusiness().checkTransLimit(getATMtxData().getMsgCtl());
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
		}

		/* 檢核中文附言欄不得超過14個中文字 */
		String feptxnChrem = feptxn.getFeptxnChrem();
		if (StringUtils.isNotBlank(feptxnChrem)) {
			if (feptxnChrem.length() > 14) {
				rtnCode = FEPReturnCode.OtherCheckError; /* E711:其他類檢核錯誤 */
				return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
			}

			RefString output = new RefString("");
			getFiscBusiness().convertFiscEncode(getFeptxn().getFeptxnChrem(), output); // (UNICODE轉CNS11643)
			if (output.get().length() > 80) { // 長度>80
				rtnCode = FEPReturnCode.OtherCheckError; /* E711:其他類檢核錯誤 */
				getLogContext().setRemark("中文摘要轉成CNS11643超過80位" + output.get());
				logMessage(Level.INFO, getLogContext());
				return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
			}
		}
		
		String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
        if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
            rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
            return; // GO TO  /* 更新交易記錄 */
        }
        
        String ATMMAC = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(getATMtxData().getTxRequestMessage(), 742, 758));// 轉 ASCII
		this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
		logMessage(this.logContext);
		
        rtnCode = new ENCHelper(getATMtxData()).checkAtmMac(feptxn.getFeptxnAtmno(), StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 742), ATMMAC); 
        
        this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
	}

	/**
	 * 4. SendToCBS/ASC: 送主機檢核帳戶資料
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
			// 轉入方為本行時,先送CBS查詢帳號
			feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
			String AATxTYPE = "0"; // 上CBS查詢、檢核
			String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
			rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
			tota = hostAA.getTota();
		}
	}

	/**
	 * 8. SendToCBS/ASC: 送主機處理帳務
	 *
	 * @throws Exception
	 */
	private void sendToCBS2() throws Exception {
		if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlCbsFlag())) {
			/* 進主機入扣帳/手續費 */
			String AATxTYPE = "1"; // 上CBS入扣帳
			String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
			rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE);
			tota = hostAA.getTota();
		}
	}

	/**
	 * 9. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
	 *
	 * @throws Exception
	 */
	private String labelEndOfFunc() throws Exception {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			ATMGeneralRequest atmReq = this.getATMRequest();
			String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
					FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
			ATMENCHelper atmEncHelper = new ATMENCHelper(this.getATMtxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				ATM_FAA_CC1B2PC atm_faa_cc1b2pc = new ATM_FAA_CC1B2PC();
				// 組Header(OUTPUT-1)
				atm_faa_cc1b2pc.setWSID(atmReq.getWSID());
				atm_faa_cc1b2pc.setRECFMT("1");
				atm_faa_cc1b2pc.setMSGCAT("F");
				atm_faa_cc1b2pc.setMSGTYP("PC"); // - response
				atm_faa_cc1b2pc.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
				atm_faa_cc1b2pc.setTRANTIME(systemTime.substring(8,14)); // 系統時間
				atm_faa_cc1b2pc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1b2pc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1b2pc.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				atm_faa_cc1b2pc.setDATATYPE("D0");
				atm_faa_cc1b2pc.setDATALEN("004");
				atm_faa_cc1b2pc.setACKNOW("0");
				// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
				String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
						getATMtxData().getLogContext());
				// 其他未列入的代碼，一律回 226
				if (StringUtils.isBlank(pageNo)) {
					pageNo = "226"; // 交易不能處理
				}
				atm_faa_cc1b2pc.setPAGENO(pageNo);

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1b2pc.setPTYPE("S0");
				atm_faa_cc1b2pc.setPLEN("191");
				atm_faa_cc1b2pc.setPBMPNO("010000"); // FPC
				// 西元年轉民國年
				atm_faa_cc1b2pc.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
				atm_faa_cc1b2pc.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
				atm_faa_cc1b2pc.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1b2pc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
				// 格式:$999.0 ex :$0.0
				atm_faa_cc1b2pc.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnNpsFeeCustpay(), "$#,##0.0"));
				atm_faa_cc1b2pc.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1b2pc.setPSTAN(feptxn.getFeptxnStan());
				// 轉入行
				atm_faa_cc1b2pc.setPITXBKNO(feptxn.getFeptxnTrinBkno());
				// ATM回應代碼(空白放 "000") // [20221216]
				atm_faa_cc1b2pc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
						getATMtxData().getLogContext()));
				// 轉出行
				atm_faa_cc1b2pc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_cc1b2pc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)
					atm_faa_cc1b2pc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b2pc.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
                    //轉出帳號(明細表顯示內容)
					atm_faa_cc1b2pc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}
				rfs.set("");
				rtnMessage = atm_faa_cc1b2pc.makeMessage();
				rtnCode = atmEncHelper.makeAtmMac(feptxn.getFeptxnAtmno(), rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1b2pc.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1b2pc.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
				rtnMessage = atm_faa_cc1b2pc.makeMessage();
			} else {
				ATM_FAA_CC1B2PN atm_faa_cc1b2pn = new ATM_FAA_CC1B2PN();
				// 組Header(OUTPUT-1)
				atm_faa_cc1b2pn.setWSID(atmReq.getWSID());
				atm_faa_cc1b2pn.setRECFMT("1");
				atm_faa_cc1b2pn.setMSGCAT("F");
				atm_faa_cc1b2pn.setMSGTYP("PN"); // + response
				atm_faa_cc1b2pn.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
				atm_faa_cc1b2pn.setTRANTIME(systemTime.substring(8,14)); // 系統時間
				atm_faa_cc1b2pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1b2pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1b2pn.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1b2pn.setPTYPE("S0");
				atm_faa_cc1b2pn.setPLEN("191");
				atm_faa_cc1b2pn.setPBMPNO("000010"); // FPN
				// 西元年轉民國年
				atm_faa_cc1b2pn.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
				atm_faa_cc1b2pn.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
				atm_faa_cc1b2pn.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1b2pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
				// 格式:$999.0 ex :$0.0
				atm_faa_cc1b2pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnNpsFeeCustpay(), "$#,##0.0"));
				atm_faa_cc1b2pn.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1b2pn.setPSTAN(feptxn.getFeptxnStan());
				// 轉入行
				atm_faa_cc1b2pn.setPITXBKNO(feptxn.getFeptxnTrinBkno());
				// ATM回應代碼(空白放 "000") // [20221216]
				atm_faa_cc1b2pn.setPRCCODE(feptxn.getFeptxnCbsRc());
				// 轉出行
				atm_faa_cc1b2pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_cc1b2pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)
					atm_faa_cc1b2pn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b2pn.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
					//轉出帳號(明細表顯示內容)
					atm_faa_cc1b2pn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}
				rfs.set("");
				rtnMessage = atm_faa_cc1b2pn.makeMessage();
				rtnCode = atmEncHelper.makeAtmMac(feptxn.getFeptxnAtmno(), rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1b2pn.setMACCODE(""); /* 訊息押碼 */
				} else {
					atm_faa_cc1b2pn.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
				logMessage(this.logContext);
				rtnMessage = atm_faa_cc1b2pn.makeMessage();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}

	/**
	 * 10. 判斷是否需組 CON 電文回財金
	 */
	private void updateTxData() {
		String feptxnRepRc = feptxn.getFeptxnRepRc();
		if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
			feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
			feptxn.setFeptxnReplyCode("    "); // 4個SPACES
			feptxn.setFeptxnTxrust("B"); /* 成功 */
			feptxn.setFeptxnConRc("4001"); /* +CON */
			rtnCode2 = getFiscBusiness().sendConfirmToFISC();
		} else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
			feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
			if ("4001".equals(feptxnRepRc)) { /* +REP */
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
						FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
				feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
				if (!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
					feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
							FEPChannel.FEP, FEPChannel.FISC, getATMtxData().getLogContext()));
					rtnCode2 = getFiscBusiness().sendConfirmToFISC();
				}
			} else { /* -REP */
				feptxn.setFeptxnTxrust("R"); /* Reject-normal */
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
						getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
			}
		} else {
			/* FEPReturnCode <> Normal */
			/* 2020/3/6 修改，主機有回應錯誤時，修改交易結果 */
			if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc())){
				feptxn.setFeptxnTxrust("R"); /* Reject-normal */
			}else{
				feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
			}

			if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
						FEPChannel.FEP, getATMtxData().getTxChannel(), getATMtxData().getLogContext()));
			}
		}
		feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
		this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
	}

	/**
	 * 11. 更新交易記錄(FEPTXN)
	 */
	private void updateTxData2() {
		if (rtnCode != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode.getValue());
		} else if (rtnCode2 != FEPReturnCode.Normal) {
			feptxn.setFeptxnAaRc(rtnCode2.getValue());
		} else {
			feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}
		feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
		this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
	}

	/**
	 * 更新feptxn
	 *
	 * @return
	 */
	private void updateFeptxn() {
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFeptxn"));
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFeptxn");
			sendEMS(getLogContext());
		}
	}
}
