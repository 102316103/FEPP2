package com.syscom.fep.batch.cmdline;

import org.springframework.boot.test.context.SpringBootTest;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

@SpringBootTest(classes = SyscomFepBatchCmdLineApplication.class)
public class BatchCmdLineBaseTest {
	protected final static LogHelper logger = LogHelperFactory.getUnitTestLogger();
}