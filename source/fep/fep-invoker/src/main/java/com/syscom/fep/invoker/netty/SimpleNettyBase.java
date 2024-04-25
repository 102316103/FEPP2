package com.syscom.fep.invoker.netty;

import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.GenericTypeUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public abstract class SimpleNettyBase<Configuration extends SimpleNettyConfiguration, MessageIn, MessageOut> extends SimpleNettyBaseMethod implements SimpleNettyConnStateListener {
    protected Class<Configuration> configurationCls = GenericTypeUtil.getGenericSuperClass(this.getClass(), 0);
    protected Configuration configuration;
    protected SimpleNettyNotification notification = new SimpleNettyNotification();

    @PostConstruct
    public void initSimpleNetty() {
        this.putMDC(null);
        this.run();
    }

    /**
     * run程式
     */
    public void run() {
        this.putMDC(null);
        this.configuration = SpringBeanFactoryUtil.registerBean(configurationCls);
        // this.infoMessage(configuration.toString());
        initialization();
        establishConnection();
    }

    /**
     * run程式並指定Configuration
     *
     * @param configuration
     */
    public void run(Configuration configuration) {
        this.putMDC(null);
        this.configuration = configuration;
        // this.infoMessage(configuration.toString());
        this.initialization();
        this.establishConnection();
    }

    /**
     * 初始化動作
     */
    protected abstract void initialization();

    /**
     * 建立連線
     */
    protected abstract void establishConnection();

    /**
     * 斷開連線
     */
    protected abstract void closeConnection();

    /**
     * 終止
     */
    @PreDestroy
    protected abstract void terminateConnection();

    /**
     * 接收遠端的訊息進來
     *
     * @param bytes
     * @return
     */
    protected abstract MessageIn bytesToMessageIn(byte[] bytes);

    /**
     * 回應訊息給遠端
     *
     * @param messageOut
     * @return
     */
    protected abstract byte[] messageOutToBytes(MessageOut messageOut);

    /**
     * 定義SslHandler用於憑證連線
     *
     * @param channel
     * @param configuration
     * @param useClientMode
     * @return
     * @throws Exception
     */
    protected SslHandler getSslHandler(Channel channel, Configuration configuration, boolean useClientMode) throws Exception {
        this.putMDC(channel);
        SslContextFactory sslContextFactory = SpringBeanFactoryUtil.getBean(SslContextFactory.class);
        SSLEngine sslEngine = sslContextFactory.getSSLEngine(configuration, configuration.isSslNeedClientAuth(), configuration.isSslWantClientAuth(), useClientMode);
        return sslEngine == null ? null : new SslHandler(sslEngine);
    }

    /**
     * 發生SSL Handshake時列印憑證訊息
     *
     * @param ctx
     * @param sslHandler
     * @param evt
     */
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        this.putMDC(ctx.channel());
        if (!evt.isSuccess()) {
            return;
        }
        try {
            SSLSession session = sslHandler.engine().getSession();
            Certificate[] certificates = this.configuration.isSslNeedClientAuth() ? session.getPeerCertificates() : session.getLocalCertificates();
            if (ArrayUtils.isEmpty(certificates)) {
                return;
            }
            X509Certificate cert = (X509Certificate) certificates[0];
            // 獲得憑證版本
            String info = String.valueOf(cert.getVersion());
            infoMessage(ctx.channel(), "憑證版本:", info);
            // 獲得憑證序列號
            info = cert.getSerialNumber().toString(16);
            infoMessage(ctx.channel(), "憑證序列號:", info);
            // 獲得憑證有效期
            Date beforedate = cert.getNotBefore();
            info = FormatUtil.dateFormat(beforedate);
            infoMessage(ctx.channel(), "憑證生效日期:", info);
            Date afterdate = cert.getNotAfter();
            info = FormatUtil.dateFormat(afterdate);
            infoMessage(ctx.channel(), "憑證失效日期:", info);
            // 獲得憑證主體信息
            info = cert.getSubjectDN().getName();
            infoMessage(ctx.channel(), "憑證擁有者:", info);
            // 獲得憑證頒發者信息
            info = cert.getIssuerDN().getName();
            infoMessage(ctx.channel(), "憑證頒發者:", info);
            // 獲得憑證籤名算法名稱
            info = cert.getSigAlgName();
            infoMessage(ctx.channel(), "憑證籤名算法:", info);
        } catch (SSLPeerUnverifiedException e) {
            warnMessage(ctx.channel(), e, e.getMessage());
        }
    }
}
