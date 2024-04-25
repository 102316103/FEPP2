package com.syscom.safeaa.mybatis.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomroledenyExtMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomroledenyExtMapperTest {

	@Autowired
	private SyscomroledenyExtMapper resourcecultureMapper;
	
	@Test
	public void queryDenyGroupsByUserId() {
		resourcecultureMapper.queryDenyGroupsByUserId(181);
	}
	
	@Test
	public void querySelectedRoleDenyByRoleId() {
		resourcecultureMapper.querySelectedRoleDenyByRoleId(222,"A",null);
	}
	
	@Test
	public void queryUNSelectRoleDenyByRoleId() {
		resourcecultureMapper.queryUNSelectRoleDenyByRoleId(222,"A",null);
	}
	
}
