package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B2PC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B2PN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APN;

/**
 * @author vincent
 */
public class PYSelfIssue extends ATMPAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private String atmno;
	private boolean isGarbageR = false;

	public PYSelfIssue(ATMData txnData) throws Exception {
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
			rtnCode = getATMBusiness().prepareFEPTXN();
			if (rtnCode != FEPReturnCode.Normal) {
				// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
				getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
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
			if (!"POS".equals(feptxn.getFeptxnChannel())) {
				if (rtnCode != FEPReturnCode.Normal) {
					setFeptxnErrorCode(FEPChannel.FEP, FEPChannel.ATM);
				}
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
				if (StringUtils.isBlank(getTxData().getTxResponseMessage())) {
					rtnMessage = this.response();
					if ("EAT".equals(feptxn.getFeptxnChannel())) {
						rtnMessage = this.eatmResponse(rtnMessage);
					}
				} else {
					rtnMessage = getTxData().getTxResponseMessage();
				}
			}else {
				//8. 	GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler 
				rtnMessage = this.garbageResponse();
				if ("EAT".equals(feptxn.getFeptxnChannel())) {
					rtnMessage = this.eatmGarbageResponse(rtnMessage);
				}
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
	 * @throws Exception
	 */
	private void addTxData() throws Exception {
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

	/**
	 * 3. CheckBusinessRule: 商業邏輯檢核
	 *
	 * @return
	 * @throws Exception
	 */
	private void checkBusinessRule() throws Exception {
		// 3.1 檢核 ATM 電文
		rtnCode = getATMBusiness().CheckATMData();
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4   /* FEP檢核錯誤處理*/
		}

		// 3.2 檢核單筆限額
		/*
		 * 自行繳費: MSGCTL_CHECK_LIMIT=2 1. 全國繳費單筆交易限額: 單筆限額 200萬 2. 「繳費移轉計畫」視為約定轉帳:
		 * 單筆限額200萬
		 */
		rtnCode = getATMBusiness().checkTransLimit(getTxData().getMsgCtl());
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4   /* FEP檢核錯誤處理*/
		}

		// 3.3 檢核ATM電文訊息押碼(MAC)
		if("POS".equals(feptxn.getFeptxnChannel())) {
        	return;
        }
		
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_TITA_PICCMACD = getTxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError; 
			return; // GO TO 4   /* FEP檢核錯誤處理*/
		}

		String ATMMAC = ATM_TITA_PICCMACD;
		this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
		logMessage(this.logContext);
		
		// CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
        atmno = feptxn.getFeptxnAtmno();
		
		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno, StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 742), ATMMAC);
        
		this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
        logMessage(this.logContext);
	}

	/**
	 * 5. SendToCBS:送往CBS主機處理
	 *
	 * @throws Exception
	 */
	private void sendToCBS() throws Exception {
		/* 交易記帳處理 */
		String AATxTYPE = "1"; // 上CBS入扣帳
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
			FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
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
			isGarbageR = true;// GO TO 8 /*組GarbageResponse回覆ATM */
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
			ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				ATM_FAA_CC1B2PC atm_faa_cc1b2pc = new ATM_FAA_CC1B2PC();
				// 組Header(OUTPUT-1)
				atm_faa_cc1b2pc.setWSID(atmReq.getWSID());
				atm_faa_cc1b2pc.setRECFMT("1");
				atm_faa_cc1b2pc.setMSGCAT("F");
				atm_faa_cc1b2pc.setMSGTYP("PC"); // - response
				atm_faa_cc1b2pc.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1b2pc.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1b2pc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1b2pc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1b2pc.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

				// 組D0(OUTPUT-2)畫面顯示(Display message)
				// 組 D0(004)
				atm_faa_cc1b2pc.setDATATYPE("D0");
				atm_faa_cc1b2pc.setDATALEN("004");
				atm_faa_cc1b2pc.setACKNOW("0");
				// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
				String pageNo = TxHelper.getRCFromErrorCode(feptxn.getFeptxnCbsRc(), FEPChannel.CBS, FEPChannel.ATM,
						getTxData().getLogContext());
				// 其他未列入的代碼，一律回 226
				if (StringUtils.isBlank(pageNo)) {
					pageNo = "226"; // 交易不能處理
				}
				atm_faa_cc1b2pc.setPAGENO(pageNo);

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1b2pc.setPTYPE("S0");
				atm_faa_cc1b2pc.setPLEN("193");
				atm_faa_cc1b2pc.setPBMPNO("010000"); // FPC
				// 西元年轉民國年
				atm_faa_cc1b2pc.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
				atm_faa_cc1b2pc.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
				atm_faa_cc1b2pc.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1b2pc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
				// 格式:$999 ex :$0
				atm_faa_cc1b2pc.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"));
				//格式 :正值放$,負值放-,$99,999,999,999.00(共18位)右靠左補空白
				BigDecimal feptxnBalb = feptxn.getFeptxnBalb();
                if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
                	atm_faa_cc1b2pc.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
                } else {
                	atm_faa_cc1b2pc.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
                }
				atm_faa_cc1b2pc.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1b2pc.setPSTAN(feptxn.getFeptxnStan());
				// 轉帳或存款提供以下資料
				// ATM回應代碼(空白放 "000") // [20221216]
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1b2pc.setPRCCODE(feptxn.getFeptxnReplyCode());
				}else {
					atm_faa_cc1b2pc.setPRCCODE(feptxn.getFeptxnCbsRc()); //主機三碼表示交易成功
				}
				
				// 轉出行
				atm_faa_cc1b2pc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				// 提領外幣
				// 處理有收到CBS Response的欄位值
				if (tota != null) {
					//交易種類
					atm_faa_cc1b2pc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)
					atm_faa_cc1b2pc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b2pc.setPTRINACCT(this.getImsPropertiesValue(tota,ImsMethodName.TOACT.getValue()));
					//CBS下送ATM的促銷應用訊息
					atm_faa_cc1b2pc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}
				
				if(!"POS".equals(feptxn.getFeptxnChannel())) {  //暫不驗MAC
					rfs.set("");
					rtnMessage = atm_faa_cc1b2pc.makeMessage();
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
					if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b2pc.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
						atm_faa_cc1b2pc.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_cc1b2pc.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
				}
				rtnMessage = atm_faa_cc1b2pc.makeMessage();
			} else {
				ATM_FAA_CC1B2PN atm_faa_cc1b2pn = new ATM_FAA_CC1B2PN();
				// 組Header(OUTPUT-1)
				atm_faa_cc1b2pn.setWSID(atmReq.getWSID());
				atm_faa_cc1b2pn.setRECFMT("1");
				atm_faa_cc1b2pn.setMSGCAT("F");
				atm_faa_cc1b2pn.setMSGTYP("PN"); // + response
				atm_faa_cc1b2pn.setTRANDATE(atmReq.getTRANDATE());
				atm_faa_cc1b2pn.setTRANTIME(atmReq.getTRANTIME());
				atm_faa_cc1b2pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
				atm_faa_cc1b2pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
				atm_faa_cc1b2pn.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

				// 組S0(OUTPUT-3)明細表內容(PRINT message),依交易下送欄位
				atm_faa_cc1b2pn.setPTYPE("S0");
				atm_faa_cc1b2pn.setPLEN("193");
				atm_faa_cc1b2pn.setPBMPNO("000010"); // FPN
				// 西元年轉民國年
				atm_faa_cc1b2pn.setPDATE(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDateAtm()));
				atm_faa_cc1b2pn.setPTIME(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxTime()));
				atm_faa_cc1b2pn.setPTID(feptxn.getFeptxnAtmno());
				// 格式 :$$$,$$$,$$9 ex :$10,000
				atm_faa_cc1b2pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
				// 格式:$999 ex :$0
				atm_faa_cc1b2pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpay(), "$#,##0"));
				//格式 :正值放$,負值放-,$99,999,999,999.00(共18位)右靠左補空白
				BigDecimal feptxnBalb = feptxn.getFeptxnBalb();
                if (feptxnBalb.compareTo(BigDecimal.ZERO) >= 0) {
                	atm_faa_cc1b2pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "$#,##0.00"),18," "));
                } else {
                	atm_faa_cc1b2pn.setPBAL(StringUtils.leftPad(FormatUtil.decimalFormat(feptxnBalb, "-#,##0.00"),18," "));
                }
				atm_faa_cc1b2pn.setPATXBKNO(feptxn.getFeptxnBkno());
				atm_faa_cc1b2pn.setPSTAN(feptxn.getFeptxnStan());
				// 轉帳或存款提供以下資料
				// ATM回應代碼(空白放 "000") // [20221216]
				if (rtnCode != FEPReturnCode.Normal) {
					atm_faa_cc1b2pn.setPRCCODE(feptxn.getFeptxnReplyCode());
				}else {
					atm_faa_cc1b2pn.setPRCCODE(feptxn.getFeptxnCbsRc()); //主機三碼表示交易成功
				}
				
				// 轉出行
				atm_faa_cc1b2pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
				if (tota != null) {
					//交易種類
					atm_faa_cc1b2pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
					//轉出帳號(明細表顯示內容)
					atm_faa_cc1b2pn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
					//轉入帳號(明細表顯示內容)
					atm_faa_cc1b2pn.setPTRINACCT(this.getImsPropertiesValue(tota,ImsMethodName.TOACT.getValue()));
					//CBS下送ATM的促銷應用訊息
					atm_faa_cc1b2pn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
				}
				
				if(!"POS".equals(feptxn.getFeptxnChannel())) {  //暫不驗MAC
					rfs.set("");
					rtnMessage = atm_faa_cc1b2pn.makeMessage();
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
					if (rtnCode != FEPReturnCode.Normal) {
						atm_faa_cc1b2pn.setPRCCODE(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
								getTxData().getLogContext()));
						atm_faa_cc1b2pn.setMACCODE(""); /* 訊息押碼 */
					} else {
						atm_faa_cc1b2pn.setMACCODE(rfs.get()); /* 訊息押碼 */
					}
					
					this.logContext.setMessage("after makeAtmMac RC:" + rtnCode.toString());
					logMessage(this.logContext);
				}
				
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
	 * 8. Response:組 EATM 回應電文 & 回 ATMMsgHandler
	 * 
	 * @throws Exception
	 */
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
	
	private void setFeptxnErrorCode(FEPChannel channel_1, FEPChannel channel_2) {
		setFeptxnErrorCode(Objects.toString(rtnCode.getValue()), channel_1, channel_2);
	}
	
	private void setFeptxnErrorCode(String errCode, FEPChannel channel_1, FEPChannel channel_2) {
		feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(errCode, channel_1, channel_2, getTxData().getLogContext()));
		feptxn.setFeptxnAaRc(rtnCode.getValue());
	}
}
