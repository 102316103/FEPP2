package com.syscom.fep.invoker.netty;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Map;

public class SimpleNettyBaseMethod extends FEPBase {

    /**
     * 程式或者服務名稱
     *
     * @return
     */
    public String getName() {
        String mdcProfile = LogMDC.get(Const.MDC_PROFILE);
        return StringUtils.isNotBlank(mdcProfile) ? mdcProfile : ProgramName;
    }

    protected void putMDC(Channel channel) {
        LogMDC.put(LogMDC.getAll(Const.MDC_KEPT));
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        if (channel != null) {
            AttributeKey<Map<String, String>> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MDCMap.name());
            if (channel.hasAttr(key)) {
                Attribute<Map<String, String>> attr = channel.attr(key);
                LogMDC.put(attr.get());
            }
        }
    }

    @Override
    protected void infoMessage(Object... messages) {
        this.infoMessage(null, messages);
    }

    protected void infoMessage(Channel channel, Object... messages) {
        super.infoMessage("[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void debugMessage(Object... messages) {
        this.debugMessage(null, messages);
    }

    protected void debugMessage(Channel channel, Object... messages) {
        super.debugMessage("[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void warnMessage(Object... messages) {
        this.warnMessage((Channel) null, messages);
    }

    protected void warnMessage(Channel channel, Object... messages) {
        super.warnMessage("[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void warnMessage(Throwable t, Object... messages) {
        this.warnMessage((Channel) null, t, messages);
    }

    protected void warnMessage(Channel channel, Throwable t, Object... messages) {
        super.warnMessage(t, "[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    @Override
    protected void errorMessage(Throwable t, Object... messages) {
        this.errorMessage(null, t, messages);
    }

    protected void errorMessage(Channel channel, Throwable t, Object... messages) {
        super.errorMessage(t, "[", getName(), "]", channelInfo(channel), StringUtils.join(messages));
    }

    public static String channelInfo(Channel channel) {
        if (channel == null)
            return "|||||||";
        StringBuilder sb = new StringBuilder();
        sb.append("|").append(getRemoteIp(channel));
        sb.append("|").append(getRemotePort(channel));
        sb.append("|").append(FEPConfig.getInstance().getHostName());
        sb.append("|").append(FEPConfig.getInstance().getHostIp());
        sb.append("|").append(channel.localAddress() != null ? ((InetSocketAddress) channel.localAddress()).getPort() : 0);
        sb.append("|").append(channel.id() != null ? channel.id().asShortText() : StringUtils.EMPTY);
        sb.append("|");
        return sb.toString();
    }

    public static String getRemoteIp(Channel channel) {
        InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
        return remoteAddr != null ? ReflectUtil.envokeMethod(remoteAddr.getAddress(), "getHostAddress", StringUtils.EMPTY) : StringUtils.EMPTY;
    }

    public static int getRemotePort(Channel channel) {
        InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
        return remoteAddr != null ? remoteAddr.getPort() : -1;
    }

    public static void setChannelMDCMap(Channel channel) {
        if (channel == null) return;
        AttributeKey<Map<String, String>> key = AttributeKey.valueOf(SimpleNettyAttributeKey.MDCMap.name());
        Attribute<Map<String, String>> attr = channel.attr(key);
        attr.set(LogMDC.getAll(Const.MDC_KEPT));
    }
}
