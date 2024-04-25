package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class BitmapdefExtMapperTest extends MybatisBaseTest {
	@Autowired
	private BitmapdefExtMapper mapper;

	@Test
	public void testQueryAllData() {
		mapper.queryAllData("UPDATE_TIME");
	}

}
