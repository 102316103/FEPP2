package com.syscom.fep.scheduler.job.impl;

import com.google.gson.Gson;
import com.syscom.fep.common.monitor.MonitorData;
import com.syscom.fep.common.monitor.MonitorDataCollector;
import com.syscom.fep.common.monitor.MonitorDataConstant;
import com.syscom.fep.common.monitor.MonitorDataDisk;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.scheduler.job.SchedulerJob;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.http.MediaType;

import javax.annotation.PreDestroy;
import java.util.Calendar;
import java.util.List;

public class SystemMonitorJob extends SchedulerJob<SystemMonitorJobConfig> implements MonitorDataConstant {
    private final HttpClient httpClient = new HttpClient(this.getJobConfig().isRecordHttpLog());

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, SystemMonitorJobConfig config) throws Exception {
        MonitorData system = createSystemMonitorData(config);
        system.setSmsServicestate("1");
        system.setSmsCpu(this.fetchSystemCpuUsage(config));
        system.setSmsRam(this.fetchSystemMemoryUsage());
        system.setSmsOthers(this.fetchSystemHardDisk(config));
        sendMonitorMessage(config, system);
    }

    /**
     * 任務停止的時候, 將狀態改為0
     */
    @PreDestroy
    public void terminateJob() {
        MonitorData system = createSystemMonitorData(config);
        system.setSmsServicestate("0");
        sendMonitorMessage(config, system);
    }

    /**
     * 系統狀態---CPU
     *
     * @param config
     * @return
     */
    private int fetchSystemCpuUsage(SystemMonitorJobConfig config) {
        int cpu = 0;
        try {
            cpu = MonitorDataCollector.fetchSystemCpuUsage(httpClient, config.getServiceUrl()).intValue();
        } catch (Throwable e) {
            warn(ProgramName, " fetchSystemCpuUsage with exception occur, ", e.getMessage());
        }
        return cpu;
    }

    /**
     * 系統狀態---RMA
     *
     * @return
     */
    private int fetchSystemMemoryUsage() {
        int memo = 0;
        try {
            memo = MonitorDataCollector.fetchSystemMemoryUsage().intValue();
        } catch (Throwable e) {
            warn(ProgramName, " fetchSystemMemoryUsage with exception occur, ", e.getMessage());
        }
        return memo;
    }

    /**
     * 系統DISK USED
     *
     * @param config
     * @return
     */
    private String fetchSystemHardDisk(SystemMonitorJobConfig config) {
        String disk = StringUtils.EMPTY;
        try {
            List<MonitorDataDisk> monitorDataDiskList = MonitorDataCollector.fetchSystemHardDisk(config.getSystemHostName(), config.getSystemHostIp());
            if (!monitorDataDiskList.isEmpty()) {
                disk = new Gson().toJson(monitorDataDiskList);
            }
        } catch (Throwable e) {
            warn(ProgramName, " fetchSystemHardDisk with exception occur, ", e.getMessage());
        }
        return disk;
    }

    private MonitorData createSystemMonitorData(SystemMonitorJobConfig config) {
        MonitorData system = new MonitorData();
        system.setSmsServicename(SERVICE_NAME_SYSTEM);
        system.setSmsServiceip(config.getSystemHostIp());
        system.setSmsHostname(config.getSystemHostName());
        system.setSmsUpdatetime(Calendar.getInstance().getTime());
        system.setSmsServicestate("0");
        system.setSmsCpu(0);
        system.setSmsRam(0);
        system.setSmsOthers(StringUtils.EMPTY);
        system.setSmsCpuThreshold(0);
        system.setSmsRamThreshold(0);
        system.setSmsThreads(0);
        system.setSmsThreadsActive(0);
        system.setSmsThreadsThreshold(0);
        return system;
    }

    private void sendMonitorMessage(SystemMonitorJobConfig config, MonitorData system) {
        boolean postFailed = false;
        String primaryUrl = config.getMonitorPrimaryUrl();
        if (StringUtils.isNotBlank(primaryUrl)) {
            try {
                httpClient.doPost(primaryUrl, MediaType.APPLICATION_JSON, system, false);
            } catch (Exception e) {
                postFailed = true;
                warn("send System Monitor Data to primaryUrl = [", primaryUrl, "] failed with exception occur = [", e.getMessage(), "]");
            }
        } else {
            warn("cannot send System Monitor Data, primaryUrl is blank!!!");
        }
        if (postFailed) {
            postFailed = false;
            String secondaryUrl = config.getMonitorSecondaryUrl();
            if (StringUtils.isNotBlank(secondaryUrl)) {
                try {
                    httpClient.doPost(secondaryUrl, MediaType.APPLICATION_JSON, system, false);
                } catch (Exception e) {
                    postFailed = true;
                    warn("send System Monitor Data to secondaryUrl = [", secondaryUrl, "] failed with exception occur = [", e.getMessage(), "]");
                }
            } else {
                warn("cannot send System Monitor Data, secondaryUrl is blank!!!");
            }
        }
    }

    private void warn(Object... msgs) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(msgs);
        }
    }
}
