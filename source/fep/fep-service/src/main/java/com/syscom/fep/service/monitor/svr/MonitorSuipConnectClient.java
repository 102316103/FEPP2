package com.syscom.fep.service.monitor.svr;

import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.invoker.netty.SimpleNettyClientShort;
import com.syscom.fep.invoker.netty.SimpleNettyConnState;
import io.netty.channel.Channel;

/**
 * 連接suip的Socket物件
 *
 * @author Richard
 */
public class MonitorSuipConnectClient extends SimpleNettyClientShort<MonitorSuipConnectClientConfiguration, String, String> {
    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return StringUtil.toHex(bytes);
    }

    @Override
    protected byte[] messageOutToBytes(String s) {
        return ConvertUtil.hexToBytes(s);
    }

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state) {}

    /**
     * 狀態發生改變
     *
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {}
}
