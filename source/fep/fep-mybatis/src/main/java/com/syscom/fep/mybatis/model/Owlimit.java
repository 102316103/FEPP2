package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Owlimit extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OWLIMIT.OWLIMIT_ACTNO
     *
     * @mbg.generated
     */
    private String owlimitActno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OWLIMIT.OWLIMIT_CARD_SEQ
     *
     * @mbg.generated
     */
    private Short owlimitCardSeq;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OWLIMIT.OWLIMIT_DATE
     *
     * @mbg.generated
     */
    private String owlimitDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OWLIMIT.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column OWLIMIT.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table OWLIMIT
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OWLIMIT
     *
     * @mbg.generated
     */
    public Owlimit(String owlimitActno, Short owlimitCardSeq, String owlimitDate, Integer updateUserid, Date updateTime) {
        this.owlimitActno = owlimitActno;
        this.owlimitCardSeq = owlimitCardSeq;
        this.owlimitDate = owlimitDate;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OWLIMIT
     *
     * @mbg.generated
     */
    public Owlimit() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OWLIMIT.OWLIMIT_ACTNO
     *
     * @return the value of OWLIMIT.OWLIMIT_ACTNO
     *
     * @mbg.generated
     */
    public String getOwlimitActno() {
        return owlimitActno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OWLIMIT.OWLIMIT_ACTNO
     *
     * @param owlimitActno the value for OWLIMIT.OWLIMIT_ACTNO
     *
     * @mbg.generated
     */
    public void setOwlimitActno(String owlimitActno) {
        this.owlimitActno = owlimitActno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OWLIMIT.OWLIMIT_CARD_SEQ
     *
     * @return the value of OWLIMIT.OWLIMIT_CARD_SEQ
     *
     * @mbg.generated
     */
    public Short getOwlimitCardSeq() {
        return owlimitCardSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OWLIMIT.OWLIMIT_CARD_SEQ
     *
     * @param owlimitCardSeq the value for OWLIMIT.OWLIMIT_CARD_SEQ
     *
     * @mbg.generated
     */
    public void setOwlimitCardSeq(Short owlimitCardSeq) {
        this.owlimitCardSeq = owlimitCardSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OWLIMIT.OWLIMIT_DATE
     *
     * @return the value of OWLIMIT.OWLIMIT_DATE
     *
     * @mbg.generated
     */
    public String getOwlimitDate() {
        return owlimitDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OWLIMIT.OWLIMIT_DATE
     *
     * @param owlimitDate the value for OWLIMIT.OWLIMIT_DATE
     *
     * @mbg.generated
     */
    public void setOwlimitDate(String owlimitDate) {
        this.owlimitDate = owlimitDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OWLIMIT.UPDATE_USERID
     *
     * @return the value of OWLIMIT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OWLIMIT.UPDATE_USERID
     *
     * @param updateUserid the value for OWLIMIT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column OWLIMIT.UPDATE_TIME
     *
     * @return the value of OWLIMIT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column OWLIMIT.UPDATE_TIME
     *
     * @param updateTime the value for OWLIMIT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OWLIMIT
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", owlimitActno=").append(owlimitActno);
        sb.append(", owlimitCardSeq=").append(owlimitCardSeq);
        sb.append(", owlimitDate=").append(owlimitDate);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OWLIMIT
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
        Owlimit other = (Owlimit) that;
        return (this.getOwlimitActno() == null ? other.getOwlimitActno() == null : this.getOwlimitActno().equals(other.getOwlimitActno()))
            && (this.getOwlimitCardSeq() == null ? other.getOwlimitCardSeq() == null : this.getOwlimitCardSeq().equals(other.getOwlimitCardSeq()))
            && (this.getOwlimitDate() == null ? other.getOwlimitDate() == null : this.getOwlimitDate().equals(other.getOwlimitDate()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OWLIMIT
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getOwlimitActno() == null) ? 0 : getOwlimitActno().hashCode());
        result = 31 * result + ((getOwlimitCardSeq() == null) ? 0 : getOwlimitCardSeq().hashCode());
        result = 31 * result + ((getOwlimitDate() == null) ? 0 : getOwlimitDate().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table OWLIMIT
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<OWLIMIT_ACTNO>").append(this.owlimitActno).append("</OWLIMIT_ACTNO>");
        sb.append("<OWLIMIT_CARD_SEQ>").append(this.owlimitCardSeq).append("</OWLIMIT_CARD_SEQ>");
        sb.append("<OWLIMIT_DATE>").append(this.owlimitDate).append("</OWLIMIT_DATE>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}