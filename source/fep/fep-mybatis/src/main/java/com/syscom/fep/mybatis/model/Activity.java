package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Activity extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.ACTIVITY_ID
     *
     * @mbg.generated
     */
    private Integer activityId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.ACTIVITY_BUSI
     *
     * @mbg.generated
     */
    private String activityBusi = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.ACTIVITY_NAME
     *
     * @mbg.generated
     */
    private String activityName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.ACTIVITY_BEGIN_DATE
     *
     * @mbg.generated
     */
    private String activityBeginDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.ACTIVITY_END_DATE
     *
     * @mbg.generated
     */
    private String activityEndDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.ACTIVITY_DUE_DATE
     *
     * @mbg.generated
     */
    private String activityDueDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column ACTIVITY.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table ACTIVITY
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ACTIVITY
     *
     * @mbg.generated
     */
    public Activity(Integer activityId, String activityBusi, String activityName, String activityBeginDate, String activityEndDate, String activityDueDate, Integer updateUserid, Date updateTime) {
        this.activityId = activityId;
        this.activityBusi = activityBusi;
        this.activityName = activityName;
        this.activityBeginDate = activityBeginDate;
        this.activityEndDate = activityEndDate;
        this.activityDueDate = activityDueDate;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ACTIVITY
     *
     * @mbg.generated
     */
    public Activity() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.ACTIVITY_ID
     *
     * @return the value of ACTIVITY.ACTIVITY_ID
     *
     * @mbg.generated
     */
    public Integer getActivityId() {
        return activityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.ACTIVITY_ID
     *
     * @param activityId the value for ACTIVITY.ACTIVITY_ID
     *
     * @mbg.generated
     */
    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.ACTIVITY_BUSI
     *
     * @return the value of ACTIVITY.ACTIVITY_BUSI
     *
     * @mbg.generated
     */
    public String getActivityBusi() {
        return activityBusi;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.ACTIVITY_BUSI
     *
     * @param activityBusi the value for ACTIVITY.ACTIVITY_BUSI
     *
     * @mbg.generated
     */
    public void setActivityBusi(String activityBusi) {
        this.activityBusi = activityBusi;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.ACTIVITY_NAME
     *
     * @return the value of ACTIVITY.ACTIVITY_NAME
     *
     * @mbg.generated
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.ACTIVITY_NAME
     *
     * @param activityName the value for ACTIVITY.ACTIVITY_NAME
     *
     * @mbg.generated
     */
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.ACTIVITY_BEGIN_DATE
     *
     * @return the value of ACTIVITY.ACTIVITY_BEGIN_DATE
     *
     * @mbg.generated
     */
    public String getActivityBeginDate() {
        return activityBeginDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.ACTIVITY_BEGIN_DATE
     *
     * @param activityBeginDate the value for ACTIVITY.ACTIVITY_BEGIN_DATE
     *
     * @mbg.generated
     */
    public void setActivityBeginDate(String activityBeginDate) {
        this.activityBeginDate = activityBeginDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.ACTIVITY_END_DATE
     *
     * @return the value of ACTIVITY.ACTIVITY_END_DATE
     *
     * @mbg.generated
     */
    public String getActivityEndDate() {
        return activityEndDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.ACTIVITY_END_DATE
     *
     * @param activityEndDate the value for ACTIVITY.ACTIVITY_END_DATE
     *
     * @mbg.generated
     */
    public void setActivityEndDate(String activityEndDate) {
        this.activityEndDate = activityEndDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.ACTIVITY_DUE_DATE
     *
     * @return the value of ACTIVITY.ACTIVITY_DUE_DATE
     *
     * @mbg.generated
     */
    public String getActivityDueDate() {
        return activityDueDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.ACTIVITY_DUE_DATE
     *
     * @param activityDueDate the value for ACTIVITY.ACTIVITY_DUE_DATE
     *
     * @mbg.generated
     */
    public void setActivityDueDate(String activityDueDate) {
        this.activityDueDate = activityDueDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.UPDATE_USERID
     *
     * @return the value of ACTIVITY.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.UPDATE_USERID
     *
     * @param updateUserid the value for ACTIVITY.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column ACTIVITY.UPDATE_TIME
     *
     * @return the value of ACTIVITY.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column ACTIVITY.UPDATE_TIME
     *
     * @param updateTime the value for ACTIVITY.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ACTIVITY
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", activityId=").append(activityId);
        sb.append(", activityBusi=").append(activityBusi);
        sb.append(", activityName=").append(activityName);
        sb.append(", activityBeginDate=").append(activityBeginDate);
        sb.append(", activityEndDate=").append(activityEndDate);
        sb.append(", activityDueDate=").append(activityDueDate);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ACTIVITY
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
        Activity other = (Activity) that;
        return (this.getActivityId() == null ? other.getActivityId() == null : this.getActivityId().equals(other.getActivityId()))
            && (this.getActivityBusi() == null ? other.getActivityBusi() == null : this.getActivityBusi().equals(other.getActivityBusi()))
            && (this.getActivityName() == null ? other.getActivityName() == null : this.getActivityName().equals(other.getActivityName()))
            && (this.getActivityBeginDate() == null ? other.getActivityBeginDate() == null : this.getActivityBeginDate().equals(other.getActivityBeginDate()))
            && (this.getActivityEndDate() == null ? other.getActivityEndDate() == null : this.getActivityEndDate().equals(other.getActivityEndDate()))
            && (this.getActivityDueDate() == null ? other.getActivityDueDate() == null : this.getActivityDueDate().equals(other.getActivityDueDate()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ACTIVITY
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getActivityId() == null) ? 0 : getActivityId().hashCode());
        result = 31 * result + ((getActivityBusi() == null) ? 0 : getActivityBusi().hashCode());
        result = 31 * result + ((getActivityName() == null) ? 0 : getActivityName().hashCode());
        result = 31 * result + ((getActivityBeginDate() == null) ? 0 : getActivityBeginDate().hashCode());
        result = 31 * result + ((getActivityEndDate() == null) ? 0 : getActivityEndDate().hashCode());
        result = 31 * result + ((getActivityDueDate() == null) ? 0 : getActivityDueDate().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table ACTIVITY
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ACTIVITY_ID>").append(this.activityId).append("</ACTIVITY_ID>");
        sb.append("<ACTIVITY_BUSI>").append(this.activityBusi).append("</ACTIVITY_BUSI>");
        sb.append("<ACTIVITY_NAME>").append(this.activityName).append("</ACTIVITY_NAME>");
        sb.append("<ACTIVITY_BEGIN_DATE>").append(this.activityBeginDate).append("</ACTIVITY_BEGIN_DATE>");
        sb.append("<ACTIVITY_END_DATE>").append(this.activityEndDate).append("</ACTIVITY_END_DATE>");
        sb.append("<ACTIVITY_DUE_DATE>").append(this.activityDueDate).append("</ACTIVITY_DUE_DATE>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}