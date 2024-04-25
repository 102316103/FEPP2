package com.syscom.fep.mybatis.mapper;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Account;

public class AccountMapperTest extends MybatisBaseTest {
	@Autowired
	private AccountMapper mapper;

	@Test
	public void testSelectByPrimaryKey() {
		Account expected = new Account(StringUtils.leftPad("1", 14, "0"), "2", "20210408");
		expected.setLogAuditTrail(true);
		mapper.insert(expected);
		Account actual = mapper.selectByPrimaryKey(expected.getAcctno(), expected.getAccttype());
		assertEquals(expected, actual);
	}

	@Test
	public void testDeleteByPrimaryKey() {
		Account expected = new Account(StringUtils.leftPad("1", 14, "0"), "2", "20210408");
		expected.setUpdateUser(837305);
		mapper.deleteByPrimaryKey(expected);
	}
}
