package com.syscom.fep.frmcommon.ebcdic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.ArrayUtils;

import com.ibm.as400.access.AS400Bin2;
import com.ibm.as400.access.AS400Bin4;
import com.ibm.as400.access.AS400Float4;
import com.ibm.as400.access.AS400Float8;
import com.ibm.as400.access.AS400Text;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.ConvertUtil;

public class EbcdicConverter {

    private EbcdicConverter() {
    }

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
        byte[] bytes = ConvertUtil.hexToBytes(hex);
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
        return fromWholeHex(hex, CCSID.Traditional_Chinese, new String[]{"0E", "0F"});
    }

    /**
     * IBM Whole Hex String to Java String Encircle with "OE" "OF"
     *
     * @param hex
     * @param nonEnglish
     * @return
     */
    public static String fromWholeHex(String hex, CCSID nonEnglish) {
        return fromWholeHex(hex, nonEnglish, new String[]{"0E", "0F"});
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
        // 每段非英數終止的位置, 也就是每段encircle[1]所在的位置
        int end = -1;
        // 找完encircle最後的位置
        int last = 0;
        while (begin != -1) {
            end = hex.indexOf(encircle[1], begin);
            if (end != -1) {
                // 先轉last和begin之間的英數的部分
                if (begin != 0) sb.append(fromHex(CCSID.English, hex.substring(last, begin)));
                // 再轉begin和end之間非英數的部分
                sb.append(fromHex(nonEnglish, hex.substring(begin + encircle[0].length(), end)));
                // 取出下一個begin
                begin = hex.indexOf(encircle[0], end + encircle[1].length());
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
        return iWholeHexToJHex(hex, CCSID.Traditional_Chinese, new String[]{"0E", "0F"});
    }

    /**
     * IBM Whole Hex String to Java Hex String Encircle with "OE" "OF"
     *
     * @param hex
     * @param nonEnglish
     * @return
     */
    public static String iWholeHexToJHex(String hex, CCSID nonEnglish) {
        return iWholeHexToJHex(hex, nonEnglish, new String[]{"0E", "0F"});
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
}
