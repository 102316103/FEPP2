package com.syscom.fep.server.netty.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.syscom.fep.invoker.netty.SimpleNettyServerConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.server.netty.web.atm")
@RefreshScope
public class WebATMNettyServerConfiguration extends SimpleNettyServerConfiguration {}
