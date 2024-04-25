package com.syscom.fep.server.gateway.ims;

import com.syscom.fep.frmcommon.util.UUIDUtil;

public class IMSGatewayProcessorGroup {
    private final String identity = UUIDUtil.randomUUID();
    private final IMSGatewayProcessor sender;
    private final IMSGatewayProcessor receiver;

    public IMSGatewayProcessorGroup(IMSGatewayProcessor sender, IMSGatewayProcessor receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public IMSGatewayProcessor getSender() {
        return sender;
    }

    public IMSGatewayProcessor getReceiver() {
        return receiver;
    }

    public void terminate() {
        if (receiver != null) {
            receiver.terminate();
        }
        if (sender != null) {
            sender.terminate();
        }
    }
}
