package com.syscom.fep.frmcommon.socket;

import java.util.EventListener;

public interface SckConnStateListener extends EventListener {

    /**
     * 通知連線狀態發生改變
     *
     * @param state
     */
    void connStateChanged(SckConnState state);

    /**
     * 通知連線狀態發生改變, 並包含異常訊息
     *
     * @param state
     * @param t
     */
    default void connStateChanged(SckConnState state, Throwable t) {}

}
