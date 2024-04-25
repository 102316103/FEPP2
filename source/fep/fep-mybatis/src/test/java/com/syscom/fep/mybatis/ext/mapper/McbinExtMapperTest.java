package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class McbinExtMapperTest extends MybatisBaseTest {

	@Autowired
	private McbinExtMapper mapper;
	
	@Test
	public void testQueryAllData() {
		mapper.queryAllData("MCBIN_FROM_BIN");
	}

}
