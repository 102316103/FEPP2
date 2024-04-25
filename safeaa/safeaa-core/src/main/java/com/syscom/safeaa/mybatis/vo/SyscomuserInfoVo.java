package com.syscom.safeaa.mybatis.vo;

import java.io.Serializable;

public class SyscomuserInfoVo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String idno;
	
	private Integer userid;
	
	private String logonid;
	
	private Integer empid;
	
	private String employeeid;
	
	private String username;
	
	private String emailaddress;
	
	private String birthday;
	
	private String sscode;
	
	private String sscodesalt;
	
	private Short islogon;
	
	private Short islockout;
	
	private Integer sscodeerrorcount;
	
	private Integer sscodeformat;
	
	private Integer failedpswansattemptcount;

	public String getIdno() {
		return idno;
	}

	public void setIdno(String idno) {
		this.idno = idno;
	}

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

	public Integer getEmpid() {
		return empid;
	}

	public void setEmpid(Integer empid) {
		this.empid = empid;
	}

	public String getEmployeeid() {
		return employeeid;
	}

	public void setEmployeeid(String employeeid) {
		this.employeeid = employeeid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmailaddress() {
		return emailaddress;
	}

	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getSscode() {
		return sscode;
	}

	public void setSscode(String sscode) {
		this.sscode = sscode;
	}

	public String getSscodesalt() {
		return sscodesalt;
	}

	public void setSscodesalt(String sscodesalt) {
		this.sscodesalt = sscodesalt;
	}

	public Short getIslogon() {
		return islogon;
	}

	public void setIslogon(Short islogon) {
		this.islogon = islogon;
	}

	public Short getIslockout() {
		return islockout;
	}

	public void setIslockout(Short islockout) {
		this.islockout = islockout;
	}

	public Integer getSscodeerrorcount() {
		return sscodeerrorcount;
	}

	public void setSscodeerrorcount(Integer sscodeerrorcount) {
		this.sscodeerrorcount = sscodeerrorcount;
	}
	
	public Integer getSscodeformat() {
		return sscodeformat;
	}

	public void setSscodeformat(Integer sscodeformat) {
		this.sscodeformat = sscodeformat;
	}

	public Integer getFailedpswansattemptcount() {
		return failedpswansattemptcount;
	}

	public void setFailedpswansattemptcount(Integer failedpswansattemptcount) {
		this.failedpswansattemptcount = failedpswansattemptcount;
	}

}
