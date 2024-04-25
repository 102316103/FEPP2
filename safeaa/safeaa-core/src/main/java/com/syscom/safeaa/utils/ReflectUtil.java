package com.syscom.safeaa.utils;

import com.syscom.safeaa.log.LogHelper;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {
	private static final LogHelper logger = new LogHelper();

	private ReflectUtil() {}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object entity, String fieldName, T defaultValue) {
		try {
			if (entity != null) {
				Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
				if (field != null) {
					ReflectionUtils.makeAccessible(field);
					return (T) ReflectionUtils.getField(field, entity);
				}
			}
		} catch (Exception e) {
			logger.warn(e, e.getMessage());
		}
		return defaultValue;
	}

	/**
	 * @param entity
	 * @param field
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object entity, Field field, T defaultValue) {
		try {
			if (entity != null) {
				ReflectionUtils.makeAccessible(field);
				return (T) ReflectionUtils.getField(field, entity);
			}
		} catch (Exception e) {
			logger.warn(e, e.getMessage());
		}
		return defaultValue;
	}

	public static <T> T envokeMethod(Object entity, String methodName, T defaultValue) {
		return envokeMethod(entity, methodName, null, null, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public static <T> T envokeMethod(Object entity, String methodName, Class<?>[] argsCls, Object[] args, T defaultValue) {
		try {
			if (entity != null) {
				Method method = ReflectionUtils.findMethod(entity.getClass(), methodName, argsCls);
				if (method != null) {
					ReflectionUtils.makeAccessible(method);
					return (T) ReflectionUtils.invokeMethod(method, entity, args);
				}
			}
		} catch (Exception e) {
			logger.warn(e, e.getMessage());
		}
		return defaultValue;
	}

	/**
	 * Get Annotation
	 *
	 * @param clazz
	 * @param annotationClazz
	 * @param <A>
	 * @return
	 */
	public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClazz) {
		// 2023-06-14 Richard modified for 嘗試修正Improper Exception Handling
		A annotation = null;
		try {
			annotation = clazz.getAnnotation(annotationClazz);
		} catch (Throwable t) {
			logger.warn(t, t.getMessage());
		}
		return annotation;
	}
}