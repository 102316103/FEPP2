package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FSN_HEAD2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Calendar;

public class EATMHandler extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private ATMData eatmData;
    private String _timeFormat = "yyyy/MM/ddTHH:mm:ss:sss";
    private LogData logData;
    private String atmText;            //ATM原始電文(ASCII)
    private String atmNo;            //ATM機台代號
    private String fsCode;            //ATM交易代號
    private String atmSeq;            //ATM交易流水號
    private String msgCategory;        //Message category
    private String msgType;            //Message type
    private Boolean isFisc = false;
    private ATMGeneral atmGeneral = new ATMGeneral();


    public String getAtmText() {
        return atmText;
    }

    public void setAtmText(String atmText) {
        this.atmText = atmText;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }

    public String getFsCode() {
        return fsCode;
    }

    public void setFsCode(String fsCode) {
        this.fsCode = fsCode;
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    public String getMsgCategory() {
        return msgCategory;
    }

    public void setMsgCategory(String msgCategory) {
        this.msgCategory = msgCategory;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public EATMHandler() {
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        //1.宣告變數
        //FEPReturnCode rtnCode = FEPReturnCode.ProgramException;	//SPEC有，但無使用暫先mark
        eatmData = new ATMData();
        String methodName = StringUtils.join(ProgramName, ".dispatch");
        String atmRes = StringUtils.EMPTY;
        //2.取EJ
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
        }
        LogData logData = new LogData();

        try {
            //前面多了奇怪的符號 ex:�<?xml version="1.0" encoding="utf-8"?><SOAP-ENV:Envelope><SOAP-ENV:Header/>
            data = data.substring(data.indexOf("<"));

            if (FEPChannel.EAT.equals(channel)) {
                // Parse電文
                eatmData.setTxObject(parseFlatfile(data));
                RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReheader = eatmData.getTxObject().getEatmrequest().getBody().getRq().getHeader();
                RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
                //4.將Hex電文轉成ASCII, 並取出特定欄位供後續流程使用
                this.setAtmText(atmReqbody.getATMDATA());
                //原則上以Terminal ID作為ATMNo, 但某些交易沒有Terminal ID欄位, 作法待討論
                String tempText = getAtmText();

                if (atmText.length() < 100 && "C3D2".equalsIgnoreCase(tempText.substring(64, 68))) { //<>”CK”：非換KEY交易
                    //換KEY電文
                    tempText = CodeGenUtil.ebcdicToAsciiDefaultEmpty(tempText);
                    this.setAtmNo(tempText.substring(0, 8));
                    this.setAtmSeq(tempText.substring(26, 30));
                    this.setFsCode(tempText.substring(32, 34));
                    this.setMsgCategory(tempText.substring(11, 12));
                    this.setMsgType(tempText.substring(12, 14));
                    eatmData.setMessageID("ChangeKeyForATM");
                } else {
                    //一般交易電文
                    if (StringUtils.isNotBlank(atmReqbody.getTRANDATE()) && StringUtils.isNotBlank(atmReqbody.getTRANTIME())) {
                        String timeStart = "20" + atmReqbody.getTRANDATE() + atmReqbody.getTRANTIME();
                        long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                        long end = FormatUtil.parseDataTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                        int diffseconds = (int) ((end - srt) / 1000);
                        if (diffseconds > 90 && !FEPChannel.EAT.equals(channel)) {
                            SEND_EATM_FSN_HEAD2 rs = new SEND_EATM_FSN_HEAD2();
                            SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body rsbody = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body();
                            SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_NS1MsgRs msgrs = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_NS1MsgRs();
                            SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_Header header = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_Header();
                            SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs body = new SEND_EATM_FSN_HEAD2.SEND_EATM_FSN_HEAD2_Body_MsgRs_SvcRs();
                            rsbody.setRs(msgrs);
                            rs.setBody(rsbody);

                            header.setCLIENTTRACEID(atmReheader.getCLIENTTRACEID());
                            header.setCHANNEL(atmReheader.getCHANNEL());
                            header.setMSGID(atmReheader.getMSGID());
                            header.setCLIENTDT(atmReheader.getCLIENTDT());
                            header.setSYSTEMID("FEP");
                            header.setSTATUSCODE("0601");
                            header.setSEVERITY("ERROR");
                            header.setSTATUSDESC("前端上送時間與FEP時間已超過90秒,該筆交易不處理");

                            atmRes = XmlUtil.toXML(rs);

                        } else {
                            tempText = CodeGenUtil.ebcdicToAsciiDefaultEmpty(tempText);
                            //ex:T9998S02
                            atmNo = tempText.substring(63, 71);
                            this.setAtmSeq(tempText.substring(32, 36));
                            this.setFsCode(tempText.substring(73, 75));
                            this.setMsgCategory(tempText.substring(17, 18));
                            this.setMsgType(tempText.substring(18, 20));
                            eatmData.setMessageID(this.getMsgId(tempText));
                        }
                    }
                }

                //3.記錄FEPLOG內容
                logData.setAtmNo(atmNo);
                logData.setMessageId(eatmData.getMessageID());
                logData.setEj(this.getEj());
                logData.setChannel(channel);
                logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(methodName);
                logData.setMessage(data);
                logData.setRemark("Enter dispatch");

                //5.將相關欄位存入ATMData中
                eatmData.setTxChannel(channel);
                eatmData.setTxSubSystem(SubSystem.ATMP);
                eatmData.setMessageFlowType(MessageFlow.Request);
                eatmData.setAtmSeq(this.getAtmSeq());
                eatmData.setTxRequestMessage(this.getAtmText());
                eatmData.setEj(this.getEj());
                eatmData.setMsgCategory(this.getMsgCategory());
                eatmData.setMsgType(this.getMsgType());
                eatmData.setFscode(this.getFsCode());
                eatmData.setAtmNo(atmNo);            //改由atmservice controller帶進來
                eatmData.setLogContext(logData);
                //6.讀取MSGCTL,並存入atmData
                Msgctl msgctl = null;
            	if(StringUtils.isNotBlank(eatmData.getMessageID())) {
            		msgctl = FEPCache.getMsgctrl(eatmData.getMessageID());
            	}
                		
                eatmData.setMsgCtl(msgctl);
                //讀不到MsgCtl先記EMS不回ATM
                if (eatmData.getMsgCtl() == null) {
                    logData.setReturnCode(CommonReturnCode.Abnormal);
                    logData.setExternalCode("E551");
                    logData.setRemark("於MSGCTL 找不到資料");
                    sendEMS(logData);
                    return atmRes;
                }
                //7.呼叫 AA
                this.setChannel(eatmData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                eatmData.setAaName(msgctl.getMsgctlAaName());
                eatmData.setTxStatus(eatmData.getMsgCtl().getMsgctlStatus() == 1);
                if (eatmData.isTxStatus()) {
                    if (eatmData.getMessageID().equals("ChangeKeyForATM")) {
                        atmRes = this.runAAK(eatmData);
                    } else {
                        atmRes = this.runAA(eatmData);
                    }
                } else {
                    return atmRes;
                }
                //8.回傳AA Response電文
                logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setProgramName(methodName);
                logData.setMessage(atmRes);
                logData.setRemark("Exit dispatch");
                logMessage(logData);
                return atmRes;

            }
        } catch (Throwable t) {
            logData.setProgramException(t);
            sendEMS(logData);
        }
        return atmRes;
    }

    private String runAA(ATMData atmData) throws Throwable {
        try {
            if (!isFisc) {
                Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
                Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRequestData", ATMData.class);
                String atmRes = (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, atmData);
                return atmRes;
            } else {
                Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
                Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processATMInbkRequestData", ATMData.class);
                String atmRes = (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, atmData);
                return atmRes;
            }
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private String runAAK(ATMData atmData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processAtmpRequestData", ATMData.class);
            String atmRes = (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, atmData);
            return atmRes;
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    /**
     * 3. 	取得ATM MSGID
     */
    private String getMsgId(String atmText) throws Exception {

        String msgId = "";

//        if (atmText.length() == 509) {
            //餘額查詢、轉帳、繳稅、繳費、晶片卡密碼變更、舊卡啟用新卡、前置交易
            // 自行前置：ATM 2 way交易(C2/CC)
            if (atmText.substring(17, 19).equals("FC")) {
                msgId = atmText.substring(17, 20);
            } else if (atmText.substring(155, 158).equals(FEPCache.getSysstat().getSysstatHbkno())) { //自行交易
                if (atmText.substring(18, 20).equals("AA")) { //Request電文
                    msgId = atmText.substring(73, 75);
                } else { //Confirm電文
                    msgId = atmText.substring(73, 75) + "C";
                }
            } else {
                // 跨行交易ATM 4 way交易
                isFisc = true;
                if ("AA".equals(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getMSGTYP())) { //Request電文
                    msgId = atmText.substring(73, 75) + "-" + atmText.substring(184, 188);
                } else { //Confirm電文
                    msgId = atmText.substring(73, 75) + "C-" + atmText.substring(184, 188);
                }
            }
//        } else {
//            //電文長度錯誤記EMS不回ATM
//            logData.setReturnCode(CommonReturnCode.Abnormal);
//            logData.setExternalCode("FE0003");
//            logData.setRemark("拆解電文錯誤(電文長度非509)");
//            sendEMS(logData);
//            return "";
//        }
//		String msgId = "";
//		String piccpcod=eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getPICCPCOD();
//		/*MQ HEADER 長度134*/
//		if(atmText.length() == 509) {
////			this.atmGeneral = new ATM_GeneralTrans().parseFlatfile(atmText);
//			//晶片、繳稅、存款、密碼交易、申請類、FC_晶片、指靜脈FV(只有自行交易)
//
//			if("AA".equals(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getMSGTYP()) || "C".equals(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getMSGTYP())) {
//				// ATM第一道Request電文 自行
////					IF  ATM.REQ[73,2]  = "FV"   OR
////			                (ATM.REQ[155,3] = SYSSTAT_HBKNO AND
////			ATM.REQ[184,4] IN (‘2500’,2523’, ‘2563 ‘, ‘ ‘  ) )  THEN  //自行交易
////			                           MSGID = ATM.REQ[73,2]
//				if("FV".equals(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getFSCODE()) ||
//						(FEPCache.getSysstat().getSysstatHbkno().equals(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getCARDDATA().substring(13,16))
//								&& ("2500".equals(piccpcod)||"2523".equals(piccpcod)||"2563".equals(piccpcod)
//										||StringUtils.isBlank(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getFSCODE())))) {
//					msgId = eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getFSCODE();
//				}else {//跨行
//					//他行卡跨行交易，ex:W1-2510
//					msgId = eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getFSCODE() + "-" + eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getPICCPCOD();
//				}
//			}else {// ATM第二道Request電文
//				if("FV".equals(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getFSCODE()) ||
//						(FEPCache.getSysstat().getSysstatHbkno().equals(eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getCARDDATA().substring(13,16))
//								&& ("2500".equals(piccpcod)||"2523".equals(piccpcod)||"2563".equals(piccpcod)
//										||StringUtils.isBlank(atmText.substring(73,75))))) {
//					msgId = eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getFSCODE() + "C";//ex:FVC
//				}else {//跨行
//					//他行卡跨行交易，ex:W1C-2510
//					msgId = eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getFSCODE() + "C-" + eatmData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq().getPICCPCOD();
//				}
//			}
//		}
        return msgId;
    }

    public ATMGeneral parseFlatfile(String data) throws Exception {
        RCV_EATM_GeneralTrans_RQ req = deserializeFromXml(data, RCV_EATM_GeneralTrans_RQ.class);
        if (!req.getBody().getRq().getHeader().getMSGID().equals("CK")) {
            req.parseMessage(req.getBody().getRq().getSvcRq().getATMDATA());
        }
        ATMGeneral gener = new ATMGeneral();
        ATMGeneralRequest atmreq = new ATMGeneralRequest();
        BeanUtils.copyProperties(req.getBody().getRq().getSvcRq(), atmreq);
        gener.setRequest(atmreq);
        eatmData.setTxObject(gener);
        eatmData.getTxObject().setEatmrequest(req);
//		eatmData.getTxObject().setResponse(null);

        return eatmData.getTxObject();
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }
}