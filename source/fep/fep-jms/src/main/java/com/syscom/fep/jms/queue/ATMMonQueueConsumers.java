package com.syscom.fep.jms.queue;

import com.syscom.fep.frmcommon.jms.JmsNotifier;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.jms.JmsMsgConstant;
import com.syscom.fep.jms.entity.MsMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * 監聽ATMMonQueue訊息
 * <p>
 * 注意, 因為Queue的名字是從DB取的, 所以在jmsMsgConfiguration.queueNames.atmMon塞入值後, 再使用SpringBeanFactoryUtil.registerBean(ATMMonQueueConsumers.class)手動註冊
 *
 * @author Richard
 */
public class ATMMonQueueConsumers extends JmsNotifier<MsMessage<String>, JmsReceiver<MsMessage<String>>> implements JmsMsgConstant {
    @Qualifier(MESSAGE_CONVERTER)
    @Autowired
    private MessageConverter messageConverter;

    @Override
    @JmsListener(
            id = "#{@" + QUEUE_NAME_ATMMON + CONFIGURATION_PROPERTIES_SUFFIX_IDENTITY + "}",
            destination = "#{@" + QUEUE_NAME_ATMMON + CONFIGURATION_PROPERTIES_SUFFIX_DESTINATION + "}",
            concurrency = "#{@" + QUEUE_NAME_ATMMON + CONFIGURATION_PROPERTIES_SUFFIX_CONCURRENCY + "}",
            containerFactory = QUEUE_LISTENER_FACTORY)
    public void onMessage(Message message) {
        super.messageReceived(message);
    }

    /**
     * @param object  the object to convert
     * @param session the Session to use for creating a JMS Message
     * @return
     * @throws JMSException
     * @throws MessageConversionException
     */
    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        return messageConverter.toMessage(object, session);
    }

    /**
     * @param message the message to convert
     * @return
     * @throws JMSException
     * @throws MessageConversionException
     */
    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        return messageConverter.fromMessage(message);
    }
}
