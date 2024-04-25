package com.syscom.safeaa.mybatis.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleresourceExtMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomroleresourceExtMapperTest {

	@Autowired
	private SyscomroleresourceExtMapper mapper;


	@Test
	public void querySelectedResourcesByRoleId() {
		mapper.querySelectedResourcesByRoleId(null, null);
	}
	@Test
	public void queryUNSelectResourcesByRoleId() {
		mapper.queryUNSelectResourcesByRoleId(null, null);
	}
	
}
