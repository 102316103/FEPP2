package com.syscom.fep.jms.test;

import com.syscom.fep.frmcommon.jms.*;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsBaseTest;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgPayloadOperator;
import com.syscom.fep.jms.JmsMsgSimpleStringInOperator;
import com.syscom.fep.jms.queue.MFTQueueConsumers;
import com.syscom.fep.jms.queue.TestQueueConsumers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import java.nio.charset.StandardCharsets;

public class JmsTest extends JmsBaseTest {
    @Autowired
    private JmsMsgConfiguration jmsMsgConfiguration;
    @Autowired
    private JmsMsgPayloadOperator jmsMsgPayloadOperator;
    @Autowired
    private JmsMsgSimpleStringInOperator jmsMsgSimpleStringInOperator;
    @Autowired
    private JmsListenerEndpointRegistry registry;

    @Test
    public void testSendQueue() throws Exception {
        JmsProperty property = new JmsProperty();
        property.setTimeToLive(10000L);
        property.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        jmsMsgPayloadOperator.sendQueue(new PlainTextMessage(JmsKind.QUEUE, "FEP.FISC.QL1", "Hello World"), property, new JmsHandler() {
            @Override
            public void setPropertyOut(Message message) throws JMSException {
                JmsFactory.setCorrelationID(message, "999999A");
            }
        });
    }

    @Test
    public void testReceiveQueue() {
        JmsProperty property = new JmsProperty();
        property.setMessageSelector(JmsFactory.getSelectorForCorrelationID("999999", StandardCharsets.UTF_8.name()));
        while (true) {
            try {
                PlainTextMessage response =
                        (PlainTextMessage) jmsMsgPayloadOperator.receiveQueue(
                                "FEP.FISC.QL1", property, new JmsHandler() {
                                    @Override
                                    public void getPropertyIn(Message message) throws JMSException {
                                        System.out.println(message);
                                    }
                                });
                UnitTestLogger.info(response);
            } catch (Exception e) {
                UnitTestLogger.error(e, e.getMessage());
            }
        }
    }

    @Test
    public void test() throws Exception {
        while (true) {
            jmsMsgPayloadOperator.sendQueue(new PlainTextMessage(JmsKind.QUEUE, jmsMsgConfiguration.getQueueNames().getTest().getDestination(), Long.toString(System.currentTimeMillis())), null, null);
            Thread.sleep(100);
        }
    }

    @Test
    public void testReceiveQueue2() {
        // JmsProperty property = new JmsProperty();
        // property.setMessageSelector(JmsFactory.getSelectorForCorrelationID("999999", StandardCharsets.UTF_8.name()));
        while (true) {
            try {
                String response =
                        (String) jmsMsgSimpleStringInOperator.receiveQueue(
                                jmsMsgConfiguration.getQueueNames().getMft().getDestination(), null, new JmsHandler() {
                                    @Override
                                    public void getPropertyIn(Message message) throws JMSException {
                                        System.out.println(message);
                                    }
                                });
                UnitTestLogger.info(response);
            } catch (Exception e) {
                UnitTestLogger.error(e, e.getMessage());
            }
        }
    }

    @Test
    public void testReceiveQueue3() throws InterruptedException {
        SpringBeanFactoryUtil.registerBean(MFTQueueConsumers.class).subscribe((destination, payload, message) -> {
            System.out.println(message);
            UnitTestLogger.info(payload);
        });
        Thread.sleep(Long.MAX_VALUE);
    }
}
