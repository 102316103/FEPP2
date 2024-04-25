package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * @author Jaime
 */
public class HCEIQSelfIssue extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;

    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    public HCEIQSelfIssue(HCEData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 1. Prepare():記錄MessageText & 準備回覆電文資料
            rtnCode = getATMBusiness().hce_PrepareFEPTxn();

            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());

            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTxn)
                addTxData();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getATMBusiness().checkRequestFromOtherChannel(getmHCEtxData(), tita.getINTIME());

            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS:送往CBS主機處理
                this.sendToCBS();


            }

            // 5. UpdateTxData: 更新交易記錄(FEPTxn)
            this.updateTxData();

            // 6.Response:組ATM回應電文 & 回 ATMMsgHandler
            if (StringUtils.isBlank(getmHCEtxData().getTxResponseMessage())) {
                rtnMessage = this.response();
            } else {
                rtnMessage = getmHCEtxData().getTxResponseMessage();
            }

            // 7. 交易通知 (if need)
            this.sendToMailHunter();

            //8. 	寫入傳送授權結果通知訊息初始資料 INBK2160 (if need)
            /*存 CBS_TOTA. SEND_FISC2160 */

            if ("Y".equals(feptxn.getFeptxnSend2160()) && ((feptxn.getFeptxnFiscFlag() == 0 && "000".equals(feptxn.getFeptxnCbsRc()))
                    || (feptxn.getFeptxnFiscFlag() == 1 && "4001".equals(feptxn.getFeptxnConRc())))) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getATMBusiness().prepareInbk2160();
            } else if ("A".equals(feptxn.getFeptxnSend2160())) {
                /*Prepare : 寫入傳送授權結果通知訊息初始資料 INBK2160*/
                rtnCode = getATMBusiness().prepareInbk2160();
            }

            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
            }

        } catch (Exception ex) {
            rtnCode = FEPReturnCode.ProgramException;
            this.getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
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
     * 4. SendToCBS:送往CBS主機處理
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
        /*進CBS主機檢核*/
        getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        /* 交易前置處理查詢處理 */
        String AATxTYPE = "0"; // 上CBS查詢、檢核
        String AA = getmHCEtxData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmHCEtxData());
        rtnCode = new CBS(hostAA, getmHCEtxData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
    }

    /**
     * 5. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {
        feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response); // (RESPONSE)
        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                    getmHCEtxData().getLogContext()));
            feptxn.setFeptxnErrMsg(msgfileExtMapper.selectByMsgfileErrorcode(feptxn.getFeptxnReplyCode()).get(0).getMsgfileShortmsg());
        } else {
            feptxn.setFeptxnReplyCode("    ");
        }
        feptxn.setFeptxnAaRc(rtnCode.getValue());
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
     * 6. Response:組ATM回應電文 & 回 ATMMsgHandler
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */

            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            String feptxnTxCode = feptxn.getFeptxnTxCode();
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getmHCEtxData());
            RefString rfs = new RefString();
            String totaToact;

            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
            SEND_HCE_GeneralTrans_RS rs = new SEND_HCE_GeneralTrans_RS();
            SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS_Body();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS_Body_MsgRs();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(atmReqheader.getMSGID());
            header.setCLIENTDT(atmReqheader.getCLIENTDT());

            if (!"000".equals(feptxn.getFeptxnCbsRc())) {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE("4001");
                header.setSEVERITY("INFO");
                header.setSTATUSDESC("");
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue()))) {
                    body.setTRANSFROUTBAL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue())));
                }
                if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue()))) {
                    body.setTRANSOUTAVBL(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue())));
                }
                body.setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
            }
            body.setTCBRTNCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));

            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setTXNSTAN(feptxn.getFeptxnStan());
            body.setCUSTOMERID(feptxn.getFeptxnIdno());
            body.setTXNTYPE(atmReqbody.getTXNTYPE());
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setACCTDATE(feptxn.getFeptxnTbsdy());

            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
                body.setHOSTACC_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()));
            }
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()))) {
                body.setHOSTRVS_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()));
            }
            if (feptxn.getFeptxnTxAmt() != null) {
                BigDecimal amt = feptxn.getFeptxnTxAmt();
                body.setTRANSAMT(new BigDecimal(StringUtils.leftPad(amt.toString().replace(".", ""), 11, '0')));
//				if(new BigDecimal(amt.intValue()).compareTo(amt) == 0) {
//					body.setTRANSAMT(amt.toString()+"00");
//				}else {
//					body.setTRANSAMT(amt.toString().replace(".", ""));
//				}
            }

            body.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            body.setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());

            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
                body.setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
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
     * 7. 交易通知 (if need)
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

}
