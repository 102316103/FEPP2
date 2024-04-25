package com.syscom.fep.frmcommon.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class ConvertUtil {
    private static final String HEX_STRING = "0123456789ABCDEF";

    private ConvertUtil() {}

    /**
     * 十六進制字串轉字節數組
     *
     * @param hexString
     * @return
     */
    public static byte[] hexToBytes(String hexString) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexString.length() / 2);
        for (int i = 0; i < hexString.length(); i += 2) {
            baos.write((HEX_STRING.indexOf(hexString.charAt(i)) << 4 | HEX_STRING.indexOf(hexString.charAt(i + 1))));
        }
        return baos.toByteArray();
    }

    /**
     * 將字節數組由源編碼轉為目標編碼
     *
     * @param bytes
     * @param srcCharsetName
     * @param dstCharsetName
     * @return
     */
    public static byte[] toBytes(byte[] bytes, String srcCharsetName, String dstCharsetName) {
        return new String(bytes, Charset.forName(srcCharsetName)).getBytes(Charset.forName(dstCharsetName));
    }

    /**
     * 字節數字轉十六進制
     *
     * @param bytes
     * @return
     */
    public static String toHex(byte[] bytes) {
        return toHex(bytes, 0, bytes.length);
    }

    public static String asciiToHex(String ascii) {
        return asciiToHex(ascii, true);
    }

    public static String asciiToHex(String ascii, boolean toUpperCase) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < ascii.length(); i++) {
            if (toUpperCase)
                hex.append(Integer.toHexString(ascii.charAt(i)).toUpperCase());
            else
                hex.append(Integer.toHexString(ascii.charAt(i)));
        }
        return hex.toString();
    }

    /**
     * 字節數組轉十六進制字串
     *
     * @param bytes
     * @param offset
     * @param length
     * @return
     */
    public static String toHex(byte[] bytes, int offset, int length) {
        if (offset + length >= bytes.length) {
            length = bytes.length - offset;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(toHex(bytes[i + offset]));
        }
        return sb.toString();
    }

    /**
     * 字節轉十六進制字串
     *
     * @param b
     * @return
     */
    public static String toHex(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(HEX_STRING.charAt(0xf & b >> 4)));
        sb.append(String.valueOf(HEX_STRING.charAt(b & 0xf)));
        return sb.toString();
    }

    /**
     * 十六進制字串轉字節
     *
     * @param hexString
     * @return
     */
    public static byte hexToByte(String hexString) {
        return Byte.parseByte(hexString, 0x10);
    }

    /**
     * 十六進制int型字串轉字節
     *
     * @param hexInt
     * @return
     */
    public static byte intToByte(String hexInt) {
        return (byte) Integer.parseInt(hexInt, 16);
    }

    /**
     * 將字節數組轉為目標編碼的字串
     *
     * @param bytes
     * @param charsetName
     * @return
     */
    public static String toString(byte[] bytes, String charsetName) {
        return toString(bytes, Charset.forName(charsetName));
    }

    public static String toString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    /**
     * 將字節數組轉為目標編碼的字串
     *
     * @param str
     * @param charsetName
     * @return
     */
    public static byte[] toBytes(String str, String charsetName) {
        return toBytes(str, Charset.forName(charsetName));
    }

    public static byte[] toBytes(String str, Charset charset) {
        return str.getBytes(charset);
    }
}
