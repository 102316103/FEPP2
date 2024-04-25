package com.syscom.safeaa.mybatis.vo;

public interface MybatisConstant {
	public static final String TABLE_NAME_BIN = "BIN";
	public static final String TABLE_NAME_FEPTXN01 = "FEPTXN01";
	
	public static final String ENTITY_PACKAGE_NAME = "com.syscom.safeaa.mybatis.model";
	public static final String ENTITY_NAME_FEPTXN01 = "Feptxn01";
	
	public static final String REPLACE_TABLE_NAME_FOR_FEPTXN01 = "FEPTXN${tableNameSuffix}";
	public static final String REPLACE_ENTITY_NAME_FOR_FEPTXN01 = "Feptxn";
	
	public static final String PARAMETER_NAME_TABLENAMESUFFIX = "tableNameSuffix";
}
