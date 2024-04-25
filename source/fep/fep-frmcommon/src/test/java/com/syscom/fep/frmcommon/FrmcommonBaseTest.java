package com.syscom.fep.frmcommon;

import org.springframework.boot.test.context.SpringBootTest;

import com.syscom.fep.frmcommon.log.LogHelper;

@SpringBootTest(classes = FrmcommonTestApplication.class)
public class FrmcommonBaseTest {
	protected static final LogHelper UnitTestLogger = new LogHelper();

}
