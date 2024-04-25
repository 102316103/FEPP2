package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.parse.StringToFieldAnnotationParser;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.request.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 處理來自ATMGW的電文Handler類
 *
 * @author Bruce
 */
public class ATMHandlerP2 extends HandlerBase {
    private Boolean isFisc = false;
    private String atmText;            //ATM原始電文(ASCII)
    private String atmNo;            //ATM機台代號
    private String fsCode;            //ATM交易代號
    private String atmSeq;            //ATM交易流水號
    private String msgCategory;        //Message category
    private String msgType;            //Message type

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

    /**
     * 2022/11/04 Bruce Modify
     * SPEC:TCB-FEP-SPC_Handler_ATMHandler(P1階段ATM交易控制程式)
     * 處理流程
     * 1.記錄LOG
     * 2.將ATM電文從EBCDIC轉成ASCII,並取出相關欄位
     * 3.讀取交易控制檔MsgCtl
     * 4.呼叫AA
     * 5.回傳Response電文
     */
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        //1.宣告變數
        //FEPReturnCode rtnCode = FEPReturnCode.ProgramException;	//SPEC有，但無使用暫先mark
        ATMData atmData = new ATMData();
        String methodName = StringUtils.join(ProgramName, ".dispatch");
        String atmRes = StringUtils.EMPTY;
        //2.取EJ
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
        }
        if (StringUtils.isBlank(this.txRquid)) {
            this.txRquid = UUIDUtil.randomUUID(true);
        }
        //3.記錄FEPLOG內容
        LogData logData = new LogData();
        logData.setEj(this.getEj());
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(methodName);
        logData.setMessage(data);
        logData.setRemark("Enter dispatch");
        logData.setTxRquid(this.txRquid);
        logData.setAtmNo(atmNo);

        try {
            //4.將Hex電文轉成ASCII, 並取出特定欄位供後續流程使用
//			this.setAtmText(StringUtil.fromHex(data));
//			this.setAtmText(data);
            this.setAtmText(CodeGenUtil.ebcdicToAsciiDefaultEmpty(data));

//			this.setAtmText(EbcdicConverter.fromHex(CCSID.English, data));
//			this.setAtmText(EbcdicConverter.toHex(CCSID.English, 12, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMM_PLAIN)));
            if (this.getAtmText().length() < 100 && "CK".equals(getAtmText().substring(32, 34))) {
                this.setAtmSeq(this.getAtmText().substring(26, 30));
                this.setFsCode(this.getAtmText().substring(32, 34));
                this.setMsgCategory(this.getAtmText().substring(11, 12));
                this.setMsgType(this.getAtmText().substring(12, 14));
                atmData.setMessageID("ChangeKeyForATM");
                logData.setAtmSeq(this.getAtmSeq());
            } else if("EBTSPM0".equals(getAtmText().substring(1, 8))) {
            	atmData.setMessageID("ATMTxForP1");
            } else {
                //一般交易電文
                this.setAtmSeq(this.getAtmText().substring(32, 36));
                this.setFsCode(this.getAtmText().substring(73, 75));
                this.setMsgCategory(this.getAtmText().substring(17, 18));
                this.setMsgType(this.getAtmText().substring(18, 20));
                atmData.setMessageID(this.getMsgId(data));
                logData.setMessageId(atmData.getMessageID());
                logData.setAtmSeq(this.getAtmSeq());
            }
            
          	//5.將相關欄位存入ATMData中
        	atmData.setTxChannel(channel);
            atmData.setTxSubSystem(SubSystem.ATMP);
            atmData.setMessageFlowType(MessageFlow.Request);
            atmData.setAtmSeq(this.getAtmSeq());
//			atmData.setTxRequestMessage(this.getAtmText());
            atmData.setEj(this.getEj());
            atmData.setMsgCategory(this.getMsgCategory());
            atmData.setMsgType(this.getMsgType());
            atmData.setFscode(this.getFsCode());
            atmData.setAtmNo(atmNo);            //改由atmservice controller帶進來
            atmData.setLogContext(logData);
            atmData.setTxObject(this.atmGeneral);    //--ben--20220930Daniel指示:給小傑可順利測試用
            
            //6.讀取MSGCTL,並存入atmData
            Msgctl msgctl = FEPCache.getMsgctrl(atmData.getMessageID());
            atmData.setMsgCtl(msgctl);
            //讀不到MsgCtl先記EMS不回ATM
            if (atmData.getMsgCtl() == null) {
                logData.setReturnCode(CommonReturnCode.Abnormal);
                logData.setExternalCode("E551");
                logData.setRemark("於MSGCTL 找不到資料");
                sendEMS(logData);
                return atmRes;

            }

            this.setChannel(atmData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            String aaName = atmData.getMsgCtl().getMsgctlAaName();
            String msgctlCbsProc = atmData.getMsgCtl().getMsgctlCbsProc();
            if (StringUtils.isNotBlank(msgctlCbsProc) && "Y".equals(msgctlCbsProc)) {
                isFisc = false;
                aaName = "ProcessATMForP1";
            }

            //7.呼叫 AA
            atmData.setAaName(aaName);
            atmData.setTxStatus(DbHelper.toBoolean(atmData.getMsgCtl().getMsgctlStatus()));
            atmData.setTxRequestMessage(data);
            if (atmData.isTxStatus()) {
                atmRes = this.runAA(atmData);
            } else {
                return atmRes;        //不允許執行交易
            }
            
            //8.回傳AA Response電文
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(methodName);
            logData.setMessage(atmRes);
            logData.setRemark("Exit dispatch");
            logMessage(logData);
            return atmRes;
        } catch (Throwable e) {
            logData.setProgramException(e);
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

    /**
     * 2022/11/07 Bruce Add 取得MSGID新邏輯
     *
     * @param atmText 電文字串
     * @return
     * @throws Exception
     */
    private String getMsgId(String atmText) throws Exception {
        String msgId = "";
        //為了判斷AA要跑哪一支轉成ASCII

        if (getAtmText().length() != 509 && getAtmText().substring(73, 75).equals("FV")) {
            //上送 ClassName： ATM_FVTrans 指靜脈建置
            ATM_FVTrans request = new StringToFieldAnnotationParser<ATM_FVTrans>(ATM_FVTrans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().substring(73, 75).equals("P5") || getAtmText().substring(73, 75).equals("P6")) {
            //上送 ClassName： ATM_IntlChangeSSCode 國際卡密碼變更
            ATM_IntlChangeSSCode request = new StringToFieldAnnotationParser<ATM_IntlChangeSSCode>(ATM_IntlChangeSSCode.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().length() == 508) {
            //上送 ClassName： ATM_FAA_IntlTrans 國際卡EMV
            ATM_IntlTrans request = new StringToFieldAnnotationParser<ATM_IntlTrans>(ATM_IntlTrans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().length() != 509 && getAtmText().substring(73, 75).equals("P1")) {
            //上送 ClassName： ATM_FAA_TRK2ChangeSSCode 磁條密碼變更
            ATM_TRK2ChangeSSCode request = new StringToFieldAnnotationParser<ATM_TRK2ChangeSSCode>(ATM_TRK2ChangeSSCode.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else if (getAtmText().substring(17, 20).equals("FC6")) {
            //上送 ClassName： ATM_FAA_D9Trans  FC_非晶片(查詢企業名稱FC6)
            ATM_D9Trans request = new StringToFieldAnnotationParser<ATM_D9Trans>(ATM_D9Trans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        } else {
            //  上送 ClassName： ATM_FAA_GeneralTrans  一般交易
            ATM_GeneralTrans request = new StringToFieldAnnotationParser<ATM_GeneralTrans>(ATM_GeneralTrans.class).readIn(atmText);
            request.toGeneral(atmGeneral);
        }

        if (getAtmText().substring(17, 19).equals("FC")) {
            //自行前置/結帳交易：ATM 2 way交易(C1/C2/C4/C5/C6/C7/C8/C9/CC/CZ/CS)
            msgId = getAtmText().substring(17, 20);
        } else if (getAtmText().substring(73, 75).equals("P1") && !getAtmText().substring(179, 184).equals("K 200")) {//磁條卡
            if (getAtmText().substring(18, 20).equals("AA")) {
                msgId = getAtmText().substring(73, 75) + "T";
            } else {
                msgId = getAtmText().substring(73, 75) + "TC";
            }
        } else if (getAtmText().substring(155, 158).equals(SysStatus.getPropertyValue().getSysstatHbkno()) &&
        			((getAtmText().substring(73, 74).equals("T") && getAtmText().substring(77, 80).trim().equals("")) ||
        			(getAtmText().substring(73, 74).equals("E") && (getAtmText().substring(53, 56).trim().equals("006"))))) {
            if (getAtmText().substring(18, 20).equals("AA")) {
                msgId = getAtmText().substring(73, 75);
            } else {
                msgId = getAtmText().substring(73, 75) + "C";
            }
        } else if (getAtmText().substring(155, 158).equals(SysStatus.getPropertyValue().getSysstatHbkno()) ||
        			(getAtmText().substring(73, 75).equals("D8") || getAtmText().substring(73, 75).equals("I5") ||
                        getAtmText().substring(73, 75).equals("DX") || getAtmText().substring(73, 75).equals("P5") ||
                        getAtmText().substring(73, 75).equals("P6") || getAtmText().substring(73, 75).equals("FV"))) {
        	 if (getAtmText().substring(18, 20).equals("AA")) {
                 msgId = getAtmText().substring(73, 75);
             } else {
                 msgId = getAtmText().substring(73, 75) + "C";
             }
        } else {
            // 跨行交易：ATM 4 way交易
            isFisc = true;
            if (getMsgType().equals("AA")) {
                msgId = getAtmText().substring(73, 75) + "-" + getAtmText().substring(184, 188);
            } else {
                msgId = getAtmText().substring(73, 75) + "C-" + getAtmText().substring(184, 188);
            }
        }

        return msgId;
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        return "";
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) {
        return true;
    }

}
