package com.syscom.safeaa.log;

public class LogHelperFactory {
    private static final LogHelper FEPLOGGER = new LogHelper("FEPMessageLogger");
    private static final LogHelper EMSLOGGER = new LogHelper("SyscomEMSLogger");
    private static final LogHelper ENCLOGGER = new LogHelper("ENCMessageLogger");

    private static final LogHelper GENERALLOGGER = new LogHelper("SyscomGeneralLogger");
    private static final LogHelper TRACELOGGER = new LogHelper("SyscomTraceLogger");
    private static final LogHelper UNITTESTLOGGER = new LogHelper("SyscomUnitTestLogger");
    private static final LogHelper WSCLIENTLOGGER = new LogHelper("SyscomWsClientLogger");
    private static final LogHelper RESTFULLOGGER = new LogHelper("SyscomRestfulClientLogger");
    private static final LogHelper SCHEDULELOGGER = new LogHelper("SyscomScheduleLogger");

    private LogHelperFactory() {}

    public static LogHelper getFEPLogger() {
        return FEPLOGGER;
    }

    public static LogHelper getEMSLogger() {
        return EMSLOGGER;
    }

    public static LogHelper getGeneralLogger() {
        return GENERALLOGGER;
    }

    public static LogHelper getENCLogger() {
        return ENCLOGGER;
    }

    public static LogHelper getTraceLogger() {
        return TRACELOGGER;
    }

    public static LogHelper getUnitTestLogger() {
        return UNITTESTLOGGER;
    }

    public static LogHelper getWsClientLogger() {
        return WSCLIENTLOGGER;
    }

    public static LogHelper getRestfulClientLogger() {
        return RESTFULLOGGER;
    }

    public static LogHelper getScheduleLogger() {
        return SCHEDULELOGGER;
    }
}
