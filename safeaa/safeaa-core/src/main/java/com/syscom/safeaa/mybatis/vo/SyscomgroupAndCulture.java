package com.syscom.safeaa.mybatis.vo;

import java.util.Date;
public class SyscomgroupAndCulture {

	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apdefine1 == null) ? 0 : apdefine1.hashCode());
		result = prime * result + ((apdefine2 == null) ? 0 : apdefine2.hashCode());
		result = prime * result + ((culture == null) ? 0 : culture.hashCode());
		result = prime * result + ((effectdate == null) ? 0 : effectdate.hashCode());
		result = prime * result + ((expireddate == null) ? 0 : expireddate.hashCode());
		result = prime * result + ((groupid == null) ? 0 : groupid.hashCode());
		result = prime * result + ((groupname == null) ? 0 : groupname.hashCode());
		result = prime * result + ((groupno == null) ? 0 : groupno.hashCode());
		result = prime * result + ((groupurl == null) ? 0 : groupurl.hashCode());
		result = prime * result + ((locationno == null) ? 0 : locationno.hashCode());
		result = prime * result + ((remark == null) ? 0 : remark.hashCode());
		result = prime * result + ((updatetime == null) ? 0 : updatetime.hashCode());
		result = prime * result + ((updateuserid == null) ? 0 : updateuserid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SyscomgroupAndCulture other = (SyscomgroupAndCulture) obj;
		if (apdefine1 == null) {
			if (other.apdefine1 != null)
				return false;
		} else if (!apdefine1.equals(other.apdefine1))
			return false;
		if (apdefine2 == null) {
			if (other.apdefine2 != null)
				return false;
		} else if (!apdefine2.equals(other.apdefine2))
			return false;
		if (culture == null) {
			if (other.culture != null)
				return false;
		} else if (!culture.equals(other.culture))
			return false;
		if (effectdate == null) {
			if (other.effectdate != null)
				return false;
		} else if (!effectdate.equals(other.effectdate))
			return false;
		if (expireddate == null) {
			if (other.expireddate != null)
				return false;
		} else if (!expireddate.equals(other.expireddate))
			return false;
		if (groupid == null) {
			if (other.groupid != null)
				return false;
		} else if (!groupid.equals(other.groupid))
			return false;
		if (groupname == null) {
			if (other.groupname != null)
				return false;
		} else if (!groupname.equals(other.groupname))
			return false;
		if (groupno == null) {
			if (other.groupno != null)
				return false;
		} else if (!groupno.equals(other.groupno))
			return false;
		if (groupurl == null) {
			if (other.groupurl != null)
				return false;
		} else if (!groupurl.equals(other.groupurl))
			return false;
		if (locationno == null) {
			if (other.locationno != null)
				return false;
		} else if (!locationno.equals(other.locationno))
			return false;
		if (remark == null) {
			if (other.remark != null)
				return false;
		} else if (!remark.equals(other.remark))
			return false;
		if (updatetime == null) {
			if (other.updatetime != null)
				return false;
		} else if (!updatetime.equals(other.updatetime))
			return false;
		if (updateuserid == null) {
			if (other.updateuserid != null)
				return false;
		} else if (!updateuserid.equals(other.updateuserid))
			return false;
		return true;
	}

	public SyscomgroupAndCulture() {
		super();
	}
	
	public SyscomgroupAndCulture(String culture) {
		super();
		this.culture = culture;
	}
	
	public SyscomgroupAndCulture(Integer groupid,String culture) {
		super();
		this.culture = culture;
		this.groupid = groupid;
	}	
	public SyscomgroupAndCulture(Integer groupid, String groupno, Integer locationno, String groupurl, Date effectdate,
			Date expireddate, Integer updateuserid, Date updatetime, String apdefine1, String apdefine2, String culture,
			String groupname, String remark) {
		super();
		this.groupid = groupid;
		this.groupno = groupno;
		this.locationno = locationno;
		this.groupurl = groupurl;
		this.effectdate = effectdate;
		this.expireddate = expireddate;
		this.updateuserid = updateuserid;
		this.updatetime = updatetime;
		this.apdefine1 = apdefine1;
		this.apdefine2 = apdefine2;
		this.culture = culture;
		this.groupname = groupname;
		this.remark = remark;
	}

	public Integer getGroupid() {
		return groupid;
	}

	public void setGroupid(Integer groupid) {
		this.groupid = groupid;
	}

	public String getGroupno() {
		return groupno;
	}

	public void setGroupno(String groupno) {
		this.groupno = groupno;
	}

	public Integer getLocationno() {
		return locationno;
	}

	public void setLocationno(Integer locationno) {
		this.locationno = locationno;
	}

	public String getGroupurl() {
		return groupurl;
	}

	public void setGroupurl(String groupurl) {
		this.groupurl = groupurl;
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

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.GROUPID
    *
    * @mbg.generated
    */
   private Integer groupid;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.GROUPNO
    *
    * @mbg.generated
    */
   private String groupno;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.LOCATIONNO
    *
    * @mbg.generated
    */
   private Integer locationno;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.GROUPURL
    *
    * @mbg.generated
    */
   private String groupurl;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.EFFECTDATE
    *
    * @mbg.generated
    */
   private Date effectdate;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.EXPIREDDATE
    *
    * @mbg.generated
    */
   private Date expireddate;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.UPDATEUSERID
    *
    * @mbg.generated
    */
   private Integer updateuserid;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.UPDATETIME
    *
    * @mbg.generated
    */
   private Date updatetime;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.APDEFINE1
    *
    * @mbg.generated
    */
   private String apdefine1;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUP.APDEFINE2
    *
    * @mbg.generated
    */
   private String apdefine2;
   
   private String culture;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUPCULTURE.GROUPNAME
    *
    * @mbg.generated
    */
   private String groupname;

   /**
    *
    * This field was generated by MyBatis Generator.
    * This field corresponds to the database column SYSCOMGROUPCULTURE.REMARK
    *
    * @mbg.generated
    */
   private String remark;

	@Override
	public String toString() {
		return "SyscomgroupAndCulture [groupid=" + groupid + ", groupno=" + groupno + ", locationno=" + locationno
				+ ", groupurl=" + groupurl + ", effectdate=" + effectdate + ", expireddate=" + expireddate
				+ ", updateuserid=" + updateuserid + ", updatetime=" + updatetime + ", apdefine1=" + apdefine1
				+ ", apdefine2=" + apdefine2 + ", culture=" + culture + ", groupname=" + groupname + ", remark=" + remark
				+ "]";
	}
   
   
}
