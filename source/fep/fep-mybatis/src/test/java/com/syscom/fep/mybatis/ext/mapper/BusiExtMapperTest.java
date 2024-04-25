package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.model.Busi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BusiExtMapperTest extends MybatisBaseTest {
	@Autowired
	private BusiExtMapper mapper;

	@Test
	public void testSelectById() {
		Busi result = mapper.selectById("a");
		assertNotNull(result);
	}

}
