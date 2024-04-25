package com.syscom.safeaa.enums;

import java.util.Arrays;

public enum EnumGetValueType {

    Maximum(1),
    Minimum(2),
    True(3),
    False(4),
    Last(5),
    First(6);

    private int value;

    private EnumGetValueType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    public static EnumGetValueType fromValue(final int value) {
        for (EnumGetValueType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static EnumGetValueType getEnumAuditTypeByValue(final int value) {
        return Arrays.stream(EnumGetValueType.values())
                .filter(e->e.getValue()==value).findFirst()
                .orElseThrow(()-> new IllegalArgumentException(String.format("Invalid value = [%s]!!!", value)));
    }
}
