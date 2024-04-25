package com.syscom.fep.frmcommon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class MathUtilTest {

	@Test
	public void testRoundUp() {
		BigDecimal value = new BigDecimal("5.6");
		BigDecimal expected = new BigDecimal("6");
		assertEquals(expected, MathUtil.roundUp(value, 0));
	}

	@Test
	public void testToString() {
		BigDecimal value = new BigDecimal("5.61234123");
		String expected = "5.61";
		assertEquals(expected, MathUtil.toString(value, 2, false));
		assertEquals(expected, MathUtil.toString(value, "0.00", false));

		value = new BigDecimal("5.61634123");
		expected = "5.62";
		assertEquals(expected, MathUtil.toString(value, 2, false));
		assertEquals(expected, MathUtil.toString(value, "0.00", false));

		value = new BigDecimal("5.61634123");
		expected = "5.6163";
		assertEquals(expected, MathUtil.toString(value, 4, false));
		assertEquals(expected, MathUtil.toString(value, "0.0000", false));

		value = new BigDecimal("5.61234123");
		expected = "561";
		assertEquals(expected, MathUtil.toString(value, 2));
		assertEquals(expected, MathUtil.toString(value, "0.00"));

		value = new BigDecimal("5.61634123");
		expected = "562";
		assertEquals(expected, MathUtil.toString(value, 2));
		assertEquals(expected, MathUtil.toString(value, "0.00"));

		value = new BigDecimal("5.61634123");
		expected = "56163";
		assertEquals(expected, MathUtil.toString(value, 4));
		assertEquals(expected, MathUtil.toString(value, "0.0000"));

		assertEquals("0038", MathUtil.toString(BigDecimal.valueOf(38), "0000"));
		assertEquals("3800", MathUtil.toString(BigDecimal.valueOf(38), "00.00"));
		assertEquals("3800", MathUtil.toString(BigDecimal.valueOf(38), ".00"));

		assertEquals("10,123.12", MathUtil.toString(BigDecimal.valueOf(10123.123), "#,#.00", false));
		assertEquals("10,123", MathUtil.toString(BigDecimal.valueOf(10123.123), "#,#", false));

		assertEquals("00,010,123", MathUtil.toString(BigDecimal.valueOf(10123.123), "##000000#,#", false));
		assertEquals("0,000,010,123", MathUtil.toString(BigDecimal.valueOf(10123.123), "#0#000000#,#", false));
		assertEquals("00,000,010,123", MathUtil.toString(BigDecimal.valueOf(10123.123), "#0#000000#,##", false));
		assertEquals("000010123.123", MathUtil.toString(BigDecimal.valueOf(10123.123), "#0#000000#.##0", false));
		assertEquals("000010123.123000000000", MathUtil.toString(BigDecimal.valueOf(10123.123), "#0#000000#.##0#####0000", false));
	}

	@Test
	public void testSplitByPow2() {
		assertEquals(Arrays.asList(1, 16), MathUtil.splitByPow2(17));
		assertEquals(Arrays.asList(4, 8, 16), MathUtil.splitByPow2(28));
	}
}
