package com.syscom.safeaa.utils;

import com.github.rkpunjal.sqlsafe.SqlSafeUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

public class DbHelper {

    private DbHelper() {
    }

    /**
     * DB Entity中的Short類型轉Boolean
     *
     * @param value
     * @return
     */
    public static Boolean toBoolean(Short value, Boolean... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : Boolean.FALSE) : (value == 0 ? Boolean.FALSE : Boolean.TRUE);
    }

    /**
     * DB Entity中的Boolean類型轉Short
     *
     * @param value
     * @return
     */
    public static Short toShort(Boolean value, Short... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : (short) 0) : (value ? Short.valueOf((short) 1) : Short.valueOf((short) 0));
    }

    public static double toDouble(String value, double... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : 0) : Double.parseDouble(value);
    }

    public static int toInteger(String value, int... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : 0) : Integer.parseInt(value);
    }

    public static boolean toBoolean(String value, boolean... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : false) : PolyfillUtil.ctype(value);
    }

    public static BigDecimal toBigDecimal(String value, int... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? new BigDecimal(defaultOfNull[0]) : new BigDecimal(0)) : new BigDecimal(value);
    }

    public static BigDecimal toBigDecimal(String value, double... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? BigDecimal.valueOf(defaultOfNull[0]) : BigDecimal.valueOf(0d)) : new BigDecimal(value);
    }

    public static String toString(String value, String... defaultOfNull) {
        return value == null ? (ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : StringUtils.EMPTY) : value;
    }

    public static String avoidEmpty(String value, String... defaultOfNull) {
        return StringUtils.isBlank(value) ? (ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : null) : value;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getMapValue(Map<String, Object> map, String columnName, T... defs) {
        boolean isNumber = false;
        T def = null;
        if (ArrayUtils.isNotEmpty(defs)) {
            def = defs[0];
            if (def != null) {
                isNumber = def instanceof Number;
            }
        }
        if (MapUtils.isEmpty(map)) {
            return def;
        }
        Object field = map.get(columnName);
        if (field == null) {
            return def;
        } else {
            if (isNumber && StringUtils.isBlank(field.toString())) {
                return def;
            }
        }
        return (T) field;
    }

    /**
     * 判斷表達式是否含有SQL注入
     *
     * @param expression
     * @return
     */
    public static boolean isSqlInjectionSafe(String expression) {
        return SqlSafeUtil.isSqlInjectionSafe(expression);
    }

    /**
     * 判斷Order By語句是否合法
     *
     * @param orderBy
     * @return
     */
    public static boolean isSqlInOrderByValid(String orderBy) {
        return StringUtils.isNotBlank(orderBy) && isSqlInjectionSafe(orderBy);
    }
}
