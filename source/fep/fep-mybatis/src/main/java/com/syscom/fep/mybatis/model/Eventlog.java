package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Eventlog extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_ID
     *
     * @mbg.generated
     */
    private Integer eventlogId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_EVENT_ID
     *
     * @mbg.generated
     */
    private Integer eventlogEventId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_HANDLE_TIME
     *
     * @mbg.generated
     */
    private Date eventlogHandleTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_HANDLE_UNIT
     *
     * @mbg.generated
     */
    private String eventlogHandleUnit;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_HANDLE_USER
     *
     * @mbg.generated
     */
    private String eventlogHandleUser;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_ACTION
     *
     * @mbg.generated
     */
    private String eventlogAction;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_REMARK
     *
     * @mbg.generated
     */
    private String eventlogRemark;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_LOGIN_ID
     *
     * @mbg.generated
     */
    private String eventlogLoginId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column EVENTLOG.EVENTLOG_LOGIN_NAME
     *
     * @mbg.generated
     */
    private String eventlogLoginName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table EVENTLOG
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTLOG
     *
     * @mbg.generated
     */
    public Eventlog(Integer eventlogId, Integer eventlogEventId, Date eventlogHandleTime, String eventlogHandleUnit, String eventlogHandleUser, String eventlogAction, String eventlogRemark, String eventlogLoginId, String eventlogLoginName) {
        this.eventlogId = eventlogId;
        this.eventlogEventId = eventlogEventId;
        this.eventlogHandleTime = eventlogHandleTime;
        this.eventlogHandleUnit = eventlogHandleUnit;
        this.eventlogHandleUser = eventlogHandleUser;
        this.eventlogAction = eventlogAction;
        this.eventlogRemark = eventlogRemark;
        this.eventlogLoginId = eventlogLoginId;
        this.eventlogLoginName = eventlogLoginName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTLOG
     *
     * @mbg.generated
     */
    public Eventlog() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_ID
     *
     * @return the value of EVENTLOG.EVENTLOG_ID
     *
     * @mbg.generated
     */
    public Integer getEventlogId() {
        return eventlogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_ID
     *
     * @param eventlogId the value for EVENTLOG.EVENTLOG_ID
     *
     * @mbg.generated
     */
    public void setEventlogId(Integer eventlogId) {
        this.eventlogId = eventlogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_EVENT_ID
     *
     * @return the value of EVENTLOG.EVENTLOG_EVENT_ID
     *
     * @mbg.generated
     */
    public Integer getEventlogEventId() {
        return eventlogEventId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_EVENT_ID
     *
     * @param eventlogEventId the value for EVENTLOG.EVENTLOG_EVENT_ID
     *
     * @mbg.generated
     */
    public void setEventlogEventId(Integer eventlogEventId) {
        this.eventlogEventId = eventlogEventId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_HANDLE_TIME
     *
     * @return the value of EVENTLOG.EVENTLOG_HANDLE_TIME
     *
     * @mbg.generated
     */
    public Date getEventlogHandleTime() {
        return eventlogHandleTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_HANDLE_TIME
     *
     * @param eventlogHandleTime the value for EVENTLOG.EVENTLOG_HANDLE_TIME
     *
     * @mbg.generated
     */
    public void setEventlogHandleTime(Date eventlogHandleTime) {
        this.eventlogHandleTime = eventlogHandleTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_HANDLE_UNIT
     *
     * @return the value of EVENTLOG.EVENTLOG_HANDLE_UNIT
     *
     * @mbg.generated
     */
    public String getEventlogHandleUnit() {
        return eventlogHandleUnit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_HANDLE_UNIT
     *
     * @param eventlogHandleUnit the value for EVENTLOG.EVENTLOG_HANDLE_UNIT
     *
     * @mbg.generated
     */
    public void setEventlogHandleUnit(String eventlogHandleUnit) {
        this.eventlogHandleUnit = eventlogHandleUnit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_HANDLE_USER
     *
     * @return the value of EVENTLOG.EVENTLOG_HANDLE_USER
     *
     * @mbg.generated
     */
    public String getEventlogHandleUser() {
        return eventlogHandleUser;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_HANDLE_USER
     *
     * @param eventlogHandleUser the value for EVENTLOG.EVENTLOG_HANDLE_USER
     *
     * @mbg.generated
     */
    public void setEventlogHandleUser(String eventlogHandleUser) {
        this.eventlogHandleUser = eventlogHandleUser;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_ACTION
     *
     * @return the value of EVENTLOG.EVENTLOG_ACTION
     *
     * @mbg.generated
     */
    public String getEventlogAction() {
        return eventlogAction;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_ACTION
     *
     * @param eventlogAction the value for EVENTLOG.EVENTLOG_ACTION
     *
     * @mbg.generated
     */
    public void setEventlogAction(String eventlogAction) {
        this.eventlogAction = eventlogAction;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_REMARK
     *
     * @return the value of EVENTLOG.EVENTLOG_REMARK
     *
     * @mbg.generated
     */
    public String getEventlogRemark() {
        return eventlogRemark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_REMARK
     *
     * @param eventlogRemark the value for EVENTLOG.EVENTLOG_REMARK
     *
     * @mbg.generated
     */
    public void setEventlogRemark(String eventlogRemark) {
        this.eventlogRemark = eventlogRemark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_LOGIN_ID
     *
     * @return the value of EVENTLOG.EVENTLOG_LOGIN_ID
     *
     * @mbg.generated
     */
    public String getEventlogLoginId() {
        return eventlogLoginId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_LOGIN_ID
     *
     * @param eventlogLoginId the value for EVENTLOG.EVENTLOG_LOGIN_ID
     *
     * @mbg.generated
     */
    public void setEventlogLoginId(String eventlogLoginId) {
        this.eventlogLoginId = eventlogLoginId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column EVENTLOG.EVENTLOG_LOGIN_NAME
     *
     * @return the value of EVENTLOG.EVENTLOG_LOGIN_NAME
     *
     * @mbg.generated
     */
    public String getEventlogLoginName() {
        return eventlogLoginName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column EVENTLOG.EVENTLOG_LOGIN_NAME
     *
     * @param eventlogLoginName the value for EVENTLOG.EVENTLOG_LOGIN_NAME
     *
     * @mbg.generated
     */
    public void setEventlogLoginName(String eventlogLoginName) {
        this.eventlogLoginName = eventlogLoginName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTLOG
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", eventlogId=").append(eventlogId);
        sb.append(", eventlogEventId=").append(eventlogEventId);
        sb.append(", eventlogHandleTime=").append(eventlogHandleTime);
        sb.append(", eventlogHandleUnit=").append(eventlogHandleUnit);
        sb.append(", eventlogHandleUser=").append(eventlogHandleUser);
        sb.append(", eventlogAction=").append(eventlogAction);
        sb.append(", eventlogRemark=").append(eventlogRemark);
        sb.append(", eventlogLoginId=").append(eventlogLoginId);
        sb.append(", eventlogLoginName=").append(eventlogLoginName);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTLOG
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
        Eventlog other = (Eventlog) that;
        return (this.getEventlogId() == null ? other.getEventlogId() == null : this.getEventlogId().equals(other.getEventlogId()))
            && (this.getEventlogEventId() == null ? other.getEventlogEventId() == null : this.getEventlogEventId().equals(other.getEventlogEventId()))
            && (this.getEventlogHandleTime() == null ? other.getEventlogHandleTime() == null : this.getEventlogHandleTime().equals(other.getEventlogHandleTime()))
            && (this.getEventlogHandleUnit() == null ? other.getEventlogHandleUnit() == null : this.getEventlogHandleUnit().equals(other.getEventlogHandleUnit()))
            && (this.getEventlogHandleUser() == null ? other.getEventlogHandleUser() == null : this.getEventlogHandleUser().equals(other.getEventlogHandleUser()))
            && (this.getEventlogAction() == null ? other.getEventlogAction() == null : this.getEventlogAction().equals(other.getEventlogAction()))
            && (this.getEventlogRemark() == null ? other.getEventlogRemark() == null : this.getEventlogRemark().equals(other.getEventlogRemark()))
            && (this.getEventlogLoginId() == null ? other.getEventlogLoginId() == null : this.getEventlogLoginId().equals(other.getEventlogLoginId()))
            && (this.getEventlogLoginName() == null ? other.getEventlogLoginName() == null : this.getEventlogLoginName().equals(other.getEventlogLoginName()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTLOG
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getEventlogId() == null) ? 0 : getEventlogId().hashCode());
        result = 31 * result + ((getEventlogEventId() == null) ? 0 : getEventlogEventId().hashCode());
        result = 31 * result + ((getEventlogHandleTime() == null) ? 0 : getEventlogHandleTime().hashCode());
        result = 31 * result + ((getEventlogHandleUnit() == null) ? 0 : getEventlogHandleUnit().hashCode());
        result = 31 * result + ((getEventlogHandleUser() == null) ? 0 : getEventlogHandleUser().hashCode());
        result = 31 * result + ((getEventlogAction() == null) ? 0 : getEventlogAction().hashCode());
        result = 31 * result + ((getEventlogRemark() == null) ? 0 : getEventlogRemark().hashCode());
        result = 31 * result + ((getEventlogLoginId() == null) ? 0 : getEventlogLoginId().hashCode());
        result = 31 * result + ((getEventlogLoginName() == null) ? 0 : getEventlogLoginName().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table EVENTLOG
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<EVENTLOG_ID>").append(this.eventlogId).append("</EVENTLOG_ID>");
        sb.append("<EVENTLOG_EVENT_ID>").append(this.eventlogEventId).append("</EVENTLOG_EVENT_ID>");
        sb.append("<EVENTLOG_HANDLE_TIME>").append(this.eventlogHandleTime).append("</EVENTLOG_HANDLE_TIME>");
        sb.append("<EVENTLOG_HANDLE_UNIT>").append(this.eventlogHandleUnit).append("</EVENTLOG_HANDLE_UNIT>");
        sb.append("<EVENTLOG_HANDLE_USER>").append(this.eventlogHandleUser).append("</EVENTLOG_HANDLE_USER>");
        sb.append("<EVENTLOG_ACTION>").append(this.eventlogAction).append("</EVENTLOG_ACTION>");
        sb.append("<EVENTLOG_REMARK>").append(this.eventlogRemark).append("</EVENTLOG_REMARK>");
        sb.append("<EVENTLOG_LOGIN_ID>").append(this.eventlogLoginId).append("</EVENTLOG_LOGIN_ID>");
        sb.append("<EVENTLOG_LOGIN_NAME>").append(this.eventlogLoginName).append("</EVENTLOG_LOGIN_NAME>");
        return sb.toString();
    }
}