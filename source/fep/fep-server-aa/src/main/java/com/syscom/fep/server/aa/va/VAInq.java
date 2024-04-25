package com.syscom.fep.server.aa.va;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

/**
 * @author Jaime
 */
public class VAInq extends ATMPAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);

    public VAInq(NBData txnData) throws Exception {
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

            // 4. 	組回應電文回給 前端
            if (StringUtils.isBlank(getmNBtxData().getTxResponseMessage())) {
                rtnMessage = this.response();
            } else {
                rtnMessage = getmNBtxData().getTxResponseMessage();
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

        // 1.1 檢核 FEPTXN 是否存在
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = this.getmNBtxData().getTxVafepObject().getRequest().getBody().getRq().getHeader();
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = this.getmNBtxData().getTxVafepObject().getRequest().getBody().getRq().getSvcRq();
        Feptxn dbfeptxn = feptxnDao.getOldFeptxndata(nbbody.getINDATE(), header.getCLIENTTRACEID(), header.getCHANNEL());
        setFeptxn(dbfeptxn);
        getmNBtxData().setFeptxn(dbfeptxn);
        if (getFeptxn() == null) {
            return FEPReturnCode.FEPTXNNotFound;
        } else {
            if (!nbbody.getTXNTYPE().equals("RQ")
                    && nbbody.getTERMINALID().equals(feptxn.getFeptxnAtmno())
                    && nbbody.getFSCODE().equals(feptxn.getFeptxnTxCode().trim())
                    && nbbody.getPCODE().equals(feptxn.getFeptxnPcode())
                    && nbbody.getVACATE().equals(feptxn.getFeptxnNoticeId().substring(0, 2))
                    && nbbody.getAEIPYTP().equals(feptxn.getFeptxnNoticeId().substring(2, 4))
                    && nbbody.getTAXIDNO().equals(feptxn.getFeptxnIdno().trim())
            ) {
                rtnCode = FEPReturnCode.Normal;
            } else {
                rtnCode = FEPReturnCode.OtherCheckError;/* 其他類檢核錯誤 */
            }
        }

        // 1.2 檢核 VATXN 是否存在
        Vatxn queryVatxn = vatxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
        if (queryVatxn != null) {
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmVAReq().getBody().getRq().getSvcRq();
            if (!queryVatxn.getVatxnPcode().equals(atmReqbody.getPCODE())
                    || !queryVatxn.getVatxnCate().equals(atmReqbody.getVACATE())
                    || !queryVatxn.getVatxnType().equals(atmReqbody.getAEIPYTP())) {
                rtnCode = FEPReturnCode.OtherCheckError;
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
        RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmVAReq().getBody().getRq().getSvcRq();

        feptxn.setFeptxnMsgflow("A3"); //INQ Request
        feptxn.setFeptxnTraceEjfno(getEj());
        feptxn.setFeptxnConTxCode(atmReqbody.getFSCODE() + "C"); //交易代號
        feptxn.setFeptxnConTxTime(atmReqbody.getINTIME()); //交易時間
        feptxn.setFeptxnConExcpCode(atmReqbody.getTXNTYPE()); //交易處理類別

        String traceEjfno = StringUtils.leftPad(String.valueOf(feptxn.getFeptxnTraceEjfno()), 7, "0");
        feptxn.setFeptxnConTxseq(traceEjfno.substring(traceEjfno.length() - 7)); // (只取7位)

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
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmVAReq().getBody().getRq().getHeader();
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmVAReq().getBody().getRq().getSvcRq();
            RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = this.getmVAReq().getBody().getRq().getSvcRq().getSENDDATA();
            SEND_VA_GeneralTrans_RS rs = new SEND_VA_GeneralTrans_RS();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body rsbody = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

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


            } else if (feptxn.getFeptxnCbsTimeout() == 1) {
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
            Vatxn Vatxn = vatxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setTXNSTAN(feptxn.getFeptxnStan());
            body.setCUSTOMERID(feptxn.getFeptxnIdno());
            body.setTXNTYPE(atmReqbody.getTXNTYPE());
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setTRANSAMT(atmReqbody.getTRANSAMT());
            body.setAEIPYTP(atmReqbody.getAEIPYTP());
            body.setTAXIDNO(Vatxn.getVatxnIdno());
            body.setAEIPCRBK(atmReqbody.getAEIPCRBK());
            body.setCLACTNO(feptxn.getFeptxnTroutActno());

            Vatxn queryVatxn = vatxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
            if (queryVatxn != null) {
                body.setTAXIDNO(queryVatxn.getVatxnIdno());
                if (queryVatxn.getVatxnCate().equals("02")) {
                    replydata.setPAYUNTNO(queryVatxn.getVatxnBusinessUnit());
                    replydata.setTAXTYPE(queryVatxn.getVatxnPaytype());
                    replydata.setPAYFEENO(queryVatxn.getVatxnFeeno());
                } else {
                    replydata.setAELFTP(queryVatxn.getVatxnItem());
                    replydata.setACRESULT(queryVatxn.getVatxnAcresult());
                    replydata.setRESULT(queryVatxn.getVatxnResult());
                    replydata.setACSTAT(queryVatxn.getVatxnAcstat());
                    replydata.setCHKCELL(queryVatxn.getVatxnTelresult());
                    replydata.setAEIPYUES(senddata.getAEIPYUES());
                }

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
