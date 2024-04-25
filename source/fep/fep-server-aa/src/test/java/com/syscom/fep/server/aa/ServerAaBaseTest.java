package com.syscom.fep.server.aa;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.boot.test.context.SpringBootTest;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.util.ReflectionUtils;

@SpringBootTest(classes = ServerAaTestApplication.class)
public class ServerAaBaseTest {
	protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();

	protected Object invokeMethod(Class<?> processorCls, String methodName, Class<?>[] argsCls, Object[] args) throws Exception {
		Method method = processorCls.getDeclaredMethod(methodName, argsCls);
		ReflectionUtils.makeAccessible(method);
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
