package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Objects;

/**
 * @author
 */
public class HCESelfIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;

    public HCESelfIssueRequestA(HCEData txnData) throws Exception {
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
            rtnCode = getFiscBusiness().hce_PrepareFEPTxn();

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                rtnCode = addTxData();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = this.getFiscBusiness().checkRequestFromOtherChannel(this.getmHCEtxData(), tita.getINTIME());
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                this.sendToCBS();
            }

            FISC fiscBusiness = getFiscBusiness();
            if (rtnCode == FEPReturnCode.Normal) {
                //RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                //getATMRequest().setICMARK(tita.getICMARK());
                //getATMRequest().setIC_TAC(tita.getIC_TAC());
                //getATMRequest().setIC_TAC_LEN(tita.getIC_TAC_LEN());
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
            }

            boolean repRcEq4001 = true;
            if (rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. SendToCBS/ASC: 送主機處理帳務
                this.sendToCBS2();
                if (rtnCode != FEPReturnCode.Normal) {
                    if (1 == getmHCEtxData().getMsgCtl().getMsgctlUpdateAptot()) {
                        /* 沖回跨行代收付(APTOT) */
                        rtnCode2 = fiscBusiness.processAptot(true);
                    }
                    // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
                }
            }

            // 8. label_END_OF_FUNC :判斷是組 CON 電文回財金( if need)
            rtnCode = this.labelEndOfFunc();

            //9.組HCE回應電文 & 回 HCEMsgHandler
            ////CALL  PrepareHCEesponseData  /* 組 HCE 回應電文 */
            rtnMessage = getFiscBusiness().prepareHCEResponseData(tota);

            //10. 	更新交易記錄(FEPTXN)
            this.updateTxData();

            //11. 交易通知(if need)
            this.sendToMailHunter();

            //12. 交易結束通知主機(By PCODE)
            this.transactionCloseConnect();

            //13. 	寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
            if ("Y".equals(feptxn.getFeptxnSend2160()) && ((feptxn.getFeptxnFiscFlag() == 0 && "000".equals(feptxn.getFeptxnCbsRc()))
                    || (feptxn.getFeptxnFiscFlag() == 1 && "4001".equals(feptxn.getFeptxnConRc())))) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getFiscBusiness().prepareInbk2160();
            } else if ("A".equals(feptxn.getFeptxnSend2160())) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getFiscBusiness().prepareInbk2160();
            }
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
            }
        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getmHCEtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmHCEtxData().getLogContext().setMessage(rtnMessage);
            getmHCEtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmHCEtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
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
    private FEPReturnCode addTxData() throws Exception {
        try {
            // 新增交易記錄(FEPTxn) Returning FEPReturnCode
            /* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
            if (insertCount <= 0) { // 新增失敗
                rtnCode = FEPReturnCode.FEPTXNInsertError;
                return rtnCode;
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNInsertError;
            return rtnCode;
        }
    }


    /**
     * 4. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        // 轉入方為本行時,先送CBS查詢帳號
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        String AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
        rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();


    }
//		}

    /**
     * 8. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");
        if (getmHCEtxData().getMsgCtl().getMsgctlCbsFlag() == 1) {
            /* 進主機入扣帳/手續費 */
            String AATxTYPE = "1"; // 上CBS入扣帳
            String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
            rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
        }
    }

    /**
     * 9. label_END_OF_FUNC :判斷是組 CON 電文回財金( if need)
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                if (DbHelper.toBoolean(getmHCEtxData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                    rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                } else {
                    /* for代理提款-ATM_3way 交易 */
                    feptxn.setFeptxnTxrust("B"); /* PENDING */
                    feptxn.setFeptxnMsgflow("A2"); /* ATM Response */
                    this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
                }
            } else {
                getLogContext().setProgramName(ProgramName);
                // 交易失敗
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    if ("4001".equals(getFeptxn().getFeptxnRepRc())) { // +REP
                        if (!DbHelper.toBoolean(getmHCEtxData().getMsgCtl().getMsgctlAtm2way())) {

                            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                                feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                            } else {
                                feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                        FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                            }
                            getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmHCEtxData().getTxChannel(), getLogContext()));
                            getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
                            rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                        } else {
                            getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                            getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmHCEtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                        }
                    } else { // -REP
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getmHCEtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                    }
                } else { // fepReturnCode <> Normal
                    getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    }

                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmHCEtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                    }
                    getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE after:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                }
                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                getFiscBusiness().updateTxData();
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToConfirm"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
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
        feptxn.setFeptxnMsgflow("A2");
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
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
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
     * 2023/8/3 Aster新增
     *
     * @return
     * @throws Exception
     */

    private void sendToMailHunter() {
        try {
            String noticeType = feptxn.getFeptxnNoticeType();
            if (StringUtils.isNotBlank(noticeType) && "4001".equals(feptxn.getFeptxnRepRc()) && "4001".equals(feptxn.getFeptxnConRc())) {
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
     * 13. 交易結束通知主機 (By PCODE)
     */

    public void transactionCloseConnect() {
        try {
            String AATxTYPE = ""; //不需提供此值
            String AATxRs = "N";//不需等待主機回應
            String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid1();
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
            rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE, AATxRs);
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }
}
