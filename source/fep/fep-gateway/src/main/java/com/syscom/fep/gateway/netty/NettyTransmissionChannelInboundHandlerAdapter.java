package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public abstract class NettyTransmissionChannelInboundHandlerAdapter<Configuration extends NettyTransmissionConfiguration> extends ChannelInboundHandlerAdapter {
    protected final String ProgramName = this.getClass().getSimpleName();

    @Autowired
    protected Configuration configuration;
    @Autowired
    protected NettyTransmissionNotification notification;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    public void initialization(Configuration configuration) {
        this.notification = SpringBeanFactoryUtil.getBean(NettyTransmissionNotification.class);
        this.configuration = configuration;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.putMDC(ctx);
        if (evt instanceof IdleStateEvent) {
            //--20221219 ben 暫將讀取資料庫部份全mark
            // NettyTransmissionUtil.infoMessage(ctx.channel(), "IdleStateEvent occur...");
            //--20221219 ben 暫將讀取資料庫部份全mark
        } else if (evt instanceof SslHandshakeCompletionEvent) {
            NettyTransmissionUtil.infoMessage(ctx.channel(), "SslHandshakeCompletionEvent occur...");
            SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
            sslHandshakeCompletionEventTriggered(ctx, sslHandler, (SslHandshakeCompletionEvent) evt);
        }
    }

    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        this.putMDC(ctx);
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
            NettyTransmissionUtil.infoMessage(ctx.channel(), "憑證版本:", info);
            // 獲得憑證序列號
            info = cert.getSerialNumber().toString(16);
            NettyTransmissionUtil.infoMessage(ctx.channel(), "憑證序列號:", info);
            // 獲得憑證有效期
            Date beforedate = cert.getNotBefore();
            info = FormatUtil.dateFormat(beforedate);
            NettyTransmissionUtil.infoMessage(ctx.channel(), "憑證生效日期:", info);
            Date afterdate = cert.getNotAfter();
            info = FormatUtil.dateFormat(afterdate);
            NettyTransmissionUtil.infoMessage(ctx.channel(), "憑證失效日期:", info);
            // 獲得憑證主體信息
            info = cert.getSubjectDN().getName();
            NettyTransmissionUtil.infoMessage(ctx.channel(), "憑證擁有者:", info);
            // 獲得憑證頒發者信息
            info = cert.getIssuerDN().getName();
            NettyTransmissionUtil.infoMessage(ctx.channel(), "憑證頒發者:", info);
            // 獲得憑證籤名算法名稱
            info = cert.getSigAlgName();
            NettyTransmissionUtil.infoMessage(ctx.channel(), "憑證籤名算法:", info);
        } catch (SSLPeerUnverifiedException e) {
            NettyTransmissionUtil.warnMessage(ctx.channel(), e, e.getMessage());
        }
    }

    protected void putMDC(ChannelHandlerContext ctx) {
        NettyTransmissionUtil.putMDC(ctx, this.configuration);
    }

    /**
     * 拆解電文
     *
     * @param ctx
     * @param bytes
     * @return
     */
    protected List<byte[]> disassembleTransmissionMessage(ChannelHandlerContext ctx, byte[] bytes) {
        // 預設不拆解, 由子類根據實際需要實作
        return Collections.singletonList(bytes);
    }
}
