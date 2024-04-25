package com.syscom.safeaa.mybatis.mapper;

import com.syscom.safeaa.mybatis.extmapper.SyscomresourceExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomresource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.extmapper" })
public class SyscomresourceMapperTest {
	
	@Autowired
	private SyscomresourceExtMapper mapper;
	
	@Test
	public void QueryResourceIdByNoTest() {
		Integer resourceId = mapper.queryResourceIdByNo("013100");
//		Assert.assertEquals(new Long(45), resourceId);
	}

	@Test
	public void upTest(){
		Syscomresource record = new Syscomresource();
		record.setLogAuditTrail(true);
		record.setResourceid(245);
		record.setResourceurl("fffxxff");
		int rt = mapper.updateByPrimaryKeySelective(record);
		Assert.assertEquals(1, rt);
	}
	
	@Test
	public void queryAllResources() {
		mapper.queryAllResources(null);
	}	
	
}
