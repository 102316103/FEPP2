package com.syscom.safeaa.mybatis.vo;

import java.util.Date;

public class SyscomUserQueryVo {

	private Integer userid;
	private String logonid;
	private String username;
	private String employeeid;
	private String idno;
	private String birthday;
	private String effectdate;
	private String expireddate;
	private String emailaddress;
	private Integer updateuserid;
	private Date updatetime;
	private Integer empid;
	private String manager;
	private String fepUserBrno;
	private String fepUserBrname;
	private String fepUserBrnoSt;
	private String fepUserJob;
	private String fepUserTlrno;

	private Date logontime;
	private String logonip;
	private Short islogon;
	private Short logonchangesscode;
	private Date lastsscodechangetime;
	private Integer sscodeerrorcount;
	private Date lastlogontime;
	private String lastlogonip;
	private Date lastlogofftime;
	private String sscode;
	private Integer sscodeformat;
	private String sscodesalt;
	private String pkiserialno;
	private String sscodehintquestion;
	private String sscodehintanswer;
	private Integer failedpswansattemptcount;
	private Short islockout;
	private Date lastlockouttime;
	private Date failedpswattempttime;
	private Date failedpswansattempttime;
	private Date sscodeeffectdate;
	private String apdefine1;
	private String apdefine2;

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

	public String getEmployeeid() {
		return employeeid;
	}

	public void setEmployeeid(String employeeid) {
		this.employeeid = employeeid;
	}

	public String getIdno() {
		return idno;
	}

	public void setIdno(String idno) {
		this.idno = idno;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getEffectdate() {
		return effectdate;
	}

	public void setEffectdate(String effectdate) {
		this.effectdate = effectdate;
	}

	public String getExpireddate() {
		return expireddate;
	}

	public void setExpireddate(String expireddate) {
		this.expireddate = expireddate;
	}

	public String getEmailaddress() {
		return emailaddress;
	}

	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}

	public Integer getUpdateuserid() {
		return updateuserid;
	}

	public void setUpdateuserid(Integer updateuserid) {
		this.updateuserid = updateuserid;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public Integer getEmpid() {
		return empid;
	}

	public void setEmpid(Integer empid) {
		this.empid = empid;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getFepUserBrno() {
		return fepUserBrno;
	}

	public void setFepUserBrno(String fepUserBrno) {
		this.fepUserBrno = fepUserBrno;
	}

	public String getFepUserBrname() {
		return fepUserBrname;
	}

	public void setFepUserBrname(String fepUserBrname) {
		this.fepUserBrname = fepUserBrname;
	}

	public String getFepUserBrnoSt() {
		return fepUserBrnoSt;
	}

	public void setFepUserBrnoSt(String fepUserBrnoSt) {
		this.fepUserBrnoSt = fepUserBrnoSt;
	}

	public String getFepUserJob() {
		return fepUserJob;
	}

	public void setFepUserJob(String fepUserJob) {
		this.fepUserJob = fepUserJob;
	}

	public String getFepUserTlrno() {
		return fepUserTlrno;
	}

	public void setFepUserTlrno(String fepUserTlrno) {
		this.fepUserTlrno = fepUserTlrno;
	}

	public Date getLogontime() {
		return logontime;
	}

	public void setLogontime(Date logontime) {
		this.logontime = logontime;
	}

	public String getLogonip() {
		return logonip;
	}

	public void setLogonip(String logonip) {
		this.logonip = logonip;
	}

	public Short getIslogon() {
		return islogon;
	}

	public void setIslogon(Short islogon) {
		this.islogon = islogon;
	}

	public Short getLogonchangesscode() {
		return logonchangesscode;
	}

	public void setLogonchangesscode(Short logonchangesscode) {
		this.logonchangesscode = logonchangesscode;
	}

	public Date getLastsscodechangetime() {
		return lastsscodechangetime;
	}

	public void setLastsscodechangetime(Date lastsscodechangetime) {
		this.lastsscodechangetime = lastsscodechangetime;
	}

	public Integer getSscodeerrorcount() {
		return sscodeerrorcount;
	}

	public void setSscodeerrorcount(Integer sscodeerrorcount) {
		this.sscodeerrorcount = sscodeerrorcount;
	}

	public Date getLastlogontime() {
		return lastlogontime;
	}

	public void setLastlogontime(Date lastlogontime) {
		this.lastlogontime = lastlogontime;
	}

	public String getLastlogonip() {
		return lastlogonip;
	}

	public void setLastlogonip(String lastlogonip) {
		this.lastlogonip = lastlogonip;
	}

	public Date getLastlogofftime() {
		return lastlogofftime;
	}

	public void setLastlogofftime(Date lastlogofftime) {
		this.lastlogofftime = lastlogofftime;
	}

	public String getSscode() {
		return sscode;
	}

	public void setSscode(String sscode) {
		this.sscode = sscode;
	}

	public Integer getSscodeformat() {
		return sscodeformat;
	}

	public void setSscodeformat(Integer sscodeformat) {
		this.sscodeformat = sscodeformat;
	}

	public String getSscodesalt() {
		return sscodesalt;
	}

	public void setSscodesalt(String sscodesalt) {
		this.sscodesalt = sscodesalt;
	}

	public String getPkiserialno() {
		return pkiserialno;
	}

	public void setPkiserialno(String pkiserialno) {
		this.pkiserialno = pkiserialno;
	}

	public String getSscodehintquestion() {
		return sscodehintquestion;
	}

	public void setSscodehintquestion(String sscodehintquestion) {
		this.sscodehintquestion = sscodehintquestion;
	}

	public String getSscodehintanswer() {
		return sscodehintanswer;
	}

	public void setSscodehintanswer(String sscodehintanswer) {
		this.sscodehintanswer = sscodehintanswer;
	}

	public Integer getFailedpswansattemptcount() {
		return failedpswansattemptcount;
	}

	public void setFailedpswansattemptcount(Integer failedpswansattemptcount) {
		this.failedpswansattemptcount = failedpswansattemptcount;
	}

	public Short getIslockout() {
		return islockout;
	}

	public void setIslockout(Short islockout) {
		this.islockout = islockout;
	}

	public Date getLastlockouttime() {
		return lastlockouttime;
	}

	public void setLastlockouttime(Date lastlockouttime) {
		this.lastlockouttime = lastlockouttime;
	}

	public Date getFailedpswattempttime() {
		return failedpswattempttime;
	}

	public void setFailedpswattempttime(Date failedpswattempttime) {
		this.failedpswattempttime = failedpswattempttime;
	}

	public Date getFailedpswansattempttime() {
		return failedpswansattempttime;
	}

	public void setFailedpswansattempttime(Date failedpswansattempttime) {
		this.failedpswansattempttime = failedpswansattempttime;
	}

	public Date getSscodeeffectdate() {
		return sscodeeffectdate;
	}

	public void setSscodeeffectdate(Date sscodeeffectdate) {
		this.sscodeeffectdate = sscodeeffectdate;
	}

	public String getApdefine1() {
		return apdefine1;
	}

	public void setApdefine1(String apdefine1) {
		this.apdefine1 = apdefine1;
	}

	public String getApdefine2() {
		return apdefine2;
	}

	public void setApdefine2(String apdefine2) {
		this.apdefine2 = apdefine2;
	}

}
