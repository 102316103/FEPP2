package com.syscom.fep.frmcommon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.syscom.fep.frmcommon.log.LogHelper;

import java.util.Calendar;

public class CalendarUtilTest {
    private LogHelper logger = new LogHelper();

    @Test
    public void testAdStringToROCString() {
        assertEquals("1100427", CalendarUtil.adStringToROCString("20210427"));
        assertEquals("20210427", CalendarUtil.rocStringToADString("1100427"));
        assertEquals(StringUtils.EMPTY, CalendarUtil.rocStringToADString("00000000"));
        assertEquals(StringUtils.EMPTY, CalendarUtil.adStringToROCString("00000000"));
        logger.info(CalendarUtil.validateDateTime(null, "yyyyMMdd"));

        logger.info(CalendarUtil.adStringToROCString("2021/09/23".replace("/", "")));
    }

    @Test
    public void test(){
        logger.info(CalendarUtil.getWeekOfMonth(CalendarUtil.parseDateValue(20230522)));
        logger.info(CalendarUtil.getWeekOfMonth(CalendarUtil.parseDateValue(20230126)));
        logger.info(CalendarUtil.getWeekOfMonth(CalendarUtil.parseDateValue(20230127)));
        logger.info(CalendarUtil.getWeekOfMonth(CalendarUtil.parseDateValue(20230128)));
        logger.info(CalendarUtil.getWeekOfMonth(CalendarUtil.parseDateValue(20230129)));
    }
}
