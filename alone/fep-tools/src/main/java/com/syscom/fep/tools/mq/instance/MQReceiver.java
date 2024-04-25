package com.syscom.fep.tools.mq.instance;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.CCSID;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.tools.mq.MQConstants;
import com.syscom.fep.tools.mq.MQInstance;
import com.syscom.fep.tools.mq.MQProperties;
import com.syscom.fep.tools.mq.MQUtil;

import java.io.UnsupportedEncodingException;

public class MQReceiver implements MQConstants {
    private static final LogHelper logger = new LogHelper();

    private final static MQInstance instance = MQInstance.receiver;

    public static void receiveQueue(MQProperties properties) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        gmo.options = gmo.options + com.ibm.mq.constants.MQConstants.MQGMO_SYNCPOINT;
        gmo.options = gmo.options + com.ibm.mq.constants.MQConstants.MQGMO_WAIT;
        gmo.options = gmo.options + com.ibm.mq.constants.MQConstants.MQGMO_FAIL_IF_QUIESCING;
        if (properties.containsKey(instance, CONFIGURATION_PROPERTIES_TIMEOUT)) {
            gmo.waitInterval = Integer.parseInt(properties.getProperty(instance, CONFIGURATION_PROPERTIES_TIMEOUT));
        }
        MQQueueManager queueManager = null;
        MQQueue getQueue = null;
        try {
            queueManager = MQUtil.createMQQueueManager(
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_HOSTNAME),
                    Integer.parseInt(properties.getProperty(instance, CONFIGURATION_PROPERTIES_PORT)),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_QUEUEMANAGER),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_CHANNEL),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_USER),
                    properties.getProperty(instance, CONFIGURATION_PROPERTIES_PASSWORD)
            );
            getQueue = queueManager.accessQueue(properties.getProperty(instance, CONFIGURATION_PROPERTIES_QUEUENAME), CMQC.MQOO_INPUT_AS_Q_DEF);
            MQMessage mqMessage = new MQMessage();
            // 塞入配置檔中的設定屬性
            MQUtil.setProperty(instance, properties, mqMessage);
            logger.info("Wait to Get message from queue in ", gmo.waitInterval, " milliseconds...");
            getQueue.get(mqMessage, gmo);
            int length = mqMessage.getMessageLength();
            byte[] bytes = new byte[length];
            mqMessage.readFully(bytes);
            String messageIn = null;
            try {
                messageIn = new String(bytes, CCSID.getCodepage(mqMessage.characterSet));
            } catch (UnsupportedEncodingException e) {
                messageIn = new String(bytes);
            }
            logger.info("Get message from queue OK", Const.MESSAGE_IN, messageIn);
        } catch (Exception e) {
            logger.error(e, "Get message from queue Failed, ", e.getMessage());
            throw e;
        } finally {
            try {
                if (getQueue != null) {
                    getQueue.close();
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
