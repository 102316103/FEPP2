package com.syscom.safeaa.core;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.utils.SyscomConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomConfigTest {

	@Autowired
	SyscomConfig config;

	@Test
	public void ConfigTest() {
		Assert.assertEquals("zh-TW", config.culture);
//		System.out.println(config.culture);
	}

	@Test
	public void ConfigTest2() {
//		System.out.println(config.batchQueue);
		Assert.assertEquals(".\\private\\$\\batch_queue", config.batchQueue);
//		System.out.println(config.batchQueue);
	}
}
