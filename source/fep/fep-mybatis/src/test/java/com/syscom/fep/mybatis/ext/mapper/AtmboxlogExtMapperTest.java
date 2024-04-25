package com.syscom.fep.mybatis.ext.mapper;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Atmboxlog;

public class AtmboxlogExtMapperTest extends MybatisBaseTest {
	@Autowired
	private AtmboxlogExtMapper mapper;

	private Atmboxlog atmboxlog;

	@BeforeEach
	public void setup() {
		atmboxlog = new Atmboxlog();
		atmboxlog.setAtmboxlogAtmno("1");
		atmboxlog.setAtmboxlogBoxArea("2");
		atmboxlog.setAtmboxlogBoxCnt((short) 3);
		atmboxlog.setAtmboxlogEjfno(4);
		atmboxlog.setAtmboxlogRwtSeqno(5);
		atmboxlog.setAtmboxlogTxDate("6");
		atmboxlog.setLogAuditTrail(true);
		atmboxlog.setUpdateTime(Calendar.getInstance().getTime());
		atmboxlog.setUpdateUserid(100000);
	}

	@Test
	public void testInsert() {
		mapper.insert(atmboxlog);
	}

	@Test
	public void testInsertSelective() {
		atmboxlog.setUpdateTime(null);
		mapper.insertSelective(atmboxlog);
	}

	@Test
	public void testUpdateByPrimaryKeySelective() {
		atmboxlog.setUpdateUserid(null);
		mapper.updateByPrimaryKeySelective(atmboxlog);
	}

	@Test
	public void testUpdateByPrimaryKey() {
		atmboxlog.setUpdateUserid(null);
		mapper.updateByPrimaryKey(atmboxlog);
	}
}
