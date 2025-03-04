package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Hotacqcntry extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HOTACQCNTRY.ZONE_CODE
     *
     * @mbg.generated
     */
    private String zoneCode = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HOTACQCNTRY.ACQ_CNTRY
     *
     * @mbg.generated
     */
    private String acqCntry = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HOTACQCNTRY.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HOTACQCNTRY.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table HOTACQCNTRY
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTACQCNTRY
     *
     * @mbg.generated
     */
    public Hotacqcntry(String zoneCode, String acqCntry, Integer updateUserid, Date updateTime) {
        this.zoneCode = zoneCode;
        this.acqCntry = acqCntry;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTACQCNTRY
     *
     * @mbg.generated
     */
    public Hotacqcntry() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HOTACQCNTRY.ZONE_CODE
     *
     * @return the value of HOTACQCNTRY.ZONE_CODE
     *
     * @mbg.generated
     */
    public String getZoneCode() {
        return zoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HOTACQCNTRY.ZONE_CODE
     *
     * @param zoneCode the value for HOTACQCNTRY.ZONE_CODE
     *
     * @mbg.generated
     */
    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HOTACQCNTRY.ACQ_CNTRY
     *
     * @return the value of HOTACQCNTRY.ACQ_CNTRY
     *
     * @mbg.generated
     */
    public String getAcqCntry() {
        return acqCntry;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HOTACQCNTRY.ACQ_CNTRY
     *
     * @param acqCntry the value for HOTACQCNTRY.ACQ_CNTRY
     *
     * @mbg.generated
     */
    public void setAcqCntry(String acqCntry) {
        this.acqCntry = acqCntry;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HOTACQCNTRY.UPDATE_USERID
     *
     * @return the value of HOTACQCNTRY.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HOTACQCNTRY.UPDATE_USERID
     *
     * @param updateUserid the value for HOTACQCNTRY.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HOTACQCNTRY.UPDATE_TIME
     *
     * @return the value of HOTACQCNTRY.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HOTACQCNTRY.UPDATE_TIME
     *
     * @param updateTime the value for HOTACQCNTRY.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTACQCNTRY
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", zoneCode=").append(zoneCode);
        sb.append(", acqCntry=").append(acqCntry);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTACQCNTRY
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
        Hotacqcntry other = (Hotacqcntry) that;
        return (this.getZoneCode() == null ? other.getZoneCode() == null : this.getZoneCode().equals(other.getZoneCode()))
            && (this.getAcqCntry() == null ? other.getAcqCntry() == null : this.getAcqCntry().equals(other.getAcqCntry()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTACQCNTRY
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getZoneCode() == null) ? 0 : getZoneCode().hashCode());
        result = 31 * result + ((getAcqCntry() == null) ? 0 : getAcqCntry().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HOTACQCNTRY
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ZONE_CODE>").append(this.zoneCode).append("</ZONE_CODE>");
        sb.append("<ACQ_CNTRY>").append(this.acqCntry).append("</ACQ_CNTRY>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}