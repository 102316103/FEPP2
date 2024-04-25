package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.FEPBase;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Sharable
public class NettyTransmissionChannelInboundHandlerAdapterClientException<Configuration extends NettyTransmissionClientConfiguration, ProcessRequest extends NettyTransmissionChannelProcessRequestClient<Configuration>>
        extends NettyTransmissionChannelInboundHandlerAdapter<Configuration> {
    @Autowired
    private ProcessRequest processRequest;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     * @param processRequest
     */
    public void initialization(Configuration configuration, ProcessRequest processRequest) {
        super.initialization(configuration);
        this.processRequest = processRequest;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 注意不要實作, NettyTransmissionChannelInboundHandlerAdapter中會實作
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.putMDC(ctx);
        processRequest.getLogContext().setProgramException(cause);
        processRequest.getLogContext().setProgramName(StringUtils.join(ProgramName, ".exceptionCaught"));
        FEPBase.sendEMS(processRequest.getLogContext());
        NettyTransmissionUtil.errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_DISCONNECTED_ON_EXCEPTION_OCCUR, cause);
        ctx.close();
    }
}
