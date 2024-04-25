package com.syscom.safeaa.utils;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RandomPassword 測試
 *
 * @author syscom
 * @date 2021/08/13
 */
public class RandomPasswordTest {

	@Before
	public void setUp() throws Exception {
//		System.out.println("******RandomPasswordTest******<開始測試>");
	}

	@Test
	public void doGenerateTest() {
		String sscode = RandomPassword.doGenerate(1,64);
//		System.out.println(sscode);
		Assert.assertTrue(sscode.length()>=1 && sscode.length()<=64);
	}

	@After
	public void tearDown() throws Exception {
//		System.out.println("******RandomPasswordTest******<測試結束>");
	}
}
