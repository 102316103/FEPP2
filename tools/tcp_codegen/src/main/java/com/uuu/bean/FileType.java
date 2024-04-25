package com.uuu.bean;

import java.util.HashMap;
import java.util.Map;

public enum FileType {
	ATM("ATM"), //
	ATM_bigClass("ATM_bigClass"), //
	MFT("MFT"), //
	MFT_bigClass("MFT_bigClass"), //
	EATM("EATM"), //
	HCE("HCE"), //
	IMS("IMS"), //
	NB("NB"), //
	VA("VA"), //
	VO("VO"), //
	;

	private String value;

	private static final Map<String, FileType> lookup = new HashMap<>();

	static {
		for (FileType FileType : FileType.values()) {
			lookup.put(FileType.getValue(), FileType);
		}
	}

	FileType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static FileType getByValue(String value) {
		return lookup.get(value);
	}
}
