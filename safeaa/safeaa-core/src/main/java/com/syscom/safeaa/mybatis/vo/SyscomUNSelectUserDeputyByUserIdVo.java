package com.syscom.safeaa.mybatis.vo;

import java.util.Date;

public class SyscomUNSelectUserDeputyByUserIdVo {

	private Integer UserId;
	private String LogOnId;
	private String UserName;
	private Date EffectDate;
	private Date ExpiredDate;

	public Integer getUserId() {
		return UserId;
	}

	public void setUserId(Integer userId) {
		UserId = userId;
	}

	public String getLogOnId() {
		return LogOnId;
	}

	public void setLogOnId(String logOnId) {
		LogOnId = logOnId;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
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
}
