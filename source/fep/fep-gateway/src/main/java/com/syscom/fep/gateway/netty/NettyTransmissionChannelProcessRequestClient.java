package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.vo.LogData;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;

public abstract class NettyTransmissionChannelProcessRequestClient<Configuration extends NettyTransmissionClientConfiguration> extends NettyTransmissionChannelProcessRequest<Configuration> {
    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    @Override
    public void initialization(Configuration configuration) {
        super.initialization(configuration);
        this.initNettyChannelProcessRequest();
    }

    @PostConstruct
    public void initNettyChannelProcessRequest() {
        this.notification.addConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    public void closeConnection() {
        putMDC();
        this.notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), this.channelHandlerContext.channel(), NettyTransmissionConnState.CLIENT_DISCONNECTED);
        this.notification.removeConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state) {
        super.connStateChanged(channel, state);
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
        if (state == NettyTransmissionConnState.CLIENT_CONNECTING) {
            logData.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            "] connecting..."));
            this.logMessage(logData);
        } else if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            logData.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            ",LocalPort:", channelInformation.getLocalPort(),
                            "] Connected OK"));
            this.logMessage(logData);
        } else if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED || state == NettyTransmissionConnState.CLIENT_DISCONNECTED_ON_EXCEPTION_OCCUR) {
            logData.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            ",LocalPort:", channelInformation.getLocalPort(),
                            "] Disconnected"));
            this.logMessage(logData);
        }
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        LogData logData = new LogData();
        logData.setRemark(
                StringUtils.join("[", configuration.getSocketType(),
                        " IP:", configuration.getHost(),
                        ",Port:", configuration.getPort(),
                        ",LocalPort:", channelInformation.getLocalPort(),
                        "] ", t.getMessage()));
        logData.setProgramException(t);
        logData.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
        sendEMS(logContext);
    }
}
