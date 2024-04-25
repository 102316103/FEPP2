package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class CbactExtMapperTest extends MybatisBaseTest {

	@Autowired
	private CbactExtMapper mapper;
	
	@Test
	public void testQueryCbactForMask() {
		mapper.queryCbactForMask("11", "22");
	}

}
