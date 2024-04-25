package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {
    public static final String FORMAT_DATE_YYYYMMDDHHMM_PLAIN = "yyyyMMddHHmm";
    public static final String FORMAT_DATE_YYYYMMDDHHMM = "yyyy/MM/dd HH:mm";
    public static final String FORMAT_DATE_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_DATE_YYYYMMDD_T_HHMM = "yyyy/MM/ddTHH:mm";
    public static final String FORMAT_DATE_YYYY_MM_DD_T_HH_MM = "yyyy-MM-ddTHH:mm";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSS = "yyyyMMdd-HHmmssSSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSS_TAX = "yyyy-MM-dd-HH.mm.ss.SSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSS = "yyyyMMdd-HHmmss-SSS";

    public static final String FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN = "yyyyMMddHHmmss";
    public static final String FORMAT_DATE_YYMMDDHHMMSS_PLAIN = "yyMMddHHmmss";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSS = "yyyy/MM/dd HH:mm:ss";
    public static final String FORMAT_DATE_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_YYYYMMDD_T_HHMMSS = "yyyy/MM/ddTHH:mm:ss";
    public static final String FORMAT_DATE_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-ddTHH:mm:ss";

    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSSS_PLAIN = "yyyyMMddHHmmssSSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSSSSSSSSSS_PLAIN = "yyyyMMddHHmmssSSSSSSS";
    public static final String FORMAT_DATE_YYYYMMDDHHMMSSSSS = "yyyy/MM/dd HH:mm:ss.SSS";
    public static final String FORMAT_DATE_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS = "yyyy/MM/ddTHH:mm:ss.SSS";
    public static final String FORMAT_DATE_YYYY_MM_DD_T_HH_MM_SS_SSS = "yyyy-MM-ddTHH:mm:ss.SSS";

    public static final String FORMAT_DATE_YYYYMM_PLAIN = "yyyyMM";
    public static final String FORMAT_DATE_YYYYMMDD_PLAIN = "yyyyMMdd";
    public static final String FORMAT_DATE_DDMMYYYY_PLAIN = "ddMMyyyy";
    public static final String FORMAT_DATE_YYYY_MM_DD = "yyyy/MM/dd";
    public static final String FORMAT_DATE_YYYY_MM_DD_2 = "yyyy-MM-dd";

    public static final String FORMAT_DATE_YYY_MM_DD = "yyy/MM/dd";
    public static final String FORMAT_DATE_YYY_MM_DD_2 = "yyy-MM-dd";

    public static final String FORMAT_TIME_HHMMSS_PLAIN = "HHmmss";
    public static final String FORMAT_TIME_HHMMSSSSS_PLAIN = "HHmmssSSS";
    public static final String FORMAT_TIME_HH_MM_SS = "HH:mm:ss";
    public static final String FORMAT_TIME_HH_MM_SSSSS = "HH:mm:ss.SSS";
    public static final String FORMAT_TIME_HH_MM = "HH:mm";

    private static final DecimalFormat longFormat, doubleFormat, rateFormat;
    private static final SimpleDateFormat dateFormat, timeFormat, timeInMillisFormat, dateTimeFormat, dateTimeInMillisFormat;

    static {
        Locale Locale = new Locale("en", "US");
        longFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale);
        longFormat.applyPattern("#,###");
        doubleFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale);
        doubleFormat.applyPattern("#,###.###");
        rateFormat = (DecimalFormat) DecimalFormat.getNumberInstance(Locale);
        rateFormat.applyPattern("#0.00%");
        dateFormat = new SimpleDateFormat(FORMAT_DATE_YYYY_MM_DD, Locale);
        timeFormat = new SimpleDateFormat(FORMAT_TIME_HH_MM_SS, Locale);
        timeInMillisFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale);
        dateTimeFormat = new SimpleDateFormat(FORMAT_DATE_YYYYMMDDHHMMSS, Locale);
        dateTimeInMillisFormat = new SimpleDateFormat(FORMAT_DATE_YYYYMMDDHHMMSSSSS, Locale);
    }

    private FormatUtil() {
    }

    public static String dateFormat(int date) {
        Calendar cal = CalendarUtil.parseDateValue(date);
        return dateFormat(cal.getTime());
    }

    public synchronized static String dateFormat(Date date) {
        return dateFormat.format(date);
    }

    public static String dateFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return dateFormat(cal.getTime());
    }

    public static String timeFormat(int time) {
        Calendar cal = CalendarUtil.parseTimeValue(time);
        return timeFormat(cal.getTime());
    }

    public synchronized static String timeFormat(Date date) {
        return timeFormat.format(date);
    }

    public synchronized static String rateFormat(double value) {
        return rateFormat.format(value);
    }

    public synchronized static String longFormat(long value) {
        return longFormat.format(value);
    }

    public static String doubleFormat(double value) {
        synchronized (doubleFormat) {
            doubleFormat.setMinimumFractionDigits(0);
            doubleFormat.setMaximumFractionDigits(100);
            return doubleFormat.format(value);
        }
    }

    public static String doubleFormat(double value, int fixFractionDigits) {
        synchronized (doubleFormat) {
            doubleFormat.setMaximumFractionDigits(fixFractionDigits);
            doubleFormat.setMinimumFractionDigits(fixFractionDigits);
            return doubleFormat.format(value);
        }
    }

    public static String timeInMillisFormat(int time) {
        Calendar cal = CalendarUtil.parseTimeValue(time);
        return timeInMillisFormat(cal.getTime());
    }

    public synchronized static String timeInMillisFormat(Date date) {
        return timeInMillisFormat.format(date);
    }

    public static String dateTimeFormat(int date, int time) {
        Calendar cal = CalendarUtil.parseDateTimeValue(date, time);
        return dateTimeFormat(cal.getTime());
    }

    public synchronized static String dateTimeFormat(Date date) {
        return dateTimeFormat.format(date);
    }

    public synchronized static String dateTimeInMillisFormat(Date date) {
        return dateTimeInMillisFormat.format(date);
    }

    public static String dateTimeFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return dateTimeFormat(cal.getTime());
    }

    public static String dateTimeInMillisFormat(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return dateTimeInMillisFormat(cal.getTime());
    }

    public static String dateTimeFormat(Calendar cal, String format) {
        int index = format.indexOf("T");
        if (index < 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(cal.getTime());
        } else {
            StringBuilder sb = new StringBuilder();
            String formatDate = format.substring(0, index);
            if (StringUtils.isNotBlank(formatDate)) {
                sb.append(dateTimeFormat(cal, formatDate)).append("T");
            }
            String formatTime = format.substring(index + 1);
            if (StringUtils.isNotBlank(formatTime)) {
                sb.append(dateTimeFormat(cal, formatTime));
            }
            return sb.toString();
        }
    }

    public static String longFormat(long value, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    public static String doubleFormat(double value, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    public static String rateFormat(double value, String pattern) {
        DecimalFormat df = new DecimalFormat(StringUtils.join(pattern, "%"));
        return df.format(value);
    }

    public static String decimalFormat(BigDecimal value, String pattern) {
        if (value == null)
            return StringUtils.EMPTY;
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    public static Date parseDataTime(String dateTime, String format) throws ParseException {
        int index = format.indexOf("T");
        if (index < 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            return sdf.parse(dateTime);
        } else {
            format = StringUtils.join(format.substring(0, index), StringUtils.SPACE, format.substring(index + 1));
            return parseDataTime(dateTime, format);
        }
    }

    public static ThreadLocal<MessageFormat> createMessageFormat(final String pattern) {
        return ThreadLocal.withInitial(() -> new MessageFormat(pattern));
    }

    public static String messageFormat(ThreadLocal<MessageFormat> messageFormatThreadLocal, String pattern, Object... args) {
        if (messageFormatThreadLocal == null) {
            messageFormatThreadLocal = createMessageFormat(pattern);
        }
        return messageFormatThreadLocal.get().format(args);
    }
}
