package com.syscom.fep.gateway.configuration;

import com.syscom.fep.gateway.netty.NettyTransmission;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GatewayManager {
    private final List<NettyTransmission<?, ?, ?>> transmissionList = new ArrayList<>();

    public void establishAllConnection() {
        NettyTransmission<?, ?, ?>[] transmissions = null;
        synchronized (this.transmissionList) {
            transmissions = new NettyTransmission[this.transmissionList.size()];
            this.transmissionList.toArray(transmissions);
        }
        if (ArrayUtils.isNotEmpty(transmissions)) {
            for (NettyTransmission<?, ?, ?> transmission : transmissions) {
                transmission.run();
            }
        }
    }

    public void addTransmission(NettyTransmission<?, ?, ?> transmission) {
        synchronized (this.transmissionList) {
            this.transmissionList.add(transmission);
        }
    }

    public void removeTransmission(NettyTransmission<?, ?, ?> transmission) {
        synchronized (this.transmissionList) {
            this.transmissionList.remove(transmission);
        }
    }
}
