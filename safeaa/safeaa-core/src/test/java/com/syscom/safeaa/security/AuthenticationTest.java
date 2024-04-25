package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.enums.EnumPasswordFormat;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserstatusExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.model.Syscomuserstatus;


/**
 *
 * @author syscom
 *
 */
public class AuthenticationTest  extends BaseTest {

	@Autowired
	private Authentication authenticationService;

	@Autowired
	private SyscomuserstatusExtMapper userstatusExtMapper;

	@Before
	public void setUp() throws Exception {
//		System.out.println("******AuthenticationTest******<開始測試>");

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition posExpireddate = new ParsePosition(0);
		ParsePosition posEffectdate = new ParsePosition(0);
		Syscomuser currentUser = new Syscomuser();
		currentUser.setUserid(1);
		currentUser.setIdno("1");
		currentUser.setBirthday("2020-01-07");
		currentUser.setLogonid("1");
		currentUser.setExpireddate(sdf.parse("2030-01-07", posExpireddate));
		currentUser.setEffectdate(sdf.parse("2020-01-07", posEffectdate));

		authenticationService.setCurrentUser(currentUser);

		Syscomuserstatus mDefUserStatus = userstatusExtMapper.selectByPrimaryKey(1);
		authenticationService.setmDefUserStatus(mDefUserStatus);
	}

	@Ignore("TEST OK")
	@Test
	public void checkLogOnTest() throws Exception {
		String logOnId = "1";
		String logOnIp = "127.0.0.1";
		String sscode = "123456";
		boolean rt = authenticationService.checkLogOn(logOnId,sscode,logOnIp,true);
		assertTrue(rt);
	}

	@Ignore("TEST OK")
	@Test
	public void logOffTest() throws Exception {
//		String logOnId = "1";
//		String logOnIp = "127.0.0.1";
//		boolean rt = authenticationService.logOff(logOnId,logOnIp);
//		assertTrue(rt);
	}

	@Ignore("TEST OK")
	@Test
	public void changePasswordTest() throws Exception {
//		String logOnId = "1";
//		String oldPassword = "123456";
//		String newPassword = "aa654321";
//		String logOnIp = "127.0.0.1";
//		boolean rt = authenticationService.changePassword(logOnId,oldPassword,newPassword,logOnIp);
//		assertTrue(rt);
	}

	@Ignore("TEST OK")
	@Test
	public void restPasswordTest() throws Exception {
		Integer userId =1;
		String logOnId = "1";
		String newSscode = "bb654321";
		String logOnIp = "127.0.0.1";
		boolean rt = authenticationService.restPassword(userId,logOnId,logOnIp,newSscode);
		assertTrue(rt);
	}

	@Ignore("TEST OK")
	@Test
	public void unlockAccountTest() throws Exception {
		Integer userId =1;
		String logOnId = "1";
		String logOnIp = "127.0.0.1";
		String logOnIdUnlocker = "2";
		boolean rt = authenticationService.unlockAccount(userId,logOnId,logOnIp,logOnIdUnlocker);
		assertTrue(rt);
	}

	//@Ignore("TEST OK")
	@Test
	public void encryptPasswordTest() throws Exception {
		String s1 =authenticationService.encryptPassword("123456", EnumPasswordFormat.values()[0]);
		assertEquals(DigestUtils.md5DigestAsHex("123456".getBytes()),s1);

		String s2 =authenticationService.encryptPassword("123456", EnumPasswordFormat.values()[2]);
		assertEquals("123456",s2);

		String s3=authenticationService.encryptPassword("123456", EnumPasswordFormat.values()[3]);
		assertNull(s3);
	}

	@After
	public void tearDown() throws Exception {
//		System.out.println("******ResourceTest******<測試結束>");
	}
}
