package com.syscom.fep.frmcommon.util;

import org.junit.jupiter.api.Test;
import org.springframework.util.Base64Utils;

import com.syscom.fep.frmcommon.log.LogHelper;

import java.nio.charset.StandardCharsets;

public class GeneralTest {
	private LogHelper logger = new LogHelper();

	@Test
	public void test() {
		byte[] bytes = Base64Utils.decodeFromString("c2lub3BhYw==");
		logger.info(ConvertUtil.toString(bytes, "950"));

		logger.info(Base64Utils.encodeToString("jasypt.encryptor.password".getBytes(StandardCharsets.UTF_8)));
		logger.info(new String(Base64Utils.decodeFromString("amFzeXB0LmVuY3J5cHRvci5wYXNzd29yZA=="), StandardCharsets.UTF_8));
	}
}
