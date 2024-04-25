package com.syscom.fep.server.common;

import org.junit.jupiter.api.Test;

public class TxHelperTest extends ServerCommonBaseTest {

	@Test
	public void testGenerateEj() {
		for (int i = 0; i < 10; i++) {
			UnitTestLogger.info("generate Ej = [", TxHelper.generateEj(), "]");
		}
	}
}
