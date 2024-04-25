package com.syscom.fep.enclib.enums;

import org.apache.commons.lang3.StringUtils;

public enum ENCProgramFlow {
	Debug(0),
	ENCIn(11),
	ENCOut(12),
	ENCSocketIn(13),
	ENCSocketOut(14);

	private int value;

	private ENCProgramFlow(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ENCProgramFlow fromValue(int value) {
		for (ENCProgramFlow e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ENCProgramFlow parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ENCProgramFlow e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
