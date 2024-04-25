package com.syscom.fep.mybatis.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.ext.mapper.RmnoctlExtMapper;
import com.syscom.fep.mybatis.mapper.AtmstatMapper;
import com.syscom.fep.mybatis.mapper.RmoutsnoMapper;
import com.syscom.fep.mybatis.model.Atmstat;
import com.syscom.fep.mybatis.model.Rmnoctl;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.mybatis.model.Stan;

public class SpCallerTest extends MybatisBaseTest {

	@Autowired
	private AtmstatMapper atmstatMapper;
	@Autowired
	private SpCaller spCaller;

	@Autowired
	private RmoutsnoMapper rmoutsnoMapper;
	@Autowired
	private RmnoctlExtMapper rmnoctlMapper;

	@Test
	public void testGetAtmTxSeq() {
		String atmstatAtmno = "00122";
		Atmstat atmstat = atmstatMapper.selectByPrimaryKey(atmstatAtmno);
		if (atmstat != null) {
			assertEquals(atmstat.getAtmstatTxSeq() + 1, spCaller.getAtmTxSeq(atmstatAtmno));
		}
	}

	@Test
	public void testgetRMOUTSNONO() {
		Rmoutsno rmoutsno = rmoutsnoMapper.selectByPrimaryKey("123","123");
		if (rmoutsno != null) {
			assertEquals(rmoutsno.getRmoutsnoRepNo() + 1, spCaller.getRMOUTSNONO(rmoutsno, true));
		}
	}

	@Test
	public void testgetRMNO() {
		Rmnoctl rmnoctl = rmnoctlMapper.selectByPrimaryKey("123","123");
		if (rmnoctl != null) {
			assertEquals(rmnoctl.getRmnoctlNo() + 1, spCaller.getRMNO(rmnoctl));
		}
	}
	
//	@Test
//	public void testGetStan() throws InterruptedException {
//		new TestThread("t1");
//		new TestThread("t2");
//		Thread.sleep(System.currentTimeMillis());
//	}
//
//	private class TestThread extends Thread {
//
//		public TestThread(String name) {
//			super(name);
//			this.start();
//		}
//
//		@Override
//		public void run() {
//			while (true) {
//				Stan stan = spCaller.getStan();
//				UnitTestLogger.info("stan = ", stan.getStan());
//				try {
//					sleep(1000L);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
}
