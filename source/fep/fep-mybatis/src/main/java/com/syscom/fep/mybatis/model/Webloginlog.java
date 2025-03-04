package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Webloginlog extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.LOGNO
     *
     * @mbg.generated
     */
    private Integer logno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.USERID
     *
     * @mbg.generated
     */
    private String userid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.APPID
     *
     * @mbg.generated
     */
    private String appid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.LOGACTION
     *
     * @mbg.generated
     */
    private String logaction;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.LOGRESULT
     *
     * @mbg.generated
     */
    private String logresult;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.LOGDATETIME
     *
     * @mbg.generated
     */
    private Date logdatetime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.HOSTIP
     *
     * @mbg.generated
     */
    private String hostip;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.CLIENTIP
     *
     * @mbg.generated
     */
    private String clientip;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.REMARK
     *
     * @mbg.generated
     */
    private String remark;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WEBLOGINLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    public Webloginlog(Integer logno, String userid, String appid, String logaction, String logresult, Date logdatetime, String hostip, String clientip, String remark, Integer updateUserid, Date updateTime) {
        this.logno = logno;
        this.userid = userid;
        this.appid = appid;
        this.logaction = logaction;
        this.logresult = logresult;
        this.logdatetime = logdatetime;
        this.hostip = hostip;
        this.clientip = clientip;
        this.remark = remark;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    public Webloginlog() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.LOGNO
     *
     * @return the value of WEBLOGINLOG.LOGNO
     *
     * @mbg.generated
     */
    public Integer getLogno() {
        return logno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.LOGNO
     *
     * @param logno the value for WEBLOGINLOG.LOGNO
     *
     * @mbg.generated
     */
    public void setLogno(Integer logno) {
        this.logno = logno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.USERID
     *
     * @return the value of WEBLOGINLOG.USERID
     *
     * @mbg.generated
     */
    public String getUserid() {
        return userid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.USERID
     *
     * @param userid the value for WEBLOGINLOG.USERID
     *
     * @mbg.generated
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.APPID
     *
     * @return the value of WEBLOGINLOG.APPID
     *
     * @mbg.generated
     */
    public String getAppid() {
        return appid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.APPID
     *
     * @param appid the value for WEBLOGINLOG.APPID
     *
     * @mbg.generated
     */
    public void setAppid(String appid) {
        this.appid = appid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.LOGACTION
     *
     * @return the value of WEBLOGINLOG.LOGACTION
     *
     * @mbg.generated
     */
    public String getLogaction() {
        return logaction;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.LOGACTION
     *
     * @param logaction the value for WEBLOGINLOG.LOGACTION
     *
     * @mbg.generated
     */
    public void setLogaction(String logaction) {
        this.logaction = logaction;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.LOGRESULT
     *
     * @return the value of WEBLOGINLOG.LOGRESULT
     *
     * @mbg.generated
     */
    public String getLogresult() {
        return logresult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.LOGRESULT
     *
     * @param logresult the value for WEBLOGINLOG.LOGRESULT
     *
     * @mbg.generated
     */
    public void setLogresult(String logresult) {
        this.logresult = logresult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.LOGDATETIME
     *
     * @return the value of WEBLOGINLOG.LOGDATETIME
     *
     * @mbg.generated
     */
    public Date getLogdatetime() {
        return logdatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.LOGDATETIME
     *
     * @param logdatetime the value for WEBLOGINLOG.LOGDATETIME
     *
     * @mbg.generated
     */
    public void setLogdatetime(Date logdatetime) {
        this.logdatetime = logdatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.HOSTIP
     *
     * @return the value of WEBLOGINLOG.HOSTIP
     *
     * @mbg.generated
     */
    public String getHostip() {
        return hostip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.HOSTIP
     *
     * @param hostip the value for WEBLOGINLOG.HOSTIP
     *
     * @mbg.generated
     */
    public void setHostip(String hostip) {
        this.hostip = hostip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.CLIENTIP
     *
     * @return the value of WEBLOGINLOG.CLIENTIP
     *
     * @mbg.generated
     */
    public String getClientip() {
        return clientip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.CLIENTIP
     *
     * @param clientip the value for WEBLOGINLOG.CLIENTIP
     *
     * @mbg.generated
     */
    public void setClientip(String clientip) {
        this.clientip = clientip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.REMARK
     *
     * @return the value of WEBLOGINLOG.REMARK
     *
     * @mbg.generated
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.REMARK
     *
     * @param remark the value for WEBLOGINLOG.REMARK
     *
     * @mbg.generated
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.UPDATE_USERID
     *
     * @return the value of WEBLOGINLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.UPDATE_USERID
     *
     * @param updateUserid the value for WEBLOGINLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WEBLOGINLOG.UPDATE_TIME
     *
     * @return the value of WEBLOGINLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WEBLOGINLOG.UPDATE_TIME
     *
     * @param updateTime the value for WEBLOGINLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", logno=").append(logno);
        sb.append(", userid=").append(userid);
        sb.append(", appid=").append(appid);
        sb.append(", logaction=").append(logaction);
        sb.append(", logresult=").append(logresult);
        sb.append(", logdatetime=").append(logdatetime);
        sb.append(", hostip=").append(hostip);
        sb.append(", clientip=").append(clientip);
        sb.append(", remark=").append(remark);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
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
        Webloginlog other = (Webloginlog) that;
        return (this.getLogno() == null ? other.getLogno() == null : this.getLogno().equals(other.getLogno()))
            && (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
            && (this.getAppid() == null ? other.getAppid() == null : this.getAppid().equals(other.getAppid()))
            && (this.getLogaction() == null ? other.getLogaction() == null : this.getLogaction().equals(other.getLogaction()))
            && (this.getLogresult() == null ? other.getLogresult() == null : this.getLogresult().equals(other.getLogresult()))
            && (this.getLogdatetime() == null ? other.getLogdatetime() == null : this.getLogdatetime().equals(other.getLogdatetime()))
            && (this.getHostip() == null ? other.getHostip() == null : this.getHostip().equals(other.getHostip()))
            && (this.getClientip() == null ? other.getClientip() == null : this.getClientip().equals(other.getClientip()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getLogno() == null) ? 0 : getLogno().hashCode());
        result = 31 * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
        result = 31 * result + ((getAppid() == null) ? 0 : getAppid().hashCode());
        result = 31 * result + ((getLogaction() == null) ? 0 : getLogaction().hashCode());
        result = 31 * result + ((getLogresult() == null) ? 0 : getLogresult().hashCode());
        result = 31 * result + ((getLogdatetime() == null) ? 0 : getLogdatetime().hashCode());
        result = 31 * result + ((getHostip() == null) ? 0 : getHostip().hashCode());
        result = 31 * result + ((getClientip() == null) ? 0 : getClientip().hashCode());
        result = 31 * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WEBLOGINLOG
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<LOGNO>").append(this.logno).append("</LOGNO>");
        sb.append("<USERID>").append(this.userid).append("</USERID>");
        sb.append("<APPID>").append(this.appid).append("</APPID>");
        sb.append("<LOGACTION>").append(this.logaction).append("</LOGACTION>");
        sb.append("<LOGRESULT>").append(this.logresult).append("</LOGRESULT>");
        sb.append("<LOGDATETIME>").append(this.logdatetime).append("</LOGDATETIME>");
        sb.append("<HOSTIP>").append(this.hostip).append("</HOSTIP>");
        sb.append("<CLIENTIP>").append(this.clientip).append("</CLIENTIP>");
        sb.append("<REMARK>").append(this.remark).append("</REMARK>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}