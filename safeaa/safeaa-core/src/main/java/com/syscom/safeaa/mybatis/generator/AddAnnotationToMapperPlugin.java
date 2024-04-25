package com.syscom.safeaa.mybatis.generator;

import com.syscom.safeaa.log.LogHelper;
import com.syscom.safeaa.mybatis.vo.MybatisConstant;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 非繼承類的Mapper增加@Resource注入
 *
 * @author Richard
 */
public class AddAnnotationToMapperPlugin extends PluginAdapter {
    private static final LogHelper logger = new LogHelper();
    private static final List<String> extModelNameList = new ArrayList<>();
    private static final Map<String, String> specialMap = new HashMap<String, String>() {{
        put("SyscomaudittrailSafeaaExtMapper.class", "SyscomaudittrailExtMapper.class");
    }};

    static {
        try {
            org.springframework.core.io.Resource[] resources =
                    new PathMatchingResourcePatternResolver().getResources(StringUtils.join(
                            ResourceUtils.CLASSPATH_URL_PREFIX, "com/syscom/safeaa/mybatis/extmapper/*ExtMapper.class"));
            for (org.springframework.core.io.Resource resource : resources) {
                String fileName = resource.getFilename();
                if (specialMap.containsKey(fileName)) {
                    fileName = specialMap.get(fileName);
                }
                fileName = fileName.substring(0, fileName.length() - "ExtMapper.class".length());
                extModelNameList.add(fileName);
            }
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage());
        } catch (IOException e) {
            logger.exceptionMsg(e, e.getMessage());
        }
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        String modelName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        modelName = modelName.equals(MybatisConstant.ENTITY_NAME_FEPTXN01) ? MybatisConstant.REPLACE_ENTITY_NAME_FOR_FEPTXN01 : modelName;
        if (!CollectionUtils.contains(extModelNameList.iterator(), modelName)) {
            interfaze.getAnnotations().add(StringUtils.join("@", Resource.class.getSimpleName()));
            interfaze.getImportedTypes().add(new FullyQualifiedJavaType(Resource.class.getName()));
        }
        return true;
    }

}
