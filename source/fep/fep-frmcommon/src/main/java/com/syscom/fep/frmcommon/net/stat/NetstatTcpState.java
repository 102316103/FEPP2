package com.syscom.fep.frmcommon.net.stat;

public enum NetstatTcpState {
    UNKNOWN,
    CLOSED,
    LISTEN,
    SYN_SENT,
    SYN_RCVD,
    ESTABLISHED,
    FIN_WAIT_1,
    FIN_WAIT_2,
    CLOSE_WAIT,
    CLOSING,
    LAST_ACK,
    TIME_WAIT,
    NONE;
}
