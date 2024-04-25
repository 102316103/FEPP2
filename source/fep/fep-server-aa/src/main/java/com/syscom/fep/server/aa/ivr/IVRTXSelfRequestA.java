package com.syscom.fep.server.aa.ivr;

import com.syscom.fep.base.aa.IVRData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.mapper.EaitxnMapper;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import com.syscom.fep.vo.text.ivr.SEND_VO_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author
 */
public class IVRTXSelfRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private EaitxnMapper eaitxnMapper = SpringBeanFactoryUtil.getBean(EaitxnMapper.class);

    public IVRTXSelfRequestA(IVRData txnData) throws Exception {
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
            rtnCode = getFiscBusiness().ivr_PrepareFEPTxn();

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                this.addTxData();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getIVRData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = this.getFiscBusiness().checkRequestFromOtherChannel(this.getIVRData(), tita.getINTIME());
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
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

            if (rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                if (1 == getTxData().getMsgCtl().getMsgctlUpdateAptot()) {
                    // 7. ProcessAPTOT:更新跨行代收付
                    rtnCode2 = fiscBusiness.processAptot(false);
                }
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 8 SendToCBS/ASC: 送主機處理帳務
                this.sendToCBS2();
                if (rtnCode == FEPReturnCode.Normal) {
                    feptxn.setFeptxnTxrust("A");
                    if (1 == getTxData().getMsgCtl().getMsgctlUpdateAptot()) {
                        /* 沖回跨行代收付(APTOT) */
                        rtnCode2 = fiscBusiness.processAptot(false);
                    }
                    // GOTO label_END_OF_FUNC /* 組回傳 ATM 電文 */
                }
            }

            // 9. label_END_OF_FUNC :判斷是組 CON 電文回財金( if need)
            rtnCode = this.labelEndOfFunc();

            // 10. 	更新交易記錄(FEPTXN)
            this.updateTxData2();

            // 11. 	組IVR回應電文 & 回 IVRMsgHandler
            rtnMessage = this.response();

            // 12.交易通知
            this.sendToMailHunter();

            // 13.交易結束通知主機(By PCODE)
            sendToCBS3();

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
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
     * 4. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        // 轉入方為本行時,先送CBS查詢帳號
        String AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getIVRData());
        rtnCode = new CBS(hostAA, getIVRData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();

    }

    /**
     * 8. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");
        /* 進主機入扣帳/手續費 */
        String AATxTYPE = "1"; // 上CBS入扣帳
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getIVRData());
        rtnCode = new CBS(hostAA, getIVRData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }

    /**
     * 9. label_END_OF_FUNC :判斷是組 CON 電文回財金( if need)
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                    rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                } else {
                    /* for代理提款-ATM_3way 交易 */
                    feptxn.setFeptxnTxrust("B"); /* PENDING */
                    getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                }
            } else {
                getLogContext().setProgramName(ProgramName);
                // 交易失敗
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { // +REP
                        if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                            feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                        } else {
                            feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                    FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                        }
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getTxData().getTxChannel(), getLogContext()));
                        getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
                        rtnCode2 = getFiscBusiness().sendConfirmToFISC();
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else { // -REP
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getTxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                        getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                    }
                } else { // fepReturnCode <> Normal
                    getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    }

                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getTxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                        getFeptxn().setFeptxnErrMsg(this.getLogContext().getResponseMessage());
                    }
                    getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE after:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                }
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
    private void updateTxData2() {
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
     * // 12.交易通知
     */
    private void sendToMailHunter() {
        try {
            String noticeType = feptxn.getFeptxnNoticeType();
            if (StringUtils.isNotBlank(noticeType) && "4001".equals(feptxn.getFeptxnRepRc()) && "4001".equals(feptxn.getFeptxnConRc())) {
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
     * 11. 組ATM回應電文 & 回 ATMMsgHandler
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getIVRData().getTxObject().getRequest().getBody().getRq().getHeader();
            RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getIVRData().getTxObject().getRequest().getBody().getRq().getSvcRq();
            SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs();


            header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(atmReqheader.getMSGID());
            header.setCLIENTDT(atmReqheader.getCLIENTDT());

            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                header.setSEVERITY("ERROR");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(feptxn.getFeptxnRepRc());
                header.setSEVERITY("ERROR");
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode().substring(2, 6));
                header.setSEVERITY("ERROR");
            } else {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE("4001");
                header.setSEVERITY("INFO");
                header.setSTATUSDESC("");
            }

            if (header.getSEVERITY().equals("ERROR")) {
                if (StringUtils.isNotBlank(feptxn.getFeptxnErrMsg())) {
                    header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
                }
            }

            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setCUSTOMERID(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_CUSID.getValue()));
            body.setTXNTYPE(atmReqbody.getTXNTYPE());

            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
                body.setHOSTACC_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()));
            }
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()))) {
                body.setHOSTRVS_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()));
            }
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setTRANSAMT(feptxn.getFeptxnTxAmt()); //轉帳金額

            if (feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue()))) {
                    body.setTRANSFROUTBAL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue())));
                }
                /*金額格式 S9(11)V99 前面有正負符號，右靠左補0*/
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue()))) {
                    body.setTRANSOUTAVBL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue())));
                }
            } else {
                body.setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                body.setTRANSOUTAVBL(feptxn.getFeptxnBala());
            }

            body.setTRANSAMTOUT(feptxn.getFeptxnTxAmtAct()); //實際轉出金額
            body.setTRNSFROUTIDNO(feptxn.getFeptxnIdno()); //轉出帳號統編

//            Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
//            if (eaitxn != null && StringUtils.isNotBlank(eaitxn.getEaitxnTrnsfroutname())) {
//                body.setTRNSFROUTNAME(eaitxn.getEaitxnTrnsfroutname()); //轉出帳戶戶名
//            }
            body.setTRNSFROUTBANK(this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue())); //轉出銀行代號
            body.setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno()); //轉出帳號
            body.setTRNSFRINBANK(this.getImsPropertiesValue(tota, ImsMethodName.TOBANK.getValue())); //轉入銀行代號
            body.setTRNSFRINACCNT(feptxn.getFeptxnTrinActno()); //轉入帳號
            body.setCLEANBRANCHOUT(feptxn.getFeptxnBrno()); //轉出帳號帳務行代號
            body.setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno()); //轉入帳號帳務行代
            body.setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay()); //客戶應付手續費真正收取客戶手續費
            body.setFISCFEE(feptxn.getFeptxnNpsFeeFisc()); //應付財金手續費
            body.setOTHERBANKFEE(feptxn.getFeptxnNpsFeeRcvr()); //應付他行手續費
            body.setCHAFEE_BRANCH(this.getImsPropertiesValue(tota, ImsMethodName.CHAFEE_BRANCH.getValue())); //優惠手續費負擔分行
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.CHAFEEAMT.getValue()))){
                body.setCHAFEEAMT(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.CHAFEEAMT.getValue()))); //手續費減免金額
            }else{
                body.setCHAFEEAMT(new BigDecimal(0)); //手續費減免金額
            }

            body.setTRNSFRINNOTE(atmReqbody.getTRNSFRINNOTE()); //給收款人訊息
            body.setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD()); //付款人自我備註
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()))){
                body.setOVTPT(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()).substring(0, 1)); //交易日註記
                body.setOVFEETY(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()).substring(1, 2)); //手續費優惠類別
                body.setOVCIRCU(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()).substring(1, 2)); //轉出存戶性質別
            }

            body.setOVOTHER(""); // VO備用
            body.setCUSTOMERNATURE(this.getImsPropertiesValue(tota, ImsMethodName.E_TRANSOUTCUST.getValue())); //他行轉入帳號存戶性質別
            body.setTCBRTNCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue())); //自行回應結果


            SEND_VO_GeneralTrans_RS vo_rs = new SEND_VO_GeneralTrans_RS();
            vo_rs.setBody(new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body());
            vo_rs.getBody().setRs(new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs());
            vo_rs.getBody().getRs().setHeader(header);
            vo_rs.getBody().getRs().setSvcRs(body);
            rtnMessage = XmlUtil.toXML(vo_rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 13.交易結束通知主機
     *
     * @throws Exception
     */
    private void sendToCBS3() throws Exception {
        String AATxTYPE = "";
        String AATxRs = "N";
        String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getIVRData());
        rtnCode = new CBS(hostAA, getIVRData()).sendToCBS(AATxTYPE, AATxRs);
    }

}
