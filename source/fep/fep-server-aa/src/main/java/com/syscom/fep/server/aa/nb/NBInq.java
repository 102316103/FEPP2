package com.syscom.fep.server.aa.nb;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;

/**
 * @author Jaime
 */
public class NBInq extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;

    public NBInq(NBData txnData) throws Exception {
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

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. 以ATM_TITA相關資料, 寫入 FEPTXN
                this.prepareFEPTXN();
            }

            // 4. 組回應電文
            rtnMessage = this.response();

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
            getmNBtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmNBtxData().getLogContext().setMessage(rtnMessage);
            getmNBtxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmNBtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    /**
     * 2. CheckBusinessRule: 商業邏輯檢核
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode checkBusinessRule() throws Exception {

        RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmNBtxData().getTxNbfepObject().getRequest().getBody().getRq().getHeader();
        RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = this.getmNBtxData().getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
        Feptxn dbfeptxn = feptxnDao.getOldFeptxndata(nbbody.getINDATE(), header.getCLIENTTRACEID(), header.getCHANNEL());
        setFeptxn(dbfeptxn);
        getmNBtxData().setFeptxn(dbfeptxn);
        if (getFeptxn() == null) {
            return FEPReturnCode.FEPTXNNotFound;
        } else {
            if (!nbbody.getTXNTYPE().equals("RQ")
                    && nbbody.getTERMINALID().equals(getFeptxn().getFeptxnAtmno())
                    && nbbody.getFSCODE().equals(getFeptxn().getFeptxnTxCode().trim())
                    && nbbody.getPCODE().equals(getFeptxn().getFeptxnPcode())
                    && nbbody.getTRNSFROUTBANK().substring(0, 3).equals(getFeptxn().getFeptxnTroutBkno())
                    && nbbody.getTRNSFROUTACCNT().equals(getFeptxn().getFeptxnTroutActno().trim())
                    && nbbody.getTRANSAMT().equals(new BigDecimal(FormatUtil.decimalFormat(getFeptxn().getFeptxnTxAmt(), "##0")))
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
        RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmNBReq().getBody().getRq().getSvcRq();

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
            RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmNBReq().getBody().getRq().getHeader();
            RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmNBReq().getBody().getRq().getSvcRq();
            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

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

                if (feptxn.getFeptxnBala().compareTo(BigDecimal.ZERO) != 0 || feptxn.getFeptxnBalb().compareTo(BigDecimal.ZERO) != 0.00) {
                    body.setTRANSOUTAVBL(feptxn.getFeptxnBalb());
                }

                body.setTRANSAMTOUT(feptxn.getFeptxnTxAmt().add(feptxn.getFeptxnFeeCustpay()));

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
            body.setACCTDATE(feptxn.getFeptxnTbsdy());
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setTRANSAMT(feptxn.getFeptxnTxAmt());
            body.setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
            body.setTRNSFROUTIDNO(feptxn.getFeptxnIdno());
            body.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            body.setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            body.setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            body.setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
            body.setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            body.setTRNSFRINNOTE(atmReqbody.getTRNSFRINNOTE());
            body.setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());

            rtnMessage = XmlUtil.toXML(rs);
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
