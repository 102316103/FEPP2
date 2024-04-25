package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Intltxn;

public class IntltxnExtMapperTest extends MybatisBaseTest {

	@Autowired
	private IntltxnExtMapper mapper;
	
	private Intltxn intltxn;
	
	@BeforeEach
	public void setup() {
		intltxn = new Intltxn();
		intltxn.setIntltxnOriMsgtype("1");
		intltxn.setIntltxnOriVisaStan("2");
		intltxn.setIntltxnOriTxMmddtime("3");
		intltxn.setIntltxnOriAcq("4");
		intltxn.setIntltxnOriFwdInst("5");
		intltxn.setIntltxnStan("6");
		intltxn.setIntltxnTxDate("7");
	}
	
	@Test
	public void testQueryByOriData() {
		mapper.queryByOriData(intltxn);
	}

	@Test
	public void testQueryByOriDataEmv() {
		mapper.queryByOriDataEmv(intltxn);
	}
	
	@Test
	public void testSelectForCheckOwdCount() {
		mapper.selectForCheckOwdCount("1", "2", "3", "4");
	}
}
