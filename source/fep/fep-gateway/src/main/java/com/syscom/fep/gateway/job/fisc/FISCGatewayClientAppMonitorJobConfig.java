package com.syscom.fep.gateway.job.fisc;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.gateway.job.fisc.app-monitor")
@RefreshScope
public class FISCGatewayClientAppMonitorJobConfig extends SchedulerJobConfig {
    private String monitorUrl;
    private boolean recordHttpLog = false;

    public String getMonitorUrl() {
        return monitorUrl;
    }

    public void setMonitorUrl(String monitorUrl) {
        this.monitorUrl = monitorUrl;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }
}
