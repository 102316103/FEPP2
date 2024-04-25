package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class DataattrExtMapperTest extends MybatisBaseTest {

	@Autowired
	private DataattrExtMapper mapper;
	
	@Test
	public void testQueryAllData() {
		mapper.queryAllData("DATAATTR_TYPE");
	}

}
