package com.syscom.safeaa.mybatis.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomresourcecultureExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomresourceculture;



@RunWith(SpringRunner.class)
@SpringBootTest(classes = SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomresourcecultureMapperTest {

	@Autowired
	private SyscomresourcecultureExtMapper resourcecultureMapper;
	
	@Test
	public void QeleteByPrimaryKeyTest() {
		Syscomresourceculture syscomresourceculture = new Syscomresourceculture();
		syscomresourceculture.setResourceid(235);
		syscomresourceculture.setCulture("zh-TW");
		int rst = resourcecultureMapper.deleteByPrimaryKey(syscomresourceculture);
//		Assert.assertEquals(1,rst);
	}
	
	@Test
	public void queryAllByResourceId() {
		resourcecultureMapper.queryAllByResourceId(1);
	}
	
}
