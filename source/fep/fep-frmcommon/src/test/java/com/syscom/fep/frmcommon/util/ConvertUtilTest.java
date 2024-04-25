package com.syscom.fep.frmcommon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class ConvertUtilTest {

    @Test
    public void testToHex() throws UnsupportedEncodingException {
//        assertEquals("52696368617264", ConvertUtil.toHex("Richard".getBytes()));
//        assertEquals("E4BDA0E5A5BDE4B8ADE59BBD", ConvertUtil.toHex("你好中國".getBytes(StandardCharsets.UTF_8)));

        // ConvertUtil.hexToByte("8A");
        System.out.println(Integer.parseInt("8A", 0x10));

        System.out.println(ConvertUtil.toHex("1CKEEPALIVEREQ20221214125959".getBytes()));

        System.out.println(ConvertUtil.toBytes(StringUtils.EMPTY, StandardCharsets.UTF_8));
    }

    @Test
    public void test() {
        byte[] bytes = ConvertUtil.toBytes("000000000000000000000000", StandardCharsets.US_ASCII);
        System.out.println(StringUtils.join(bytes, ','));

        String message = "I am '{'{0}'}', {1}";
        System.out.println(MessageFormat.format(message, "Richard", "I am a boy!!!"));

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:/Users/Richard/Desktop/aaaaaa.csv"), StandardCharsets.UTF_8))) {
            bw.write("\uFEFF");
            bw.write("姓名,年齡,班級");
        } catch (Exception e) {
            // TODO logging exception or throw
        }
    }
}
