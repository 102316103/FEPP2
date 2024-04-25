package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Fwderr extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FWDERR.FWDERR_SEQNO
     *
     * @mbg.generated
     */
    private Integer fwderrSeqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FWDERR.FWDERR_TX_DATE
     *
     * @mbg.generated
     */
    private String fwderrTxDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FWDERR.FWDERR_CONT
     *
     * @mbg.generated
     */
    private String fwderrCont;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FWDERR.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FWDERR.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    public Fwderr(Integer fwderrSeqno, String fwderrTxDate, String fwderrCont, Integer updateUserid, Date updateTime) {
        this.fwderrSeqno = fwderrSeqno;
        this.fwderrTxDate = fwderrTxDate;
        this.fwderrCont = fwderrCont;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    public Fwderr() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FWDERR.FWDERR_SEQNO
     *
     * @return the value of FWDERR.FWDERR_SEQNO
     *
     * @mbg.generated
     */
    public Integer getFwderrSeqno() {
        return fwderrSeqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FWDERR.FWDERR_SEQNO
     *
     * @param fwderrSeqno the value for FWDERR.FWDERR_SEQNO
     *
     * @mbg.generated
     */
    public void setFwderrSeqno(Integer fwderrSeqno) {
        this.fwderrSeqno = fwderrSeqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FWDERR.FWDERR_TX_DATE
     *
     * @return the value of FWDERR.FWDERR_TX_DATE
     *
     * @mbg.generated
     */
    public String getFwderrTxDate() {
        return fwderrTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FWDERR.FWDERR_TX_DATE
     *
     * @param fwderrTxDate the value for FWDERR.FWDERR_TX_DATE
     *
     * @mbg.generated
     */
    public void setFwderrTxDate(String fwderrTxDate) {
        this.fwderrTxDate = fwderrTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FWDERR.FWDERR_CONT
     *
     * @return the value of FWDERR.FWDERR_CONT
     *
     * @mbg.generated
     */
    public String getFwderrCont() {
        return fwderrCont;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FWDERR.FWDERR_CONT
     *
     * @param fwderrCont the value for FWDERR.FWDERR_CONT
     *
     * @mbg.generated
     */
    public void setFwderrCont(String fwderrCont) {
        this.fwderrCont = fwderrCont;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FWDERR.UPDATE_USERID
     *
     * @return the value of FWDERR.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FWDERR.UPDATE_USERID
     *
     * @param updateUserid the value for FWDERR.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FWDERR.UPDATE_TIME
     *
     * @return the value of FWDERR.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FWDERR.UPDATE_TIME
     *
     * @param updateTime the value for FWDERR.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", fwderrSeqno=").append(fwderrSeqno);
        sb.append(", fwderrTxDate=").append(fwderrTxDate);
        sb.append(", fwderrCont=").append(fwderrCont);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
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
        Fwderr other = (Fwderr) that;
        return (this.getFwderrSeqno() == null ? other.getFwderrSeqno() == null : this.getFwderrSeqno().equals(other.getFwderrSeqno()))
            && (this.getFwderrTxDate() == null ? other.getFwderrTxDate() == null : this.getFwderrTxDate().equals(other.getFwderrTxDate()))
            && (this.getFwderrCont() == null ? other.getFwderrCont() == null : this.getFwderrCont().equals(other.getFwderrCont()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getFwderrSeqno() == null) ? 0 : getFwderrSeqno().hashCode());
        result = 31 * result + ((getFwderrTxDate() == null) ? 0 : getFwderrTxDate().hashCode());
        result = 31 * result + ((getFwderrCont() == null) ? 0 : getFwderrCont().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FWDERR
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<FWDERR_SEQNO>").append(this.fwderrSeqno).append("</FWDERR_SEQNO>");
        sb.append("<FWDERR_TX_DATE>").append(this.fwderrTxDate).append("</FWDERR_TX_DATE>");
        sb.append("<FWDERR_CONT>").append(this.fwderrCont).append("</FWDERR_CONT>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}