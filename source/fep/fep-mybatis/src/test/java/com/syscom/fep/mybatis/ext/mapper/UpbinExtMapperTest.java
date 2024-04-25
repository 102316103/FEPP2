package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class UpbinExtMapperTest extends MybatisBaseTest {

	@Autowired
	private UpbinExtMapper mapper;

	@Test
	public void testQueryAllData() {
		mapper.queryAllData("UPBIN_BIN");
	}

}
