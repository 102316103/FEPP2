package com.syscom.safeaa.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Richard
 */
public class ExceptionUtil {
    private ExceptionUtil() {
    }

    public static Exception createException(Object... messages) {
        return new Exception(StringUtils.join(messages));
    }

    public static Exception createException(Throwable cause, Object... messages) {
        return new Exception(StringUtils.join(messages), cause);
    }

    public static IllegalArgumentException createIllegalArgumentException(Object... messages) {
        return new IllegalArgumentException(StringUtils.join(messages));
    }

    public static ArrayIndexOutOfBoundsException createArrayIndexOutOfBoundsException(Object... messages) {
        return new ArrayIndexOutOfBoundsException(StringUtils.join(messages));
    }

    public static UnsupportedOperationException createUnsupportedOperationException(Object... messages) {
        return new UnsupportedOperationException(StringUtils.join(messages));
    }

    public static FileNotFoundException createFileNotFoundException(Object... messages) {
        return new FileNotFoundException(StringUtils.join(messages));
    }

    public static RuntimeException createRuntimeException(Object... messages) {
        return new RuntimeException(StringUtils.join(messages));
    }

    public static TimeoutException createTimeoutException(Object... messages) {
        return new TimeoutException(StringUtils.join(messages));
    }

    public static NotImplementedException createNotImplementedException(Object... messages) {
        return new NotImplementedException(StringUtils.join(messages));
    }

    public static ClassNotFoundException createClassNotFoundException(Object... messages) {
        return new ClassNotFoundException(StringUtils.join(messages));
    }

    public static SocketException createSocketException(Object... messages) {
        return new SocketException(StringUtils.join(messages));
    }

    public static String getStackTrace(Throwable t, boolean... notFormatted) {
        return getStackTrace(t, null, notFormatted);
    }

    public static String getStackTrace(Throwable t, Map<String, String> regexMap, boolean... notFormatted) {
        try (Writer w = new StringWriter()) {
            PrintWriter pw = new PrintWriter(w);
            t.printStackTrace(pw);
            String stackTrace = w.toString();
            stackTrace = StringUtil.replace(stackTrace, regexMap);
            if (ArrayUtils.isNotEmpty(notFormatted) && notFormatted[0]) {
                Pattern p = Pattern.compile("\t|\r|\n");
                Matcher m = p.matcher(stackTrace);
                stackTrace = m.replaceAll(StringUtils.EMPTY);
            }
            return stackTrace;
        } catch (IOException e) {
            return StringUtils.EMPTY;
        }
    }

    public static Throwable find(Throwable t, Predicate<Throwable> predicate) {
        Throwable cause = t;
        while (cause != null) {
            if (predicate.test(cause)) {
                return cause;
            }
            cause = cause.getCause();
        }
        return null;
    }
}