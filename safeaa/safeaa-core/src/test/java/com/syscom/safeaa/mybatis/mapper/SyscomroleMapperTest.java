package com.syscom.safeaa.mybatis.mapper;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleExtMapper;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomroleMapperTest {

	@Autowired
	private SyscomroleExtMapper mapper;

	@Test
	public void getAllRolesTest() {
		List<SyscomroleAndCulture> result = mapper.queryAllRoles(new SyscomroleAndCulture("zh-TW"));
//		assertEquals(result.size(), 7);
	}

	@Test
	public void queryRoleIdByNoTest() {
		mapper.queryRoleIdByNo("FEPAdmin");
		mapper.queryRoleIdByNo("!@#$%^&*()_+");
//		assertEquals(mapper.queryRoleIdByNo("FEPAdmin"), new Long(2L));
//		assertEquals(mapper.queryRoleIdByNo("!@#$%^&*()_+"), null);
	}

	@Test
	public void getSyscomroleInfoVoAll() {
		mapper.getSyscomroleInfoVoAll();
	}
	
}
