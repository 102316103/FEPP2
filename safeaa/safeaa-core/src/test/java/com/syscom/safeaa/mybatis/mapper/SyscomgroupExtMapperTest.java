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
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupExtMapper;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomgroupExtMapperTest {

	@Autowired
	private SyscomgroupExtMapper mapper;

	@Test
	public void getSyscomgroupInfoVoAllTest() {
		List<SyscomgroupInfoVo> list = mapper.getSyscomgroupInfoVoAll();
		assertNotNull(list);
		Assert.assertEquals("root", list.get(0).getName());
//		System.out.println(list.get(30).getEffectdate());

	}

	@Test
	public void getMaxLocationnoTest() {
		Integer locationno = mapper.getMaxLocationno();
		assertNotNull(locationno);
	}

	@Test
	public void queryAllExcludeGroupsTest() {
		mapper.queryAllExcludeGroups("SYSCOM", "zh-TW");
	}

	@Test
	public void queryAllExcludeGroupsTest2() {
		mapper.queryGroupIdByNo(null);
		mapper.queryAllGroups(null);
	}
}
