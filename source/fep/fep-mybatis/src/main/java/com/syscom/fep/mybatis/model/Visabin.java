package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Visabin extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column VISABIN.VISABIN_BIN
     *
     * @mbg.generated
     */
    private String visabinBin = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column VISABIN.VISABIN_PROCESSOR_BIN
     *
     * @mbg.generated
     */
    private String visabinProcessorBin;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column VISABIN.VISABIN_REGION
     *
     * @mbg.generated
     */
    private String visabinRegion;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column VISABIN.VISABIN_COUNTRY
     *
     * @mbg.generated
     */
    private String visabinCountry;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column VISABIN.VISABIN_TYPE
     *
     * @mbg.generated
     */
    private String visabinType;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column VISABIN.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column VISABIN.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table VISABIN
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table VISABIN
     *
     * @mbg.generated
     */
    public Visabin(String visabinBin, String visabinProcessorBin, String visabinRegion, String visabinCountry, String visabinType, Integer updateUserid, Date updateTime) {
        this.visabinBin = visabinBin;
        this.visabinProcessorBin = visabinProcessorBin;
        this.visabinRegion = visabinRegion;
        this.visabinCountry = visabinCountry;
        this.visabinType = visabinType;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table VISABIN
     *
     * @mbg.generated
     */
    public Visabin() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column VISABIN.VISABIN_BIN
     *
     * @return the value of VISABIN.VISABIN_BIN
     *
     * @mbg.generated
     */
    public String getVisabinBin() {
        return visabinBin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column VISABIN.VISABIN_BIN
     *
     * @param visabinBin the value for VISABIN.VISABIN_BIN
     *
     * @mbg.generated
     */
    public void setVisabinBin(String visabinBin) {
        this.visabinBin = visabinBin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column VISABIN.VISABIN_PROCESSOR_BIN
     *
     * @return the value of VISABIN.VISABIN_PROCESSOR_BIN
     *
     * @mbg.generated
     */
    public String getVisabinProcessorBin() {
        return visabinProcessorBin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column VISABIN.VISABIN_PROCESSOR_BIN
     *
     * @param visabinProcessorBin the value for VISABIN.VISABIN_PROCESSOR_BIN
     *
     * @mbg.generated
     */
    public void setVisabinProcessorBin(String visabinProcessorBin) {
        this.visabinProcessorBin = visabinProcessorBin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column VISABIN.VISABIN_REGION
     *
     * @return the value of VISABIN.VISABIN_REGION
     *
     * @mbg.generated
     */
    public String getVisabinRegion() {
        return visabinRegion;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column VISABIN.VISABIN_REGION
     *
     * @param visabinRegion the value for VISABIN.VISABIN_REGION
     *
     * @mbg.generated
     */
    public void setVisabinRegion(String visabinRegion) {
        this.visabinRegion = visabinRegion;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column VISABIN.VISABIN_COUNTRY
     *
     * @return the value of VISABIN.VISABIN_COUNTRY
     *
     * @mbg.generated
     */
    public String getVisabinCountry() {
        return visabinCountry;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column VISABIN.VISABIN_COUNTRY
     *
     * @param visabinCountry the value for VISABIN.VISABIN_COUNTRY
     *
     * @mbg.generated
     */
    public void setVisabinCountry(String visabinCountry) {
        this.visabinCountry = visabinCountry;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column VISABIN.VISABIN_TYPE
     *
     * @return the value of VISABIN.VISABIN_TYPE
     *
     * @mbg.generated
     */
    public String getVisabinType() {
        return visabinType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column VISABIN.VISABIN_TYPE
     *
     * @param visabinType the value for VISABIN.VISABIN_TYPE
     *
     * @mbg.generated
     */
    public void setVisabinType(String visabinType) {
        this.visabinType = visabinType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column VISABIN.UPDATE_USERID
     *
     * @return the value of VISABIN.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column VISABIN.UPDATE_USERID
     *
     * @param updateUserid the value for VISABIN.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column VISABIN.UPDATE_TIME
     *
     * @return the value of VISABIN.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column VISABIN.UPDATE_TIME
     *
     * @param updateTime the value for VISABIN.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table VISABIN
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", visabinBin=").append(visabinBin);
        sb.append(", visabinProcessorBin=").append(visabinProcessorBin);
        sb.append(", visabinRegion=").append(visabinRegion);
        sb.append(", visabinCountry=").append(visabinCountry);
        sb.append(", visabinType=").append(visabinType);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table VISABIN
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
        Visabin other = (Visabin) that;
        return (this.getVisabinBin() == null ? other.getVisabinBin() == null : this.getVisabinBin().equals(other.getVisabinBin()))
            && (this.getVisabinProcessorBin() == null ? other.getVisabinProcessorBin() == null : this.getVisabinProcessorBin().equals(other.getVisabinProcessorBin()))
            && (this.getVisabinRegion() == null ? other.getVisabinRegion() == null : this.getVisabinRegion().equals(other.getVisabinRegion()))
            && (this.getVisabinCountry() == null ? other.getVisabinCountry() == null : this.getVisabinCountry().equals(other.getVisabinCountry()))
            && (this.getVisabinType() == null ? other.getVisabinType() == null : this.getVisabinType().equals(other.getVisabinType()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table VISABIN
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getVisabinBin() == null) ? 0 : getVisabinBin().hashCode());
        result = 31 * result + ((getVisabinProcessorBin() == null) ? 0 : getVisabinProcessorBin().hashCode());
        result = 31 * result + ((getVisabinRegion() == null) ? 0 : getVisabinRegion().hashCode());
        result = 31 * result + ((getVisabinCountry() == null) ? 0 : getVisabinCountry().hashCode());
        result = 31 * result + ((getVisabinType() == null) ? 0 : getVisabinType().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table VISABIN
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<VISABIN_BIN>").append(this.visabinBin).append("</VISABIN_BIN>");
        sb.append("<VISABIN_PROCESSOR_BIN>").append(this.visabinProcessorBin).append("</VISABIN_PROCESSOR_BIN>");
        sb.append("<VISABIN_REGION>").append(this.visabinRegion).append("</VISABIN_REGION>");
        sb.append("<VISABIN_COUNTRY>").append(this.visabinCountry).append("</VISABIN_COUNTRY>");
        sb.append("<VISABIN_TYPE>").append(this.visabinType).append("</VISABIN_TYPE>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}