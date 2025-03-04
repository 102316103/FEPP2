package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Fcrmnoctl extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMNOCTL.FCRMNOCTL_BRNO
     *
     * @mbg.generated
     */
    private String fcrmnoctlBrno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMNOCTL.FCRMNOCTL_CATEGORY
     *
     * @mbg.generated
     */
    private String fcrmnoctlCategory = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMNOCTL.FCRMNOCTL_NO
     *
     * @mbg.generated
     */
    private Integer fcrmnoctlNo;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMNOCTL.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMNOCTL.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table FCRMNOCTL
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMNOCTL
     *
     * @mbg.generated
     */
    public Fcrmnoctl(String fcrmnoctlBrno, String fcrmnoctlCategory, Integer fcrmnoctlNo, Integer updateUserid, Date updateTime) {
        this.fcrmnoctlBrno = fcrmnoctlBrno;
        this.fcrmnoctlCategory = fcrmnoctlCategory;
        this.fcrmnoctlNo = fcrmnoctlNo;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMNOCTL
     *
     * @mbg.generated
     */
    public Fcrmnoctl() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMNOCTL.FCRMNOCTL_BRNO
     *
     * @return the value of FCRMNOCTL.FCRMNOCTL_BRNO
     *
     * @mbg.generated
     */
    public String getFcrmnoctlBrno() {
        return fcrmnoctlBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMNOCTL.FCRMNOCTL_BRNO
     *
     * @param fcrmnoctlBrno the value for FCRMNOCTL.FCRMNOCTL_BRNO
     *
     * @mbg.generated
     */
    public void setFcrmnoctlBrno(String fcrmnoctlBrno) {
        this.fcrmnoctlBrno = fcrmnoctlBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMNOCTL.FCRMNOCTL_CATEGORY
     *
     * @return the value of FCRMNOCTL.FCRMNOCTL_CATEGORY
     *
     * @mbg.generated
     */
    public String getFcrmnoctlCategory() {
        return fcrmnoctlCategory;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMNOCTL.FCRMNOCTL_CATEGORY
     *
     * @param fcrmnoctlCategory the value for FCRMNOCTL.FCRMNOCTL_CATEGORY
     *
     * @mbg.generated
     */
    public void setFcrmnoctlCategory(String fcrmnoctlCategory) {
        this.fcrmnoctlCategory = fcrmnoctlCategory;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMNOCTL.FCRMNOCTL_NO
     *
     * @return the value of FCRMNOCTL.FCRMNOCTL_NO
     *
     * @mbg.generated
     */
    public Integer getFcrmnoctlNo() {
        return fcrmnoctlNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMNOCTL.FCRMNOCTL_NO
     *
     * @param fcrmnoctlNo the value for FCRMNOCTL.FCRMNOCTL_NO
     *
     * @mbg.generated
     */
    public void setFcrmnoctlNo(Integer fcrmnoctlNo) {
        this.fcrmnoctlNo = fcrmnoctlNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMNOCTL.UPDATE_USERID
     *
     * @return the value of FCRMNOCTL.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMNOCTL.UPDATE_USERID
     *
     * @param updateUserid the value for FCRMNOCTL.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMNOCTL.UPDATE_TIME
     *
     * @return the value of FCRMNOCTL.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMNOCTL.UPDATE_TIME
     *
     * @param updateTime the value for FCRMNOCTL.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMNOCTL
     *
     * @mbg.generated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Fcrmnoctl other = (Fcrmnoctl) that;
        return (this.getFcrmnoctlBrno() == null ? other.getFcrmnoctlBrno() == null : this.getFcrmnoctlBrno().equals(other.getFcrmnoctlBrno()))
            && (this.getFcrmnoctlCategory() == null ? other.getFcrmnoctlCategory() == null : this.getFcrmnoctlCategory().equals(other.getFcrmnoctlCategory()))
            && (this.getFcrmnoctlNo() == null ? other.getFcrmnoctlNo() == null : this.getFcrmnoctlNo().equals(other.getFcrmnoctlNo()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMNOCTL
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFcrmnoctlBrno() == null) ? 0 : getFcrmnoctlBrno().hashCode());
        result = prime * result + ((getFcrmnoctlCategory() == null) ? 0 : getFcrmnoctlCategory().hashCode());
        result = prime * result + ((getFcrmnoctlNo() == null) ? 0 : getFcrmnoctlNo().hashCode());
        result = prime * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMNOCTL
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", fcrmnoctlBrno=").append(fcrmnoctlBrno);
        sb.append(", fcrmnoctlCategory=").append(fcrmnoctlCategory);
        sb.append(", fcrmnoctlNo=").append(fcrmnoctlNo);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMNOCTL
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<FCRMNOCTL_BRNO>").append(this.fcrmnoctlBrno).append("</FCRMNOCTL_BRNO>");
        sb.append("<FCRMNOCTL_CATEGORY>").append(this.fcrmnoctlCategory).append("</FCRMNOCTL_CATEGORY>");
        sb.append("<FCRMNOCTL_NO>").append(this.fcrmnoctlNo).append("</FCRMNOCTL_NO>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}