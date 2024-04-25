package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.model.Atmcash;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AtmcashExtMapperTest extends MybatisBaseTest {
	@Autowired
	private AtmcashExtMapper mapper;

	@Test
	public void testGetAtmCashByAtmNo() {
		List<Atmcash> list = mapper.getAtmCashByAtmNo("01027", "ATMCASH_BOXNO");
		assertTrue(CollectionUtils.isEmpty(list));
	}
	
	@Test
	public void testUpdateBusinessDay() {
		int result = mapper.updateBusinessDay("01027");
		assertEquals((int)0, result);
	}
	@Test
	public void getAtmCashByCurAtmNoForIWDTest() {
		Map<String, Object> resuit = mapper.getAtmCashByCurAtmNoForIWD("01027", "TWD");
		assertNull(resuit);
	}
}
