package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.queue.TestQueueConsumers;

import javax.annotation.PostConstruct;
import javax.jms.Message;

/**
 * 僅用於測試程式參考
 */
public class TestReceiver extends FEPBase implements JmsReceiver<PlainTextMessage> {

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(TestQueueConsumers.class).subscribe(this));
    }

    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, PlainTextMessage payload, Message message) {
        LogHelperFactory.getTraceLogger().info(Const.MESSAGE_IN, payload.getPayload());
    }
}
