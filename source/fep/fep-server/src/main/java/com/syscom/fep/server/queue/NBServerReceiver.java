package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.NBQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.NBFEPHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.jms.Message;

@StackTracePointCut(caller = SvrConst.SVR_NB)
public class NBServerReceiver extends FEPBase implements JmsReceiver<String> {
    private static final String PROGRAM_NAME = NBServerReceiver.class.getSimpleName();

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(NBQueueConsumers.class).subscribe(this));
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
            String returnStr = MultiProcess(messageIn);
            String channel = getChannel(messageIn);
            if (StringUtils.isNotBlank(returnStr)) {
                if (messageIn.equals("NBP")) { // 個銀
                    FeeBackSuccessNBP(returnStr);
                } else if (messageIn.equals("NBQ")) { // 企銀
                    FeeBackSuccessNBQ(returnStr);
                }
            }
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return SvrConst.SVR_NB;
    }

    private String MultiProcess(String messageIn) {
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
        logData.setMessage(messageIn);
        logData.setRemark("NBService Receive Request");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            NBFEPHandler nbHandler = new NBFEPHandler();
            nbHandler.setEj(logData.getEj());
            nbHandler.setLogContext(logData);
            messageOut = nbHandler.dispatch(FEPChannel.NETBANK, messageIn);


            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark("NBService Get Response from NBHandler");
            this.logMessage(logData);
            return messageOut;
        } catch (Exception e) {
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/nbfep/recv");
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

    private boolean FeeBackSuccessNBP(String returnStr) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getNbPAck().getDestination(), returnStr, null, null);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean FeeBackSuccessNBQ(String returnStr) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getNbQAck().getDestination(), returnStr, null, null);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 從電文拆解 CHANNEL
     *
     * @param data
     * @return
     */
    public String getChannel(String data) {
        String str = "";
        int startIndex = data.indexOf("<CHANNEL>");
        if (startIndex != -1) {
            int endIndex = data.indexOf("</CHANNEL>", startIndex);
            if (endIndex != -1) {
                str = data.substring(startIndex + "<CHANNEL>".length(), endIndex);
            }
        }
        return str;
    }
}
