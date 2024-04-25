package com.syscom.safeaa.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private StringUtil() {
    }

    /**
     * 字串轉十六進制ASCII字串
     *
     * @param str
     * @return
     */
    public static String toHex(String str) {
        return toHex(str, StandardCharsets.US_ASCII);
    }

    /**
     * 字串轉為十六進制指定編碼的字串
     *
     * @param str
     * @param charset
     * @return
     */
    public static String toHex(String str, Charset charset) {
        return toHex(str, charset, false);
    }

    /**
     * 字串轉為十六進制指定編碼的字串，並指定大小寫
     *
     * @param str
     * @param charset
     * @param toLowerCase
     * @return
     */
    public static String toHex(String str, Charset charset, boolean toLowerCase) {
        return toHex(str.getBytes(charset), toLowerCase);
    }

    /**
     * 字串轉為十六進制指定編碼的字串
     *
     * @param bytes
     * @return
     */
    public static String toHex(byte[] bytes) {
        return toHex(bytes, false);
    }

    /**
     * 字串轉為十六進制指定編碼的字串，並指定大小寫
     *
     * @param bytes
     * @param toLowerCase
     * @return
     */
    public static String toHex(byte[] bytes, boolean toLowerCase) {
        return Hex.encodeHexString(bytes, toLowerCase);
    }

    /**
     * 十六進制字串轉ASCII碼字串
     *
     * @param str
     * @return
     * @throws DecoderException
     */
    public static String fromHex(String str) throws DecoderException {
        return fromHex(str, true);
    }

    /**
     * 十六進制字串轉ASCII碼字串
     *
     * @param str
     * @param ignoreZeroInHex
     * @return
     * @throws DecoderException
     */
    public static String fromHex(String str, boolean ignoreZeroInHex) throws DecoderException {
        return fromHex(str, StandardCharsets.US_ASCII, ignoreZeroInHex);
    }

    /**
     * 十六進制字串轉指定編碼字串
     *
     * @param str
     * @param charset
     * @param ignoreZeroInHex
     * @return
     * @throws DecoderException
     */
    public static String fromHex(String str, Charset charset, boolean ignoreZeroInHex) throws DecoderException {
        byte[] bytes = Hex.decodeHex(str);
        if (ignoreZeroInHex) {
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 0) {
                    bytes[i] = 0x20;
                }
            }
        }
        return new String(bytes, charset);
    }

    /**
     * 將字串由源進制轉為目標進制字串，並指定左邊補齊0
     *
     * @param value
     * @param fromBase
     * @param toBase
     * @param paddingZero
     * @return
     */
    public static String convertFromAnyBaseString(String value, int fromBase, int toBase, int paddingZero) {
        long num = Long.parseLong(value, fromBase);
        if (paddingZero <= 0) {
            return Long.toString(num, toBase).toUpperCase();
        }
        return StringUtils.leftPad(Long.toString(num, toBase), paddingZero, '0').toUpperCase();
    }

    /**
     * 判斷字串是否是十六進制字串
     *
     * @param str
     * @return
     */
    public static boolean isHex(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!isHexChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判斷字符是否是十六進制字符
     *
     * @param ch
     * @return
     */
    public static boolean isHexChar(char ch) {
        if (ch >= '0' && ch <= '9')
            return true;
        if (ch >= 'A' && ch <= 'F')
            return true;
        if (ch >= 'a' && ch <= 'f')
            return true;
        return false;
    }

    /**
     * 拼接字串, 如果字串為null, 則拼接一個空白字串
     *
     * @param sb
     * @param str
     * @return
     */
    public static StringBuilder append(StringBuilder sb, String str) {
        return sb.append(str == null ? StringUtils.EMPTY : str);
    }

    /**
     * 將指定beginIndex到endIndex的字符轉為小寫
     *
     * @param str
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static String toLowerCase(String str, int beginIndex, int endIndex) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (beginIndex > endIndex) {
            throw ExceptionUtil.createIllegalArgumentException("beginIndex = [", beginIndex, "] cannot bigger than endIndex = [", endIndex, "]!!");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < beginIndex - 1; i++) {
                sb.append(str.charAt(i));
            }
            for (int i = beginIndex; i <= endIndex; i++) {
                sb.append(String.valueOf(str.charAt(i)).toLowerCase());
            }
            for (int i = endIndex + 1; i < str.length(); i++) {
                sb.append(str.charAt(i));
            }
            return sb.toString();
        }
    }

    /**
     * 正則替換字符串
     *
     * @param str
     * @param regexMap
     * @return
     */
    public static String replace(String str, Map<String, String> regexMap) {
        if (StringUtils.isNotBlank(str) && MapUtils.isNotEmpty(regexMap)) {
            for (Map.Entry<String, String> entry : regexMap.entrySet()) {
                Pattern p = Pattern.compile(entry.getKey());
                Matcher m = p.matcher(str);
                str = m.replaceAll(entry.getValue());
            }
        }
        return str;
    }
}
