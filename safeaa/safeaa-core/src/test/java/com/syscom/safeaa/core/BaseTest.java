package com.syscom.safeaa.core;

import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@Rollback(true)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = {"com.syscom.safeaa.mybatis.extmapper" })
public class BaseTest {
	
}