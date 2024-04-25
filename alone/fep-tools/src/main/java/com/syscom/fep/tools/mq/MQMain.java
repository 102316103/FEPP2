package com.syscom.fep.tools.mq;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.tools.mq.instance.MQReceiver;
import com.syscom.fep.tools.mq.instance.MQSender;

public class MQMain {
    private static final LogHelper logger = new LogHelper();

    public static void main(String[] args) {
        try {
            MQProperties properties = new MQProperties();
            properties.load();
            MQSender.sendQueue(properties);
            MQReceiver.receiveQueue(properties);
            logger.info("Test OK");
        } catch (Exception e) {
            logger.error("Test Failed");
        }
    }
}
