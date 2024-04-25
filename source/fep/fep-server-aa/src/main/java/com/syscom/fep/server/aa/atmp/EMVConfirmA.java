package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;

import com.syscom.fep.base.enums.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;

public class EMVConfirmA extends INBKAABase {
	private Object tota ;
	private String rtnMessage = "";
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
	@SuppressWarnings("unused")
	private boolean check_rtnCode = false;
	private IntltxnMapper intltxnMapper = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
	String AATxTYPE = "";

	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 */
	public EMVConfirmA(ATMData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			getFiscBusiness().getFISCTxData().setFiscTeleType(FISCSubSystem.EMVIC);
			// 3. 商業邏輯檢核
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = checkBusinessRule();
			}

			// 4. 更新交易記錄(FEPTXN/INTLTXN)
			if (_rtnCode != CommonReturnCode.OriginalMessageNotFound) {
				updateTxData();
			}

			// 5. SendToCBS
			if(_rtnCode == CommonReturnCode.Normal) {
				this.sendToCBS();
			}
			// 6. 送Confirm 電文至 FISC
			if (_rtnCode == CommonReturnCode.Normal && "26".equals(feptxn.getFeptxnPcode())) {
				if(DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())){
					feptxn.setFeptxnConRc("0601");
				}else if(StringUtils.isNotBlank(AATxTYPE)){
					//有上CBS沖正或註記，以主機處理結果回應財金，
					//否則依據上送的STATUS 回覆財金
					feptxn.setFeptxnConRc(getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
				}
				_rtnCode = getFiscBusiness().sendConfirmToFISCEMV(); // 送Confirm 電文至 FISC
				if(_rtnCode != FEPReturnCode.Normal){
					feptxn.setFeptxnAaRc(_rtnCode.getValue());
				}
			}

			// 7.更新交易紀錄
			if(_rtnCode == CommonReturnCode.Normal){
				feptxn.setFeptxnAaComplete((short)1);
				this.updateFEPTXN();
			}

			// 8. 交易通知(if need)
			if(_rtnCode == CommonReturnCode.Normal) {
				this.sendToMailHunter();
			}
			// 9. 組回應電文回給ATM
			this.rtnMessage = prepareResponseData();

			// 10. 交易結束通知主機(By PCODE)
			if("26".equals(feptxn.getFeptxnPcode().substring(0,2)) && !"1".equals(feptxn.getFeptxnPcode().substring(3,4))){
				AATxTYPE = "";
				String AATxRs="N";
				String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
				ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
				_rtnCode = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE,AATxRs);
			}
		} catch (Exception ex) {
			this.rtnMessage = "";
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		}

		try {
			if (this.check_rtnCode = true) {
				getLogContext().setProgramFlowType(ProgramFlow.AAOut);
				getLogContext().setMessage(this.rtnMessage);
				getLogContext().setProgramName(this.aaName);
				getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
				logMessage(Level.DEBUG, getLogContext());
			} else {
				getLogContext().setProgramFlowType(ProgramFlow.AAIn);
				getLogContext().setMessage(this.rtnMessage);
				getLogContext().setProgramName(this.aaName);
				getLogContext().setMessageFlowType(MessageFlow.ResponseConfirmation);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
				logMessage(Level.DEBUG, getLogContext());
			}
		} catch (Exception ex) {
			this.rtnMessage = "";
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		}

		return rtnMessage;
	}

	private void sendToCBS() throws Exception {
		if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way()) && _rtnCode ==FEPReturnCode.Normal) {
			if(feptxn.getFeptxnTxrust().equals("C") && getTxData().getMsgCtl().getMsgctlCbsFlag() == 2){
				_rtnCode = getFiscBusiness().processAptot(true);
				if(_rtnCode != FEPReturnCode.Normal){
					feptxn.setFeptxnAaRc(_rtnCode.getValue());
				}
			}

			/* 提款交易依ATM如發異常電文, 才送主機沖正或註記 */
            // 提款確認電文, 如ATM送Con(-), 須組I002電文送往CBS主機
			if(feptxn.getFeptxnPcode().substring(0,2).equals("26") && !feptxn.getFeptxnPcode().substring(3,4).equals("1")){
				AATxTYPE =""; //預設
				if(getATMRequest().getMSGTYP().equals("SE") && DbHelper.toBoolean(feptxn.getFeptxnAccType())){
					AATxTYPE ="2"; //上CBS沖正
				}
			}

			// 餘額查詢交易手續費入帳
			if(feptxn.getFeptxnPcode().substring(0,2).equals("26") && feptxn.getFeptxnPcode().substring(3,4).equals("1")){
				AATxTYPE ="1"; //上CBS入帳
			}
			if(StringUtils.isNotBlank(AATxTYPE)){
				String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
				feptxn.setFeptxnCbsTxCode(AA);
				ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
				_rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
				tota = hostAA.getTota();
			}
		}
	}
	/**
	 * 商業邏輯檢核
	 *
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode _rtnCode = CommonReturnCode.Normal;
		try {
			// (1) 檢核原交易帳號
			Feptxn tempFeptxn = getATMBusiness().checkConData();
			feptxn = tempFeptxn;
			getATMBusiness().setFeptxn(tempFeptxn);

			if (feptxn == null) {
				/* 查無原交易 */
				// 將ERROR MSG送 EMS
				sendEMS(getLogContext());
				// GOTO STEP 5: 組回應電文回給 ATM
				return CommonReturnCode.OriginalMessageNotFound;
			}

			this.check_rtnCode = true;
			// (2) 更新 FEPTXN
			_rtnCode = getATMBusiness().prepareConFEPTXN();
			if (_rtnCode != CommonReturnCode.Normal) {
				return _rtnCode;
			}

			// (3) 檢核 ATM Confirm MAC(if need)
			/* 因Confirm MAC error 需繼續執行其他步驟,故存入不同 RC */
			String PICCMACD = getATMRequest().getPICCMACD();
			if (StringUtils.isBlank(PICCMACD)) {
				_rtnCode2 = FEPReturnCode.ENCCheckMACError;
			}else {
				_rtnCode2 = new ENCHelper(getTxData()).checkAtmMac(
						StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 18, 375),
						PICCMACD); // EBCDIC(36,750)
			}
			return _rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".checkBusinessRule");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 更新交易記錄(FEPTXN/INTLTXN)
	 *
	 * @return
	 */
	private void updateTxData() {
		Intltxn defIntltxn = new Intltxn(); // intltxn table 欄位物件
		int intltxnRes = 0;
		try {
			feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response);
			getLogContext().setProgramName(ProgramName);
			if (_rtnCode != FEPReturnCode.Normal) {
				feptxn.setFeptxnAaRc(_rtnCode.getValue());
				feptxn.setFeptxnConReplyCode(
						TxHelper.getRCFromErrorCode(_rtnCode.name(), FEPChannel.FEP, FEPChannel.ATM, getLogContext()));
			} else if (_rtnCode2 != FEPReturnCode.Normal) {
				feptxn.setFeptxnAaRc(_rtnCode2.getValue());
				feptxn.setFeptxnConReplyCode(
						TxHelper.getRCFromErrorCode(_rtnCode2.name(), FEPChannel.FEP, FEPChannel.ATM, getLogContext()));
			}
			/* Ignore ATM MAC ERROR */
			if (!DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlAtm2way())) {
				ATMGeneralRequest atmReq = getATMRequest();

				/* 代理提款為 ATM_3WAY */
				if ("SN".equals(atmReq.getMSGTYP())) {
					feptxn.setFeptxnConRc("4001"); /* +CON */
					feptxn.setFeptxnTxrust("A"); /* 成功 */
				} else {
					/* ATM CON(-), 轉成 RC:‘8120’ 回給財金 */
					BigDecimal PIARQCLN = null;
					if (StringUtils.isNotBlank(atmReq.getPIARQCLN())) {
						PIARQCLN = new BigDecimal(atmReq.getPIARQCLN());
					}
					if ("SE".equals(atmReq.getMSGTYP()) && "E".equals(atmReq.getCARDFMT())
							&& (PIARQCLN != null && PIARQCLN.compareTo(BigDecimal.ZERO) > 0)) {
						feptxn.setFeptxnConRc("8120");
						/* for EMV VISA卡(2620&2622) CON(-) */
						String feptxnPcode = feptxn.getFeptxnPcode();
						if ("2620".equals(feptxnPcode) && "2622".equals(feptxnPcode)) {
							getLogContext().setRemark("ATMRequest.ICCHKDATA:");
							logMessage(Level.DEBUG, getLogContext());
							try {
								String tk3 = getFiscBusiness().check_IC_CHECKDATA(getATMRequest().getPIARQCLN(),getATMRequest().getPIARQC());
								feptxn.setFeptxnTrk3(tk3);
							} catch (Exception ex) {
								feptxn.setFeptxnTrk3("");
							}
						}
					} else {
						feptxn.setFeptxnConRc("0501"); /* -CON */
					}
					feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
				}
				feptxn.setFeptxnPending((short) 2); /* 取消 PENDING */
				/* EMV 晶片卡交易 */
				if ("26".equals(feptxn.getFeptxnPcode().substring(0, 2))) {
					defIntltxn.setIntltxnConRc(feptxn.getFeptxnConRc());
					defIntltxn.setIntltxnTxrust(feptxn.getFeptxnTxrust());
					defIntltxn.setIntltxnTxDate(feptxn.getFeptxnTxDate());
					defIntltxn.setIntltxnEjfno(feptxn.getFeptxnEjfno());
					intltxnRes = intltxnMapper.updateByPrimaryKeySelective(defIntltxn);
					if (intltxnRes < 1) {// return 不為正代表失敗
						_rtnCode2 = FEPReturnCode.UpdateFail;
						feptxn.setFeptxnAaRc(_rtnCode2.getValue());
					}
				}
			}
			_rtnCode2 = this.updateFEPTXN();
			if (_rtnCode2 != FEPReturnCode.Normal) {
				return; /* 若更新 FEPTXN 失敗時不做處理, 直接 RETURN */
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "updateTxData");
			sendEMS(getLogContext());
		}
	}

	/**
	 * 組回應電文回給 ATM
	 *
	 * @return
	 */
	private String prepareResponseData() {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
			// 組 Header
			ATMGeneralRequest atmReq = this.getATMRequest();
			RefString rfs = new RefString();
			ATM_FSN_HEAD2 atm_fsn_head2 = new ATM_FSN_HEAD2();
			atm_fsn_head2.setWSID(atmReq.getWSID());
			atm_fsn_head2.setRECFMT("1");
			atm_fsn_head2.setMSGCAT("F");
			atm_fsn_head2.setMSGTYP("PC");
			atm_fsn_head2.setTRANDATE(atmReq.getTRANDATE()); // 西元後兩碼+系統月日共六碼
			atm_fsn_head2.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
			atm_fsn_head2.setTRANSEQ(atmReq.getTRANSEQ());
			atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
			// PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易
			// (FC1、P1)主機才有可能依據狀況要求吃卡
			atm_fsn_head2.setPRCRDACT("0");
			if (feptxn == null) {
				atm_fsn_head2.setRECFMT("0");
			}
			
			/* CALL ENC 取得MAC 資料 */
			ENCHelper atmEncHelper = new ENCHelper(this.getTxData());
			rfs.set("");
			rtnMessage = atm_fsn_head2.makeMessage();
			_rtnCode = atmEncHelper.makeAtmMac(rtnMessage, rfs);
			if (_rtnCode != FEPReturnCode.Normal) {
				atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
			} else {
				atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
			}
			rtnMessage = atm_fsn_head2.makeMessage();
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}

	/**
	 * 交易通知 (if need)
	 *
	 * @return
	 * @throws Exception
	 */
	private void sendToMailHunter() {
		try {
			String noticeType = feptxn.getFeptxnNoticeType();
			if ("4001".equals(feptxn.getFeptxnConRc()) && "4001".equals(feptxn.getFeptxnConRc())
					&& StringUtils.isNotBlank(noticeType)) {
				switch (noticeType) {
				case "P": /* 送推播 */
					getATMBusiness().preparePush(this.feptxn);
					break;
				case "M": /* 簡訊 */
					getATMBusiness().prepareSms(this.feptxn);
					break;
				case "E": /* Email */
					getATMBusiness().prepareMail(this.feptxn);
					break;
				}
			}
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
			sendEMS(this.logContext);
		}
	}

	/**
	 * 更新 FEPTXN
	 *
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode updateFEPTXN() throws Exception {
		try {
			// 新增交易記錄(FEPTxn) Returning FEPReturnCode
			/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".updateFEPTXN"));
			int insertCount = feptxnDao.insertSelective(feptxn); // 新增資料
			if (insertCount <= 0) { // 修改失敗
				return FEPReturnCode.FEPTXNUpdateError;
			}
		} catch (Exception ex) { // 修改失敗
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateFEPTXN");
			sendEMS(getLogContext());
			return FEPReturnCode.FEPTXNUpdateError;
		}
		return FEPReturnCode.Normal;
	}

}
