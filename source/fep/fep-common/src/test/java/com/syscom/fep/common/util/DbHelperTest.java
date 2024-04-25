package com.syscom.fep.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DbHelperTest {

	@Test
	public void toBoolean() {
		assertEquals(Boolean.FALSE, DbHelper.toBoolean(Short.valueOf((short) 0)));
		assertEquals(Boolean.TRUE, DbHelper.toBoolean(Short.valueOf((short) 1)));
		assertEquals(Boolean.TRUE, DbHelper.toBoolean(Short.valueOf((short) 2)));
		assertEquals(Boolean.TRUE, DbHelper.toBoolean(Short.valueOf((short) -1)));
		assertEquals(false, DbHelper.toBoolean((Short) null));
		assertEquals(Boolean.TRUE, DbHelper.toBoolean((Short) null, Boolean.TRUE));
		assertEquals(Boolean.FALSE, DbHelper.toBoolean((Short) null, Boolean.FALSE));
	}

	@Test
	public void toShort() {
		assertEquals(Short.valueOf((short) 0), DbHelper.toShort(Boolean.FALSE));
		assertEquals(Short.valueOf((short) 1), DbHelper.toShort(Boolean.TRUE));
		assertEquals((short) 0, DbHelper.toShort(null));
		assertEquals(Short.valueOf((short) 1), DbHelper.toShort(null, Short.valueOf((short) 1)));
	}
}
