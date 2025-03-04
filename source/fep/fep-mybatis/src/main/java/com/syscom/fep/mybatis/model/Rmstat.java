package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Rmstat extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_HBKNO
     *
     * @mbg.generated
     */
    private String rmstatHbkno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_SENDING_CNT
     *
     * @mbg.generated
     */
    private Short rmstatSendingCnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_CBS_IN_FLAG
     *
     * @mbg.generated
     */
    private String rmstatCbsInFlag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_CBS_OUT_FLAG
     *
     * @mbg.generated
     */
    private String rmstatCbsOutFlag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_FISCI_FLAG1
     *
     * @mbg.generated
     */
    private String rmstatFisciFlag1;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_FISCI_FLAG4
     *
     * @mbg.generated
     */
    private String rmstatFisciFlag4;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_FISCO_FLAG1
     *
     * @mbg.generated
     */
    private String rmstatFiscoFlag1;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_FISCO_FLAG4
     *
     * @mbg.generated
     */
    private String rmstatFiscoFlag4;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_RM_FLAG
     *
     * @mbg.generated
     */
    private String rmstatRmFlag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column RMSTAT.RMSTAT_AMLFLAG
     *
     * @mbg.generated
     */
    private String rmstatAmlflag;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    public Rmstat(String rmstatHbkno, Short rmstatSendingCnt, String rmstatCbsInFlag, String rmstatCbsOutFlag, String rmstatFisciFlag1, String rmstatFisciFlag4, String rmstatFiscoFlag1, String rmstatFiscoFlag4, String rmstatRmFlag, Integer updateUserid, Date updateTime, String rmstatAmlflag) {
        this.rmstatHbkno = rmstatHbkno;
        this.rmstatSendingCnt = rmstatSendingCnt;
        this.rmstatCbsInFlag = rmstatCbsInFlag;
        this.rmstatCbsOutFlag = rmstatCbsOutFlag;
        this.rmstatFisciFlag1 = rmstatFisciFlag1;
        this.rmstatFisciFlag4 = rmstatFisciFlag4;
        this.rmstatFiscoFlag1 = rmstatFiscoFlag1;
        this.rmstatFiscoFlag4 = rmstatFiscoFlag4;
        this.rmstatRmFlag = rmstatRmFlag;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
        this.rmstatAmlflag = rmstatAmlflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    public Rmstat() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_HBKNO
     *
     * @return the value of RMSTAT.RMSTAT_HBKNO
     *
     * @mbg.generated
     */
    public String getRmstatHbkno() {
        return rmstatHbkno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_HBKNO
     *
     * @param rmstatHbkno the value for RMSTAT.RMSTAT_HBKNO
     *
     * @mbg.generated
     */
    public void setRmstatHbkno(String rmstatHbkno) {
        this.rmstatHbkno = rmstatHbkno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_SENDING_CNT
     *
     * @return the value of RMSTAT.RMSTAT_SENDING_CNT
     *
     * @mbg.generated
     */
    public Short getRmstatSendingCnt() {
        return rmstatSendingCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_SENDING_CNT
     *
     * @param rmstatSendingCnt the value for RMSTAT.RMSTAT_SENDING_CNT
     *
     * @mbg.generated
     */
    public void setRmstatSendingCnt(Short rmstatSendingCnt) {
        this.rmstatSendingCnt = rmstatSendingCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_CBS_IN_FLAG
     *
     * @return the value of RMSTAT.RMSTAT_CBS_IN_FLAG
     *
     * @mbg.generated
     */
    public String getRmstatCbsInFlag() {
        return rmstatCbsInFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_CBS_IN_FLAG
     *
     * @param rmstatCbsInFlag the value for RMSTAT.RMSTAT_CBS_IN_FLAG
     *
     * @mbg.generated
     */
    public void setRmstatCbsInFlag(String rmstatCbsInFlag) {
        this.rmstatCbsInFlag = rmstatCbsInFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_CBS_OUT_FLAG
     *
     * @return the value of RMSTAT.RMSTAT_CBS_OUT_FLAG
     *
     * @mbg.generated
     */
    public String getRmstatCbsOutFlag() {
        return rmstatCbsOutFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_CBS_OUT_FLAG
     *
     * @param rmstatCbsOutFlag the value for RMSTAT.RMSTAT_CBS_OUT_FLAG
     *
     * @mbg.generated
     */
    public void setRmstatCbsOutFlag(String rmstatCbsOutFlag) {
        this.rmstatCbsOutFlag = rmstatCbsOutFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_FISCI_FLAG1
     *
     * @return the value of RMSTAT.RMSTAT_FISCI_FLAG1
     *
     * @mbg.generated
     */
    public String getRmstatFisciFlag1() {
        return rmstatFisciFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_FISCI_FLAG1
     *
     * @param rmstatFisciFlag1 the value for RMSTAT.RMSTAT_FISCI_FLAG1
     *
     * @mbg.generated
     */
    public void setRmstatFisciFlag1(String rmstatFisciFlag1) {
        this.rmstatFisciFlag1 = rmstatFisciFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_FISCI_FLAG4
     *
     * @return the value of RMSTAT.RMSTAT_FISCI_FLAG4
     *
     * @mbg.generated
     */
    public String getRmstatFisciFlag4() {
        return rmstatFisciFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_FISCI_FLAG4
     *
     * @param rmstatFisciFlag4 the value for RMSTAT.RMSTAT_FISCI_FLAG4
     *
     * @mbg.generated
     */
    public void setRmstatFisciFlag4(String rmstatFisciFlag4) {
        this.rmstatFisciFlag4 = rmstatFisciFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_FISCO_FLAG1
     *
     * @return the value of RMSTAT.RMSTAT_FISCO_FLAG1
     *
     * @mbg.generated
     */
    public String getRmstatFiscoFlag1() {
        return rmstatFiscoFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_FISCO_FLAG1
     *
     * @param rmstatFiscoFlag1 the value for RMSTAT.RMSTAT_FISCO_FLAG1
     *
     * @mbg.generated
     */
    public void setRmstatFiscoFlag1(String rmstatFiscoFlag1) {
        this.rmstatFiscoFlag1 = rmstatFiscoFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_FISCO_FLAG4
     *
     * @return the value of RMSTAT.RMSTAT_FISCO_FLAG4
     *
     * @mbg.generated
     */
    public String getRmstatFiscoFlag4() {
        return rmstatFiscoFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_FISCO_FLAG4
     *
     * @param rmstatFiscoFlag4 the value for RMSTAT.RMSTAT_FISCO_FLAG4
     *
     * @mbg.generated
     */
    public void setRmstatFiscoFlag4(String rmstatFiscoFlag4) {
        this.rmstatFiscoFlag4 = rmstatFiscoFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_RM_FLAG
     *
     * @return the value of RMSTAT.RMSTAT_RM_FLAG
     *
     * @mbg.generated
     */
    public String getRmstatRmFlag() {
        return rmstatRmFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_RM_FLAG
     *
     * @param rmstatRmFlag the value for RMSTAT.RMSTAT_RM_FLAG
     *
     * @mbg.generated
     */
    public void setRmstatRmFlag(String rmstatRmFlag) {
        this.rmstatRmFlag = rmstatRmFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.UPDATE_USERID
     *
     * @return the value of RMSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.UPDATE_USERID
     *
     * @param updateUserid the value for RMSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.UPDATE_TIME
     *
     * @return the value of RMSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.UPDATE_TIME
     *
     * @param updateTime the value for RMSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column RMSTAT.RMSTAT_AMLFLAG
     *
     * @return the value of RMSTAT.RMSTAT_AMLFLAG
     *
     * @mbg.generated
     */
    public String getRmstatAmlflag() {
        return rmstatAmlflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column RMSTAT.RMSTAT_AMLFLAG
     *
     * @param rmstatAmlflag the value for RMSTAT.RMSTAT_AMLFLAG
     *
     * @mbg.generated
     */
    public void setRmstatAmlflag(String rmstatAmlflag) {
        this.rmstatAmlflag = rmstatAmlflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
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
        Rmstat other = (Rmstat) that;
        return (this.getRmstatHbkno() == null ? other.getRmstatHbkno() == null : this.getRmstatHbkno().equals(other.getRmstatHbkno()))
            && (this.getRmstatSendingCnt() == null ? other.getRmstatSendingCnt() == null : this.getRmstatSendingCnt().equals(other.getRmstatSendingCnt()))
            && (this.getRmstatCbsInFlag() == null ? other.getRmstatCbsInFlag() == null : this.getRmstatCbsInFlag().equals(other.getRmstatCbsInFlag()))
            && (this.getRmstatCbsOutFlag() == null ? other.getRmstatCbsOutFlag() == null : this.getRmstatCbsOutFlag().equals(other.getRmstatCbsOutFlag()))
            && (this.getRmstatFisciFlag1() == null ? other.getRmstatFisciFlag1() == null : this.getRmstatFisciFlag1().equals(other.getRmstatFisciFlag1()))
            && (this.getRmstatFisciFlag4() == null ? other.getRmstatFisciFlag4() == null : this.getRmstatFisciFlag4().equals(other.getRmstatFisciFlag4()))
            && (this.getRmstatFiscoFlag1() == null ? other.getRmstatFiscoFlag1() == null : this.getRmstatFiscoFlag1().equals(other.getRmstatFiscoFlag1()))
            && (this.getRmstatFiscoFlag4() == null ? other.getRmstatFiscoFlag4() == null : this.getRmstatFiscoFlag4().equals(other.getRmstatFiscoFlag4()))
            && (this.getRmstatRmFlag() == null ? other.getRmstatRmFlag() == null : this.getRmstatRmFlag().equals(other.getRmstatRmFlag()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getRmstatAmlflag() == null ? other.getRmstatAmlflag() == null : this.getRmstatAmlflag().equals(other.getRmstatAmlflag()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRmstatHbkno() == null) ? 0 : getRmstatHbkno().hashCode());
        result = prime * result + ((getRmstatSendingCnt() == null) ? 0 : getRmstatSendingCnt().hashCode());
        result = prime * result + ((getRmstatCbsInFlag() == null) ? 0 : getRmstatCbsInFlag().hashCode());
        result = prime * result + ((getRmstatCbsOutFlag() == null) ? 0 : getRmstatCbsOutFlag().hashCode());
        result = prime * result + ((getRmstatFisciFlag1() == null) ? 0 : getRmstatFisciFlag1().hashCode());
        result = prime * result + ((getRmstatFisciFlag4() == null) ? 0 : getRmstatFisciFlag4().hashCode());
        result = prime * result + ((getRmstatFiscoFlag1() == null) ? 0 : getRmstatFiscoFlag1().hashCode());
        result = prime * result + ((getRmstatFiscoFlag4() == null) ? 0 : getRmstatFiscoFlag4().hashCode());
        result = prime * result + ((getRmstatRmFlag() == null) ? 0 : getRmstatRmFlag().hashCode());
        result = prime * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getRmstatAmlflag() == null) ? 0 : getRmstatAmlflag().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", rmstatHbkno=").append(rmstatHbkno);
        sb.append(", rmstatSendingCnt=").append(rmstatSendingCnt);
        sb.append(", rmstatCbsInFlag=").append(rmstatCbsInFlag);
        sb.append(", rmstatCbsOutFlag=").append(rmstatCbsOutFlag);
        sb.append(", rmstatFisciFlag1=").append(rmstatFisciFlag1);
        sb.append(", rmstatFisciFlag4=").append(rmstatFisciFlag4);
        sb.append(", rmstatFiscoFlag1=").append(rmstatFiscoFlag1);
        sb.append(", rmstatFiscoFlag4=").append(rmstatFiscoFlag4);
        sb.append(", rmstatRmFlag=").append(rmstatRmFlag);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", rmstatAmlflag=").append(rmstatAmlflag);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table RMSTAT
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<RMSTAT_HBKNO>").append(this.rmstatHbkno).append("</RMSTAT_HBKNO>");
        sb.append("<RMSTAT_SENDING_CNT>").append(this.rmstatSendingCnt).append("</RMSTAT_SENDING_CNT>");
        sb.append("<RMSTAT_CBS_IN_FLAG>").append(this.rmstatCbsInFlag).append("</RMSTAT_CBS_IN_FLAG>");
        sb.append("<RMSTAT_CBS_OUT_FLAG>").append(this.rmstatCbsOutFlag).append("</RMSTAT_CBS_OUT_FLAG>");
        sb.append("<RMSTAT_FISCI_FLAG1>").append(this.rmstatFisciFlag1).append("</RMSTAT_FISCI_FLAG1>");
        sb.append("<RMSTAT_FISCI_FLAG4>").append(this.rmstatFisciFlag4).append("</RMSTAT_FISCI_FLAG4>");
        sb.append("<RMSTAT_FISCO_FLAG1>").append(this.rmstatFiscoFlag1).append("</RMSTAT_FISCO_FLAG1>");
        sb.append("<RMSTAT_FISCO_FLAG4>").append(this.rmstatFiscoFlag4).append("</RMSTAT_FISCO_FLAG4>");
        sb.append("<RMSTAT_RM_FLAG>").append(this.rmstatRmFlag).append("</RMSTAT_RM_FLAG>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        sb.append("<RMSTAT_AMLFLAG>").append(this.rmstatAmlflag).append("</RMSTAT_AMLFLAG>");
        return sb.toString();
    }
}