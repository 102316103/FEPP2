package com.syscom.safeaa.mybatis.vo;

import java.util.Date;

public class SyscomQueryAllPolicysVo {

	private Integer PolicyId;
	private String PolicyNo;
	private Integer AllowLocal;
	private Integer GetValueType;
	private String defaultpolicyvalue;
	private String invalidmessageid;
	private Date EffectDate;
	private Date ExpiredDate;
	private Integer updateuserid;
	private Date updatetime;
	private String culture;
	private String policyname;
	private String remark;

	public Integer getPolicyId() {
		return PolicyId;
	}

	public void setPolicyId(Integer policyId) {
		PolicyId = policyId;
	}

	public String getPolicyNo() {
		return PolicyNo;
	}

	public void setPolicyNo(String policyNo) {
		PolicyNo = policyNo;
	}

	public Integer getAllowLocal() {
		return AllowLocal;
	}

	public void setAllowLocal(Integer allowLocal) {
		AllowLocal = allowLocal;
	}

	public Integer getGetValueType() {
		return GetValueType;
	}

	public void setGetValueType(Integer getValueType) {
		GetValueType = getValueType;
	}

	public String getDefaultpolicyvalue() {
		return defaultpolicyvalue;
	}

	public void setDefaultpolicyvalue(String defaultpolicyvalue) {
		this.defaultpolicyvalue = defaultpolicyvalue;
	}

	public String getInvalidmessageid() {
		return invalidmessageid;
	}

	public void setInvalidmessageid(String invalidmessageid) {
		this.invalidmessageid = invalidmessageid;
	}

	public Date getEffectDate() {
		return EffectDate;
	}

	public void setEffectDate(Date effectDate) {
		EffectDate = effectDate;
	}

	public Date getExpiredDate() {
		return ExpiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		ExpiredDate = expiredDate;
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

	public String getPolicyname() {
		return policyname;
	}

	public void setPolicyname(String policyname) {
		this.policyname = policyname;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
