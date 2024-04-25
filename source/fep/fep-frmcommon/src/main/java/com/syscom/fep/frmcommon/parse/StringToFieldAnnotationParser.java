package com.syscom.fep.frmcommon.parse;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.util.ReflectionUtils;

import java.math.BigDecimal;

public class StringToFieldAnnotationParser<T> implements Parser<T, String> {
    private LogHelper logger = new LogHelper();

    private final Class<T> genericType;

    public StringToFieldAnnotationParser(Class<T> genericType) {
        this.genericType = genericType;
    }

    @Override
    public T readIn(String in) throws Exception {
        Class<T> cls = this.genericType;
        @SuppressWarnings("deprecation")
        T entity = cls.newInstance();
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        int offset = 0;
        for (java.lang.reflect.Field field : fields) {
            Field annotation = field.getAnnotation(Field.class);
            if (annotation == null) {
                continue;
            }
            int length = annotation.length() * 2;
            if (length > 0) {
                if (offset + length <= in.length()) {
                    String substr = in.substring(offset, offset + length);
                    if (annotation.trim()) {
                        substr = substr.trim();
                    }
                    if (BigDecimal.class.isAssignableFrom(field.getType())) {
                        BigDecimal bigstr = new BigDecimal(EbcdicConverter.fromHex(CCSID.English, substr));
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, entity, bigstr);
                    } else {
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, entity, substr);
                    }
                    logger.debug(
                            "set hex value = [", substr, "]",
                            // (annotation.toAscii() ? StringUtils.join("(ascii value = [", StringUtil.fromHex(substr), "])") : StringUtils.EMPTY),
                            " at offset = [", offset, "] with length = [", length, "] for field = [", field.getName(), "] of [", this.genericType.getName(), "]");
                } else {
                    logger.warn("ignore to set string value at offset = [", offset, "] with length = [", length, "] for field = [", field.getName(), "] of [", this.genericType.getName(), "]");
                    break;
                }
            }
            offset += length;
        }
        return entity;
    }

    @Override
    public String writeOut(T out) throws Exception {
        return null;
    }
}
