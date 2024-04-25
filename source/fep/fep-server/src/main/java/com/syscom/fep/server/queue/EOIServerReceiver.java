package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
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
import com.syscom.fep.jms.queue.EOIQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.EOIHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.jms.Message;

@StackTracePointCut(caller = SvrConst.SVR_EOI)
public class EOIServerReceiver extends FEPBase implements JmsReceiver<String> {
    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(EOIQueueConsumers.class).subscribe(this));
    }

    /**
     * 接收訊息
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, String payload, Message message) {
        String messageIn ="";
        messageIn = payload;
        LogHelperFactory.getTraceLogger().trace(SvrConst.SVR_EOI, " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setProgramFlowType(ProgramFlow.RESTFulOut);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setMessage(messageIn);
        logData.setRemark("EOIService Receive Request");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            EOIHandler eoiHandler = new EOIHandler();
            eoiHandler.setEj(logData.getEj());
            eoiHandler.setLogContext(logData);
//            messageOut = Handler.dispatch(FEPChannel.MCH, messageIn);
            //            logData.setProgramName(StringUtils.join(ProgramName, ".processResponseData"));
            //            logData.setProgramFlowType(ProgramFlow.MFTGWOut);
            //            logData.setMessageFlowType(MessageFlow.Response);
            //            logData.setMessage(messageOut);
            //            logData.setRemark("MFTService Receive Response");
            //            this.logMessage(logData);
            //
            //            if (StringUtils.isNotBlank(messageOut)) {
            //                FeeBackSuccess(messageOut,logData);
            //            }
        } catch (Exception e) {
            logData = new LogData();
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/eoifep/recv");
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
        return SvrConst.SVR_EOI;
    }
    private boolean FeeBackSuccess(String returnStr,LogData logData) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getEoiAck().getDestination(), returnStr, null, null);
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
}
