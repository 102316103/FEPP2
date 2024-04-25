package com.syscom.fep.gateway.netty.fisc;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionConnStateListener;
import com.syscom.fep.gateway.netty.NettyTransmissionNotification;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiver;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverChannelInboundHandlerAdapter;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.receiver.FISCGatewayClientReceiverProcessRequest;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSender;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderChannelInboundHandlerAdapter;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderConfiguration;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderProcessRequest;
import com.syscom.fep.gateway.netty.fisc.server.*;
import io.netty.channel.Channel;

/**
 * 財經Socket物件組
 * 一個Sender
 * 一個Receiver
 * 一個Server
 *
 * @author Richard
 */
public class FISCGatewayGroup extends FEPBase implements NettyTransmissionConnStateListener {
    private final FISCGatewayMode mode;
    private final FISCGatewayClientSenderConfiguration senderConfiguration;
    private final FISCGatewayClientReceiverConfiguration receiverConfiguration;
    private final FISCGatewayServerConfiguration serverConfiguration;
    private FISCGatewayClientSender sender;
    private FISCGatewayClientReceiver receiver;
    private FISCGatewayServer server;
    private FISCGatewayClientSenderProcessRequest senderProcessRequest;
    private final NettyTransmissionNotification notification = SpringBeanFactoryUtil.getBean(NettyTransmissionNotification.class);
    private final Object lock = new Object();

    public FISCGatewayGroup(FISCGatewayMode mode, FISCGatewayClientSenderConfiguration senderConfiguration, FISCGatewayClientReceiverConfiguration receiverConfiguration, FISCGatewayServerConfiguration serverConfiguration) {
        this.mode = mode;
        this.senderConfiguration = senderConfiguration;
        this.receiverConfiguration = receiverConfiguration;
        this.serverConfiguration = serverConfiguration;
    }

    public FISCGatewayMode getMode() {
        return mode;
    }

    /**
     * 運行
     */
    public void run(boolean postConstruct) {
        this.logContext.clear();
        // 啟動sender
        this.runSender(this.senderConfiguration, postConstruct);
        // 啟動receiver
        this.runReceiver(this.receiverConfiguration, postConstruct);
    }

    /**
     * 停止
     */
    public void stop() {
        // 停止server
        if (this.server != null) {
            this.server.terminateConnection();
            this.server = null;
        }
        // 停止receiver
        if (this.receiver != null) {
            this.receiver.terminateConnection();
            this.receiver = null;
        }
        // 停止sender
        if (this.sender != null) {
            this.sender.terminateConnection();
            this.sender = null;
        }
    }

    /**
     * 啟動sender建立與財經的連線
     *
     * @param configuration
     * @param postConstruct
     */
    private void runSender(FISCGatewayClientSenderConfiguration configuration, boolean postConstruct) {
        // Processor, 必須
        this.senderProcessRequest = new FISCGatewayClientSenderProcessRequest();
        this.senderProcessRequest.initialization(configuration);
        // HandlerAdapter, 必須
        FISCGatewayClientSenderChannelInboundHandlerAdapter adapter = new FISCGatewayClientSenderChannelInboundHandlerAdapter();
        adapter.initialization(configuration, this.senderProcessRequest);
        // Gateway, 必須
        this.sender = new FISCGatewayClientSender();
        this.sender.initialization(configuration, adapter);
        // postConstruct下會自動啟動Gateway程式, 所以這裡判斷一下, 只有在非postConstruct下才需要呼叫run方法
        if (!postConstruct) {
            this.sender.run();
        }
        // 監聽sender的狀態
        notification.addConnStateListener(configuration.getNettyTransmissionNotificationKey(), this);
    }

    /**
     * 啟動receiver建立與財經的連線
     *
     * @param configuration
     * @param postConstruct
     */
    private void runReceiver(FISCGatewayClientReceiverConfiguration configuration, boolean postConstruct) {
        // Processor, 必須
        FISCGatewayClientReceiverProcessRequest processRequest = new FISCGatewayClientReceiverProcessRequest();
        processRequest.initialization(configuration);
        // HandlerAdapter, 必須
        FISCGatewayClientReceiverChannelInboundHandlerAdapter adapter = new FISCGatewayClientReceiverChannelInboundHandlerAdapter();
        adapter.initialization(configuration, processRequest);
        // Gateway, 必須
        this.receiver = new FISCGatewayClientReceiver();
        this.receiver.initialization(configuration, adapter);
        if (!postConstruct) {
            this.receiver.run();
        }
        // 監聽receiver的狀態
        notification.addConnStateListener(configuration.getNettyTransmissionNotificationKey(), this);
    }

    /**
     * 啟動server接收FISCAdapter丟過來的電文
     *
     * @param configuration
     */
    private void runServer(FISCGatewayServerConfiguration configuration) {
        // ProcessorManager, 必須
        FISCGatewayServerProcessRequestManager manager = new FISCGatewayServerProcessRequestManager();
        manager.initialization(configuration, this.senderProcessRequest);
        // HandlerAdapter, 必須
        FISCGatewayServerChannelInboundHandlerAdapter adapter = new FISCGatewayServerChannelInboundHandlerAdapter();
        adapter.initialization(configuration, manager);
        // IP過濾規則, 必須
        FISCGatewayServerRuleIpFilter ruleIpFilter = new FISCGatewayServerRuleIpFilter();
        ruleIpFilter.initialization(configuration);
        // Gateway, 必須
        this.server = new FISCGatewayServer();
        this.server.initialization(configuration, adapter, manager, ruleIpFilter);
        this.server.run();
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state) {
        synchronized (lock) {
            // 當sender和receiver都成功連到財經時, 才啟動server
            if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
                if (sender != null && sender.getTransmissionClientMonitor().getConnState() == NettyTransmissionConnState.CLIENT_CONNECTED
                        && receiver != null && receiver.getTransmissionClientMonitor().getConnState() == NettyTransmissionConnState.CLIENT_CONNECTED
                        && this.server == null) {
                    LogHelperFactory.getGeneralLogger().info("Both Sender and Receiver has been connected to FISC, start to build Socket Server connection for FISCAdapter");
                    this.runServer(this.serverConfiguration);
                }
            }
        }
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {}

    public FISCGatewayClientSender getSender() {
        return sender;
    }

    public FISCGatewayClientReceiver getReceiver() {
        return receiver;
    }

    public FISCGatewayServer getServer() {
        return server;
    }
}
