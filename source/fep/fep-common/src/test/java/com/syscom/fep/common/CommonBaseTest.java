package com.syscom.fep.common;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CommonTestApplication.class)
public class CommonBaseTest {
	protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();
}
