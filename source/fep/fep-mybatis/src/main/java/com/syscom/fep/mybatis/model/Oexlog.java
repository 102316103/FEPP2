package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Oexlog extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.OEXLOG_TX_DATE
     *
     * @mbg.generated
     */
    private String oexlogTxDate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.OEXLOG_EJFNO
     *
     * @mbg.generated
     */
    private Integer oexlogEjfno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.OEXLOG_ATMNO
     *
     * @mbg.generated
     */
    private String oexlogAtmno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.OEXLOG_TX_TIME
     *
     * @mbg.generated
     */
    private String oexlogTxTime = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.OEXLOG_BOX_AREA
     *
     * @mbg.generated
     */
    private String oexlogBoxArea;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.OEXLOG_COMPARE
     *
     * @mbg.generated
     */
    private String oexlogCompare;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.OEXLOG_REMARK
     *
     * @mbg.generated
     */
    private String oexlogRemark;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OEXLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    public Oexlog(String oexlogTxDate, Integer oexlogEjfno, String oexlogAtmno, String oexlogTxTime, String oexlogBoxArea, String oexlogCompare, String oexlogRemark, Integer updateUserid, Date updateTime) {
        this.oexlogTxDate = oexlogTxDate;
        this.oexlogEjfno = oexlogEjfno;
        this.oexlogAtmno = oexlogAtmno;
        this.oexlogTxTime = oexlogTxTime;
        this.oexlogBoxArea = oexlogBoxArea;
        this.oexlogCompare = oexlogCompare;
        this.oexlogRemark = oexlogRemark;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    public Oexlog() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.OEXLOG_TX_DATE
     *
     * @return the value of OEXLOG.OEXLOG_TX_DATE
     *
     * @mbg.generated
     */
    public String getOexlogTxDate() {
        return oexlogTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.OEXLOG_TX_DATE
     *
     * @param oexlogTxDate the value for OEXLOG.OEXLOG_TX_DATE
     *
     * @mbg.generated
     */
    public void setOexlogTxDate(String oexlogTxDate) {
        this.oexlogTxDate = oexlogTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.OEXLOG_EJFNO
     *
     * @return the value of OEXLOG.OEXLOG_EJFNO
     *
     * @mbg.generated
     */
    public Integer getOexlogEjfno() {
        return oexlogEjfno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.OEXLOG_EJFNO
     *
     * @param oexlogEjfno the value for OEXLOG.OEXLOG_EJFNO
     *
     * @mbg.generated
     */
    public void setOexlogEjfno(Integer oexlogEjfno) {
        this.oexlogEjfno = oexlogEjfno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.OEXLOG_ATMNO
     *
     * @return the value of OEXLOG.OEXLOG_ATMNO
     *
     * @mbg.generated
     */
    public String getOexlogAtmno() {
        return oexlogAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.OEXLOG_ATMNO
     *
     * @param oexlogAtmno the value for OEXLOG.OEXLOG_ATMNO
     *
     * @mbg.generated
     */
    public void setOexlogAtmno(String oexlogAtmno) {
        this.oexlogAtmno = oexlogAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.OEXLOG_TX_TIME
     *
     * @return the value of OEXLOG.OEXLOG_TX_TIME
     *
     * @mbg.generated
     */
    public String getOexlogTxTime() {
        return oexlogTxTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.OEXLOG_TX_TIME
     *
     * @param oexlogTxTime the value for OEXLOG.OEXLOG_TX_TIME
     *
     * @mbg.generated
     */
    public void setOexlogTxTime(String oexlogTxTime) {
        this.oexlogTxTime = oexlogTxTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.OEXLOG_BOX_AREA
     *
     * @return the value of OEXLOG.OEXLOG_BOX_AREA
     *
     * @mbg.generated
     */
    public String getOexlogBoxArea() {
        return oexlogBoxArea;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.OEXLOG_BOX_AREA
     *
     * @param oexlogBoxArea the value for OEXLOG.OEXLOG_BOX_AREA
     *
     * @mbg.generated
     */
    public void setOexlogBoxArea(String oexlogBoxArea) {
        this.oexlogBoxArea = oexlogBoxArea;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.OEXLOG_COMPARE
     *
     * @return the value of OEXLOG.OEXLOG_COMPARE
     *
     * @mbg.generated
     */
    public String getOexlogCompare() {
        return oexlogCompare;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.OEXLOG_COMPARE
     *
     * @param oexlogCompare the value for OEXLOG.OEXLOG_COMPARE
     *
     * @mbg.generated
     */
    public void setOexlogCompare(String oexlogCompare) {
        this.oexlogCompare = oexlogCompare;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.OEXLOG_REMARK
     *
     * @return the value of OEXLOG.OEXLOG_REMARK
     *
     * @mbg.generated
     */
    public String getOexlogRemark() {
        return oexlogRemark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.OEXLOG_REMARK
     *
     * @param oexlogRemark the value for OEXLOG.OEXLOG_REMARK
     *
     * @mbg.generated
     */
    public void setOexlogRemark(String oexlogRemark) {
        this.oexlogRemark = oexlogRemark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.UPDATE_USERID
     *
     * @return the value of OEXLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.UPDATE_USERID
     *
     * @param updateUserid the value for OEXLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OEXLOG.UPDATE_TIME
     *
     * @return the value of OEXLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OEXLOG.UPDATE_TIME
     *
     * @param updateTime the value for OEXLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", oexlogTxDate=").append(oexlogTxDate);
        sb.append(", oexlogEjfno=").append(oexlogEjfno);
        sb.append(", oexlogAtmno=").append(oexlogAtmno);
        sb.append(", oexlogTxTime=").append(oexlogTxTime);
        sb.append(", oexlogBoxArea=").append(oexlogBoxArea);
        sb.append(", oexlogCompare=").append(oexlogCompare);
        sb.append(", oexlogRemark=").append(oexlogRemark);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
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
        Oexlog other = (Oexlog) that;
        return (this.getOexlogTxDate() == null ? other.getOexlogTxDate() == null : this.getOexlogTxDate().equals(other.getOexlogTxDate()))
            && (this.getOexlogEjfno() == null ? other.getOexlogEjfno() == null : this.getOexlogEjfno().equals(other.getOexlogEjfno()))
            && (this.getOexlogAtmno() == null ? other.getOexlogAtmno() == null : this.getOexlogAtmno().equals(other.getOexlogAtmno()))
            && (this.getOexlogTxTime() == null ? other.getOexlogTxTime() == null : this.getOexlogTxTime().equals(other.getOexlogTxTime()))
            && (this.getOexlogBoxArea() == null ? other.getOexlogBoxArea() == null : this.getOexlogBoxArea().equals(other.getOexlogBoxArea()))
            && (this.getOexlogCompare() == null ? other.getOexlogCompare() == null : this.getOexlogCompare().equals(other.getOexlogCompare()))
            && (this.getOexlogRemark() == null ? other.getOexlogRemark() == null : this.getOexlogRemark().equals(other.getOexlogRemark()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getOexlogTxDate() == null) ? 0 : getOexlogTxDate().hashCode());
        result = 31 * result + ((getOexlogEjfno() == null) ? 0 : getOexlogEjfno().hashCode());
        result = 31 * result + ((getOexlogAtmno() == null) ? 0 : getOexlogAtmno().hashCode());
        result = 31 * result + ((getOexlogTxTime() == null) ? 0 : getOexlogTxTime().hashCode());
        result = 31 * result + ((getOexlogBoxArea() == null) ? 0 : getOexlogBoxArea().hashCode());
        result = 31 * result + ((getOexlogCompare() == null) ? 0 : getOexlogCompare().hashCode());
        result = 31 * result + ((getOexlogRemark() == null) ? 0 : getOexlogRemark().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OEXLOG
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<OEXLOG_TX_DATE>").append(this.oexlogTxDate).append("</OEXLOG_TX_DATE>");
        sb.append("<OEXLOG_EJFNO>").append(this.oexlogEjfno).append("</OEXLOG_EJFNO>");
        sb.append("<OEXLOG_ATMNO>").append(this.oexlogAtmno).append("</OEXLOG_ATMNO>");
        sb.append("<OEXLOG_TX_TIME>").append(this.oexlogTxTime).append("</OEXLOG_TX_TIME>");
        sb.append("<OEXLOG_BOX_AREA>").append(this.oexlogBoxArea).append("</OEXLOG_BOX_AREA>");
        sb.append("<OEXLOG_COMPARE>").append(this.oexlogCompare).append("</OEXLOG_COMPARE>");
        sb.append("<OEXLOG_REMARK>").append(this.oexlogRemark).append("</OEXLOG_REMARK>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}