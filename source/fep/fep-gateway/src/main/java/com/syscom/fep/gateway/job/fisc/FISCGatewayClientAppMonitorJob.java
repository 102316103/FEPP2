package com.syscom.fep.gateway.job.fisc;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.netty.NettyTransmissionClientMonitor;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayGroup;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayManager;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClient;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiver;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSender;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderConfiguration;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.vo.monitor.ClientNetworkStatus;
import com.syscom.fep.vo.monitor.MonitorConstant;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.http.MediaType;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * 定時送網絡服務端監控資料給APPMon Service
 */
public class FISCGatewayClientAppMonitorJob extends SchedulerJob<FISCGatewayClientAppMonitorJobConfig> implements MonitorConstant {
    private final HttpClient httpClient = new HttpClient(this.getJobConfig().isRecordHttpLog()); // 避免log太多, 這裡設置false不記錄http的log
    private static final List<ClientNetworkStatus> clientNetworkStatusList = new ArrayList<>();

    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.FISCGW.name());
    }

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, FISCGatewayClientAppMonitorJobConfig config) throws Exception {
        try {
            FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class, false);
            if (manager != null) {
                // 先重置所有數據的狀態, 下面再重新塞入
                for (ClientNetworkStatus status : clientNetworkStatusList) {
                    status.setLocalEndPoint(StringUtils.EMPTY);
                    status.setSocketCount(Long.toString(0L));
                    status.setServiceState("0");
                    status.setState(NET_CLIENT_STATE_DISCONNECT);
                }
                this.fetchMonitorData("Primary", manager.getPrimaryGatewayGroupList());
                this.fetchMonitorData("Secondary", manager.getSecondaryGatewayGroupList());
                httpClient.doPost(config.getMonitorUrl(), MediaType.APPLICATION_JSON, clientNetworkStatusList, false);
            }
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
        }
    }

    @PreDestroy
    public void terminateJob() {
        try {
            for (ClientNetworkStatus status : clientNetworkStatusList) {
                status.setLocalEndPoint(StringUtils.EMPTY);
                status.setSocketCount(Long.toString(0L));
                status.setServiceState("0");
                status.setState(NET_CLIENT_STATE_DISCONNECT);
            }
            httpClient.doPost(this.getJobConfig().getMonitorUrl(), MediaType.APPLICATION_JSON, clientNetworkStatusList, false);
        } catch (Exception e) {
            warn("send App Monitor Network Data failed with exception occur = [", e.getMessage(), "]");
        }
    }

    private void fetchMonitorData(String type, List<FISCGatewayGroup> gatewayGroupList) {
        if (!gatewayGroupList.isEmpty()) {
            for (int i = 0; i < gatewayGroupList.size(); i++) {
                FISCGatewayGroup group = gatewayGroupList.get(i);
                // receiver
                FISCGatewayClientReceiver receiver = group.getReceiver();
                if (receiver != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientReceiverConfiguration> receiverMonitorData = receiver.getTransmissionClientMonitor();
                    String serviceName = this.getServiceName(type, receiverMonitorData.getConfiguration());
                    ClientNetworkStatus receiverClientNetworkStatus = clientNetworkStatusList.stream().filter(t -> t.getServiceName().equals(serviceName)).findFirst().orElse(null);
                    if (receiverClientNetworkStatus == null) {
                        receiverClientNetworkStatus = new ClientNetworkStatus();
                        clientNetworkStatusList.add(receiverClientNetworkStatus);
                    }
                    receiverClientNetworkStatus.setServiceHostName(FEPConfig.getInstance().getHostName());
                    receiverClientNetworkStatus.setServiceIP(FEPConfig.getInstance().getHostIp());
                    receiverClientNetworkStatus.setServiceName(serviceName);
                    receiverClientNetworkStatus.setLocalEndPoint(receiverMonitorData.getLocal());
                    receiverClientNetworkStatus.setRemoteEndPoint(receiverMonitorData.getRemote());
                    receiverClientNetworkStatus.setSocketCount(Long.toString(receiverMonitorData.getConnections()));
                    receiverClientNetworkStatus.setState(NettyTransmissionConnState.isClientConnected(receiverMonitorData.getConnState()) ? NET_CLIENT_STATE_CONNECT : NET_CLIENT_STATE_DISCONNECT);
                    receiverClientNetworkStatus.setServiceState("1");
                }
                // sender
                FISCGatewayClientSender sender = group.getSender();
                if (sender != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientSenderConfiguration> senderMonitorData = sender.getTransmissionClientMonitor();
                    String serviceName = this.getServiceName(type, senderMonitorData.getConfiguration());
                    ClientNetworkStatus senderClientNetworkStatus = clientNetworkStatusList.stream().filter(t -> t.getServiceName().equals(serviceName)).findFirst().orElse(null);
                    if (senderClientNetworkStatus == null) {
                        senderClientNetworkStatus = new ClientNetworkStatus();
                        clientNetworkStatusList.add(senderClientNetworkStatus);
                    }
                    senderClientNetworkStatus.setServiceHostName(FEPConfig.getInstance().getHostName());
                    senderClientNetworkStatus.setServiceIP(FEPConfig.getInstance().getHostIp());
                    senderClientNetworkStatus.setServiceName(serviceName);
                    senderClientNetworkStatus.setLocalEndPoint(senderMonitorData.getLocal());
                    senderClientNetworkStatus.setRemoteEndPoint(senderMonitorData.getRemote());
                    senderClientNetworkStatus.setSocketCount(Long.toString(senderMonitorData.getConnections()));
                    senderClientNetworkStatus.setState(NettyTransmissionConnState.isClientConnected(senderMonitorData.getConnState()) ? NET_CLIENT_STATE_CONNECT : NET_CLIENT_STATE_DISCONNECT);
                    senderClientNetworkStatus.setServiceState("1");
                }
            }
        }
    }

    private String getServiceName(String type, FISCGatewayClientConfiguration configuration) {
        return StringUtils.join(type, StringUtils.SPACE, configuration.getSocketType().name(), "(", configuration.getClientId(), ")");
    }

    private void warn(Object... messages) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(messages);
        }
    }
}
