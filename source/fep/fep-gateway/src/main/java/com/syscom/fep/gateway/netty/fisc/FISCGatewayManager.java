package com.syscom.fep.gateway.netty.fisc;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.gateway.entity.ToFEPAPMode;
import com.syscom.fep.gateway.netty.NettyTransmissionClientMonitor;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiver;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSender;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.fisc.server.FISCGatewayServerConfiguration;
import com.syscom.fep.invoker.netty.impl.ToFEPFISCNettyClientConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.fisc")
@RefreshScope
public class FISCGatewayManager {
    /**
     * 訊息進到response queue若60秒未被收走, 自動drop掉
     */
    private long responseQueueExpiration = 60000L;
    /**
     * 交送Request或confirm交易至FEPAP方式, static固定只送第1台, dynamic代表2台輪流送
     */
    private ToFEPAPMode tofepapMode = ToFEPAPMode.STATIC;
    /**
     * 每次失敗後間隔多久後連另1台,單位為秒
     */
    private int tofepapRetryInterval = 3;
    /**
     * 最大重試次數, 若已達最大次數則不再retry, 直接sendEMS即可
     */
    private int tofepapRetryCount = 10;
    /**
     * 啟動一個onetime的timer, interval如下參數(單位為秒), 當時間到時, 再重設tofepapmode變數為config預設值
     */
    private int tofepapResetToFepApModeTimer = 300;
    @NestedConfigurationProperty
    private List<ToFEPFISCNettyClientConfiguration> tofepap = new ArrayList<>();
    private RoundRobin<ToFEPFISCNettyClientConfiguration> tofepapRoundRobin;
    @NestedConfigurationProperty
    private final FISCGatewayConfiguration primary = new FISCGatewayConfiguration();
    @NestedConfigurationProperty
    private final FISCGatewayConfiguration secondary = new FISCGatewayConfiguration();
    private final List<FISCGatewayGroup> primaryGatewayGroupList = new ArrayList<>();
    private final List<FISCGatewayGroup> secondaryGatewayGroupList = new ArrayList<>();

    public FISCGatewayConfiguration getPrimary() {
        return primary;
    }

    public FISCGatewayConfiguration getSecondary() {
        return secondary;
    }

    public long getResponseQueueExpiration() {
        return responseQueueExpiration;
    }

    public void setResponseQueueExpiration(long responseQueueExpiration) {
        this.responseQueueExpiration = responseQueueExpiration;
    }

    public ToFEPAPMode getTofepapMode() {
        return tofepapMode;
    }

    public void setTofepapMode(ToFEPAPMode tofepapMode) {
        this.tofepapMode = tofepapMode;
    }

    public int getTofepapRetryInterval() {
        return tofepapRetryInterval;
    }

    public void setTofepapRetryInterval(int tofepapRetryInterval) {
        this.tofepapRetryInterval = tofepapRetryInterval;
    }

    public int getTofepapRetryCount() {
        return tofepapRetryCount;
    }

    public void setTofepapRetryCount(int tofepapRetryCount) {
        this.tofepapRetryCount = tofepapRetryCount;
    }

    public int getTofepapResetToFepApModeTimer() {
        return tofepapResetToFepApModeTimer;
    }

    public void setTofepapResetToFepApModeTimer(int tofepapResetToFepApModeTimer) {
        this.tofepapResetToFepApModeTimer = tofepapResetToFepApModeTimer;
    }

    public List<ToFEPFISCNettyClientConfiguration> getTofepap() {
        return tofepap;
    }

    public RoundRobin<ToFEPFISCNettyClientConfiguration> getTofepapRoundRobin() {
        return tofepapRoundRobin;
    }

    @PostConstruct
    public void runGateway() {
        tofepapRoundRobin = new RoundRobin<>(this.tofepap);
        // 啟動時預設皆以Primary群組腳位連接財金
        runPrimaryGateway(true);
    }

    /**
     * 以Primary群組腳位連接財金
     *
     * @param postConstruct
     * @return
     */
    public boolean runPrimaryGateway(boolean postConstruct) {
        return this.runGateway(FISCGatewayMode.primary, primary, primaryGatewayGroupList, postConstruct);
    }

    /**
     * 以Secondary群組腳位連接財金
     *
     * @return
     */
    public boolean runSecondaryGateway(boolean postConstruct) {
        return this.runGateway(FISCGatewayMode.secondary, secondary, secondaryGatewayGroupList, postConstruct);
    }

    private boolean runGateway(FISCGatewayMode mode, FISCGatewayConfiguration configuration, List<FISCGatewayGroup> gatewayGroupList, boolean postConstruct) {
        synchronized (configuration.getConnState()) {
            if (configuration.getConnState().get() == NettyTransmissionConnState.CLIENT_RUNNING) {
                LogHelperFactory.getGeneralLogger().warn(mode.name(), " was running...");
                return false;
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_READY_TO_RUN);
            // 這裡要判斷一下, 避免重複添加到List
            if (gatewayGroupList.isEmpty()) {
                this.setConfigurationPropertiesPrefix(mode, configuration);
                for (int i = 0; i < configuration.getSender().size(); i++) {
                    FISCGatewayGroup gatewayGroup = new FISCGatewayGroup(
                            mode,
                            configuration.getSender().get(i),
                            configuration.getReceiver().get(i),
                            configuration.getFepap().get(i)
                    );
                    gatewayGroupList.add(gatewayGroup);
                    // postConstruct下會自動啟動Gateway程式
                    gatewayGroup.run(postConstruct);
                }
            } else {
                for (FISCGatewayGroup gatewayGroup : gatewayGroupList) {
                    // postConstruct下會自動啟動Gateway程式
                    gatewayGroup.run(postConstruct);
                }
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_RUNNING);
            return true;
        }
    }

    private void setConfigurationPropertiesPrefix(FISCGatewayMode mode, FISCGatewayConfiguration configuration) {
        List<FISCGatewayClientSenderConfiguration> senderConfigurationList = configuration.getSender();
        for (int i = 0; i < senderConfigurationList.size(); i++) {
            senderConfigurationList.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.fisc.", mode.name(), ".sender[", i, "]"));
        }
        List<FISCGatewayClientReceiverConfiguration> receiverConfigurationList = configuration.getReceiver();
        for (int i = 0; i < receiverConfigurationList.size(); i++) {
            receiverConfigurationList.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.fisc.", mode.name(), ".receiver[", i, "]"));
        }
        List<FISCGatewayServerConfiguration> serverConfigurationList = configuration.getFepap();
        for (int i = 0; i < serverConfigurationList.size(); i++) {
            serverConfigurationList.get(i).setConfigurationPropertiesPrefix(StringUtils.join("spring.fep.gateway.transmission.fisc.", mode.name(), ".fepap[", i, "]"));
        }
    }

    @PreDestroy
    public void terminate() {
        // 關閉程序時, 要stop所有的socket連線
        this.stopPrimaryGateway();
        this.stopSecondaryGateway();
    }

    /**
     * 斷開Primary群組腳位與財金的連線
     *
     * @return
     */
    public boolean stopPrimaryGateway() {
        return this.stopGateway(FISCGatewayMode.primary, primary, primaryGatewayGroupList);
    }

    /**
     * 斷開Secondary群組腳位與財金的連線
     *
     * @return
     */
    public boolean stopSecondaryGateway() {
        return this.stopGateway(FISCGatewayMode.secondary, secondary, secondaryGatewayGroupList);
    }

    public boolean stopGateway(FISCGatewayMode mode, FISCGatewayConfiguration configuration, List<FISCGatewayGroup> gatewayGroupList) {
        synchronized (configuration.getConnState()) {
            if (configuration.getConnState().get() == NettyTransmissionConnState.CLIENT_SHUT_DOWN) {
                LogHelperFactory.getGeneralLogger().warn(mode.name(), " was stopped...");
                return false;
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_SHUTTING_DOWN);
            for (FISCGatewayGroup gatewayGroup : gatewayGroupList) {
                gatewayGroup.stop();
            }
            configuration.setConnState(NettyTransmissionConnState.CLIENT_SHUT_DOWN);
            return true;
        }
    }

    public List<FISCGatewayGroup> getPrimaryGatewayGroupList() {
        return primaryGatewayGroupList;
    }

    public List<FISCGatewayGroup> getSecondaryGatewayGroupList() {
        return secondaryGatewayGroupList;
    }

    /**
     * checkStatus: 顯示目前各腳位的連線狀態, 如下:
     * primary FISC(B889A01I) 172.X.X.X:5001 Connected
     * primary FISC(B889A01O) 172.X.X.X:5002 Connected
     * 若有接手secondary線路(因heartbeat自動接手或曾下過start Secondary指令), 則顯示以下訊息, 若無 則不顯示
     * secondary FISC(B889A02I) 172.X.X.X:5003 Connected
     * secondary FISC(B889A02O) 172.X.X.X:5004 Connected
     *
     * @return
     */
    public String checkStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.checkStatus(FISCGatewayMode.primary, primary, primaryGatewayGroupList));
        sb.append(this.checkStatus(FISCGatewayMode.secondary, secondary, secondaryGatewayGroupList));
        return sb.toString();
    }

    private String checkStatus(FISCGatewayMode mode, FISCGatewayConfiguration configuration, List<FISCGatewayGroup> gatewayGroupList) {
        synchronized (configuration.getConnState()) {
            if (configuration.getConnState().get() != NettyTransmissionConnState.CLIENT_RUNNING) {
                return StringUtils.EMPTY;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(gatewayGroupList)) {
            for (FISCGatewayGroup group : gatewayGroupList) {
                FISCGatewayClientSender sender = group.getSender();
                if (sender != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientSenderConfiguration> senderMonitorData = sender.getTransmissionClientMonitor();
                    sb.append(mode.name()).append(StringUtils.SPACE)
                            .append("FISC(").append(sender.getConfiguration().getClientId()).append(")").append(StringUtils.SPACE)
                            .append(senderMonitorData.getRemote()).append(StringUtils.SPACE)
                            .append(NettyTransmissionConnState.isClientConnected(senderMonitorData.getConnState()) ? "Connected" : "Disconnected")
                            .append("\r\n");
                }
                FISCGatewayClientReceiver receiver = group.getReceiver();
                if (receiver != null) {
                    NettyTransmissionClientMonitor<FISCGatewayClientReceiverConfiguration> receiverMonitorData = receiver.getTransmissionClientMonitor();
                    sb.append(mode.name()).append(StringUtils.SPACE)
                            .append("FISC(").append(receiver.getConfiguration().getClientId()).append(")").append(StringUtils.SPACE)
                            .append(receiverMonitorData.getRemote()).append(StringUtils.SPACE)
                            .append(NettyTransmissionConnState.isClientConnected(receiverMonitorData.getConnState()) ? "Connected" : "Disconnected")
                            .append("\r\n");
                }
            }
        }
        return sb.toString();
    }
}