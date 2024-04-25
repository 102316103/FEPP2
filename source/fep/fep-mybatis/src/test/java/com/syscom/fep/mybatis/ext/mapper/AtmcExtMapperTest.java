package com.syscom.fep.mybatis.ext.mapper;

import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Atmc;

import static org.junit.jupiter.api.Assertions.*;

public class AtmcExtMapperTest extends MybatisBaseTest {
	@Autowired
	private AtmcExtMapper mapper;

	private Atmc atmc;

	@BeforeEach
	public void setup() {
		atmc = new Atmc();
		atmc.setAtmcTbsdy("1");
		atmc.setAtmcBrnoSt("2");
		atmc.setAtmcAtmno("3");;
		atmc.setAtmcCur("4");
		atmc.setAtmcTxCode("5");
		atmc.setAtmcDscpt("6");
		atmc.setAtmcSelfcd((short) 1);
		atmc.setAtmcDrCnt(7);
		atmc.setAtmcDrAmt(new BigDecimal(8));
		atmc.setAtmcCrCnt(9);
		atmc.setAtmcCrCnt(10);
		atmc.setAtmcCrAmt(new BigDecimal(11));
		atmc.setAtmcTxCntCr(12);
		atmc.setAtmcTxFeeCr(new BigDecimal(13));
		atmc.setAtmcTxCntDr(14);
		atmc.setAtmcTxFeeDr(new BigDecimal(15));
		atmc.setAtmcTmCnt(16);
		atmc.setAtmcTmAmt(new BigDecimal(17));
		atmc.setAtmcLoc((short) 18);
		atmc.setAtmcZone("19");
		atmc.setAtmcCurSt("20");
		atmc.setAtmcCrossFlag((short) 21);
		atmc.setAtmcTbsdyFisc("22");
		atmc.setUpdateTime(Calendar.getInstance().getTime());
		atmc.setUpdateUserid(100000);
	}

	@Test
	public void testInsert() {
		mapper.insert(atmc);
	}

	@Test
	public void testInsertSelective() {
		atmc.setUpdateTime(null);
		mapper.insertSelective(atmc);
	}

	@Test
	public void testGetAtmcByConditions() {
		Atmc result = mapper.getAtmcByConditions("1", "22", "2", "3", "4", "5", "6", (short) 1);
		assertNotNull(result);

		Atmc result1 = mapper.getAtmcByConditions(null, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, null, StringUtils.EMPTY, StringUtils.EMPTY, null);
		assertNull(result1);
	}
}
