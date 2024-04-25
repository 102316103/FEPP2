package com.syscom.fep.invoker;

import org.springframework.boot.test.context.SpringBootTest;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

@SpringBootTest(classes = InvokerTestApplication.class)
public class InvokerBaseTest {
	protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();
}
