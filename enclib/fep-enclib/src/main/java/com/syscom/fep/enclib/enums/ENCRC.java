package com.syscom.fep.enclib.enums;

import org.apache.commons.lang3.StringUtils;

public enum ENCRC {
	Normal(0),
	KeyLengthError(79),
	InputString1Error(80),
	InputString2Error(81),
	SuipReturnError(82),
	ATMKeyFileReadError(83),
	RMKeyFileReadError(84),
	UpdateKeyFileError(85),
	SendError(94),
	ReceiveError(95),
	ENCKeyIoError(97),
	ConnectSuipError(99),
	CheckReturn1Error(101),
	CheckReturn2Error(102),
	GetSuipSocketError(998),
	ENCLibError(999);

	private int value;

	private ENCRC(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ENCRC fromValue(int value) {
		for (ENCRC e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ENCRC parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ENCRC e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
