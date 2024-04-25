package com.syscom.fep.mybatis.ext.mapper;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.History;

public class HistoryExtMapperTest extends MybatisBaseTest {
	@Autowired
	private HistoryExtMapper historyMapper;

	private History history;

	@BeforeEach
	public void setup() {
		history = new History();
		history.setHistoryBatchid(1);
		history.setHistoryDuration(2);
		history.setHistoryInstanceid(Long.toString(System.currentTimeMillis()));
		history.setHistoryJobid(3);
		history.setHistoryLogfile("/home/syscom/fep-app-/batch/logs/log111111.txt");
		history.setHistoryMessage("Everything is OK");
		history.setHistoryStarttime(Calendar.getInstance().getTime());
		history.setHistoryStatus("1");
		history.setHistoryStepid(99);
		history.setHistoryTaskbegintime(Calendar.getInstance().getTime());
		history.setHistoryTaskendtime(Calendar.getInstance().getTime());
		history.setHistoryTaskid(4);
		history.setLogAuditTrail(true);
		history.setUpdateUser(8888);
	}

	@Test
	public void testInsert() {
		historyMapper.insert(history);
		UnitTestLogger.info(history.getHistorySeq().longValue());
	}

	@Test
	public void testInsertSelective() {
		historyMapper.insertSelective(history);
		UnitTestLogger.info(history.getHistorySeq().longValue());
	}
}
