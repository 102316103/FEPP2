package com.syscom.fep.service.ems.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;

@RefreshScope
public class EMSServiceConfiguration {
    @Value("${spring.fep.service.ems.resetNotifyInterval:3}")
    private int resetNotifyInterval;
    @Value("${spring.fep.service.ems.repeatInterval:3}")
    private int repeatInterval;
    @Value("#{'${spring.fep.service.ems.mailList:}'.split(',')}")
    private String[] mailList;
    @Value("#{'${spring.fep.service.ems.mailListNps2262:}'.split(',')}")
    private String[] mailListNps2262;
    @Value("${spring.fep.service.ems.mailSender:}")
    private String mailSender;
    @Value("${spring.fep.service.ems.warningPattern:無法取得ENCLib;RC:10,;RC:11,;RC:17,;RC:81,;RC:82,;RC:83,;RC:84,;RC:94,;RC:95,;RC:98,;RC:99,}")
    private String warningPattern;
    @Value("#{'${spring.fep.service.ems.exclude.messageId:}'.split(',')}")
    private List<String> excludeMessageIdList;
    @Value("${spring.fep.service.ems.flushStatementsTotal:1000}")
    private int flushStatementsTotal;

    public int getResetNotifyInterval() {
        return resetNotifyInterval;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public String[] getMailList() {
        return mailList;
    }

    public String[] getMailListNps2262() {
        return mailListNps2262;
    }

    public String getMailSender() {
        return mailSender;
    }

    public String getWarningPattern() {
        return warningPattern;
    }

    public List<String> getExcludeMessageIdList() {
        return excludeMessageIdList;
    }

    public int getFlushStatementsTotal() {
        return flushStatementsTotal;
    }

    public void setFlushStatementsTotal(int flushStatementsTotal) {
        this.flushStatementsTotal = flushStatementsTotal;
    }

    @PostConstruct
    public void print() {
        Field[] fields = EMSServiceConfiguration.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("Service EMS Configuration:\r\n");
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                Value annotation = field.getAnnotation(Value.class);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat));
                if (annotation != null) {
                    sb.append(annotation.value().substring(annotation.value().indexOf("${") + 2, annotation.value().contains(":") ? annotation.value().indexOf(":") : annotation.value().length() - 1));
                } else {
                    ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
                    if (annotation2 != null) {
                        String prefix = annotation2.prefix();
                        if (StringUtils.isNotBlank(prefix)) {
                            sb.append(prefix).append(".");
                        }
                    }
                    sb.append(field.getName());
                }
                sb.append(" = ");
                Object value = ReflectionUtils.getField(field, this);
                if (value instanceof List) {
                    sb.append(StringUtils.join((List<?>) value, ','));
                } else if (value instanceof String[]) {
                    sb.append(StringUtils.join((String[]) value, ','));
                } else {
                    sb.append(value);
                }
                sb.append("\r\n");
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }
}