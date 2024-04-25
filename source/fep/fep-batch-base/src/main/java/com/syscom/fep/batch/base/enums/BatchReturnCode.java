package com.syscom.fep.batch.base.enums;

import org.apache.commons.lang3.StringUtils;

public enum BatchReturnCode {

    ProgramException(-1),
    Succeed(0),
    TableNotFound(1),
    FileNotFound(2),
    DBIOError(3);

    private int value;

    private BatchReturnCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BatchReturnCode fromValue(int value) {
        for (BatchReturnCode e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static BatchReturnCode parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (BatchReturnCode e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}
