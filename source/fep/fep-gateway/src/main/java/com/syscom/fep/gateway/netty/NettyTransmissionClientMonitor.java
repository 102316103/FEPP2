package com.syscom.fep.gateway.netty;

import org.apache.commons.lang3.StringUtils;

public class NettyTransmissionClientMonitor<Configuration extends NettyTransmissionConfiguration> {
    private Configuration configuration;
    // 服務名稱
    private String serviceName;
    // 本機連接埠
    private String local = StringUtils.EMPTY;
    // 遠端連接埠
    private String remote = StringUtils.EMPTY;
    // 連線狀態
    private NettyTransmissionConnState nettyTransmissionConnState = NettyTransmissionConnState.CLIENT_DISCONNECTED;
    // 目前連接數
    private long connections;

    public void setTransmissionConfiguration(Configuration configuration) {
        this.configuration = configuration;
        this.serviceName = StringUtils.join(configuration.getGateway().name(),
                configuration.getSocketType() != null ? StringUtils.join("_", configuration.getSocketType().name()) : StringUtils.EMPTY);
        this.remote = StringUtils.join(configuration.getHost(), ":", configuration.getPort());
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public NettyTransmissionConnState getConnState() {
        return nettyTransmissionConnState;
    }

    public void setConnState(NettyTransmissionConnState nettyTransmissionConnState) {
        this.nettyTransmissionConnState = nettyTransmissionConnState;
    }

    public long getConnections() {
        return connections;
    }

    public void setConnections(long connections) {
        this.connections = connections;
    }
}
