package com.syscom.fep.frmcommon.ebcdic;

import com.ibm.as400.access.*;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.Chars;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EbcdicConverter {
    private static final LogHelper logger = new LogHelper();
    private static final String[] ENCIRCLE_FOR_CHINESE = new String[] {"0E", "0F"};
    private static final String ENGLISH = "IBM037";
    private static final String TRADITIONAL_CHINESE = "IBM937";

    private EbcdicConverter() {}

    /**
     * IBM data to Java Short
     *
     * @param bytes
     * @return
     */
    public static short toShort(byte[] bytes) {
        AS400Bin2 bin2Converter = new AS400Bin2();
        short value = bin2Converter.toShort(bytes);
        return value;
    }

    /**
     * IBM data to Java Integer
     *
     * @param bytes
     * @return
     */
    public static int toInteger(byte[] bytes) {
        AS400Bin4 bin4Converter = new AS400Bin4();
        int value = bin4Converter.toInt(bytes);
        return value;
    }

    /**
     * IBM data to Java Float
     *
     * @param bytes
     * @return
     */
    public static float toFloat(byte[] bytes) {
        AS400Float4 float4Converter = new AS400Float4();
        float value = float4Converter.toFloat(bytes);
        return value;
    }

    /**
     * IBM data to Java Double
     *
     * @param bytes
     * @return
     */
    public static double toDouble(byte[] bytes) {
        AS400Float8 double8Converter = new AS400Float8();
        double value = double8Converter.toDouble(bytes);
        return value;
    }

    /**
     * IBM data to Java String
     *
     * @param ccsid
     * @param bytes
     * @return
     */
    public static String toString(CCSID ccsid, byte[] bytes) {
        AS400Text textConverter = new AS400Text(bytes.length, ccsid.getValue());
        String value = (String) textConverter.toObject(bytes);
        return value;
    }

    /**
     * IBM Hex String to Java String
     *
     * @param ccsid
     * @param hex
     * @return
     */
    public static String fromHex(CCSID ccsid, String hex) {
        byte[] bytes = ConvertUtil.hexToBytes(hex.toUpperCase());
        String value = toString(ccsid, bytes);
        return value;
    }

    /**
     * Java Short to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(short value) {
        AS400Bin2 bin2Converter = new AS400Bin2();
        byte[] bytes = bin2Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java Integer to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(int value) {
        AS400Bin4 bin4Converter = new AS400Bin4();
        byte[] bytes = bin4Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java Float to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(float value) {
        AS400Float4 float4Converter = new AS400Float4();
        byte[] bytes = float4Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java Double to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(double value) {
        AS400Float8 double8Converter = new AS400Float8();
        byte[] bytes = double8Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java String to IBM data
     *
     * @param length
     * @param ccsid
     * @param value
     * @return
     */
    public static byte[] toBytes(CCSID ccsid, int length, String value) {
        AS400Text textConverter = new AS400Text(length, ccsid.getValue());
        byte[] bytes = textConverter.toBytes(value);
        return bytes;
    }

    /**
     * Java String to IBM Hex String
     *
     * @param length
     * @param ccsid
     * @param value
     * @return
     */
    public static String toHex(CCSID ccsid, int length, String value) {
        byte[] bytes = toBytes(ccsid, length, value);
        String hex = ConvertUtil.toHex(bytes);
        return hex;
    }

    /**
     * Java String to IBM Hex String
     * <p>
     * 這個方法會自動判斷字串中是否含有中文, 如果有則以中文方式轉換, 並使用OE和OF前後包起來
     *
     * @param length
     * @param value
     * @return
     */
    public static String toHex(int length, String value) {
        return toHex(length, value, false);
    }

    /**
     * Java String to IBM Hex String
     * <p>
     * 這個方法會自動判斷字串中是否含有中文, 如果有則以中文方式轉換, 並使用OE和OF前後包起來
     *
     * @param length
     * @param value
     * @return
     */
    public static String toHex(int length, String value, boolean leftPadding) {
        return toHex(length, value, ENCIRCLE_FOR_CHINESE, leftPadding);
    }

    /**
     * Java String to IBM Hex String
     * <p>
     * 這個方法會自動判斷字串中是否含有中文, 如果有則以中文方式轉換, 並使用encircle[0]和encircle[1]前後包起來
     *
     * @param length
     * @param value
     * @param encircle
     * @param leftPadding
     * @return
     */
    public static String toHex(int length, String value, String[] encircle, boolean leftPadding) {
        int counted = 0;
        StringBuilder sb = new StringBuilder();
        List<String> list = StringUtil.splitByChinese(value);
        for (String str : list) {
            if (StringUtil.isChinese(str)) {
                byte[] bytes = toBytes(CCSID.Traditional_Chinese, str.length() * 2, str);
                counted += bytes.length;
                String hex = ConvertUtil.toHex(bytes);
                if (ArrayUtils.isNotEmpty(encircle) && encircle.length >= 2) {
                    hex = StringUtils.join(encircle[0], hex, encircle[1]);
                    counted += 2;
                }
                sb.append(hex);
            } else {
                sb.append(toHex(CCSID.English, str.length(), str));
                counted += str.length();
            }
        }
        // 位數不夠, 則右邊補齊空白
        if (counted < length) {
            String padding = StringUtils.repeat(StringUtils.SPACE, length - counted);
            if (leftPadding) {
                sb.insert(0, toHex(CCSID.English, padding.length(), padding));
            } else {
                sb.append(toHex(CCSID.English, padding.length(), padding));
            }
        }
        return sb.toString();
    }

    /**
     * IBM Hex String to Java Hex String
     *
     * @param ccsid
     * @param hex
     * @param charsets
     * @return
     */
    public static String iHexToJHex(CCSID ccsid, String hex, Charset... charsets) {
        String value = fromHex(ccsid, hex);
        byte[] bytes = null;
        if (ArrayUtils.isEmpty(charsets) || charsets[0] == null) {
            bytes = value.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = value.getBytes(charsets[0]);
        }
        return StringUtil.toHex(bytes);
    }

    /**
     * Java Hex String to IBM Hex String
     *
     * @param ccsid
     * @param length
     * @param hex
     * @param charsets
     * @return
     */
    public static String jHexToIHex(CCSID ccsid, int length, String hex, Charset... charsets) {
        byte[] bytes = ConvertUtil.hexToBytes(hex);
        String str = null;
        if (ArrayUtils.isEmpty(charsets) || charsets[0] == null) {
            str = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
        } else {
            str = ConvertUtil.toString(bytes, charsets[0]);
        }
        return toHex(ccsid, length, str);
    }

    /**
     * IBM Whole Hex String to Java String With Traditional Chinese for Non-English and Encircle with "OE" "OF"
     *
     * @param hex
     * @return
     */
    public static String fromWholeHex(String hex) {
        return fromWholeHex(hex, CCSID.Traditional_Chinese, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java String Encircle with "OE" "OF"
     *
     * @param hex
     * @param nonEnglish
     * @return
     */
    public static String fromWholeHex(String hex, CCSID nonEnglish) {
        return fromWholeHex(hex, nonEnglish, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java String
     *
     * @param hex        IBM HEX String
     * @param nonEnglish 非英文部分需要轉換為那種語言
     * @param encircle   非英文部分在IBM HEX String中前後包裹的字符
     * @return
     */
    public static String fromWholeHex(String hex, CCSID nonEnglish, String[] encircle) {
        StringBuilder sb = new StringBuilder();
        // 每段非英數開始的位置, 也就是每段encircle[0]所在的位置
        int begin = hex.indexOf(encircle[0]);
        // 注意begin必須是偶數, 如果是奇數, 表示取得不對
        while (begin != -1 && begin % 2 != 0) {
            begin = hex.indexOf(encircle[0], begin + 1);
        }
        // 每段非英數終止的位置, 也就是每段encircle[1]所在的位置
        int end = -1;
        // 找完encircle最後的位置
        int last = 0;
        while (begin != -1) {
            end = hex.indexOf(encircle[1], begin);
            // 注意end必須是偶數, 如果是奇數, 表示取得不對
            while (end != -1 && end % 2 != 0) {
                end = hex.indexOf(encircle[1], end + 1);
            }
            if (end != -1) {
                // 先轉last和begin之間的英數的部分
                if (begin != 0) sb.append(fromHex(CCSID.English, hex.substring(last, begin)));
                // 再轉begin和end之間非英數的部分
                sb.append(fromHex(nonEnglish, hex.substring(begin + encircle[0].length(), end)));
                // 取出下一個begin
                begin = hex.indexOf(encircle[0], end + encircle[1].length());
                // 注意begin必須是偶數, 如果是奇數, 表示取得不對
                while (begin != -1 && begin % 2 != 0) {
                    begin = hex.indexOf(encircle[0], begin + 1);
                }
                // 確定last
                last = end + encircle[1].length();
            } else {
                break;
            }
        }
        // 如last為0, 表示沒有非英數的部分, 則直接全部轉
        if (last == 0) {
            return fromHex(CCSID.English, hex);
        }
        // 最後將剩餘的英數部分轉換
        else if (last != hex.length()) {
            sb.append(fromHex(CCSID.English, hex.substring(last)));
        }
        return sb.toString();
    }

    /**
     * IBM Whole Hex String to Java Hex String With Traditional Chinese for Non-English and Encircle with "OE" "OF"
     *
     * @param hex
     * @return
     */
    public static String iWholeHexToJHex(String hex) {
        return iWholeHexToJHex(hex, CCSID.Traditional_Chinese, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java Hex String Encircle with "OE" "OF"
     *
     * @param hex
     * @param nonEnglish
     * @return
     */
    public static String iWholeHexToJHex(String hex, CCSID nonEnglish) {
        return iWholeHexToJHex(hex, nonEnglish, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java Hex String
     *
     * @param hex
     * @param nonEnglish
     * @param encircle
     * @param charsets
     * @return
     */
    public static String iWholeHexToJHex(String hex, CCSID nonEnglish, String[] encircle, Charset... charsets) {
        String value = fromWholeHex(hex, nonEnglish, encircle);
        byte[] bytes = null;
        if (ArrayUtils.isEmpty(charsets) || charsets[0] == null) {
            bytes = value.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = value.getBytes(charsets[0]);
        }
        return StringUtil.toHex(bytes);
    }

    /**
     * 判斷EDBIC字串中含有 0E開頭0F結尾的字串 如有找到回傳中文 如找不到EDBIC轉後返回
     *
     * @param str 要轉的參數(EDBIC)
     * @return
     */
    public static String changeChinese(String str) {
        String result = "";
        try {
            String regexStart = ENCIRCLE_FOR_CHINESE[0];
            int maxLength = str.length();
            int sindex = str.indexOf(regexStart);
            //判斷 0E 若從中間開始，則需為偶數的位址
            if (sindex == 0 || (sindex > 0 && (sindex % 2 == 0))) {
                int eIndex = -1;
                String _str = str;
                for (int i = 0; i < maxLength; i++) {
                    eIndex = getEndChineseKey(_str);
                    if (eIndex < 0) {
                        break;
                    }
                    if (eIndex % 2 == 0) {
                        break;
                    } else {
                        _str = str.substring(sindex + 2, eIndex);
                    }
                }
                if (eIndex > 0) {
                    _str = str.substring(sindex + 2, eIndex);
                    //判斷不為最開頭的處理
                    if (sindex > 0) {
                        result = result.concat(EbcdicConverter.fromHex(CCSID.English, str.substring(0, sindex)));
                    }
                    result = result.concat(EbcdicConverter.fromHex(CCSID.Traditional_Chinese, _str));
                    //判斷不是最尾端的處理
                    if (eIndex + 2 != str.length()) {
                        result = result.concat(EbcdicConverter.fromHex(CCSID.English, str.substring(eIndex + 2)));
                    }
                } else {
                    result = EbcdicConverter.fromHex(CCSID.English, str);
                }
            } else {
                result = EbcdicConverter.fromHex(CCSID.English, str);
            }
        } catch (Exception e) {
            result = EbcdicConverter.fromHex(CCSID.English, str);
            logger.error(e, e.getMessage());
        }
        return result;
    }

    /**
     * 回傳字串中是否有0F存在，沒有就回傳-1
     *
     * @param str
     * @return
     */
    private static int getEndChineseKey(String str) {
        int result = -1;
        String regexEnd = ENCIRCLE_FOR_CHINESE[1];
        int eIndex = str.lastIndexOf(regexEnd);
        if (eIndex > 0) {
            return eIndex;
        }
        return result;
    }

    /**
     * 英數字串ASCII轉EBCDIC
     *
     * @param ascii
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toHex(String ascii) throws UnsupportedEncodingException {
        return StringUtil.toHex(ascii.getBytes(ENGLISH));
    }

    /**
     * 英數字節ASCII轉EBCDIC
     *
     * @param ascii
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] toBytes(byte[] ascii) throws UnsupportedEncodingException {
        return ConvertUtil.toBytes(ascii, StandardCharsets.US_ASCII.name(), ENGLISH);
    }

    /**
     * 英數繁中字串ASCII轉EBCDIC, 注意這個方法轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ascii
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toTraditionalChineseHex(String ascii) throws UnsupportedEncodingException {
        return StringUtil.toHex(ascii.getBytes(TRADITIONAL_CHINESE));
    }

    /**
     * 英數繁中字節ASCII轉EBCDIC, 注意這個方法轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ascii
     * @return
     */
    public static byte[] toTraditionalChineseBytes(byte[] ascii) {
        return ConvertUtil.toBytes(ascii, StandardCharsets.US_ASCII.name(), TRADITIONAL_CHINESE);
    }

    /**
     * 英數字串EBCDIC轉ASCII
     *
     * @param ebcdic
     * @return
     * @throws DecoderException
     */
    public static String fromHex(String ebcdic) throws DecoderException {
        return StringUtil.fromHex(ebcdic, Charset.forName(ENGLISH), true);
    }

    /**
     * 英數字節EBCDIC轉ASCII
     *
     * @param ebcdic
     * @return
     */
    public static byte[] fromBytes(byte[] ebcdic) {
        return ConvertUtil.toBytes(ebcdic, ENGLISH, StandardCharsets.US_ASCII.name());
    }

    /**
     * 英數繁中字串EBCDIC轉ASCII, 注意這個方法待轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ebcdic
     * @return
     * @throws DecoderException
     */
    public static String fromTraditionalChineseHex(String ebcdic) throws DecoderException {
        return StringUtil.fromHex(ebcdic, Charset.forName(TRADITIONAL_CHINESE), true);
    }

    /**
     * 英數繁中字節EBCDIC轉ASCII, 注意這個方法待轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ebcdic
     * @return
     */
    public static byte[] fromTraditionalChineseBytes(byte[] ebcdic) {
        return ConvertUtil.toBytes(ebcdic, TRADITIONAL_CHINESE, StandardCharsets.US_ASCII.name());
    }
}
