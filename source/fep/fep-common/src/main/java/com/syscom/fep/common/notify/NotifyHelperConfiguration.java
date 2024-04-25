package com.syscom.fep.common.notify;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = NotifyHelperConstant.CONFIGURATION_PROPERTIES_PREFIX)
@ConditionalOnProperty(prefix = NotifyHelperConstant.CONFIGURATION_PROPERTIES_PREFIX, name = {NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_SEND_NOTIFY, NotifyHelperConstant.CONFIGURATION_PROPERTIES_URL_LOG_NOTIFY})
@RefreshScope
public class NotifyHelperConfiguration {
    /**
     * Notify服務提供的URL
     */
    private NotifyHelperConfigurationURL url;
    /**
     * 超時時間, 秒
     */
    private int timeout = 30;
    /**
     * 是否記錄HTTP Log
     */
    private boolean recordHttpLog = true;

    public NotifyHelperConfigurationURL getUrl() {
        return url;
    }

    public void setUrl(NotifyHelperConfigurationURL url) {
        this.url = url;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "NotifyHelper Configuration"));
    }
}
