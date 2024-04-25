package com.syscom.fep.server.aa.va;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Calendar;

/**
 * @author Jeff
 */
public class LFSelfIssue extends ATMPAABase {
    private Object tota = null;
    private RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private String rtnMessage = "";
    private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);

    public LFSelfIssue(NBData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        Vatxn vatxn = new Vatxn();
        try {
            // 1. 交易記錄初始資料
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = getATMBusiness().VAPrepareFEPTXN();
            }

            // 2. 新增交易記錄(FEPTXN)
            if (_rtnCode == FEPReturnCode.Normal) {
                addTxData();
                // writeLog("新增交易記錄(FEPTXN)", ProgramFlow.AAIn);
            }

            // 3. 商業邏輯檢核(ATM電文)
            _rtnCode = getATMBusiness().checkChannelEJFNO();
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = checkBusinessRule();
                // writeLog("商業邏輯檢核(ATM電文) : RtnCode : " + _rtnCode, ProgramFlow.AAIn);
            }

            // 4. Prepare約定及核驗交易(VATXN)記錄(if need)
            if (_rtnCode == CommonReturnCode.Normal) {
                RefBase<Vatxn> vatxnRefBase = new RefBase<>(vatxn);
                prepareVATXNforVALE(vatxnRefBase, tita);
                vatxn = vatxnRefBase.get();
                vatxnMapper.insertSelective(vatxn);
                // writeLog("Prepare約定及核驗交易(VATXN)記錄", ProgramFlow.AAIn);
            }

            //			5. 	SendToCBS/ASC: 帳務主機處理
            if (_rtnCode == CommonReturnCode.Normal) {
                getFeptxn().setFeptxnTxrust("S");/* Reject-abnormal */
                /*一般金融卡帳號*/
                /*進CBS主機檢核*/
                getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
                /* CBSProcess_ABVAI001(組送CBS 主機撤銷通知Request交易電文).doc*/
                String AATxTYPE = "0"; // 上CBS入扣帳
                String AA = getmNBtxData().getMsgCtl().getMsgctlTwcbstxid();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmNBtxData());
                this.getmNBtxData().setVatxn(vatxn);
                _rtnCode = new CBS(hostAA, getmNBtxData()).sendToCBS(AATxTYPE);
                tota = hostAA.getTota();
                // writeLog("SendToCBS/ASC: 帳務主機處理", ProgramFlow.AAIn);
            }

            //			6. 	label_END_OF_FUNC 更新交易記錄
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = labelEndOfFunc();
                // writeLog("label_END_OF_FUNC 更新交易記錄", ProgramFlow.AAIn);
            }

            // 7. 	@新增約定及核驗交易(VATXN)記錄(if need)
            vatxn.setVatxnTxrust(getFeptxn().getFeptxnTxrust());
            Vatxn po = vatxnMapper.selectByPrimaryKey(vatxn.getVatxnTxDate(), vatxn.getVatxnEjfno());
            vatxnMapper.updateByPrimaryKeySelective(vatxn);

            // 8. 	組ATM回應電文 & 回 ATMMsgHandler
            this.response(vatxn);

        } catch (Exception ex) {
            rtnMessage = "";
            _rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(getLogContext());
        } finally {
            getmNBtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmNBtxData().getLogContext().setMessage(rtnMessage);
            getmNBtxData().getLogContext().setProgramName(this.aaName);
            getmNBtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG, getLogContext());
        }
        // 先組回應ATM 故最後return空字串m
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
                _rtnCode = FEPReturnCode.FEPTXNInsertError;
            }
        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            _rtnCode = FEPReturnCode.FEPTXNInsertError;
        }
    }

    /**
     * "3. 商業邏輯檢核"
     *
     * @return
     */
    private FEPReturnCode checkBusinessRule() {
        FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
        feptxn.setFeptxnIcSeqno(StringUtils.EMPTY);
        feptxn.setFeptxnIcmark(StringUtils.EMPTY);
        try {
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = tita.getBody().getRq().getHeader();
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = tita.getBody().getRq().getSvcRq();
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = tita.getBody().getRq().getSvcRq().getSENDDATA();

            // 3.1 檢核外圍 EJ 是否重覆
            rtnCode = getATMBusiness().checkChannelEJFNO();
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnCode;
            }

            //			3.2	   檢核前端電文
            if (StringUtils.isBlank(nbbody.getVACATE()) && StringUtils.isBlank(nbbody.getAEIPYTP()) && StringUtils.isBlank(senddata.getAELFTP())) {
                rtnCode = FEPReturnCode.OtherCheckError;
                // writeLog("檢核前端電文錯誤", ProgramFlow.AAIn);
            }

            /*晶片卡核驗*/
            if (nbbody.getAEIPYTP().equals("00") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("00") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("01") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("02") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("03") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("04")) {
                // writeLog("晶片卡核驗錯誤", ProgramFlow.AAIn);
                return FEPReturnCode.OtherCheckError;
            }

            /*無卡核驗*/
            if (!nbbody.getAEIPYTP().equals("00") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("11") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("12") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("13") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("14") &&
                    !nbbody.getSENDDATA().getAELFTP().equals("15")) {
                // writeLog("無卡核驗", ProgramFlow.AAIn);
                return FEPReturnCode.OtherCheckError;
            }

            //			3.3	檢核委託單位檔
            if (StringUtils.isBlank(tita.getBody().getRq().getSvcRq().getVACATE())) {
                return FEPReturnCode.VACATENONotFound;   /* 約定及核驗服務業務類別不存在 */
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 4. Prepare約定及核驗交易(VATXN)記錄
     *
     * @param vatxn
     * @param tita
     * @return
     */
    public FEPReturnCode prepareVATXNforVALE(RefBase<Vatxn> vatxn, RCV_VA_GeneralTrans_RQ tita) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            vatxn.get().setVatxnTxDate(getFeptxn().getFeptxnTxDate());
            vatxn.get().setVatxnEjfno(getFeptxn().getFeptxnEjfno());
            vatxn.get().setVatxnBkno(getFeptxn().getFeptxnBkno());
            vatxn.get().setVatxnPcode(getFeptxn().getFeptxnPcode());
            vatxn.get().setVatxnTxTime(getFeptxn().getFeptxnTxTime());
            vatxn.get().setVatxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            vatxn.get().setVatxnReqRc(getFeptxn().getFeptxnReqRc());
            vatxn.get().setVatxnRepRc(getFeptxn().getFeptxnRepRc());
            vatxn.get().setVatxnConRc(getFeptxn().getFeptxnConRc());
            vatxn.get().setVatxnTxrust(getFeptxn().getFeptxnTxrust());
            vatxn.get().setVatxnCate(feptxn.getFeptxnNoticeId().substring(0, 2));//'業務類別代號’
            vatxn.get().setVatxnType(feptxn.getFeptxnNoticeId().substring(2, 4));//‘交易類別’
            vatxn.get().setVatxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            vatxn.get().setVatxnTroutActno(getFeptxn().getFeptxnTroutActno());
            vatxn.get().setVatxnBrno(getFeptxn().getFeptxnBrno());
            vatxn.get().setVatxnZoneCode(getFeptxn().getFeptxnZoneCode());
            vatxn.get().setVatxnItem(tita.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP());

            switch (vatxn.get().getVatxnItem()) {
                case "00": //'除卡片及帳號外，無其他核驗項目

                case "01": // 身份證號或外國人統一編號
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    break;
                case "02": // 持卡人之行動電話號碼
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    break;
                case "03": // 持卡人之出生年月日
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    break;
                case "04": // 持卡人之住家電話號碼
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "11": // 持卡人之身分證號及行動電話號碼
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    break;
                case "12": // 持卡人之身分證號、行動電話號碼及出生年月
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    break;
                case "13": // 持卡人之身分證號、行動電話號碼及住家電話
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "14": // 持卡人之身分證號、行動電話號碼、出生年月日及住家電話號碼
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "15": // 持卡人之身份證號或外國人統一編號
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
            }
            vatxn.get().setVatxnUse(tita.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYUES());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareVATXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    // 帳務主機處理
    // 6. 	label_END_OF_FUNC 更新交易記錄
    private FEPReturnCode labelEndOfFunc() {
        try {
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);

            if (_rtnCode != CommonReturnCode.Normal) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                        getmNBtxData().getLogContext()));
                feptxn.setFeptxnErrMsg(msgfileExtMapper.select(feptxn.getFeptxnReplyCode()).getMsgfileShortmsg());
            } else {
                getFeptxn().setFeptxnReplyCode("    ");
            }

            getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
            getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /* AA Complete */

            if (_rtnCode == CommonReturnCode.Normal) {
                if ("3".equals(getFeptxn().getFeptxnWay())) {
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); // PENDING
                } else {
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                }
            } else {
                getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
            }

            try {
                if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) > 0) {
                    return FEPReturnCode.Normal;
                } else {
                    getFeptxn().setFeptxnReplyCode("L013");
                    return FEPReturnCode.FEPTXNUpdateError;
                }
            } catch (Exception ex) {
                getFeptxn().setFeptxnReplyCode("L013");
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(ProgramName + ".UpdateTxData");
                sendEMS(getLogContext());
                return FEPReturnCode.FEPTXNUpdateError;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToConfirm"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 8. 	組ATM回應電文 & 回 ATMMsgHandler
     *
     * @param vatxn
     * @return
     * @throws Exception
     */
    private String response(Vatxn vatxn) throws Exception {
        try {
            /* 組 ATM Response OUT-TEXT */
            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            String feptxnTxCode = feptxn.getFeptxnTxCode();
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getmNBtxData());
            RefString rfs = new RefString();
            String totaToact;

            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmVAReq().getBody().getRq().getSvcRq();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

            header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(header.getMSGID());
            header.setCLIENTDT(header.getCLIENTDT());

            if (!"000".equals(feptxn.getFeptxnCbsRc())) {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                body.setRSPRESULT(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnReplyCode()));
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(NormalRC.FISC_ATM_OK);
                header.setSEVERITY("INFO");
                header.setSTATUSDESC("");
            }

            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setTXNSTAN(feptxn.getFeptxnStan());
            body.setCUSTOMERID(feptxn.getFeptxnIdno());
            body.setTXNTYPE(tita.getTXNTYPE());
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setTRANSAMT(tita.getTRANSAMT());
            body.setAEIPYTP(tita.getAEIPYTP());
            body.setTAXIDNO(vatxn.getVatxnIdno());
            body.setAEIPCRBK(tita.getAEIPCRBK());
            body.setCLACTNO(feptxn.getFeptxnTroutActno());

            replydata.setAELFTP(vatxn.getVatxnItem());
            replydata.setACRESULT(vatxn.getVatxnAcresult());
            replydata.setRESULT(vatxn.getVatxnResult());
            replydata.setACSTAT(vatxn.getVatxnAcstat());
            replydata.setCHKCELL(vatxn.getVatxnTelresult());
            replydata.setAEIPYUES(this.getImsPropertiesValue(tota, ImsMethodName.AEIPYUES.getValue()));

            body.setREPLYDATA(replydata);
            SEND_VA_GeneralTrans_RS va_rs = new SEND_VA_GeneralTrans_RS();
            va_rs.setBody(new SEND_VA_GeneralTrans_RS_Body());
            va_rs.getBody().setRs(new SEND_VA_GeneralTrans_RS_Body_MsgRs());
            va_rs.getBody().getRs().setHeader(header);
            va_rs.getBody().getRs().setSvcRs(body);
            rtnMessage = XmlUtil.toXML(va_rs);

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    public void writeLog(String msg, ProgramFlow flow) {
        LogData logData = new LogData();
        logData.setProgramFlowType(flow);
        logData.setSubSys(SubSystem.NB);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(ProgramName);
        logData.setMessage(msg);
        // logData.setRemark("MBService Receive Request");
        logData.setServiceUrl("/mb/recv");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
    }
}
