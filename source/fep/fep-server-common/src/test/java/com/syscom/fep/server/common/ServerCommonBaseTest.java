package com.syscom.fep.server.common;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SpringBootTest(classes = ServerCommonTestApplication.class)
public class ServerCommonBaseTest {
	protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();

	protected Object invokeMethod(Class<?> processorCls, String methodName, Class<?>[] argsCls, Object[] args) throws Exception {
		Method method = processorCls.getDeclaredMethod(methodName, argsCls);
		ReflectionUtils.makeAccessible(method);
		@SuppressWarnings("deprecation")
		Object processor = processorCls.newInstance();
		return method.invoke(processor, args);
	}

	protected Object invokeMethod(Object processor, String methodName, Class<?>[] argsCls, Object[] args) throws Exception {
		Method method = processor.getClass().getDeclaredMethod(methodName, argsCls);
		ReflectionUtils.makeAccessible(method);
		return method.invoke(processor, args);
	}

	/**
	 * used reflection to set value to private variable
	 * 
	 * @param processor
	 * @param fieldName
	 * @param args
	 * @throws Exception
	 */
	protected void invokeSetField(Object processor, String fieldName, Object args) throws Exception {
		Field field = processor.getClass().getDeclaredField(fieldName);
		ReflectionUtils.makeAccessible(field);
		field.set(processor, args);
	}
}
