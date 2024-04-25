package com.syscom.fep.vo.text.unisys.hk;

public class HKGeneral {
    private HKGeneralRequest mRequest;
    private HKGeneralResponse mResponse;

    public HKGeneral()
    {
        mRequest = new HKGeneralRequest();
        mResponse = new HKGeneralResponse();
    }

    public HKGeneralRequest getmRequest() {
        return mRequest;
    }

    public void setmRequest(HKGeneralRequest mRequest) {
        this.mRequest = mRequest;
    }

    public HKGeneralResponse getmResponse() {
        return mResponse;
    }

    public void setmResponse(HKGeneralResponse mResponse) {
        this.mResponse = mResponse;
    }
}
