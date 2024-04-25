package com.syscom.safeaa.mybatis.vo;

import java.io.Serializable;

public class SyscomGroupVo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userId;
	private String logonId;
	private String userName;
	private String roleId;
	private String roleNo;
	private String roleName;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLogonId() {
		return logonId;
	}

	public void setLogonId(String logonId) {
		this.logonId = logonId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleNo() {
		return roleNo;
	}

	public void setRoleNo(String roleNo) {
		this.roleNo = roleNo;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@Override
	public String toString() {
		return "SyscomGroupVo [userId=" + userId + ", logonId=" + logonId + ", userName=" + userName + ", roleId="
				+ roleId + ", roleNo=" + roleNo + ", roleName=" + roleName + "]";
	}

}
