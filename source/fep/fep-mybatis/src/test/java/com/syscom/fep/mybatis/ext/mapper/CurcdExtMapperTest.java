package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class CurcdExtMapperTest extends MybatisBaseTest {

	@Autowired
	private CurcdExtMapper mapper;
	
	@Test
	public void testSelectAll() {
		mapper.selectAll();
	}

}
