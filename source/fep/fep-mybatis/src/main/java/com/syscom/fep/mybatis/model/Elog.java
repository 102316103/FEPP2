package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Elog extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_EJFNO
     *
     * @mbg.generated
     */
    private Integer elogEjfno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_TBSDY
     *
     * @mbg.generated
     */
    private String elogTbsdy = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_BRNO
     *
     * @mbg.generated
     */
    private String elogBrno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_WSNO
     *
     * @mbg.generated
     */
    private String elogWsno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_TXSEQ
     *
     * @mbg.generated
     */
    private String elogTxseq = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_TXDATE
     *
     * @mbg.generated
     */
    private String elogTxdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_TIME
     *
     * @mbg.generated
     */
    private String elogTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_MODE
     *
     * @mbg.generated
     */
    private Short elogMode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_PEND_F
     *
     * @mbg.generated
     */
    private Short elogPendF;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_TIMEOUT
     *
     * @mbg.generated
     */
    private Short elogTimeout;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_RESEND
     *
     * @mbg.generated
     */
    private Short elogResend;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_ALIAS
     *
     * @mbg.generated
     */
    private String elogAlias;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_TXCD
     *
     * @mbg.generated
     */
    private String elogTxcd;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_IPCIN
     *
     * @mbg.generated
     */
    private String elogIpcin;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_RCV
     *
     * @mbg.generated
     */
    private String elogRcv;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_IPCOUT
     *
     * @mbg.generated
     */
    private String elogIpcout;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.ELOG_SEND
     *
     * @mbg.generated
     */
    private String elogSend;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ELOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table ELOG
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ELOG
     *
     * @mbg.generated
     */
    public Elog(Integer elogEjfno, String elogTbsdy, String elogBrno, String elogWsno, String elogTxseq, String elogTxdate, String elogTime, Short elogMode, Short elogPendF, Short elogTimeout, Short elogResend, String elogAlias, String elogTxcd, String elogIpcin, String elogRcv, String elogIpcout, String elogSend, Integer updateUserid, Date updateTime) {
        this.elogEjfno = elogEjfno;
        this.elogTbsdy = elogTbsdy;
        this.elogBrno = elogBrno;
        this.elogWsno = elogWsno;
        this.elogTxseq = elogTxseq;
        this.elogTxdate = elogTxdate;
        this.elogTime = elogTime;
        this.elogMode = elogMode;
        this.elogPendF = elogPendF;
        this.elogTimeout = elogTimeout;
        this.elogResend = elogResend;
        this.elogAlias = elogAlias;
        this.elogTxcd = elogTxcd;
        this.elogIpcin = elogIpcin;
        this.elogRcv = elogRcv;
        this.elogIpcout = elogIpcout;
        this.elogSend = elogSend;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ELOG
     *
     * @mbg.generated
     */
    public Elog() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_EJFNO
     *
     * @return the value of ELOG.ELOG_EJFNO
     *
     * @mbg.generated
     */
    public Integer getElogEjfno() {
        return elogEjfno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_EJFNO
     *
     * @param elogEjfno the value for ELOG.ELOG_EJFNO
     *
     * @mbg.generated
     */
    public void setElogEjfno(Integer elogEjfno) {
        this.elogEjfno = elogEjfno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_TBSDY
     *
     * @return the value of ELOG.ELOG_TBSDY
     *
     * @mbg.generated
     */
    public String getElogTbsdy() {
        return elogTbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_TBSDY
     *
     * @param elogTbsdy the value for ELOG.ELOG_TBSDY
     *
     * @mbg.generated
     */
    public void setElogTbsdy(String elogTbsdy) {
        this.elogTbsdy = elogTbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_BRNO
     *
     * @return the value of ELOG.ELOG_BRNO
     *
     * @mbg.generated
     */
    public String getElogBrno() {
        return elogBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_BRNO
     *
     * @param elogBrno the value for ELOG.ELOG_BRNO
     *
     * @mbg.generated
     */
    public void setElogBrno(String elogBrno) {
        this.elogBrno = elogBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_WSNO
     *
     * @return the value of ELOG.ELOG_WSNO
     *
     * @mbg.generated
     */
    public String getElogWsno() {
        return elogWsno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_WSNO
     *
     * @param elogWsno the value for ELOG.ELOG_WSNO
     *
     * @mbg.generated
     */
    public void setElogWsno(String elogWsno) {
        this.elogWsno = elogWsno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_TXSEQ
     *
     * @return the value of ELOG.ELOG_TXSEQ
     *
     * @mbg.generated
     */
    public String getElogTxseq() {
        return elogTxseq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_TXSEQ
     *
     * @param elogTxseq the value for ELOG.ELOG_TXSEQ
     *
     * @mbg.generated
     */
    public void setElogTxseq(String elogTxseq) {
        this.elogTxseq = elogTxseq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_TXDATE
     *
     * @return the value of ELOG.ELOG_TXDATE
     *
     * @mbg.generated
     */
    public String getElogTxdate() {
        return elogTxdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_TXDATE
     *
     * @param elogTxdate the value for ELOG.ELOG_TXDATE
     *
     * @mbg.generated
     */
    public void setElogTxdate(String elogTxdate) {
        this.elogTxdate = elogTxdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_TIME
     *
     * @return the value of ELOG.ELOG_TIME
     *
     * @mbg.generated
     */
    public String getElogTime() {
        return elogTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_TIME
     *
     * @param elogTime the value for ELOG.ELOG_TIME
     *
     * @mbg.generated
     */
    public void setElogTime(String elogTime) {
        this.elogTime = elogTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_MODE
     *
     * @return the value of ELOG.ELOG_MODE
     *
     * @mbg.generated
     */
    public Short getElogMode() {
        return elogMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_MODE
     *
     * @param elogMode the value for ELOG.ELOG_MODE
     *
     * @mbg.generated
     */
    public void setElogMode(Short elogMode) {
        this.elogMode = elogMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_PEND_F
     *
     * @return the value of ELOG.ELOG_PEND_F
     *
     * @mbg.generated
     */
    public Short getElogPendF() {
        return elogPendF;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_PEND_F
     *
     * @param elogPendF the value for ELOG.ELOG_PEND_F
     *
     * @mbg.generated
     */
    public void setElogPendF(Short elogPendF) {
        this.elogPendF = elogPendF;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_TIMEOUT
     *
     * @return the value of ELOG.ELOG_TIMEOUT
     *
     * @mbg.generated
     */
    public Short getElogTimeout() {
        return elogTimeout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_TIMEOUT
     *
     * @param elogTimeout the value for ELOG.ELOG_TIMEOUT
     *
     * @mbg.generated
     */
    public void setElogTimeout(Short elogTimeout) {
        this.elogTimeout = elogTimeout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_RESEND
     *
     * @return the value of ELOG.ELOG_RESEND
     *
     * @mbg.generated
     */
    public Short getElogResend() {
        return elogResend;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_RESEND
     *
     * @param elogResend the value for ELOG.ELOG_RESEND
     *
     * @mbg.generated
     */
    public void setElogResend(Short elogResend) {
        this.elogResend = elogResend;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_ALIAS
     *
     * @return the value of ELOG.ELOG_ALIAS
     *
     * @mbg.generated
     */
    public String getElogAlias() {
        return elogAlias;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_ALIAS
     *
     * @param elogAlias the value for ELOG.ELOG_ALIAS
     *
     * @mbg.generated
     */
    public void setElogAlias(String elogAlias) {
        this.elogAlias = elogAlias;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_TXCD
     *
     * @return the value of ELOG.ELOG_TXCD
     *
     * @mbg.generated
     */
    public String getElogTxcd() {
        return elogTxcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_TXCD
     *
     * @param elogTxcd the value for ELOG.ELOG_TXCD
     *
     * @mbg.generated
     */
    public void setElogTxcd(String elogTxcd) {
        this.elogTxcd = elogTxcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_IPCIN
     *
     * @return the value of ELOG.ELOG_IPCIN
     *
     * @mbg.generated
     */
    public String getElogIpcin() {
        return elogIpcin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_IPCIN
     *
     * @param elogIpcin the value for ELOG.ELOG_IPCIN
     *
     * @mbg.generated
     */
    public void setElogIpcin(String elogIpcin) {
        this.elogIpcin = elogIpcin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_RCV
     *
     * @return the value of ELOG.ELOG_RCV
     *
     * @mbg.generated
     */
    public String getElogRcv() {
        return elogRcv;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_RCV
     *
     * @param elogRcv the value for ELOG.ELOG_RCV
     *
     * @mbg.generated
     */
    public void setElogRcv(String elogRcv) {
        this.elogRcv = elogRcv;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_IPCOUT
     *
     * @return the value of ELOG.ELOG_IPCOUT
     *
     * @mbg.generated
     */
    public String getElogIpcout() {
        return elogIpcout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_IPCOUT
     *
     * @param elogIpcout the value for ELOG.ELOG_IPCOUT
     *
     * @mbg.generated
     */
    public void setElogIpcout(String elogIpcout) {
        this.elogIpcout = elogIpcout;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.ELOG_SEND
     *
     * @return the value of ELOG.ELOG_SEND
     *
     * @mbg.generated
     */
    public String getElogSend() {
        return elogSend;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.ELOG_SEND
     *
     * @param elogSend the value for ELOG.ELOG_SEND
     *
     * @mbg.generated
     */
    public void setElogSend(String elogSend) {
        this.elogSend = elogSend;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.UPDATE_USERID
     *
     * @return the value of ELOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.UPDATE_USERID
     *
     * @param updateUserid the value for ELOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ELOG.UPDATE_TIME
     *
     * @return the value of ELOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ELOG.UPDATE_TIME
     *
     * @param updateTime the value for ELOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ELOG
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", elogEjfno=").append(elogEjfno);
        sb.append(", elogTbsdy=").append(elogTbsdy);
        sb.append(", elogBrno=").append(elogBrno);
        sb.append(", elogWsno=").append(elogWsno);
        sb.append(", elogTxseq=").append(elogTxseq);
        sb.append(", elogTxdate=").append(elogTxdate);
        sb.append(", elogTime=").append(elogTime);
        sb.append(", elogMode=").append(elogMode);
        sb.append(", elogPendF=").append(elogPendF);
        sb.append(", elogTimeout=").append(elogTimeout);
        sb.append(", elogResend=").append(elogResend);
        sb.append(", elogAlias=").append(elogAlias);
        sb.append(", elogTxcd=").append(elogTxcd);
        sb.append(", elogIpcin=").append(elogIpcin);
        sb.append(", elogRcv=").append(elogRcv);
        sb.append(", elogIpcout=").append(elogIpcout);
        sb.append(", elogSend=").append(elogSend);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ELOG
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
        Elog other = (Elog) that;
        return (this.getElogEjfno() == null ? other.getElogEjfno() == null : this.getElogEjfno().equals(other.getElogEjfno()))
            && (this.getElogTbsdy() == null ? other.getElogTbsdy() == null : this.getElogTbsdy().equals(other.getElogTbsdy()))
            && (this.getElogBrno() == null ? other.getElogBrno() == null : this.getElogBrno().equals(other.getElogBrno()))
            && (this.getElogWsno() == null ? other.getElogWsno() == null : this.getElogWsno().equals(other.getElogWsno()))
            && (this.getElogTxseq() == null ? other.getElogTxseq() == null : this.getElogTxseq().equals(other.getElogTxseq()))
            && (this.getElogTxdate() == null ? other.getElogTxdate() == null : this.getElogTxdate().equals(other.getElogTxdate()))
            && (this.getElogTime() == null ? other.getElogTime() == null : this.getElogTime().equals(other.getElogTime()))
            && (this.getElogMode() == null ? other.getElogMode() == null : this.getElogMode().equals(other.getElogMode()))
            && (this.getElogPendF() == null ? other.getElogPendF() == null : this.getElogPendF().equals(other.getElogPendF()))
            && (this.getElogTimeout() == null ? other.getElogTimeout() == null : this.getElogTimeout().equals(other.getElogTimeout()))
            && (this.getElogResend() == null ? other.getElogResend() == null : this.getElogResend().equals(other.getElogResend()))
            && (this.getElogAlias() == null ? other.getElogAlias() == null : this.getElogAlias().equals(other.getElogAlias()))
            && (this.getElogTxcd() == null ? other.getElogTxcd() == null : this.getElogTxcd().equals(other.getElogTxcd()))
            && (this.getElogIpcin() == null ? other.getElogIpcin() == null : this.getElogIpcin().equals(other.getElogIpcin()))
            && (this.getElogRcv() == null ? other.getElogRcv() == null : this.getElogRcv().equals(other.getElogRcv()))
            && (this.getElogIpcout() == null ? other.getElogIpcout() == null : this.getElogIpcout().equals(other.getElogIpcout()))
            && (this.getElogSend() == null ? other.getElogSend() == null : this.getElogSend().equals(other.getElogSend()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ELOG
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getElogEjfno() == null) ? 0 : getElogEjfno().hashCode());
        result = 31 * result + ((getElogTbsdy() == null) ? 0 : getElogTbsdy().hashCode());
        result = 31 * result + ((getElogBrno() == null) ? 0 : getElogBrno().hashCode());
        result = 31 * result + ((getElogWsno() == null) ? 0 : getElogWsno().hashCode());
        result = 31 * result + ((getElogTxseq() == null) ? 0 : getElogTxseq().hashCode());
        result = 31 * result + ((getElogTxdate() == null) ? 0 : getElogTxdate().hashCode());
        result = 31 * result + ((getElogTime() == null) ? 0 : getElogTime().hashCode());
        result = 31 * result + ((getElogMode() == null) ? 0 : getElogMode().hashCode());
        result = 31 * result + ((getElogPendF() == null) ? 0 : getElogPendF().hashCode());
        result = 31 * result + ((getElogTimeout() == null) ? 0 : getElogTimeout().hashCode());
        result = 31 * result + ((getElogResend() == null) ? 0 : getElogResend().hashCode());
        result = 31 * result + ((getElogAlias() == null) ? 0 : getElogAlias().hashCode());
        result = 31 * result + ((getElogTxcd() == null) ? 0 : getElogTxcd().hashCode());
        result = 31 * result + ((getElogIpcin() == null) ? 0 : getElogIpcin().hashCode());
        result = 31 * result + ((getElogRcv() == null) ? 0 : getElogRcv().hashCode());
        result = 31 * result + ((getElogIpcout() == null) ? 0 : getElogIpcout().hashCode());
        result = 31 * result + ((getElogSend() == null) ? 0 : getElogSend().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ELOG
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ELOG_EJFNO>").append(this.elogEjfno).append("</ELOG_EJFNO>");
        sb.append("<ELOG_TBSDY>").append(this.elogTbsdy).append("</ELOG_TBSDY>");
        sb.append("<ELOG_BRNO>").append(this.elogBrno).append("</ELOG_BRNO>");
        sb.append("<ELOG_WSNO>").append(this.elogWsno).append("</ELOG_WSNO>");
        sb.append("<ELOG_TXSEQ>").append(this.elogTxseq).append("</ELOG_TXSEQ>");
        sb.append("<ELOG_TXDATE>").append(this.elogTxdate).append("</ELOG_TXDATE>");
        sb.append("<ELOG_TIME>").append(this.elogTime).append("</ELOG_TIME>");
        sb.append("<ELOG_MODE>").append(this.elogMode).append("</ELOG_MODE>");
        sb.append("<ELOG_PEND_F>").append(this.elogPendF).append("</ELOG_PEND_F>");
        sb.append("<ELOG_TIMEOUT>").append(this.elogTimeout).append("</ELOG_TIMEOUT>");
        sb.append("<ELOG_RESEND>").append(this.elogResend).append("</ELOG_RESEND>");
        sb.append("<ELOG_ALIAS>").append(this.elogAlias).append("</ELOG_ALIAS>");
        sb.append("<ELOG_TXCD>").append(this.elogTxcd).append("</ELOG_TXCD>");
        sb.append("<ELOG_IPCIN>").append(this.elogIpcin).append("</ELOG_IPCIN>");
        sb.append("<ELOG_RCV>").append(this.elogRcv).append("</ELOG_RCV>");
        sb.append("<ELOG_IPCOUT>").append(this.elogIpcout).append("</ELOG_IPCOUT>");
        sb.append("<ELOG_SEND>").append(this.elogSend).append("</ELOG_SEND>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}