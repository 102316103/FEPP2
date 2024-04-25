package com.syscom.fep.mybatis.ext.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Inbkparm;

public class InbkparmExtMapperTest extends MybatisBaseTest {
	@Autowired
	private InbkparmExtMapper mapper;

	@Test
	public void testQueryByPK() {
		Inbkparm record = new Inbkparm();
		record.setInbkparmApid("2261");
		record.setInbkparmPcode(StringUtils.EMPTY);
		record.setInbkparmAcqFlag("A");
		record.setInbkparmEffectDate("19990101");
		record.setInbkparmCur("TWD");
		Inbkparm result = mapper.queryByPK(record);
		assertTrue(result != null);
	}
}
