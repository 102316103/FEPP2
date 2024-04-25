package com.syscom.fep.vo.text.unisys.mo;

public class MOGeneral {
    private MOGeneralRequest request;
    private MOGeneralResponse response;

    public MOGeneral() {
        this.request = new MOGeneralRequest();
        this.response = new MOGeneralResponse();
    }

    public MOGeneralRequest getRequest() {
        return request;
    }

    public void setRequest(MOGeneralRequest request) {
        this.request = request;
    }

    public MOGeneralResponse getResponse() {
        return response;
    }

    public void setResponse(MOGeneralResponse response) {
        this.response = response;
    }
}
