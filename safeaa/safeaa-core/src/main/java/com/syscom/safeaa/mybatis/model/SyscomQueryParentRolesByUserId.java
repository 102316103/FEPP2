package com.syscom.safeaa.mybatis.model;

import java.io.Serializable;

public class SyscomQueryParentRolesByUserId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer RoleId;

	private Integer IsGlobalScope;

	private String PolicyValue;

	private String PolicyNo;

	private String GetValueType;

	public SyscomQueryParentRolesByUserId(Integer RoleId, Integer IsGlobalScope, String PolicyValue, String PolicyNo, String GetValueType) {
		this.RoleId = RoleId;
		this.IsGlobalScope = IsGlobalScope;
		this.PolicyValue = PolicyValue;
		this.PolicyNo = PolicyNo;
		this.GetValueType = GetValueType;
	}

	public SyscomQueryParentRolesByUserId() {
		super();
	}

	public Integer getRoleId() {
		return RoleId;
	}

	public void setRoleId(Integer roleId) {
		RoleId = roleId;
	}

	public Integer getIsGlobalScope() {
		return IsGlobalScope;
	}

	public void setIsGlobalScope(Integer isGlobalScope) {
		IsGlobalScope = isGlobalScope;
	}

	public String getPolicyValue() {
		return PolicyValue;
	}

	public void setPolicyValue(String policyValue) {
		PolicyValue = policyValue;
	}

	public String getPolicyNo() {
		return PolicyNo;
	}

	public void setPolicyNo(String policyNo) {
		PolicyNo = policyNo;
	}

	public String getGetValueType() {
		return GetValueType;
	}

	public void setGetValueType(String getValueType) {
		GetValueType = getValueType;
	}
}