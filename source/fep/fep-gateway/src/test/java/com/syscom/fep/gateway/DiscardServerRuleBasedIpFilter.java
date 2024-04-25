package com.syscom.fep.gateway;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.RuleBasedIpFilter;

import java.net.InetSocketAddress;

public class DiscardServerRuleBasedIpFilter extends RuleBasedIpFilter {
    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        return false;
    }
}
