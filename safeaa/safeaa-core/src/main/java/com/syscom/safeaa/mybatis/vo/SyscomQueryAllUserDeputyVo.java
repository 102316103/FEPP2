package com.syscom.safeaa.mybatis.vo;

import java.util.Date;

public class SyscomQueryAllUserDeputyVo {

	private Integer userId;
	private Integer logOnId;
	private String userName;
	private Integer deputyUserId;
	private Integer deputyLogOnId;
	private String deputyUserName;
	private Integer roleid;
	private String roleno;
	private String roletype;
	private String safedefinefunctionlist;
	private String userdefinefunctionlist;
	private Date effectdate;
	private Date expireddate;
	private String culture;
	private String resourcename;
	private String employeeid;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getLogOnId() {
		return logOnId;
	}

	public void setLogOnId(Integer logOnId) {
		this.logOnId = logOnId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getDeputyUserId() {
		return deputyUserId;
	}

	public void setDeputyUserId(Integer deputyUserId) {
		this.deputyUserId = deputyUserId;
	}

	public Integer getDeputyLogOnId() {
		return deputyLogOnId;
	}

	public void setDeputyLogOnId(Integer deputyLogOnId) {
		this.deputyLogOnId = deputyLogOnId;
	}

	public String getDeputyUserName() {
		return deputyUserName;
	}

	public void setDeputyUserName(String deputyUserName) {
		this.deputyUserName = deputyUserName;
	}

	public Integer getRoleid() {
		return roleid;
	}

	public void setRoleid(Integer roleid) {
		this.roleid = roleid;
	}

	public String getRoleno() {
		return roleno;
	}

	public void setRoleno(String roleno) {
		this.roleno = roleno;
	}

	public String getRoletype() {
		return roletype;
	}

	public void setRoletype(String roletype) {
		this.roletype = roletype;
	}

	public String getSafedefinefunctionlist() {
		return safedefinefunctionlist;
	}

	public void setSafedefinefunctionlist(String safedefinefunctionlist) {
		this.safedefinefunctionlist = safedefinefunctionlist;
	}

	public String getUserdefinefunctionlist() {
		return userdefinefunctionlist;
	}

	public void setUserdefinefunctionlist(String userdefinefunctionlist) {
		this.userdefinefunctionlist = userdefinefunctionlist;
	}

	public Date getEffectdate() {
		return effectdate;
	}

	public void setEffectdate(Date effectdate) {
		this.effectdate = effectdate;
	}

	public Date getExpireddate() {
		return expireddate;
	}

	public void setExpireddate(Date expireddate) {
		this.expireddate = expireddate;
	}

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	public String getResourcename() {
		return resourcename;
	}

	public void setResourcename(String resourcename) {
		this.resourcename = resourcename;
	}

	public String getEmployeeid() {
		return employeeid;
	}

	public void setEmployeeid(String employeeid) {
		this.employeeid = employeeid;
	}
}
