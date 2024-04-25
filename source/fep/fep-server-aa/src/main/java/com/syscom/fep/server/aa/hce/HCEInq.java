package com.syscom.fep.server.aa.hce;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
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

/**
 * @author Jaime
 */
public class HCEInq extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;

    public HCEInq(HCEData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {

            // 1. 記錄文字記錄檔Log (MessageText)
            // Do Nothing

            // 2. CheckBusinessRule: 商業邏輯檢核
            rtnCode = this.checkBusinessRule();

            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. 以ATM_TITA相關資料, 寫入 FEPTXN
                this.prepareFEPTXN();
            }

            // 4. 組回應電文
            if (StringUtils.isBlank(getmHCEtxData().getTxResponseMessage())) {
                rtnMessage = this.response();
            } else {
                rtnMessage = getmHCEtxData().getTxResponseMessage();
            }

            // 5. UpdateTxData: 更新交易記錄(FEPTxn)
            this.updateTxData();

        } catch (Exception ex) {
            rtnCode = FEPReturnCode.ProgramException;
            this.getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            if (rtnCode != FEPReturnCode.Normal) {
                // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
                sendEMS(getLogContext());
            }
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
     * 1. Prepare():記錄MessageText & 準備回覆電文資料
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode checkBusinessRule() throws Exception {

        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq hcebody = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
        Feptxn dbfeptxn = feptxnDao.getOldFeptxndata(hcebody.getINDATE(), header.getCLIENTTRACEID(), header.getCHANNEL());
        setFeptxn(dbfeptxn);
        getmHCEtxData().setFeptxn(dbfeptxn);
        if (getFeptxn() == null) {
            return FEPReturnCode.FEPTXNNotFound;
        } else {
            if (!hcebody.getTXNTYPE().equals("RQ")
                    && hcebody.getTERMINALID().equals(getFeptxn().getFeptxnAtmno())
                    && hcebody.getFSCODE().equals(getFeptxn().getFeptxnTxCode().trim())
                    && hcebody.getPCODE().equals(getFeptxn().getFeptxnPcode())
                    && hcebody.getTRNSFROUTBANK().substring(0, 3).equals(getFeptxn().getFeptxnTroutBkno())
                    && hcebody.getTRNSFROUTACCNT().equals(getFeptxn().getFeptxnTroutActno().trim())
                    && hcebody.getTRANSAMT().equals(new BigDecimal(FormatUtil.decimalFormat(getFeptxn().getFeptxnTxAmt(), "##0")))
            ) {
                rtnCode = FEPReturnCode.Normal;
            } else {
                rtnCode = FEPReturnCode.OtherCheckError;/* 其他類檢核錯誤 */
            }
        }
        return rtnCode;
    }

    /**
     * 3. 以ATM_TITA相關資料, 寫入 FEPTXN
     *
     * @throws Exception
     */
    private void prepareFEPTXN() throws Exception {
        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();

        feptxn.setFeptxnMsgflow("A3"); //INQ Request
        feptxn.setFeptxnTraceEjfno(getEj());
        feptxn.setFeptxnConTxCode(atmReqbody.getFSCODE() + "C"); //交易代號
        feptxn.setFeptxnConTxTime(atmReqbody.getINTIME()); //交易時間
        feptxn.setFeptxnConExcpCode(atmReqbody.getTXNTYPE()); //交易處理類別

        String traceEjfno = StringUtils.leftPad(String.valueOf(feptxn.getFeptxnTraceEjfno()), 7, "0");
        feptxn.setFeptxnConTxseq(traceEjfno.substring(traceEjfno.length() - 7));

    }


    /**
     * 4. 組回應電文
     *
     * @throws Exception
     */
    private String response() throws Exception {
        String rtnMessage = "";
        try {
            /* 組 ATM Response OUT-TEXT */
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
            SEND_HCE_GeneralTrans_RS hceRs = new SEND_HCE_GeneralTrans_RS();
            SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS_Body();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS_Body_MsgRs();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            hceRs.setBody(rsbody);

            header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(atmReqheader.getMSGID());
            header.setCLIENTDT(atmReqheader.getCLIENTDT());

            if (feptxn.getFeptxnTxrust().equals("A") && feptxn.getFeptxnReplyCode().equals("    ")) {
                header.setSYSTEMID("ATM");
                header.setSEVERITY("INFO");
                header.setSTATUSCODE("4001");

                if (feptxn.getFeptxnAccType() == 1) {
                    body.setHOSTACC_FLAG("Y");
                } else {
                    body.setHOSTACC_FLAG("N");
                }

                if (feptxn.getFeptxnBalb().compareTo(BigDecimal.ZERO) != 0 || feptxn.getFeptxnBalb().compareTo(BigDecimal.ZERO) != 0.00) {
                    body.setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                }

                if (feptxn.getFeptxnBala().compareTo(BigDecimal.ZERO) != 0 || feptxn.getFeptxnBala().compareTo(BigDecimal.ZERO) != 0.00) {
                    body.setTRANSOUTAVBL(feptxn.getFeptxnBalb());
                }


            } else {
                body.setHOSTACC_FLAG("N");
                if (feptxn.getFeptxnCbsTimeout() == 1) {
                    rtnCode = FEPReturnCode.HostResponseTimeout;
                    header.setSYSTEMID("FEP");
                    header.setSEVERITY("ERROR");
                    header.setSTATUSCODE(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FEP, getLogContext()), 4, " "));
                } else if (feptxn.getFeptxnReplyCode().length() > 4) {
                    header.setSYSTEMID("FEP");
                    header.setSEVERITY("ERROR");
                    header.setSTATUSCODE(feptxn.getFeptxnReplyCode().substring(feptxn.getFeptxnReplyCode().length() - 4));
                } else {
                    header.setSYSTEMID("ATM");
                    header.setSEVERITY("ERROR");
                    if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
                        header.setSTATUSCODE(feptxn.getFeptxnRepRc());
                    } else {
                        header.setSTATUSCODE("2999");
                    }
                }
            }


            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setTXNSTAN(feptxn.getFeptxnStan());
            body.setCUSTOMERID(feptxn.getFeptxnIdno());
            body.setTXNTYPE(atmReqbody.getTXNTYPE());
            body.setTRANSAMT(feptxn.getFeptxnTxAmt());
            body.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            body.setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            body.setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            body.setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());

            rtnMessage = XmlUtil.toXML(hceRs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * 5. UpdateTxData: 更新交易記錄(FEPTxn)
     */
    private void updateTxData() {

        feptxn.setFeptxnMsgflow("A4"); //Confirm RESPONSE

        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnConReplyCode(StringUtils.rightPad(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FEP, getLogContext()), 4, " "));
        }

        try {
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
        }

        if (rtnCode != FEPReturnCode.Normal) {
            // 回寫檔案 (FEPTxn) 發生錯誤
            this.feptxn.setFeptxnReplyCode("L013");
            sendEMS(getLogContext());
        }
    }

}
