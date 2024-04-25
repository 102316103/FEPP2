package com.syscom.fep.service.cbstimeoutrerun;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.CBSPENDQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.cbspend.CBSPEND;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.jms.Message;

@StackTracePointCut(caller = SvrConst.SVR_CBSTimeOutRerun)
public class CBSTimeOutRerun extends FEPBase implements JmsReceiver<String> {
    private static final String PROGRAM_NAME = CBSTimeOutRerun.class.getSimpleName();

    @PostConstruct
    public void initialization() {
        SpringBeanFactoryUtil.registerBean(CBSPENDQueueConsumers.class).subscribe(this);
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
        LogMDC.put(Const.MDC_PGFILE, SvrConst.SVR_CBSTimeOutRerun);
        String messageIn ="";
        messageIn = payload;
        LogHelperFactory.getTraceLogger().trace(SvrConst.SVR_CBSTimeOutRerun, " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setProgramFlowType(ProgramFlow.CBSTimeOutRerunIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setMessage(messageIn);
        logData.setRemark("CBSTimeOutRerun Receive Request");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            CBSPEND cbspend = new CBSPEND();
            cbspend.setLogContext(logData);
//            messageOut = cbspend.dispatch(FEPChannel.CBSPEND, messageIn);

            if(StringUtils.isNotBlank(messageOut)){
                FeeBackSuccess(messageOut,logData);
            }

            return;
        } catch (Exception e) {
            logData = new LogData();
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.CBSTimeOutRerunOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/CBSPEND/recv");
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

    public String getName() {
        return SvrConst.SVR_CBSTimeOutRerun;
    }


    private boolean FeeBackSuccess(String returnStr,LogData logData) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getCbspend().getDestination(), returnStr, null, null);
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
