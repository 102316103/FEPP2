package com.syscom.fep.enclib;

import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.enclib.enums.ENCMessageFlow;
import com.syscom.fep.enclib.enums.ENCProgramFlow;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.EnumUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;
import java.util.Calendar;

public class ENCSendEMS {
    private static final LogHelper FEPLogger = LogHelperFactory.getFEPLogger();
    private static final LogHelper EMSLogger = LogHelperFactory.getEMSLogger();
    private static final LogHelper GeneralLogger = LogHelperFactory.getGeneralLogger();

    private ENCSendEMS() {
    }

    public static void sendEMS(ENCLogData aaLog) {
        EMSLogger.accumulateStackFrameLevel();
        sendEMS(Level.ERROR, aaLog);
    }

    private static void sendEMS(Level level, ENCLogData aaLog) {
        EMSLogger.accumulateStackFrameLevel();
        setLogContextField(aaLog);
        sendToEMS(level, aaLog);
        clearLogContext(aaLog);
    }

    private static void setLogContextField(ENCLogData aaLog) {
        LogMDC.put(Const.MDC_LOGDATE, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
        LogMDC.put(Const.MDC_SUBSYS, aaLog.getSubSys());
        LogMDC.put(Const.MDC_EJ, String.valueOf(aaLog.getEj()));
        LogMDC.put(Const.MDC_CHANNEL, aaLog.getChannel());
        LogMDC.put(Const.MDC_MESSAGEFLOW, EnumUtil.getEnumName(aaLog.getMessageFlowType()));
        LogMDC.put(Const.MDC_PROGRAMFLOW, EnumUtil.getEnumName(aaLog.getProgramFlowType()));
        LogMDC.put(Const.MDC_PROGRAMNAME, aaLog.getProgramName());
        LogMDC.put(Const.MDC_MESSAGEID, aaLog.getMessageId());
        LogMDC.put(Const.MDC_STAN, aaLog.getStan());
        LogMDC.put(Const.MDC_ATMSEQ, aaLog.getAtmSeq());
        LogMDC.put(Const.MDC_ATMNO, aaLog.getAtmNo());
        LogMDC.put(Const.MDC_TRINBANK, aaLog.getTrinBank());
        LogMDC.put(Const.MDC_TROUTBANK, aaLog.getTroutBank());
        LogMDC.put(Const.MDC_TRINACTNO, aaLog.getTrinActno());
        LogMDC.put(Const.MDC_TROUTACTNO, aaLog.getTroutActno());
        LogMDC.put(Const.MDC_TXDATE, aaLog.getTxDate());
        LogMDC.put(Const.MDC_TXMESSAGE, aaLog.getMessage());
        LogMDC.put(Const.MDC_STEP, String.valueOf(aaLog.getStep()));
        LogMDC.put(Const.MDC_BKNO, aaLog.getBkno());
        LogMDC.put(Const.MDC_MESSAGEGROUP, aaLog.getMessageGroup());
        LogMDC.put(Const.MDC_TXRQUID, aaLog.getTxRquid());
        LogMDC.put(Const.MDC_HOSTNAME, aaLog.getHostName());
        LogMDC.put(Const.MDC_HOSTIP, aaLog.getHostIp());
    }

    private static void clearLogContext(ENCLogData aaLog) {
        clearMDC();
        aaLog.setRemark(StringUtils.EMPTY);
        aaLog.setMessage(StringUtils.EMPTY);
        aaLog.setProgramFlowType(ENCProgramFlow.Debug);
        aaLog.setMessageFlowType(ENCMessageFlow.Debug);
        aaLog.setNotification(false);
        aaLog.setProgramException(null);
    }

    private static void clearMDC() {
        FEPBaseMethod.clearMDC();
    }

    private static void sendToEMS(Level level, ENCLogData aaLog, boolean... notPrintExceptionStack) {
        EMSLogger.accumulateStackFrameLevel();
        try {
            String exMessage = StringUtils.EMPTY;
            String exStack = StringUtils.EMPTY;
            String exCode = StringUtils.EMPTY;
            String exSource = StringUtils.EMPTY;
            StringBuilder sb = new StringBuilder();
            if (aaLog != null) {
                String logXml = XmlUtil.toXML(aaLog);
                sb.append(logXml);
                if (aaLog.getProgramException() != null) {
                    // 為了配合EMS Alert日誌記錄, 這裡加上自定義的訊息
                    exMessage = StringUtils.join(
                            Const.MESSAGE_ERR_EXCEPTION_OCCUR,
                            StringUtils.isNotBlank(aaLog.getProgramException().getMessage()) ?
                                    StringUtils.join(", ", aaLog.getProgramException().getMessage()) : StringUtils.EMPTY);
                    if (ArrayUtils.isEmpty(notPrintExceptionStack) || !notPrintExceptionStack[0]) {
                        GeneralLogger.exceptionMsg(aaLog.getProgramException(), exMessage);
                    }
                    exCode = aaLog.getProgramException().getClass().getSimpleName();
                    exSource = aaLog.getProgramException().getCause() == null ? StringUtils.EMPTY : aaLog.getProgramException().getCause().getClass().getSimpleName();
                    String customMsg = StringUtils.EMPTY;
                    // BadSqlGrammarException
                    // 2023-07-24 Richard add因為BadSqlGrammarException的message太長, 並且帶有sql語法, 所以這裡需要重新塞一次exMessage
                    if (aaLog.getProgramException() instanceof BadSqlGrammarException) {
                        BadSqlGrammarException badSqlGrammarException = (BadSqlGrammarException) aaLog.getProgramException();
                        SQLException sqlEx = badSqlGrammarException.getSQLException();
                        // 為了配合EMS Alert日誌記錄, 這裡加上自定義的訊息
                        exMessage = StringUtils.join(
                                Const.MESSAGE_ERR_EXCEPTION_OCCUR,
                                ", BadSqlGrammarException occur",
                                StringUtils.isNotBlank(sqlEx.getMessage()) ?
                                        StringUtils.join(", ", sqlEx.getMessage()) : StringUtils.EMPTY);
                        sb.append("<ExSubCode>").append(sqlEx.getErrorCode()).append("</ExSubCode>");
                    }
                    // DataAccessException
                    else if (aaLog.getProgramException() instanceof DataAccessException) {
                        DataAccessException dae = (DataAccessException) aaLog.getProgramException();
                        Throwable cause = dae.getCause();
                        if (cause instanceof SQLException) {
                            SQLException sqlEx = (SQLException) cause;
                            customMsg = StringUtils.join(",CustomMessage:", sqlEx.getMessage());
                            sb.append("<ExSubCode>").append(sqlEx.getErrorCode()).append("</ExSubCode>");
                        }
                    }
                    // SQLException
                    else if (aaLog.getProgramException() instanceof SQLException) {
                        SQLException sqlEx = (SQLException) aaLog.getProgramException();
                        sb.append("<ExSubCode>").append(sqlEx.getErrorCode()).append("</ExSubCode>");
                    } else {
                        sb.append("<ExSubCode/>");
                    }
                    exStack = ExceptionUtil.getStackTrace(aaLog.getProgramException());
                    sb.append("<TxErrDesc>").append(exMessage).append(customMsg).append("</TxErrDesc>");
                    sb.append("<ExSource>").append(exSource).append("</ExSource>");
                    sb.append("<ExStack><![CDATA[").append(exStack).append("]]></ExStack>");
                    if (StringUtils.isBlank(aaLog.getExternalCode())) {
                        sb.append("<TxExternalCode>ProgramException</TxExternalCode>");
                    } else {
                        sb.append("<TxExternalCode>").append(aaLog.getExternalCode()).append("</TxExternalCode>");
                    }
                } else {
                    exMessage = aaLog.getRemark();
                    sb.append("<TxErrDesc>").append(exMessage).append("</TxErrDesc>");
                    sb.append("<ExSource/>");
                    sb.append("<ExStack/>");
                    if (StringUtils.isBlank(aaLog.getExternalCode())) {
                        sb.append("<TxExternalCode>AbNormal</TxExternalCode>");
                    } else {
                        sb.append("<TxExternalCode>").append(aaLog.getExternalCode()).append("</TxExternalCode>");
                    }
                    sb.append("<ExSubCode/>");
                }
            } else {
                sb.append("<LogData/>");
            }
            sb.append("<TxPK/>");
            sb.append("<TxSource>").append(LogMDC.get(Const.MDC_PROFILE, StringUtils.EMPTY)).append("</TxSource>");
            StackTraceElement[] elements = (new Throwable()).getStackTrace();
            int stackFrameLevel = EMSLogger.getStackFrameLevel();
            stackFrameLevel = stackFrameLevel < elements.length ? stackFrameLevel : elements.length - 1;
            StackTraceElement st = (new Throwable()).getStackTrace()[stackFrameLevel];
            LogMDC.put(Const.MDC_PGFILE, st.getFileName());
            LogMDC.put(Const.MDC_LINENUMBER, String.valueOf(st.getLineNumber()));
            LogMDC.put(Const.MDC_APP, aaLog != null ? aaLog.getApp() : StringUtils.EMPTY);
            LogMDC.put(Const.MDC_ERRCODE, exCode);
            LogMDC.put(Const.MDC_ERRDESCRIPTION, exMessage);
            LogMDC.put(Const.MDC_HOSTNAME, aaLog != null ? aaLog.getHostName() : StringUtils.EMPTY);
            LogMDC.put(Const.MDC_HOSTIP, aaLog != null ? aaLog.getHostIp() : StringUtils.EMPTY);
            LogMDC.put(Const.MDC_LEVEL, level.name());
            LogMDC.put(Const.MDC_SYS, aaLog != null ? aaLog.getSystemId() : StringUtils.EMPTY);
            LogMDC.put(Const.MDC_SUBSYS, aaLog != null && aaLog.getSubSys() != null ? aaLog.getSubSys() : StringUtils.EMPTY);
            LogMDC.put(Const.MDC_TXRQUID, aaLog != null && aaLog.getTxRquid() != null ? aaLog.getTxRquid() : StringUtils.EMPTY);
            switch (level) {
                case ERROR:
                    EMSLogger.error(sb.toString());
                    break;
                case INFO:
                    EMSLogger.info(sb.toString());
                    break;
                case DEBUG:
                    EMSLogger.debug(sb.toString());
                    break;
                case WARN:
                    EMSLogger.warn(sb.toString());
                    break;
                case TRACE:
                    EMSLogger.trace(sb.toString());
                    break;
            }
        } catch (Exception e) {
            FEPLogger.exceptionMsg(e, "SendEMS Fail!EX:", e.getMessage());
        } finally {
            if (aaLog != null) {
                aaLog.setResponseMessage(StringUtils.EMPTY);
                aaLog.setResponsible(StringUtils.EMPTY);
            }
        }
    }
}
