package com.syscom.fep.tools.mq.instance;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.tools.mq.MQConstants;
import com.syscom.fep.tools.mq.MQInstance;
import com.syscom.fep.tools.mq.MQProperties;
import com.syscom.fep.tools.mq.MQUtil;

public class MQSender implements MQConstants {
    private final static LogHelper logger = new LogHelper();

    private final static MQInstance instance = MQInstance.sender;

    public static void sendQueue(MQProperties properties) throws Exception {
        MQQueueManager queueManager = null;
        MQQueue putQueue = null;
        try {
            queueManager = MQUtil.createMQQueueManager(
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_HOSTNAME),
                    Integer.parseInt(properties.getProperty(instance, CONFIGURATION_PROPERTIES_PORT)),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_QUEUEMANAGER),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_CHANNEL),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_USER),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_PASSWORD)
            );
            putQueue = queueManager.accessQueue(properties.getProperty(instance, CONFIGURATION_PROPERTIES_QUEUENAME), CMQC.MQOO_OUTPUT);
            MQMessage mqMessage = new MQMessage();
            // 塞入配置檔中的設定屬性
            MQUtil.setProperty(instance, properties, mqMessage);
            String messageOut = properties.getProperty(instance, CONFIGURATION_PROPERTIES_MESSAGE);
            mqMessage.writeString(messageOut);
            MQPutMessageOptions pmo = new MQPutMessageOptions();
            putQueue.put(mqMessage, pmo);
            logger.info("Put message to queue OK", Const.MESSAGE_OUT, messageOut);
        } catch (Exception e) {
            logger.error(e, "Put message to queue Failed, ", e.getMessage());
            throw e;
        } finally {
            try {
                if (putQueue != null) {
                    putQueue.close();
                }
            } catch (MQException e) {
                logger.warn(e, e.getMessage());
            }
            try {
                if (queueManager != null) {
                    queueManager.disconnect();
                }
            } catch (MQException e) {
                logger.warn(e, e.getMessage());
            }
        }
    }
}
