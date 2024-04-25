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
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用建立短連線的Client
 *
 * @param <Configuration>
 * @param <MessageIn>
 * @param <MessageOut>
 */
public abstract class SimpleNettyClientShort<Configuration extends SimpleNettyClientConfiguration, MessageIn, MessageOut>
        extends SimpleNettyBase<Configuration, MessageIn, MessageOut> {
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private EventExecutorGroup bisEventExecutorGroup;
    private final NettyClientChannelInboundHandler handler = new NettyClientChannelInboundHandler();
    private final NettyClientChannelInboundHandlerException exceptionHandler = new NettyClientChannelInboundHandlerException();
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
        // 短連線不需要覆寫這個方法
    }

    @Override
    public void closeConnection() {
        // 短連線不需要覆寫這個方法
    }

    @Override
    public void terminateConnection() {
        this.putMDC(this.channel);
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

    /**
     * 建立連線發送訊息並接收訊息
     *
     * @param messageOut
     * @param timeout
     * @return
     * @throws Exception
     */
    public MessageIn establishConnectionAndSendReceive(MessageOut messageOut, int timeout) throws Exception {
        return this.establishConnectionAndSendReceive(this.configuration, messageOut, timeout);
    }

    /**
     * 建立連線發送訊息並接收訊息
     *
     * @param configuration
     * @param messageOut
     * @param timeout
     * @return
     * @throws Exception
     */
    public MessageIn establishConnectionAndSendReceive(Configuration configuration, MessageOut messageOut, int timeout) throws Exception {
        this.putMDC(null);
        Channel channel = null;
        try {
            this.configuration = configuration;
            String host = configuration.getHost();
            int port = configuration.getPort();
            // send request
            if (messageOut == null) {
                throw ExceptionUtil.createException("Cannot send message to host = [", host, "], port = [", port, "], cause MessageOut is null!!!");
            }
            this.infoMessage("Try to establish connection, host = [", host, "], port = [", port, "]...");
            notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_CONNECTING);
            // connect
            try {
                channel = this.bootstrap.connect(host, port).sync().channel();
                setChannelMDCMap(channel); // 注意注意, 這裡一定要set一次
                this.putMDC(channel);
                this.infoMessage(channel, "Establish connection succeed, host = [", host, "], port = [", port, "]");
            } catch (Exception e) {
                notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, SimpleNettyConnState.CLIENT_CONNECTING_FAILED);
                throw ExceptionUtil.createException(e, "Establish connection failed, host = [", host, "], port = [", port, "]");
            }
            try {
                this.infoMessage(channel, Const.MESSAGE_OUT, messageOut);
                channel.writeAndFlush(toByteBuf(messageOutToBytes(messageOut)));
            } catch (Exception e) {
                throw ExceptionUtil.createException(e, "Send message failed, message = [", messageOut, "], host = [", host, "], port = [", port, "]");
            }
            // wait and get response
            AttributeKey<MessageIn> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MessageIn.name());
            Attribute<MessageIn> attr = channel.attr(key);
            if (timeout > 0) {
                long currentTimeMillis = System.currentTimeMillis();
                this.infoMessage(channel, "start to wait message in for timeout = [", timeout, "]");
                synchronized (attr) {
                    try {
                        attr.wait(timeout);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                }
                if (System.currentTimeMillis() - currentTimeMillis >= timeout) {
                    throw ExceptionUtil.createTimeoutException("Receive timeout after ", timeout, " millisecond, host = [", host, "], port = [", port, "]");
                }
            }
            return attr.get();
        } catch (Exception e) {
            this.errorMessage(channel, e, e.getMessage());
            LogData logData = new LogData();
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".establishConnectionAndSendReceive"));
            logData.setRemark(e.getMessage());
            sendEMS(logData);
            throw e;
        } finally {
            // 這裡記得關閉連線
            if (channel != null) {
                channel.close();
            }
        }
    }

    @Sharable
    private class NettyClientChannelInboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            putMDC(ctx.channel());
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.SERVER_MESSAGE_INCOMING);
            AttributeKey<MessageIn> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MessageIn.name());
            Attribute<MessageIn> attr = ctx.channel().attr(key);
            MessageIn messageIn = null;
            try {
                byte[] bytes = toBytes((ByteBuf) msg);
                messageIn = bytesToMessageIn(bytes);
                if (messageIn == null) {
                    warnMessage(ctx.channel(), "MessageIn is null!!! host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
                    return;
                }
                infoMessage(ctx.channel(), Const.MESSAGE_IN, messageIn);
            } finally {
                notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_CONNECTED_IDLE);
                ReferenceCountUtil.release(msg);
                synchronized (attr) {
                    infoMessage(ctx.channel(), "start to call attr.set(messageIn) and attr.notifyAll()");
                    attr.set(messageIn);
                    attr.notifyAll();
                    infoMessage(ctx.channel(), "finish to call attr.set(messageIn) and attr.notifyAll()");
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelActive(ctx);
            infoMessage(ctx.channel(), "Connected to server, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_CONNECTED);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            putMDC(ctx.channel());
            super.channelInactive(ctx);
            infoMessage(ctx.channel(), "Disconnected from server, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_DISCONNECTED);
            notification.removeConnStateListener(configuration.getNettyTransmissionNotificationKey(), SimpleNettyClientShort.this);
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
    }

    @Sharable
    private class NettyClientChannelInboundHandlerException extends ChannelInboundHandlerAdapter {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            putMDC(ctx.channel());
            errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised, host = [", configuration.getHost(), "], port = [", configuration.getPort(), "]");
            notification.notifyConnStateChanged(configuration.getNettyTransmissionNotificationKey(), ctx.channel(), SimpleNettyConnState.CLIENT_DISCONNECTED_ON_EXCEPTION_OCCUR, cause);
            LogData logData = new LogData();
            logData.setProgramException(cause);
            logData.setProgramName(StringUtils.join(ProgramName, ".exceptionHandler.exceptionCaught"));
            sendEMS(logData);
            ctx.close();
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
