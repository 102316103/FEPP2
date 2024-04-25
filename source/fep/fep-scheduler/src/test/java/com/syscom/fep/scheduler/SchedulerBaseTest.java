package com.syscom.fep.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;

@SpringBootTest(classes = SchedulerTestApplication.class)
public class SchedulerBaseTest {
	protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();

	@Test
	public void test() {
		int times = 0, total = 100, sleep = 1000;

		while (times++ < total) {
			try {
				UnitTestLogger.info("Scheduler running at [", times, "] times");
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				UnitTestLogger.warn(e, e.getMessage());
			}
		}
	}
}
