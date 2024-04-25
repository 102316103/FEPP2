package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.HCEQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.HCEHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;

@StackTracePointCut(caller = SvrConst.SVR_HCE)
public class HCEServerReceiver extends FEPBase implements JmsReceiver<String> {

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(HCEQueueConsumers.class).subscribe(this));
    }

    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, String payload, Message message) {
        String messageIn = payload;
        try {
            String Msgid = JmsFactory.getMessageId(message);
            String MsgidFromXml = findCLIENTTRACEID(messageIn);
            JmsFactory.setCorrelationID(message, MsgidFromXml);
            String correlationID = JmsFactory.getCorrelationID(message);
            LogData logData = new LogData();
            logData.setProgramFlowType(ProgramFlow.RESTFulIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setMessage("Message: " + message);
            logData.setMessage("correlationID: " + correlationID + " ,Msgid: " + Msgid + " ,Msgid for XML: " + MsgidFromXml);
            logData.setRemark("HCEService Receive Request");
            logData.setServiceUrl("/hce/recv");
            logData.setEj(TxHelper.generateEj());
            this.logMessage(logData);
            String returnStr = MultiProcess(messageIn);
            if (StringUtils.isNotBlank(returnStr)) {
                FeeBackSuccess(returnStr, message);
                correlationID = JmsFactory.getCorrelationID(message);
                logData.setProgramFlowType(ProgramFlow.RESTFulIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
                logData.setMessage("Message: " + message);
                logData.setMessage("correlationID: " + correlationID + " ,Msgid: " + Msgid + " ,Msgid for XML: " + MsgidFromXml);
                logData.setRemark("FeeBackSuccess");
                logData.setServiceUrl("/hce/recv");
                logData.setEj(TxHelper.generateEj());
                this.logMessage(logData);
            }


            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return SvrConst.SVR_HCE;
    }

    private String MultiProcess(String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_HCE);
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setProgramFlowType(ProgramFlow.RESTFulIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
        logData.setMessage(messageIn);
        logData.setRemark("HCEService Receive Request");
        logData.setServiceUrl("/hce/recv");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            HCEHandler hceHandler = new HCEHandler();
            hceHandler.setEj(logData.getEj());
            hceHandler.setLogContext(logData);
            messageOut = hceHandler.dispatch(FEPChannel.HCE, messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark("HCEService Get Response from HCEHandler");
            this.logMessage(logData);
            return messageOut;
        } catch (Exception e) {
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/hce/recv");
            this.logMessage(logData);
            // sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw ExceptionUtil.createRuntimeException(e);
        } finally {
            if (StringUtils.isNotBlank(messageOut))
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send msg:", messageOut);
        }
    }

    private boolean FeeBackSuccess(String returnStr, Message message) {
        try {
            // String messageIn = payload;
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getHceAck().getDestination(), returnStr, null, new JmsHandler() {
                @Override
                public void setPropertyOut(Message messageOut) throws JMSException {
                    try {
                        // String s = findCLIENTTRACEID(returnStr);
                        // String s = findCLIENTTRACEID(messageIn2);
                        // byte[] b = s.getBytes();
                        JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(message));
                        // JmsFactory.setCorrelationID(messageOut, findCLIENTTRACEID(messageIn).getBytes());
                        // JmsFactory.setMessageId(messages, JmsFactory.getMessageId(message).getBytes());
                    } catch (Exception e) {
                        return;
                    }
                }
            });

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String findCLIENTTRACEID(String str) {
        // 找到開始標籤和結束標籤的索引
        int startIndex = str.indexOf("<CLIENTTRACEID>");
        int endIndex = str.indexOf("</CLIENTTRACEID>");
        // 提取 CLIENTTRACEID 的值
        String clientTraceId = str.substring(startIndex + "<CLIENTTRACEID>".length(), endIndex);
        return clientTraceId;
    }
}
