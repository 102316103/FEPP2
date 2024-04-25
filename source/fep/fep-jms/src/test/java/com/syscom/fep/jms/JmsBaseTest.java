package com.syscom.fep.jms;

import org.springframework.boot.test.context.SpringBootTest;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

@SpringBootTest(classes = JmsTestApplication.class)
public class JmsBaseTest {
	protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();

}
