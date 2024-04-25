package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.MCHQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.MCHHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.jms.Message;

@StackTracePointCut(caller = SvrConst.SVR_MCH)
public class MCHServerReceiver extends FEPBase implements JmsReceiver<String> {
    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(MCHQueueConsumers.class).subscribe(this));
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
        LogData logData = new LogData();
        String messageOut = StringUtils.EMPTY;
        try {
            String Msgid = JmsFactory.getMessageId(message);
            LogHelperFactory.getTraceLogger().trace(SvrConst.SVR_MCH, " Recv msg:", messageIn);
            String MsgidFromXml = findCLIENTTRACEID(messageIn);
            JmsFactory.setCorrelationID(message, MsgidFromXml);
            String correlationID = JmsFactory.getCorrelationID(message);

            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setMessage("Message: " + message);
            logData.setMessage("correlationID: " + correlationID + " ,Msgid: " + Msgid + " ,Msgid for XML: " + MsgidFromXml);
            logData.setRemark("MCHService Receive Request");
            logData.setEj(TxHelper.generateEj());
            this.logMessage(logData);
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            MCHHandler mchHandler = new MCHHandler();
            mchHandler.setEj(logData.getEj());
            mchHandler.setLogContext(logData);
            FEPChannel channel = FEPChannel.parse(findCHANNEL(messageIn));
            if(channel.equals(FEPChannel.NBP) || channel.equals(FEPChannel.NBQ))
                channel = FEPChannel.NETBANK;
            messageOut = mchHandler.dispatch(channel, messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".processResponseData"));
            logData.setProgramFlowType(ProgramFlow.MFTGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark("MFTService Receive Response");
            this.logMessage(logData);

            if (StringUtils.isNotBlank(messageOut)) {
                FeeBackSuccess(messageOut, logData);
            }
        } catch (Exception e) {
            logData = new LogData();
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/mchfep/recv");
            this.logMessage(logData);
            // sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw ExceptionUtil.createRuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (StringUtils.isNotBlank(messageOut))
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send msg:", messageOut);
        }
    }

    public String getName() {
        return SvrConst.SVR_MCH;
    }

    private boolean FeeBackSuccess(String returnStr, LogData logData) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getMchAck().getDestination(), returnStr, null, null);
            return true;
        } catch (Exception ex) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sendQueue"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(ex.getMessage());
            logData.setProgramException(ex);
            this.logMessage(logData);
            // sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".sendQueue"));
            logData.setProgramException(ex);
            sendEMS(logData);
            return false;
        }
    }

    private String findCHANNEL(String str) {
        // 找到開始標籤和結束標籤的索引
        int startIndex = str.indexOf("<CHANNEL>");
        int endIndex = str.indexOf("</CHANNEL>");
        // 提取 CHANNEL 的值
        String clientTraceId = str.substring(startIndex + "<CHANNEL>".length(), endIndex);
        return clientTraceId;
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
