package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.frmcommon.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.List;

public abstract class NettyTransmissionChannelInboundHandlerAdapterClient<Configuration extends NettyTransmissionClientConfiguration, ProcessRequest extends NettyTransmissionChannelProcessRequestClient<Configuration>>
        extends NettyTransmissionChannelInboundHandlerAdapter<Configuration> {
    @Autowired
    private ProcessRequest processRequest;
    private EventExecutorGroup disassembledEventExecutorGroup;

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

    @PreDestroy
    public void destroy() {
        if (this.disassembledEventExecutorGroup != null) {
            this.disassembledEventExecutorGroup.shutdownGracefully();
            this.disassembledEventExecutorGroup = null;
        }
    }

    public ProcessRequest getProcessRequest() {
        return processRequest;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.putMDC(ctx);
        try {
            byte[] bytes = NettyTransmissionUtil.toBytes((ByteBuf) msg);
            NettyTransmissionUtil.infoMessage(ctx.channel(), Const.MESSAGE_IN, StringUtil.toHex(bytes));
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.SERVER_MESSAGE_INCOMING);
            List<byte[]> disassembledBytes = this.disassembleTransmissionMessage(ctx, bytes);
            if (disassembledBytes.size() == 1) {
                processRequest.doProcess(ctx, disassembledBytes.get(0));
            } else {
                if (this.configuration.isAsyncDisassembled()) {
                    if (disassembledEventExecutorGroup == null)
                        disassembledEventExecutorGroup = new DefaultEventExecutorGroup(this.configuration.getDisassembledThreadNum(),
                                new DefaultThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "DisassembledThread"), true));
                    for (byte[] disassembledByte : disassembledBytes) {
                        disassembledEventExecutorGroup.execute(() -> {
                            this.putMDC(ctx);
                            try {
                                processRequest.doProcess(ctx, disassembledByte);
                            } catch (Exception e) {
                                ctx.fireExceptionCaught(e);
                            }
                        });
                    }
                } else {
                    for (byte[] disassembledByte : disassembledBytes) {
                        processRequest.doProcess(ctx, disassembledByte);
                    }
                }
            }
        } finally {
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_CONNECTED_IDLE);
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelActive(ctx);
        processRequest.setChannelHandlerContext(ctx);
        NettyTransmissionUtil.infoMessage(ctx.channel(), "Connected to server");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_CONNECTED);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelInactive(ctx);
        NettyTransmissionUtil.infoMessage(ctx.channel(), "Disconnected from server");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_DISCONNECTED);
        ctx.close();
    }

    @Override
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        this.putMDC(ctx);
        super.sslHandshakeCompletionEventTriggered(ctx, sslHandler, evt);
        if (evt.isSuccess()) {
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.SSL_CERTIFICATE_ACCEPT);
        }
    }
}
