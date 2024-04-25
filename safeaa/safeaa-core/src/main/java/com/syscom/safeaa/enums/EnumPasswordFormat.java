package com.syscom.safeaa.enums;

import java.util.Arrays;

/**
 * Enum of Cipher method for password
 *
 */
public enum EnumPasswordFormat {
	
	MD5(1),
	DES(2),
	TEXT(3),
	AES(4),
    SHA(5);
	
	private final int value;
	
    private EnumPasswordFormat(int value) {
        this.value = value;
    }	
    
    public int getValue() {
        return value;
    }
	
    public static EnumPasswordFormat getEnumPasswordFormatByValue(final int value) {
    	return Arrays.stream(EnumPasswordFormat.values())
    			.filter(e->e.getValue()==value).findFirst()
    			.orElseThrow(()-> new IllegalArgumentException(String.format("Invalid value = [%s]!!!", value)));
    }    
	
}
