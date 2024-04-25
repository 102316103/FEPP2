package com.syscom.fep.tools.jms.consumers;

import com.syscom.fep.frmcommon.jms.JmsNotifier;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.jms.JmsMsgConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * 監聽PYBatchAckQueue訊息
 *
 * @author Richard
 */
public class CBSPENDQueueForTestConsumers extends JmsNotifier<String, JmsReceiver<String>> implements JmsMsgConstant {
    /**
     * TODO 這個messageConvert先暫時這樣寫, 包括PlainTextMessage, 要根據實際進來的電文格式調整 By Richard
     */
    @Qualifier(SIMPLE_STRING_IN_MESSAGE_CONVERTER)
    @Autowired
    private MessageConverter messageConverter;

    @Override
    @JmsListener(
            id = "#{@" + QUEUE_NAME_CBSPEND + CONFIGURATION_PROPERTIES_SUFFIX_IDENTITY + "}",
            destination = "#{@" + QUEUE_NAME_CBSPEND + CONFIGURATION_PROPERTIES_SUFFIX_DESTINATION + "}",
            concurrency = "#{@" + QUEUE_NAME_CBSPEND + CONFIGURATION_PROPERTIES_SUFFIX_CONCURRENCY + "}",
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
