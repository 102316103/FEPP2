package com.syscom.fep.frmcommon.scheduler.enums;

import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

public enum MonthsOfTheYear {
    January(1, Calendar.JANUARY),
    February(2, Calendar.FEBRUARY),
    March(4, Calendar.MARCH),
    April(8, Calendar.APRIL),
    May(16, Calendar.MAY),
    June(32, Calendar.JUNE),
    July(64, Calendar.JULY),
    August(128, Calendar.AUGUST),
    September(256, Calendar.SEPTEMBER),
    October(512, Calendar.OCTOBER),
    November(1024, Calendar.NOVEMBER),
    December(2048, Calendar.DECEMBER),
    AllMonths(4095, -1);

    private int value;
    private int monthOfYear;
    private String shortName;

    private MonthsOfTheYear(int value, int monthOfYear) {
        this.value = value;
        this.monthOfYear = monthOfYear;
        this.shortName = this.name().substring(0, 3).toUpperCase();
    }

    public int getValue() {
        return value;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }

    public String getShortName() {
        return shortName;
    }

    public static MonthsOfTheYear fromValue(int value) {
        for (MonthsOfTheYear e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static MonthsOfTheYear fromMonthOfYear(int monthOfYear) {
        for (MonthsOfTheYear e : values()) {
            if (e.getMonthOfYear() == monthOfYear) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid monthOfYear = [" + monthOfYear + "]!!!");
    }

    public static MonthsOfTheYear parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (MonthsOfTheYear e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }

    public static MonthsOfTheYear getMonthsOfTheYear(Calendar calendar, int amount){
        return getMonthsOfTheYear(calendar, Calendar.MONTH, amount);
    }

    public static MonthsOfTheYear getMonthsOfTheYear(Calendar calendar, int field, int amount){
        int monthOfYear = CalendarUtil.add(calendar, field, amount).get(Calendar.MONTH);
        return fromMonthOfYear(monthOfYear);
    }
}
