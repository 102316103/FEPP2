package com.syscom.fep.invoker.netty.impl;

import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.invoker.netty.SimpleNettyClientShort;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import com.syscom.fep.vo.communication.BaseCommu;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

/**
 * 發送socket到FEP ATM
 *
 * @author Richard
 */
public class ToFEPATMNettyClient extends SimpleNettyClientShort<ToFEPATMNettyClientConfiguration, String, BaseCommu> {

    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] messageOutToBytes(BaseCommu messageOut) {
        return ConvertUtil.toBytes(messageOut.toString(), StandardCharsets.UTF_8);
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
}
