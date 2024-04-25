package com.syscom.fep.server.aa.atmp;

import java.util.Objects;

import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1APC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B1PN;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B3PC;
import com.syscom.fep.vo.text.atm.response.ATM_FAA_CC1B3PN;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;

/**
 * @author vincent
 */
public class WDSelfIssue extends ATMPAABase {
	private Object tota = null;
	private FEPReturnCode rtnCode = FEPReturnCode.Normal;
	private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
	private Nwdtxn nwdtxn;
	private String atmno;
	private String tita ;
	private boolean isGarbageR = false;

	public WDSelfIssue(ATMData txnData) throws Exception {
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
			this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
			this.logContext.setMessage("ASCII TITA:"+tita);
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
			} else if ("W2".equals(feptxn.getFeptxnTxCode())) {
				/* 無卡提款交易寫入NWDTXN */
				feptxn.setFeptxnAtmType("6071"); /* 端末設備型態=無實體卡片 */
				feptxn.setFeptxnMajorActno(StringUtils.substring(getATMRequest().getFADATA(), 2, 18));
				RefBase<Nwdtxn> nwdtxnRefBase = new RefBase<>(new Nwdtxn());// 提款序號
				rtnCode = getATMBusiness().prepareNWDTXN(nwdtxnRefBase);
				if (rtnCode != FEPReturnCode.Normal) {
					// 將 ERROR MSG 送 EVENT MONITOR SYSTEM
					getLogContext().setProgramName(ProgramName + ".prepareNWDTXN");
					getLogContext().setMessage("NWDTXN PREPARE ERROR");
					sendEMS(getLogContext());
					return rtnMessage; // RETUEN 空字串，不回覆ATM
				}
				nwdtxn = nwdtxnRefBase.get();
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

			//4. FEP檢核錯誤處理
			if(rtnCode != FEPReturnCode.Normal){
				feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()), FEPChannel.FEP,
						FEPChannel.ATM, getTxData().getLogContext()));
				feptxn.setFeptxnAaRc(rtnCode.getValue());
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
			getTxData().getLogContext().setMessage("MessageToATM:"+rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logMessage(Level.DEBUG, this.logContext);

			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			String ASCIIMessage = EbcdicConverter.fromHex(CCSID.English,rtnMessage.substring(0,rtnMessage.length()-8)) + rtnMessage.substring(rtnMessage.length()-8,rtnMessage.length());
			getTxData().getLogContext().setMessage("ASCII MessageToATM:"+ASCIIMessage);
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
				rtnCode = FEPReturnCode.FEPTXNInsertError;
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
				rtnCode = FEPReturnCode.FEPTXNInsertError;
				getLogContext().setProgramException(ex);
				getLogContext().setProgramName(ProgramName + ".addTxData");
				sendEMS(getLogContext());
			}
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
			return;
		}

		// 3.2 檢核單筆限額
		/* 自行提款:FEP檢核單筆限額，MSGCTL_CHECK_LIMIT=1 */
		rtnCode = getATMBusiness().checkTransLimit(getTxData().getMsgCtl());
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4 /* FEP檢核錯誤處理 */
		}

		// 3.3 檢核ATM電文訊息押碼(MAC)
		/* 如為晶片卡交易檢核MAC，TAC由CBS檢核 */
		String ATM_TITA_PICCMACD = getTxData().getTxObject().getRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode = FEPReturnCode.ENCCheckMACError; /* MAC Error */
			return; // GO TO 4 /* FEP檢核錯誤處理 */
		}
		String newMac = ATM_TITA_PICCMACD;
		this.logContext.setMessage("Begin checkAtmMac mac:" + newMac);
		logMessage(this.logContext);

		// CHANNEL = "EAT"，用ATMNO = "NEATM001"去押驗，在 CheckATMMACNew replace
		atmno = feptxn.getFeptxnAtmno();
		rtnCode = new ENCHelper(getTxData()).checkAtmMacNew(atmno,
				StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 36, 742),
				newMac);
		this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
		logMessage(this.logContext);
		if (rtnCode != FEPReturnCode.Normal) {
			return; // GO TO 4 /* FEP檢核錯誤處理 */
		}
		
		//3.4	轉換PINBLOCK
		if("W2".equals(feptxn.getFeptxnTxCode())) {
			RefString rfs = new RefString();
			ENCKeyType keyType = ENCKeyType.T3;
			String mode = "00";
			String pin = this.getATMRequest().getIPYDATA().substring(2, 18);
			String atmSeqNo = this.getATMRequest().getTRANSEQ();
			String accno = "";
			rfs.set("");
	
			try {
				this.rtnCode = new ENCHelper(getTxData()).ConvertATMPinToIMS(keyType, mode, atmno, atmSeqNo, accno, pin, rfs);
			} catch (Exception e) {
				getLogContext().setProgramException(e);
				sendEMS(getLogContext());
				this.rtnCode = ENCReturnCode.ENCPINBlockConvertError;
			}
			if (this.rtnCode == FEPReturnCode.Normal) {
				this.logContext.setMessage("new pin:" + rfs.get());
		        logMessage(this.logContext);
				feptxn.setFeptxnPinblock(rfs.get());
			}else {
				return ; // GO TO  4    /* FEP檢核錯誤處理*/
			}
		}
		
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
		
		if("XXXX".equals(IMSRC4_FISC) && "XXX".equals(IMSRC_TCB)) {
			isGarbageR = true;// GO TO  8     /*組GarbageResponse回覆ATM */
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
			String feptxnTxCode = feptxn.getFeptxnTxCode();
			ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
			RefString rfs = new RefString();
			if (rtnCode != FEPReturnCode.Normal) {
				switch (feptxnTxCode) {
				case "US": // 外幣提款交易
				case "JP": // 外幣提款交易
					ATM_FAA_CC1B3PC atm_faa_cc1b3pc = new ATM_FAA_CC1B3PC();
					// 組 Header
					atm_faa_cc1b3pc.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pc.setRECFMT("1");
					atm_faa_cc1b3pc.setMSGCAT("F");
					atm_faa_cc1b3pc.setMSGTYP("PC"); // - response
					atm_faa_cc1b3pc.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1b3pc.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1b3pc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b3pc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pc.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組D0(004)畫面顯示(Display message)
					atm_faa_cc1b3pc.setDATATYPE("D0");
					atm_faa_cc1b3pc.setDATALEN("004");
					atm_faa_cc1b3pc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					String pageNo;
					// 此欄給主機回應的代碼，尚未走到主機就給空值
					if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
						pageNo = feptxn.getFeptxnCbsRc();
					} else {
						pageNo = feptxn.getFeptxnReplyCode();
					}

					atm_faa_cc1b3pc.setPAGENO(pageNo);

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
					atm_faa_cc1b3pc.setPTYPE("S0");
					atm_faa_cc1b3pc.setPLEN("215");
					atm_faa_cc1b3pc.setPBMPNO("010000"); // FPC
					// 西元年轉民國年
					atm_faa_cc1b3pc.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b3pc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b3pc.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b3pc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1b3pc.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					atm_faa_cc1b3pc.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b3pc.setPSTAN(feptxn.getFeptxnStan());

					atm_faa_cc1b3pc.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1b3pc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 提領外幣
					atm_faa_cc1b3pc.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pc.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1b3pc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)
						atm_faa_cc1b3pc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
						//轉出行
						atm_faa_cc1b3pc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
						atm_faa_cc1b3pc.setPTMEXNO(this.getImsPropertiesValue(tota,ImsMethodName.FWDTMEX_NO.getValue()));
					}
					rfs.set("");
					rtnMessage = atm_faa_cc1b3pc.makeMessage();
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
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
					// 組 Header
					atm_faa_cc1apc.setWSID(atmReq.getWSID());
					atm_faa_cc1apc.setRECFMT("1");
					atm_faa_cc1apc.setMSGCAT("F");
					atm_faa_cc1apc.setMSGTYP("PC"); // - response
					atm_faa_cc1apc.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1apc.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1apc.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1apc.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1apc.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組D0(004)畫面顯示(Display message)
					atm_faa_cc1apc.setDATATYPE("D0");
					atm_faa_cc1apc.setDATALEN("004");
					atm_faa_cc1apc.setACKNOW("0");
					// 以CBS_RC取得轉換後的PBMDPO編號 // [20221216]
					
					// 此欄給主機回應的代碼，尚未走到主機就給空值
					if (StringUtils.isBlank(feptxn.getFeptxnCbsRc())) { //交易尚未送主機
						pageNo = feptxn.getFeptxnCbsRc();
					} else {
						pageNo = feptxn.getFeptxnReplyCode();
					}

					atm_faa_cc1apc.setPAGENO(pageNo);

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
					atm_faa_cc1apc.setPTYPE("S0");
					atm_faa_cc1apc.setPLEN("191");
					atm_faa_cc1apc.setPBMPNO("010000"); // FPC
					// 西元年轉民國年
					atm_faa_cc1apc.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1apc.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1apc.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1apc.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1apc.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					atm_faa_cc1apc.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1apc.setPSTAN(feptxn.getFeptxnStan());

					atm_faa_cc1apc.setPRCCODE(feptxn.getFeptxnReplyCode());
					// 轉出行
					atm_faa_cc1apc.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1apc.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)
						atm_faa_cc1apc.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
						//轉出行
						atm_faa_cc1apc.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
					}
					rfs.set("");
					rtnMessage = atm_faa_cc1apc.makeMessage();
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
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
					// 組 Header
					atm_faa_cc1b3pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b3pn.setRECFMT("1");
					atm_faa_cc1b3pn.setMSGCAT("F");
					atm_faa_cc1b3pn.setMSGTYP("PN"); // - response
					atm_faa_cc1b3pn.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1b3pn.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1b3pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b3pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b3pn.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
					atm_faa_cc1b3pn.setPTYPE("S0");
					atm_faa_cc1b3pn.setPLEN("215");
					atm_faa_cc1b3pn.setPBMPNO("000010"); // FPN
					// 西元年轉民國年
					atm_faa_cc1b3pn.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b3pn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
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
					// 提領外幣
					atm_faa_cc1b3pn.setPEXRATE(Objects.toString(feptxn.getFeptxnExrate()));
					atm_faa_cc1b3pn.setPAMT(Objects.toString(feptxn.getFeptxnTxAmt()));
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1b3pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)
						atm_faa_cc1b3pn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
						//轉出行
						atm_faa_cc1b3pn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
						atm_faa_cc1b3pn.setPTMEXNO(this.getImsPropertiesValue(tota,ImsMethodName.FWDTMEX_NO.getValue()));
					}
					rfs.set("");
					rtnMessage = atm_faa_cc1b3pn.makeMessage();
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
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
					// 組 Header
					atm_faa_cc1b1pn.setWSID(atmReq.getWSID());
					atm_faa_cc1b1pn.setRECFMT("1");
					atm_faa_cc1b1pn.setMSGCAT("F");
					atm_faa_cc1b1pn.setMSGTYP("PN"); // + response
					atm_faa_cc1b1pn.setTRANDATE(atmReq.getTRANDATE());
					atm_faa_cc1b1pn.setTRANTIME(atmReq.getTRANTIME());
					atm_faa_cc1b1pn.setTRANSEQ(feptxn.getFeptxnAtmSeqno());
					atm_faa_cc1b1pn.setTDRSEG(atmReq.getTDRSEG()); // 回覆FAA
					atm_faa_cc1b1pn.setPRCRDACT("4"); // 晶片卡不留置:固定放”4”

					// 組S0明細表內容(PRINT message),依交易下送欄位,電文總長度也不同
					atm_faa_cc1b1pn.setPTYPE("S0");
					atm_faa_cc1b1pn.setPLEN("191");
					atm_faa_cc1b1pn.setPBMPNO("000010"); // FPN
					// 西元年轉民國年
					atm_faa_cc1b1pn.setPDATE(this.formatDate(feptxn.getFeptxnTxDateAtm()));
					atm_faa_cc1b1pn.setPTIME(this.formatTime(feptxn.getFeptxnTxTime()));
					atm_faa_cc1b1pn.setPTID(feptxn.getFeptxnAtmno());
					// 格式 :$$$,$$$,$$9 ex :$10,000
					atm_faa_cc1b1pn.setPTXAMT(FormatUtil.decimalFormat(feptxn.getFeptxnTxAmtAct(), "$#,##0"));
					// 格式:$999 ex :$0
					atm_faa_cc1b1pn.setPFEE(FormatUtil.decimalFormat(feptxn.getFeptxnFeeCustpayAct(), "$#,##0"));
					atm_faa_cc1b1pn.setPATXBKNO(feptxn.getFeptxnBkno());
					atm_faa_cc1b1pn.setPSTAN(feptxn.getFeptxnStan());

					atm_faa_cc1b1pn.setPRCCODE(feptxn.getFeptxnCbsRc());
					// 轉出行
					atm_faa_cc1b1pn.setPOTXBKNO(feptxn.getFeptxnTroutBkno());
					// 處理有收到CBS Response的欄位值
					if (tota != null) {
						//交易種類
						atm_faa_cc1b1pn.setPTXTYPE(this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue()));
						//轉出帳號(明細表顯示內容)
						atm_faa_cc1b1pn.setPACCNO(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()));
						//轉出行
						atm_faa_cc1b1pn.setPARPC(this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue()));
					}
					rfs.set("");
					rtnMessage = atm_faa_cc1b1pn.makeMessage();
					rtnCode = atmEncHelper.makeAtmMacP3(atmno, rtnMessage, rfs);
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

	//8. 	GarbageResponse:組ATM回應電文 & 回 ATMMsgHandler 
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
	 * 西元日期(yyyyMMdd)轉成民國日期(yyy/MM/dd)
	 *
	 * @param date
	 * @return
	 */
	private String formatDate(String date) {
		if (StringUtils.isBlank(date)) {
			return date;
		} else if (date.length() != 8) {
			date = StringUtils.leftPad(StringUtils.right(date, 8), 8, '0');
		}
		StringBuilder sb = new StringBuilder();
		date = CalendarUtil.adStringToROCString(date);
		int dateLength = date.length();
		String year = StringUtils.substring(date, 0, dateLength - 4);
		String month = StringUtils.substring(date, dateLength - 4, dateLength - 2);
		String day = StringUtils.substring(date, dateLength - 2);
		sb.append(year).append('/').append(month).append('/').append(day);
		return sb.toString();
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
}
