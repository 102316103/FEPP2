package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.mapper.EaitxnMapper;
import com.syscom.fep.mybatis.model.Eaitxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author
 */
public class NBSelfIssueRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
    private String AATxTYPE = "";
    private EaitxnMapper eaitxnMapper = SpringBeanFactoryUtil.getBean(EaitxnMapper.class);

    public NBSelfIssueRequestA(NBData txnData) throws Exception {
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
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                this.sendToCBS();
            }

            FISC fiscBusiness = getFiscBusiness();
            if (feptxn.getFeptxnFiscFlag() != 0 && rtnCode == FEPReturnCode.Normal) {
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
            }

            boolean repRcEq4001 = true;
            if (feptxn.getFeptxnFiscFlag() != 0 && rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

            if (feptxn.getFeptxnFiscFlag() != 0 && rtnCode == FEPReturnCode.Normal && repRcEq4001) {
                // 7. SendToCBS/ASC: 送主機處理帳務
                this.sendToCBS2();
                if (rtnCode == FEPReturnCode.Normal && rtnCode2 == FEPReturnCode.Normal && repRcEq4001 && "000".equals(feptxn.getFeptxnCbsRc()) && "1".equals(feptxn.getFeptxnAccType())) {
                    //  ProcessAPTOT:更新跨行代收付
                    if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()) {
                        rtnCode = fiscBusiness.processAptot(false);
                    }
                }
            }
            // 8. label_END_OF_FUNC : 判斷是否需組 CON 電文回財金
            rtnCode = this.labelEndOfFunc();

            // 9. 	更新交易記錄(FEPTXN)
            this.updateTxData();

            // 10. 	組NB回應電文 & 回 NBMsgHandler
            // 電文內容格式請參照: SEND_HCE_GeneralTrans_RS
            rtnMessage = this.prepareNBResponseDataNBSelfIssueRequestA(tota);

            // 11. 交易通知 (if need)
            this.sendToMailHunter();

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
     * 1. 	組回應電文回給 NB
     */
    protected String prepareNBResponseDataNBSelfIssueRequestA(Object tota) throws Exception {
        String rtnMessage = "";
        try {

            RCV_NB_GeneralTrans_RQ atmReq = this.getmNBReq();

            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());

            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())
                    && !"0000".equals(feptxn.getFeptxnCbsRc()) && !"4001".equals(feptxn.getFeptxnCbsRc())) {
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                if ("0".equals(AATxTYPE)) {
                    rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnCbsRc());
                    rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                    rs.getBody().getRs().getHeader().setSTATUSDESC(feptxn.getFeptxnErrMsg());
                } else {
                    rs.getBody().getRs().getHeader().setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));
                    rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                    rs.getBody().getRs().getHeader().setSTATUSDESC(feptxn.getFeptxnErrMsg());
                }

            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(String.valueOf(feptxn.getFeptxnRepRc()));
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getHeader().setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
                rs.getBody().getRs().getHeader().setSYSTEMID("FEP");
                rs.getBody().getRs().getHeader().setSTATUSCODE(String.valueOf(feptxn.getFeptxnReplyCode()).substring(2,6));
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getHeader().setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else {
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue())));
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue()))) {
                    rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue())));
                }

                if (eaitxn.getEaitxnTransamtout() != null) {
                    rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(eaitxn.getEaitxnTransamtout());
                } else {
                    BigDecimal amt = feptxn.getFeptxnTxAmt().add(feptxn.getFeptxnFeeCustpay());
                    rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(amt);
                }

                if (feptxn.getFeptxnFeeCustpay() != null) {
                    rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                }
                rs.getBody().getRs().getSvcRs().setFISCFEE(atmReq.getBody().getRq().getSvcRq().getFISCFEE());
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(atmReq.getBody().getRq().getSvcRq().getOTHERBANKFEE());
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(this.getImsPropertiesValue(tota, ImsMethodName.CHAFEE_BRANCH.getValue()));
                rs.getBody().getRs().getSvcRs().setCHAFEEAMT(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.CHAFEEAMT.getValue())));
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
            rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
            rs.getBody().getRs().getSvcRs().setACCTDATE(feptxn.getFeptxnTbsdy());
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()));

            }
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()))) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()));
            }

            rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());

            if (feptxn.getFeptxnTxAmt() != null) {
                BigDecimal amt = feptxn.getFeptxnTxAmt();
                rs.getBody().getRs().getSvcRs().setTRANSAMT(amt);

            }

            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());

            if (eaitxn != null && StringUtils.isNotBlank(String.valueOf(eaitxn.getEaitxnTrnsfroutname()))) {
                rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(eaitxn.getEaitxnTrnsfroutname());
            }

            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
            rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()));
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.E_TRANSOUTCUST.getValue()))) {
                rs.getBody().getRs().getSvcRs().setCUSTOMERNATURE(this.getImsPropertiesValue(tota, ImsMethodName.E_TRANSOUTCUST.getValue()));
            }

            rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
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

        AATxTYPE = "0"; // 上CBS查詢、檢核
        getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();

    }

    /**
     * 7. SendToCBS/ASC: 送主機處理帳務
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");

        /* 進主機入扣帳/手續費 */
        AATxTYPE = "1"; // 上CBS入扣帳

        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();

    }

    /**
     * 8. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { /*+REP*/
                if (DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                    /*轉帳交易直接送 Confirm 給財金*/
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);/*+CON*/
                    rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                } else {
                    /* for代理提款-ATM_3way 交易 */
                    getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                    feptxn.setFeptxnTxrust("B"); /* PENDING */
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
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                FEPChannel.FEP, getnBData().getTxChannel(), getnBData().getLogContext()));
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length() > 256 ? getLogContext().getResponseMessage().substring(0, 256) : getLogContext().getResponseMessage());
                        feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
                        rtnCode2 = getFiscBusiness().sendConfirmToFISC();

                    } else { // -REP
                        feptxn.setFeptxnTxrust("R"); /* Reject-normal */
                        feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
                                getnBData().getTxChannel(), getnBData().getLogContext()));
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length() > 256 ? getLogContext().getResponseMessage().substring(0, 256) : getLogContext().getResponseMessage());
                        if ("TS".equals(feptxn.getFeptxnTxCode())) {
                            /*EAI沖正存款主機手續費優惠次數(原解圈) */
                            AATxTYPE = "4";
                            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
                            feptxn.setFeptxnCbsTxCode(AA);
                            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
                            rtnCode2 = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
//    						tota = hostAA.getTota();
                        }

                    }
                } else { // fepReturnCode <> Normal
                    getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    }

                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getnBData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
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
     * 9. 更新交易記錄(FEPTXN)
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
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response*/
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
    }

    /**
     * 9. 更新交易記錄
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
     * 11. 交易通知
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

}
