package com.syscom.fep.server.aa.nb;

import java.math.BigDecimal;
import java.util.Objects;

import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.business.BusinessBase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;

/**
 * @author Jaime
 */
public class NBPYOtherRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

    public NBPYOtherRequestA(NBData txnData) throws Exception {
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
            rtnCode = getFiscBusiness().nb_PrepareFEPTxn();

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                this.addTxData();
            }
            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getFiscBusiness().checkRequestFromOtherChannel(getnBData(), tita.getINTIME());
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
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
            //VFISC本機不能測，BYPASS 塞成功
//            feptxn.setFeptxnRepRc("4001");

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. ProcessAPTOT:更新跨行代收付
                if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()) {
                    rtnCode = fiscBusiness.processAptot(false);
                }
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 8. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
                this.sendToCBS2();
                if (rtnCode != FEPReturnCode.Normal) {
                    if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()) {
                        /* 沖回跨行代收付(APTOT) */
                        rtnCode2 = fiscBusiness.processAptot(true);
                    }
                    // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
                }
            }

            // 9. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
            this.labelEndOfFunc();

            // 10. 更新交易記錄(FEPTXN)
            this.updateTxData();
//		 	11. 	組NB回應電文 & 回 NBMsgHandler
//		       電文內容格式請參照: SEND_HCE_GeneralTrans_RS
            rtnMessage = getFiscBusiness().prepareNBResponseData(tota);

            // 12. 交易通知 (if need)
            this.sendToMailHunter();

            //13. 交易結束通知主機(By PCODE)
            this.transactionCloseConnect();

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getFiscBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {

            getnBData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getnBData().getLogContext().setMessage(rtnMessage);
            getnBData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getnBData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
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
        rtnCode = checkRequestFromATM(getATMtxData());
    }

    /**
     * 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        // 轉入方為本行時,先送CBS查詢帳號
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        getFeptxn();
        String AATxTYPE = "0"; // 上CBS查詢、檢核
        getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        String charge = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
        String brch = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
        String chargeFlag = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
        if (StringUtils.isNotBlank(charge)) {
            BigDecimal hostCharge = new BigDecimal(charge);
            getnBData().setCharge(hostCharge);
        }
        getnBData().setBrch(brch);
        getnBData().setChargeFlag(chargeFlag);
    }

    /**
     * 8. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");
        /* 進主機入扣帳/手續費 */
        String AATxTYPE = ""; // 上CBS入扣帳
        AATxTYPE = "1";
        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        if (rtnCode2 == CommonReturnCode.Normal) {
            feptxn.setFeptxnTxrust("A");
        }
        String charge = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
        String brch = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
        String chargeFlag = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
        if (StringUtils.isNotBlank(charge)) {
            BigDecimal hostCharge = new BigDecimal(charge);
            getnBData().setCharge(hostCharge);
        }
        getnBData().setBrch(brch);
        getnBData().setChargeFlag(chargeFlag);
    }

    /**
     * 9. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
     *
     * @throws Exception
     */
    private void labelEndOfFunc() throws Exception {
        String feptxnRepRc = feptxn.getFeptxnRepRc();
        if ((rtnCode == FEPReturnCode.Normal) && "4001".equals(feptxnRepRc)) {
            /* +REP */
            feptxn.setFeptxnReplyCode("    "); // 4個SPACES 回覆 ATM正常
            /* ATM_2way 交易 */
            feptxn.setFeptxnTxrust("A"); /* 成功 */
            feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */

            feptxn.setFeptxnConRc("4001");
            /* 組 CON 電文送財金 */
            rtnCode2 = getFiscBusiness().sendConfirmToFISC();
        } else { /* 交易失敗FEPReturnCode <> Normal */
            if (StringUtils.isNotBlank(feptxnRepRc)) {
                feptxn.setFeptxnPending((short) 2); /* 解除 PENDING */
                if ("4001".equals(feptxnRepRc)) { /* +REP */
                    if (!DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlFisc2way(), false)) { /* 3WAY */
                        if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                            feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota,ImsMethodName.IMSRC4_FISC.getValue()));
                        }else {
                            feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                    FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                        }
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                                getnBData().getTxChannel(), getnBData().getLogContext()));
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());
                        feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
                        rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                    } else {
                        /* 修改 for 2WAY */
                        feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                                getnBData().getTxChannel(), getnBData().getLogContext()));
                    }
                } else { /* -REP */
                    feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
                            getnBData().getTxChannel(), getnBData().getLogContext()));
                    feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());
                }
            } else { /* FEPReturnCode <> Normal */
                feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
                if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
                    feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                            getnBData().getTxChannel(), getnBData().getLogContext()));
                }
            }
        }
    }

    /**
     * 10. 更新交易記錄(FEPTXN)
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
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
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
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            fpeReturnCode = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
        return fpeReturnCode;
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
     * 12. 交易通知 (if need)
     */
    private void sendToMailHunter() {
        try {
            String noticeType = feptxn.getFeptxnNoticeType();
            if (StringUtils.isNotBlank(noticeType) && "4001".equals(feptxn.getFeptxnRepRc())&& "4001".equals(feptxn.getFeptxnConRc())) {
                switch (noticeType) {
                    case "P": /* 送推播 */
                        getFiscBusiness().preparePush(this.feptxn);
                        break;
                    case "M": /* 簡訊 */
                        getFiscBusiness().prepareSms(this.feptxn);
                        break;
                    case "E": /* Email */
                        getFiscBusiness().prepareMail(this.feptxn);
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
     * 13. 交易結束通知主機(By PCODE)
     */
    public void transactionCloseConnect() {
        try {
            String AATxTYPE = ""; //不需提供此值
            String AATxRs = "N";//不需等待主機回應
            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
            rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE, AATxRs);
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }
}
