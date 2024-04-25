package com.syscom.fep.mybatis.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import com.syscom.fep.mybatis.MybatisBaseTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "mybatis", "taipei"})
public class EjfnoGeneratorTest extends MybatisBaseTest {
	@Autowired
	private EjfnoGenerator generator;

	@Test
	public void testGenerate() {
		UnitTestLogger.info("ejfno = ", generator.generate());
	}

	@Test
	@Rollback(false)
	public void testThread() throws InterruptedException {
		new TestThread("t1");
		new TestThread("t2");
		Thread.sleep(System.currentTimeMillis());
	}

	private class TestThread extends Thread {

		public TestThread(String name) {
			super(name);
			this.start();
		}

		@Override
		public void run() {
			while (true) {
				UnitTestLogger.info("ejfno = ", generator.generate());
				try {
					sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
