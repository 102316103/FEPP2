package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Channel extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CHANNEL.CHANNEL_CHANNELNO
     *
     * @mbg.generated
     */
    private Short channelChannelno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CHANNEL.CHANNEL_NAME
     *
     * @mbg.generated
     */
    private String channelName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CHANNEL.CHANNEL_NAME_S
     *
     * @mbg.generated
     */
    private String channelNameS;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CHANNEL.CHANNEL_ENABLE
     *
     * @mbg.generated
     */
    private Short channelEnable;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CHANNEL.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CHANNEL.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CHANNEL.CHANNEL_ATM_TYPE
     *
     * @mbg.generated
     */
    private String channelAtmType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table CHANNEL
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHANNEL
     *
     * @mbg.generated
     */
    public Channel(Short channelChannelno, String channelName, String channelNameS, Short channelEnable, Integer updateUserid, Date updateTime, String channelAtmType) {
        this.channelChannelno = channelChannelno;
        this.channelName = channelName;
        this.channelNameS = channelNameS;
        this.channelEnable = channelEnable;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
        this.channelAtmType = channelAtmType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHANNEL
     *
     * @mbg.generated
     */
    public Channel() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CHANNEL.CHANNEL_CHANNELNO
     *
     * @return the value of CHANNEL.CHANNEL_CHANNELNO
     *
     * @mbg.generated
     */
    public Short getChannelChannelno() {
        return channelChannelno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CHANNEL.CHANNEL_CHANNELNO
     *
     * @param channelChannelno the value for CHANNEL.CHANNEL_CHANNELNO
     *
     * @mbg.generated
     */
    public void setChannelChannelno(Short channelChannelno) {
        this.channelChannelno = channelChannelno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CHANNEL.CHANNEL_NAME
     *
     * @return the value of CHANNEL.CHANNEL_NAME
     *
     * @mbg.generated
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CHANNEL.CHANNEL_NAME
     *
     * @param channelName the value for CHANNEL.CHANNEL_NAME
     *
     * @mbg.generated
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CHANNEL.CHANNEL_NAME_S
     *
     * @return the value of CHANNEL.CHANNEL_NAME_S
     *
     * @mbg.generated
     */
    public String getChannelNameS() {
        return channelNameS;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CHANNEL.CHANNEL_NAME_S
     *
     * @param channelNameS the value for CHANNEL.CHANNEL_NAME_S
     *
     * @mbg.generated
     */
    public void setChannelNameS(String channelNameS) {
        this.channelNameS = channelNameS;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CHANNEL.CHANNEL_ENABLE
     *
     * @return the value of CHANNEL.CHANNEL_ENABLE
     *
     * @mbg.generated
     */
    public Short getChannelEnable() {
        return channelEnable;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CHANNEL.CHANNEL_ENABLE
     *
     * @param channelEnable the value for CHANNEL.CHANNEL_ENABLE
     *
     * @mbg.generated
     */
    public void setChannelEnable(Short channelEnable) {
        this.channelEnable = channelEnable;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CHANNEL.UPDATE_USERID
     *
     * @return the value of CHANNEL.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CHANNEL.UPDATE_USERID
     *
     * @param updateUserid the value for CHANNEL.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CHANNEL.UPDATE_TIME
     *
     * @return the value of CHANNEL.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CHANNEL.UPDATE_TIME
     *
     * @param updateTime the value for CHANNEL.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CHANNEL.CHANNEL_ATM_TYPE
     *
     * @return the value of CHANNEL.CHANNEL_ATM_TYPE
     *
     * @mbg.generated
     */
    public String getChannelAtmType() {
        return channelAtmType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CHANNEL.CHANNEL_ATM_TYPE
     *
     * @param channelAtmType the value for CHANNEL.CHANNEL_ATM_TYPE
     *
     * @mbg.generated
     */
    public void setChannelAtmType(String channelAtmType) {
        this.channelAtmType = channelAtmType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHANNEL
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", channelChannelno=").append(channelChannelno);
        sb.append(", channelName=").append(channelName);
        sb.append(", channelNameS=").append(channelNameS);
        sb.append(", channelEnable=").append(channelEnable);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", channelAtmType=").append(channelAtmType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHANNEL
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
        Channel other = (Channel) that;
        return (this.getChannelChannelno() == null ? other.getChannelChannelno() == null : this.getChannelChannelno().equals(other.getChannelChannelno()))
            && (this.getChannelName() == null ? other.getChannelName() == null : this.getChannelName().equals(other.getChannelName()))
            && (this.getChannelNameS() == null ? other.getChannelNameS() == null : this.getChannelNameS().equals(other.getChannelNameS()))
            && (this.getChannelEnable() == null ? other.getChannelEnable() == null : this.getChannelEnable().equals(other.getChannelEnable()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getChannelAtmType() == null ? other.getChannelAtmType() == null : this.getChannelAtmType().equals(other.getChannelAtmType()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHANNEL
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getChannelChannelno() == null) ? 0 : getChannelChannelno().hashCode());
        result = 31 * result + ((getChannelName() == null) ? 0 : getChannelName().hashCode());
        result = 31 * result + ((getChannelNameS() == null) ? 0 : getChannelNameS().hashCode());
        result = 31 * result + ((getChannelEnable() == null) ? 0 : getChannelEnable().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = 31 * result + ((getChannelAtmType() == null) ? 0 : getChannelAtmType().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CHANNEL
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<CHANNEL_CHANNELNO>").append(this.channelChannelno).append("</CHANNEL_CHANNELNO>");
        sb.append("<CHANNEL_NAME>").append(this.channelName).append("</CHANNEL_NAME>");
        sb.append("<CHANNEL_NAME_S>").append(this.channelNameS).append("</CHANNEL_NAME_S>");
        sb.append("<CHANNEL_ENABLE>").append(this.channelEnable).append("</CHANNEL_ENABLE>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        sb.append("<CHANNEL_ATM_TYPE>").append(this.channelAtmType).append("</CHANNEL_ATM_TYPE>");
        return sb.toString();
    }
}