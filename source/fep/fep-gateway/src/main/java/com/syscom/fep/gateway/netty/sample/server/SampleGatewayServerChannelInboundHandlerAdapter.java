package com.syscom.fep.gateway.netty.sample.server;

import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterServer;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class SampleGatewayServerChannelInboundHandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterServer<SampleGatewayServerConfiguration, SampleGatewayServerProcessRequestManager, SampleGatewayServerProcessRequest> {}
