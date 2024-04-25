package com.syscom.safeaa.mybatis.vo;

public enum MybatisParam {
	UPDATUSER("updatUser"),
	LOGAUDITTRAIL("logAuditTrail");

	private String name;

	private MybatisParam(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
