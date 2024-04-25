package com.syscom.fep.batch.base.enums;

import org.apache.commons.lang3.StringUtils;

public enum JobState {
	Start(1),
	Running(2),
	End(3),
	Failed(4),
	Abort(5);

	private int value;

	private JobState(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static JobState fromValue(int value) {
		for (JobState e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static JobState parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (JobState e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
