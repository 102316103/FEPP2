package com.syscom.safeaa.enums;

import org.apache.commons.lang3.StringUtils;

//    ''' <summary>
//    ''' Level of error.<br/>
//    ''' Designer: David Tai<br/>
//    ''' Version: 1.0.0.0<br/>
//    ''' Update Date: 2006-12-26
//    ''' </summary>
//    ''' <localsummary>
//    ''' 錯誤等級
//    ''' </localsummary>
public enum ErrorPriority {
    Info(0),

    Warning(1),

    //todo
    Error(2),

    Fatal(3);

    private int value;

    private ErrorPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ErrorPriority fromValue(int value) {
        for (ErrorPriority e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static ErrorPriority parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (ErrorPriority e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
