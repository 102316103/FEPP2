package com.syscom.fep.invoker.netty;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.GenericTypeUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;

import java.net.StandardSocketOptions;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SimpleNettyServer<Configuration extends SimpleNettyServerConfiguration, Processor extends SimpleNettyServerProcessor<MessageIn, MessageOut>, MessageIn, MessageOut>
        extends SimpleNettyBase<Configuration, MessageIn, MessageOut> {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup bisEventExecutorGroup;
    private ServerBootstrap bootstrap;
    private final AtomicInteger bindConnectionTimes = new AtomicInteger(1);
    private final Class<Processor> processorCls = GenericTypeUtil.getGenericSuperClass(this.getClass(), 1);
    private SimpleNettyServerRuleIpFilter<Configuration> ruleIpFilter;
    private SimpleNettyProcessorManager<Processor> simpleNettyProcessorManager;

    @Override
    protected void initialization() {
        this.putMDC(null);
        this.bossGroup = new NioEventLoopGroup(
                this.configuration.getBossThreadNum(),
                new DefaultThreadFactory(StringUtils.join(this.getName(), "BossThread"), true));
        this.workerGroup = new NioEventLoopGroup(
                this.configuration.getServerWorkerThreadNum(),
                new DefaultThreadFactory(StringUtils.join(this.getName(), "WorkerThread"), true));
        this.bootstrap = new ServerBootstrap();
        if (this.configuration.getServerBisThreadNum() > 0) {
            this.bisEventExecutorGroup = new DefaultEventExecutorGroup(
                    this.configuration.getServerBisThreadNum(),
                    new DefaultThreadFactory(StringUtils.join(this.getName(), "BusinessThread"), true));
        }
        this.bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        putMDC(ch);
                        ChannelPipeline pipeline = ch.pipeline();
                        // import ssl key to enable SSL
                        try {
                            SslHandler sslHandler = getSslHandler(ch, configuration, false); // 服務端認證方式
                            if (sslHandler != null) {
                                if (bisEventExecutorGroup != null) {
                                    pipeline.addLast(bisEventExecutorGroup, sslHandler);
                                } else {
                                    pipeline.addLast(sslHandler);
                                }
                            }
                        } catch (Exception e) {
                            errorMessage(e, "SSL initialized failed!!!");
                            throw ExceptionUtil.createException(e, "SSL initialized failed!!!");
                        }
                        if (configuration.getReaderIdleTime() > 0 || configuration.getWriterIdleTime() > 0) {
                            pipeline.addLast(new IdleStateHandler(configuration.getReaderIdleTime(), configuration.getWriterIdleTime(), 0, TimeUnit.MILLISECONDS));
                        }
                        if (bisEventExecutorGroup != null) {
                            pipeline.addLast(bisEventExecutorGroup, getRuleIpFilter(ch))
                                    .addLast(bisEventExecutorGroup, new NettyServerChannelInboundHandler())
                                    .addLast(bisEventExecutorGroup, new NettyServerChannelInboundHandlerException()); // NettyServerChannelInboundHandlerException一定要放在最後, 切記切記
                        } else {
                            pipeline.addLast(getRuleIpFilter(ch))
                                    .addLast(new NettyServerChannelInboundHandler())
                                    .addLast(new NettyServerChannelInboundHandlerException()); // NettyServerChannelInboundHandlerException一定要放在最後, 切記切記
                        }
                    }
                })
                .option(ChannelOption.SO_BACKLOG, this.configuration.getBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, this.configuration.isSoKeepalive())
                .childOption(NioChannelOption.of(StandardSocketOptions.SO_KEEPALIVE), this.configuration.isSoKeepalive())
                .childOption(ChannelOption.TCP_NODELAY, this.configuration.isTcpNodelay())
                .childOption(ChannelOption.SO_SNDBUF, this.configuration.getSoSndBuf())
                .childOption(ChannelOption.SO_RCVBUF, this.configuration.getSoRcvBuf());
        if (this.configuration.getTcpKeepIdle() > 0) {
            try {
                this.bootstrap.childOption(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPIDLE)), configuration.getTcpKeepIdle());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPIDLE'");
            }
        }
        if (this.configuration.getTcpKeepInterval() > 0) {
            try {
                this.bootstrap.childOption(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPINTERVAL)), configuration.getTcpKeepInterval());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPINTERVAL'");
            }
        }
        if (this.configuration.getTcpKeepCount() > 0) {
            try {
                this.bootstrap.childOption(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPCOUNT)), configuration.getTcpKeepCount());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPCOUNT'");
            }
        }
        if (this.configuration.getRcvBufAllocator() == 0) {
            this.bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator()); // 動態緩衝區分配器
        } else if (this.configuration.getRcvBufAllocator() > 0) {
            this.bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(this.configuration.getRcvBufAllocator())); // 固定長度緩衝區分配器
        }
        notification.addConnStateListener(0, this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    protected void establishConnection() {
        this.putMDC(null);
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        this.infoMessage("Try to listen host = [", host, "], port = [", port, "] at [", bindConnectionTimes.get(), "] times...");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.SERVER_BINDING);
        try {
            ChannelFuture connFuture = this.bootstrap.bind(port).sync().addListener((ChannelFuture future) -> {
                putMDC(future.channel());
                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    int connectTimes = bindConnectionTimes.get();
                    this.infoMessage("Establish connection succeed at [", connectTimes, "] times, and begin listen host = [", host, "], port = [", port, "]");
                    notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), channel, SimpleNettyConnState.SERVER_BOUND);
                } else {
                    this.errorMessage(future.cause(), "Establish connection failed, host = [", host, "], port = [", port, "] at [", bindConnectionTimes.get(), "] times");
                    notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.SERVER_BIND_FAILED);
                }
            });
            // connFuture.channel().closeFuture().sync();
            // connFuture.channel().closeFuture().addListener((ChannelFuture closeFut) -> {
            //     this.scheduleToReestablishConnection(true);
            // });
            connFuture.channel().closeFuture().addListener((ChannelFuture closeFut) -> {
                closeFut.channel().close();
            });
        } catch (Exception e) {
            LogData logData = new LogData();
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".establishConnection"));
            sendEMS(logData);
            throw ExceptionUtil.createRuntimeException(e, e.getMessage());
        }
    }

    // private void scheduleToReestablishConnection(ChannelFuture closeFut, boolean reestablishConnectionAfterTerminateConnection) {
    //     this.putMDC(closeFut.channel());
    //     if (reestablishConnectionAfterTerminateConnection) {
    //         String host = this.configuration.getHost();
    //         int port = this.configuration.getPort();
    //         long delay = this.configuration.getReestablishConnectionInterval();
    //         warnMessage("Try to re-bind connection, host = [", host, "], port = [", port, "] at [", bindConnectionTimes.incrementAndGet(), "] times after [", delay, "] milliseconds");
    //         closeFut.channel().eventLoop().schedule(() -> {
    //             putMDC(closeFut.channel());
    //             infoMessage("Begin Retry Bind Socket...");
    //             initialization();
    //             establishConnection();
    //         }, delay, TimeUnit.MILLISECONDS);
    //     }
    // }

    @Override
    public void closeConnection() {
        // TODO
    }

    @Override
    public void terminateConnection() {
        this.putMDC(null);
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.SERVER_SHUTTING_DOWN);
        this.infoMessage(null, "[Terminate Connection]Try to stop listen, host = [", host, "], port = [", port, "]...");
        if (this.bisEventExecutorGroup != null) {
            this.bisEventExecutorGroup.shutdownGracefully();
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
        }
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
        }
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.SERVER_SHUT_DOWN);
        this.infoMessage(null, "[Terminate Connection]Stop listen host = [", host, "], port = [", port, "] successful");
        notification.removeConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    protected SimpleNettyServerRuleIpFilter<Configuration> getRuleIpFilter(final Channel channel) {
        if (ruleIpFilter == null) {
            ruleIpFilter = new SimpleNettyServerRuleIpFilter<Configuration>(configuration) {
                @Override
                protected void putMDC(Channel channel) {
                    SimpleNettyServer.this.putMDC(channel);
                }
            };
        }
        return ruleIpFilter;
    }

    public void setRuleIpFilter(SimpleNettyServerRuleIpFilter<Configuration> ruleIpFilter) {
        this.ruleIpFilter = ruleIpFilter;
    }

    protected SimpleNettyProcessorManager<Processor> getProcessorManager() {
        if (simpleNettyProcessorManager == null) {
            simpleNettyProcessorManager = new SimpleNettyServerProcessorMultiManager<Processor>(processorCls);
        }
        return simpleNettyProcessorManager;
    }

    public void setProcessorManager(SimpleNettyProcessorManager<Processor> simpleNettyProcessorManager) {
        this.simpleNettyProcessorManager = simpleNettyProcessorManager;
    }

    public Class<Processor> getProcessorCls() {
        return processorCls;
    }

    private class NettyServerChannelInboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            putMDC(ctx.channel());
            notification.notifyConnStateChanged(ctx.channel().id().asLongText(), ctx.channel(), SimpleNettyConnState.CLIENT_MESSAGE_INCOMING);
            try {
                Channel channel = ctx.channel();
                Processor processor = getProcessorManager().getProcessor(ctx);
                byte[] bytes = toBytes((ByteBuf) msg);
                MessageIn messageIn = bytesToMessageIn(bytes);
                if (messageIn == null) {
                    warnMessage(channel, "messageIn is null!!!");
                    return;
                }
                if (messageIn instanceof byte[]) {
                    infoMessage(channel, Const.MESSAGE_IN, StringUtils.join("bytes:[", StringUtils.join(((byte[]) messageIn), ','), "]"));
                } else {
                    infoMessage(channel, Const.MESSAGE_IN, messageIn);
                }
                MessageOut messageOut = processor.processRequestData(messageIn);
                sendMessage(channel, messageOut);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelActive(ctx);
            infoMessage(ctx.channel(), "Client connected in");
            Processor processor = getProcessorManager().addProcessor(ctx);
            notification.addConnStateListener(ctx.channel().id().asLongText(), processor);
            notification.notifyConnStateChanged(ctx.channel().id().asLongText(), ctx.channel(), SimpleNettyConnState.CLIENT_CONNECTED);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelInactive(ctx);
            infoMessage(ctx.channel(), "Client disconnected");
            notification.notifyConnStateChanged(ctx.channel().id().asLongText(), ctx.channel(), SimpleNettyConnState.CLIENT_DISCONNECTED);
            Processor processor = getProcessorManager().removeProcessor(ctx);
            notification.removeConnStateListener(ctx.channel().id().asLongText(), processor);
            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            putMDC(ctx.channel());
            if (evt instanceof IdleStateEvent) {
                //--20221219 ben 暫將讀取資料庫部份全mark
                //infoMessage(ctx.channel(), "IdleStateEvent occur...");
                //--20221219 ben 暫將讀取資料庫部份全mark
            } else if (evt instanceof SslHandshakeCompletionEvent) {
                infoMessage(ctx.channel(), "SslHandshakeCompletionEvent occur...");
                SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
                sslHandshakeCompletionEventTriggered(ctx, sslHandler, (SslHandshakeCompletionEvent) evt);
            }
        }

        private void sendMessage(Channel channel, MessageOut messageOut) {
            putMDC(channel);
            if (channel != null && messageOut != null) {
                infoMessage(channel, Const.MESSAGE_OUT, messageOut);
                ChannelFuture future = channel.writeAndFlush(toByteBuf(messageOutToBytes(messageOut)));
                future.addListener(f -> {
                    putMDC(channel);
                    if (f.isSuccess()) {
                    } else {
                        Throwable t = f.cause();
                        errorMessage(channel, t, "send message failed, message = [", messageOut, "]");
                        LogData logData = new LogData();
                        logData.setProgramException(t);
                        logData.setProgramName(StringUtils.join(ProgramName, ".handler.sendMessage"));
                        sendEMS(logData);
                    }
                });
            }
        }

        private byte[] toBytes(ByteBuf buffer) {
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        }

        private ByteBuf toByteBuf(byte[] bytes) {
            return Unpooled.wrappedBuffer(bytes);
        }
    }

    private class NettyServerChannelInboundHandlerException extends ChannelInboundHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            putMDC(ctx.channel());
            errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised");
            LogData logData = new LogData();
            logData.setProgramException(cause);
            logData.setProgramName(StringUtils.join(ProgramName, ".exceptionHandler.exceptionCaught"));
            sendEMS(logData);
            Channel channel = ctx.channel();
            notification.notifyConnStateChanged(channel.id().asLongText(), channel, SimpleNettyConnState.CLIENT_DISCONNECTED_ON_EXCEPTION_OCCUR, cause);
            ctx.close();
        }
    }

    /**
     * 可以讓子類去複寫
     *
     * @param ctx
     * @param sslHandler
     * @param evt
     */
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        putMDC(ctx.channel());
        super.sslHandshakeCompletionEventTriggered(ctx, sslHandler, evt);
        if (evt.isSuccess()) {
            Channel channel = ctx.channel();
            notification.notifyConnStateChanged(channel.id().asLongText(), channel, SimpleNettyConnState.SSL_CERTIFICATE_ACCEPT);
        }
    }
}
