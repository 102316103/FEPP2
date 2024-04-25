package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.annotation.IgnoreSerial;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.ssl.SslKeyTrust;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import nu.studer.java.util.OrderedProperties;
import nu.studer.java.util.OrderedProperties.OrderedPropertiesBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class NettyTransmissionServerConfiguration extends NettyTransmissionConfiguration {
    /**
     * BOSS線程數, 一般是有幾個protocol port設定幾個
     */
    private int bossThreadNum = 1;
    private long readerIdleTime = 120000L;
    private long writerIdleTime = 0L;
    private boolean sslBackup = true;
    private List<SslKeyTrust> sslConfigs;

    public int getBossThreadNum() {
        return bossThreadNum;
    }

    public void setBossThreadNum(int bossThreadNum) {
        this.bossThreadNum = bossThreadNum;
    }

    public long getReaderIdleTime() {
        return readerIdleTime;
    }

    public void setReaderIdleTime(long readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
    }

    public long getWriterIdleTime() {
        return writerIdleTime;
    }

    public void setWriterIdleTime(long writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
    }

    public boolean isSslBackup() {
        return sslBackup;
    }

    public void setSslBackup(boolean sslBackup) {
        this.sslBackup = sslBackup;
    }

    public List<SslKeyTrust> getSslConfigs() {
        return sslConfigs;
    }

    public void setSslConfigs(List<SslKeyTrust> sslConfigs) {
        this.sslConfigs = Collections.synchronizedList(sslConfigs);
        setSslConfigsIndex();
    }

    public void setSslConfigsIndex() {
        if (CollectionUtils.isNotEmpty(this.sslConfigs)) {
            for (int i = 0; i < this.sslConfigs.size(); i++) {
                this.sslConfigs.get(i).setIndex(i);
            }
        }
    }

    public boolean storeSslConfigs(File file) throws Exception {
        if (file == null || file.isDirectory()) {
            return false;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        } else {
            if (file.exists()) {
                if (sslBackup) {
                    File backup = new File(StringUtils.join(file.getAbsolutePath(), GatewayCodeConstant.EXTENSION_BAK));
                    FileUtils.copyFile(file, backup);
                }
                file.delete();
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            String prefix = StringUtils.EMPTY;
            ConfigurationProperties annotation = this.getClass().getAnnotation(ConfigurationProperties.class);
            if (annotation != null) {
                prefix = annotation.prefix();
            }
            OrderedPropertiesBuilder builder = new OrderedPropertiesBuilder();
            builder.withSuppressDateInComment(true);
            OrderedProperties properties = builder.build();
            for (int i = 0; i < this.sslConfigs.size(); i++) {
                SslKeyTrust sslKeyTrust = this.sslConfigs.get(i);
                List<Field> fieldList = ReflectUtil.getAllFields(sslKeyTrust);
                for (Field field : fieldList) {
                    IgnoreSerial ignoreSerial = field.getAnnotation(IgnoreSerial.class);
                    if (ignoreSerial != null) {
                        continue;
                    }
                    ReflectionUtils.makeAccessible(field);
                    Object value = ReflectionUtils.getField(field, sslKeyTrust);
                    if (value != null) {
                        String key = StringUtils.join(prefix, ".sslConfigs[", i, "].", field.getName());
                        if ("sslKeySscode".equals(field.getName()) || "sslTrustSscode".equals(field.getName())) {
                            properties.setProperty(key, Jasypt.encrypt(value.toString(), true));
                        } else {
                            properties.setProperty(key, value.toString());
                        }
                    }
                }
            }
            properties.store(writer, null);
            return true;
        } catch (Exception e) {
            throw ExceptionUtil.createException(e, "store SslConfigs failed!!!");
        }
    }
}
