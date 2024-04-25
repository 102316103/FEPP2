package com.syscom.fep.service.monitor.job;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.service.monitor.svr.MonitorSchedulerService;
import com.syscom.fep.service.monitor.vo.MonitorHeartbeat;
import com.syscom.fep.vo.monitor.MonitorConstant;
import org.quartz.JobExecutionContext;
import org.springframework.http.MediaType;

/**
 * 定時送Heartbeat給另一台AppMon Service
 */
public class MonitorHeartbeatJob extends SchedulerJob<MonitorHeartbeatJobConfig> implements MonitorConstant {
    private final HttpClient httpClient = new HttpClient(this.getJobConfig().isRecordHttpLog()); // 避免log太多, 這裡設置false不記錄http的log

    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, MonitorHeartbeatJobConfig config) throws Exception {
        MonitorHeartbeat heartbeat = new MonitorHeartbeat();
        heartbeat.setFromHostName(FEPConfig.getInstance().getHostName());
        heartbeat.setFromHostIp(FEPConfig.getInstance().getHostIp());
        heartbeat.setFromAppName(FEPConfig.getInstance().getApplicationName());
        String remoteAppMonUrl = config.getRemoteAppMonUrl();
        try {
            httpClient.doPost(remoteAppMonUrl, MediaType.APPLICATION_JSON, heartbeat, false);
            processAfterSucceed();
        } catch (Exception e) {
            warn("send Heartbeat to remoteAppMonUrl = [", remoteAppMonUrl, "], failed with exception occur = [", e.getMessage(), "]");
            processAfterFailed();
        }
    }

    /**
     * ping成功, 則清除本機獲取的遠程suip和HSM的監控數據
     */
    private void processAfterSucceed() {
        MonitorSchedulerService service = SpringBeanFactoryUtil.getBean(MonitorSchedulerService.class, false);
        if (service == null) return;
        // 清除遠程SUIP以及HSM的監控數據
        service.clearRemoteSuipHsmMonitorData();
    }

    /**
     * ping失敗, 則獲取遠程suip和HSM的監控數據
     */
    private void processAfterFailed() {
        MonitorSchedulerService service = SpringBeanFactoryUtil.getBean(MonitorSchedulerService.class, false);
        if (service == null) return;
        // 取遠程SUIP以及HSM的監控數據
        service.fetchRemoteSuipHsmMonitorData();
    }

    private void warn(Object... msgs) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(msgs);
        }
    }
}
