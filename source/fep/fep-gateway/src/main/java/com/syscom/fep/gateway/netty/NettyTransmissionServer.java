package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ssl.SslKeyTrust;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.StandardSocketOptions;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class NettyTransmissionServer<Configuration extends NettyTransmissionServerConfiguration, HandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterServer<Configuration, ProcessRequestManager, ProcessRequest>, RuleIpFilter extends NettyTransmissionServerRuleIpFilter<Configuration>, ProcessRequestManager extends NettyTransmissionChannelProcessRequestServerManager<Configuration, ProcessRequest>, ProcessRequest extends NettyTransmissionChannelProcessRequestServer<Configuration>>
        extends NettyTransmission<Configuration, HandlerAdapter, ProcessRequest> {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup bisEventExecutorGroup;
    private ServerBootstrap bootstrap;
    private final AtomicInteger bindConnectionTimes = new AtomicInteger(1);
    protected AtomicReference<NettyTransmissionConnState> currentConnState = new AtomicReference<NettyTransmissionConnState>(NettyTransmissionConnState.SERVER_NOT_BOUND);
    private boolean reestablishConnectionAfterTerminateConnection = false;
    protected NettyTransmissionServerMonitor<Configuration> transmissionServerMonitor = new NettyTransmissionServerMonitor<>();
    @Autowired
    protected ProcessRequestManager requestManager;
    @Autowired
    protected RuleIpFilter ruleIpFilter;
    protected NettyTransmissionChannelInboundHandlerAdapterServerException<Configuration, ProcessRequestManager, ProcessRequest> exceptionHandlerAdapter;

    /**
     * 專門用於非SpringBean模式下啟動Gateway
     *
     * @param configuration
     * @param handlerAdapter
     * @param requestManager
     * @param ruleIpFilter
     */
    public void initialization(Configuration configuration, HandlerAdapter handlerAdapter, ProcessRequestManager requestManager, RuleIpFilter ruleIpFilter) {
        this.requestManager = requestManager;
        this.ruleIpFilter = ruleIpFilter;
        super.initialization(configuration, handlerAdapter);
    }

    @Override
    protected void initData() {}

    @Override
    protected void initialization() {
        this.putMDC(null);
        this.ruleIpFilter.setLogContext(this.logContext);
        this.bossGroup = new NioEventLoopGroup(
                this.configuration.getBossThreadNum(),
                new DefaultThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "BossThread"), true));
        this.workerGroup = new NioEventLoopGroup(
                this.configuration.getWorkerThreadNum(),
                new DefaultThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "WorkerThread"), true));
        this.bootstrap = new ServerBootstrap();
        if (this.configuration.getBisThreadNum() > 0) {
            this.bisEventExecutorGroup = new DefaultEventExecutorGroup(
                    this.configuration.getBisThreadNum(),
                    new DefaultThreadFactory(StringUtils.join(this.configuration.getGateway(), this.configuration.getSocketType(), "BusinessThread"), true));
        }
        this.bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        putMDC(ch);
                        channelInitialization(ch);
                        // 這裡判斷是否拒絕Client連入
                        if (channelRejected(ch)) {
                            ch.close(); // 如果拒絕, 則直接close與Client的連線
                            return;
                        }
                        ChannelPipeline pipeline = ch.pipeline();
                        // import ssl key to enable SSL
                        try {
                            SslHandler sslHandler = getSslHandler(new LogData(), ch);
                            if (sslHandler != null) {
                                if (bisEventExecutorGroup != null) {
                                    pipeline.addLast(bisEventExecutorGroup, sslHandler);
                                } else {
                                    pipeline.addLast(sslHandler);
                                }
                            }
                        } catch (Exception e) {
                            NettyTransmissionUtil.errorMessage(ch, e, "SSL initialized failed!!!");
                            throw ExceptionUtil.createException(e, "SSL initialized failed!!!");
                        }
                        // IdleStateHandler
                        if (configuration.getReaderIdleTime() > 0 || configuration.getWriterIdleTime() > 0) {
                            pipeline.addLast(new IdleStateHandler(configuration.getReaderIdleTime(), configuration.getWriterIdleTime(), 0, TimeUnit.MILLISECONDS));
                        }
                        // ruleIpFilter
                        if (bisEventExecutorGroup != null) {
                            pipeline.addLast(bisEventExecutorGroup, ruleIpFilter);
                        } else {
                            pipeline.addLast(ruleIpFilter);
                        }
                        // ByteToMessageDecoder
                        NettyTransmissionByteToMessageDecoder decoder = getByteToMessageDecoder();
                        if (decoder != null) {
                            if (bisEventExecutorGroup != null) {
                                pipeline.addLast(bisEventExecutorGroup, decoder);
                            } else {
                                pipeline.addLast(decoder);
                            }
                        }
                        // handlerAdapter
                        // exceptionHandlerAdapter
                        if (bisEventExecutorGroup != null) {
                            pipeline.addLast(bisEventExecutorGroup, handlerAdapter)
                                    .addLast(bisEventExecutorGroup, getExceptionHandlerAdapter()); // exceptionHandlerAdapter一定要放在最後, 切記切記
                        } else {
                            pipeline.addLast(handlerAdapter)
                                    .addLast(getExceptionHandlerAdapter()); // exceptionHandlerAdapter一定要放在最後, 切記切記
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
                this.bootstrap.childOption(NioChannelOption.of(NettyTransmissionSocketOptions.getSocketOption(NettyTransmissionSocketOptionName.TCP_KEEPIDLE)), configuration.getTcpKeepIdle());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPIDLE'");
            }
        }
        if (this.configuration.getTcpKeepInterval() > 0) {
            try {
                this.bootstrap.childOption(NioChannelOption.of(NettyTransmissionSocketOptions.getSocketOption(NettyTransmissionSocketOptionName.TCP_KEEPINTERVAL)), configuration.getTcpKeepInterval());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPINTERVAL'");
            }
        }
        if (this.configuration.getTcpKeepCount() > 0) {
            try {
                this.bootstrap.childOption(NioChannelOption.of(NettyTransmissionSocketOptions.getSocketOption(NettyTransmissionSocketOptionName.TCP_KEEPCOUNT)), configuration.getTcpKeepCount());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn("Cannot Supported Socket Options 'TCP_KEEPCOUNT'");
            }
        }
        if (this.configuration.getRcvBufAllocator() == 0) {
            this.bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator()); // 動態緩衝區分配器
        } else if (this.configuration.getRcvBufAllocator() > 0) {
            this.bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(this.configuration.getRcvBufAllocator())); // 固定長度緩衝區分配器
        }
        transmissionServerMonitor.setTransmissionConfiguration(configuration);
        handlerAdapter.setTransmissionServerMonitor(transmissionServerMonitor);
        requestManager.setTransmissionServerMonitor(transmissionServerMonitor);
        notification.addConnStateListener(0, this.configuration.getNettyTransmissionNotificationKey(), this);
    }

    @Override
    protected void establishConnection() {
        this.putMDC(null);
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        NettyTransmissionUtil.infoMessage(null, "Try to bind connection, host = [", host, "], port = [", port, "] at [", bindConnectionTimes.get(), "] times...");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, NettyTransmissionConnState.SERVER_BINDING);
        try {
            ChannelFuture connFuture = this.bootstrap.bind(port).sync().addListener((ChannelFuture future) -> {
                this.putMDC(future.channel());
                if (future.isSuccess()) {
                    Channel channel = future.channel();
                    int connectTimes = bindConnectionTimes.get();
                    NettyTransmissionUtil.infoMessage(channel, "Establish connection succeed at [", connectTimes, "] times, and begin listen host = [", host, "], port = [", port, "]");
                    notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), channel, NettyTransmissionConnState.SERVER_BOUND);
                } else {
                    NettyTransmissionUtil.errorMessage(null, future.cause(), "Establish connection failed, host = [", host, "], port = [", port, "] at [", bindConnectionTimes.get(), "] times");
                    notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, NettyTransmissionConnState.SERVER_BIND_FAILED);
                }
            });
            // connFuture.channel().closeFuture().sync();
            // connFuture.channel().closeFuture().addListener((ChannelFuture closeFut) -> {
            //     this.scheduleToReestablishConnection(closeFut, this.reestablishConnectionAfterTerminateConnection);
            // });
            connFuture.channel().closeFuture().addListener((ChannelFuture closeFut) -> {
                closeFut.channel().close();
            });
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".establishConnection"));
            sendEMS(this.logContext);
            throw ExceptionUtil.createRuntimeException(e, e.getMessage());
        }
    }

    // private void scheduleToReestablishConnection(ChannelFuture closeFut, boolean reestablishConnectionAfterTerminateConnection) {
    //     this.putMDC(null);
    //     if (reestablishConnectionAfterTerminateConnection) {
    //         String host = this.configuration.getHost();
    //         int port = this.configuration.getPort();
    //         long delay = this.configuration.getReestablishConnectionInterval();
    //         NettyTransmissionUtil.warnMessage(null, "Try to re-bind connection, host = [", host, "], port = [", port, "] at [", bindConnectionTimes.incrementAndGet(), "] times after [", delay, "] milliseconds");
    //         closeFut.channel().eventLoop().schedule(() -> {
    //             this.putMDC(null);
    //             LogHelperFactory.getTraceLogger().trace(this.configuration.getGateway().name(), "GW Begin Retry Bind Socket...");
    //             initialization();
    //             establishConnection();
    //         }, delay - GatewayCodeConstant.FIXED_MILLISECONDS, TimeUnit.MILLISECONDS);
    //     }
    // }

    @Override
    public void terminateConnection() {
        this.putMDC(null);
        String host = this.configuration.getHost();
        int port = this.configuration.getPort();
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, NettyTransmissionConnState.SERVER_SHUTTING_DOWN);
        NettyTransmissionUtil.infoMessage(null, "[Terminate Connection] Try to stop listen, host = [", host, "], port = [", port, "]...");
        if (handlerAdapter != null) {
            handlerAdapter.destroy();
        }
        if (this.bisEventExecutorGroup != null) {
            this.bisEventExecutorGroup.shutdownGracefully();
            this.bisEventExecutorGroup = null;
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
        }
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
            this.bossGroup = null;
        }
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), null, NettyTransmissionConnState.SERVER_SHUT_DOWN);
        NettyTransmissionUtil.infoMessage(null, "[Terminate Connection] Stop listen host = [", host, "], port = [", port, "] successful");
        notification.removeConnStateListener(this.configuration.getNettyTransmissionNotificationKey(), this);
        manager.removeTransmission(this);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state) {
        this.currentConnState.set(state);
        NettyTransmissionUtil.infoMessage(channel, "connection state changed, listener = [", this.ProgramName, "], status = [", state, "]");
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        this.currentConnState.set(state);
        NettyTransmissionUtil.infoMessage(channel, "connection state changed, listener = [", this.ProgramName, "], status = [", state, "], exception message = [", (t != null ? t.getMessage() : StringUtils.EMPTY), "]");
    }

    public void setReestablishConnectionAfterTerminateConnection(boolean reestablishConnectionAfterTerminateConnection) {
        this.reestablishConnectionAfterTerminateConnection = reestablishConnectionAfterTerminateConnection;
    }

    /**
     * 預設可切換的Single憑證
     *
     * @param logData
     * @param ch
     * @return
     * @throws Exception
     */
    protected SslHandler getSslHandler(LogData logData, SocketChannel ch) throws Exception {
        SslHandler sslHandler = NettyTransmissionUtil.getSslHandler(getCurrentSslKeyTrust(), configuration.isSslNeedClientAuth(), configuration.isSslWantClientAuth(), false); // 服務端認證方式
        return sslHandler;
    }

    /**
     * 拒絕Client連入, 由子類覆寫
     *
     * @param ch
     * @return
     */
    protected boolean channelRejected(SocketChannel ch) {
        return false;
    }

    /**
     * 預設取第1個憑證
     *
     * @return
     */
    public SslKeyTrust getCurrentSslKeyTrust() {
        return CollectionUtils.isEmpty(this.configuration.getSslConfigs()) ? null : this.configuration.getSslConfigs().get(0);
    }

    public NettyTransmissionServerMonitor<Configuration> getTransmissionServerMonitor() {
        return this.transmissionServerMonitor;
    }

    /**
     * 處理異常的類
     *
     * @return
     */
    protected NettyTransmissionChannelInboundHandlerAdapterServerException<Configuration, ProcessRequestManager, ProcessRequest> getExceptionHandlerAdapter() {
        if (this.exceptionHandlerAdapter == null) {
            this.exceptionHandlerAdapter = new NettyTransmissionChannelInboundHandlerAdapterServerException<Configuration, ProcessRequestManager, ProcessRequest>();
            this.exceptionHandlerAdapter.initialization(this.configuration, this.requestManager);
        }
        return this.exceptionHandlerAdapter;
    }

    @Override
    public void closeConnection() {
        // TODO
    }
}
