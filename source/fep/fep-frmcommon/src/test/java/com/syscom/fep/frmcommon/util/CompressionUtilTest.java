package com.syscom.fep.frmcommon.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class CompressionUtilTest {
    @Test
    public void test() throws Exception {
        CompressionUtil.compressFiles2Zip(
                new File[]{
                        new File(CleanPathUtil.cleanString("C:/Users/Richard/Desktop/openssl.txt")),
                        new File(CleanPathUtil.cleanString("C:/Users/Richard/Desktop/Batch.docx"))
                }, "C:/Users/Richard/Desktop/aaaaa.zip"
        );
    }
}
