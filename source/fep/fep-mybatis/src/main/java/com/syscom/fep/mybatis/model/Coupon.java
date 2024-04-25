package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Coupon extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_SEQNO
     *
     * @mbg.generated
     */
    private Integer couponSeqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_TX_DATE
     *
     * @mbg.generated
     */
    private String couponTxDate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_TX_TIME
     *
     * @mbg.generated
     */
    private String couponTxTime = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_ATMNO
     *
     * @mbg.generated
     */
    private String couponAtmno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_SDATE
     *
     * @mbg.generated
     */
    private String couponSdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_EDATE
     *
     * @mbg.generated
     */
    private String couponEdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_FILENAME
     *
     * @mbg.generated
     */
    private String couponFilename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.COUPON_PRT_TIME
     *
     * @mbg.generated
     */
    private String couponPrtTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column COUPON.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table COUPON
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table COUPON
     *
     * @mbg.generated
     */
    public Coupon(Integer couponSeqno, String couponTxDate, String couponTxTime, String couponAtmno, String couponSdate, String couponEdate, String couponFilename, String couponPrtTime, Integer updateUserid, Date updateTime) {
        this.couponSeqno = couponSeqno;
        this.couponTxDate = couponTxDate;
        this.couponTxTime = couponTxTime;
        this.couponAtmno = couponAtmno;
        this.couponSdate = couponSdate;
        this.couponEdate = couponEdate;
        this.couponFilename = couponFilename;
        this.couponPrtTime = couponPrtTime;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table COUPON
     *
     * @mbg.generated
     */
    public Coupon() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_SEQNO
     *
     * @return the value of COUPON.COUPON_SEQNO
     *
     * @mbg.generated
     */
    public Integer getCouponSeqno() {
        return couponSeqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_SEQNO
     *
     * @param couponSeqno the value for COUPON.COUPON_SEQNO
     *
     * @mbg.generated
     */
    public void setCouponSeqno(Integer couponSeqno) {
        this.couponSeqno = couponSeqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_TX_DATE
     *
     * @return the value of COUPON.COUPON_TX_DATE
     *
     * @mbg.generated
     */
    public String getCouponTxDate() {
        return couponTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_TX_DATE
     *
     * @param couponTxDate the value for COUPON.COUPON_TX_DATE
     *
     * @mbg.generated
     */
    public void setCouponTxDate(String couponTxDate) {
        this.couponTxDate = couponTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_TX_TIME
     *
     * @return the value of COUPON.COUPON_TX_TIME
     *
     * @mbg.generated
     */
    public String getCouponTxTime() {
        return couponTxTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_TX_TIME
     *
     * @param couponTxTime the value for COUPON.COUPON_TX_TIME
     *
     * @mbg.generated
     */
    public void setCouponTxTime(String couponTxTime) {
        this.couponTxTime = couponTxTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_ATMNO
     *
     * @return the value of COUPON.COUPON_ATMNO
     *
     * @mbg.generated
     */
    public String getCouponAtmno() {
        return couponAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_ATMNO
     *
     * @param couponAtmno the value for COUPON.COUPON_ATMNO
     *
     * @mbg.generated
     */
    public void setCouponAtmno(String couponAtmno) {
        this.couponAtmno = couponAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_SDATE
     *
     * @return the value of COUPON.COUPON_SDATE
     *
     * @mbg.generated
     */
    public String getCouponSdate() {
        return couponSdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_SDATE
     *
     * @param couponSdate the value for COUPON.COUPON_SDATE
     *
     * @mbg.generated
     */
    public void setCouponSdate(String couponSdate) {
        this.couponSdate = couponSdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_EDATE
     *
     * @return the value of COUPON.COUPON_EDATE
     *
     * @mbg.generated
     */
    public String getCouponEdate() {
        return couponEdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_EDATE
     *
     * @param couponEdate the value for COUPON.COUPON_EDATE
     *
     * @mbg.generated
     */
    public void setCouponEdate(String couponEdate) {
        this.couponEdate = couponEdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_FILENAME
     *
     * @return the value of COUPON.COUPON_FILENAME
     *
     * @mbg.generated
     */
    public String getCouponFilename() {
        return couponFilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_FILENAME
     *
     * @param couponFilename the value for COUPON.COUPON_FILENAME
     *
     * @mbg.generated
     */
    public void setCouponFilename(String couponFilename) {
        this.couponFilename = couponFilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.COUPON_PRT_TIME
     *
     * @return the value of COUPON.COUPON_PRT_TIME
     *
     * @mbg.generated
     */
    public String getCouponPrtTime() {
        return couponPrtTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.COUPON_PRT_TIME
     *
     * @param couponPrtTime the value for COUPON.COUPON_PRT_TIME
     *
     * @mbg.generated
     */
    public void setCouponPrtTime(String couponPrtTime) {
        this.couponPrtTime = couponPrtTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.UPDATE_USERID
     *
     * @return the value of COUPON.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.UPDATE_USERID
     *
     * @param updateUserid the value for COUPON.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column COUPON.UPDATE_TIME
     *
     * @return the value of COUPON.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column COUPON.UPDATE_TIME
     *
     * @param updateTime the value for COUPON.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table COUPON
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", couponSeqno=").append(couponSeqno);
        sb.append(", couponTxDate=").append(couponTxDate);
        sb.append(", couponTxTime=").append(couponTxTime);
        sb.append(", couponAtmno=").append(couponAtmno);
        sb.append(", couponSdate=").append(couponSdate);
        sb.append(", couponEdate=").append(couponEdate);
        sb.append(", couponFilename=").append(couponFilename);
        sb.append(", couponPrtTime=").append(couponPrtTime);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table COUPON
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
        Coupon other = (Coupon) that;
        return (this.getCouponSeqno() == null ? other.getCouponSeqno() == null : this.getCouponSeqno().equals(other.getCouponSeqno()))
            && (this.getCouponTxDate() == null ? other.getCouponTxDate() == null : this.getCouponTxDate().equals(other.getCouponTxDate()))
            && (this.getCouponTxTime() == null ? other.getCouponTxTime() == null : this.getCouponTxTime().equals(other.getCouponTxTime()))
            && (this.getCouponAtmno() == null ? other.getCouponAtmno() == null : this.getCouponAtmno().equals(other.getCouponAtmno()))
            && (this.getCouponSdate() == null ? other.getCouponSdate() == null : this.getCouponSdate().equals(other.getCouponSdate()))
            && (this.getCouponEdate() == null ? other.getCouponEdate() == null : this.getCouponEdate().equals(other.getCouponEdate()))
            && (this.getCouponFilename() == null ? other.getCouponFilename() == null : this.getCouponFilename().equals(other.getCouponFilename()))
            && (this.getCouponPrtTime() == null ? other.getCouponPrtTime() == null : this.getCouponPrtTime().equals(other.getCouponPrtTime()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table COUPON
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getCouponSeqno() == null) ? 0 : getCouponSeqno().hashCode());
        result = 31 * result + ((getCouponTxDate() == null) ? 0 : getCouponTxDate().hashCode());
        result = 31 * result + ((getCouponTxTime() == null) ? 0 : getCouponTxTime().hashCode());
        result = 31 * result + ((getCouponAtmno() == null) ? 0 : getCouponAtmno().hashCode());
        result = 31 * result + ((getCouponSdate() == null) ? 0 : getCouponSdate().hashCode());
        result = 31 * result + ((getCouponEdate() == null) ? 0 : getCouponEdate().hashCode());
        result = 31 * result + ((getCouponFilename() == null) ? 0 : getCouponFilename().hashCode());
        result = 31 * result + ((getCouponPrtTime() == null) ? 0 : getCouponPrtTime().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table COUPON
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<COUPON_SEQNO>").append(this.couponSeqno).append("</COUPON_SEQNO>");
        sb.append("<COUPON_TX_DATE>").append(this.couponTxDate).append("</COUPON_TX_DATE>");
        sb.append("<COUPON_TX_TIME>").append(this.couponTxTime).append("</COUPON_TX_TIME>");
        sb.append("<COUPON_ATMNO>").append(this.couponAtmno).append("</COUPON_ATMNO>");
        sb.append("<COUPON_SDATE>").append(this.couponSdate).append("</COUPON_SDATE>");
        sb.append("<COUPON_EDATE>").append(this.couponEdate).append("</COUPON_EDATE>");
        sb.append("<COUPON_FILENAME>").append(this.couponFilename).append("</COUPON_FILENAME>");
        sb.append("<COUPON_PRT_TIME>").append(this.couponPrtTime).append("</COUPON_PRT_TIME>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}