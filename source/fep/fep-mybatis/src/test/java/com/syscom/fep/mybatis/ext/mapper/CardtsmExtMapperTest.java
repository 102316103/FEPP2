package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.model.Cardtsm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CardtsmExtMapperTest extends MybatisBaseTest {
	@Autowired
	private CardtsmExtMapper mapper;

	@Test
	public void testGetSingleCard() {
		Cardtsm cardtsm = mapper.getSingleCard("TEST");
		assertNotNull(cardtsm);
	}

}
