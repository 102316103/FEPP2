package com.syscom.fep.gateway.job.atm;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.netty.NettyTransmissionServerMonitor;
import com.syscom.fep.gateway.netty.atm.ATMGatewayServer;
import com.syscom.fep.gateway.netty.atm.ATMGatewayServerConfiguration;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.vo.monitor.ServerNetworkStatus;
import org.quartz.JobExecutionContext;
import org.springframework.http.MediaType;

import javax.annotation.PreDestroy;
import java.util.Collections;

/**
 * 定時送網絡服務端監控資料給APPMon Service
 */
public class ATMGatewayServerAppMonitorJob extends SchedulerJob<ATMGatewayServerAppMonitorJobConfig> {
    private HttpClient httpClient = new HttpClient(this.getJobConfig().isRecordHttpLog()); // 避免log太多, 這裡設置false不記錄http的log
    private static ServerNetworkStatus serverNetworkStatus = new ServerNetworkStatus();

    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.ATMGW.name());
    }

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, ATMGatewayServerAppMonitorJobConfig config) throws Exception {
        try {
            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class, false);
            if (atmGatewayServer == null) return;
            NettyTransmissionServerMonitor<ATMGatewayServerConfiguration> monitorData = atmGatewayServer.getTransmissionServerMonitor();
            serverNetworkStatus.setServiceHostName(FEPConfig.getInstance().getHostName());
            serverNetworkStatus.setServiceName(monitorData.getServiceName());
            serverNetworkStatus.setServiceIP(monitorData.getHostIp());
            serverNetworkStatus.setServicePort(Integer.toString(monitorData.getHostPort()));
            serverNetworkStatus.setSocketCount(Long.toString(monitorData.getConnections()));
            serverNetworkStatus.setServiceState("1");
            httpClient.doPost(config.getMonitorUrl(), MediaType.APPLICATION_JSON, Collections.singletonList(serverNetworkStatus), false);
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
        }
    }

    @PreDestroy
    public void terminateJob() {
        try {
            serverNetworkStatus.setSocketCount(Long.toString(0L));
            serverNetworkStatus.setServiceState("0");
            httpClient.doPost(this.getJobConfig().getMonitorUrl(), MediaType.APPLICATION_JSON, Collections.singletonList(serverNetworkStatus), false);
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
        }
    }

    private void warn(Object... msgs) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(msgs);
        }
    }
}
