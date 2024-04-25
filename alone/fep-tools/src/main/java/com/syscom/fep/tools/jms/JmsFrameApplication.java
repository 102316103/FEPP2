package com.syscom.fep.tools.jms;

import com.ibm.msg.client.jms.JmsConstants;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.esapi.ESAPIConfiguration;
import com.syscom.fep.frmcommon.gui.AbstractFrame;
import com.syscom.fep.frmcommon.gui.AbstractPanel;
import com.syscom.fep.frmcommon.gui.AdvCombobox;
import com.syscom.fep.frmcommon.gui.GuiProperties;
import com.syscom.fep.frmcommon.gui.util.GuiUtil;
import com.syscom.fep.frmcommon.jms.*;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgPayloadOperator;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.entity.MsMessage;
import com.syscom.fep.jms.instance.batch.BatchQueueOperator;
import com.syscom.fep.jms.instance.ems.sender.EmsQueueSenderConstant;
import com.syscom.fep.jms.instance.ems.sender.EmsQueueSenderOperator;
import com.syscom.fep.jms.queue.CBSPENDQueueConsumers;
import com.syscom.fep.jms.queue.MCHQueueConsumers;
import com.syscom.fep.jms.queue.TestQueueConsumers;
import com.syscom.fep.jms.topic.TestTopicSubscriber;
import com.syscom.fep.tools.jms.consumers.CBSPENDQueueForTestConsumers;
import com.syscom.fep.tools.jms.consumers.PYBatchAckQueueForTestConsumers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.syscom.fep"})
public class JmsFrameApplication {
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();

    private static final List<String> MSMESSAGE_DESTINATION_NAME_FIELD_LIST = Arrays.asList(
            "dead", "atmMon"
    );
    private static final List<String> SIMPLE_DESTINATION_NAME_FIELD_LIST = Arrays.asList(
            "pyBatch", "pyBatchAck", "nb", "mb", "ivr", "eatm", "mft", "mftAck", "hce", "cbspend", "mch"
    );

    static {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("management.metrics.tags.application", "fep-jms-frame");
        ESAPIConfiguration.init();
        Jasypt.loadEncryptorKey(null);
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(JmsFrameApplication.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            application.run(args);
            new JmsFrame("IBM MQ Test Frame").showFrame();
        } catch (Exception e) {
            if ("org.springframework.boot.devtools.restart.SilentExitExceptionHandler$SilentExitException".equals(e.getClass().getName())) {
                // ignore
            } else {
                logger.exceptionMsg(e, "JmsFrameApplication run failed!!!");
            }
        }
    }

    private static class JmsFrame extends AbstractFrame {
        public JmsFrame(String title) {
            super(title);
        }

        @Override
        protected Component guiLayout() {
            JmsPanel jmsPanel = new JmsPanel();
            return jmsPanel;
        }

        @Override
        protected JMenuBar createMenuBar() {
            return null;
        }

        private class JmsPanel extends AbstractPanel {
            private static final long serialVersionUID = 1L;

            private JButton sendBtn;
            private JCheckBox receiveStopChk;
            private AdvCombobox destinationNameCombo;
            private JTextArea messageSendToArea, messageRecvFromArea;

            @Override
            protected void initComponents() {
                JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
                configuration.getQueueNames().setDead(new JmsDefinition());
                configuration.getQueueNames().setDeadFisc(new JmsDefinition());
                configuration.getQueueNames().setAtmMon(new JmsDefinition());
                configuration.getQueueNames().getDead().setDestination("DeadQueue");
                configuration.getQueueNames().getDeadFisc().setDestination("DeadFiscQueue");
                configuration.getQueueNames().getAtmMon().setDestination("AtmMonQueue");

                MyJmsReceiver myJmsReceiver = new MyJmsReceiver();

                SpringBeanFactoryUtil.registerBean(TestQueueConsumers.class).subscribe(myJmsReceiver);
                SpringBeanFactoryUtil.registerBean(TestTopicSubscriber.class).subscribe(myJmsReceiver);

                // SpringBeanFactoryUtil.registerBean(DeadQueueConsumers.class).subscribe(myJmsReceiver);
                // SpringBeanFactoryUtil.registerBean(BatchQueueConsumers.class).subscribe(myJmsReceiver);
                // SpringBeanFactoryUtil.registerBean(EmsFromQueueConsumers.class).subscribe(myJmsReceiver);
                // SpringBeanFactoryUtil.registerBean(PYBatchQueueConsumers.class).subscribe(myJmsReceiver);
                // SpringBeanFactoryUtil.registerBean(ATMMonQueueConsumers.class).subscribe(msReceiver);
                // SpringBeanFactoryUtil.registerBean(NBQueueConsumers.class).subscribe(myJmsReceiver);
                SpringBeanFactoryUtil.registerBean(PYBatchAckQueueForTestConsumers.class).subscribe(myJmsReceiver);
                SpringBeanFactoryUtil.registerBean(MCHQueueConsumers.class).subscribe(myJmsReceiver);
                // SpringBeanFactoryUtil.registerBean(CBSPENDQueueConsumers.class).subscribe(myJmsReceiver);

                MyActionListener listener = new MyActionListener();
                sendBtn = GuiUtil.createButton("發送", listener);
                receiveStopChk = GuiUtil.createCheckBox("停止接收", listener);
                messageSendToArea = GuiUtil.createTextArea(StringUtils.EMPTY, null, false);
                messageRecvFromArea = GuiUtil.createTextArea(StringUtils.EMPTY, null, false);
                destinationNameCombo = new AdvCombobox() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public List<ComboItem> getComboItemList() {
                        List<ComboItem> dataList = new ArrayList<>();
                        List<Field> fieldList = ReflectUtil.getAllFields(configuration.getQueueNames());
                        for (Field field : fieldList) {
                            JmsDefinition destination = ReflectUtil.getFieldValue(configuration.getQueueNames(), field, null);
                            if (destination == null) {
                                continue;
                            }
                            if (MSMESSAGE_DESTINATION_NAME_FIELD_LIST.contains(field.getName())) {
                                dataList.add(new ComboItem(destination.getDestination(), new MsMessage<String>(JmsKind.QUEUE, destination.getDestination(), StringUtils.EMPTY)));
                            } else if (SIMPLE_DESTINATION_NAME_FIELD_LIST.contains(field.getName()) || SIMPLE_DESTINATION_NAME_FIELD_LIST.contains(destination.getDestination())) {
                                dataList.add(new ComboItem(destination.getDestination(), JmsKind.QUEUE));
                            } else {
                                dataList.add(new ComboItem(destination.getDestination(), new PlainTextMessage(JmsKind.QUEUE, destination.getDestination(), StringUtils.EMPTY)));
                            }
                        }
                        fieldList = ReflectUtil.getAllFields(configuration.getTopicNames());
                        for (Field field : fieldList) {
                            JmsDefinition destination = ReflectUtil.getFieldValue(configuration.getTopicNames(), field, null);
                            if (destination == null) {
                                continue;
                            }
                            if (SIMPLE_DESTINATION_NAME_FIELD_LIST.contains(field.getName()) || SIMPLE_DESTINATION_NAME_FIELD_LIST.contains(destination.getDestination())) {
                                dataList.add(new ComboItem(destination.getDestination(), JmsKind.TOPIC));
                            } else {
                                dataList.add(new ComboItem(destination.getDestination(), new PlainTextMessage(JmsKind.TOPIC, destination.getDestination(), StringUtils.EMPTY)));
                            }
                        }
                        return dataList;
                    }

                    @Override
                    public String getCaption() {
                        return "訊息名稱";
                    }
                };
            }

            @Override
            protected Component guiLayout() {
                JPanel contentPane = GuiUtil.createPanel(new GridBagLayout());
                contentPane.setBorder(GuiUtil.createLineBorder(GuiProperties.CLR_BORDER));
                contentPane.add(GuiUtil.createLabel("Queue/Topic Name"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(destinationNameCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(GuiProperties.GAP, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                contentPane.add(GuiUtil.createLabel("Message To Send"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(new JScrollPane(messageSendToArea), new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        GuiUtil.createInsets(0, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                contentPane.add(GuiUtil.createLabel("Message Recv From"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                contentPane.add(new JScrollPane(messageRecvFromArea), new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        GuiUtil.createInsets(0, 0, GuiProperties.GAP, GuiProperties.GAP), 0, 0));

                JPanel btnPane = GuiUtil.createPanel(new FlowLayout(FlowLayout.RIGHT, GuiProperties.GAP, 0));
                btnPane.add(sendBtn);
                btnPane.add(receiveStopChk);
                contentPane.add(btnPane, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        GuiUtil.createInsets(0, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP), 0, 0));
                return contentPane;
            }

            final class MyActionListener implements ActionListener {

                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public void actionPerformed(ActionEvent e) {
                    JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
                    JmsPayloadOperator jmsMsgPayloadOperator = SpringBeanFactoryUtil.getBean(JmsMsgPayloadOperator.class);
                    JmsMsgSimpleOperator jmsMsgSimpleOperator = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
                    EmsQueueSenderOperator emsQueueSenderOperator = ((List<EmsQueueSenderOperator>) SpringBeanFactoryUtil.getBean(EmsQueueSenderConstant.JMS_OPERATOR_LIST)).get(0);
                    BatchQueueOperator batchQueueOperator = SpringBeanFactoryUtil.getBean(BatchQueueOperator.class);
                    try {
                        JmsPayloadOperator jmsOperator = null;
                        String destination = (String) destinationNameCombo.getItemAt(destinationNameCombo.getSelectedIndex());
                        if (destination.equals(configuration.getQueueNames().getEms().getDestination())) {
                            jmsOperator = emsQueueSenderOperator;
                        } else if (destination.equals(configuration.getQueueNames().getBatch().getDestination())) {
                            jmsOperator = batchQueueOperator;
                        } else {
                            jmsOperator = jmsMsgPayloadOperator;
                        }
                        if (sendBtn.equals(e.getSource())) {
                            String message = messageSendToArea.getText();
                            if (StringUtils.isBlank(message)) {
                                String errorMessage = "請輸入發送訊息";
                                showErrorMessage(errorMessage);
                                GuiUtil.showErrorMessage(JmsPanel.this, "錯誤", errorMessage);
                                messageSendToArea.requestFocus();
                                return;
                            }
                            Object data = destinationNameCombo.getData();
                            if (data instanceof JmsPayload) {
                                JmsPayload jmsPayload = (JmsPayload) data;
                                jmsPayload.setPayload(message);
                                if (jmsPayload.getKind() == JmsKind.QUEUE)
                                    if (jmsPayload instanceof MsMessage) {
                                        ((MsMessage<String>) jmsPayload).setLabel("88888");
                                        if (message.contains("\n")) {
                                            String[] tmps = message.split("\n");
                                            ((MsMessage<String>) jmsPayload).setLabel(tmps[0]);
                                            jmsPayload.setPayload(tmps[1]);
                                        }
                                        jmsOperator.sendQueue(jmsPayload, null, null);
                                    } else {
                                        jmsOperator.sendQueue(jmsPayload, null, new JmsHandler() {
                                            @Override
                                            public void setPropertyOut(Message message) throws JMSException {
                                                JmsFactory.setCharacterSet(message, 1208);
                                                JmsFactory.setCorrelationID(message, "999999");
                                                JmsFactory.setMessageId(message, "我是MessageId");
                                            }
                                        });
                                    }
                                else if (jmsPayload.getKind() == JmsKind.TOPIC) {
                                    jmsOperator.publishTopic(jmsPayload, null, null);
                                }
                            } else if (data instanceof JmsKind) {
                                if (JmsKind.QUEUE.equals(data))
                                    jmsMsgSimpleOperator.sendQueue(destination, message, null, null);
                                else
                                    jmsMsgSimpleOperator.publishTopic(destination, message, null, null);
                            } else {
                                throw ExceptionUtil.createUnsupportedOperationException("Cannot handle data = [", data.getClass().getName(), "]");
                            }
                        } else if (e.getSource().equals(receiveStopChk)) {
                            if (receiveStopChk.isSelected()) {
                                jmsOperator.stopReceive(destination);
                            } else {
                                jmsOperator.startReceive(destination);
                            }
                        }
                    } catch (Exception ex) {
                        GuiUtil.showErrorMessage(JmsPanel.this, "錯誤", ex.getMessage());
                    }
                }
            }

            private class MyJmsReceiver<T extends Serializable> implements JmsReceiver<T> {
                @Override
                public void messageReceived(String destination, Serializable payload, Message message) {
                    messageRecvFromArea.setText(StringUtils.join("[", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_SSS), "]", payload));
                    try {
                        showMessage("收到", MessageFormat.format(Const.KEY_WORDS_IN_MESSAGE, destination), "訊息");
                        // logger.info("replyToQueueManagerName = " , ((MQQueue) message.getJMSReplyTo()).getBaseQueueManagerName());
                        // logger.info("replyToQueueName = " , ((MQQueue) message.getJMSReplyTo()).getBaseQueueName());
                        // logger.info("JMSTimestamp = " , FormatUtil.dateTimeFormat(JmsFactory.getTimestamp(message).getTimeInMillis()));

                        logger.info("JMSCorrelationID = ", JmsFactory.getCorrelationID(message));
                        logger.info("JMSMessageID = ", JmsFactory.getMessageId(message));

                        logger.info("JMSCorrelationID Bytes = ", StringUtil.toHex(JmsFactory.getCorrelationIDAsBytes(message)));
                        logger.info("JMSMessageID Bytes = ", StringUtil.toHex(JmsFactory.getMessageIdAsBytes(message)));

                        logger.info("JMSCorrelationID Hex = ", message.getJMSCorrelationID());
                        logger.info("JMSMessageID Hex = ", message.getJMSMessageID());
                        logger.info("JMSMessageID Hex 111111111111111111= ", message.getObjectProperty(JmsConstants.JMS_IBM_MQMD_MSGID));

                        logger.info("CharacterSet = ", JmsFactory.getCharacterSet(message));
                        logger.info("CharacterSet Integer = ", JmsFactory.getCharacterSetAsInt(message));
                    } catch (Exception e) {
                        logger.error(e, e.getMessage());
                    }
                }
            }
        }
    }
}