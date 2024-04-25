package com.syscom.fep.frmcommon.jms.entity;

public enum JmsMonitorResponseErrorCode {
    INVALID_PARAMETER_CONCURRENCY_CANNOT_BE_EMPTY,
    CONSUMERS_NOT_EXIST,
    SET_CONCURRENCY_FAILED,
    CANNOT_REDUCE_CONCURRENCY;
}
