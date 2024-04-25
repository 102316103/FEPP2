package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Atmcoin;

public class AtmcoinExtMapperTest extends MybatisBaseTest {
	@Autowired
	private AtmcoinExtMapper mapper;

	private Atmcoin atmcoin;

	@BeforeEach
	public void setup() {
		atmcoin = new Atmcoin();
		atmcoin.setAtmcoinTxDate("1");
		atmcoin.setAtmcoinRwtSeqno(3);
		atmcoin.setAtmcoinAtmno("2");
		atmcoin.setAtmcoinSettle((short) 4);
		atmcoin.setAtmcoinBoxno((short) 5);
		atmcoin.setAtmcoinCur("5");
		atmcoin.setAtmcoinRefill(7);
		atmcoin.setAtmcoinDeposit(8);
		atmcoin.setAtmcoinUnit(6);
	}

	@Test
	public void testGetAtmCoinForInventoryCash() {
		List<Long> list = mapper.getAtmCoinForInventoryCash("62022", 1);
		UnitTestLogger.info(list.get(0).intValue());
	}

	@Test
	public void testUpdateByCandidateKeyAccumulation() {
		mapper.updateByCandidateKeyAccumulation(atmcoin);
	}
}
