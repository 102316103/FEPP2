package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class ChannelExtMapperTest extends MybatisBaseTest {

	@Autowired
	private ChannelExtMapper mapper;
	
	@Test
	public void testSelectByChannelName() {
		mapper.selectByChannelName("ATM");
	}

}
