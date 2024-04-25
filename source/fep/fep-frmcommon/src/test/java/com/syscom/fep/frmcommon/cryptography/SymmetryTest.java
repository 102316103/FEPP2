package com.syscom.fep.frmcommon.cryptography;

import com.syscom.fep.frmcommon.util.ConvertUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SymmetryTest {

    /**
     * String dataInput = "hello";
     * byte[] key = Encoding.ASCII.GetBytes("123456789999123456789999");
     * byte[] iv = Encoding.ASCII.GetBytes("000000000000000000000000");
     * key.Dump();
     * iv.Dump();
     * Syscom.Utility.CryptographyLib.Symmetry.Encrypt.TripleDESEncrypt(dataInput, key, iv).Dump();
     * <p>
     * result:
     * <p>
     * 29
     * 240
     * 182
     * 212
     * 247
     * 41
     * 144
     * 165
     */
    @Test
    public void testTripleDESEncrypt() {
        byte[] expected = new byte[]{
                -81, (byte) 125, (byte) -54, (byte) 100, (byte) -94, (byte) -8, (byte) -90, (byte) -80,
                (byte) -55,(byte) -61,(byte) 100,(byte) -76,(byte) 101,(byte) -36,(byte) -11,(byte) 31,(byte) 121,(byte) 28,(byte) -43,(byte) 82,(byte) -24
        };
        byte[] actual = null;
        try {
            actual = Symmetry.Encrypt.encrypt("hello",
                    ConvertUtil.toBytes("1234567899991234", StandardCharsets.US_ASCII),
                    ConvertUtil.toBytes("0000000000000000", StandardCharsets.US_ASCII));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testTripleDESDecrypt() {
        String expected = "hello";
        String actual = null;
        try {
            byte[] dataInput = new byte[]{
                    -81, (byte) 125, (byte) -54, (byte) 100, (byte) -94, (byte) -8, (byte) -90, (byte) -80,
                    (byte) -55,(byte) -61,(byte) 100,(byte) -76,(byte) 101,(byte) -36,(byte) -11,(byte) 31,(byte) 121,(byte) 28,(byte) -43,(byte) 82,(byte) -24
            };
            actual = Symmetry.Decrypt.decrypt(dataInput,
                    ConvertUtil.toBytes("1234567899991234", StandardCharsets.US_ASCII),
                    ConvertUtil.toBytes("0000000000000000", StandardCharsets.US_ASCII));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(expected, actual);
    }
}
