package com.syscom.safeaa.mybatis.vo;

import java.util.Date;

public class SyscomDeputyUserMatrixVo {

	private int deputyUserId;
	private String deputyLogOnId;
	private String deputyUserName;
	private long status;
	private Date effectDate;
	private Date expiredDate;

	public int getDeputyUserId() {
		return deputyUserId;
	}

	public void setDeputyUserId(int deputyUserId) {
		this.deputyUserId = deputyUserId;
	}

	public String getDeputyLogOnId() {
		return deputyLogOnId;
	}

	public void setDeputyLogOnId(String deputyLogOnId) {
		this.deputyLogOnId = deputyLogOnId;
	}

	public String getDeputyUserName() {
		return deputyUserName;
	}

	public void setDeputyUserName(String deputyUserName) {
		this.deputyUserName = deputyUserName;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public Date getEffectDate() {
		return effectDate;
	}

	public void setEffectDate(Date effectDate) {
		this.effectDate = effectDate;
	}

	public Date getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		this.expiredDate = expiredDate;
	}
}
