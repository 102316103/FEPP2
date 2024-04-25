package com.syscom.safeaa.mybatis.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomauditlogExtMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomauditlogExtMapperTest {
	@Autowired
	private SyscomauditlogExtMapper mapper;

	@Test
	public void test() {
		mapper.queryAllDataBy(1, 4, (short) 1, 10, "UPDATETIME DESC");
	}
}
