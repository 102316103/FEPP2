package com.syscom.fep.mybatis.ext.mapper;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Fepuser;

public class FepuserExtMapperTest extends MybatisBaseTest {
	@Autowired
	private FepuserExtMapper mapper;

	private Fepuser record;

	@BeforeEach
	public void setup() {
		record = new Fepuser();
		record.setFepuserBoss("bos");
		record.setFepuserBrno("brn");
		record.setFepuserBrnoSt("bst");
		record.setFepuserGroup("gro");
		record.setFepuserJob("j");
		record.setFepuserLevel((short) 1);
		record.setFepuserLogonid("1312");
		record.setFepuserLuDate("20220506");
		record.setFepuserLuTime("122339");
		record.setFepuserName("YU");
		record.setFepuserStatus((short) 0);
		record.setFepuserTlrno("tlr");
		record.setFepuserUserid(1312);
		record.setUpdateTime(Calendar.getInstance().getTime());
		record.setUpdateUser(0);
		record.setUpdateUserid(0);
		record.setUserUpdateTime(Calendar.getInstance().getTime());
	}

	@Test
	public void testInsertSelective() {
		mapper.insertSelective(record);
	}
}
