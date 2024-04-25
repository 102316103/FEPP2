package com.syscom.fep.jms.instance.ems.sender;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.instance.ems.EmsQueueConfigurationProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

@Component
@EnableJms
@Validated
@ConfigurationProperties(prefix = EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(value = {
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_USER,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_PASSWORD})
@RefreshScope
public class EmsQueueSenderConfiguration implements EmsQueueSenderConstant {
    @Autowired
    private JmsListenerEndpointRegistry registry;
    private List<EmsQueueConfigurationProperties> prop;
    private final List<ConnectionFactory> connectionFactories = new ArrayList<>();

    public void setProp(List<EmsQueueConfigurationProperties> prop) {
        this.prop = prop;
    }

    public List<EmsQueueConfigurationProperties> getProp() {
        return prop;
    }

    @Bean(name = MESSAGE_CONVERTER)
    public MessageConverter messageConverter() {
        return JmsFactory.createMappingJackson2MessageConverter();
    }

    @Bean(name = JMS_OPERATOR_LIST)
    public List<EmsQueueSenderOperator> emsQueueSenderOperatorList(@Qualifier(MESSAGE_CONVERTER) MessageConverter messageConverter) throws JMSException {
        List<EmsQueueSenderOperator> list = new ArrayList<>();
        for (JmsConfigurationProperties properties : prop) {
            ConnectionFactory connectionFactory = this.connectionFactory(properties);
            JmsTemplate jmsTemplate = JmsFactory.createJmsTemplate(connectionFactory, messageConverter);
            list.add(new EmsQueueSenderOperator(registry, jmsTemplate, messageConverter));
        }
        return list;
    }

    private ConnectionFactory connectionFactory(JmsConfigurationProperties prop) throws JMSException {
        ConnectionFactory connectionFactory;
        if (prop.getCache() != null && prop.getCache().isEnabled()) {
            connectionFactory = cachingJmsConnectionFactory(prop);
        } else {
            connectionFactory = jmsConnectionFactory(prop);
        }
        this.connectionFactories.add(connectionFactory);
        return connectionFactory;
    }

    private ConnectionFactory jmsConnectionFactory(JmsConfigurationProperties prop) throws JMSException {
        LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " single IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.creactMQConnectionFactory(prop);
            return JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " single IBM MQ ConnectionFactory finish");
        }
    }

    private CachingConnectionFactory cachingJmsConnectionFactory(JmsConfigurationProperties prop) throws JMSException {
        LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " caching IBM MQ ConnectionFactory start...");
        try {
            ConnectionFactory connectionFactory = JmsFactory.creactMQConnectionFactory(prop);
            UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = JmsFactory.createUserCredentialsConnectionFactoryAdapter(connectionFactory, prop);
            return JmsFactory.createCachingConnectionFactory(userCredentialsConnectionFactoryAdapter, prop);
        } finally {
            LogHelperFactory.getTraceLogger().trace("Creating ", prop.getHostName(), StringUtils.SPACE, INSTANCE_NAME.toUpperCase(), " caching IBM MQ ConnectionFactory finish");
        }
    }

    @PostConstruct
    public void print() {
        printConstant();
        int index = 0;
        for (JmsConfigurationProperties properties : prop) {
            LogHelperFactory.getGeneralLogger().info(properties.toString(INSTANCE_NAME + "[" + index + "]", CONFIGURATION_PROPERTIES_PREFIX + ".prop[" + index++ + "]"));
        }
    }

    /**
     * 取出EMS Queue Name, 如果spring.fep.jms.ems.sender.prop[0].queue-names.ems沒有設定, 則取spring.fep.jms.queue-names.ems
     *
     * @return
     */
    public String getEmsQueueName() {
        if (CollectionUtils.isNotEmpty(this.prop)
                && this.prop.get(0).getQueueNames().getEms() != null && StringUtils.isNotBlank(this.prop.get(0).getQueueNames().getEms().getDestination())) {
            return this.prop.get(0).getQueueNames().getEms().getDestination();
        }
        JmsMsgConfiguration jmsMsgConfiguration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        return jmsMsgConfiguration.getQueueNames().getEms().getDestination();
    }

    @PreDestroy
    public void destroy() {
        if (!connectionFactories.isEmpty()) {
            int index = 0;
            for (ConnectionFactory connectionFactory : connectionFactories)
                JmsFactory.closeConnection(INSTANCE_NAME.toUpperCase() + "[" + index++ + "]", connectionFactory);
        }
    }
}
