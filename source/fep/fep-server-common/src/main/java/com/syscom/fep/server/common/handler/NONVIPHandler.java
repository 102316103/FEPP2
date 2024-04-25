package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.enums.FEPChannel;

public class NONVIPHandler extends HandlerBase{
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) throws Exception {
        return null;
    }

    @Override
    public String dispatch(FEPChannel channel, String data){
        return null;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }
}
