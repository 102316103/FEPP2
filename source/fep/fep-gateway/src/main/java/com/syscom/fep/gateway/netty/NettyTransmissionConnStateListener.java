package com.syscom.fep.gateway.netty;

import io.netty.channel.Channel;

import java.util.EventListener;

public interface NettyTransmissionConnStateListener extends EventListener {

    public void connStateChanged(Channel channel, NettyTransmissionConnState state);

    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t);

}
