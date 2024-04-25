package com.syscom.fep.mybatis.ext.mapper;

import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

public class WkpostdtlExtMapperTest extends MybatisBaseTest {
	@Autowired
	private WkpostdtlExtMapper mapper;

	@Test
	public void testGetWkPostDtl() {
		mapper.getWkPostDtl(Calendar.getInstance().getTime(), null, null, null, null, null, null, null, null, "WKPOSTDTL");
	}
}
