package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Odrc extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.ODRC_TX_DATE
     *
     * @mbg.generated
     */
    private String odrcTxDate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.ODRC_BKNO
     *
     * @mbg.generated
     */
    private String odrcBkno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.ODRC_ACTNO
     *
     * @mbg.generated
     */
    private String odrcActno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.ODRC_CURCD
     *
     * @mbg.generated
     */
    private String odrcCurcd = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.ODRC_TX_CNT
     *
     * @mbg.generated
     */
    private Integer odrcTxCnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.ODRC_TX_AMT
     *
     * @mbg.generated
     */
    private BigDecimal odrcTxAmt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ODRC.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table ODRC
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ODRC
     *
     * @mbg.generated
     */
    public Odrc(String odrcTxDate, String odrcBkno, String odrcActno, String odrcCurcd, Integer odrcTxCnt, BigDecimal odrcTxAmt, Integer updateUserid, Date updateTime) {
        this.odrcTxDate = odrcTxDate;
        this.odrcBkno = odrcBkno;
        this.odrcActno = odrcActno;
        this.odrcCurcd = odrcCurcd;
        this.odrcTxCnt = odrcTxCnt;
        this.odrcTxAmt = odrcTxAmt;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ODRC
     *
     * @mbg.generated
     */
    public Odrc() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.ODRC_TX_DATE
     *
     * @return the value of ODRC.ODRC_TX_DATE
     *
     * @mbg.generated
     */
    public String getOdrcTxDate() {
        return odrcTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.ODRC_TX_DATE
     *
     * @param odrcTxDate the value for ODRC.ODRC_TX_DATE
     *
     * @mbg.generated
     */
    public void setOdrcTxDate(String odrcTxDate) {
        this.odrcTxDate = odrcTxDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.ODRC_BKNO
     *
     * @return the value of ODRC.ODRC_BKNO
     *
     * @mbg.generated
     */
    public String getOdrcBkno() {
        return odrcBkno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.ODRC_BKNO
     *
     * @param odrcBkno the value for ODRC.ODRC_BKNO
     *
     * @mbg.generated
     */
    public void setOdrcBkno(String odrcBkno) {
        this.odrcBkno = odrcBkno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.ODRC_ACTNO
     *
     * @return the value of ODRC.ODRC_ACTNO
     *
     * @mbg.generated
     */
    public String getOdrcActno() {
        return odrcActno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.ODRC_ACTNO
     *
     * @param odrcActno the value for ODRC.ODRC_ACTNO
     *
     * @mbg.generated
     */
    public void setOdrcActno(String odrcActno) {
        this.odrcActno = odrcActno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.ODRC_CURCD
     *
     * @return the value of ODRC.ODRC_CURCD
     *
     * @mbg.generated
     */
    public String getOdrcCurcd() {
        return odrcCurcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.ODRC_CURCD
     *
     * @param odrcCurcd the value for ODRC.ODRC_CURCD
     *
     * @mbg.generated
     */
    public void setOdrcCurcd(String odrcCurcd) {
        this.odrcCurcd = odrcCurcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.ODRC_TX_CNT
     *
     * @return the value of ODRC.ODRC_TX_CNT
     *
     * @mbg.generated
     */
    public Integer getOdrcTxCnt() {
        return odrcTxCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.ODRC_TX_CNT
     *
     * @param odrcTxCnt the value for ODRC.ODRC_TX_CNT
     *
     * @mbg.generated
     */
    public void setOdrcTxCnt(Integer odrcTxCnt) {
        this.odrcTxCnt = odrcTxCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.ODRC_TX_AMT
     *
     * @return the value of ODRC.ODRC_TX_AMT
     *
     * @mbg.generated
     */
    public BigDecimal getOdrcTxAmt() {
        return odrcTxAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.ODRC_TX_AMT
     *
     * @param odrcTxAmt the value for ODRC.ODRC_TX_AMT
     *
     * @mbg.generated
     */
    public void setOdrcTxAmt(BigDecimal odrcTxAmt) {
        this.odrcTxAmt = odrcTxAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.UPDATE_USERID
     *
     * @return the value of ODRC.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.UPDATE_USERID
     *
     * @param updateUserid the value for ODRC.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ODRC.UPDATE_TIME
     *
     * @return the value of ODRC.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ODRC.UPDATE_TIME
     *
     * @param updateTime the value for ODRC.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ODRC
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", odrcTxDate=").append(odrcTxDate);
        sb.append(", odrcBkno=").append(odrcBkno);
        sb.append(", odrcActno=").append(odrcActno);
        sb.append(", odrcCurcd=").append(odrcCurcd);
        sb.append(", odrcTxCnt=").append(odrcTxCnt);
        sb.append(", odrcTxAmt=").append(odrcTxAmt);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ODRC
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
        Odrc other = (Odrc) that;
        return (this.getOdrcTxDate() == null ? other.getOdrcTxDate() == null : this.getOdrcTxDate().equals(other.getOdrcTxDate()))
            && (this.getOdrcBkno() == null ? other.getOdrcBkno() == null : this.getOdrcBkno().equals(other.getOdrcBkno()))
            && (this.getOdrcActno() == null ? other.getOdrcActno() == null : this.getOdrcActno().equals(other.getOdrcActno()))
            && (this.getOdrcCurcd() == null ? other.getOdrcCurcd() == null : this.getOdrcCurcd().equals(other.getOdrcCurcd()))
            && (this.getOdrcTxCnt() == null ? other.getOdrcTxCnt() == null : this.getOdrcTxCnt().equals(other.getOdrcTxCnt()))
            && (this.getOdrcTxAmt() == null ? other.getOdrcTxAmt() == null : this.getOdrcTxAmt().equals(other.getOdrcTxAmt()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ODRC
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getOdrcTxDate() == null) ? 0 : getOdrcTxDate().hashCode());
        result = 31 * result + ((getOdrcBkno() == null) ? 0 : getOdrcBkno().hashCode());
        result = 31 * result + ((getOdrcActno() == null) ? 0 : getOdrcActno().hashCode());
        result = 31 * result + ((getOdrcCurcd() == null) ? 0 : getOdrcCurcd().hashCode());
        result = 31 * result + ((getOdrcTxCnt() == null) ? 0 : getOdrcTxCnt().hashCode());
        result = 31 * result + ((getOdrcTxAmt() == null) ? 0 : getOdrcTxAmt().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ODRC
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ODRC_TX_DATE>").append(this.odrcTxDate).append("</ODRC_TX_DATE>");
        sb.append("<ODRC_BKNO>").append(this.odrcBkno).append("</ODRC_BKNO>");
        sb.append("<ODRC_ACTNO>").append(this.odrcActno).append("</ODRC_ACTNO>");
        sb.append("<ODRC_CURCD>").append(this.odrcCurcd).append("</ODRC_CURCD>");
        sb.append("<ODRC_TX_CNT>").append(this.odrcTxCnt).append("</ODRC_TX_CNT>");
        sb.append("<ODRC_TX_AMT>").append(this.odrcTxAmt).append("</ODRC_TX_AMT>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}