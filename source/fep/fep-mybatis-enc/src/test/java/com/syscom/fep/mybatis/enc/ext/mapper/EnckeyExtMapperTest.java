package com.syscom.fep.mybatis.enc.ext.mapper;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.enc.MybatisEncBaseTest;

public class EnckeyExtMapperTest extends MybatisEncBaseTest {
	@Autowired
	private EnckeyExtMapper mapper;

	@Test
	public void testUpdateKey() {
		mapper.updateKey(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, 0, StringUtils.EMPTY);
	}
}
