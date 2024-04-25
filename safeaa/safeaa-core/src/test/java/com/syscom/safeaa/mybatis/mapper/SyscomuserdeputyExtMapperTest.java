package com.syscom.safeaa.mybatis.mapper;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomuserdeputyExtMapper;
import com.syscom.safeaa.mybatis.vo.SyscomresourceInfoVo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomuserdeputyExtMapperTest {

	@Autowired
	private SyscomuserdeputyExtMapper mapper;
	
	@Test
	public void queryAllUserDeputy() {
		mapper.queryAllUserDeputy(1,1,"UserId");
	}
	
	@Test
	public void queryParentRolesByUserId() {
		mapper.queryParentRolesByUserId(1,"ss");
	}
	@Test
	public void queryUNSelectUserDeputyByUserId() {
		mapper.queryUNSelectUserDeputyByUserId(1,1);
	}
}
