package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Clrdtltxn extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_TXDATE
     *
     * @mbg.generated
     */
    private String clrdtltxnTxdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_EJFNO
     *
     * @mbg.generated
     */
    private Integer clrdtltxnEjfno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_AP_ID
     *
     * @mbg.generated
     */
    private String clrdtltxnApId = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_PAYTYPE
     *
     * @mbg.generated
     */
    private String clrdtltxnPaytype = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_TIME
     *
     * @mbg.generated
     */
    private String clrdtltxnTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_STAN
     *
     * @mbg.generated
     */
    private String clrdtltxnStan;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_PRE_FUND
     *
     * @mbg.generated
     */
    private BigDecimal clrdtltxnPreFund;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_USE_BAL
     *
     * @mbg.generated
     */
    private BigDecimal clrdtltxnUseBal;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_TOT_DBCNT
     *
     * @mbg.generated
     */
    private Integer clrdtltxnTotDbcnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_TOT_DBAMT
     *
     * @mbg.generated
     */
    private BigDecimal clrdtltxnTotDbamt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_TOT_CRCNT
     *
     * @mbg.generated
     */
    private Integer clrdtltxnTotCrcnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_TOT_CRAMT
     *
     * @mbg.generated
     */
    private BigDecimal clrdtltxnTotCramt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_FEE_DBAMT
     *
     * @mbg.generated
     */
    private BigDecimal clrdtltxnFeeDbamt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_FEE_CRAMT
     *
     * @mbg.generated
     */
    private BigDecimal clrdtltxnFeeCramt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.CLRDTLTXN_RMSTAT
     *
     * @mbg.generated
     */
    private String clrdtltxnRmstat;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    public Clrdtltxn(String clrdtltxnTxdate, Integer clrdtltxnEjfno, String clrdtltxnApId, String clrdtltxnPaytype, String clrdtltxnTime, String clrdtltxnStan, BigDecimal clrdtltxnPreFund, BigDecimal clrdtltxnUseBal, Integer clrdtltxnTotDbcnt, BigDecimal clrdtltxnTotDbamt, Integer clrdtltxnTotCrcnt, BigDecimal clrdtltxnTotCramt, BigDecimal clrdtltxnFeeDbamt, BigDecimal clrdtltxnFeeCramt, String clrdtltxnRmstat, Integer updateUserid, Date updateTime) {
        this.clrdtltxnTxdate = clrdtltxnTxdate;
        this.clrdtltxnEjfno = clrdtltxnEjfno;
        this.clrdtltxnApId = clrdtltxnApId;
        this.clrdtltxnPaytype = clrdtltxnPaytype;
        this.clrdtltxnTime = clrdtltxnTime;
        this.clrdtltxnStan = clrdtltxnStan;
        this.clrdtltxnPreFund = clrdtltxnPreFund;
        this.clrdtltxnUseBal = clrdtltxnUseBal;
        this.clrdtltxnTotDbcnt = clrdtltxnTotDbcnt;
        this.clrdtltxnTotDbamt = clrdtltxnTotDbamt;
        this.clrdtltxnTotCrcnt = clrdtltxnTotCrcnt;
        this.clrdtltxnTotCramt = clrdtltxnTotCramt;
        this.clrdtltxnFeeDbamt = clrdtltxnFeeDbamt;
        this.clrdtltxnFeeCramt = clrdtltxnFeeCramt;
        this.clrdtltxnRmstat = clrdtltxnRmstat;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    public Clrdtltxn() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_TXDATE
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_TXDATE
     *
     * @mbg.generated
     */
    public String getClrdtltxnTxdate() {
        return clrdtltxnTxdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_TXDATE
     *
     * @param clrdtltxnTxdate the value for CLRDTLTXN.CLRDTLTXN_TXDATE
     *
     * @mbg.generated
     */
    public void setClrdtltxnTxdate(String clrdtltxnTxdate) {
        this.clrdtltxnTxdate = clrdtltxnTxdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_EJFNO
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_EJFNO
     *
     * @mbg.generated
     */
    public Integer getClrdtltxnEjfno() {
        return clrdtltxnEjfno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_EJFNO
     *
     * @param clrdtltxnEjfno the value for CLRDTLTXN.CLRDTLTXN_EJFNO
     *
     * @mbg.generated
     */
    public void setClrdtltxnEjfno(Integer clrdtltxnEjfno) {
        this.clrdtltxnEjfno = clrdtltxnEjfno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_AP_ID
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_AP_ID
     *
     * @mbg.generated
     */
    public String getClrdtltxnApId() {
        return clrdtltxnApId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_AP_ID
     *
     * @param clrdtltxnApId the value for CLRDTLTXN.CLRDTLTXN_AP_ID
     *
     * @mbg.generated
     */
    public void setClrdtltxnApId(String clrdtltxnApId) {
        this.clrdtltxnApId = clrdtltxnApId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_PAYTYPE
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_PAYTYPE
     *
     * @mbg.generated
     */
    public String getClrdtltxnPaytype() {
        return clrdtltxnPaytype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_PAYTYPE
     *
     * @param clrdtltxnPaytype the value for CLRDTLTXN.CLRDTLTXN_PAYTYPE
     *
     * @mbg.generated
     */
    public void setClrdtltxnPaytype(String clrdtltxnPaytype) {
        this.clrdtltxnPaytype = clrdtltxnPaytype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_TIME
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_TIME
     *
     * @mbg.generated
     */
    public String getClrdtltxnTime() {
        return clrdtltxnTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_TIME
     *
     * @param clrdtltxnTime the value for CLRDTLTXN.CLRDTLTXN_TIME
     *
     * @mbg.generated
     */
    public void setClrdtltxnTime(String clrdtltxnTime) {
        this.clrdtltxnTime = clrdtltxnTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_STAN
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_STAN
     *
     * @mbg.generated
     */
    public String getClrdtltxnStan() {
        return clrdtltxnStan;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_STAN
     *
     * @param clrdtltxnStan the value for CLRDTLTXN.CLRDTLTXN_STAN
     *
     * @mbg.generated
     */
    public void setClrdtltxnStan(String clrdtltxnStan) {
        this.clrdtltxnStan = clrdtltxnStan;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_PRE_FUND
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_PRE_FUND
     *
     * @mbg.generated
     */
    public BigDecimal getClrdtltxnPreFund() {
        return clrdtltxnPreFund;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_PRE_FUND
     *
     * @param clrdtltxnPreFund the value for CLRDTLTXN.CLRDTLTXN_PRE_FUND
     *
     * @mbg.generated
     */
    public void setClrdtltxnPreFund(BigDecimal clrdtltxnPreFund) {
        this.clrdtltxnPreFund = clrdtltxnPreFund;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_USE_BAL
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_USE_BAL
     *
     * @mbg.generated
     */
    public BigDecimal getClrdtltxnUseBal() {
        return clrdtltxnUseBal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_USE_BAL
     *
     * @param clrdtltxnUseBal the value for CLRDTLTXN.CLRDTLTXN_USE_BAL
     *
     * @mbg.generated
     */
    public void setClrdtltxnUseBal(BigDecimal clrdtltxnUseBal) {
        this.clrdtltxnUseBal = clrdtltxnUseBal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_DBCNT
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_TOT_DBCNT
     *
     * @mbg.generated
     */
    public Integer getClrdtltxnTotDbcnt() {
        return clrdtltxnTotDbcnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_DBCNT
     *
     * @param clrdtltxnTotDbcnt the value for CLRDTLTXN.CLRDTLTXN_TOT_DBCNT
     *
     * @mbg.generated
     */
    public void setClrdtltxnTotDbcnt(Integer clrdtltxnTotDbcnt) {
        this.clrdtltxnTotDbcnt = clrdtltxnTotDbcnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_DBAMT
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_TOT_DBAMT
     *
     * @mbg.generated
     */
    public BigDecimal getClrdtltxnTotDbamt() {
        return clrdtltxnTotDbamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_DBAMT
     *
     * @param clrdtltxnTotDbamt the value for CLRDTLTXN.CLRDTLTXN_TOT_DBAMT
     *
     * @mbg.generated
     */
    public void setClrdtltxnTotDbamt(BigDecimal clrdtltxnTotDbamt) {
        this.clrdtltxnTotDbamt = clrdtltxnTotDbamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_CRCNT
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_TOT_CRCNT
     *
     * @mbg.generated
     */
    public Integer getClrdtltxnTotCrcnt() {
        return clrdtltxnTotCrcnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_CRCNT
     *
     * @param clrdtltxnTotCrcnt the value for CLRDTLTXN.CLRDTLTXN_TOT_CRCNT
     *
     * @mbg.generated
     */
    public void setClrdtltxnTotCrcnt(Integer clrdtltxnTotCrcnt) {
        this.clrdtltxnTotCrcnt = clrdtltxnTotCrcnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_CRAMT
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_TOT_CRAMT
     *
     * @mbg.generated
     */
    public BigDecimal getClrdtltxnTotCramt() {
        return clrdtltxnTotCramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_TOT_CRAMT
     *
     * @param clrdtltxnTotCramt the value for CLRDTLTXN.CLRDTLTXN_TOT_CRAMT
     *
     * @mbg.generated
     */
    public void setClrdtltxnTotCramt(BigDecimal clrdtltxnTotCramt) {
        this.clrdtltxnTotCramt = clrdtltxnTotCramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_FEE_DBAMT
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_FEE_DBAMT
     *
     * @mbg.generated
     */
    public BigDecimal getClrdtltxnFeeDbamt() {
        return clrdtltxnFeeDbamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_FEE_DBAMT
     *
     * @param clrdtltxnFeeDbamt the value for CLRDTLTXN.CLRDTLTXN_FEE_DBAMT
     *
     * @mbg.generated
     */
    public void setClrdtltxnFeeDbamt(BigDecimal clrdtltxnFeeDbamt) {
        this.clrdtltxnFeeDbamt = clrdtltxnFeeDbamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_FEE_CRAMT
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_FEE_CRAMT
     *
     * @mbg.generated
     */
    public BigDecimal getClrdtltxnFeeCramt() {
        return clrdtltxnFeeCramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_FEE_CRAMT
     *
     * @param clrdtltxnFeeCramt the value for CLRDTLTXN.CLRDTLTXN_FEE_CRAMT
     *
     * @mbg.generated
     */
    public void setClrdtltxnFeeCramt(BigDecimal clrdtltxnFeeCramt) {
        this.clrdtltxnFeeCramt = clrdtltxnFeeCramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.CLRDTLTXN_RMSTAT
     *
     * @return the value of CLRDTLTXN.CLRDTLTXN_RMSTAT
     *
     * @mbg.generated
     */
    public String getClrdtltxnRmstat() {
        return clrdtltxnRmstat;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.CLRDTLTXN_RMSTAT
     *
     * @param clrdtltxnRmstat the value for CLRDTLTXN.CLRDTLTXN_RMSTAT
     *
     * @mbg.generated
     */
    public void setClrdtltxnRmstat(String clrdtltxnRmstat) {
        this.clrdtltxnRmstat = clrdtltxnRmstat;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.UPDATE_USERID
     *
     * @return the value of CLRDTLTXN.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.UPDATE_USERID
     *
     * @param updateUserid the value for CLRDTLTXN.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN.UPDATE_TIME
     *
     * @return the value of CLRDTLTXN.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN.UPDATE_TIME
     *
     * @param updateTime the value for CLRDTLTXN.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", clrdtltxnTxdate=").append(clrdtltxnTxdate);
        sb.append(", clrdtltxnEjfno=").append(clrdtltxnEjfno);
        sb.append(", clrdtltxnApId=").append(clrdtltxnApId);
        sb.append(", clrdtltxnPaytype=").append(clrdtltxnPaytype);
        sb.append(", clrdtltxnTime=").append(clrdtltxnTime);
        sb.append(", clrdtltxnStan=").append(clrdtltxnStan);
        sb.append(", clrdtltxnPreFund=").append(clrdtltxnPreFund);
        sb.append(", clrdtltxnUseBal=").append(clrdtltxnUseBal);
        sb.append(", clrdtltxnTotDbcnt=").append(clrdtltxnTotDbcnt);
        sb.append(", clrdtltxnTotDbamt=").append(clrdtltxnTotDbamt);
        sb.append(", clrdtltxnTotCrcnt=").append(clrdtltxnTotCrcnt);
        sb.append(", clrdtltxnTotCramt=").append(clrdtltxnTotCramt);
        sb.append(", clrdtltxnFeeDbamt=").append(clrdtltxnFeeDbamt);
        sb.append(", clrdtltxnFeeCramt=").append(clrdtltxnFeeCramt);
        sb.append(", clrdtltxnRmstat=").append(clrdtltxnRmstat);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
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
        Clrdtltxn other = (Clrdtltxn) that;
        return (this.getClrdtltxnTxdate() == null ? other.getClrdtltxnTxdate() == null : this.getClrdtltxnTxdate().equals(other.getClrdtltxnTxdate()))
            && (this.getClrdtltxnEjfno() == null ? other.getClrdtltxnEjfno() == null : this.getClrdtltxnEjfno().equals(other.getClrdtltxnEjfno()))
            && (this.getClrdtltxnApId() == null ? other.getClrdtltxnApId() == null : this.getClrdtltxnApId().equals(other.getClrdtltxnApId()))
            && (this.getClrdtltxnPaytype() == null ? other.getClrdtltxnPaytype() == null : this.getClrdtltxnPaytype().equals(other.getClrdtltxnPaytype()))
            && (this.getClrdtltxnTime() == null ? other.getClrdtltxnTime() == null : this.getClrdtltxnTime().equals(other.getClrdtltxnTime()))
            && (this.getClrdtltxnStan() == null ? other.getClrdtltxnStan() == null : this.getClrdtltxnStan().equals(other.getClrdtltxnStan()))
            && (this.getClrdtltxnPreFund() == null ? other.getClrdtltxnPreFund() == null : this.getClrdtltxnPreFund().equals(other.getClrdtltxnPreFund()))
            && (this.getClrdtltxnUseBal() == null ? other.getClrdtltxnUseBal() == null : this.getClrdtltxnUseBal().equals(other.getClrdtltxnUseBal()))
            && (this.getClrdtltxnTotDbcnt() == null ? other.getClrdtltxnTotDbcnt() == null : this.getClrdtltxnTotDbcnt().equals(other.getClrdtltxnTotDbcnt()))
            && (this.getClrdtltxnTotDbamt() == null ? other.getClrdtltxnTotDbamt() == null : this.getClrdtltxnTotDbamt().equals(other.getClrdtltxnTotDbamt()))
            && (this.getClrdtltxnTotCrcnt() == null ? other.getClrdtltxnTotCrcnt() == null : this.getClrdtltxnTotCrcnt().equals(other.getClrdtltxnTotCrcnt()))
            && (this.getClrdtltxnTotCramt() == null ? other.getClrdtltxnTotCramt() == null : this.getClrdtltxnTotCramt().equals(other.getClrdtltxnTotCramt()))
            && (this.getClrdtltxnFeeDbamt() == null ? other.getClrdtltxnFeeDbamt() == null : this.getClrdtltxnFeeDbamt().equals(other.getClrdtltxnFeeDbamt()))
            && (this.getClrdtltxnFeeCramt() == null ? other.getClrdtltxnFeeCramt() == null : this.getClrdtltxnFeeCramt().equals(other.getClrdtltxnFeeCramt()))
            && (this.getClrdtltxnRmstat() == null ? other.getClrdtltxnRmstat() == null : this.getClrdtltxnRmstat().equals(other.getClrdtltxnRmstat()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getClrdtltxnTxdate() == null) ? 0 : getClrdtltxnTxdate().hashCode());
        result = 31 * result + ((getClrdtltxnEjfno() == null) ? 0 : getClrdtltxnEjfno().hashCode());
        result = 31 * result + ((getClrdtltxnApId() == null) ? 0 : getClrdtltxnApId().hashCode());
        result = 31 * result + ((getClrdtltxnPaytype() == null) ? 0 : getClrdtltxnPaytype().hashCode());
        result = 31 * result + ((getClrdtltxnTime() == null) ? 0 : getClrdtltxnTime().hashCode());
        result = 31 * result + ((getClrdtltxnStan() == null) ? 0 : getClrdtltxnStan().hashCode());
        result = 31 * result + ((getClrdtltxnPreFund() == null) ? 0 : getClrdtltxnPreFund().hashCode());
        result = 31 * result + ((getClrdtltxnUseBal() == null) ? 0 : getClrdtltxnUseBal().hashCode());
        result = 31 * result + ((getClrdtltxnTotDbcnt() == null) ? 0 : getClrdtltxnTotDbcnt().hashCode());
        result = 31 * result + ((getClrdtltxnTotDbamt() == null) ? 0 : getClrdtltxnTotDbamt().hashCode());
        result = 31 * result + ((getClrdtltxnTotCrcnt() == null) ? 0 : getClrdtltxnTotCrcnt().hashCode());
        result = 31 * result + ((getClrdtltxnTotCramt() == null) ? 0 : getClrdtltxnTotCramt().hashCode());
        result = 31 * result + ((getClrdtltxnFeeDbamt() == null) ? 0 : getClrdtltxnFeeDbamt().hashCode());
        result = 31 * result + ((getClrdtltxnFeeCramt() == null) ? 0 : getClrdtltxnFeeCramt().hashCode());
        result = 31 * result + ((getClrdtltxnRmstat() == null) ? 0 : getClrdtltxnRmstat().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<CLRDTLTXN_TXDATE>").append(this.clrdtltxnTxdate).append("</CLRDTLTXN_TXDATE>");
        sb.append("<CLRDTLTXN_EJFNO>").append(this.clrdtltxnEjfno).append("</CLRDTLTXN_EJFNO>");
        sb.append("<CLRDTLTXN_AP_ID>").append(this.clrdtltxnApId).append("</CLRDTLTXN_AP_ID>");
        sb.append("<CLRDTLTXN_PAYTYPE>").append(this.clrdtltxnPaytype).append("</CLRDTLTXN_PAYTYPE>");
        sb.append("<CLRDTLTXN_TIME>").append(this.clrdtltxnTime).append("</CLRDTLTXN_TIME>");
        sb.append("<CLRDTLTXN_STAN>").append(this.clrdtltxnStan).append("</CLRDTLTXN_STAN>");
        sb.append("<CLRDTLTXN_PRE_FUND>").append(this.clrdtltxnPreFund).append("</CLRDTLTXN_PRE_FUND>");
        sb.append("<CLRDTLTXN_USE_BAL>").append(this.clrdtltxnUseBal).append("</CLRDTLTXN_USE_BAL>");
        sb.append("<CLRDTLTXN_TOT_DBCNT>").append(this.clrdtltxnTotDbcnt).append("</CLRDTLTXN_TOT_DBCNT>");
        sb.append("<CLRDTLTXN_TOT_DBAMT>").append(this.clrdtltxnTotDbamt).append("</CLRDTLTXN_TOT_DBAMT>");
        sb.append("<CLRDTLTXN_TOT_CRCNT>").append(this.clrdtltxnTotCrcnt).append("</CLRDTLTXN_TOT_CRCNT>");
        sb.append("<CLRDTLTXN_TOT_CRAMT>").append(this.clrdtltxnTotCramt).append("</CLRDTLTXN_TOT_CRAMT>");
        sb.append("<CLRDTLTXN_FEE_DBAMT>").append(this.clrdtltxnFeeDbamt).append("</CLRDTLTXN_FEE_DBAMT>");
        sb.append("<CLRDTLTXN_FEE_CRAMT>").append(this.clrdtltxnFeeCramt).append("</CLRDTLTXN_FEE_CRAMT>");
        sb.append("<CLRDTLTXN_RMSTAT>").append(this.clrdtltxnRmstat).append("</CLRDTLTXN_RMSTAT>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}