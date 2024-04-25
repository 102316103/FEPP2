package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

@Validated
public class NettyTransmissionConfiguration {
    private String configurationPropertiesPrefix;
    private String NettyTransmissionNotificationKey;
    private String host;
    private int port;
    private long reestablishConnectionInterval = 3000L;
    private SocketType socketType;
    private int backlog;
    private int tcpKeepIdle = 120;
    private int tcpKeepInterval = 10;
    private int tcpKeepCount = 0;
    private Gateway gateway;
    private int timeout = 60000;
    private boolean sslNeedClientAuth;
    private boolean sslWantClientAuth;
    private int rcvBufAllocator = 4192;
    private int soSndBuf = 4192;
    private int soRcvBuf = 4192;
    private boolean soKeepalive = true;
    private boolean tcpNodelay = true;
    private boolean asyncDisassembled = true; // 如果進來的電文有粘連並且需要進行拆解, 則預設異步處理
    /**
     * WROKER線程數, 預設CPU核心數*2, 一般不需要特別設定
     */
    private int workerThreadNum = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    /**
     * 業務線程數, 預設0, 根據業務需要設定
     */
    private int bisThreadNum = 0;
    /**
     * 當電文發生粘連需要進行拆解時, 異步處理需要的線程池數量設定
     */
    private int disassembledThreadNum = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

    /**
     * 專門用於非SpringBean模式下初始化
     */
    public void initialization() {}

    public String getNettyTransmissionNotificationKey() {
        if (NettyTransmissionNotificationKey == null)
            NettyTransmissionNotificationKey = UUIDUtil.randomUUID(true);
        return NettyTransmissionNotificationKey;
    }

    public String getConfigurationPropertiesPrefix() {
        return configurationPropertiesPrefix;
    }

    public void setConfigurationPropertiesPrefix(String configurationPropertiesPrefix) {
        this.configurationPropertiesPrefix = configurationPropertiesPrefix;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getReestablishConnectionInterval() {
        return reestablishConnectionInterval;
    }

    public void setReestablishConnectionInterval(long reestablishConnectionInterval) {
        this.reestablishConnectionInterval = reestablishConnectionInterval;
    }

    public SocketType getSocketType() {
        return socketType;
    }

    public void setSocketType(SocketType socketType) {
        this.socketType = socketType;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getTcpKeepIdle() {
        return tcpKeepIdle;
    }

    public void setTcpKeepIdle(int tcpKeepIdle) {
        this.tcpKeepIdle = tcpKeepIdle;
    }

    public int getTcpKeepInterval() {
        return tcpKeepInterval;
    }

    public void setTcpKeepInterval(int tcpKeepInterval) {
        this.tcpKeepInterval = tcpKeepInterval;
    }

    public int getTcpKeepCount() {
        return tcpKeepCount;
    }

    public void setTcpKeepCount(int tcpKeepCount) {
        this.tcpKeepCount = tcpKeepCount;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isSslNeedClientAuth() {
        return sslNeedClientAuth;
    }

    public void setSslNeedClientAuth(boolean sslNeedClientAuth) {
        this.sslNeedClientAuth = sslNeedClientAuth;
    }

    public boolean isSslWantClientAuth() {
        return sslWantClientAuth;
    }

    public void setSslWantClientAuth(boolean sslWantClientAuth) {
        this.sslWantClientAuth = sslWantClientAuth;
    }

    public int getRcvBufAllocator() {
        return rcvBufAllocator;
    }

    public void setRcvBufAllocator(int rcvBufAllocator) {
        this.rcvBufAllocator = rcvBufAllocator;
    }

    public int getSoSndBuf() {
        return soSndBuf;
    }

    public void setSoSndBuf(int soSndBuf) {
        this.soSndBuf = soSndBuf;
    }

    public int getSoRcvBuf() {
        return soRcvBuf;
    }

    public void setSoRcvBuf(int soRcvBuf) {
        this.soRcvBuf = soRcvBuf;
    }

    public boolean isSoKeepalive() {
        return soKeepalive;
    }

    public void setSoKeepalive(boolean soKeepalive) {
        this.soKeepalive = soKeepalive;
    }

    public boolean isTcpNodelay() {
        return tcpNodelay;
    }

    public void setTcpNodelay(boolean tcpNodelay) {
        this.tcpNodelay = tcpNodelay;
    }

    public boolean isAsyncDisassembled() {
        return asyncDisassembled;
    }

    public void setAsyncDisassembled(boolean asyncDisassembled) {
        this.asyncDisassembled = asyncDisassembled;
    }

    public int getWorkerThreadNum() {
        return workerThreadNum;
    }

    public void setWorkerThreadNum(int workerThreadNum) {
        this.workerThreadNum = workerThreadNum;
    }

    public int getBisThreadNum() {
        return bisThreadNum;
    }

    public void setBisThreadNum(int bisThreadNum) {
        this.bisThreadNum = bisThreadNum;
    }

    public int getDisassembledThreadNum() {
        return disassembledThreadNum;
    }

    public void setDisassembledThreadNum(int disassembledThreadNum) {
        this.disassembledThreadNum = disassembledThreadNum;
    }

    @Override
    public String toString() {
        String prefix = null;
        ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
        if (annotation2 != null) {
            prefix = annotation2.prefix();
        }
        if (StringUtils.isBlank(prefix)) {
            prefix = this.configurationPropertiesPrefix;
        }
        if (StringUtils.isNotBlank(prefix)) {
            List<Field> fieldList = ReflectUtil.getAllFields(this);
            if (CollectionUtils.isNotEmpty(fieldList)) {
                int repeat = 2;
                // 列印配置檔內容
                StringBuilder sb = new StringBuilder();
                sb.append(this.getClass().getSimpleName()).append(":\r\n");
                for (Field field : fieldList) {
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    if ("configurationPropertiesPrefix".equals(field.getName())) {
                        continue;
                    }
                    ReflectionUtils.makeAccessible(field);
                    Object object = ReflectionUtils.getField(field, this);
                    String prefix2 = prefix == null ? StringUtils.EMPTY : StringUtils.join(prefix, ".");
                    if (object instanceof List) {
                        prefix2 = StringUtils.join(prefix2, field.getName());
                        List<?> list = (List<?>) object;
                        int i = 0;
                        for (Object e : list) {
                            print(e, StringUtils.join(prefix2, "[", i++, "]"), sb);
                        }
                    } else {
                        sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                                .append(prefix2)
                                .append(field.getName())
                                .append(" = ").append(object).append("\r\n");
                    }
                }
                return sb.toString();
            }
        }
        return StringUtils.join(this.getClass().getSimpleName(), ":\r\n", ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    private void print(Object object, String prefix, StringBuilder sb) {
        int repeat = 2;
        if ("java.lang".equals(object.getClass().getPackage().getName())) {
            sb.append(StringUtils.repeat(StringUtils.SPACE, 2))
                    .append(prefix)
                    .append(" = ")
                    .append(object)
                    .append("\r\n");
        } else {
            Field[] fields = object.getClass().getDeclaredFields();
            if (ArrayUtils.isNotEmpty(fields)) {
                for (Field field : fields) {
                    ReflectionUtils.makeAccessible(field);
                    Object value = ReflectionUtils.getField(field, object);
                    sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                            .append(prefix).append(".")
                            .append(field.getName()).append(" = ");
                    if (value != null &&
                            ("sslKeySscode".equals(field.getName()) || "sslTrustSscode".equals(field.getName()))) {
                        sb.append(StringUtils.repeat("*", ((String) value).length()));
                    } else {sb.append(value);}
                    sb.append("\r\n");
                }
            }
        }
    }
}