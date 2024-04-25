package com.syscom.fep.batch.task;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BatchTaskTestApplication.class)
public class BatchTaskBaseTest {
    protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();
}
