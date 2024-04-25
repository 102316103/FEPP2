package com.syscom.safeaa.mybatis.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomserialMapperTest {
//	@Autowired
//	private SyscomserialMapper syscomserialMapper;
	
	@Test
	public void SelectByPrimaryKeyTest() {
//		Syscomserial syscomserial = syscomserialMapper.selectByPrimaryKey("SAFEId");
//		Assert.assertNotNull(syscomserial);
	}
	
	@Test
	public void UpdateByPrimaryKeyTest() {
//		Syscomserial syscomserial = syscomserialMapper.selectByPrimaryKey("SAFEId");
//		Assert.assertNotNull(syscomserial);
//		syscomserial.setNextid(syscomserial.getNextid().add(new BigDecimal("1")));
//		int rst = syscomserialMapper.updateByPrimaryKey(syscomserial);
//		Assert.assertEquals(1, rst);
	
	}
}
