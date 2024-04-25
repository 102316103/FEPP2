package com.syscom.safeaa.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.vo.SyscomUserQueryVo;

public class UserTest2  extends BaseTest{

	@Resource
    User syscomuserService;

	@Test
	public void queryUsersByTest() throws Exception {
		List<SyscomUserQueryVo> list = syscomuserService.queryUsersBy("","","USERNAME DESC");
		assertNotNull(list);
		assertThat (list.size(), greaterThan(10));
//		System.out.println(list.get(0).getUsername());
	}

}
