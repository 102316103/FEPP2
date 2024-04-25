package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectUtil {
    private static final LogHelper logger = new LogHelper();

    private ReflectUtil() {
    }

    public static <T> T getFieldValue(Object entity, String fieldName, T defaultValue) {
        try {
            if (entity != null) {
                Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
                return getFieldValue(entity, field, defaultValue);
            }
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    public static <T> void setFieldValue(Object entity, String fieldName, T fieldValue) {
        try {
            if (entity != null) {
                Field field = ReflectionUtils.findField(entity.getClass(), fieldName);
                setFieldValue(entity, field, fieldValue);
            }
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }

    /**
     * @param entity
     * @param field
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object entity, Field field, T defaultValue) {
        try {
            if (entity != null && field != null) {
                ReflectionUtils.makeAccessible(field);
                return (T) ReflectionUtils.getField(field, entity);
            }
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    /**
     * @param entity
     * @param field
     */
    public static <T> void setFieldValue(Object entity, Field field, T fieldValue) {
        try {
            if (entity != null && field != null) {
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, entity, fieldValue);
            }
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
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
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    public static void envokeMethod(Object entity, String methodName) {
        envokeMethod(entity, methodName, null, null);
    }

    public static void envokeMethod(Object entity, String methodName, Class<?>[] argsCls, Object[] args) {
        try {
            if (entity != null) {
                Method method = ReflectionUtils.findMethod(entity.getClass(), methodName, argsCls);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    ReflectionUtils.invokeMethod(method, entity, args);
                }
            }
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }

    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, T defaultValue) {
        return envokeStaticMethod(clazz, methodName, null, null, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> T envokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] argsCls, Object[] args, T defaultValue) {
        try {
            if (clazz != null) {
                Method method = ReflectionUtils.findMethod(clazz, methodName, argsCls);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    return (T) ReflectionUtils.invokeMethod(method, null, args);
                }
            }
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
        return defaultValue;
    }

    public static void envokeStaticMethod(Class<?> clazz, String methodName) {
        envokeStaticMethod(clazz, methodName, null, null);
    }

    public static void envokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] argsCls, Object[] args) {
        try {
            if (clazz != null) {
                Method method = ReflectionUtils.findMethod(clazz, methodName, argsCls);
                if (method != null) {
                    ReflectionUtils.makeAccessible(method);
                    ReflectionUtils.invokeMethod(method, null, args);
                }
            }
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }

    public static List<Field> getAllFields(Object entity) {
        Class<?> clazz = entity.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 動態載入Jar檔中的某個Class程式
     *
     * @param jarPath
     * @param className
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T dynamicLoadClass(String jarPath, String className) throws Exception {
        // 先列出jar檔中所有的class
        Set<String> classNameSet = listClassname(jarPath);
        if (CollectionUtils.isEmpty(classNameSet)) {
            throw ExceptionUtil.createClassNotFoundException("Cannot find className = [", className, "], cause [", jarPath, "] is empty!!!");
        }
        logger.debug("try to dynamic load class [", className, "] in file [", jarPath, "]...");
        File file = new File(CleanPathUtil.cleanString(jarPath));
        if (!file.exists() || file.isDirectory()) {
            throw ExceptionUtil.createFileNotFoundException("Cannot find className = [", className, "], cause [", jarPath, "] is not exist, or it is directory!!!");
        }
        URLClassLoader classLoader = null;
        try {
            URL url = file.toURI().toURL();
            classLoader = new URLClassLoader(new URL[] {url}, ReflectUtil.class.getClassLoader());
            // 注意這裡要將jar檔中所有的class檔全部載入到ClassLoader中
            for (String clazzName : classNameSet) {
                if (clazzName.equals(className)) {
                    continue;
                }
                classLoader.loadClass(clazzName);
                logger.debug("success dynamic load class = [", clazzName, "] in file [", jarPath, "]");
            }
            Class<T> clazz = (Class<T>) classLoader.loadClass(className);
            logger.debug("success dynamic load class [", className, "] in file [", jarPath, "]");
            return (T) clazz.newInstance();
        } catch (Throwable e) {
            logger.error(e, "Cannot load [", className, "] in file [", jarPath, "]!!!");
            if (e instanceof Error) {
                throw ExceptionUtil.createException(e, e.getMessage());
            }
            throw e;
        } finally {
            IOUtils.closeQuietly(classLoader);
        }
    }

    /**
     * 列舉jar檔中所有的classname
     *
     * @param jarPath
     * @return
     * @throws Exception
     */
    public static Set<String> listClassname(String jarPath) throws Exception {
        return listClassname(jarPath, null);
    }

    /**
     * 依據predicateForClassname列舉jar檔中所有的classname
     *
     * @param jarPath
     * @param predicateForClassname
     * @return
     * @throws Exception
     */
    public static Set<String> listClassname(String jarPath, Predicate<String> predicateForClassname) throws Exception {
        File file = new File(CleanPathUtil.cleanString(jarPath));
        if (!file.exists() || file.isDirectory()) {
            throw ExceptionUtil.createFileNotFoundException("Cannot list className, cause [", jarPath, "] is not exist, or it is directory!!!");
        }
        logger.debug("try to list className in file [", jarPath, "]...");
        Set<String> classNames = new HashSet<>();
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry jarEntry = e.nextElement();
                if (jarEntry.getName().toLowerCase().endsWith(".class")) {
                    String className = StringUtils.replace(FilenameUtils.removeExtension(jarEntry.getName()), "/", ".");
                    if (predicateForClassname == null || predicateForClassname.test(className))
                        classNames.add(className);
                }
            }
            logger.debug("success list className in file [", jarPath, "]");
            return classNames;
        } catch (Throwable e) {
            logger.error(e, "Cannot list className in file [", jarPath, "]!!!");
            if (e instanceof Error) {
                throw ExceptionUtil.createException(e, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 獲取指定package下所有的類名
     *
     * @param loader
     * @param packagePattern
     * @return
     * @throws IOException
     */
    public static List<String> listClassname(ResourceLoader loader, String packagePattern) throws IOException {
        List<String> list = new ArrayList<>();
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(loader);
        MetadataReaderFactory factory = new CachingMetadataReaderFactory(loader);
        Resource[] resources = resolver.getResources(StringUtils.join("classpath*:", packagePattern));
        if (ArrayUtils.isNotEmpty(resources)) {
            for (Resource resource : resources) {
                MetadataReader reader = factory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                list.add(className);
            }
        }
        return list;
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
        A annotation = null;
        try {
            annotation = clazz.getAnnotation(annotationClazz);
        } catch (Throwable t) {
            logger.warn(t, t.getMessage());
        }
        return annotation;
    }
}