package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;

import com.syscom.fep.base.enums.*;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.IntltxnExtMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.response.ATM_FSN_HEAD2;
import com.syscom.fep.vo.text.ims.IMSTextBase;

/**
 * @author vincent
 */
public class ATMConfirmA extends INBKAABase {
    private String AATxTYPE = ""; // 預設
    private Object tota = null;

    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    boolean isGoResponse = false;

    private IntltxnExtMapper intltxnExtMapper = SpringBeanFactoryUtil.getBean(IntltxnExtMapper.class);
    private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);

    public ATMConfirmA(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 3. CheckBusinessRule: 商業邏輯檢核
            this.checkBusinessRule();

            // 4. UpdateTxData: 更新交易記錄(FEPTXN/INTLTXN)
            if (!isGoResponse) {
                this.updateTxData();
            }

            // 5. 判斷是否為餘額查詢交易
            /* 若是餘額查詢交易(3WAY)，直接更新FEPTXN */
            boolean IS_GOTO_STEP_8 = false;
            if (rtnCode != FEPReturnCode.Normal || "2500".equals(feptxn.getFeptxnPcode())) {
                // GOTO STEP 9 /* 若 3.(1) ~ 3.(5) 檢核有誤或餘額查詢交易 */
                IS_GOTO_STEP_8 = true;
            }

            // 6. SendToCBS/ASC(if need): 帳務主機處理 (含沖轉跨行代收付)
            if (rtnCode == FEPReturnCode.Normal && !IS_GOTO_STEP_8) {
                this.sendToCBS();
            }

            // 7. 送Confirm 電文至 FISC
            if (!IS_GOTO_STEP_8 && feptxn.getFeptxnPcode().equals("2510")) {
                if (DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                    feptxn.setFeptxnConRc("0601"); //主機逾時
                } else if (StringUtils.isNotBlank(AATxTYPE)) {
                    feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                }
                rtnCode = getFiscBusiness().sendConfirmToFISC();
                if (rtnCode != FEPReturnCode.Normal) {
                    feptxn.setFeptxnAaRc(rtnCode.getValue());
                }
            }

            // 8.更新交易記錄(FEPTXN)
            feptxn.setFeptxnAaComplete((short) 1); /* AA close */
            this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */

            // 9.交易通知 (if need)
            this.sendToMailHunter();


            // 10. 組回應電文回給 ATM
            rtnMessage = this.response();

            // 11.交易結束通知主機(By PCODE)
            this.sendToCBS2();
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }
        try {
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
     * CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private void checkBusinessRule() throws Exception {

        // (1)檢核原交易帳號
        getATMBusiness().setFeptxn(getATMBusiness().checkConData());
        if (getATMBusiness().getFeptxn() == null) {
            // FEPReturnCode = "E944" /* 查無原交易 */
            rtnCode = FEPReturnCode.OriginalMessageNotFound;
            // 將ERROR MSG送 EVENT MONITOR SYSTEM
            sendEMS(getLogContext());
            isGoResponse = true;
            return; // GOTO STEP 10: 組回應電文回給 ATM
        }

        // (2)更新 FEPTXN
        rtnCode = getATMBusiness().prepareConFEPTXN();
        if (rtnCode != FEPReturnCode.Normal) {
            return; // GOTO NEXT STEP 4: UpdateTxData
        }

        // (3)檢核 ATM Confirm MAC(if need)
        /* 因Confirm MAC error 需繼續執行其他步驟,故存入不同 RC */
        /* ATM 電文檢核 Confirm MAC */
        String ATM_TITA_PICCMACD = getATMtxData().getTxObject().getRequest().getPICCMACD();
        if (StringUtils.isNotBlank(ATM_TITA_PICCMACD)
                && !DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way(), false)) {
            rtnCode2 = new ENCHelper(getTxData()).checkAtmMac(
                    StringUtils.substring(getATMBusiness().getAtmTxData().getTxRequestMessage(), 17, 370),
                    ATM_TITA_PICCMACD);
        }
    }

    /**
     * 更新交易記錄(FEPTXN/INTLTXN)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response); /* ATM Confirm Response */
        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
            feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                    FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
            feptxn.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode2.getValue()),
                    FEPChannel.FEP, FEPChannel.ATM, getTxData().getLogContext()));
        }

        /* Ignore ATM MAC ERROR */
        if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way(), false)) { /* 代理提款為 ATM_3WAY */
            if ("NN".equals(StringUtils.substring(getATMRequest().getSTATUS(), 0, 2))) {
                feptxn.setFeptxnConRc("4001"); /* +CON */
                feptxn.setFeptxnTxrust("A"); /* 成功 */
            } else {
                feptxn.setFeptxnConRc("0501"); /* -CON */
                feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
            }
        }
        feptxn.setFeptxnPending((short) 2); /* 取消 PENDING */
        if ("24".equals(StringUtils.substring(feptxn.getFeptxnPcode(), 0, 2))) { /* 國際卡交易 */
            rtnCode2 = FEPReturnCode.Normal;
            try {
                /* 更新 INTLTXN */
                Intltxn intltxn = new Intltxn();
                intltxn.setIntltxnConRc(feptxn.getFeptxnConRc());
                intltxn.setIntltxnTxrust(feptxn.getFeptxnTxrust());
                intltxn.setIntltxnTxDate(feptxn.getFeptxnTxDate());
                intltxn.setIntltxnEjfno(feptxn.getFeptxnEjfno());
                int updCount = intltxnExtMapper.updateByPrimaryKeySelective(intltxn);
                if (updCount <= 0) {
                    rtnCode2 = FEPReturnCode.FEPTXNUpdateNotFound;
                }
            } catch (Exception e) {
                rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
                getLogContext().setProgramException(e);
                getLogContext().setProgramName(ProgramName + ".updateTxData");
                sendEMS(getLogContext());
            }
            if (rtnCode2 != FEPReturnCode.Normal) {
                feptxn.setFeptxnAaRc(rtnCode2.getValue());
            }
        }

        /* for 無卡跨行提款交易 */
        if ("W2".equals(feptxn.getFeptxnTxCode())) {
            rtnCode2 = FEPReturnCode.Normal;
            try {
                /* 更新 NWDTXN */
                Nwdtxn nwdtxn = nwdtxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(),feptxn.getFeptxnEjfno());
                nwdtxn.setNwdtxnConRc(feptxn.getFeptxnConRc());
                nwdtxn.setNwdtxnTxrust(feptxn.getFeptxnTxrust());
                nwdtxn.setNwdtxnTxDate(feptxn.getFeptxnTxDate());
                nwdtxn.setNwdtxnEjfno(feptxn.getFeptxnEjfno());
                int updCount = nwdtxnMapper.updateByPrimaryKeySelective(nwdtxn);
                if (updCount <= 0) {
                    rtnCode2 = FEPReturnCode.FEPTXNUpdateNotFound;
                }
            } catch (Exception e) {
                rtnCode2 = FEPReturnCode.FEPTXNUpdateError;
                getLogContext().setProgramException(e);
                getLogContext().setProgramName(ProgramName + ".updateTxData");
                sendEMS(getLogContext());
            }
            if (rtnCode2 != FEPReturnCode.Normal) {
                feptxn.setFeptxnAaRc(rtnCode2.getValue());
            }
        }
        /* 更新 FEPTXN */
        rtnCode2 = this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
        /* 若更新 FEPTXN 失敗時不做處理, 程式結束 */
    }

    /**
     * 組回應電文回給 ATM
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            if (!feptxn.getFeptxnPcode().equals("2510") && !DbHelper.toBoolean(feptxn.getFeptxnCbsTimeout())) {
                ATMGeneralRequest atmReq = getATMRequest();
                String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                        FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
                ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
                RefString rfs = new RefString();

                ATM_FSN_HEAD2 atm_fsn_head2 = new ATM_FSN_HEAD2();
                // 組Header(OUTPUT-1)
                atm_fsn_head2.setWSID(atmReq.getWSID());
                atm_fsn_head2.setRECFMT("1");
                atm_fsn_head2.setMSGCAT("F");
                atm_fsn_head2.setMSGTYP("PC"); // - response
                atm_fsn_head2.setTRANDATE(atmReq.getTRANDATE()); // 西元後兩碼+系統月日共六碼
                atm_fsn_head2.setTRANTIME(atmReq.getTRANTIME()); // 系統時間
                atm_fsn_head2.setTRANSEQ(atmReq.getTRANSEQ());
                atm_fsn_head2.setTDRSEG(atmReq.getTDRSEG()); // 回覆FSN或FSE
                // PRCRDACT = 0 或4都是未留置卡片, 2 是吃卡, 只有磁條密碼變更交易(FC1、P1)主機才有可能依據狀況要求吃卡
                atm_fsn_head2.setPRCRDACT("0");

                if (feptxn == null) {
                    atm_fsn_head2.setRECFMT("0");
                }

                /* CALL ENC 取得MAC 資料 */
                if ("W2".equals(feptxn.getFeptxnTxCode())) { // 非無卡提款交易
                    rfs.set("");
                    rtnMessage = atm_fsn_head2.makeMessage();
                    rtnCode = atmEncHelper.makeAtmMac(rtnMessage, rfs);
                    if (rtnCode != FEPReturnCode.Normal) {
                        atm_fsn_head2.setMACCODE(""); /* 訊息押碼 */
                    } else {
                        atm_fsn_head2.setMACCODE(rfs.get()); /* 訊息押碼 */
                    }
                    rtnMessage = atm_fsn_head2.makeMessage();
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
     * SendToCBS/ASC(if need): 帳務主機處理 (含沖轉跨行代收付)
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        if (rtnCode == FEPReturnCode.Normal && !DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way(), false)) {
            /* 失敗 */ /* 需沖正 */
            if ("C".equals(feptxn.getFeptxnTxrust()) && getTxData().getMsgCtl().getMsgctlCbsFlag() == 2) {
                /* 沖轉跨行代收付 */
                rtnCode = getFiscBusiness().processAptot(true);
                if (rtnCode != FEPReturnCode.Normal) {
                    feptxn.setFeptxnAaRc(rtnCode.getValue());
                }
            }
        }
        /* 提款交易依ATM如發異常電文, 才送主機沖正或註記 */
        if (feptxn.getFeptxnPcode().equals("2510")) {
            ATMGeneralRequest atmReq = this.getATMRequest();
            // 提款確認電文, 如ATM送Con(-), 須組I002電文送往CBS主機
            // ATM第二道為失敗電文且FEP紀錄CBS已記帳
            if ("SE".equals(getATMRequest().getMSGTYP()) && feptxn.getFeptxnAccType() == 1) {
                if (BigDecimal.ZERO.compareTo(new BigDecimal(StringUtils.substring(atmReq.getSTATUS(), 2, 12))) != 0) {
                    AATxTYPE = "2"; // 上CBS沖正
                }
            }

            if (StringUtils.isNotBlank(AATxTYPE)) {
                String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid(); // AA = MSGCTL_TWCBSTXID的電文
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
                rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
                tota = hostAA.getTota();
                if (rtnCode != FEPReturnCode.Normal) {
                    feptxn.setFeptxnAaRc(rtnCode.getValue());
                }
            }
        }
    }

    private void sendToCBS2() throws Exception {
        if (feptxn.getFeptxnPcode().equals("2510") || feptxn.getFeptxnPcode().equals("2521")
                || feptxn.getFeptxnPcode().equals("2522") || feptxn.getFeptxnPcode().substring(0, 3).equals("253")
                || feptxn.getFeptxnPcode().substring(0, 3).equals("256")) {
            AATxTYPE = "";
            String AATxRs ="N";
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1(); // AA = MSGCTL_TWCBSTXID的電文
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE,AATxRs);
        }
    }

    /**
     * 交易通知 (if need)
     *
     * @return
     * @throws Exception
     */
    private void sendToMailHunter() {
        if ("4001".equals(feptxn.getFeptxnRepRc()) && "4001".equals(feptxn.getFeptxnConRc())) {
            try {
                switch (feptxn.getFeptxnNoticeType()) {
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
            } catch (Exception ex) {
                this.logContext.setProgramException(ex);
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
                sendEMS(this.logContext);
            }
        }
    }

    /**
     * 更新FEPTXN
     *
     * @return
     */
    private FEPReturnCode updateFeptxn() {
        FEPReturnCode fepReturnCode = FEPReturnCode.Normal;
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            fepReturnCode = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
        return fepReturnCode;
    }

}
