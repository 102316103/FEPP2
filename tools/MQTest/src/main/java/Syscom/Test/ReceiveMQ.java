package Syscom.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;

public class ReceiveMQ {

	public static void main(String[] args) {

		try {
			Properties properties = new Properties();
			BufferedReader bufferedReader = new BufferedReader(new FileReader("./mqParam.properties"));
			properties.load(bufferedReader);

			// 是否取得所有Queue
			boolean fromIsGetAllQueue = Boolean.parseBoolean(properties.getProperty("fromIsGetAllQueue"));
			String hostname = properties.getProperty("fromHostname");
			String port = properties.getProperty("fromPort");
			String channel2 = properties.getProperty("fromChannel");
			String managerId = properties.getProperty("fromQmanagerId");
			String accessQueueId = properties.getProperty("fromQueueId");
			String characterSet = properties.getProperty("characterSet");
			String waitInterval = properties.getProperty("waitInterval");
			String appnameProperty = properties.getProperty("fromMqappnameProperty");
			String fromMqUserID = properties.getProperty("fromMqUserID");
			String fromMqpwd = properties.getProperty("fromMqpwd");
			System.out.println("fromHostname:" + hostname + ",  fromPort:" + port + ",  fromChannel:" + channel2
					+ ",  fromQmanagerId:" + managerId + ",  characterSet:" + characterSet + ",  waitInterval:"
					+ waitInterval + ",  fromMqappnameProperty:" + appnameProperty);
			System.out.println("fromQueueId:" + accessQueueId);
			System.out.println("fromMqUserID:" + fromMqUserID);
			System.out.println("fromMqpwd:" + fromMqpwd);

			MQEnvironment.hostname = hostname;
			MQEnvironment.port = Integer.valueOf(port);
			MQEnvironment.channel = channel2;
//			MQEnvironment.properties.put(MQConstants.APPNAME_PROPERTY, appnameProperty);
			MQEnvironment.userID = fromMqUserID;
			if (fromMqpwd != null && !"".equals(fromMqpwd)) {
				MQEnvironment.password = fromMqpwd;
			}

			while (true) {
				MQQueueManager queueManager = new MQQueueManager(managerId);
				MQQueue getQueue = queueManager.accessQueue(accessQueueId, CMQC.MQOO_INPUT_AS_Q_DEF);
				MQMessage theMessage = new MQMessage();
				MQGetMessageOptions gmo = new MQGetMessageOptions();
//				theMessage.characterSet = Integer.valueOf(characterSet);
				gmo.waitInterval = Integer.valueOf(waitInterval); // wait for 5 second
				getQueue.get(theMessage, gmo);
//				String data = theMessage.readUTF();
				int strLen = theMessage.getMessageLength();
				byte[] strData = new byte[strLen];
				theMessage.readFully(strData);
				String data = new String(strData);
				getQueue.close();

				System.out.println("Get message OK, ");
//				System.out.println("    JMSType: " + theMessage.getStringProperty("JMSType"));
//				System.out.println("    message format: " + theMessage.format);
				System.out.println("    message body:" + data);
				if (!fromIsGetAllQueue) {
					break;
				}
				queueManager.disconnect();
			}
		} catch (MQException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("END");
	}

}
