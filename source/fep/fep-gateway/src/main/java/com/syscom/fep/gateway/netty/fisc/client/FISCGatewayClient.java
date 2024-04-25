package com.syscom.fep.gateway.netty.fisc.client;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterClient;
import com.syscom.fep.gateway.netty.NettyTransmissionClient;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

@StackTracePointCut(caller = SvrConst.SVR_FISC_GATEWAY)
public abstract class FISCGatewayClient<Configuration extends FISCGatewayClientConfiguration, HandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterClient<Configuration, ProcessRequest>, ProcessRequest extends FISCGatewayClientProcessRequest<Configuration>>
        extends NettyTransmissionClient<Configuration, HandlerAdapter, ProcessRequest> {

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state) {
        super.connStateChanged(channel, state);
        if (state == NettyTransmissionConnState.CLIENT_CONNECTING) {
            this.transmissionClientMonitor.setRemote(StringUtils.join(configuration.getHost(), ":", configuration.getPort()));
        }
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }
}
