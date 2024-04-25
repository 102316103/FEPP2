package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Gatmc extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_ID
     *
     * @mbg.generated
     */
    private Integer gatmcId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_TX_DATE
     *
     * @mbg.generated
     */
    private String gatmcTxDate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_ATMNO
     *
     * @mbg.generated
     */
    private String gatmcAtmno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_CUR
     *
     * @mbg.generated
     */
    private String gatmcCur = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_TX_CODE
     *
     * @mbg.generated
     */
    private String gatmcTxCode = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_DR_CNT
     *
     * @mbg.generated
     */
    private Integer gatmcDrCnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_DR_AMT
     *
     * @mbg.generated
     */
    private BigDecimal gatmcDrAmt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_CR_CNT
     *
     * @mbg.generated
     */
    private Integer gatmcCrCnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.GATMC_CR_AMT
     *
     * @mbg.generated
     */
    private BigDecimal gatmcCrAmt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column GATMC.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table GATMC
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GATMC
     *
     * @mbg.generated
     */
    public Gatmc(Integer gatmcId, String gatmcTxDate, String gatmcAtmno, String gatmcCur, String gatmcTxCode, Integer gatmcDrCnt, BigDecimal gatmcDrAmt, Integer gatmcCrCnt, BigDecimal gatmcCrAmt, Integer updateUserid, Date updateTime) {
        this.gatmcId = gatmcId;
        this.gatmcTxDate = gatmcTxDate;
        this.gatmcAtmno = gatmcAtmno;
        this.gatmcCur = gatmcCur;
        this.gatmcTxCode = gatmcTxCode;
        this.gatmcDrCnt = gatmcDrCnt;
        this.gatmcDrAmt = gatmcDrAmt;
        this.gatmcCrCnt = gatmcCrCnt;
        this.gatmcCrAmt = gatmcCrAmt;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GATMC
     *
     * @mbg.generated
     */
    public Gatmc() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_ID
     *
     * @return the value of GATMC.GATMC_ID
     *
     * @mbg.generated
     */
    public Integer getGatmcId() {
        return gatmcId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_ID
     *
     * @param gatmcId the value for GATMC.GATMC_ID
     *
     * @mbg.generated
     */
    public void setGatmcId(Integer gatmcId) {
        this.gatmcId = gatmcId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_TX_DATE
     *
     * @return the value of GATMC.GATMC_TX_DATE
     *
     * @mbg.generated
     */
    public String getGatmcTxDate() {
        return gatmcTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_TX_DATE
     *
     * @param gatmcTxDate the value for GATMC.GATMC_TX_DATE
     *
     * @mbg.generated
     */
    public void setGatmcTxDate(String gatmcTxDate) {
        this.gatmcTxDate = gatmcTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_ATMNO
     *
     * @return the value of GATMC.GATMC_ATMNO
     *
     * @mbg.generated
     */
    public String getGatmcAtmno() {
        return gatmcAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_ATMNO
     *
     * @param gatmcAtmno the value for GATMC.GATMC_ATMNO
     *
     * @mbg.generated
     */
    public void setGatmcAtmno(String gatmcAtmno) {
        this.gatmcAtmno = gatmcAtmno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_CUR
     *
     * @return the value of GATMC.GATMC_CUR
     *
     * @mbg.generated
     */
    public String getGatmcCur() {
        return gatmcCur;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_CUR
     *
     * @param gatmcCur the value for GATMC.GATMC_CUR
     *
     * @mbg.generated
     */
    public void setGatmcCur(String gatmcCur) {
        this.gatmcCur = gatmcCur;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_TX_CODE
     *
     * @return the value of GATMC.GATMC_TX_CODE
     *
     * @mbg.generated
     */
    public String getGatmcTxCode() {
        return gatmcTxCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_TX_CODE
     *
     * @param gatmcTxCode the value for GATMC.GATMC_TX_CODE
     *
     * @mbg.generated
     */
    public void setGatmcTxCode(String gatmcTxCode) {
        this.gatmcTxCode = gatmcTxCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_DR_CNT
     *
     * @return the value of GATMC.GATMC_DR_CNT
     *
     * @mbg.generated
     */
    public Integer getGatmcDrCnt() {
        return gatmcDrCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_DR_CNT
     *
     * @param gatmcDrCnt the value for GATMC.GATMC_DR_CNT
     *
     * @mbg.generated
     */
    public void setGatmcDrCnt(Integer gatmcDrCnt) {
        this.gatmcDrCnt = gatmcDrCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_DR_AMT
     *
     * @return the value of GATMC.GATMC_DR_AMT
     *
     * @mbg.generated
     */
    public BigDecimal getGatmcDrAmt() {
        return gatmcDrAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_DR_AMT
     *
     * @param gatmcDrAmt the value for GATMC.GATMC_DR_AMT
     *
     * @mbg.generated
     */
    public void setGatmcDrAmt(BigDecimal gatmcDrAmt) {
        this.gatmcDrAmt = gatmcDrAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_CR_CNT
     *
     * @return the value of GATMC.GATMC_CR_CNT
     *
     * @mbg.generated
     */
    public Integer getGatmcCrCnt() {
        return gatmcCrCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_CR_CNT
     *
     * @param gatmcCrCnt the value for GATMC.GATMC_CR_CNT
     *
     * @mbg.generated
     */
    public void setGatmcCrCnt(Integer gatmcCrCnt) {
        this.gatmcCrCnt = gatmcCrCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.GATMC_CR_AMT
     *
     * @return the value of GATMC.GATMC_CR_AMT
     *
     * @mbg.generated
     */
    public BigDecimal getGatmcCrAmt() {
        return gatmcCrAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.GATMC_CR_AMT
     *
     * @param gatmcCrAmt the value for GATMC.GATMC_CR_AMT
     *
     * @mbg.generated
     */
    public void setGatmcCrAmt(BigDecimal gatmcCrAmt) {
        this.gatmcCrAmt = gatmcCrAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.UPDATE_USERID
     *
     * @return the value of GATMC.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.UPDATE_USERID
     *
     * @param updateUserid the value for GATMC.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column GATMC.UPDATE_TIME
     *
     * @return the value of GATMC.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column GATMC.UPDATE_TIME
     *
     * @param updateTime the value for GATMC.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GATMC
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", gatmcId=").append(gatmcId);
        sb.append(", gatmcTxDate=").append(gatmcTxDate);
        sb.append(", gatmcAtmno=").append(gatmcAtmno);
        sb.append(", gatmcCur=").append(gatmcCur);
        sb.append(", gatmcTxCode=").append(gatmcTxCode);
        sb.append(", gatmcDrCnt=").append(gatmcDrCnt);
        sb.append(", gatmcDrAmt=").append(gatmcDrAmt);
        sb.append(", gatmcCrCnt=").append(gatmcCrCnt);
        sb.append(", gatmcCrAmt=").append(gatmcCrAmt);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GATMC
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
        Gatmc other = (Gatmc) that;
        return (this.getGatmcId() == null ? other.getGatmcId() == null : this.getGatmcId().equals(other.getGatmcId()))
            && (this.getGatmcTxDate() == null ? other.getGatmcTxDate() == null : this.getGatmcTxDate().equals(other.getGatmcTxDate()))
            && (this.getGatmcAtmno() == null ? other.getGatmcAtmno() == null : this.getGatmcAtmno().equals(other.getGatmcAtmno()))
            && (this.getGatmcCur() == null ? other.getGatmcCur() == null : this.getGatmcCur().equals(other.getGatmcCur()))
            && (this.getGatmcTxCode() == null ? other.getGatmcTxCode() == null : this.getGatmcTxCode().equals(other.getGatmcTxCode()))
            && (this.getGatmcDrCnt() == null ? other.getGatmcDrCnt() == null : this.getGatmcDrCnt().equals(other.getGatmcDrCnt()))
            && (this.getGatmcDrAmt() == null ? other.getGatmcDrAmt() == null : this.getGatmcDrAmt().equals(other.getGatmcDrAmt()))
            && (this.getGatmcCrCnt() == null ? other.getGatmcCrCnt() == null : this.getGatmcCrCnt().equals(other.getGatmcCrCnt()))
            && (this.getGatmcCrAmt() == null ? other.getGatmcCrAmt() == null : this.getGatmcCrAmt().equals(other.getGatmcCrAmt()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GATMC
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getGatmcId() == null) ? 0 : getGatmcId().hashCode());
        result = 31 * result + ((getGatmcTxDate() == null) ? 0 : getGatmcTxDate().hashCode());
        result = 31 * result + ((getGatmcAtmno() == null) ? 0 : getGatmcAtmno().hashCode());
        result = 31 * result + ((getGatmcCur() == null) ? 0 : getGatmcCur().hashCode());
        result = 31 * result + ((getGatmcTxCode() == null) ? 0 : getGatmcTxCode().hashCode());
        result = 31 * result + ((getGatmcDrCnt() == null) ? 0 : getGatmcDrCnt().hashCode());
        result = 31 * result + ((getGatmcDrAmt() == null) ? 0 : getGatmcDrAmt().hashCode());
        result = 31 * result + ((getGatmcCrCnt() == null) ? 0 : getGatmcCrCnt().hashCode());
        result = 31 * result + ((getGatmcCrAmt() == null) ? 0 : getGatmcCrAmt().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table GATMC
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<GATMC_ID>").append(this.gatmcId).append("</GATMC_ID>");
        sb.append("<GATMC_TX_DATE>").append(this.gatmcTxDate).append("</GATMC_TX_DATE>");
        sb.append("<GATMC_ATMNO>").append(this.gatmcAtmno).append("</GATMC_ATMNO>");
        sb.append("<GATMC_CUR>").append(this.gatmcCur).append("</GATMC_CUR>");
        sb.append("<GATMC_TX_CODE>").append(this.gatmcTxCode).append("</GATMC_TX_CODE>");
        sb.append("<GATMC_DR_CNT>").append(this.gatmcDrCnt).append("</GATMC_DR_CNT>");
        sb.append("<GATMC_DR_AMT>").append(this.gatmcDrAmt).append("</GATMC_DR_AMT>");
        sb.append("<GATMC_CR_CNT>").append(this.gatmcCrCnt).append("</GATMC_CR_CNT>");
        sb.append("<GATMC_CR_AMT>").append(this.gatmcCrAmt).append("</GATMC_CR_AMT>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}