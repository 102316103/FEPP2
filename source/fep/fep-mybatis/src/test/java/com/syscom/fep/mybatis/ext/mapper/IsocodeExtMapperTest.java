package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class IsocodeExtMapperTest extends MybatisBaseTest {

	@Autowired
	private IsocodeExtMapper mapper;
	
	@Test
	public void testQueryByAlpha3() {
		mapper.queryByAlpha3("11");
	}

}
