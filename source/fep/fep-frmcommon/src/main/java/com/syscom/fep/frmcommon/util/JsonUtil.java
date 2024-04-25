package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonUtil {
    private JsonUtil() {}

    public static float getFloat(JSONObject jsonObject, String key, long... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getFloat(key);
        }
        return jsonObject.getFloat(key);
    }

    public static long getLong(JSONObject jsonObject, String key, long... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getLong(key);
        }
        return jsonObject.getLong(key);
    }

    public static double getDouble(JSONObject jsonObject, String key, double... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getDouble(key);
        }
        return jsonObject.getDouble(key);
    }

    public static int getInt(JSONObject jsonObject, String key, int... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getInt(key);
        }
        return jsonObject.getInt(key);
    }

    public static boolean getBoolean(JSONObject jsonObject, String key, boolean... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getBoolean(key);
        }
        return jsonObject.getBoolean(key);
    }

    public static String getString(JSONObject jsonObject, String key, String... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getString(key);
        }
        String value = jsonObject.getString(key);
        if (value != null) {
            return value;
        } else if (ArrayUtils.isNotEmpty(defaultValue)) {
            return defaultValue[0];
        }
        return null;
    }

    public static BigDecimal getBigDecimal(JSONObject jsonObject, String key, BigDecimal... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getBigDecimal(key);
        }
        BigDecimal value = jsonObject.getBigDecimal(key);
        if (value != null) {
            return value;
        } else if (ArrayUtils.isNotEmpty(defaultValue)) {
            return defaultValue[0];
        }
        return null;
    }

    public static BigInteger getBigInteger(JSONObject jsonObject, String key, BigInteger... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getBigInteger(key);
        }
        BigInteger value = jsonObject.getBigInteger(key);
        if (value != null) {
            return value;
        } else if (ArrayUtils.isNotEmpty(defaultValue)) {
            return defaultValue[0];
        }
        return null;
    }

    public static JSONObject getJSONObject(JSONObject jsonObject, String key, JSONObject... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getJSONObject(key);
        }
        JSONObject value = jsonObject.getJSONObject(key);
        if (value != null) {
            return value;
        } else if (ArrayUtils.isNotEmpty(defaultValue)) {
            return defaultValue[0];
        }
        return null;
    }

    public static JSONArray getJSONArray(JSONObject jsonObject, String key, JSONArray... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : jsonObject.getJSONArray(key);
        }
        JSONArray value = jsonObject.getJSONArray(key);
        if (value != null) {
            return value;
        } else if (ArrayUtils.isNotEmpty(defaultValue)) {
            return defaultValue[0];
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T get(JSONObject jsonObject, String key, T... defaultValue) {
        if (!jsonObject.has(key)) {
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : null;
        }
        Object value = jsonObject.get(key);
        if (value != null) {
            return (T) value;
        } else if (ArrayUtils.isNotEmpty(defaultValue)) {
            return defaultValue[0];
        }
        return (T) null;
    }
}
