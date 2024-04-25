package com.syscom.safeaa.mybatis.vo;

import java.io.Serializable;

public class SyscomresourceInfoVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer userid;
	private String logonid;
	private String username; 
	private Integer roleid;
	private String rolename;
	private Integer resourcepid;
	private Integer resourceid;
	private String resourcename;
	private String resourceurl;
	private String resourceno;
	
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public String getLogonid() {
		return logonid;
	}
	public void setLogonid(String logonid) {
		this.logonid = logonid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Integer getRoleid() {
		return roleid;
	}
	public void setRoleid(Integer roleid) {
		this.roleid = roleid;
	}
	public String getRolename() {
		return rolename;
	}
	public void setRolename(String rolename) {
		this.rolename = rolename;
	}
	public Integer getResourcepid() {
		return resourcepid;
	}
	public void setResourcepid(Integer resourcepid) {
		this.resourcepid = resourcepid;
	}
	public Integer getResourceid() {
		return resourceid;
	}
	public void setResourceid(Integer resourceid) {
		this.resourceid = resourceid;
	}
	public String getResourcename() {
		return resourcename;
	}
	public void setResourcename(String resourcename) {
		this.resourcename = resourcename;
	}
	public String getResourceurl() {
		return resourceurl;
	}
	public void setResourceurl(String resourceurl) {
		this.resourceurl = resourceurl;
	}
	public String getResourceno() {
		return resourceno;
	}
	public void setResourceno(String resourceno) {
		this.resourceno = resourceno;
	}
	
	
}
