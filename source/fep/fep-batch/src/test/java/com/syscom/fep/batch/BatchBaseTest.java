package com.syscom.fep.batch;

import org.springframework.boot.test.context.SpringBootTest;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

@SpringBootTest(classes = SyscomFepBatchApplication.class)
public class BatchBaseTest {
	protected final static LogHelper logger = LogHelperFactory.getUnitTestLogger();
}