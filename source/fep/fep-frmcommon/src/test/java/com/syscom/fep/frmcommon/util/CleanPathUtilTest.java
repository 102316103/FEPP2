package com.syscom.fep.frmcommon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CleanPathUtilTest {

    @Test
    public void test() {
        String expected = "C:\\Users\\Richard\\";
        assertEquals(expected, CleanPathUtil.cleanString(expected));
    }
}
