package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

public class UI_060550_FormDetail extends BaseForm {
	private static final long serialVersionUID = 1L;
	
	private String feptxnTxDate;
	private String feptxnTxTime;
	private Integer feptxnEjfno;
	private String feptxnTbsdyFisc;
	private String feptxnMsgid;
	private Short feptxnSubsys;

	public String getFeptxnTxDate() {
		return feptxnTxDate;
	}

	public void setFeptxnTxDate(String feptxnTxDate) {
		this.feptxnTxDate = feptxnTxDate;
	}

	public String getFeptxnTxTime() {
		return feptxnTxTime;
	}

	public void setFeptxnTxTime(String feptxnTxTime) {
		this.feptxnTxTime = feptxnTxTime;
	}

	public Integer getFeptxnEjfno() {
		return feptxnEjfno;
	}

	public void setFeptxnEjfno(Integer feptxnEjfno) {
		this.feptxnEjfno = feptxnEjfno;
	}

	public String getFeptxnTbsdyFisc() {
		return feptxnTbsdyFisc;
	}

	public void setFeptxnTbsdyFisc(String feptxnTbsdyFisc) {
		this.feptxnTbsdyFisc = feptxnTbsdyFisc;
	}

	public String getFeptxnMsgid() {
		return feptxnMsgid;
	}

	public void setFeptxnMsgid(String feptxnMsgid) {
		this.feptxnMsgid = feptxnMsgid;
	}

	public Short getFeptxnSubsys() {
		return feptxnSubsys;
	}

	public void setFeptxnSubsys(Short feptxnSubsys) {
		this.feptxnSubsys = feptxnSubsys;
	}
}
