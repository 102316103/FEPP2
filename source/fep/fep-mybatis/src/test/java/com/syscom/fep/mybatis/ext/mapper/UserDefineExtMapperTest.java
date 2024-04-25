package com.syscom.fep.mybatis.ext.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.mapper.EjfnoMapper;
import com.syscom.fep.mybatis.mapper.StanMapper;
import com.syscom.fep.mybatis.model.Ejfno;
import com.syscom.fep.mybatis.util.SpCaller;

public class UserDefineExtMapperTest extends MybatisBaseTest {
	@Autowired
	private UserDefineExtMapper mapper;
	@Autowired
	private EjfnoMapper ejfnoMapper;
	@Autowired
	private StanMapper stanMapper;

//	@Test
//	public void testGetEj() {
//		Map<String, Integer> args = new HashMap<>();
//		mapper.getEj(args);
//		int ejfno = args.get(SpCaller.OUT_ID);
//		Ejfno entity = ejfnoMapper.selectByPrimaryKey(ejfno);
//		assertTrue(entity != null);
//	}

//	@Test
//	public void testGetStan() {
//		Map<String, Integer> args = new HashMap<>();
//		mapper.getStan(args);
//		int stan = args.get(SpCaller.OUT_ID);
//		Stan entity = stanMapper.selectByPrimaryKey(stan);
//		assertTrue(entity != null);
//	}

	@Test
	public void testGetPBMRbyIDNO() {
		Map<String, Object> args = new HashMap<>();
		args.put("IDNO", "123456");
		args.put("STARTDAY", "20210702");
		mapper.getPbmrByIdNo(args);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Map> result = (List<Map>) args.get("RESULT");
		assertTrue(result != null && result.size() == 4);
	}

	@Test
	public void testQueryIbtId() {
		Map<String, Object> args = new HashMap<>();
		args.put("IDNO", "123456");
		args.put("STARTDAY", "20210702");
		mapper.queryIbtId(args);
	}

	@Test
	public void testGetBoxByIdNo() {
		Map<String, Object> args = new HashMap<>();
		args.put("IDNO", "123456");
		args.put("STARTDAY", "20210702");
		mapper.getBoxByIdNo(args);
	}
}
