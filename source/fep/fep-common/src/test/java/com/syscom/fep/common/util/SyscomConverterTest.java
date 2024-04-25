package com.syscom.fep.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.syscom.fep.common.converter.SyscomConverter;
import com.syscom.fep.common.converter.SyscomConverter.EncodingType;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConvertUtil;

public class SyscomConverterTest {
	@Test
	public void testConvertToByteArray() {
		byte[] bytes = ConvertUtil.toBytes("您好我是凌群計算機", "MS950");
		int[] actuals = new int[bytes.length];
		int i = 0;
		for (byte b : bytes) {
			actuals[i++] = PolyfillUtil.toInteger(b);
		}
		LogHelperFactory.getUnitTestLogger().info(StringUtils.join(actuals, ','));
		assertArrayEquals(new int[] { 177, 122, 166, 110, 167, 218, 172, 79, 173, 226, 184, 115, 185, 113, 184, 163 }, actuals);

		bytes = SyscomConverter.convertToByteArray(EncodingType.Big5, EncodingType.Unicode, bytes);
		actuals = new int[bytes.length];
		i = 0;
		for (byte b : bytes) {
			actuals[i++] = PolyfillUtil.toInteger(b);
		}
		LogHelperFactory.getUnitTestLogger().info(StringUtils.join(actuals, ','));
		assertArrayEquals(new int[] { 168, 96, 125, 89, 17, 98, 47, 102, 204, 81, 164, 127, 251, 150, 102, 129 }, actuals);
	}

	@Test
	public void testConvertToHexString() {
		byte[] bytes = ConvertUtil.toBytes("您好我是凌群計算機", "MS950");
		int[] actuals = new int[bytes.length];
		int i = 0;
		for (byte b : bytes) {
			actuals[i++] = PolyfillUtil.toInteger(b);
		}
		LogHelperFactory.getUnitTestLogger().info(StringUtils.join(actuals, ','));
		assertArrayEquals(new int[] { 177, 122, 166, 110, 167, 218, 172, 79, 173, 226, 184, 115, 185, 113, 184, 163 }, actuals);

		String actual = SyscomConverter.convertToHexString(EncodingType.Big5, EncodingType.Unicode, bytes);
		LogHelperFactory.getUnitTestLogger().info(actual);
		assertEquals("A8607D5911622F66CC51A47FFB966681", actual);
	}
}
