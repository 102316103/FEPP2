package com.syscom.fep.mybatis.logging;

import org.apache.ibatis.logging.slf4j.Slf4jImpl;

public class BaseJdbcLoggerActivationLogImpl extends Slf4jImpl {
    public BaseJdbcLoggerActivationLogImpl(String clazz) {
        super(clazz);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }
}
