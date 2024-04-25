package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class MsgctlExtMapperTest extends MybatisBaseTest {

	@Autowired
	private MsgctlExtMapper mapper;
	
	@Test
	public void testSelectAll() {
		mapper.selectAll();
	}

}
