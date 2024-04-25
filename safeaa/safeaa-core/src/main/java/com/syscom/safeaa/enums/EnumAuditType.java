package com.syscom.safeaa.enums;

import java.util.Arrays;

public enum EnumAuditType {

	None(0),
    ChangePassword(1),
    ResetPassword(2),
    QueryPassword(3),
    LogOn(4),
    LogOff(5),
    Execute(6),
    //'Add by David Tai on 2013-10-08 for 解除帳號鎖定
    UnlockAccount(7),
    Others(9);
	
	private int value;

	private EnumAuditType(int value) {
        this.value = value;
    }	
    
    public int getValue() {
        return value;
    }


    public static EnumAuditType fromValue(final int value) {
        for (EnumAuditType e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }   
    
    public static EnumAuditType getEnumAuditTypeByValue(final int value) {
    	return Arrays.stream(EnumAuditType.values())
    			.filter(e->e.getValue()==value).findFirst()
    			.orElseThrow(()-> new IllegalArgumentException(String.format("Invalid value = [%s]!!!", value)));
    }
}
