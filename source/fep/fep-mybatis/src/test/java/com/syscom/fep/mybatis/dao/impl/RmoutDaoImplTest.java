package com.syscom.fep.mybatis.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.dao.RmoutDao;
import com.syscom.fep.mybatis.model.Rmout;

public class RmoutDaoImplTest extends MybatisBaseTest {
	@Autowired
	private RmoutDao rmoutDao;

	@Test
	public void testThread() throws InterruptedException {
		new TestThread("t1");
		new TestThread("t2");
		new TestThread("t3");
		new TestThread("t4");
		new TestThread("t5");
		new TestThread("t21");
		new TestThread("t22");
		new TestThread("t23");
		new TestThread("t24");
		new TestThread("t25");
		new TestThread("t31");
		new TestThread("t32");
		new TestThread("t33");
		new TestThread("t34");
		new TestThread("t35");
		Thread.sleep(20000);
	}

	private class TestThread extends Thread {

		public TestThread(String name) {
			super(name);
			this.start();
		}

		@Override
		public void run() {
			while (true) {
				Rmout rmout = rmoutDao.queryByPrimaryKeyWithUpdLock("20211216", "001", "5", "9032210");
				UnitTestLogger.info(rmout);
				try {
					sleep(5000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
