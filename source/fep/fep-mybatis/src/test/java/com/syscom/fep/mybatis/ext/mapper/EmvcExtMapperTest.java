package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class EmvcExtMapperTest extends MybatisBaseTest {

	@Autowired
	private EmvcExtMapper mapper;
	
	@Test
	public void testGetEmvcByMonth() {
		mapper.getEmvcByMonth("20210102", "20210809", "33");
	}

}
