package com.syscom.fep.frmcommon.jms;

import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQTopic;

import javax.jms.JMSException;
import javax.jms.Message;

public interface JmsHandler {

    default void setPropertyOut(Message message) throws JMSException {}

    default void getPropertyIn(Message message) throws JMSException {}

    default void setPropertyOut(MQQueue destination) throws JMSException {}

    default void setPropertyOut(MQTopic destination) throws JMSException {}

}
