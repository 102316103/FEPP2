package com.syscom.fep.common.mail;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.fep")
@RefreshScope
public class MailConfiguration {
    @NestedConfigurationProperty
    private MailSmtp mail = new MailSmtp();

    public void setMail(MailSmtp mail) {
        this.mail = mail;
    }

    public MailSmtp getMail() {
        return mail;
    }

    @PostConstruct
    public void print() {
        String prefix = null;
        ConfigurationProperties annotation2 = MailConfiguration.class.getAnnotation(ConfigurationProperties.class);
        if (annotation2 != null) {
            prefix = annotation2.prefix();
            if (StringUtils.isBlank(prefix)) {
                prefix = null;
            }
        }
        Field[] fields = MailConfiguration.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("Mail Configuration:\r\n");
            for (Field field : fields) {
                ReflectionUtils.makeAccessible(field);
                Object object = ReflectionUtils.getField(field, this);
                print(object, StringUtils.join(prefix, ".", field.getName()), sb);
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }

    private void print(Object object, String prefix, StringBuilder sb) {
        if (object == null) return;
        int repeat = 2;
        if ("java.lang".equals(object.getClass().getPackage().getName())) {
            sb.append(StringUtils.repeat(StringUtils.SPACE, 2))
                    .append(prefix)
                    .append(" = ")
                    .append(object)
                    .append("\r\n");
        } else {
            Field[] fields = object.getClass().getDeclaredFields();
            if (ArrayUtils.isNotEmpty(fields)) {
                for (Field field : fields) {
                    ReflectionUtils.makeAccessible(field);
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(prefix)
                            .append(".")
                            .append(field.getName()).append(" = ")
                            .append(ReflectionUtils.getField(field, object))
                            .append("\r\n");
                }
            }
        }
    }
}
