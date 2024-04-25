package com.syscom.safeaa.mybatis.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomrolemembersExtMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomrolemembersExtMapperTest {

	@Autowired
	private SyscomrolemembersExtMapper mapper;

	@Test
	public void querySelectedByRoleId() {
		mapper.querySelectedByRoleId(32, "zh-TW");
	}
	@Test
	public void queryParentRolesByUserId() {
		mapper.queryParentRolesByUserId(32, "zh-TW");
	}
	@Test
	public void queryRoleUsersByRoleId() {
		mapper.queryRoleUsersByRoleId(1);
	}
	@Test
	public void queryUNSelectMembers() {
		mapper.queryUNSelectMembers(32, "zh-TW");
	}
	
}
