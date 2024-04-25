package com.syscom.fep.gateway.netty;

import io.netty.channel.ChannelHandlerContext;

public abstract class NettyTransmissionChannelProcessRequestServer<Configuration extends NettyTransmissionServerConfiguration> extends NettyTransmissionChannelProcessRequest<Configuration> {
    @Override
    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        super.setChannelHandlerContext(channelHandlerContext);
        this.notification.addConnStateListener(this.channelInformation.getLongChannelId(), this);
    }

    @Override
    public void closeConnection() {
        putMDC();
        this.notification.notifyConnStateChanged(this.channelInformation.getLongChannelId(), this.channelHandlerContext.channel(), NettyTransmissionConnState.CLIENT_DISCONNECTED);
        this.notification.removeConnStateListener(this.channelInformation.getLongChannelId(), this);
    }
}
