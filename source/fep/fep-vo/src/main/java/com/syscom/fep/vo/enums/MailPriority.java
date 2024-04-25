package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum MailPriority {
	Normal(0),
	Low(1),
	High(2);

	private int value;

	private MailPriority(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static MailPriority fromValue(int value) {
		for (MailPriority e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static MailPriority parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (MailPriority e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
