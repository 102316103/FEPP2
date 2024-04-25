package com.syscom.safeaa.log;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHelper {
    private String loggerName;
    private Logger logger;
    private AtomicInteger stackFrameLevel = new AtomicInteger(0);

    public LogHelper() {
        this(null);
    }

    public LogHelper(String loggerName) {
        this.loggerName = loggerName;
    }

    protected Logger getLogger() {
        if (StringUtils.isBlank(this.loggerName)) {
            StackTraceElement[] stack = (new Throwable()).getStackTrace();
            return LoggerFactory.getLogger(stack[this.stackFrameLevel.get() + 2].getClassName());
        } else {
            if (this.logger == null) {
                logger = LoggerFactory.getLogger(this.loggerName);
            }
            return logger;
        }
    }

    public void debug(Object... msgs) {
        this.getLogger().debug(StringUtils.join(msgs));
    }

    public void info(Object... msgs) {
        this.getLogger().info(StringUtils.join(msgs));
    }

    public void warn(Object... msgs) {
        this.getLogger().warn(StringUtils.join(msgs));
    }

    public void warn(Exception e, Object... msgs) {
        this.getLogger().warn(StringUtils.join(msgs), e);
    }

    public void trace(Object... msgs) {
        this.getLogger().trace(StringUtils.join(msgs));
    }

    public void error(Object... msgs) {
        this.getLogger().error(StringUtils.join(msgs));
    }

    public void exceptionMsg(Exception e, Object... msgs) {
        this.getLogger().error(StringUtils.join(msgs), e);
    }

    public int getStackFrameLevel() {
        return stackFrameLevel.getAndSet(0);
    }

    public void setStackFrameLevel(int stackFrameLevel) {
        this.stackFrameLevel.set(stackFrameLevel);
    }

    public void accumulateStackFrameLevel() {
        this.stackFrameLevel.incrementAndGet();
    }
}
