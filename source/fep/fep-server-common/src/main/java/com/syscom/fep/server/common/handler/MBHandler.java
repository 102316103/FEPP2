package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.inbk.response.S0710Response;
import com.syscom.fep.vo.text.nb.*;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MBHandler extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private NBData nbData;
    private String _timeFormat = "yyyy/MM/dd HH:mm:ss:sss";
    private LogData logData;
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

    public MBHandler() {
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        // data = data.Replace("\r\n", string.Empty);
        LogHelperFactory.getTraceLogger().trace("Dispatch rcv " + data);
        String res = "";
        RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = new RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header vaatmReqheader = new RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_NB_GeneralTrans_RQ req = new RCV_NB_GeneralTrans_RQ();
        RCV_VA_GeneralTrans_RQ reqva = new RCV_VA_GeneralTrans_RQ();

        try {
            // 前面多了奇怪的符號 ex:�<?xml version="1.0" encoding="utf-8"?><SOAP-ENV:Envelope><SOAP-ENV:Header/>
            data = data.substring(data.indexOf("<"));

            if (getEj() == 0) {
                setEj(TxHelper.generateEj());
            }

            if (getLogContext() == null) {
                LogData tempVar = new LogData();
                tempVar.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                tempVar.setSubSys(SubSystem.NB);
                tempVar.setChannel(channel);
                tempVar.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                tempVar.setMessageFlowType(MessageFlow.Request);
                tempVar.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
                tempVar.setMessage(data);
                tempVar.setEj(getEj());
                logData = tempVar;
            } else {
                logData = getLogContext();
            }

            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setSubSys(SubSystem.NB);
            logData.setChannel(channel);
            logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(data);
            logData.setEj(getEj());
            logMessage(Level.DEBUG, logData);

            nbData = new NBData();

            // 因為電文進來時有可能是NB或是VA，沒先判斷過的話，無法直接使用deserializeFromXml轉入格式物件裡
            // 所以這邊先用JSONObject抓欄位來判斷，確認過是哪種電文之後才用deserializeFromXml轉入格式物件
            JSONObject jsonobj = XML.toJSONObject(data);
            JSONObject json1 = new JSONObject(jsonobj.get("SOAP-ENV:Envelope").toString());
            JSONObject json2 = new JSONObject(json1.get("SOAP-ENV:Body").toString());
            JSONObject json3 = new JSONObject(json2.get("esb:MsgRq").toString());
            JSONObject json = new JSONObject(json3.get("SvcRq").toString());

            String fscode = json.get("FSCODE").toString();
            String timeStart = "";
            if ("LE".equals(fscode) || "LF".equals(fscode)) {
                reqva = deserializeFromXml(data, RCV_VA_GeneralTrans_RQ.class);
                //				BeanUtils.copyProperties(reqva, req);
                vaatmReqheader = reqva.getBody().getRq().getHeader();
                RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = reqva.getBody().getRq().getSvcRq();
                // 3.	檢核若前端上送時間與FEP時間已超過90秒,該筆交易不處理(if need )*/

                /*ID+ACCT 全繳不用檢核*/
                if (StringUtils.equals(vaatmReqheader.getCHANNEL().substring(0, 1), "M")) {
                    if (StringUtils.isNotBlank(atmReqbody.getINDATE())
                            && StringUtils.isNotBlank(atmReqbody.getINTIME())) {
                        timeStart = atmReqbody.getINDATE() + atmReqbody.getINTIME();

                        // 不知為何要轉民國年
                        //						long srt = FormatUtil.parseDataTime(CalendarUtil.rocStringToADString14(timeStart), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                        long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                        long end = FormatUtil.parseDataTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                        int diffseconds = (int) ((end - srt) / 1000);
                        /*回覆前端交易失敗0601, 合庫三碼代號: 126 */
                        // 前端上送時間與FEP時間已超過90秒,該筆交易不處理
                        // if (diffseconds > 90 ) {
                        //     SEND_VA_GeneralTrans_RS rs = new SEND_VA_GeneralTrans_RS();
                        //     SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body rsbody = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body();
                        //     SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs();
                        //     SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
                        //     SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
                        //     msgrs.setHeader(header);
                        //     msgrs.setSvcRs(body);
                        //     rsbody.setRs(msgrs);
                        //     rs.setBody(rsbody);
                        //
                        //     header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                        //     header.setCHANNEL(atmReqheader.getCHANNEL());
                        //     header.setMSGID(atmReqheader.getMSGID());
                        //     header.setCLIENTDT(atmReqheader.getCLIENTDT());
                        //     header.setSYSTEMID("FEP");
                        //     header.setSTATUSCODE("0601");
                        //     header.setSEVERITY("ERROR");
                        //     header.setSTATUSDESC("前端上送時間與FEP時間已超過90秒,該筆交易不處理");
                        //
                        //     res = XmlUtil.toXML(rs);
                        //
                        // } else {
                        if (!data.contains("<CHANNEL>") || !data.contains("<MSGID>") || !data.contains("<MSGID>") || !data.contains("<MSGID>")) {
                            SEND_VA_GeneralTrans_RS rs = new SEND_VA_GeneralTrans_RS();
                            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body rsbody = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body();
                            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs();
                            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
                            SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
                            msgrs.setHeader(header);
                            msgrs.setSvcRs(body);
                            rsbody.setRs(msgrs);
                            rs.setBody(rsbody);

                            header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                            header.setCHANNEL(atmReqheader.getCHANNEL());
                            header.setMSGID(atmReqheader.getMSGID());
                            header.setCLIENTDT(atmReqheader.getCLIENTDT());
                            header.setSYSTEMID("FEP");
                            header.setSTATUSCODE("2999");
                            header.setSEVERITY("ERROR");
                            header.setSTATUSDESC("前端上送格式錯誤");

                            res = XmlUtil.toXML(rs);
                        } else {
                            nbData.setEj(getEj());
                            String txntype = "";
                            // Parse電文
                            nbData.setTxVafepObject(parseFlatfileva(data));
                            txntype = nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getTXNTYPE().trim();

                            if ("IQ".equals(txntype) || "RV".equals(txntype)) {
                                // RUN  NBINQ
                                nbData.setMessageID("NBINQ");
                                if ("LE".equals(fscode) || "LF".equals(fscode)) {
                                    // RUN  VAINQ
                                    nbData.setMessageID("VAINQ");
                                }
                            } else {
                                if ("RQ".equals(txntype)) {
                                    /*全國性繳費_線上約定(授權)*/
                                    if ("LE".equals(fscode)) {
                                        if ("03".equals(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getAEIPYTP())) {
                                            // 屬於VA 交易 VA 交易也是從網銀進來 但因為電文格式不同 所以是這樣判斷
                                            Npsunit npsunits = dbNPSUNIT.selectByPrimaryKey(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE(), nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());

                                            if (npsunits != null) {
                                                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(npsunits.getNpsunitBkno().substring(0, 3))) {
                                                    // 自行
                                                    nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                                } else {
                                                    nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                                }
                                            }
                                        } else {
                                            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getAEIPCRBK())) {
                                                // 自行
                                                nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                            } else {
                                                nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                            }
                                        }
                                        /*跨行金融帳戶資訊核驗服務   無自行*/
                                    } else if ("LF".equals(fscode)) {
                                        nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getAEIPCRBK())) {
                                            nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                        } else {
                                            nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                        }
                                    } else {

                                    }

                                }
                            }

                            nbData.setMsgCtl(FEPCache.getMsgctrl(nbData.getMessageID()));
                            if (nbData.getMsgCtl() == null) {
                                this.logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
                                sendEMS(this.logContext);
                                return StringUtils.EMPTY;
                            }

                            this.setChannel(nbData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                            logData.setMessageId(nbData.getMessageID());
                            logData.setChannel(channel);
                            logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                            logData.setMessageFlowType(MessageFlow.Request);
                            logData.setMessage(data);
                            nbData.setAaName(nbData.getMsgCtl().getMsgctlAaName());
                            nbData.setTxStatus(nbData.getMsgCtl().getMsgctlStatus() == 1);
                            nbData.setTxRequestMessage(data);
                            nbData.setTxChannel(channel);
                            nbData.setMessageFlowType(MessageFlow.Request);
                            nbData.setLogContext(logData);
                            nbData.setEj(getEj());
                            ATMGeneral atmGeneral = new ATMGeneral();
                            nbData.setAtmtxObject(atmGeneral);
                            nbData.getAtmtxObject().getRequest().setPICCBI55(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getICMARK());
                            //nbData.getAtmtxObject().getRequest().setPICCTACL(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getIC_TAC_LEN());
                            nbData.getAtmtxObject().getRequest().setPICCTAC(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getIC_TAC());

                            LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
                            // 根據MessageID決定要New那支AA
                            res = this.runVA(nbData.getMessageID());
                        }
                        // }
                    }
                }
            } else {
                req = deserializeFromXml(data, RCV_NB_GeneralTrans_RQ.class);
                //				BeanUtils.copyProperties(req, reqva);

                atmReqheader = req.getBody().getRq().getHeader();
                RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = req.getBody().getRq().getSvcRq();
                // 3.	檢核若前端上送時間與FEP時間已超過90秒,該筆交易不處理(if need )*/
                /*ID+ACCT 全繳不用檢核*/
                if (StringUtils.equals(atmReqheader.getCHANNEL().substring(0, 1), "M")) {
                    if (StringUtils.isNotBlank(atmReqbody.getINDATE())
                            && StringUtils.isNotBlank(atmReqbody.getINTIME())) {
                        timeStart = atmReqbody.getINDATE() + atmReqbody.getINTIME();

                        long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                        long end = FormatUtil.parseDataTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                        int diffseconds = (int) ((end - srt) / 1000);
                        /*回覆前端交易失敗0601, 合庫三碼代號: 126 */
                        // 前端上送時間與FEP時間已超過90秒,該筆交易不處理
                        // if (diffseconds > 90) {
                        //     SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
                        //     SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
                        //     SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
                        //     SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
                        //     SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
                        //     msgrs.setHeader(header);
                        //     msgrs.setSvcRs(body);
                        //     rsbody.setRs(msgrs);
                        //     rs.setBody(rsbody);
                        //
                        //     header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                        //     header.setCHANNEL(atmReqheader.getCHANNEL());
                        //     header.setMSGID(atmReqheader.getMSGID());
                        //     header.setCLIENTDT(atmReqheader.getCLIENTDT());
                        //     header.setSYSTEMID("FEP");
                        //     header.setSTATUSCODE("0601");
                        //     header.setSEVERITY("ERROR");
                        //     header.setSTATUSDESC("前端上送時間與FEP時間已超過90秒,該筆交易不處理");
                        //
                        //     res = XmlUtil.toXML(rs);
                        //
                        // } else {
                            if (!data.contains("<CHANNEL>") || !data.contains("<MSGID>") || !data.contains("<MSGID>") || !data.contains("<MSGID>")) {
                                SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
                                SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
                                SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
                                SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
                                SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
                                msgrs.setHeader(header);
                                msgrs.setSvcRs(body);
                                rsbody.setRs(msgrs);
                                rs.setBody(rsbody);

                                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                                header.setCHANNEL(atmReqheader.getCHANNEL());
                                header.setMSGID(atmReqheader.getMSGID());
                                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                                header.setSYSTEMID("FEP");
                                header.setSTATUSCODE("2999");
                                header.setSEVERITY("ERROR");
                                header.setSTATUSDESC("前端上送格式錯誤");

                                res = XmlUtil.toXML(rs);
                            } else {
                                nbData.setEj(getEj());
                                String txntype = "";
                                // Parse電文
                                nbData.setTxNbfepObject(parseFlatfile(data));
                                txntype = nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getTXNTYPE().trim();

                                if ("IQ".equals(txntype) || "RV".equals(txntype)) {
                                    // RUN  NBINQ
                                    nbData.setMessageID("NBINQ");
                                    if ("LE".equals(fscode) || "LF".equals(fscode)) {
                                        // RUN  VAINQ
                                        nbData.setMessageID("VAINQ");
                                    }
                                } else {
                                    if ("RQ".equals(txntype)) {
                                        /*全國性繳費_線上約定(授權)*/
                                        if ("LE".equals(fscode)) {
                                            if ("03".equals(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getAEIPYTP())) {
                                                // 屬於VA 交易 VA 交易也是從網銀進來 但因為電文格式不同 所以是這樣判斷
                                                Npsunit npsunits = dbNPSUNIT.selectByPrimaryKey(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE(), nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());

                                                if (npsunits != null) {
                                                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(npsunits.getNpsunitBkno().substring(0, 3))) {
                                                        // 自行
                                                        nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                                    } else {
                                                        nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                                    }
                                                }
                                            } else {
                                                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getAEIPCRBK())) {
                                                    // 自行
                                                    nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                                } else {
                                                    nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                                }
                                            }
                                            /*跨行金融帳戶資訊核驗服務   無自行*/
                                        } else if ("LF".equals(fscode)) {
                                            nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getAEIPCRBK())) {
                                                nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                            } else {
                                                nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                            }
                                        } else {
                                            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getTRNSFROUTBANK().substring(0, 3))
                                                    && SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getTRNSFRINBANK().substring(0, 3))) {
                                                nbData.setMessageID(nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                            } else {
                                                nbData.setMessageID(nbData.getTxNbfepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                            }
                                        }

                                    }
                                }

                                nbData.setMsgCtl(FEPCache.getMsgctrl(nbData.getMessageID()));
                                if (nbData.getMsgCtl() == null) {
                                    this.logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
                                    sendEMS(this.logContext);
                                    return StringUtils.EMPTY;
                                }

                                this.setChannel(nbData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                                logData.setMessageId(nbData.getMessageID());
                                logData.setChannel(channel);
                                logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                                logData.setMessageFlowType(MessageFlow.Request);
                                logData.setMessage(data);
                                nbData.setAaName(nbData.getMsgCtl().getMsgctlAaName());
                                nbData.setTxStatus(nbData.getMsgCtl().getMsgctlStatus() == 1);
                                nbData.setTxRequestMessage(data);
                                nbData.setTxChannel(channel);
                                nbData.setMessageFlowType(MessageFlow.Request);
                                nbData.setLogContext(logData);
                                nbData.setEj(getEj());

                                LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
                                // 根據MessageID決定要New那支AA
                                res = this.runAA(nbData.getMessageID());
                            }
                        // }
                    }
                } else {
                    nbData.setEj(getEj());
                    String txntype = "";
                    // Parse電文
                    nbData.setTxNbfepObject(parseFlatfile(data));
                    txntype = nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getTXNTYPE().trim();

                    if ("IQ".equals(txntype) || "RV".equals(txntype)) {
                        // RUN  NBINQ
                        nbData.setMessageID("NBINQ");
                    } else {
                        if ("RQ".equals(txntype)) {
                            /*其他網銀交易*/
                            ATMGeneral atmGeneral = new ATMGeneral(); /*ICMARK*/
                            nbData.setAtmtxObject(atmGeneral);
                            nbData.getAtmtxObject().getRequest().setPICCBI55("202020202020202020202020202020");
                            if (nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getTRNSFRINBANK().length() >= 3) {
                                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getTRNSFROUTBANK().substring(0, 3))
                                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getTRNSFRINBANK().substring(0, 3))) {
                                    nbData.setMessageID(nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
                                } else {
                                    nbData.setMessageID(nbData.getTxNbfepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                                }
                            } else {
                                nbData.setMessageID(nbData.getTxNbfepObject().getRequest().getBody().getRq().getHeader().getMSGID());
                            }
                        }
                    }

                    nbData.setMsgCtl(FEPCache.getMsgctrl(nbData.getMessageID()));
                    if (nbData.getMsgCtl() == null) {
                        this.logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
                        sendEMS(this.logContext);
                        return StringUtils.EMPTY;
                    }

                    this.setChannel(nbData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                    logData.setMessageId(nbData.getMessageID());
                    logData.setChannel(channel);
                    logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                    logData.setMessageFlowType(MessageFlow.Request);
                    logData.setMessage(data);
                    nbData.setAaName(nbData.getMsgCtl().getMsgctlAaName());
                    nbData.setTxStatus(nbData.getMsgCtl().getMsgctlStatus() == 1);
                    nbData.setTxRequestMessage(data);
                    nbData.setTxChannel(channel);
                    nbData.setMessageFlowType(MessageFlow.Request);
                    nbData.setLogContext(logData);
                    nbData.setEj(getEj());

                    LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
                    // 根據MessageID決定要New那支AA
                    res = this.runAA(nbData.getMessageID());
                }
            }
            return res;
        } catch (Throwable ex) {
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.toString());
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.getStackTrace());
            // logData = new LogData();
            logData.setChannel(channel);
            logData.setMessage(data);
            logData.setMessageFlowType(MessageFlow.Request);
            if (nbData != null) {
                logData.setMessageId(nbData.getMessageID());
            }

            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setSubSys(SubSystem.NB);
            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setProgramException(ex);
            sendEMS(logData);
            res = getResStr(FEPReturnCode.ParseTelegramError, nbData.getMessageID());
            return res;
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(res);
            logMessage(Level.DEBUG, logData);
        }
    }

    public NBFEPGeneral parseFlatfile(String data) throws Exception {
        RCV_NB_GeneralTrans_RQ req = deserializeFromXml(data, RCV_NB_GeneralTrans_RQ.class);
        nbData.setTxNbfepObject(new NBFEPGeneral());
        nbData.getTxNbfepObject().setRequest(req);
        nbData.getTxNbfepObject().setResponse(null);

        return nbData.getTxNbfepObject();
    }

    public VAFEPGeneral parseFlatfileva(String data) throws Exception {
        RCV_VA_GeneralTrans_RQ req = deserializeFromXml(data, RCV_VA_GeneralTrans_RQ.class);
        nbData.setTxVafepObject(new VAFEPGeneral());
        nbData.getTxVafepObject().setRequest(req);
        nbData.getTxVafepObject().setResponse(null);

        return nbData.getTxVafepObject();
    }

    private String runAA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processNbRequestData", NBData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, nbData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }
    //	private String runNBAA(String msgId) throws Throwable {
    //		try {
    //			Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
    //			Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processNbRequestData", NBData.class);
    //			return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, nbData);
    //		} catch (Exception e) {
    //			throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
    //		}
    //	}

    // 組INBKAA 在建構式發生EXCEPTION處理
    private String getResStr(FEPReturnCode code, String MSGID) {
        String ResStr = "";
        @SuppressWarnings("unused")
        StringBuilder bodystring = new StringBuilder();
        //		if (S0710_ID.equals(MSGID)) {
        //			S0710Response s0710Rsp = new S0710Response();
        //			s0710Rsp.setRsHeader(new FEPRsHeader());
        //			s0710Rsp.getRsHeader().setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
        //			s0710Rsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
        //			s0710Rsp.getRsHeader().getRsStat().getRsStatCode().setValue(code.name());
        //			s0710Rsp.getRsHeader().getRsStat().setDesc(TxHelper.getMessageFromFEPReturnCode(code)); // 要查錯誤碼;
        //			s0710Rsp.getRsHeader().setRsTime(new SimpleDateFormat(_timeFormat).format(new Date()));
        //			s0710Rsp.getRsHeader().setChlEJNo(StringUtils.join(new SimpleDateFormat("yyyyMMdd").format(new Date()), StringUtils.leftPad(String.valueOf(getEj()), 12, '0')));
        //			ResStr = serializeToXml(s0710Rsp).replace("&lt;", "<").replace("&gt;", ">");
        //		} else {
        // 用S0710_Response回
        S0710Response errorRsp = new S0710Response();
        errorRsp.setRsHeader(new FEPRsHeader());
        errorRsp.getRsHeader().setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
        errorRsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
        errorRsp.getRsHeader().getRsStat().getRsStatCode().setValue(TxHelper.getRCFromErrorCode(String.valueOf(code.getValue()), FEPChannel.FEP, FEPChannel.BRANCH, getLogContext()));
        errorRsp.getRsHeader().getRsStat().setDesc(TxHelper.getMessageFromFEPReturnCode(code)); // 要查錯誤碼;
        errorRsp.getRsHeader().setChlEJNo(StringUtils.join(new SimpleDateFormat("yyyyMMdd").format(new Date()), StringUtils.leftPad(String.valueOf(getEj()), 12, '0')));
        errorRsp.getRsHeader().setRsTime(new SimpleDateFormat(_timeFormat).format(new Date()));
        ResStr = serializeToXml(errorRsp).replace("&lt;", "<").replace("&gt;", ">");
        //		}

        return ResStr;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    private String runVA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processVARequestData", NBData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, nbData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }
}
