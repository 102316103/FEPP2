package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISCHeader;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class CBSHandler extends HandlerBase {
    private static final String ProgramName = StringUtils.join(CBSHandler.class.getSimpleName(), "");
    private FISCGeneral general;
    private final FISCSubSystem fiscSubSystem = FISCSubSystem.INBK;
    private final SubSystem subSystem = SubSystem.INBK;

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        return null;
    }

    //1.Handler進入點
    @Override
    public String dispatch(FEPChannel channel, String data) {
        String ctfRes = StringUtils.EMPTY;
        String programName = StringUtils.join(ProgramName, ".dispatch");
        FISCData fiscData = new FISCData();
        general = new FISCGeneral();
        general.setSubSystem(fiscSubSystem);

        this.setEj(TxHelper.generateEj());

        //記錄FEPLOG內容
        LogData logData = new LogData();
        logData.setChannel(channel);
        logData.setSubSys(subSystem);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(programName);
        logData.setEj(this.getEj());
        logData.setTxRquid(this.txRquid);
        logData.setMessage(data);
        logData.setRemark("Enter dispatch");
        logMessage(Level.DEBUG, logData);

        try {
            //2. 拆解電文, 取得MSGID
            RefBase<FISCHeader> refFiscHeader = new RefBase<FISCHeader>(null);
            FEPReturnCode rtn = this.getFISCRequestHeader(data, refFiscHeader);// 拆財金電文Header 判斷狀態
            FISCHeader fiscHeader = refFiscHeader.get();
            String msgId = fiscHeader.getProcessingCode() + fiscHeader.getMessageType().substring(2) + "IMS";
			String MsgCtlId = "IMSRequest";
			if(fiscHeader.getMessageKind() == MessageFlow.Confirmation) {
				MsgCtlId = "IMSConfirm";
			}
			
			fiscData.setMessageID(msgId);
			fiscData.setMsgCtl(FEPCache.getMsgctrl(MsgCtlId));
            this.setChannel(fiscData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            fiscData.setEj(this.getEj());
            fiscData.setTxChannel(channel);
            fiscData.setTxSubSystem(subSystem);
            fiscData.setFiscTeleType(fiscSubSystem);
            fiscData.setTxRquid(this.txRquid);
            fiscData.setMessageFlowType(fiscHeader.getMessageKind());
            fiscData.setStan(fiscHeader.getSystemTraceAuditNo());
            fiscData.setTxObject(this.general);
            fiscData.setLogContext(logData);
            fiscData.setTxRequestMessage(data);

            //3.CALL AA
            //FISCdata 為主機下送財金電文

            if (rtn == FEPReturnCode.Normal) {
                if (fiscData.getMsgCtl() != null) {
                    fiscData.setAaName(fiscData.getMsgCtl().getMsgctlAaName());
                    fiscData.setTxStatus(DbHelper.toBoolean(fiscData.getMsgCtl().getMsgctlStatus()));

                    ctfRes = this.runAA(fiscData);
                } else {
                    logData.setReturnCode(CommonReturnCode.Abnormal);
                    logData.setExternalCode("E551");
                    logData.setMessage("MessageID:" + msgId);
                    logData.setRemark("於MSGCTL 找不到資料");
                    logMessage(logData);
                    return ctfRes;
                }
            }

            //4.回傳AA Response電文( if need)
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(programName);
            logData.setMessage(ctfRes);
            logData.setRemark("Exit dispatch");
            logMessage(logData);
            return ctfRes;
        } catch (Throwable t) {
            logData.setProgramException(t);
            sendEMS(logData);
        }
        return ctfRes;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }

    private FEPReturnCode getFISCRequestHeader(String data, RefBase<FISCHeader> fiscHeader) {
        FISC_INBK fiscInbk = new FISC_INBK(data);
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        rtnCode = fiscInbk.parseFISCMsg();
        if (rtnCode == CommonReturnCode.Normal) {
            if (fiscInbk.getMessageKind() == MessageFlow.Request) {
                general.setINBKRequest(fiscInbk);
                general.setINBKResponse(new FISC_INBK());
                general.setINBKConfirm(new FISC_INBK());
                fiscHeader.set(general.getINBKRequest());
            }
            if (fiscInbk.getMessageKind() == MessageFlow.Confirmation) {
                general.setINBKRequest(new FISC_INBK());
                general.setINBKResponse(new FISC_INBK());
                general.setINBKConfirm(fiscInbk);
                fiscHeader.set(general.getINBKConfirm());
            }
        }
        return rtnCode;
    }

    private String runAA(FISCData fiscData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRequestIMSData", FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, fiscData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }

    }
}
