package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class BarcodeExtMapperTest  extends MybatisBaseTest {
	@Autowired
	private BarcodeExtMapper mapper;

	@Test
	public void testQueryByActId() {
		mapper.queryByActId(1);
	}

}
