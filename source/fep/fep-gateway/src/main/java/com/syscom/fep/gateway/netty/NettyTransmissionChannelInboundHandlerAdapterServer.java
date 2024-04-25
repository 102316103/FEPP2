package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.frmcommon.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
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
import java.util.Calendar;
import java.util.List;

public abstract class NettyTransmissionChannelInboundHandlerAdapterServer<Configuration extends NettyTransmissionServerConfiguration, ProcessRequestManager extends NettyTransmissionChannelProcessRequestServerManager<Configuration, ProcessRequest>, ProcessRequest extends NettyTransmissionChannelProcessRequestServer<Configuration>>
        extends NettyTransmissionChannelInboundHandlerAdapter<Configuration> {
    @Autowired
    protected ProcessRequestManager processRequestManager;
    private NettyTransmissionServerMonitor<Configuration> transmissionServerMonitor;
    private EventExecutorGroup disassembledEventExecutorGroup;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     * @param processRequestManager
     */
    public void initialization(Configuration configuration, ProcessRequestManager processRequestManager) {
        super.initialization(configuration);
        this.processRequestManager = processRequestManager;
    }

    @PreDestroy
    public void destroy() {
        if (this.disassembledEventExecutorGroup != null) {
            this.disassembledEventExecutorGroup.shutdownGracefully();
            this.disassembledEventExecutorGroup = null;
        }
    }

    public void setTransmissionServerMonitor(NettyTransmissionServerMonitor<Configuration> transmissionServerMonitor) {
        this.transmissionServerMonitor = transmissionServerMonitor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.putMDC(ctx);
        try {
            byte[] bytes = NettyTransmissionUtil.toBytes((ByteBuf) msg);
            NettyTransmissionUtil.infoMessage(ctx.channel(), Const.MESSAGE_IN, StringUtil.toHex(bytes));
            Channel channel = ctx.channel();
            notification.notifyConnStateChanged(channel.id().asLongText(), channel, NettyTransmissionConnState.CLIENT_MESSAGE_INCOMING);
            ProcessRequest processRequest = processRequestManager.getNettyChannelProcessRequest(ctx);
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
            processRequest.doProcess(ctx, bytes);
        } finally {
            ReferenceCountUtil.release(msg);
            if (this.transmissionServerMonitor != null) {
                this.transmissionServerMonitor.setLatestActiveDateTime(Calendar.getInstance());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelActive(ctx);
        NettyTransmissionUtil.infoMessage(ctx.channel(), "Client connected in");
        processRequestManager.addNettyChannelProcessRequest(ctx);
        Channel channel = ctx.channel();
        notification.notifyConnStateChanged(channel.id().asLongText(), channel, NettyTransmissionConnState.CLIENT_CONNECTED);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelInactive(ctx);
        NettyTransmissionUtil.infoMessage(ctx.channel(), "Client disconnected");
        Channel channel = ctx.channel();
        notification.notifyConnStateChanged(channel.id().asLongText(), channel, NettyTransmissionConnState.CLIENT_DISCONNECTED);
        processRequestManager.removeNettyChannelProcessRequest(ctx);
        ctx.close();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelRegistered(ctx);
        if (this.transmissionServerMonitor != null) {
            if (this.transmissionServerMonitor.incrementAndGetConnections() % 100 == 0) {
                NettyTransmissionUtil.infoMessage(ctx.channel(), "Client connections was already increment to [", this.transmissionServerMonitor.getConnections(), "]");
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.putMDC(ctx);
        super.channelUnregistered(ctx);
        if (this.transmissionServerMonitor != null) {
            if (this.transmissionServerMonitor.decrementAndGetConnections() % 100 == 0) {
                NettyTransmissionUtil.infoMessage(ctx.channel(), "Client connections was already decrement to [", this.transmissionServerMonitor.getConnections(), "]");
            }
        }
    }

    @Override
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        this.putMDC(ctx);
        super.sslHandshakeCompletionEventTriggered(ctx, sslHandler, evt);
        if (evt.isSuccess()) {
            Channel channel = ctx.channel();
            notification.notifyConnStateChanged(channel.id().asLongText(), channel, NettyTransmissionConnState.SSL_CERTIFICATE_ACCEPT);
        }
    }
}
