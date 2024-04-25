package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.enums.EnumActionType;

public class AuthorizationTest extends BaseTest {

	//TODO Authorization better not use auto injection
	@Resource
	Authorization auth;

	@Before
	public void setUp() throws Exception {
	}

	public @After
	void tearDown() throws Exception {
	}

	@Test
	public void testCheckButtonEnableWithFalse() {
		String functionList ="1111111111";
		EnumActionType actionType = EnumActionType.Add ;

		boolean result = auth.checkButtonEnable(functionList, actionType.Add);
		assertEquals(result, false);
	}

	@Test
	public void testCheckButtonEnableWithTrue() {
		String functionList ="1111111111";

		boolean result = auth.checkButtonEnable(functionList, EnumActionType.Modify);
		assertEquals(result, true);
	}

	@Test
	public void testCheckButtonEnableWithTrue1() {
		String functionList ="2121111111";

		boolean result = auth.checkButtonEnable(functionList, EnumActionType.Delete);
		assertEquals(result, true);
	}

	@Test
	public void testEnumActionType() {
		int action= 10;
		EnumActionType result = EnumActionType.getEnumActionByValue(action);
//		System.out.println(result.getValue());
		assertEquals(result, EnumActionType.Quit);

		//assertNotEquals(result, EnumActionType.Query);

		EnumActionType result1 = EnumActionType.fromValue(action);
//		System.out.println(result1.getValue());
		assertEquals(result1, EnumActionType.Quit);

	}

	@Test
	public void testGetMenuXml() throws Exception {
//		String menuxml = auth.getMenuXml(187L, "zh-TW", "SYSCOM");
//		System.out.println(menuxml);
	}
}
