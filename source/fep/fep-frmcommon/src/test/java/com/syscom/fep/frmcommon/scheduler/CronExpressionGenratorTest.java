package com.syscom.fep.frmcommon.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.frmcommon.FrmcommonBaseTest;
import com.syscom.fep.frmcommon.util.FormatUtil;

public class CronExpressionGenratorTest extends FrmcommonBaseTest {
	@Autowired
	private CronExpressionGenerator generator;

	@Test
	public void testgenerateCronExpressionByDaysInterval() throws ParseException {
		Date startDate = FormatUtil.parseDataTime("12:30:45", FormatUtil.FORMAT_TIME_HH_MM_SS);
		assertEquals("45 30 12 * * ? *", generator.generateCronExpressionByDaily(startDate, (short) 1));
		assertEquals("45 30 12 */2 * ? *", generator.generateCronExpressionByDaily(startDate, (short) 2));
	}
}
