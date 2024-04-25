package com.syscom.fep.frmcommon.util;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class FormatUtilTest {
    @Test
    public void testDoubleFormat() {
        BigDecimal b = new BigDecimal(123456.12);
        System.out.println(FormatUtil.decimalFormat(b,"#,###.###"));
        b = new BigDecimal(123456);
        System.out.println(FormatUtil.decimalFormat(b,"#,##0"));
        b = new BigDecimal(0);
        System.out.println(FormatUtil.decimalFormat(b,"#,##0"));
    }
}
