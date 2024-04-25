package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Cardseq extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDSEQ.CARDSEQNO
     *
     * @mbg.generated
     */
    private String cardseqno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDSEQ.CARDTYPE
     *
     * @mbg.generated
     */
    private Short cardtype;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDSEQ.STATUS
     *
     * @mbg.generated
     */
    private Short status;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDSEQ.CREATEDATE
     *
     * @mbg.generated
     */
    private String createdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDSEQ.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDSEQ.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDSEQ.CREDITPRODTYPE
     *
     * @mbg.generated
     */
    private String creditprodtype;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table CARDSEQ
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDSEQ
     *
     * @mbg.generated
     */
    public Cardseq(String cardseqno, Short cardtype, Short status, String createdate, Integer updateUserid, Date updateTime, String creditprodtype) {
        this.cardseqno = cardseqno;
        this.cardtype = cardtype;
        this.status = status;
        this.createdate = createdate;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
        this.creditprodtype = creditprodtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDSEQ
     *
     * @mbg.generated
     */
    public Cardseq() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDSEQ.CARDSEQNO
     *
     * @return the value of CARDSEQ.CARDSEQNO
     *
     * @mbg.generated
     */
    public String getCardseqno() {
        return cardseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDSEQ.CARDSEQNO
     *
     * @param cardseqno the value for CARDSEQ.CARDSEQNO
     *
     * @mbg.generated
     */
    public void setCardseqno(String cardseqno) {
        this.cardseqno = cardseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDSEQ.CARDTYPE
     *
     * @return the value of CARDSEQ.CARDTYPE
     *
     * @mbg.generated
     */
    public Short getCardtype() {
        return cardtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDSEQ.CARDTYPE
     *
     * @param cardtype the value for CARDSEQ.CARDTYPE
     *
     * @mbg.generated
     */
    public void setCardtype(Short cardtype) {
        this.cardtype = cardtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDSEQ.STATUS
     *
     * @return the value of CARDSEQ.STATUS
     *
     * @mbg.generated
     */
    public Short getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDSEQ.STATUS
     *
     * @param status the value for CARDSEQ.STATUS
     *
     * @mbg.generated
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDSEQ.CREATEDATE
     *
     * @return the value of CARDSEQ.CREATEDATE
     *
     * @mbg.generated
     */
    public String getCreatedate() {
        return createdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDSEQ.CREATEDATE
     *
     * @param createdate the value for CARDSEQ.CREATEDATE
     *
     * @mbg.generated
     */
    public void setCreatedate(String createdate) {
        this.createdate = createdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDSEQ.UPDATE_USERID
     *
     * @return the value of CARDSEQ.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDSEQ.UPDATE_USERID
     *
     * @param updateUserid the value for CARDSEQ.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDSEQ.UPDATE_TIME
     *
     * @return the value of CARDSEQ.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDSEQ.UPDATE_TIME
     *
     * @param updateTime the value for CARDSEQ.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDSEQ.CREDITPRODTYPE
     *
     * @return the value of CARDSEQ.CREDITPRODTYPE
     *
     * @mbg.generated
     */
    public String getCreditprodtype() {
        return creditprodtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDSEQ.CREDITPRODTYPE
     *
     * @param creditprodtype the value for CARDSEQ.CREDITPRODTYPE
     *
     * @mbg.generated
     */
    public void setCreditprodtype(String creditprodtype) {
        this.creditprodtype = creditprodtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDSEQ
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", cardseqno=").append(cardseqno);
        sb.append(", cardtype=").append(cardtype);
        sb.append(", status=").append(status);
        sb.append(", createdate=").append(createdate);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", creditprodtype=").append(creditprodtype);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDSEQ
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
        Cardseq other = (Cardseq) that;
        return (this.getCardseqno() == null ? other.getCardseqno() == null : this.getCardseqno().equals(other.getCardseqno()))
            && (this.getCardtype() == null ? other.getCardtype() == null : this.getCardtype().equals(other.getCardtype()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedate() == null ? other.getCreatedate() == null : this.getCreatedate().equals(other.getCreatedate()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getCreditprodtype() == null ? other.getCreditprodtype() == null : this.getCreditprodtype().equals(other.getCreditprodtype()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDSEQ
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getCardseqno() == null) ? 0 : getCardseqno().hashCode());
        result = 31 * result + ((getCardtype() == null) ? 0 : getCardtype().hashCode());
        result = 31 * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = 31 * result + ((getCreatedate() == null) ? 0 : getCreatedate().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = 31 * result + ((getCreditprodtype() == null) ? 0 : getCreditprodtype().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDSEQ
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<CARDSEQNO>").append(this.cardseqno).append("</CARDSEQNO>");
        sb.append("<CARDTYPE>").append(this.cardtype).append("</CARDTYPE>");
        sb.append("<STATUS>").append(this.status).append("</STATUS>");
        sb.append("<CREATEDATE>").append(this.createdate).append("</CREATEDATE>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        sb.append("<CREDITPRODTYPE>").append(this.creditprodtype).append("</CREDITPRODTYPE>");
        return sb.toString();
    }
}