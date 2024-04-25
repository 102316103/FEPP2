package com.syscom.safeaa.mybatis.vo;

import java.io.Serializable;

public class SyscomgroupInfoVo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer pid;
	private Integer id;
	private String no;
	private String name;
	private String url;
	private Integer locationno;
	private String effectdate;
	private String expireddate;
	private String type;
	
	
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	public Integer getLocationno() {
		return locationno;
	}
	public void setLocationno(Integer locationno) {
		this.locationno = locationno;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
