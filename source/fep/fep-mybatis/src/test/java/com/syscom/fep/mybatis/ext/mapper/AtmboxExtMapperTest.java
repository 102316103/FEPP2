package com.syscom.fep.mybatis.ext.mapper;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.mapper.AtmboxMapper;
import com.syscom.fep.mybatis.model.Atmbox;

import static org.junit.jupiter.api.Assertions.*;

public class AtmboxExtMapperTest extends MybatisBaseTest {
	@Autowired
	private AtmboxExtMapper mapper;
	private Atmbox atmbox;
	
	@BeforeEach
	public void setup() {
		atmbox = new Atmbox();
		atmbox.setAtmboxAtmno("1");
		atmbox.setAtmboxBoxno((short)1);
		atmbox.setAtmboxBrnoSt("2");
		atmbox.setAtmboxCur("TWD");
		atmbox.setAtmboxDeposit(10000);
		atmbox.setAtmboxPresent(200000);
		atmbox.setAtmboxRefill(500);
		atmbox.setAtmboxReject(100);
		atmbox.setAtmboxRwtSeqno(300);
		atmbox.setAtmboxSettle((short)1);
		atmbox.setAtmboxTxDate("20210507");
		atmbox.setAtmboxUnit(900000);
		atmbox.setAtmboxUnknown(40);
		atmbox.setUpdateTime(Calendar.getInstance().getTime());
		atmbox.setUpdateUserid(100000);
	}
	
	@Test
	public void testInsert() {
		mapper.insert(atmbox);
		int result = mapper.insertSelective(atmbox);
		assertEquals((int)1, result);
	}

	@Test
	public void testInsertSelective() {
		atmbox.setUpdateTime(null);
		int result = mapper.insertSelective(atmbox);
		assertEquals((int)1, result);
	}

	@Test
	public void test() {
		atmbox.setAtmboxId(1);
		Atmbox result2 = mapper.queryByCandidateKey(atmbox);
		assertNotNull(result2);
		int result3 = mapper.updateByCandidateKey(atmbox);
		assertEquals((int)1, result3);
		int result1 = mapper.deleteByPrimaryKey(atmbox);
		assertEquals((int)1, result1);
	}
}
