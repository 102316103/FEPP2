package com.syscom.fep.scheduler.job.impl;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.scheduler.job.app-monitor")
@RefreshScope
public class AppMonitorJobConfig extends SchedulerJobConfig {
    private String serviceName;
    private String serviceHostIp;
    private String serviceHostName;
    private String serviceUrl;
    private String monitorPrimaryUrl;
    private String monitorSecondaryUrl;
    private boolean recordHttpLog = false;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceHostIp() {
        return serviceHostIp;
    }

    public void setServiceHostIp(String serviceHostIp) {
        this.serviceHostIp = serviceHostIp;
    }

    public String getServiceHostName() {
        return serviceHostName;
    }

    public void setServiceHostName(String serviceHostName) {
        this.serviceHostName = serviceHostName;
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
