package com.syscom.safeaa.core;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import com.syscom.safeaa.mybatis.model.Syscomresource;
import com.syscom.safeaa.security.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class ResourceTest {

	@Autowired
	Resource resource;
	
	@Test
	public  void SelectByPrimaryKeyTest(){
		Syscomresource oDefResource = new Syscomresource();
		int result = 0;
		try {
			result =resource.createResource(oDefResource);
			
		}catch(Exception e) {
			
		}
		
		Assert.assertEquals(1, result);
	}
}
