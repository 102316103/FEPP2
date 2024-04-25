package Syscom.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.msg.client.wmq.compat.base.internal.MQC;
public class PutMQ {

	public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			BufferedReader bufferedReader = new BufferedReader(new FileReader("./mqParam.properties"));
			properties.load(bufferedReader);
			String hostname = properties.getProperty("toHostname");
			String port = properties.getProperty("toPort");
			String channel = properties.getProperty("toChannel");
			String managerId = properties.getProperty("toQmanagerId");
			String accessQueueId = properties.getProperty("toQueueId");
			String characterSet = properties.getProperty("characterSet");
			String mqContent = properties.getProperty("toData");
			String appnameProperty = properties.getProperty("toMqappnameProperty");
			String toMqUserID = properties.getProperty("toMqUserID");
			String toMqpwd = properties.getProperty("toMqpwd");
			String toJmsTypeIsText = properties.getProperty("toJmsTypeIsText");
			System.out.println("toHostname:"+hostname+",  toPort:"+port+",  toChannel:"+channel+",  toQmanagerId:"+managerId+",  characterSet:"+characterSet+",  toMqappnameProperty:"+appnameProperty);
			System.out.println("toQueueId:"+accessQueueId);
			System.out.println("toMqUserID:"+toMqUserID);
			System.out.println("toMqpwd:"+toMqpwd);
			if("true".equals(toJmsTypeIsText)) {
				System.out.println("message format:"+MQC.MQFMT_STRING);
				System.out.println("JMSType: TextMessage");
			}else {
				System.out.println("message format: null");
				System.out.println("JMSType: null");
			}
			System.out.println("toData:"+mqContent);

			MQEnvironment.hostname = hostname;
			MQEnvironment.port = Integer.valueOf(port);
			MQEnvironment.channel = channel;
			MQEnvironment.userID = toMqUserID;
			if(toMqpwd != null && !"".equals(toMqpwd)) {
				MQEnvironment.password = toMqpwd;
			}
			MQQueueManager queueManager = new MQQueueManager(managerId);

			MQEnvironment.properties.put(MQConstants.APPNAME_PROPERTY, appnameProperty);
//			// put message to queue
			MQQueue putQueue = queueManager.accessQueue(accessQueueId, CMQC.MQOO_OUTPUT);
			MQMessage myMessage = new MQMessage();
		    myMessage.characterSet = Integer.valueOf(characterSet);
//		    //myMessage.expiry = 300; //30秒,單位為1/10秒
			myMessage.writeString(mqContent);
			if("true".equals(toJmsTypeIsText)) {
				myMessage.format = MQC.MQFMT_STRING;
				myMessage.setStringProperty("JMSType", "TextMessage");
			}
//		    myMessage.writeUTF(mqContent);
	        MQPutMessageOptions pmo = new MQPutMessageOptions();
	        putQueue.put(myMessage, pmo);
	        putQueue.close();
	        System.out.println("Put message to queue OK");

	        queueManager.disconnect();
		} catch (MQException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		System.out.println("END");
	}

}
