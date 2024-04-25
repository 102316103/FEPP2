package com.syscom.fep.mybatis.ext.mapper;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Odrc;

public class OdrcExtMapperTest extends MybatisBaseTest {
	@Autowired
	private OdrcExtMapper mapper;

	@Test
	public void testGetODRCByDay() {
		Odrc record = new Odrc();
		record.setOdrcTxDate("20210720");
		record.setOdrcBkno("807");
		record.setOdrcActno(StringUtils.rightPad("123456", 16, StringUtils.SPACE));
		record.setOdrcCurcd("999");
		Map<String, Object> result = mapper.getOdrcByDay(record);
		if (MapUtils.isNotEmpty(result))
			LogHelperFactory.getUnitTestLogger().info("TOT_AMT = ", result.get("TOT_AMT"));
	}

	@Test
	public void testInsertSelective() {
		Odrc record = new Odrc();
		record.setOdrcTxDate("20210720");
		record.setOdrcBkno("807");
		record.setOdrcActno(StringUtils.rightPad("123456", 16, StringUtils.SPACE));
		record.setOdrcCurcd("999");
		record.setOdrcTxCnt(1);
		record.setOdrcTxAmt(BigDecimal.valueOf(100L));
		mapper.insertSelective(record);
	}
}
