package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Atmorderlog extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_ID
     *
     * @mbg.generated
     */
    private Integer atmorderId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_ATMNO
     *
     * @mbg.generated
     */
    private String atmorderAtmno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_TX_DATE
     *
     * @mbg.generated
     */
    private String atmorderTxDate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_TX_TIME
     *
     * @mbg.generated
     */
    private String atmorderTxTime = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_TX_CODE
     *
     * @mbg.generated
     */
    private String atmorderTxCode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_ATMOD
     *
     * @mbg.generated
     */
    private Short atmorderAtmod;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_MSGTYPE
     *
     * @mbg.generated
     */
    private Short atmorderMsgtype;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ATMORDERLOG.ATMORDER_MESSAGE
     *
     * @mbg.generated
     */
    private String atmorderMessage;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table ATMORDERLOG
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMORDERLOG
     *
     * @mbg.generated
     */
    public Atmorderlog(Integer atmorderId, String atmorderAtmno, String atmorderTxDate, String atmorderTxTime, String atmorderTxCode, Short atmorderAtmod, Short atmorderMsgtype, Integer updateUserid, Date updateTime) {
        this.atmorderId = atmorderId;
        this.atmorderAtmno = atmorderAtmno;
        this.atmorderTxDate = atmorderTxDate;
        this.atmorderTxTime = atmorderTxTime;
        this.atmorderTxCode = atmorderTxCode;
        this.atmorderAtmod = atmorderAtmod;
        this.atmorderMsgtype = atmorderMsgtype;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMORDERLOG
     *
     * @mbg.generated
     */
    public Atmorderlog(Integer atmorderId, String atmorderAtmno, String atmorderTxDate, String atmorderTxTime, String atmorderTxCode, Short atmorderAtmod, Short atmorderMsgtype, Integer updateUserid, Date updateTime, String atmorderMessage) {
        this.atmorderId = atmorderId;
        this.atmorderAtmno = atmorderAtmno;
        this.atmorderTxDate = atmorderTxDate;
        this.atmorderTxTime = atmorderTxTime;
        this.atmorderTxCode = atmorderTxCode;
        this.atmorderAtmod = atmorderAtmod;
        this.atmorderMsgtype = atmorderMsgtype;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
        this.atmorderMessage = atmorderMessage;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMORDERLOG
     *
     * @mbg.generated
     */
    public Atmorderlog() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_ID
     *
     * @return the value of ATMORDERLOG.ATMORDER_ID
     *
     * @mbg.generated
     */
    public Integer getAtmorderId() {
        return atmorderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_ID
     *
     * @param atmorderId the value for ATMORDERLOG.ATMORDER_ID
     *
     * @mbg.generated
     */
    public void setAtmorderId(Integer atmorderId) {
        this.atmorderId = atmorderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_ATMNO
     *
     * @return the value of ATMORDERLOG.ATMORDER_ATMNO
     *
     * @mbg.generated
     */
    public String getAtmorderAtmno() {
        return atmorderAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_ATMNO
     *
     * @param atmorderAtmno the value for ATMORDERLOG.ATMORDER_ATMNO
     *
     * @mbg.generated
     */
    public void setAtmorderAtmno(String atmorderAtmno) {
        this.atmorderAtmno = atmorderAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_TX_DATE
     *
     * @return the value of ATMORDERLOG.ATMORDER_TX_DATE
     *
     * @mbg.generated
     */
    public String getAtmorderTxDate() {
        return atmorderTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_TX_DATE
     *
     * @param atmorderTxDate the value for ATMORDERLOG.ATMORDER_TX_DATE
     *
     * @mbg.generated
     */
    public void setAtmorderTxDate(String atmorderTxDate) {
        this.atmorderTxDate = atmorderTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_TX_TIME
     *
     * @return the value of ATMORDERLOG.ATMORDER_TX_TIME
     *
     * @mbg.generated
     */
    public String getAtmorderTxTime() {
        return atmorderTxTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_TX_TIME
     *
     * @param atmorderTxTime the value for ATMORDERLOG.ATMORDER_TX_TIME
     *
     * @mbg.generated
     */
    public void setAtmorderTxTime(String atmorderTxTime) {
        this.atmorderTxTime = atmorderTxTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_TX_CODE
     *
     * @return the value of ATMORDERLOG.ATMORDER_TX_CODE
     *
     * @mbg.generated
     */
    public String getAtmorderTxCode() {
        return atmorderTxCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_TX_CODE
     *
     * @param atmorderTxCode the value for ATMORDERLOG.ATMORDER_TX_CODE
     *
     * @mbg.generated
     */
    public void setAtmorderTxCode(String atmorderTxCode) {
        this.atmorderTxCode = atmorderTxCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_ATMOD
     *
     * @return the value of ATMORDERLOG.ATMORDER_ATMOD
     *
     * @mbg.generated
     */
    public Short getAtmorderAtmod() {
        return atmorderAtmod;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_ATMOD
     *
     * @param atmorderAtmod the value for ATMORDERLOG.ATMORDER_ATMOD
     *
     * @mbg.generated
     */
    public void setAtmorderAtmod(Short atmorderAtmod) {
        this.atmorderAtmod = atmorderAtmod;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_MSGTYPE
     *
     * @return the value of ATMORDERLOG.ATMORDER_MSGTYPE
     *
     * @mbg.generated
     */
    public Short getAtmorderMsgtype() {
        return atmorderMsgtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_MSGTYPE
     *
     * @param atmorderMsgtype the value for ATMORDERLOG.ATMORDER_MSGTYPE
     *
     * @mbg.generated
     */
    public void setAtmorderMsgtype(Short atmorderMsgtype) {
        this.atmorderMsgtype = atmorderMsgtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.UPDATE_USERID
     *
     * @return the value of ATMORDERLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.UPDATE_USERID
     *
     * @param updateUserid the value for ATMORDERLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.UPDATE_TIME
     *
     * @return the value of ATMORDERLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.UPDATE_TIME
     *
     * @param updateTime the value for ATMORDERLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ATMORDERLOG.ATMORDER_MESSAGE
     *
     * @return the value of ATMORDERLOG.ATMORDER_MESSAGE
     *
     * @mbg.generated
     */
    public String getAtmorderMessage() {
        return atmorderMessage;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ATMORDERLOG.ATMORDER_MESSAGE
     *
     * @param atmorderMessage the value for ATMORDERLOG.ATMORDER_MESSAGE
     *
     * @mbg.generated
     */
    public void setAtmorderMessage(String atmorderMessage) {
        this.atmorderMessage = atmorderMessage;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMORDERLOG
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", atmorderId=").append(atmorderId);
        sb.append(", atmorderAtmno=").append(atmorderAtmno);
        sb.append(", atmorderTxDate=").append(atmorderTxDate);
        sb.append(", atmorderTxTime=").append(atmorderTxTime);
        sb.append(", atmorderTxCode=").append(atmorderTxCode);
        sb.append(", atmorderAtmod=").append(atmorderAtmod);
        sb.append(", atmorderMsgtype=").append(atmorderMsgtype);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", atmorderMessage=").append(atmorderMessage);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMORDERLOG
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
        Atmorderlog other = (Atmorderlog) that;
        return (this.getAtmorderId() == null ? other.getAtmorderId() == null : this.getAtmorderId().equals(other.getAtmorderId()))
            && (this.getAtmorderAtmno() == null ? other.getAtmorderAtmno() == null : this.getAtmorderAtmno().equals(other.getAtmorderAtmno()))
            && (this.getAtmorderTxDate() == null ? other.getAtmorderTxDate() == null : this.getAtmorderTxDate().equals(other.getAtmorderTxDate()))
            && (this.getAtmorderTxTime() == null ? other.getAtmorderTxTime() == null : this.getAtmorderTxTime().equals(other.getAtmorderTxTime()))
            && (this.getAtmorderTxCode() == null ? other.getAtmorderTxCode() == null : this.getAtmorderTxCode().equals(other.getAtmorderTxCode()))
            && (this.getAtmorderAtmod() == null ? other.getAtmorderAtmod() == null : this.getAtmorderAtmod().equals(other.getAtmorderAtmod()))
            && (this.getAtmorderMsgtype() == null ? other.getAtmorderMsgtype() == null : this.getAtmorderMsgtype().equals(other.getAtmorderMsgtype()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getAtmorderMessage() == null ? other.getAtmorderMessage() == null : this.getAtmorderMessage().equals(other.getAtmorderMessage()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMORDERLOG
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getAtmorderId() == null) ? 0 : getAtmorderId().hashCode());
        result = 31 * result + ((getAtmorderAtmno() == null) ? 0 : getAtmorderAtmno().hashCode());
        result = 31 * result + ((getAtmorderTxDate() == null) ? 0 : getAtmorderTxDate().hashCode());
        result = 31 * result + ((getAtmorderTxTime() == null) ? 0 : getAtmorderTxTime().hashCode());
        result = 31 * result + ((getAtmorderTxCode() == null) ? 0 : getAtmorderTxCode().hashCode());
        result = 31 * result + ((getAtmorderAtmod() == null) ? 0 : getAtmorderAtmod().hashCode());
        result = 31 * result + ((getAtmorderMsgtype() == null) ? 0 : getAtmorderMsgtype().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = 31 * result + ((getAtmorderMessage() == null) ? 0 : getAtmorderMessage().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ATMORDERLOG
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ATMORDER_ID>").append(this.atmorderId).append("</ATMORDER_ID>");
        sb.append("<ATMORDER_ATMNO>").append(this.atmorderAtmno).append("</ATMORDER_ATMNO>");
        sb.append("<ATMORDER_TX_DATE>").append(this.atmorderTxDate).append("</ATMORDER_TX_DATE>");
        sb.append("<ATMORDER_TX_TIME>").append(this.atmorderTxTime).append("</ATMORDER_TX_TIME>");
        sb.append("<ATMORDER_TX_CODE>").append(this.atmorderTxCode).append("</ATMORDER_TX_CODE>");
        sb.append("<ATMORDER_ATMOD>").append(this.atmorderAtmod).append("</ATMORDER_ATMOD>");
        sb.append("<ATMORDER_MSGTYPE>").append(this.atmorderMsgtype).append("</ATMORDER_MSGTYPE>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        sb.append("<ATMORDER_MESSAGE>").append(this.atmorderMessage).append("</ATMORDER_MESSAGE>");
        return sb.toString();
    }
}