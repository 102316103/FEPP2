package com.syscom.fep.service.monitor.svr;

import com.syscom.fep.invoker.netty.SimpleNettyClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.service.monitor.suip")
@RefreshScope
public class MonitorSuipConnectClientConfiguration extends SimpleNettyClientConfiguration {}
