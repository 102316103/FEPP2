package com.syscom.fep.notify.common;

import java.util.Map;

public abstract class SenderBase<T> {
    public abstract void send(Map<String, T> content) throws Exception ;
}
