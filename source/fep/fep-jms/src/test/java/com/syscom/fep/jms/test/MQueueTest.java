package com.syscom.fep.jms.test;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class MQueueTest {
    static {
        System.setProperty("com.ibm.mq.cfg.jmqi.useMQCSPauthentication", "Y");
    }

    @Test
    public void send() {
        // String hostname = "localhost";
        String hostname = "192.168.30.29";
        int port = 1414;
        String channel = "DEV.ADMIN.SVRCONN";
        String userID = "admin";
        String sscode = "passw0rd";
        // String userID = "mquser";

        // String queueName = "TestQueue";
        // String queueName = "FEP.FISC.QL1";
        // String queueName = "MFTQueue";
        String queueName = "MCH.FEP.ONLINE.RQ";

        // String message = " ?  IBAFRCV1                    0000Y   1130201165718                        4upcbin02011657.TXT                                                                                                                                      ";

        // String hex = "00B10000C9C2C1C6D9C3E5F14040404040404040404040404040404040404040F0F0F0F0E8404040F1F1F3F0F2F1F9F1F6F1F5F0F7404040404040404040404040404040404040404040404040F4A49783828995F0F2F1F9F1F6F1F54BA3A7A3404040404040404040404040404040404040404040404040404040404040404040";
        // String hex = "6F4040C9C2C1C6D9C3E5F14040404040404040404040404040404040404040F0F0F0F0E8404040F1F1F3F0F2F0F1F1F6F5F7F1F8404040404040404040404040404040404040404040404040F4A49783828995F0F2F0F1F1F6F5F74BE3E7E3404040404040404040404040404040404040404040404040404040404040404040";
        // String hex = EbcdicConverter.jHexToIHex(CCSID.English, 230, "00B60000494241465243563120202020202020202020202020202020202020203030303059202020313133303230313136353731382020202020202020202020202020202020202020202020203475706362696E30323031313635372E5458542020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020");

        MQQueueManager queueManager = null;
        MQQueue putQueue = null;
        try {
            queueManager = JmsFactory.createMQQueueManager(hostname, port, "QM1", channel, userID, sscode);
            putQueue = queueManager.accessQueue(queueName, CMQC.MQOO_OUTPUT);
            MQMessage mqMessage = new MQMessage();
            mqMessage.characterSet = 937;
            mqMessage.encoding = 785;
            mqMessage.replyToQueueManagerName = "ReplyToQueueManagerName";
            mqMessage.replyToQueueName = "ReplyToQueueName";
            mqMessage.correlationId = "9999998".getBytes();
            mqMessage.expiry = 100;
            mqMessage.messageId = "我是messageId".getBytes();
            mqMessage.writeString("Hello World");
            // mqMessage.format = "MQSTR   ";
            // mqMessage.write("Hello World".getBytes(CCSID.getCodepage(mqMessage.characterSet)));
            // mqMessage.write(EbcdicConverter.toBytes(CCSID.English, message.length(), message));
            // mqMessage.write(ConvertUtil.hexToBytes(hex));
            MQPutMessageOptions pmo = new MQPutMessageOptions();
            putQueue.put(mqMessage, pmo);
            System.out.println("Put message to queue OK");
        } catch (Exception e) {
            LogHelperFactory.getUnitTestLogger().error(e, e.getMessage());
        } finally {
            try {
                if (putQueue != null) {
                    putQueue.close();
                }
            } catch (MQException e) {
                LogHelperFactory.getUnitTestLogger().warn(e, e.getMessage());
            }
            try {
                if (queueManager != null) {
                    queueManager.disconnect();
                }
            } catch (MQException e) {
                LogHelperFactory.getUnitTestLogger().warn(e, e.getMessage());
            }
        }
    }

    @Test
    public void receive() {
        String hostname = "localhost";
        int port = 1414;
        String channel = "DEV.ADMIN.SVRCONN";
        String userID = "admin";
        String sscode = "passw0rd";

        MQGetMessageOptions gmo = new MQGetMessageOptions();
        gmo.options = gmo.options + MQConstants.MQGMO_SYNCPOINT;
        gmo.options = gmo.options + MQConstants.MQGMO_WAIT;
        gmo.options = gmo.options + MQConstants.MQGMO_FAIL_IF_QUIESCING;
        gmo.waitInterval = 300000;

        MQQueueManager queueManager = null;
        MQQueue getQueue = null;
        try {
            queueManager = JmsFactory.createMQQueueManager(hostname, port, "QM1", channel, userID, sscode);
            getQueue = queueManager.accessQueue("TestQueue", CMQC.MQOO_INPUT_AS_Q_DEF);
            while (true) {
                MQMessage mqMessage = new MQMessage();
                mqMessage.correlationId = "correlationId".getBytes();
                getQueue.get(mqMessage, gmo);
                // String msgContent = mqMessage.readUTF();
                int length = mqMessage.getMessageLength();
                byte[] bytes = new byte[length];
                mqMessage.readFully(bytes);
                String msgContent = new String(bytes);
                System.out.println("Get message from queue OK, msgContent = [" + msgContent + "]");
                System.out.println("replyToQueueManagerName = " + mqMessage.replyToQueueManagerName);
                System.out.println("replyToQueueName = " + mqMessage.replyToQueueName);
                System.out.println("correlationId = " + new String(mqMessage.correlationId, StandardCharsets.UTF_8));
                System.out.println("messageId = " + new String(mqMessage.messageId, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LogHelperFactory.getUnitTestLogger().error(e, e.getMessage());
        } finally {
            try {
                if (getQueue != null) {
                    getQueue.close();
                }
            } catch (MQException e) {
                LogHelperFactory.getUnitTestLogger().warn(e, e.getMessage());
            }
            try {
                if (queueManager != null) {
                    queueManager.disconnect();
                }
            } catch (MQException e) {
                LogHelperFactory.getUnitTestLogger().warn(e, e.getMessage());
            }
        }
    }
}
