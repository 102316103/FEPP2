package com.syscom.safeaa.mybatis.vo;

import java.util.Date;

public class SyscomresourceAndCulture {

	private Integer resourceid;
	private String resourceno;
	private String safedefinefunctionlist;
	private String userdefinefunctionlist;
	private String resourceurl;
	private Date effectdate;
	private Date expireddate;
	private Integer updateuserid;
	private Date updatetime;

	private String culture;
	private String resourcename;
	private String remark;

	public Integer getResourceid() {
		return resourceid;
	}

	public void setResourceid(Integer resourceid) {
		this.resourceid = resourceid;
	}

	public String getResourceno() {
		return resourceno;
	}

	public void setResourceno(String resourceno) {
		this.resourceno = resourceno;
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

	public String getResourceurl() {
		return resourceurl;
	}

	public void setResourceurl(String resourceurl) {
		this.resourceurl = resourceurl;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
