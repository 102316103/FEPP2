package com.syscom.fep.tools.mq;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMD;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.headers.CCSID;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

public class MQUtil {
    private static final LogHelper logger = new LogHelper();

    private MQUtil() {}

    public static MQQueueManager createMQQueueManager(String hostname, int port, String queueManagerName, String channel, String userID, String password) throws MQException {
        Hashtable<String, Object> mqht = new Hashtable<>();
        mqht.put(CMQC.CHANNEL_PROPERTY, channel);
        mqht.put(CMQC.HOST_NAME_PROPERTY, hostname);
        mqht.put(CMQC.PORT_PROPERTY, port);
        if (StringUtils.isNotEmpty(userID)) {
            mqht.put(CMQC.USER_ID_PROPERTY, userID);
        } else {
            logger.warn("no specific userID for hostname = [", hostname, "], port = [", port, "], queueManagerName = [", queueManagerName, "], channel = [", channel, "]");
        }
        if (StringUtils.isNotEmpty(password)) {
            mqht.put(CMQC.PASSWORD_PROPERTY, password);
        } else {
            logger.warn("no specific password for hostname = [", hostname, "], port = [", port, "], queueManagerName = [", queueManagerName, "], channel = [", channel, "], userID = [", userID, "]");
        }
        return new MQQueueManager(queueManagerName, mqht);
    }

    public static void setProperty(MQInstance instance, MQProperties properties, MQMessage mqMessage) {
        String key, value;
        Field[] fields = MQMD.class.getFields();
        for (Field field : fields) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            key = field.getName();
            if (properties.containsKey(instance, key)) {
                value = properties.getProperty(instance, key);
                if (field.getType().equals(String.class)) {
                    ReflectUtil.setFieldValue(mqMessage, field.getName(), value);
                    logger.info("[setProperty][", instance, "] mqMessage.", field.getName(), " = ", value);
                } else if (field.getType().equals(int.class)) {
                    ReflectUtil.setFieldValue(mqMessage, field.getName(), Integer.parseInt(value));
                    logger.info("[setProperty][", instance, "] mqMessage.", field.getName(), " = ", value);
                } else if (field.getType().equals(byte[].class)) {
                    byte[] bytes = null;
                    try {
                        bytes = value.getBytes(MQUtil.getCharsetName(instance, properties));
                    } catch (Exception e) {
                        bytes = value.getBytes();
                    }
                    ReflectUtil.setFieldValue(mqMessage, field.getName(), bytes);
                    logger.info("[setProperty][", instance, "] mqMessage.", field.getName(), " = [", StringUtils.join(bytes, ','), "]");
                } else if (field.getType().equals(GregorianCalendar.class)) {
                    Calendar calendar = CalendarUtil.parseDateTimeValue(Long.parseLong(value));
                    ReflectUtil.setFieldValue(mqMessage, field.getName(), calendar);
                    logger.info("[setProperty][", instance, "] mqMessage.", field.getName(), " = ", FormatUtil.dateTimeInMillisFormat(calendar.getTime()));
                } else if (field.getType().equals(CodingErrorAction.class)) {
                    switch (value) {
                        case "IGNORE":
                            ReflectUtil.setFieldValue(mqMessage, field.getName(), CodingErrorAction.IGNORE);
                            break;
                        case "REPLACE":
                            ReflectUtil.setFieldValue(mqMessage, field.getName(), CodingErrorAction.REPLACE);
                            break;
                        case "REPORT":
                            ReflectUtil.setFieldValue(mqMessage, field.getName(), CodingErrorAction.REPORT);
                            break;
                    }
                    logger.info("[setProperty][", instance, "] mqMessage.", field.getName(), " = CodingErrorAction.", value);
                }
            }
        }
    }

    public static String getCharsetName(MQInstance instance, MQProperties properties) {
        final String key = "characterSet";
        int characterSet = 1208;
        if (properties.containsKey(instance, key)) {
            characterSet = Integer.parseInt(properties.getProperty(instance, key));
        }
        try {
            return CCSID.getCodepage(characterSet);
        } catch (NumberFormatException | UnsupportedEncodingException e) {
            return StandardCharsets.UTF_8.name();
        }
    }
}
