package com.syscom.fep.frmcommon.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StringUtil {
    private StringUtil() {}

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
     * 字節數組轉為十六進制指定編碼的字串
     *
     * @param bytes
     * @return
     */
    public static String toHex(byte[] bytes) {
        return toHex(bytes, false);
    }

    /**
     * 字節數組轉為十六進制指定編碼的字串，並指定大小寫
     *
     * @param bytes
     * @param toLowerCase
     * @return
     */
    public static String toHex(byte[] bytes, boolean toLowerCase) {
        return Hex.encodeHexString(bytes, toLowerCase);
    }

    /**
     * 字節數組轉為十六進制指定編碼的字串
     *
     * @param bytes
     * @param length
     * @return
     */
    public static String toHex(byte[] bytes, int length) {
        return toHex(bytes, length, false);
    }

    /**
     * 字節數組轉為十六進制指定編碼的字串，並指定大小寫
     *
     * @param bytes
     * @param length
     * @param toLowerCase
     * @return
     */
    public static String toHex(byte[] bytes, int length, boolean toLowerCase) {
        String hex = ConvertUtil.toHex(bytes, 0, length);
        return toLowerCase ? hex.toLowerCase() : hex;
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
        long num = Long.MIN_VALUE;
        boolean parseUnsigned = false;
        try {
            num = Long.parseLong(value, fromBase);
        } catch (NumberFormatException e) {
            num = Long.parseUnsignedLong(value, fromBase);
            parseUnsigned = true;
        }
        String result = null;
        if (parseUnsigned) {
            result = Long.toUnsignedString(num, toBase);
        } else {
            result = Long.toString(num, toBase);
        }
        if (result != null)
            result = result.toUpperCase();
        if (paddingZero <= 0)
            return result;
        return StringUtils.leftPad(result, paddingZero, '0');
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

    public static String unPack(String data) {
        // 特殊字元不UNPACK (: ; < = > ?)
        // if (!StringUtils.containsAny(data, ':', ';', '<', '=', '>', '?')) {
        // return data;
        // }
        data = StringUtil.toHex(data);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length(); i += 2) {
            String x = data.substring(i, i + 2);
            int dec = Integer.parseInt(x, 16);
            char ascS;
            if (dec >= 48 && dec <= 57) {
                ascS = (char) dec;
            } else {
                ascS = (char) (dec + 7);
            }
            sb.append(ascS);
        }
        return sb.toString();
    }

    /**
     * 根據0123456789:;<=>?, 產出指定長度的隨機字串
     *
     * @param count – the length of random string to create
     * @return
     */
    public static String random(int count) {
        return random(count, "0123456789:;<=>?");
    }

    /**
     * 根據指定的字串, 產出指定長度的隨機字串
     *
     * @param count – the length of random string to create
     * @param chars – the String containing the set of characters to use, may be null, but must not be empty
     * @return
     */
    public static String random(int count, String chars) {
        return RandomStringUtils.random(count, chars);
    }

    /**
     * 根據傳入多个分隔符分隔字符串
     *
     * @param str
     * @param separatorChars
     * @return
     */
    public static List<String> split(final String str, char... separatorChars) {
        if (StringUtils.isBlank(str) || ArrayUtils.isEmpty(separatorChars) || Stream.<char[]>of(separatorChars).filter(Objects::isNull).count() == separatorChars.length) {
            return Collections.singletonList(str);
        }
        List<String> result = new ArrayList<>();
        result.add(str);
        List<String> tmp = new ArrayList<>();
        for (char separatorChar : separatorChars) {
            for (String s : result) {
                String[] splits = StringUtils.split(s, separatorChar);
                if (ArrayUtils.isNotEmpty(splits)) {
                    Collections.addAll(tmp, splits);
                }
            }
            result.clear();
            result.addAll(tmp);
            tmp.clear();
        }
        return result;
    }

    /**
     * 判斷一個字符是否為中文, 包含簡體繁體以及標點符號
     *
     * @param ch
     * @return
     */
    public static boolean isChinese(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    /**
     * 判斷一個字符串是否全部是中文, 包含簡體繁體以及標點符號
     *
     * @param str
     * @return
     */
    public static boolean isChinese(String str) {
        char[] ch = str.toCharArray();
        for (char c : ch) {
            if (!isChinese(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 將一個字串中文和非中文提取出來, 按順序拆解成字串數組
     *
     * @param str
     * @return
     */
    public static List<String> splitByChinese(String str) {
        List<String> list = new ArrayList<>();
        char[] ch = str.toCharArray();
        int lastType = 0; // 1 is NonChinese, 2 is Chinese
        StringBuilder sb = new StringBuilder();
        for (char c : ch) {
            if (isChinese(c)) {
                if (lastType == 1) {
                    list.add(sb.toString());
                    sb.setLength(0);
                    sb.append(c);
                } else {
                    sb.append(c);
                }
                lastType = 2;
            } else {
                if (lastType == 2) {
                    list.add(sb.toString());
                    sb.setLength(0);
                    sb.append(c);
                } else {
                    sb.append(c);
                }
                lastType = 1;
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list;
    }

    /**
     * 將一個字串按照UTF-8編碼切割為最大長度限制的字串
     *
     * @param str
     * @param byteLengthLimit
     * @return
     */
    public static String substring(String str, int byteLengthLimit) {
        if (StringUtils.isBlank(str))
            return str;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > byteLengthLimit) {
            bytes = ArrayUtils.subarray(bytes, 0, byteLengthLimit);
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return str;
    }
}
