package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.MBQueueConsumers;
import com.syscom.fep.jms.queue.NONVIPQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.NONVIPHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.List;

public class NONVIPServerReceiver extends FEPBase implements JmsReceiver<String> {

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(NONVIPQueueConsumers.class).subscribe(this));
    }
    @Override
    public void messageReceived(String destination, String payload, Message message) {
        String messageIn = payload;
        try {
            String returnStr = MultiProcess(messageIn);
            String MsgidFromXml = findCLIENTTRACEID(messageIn);
            JmsFactory.setCorrelationID(message, MsgidFromXml);
            String correlationID = JmsFactory.getCorrelationID(message);
            String Msgid = JmsFactory.getMessageId(message);
            LogData logData = new LogData();
            logData.setProgramFlowType(ProgramFlow.RESTFulIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setMessage("Message: " + message);
            logData.setMessage("correlationID: " + correlationID + " ,Msgid: " + Msgid);
            logData.setRemark("NONVIPService Receive Request");
            logData.setServiceUrl("/nonvip/recv");
            logData.setEj(TxHelper.generateEj());
            this.logMessage(logData);
            if (StringUtils.isNotBlank(returnStr)) {
                FeeBackSuccess(returnStr, message);
                logData.setProgramFlowType(ProgramFlow.RESTFulOut);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
                logData.setMessage("Message: " + message);
                logData.setMessage("correlationID: " + correlationID + " ,Msgid: " + Msgid + " ,Msgid for XML: " + MsgidFromXml);
                logData.setRemark("FeeBackSuccess");
                logData.setServiceUrl("/nonvip/recv");
                logData.setEj(TxHelper.generateEj());
                this.logMessage(logData);
            }
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return SvrConst.SVR_NONVIP;
    }

    @Override
    public void messageBatchReceived(String destination, List<String> payloadList, List<Message> messageList) {
        JmsReceiver.super.messageBatchReceived(destination, payloadList, messageList);
    }

    private String MultiProcess(String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_NONVIP);
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setProgramFlowType(ProgramFlow.RESTFulIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
        logData.setMessage(messageIn);
        logData.setRemark("NONVIPService Receive Request");
        logData.setServiceUrl("/nonvip/recv");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            NONVIPHandler nonvipHandler = new NONVIPHandler();
            nonvipHandler.setEj(logData.getEj());
            nonvipHandler.setLogContext(logData);
            FEPChannel channel = FEPChannel.parse(findCHANNEL(messageIn));
            if (channel.equals(FEPChannel.NBP) || channel.equals(FEPChannel.NBQ))
                channel = FEPChannel.NETBANK;
            messageOut = nonvipHandler.dispatch(channel, messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark("NONVIPService Get Response from NONVIPHandler");
            this.logMessage(logData);
            return messageOut;
        } catch (Exception e) {
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/nonvip/recv");
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

    private boolean FeeBackSuccess(String returnStr, Message messageIn) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getNonvipAck().getDestination(), returnStr, null, new JmsHandler() {
                @Override
                public void setPropertyOut(Message messageOut) throws JMSException {
                    try {
                        JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
                        //                        JmsFactory.setMessageId(messages, JmsFactory.getMessageId(message).getBytes());
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

    private String findCHANNEL(String str) {
        // 找到開始標籤和結束標籤的索引
        int startIndex = str.indexOf("<CHANNEL>");
        int endIndex = str.indexOf("</CHANNEL>");
        // 提取 CHANNEL 的值
        String clientTraceId = str.substring(startIndex + "<CHANNEL>".length(), endIndex);
        return clientTraceId;
    }
}
