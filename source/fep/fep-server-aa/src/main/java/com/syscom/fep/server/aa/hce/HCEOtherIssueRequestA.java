package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Objects;

/**
 * @author
 */
public class HCEOtherIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;


    public HCEOtherIssueRequestA(HCEData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */

    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
//			boolean notgoto = true;
            getFiscBusiness().setmHCEtxData(getmHCEtxData());
            // 1. Prepare : 交易記錄初始資料
            rtnCode = getFiscBusiness().hce_PrepareFEPTxn();


            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                addTxData();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getFiscBusiness().checkRequestFromOtherChannel(this.getmHCEtxData(), tita.getINTIME());

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
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = fiscBusiness.checkResponseMessage();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. ProcessAPTOT:更新跨行代收付
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
                    rtnCode = fiscBusiness.processAptot(false);
                }
            }

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 8. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
                this.sendToCBS2();
            }

            // 9. 判斷是否需組 CON 電文回財金/本行轉入交易掛帳
            rtnCode = this.labelEndOfFunc();

            // 10. 更新交易記錄(FEPTXN)
            this.updateTxData();

            // 11. 組HCE回應電文 & 回 HCEMsgHandler
            rtnMessage = getFiscBusiness().prepareHCEResponseData(tota);

            // 12. 交易通知 (if need)
            this.sendToMailHunter();

            // 13. 交易結束通知主機 (By PCODE)
            this.transactionCloseConnect();

            // 14. 寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
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
            //getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage(rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
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
     * 4. SendToCBS/ASC(if need): 本行轉入-進帳務主機查詢帳號
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        if (StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
            getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
            // 轉入方為本行時,先送CBS查詢帳號
            feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
            String AATxTYPE = "0"; // 上CBS查詢、檢核
            String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
            rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();
        }
    }

    /**
     * 8. SendToCBS/ASC(if need): 進帳務主機入扣帳/手續費
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        if (getTxData().getMsgCtl().getMsgctlCbsFlag() == 1) {
            /* 進主機入扣帳/手續費 */
            String AATxTYPE = "1"; // 上CBS入扣帳
            String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
            rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
            tota = hostAA.getTota();

            if (rtnCode != FEPReturnCode.Normal) {
                if (getTxData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
                    /* 沖回跨行代收付(APTOT) */
                    rtnCode2 = getFiscBusiness().processAptot(true);
                }

                // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
            }
        }
    }

    /**
     * 9. 判斷是否需組 CON 電文回財金/本行轉入交易掛帳
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                /* +REP */
                getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) {/*for餘額查詢*/
                    getFeptxn().setFeptxnMsgflow("A2");/* ATM Response*/
                    this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
                    /*組回應電文回給 HCE, 寫入 HCE Response Queue*/
                } else {
                    /*轉帳類交易直接送confirm to FISC*/
                    getFeptxn().setFeptxnConRc("4001"); /* +CON */
                    /* 組 CON 電文送財金 */
                    if (rtnCode2 != CommonReturnCode.Normal) {
                        rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                    }
                    getFeptxn().setFeptxnConRc("A2");/* ATM Response*/
                    this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
                    /*組回應電文回給 HCE, 寫入 HCE Response Queue*/
                }
            } else { /*交易失敗FEPReturnCode<>Normal or FEPTXN_REP_RC <> 4001*/
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { // +REP
                        if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) { /*3WAY*/
                            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                                feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                            } else {
                                feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                        FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                            }
                            getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmHCEtxData().getTxChannel(), getLogContext()));
                            getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                            getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); /*Accept-Reverse*/
                            if (rtnCode2 != CommonReturnCode.Normal) {
                                rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                            }
                        } else {
                            getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); /*Reject-normal*/
                            getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmHCEtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                            getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                        }
                    } else { // -REP
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); /*Reject-normal*/
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getmHCEtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                        getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                    }
                } else { // fepReturnCode <> Normal
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); /*Reject-abnormal*/
                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmHCEtxData().getTxChannel(), getLogContext()));
                        getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                    }
                }
                /* IF  FEPTXN_REP_RC  有值 */
                getFeptxn().setFeptxnMsgflow("A2");/* ATM Response*/
                this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
                /*組回應電文回給 HCE, 寫入 HCE Response Queue*/
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
     * 字串指定區間的每個字元皆取代為'*'
     *
     * @param value
     * @param starIndex 開始位置索引
     * @param endIndex  結束位置索引
     * @return
     */
    private String setRangeCharAtStar(String value, int starIndex, int endIndex) {
        StringBuilder sb = new StringBuilder(value);
        for (int i = 9; i <= 11; i++) {
            sb.setCharAt(i, '*');
        }
        return sb.toString();
    }

    /**
     * 12. 交易通知 (if need)
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
    private void transactionCloseConnect() {
        try {
            if (StringUtils.isNotBlank(feptxn.getFeptxnPcode()) && "2522".equals(feptxn.getFeptxnPcode())) { //判斷Pcode是否為不為空值和為"2522"
                String AATxTYPE = ""; //不需提供此值
                String AATxRs = "N";  //不需等待主機回應
                String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
                rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE, AATxRs);
            }
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }
}