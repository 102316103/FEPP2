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
import com.syscom.safeaa.mybatis.extmapper.SyscomuserExtMapper;
import com.syscom.safeaa.mybatis.vo.SyscomresourceInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomuserInfoVo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomuserMapperTest {

	@Autowired
	private SyscomuserExtMapper mapper;

	@Test
	public void getSyscomuserInfoTest() {
		SyscomuserInfoVo vo = mapper.getSyscomuserInfo("admin");
		Assert.assertNotNull(vo);
		Assert.assertEquals("系統管理員", vo.getUsername());
		Assert.assertEquals(Long.valueOf(1L).intValue(),vo.getIslogon().intValue());
	}
	
	@Test
	public void querySyscomresourceByLogOnIdTest() {
		List<SyscomresourceInfoVo> list = mapper.querySyscomresourceByLogOnId("admin");
		assertNotNull(list);
		Assert.assertEquals("系統管理員", list.get(0).getUsername());
	}
	
	@Test
	public void queryUserByNo() {
		mapper.queryUserByNo("001");
	}
	
	@Test
	public void querySyscomresourceByLogOnId() {
		mapper.querySyscomresourceByLogOnId("001");
	}
	
	@Test
	public void queryUsersBy() {
		mapper.queryUsersBy("admin","系統管理員","LOGONID");
	}
	
	
	
	
}
