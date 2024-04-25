package com.syscom.fep.gateway;

import com.syscom.fep.frmcommon.ssl.SslKeyTrustType;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.invoker.netty.SimpleNettyClient;
import com.syscom.fep.invoker.netty.SimpleNettyClientConfiguration;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class NettySSLClient extends GatewayBaseTest {

    @Test
    public void test() throws Exception {
//        for (int index = 0; index < 2; index++) {
//            startClient(index);
//            try {
//                Thread.sleep(5000L);
//            } catch (InterruptedException e) {
//                logger.warn(e, e.getMessage());
//            }
//        }
//        startClient(0, true, true);
        NettySSLClientImpl client = new NettySSLClientImpl(false);
        client.run(getNettySSLClientConfiguration3());
        client.sendReceive("Hello, i am NettySSLClient", 0);
        Thread.sleep(Long.MAX_VALUE);
    }

    private void startClient(int index, boolean attemptToBuildConnectionAfterFailed, boolean sendReceive) throws Exception {
        Executors.newCachedThreadPool().execute(() -> {
            NettySSLClientImpl client = new NettySSLClientImpl(attemptToBuildConnectionAfterFailed);
            //client.run(index % 2 == 0 ? getNettySSLClientConfiguration1() : getNettySSLClientConfiguration2());
            client.run(getNettySSLClientConfiguration());
            if (!sendReceive) return;
            while (true) {
                try {
                    client.sendReceive("Hello, i am NettySSLClient(" + index + ")!!! at " + FormatUtil.dateTimeFormat(Calendar.getInstance().getTime()), 0);
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                }
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    logger.warn(e, e.getMessage());
                }
            }
        });
    }

    private static NettySSLClientImpl.NettySSLClientConfiguration getNettySSLClientConfiguration() {
        NettySSLClientImpl.NettySSLClientConfiguration configuration = new NettySSLClientImpl.NettySSLClientConfiguration();
        configuration.setHost("127.0.0.1");
        configuration.setPort(18090);
        return configuration;
    }

    private static NettySSLClientImpl.NettySSLClientConfiguration getNettySSLClientConfiguration1() {
        NettySSLClientImpl.NettySSLClientConfiguration configuration = new NettySSLClientImpl.NettySSLClientConfiguration();
        configuration.setHost("127.0.0.1");
        configuration.setPort(18090);
        configuration.setSslTrustPath("atmgw-certificate.p12");
        configuration.setSslTrustSscode("syscom");
        configuration.setSslTrustType(SslKeyTrustType.PKCS12);
        configuration.setSslKeyPath("atmgw-certificate.p12");
        configuration.setSslKeySscode("syscom");
        configuration.setSslKeyType(SslKeyTrustType.PKCS12);
        return configuration;
    }

    private static NettySSLClientImpl.NettySSLClientConfiguration getNettySSLClientConfiguration2() {
        NettySSLClientImpl.NettySSLClientConfiguration configuration = new NettySSLClientImpl.NettySSLClientConfiguration();
        configuration.setHost("127.0.0.1");
        configuration.setPort(18090);
        configuration.setSslTrustPath("atmgw-certificate_2.p12");
        configuration.setSslTrustSscode("syscom");
        configuration.setSslTrustType(SslKeyTrustType.PKCS12);
        configuration.setSslKeyPath("atmgw-certificate_2.p12");
        configuration.setSslKeySscode("syscom");
        configuration.setSslKeyType(SslKeyTrustType.PKCS12);
        return configuration;
    }

    private static NettySSLClientImpl.NettySSLClientConfiguration getNettySSLClientConfiguration3() {
        NettySSLClientImpl.NettySSLClientConfiguration configuration = new NettySSLClientImpl.NettySSLClientConfiguration();
        configuration.setHost("127.0.0.1");
        configuration.setPort(18090);
        configuration.setSslTrustPath("C:/Users/Richard/Desktop/tcb_cert/fep-atmgwd.p12");
        configuration.setSslTrustSscode("syscom");
        configuration.setSslTrustType(SslKeyTrustType.PKCS12);
        configuration.setSslKeyPath("C:/Users/Richard/Desktop/tcb_cert/fep-atmgwd.p12");
        configuration.setSslKeySscode("syscom");
        configuration.setSslKeyType(SslKeyTrustType.PKCS12);
        return configuration;
    }

    public static class NettySSLClientImpl extends SimpleNettyClient<NettySSLClientImpl.NettySSLClientConfiguration, String, String> {
        private boolean attemptToBuildConnectionAfterFailed;

        public NettySSLClientImpl(boolean attemptToBuildConnectionAfterFailed) {
            this.attemptToBuildConnectionAfterFailed = attemptToBuildConnectionAfterFailed;
        }

        /**
         * @param bytes
         * @return
         */
        @Override
        protected String bytesToMessageIn(byte[] bytes) {
            return ConvertUtil.toString(bytes, StandardCharsets.US_ASCII);
        }

        /**
         * @param s
         * @return
         */
        @Override
        protected byte[] messageOutToBytes(String s) {
            return ConvertUtil.toBytes(s, StandardCharsets.US_ASCII);
        }

        /**
         * @param channel
         * @param state
         */
        @Override
        public void connStateChanged(Channel channel, SimpleNettyConnState state) {
        }

        /**
         * @param channel
         * @param state
         * @param t
         */
        @Override
        public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {
        }

        @Override
        protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        }

        public static class NettySSLClientConfiguration extends SimpleNettyClientConfiguration {
        }

        @Override
        protected void scheduleToReestablishConnection(ChannelFuture future) {
            if (this.attemptToBuildConnectionAfterFailed) {
                super.scheduleToReestablishConnection(future);
            }
        }
    }
}
