package com.syscom.fep.jms.instance.ems.sender;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsKind;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.jms.instance.ems.EmsQueueConfigurationProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(value = {
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_QUEUEMANAGER,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_CHANNEL,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_CONNNAME,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_USER,
        EmsQueueSenderConstant.CONFIGURATION_PROPERTIES_PASSWORD})
public class EmsQueueSender implements EmsQueueSenderConstant {
    private static final LogHelper logger = LogHelperFactory.getJmsLogger();
    @Autowired
    private EmsQueueSenderConfiguration emsQueueSenderConfiguration;
    @Qualifier(JMS_OPERATOR_LIST)
    @Autowired
    private List<EmsQueueSenderOperator> emsQueueSenderOperatorList;

    public void sendEMS(String message) throws Exception {
        if (CollectionUtils.isEmpty(emsQueueSenderOperatorList)) return;
        List<EmsQueueConfigurationProperties> prop = emsQueueSenderConfiguration.getProp();
        if (CollectionUtils.isEmpty(prop)) return;
        String queueName = emsQueueSenderConfiguration.getEmsQueueName();
        if (StringUtils.isBlank(queueName)) return;
        PlainTextMessage payload = new PlainTextMessage(JmsKind.QUEUE, queueName, message);
        int errorCount = 0;
        for (int i = 0; i < emsQueueSenderOperatorList.size(); i++) {
            EmsQueueSenderOperator operator = emsQueueSenderOperatorList.get(i);
            try {
                operator.sendQueue(payload, null, null);
                // 跳出for
                break;
            } catch (Exception e) {
                // 異常次數累加
                errorCount++;
                // 取出Properties列印log
                EmsQueueConfigurationProperties properties = prop.get(i);
                logger.error("Send EMS Queue Failed!!!",
                        " QueueManager:", properties.getQueueManager(),
                        ", Channel:", properties.getChannel(),
                        ", ConnName:", properties.getConnName(),
                        ", User:", properties.getUser(),
                        ", Payload:", payload.toString());
            }
        }
        // 如果有異常, 則丟出去
        if (errorCount == emsQueueSenderOperatorList.size()) throw ExceptionUtil.createException("Send EMS Queue Failed!!!");
    }
}
