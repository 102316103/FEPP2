package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B3PC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B3PN;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Calendar;
import java.util.Objects;

/**
 * @author vincent
 */
public class WDOtherRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
	private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
	private Nwdtxn nwdtxn;
	private String atmno;
	private String tita ;


	public WDOtherRequestA(ATMData txnData) throws Exception {
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
			tita =EbcdicConverter.fromHex(CCSID.English, this.getTxData().getTxRequestMessage());
			this.logContext.setProgramFlowType(ProgramFlow.AAIn);
			this.logContext.setMessageFlowType(MessageFlow.Request);
			this.logContext.setProgramName(StringUtils.join(this.getATMtxData().getAaName(), ".processRequestData"));
			this.logContext.setMessage("ASCII TITA:"+tita);
			this.logContext.setRemark(StringUtils.join("Enter ", this.getATMtxData().getAaName()));
			logMessage(this.logContext);

			// 1. Prepare : 交易記錄初始資料
			rtnCode = getATMBusiness().prepareFEPTXN();
			if (rtnCode == FEPReturnCode.Normal) {
				if ("W2".equals(feptxn.getFeptxnTxCode())) { // 無卡提款
					feptxn.setFeptxnAtmType("6071"); // 端末設備型態=無實體卡片
					feptxn.setFeptxnTroutActno(StringUtils.repeat('0', 16)); // 補滿16位0
					feptxn.setFeptxnMajorActno(StringUtils.substring(getATMRequest().getFADATA(), 2, 18)); // 提款序號
					RefBase<Nwdtxn> nwdtxnRefBase = new RefBase<>(new Nwdtxn());
					rtnCode = getATMBusiness().prepareNWDTXN(nwdtxnRefBase);
					nwdtxn = nwdtxnRefBase.get();
				}
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 2. AddTxData: 新增交易記錄(FEPTXN)
				this.addTxData();
			}

			if (rtnCode == FEPReturnCode.Normal) {
				// 3. CheckBusinessRule: 商業邏輯檢核
				rtnCode = this.checkBusinessRule();
			}

			FISC fiscBusiness = getFiscBusiness();
			if (rtnCode == FEPReturnCode.Normal) {
				// 4. 組送往 FISC 之 Request 電文並等待財金之 Response
				/* for 無卡提款 */
				if ("W2".equals(feptxn.getFeptxnTxCode())) {
					rtnCode = fiscBusiness.sendNCRequestToFISC(getATMRequest());
				} else {
					rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
				}
			}

			boolean repRcEq4001 = true;
			if (rtnCode == FEPReturnCode.Normal) {
				// 5. CheckResponseFromFISC:檢核回應電文是否正確
				rtnCode = fiscBusiness.checkResponseMessage();
				repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
			}

			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				// 6. ProcessAPTOT:更新跨行代收付
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					rtnCode = fiscBusiness.processAptot(false);
				}
			}

			if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
				// 7. SendToCBS/ASC(if need): 代理提款-進帳務主機掛現金帳
				this.sendToCBS();
			}

			// 8. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
            if(!DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                rtnMessage = this.labelEndOfFunc();
            }
			// 9. 判斷是否需組 CON 電文回財金
			this.updateTxData();

		} catch (Exception ex) {
			rtnMessage = "";
			rtnCode = FEPReturnCode.ProgramException;
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		}

		try {
			// 10. 更新交易記錄(FEPTXN & NWDTXN)
			this.updateTxData2();

			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
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
	 * 2. AddTxData: 新增交易記錄( FEPTxn)
	 *
	 * @return
	 * @throws Exception
	 */
	private void addTxData() throws Exception {
		if ("W2".equals(feptxn.getFeptxnTxCode())) {
			/* 無卡提款交易, 以 TRANSACTION 新增交易記錄 */
			PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
			TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
			try {
				// 新增交易記錄(FEPTxn) Returning FEPReturnCode
				/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
				FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
				String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
				feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".addTxData"));
				int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
				if (insertCount <= 0) { // 新增失敗
					rtnCode = FEPReturnCode.FEPTXNInsertError;
					transactionManager.rollback(txStatus);
					return;
				}
				// 新增交易記錄(NWDTxn) Returning FEPReturnCode
				insertCount = nwdtxnMapper.insertSelective(nwdtxn);
				if (insertCount <= 0) { // 新增失敗
					rtnCode = FEPReturnCode.FEPTXNInsertError;
					transactionManager.rollback(txStatus);
					return;
				}
				transactionManager.commit(txStatus);
			} catch (Exception ex) {
				transactionManager.rollback(txStatus);
				getLogContext().setProgramException(ex);
				getLogContext().setProgramName(ProgramName + ".addTxData");
				sendEMS(getLogContext());
			}
		} else {
			try {
				// 新增交易記錄(FEPTxn) Returning FEPReturnCode
				/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
				FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
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
	}

	/**
	 * 3. CheckBusinessRule: 商業邏輯檢核
	 *
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode checkBusinessRule() throws Exception {
		rtnCode = checkRequestFromATM(getATMtxData());
        if (rtnCode != FEPReturnCode.Normal) {
			return rtnCode;
		}
		
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_REQ_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_REQ_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError;
			return rtnCode;
		}

		this.logContext.setMessage("Begin checkAtmMac mac:" + ATM_REQ_PICCMACD);
		logMessage(this.logContext);
		atmno= EbcdicConverter.toHex(CCSID.English, feptxn.getFeptxnAtmno().length(), feptxn.getFeptxnAtmno());
		rtnCode = new ENCHelper(getTxData())
				.checkAtmMacNew(atmno,StringUtils.substring(this.getTxData().getTxRequestMessage(), 36, 742), ATM_REQ_PICCMACD); // EBCDIC(36,742)
		this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
		return rtnCode;
	}

	/**
	 * 7. SendToCBS/ASC(if need): 代理提款-進帳務主機掛現金帳
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlCbsFlag()) && StringUtils.isBlank(feptxn.getFeptxnTrinBkno())) {
			/* 進主機掛現金帳 */
			String AATxTYPE = "1"; // 上CBS入扣帳
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			feptxn.setFeptxnCbsTxCode(AA);
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
			tota = hostAA.getTota();
			if (rtnCode != FEPReturnCode.Normal) {
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					/* 沖回跨行代收付(APTOT) */
					rtnCode2 = getFiscBusiness().processAptot(true);
				}
				// GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
			}
		}
	}

	/**
	 * 8. label_END_OF_FUNC :組ATM回應電文 & 回 ATMMsgHandler
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
				switch (feptxn.getFeptxnTxCode()) {
				case "US": // 外幣提款交易
				case "JP": // 外幣提款交易
					ATM_FAA_CC1B3PC atm_faa_cc1b3pc = new ATM_FAA_CC1B3PC();
					// 組Header(OUTPUT-1)
					atm_faa_cc1b3pc.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pc.setRECFMT("1");
					atm_faa_cc1b3pc.setMSGCAT("F");
					atm_faa_cc1b3pc.setMSGTYP("PC"); // - response
					atm_faa_cc1b3pc.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
					atm_faa_cc1b3pc.setTRANTIME(systemTime.substring(8,14)); // 系統時間
					atm_faa_cc1b3pc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b3pc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pc.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組D0(OUTPUT-2)畫面顯示(Display message)
					// 組 D0(004)
					atm_faa_cc1b3pc.setDATATYPE("D0");
					atm_faa_cc1b3pc.setDATALEN("004");
					atm_faa_cc1b3pc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
							getTxData().getLogContext());
					// 其他未列入的代碼，一律回 226
					if (StringUtils.isBlank(pageNo)) {
						pageNo = "226"; // 交易不能處理
					}
					atm_faa_cc1b3pc.setPAGENO(pageNo);

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1b3pc.setPTYPE("S0");
					atm_faa_cc1b3pc.setPLEN("215");
					atm_faa_cc1b3pc.setPBMPNO("010000"); // FPC
					// 西元年轉民國年
					atm_faa_cc1b3pc.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b3pc.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b3pc.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b3pc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1b3pc.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					atm_faa_cc1b3pc.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b3pc.setPSTAN(feptxn.getFeptxnStan());
					// ATM回應代碼(空白放 "000") // [20221216]
                    atm_faa_cc1b3pc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                            getTxData().getLogContext()));
					// 轉出行
					atm_faa_cc1b3pc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1b3pc.setPARPC(feptxn.getFeptxnLuckyno());
					// 提領外幣
					atm_faa_cc1b3pc.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pc.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1b3pc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)
                        atm_faa_cc1b3pc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                        //轉入帳號(明細表顯示內容)
                        atm_faa_cc1b3pc.setPTMEXNO(this.getImsPropertiesValue(tota, ImsMethodName.FWDTMEX_NO.getValue()));
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1b3pc.makeMessage();
					rtnCode = atmEncHelper.makeAtmMac(atmno,rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b3pc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
                        atm_faa_cc1b3pc.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1b3pc.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
                    rtnMessage = atm_faa_cc1b3pc.makeMessage();
					break;
				default: // 其他提款交易
					ATM_FAA_CC1APC atm_faa_cc1apc = new ATM_FAA_CC1APC();
					// 組Header(OUTPUT-1)
					atm_faa_cc1apc.setWSID(atmReq.getWSID());
					atm_faa_cc1apc.setRECFMT("1");
					atm_faa_cc1apc.setMSGCAT("F");
					atm_faa_cc1apc.setMSGTYP("PC"); // - response
					atm_faa_cc1apc.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
					atm_faa_cc1apc.setTRANTIME(systemTime.substring(8,14)); // 系統時間
					atm_faa_cc1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1apc.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組D0(OUTPUT-2)畫面顯示(Display message)
					// 組 D0(004)
					atm_faa_cc1apc.setDATATYPE("D0");
					atm_faa_cc1apc.setDATALEN("004");
					atm_faa_cc1apc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
							getTxData().getLogContext());
					// 其他未列入的代碼，一律回 226
					if (StringUtils.isBlank(pageNo)) {
						pageNo = "226"; // 交易不能處理
					}
					atm_faa_cc1apc.setPAGENO(pageNo);

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1apc.setPTYPE("S0");
					atm_faa_cc1apc.setPLEN("191");
					atm_faa_cc1apc.setPBMPNO("010000"); // FPC
					// 西元年轉民國年
					atm_faa_cc1apc.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1apc.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
					atm_faa_cc1apc.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1apc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1apc.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					atm_faa_cc1apc.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1apc.setPSTAN(feptxn.getFeptxnStan());
					// ATM回應代碼(空白放 "000") // [20221216]
                    atm_faa_cc1apc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                            getTxData().getLogContext()));
					// 轉出行
					atm_faa_cc1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1apc.setPARPC(feptxn.getFeptxnLuckyno());
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1apc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)
                        atm_faa_cc1apc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1apc.makeMessage();
                    rtnCode = atmEncHelper.makeAtmMac(atmno,rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1apc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
                        atm_faa_cc1apc.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1apc.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
                    rtnMessage = atm_faa_cc1apc.makeMessage();
					break;
				}
			} else {
				switch (feptxn.getFeptxnTxCode()) {
				case "US": // 外幣提款交易
				case "JP": // 外幣提款交易
					ATM_FAA_CC1B3PN atm_faa_cc1b3pn = new ATM_FAA_CC1B3PN();
					// 組Header(OUTPUT-1)
					atm_faa_cc1b3pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pn.setRECFMT("1");
					atm_faa_cc1b3pn.setMSGCAT("F");
					atm_faa_cc1b3pn.setMSGTYP("PN"); // + response
					atm_faa_cc1b3pn.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
					atm_faa_cc1b3pn.setTRANTIME(systemTime.substring(8,14)); // 系統時間
					atm_faa_cc1b3pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b3pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pn.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1b3pn.setPTYPE("S0");
					atm_faa_cc1b3pn.setPLEN("215");
					atm_faa_cc1b3pn.setPBMPNO("000010"); // FPN
					// 西元年轉民國年
					atm_faa_cc1b3pn.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b3pn.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b3pn.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b3pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1b3pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					atm_faa_cc1b3pn.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b3pn.setPSTAN(feptxn.getFeptxnStan());

                    atm_faa_cc1b3pn.setPRCCODE(feptxn.getFeptxnCbsRc());
					// 轉出行
					atm_faa_cc1b3pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1b3pn.setPARPC(feptxn.getFeptxnLuckyno());
					// 提領外幣
					atm_faa_cc1b3pn.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pn.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1b3pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)
                        atm_faa_cc1b3pn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                        //轉入帳號(明細表顯示內容)
                        atm_faa_cc1b3pn.setPTMEXNO(this.getImsPropertiesValue(tota, ImsMethodName.FWDTMEX_NO.getValue()));
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1b3pn.makeMessage();
                    rtnCode = atmEncHelper.makeAtmMac(atmno,rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b3pn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
                        atm_faa_cc1b3pn.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1b3pn.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
                    rtnMessage = atm_faa_cc1b3pn.makeMessage();
					break;
				default: // 其他提款交易
					ATM_FAA_CC1B1PN atm_faa_cc1b1pn = new ATM_FAA_CC1B1PN();
					// 組Header(OUTPUT-1)
					atm_faa_cc1b1pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b1pn.setRECFMT("1");
					atm_faa_cc1b1pn.setMSGCAT("F");
					atm_faa_cc1b1pn.setMSGTYP("PN"); // + response
					atm_faa_cc1b1pn.setTRANDATE(systemTime.substring(2, 8)); // 西元後兩碼+系統月日共六碼
					atm_faa_cc1b1pn.setTRANTIME(systemTime.substring(8,14)); // 系統時間
					atm_faa_cc1b1pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b1pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b1pn.setPRCRDACT("0"); // 晶片卡不留置:固定放”0”

					// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
					atm_faa_cc1b1pn.setPTYPE("S0");
					atm_faa_cc1b1pn.setPLEN("191");
					atm_faa_cc1b1pn.setPBMPNO("000010"); // FPN
					// 西元年轉民國年
					atm_faa_cc1b1pn.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b1pn.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b1pn.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b1pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1b1pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					atm_faa_cc1b1pn.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b1pn.setPSTAN(feptxn.getFeptxnStan());
					// ATM回應代碼(空白放 "000") // [20221216]

                    atm_faa_cc1b1pn.setPRCCODE(feptxn.getFeptxnCbsRc());
					// 轉出行
					atm_faa_cc1b1pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 取得原存行的促銷應用訊息
					atm_faa_cc1b1pn.setPARPC(feptxn.getFeptxnLuckyno());
                    // 處理有收到CBS Response的欄位值
                    if (tota != null) {
                        //交易種類
                        atm_faa_cc1b1pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
                        //轉出帳號(明細表顯示內容)
                        atm_faa_cc1b1pn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
                    }
                    rfs.set("");
                    rtnMessage = atm_faa_cc1b1pn.makeMessage();
                    rtnCode = atmEncHelper.makeAtmMac(atmno,rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b1pn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
                        atm_faa_cc1b1pn.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_faa_cc1b1pn.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
                    rtnMessage = atm_faa_cc1b1pn.makeMessage();
					break;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}

	/**
	 * 9. 判斷是否需組 CON 電文回財金
	 */
	private void updateTxData() {
		String feptxnRepRc = feptxn.getFeptxnRepRc();
		if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
			feptxn.setFeptxnReplyCode("    "); // 4個SPACES
			feptxn.setFeptxnTxrust("B"); /* 成功 */
			feptxn.setFeptxnConRc("4001"); /* +CON */
			rtnCode2 = getFiscBusiness().sendConfirmToFISC();
			// 組回應電文回給 ATM, 寫入 ATM Response Queue 
		} else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
			feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
			if ("4001".equals(feptxnRepRc)) { /* +REP */
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
						FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
				feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
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
	 * 10. 更新交易記錄(FEPTXN & NWDTXN)
	 */
	private void updateTxData2() {
		if ("W2".equals(feptxn.getFeptxnTxCode())) {
			// 更新 FEPTXN
			PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
			TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
			try {
				if (rtnCode != FEPReturnCode.Normal) {
					feptxn.setFeptxnAaRc(rtnCode.getValue());
				} else if (rtnCode2 != FEPReturnCode.Normal) {
					feptxn.setFeptxnAaRc(rtnCode2.getValue());
				} else {
					feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
				}
				feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
				FEPReturnCode updRtnCode = this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
				if (updRtnCode != FEPReturnCode.Normal) {
					transactionManager.rollback(txStatus);
					return;
				}
				// 新增交易記錄(NWDTxn) Returning FEPReturnCode
				Nwdtxn nwdtxn2 = new Nwdtxn();
				nwdtxn2.setNwdtxnTxCurAct(feptxn.getFeptxnTxCurAct());
				nwdtxn2.setNwdtxnTxAmtAct(feptxn.getFeptxnTxAmtAct());
				nwdtxn2.setNwdtxnBrno(feptxn.getFeptxnBrno());
				nwdtxn2.setNwdtxnZoneCode(feptxn.getFeptxnZoneCode());
				nwdtxn2.setNwdtxnRepRc(feptxn.getFeptxnReplyCode());
				nwdtxn2.setNwdtxnTxrust(feptxn.getFeptxnTxrust());
				nwdtxn2.setNwdtxnTroutActno(feptxn.getFeptxnTroutActno());
				nwdtxn2.setNwdtxnIdno(feptxn.getFeptxnIdno());
				/* 20221/5/31 修改 for 將手續費寫入 NWDTXN */
				nwdtxn2.setNwdtxnFeeCur(feptxn.getFeptxnFeeCur());
				nwdtxn2.setNwdtxnFeeCustpay(feptxn.getFeptxnFeeCustpay());
				nwdtxn2.setNwdtxnFeeCustpayAct(feptxn.getFeptxnFeeCustpayAct());
				int updCount = nwdtxnMapper.updateByPrimaryKeySelective(nwdtxn2);
				if (updCount <= 0) { // 更新失敗
					transactionManager.rollback(txStatus);
					return;
				}
				transactionManager.commit(txStatus);
			} catch (Exception ex) {
				transactionManager.rollback(txStatus);
				getLogContext().setProgramException(ex);
				getLogContext().setProgramName(ProgramName + ".updateTxData2");
				sendEMS(getLogContext());
			}
		} else {
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
	}

	/**
	 * 更新feptxn
	 *
	 * @return
	 */
	private FEPReturnCode updateFeptxn() {
		FEPReturnCode fpeReturnCode = FEPReturnCode.Normal;
		try {
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFeptxn"));
			feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
		} catch (Exception ex) {
			fpeReturnCode = FEPReturnCode.FEPTXNUpdateError;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFeptxn");
			sendEMS(getLogContext());
		}
		return fpeReturnCode;
	}
}
