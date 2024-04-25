package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class SysstatExtMapperTest extends MybatisBaseTest {

	@Autowired
	private SysstatExtMapper mapper;
	
	@Test
	public void testSelectAll() {
		mapper.selectAll();
	}

}
