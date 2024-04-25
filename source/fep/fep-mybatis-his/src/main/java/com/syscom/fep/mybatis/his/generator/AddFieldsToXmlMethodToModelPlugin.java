package com.syscom.fep.mybatis.his.generator;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

public class AddFieldsToXmlMethodToModelPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		Method fieldsToXmlMethod = new Method("fieldsToXml");
		fieldsToXmlMethod.addJavaDocLine("/**");
		fieldsToXmlMethod.addJavaDocLine(" * This method was generated by MyBatis Generator.");
		fieldsToXmlMethod.addJavaDocLine(StringUtils.join(" * This method corresponds to the database table " , introspectedTable.getFullyQualifiedTable().getIntrospectedTableName()));
		fieldsToXmlMethod.addJavaDocLine(" * ");
		fieldsToXmlMethod.addJavaDocLine(" * @mbg.generated");
		fieldsToXmlMethod.addJavaDocLine(" */");
		fieldsToXmlMethod.addAnnotation(StringUtils.join("@", Override.class.getSimpleName()));
		fieldsToXmlMethod.setReturnType(new FullyQualifiedJavaType(String.class.getName()));
		fieldsToXmlMethod.setVisibility(JavaVisibility.PUBLIC);
		fieldsToXmlMethod.addBodyLine("StringBuilder sb = new StringBuilder();");
		for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
			fieldsToXmlMethod.addBodyLine(StringUtils.join(
					// "sb.append(\"<", column.getActualColumnName(), "><![CDATA[\").append(this.", column.getJavaProperty(), ").append(\"]]></", column.getActualColumnName(), ">\");"));
					"sb.append(\"<", column.getActualColumnName(), ">\").append(this.", column.getJavaProperty(), ").append(\"</", column.getActualColumnName(), ">\");"));
		}
		fieldsToXmlMethod.addBodyLine("return sb.toString();");
		topLevelClass.getMethods().add(fieldsToXmlMethod);
		return true;
	}
}
