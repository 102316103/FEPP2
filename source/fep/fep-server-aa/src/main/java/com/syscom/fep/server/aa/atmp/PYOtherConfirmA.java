package com.syscom.fep.server.aa.atmp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;

/**
 * @author vincent
 */
public class PYOtherConfirmA extends INBKAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private boolean isGoResponse = false;

    public PYOtherConfirmA(ATMData txnData) throws Exception {
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
            
            // 1. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();

            if (!isGoResponse) {
                // 2. UpdateTxData: 更新交易記錄(FEPTxn)
                this.updateTxData();
            }

            // 3. 交易通知 (if need)
            this.sendToMailHunter();

            // 4. Response:組ATM回應電文 & 回 ATMMsgHandler
            rtnMessage = this.response();

            // 5. 交易結束通知主機
            this.sendtoCBS();

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }

        try {
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
     * 1. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {
        // 1.1 取得原交易之 FEPTXN
        Feptxn tempFeptxn = getATMBusiness().checkConData();
        feptxn = tempFeptxn;
        getATMBusiness().setFeptxn(tempFeptxn);
        getATMtxData().setFeptxn(feptxn);
        if (getATMBusiness().getFeptxn() == null) {
            rtnCode = FEPReturnCode.OriginalMessageNotFound; // E944 /* 查無原交易 */
            sendEMS(getLogContext()); // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            isGoResponse = true;
            return; // GO TO 4 /* 組 ATM 回應電文 */
        }

        // 1.2 將ATM確認電文, 準備寫入原交易 FEPTXN欄位
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GO TO 3 /* 更新交易記錄 */
        }

        // 1.3 交易確認電文檢核 MAC
        /*因Confirm MAC error 需繼續執行其他步驟,故存入不同 RC*/
		String ATM_TITA_PICCMACD = this.getATMRequest().getPICCMACD();
		if (StringUtils.isBlank(ATM_TITA_PICCMACD)) {
			rtnCode2 = FEPReturnCode.ENCCheckMACError;
		}else {
			String ATMMAC = EbcdicConverter.fromHex(CCSID.English, StringUtils.substring(getATMtxData().getTxRequestMessage(), 742, 758));// 轉 ASCII
			String wkMAC = getATMBusiness().getAtmTxData().getTxRequestMessage().substring(36, 742); 
			this.logContext.setMessage("Begin checkAtmMac mac:" + ATMMAC);
			logMessage(this.logContext);
			rtnCode2 = new ENCHelper(getATMtxData()).checkAtmMacNew(feptxn.getFeptxnAtmno(), wkMAC, ATM_TITA_PICCMACD);
			this.logContext.setMessage("after checkAtmMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
		}
    }

    /**
     * 3. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); // (RESPONSE)
        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
            feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                    FEPChannel.ATM, getATMtxData().getLogContext()));
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
            feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode2.getValue()), FEPChannel.FEP,
                    FEPChannel.ATM, getATMtxData().getLogContext()));
        }
        if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlAtm2way())) {
            if ("NN".equals(getATMRequest().getSTATUS())) {
                feptxn.setFeptxnConRc("4001"); /*+CON*/
                feptxn.setFeptxnTxrust("A"); /*成功*/
            } else {
                feptxn.setFeptxnConRc("0501"); /*-CON*/
                feptxn.setFeptxnTxrust("C"); /*Accept-Reverse*/
            }
            feptxn.setFeptxnPending((short) 2);
        }
        feptxn.setFeptxnAaComplete((short) 1);

        // 回寫 FEPTXN
        /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
        FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
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
            sendEMS(getLogContext());
        }
    }

    /**
     * 4. Response:組ATM回應電文 & 回 ATMMsgHandler
     *
     * @return
     * @throws Exception
     */
    private String response() throws Exception {
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
            ENCHelper atmEncHelper = new ENCHelper(this.getATMtxData());

            rfs.set("");
            rtnMessage = atm_fsn_head2.makeMessage();
            rtnCode = atmEncHelper.makeAtmMac(feptxn.getFeptxnAtmno(), rtnMessage, rfs);
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
     * 5. 交易通知 (if need)
     *
     * @return
     * @throws Exception
     */
    private void sendToMailHunter() {
        try {
            if ("4001".equals(feptxn.getFeptxnRepRc()) && "4001".equals(feptxn.getFeptxnConRc())) {
                String noticeType = feptxn.getFeptxnNoticeType();
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

    private void sendtoCBS() {
        try {
            String AATxTYPE = "";
            String AATxRs = "N";
            String AA = getATMtxData().getMsgCtl().getMsgctlTwcbstxid1();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getATMtxData());
            rtnCode = new CBS(hostAA, getATMtxData()).sendToCBS(AATxTYPE, AATxRs);
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToCBS"));
            sendEMS(this.logContext);
        }
    }

}
