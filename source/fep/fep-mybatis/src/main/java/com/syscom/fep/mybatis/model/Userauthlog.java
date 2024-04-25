package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Userauthlog extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.LOGNO
     *
     * @mbg.generated
     */
    private Integer logno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.LOGDATE
     *
     * @mbg.generated
     */
    private String logdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.PROGRAMID
     *
     * @mbg.generated
     */
    private String programid = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.AUTHPROGRAMID
     *
     * @mbg.generated
     */
    private String authprogramid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.AUTHGROUPID
     *
     * @mbg.generated
     */
    private String authgroupid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.AUTHUSERID
     *
     * @mbg.generated
     */
    private String authuserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.AUTHSTATUS
     *
     * @mbg.generated
     */
    private String authstatus = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.AUTHDESC
     *
     * @mbg.generated
     */
    private String authdesc;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.AUTHUPDATEB
     *
     * @mbg.generated
     */
    private String authupdateb;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column USERAUTHLOG.AUTHUPDATEA
     *
     * @mbg.generated
     */
    private String authupdatea;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table USERAUTHLOG
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table USERAUTHLOG
     *
     * @mbg.generated
     */
    public Userauthlog(Integer logno, String logdate, String programid, String authprogramid, String authgroupid, String authuserid, String authstatus, String authdesc, Integer updateUserid, Date updateTime) {
        this.logno = logno;
        this.logdate = logdate;
        this.programid = programid;
        this.authprogramid = authprogramid;
        this.authgroupid = authgroupid;
        this.authuserid = authuserid;
        this.authstatus = authstatus;
        this.authdesc = authdesc;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table USERAUTHLOG
     *
     * @mbg.generated
     */
    public Userauthlog(Integer logno, String logdate, String programid, String authprogramid, String authgroupid, String authuserid, String authstatus, String authdesc, Integer updateUserid, Date updateTime, String authupdateb, String authupdatea) {
        this.logno = logno;
        this.logdate = logdate;
        this.programid = programid;
        this.authprogramid = authprogramid;
        this.authgroupid = authgroupid;
        this.authuserid = authuserid;
        this.authstatus = authstatus;
        this.authdesc = authdesc;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
        this.authupdateb = authupdateb;
        this.authupdatea = authupdatea;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table USERAUTHLOG
     *
     * @mbg.generated
     */
    public Userauthlog() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.LOGNO
     *
     * @return the value of USERAUTHLOG.LOGNO
     *
     * @mbg.generated
     */
    public Integer getLogno() {
        return logno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.LOGNO
     *
     * @param logno the value for USERAUTHLOG.LOGNO
     *
     * @mbg.generated
     */
    public void setLogno(Integer logno) {
        this.logno = logno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.LOGDATE
     *
     * @return the value of USERAUTHLOG.LOGDATE
     *
     * @mbg.generated
     */
    public String getLogdate() {
        return logdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.LOGDATE
     *
     * @param logdate the value for USERAUTHLOG.LOGDATE
     *
     * @mbg.generated
     */
    public void setLogdate(String logdate) {
        this.logdate = logdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.PROGRAMID
     *
     * @return the value of USERAUTHLOG.PROGRAMID
     *
     * @mbg.generated
     */
    public String getProgramid() {
        return programid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.PROGRAMID
     *
     * @param programid the value for USERAUTHLOG.PROGRAMID
     *
     * @mbg.generated
     */
    public void setProgramid(String programid) {
        this.programid = programid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.AUTHPROGRAMID
     *
     * @return the value of USERAUTHLOG.AUTHPROGRAMID
     *
     * @mbg.generated
     */
    public String getAuthprogramid() {
        return authprogramid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.AUTHPROGRAMID
     *
     * @param authprogramid the value for USERAUTHLOG.AUTHPROGRAMID
     *
     * @mbg.generated
     */
    public void setAuthprogramid(String authprogramid) {
        this.authprogramid = authprogramid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.AUTHGROUPID
     *
     * @return the value of USERAUTHLOG.AUTHGROUPID
     *
     * @mbg.generated
     */
    public String getAuthgroupid() {
        return authgroupid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.AUTHGROUPID
     *
     * @param authgroupid the value for USERAUTHLOG.AUTHGROUPID
     *
     * @mbg.generated
     */
    public void setAuthgroupid(String authgroupid) {
        this.authgroupid = authgroupid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.AUTHUSERID
     *
     * @return the value of USERAUTHLOG.AUTHUSERID
     *
     * @mbg.generated
     */
    public String getAuthuserid() {
        return authuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.AUTHUSERID
     *
     * @param authuserid the value for USERAUTHLOG.AUTHUSERID
     *
     * @mbg.generated
     */
    public void setAuthuserid(String authuserid) {
        this.authuserid = authuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.AUTHSTATUS
     *
     * @return the value of USERAUTHLOG.AUTHSTATUS
     *
     * @mbg.generated
     */
    public String getAuthstatus() {
        return authstatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.AUTHSTATUS
     *
     * @param authstatus the value for USERAUTHLOG.AUTHSTATUS
     *
     * @mbg.generated
     */
    public void setAuthstatus(String authstatus) {
        this.authstatus = authstatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.AUTHDESC
     *
     * @return the value of USERAUTHLOG.AUTHDESC
     *
     * @mbg.generated
     */
    public String getAuthdesc() {
        return authdesc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.AUTHDESC
     *
     * @param authdesc the value for USERAUTHLOG.AUTHDESC
     *
     * @mbg.generated
     */
    public void setAuthdesc(String authdesc) {
        this.authdesc = authdesc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.UPDATE_USERID
     *
     * @return the value of USERAUTHLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.UPDATE_USERID
     *
     * @param updateUserid the value for USERAUTHLOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.UPDATE_TIME
     *
     * @return the value of USERAUTHLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.UPDATE_TIME
     *
     * @param updateTime the value for USERAUTHLOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.AUTHUPDATEB
     *
     * @return the value of USERAUTHLOG.AUTHUPDATEB
     *
     * @mbg.generated
     */
    public String getAuthupdateb() {
        return authupdateb;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.AUTHUPDATEB
     *
     * @param authupdateb the value for USERAUTHLOG.AUTHUPDATEB
     *
     * @mbg.generated
     */
    public void setAuthupdateb(String authupdateb) {
        this.authupdateb = authupdateb;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column USERAUTHLOG.AUTHUPDATEA
     *
     * @return the value of USERAUTHLOG.AUTHUPDATEA
     *
     * @mbg.generated
     */
    public String getAuthupdatea() {
        return authupdatea;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column USERAUTHLOG.AUTHUPDATEA
     *
     * @param authupdatea the value for USERAUTHLOG.AUTHUPDATEA
     *
     * @mbg.generated
     */
    public void setAuthupdatea(String authupdatea) {
        this.authupdatea = authupdatea;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table USERAUTHLOG
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
        sb.append(", logdate=").append(logdate);
        sb.append(", programid=").append(programid);
        sb.append(", authprogramid=").append(authprogramid);
        sb.append(", authgroupid=").append(authgroupid);
        sb.append(", authuserid=").append(authuserid);
        sb.append(", authstatus=").append(authstatus);
        sb.append(", authdesc=").append(authdesc);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", authupdateb=").append(authupdateb);
        sb.append(", authupdatea=").append(authupdatea);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table USERAUTHLOG
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
        Userauthlog other = (Userauthlog) that;
        return (this.getLogno() == null ? other.getLogno() == null : this.getLogno().equals(other.getLogno()))
            && (this.getLogdate() == null ? other.getLogdate() == null : this.getLogdate().equals(other.getLogdate()))
            && (this.getProgramid() == null ? other.getProgramid() == null : this.getProgramid().equals(other.getProgramid()))
            && (this.getAuthprogramid() == null ? other.getAuthprogramid() == null : this.getAuthprogramid().equals(other.getAuthprogramid()))
            && (this.getAuthgroupid() == null ? other.getAuthgroupid() == null : this.getAuthgroupid().equals(other.getAuthgroupid()))
            && (this.getAuthuserid() == null ? other.getAuthuserid() == null : this.getAuthuserid().equals(other.getAuthuserid()))
            && (this.getAuthstatus() == null ? other.getAuthstatus() == null : this.getAuthstatus().equals(other.getAuthstatus()))
            && (this.getAuthdesc() == null ? other.getAuthdesc() == null : this.getAuthdesc().equals(other.getAuthdesc()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getAuthupdateb() == null ? other.getAuthupdateb() == null : this.getAuthupdateb().equals(other.getAuthupdateb()))
            && (this.getAuthupdatea() == null ? other.getAuthupdatea() == null : this.getAuthupdatea().equals(other.getAuthupdatea()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table USERAUTHLOG
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getLogno() == null) ? 0 : getLogno().hashCode());
        result = 31 * result + ((getLogdate() == null) ? 0 : getLogdate().hashCode());
        result = 31 * result + ((getProgramid() == null) ? 0 : getProgramid().hashCode());
        result = 31 * result + ((getAuthprogramid() == null) ? 0 : getAuthprogramid().hashCode());
        result = 31 * result + ((getAuthgroupid() == null) ? 0 : getAuthgroupid().hashCode());
        result = 31 * result + ((getAuthuserid() == null) ? 0 : getAuthuserid().hashCode());
        result = 31 * result + ((getAuthstatus() == null) ? 0 : getAuthstatus().hashCode());
        result = 31 * result + ((getAuthdesc() == null) ? 0 : getAuthdesc().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = 31 * result + ((getAuthupdateb() == null) ? 0 : getAuthupdateb().hashCode());
        result = 31 * result + ((getAuthupdatea() == null) ? 0 : getAuthupdatea().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table USERAUTHLOG
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<LOGNO>").append(this.logno).append("</LOGNO>");
        sb.append("<LOGDATE>").append(this.logdate).append("</LOGDATE>");
        sb.append("<PROGRAMID>").append(this.programid).append("</PROGRAMID>");
        sb.append("<AUTHPROGRAMID>").append(this.authprogramid).append("</AUTHPROGRAMID>");
        sb.append("<AUTHGROUPID>").append(this.authgroupid).append("</AUTHGROUPID>");
        sb.append("<AUTHUSERID>").append(this.authuserid).append("</AUTHUSERID>");
        sb.append("<AUTHSTATUS>").append(this.authstatus).append("</AUTHSTATUS>");
        sb.append("<AUTHDESC>").append(this.authdesc).append("</AUTHDESC>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        sb.append("<AUTHUPDATEB>").append(this.authupdateb).append("</AUTHUPDATEB>");
        sb.append("<AUTHUPDATEA>").append(this.authupdatea).append("</AUTHUPDATEA>");
        return sb.toString();
    }
}