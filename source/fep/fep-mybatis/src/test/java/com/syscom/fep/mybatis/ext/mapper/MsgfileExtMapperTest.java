package com.syscom.fep.mybatis.ext.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Msgfile;

public class MsgfileExtMapperTest extends MybatisBaseTest {

	@Autowired
	private MsgfileExtMapper mapper;
	
	@Test
	public void testSelectByMsgfileErrorcode() {
        mapper.selectByMsgfileErrorcode("4003");
	}
	
	@Test
	public void queryMsgFileByDef() {
		Map<String, Object> args = new HashMap<String, Object>();
		mapper.queryMsgFileByDef(args);
	}
	
	@Test
	public void queryMsgFileByDefLike() {
		Map<String, Object> args = new HashMap<String, Object>();
		mapper.queryMsgFileByDef(args);
	}

}
