package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.fisc.keepalive.client")
@RefreshScope
public class FISCGatewayKeepAliveClientConfiguration extends NettyTransmissionClientConfiguration {
    /**
     * 啟動後經過多久(單位為秒)開始發第一道keepalive電文,若未設代表不做keepalive功能
     */
    private int start;
    /**
     * keepalive開始後每隔多久發一次,單位為秒
     */
    private int interval = 5;
    /**
     * keepalive超時等待回應時間, 單位為秒
     */
    private int timeout = 5;
    /**
     * 是否記錄log
     */
    private boolean logging = false;

    @PostConstruct
    public void initReceiver() {
        super.setGateway(Gateway.FISCGW);
        super.setSocketType(SocketType.Client);
    }

    /**
     * @return
     */
    @Override
    public final Gateway getGateway() {
        return super.getGateway();
    }

    /**
     * @param gateway
     */
    @Override
    public final void setGateway(Gateway gateway) {}

    /**
     * @return
     */
    @Override
    public final SocketType getSocketType() {
        return super.getSocketType();
    }

    /**
     * @param socketType
     */
    @Override
    public final void setSocketType(SocketType socketType) {}

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }
}