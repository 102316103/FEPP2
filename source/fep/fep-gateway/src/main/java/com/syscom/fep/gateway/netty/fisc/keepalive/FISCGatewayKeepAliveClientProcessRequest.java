package com.syscom.fep.gateway.netty.fisc.keepalive;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestClient;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class FISCGatewayKeepAliveClientProcessRequest extends NettyTransmissionChannelProcessRequestClient<FISCGatewayKeepAliveClientConfiguration> {
    private ScheduledFuture<?> sendKeepAliveFuture, runSecondaryGatewayFuture;

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state) {
        super.connStateChanged(channel, state);
        // 當連線成功後
        if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            // 開始定時送KeepAlive電文
            this.restartSendKeepAliveFuture(channel, this.configuration.getStart(), false);
        }
        // 關閉的時候, 要將timer停掉
        else if (state == NettyTransmissionConnState.CLIENT_SHUTTING_DOWN) {
            // cancel定時送KeepAlive電文的任務
            this.restartSendKeepAliveFuture(channel, this.configuration.getInterval(), true);
            // cancel啟動Secondary Gateway的任務
            this.restartRunSecondaryGatewayFuture(channel, true);
        }
    }

    /**
     * 處理Client進來的電文
     *
     * @param ctx
     * @param bytes
     * @throws Exception
     */
    @Override
    public void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        if (ArrayUtils.isNotEmpty(bytes)) {
            String keepAlive = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
            // 如果收到KeepAlive Ack
            if (GatewayCodeConstant.FISCGWKeepAliveResponse.equals(keepAlive)) {
                // 再次發送KeepAlive電文
                this.restartSendKeepAliveFuture(ctx.channel(), this.configuration.getInterval(), false);
            }
        }
    }

    /**
     * 執行delay任務送KeepAlive
     *
     * @param channel
     * @param delay
     * @param onlyCancel
     */
    private void restartSendKeepAliveFuture(Channel channel, long delay, boolean onlyCancel) {
        // 若未設代表不做keepalive功能
        if (delay <= 0) {
            return;
        }
        // cancel啟動Secondary Gateway的任務
        this.restartRunSecondaryGatewayFuture(channel, true);
        if (runSecondaryGatewayFuture != null) {
            try {
                runSecondaryGatewayFuture.cancel(false);
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            } finally {
                runSecondaryGatewayFuture = null;
            }
        }
        if (!onlyCancel) {
            runSecondaryGatewayFuture = channel.eventLoop().schedule(() -> {
                this.putMDC();
                this.logContext.clear();
                this.logContext.setRemark(StringUtils.join("Send KeepAlive"));
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartSendKeepAliveFuture"));
                this.logMessage(this.logContext);
                // 送KeepAlive
                if (NettyTransmissionConnState.isClientConnected(this.currentConnState.get())) {
                    NettyTransmissionUtil.sendPlainMessage(this, this.configuration, channel, GatewayCodeConstant.FISCGWKeepAliveRequest);
                }
                // 開始delay執行啟動Secondary Gateway
                this.restartRunSecondaryGatewayFuture(channel, false);
            }, delay, TimeUnit.SECONDS);
        }
    }

    /**
     * 執行delay任務啟動Secondary Gateway
     * <p>
     * 如果沒有收到KeepAlive回應
     * 1. 啟動Secondary
     * 2. 並停止送KeepAlive, 並且斷開長連線
     *
     * @param channel
     * @param onlyCancel
     */
    private void restartRunSecondaryGatewayFuture(Channel channel, boolean onlyCancel) {
        if (sendKeepAliveFuture != null) {
            try {
                sendKeepAliveFuture.cancel(false);
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            } finally {
                sendKeepAliveFuture = null;
            }
        }
        if (!onlyCancel) {
            sendKeepAliveFuture = channel.eventLoop().schedule(() -> {
                this.putMDC();
                this.logContext.setRemark(StringUtils.join("No KeepAlive Response received in ", configuration.getTimeout(), " seconds, start to run Secondary Gateway"));
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".restartRunSecondaryGatewayFuture"));
                sendEMS(logContext);
                // 啟動Secondary
                this.runSecondaryGateway();
                // 並停止送KeepAlive, 並且斷開長連線
                notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), channel, NettyTransmissionConnState.CHANNEL_CLOSE_BY_MANUAL);
            }, configuration.getTimeout(), TimeUnit.SECONDS);
        }
    }

    /**
     * 啟動Secondary
     */
    private void runSecondaryGateway() {
        // 則啟動Secondary
        FISCGatewayManager manager = SpringBeanFactoryUtil.getBean(FISCGatewayManager.class);
        manager.runSecondaryGateway(false);
    }
}
