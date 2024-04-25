package com.syscom.fep.frmcommon.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MathUtil {

    private MathUtil() {
    }

    /**
     * 向下取整
     *
     * @param value
     * @param scale
     * @return
     */
    public static BigDecimal roundFloor(BigDecimal value, int scale) {
        if (value != null) {
            return setScale(value, scale, BigDecimal.ROUND_FLOOR);
        }
        return null;
    }

    /**
     * 向下取整
     *
     * @param value
     * @param scale
     * @return
     */
    public static BigDecimal roundFloor(double value, int scale) {
        return roundFloor(BigDecimal.valueOf(value), scale);
    }

    /**
     * 四捨五入小數點後保留到scale位
     *
     * @param value
     * @param scale
     * @return
     */
    public static BigDecimal roundUp(BigDecimal value, int scale) {
        if (value != null) {
            return setScale(value, scale, BigDecimal.ROUND_HALF_UP);
        }
        return null;
    }

    /**
     * 四捨五入小數點後保留到scale位
     *
     * @param value
     * @param scale
     * @return
     */
    public static BigDecimal roundUp(double value, int scale) {
        return roundUp(BigDecimal.valueOf(value), scale);
    }

    /**
     * 四捨五入並輸出為pattern字串形式
     *
     * @param value
     * @param pattern
     * @return
     */
    public static String toString(BigDecimal value, String pattern) {
        return toString(value, pattern, true);
    }

    /**
     * 四捨五入並輸出為pattern字串形式
     *
     * @param value
     * @param pattern
     * @param removeDot
     * @return
     */
    public static String toString(BigDecimal value, String pattern, boolean removeDot) {
        boolean invalid = false;
        int dotCount = StringUtils.countMatches(pattern, ".");
        int commaCount = StringUtils.countMatches(pattern, ",");
        if (dotCount > 1 || commaCount > 1) {
            invalid = true;
        } else {
            String tmp = pattern;
            if (dotCount == 1) {
                tmp = StringUtils.replace(tmp, ".", StringUtils.EMPTY);
            }
            if (commaCount == 1) {
                tmp = StringUtils.replace(tmp, ",", StringUtils.EMPTY);
            }
            for (int i = 0; i < tmp.length(); i++) {
                if (tmp.charAt(i) != '0' && tmp.charAt(i) != '#') {
                    invalid = true;
                    break;
                }
            }
        }
        if (invalid) {
            throw ExceptionUtil.createIllegalArgumentException("invalid pattern : [", pattern, "]");
        }
        String leftPattern = pattern;
        // 先確定小數部分的精度, 按照精度轉換出結果
        int scale = 0;
        int indexOf = StringUtils.indexOf(pattern, ".");
        if (indexOf != -1) {
            // 小數部分的Pattern
            String rightPattern = pattern.substring(indexOf + 1);
            int begin = rightPattern.indexOf("0");
            int end = rightPattern.lastIndexOf("0");
            // 如果小數部分的Pattern沒有0, 則保留原始的精度
            if (begin == -1 && end == -1) {
                scale = value.scale();
            }
            // 如果小數部分的Pattern只有一個0, 則0所在的位置就是精度
            else if (begin == end) {
                scale = begin + 1;
            }
            // 如果小數部分的Pattern有多個0, 則取最後一次出現0的位置+1, 就是精度
            else {
                scale = end + 1;
            }
            leftPattern = pattern.substring(0, indexOf);
        }
        // 按照精度先轉String, 小數部分就確定好了, 後面處理整數部分
        String result = toString(value, scale, removeDot);
        // 左邊整數部分, 右邊小數部分
        String leftResult, rightResult;
        if (scale > 0) {
            leftResult = StringUtils.left(result, result.length() - scale - (removeDot ? 0 : 1));
            rightResult = StringUtils.right(result, scale);
        } else {
            leftResult = result;
            rightResult = StringUtils.EMPTY;
        }
        // 先拿掉整數部分Pattern中的逗號
        if (commaCount > 0) {
            leftPattern = StringUtils.replace(leftPattern, ",", StringUtils.EMPTY);
        }
        int begin = leftPattern.indexOf("0");
        int size = 0;
        // 整數部分Pattern沒有0, 則整數部分保留原本的長度
        if (begin == -1) {
            size = leftResult.length();
        }
        // 整數部分Pattern只有一個0
        else {
            size = leftPattern.length() - begin;
        }
        // 這裡還要再判斷一次, 如果長度少於原本整數部分的長度, 那麼就取整數部分的長度
        if (size < leftResult.length()) {
            size = leftResult.length();
        }
        // 整數部分根據計算的size, 左邊補齊0
        leftResult = StringUtils.leftPad(leftResult, size, '0');
        if (commaCount > 0) {
            StringBuilder sb = new StringBuilder(leftResult).reverse();
            int count = 0, insert = 0;
            for (int i = 3; i < sb.length(); i += 3) {
                insert = i + count++;
                if (insert > sb.length()) {
                    break;
                }
                sb.insert(insert, ',');
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            leftResult = sb.reverse().toString();
        }
        return StringUtils.join(leftResult, scale > 0 && !removeDot && StringUtils.isNotBlank(rightResult) ? "." : StringUtils.EMPTY, rightResult);
    }

    /**
     * 四捨五入小數點後保留到scale位並輸出為字串形式
     *
     * @param value
     * @param scale
     * @return
     */
    public static String toString(BigDecimal value, int scale) {
        return toString(value, scale, true);
    }

    /**
     * 四捨五入小數點後保留到scale位並輸出為字串形式
     *
     * @param value
     * @param scale
     * @param removeDot
     * @return
     */
    public static String toString(BigDecimal value, int scale, boolean removeDot) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        String str = roundUp(value, scale).toString();
        return removeDot ? StringUtils.replace(str, ".", StringUtils.EMPTY) : str;
    }

    /**
     * 替換
     *
     * @param value
     * @param searchString
     * @param replacement
     * @return
     */
    public static String replace(BigDecimal value, String searchString, String replacement) {
        return StringUtils.replace(value.toString(), searchString, replacement);
    }

    /**
     * compare with int
     *
     * @param value
     * @param compareTo
     * @return
     */
    public static int compareTo(BigDecimal value, int compareTo) {
        if (value != null) {
            return value.compareTo(BigDecimal.valueOf(compareTo));
        } else {
            return -1;
        }
    }

    /**
     * compare with double
     *
     * @param value
     * @param compareTo
     * @return
     */
    public static int compareTo(BigDecimal value, double compareTo) {
        if (value != null) {
            return value.compareTo(BigDecimal.valueOf(compareTo));
        } else {
            return -1;
        }
    }

    /**
     * compare with String
     *
     * @param value
     * @param compareTo
     * @return
     */
    public static int compareTo(BigDecimal value, String compareTo) {
        if (value != null) {
            return value.compareTo(new BigDecimal(compareTo));
        } else {
            return -1;
        }
    }

    /**
     * setScale
     *
     * @param value
     * @param scale
     * @param roundingMode
     * @return
     */
    private static BigDecimal setScale(BigDecimal value, int scale, int roundingMode) {
        if (value != null) {
            return value.setScale(scale, roundingMode);
        }
        return null;
    }

    /**
     * 把一個數拆成2的冪次方個數
     *
     * @param number
     * @return
     */
    public static List<Integer> splitByPow2(int number) {
        List<Integer> list = new ArrayList<Integer>();
        if (number > 0) {
            String binaryStr = new StringBuilder(Integer.toBinaryString(number)).reverse().toString();
            char[] chars = binaryStr.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                int num = Integer.parseInt(String.valueOf(chars[i])) * (int) Math.pow(2, i);
                if (num > 0) {
                    list.add(num);
                }
            }
        }
        return list;
    }
}
