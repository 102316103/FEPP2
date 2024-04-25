package com.syscom.fep.ws.client;

import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.ws.client.entity.WsClientType;
import com.syscom.fep.ws.client.entity.fisc.FISCMessageOut;
import com.syscom.fep.ws.client.fisc.WsFISCClient;
import com.syscom.fep.ws.client.t24.WsT24Client;
import org.springframework.stereotype.Component;

@Component
public class WsClientFactory {

    @SuppressWarnings("unchecked")
    public <MessageOut, MessageIn> MessageIn sendReceive(WsClientType clientType, String uri, MessageOut messageOut) throws Exception {
        switch (clientType) {
            case FISC:
                WsFISCClient fiscClient = new WsFISCClient(uri);
                return (MessageIn) fiscClient.sendReceive((FISCMessageOut) messageOut);
            case T24:
                WsT24Client t24Client = new WsT24Client(uri);
                return (MessageIn) t24Client.sendReceive((String) messageOut);
            default:
                throw ExceptionUtil.createIllegalArgumentException("Unsupported WsClientType = [", clientType.name(), "]");
        }
    }
}
