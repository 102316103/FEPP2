package com.syscom.fep.server.netty.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.syscom.fep.invoker.netty.SimpleNettyServerConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.server.netty.atm")
@RefreshScope
public class ATMNettyServerConfiguration extends SimpleNettyServerConfiguration {}