package com.syscom.fep.frmcommon.jms.entity;

import java.io.Serializable;

public class JmsMonitorRequest<T extends Serializable> implements Serializable {
    private T data;

    public JmsMonitorRequest() {}

    public JmsMonitorRequest(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
