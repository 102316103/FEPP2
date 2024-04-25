package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Inbkdatavalid extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column INBKDATAVALID.INBKDATAVALID_DATE
     *
     * @mbg.generated
     */
    private String inbkdatavalidDate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column INBKDATAVALID.INBKDATAVALID_PROGRAM
     *
     * @mbg.generated
     */
    private String inbkdatavalidProgram = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column INBKDATAVALID.INBKDATAVALID_FILENAME
     *
     * @mbg.generated
     */
    private String inbkdatavalidFilename = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column INBKDATAVALID.INBKDATAVALID_COMPLETEFLAG
     *
     * @mbg.generated
     */
    private Long inbkdatavalidCompleteflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column INBKDATAVALID.INBKDATAVALID_RECORD
     *
     * @mbg.generated
     */
    private Integer inbkdatavalidRecord;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column INBKDATAVALID.UPDATE USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column INBKDATAVALID.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table INBKDATAVALID
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table INBKDATAVALID
     *
     * @mbg.generated
     */
    public Inbkdatavalid(String inbkdatavalidDate, String inbkdatavalidProgram, String inbkdatavalidFilename, Long inbkdatavalidCompleteflag, Integer inbkdatavalidRecord, Integer updateUserid, Date updateTime) {
        this.inbkdatavalidDate = inbkdatavalidDate;
        this.inbkdatavalidProgram = inbkdatavalidProgram;
        this.inbkdatavalidFilename = inbkdatavalidFilename;
        this.inbkdatavalidCompleteflag = inbkdatavalidCompleteflag;
        this.inbkdatavalidRecord = inbkdatavalidRecord;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table INBKDATAVALID
     *
     * @mbg.generated
     */
    public Inbkdatavalid() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column INBKDATAVALID.INBKDATAVALID_DATE
     *
     * @return the value of INBKDATAVALID.INBKDATAVALID_DATE
     *
     * @mbg.generated
     */
    public String getInbkdatavalidDate() {
        return inbkdatavalidDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column INBKDATAVALID.INBKDATAVALID_DATE
     *
     * @param inbkdatavalidDate the value for INBKDATAVALID.INBKDATAVALID_DATE
     *
     * @mbg.generated
     */
    public void setInbkdatavalidDate(String inbkdatavalidDate) {
        this.inbkdatavalidDate = inbkdatavalidDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column INBKDATAVALID.INBKDATAVALID_PROGRAM
     *
     * @return the value of INBKDATAVALID.INBKDATAVALID_PROGRAM
     *
     * @mbg.generated
     */
    public String getInbkdatavalidProgram() {
        return inbkdatavalidProgram;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column INBKDATAVALID.INBKDATAVALID_PROGRAM
     *
     * @param inbkdatavalidProgram the value for INBKDATAVALID.INBKDATAVALID_PROGRAM
     *
     * @mbg.generated
     */
    public void setInbkdatavalidProgram(String inbkdatavalidProgram) {
        this.inbkdatavalidProgram = inbkdatavalidProgram;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column INBKDATAVALID.INBKDATAVALID_FILENAME
     *
     * @return the value of INBKDATAVALID.INBKDATAVALID_FILENAME
     *
     * @mbg.generated
     */
    public String getInbkdatavalidFilename() {
        return inbkdatavalidFilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column INBKDATAVALID.INBKDATAVALID_FILENAME
     *
     * @param inbkdatavalidFilename the value for INBKDATAVALID.INBKDATAVALID_FILENAME
     *
     * @mbg.generated
     */
    public void setInbkdatavalidFilename(String inbkdatavalidFilename) {
        this.inbkdatavalidFilename = inbkdatavalidFilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column INBKDATAVALID.INBKDATAVALID_COMPLETEFLAG
     *
     * @return the value of INBKDATAVALID.INBKDATAVALID_COMPLETEFLAG
     *
     * @mbg.generated
     */
    public Long getInbkdatavalidCompleteflag() {
        return inbkdatavalidCompleteflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column INBKDATAVALID.INBKDATAVALID_COMPLETEFLAG
     *
     * @param inbkdatavalidCompleteflag the value for INBKDATAVALID.INBKDATAVALID_COMPLETEFLAG
     *
     * @mbg.generated
     */
    public void setInbkdatavalidCompleteflag(Long inbkdatavalidCompleteflag) {
        this.inbkdatavalidCompleteflag = inbkdatavalidCompleteflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column INBKDATAVALID.INBKDATAVALID_RECORD
     *
     * @return the value of INBKDATAVALID.INBKDATAVALID_RECORD
     *
     * @mbg.generated
     */
    public Integer getInbkdatavalidRecord() {
        return inbkdatavalidRecord;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column INBKDATAVALID.INBKDATAVALID_RECORD
     *
     * @param inbkdatavalidRecord the value for INBKDATAVALID.INBKDATAVALID_RECORD
     *
     * @mbg.generated
     */
    public void setInbkdatavalidRecord(Integer inbkdatavalidRecord) {
        this.inbkdatavalidRecord = inbkdatavalidRecord;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column INBKDATAVALID.UPDATE USERID
     *
     * @return the value of INBKDATAVALID.UPDATE USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column INBKDATAVALID.UPDATE USERID
     *
     * @param updateUserid the value for INBKDATAVALID.UPDATE USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column INBKDATAVALID.UPDATE_TIME
     *
     * @return the value of INBKDATAVALID.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column INBKDATAVALID.UPDATE_TIME
     *
     * @param updateTime the value for INBKDATAVALID.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table INBKDATAVALID
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", inbkdatavalidDate=").append(inbkdatavalidDate);
        sb.append(", inbkdatavalidProgram=").append(inbkdatavalidProgram);
        sb.append(", inbkdatavalidFilename=").append(inbkdatavalidFilename);
        sb.append(", inbkdatavalidCompleteflag=").append(inbkdatavalidCompleteflag);
        sb.append(", inbkdatavalidRecord=").append(inbkdatavalidRecord);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table INBKDATAVALID
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
        Inbkdatavalid other = (Inbkdatavalid) that;
        return (this.getInbkdatavalidDate() == null ? other.getInbkdatavalidDate() == null : this.getInbkdatavalidDate().equals(other.getInbkdatavalidDate()))
            && (this.getInbkdatavalidProgram() == null ? other.getInbkdatavalidProgram() == null : this.getInbkdatavalidProgram().equals(other.getInbkdatavalidProgram()))
            && (this.getInbkdatavalidFilename() == null ? other.getInbkdatavalidFilename() == null : this.getInbkdatavalidFilename().equals(other.getInbkdatavalidFilename()))
            && (this.getInbkdatavalidCompleteflag() == null ? other.getInbkdatavalidCompleteflag() == null : this.getInbkdatavalidCompleteflag().equals(other.getInbkdatavalidCompleteflag()))
            && (this.getInbkdatavalidRecord() == null ? other.getInbkdatavalidRecord() == null : this.getInbkdatavalidRecord().equals(other.getInbkdatavalidRecord()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table INBKDATAVALID
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getInbkdatavalidDate() == null) ? 0 : getInbkdatavalidDate().hashCode());
        result = 31 * result + ((getInbkdatavalidProgram() == null) ? 0 : getInbkdatavalidProgram().hashCode());
        result = 31 * result + ((getInbkdatavalidFilename() == null) ? 0 : getInbkdatavalidFilename().hashCode());
        result = 31 * result + ((getInbkdatavalidCompleteflag() == null) ? 0 : getInbkdatavalidCompleteflag().hashCode());
        result = 31 * result + ((getInbkdatavalidRecord() == null) ? 0 : getInbkdatavalidRecord().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table INBKDATAVALID
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<INBKDATAVALID_DATE>").append(this.inbkdatavalidDate).append("</INBKDATAVALID_DATE>");
        sb.append("<INBKDATAVALID_PROGRAM>").append(this.inbkdatavalidProgram).append("</INBKDATAVALID_PROGRAM>");
        sb.append("<INBKDATAVALID_FILENAME>").append(this.inbkdatavalidFilename).append("</INBKDATAVALID_FILENAME>");
        sb.append("<INBKDATAVALID_COMPLETEFLAG>").append(this.inbkdatavalidCompleteflag).append("</INBKDATAVALID_COMPLETEFLAG>");
        sb.append("<INBKDATAVALID_RECORD>").append(this.inbkdatavalidRecord).append("</INBKDATAVALID_RECORD>");
        sb.append("<UPDATE USERID>").append(this.updateUserid).append("</UPDATE USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}