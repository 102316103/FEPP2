package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
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
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Calendar;
import java.util.Objects;

/**
 * 負責處理 ATM 發動跨行存款交易電文
 *
 * @author mickey
 */
public class DPOtherRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
	private String atmno;

	public DPOtherRequestA(INBKData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() throws Exception {
		String rtnMessage = "";
		try {
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
				// 4. SendToCBS/ASC(if need): 本行卡-進帳務主機查詢帳號
				this.SendToCBSorASC4();
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
				repRcEq4001 = NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc());
			}

			Short msgctlUpdateAptot = getTxData().getMsgCtl().getMsgctlUpdateAptot();
			if (rtnCode == FEPReturnCode.Normal && repRcEq4001 && msgctlUpdateAptot == 1) {
				// 7. ProcessAPTOT:更新跨行代收付
				rtnCode = fiscBusiness.processAptot(false);
			}

			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				// 8. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
				this.SendToCBSorASC8();
			}

			// 9. label_END_OF_FUNC: 組ATM回應電文 & 回 ATMMsgHandler
			if(!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
				rtnMessage = this.labelEndOfFunc();
			}
			// 10. 判斷是否需組 CON 電文回財金
			this.sendToConfirm();

		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		}

		try {
			// 11. 更新交易記錄(FEPTXN)
			this.updateTxData();

			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
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
	 * 2. AddTxData: 新增交易記錄(FEPTXN)
	 *
	 * @return
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
	 * 3. CheckBusinessRule: 商業邏輯檢核
	 *
	 * @return
	 * @throws Exception
	 */
	private void checkBusinessRule() throws Exception {
		// 3.1 檢核ATM電文
		rtnCode = checkRequestFromATM(getATMtxData());
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
		}
		
		// 3.2 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError;
			return; // GO TO label_END_OF_FUNC /* 組回傳 ATM 電文 */
		}

		atmno= EbcdicConverter.toHex(CCSID.English, feptxn.getFeptxnAtmno().length(), feptxn.getFeptxnAtmno());
		rtnCode = new ENCHelper(getTxData())
				.checkAtmMac(atmno,StringUtils.substring(getATMtxData().getTxRequestMessage(), 18, 371), ATM_REQ_PICCMACD); // EBCDIC(36,742)
		
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
		}
		
		// 3.3 檢核單筆限額
		rtnCode = getATMBusiness().checkTransLimit(getTxData().getMsgCtl());
	}

	/**
	 * 4. SendToCBS/ASC(if need): 本行卡-進帳務主機查詢帳號
	 *
	 * @throws Exception
	 */
	private void SendToCBSorASC4() throws Exception {
		if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
			// 本行卡,先送CBS查詢帳號
			feptxn.setFeptxnTxrust("S"); // Reject-abnormal
			String AATxTYPE = "0"; // 上CBS查詢、檢核
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
			tota = hostAA.getTota();
		}
	}

	/**
	 * 8. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
	 *
	 * @throws Exception
	 */
	private void SendToCBSorASC8() throws Exception {
		if (getTxData().getMsgCtl().getMsgctlCbsFlag() == 1) {
			/* 進主機入扣帳/手續費 */
			String AATxTYPE = "1"; // 上CBS入扣帳
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
			tota = hostAA.getTota();
			if (rtnCode != FEPReturnCode.Normal && DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlCbsFlag())) {
				/* 沖回跨行代收付(APTOT) */
				rtnCode2 = getFiscBusiness().processAptot(true);
			}
		}
	}

	/**
	 * 9. label_END_OF_FUNC: 組ATM回應電文 & 回 ATMMsgHandler
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
			ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				ATM_FAA_CC1APC msgbody = new ATM_FAA_CC1APC();
				msgbody.setWSID(atmReq.getWSID());
				msgbody.setRECFMT("1");
				msgbody.setMSGCAT("F");
				msgbody.setMSGTYP("PC"); // - response
				msgbody.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
				msgbody.setTRANTIME(systemTime.substring(8,14)); // 系統時間
				msgbody.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				msgbody.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				msgbody.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				msgbody.setDATATYPE("D0");
				msgbody.setDATALEN("004");
				msgbody.setACKNOW("0");
				// 以CBS_RC取得轉換後的PBMDPO編號
				msgbody.setPAGENO(TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.FEP, FEPChannel.ATM,
						this.getTxData().getLogContext()));
				// 其他未列入的代碼，一律回 226
				if (StringUtils.isBlank(msgbody.getPAGENO())) {
					msgbody.setPAGENO("226"); // 交易不能處理
				}
				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				msgbody.setPTYPE("S0");
				msgbody.setPLEN("191");
				msgbody.setPBMPNO("010000"); // FPC
				// 西元年轉民國年，格式：YYY/MM/DD
				msgbody.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
				// 時間格式：HH :MM :SS
				msgbody.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				msgbody.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				msgbody.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmt(), "$#,##0"));
				// 格式:$999 ex :$0
				msgbody.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
				msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
				msgbody.setPSTAN(feptxn.getFeptxnStan());
				// CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				msgbody.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
				// ATM回應代碼(空白放 "000")
				msgbody.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
						getTxData().getLogContext()));
				// 轉出行
				msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 轉入行
				msgbody.setPITXBKNO(feptxn.getFeptxnTrinBkno());

				if (tota != null) {
					//交易種類
					msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)
					msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
					//轉入帳號(明細表顯示內容)
					msgbody.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
					//促銷應用訊息
					msgbody.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}
				rfs.set("");
				rtnMessage = msgbody.makeMessage();
				rtnCode = atmEncHelper.makeAtmMac(atmno,rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					msgbody.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getTxData().getLogContext()));
					msgbody.setMACCODE(""); /* 訊息押碼 */
				} else {
					msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				rtnMessage = msgbody.makeMessage();
			} else {
				ATM_FAA_CC1B1PN msgbody = new ATM_FAA_CC1B1PN();
				msgbody.setWSID(atmReq.getWSID());
				msgbody.setRECFMT("1");
				msgbody.setMSGCAT("F");
				msgbody.setMSGTYP("PN"); // + response
				msgbody.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
				msgbody.setTRANTIME(systemTime.substring(8,14)); // 系統時間
				msgbody.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				msgbody.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				msgbody.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”
				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				msgbody.setPTYPE("S0");
				msgbody.setPLEN("191");
				msgbody.setPBMPNO("000010"); // FPN
				// 西元年轉民國年，格式：YYY/MM/DD
				msgbody.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
				// 時間格式：HH :MM :SS
				msgbody.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
				msgbody.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				msgbody.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
				// 格式:$999 ex :$0
				msgbody.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));

				msgbody.setPATXBKNO(feptxn.getFeptxnBkno());
				msgbody.setPSTAN(feptxn.getFeptxnStan());
				// CBS記帳日(轉民國年,需格式化為YY/MM/DD,年度取後兩碼,ex :11/06/29)
				msgbody.setPBUSINESSDATE(this.dateStrToYYMMDD(feptxn.getFeptxnTbsdy()));
				// ATM回應代碼(空白放 "000")
				msgbody.setPRCCODE(feptxn.getFeptxnCbsRc());
				// 轉出行
				msgbody.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 轉入行
				msgbody.setPITXBKNO(feptxn.getFeptxnTrinBkno());
				if (tota != null) {
					//交易種類
					msgbody.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)
					msgbody.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
					//轉入帳號(明細表顯示內容)
					msgbody.setPTRINACCT(this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue()));
					//促銷應用訊息
					msgbody.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}
				rfs.set("");
				rtnMessage = msgbody.makeMessage();
				rtnCode = atmEncHelper.makeAtmMac(atmno,rtnMessage, rfs);
				if (rtnCode != FEPReturnCode.Normal) {
					msgbody.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
							getTxData().getLogContext()));
					msgbody.setMACCODE(""); /* 訊息押碼 */
				} else {
					msgbody.setMACCODE(rfs.get()); /* 訊息押碼 */
				}
				rtnMessage = msgbody.makeMessage();
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
	private void sendToConfirm() {
		String feptxnRepRc = feptxn.getFeptxnRepRc();
		if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
			feptxn.setFeptxnReplyCode("    "); // 4個SPACES
			feptxn.setFeptxnTxrust("B"); /* 成功 */
			feptxn.setFeptxnPending((short)2);
			feptxn.setFeptxnConRc("4001"); /* +CON */
			rtnCode2 = getFiscBusiness().sendConfirmToFISC();
			// 組回應電文回給 ATM, 寫入 ATM Response Queue 
		} else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
			feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
			if ("4001".equals(feptxnRepRc)) { /* +REP */
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
						FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
				feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
				if(!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())){
					feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
							FEPChannel.FEP, FEPChannel.FISC, getTxData().getLogContext())); /* +CON */
					rtnCode2 = getFiscBusiness().sendConfirmToFISC();
				}
			} else { /* -REP */
				feptxn.setFeptxnTxrust("R"); /* Reject-normal */
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
						getTxData().getTxChannel(), getTxData().getLogContext()));
			}
		} else {
			/* FEPReturnCode <> Normal */
			/* 2020/3/6 修改，主機有回應錯誤時，修改交易結果 */
			feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */

			if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
						FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
			}
		}

		feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
		this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
	}

	/**
	 * 11. 更新交易記錄(FEPTXN)
	 */
	private void updateTxData() {
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
	 * 轉民國年,格式化為YY/MM/DD,年度取後碼,ex :11/06/29
	 *
	 * @param dateStr
	 * @return
	 */
	private String dateStrToYYMMDD(String dateStr) {
		String rtnDate;
		if ("00000000".equals(dateStr) || dateStr.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
			rtnDate = "00/00/00";
		} else {
			dateStr = CalendarUtil.adStringToROCString(dateStr);
			int dateStrLength = dateStr.length();
			dateStr = dateStr.substring(dateStrLength - 6, dateStrLength).replaceAll("(.{2})", "$1/");
			rtnDate = dateStr.substring(0, dateStr.length() - 1);
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

	/**
	 * 更新feptxn
	 */
	private void updateFeptxn() {
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFeptxn");
			sendEMS(getLogContext());
		}
	}
}
