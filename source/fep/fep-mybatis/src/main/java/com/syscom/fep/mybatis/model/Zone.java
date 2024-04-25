package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Zone extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_CODE
     *
     * @mbg.generated
     */
    private String zoneCode = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_TX_SEQ
     *
     * @mbg.generated
     */
    private Integer zoneTxSeq;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_CBS_MODE
     *
     * @mbg.generated
     */
    private Short zoneCbsMode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_CHGDAY
     *
     * @mbg.generated
     */
    private Short zoneChgday;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_CHGDAY_TIME
     *
     * @mbg.generated
     */
    private Integer zoneChgdayTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_LLBSDY
     *
     * @mbg.generated
     */
    private String zoneLlbsdy = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_LBSDY
     *
     * @mbg.generated
     */
    private String zoneLbsdy = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_TBSDY
     *
     * @mbg.generated
     */
    private String zoneTbsdy = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_NBSDY
     *
     * @mbg.generated
     */
    private String zoneNbsdy = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_WEEKNO
     *
     * @mbg.generated
     */
    private Short zoneWeekno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.ZONE_WORKDAY
     *
     * @mbg.generated
     */
    private Short zoneWorkday;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ZONE.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table ZONE
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ZONE
     *
     * @mbg.generated
     */
    public Zone(String zoneCode, Integer zoneTxSeq, Short zoneCbsMode, Short zoneChgday, Integer zoneChgdayTime, String zoneLlbsdy, String zoneLbsdy, String zoneTbsdy, String zoneNbsdy, Short zoneWeekno, Short zoneWorkday, Integer updateUserid, Date updateTime) {
        this.zoneCode = zoneCode;
        this.zoneTxSeq = zoneTxSeq;
        this.zoneCbsMode = zoneCbsMode;
        this.zoneChgday = zoneChgday;
        this.zoneChgdayTime = zoneChgdayTime;
        this.zoneLlbsdy = zoneLlbsdy;
        this.zoneLbsdy = zoneLbsdy;
        this.zoneTbsdy = zoneTbsdy;
        this.zoneNbsdy = zoneNbsdy;
        this.zoneWeekno = zoneWeekno;
        this.zoneWorkday = zoneWorkday;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ZONE
     *
     * @mbg.generated
     */
    public Zone() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_CODE
     *
     * @return the value of ZONE.ZONE_CODE
     *
     * @mbg.generated
     */
    public String getZoneCode() {
        return zoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_CODE
     *
     * @param zoneCode the value for ZONE.ZONE_CODE
     *
     * @mbg.generated
     */
    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_TX_SEQ
     *
     * @return the value of ZONE.ZONE_TX_SEQ
     *
     * @mbg.generated
     */
    public Integer getZoneTxSeq() {
        return zoneTxSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_TX_SEQ
     *
     * @param zoneTxSeq the value for ZONE.ZONE_TX_SEQ
     *
     * @mbg.generated
     */
    public void setZoneTxSeq(Integer zoneTxSeq) {
        this.zoneTxSeq = zoneTxSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_CBS_MODE
     *
     * @return the value of ZONE.ZONE_CBS_MODE
     *
     * @mbg.generated
     */
    public Short getZoneCbsMode() {
        return zoneCbsMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_CBS_MODE
     *
     * @param zoneCbsMode the value for ZONE.ZONE_CBS_MODE
     *
     * @mbg.generated
     */
    public void setZoneCbsMode(Short zoneCbsMode) {
        this.zoneCbsMode = zoneCbsMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_CHGDAY
     *
     * @return the value of ZONE.ZONE_CHGDAY
     *
     * @mbg.generated
     */
    public Short getZoneChgday() {
        return zoneChgday;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_CHGDAY
     *
     * @param zoneChgday the value for ZONE.ZONE_CHGDAY
     *
     * @mbg.generated
     */
    public void setZoneChgday(Short zoneChgday) {
        this.zoneChgday = zoneChgday;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_CHGDAY_TIME
     *
     * @return the value of ZONE.ZONE_CHGDAY_TIME
     *
     * @mbg.generated
     */
    public Integer getZoneChgdayTime() {
        return zoneChgdayTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_CHGDAY_TIME
     *
     * @param zoneChgdayTime the value for ZONE.ZONE_CHGDAY_TIME
     *
     * @mbg.generated
     */
    public void setZoneChgdayTime(Integer zoneChgdayTime) {
        this.zoneChgdayTime = zoneChgdayTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_LLBSDY
     *
     * @return the value of ZONE.ZONE_LLBSDY
     *
     * @mbg.generated
     */
    public String getZoneLlbsdy() {
        return zoneLlbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_LLBSDY
     *
     * @param zoneLlbsdy the value for ZONE.ZONE_LLBSDY
     *
     * @mbg.generated
     */
    public void setZoneLlbsdy(String zoneLlbsdy) {
        this.zoneLlbsdy = zoneLlbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_LBSDY
     *
     * @return the value of ZONE.ZONE_LBSDY
     *
     * @mbg.generated
     */
    public String getZoneLbsdy() {
        return zoneLbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_LBSDY
     *
     * @param zoneLbsdy the value for ZONE.ZONE_LBSDY
     *
     * @mbg.generated
     */
    public void setZoneLbsdy(String zoneLbsdy) {
        this.zoneLbsdy = zoneLbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_TBSDY
     *
     * @return the value of ZONE.ZONE_TBSDY
     *
     * @mbg.generated
     */
    public String getZoneTbsdy() {
        return zoneTbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_TBSDY
     *
     * @param zoneTbsdy the value for ZONE.ZONE_TBSDY
     *
     * @mbg.generated
     */
    public void setZoneTbsdy(String zoneTbsdy) {
        this.zoneTbsdy = zoneTbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_NBSDY
     *
     * @return the value of ZONE.ZONE_NBSDY
     *
     * @mbg.generated
     */
    public String getZoneNbsdy() {
        return zoneNbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_NBSDY
     *
     * @param zoneNbsdy the value for ZONE.ZONE_NBSDY
     *
     * @mbg.generated
     */
    public void setZoneNbsdy(String zoneNbsdy) {
        this.zoneNbsdy = zoneNbsdy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_WEEKNO
     *
     * @return the value of ZONE.ZONE_WEEKNO
     *
     * @mbg.generated
     */
    public Short getZoneWeekno() {
        return zoneWeekno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_WEEKNO
     *
     * @param zoneWeekno the value for ZONE.ZONE_WEEKNO
     *
     * @mbg.generated
     */
    public void setZoneWeekno(Short zoneWeekno) {
        this.zoneWeekno = zoneWeekno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.ZONE_WORKDAY
     *
     * @return the value of ZONE.ZONE_WORKDAY
     *
     * @mbg.generated
     */
    public Short getZoneWorkday() {
        return zoneWorkday;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.ZONE_WORKDAY
     *
     * @param zoneWorkday the value for ZONE.ZONE_WORKDAY
     *
     * @mbg.generated
     */
    public void setZoneWorkday(Short zoneWorkday) {
        this.zoneWorkday = zoneWorkday;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.UPDATE_USERID
     *
     * @return the value of ZONE.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.UPDATE_USERID
     *
     * @param updateUserid the value for ZONE.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ZONE.UPDATE_TIME
     *
     * @return the value of ZONE.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ZONE.UPDATE_TIME
     *
     * @param updateTime the value for ZONE.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ZONE
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
        sb.append(", zoneTxSeq=").append(zoneTxSeq);
        sb.append(", zoneCbsMode=").append(zoneCbsMode);
        sb.append(", zoneChgday=").append(zoneChgday);
        sb.append(", zoneChgdayTime=").append(zoneChgdayTime);
        sb.append(", zoneLlbsdy=").append(zoneLlbsdy);
        sb.append(", zoneLbsdy=").append(zoneLbsdy);
        sb.append(", zoneTbsdy=").append(zoneTbsdy);
        sb.append(", zoneNbsdy=").append(zoneNbsdy);
        sb.append(", zoneWeekno=").append(zoneWeekno);
        sb.append(", zoneWorkday=").append(zoneWorkday);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ZONE
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
        Zone other = (Zone) that;
        return (this.getZoneCode() == null ? other.getZoneCode() == null : this.getZoneCode().equals(other.getZoneCode()))
            && (this.getZoneTxSeq() == null ? other.getZoneTxSeq() == null : this.getZoneTxSeq().equals(other.getZoneTxSeq()))
            && (this.getZoneCbsMode() == null ? other.getZoneCbsMode() == null : this.getZoneCbsMode().equals(other.getZoneCbsMode()))
            && (this.getZoneChgday() == null ? other.getZoneChgday() == null : this.getZoneChgday().equals(other.getZoneChgday()))
            && (this.getZoneChgdayTime() == null ? other.getZoneChgdayTime() == null : this.getZoneChgdayTime().equals(other.getZoneChgdayTime()))
            && (this.getZoneLlbsdy() == null ? other.getZoneLlbsdy() == null : this.getZoneLlbsdy().equals(other.getZoneLlbsdy()))
            && (this.getZoneLbsdy() == null ? other.getZoneLbsdy() == null : this.getZoneLbsdy().equals(other.getZoneLbsdy()))
            && (this.getZoneTbsdy() == null ? other.getZoneTbsdy() == null : this.getZoneTbsdy().equals(other.getZoneTbsdy()))
            && (this.getZoneNbsdy() == null ? other.getZoneNbsdy() == null : this.getZoneNbsdy().equals(other.getZoneNbsdy()))
            && (this.getZoneWeekno() == null ? other.getZoneWeekno() == null : this.getZoneWeekno().equals(other.getZoneWeekno()))
            && (this.getZoneWorkday() == null ? other.getZoneWorkday() == null : this.getZoneWorkday().equals(other.getZoneWorkday()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ZONE
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getZoneCode() == null) ? 0 : getZoneCode().hashCode());
        result = 31 * result + ((getZoneTxSeq() == null) ? 0 : getZoneTxSeq().hashCode());
        result = 31 * result + ((getZoneCbsMode() == null) ? 0 : getZoneCbsMode().hashCode());
        result = 31 * result + ((getZoneChgday() == null) ? 0 : getZoneChgday().hashCode());
        result = 31 * result + ((getZoneChgdayTime() == null) ? 0 : getZoneChgdayTime().hashCode());
        result = 31 * result + ((getZoneLlbsdy() == null) ? 0 : getZoneLlbsdy().hashCode());
        result = 31 * result + ((getZoneLbsdy() == null) ? 0 : getZoneLbsdy().hashCode());
        result = 31 * result + ((getZoneTbsdy() == null) ? 0 : getZoneTbsdy().hashCode());
        result = 31 * result + ((getZoneNbsdy() == null) ? 0 : getZoneNbsdy().hashCode());
        result = 31 * result + ((getZoneWeekno() == null) ? 0 : getZoneWeekno().hashCode());
        result = 31 * result + ((getZoneWorkday() == null) ? 0 : getZoneWorkday().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ZONE
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ZONE_CODE>").append(this.zoneCode).append("</ZONE_CODE>");
        sb.append("<ZONE_TX_SEQ>").append(this.zoneTxSeq).append("</ZONE_TX_SEQ>");
        sb.append("<ZONE_CBS_MODE>").append(this.zoneCbsMode).append("</ZONE_CBS_MODE>");
        sb.append("<ZONE_CHGDAY>").append(this.zoneChgday).append("</ZONE_CHGDAY>");
        sb.append("<ZONE_CHGDAY_TIME>").append(this.zoneChgdayTime).append("</ZONE_CHGDAY_TIME>");
        sb.append("<ZONE_LLBSDY>").append(this.zoneLlbsdy).append("</ZONE_LLBSDY>");
        sb.append("<ZONE_LBSDY>").append(this.zoneLbsdy).append("</ZONE_LBSDY>");
        sb.append("<ZONE_TBSDY>").append(this.zoneTbsdy).append("</ZONE_TBSDY>");
        sb.append("<ZONE_NBSDY>").append(this.zoneNbsdy).append("</ZONE_NBSDY>");
        sb.append("<ZONE_WEEKNO>").append(this.zoneWeekno).append("</ZONE_WEEKNO>");
        sb.append("<ZONE_WORKDAY>").append(this.zoneWorkday).append("</ZONE_WORKDAY>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}