package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Prgstat extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column PRGSTAT.PRGSTAT_PROGRAMID
     *
     * @mbg.generated
     */
    private String prgstatProgramid = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column PRGSTAT.PRGSTAT_FLAG
     *
     * @mbg.generated
     */
    private Integer prgstatFlag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column PRGSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column PRGSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    public Prgstat(String prgstatProgramid, Integer prgstatFlag, Integer updateUserid, Date updateTime) {
        this.prgstatProgramid = prgstatProgramid;
        this.prgstatFlag = prgstatFlag;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    public Prgstat() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column PRGSTAT.PRGSTAT_PROGRAMID
     *
     * @return the value of PRGSTAT.PRGSTAT_PROGRAMID
     *
     * @mbg.generated
     */
    public String getPrgstatProgramid() {
        return prgstatProgramid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column PRGSTAT.PRGSTAT_PROGRAMID
     *
     * @param prgstatProgramid the value for PRGSTAT.PRGSTAT_PROGRAMID
     *
     * @mbg.generated
     */
    public void setPrgstatProgramid(String prgstatProgramid) {
        this.prgstatProgramid = prgstatProgramid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column PRGSTAT.PRGSTAT_FLAG
     *
     * @return the value of PRGSTAT.PRGSTAT_FLAG
     *
     * @mbg.generated
     */
    public Integer getPrgstatFlag() {
        return prgstatFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column PRGSTAT.PRGSTAT_FLAG
     *
     * @param prgstatFlag the value for PRGSTAT.PRGSTAT_FLAG
     *
     * @mbg.generated
     */
    public void setPrgstatFlag(Integer prgstatFlag) {
        this.prgstatFlag = prgstatFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column PRGSTAT.UPDATE_USERID
     *
     * @return the value of PRGSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column PRGSTAT.UPDATE_USERID
     *
     * @param updateUserid the value for PRGSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column PRGSTAT.UPDATE_TIME
     *
     * @return the value of PRGSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column PRGSTAT.UPDATE_TIME
     *
     * @param updateTime the value for PRGSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", prgstatProgramid=").append(prgstatProgramid);
        sb.append(", prgstatFlag=").append(prgstatFlag);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
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
        Prgstat other = (Prgstat) that;
        return (this.getPrgstatProgramid() == null ? other.getPrgstatProgramid() == null : this.getPrgstatProgramid().equals(other.getPrgstatProgramid()))
            && (this.getPrgstatFlag() == null ? other.getPrgstatFlag() == null : this.getPrgstatFlag().equals(other.getPrgstatFlag()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getPrgstatProgramid() == null) ? 0 : getPrgstatProgramid().hashCode());
        result = 31 * result + ((getPrgstatFlag() == null) ? 0 : getPrgstatFlag().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table PRGSTAT
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<PRGSTAT_PROGRAMID>").append(this.prgstatProgramid).append("</PRGSTAT_PROGRAMID>");
        sb.append("<PRGSTAT_FLAG>").append(this.prgstatFlag).append("</PRGSTAT_FLAG>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}