package com.syscom.fep.frmcommon.log;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class LogHelper {
    private final String loggerName;
    private Logger logger;
    private final AtomicInteger stackFrameLevel = new AtomicInteger(0);
    private static final LogHelper ADDITIONALLOGGER = new LogHelper("AdditionalLogger");

    public LogHelper() {
        this(null);
    }

    public LogHelper(String loggerName) {
        this.loggerName = loggerName;
    }

    /**
     * 這個logger在配置檔中設定level為OFF, 主要是for Insufficient Logging of Exceptions的弱掃修正而建立
     *
     * @return
     */
    public static LogHelper getAdditionalLogger() {
        return ADDITIONALLOGGER;
    }

    protected Logger getLogger() {
        //  System.out.println("===============[" + Thread.currentThread().getName() + "]logEnable is " + LogMDC.get("logEnable"));
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

    public String debug(Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().debug(message);
        return message;
    }

    public String info(Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().info(message);
        return message;
    }

    public String warn(Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().warn(message);
        return message;
    }

    public String warn(Throwable t, Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().warn(message, t);
        return message;
    }

    public String trace(Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().trace(message);
        return message;
    }

    public String error(Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().error(message);
        return message;
    }

    public String error(Throwable t, Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().error(message, t);
        return message;
    }

    public String exceptionMsg(Throwable t, Object... messages) {
        String message = StringUtils.join(messages);
        this.getLogger().error(message, t);
        return message;
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
