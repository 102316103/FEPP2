package com.syscom.fep.scheduler.job.impl;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.scheduler.job.system-monitor")
@RefreshScope
public class SystemMonitorJobConfig extends SchedulerJobConfig {
    private String systemName;
    private String systemHostName;
    private String systemHostIp;
    private String serviceUrl;
    private String monitorPrimaryUrl;
    private String monitorSecondaryUrl;
    private boolean recordHttpLog = false;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemHostName() {
        return systemHostName;
    }

    public void setSystemHostName(String systemHostName) {
        this.systemHostName = systemHostName;
    }

    public String getSystemHostIp() {
        return systemHostIp;
    }

    public void setSystemHostIp(String systemHostIp) {
        this.systemHostIp = systemHostIp;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getMonitorPrimaryUrl() {
        return monitorPrimaryUrl;
    }

    public void setMonitorPrimaryUrl(String monitorPrimaryUrl) {
        this.monitorPrimaryUrl = monitorPrimaryUrl;
    }

    public String getMonitorSecondaryUrl() {
        return monitorSecondaryUrl;
    }

    public void setMonitorSecondaryUrl(String monitorSecondaryUrl) {
        this.monitorSecondaryUrl = monitorSecondaryUrl;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }
}
