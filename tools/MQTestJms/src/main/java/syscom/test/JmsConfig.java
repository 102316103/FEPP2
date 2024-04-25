package syscom.test;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.common.CommonConstants;

@Configuration
@EnableJms
public class JmsConfig {
    private static Logger logger = LoggerFactory.getLogger(JmsConfig.class);

	@Value("${config.queueManager}")
	private String queueManager;

	@Value("${config.channel}")
	private String channel;

	@Value("${config.connName}")
	private String connName;

	@Value("${config.user}")
	private String user;

	@Value("${config.password}")
	private String password;

	@Bean
	public ConnectionFactory connectionFactory() throws JMSException {
		logger.info("toConnName:{}, toChannel:{}, toQmanagerId:{}", this.connName, this.channel, this.queueManager);
		logger.info("toMqUserID:{}", this.user);
		logger.info("toMqpwd:{}", this.password);

		MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();
		mqConnectionFactory.setQueueManager(this.queueManager);
		mqConnectionFactory.setChannel(this.channel);
		mqConnectionFactory.setConnectionNameList(this.connName);
//		mqConnectionFactory.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, CommonConstants.WMQ_CM_CLIENT);
		mqConnectionFactory.setTransportType(CommonConstants.WMQ_CM_CLIENT);

		UserCredentialsConnectionFactoryAdapter connectionFactory = new UserCredentialsConnectionFactoryAdapter();
		connectionFactory.setTargetConnectionFactory(mqConnectionFactory);
		connectionFactory.setUsername(this.user);
		connectionFactory.setPassword(this.password);

		return connectionFactory;
	}

	@Bean
	public JmsTemplate jmsTemplate() throws JMSException {
		return new JmsTemplate(connectionFactory());
	}

	@Bean
	public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
			DefaultJmsListenerContainerFactoryConfigurer configurer) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		return factory;
	}
}
