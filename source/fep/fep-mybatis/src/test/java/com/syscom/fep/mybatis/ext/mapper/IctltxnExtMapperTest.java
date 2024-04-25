package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class IctltxnExtMapperTest extends MybatisBaseTest {
	
	@Autowired
	private IctltxnExtMapper mapper;
	
	@Test
	public void testQueryByOriData() {
		mapper.queryByOriData("11", "22");
	}

}
