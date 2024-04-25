package com.syscom.safeaa.mybatis.mapper;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.syscom.safeaa.core.SyscomSafeAaCoreTestApplication;
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupmembersExtMapper;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SyscomSafeAaCoreTestApplication.class)
@ComponentScan(basePackages = { "com.syscom.safeaa.*" })
@MapperScan(basePackages = { "com.syscom.safeaa.mybatis.mapper" })
public class SyscomgroupmembersExtMapperTest {
	@Autowired
	private SyscomgroupmembersExtMapper mapper;

	@Test
	public void queryUNSelectMembers() {
		mapper.queryUNSelectMembers(1, "zh-TW");
	}
	@Test
	public void queryNestedMembersByGroupId() {
		List<SyscomgroupmembersAndGroupLevel> list = mapper.queryNestedMembersByGroupId(32, "zh-TW");
		list.size();
	}
	@Test
	public void querySelectedByGroupId() {
		mapper.querySelectedByGroupId(32, "zh-TW");
	}
	@Test
	public void getMaxLocationno() {
		mapper.getMaxLocationno(32);
	}
}
