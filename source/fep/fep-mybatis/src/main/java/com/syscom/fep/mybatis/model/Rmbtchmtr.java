package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Rmbtchmtr extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_SDN
     *
     * @mbg.generated
     */
    private String rmbtchmtrSdn = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_REMDATE
     *
     * @mbg.generated
     */
    private String rmbtchmtrRemdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_TIMES
     *
     * @mbg.generated
     */
    private String rmbtchmtrTimes = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_CNT
     *
     * @mbg.generated
     */
    private Integer rmbtchmtrCnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_AMT
     *
     * @mbg.generated
     */
    private BigDecimal rmbtchmtrAmt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_FAIL_CNT
     *
     * @mbg.generated
     */
    private Integer rmbtchmtrFailCnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_FAIL_AMT
     *
     * @mbg.generated
     */
    private BigDecimal rmbtchmtrFailAmt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_SUCESS_CNT
     *
     * @mbg.generated
     */
    private Integer rmbtchmtrSucessCnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_SUCESS_AMT
     *
     * @mbg.generated
     */
    private BigDecimal rmbtchmtrSucessAmt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_FLAG
     *
     * @mbg.generated
     */
    private String rmbtchmtrFlag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_SEND_CBS
     *
     * @mbg.generated
     */
    private String rmbtchmtrSendCbs = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_CBS_RC
     *
     * @mbg.generated
     */
    private String rmbtchmtrCbsRc;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.RMBTCHMTR_KINBR
     *
     * @mbg.generated
     */
    private String rmbtchmtrKinbr = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMBTCHMTR.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table RMBTCHMTR
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCHMTR
     *
     * @mbg.generated
     */
    public Rmbtchmtr(String rmbtchmtrSdn, String rmbtchmtrRemdate, String rmbtchmtrTimes, Integer rmbtchmtrCnt, BigDecimal rmbtchmtrAmt, Integer rmbtchmtrFailCnt, BigDecimal rmbtchmtrFailAmt, Integer rmbtchmtrSucessCnt, BigDecimal rmbtchmtrSucessAmt, String rmbtchmtrFlag, String rmbtchmtrSendCbs, String rmbtchmtrCbsRc, String rmbtchmtrKinbr, Integer updateUserid, Date updateTime) {
        this.rmbtchmtrSdn = rmbtchmtrSdn;
        this.rmbtchmtrRemdate = rmbtchmtrRemdate;
        this.rmbtchmtrTimes = rmbtchmtrTimes;
        this.rmbtchmtrCnt = rmbtchmtrCnt;
        this.rmbtchmtrAmt = rmbtchmtrAmt;
        this.rmbtchmtrFailCnt = rmbtchmtrFailCnt;
        this.rmbtchmtrFailAmt = rmbtchmtrFailAmt;
        this.rmbtchmtrSucessCnt = rmbtchmtrSucessCnt;
        this.rmbtchmtrSucessAmt = rmbtchmtrSucessAmt;
        this.rmbtchmtrFlag = rmbtchmtrFlag;
        this.rmbtchmtrSendCbs = rmbtchmtrSendCbs;
        this.rmbtchmtrCbsRc = rmbtchmtrCbsRc;
        this.rmbtchmtrKinbr = rmbtchmtrKinbr;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCHMTR
     *
     * @mbg.generated
     */
    public Rmbtchmtr() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_SDN
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_SDN
     *
     * @mbg.generated
     */
    public String getRmbtchmtrSdn() {
        return rmbtchmtrSdn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_SDN
     *
     * @param rmbtchmtrSdn the value for RMBTCHMTR.RMBTCHMTR_SDN
     *
     * @mbg.generated
     */
    public void setRmbtchmtrSdn(String rmbtchmtrSdn) {
        this.rmbtchmtrSdn = rmbtchmtrSdn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_REMDATE
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_REMDATE
     *
     * @mbg.generated
     */
    public String getRmbtchmtrRemdate() {
        return rmbtchmtrRemdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_REMDATE
     *
     * @param rmbtchmtrRemdate the value for RMBTCHMTR.RMBTCHMTR_REMDATE
     *
     * @mbg.generated
     */
    public void setRmbtchmtrRemdate(String rmbtchmtrRemdate) {
        this.rmbtchmtrRemdate = rmbtchmtrRemdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_TIMES
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_TIMES
     *
     * @mbg.generated
     */
    public String getRmbtchmtrTimes() {
        return rmbtchmtrTimes;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_TIMES
     *
     * @param rmbtchmtrTimes the value for RMBTCHMTR.RMBTCHMTR_TIMES
     *
     * @mbg.generated
     */
    public void setRmbtchmtrTimes(String rmbtchmtrTimes) {
        this.rmbtchmtrTimes = rmbtchmtrTimes;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_CNT
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_CNT
     *
     * @mbg.generated
     */
    public Integer getRmbtchmtrCnt() {
        return rmbtchmtrCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_CNT
     *
     * @param rmbtchmtrCnt the value for RMBTCHMTR.RMBTCHMTR_CNT
     *
     * @mbg.generated
     */
    public void setRmbtchmtrCnt(Integer rmbtchmtrCnt) {
        this.rmbtchmtrCnt = rmbtchmtrCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_AMT
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_AMT
     *
     * @mbg.generated
     */
    public BigDecimal getRmbtchmtrAmt() {
        return rmbtchmtrAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_AMT
     *
     * @param rmbtchmtrAmt the value for RMBTCHMTR.RMBTCHMTR_AMT
     *
     * @mbg.generated
     */
    public void setRmbtchmtrAmt(BigDecimal rmbtchmtrAmt) {
        this.rmbtchmtrAmt = rmbtchmtrAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_FAIL_CNT
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_FAIL_CNT
     *
     * @mbg.generated
     */
    public Integer getRmbtchmtrFailCnt() {
        return rmbtchmtrFailCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_FAIL_CNT
     *
     * @param rmbtchmtrFailCnt the value for RMBTCHMTR.RMBTCHMTR_FAIL_CNT
     *
     * @mbg.generated
     */
    public void setRmbtchmtrFailCnt(Integer rmbtchmtrFailCnt) {
        this.rmbtchmtrFailCnt = rmbtchmtrFailCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_FAIL_AMT
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_FAIL_AMT
     *
     * @mbg.generated
     */
    public BigDecimal getRmbtchmtrFailAmt() {
        return rmbtchmtrFailAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_FAIL_AMT
     *
     * @param rmbtchmtrFailAmt the value for RMBTCHMTR.RMBTCHMTR_FAIL_AMT
     *
     * @mbg.generated
     */
    public void setRmbtchmtrFailAmt(BigDecimal rmbtchmtrFailAmt) {
        this.rmbtchmtrFailAmt = rmbtchmtrFailAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_SUCESS_CNT
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_SUCESS_CNT
     *
     * @mbg.generated
     */
    public Integer getRmbtchmtrSucessCnt() {
        return rmbtchmtrSucessCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_SUCESS_CNT
     *
     * @param rmbtchmtrSucessCnt the value for RMBTCHMTR.RMBTCHMTR_SUCESS_CNT
     *
     * @mbg.generated
     */
    public void setRmbtchmtrSucessCnt(Integer rmbtchmtrSucessCnt) {
        this.rmbtchmtrSucessCnt = rmbtchmtrSucessCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_SUCESS_AMT
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_SUCESS_AMT
     *
     * @mbg.generated
     */
    public BigDecimal getRmbtchmtrSucessAmt() {
        return rmbtchmtrSucessAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_SUCESS_AMT
     *
     * @param rmbtchmtrSucessAmt the value for RMBTCHMTR.RMBTCHMTR_SUCESS_AMT
     *
     * @mbg.generated
     */
    public void setRmbtchmtrSucessAmt(BigDecimal rmbtchmtrSucessAmt) {
        this.rmbtchmtrSucessAmt = rmbtchmtrSucessAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_FLAG
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_FLAG
     *
     * @mbg.generated
     */
    public String getRmbtchmtrFlag() {
        return rmbtchmtrFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_FLAG
     *
     * @param rmbtchmtrFlag the value for RMBTCHMTR.RMBTCHMTR_FLAG
     *
     * @mbg.generated
     */
    public void setRmbtchmtrFlag(String rmbtchmtrFlag) {
        this.rmbtchmtrFlag = rmbtchmtrFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_SEND_CBS
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_SEND_CBS
     *
     * @mbg.generated
     */
    public String getRmbtchmtrSendCbs() {
        return rmbtchmtrSendCbs;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_SEND_CBS
     *
     * @param rmbtchmtrSendCbs the value for RMBTCHMTR.RMBTCHMTR_SEND_CBS
     *
     * @mbg.generated
     */
    public void setRmbtchmtrSendCbs(String rmbtchmtrSendCbs) {
        this.rmbtchmtrSendCbs = rmbtchmtrSendCbs;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_CBS_RC
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_CBS_RC
     *
     * @mbg.generated
     */
    public String getRmbtchmtrCbsRc() {
        return rmbtchmtrCbsRc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_CBS_RC
     *
     * @param rmbtchmtrCbsRc the value for RMBTCHMTR.RMBTCHMTR_CBS_RC
     *
     * @mbg.generated
     */
    public void setRmbtchmtrCbsRc(String rmbtchmtrCbsRc) {
        this.rmbtchmtrCbsRc = rmbtchmtrCbsRc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.RMBTCHMTR_KINBR
     *
     * @return the value of RMBTCHMTR.RMBTCHMTR_KINBR
     *
     * @mbg.generated
     */
    public String getRmbtchmtrKinbr() {
        return rmbtchmtrKinbr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.RMBTCHMTR_KINBR
     *
     * @param rmbtchmtrKinbr the value for RMBTCHMTR.RMBTCHMTR_KINBR
     *
     * @mbg.generated
     */
    public void setRmbtchmtrKinbr(String rmbtchmtrKinbr) {
        this.rmbtchmtrKinbr = rmbtchmtrKinbr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.UPDATE_USERID
     *
     * @return the value of RMBTCHMTR.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.UPDATE_USERID
     *
     * @param updateUserid the value for RMBTCHMTR.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMBTCHMTR.UPDATE_TIME
     *
     * @return the value of RMBTCHMTR.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMBTCHMTR.UPDATE_TIME
     *
     * @param updateTime the value for RMBTCHMTR.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCHMTR
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
        Rmbtchmtr other = (Rmbtchmtr) that;
        return (this.getRmbtchmtrSdn() == null ? other.getRmbtchmtrSdn() == null : this.getRmbtchmtrSdn().equals(other.getRmbtchmtrSdn()))
            && (this.getRmbtchmtrRemdate() == null ? other.getRmbtchmtrRemdate() == null : this.getRmbtchmtrRemdate().equals(other.getRmbtchmtrRemdate()))
            && (this.getRmbtchmtrTimes() == null ? other.getRmbtchmtrTimes() == null : this.getRmbtchmtrTimes().equals(other.getRmbtchmtrTimes()))
            && (this.getRmbtchmtrCnt() == null ? other.getRmbtchmtrCnt() == null : this.getRmbtchmtrCnt().equals(other.getRmbtchmtrCnt()))
            && (this.getRmbtchmtrAmt() == null ? other.getRmbtchmtrAmt() == null : this.getRmbtchmtrAmt().equals(other.getRmbtchmtrAmt()))
            && (this.getRmbtchmtrFailCnt() == null ? other.getRmbtchmtrFailCnt() == null : this.getRmbtchmtrFailCnt().equals(other.getRmbtchmtrFailCnt()))
            && (this.getRmbtchmtrFailAmt() == null ? other.getRmbtchmtrFailAmt() == null : this.getRmbtchmtrFailAmt().equals(other.getRmbtchmtrFailAmt()))
            && (this.getRmbtchmtrSucessCnt() == null ? other.getRmbtchmtrSucessCnt() == null : this.getRmbtchmtrSucessCnt().equals(other.getRmbtchmtrSucessCnt()))
            && (this.getRmbtchmtrSucessAmt() == null ? other.getRmbtchmtrSucessAmt() == null : this.getRmbtchmtrSucessAmt().equals(other.getRmbtchmtrSucessAmt()))
            && (this.getRmbtchmtrFlag() == null ? other.getRmbtchmtrFlag() == null : this.getRmbtchmtrFlag().equals(other.getRmbtchmtrFlag()))
            && (this.getRmbtchmtrSendCbs() == null ? other.getRmbtchmtrSendCbs() == null : this.getRmbtchmtrSendCbs().equals(other.getRmbtchmtrSendCbs()))
            && (this.getRmbtchmtrCbsRc() == null ? other.getRmbtchmtrCbsRc() == null : this.getRmbtchmtrCbsRc().equals(other.getRmbtchmtrCbsRc()))
            && (this.getRmbtchmtrKinbr() == null ? other.getRmbtchmtrKinbr() == null : this.getRmbtchmtrKinbr().equals(other.getRmbtchmtrKinbr()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCHMTR
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRmbtchmtrSdn() == null) ? 0 : getRmbtchmtrSdn().hashCode());
        result = prime * result + ((getRmbtchmtrRemdate() == null) ? 0 : getRmbtchmtrRemdate().hashCode());
        result = prime * result + ((getRmbtchmtrTimes() == null) ? 0 : getRmbtchmtrTimes().hashCode());
        result = prime * result + ((getRmbtchmtrCnt() == null) ? 0 : getRmbtchmtrCnt().hashCode());
        result = prime * result + ((getRmbtchmtrAmt() == null) ? 0 : getRmbtchmtrAmt().hashCode());
        result = prime * result + ((getRmbtchmtrFailCnt() == null) ? 0 : getRmbtchmtrFailCnt().hashCode());
        result = prime * result + ((getRmbtchmtrFailAmt() == null) ? 0 : getRmbtchmtrFailAmt().hashCode());
        result = prime * result + ((getRmbtchmtrSucessCnt() == null) ? 0 : getRmbtchmtrSucessCnt().hashCode());
        result = prime * result + ((getRmbtchmtrSucessAmt() == null) ? 0 : getRmbtchmtrSucessAmt().hashCode());
        result = prime * result + ((getRmbtchmtrFlag() == null) ? 0 : getRmbtchmtrFlag().hashCode());
        result = prime * result + ((getRmbtchmtrSendCbs() == null) ? 0 : getRmbtchmtrSendCbs().hashCode());
        result = prime * result + ((getRmbtchmtrCbsRc() == null) ? 0 : getRmbtchmtrCbsRc().hashCode());
        result = prime * result + ((getRmbtchmtrKinbr() == null) ? 0 : getRmbtchmtrKinbr().hashCode());
        result = prime * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCHMTR
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", rmbtchmtrSdn=").append(rmbtchmtrSdn);
        sb.append(", rmbtchmtrRemdate=").append(rmbtchmtrRemdate);
        sb.append(", rmbtchmtrTimes=").append(rmbtchmtrTimes);
        sb.append(", rmbtchmtrCnt=").append(rmbtchmtrCnt);
        sb.append(", rmbtchmtrAmt=").append(rmbtchmtrAmt);
        sb.append(", rmbtchmtrFailCnt=").append(rmbtchmtrFailCnt);
        sb.append(", rmbtchmtrFailAmt=").append(rmbtchmtrFailAmt);
        sb.append(", rmbtchmtrSucessCnt=").append(rmbtchmtrSucessCnt);
        sb.append(", rmbtchmtrSucessAmt=").append(rmbtchmtrSucessAmt);
        sb.append(", rmbtchmtrFlag=").append(rmbtchmtrFlag);
        sb.append(", rmbtchmtrSendCbs=").append(rmbtchmtrSendCbs);
        sb.append(", rmbtchmtrCbsRc=").append(rmbtchmtrCbsRc);
        sb.append(", rmbtchmtrKinbr=").append(rmbtchmtrKinbr);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMBTCHMTR
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<RMBTCHMTR_SDN>").append(this.rmbtchmtrSdn).append("</RMBTCHMTR_SDN>");
        sb.append("<RMBTCHMTR_REMDATE>").append(this.rmbtchmtrRemdate).append("</RMBTCHMTR_REMDATE>");
        sb.append("<RMBTCHMTR_TIMES>").append(this.rmbtchmtrTimes).append("</RMBTCHMTR_TIMES>");
        sb.append("<RMBTCHMTR_CNT>").append(this.rmbtchmtrCnt).append("</RMBTCHMTR_CNT>");
        sb.append("<RMBTCHMTR_AMT>").append(this.rmbtchmtrAmt).append("</RMBTCHMTR_AMT>");
        sb.append("<RMBTCHMTR_FAIL_CNT>").append(this.rmbtchmtrFailCnt).append("</RMBTCHMTR_FAIL_CNT>");
        sb.append("<RMBTCHMTR_FAIL_AMT>").append(this.rmbtchmtrFailAmt).append("</RMBTCHMTR_FAIL_AMT>");
        sb.append("<RMBTCHMTR_SUCESS_CNT>").append(this.rmbtchmtrSucessCnt).append("</RMBTCHMTR_SUCESS_CNT>");
        sb.append("<RMBTCHMTR_SUCESS_AMT>").append(this.rmbtchmtrSucessAmt).append("</RMBTCHMTR_SUCESS_AMT>");
        sb.append("<RMBTCHMTR_FLAG>").append(this.rmbtchmtrFlag).append("</RMBTCHMTR_FLAG>");
        sb.append("<RMBTCHMTR_SEND_CBS>").append(this.rmbtchmtrSendCbs).append("</RMBTCHMTR_SEND_CBS>");
        sb.append("<RMBTCHMTR_CBS_RC>").append(this.rmbtchmtrCbsRc).append("</RMBTCHMTR_CBS_RC>");
        sb.append("<RMBTCHMTR_KINBR>").append(this.rmbtchmtrKinbr).append("</RMBTCHMTR_KINBR>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}