package com.syscom.fep.invoker.netty;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用於建立長連線的Client
 *
 * @param <Configuration>
 * @param <MessageIn>
 * @param <MessageOut>
 */
public abstract class SimpleNettyClient<Configuration extends SimpleNettyClientConfiguration, MessageIn, MessageOut>
        extends SimpleNettyBase<Configuration, MessageIn, MessageOut> {
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private EventExecutorGroup bisEventExecutorGroup;
    private final AtomicInteger establishConnectionTimes = new AtomicInteger(1);
    private boolean isShuttingDown = false;
    private final NettyClientChannelInboundHandler handler = new NettyClientChannelInboundHandler();
    private final NettyClientChannelInboundHandlerException exceptionHandler = new NettyClientChannelInboundHandlerException();
    private boolean connected = false;
    private boolean reestablishConnectionWhenDisconnected = true;
    private Channel channel;

    @Override
    protected void initialization() {
        this.putMDC(null);
        this.workerGroup = new NioEventLoopGroup(
                this.configuration.getClientWorkerThreadNum(),
                new DefaultThreadFactory(StringUtils.join(this.getName(), "WorkerThread"), true));
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(this.workerGroup);
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, this.configuration.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, this.configuration.isTcpNodelay())
                .option(ChannelOption.SO_SNDBUF, this.configuration.getSoSndBuf())
                .option(ChannelOption.SO_RCVBUF, this.configuration.getSoRcvBuf());
        if (this.configuration.getTcpKeepIdle() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPIDLE)), configuration.getTcpKeepIdle());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPIDLE'");
            }
        }
        if (this.configuration.getTcpKeepInterval() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPINTERVAL)), configuration.getTcpKeepInterval());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPINTERVAL'");
            }
        }
        if (this.configuration.getTcpKeepCount() > 0) {
            try {
                this.bootstrap.option(NioChannelOption.of(SimpleNettySocketOptions.getSocketOption(SimpleNettySocketOptionName.TCP_KEEPCOUNT)), configuration.getTcpKeepCount());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPCOUNT'");
            }
        }
        if (this.configuration.getRcvBufAllocator() == 0) {
            this.bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator()); // 動態緩衝區分配器
        } else if (this.configuration.getRcvBufAllocator() > 0) {
            this.bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(this.configuration.getRcvBufAllocator())); // 固定長度緩衝區分配器
        }
        if (this.configuration.getClientBisThreadNum() > 0) {
            this.bisEventExecutorGroup = new DefaultEventExecutorGroup(
                    this.configuration.getClientBisThreadNum(),
                    new DefaultThreadFactory(StringUtils.join(this.getName(), "BusinessThread"), true));
        }
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                putMDC(ch);
                ChannelPipeline pipeline = ch.pipeline();
                // import ssl key to enable SSL
                try {
                    SslHandler sslHandler = getSslHandler(ch, configuration, true); // 客戶端認證方式
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
                    pipeline.addLast(bisEventExecutorGroup, handler)
                            .addLast(bisEventExecutorGroup, exceptionHandler); // exceptionHandlerAdapter一定要放在最後, 切記切記
                } else {
                    pipeline.addLast(handler)
                            .addLast(exceptionHandler); // exceptionHandlerAdapter一定要放在最後, 切記切記
                }
            }
        });
        notification.addConnStateListener(0, this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    protected void establishConnection() {
        this.putMDC(null);
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        this.infoMessage("Try to establish connection, host = [", host, "], port = [", port, "] at [", establishConnectionTimes.get(), "] times...");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_CONNECTING);
        ChannelFuture connFuture = this.bootstrap.connect(host, port);
        setChannelMDCMap(connFuture.channel()); // 注意注意, 這裡一定要set一次
        this.putMDC(connFuture.channel());
        connFuture.addListener((ChannelFuture future) -> {
            putMDC(future.channel());
            if (future.isSuccess()) {
                this.connected = true;
                int connectTimes = establishConnectionTimes.getAndSet(0);
                this.infoMessage(future.channel(), "Establish connection succeed, host = [", host, "], port = [", port, "] at [", connectTimes, "] times");
                this.channel = future.channel();
                this.channel.closeFuture().addListener((ChannelFuture closeFut) -> {
                    this.scheduleToReestablishConnection(future);
                });
            } else {
                this.errorMessage(future.cause(), "Establish connection failed, host = [", host, "], port = [", port, "] at [", establishConnectionTimes.get(), "] times");
                notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_CONNECTING_FAILED);
                handler.connectFailed();
                this.scheduleToReestablishConnection(future);
            }
        });
    }

    protected void scheduleToReestablishConnection(ChannelFuture future) {
        this.putMDC(future.channel());
        this.connected = false;
        if (!this.isShuttingDown && reestablishConnectionWhenDisconnected) {
            String host = this.configuration.getHost();
            int port = this.configuration.getPort();
            long delay = this.configuration.getReestablishConnectionInterval();
            this.warnMessage("Try to reestablish connection, host = [", host, "], port = [", port, "] at [", establishConnectionTimes.incrementAndGet(), "] times after [", delay, "] milliseconds");
            future.channel().eventLoop().schedule(this::establishConnection, delay, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void closeConnection() {
        if (this.channel != null) {
            this.channel.close();
        }
    }

    @Override
    public void terminateConnection() {
        this.putMDC(this.channel);
        this.connected = false;
        this.isShuttingDown = true;
        // 避免出現一直等待
        synchronized (this) {
            this.notifyAll();
        }
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), this.channel, SimpleNettyConnState.CLIENT_SHUTTING_DOWN);
        this.infoMessage("[Terminate Connection]Try to destroy all event executor group from host = [", host, "], port = [", port, "]...");
        if (this.bisEventExecutorGroup != null) {
            this.bisEventExecutorGroup.shutdownGracefully();
            this.bisEventExecutorGroup = null;
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), this.channel, SimpleNettyConnState.CLIENT_SHUT_DOWN);
        this.infoMessage("[Terminate Connection]Destroy all event executor group from host = [", host, "], port = [", port, "] successful");
        notification.removeConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    public MessageIn sendReceive(MessageOut messageOut, int timeout) throws Exception {
        this.putMDC(this.channel);
        ChannelPromise promise = this.handler.sendReceive(messageOut);
        if (timeout > 0) {
            boolean completed = promise.await(timeout);
            if (!completed) {
                throw ExceptionUtil.createTimeoutException("Receive timeout after ", timeout, " millisecond, host = [", this.configuration.getHost(), "], port = [", this.configuration.getPort(), "] successful");
            }
        }
        return this.handler.getMessageIn();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setReestablishConnectionWhenDisconnected(boolean reestablishConnectionWhenDisconnected) {
        this.reestablishConnectionWhenDisconnected = reestablishConnectionWhenDisconnected;
    }

    @Sharable
    private class NettyClientChannelInboundHandler extends ChannelInboundHandlerAdapter {
        private boolean isFirstConnect = true; // 是否首次連線
        private ChannelHandlerContext ctx;
        private ChannelPromise promise;
        private MessageIn messageIn;

        public MessageIn getMessageIn() {
            return this.messageIn;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            putMDC(ctx.channel());
            connected = true;
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.SERVER_MESSAGE_INCOMING);
            try {
                byte[] bytes = toBytes((ByteBuf) msg);
                this.messageIn = bytesToMessageIn(bytes);
                this.promise.setSuccess();
                if (this.messageIn == null) {
                    warnMessage(ctx.channel(), "MessageIn is null!!! host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
                    return;
                }
                infoMessage(ctx.channel(), Const.MESSAGE_IN, messageIn);
            } finally {
                notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_CONNECTED_IDLE);
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            connected = true;
            super.channelActive(ctx);
            infoMessage(ctx.channel(), "Connected to server, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_CONNECTED);
            this.ctx = ctx;
            this.isFirstConnect = false;
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            connected = false;
            super.channelInactive(ctx);
            infoMessage(ctx.channel(), "Disconnected from server, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_DISCONNECTED);
            notification.removeConnStateListener(configuration.getNettyTransmissionNotificationKey(), SimpleNettyClient.this);
            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            putMDC(ctx.channel());
            if (evt instanceof IdleStateEvent) {
                // --20221219 ben 暫將讀取資料庫部份全mark
                // infoMessage(ctx.channel(), "IdleStateEvent occur...");
                // --20221219 ben 暫將讀取資料庫部份全mark
            } else if (evt instanceof SslHandshakeCompletionEvent) {
                infoMessage(ctx.channel(), "SslHandshakeCompletionEvent occur...");
                SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
                sslHandshakeCompletionEventTriggered(ctx, sslHandler, (SslHandshakeCompletionEvent) evt);
            }
        }

        public void connectFailed() {
            // 如果是首次連線, 並且有呼叫sendReceive, 會一直等待連線成功, 所以這裡要notifyAll一次, 不要再等待了
            if (this.isFirstConnect) {
                synchronized (this) {
                    this.notifyAll();
                }
            }
            this.isFirstConnect = false;
        }

        public ChannelPromise sendReceive(MessageOut messageOut) throws Exception {
            putMDC(channel);
            // 首次連線等待成功後, 再發送訊息
            if (this.isFirstConnect) {
                synchronized (this) {
                    this.wait();
                }
            }
            if (this.ctx == null || !connected) {
                connected = false;
                String errorMessage = StringUtils.join("Disconnect from server, cannot send any message!!!, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
                throw ExceptionUtil.createException(errorMessage);
            }
            connected = true;
            if (messageOut == null) {
                warnMessage(this.ctx.channel(), "MessageOut is null!!! host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
                return null;
            }
            final Channel channel = this.ctx.channel();
            this.promise = this.ctx.newPromise();
            infoMessage(channel, Const.MESSAGE_OUT, messageOut);
            ChannelFuture future = channel.writeAndFlush(toByteBuf(messageOutToBytes(messageOut)));
            future.addListener(f -> {
                putMDC(channel);
                if (f.isSuccess()) {
                } else {
                    Throwable t = f.cause();
                    errorMessage(channel, t, "send message failed, message = [", messageOut, "], host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
                    LogData logData = new LogData();
                    logData.setProgramException(t);
                    logData.setProgramName(StringUtils.join(ProgramName, ".handler.sendReceive"));
                    sendEMS(logData);
                }
            });
            return this.promise;
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

    @Sharable
    private class NettyClientChannelInboundHandlerException extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            putMDC(ctx.channel());
            connected = false;
            errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_DISCONNECTED_ON_EXCEPTION_OCCUR, cause);
            LogData logData = new LogData();
            logData.setProgramException(cause);
            logData.setProgramName(StringUtils.join(ProgramName, ".exceptionHandler.exceptionCaught"));
            sendEMS(logData);
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
    @Override
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        putMDC(ctx.channel());
        super.sslHandshakeCompletionEventTriggered(ctx, sslHandler, evt);
        if (evt.isSuccess()) {
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.SSL_CERTIFICATE_ACCEPT);
        }
    }
}
