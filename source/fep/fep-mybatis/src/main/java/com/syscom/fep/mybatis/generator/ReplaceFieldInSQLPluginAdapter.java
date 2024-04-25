package com.syscom.fep.mybatis.generator;

import com.syscom.fep.mybatis.vo.MybatisConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定義替換SQL中的field
 */
public class ReplaceFieldInSQLPluginAdapter extends PluginAdapter {
    private final Map<String, Map<String, String>> replacementMap = new HashMap<String, Map<String, String>>() {
        {
            // FEPTXN
            put(MybatisConstant.TABLE_NAME_FEPTXN, new HashMap<String, String>() {
                {
                    put("#{feptxnIcmark,jdbcType=CHAR}", "SUBSTR(#{feptxnIcmark,jdbcType=CHAR}, 1, 30)");
                    put("#{feptxnTrk2,jdbcType=VARCHAR}", "SUBSTR(#{feptxnTrk2,jdbcType=VARCHAR}, 1, 50)");
                    put("#{feptxnOrderNo,jdbcType=VARCHAR}", "SUBSTR(#{feptxnOrderNo,jdbcType=VARCHAR}, 1, 16)");
                }
            });
        }
    };

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
        Map<String, String> fieldMap = replacementMap.get(tableName);
        // 直接skip不需要處理的Table
        if (fieldMap == null) {
            return true;
        }
        List<VisitableElement> list = new ArrayList<>();
        List<VisitableElement> elementList = element.getElements();
        if (CollectionUtils.isNotEmpty(elementList)) {
            for (VisitableElement child : elementList) {
                if (child instanceof XmlElement) {
                    sqlMapInsertElementGenerated((XmlElement) child, introspectedTable);
                } else if (child instanceof TextElement) {
                    TextElement textElement = (TextElement) child;
                    String content = textElement.getContent();
                    for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                        if (content.contains(entry.getKey())) {
                            child = new TextElement(StringUtils.replace(content, entry.getKey(), entry.getValue()));
                        }
                    }
                }
                list.add(child);
            }
            element.getElements().clear();
            element.getElements().addAll(list);
        }
        return true;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapInsertElementGenerated(element, introspectedTable);
    }
}
