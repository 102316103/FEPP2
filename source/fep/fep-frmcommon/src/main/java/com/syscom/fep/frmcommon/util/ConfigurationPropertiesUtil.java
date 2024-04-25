package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class ConfigurationPropertiesUtil {
    private ConfigurationPropertiesUtil() {}

    public static String info(Object configuration, String configurationName, String... excludeFields) {
        return info(configuration, configurationName, false, excludeFields);
    }

    public static String info(Object configuration, String configurationName, boolean includeSuperclass, String... excludeFields) {
        Class<?> clazz = configuration.getClass();
        String prefix = clazz.getSimpleName();
        // 檢查是不是SpringCGLIB動態代理類, 如果是的話, 需要取SuperClass
        if (prefix.contains("$$EnhancerBySpringCGLIB$$")) {
            clazz = clazz.getSuperclass();
            prefix = clazz.getSimpleName();
        }
        ConfigurationProperties configurationPropertiesAnnotation = clazz.getAnnotation(ConfigurationProperties.class);
        if (configurationPropertiesAnnotation != null) {
            prefix = configurationPropertiesAnnotation.prefix();
        }
        Field[] fields = null;
        if (includeSuperclass) {
            List<Field> list = ReflectUtil.getAllFields(configuration);
            if (list.isEmpty()) {
                fields = new Field[0];
            } else {
                fields = new Field[list.size()];
                list.toArray(fields);
            }
        } else {
            fields = clazz.getDeclaredFields();
        }
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append(configurationName).append(":\r\n");
            for (Field field : fields) {
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    continue;
                } else if (ArrayUtils.isNotEmpty(excludeFields) && ArrayUtils.contains(excludeFields, field.getName())) {
                    continue;
                } else if (field.getType().equals(LogHelper.class)) {
                    continue;
                }
                ReflectionUtils.makeAccessible(field);
                Object object = ReflectionUtils.getField(field, configuration);
                info(field, object, prefix == null ? StringUtils.EMPTY : StringUtils.join(prefix, ".", field.getName()), sb);
            }
            return sb.toString();
        }
        return StringUtils.join(configurationName, ":\r\n");
    }

    private static void info(Field field, Object object, String prefix, StringBuilder sb) {
        int repeat = 2;
        Value annotation = field.getAnnotation(Value.class);
        if (annotation != null) {
            sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                    .append(annotation.value(), annotation.value().indexOf("${") + 2, annotation.value().contains(":") ? annotation.value().indexOf(":") : annotation.value().indexOf("}"))
                    .append(" = ");
            if (object == null) {
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                        .append("null")
                        .append("\r\n");
            } else if (object.getClass().isArray()) {
                sb.append("[");
                for (int i = 0; i < Array.getLength(object); i++) {
                    sb.append(Array.get(object, i)).append(",");
                }
                if (sb.length() > 1) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("]");
            } else if (object instanceof List) {
                sb.append("[")
                        .append(StringUtils.join((List<?>) object, ','))
                        .append("]");
            } else {
                sb.append(object);
            }
            sb.append("\r\n");
        } else if (object == null) {
            sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                    .append(prefix)
                    .append(" = ")
                    .append("null")
                    .append("\r\n");
        } else {
            if (object.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(object); i++) {
                    info(field, Array.get(object, i), StringUtils.join(prefix, "[", i++, "]"), sb);
                }
            } else if ("java.lang".equals(object.getClass().getPackage().getName())
                    || object instanceof Map) {
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                        .append(prefix)
                        .append(" = ")
                        .append(object)
                        .append("\r\n");
            } else if (object instanceof List) {
                List<?> list = (List<?>) object;
                int i = 0;
                for (Object e : list) {
                    info(field, e, StringUtils.join(prefix, "[", i++, "]"), sb);
                }
            } else {
                Field[] fields = object.getClass().getDeclaredFields();
                // 有時候這樣會取不到, 則換另外的方式再嘗試取一次
                if (ArrayUtils.isEmpty(fields)) {
                    List<Field> list = ReflectUtil.getAllFields(object);
                    fields = list.toArray(new Field[0]);
                }
                if (ArrayUtils.isNotEmpty(fields)) {
                    for (Field f : fields) {
                        ReflectionUtils.makeAccessible(f);
                        info(f, ReflectionUtils.getField(f, object), StringUtils.join(prefix, ".", f.getName()), sb);
                    }
                }
            }
        }
    }
}
