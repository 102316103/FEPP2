package com.syscom.fep.mybatis.util;

import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;
import java.util.List;

public class DB2Util {

    private DB2Util() {
    }

    public static String getClobValue(Object value, String... defaultOfNull) throws Exception {
        if (value != null) {
            Reader reader = ReflectUtil.envokeMethod(value, "w", null);
            if (reader != null) {
                List<String> lines = IOUtils.readLines(reader);
                if (CollectionUtils.isNotEmpty(lines)) {
                    return StringUtils.join(lines, "\r\n");
                }
            }
        }
        return ArrayUtils.isNotEmpty(defaultOfNull) ? defaultOfNull[0] : null;
    }

    public static String getClobValues(Object value, String... defaultOfNull) throws Exception {
        if (value != null && value instanceof Clob) {
            Clob clob = (Clob) value;
            return readClobData(clob);
        }
        return defaultOfNull.length > 0 ? defaultOfNull[0] : null;
    }

    private static String readClobData(Clob clob) throws Exception {
        StringBuilder data = new StringBuilder();
        try (Reader reader = clob.getCharacterStream();
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                data.append(line).append("\r\n");
            }
        }
        // Remove the last newline character
        if (data.length() > 0) {
            data.setLength(data.length() - 2);
        }
        return data.toString();
    }
}
