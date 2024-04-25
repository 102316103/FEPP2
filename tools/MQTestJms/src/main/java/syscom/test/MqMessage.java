package syscom.test;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class MqMessage {
    private static Logger logger = LoggerFactory.getLogger(MqMessage.class);

    private final JmsTemplate jmsTemplate;

	@Value("${config.queueId}")
	private String queueId;

	@Value("${config.data}")
	private String data;

    @Autowired
    public MqMessage(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage() {
    	logger.info("toQueueId:{}", this.queueId);
		logger.info("data : {}", this.data);
        jmsTemplate.convertAndSend(this.queueId, this.data);
    }

    public void receiveMessage() throws JMSException {
    	Message receive = jmsTemplate.receive(this.queueId);
    	String body = receive.getBody(String.class);
    	logger.info("data : {}", body);
    }
}
