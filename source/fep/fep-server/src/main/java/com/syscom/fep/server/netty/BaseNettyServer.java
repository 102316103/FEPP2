package com.syscom.fep.server.netty;

import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.invoker.netty.*;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public abstract class BaseNettyServer<Configuration extends SimpleNettyServerConfiguration, Processor extends SimpleNettyServerProcessor<String, String>>
        extends SimpleNettyServer<Configuration, Processor, String, String> {

    private final SimpleNettyServerProcessorSingletonManager<Processor> manager = new SimpleNettyServerProcessorSingletonManager<>(this.getProcessorCls());

    @Override
    protected SimpleNettyProcessorManager<Processor> getProcessorManager() {
        return manager;
    }

    @Override
    public String getName() {
        return manager.getProcessor().getName();
    }

    @Override
    protected String bytesToMessageIn(byte[] bytes) {
        return ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] messageOutToBytes(String messageOut) {
        return ConvertUtil.toBytes(messageOut, StandardCharsets.UTF_8);
    }

    /**
     * @param channel
     * @param state
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state) {}

    /**
     * @param channel
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Channel channel, SimpleNettyConnState state, Throwable t) {}
}
