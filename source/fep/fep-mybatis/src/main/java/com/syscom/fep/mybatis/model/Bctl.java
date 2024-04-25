package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Bctl extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_BRNO
     *
     * @mbg.generated
     */
    private String bctlBrno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_LOGON
     *
     * @mbg.generated
     */
    private Short bctlLogon;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_BRKIND
     *
     * @mbg.generated
     */
    private Short bctlBrkind;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_RTCLS
     *
     * @mbg.generated
     */
    private Short bctlRtcls;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_FCRTCLS
     *
     * @mbg.generated
     */
    private Short bctlFcrtcls;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_BRNAME
     *
     * @mbg.generated
     */
    private String bctlBrname;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_ALIAS
     *
     * @mbg.generated
     */
    private String bctlAlias;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_ADDR
     *
     * @mbg.generated
     */
    private String bctlAddr;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_WSIP
     *
     * @mbg.generated
     */
    private String bctlWsip;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_DELETE_FG
     *
     * @mbg.generated
     */
    private Short bctlDeleteFg;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BCTL.BCTL_PHASE
     *
     * @mbg.generated
     */
    private Short bctlPhase;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table BCTL
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BCTL
     *
     * @mbg.generated
     */
    public Bctl(String bctlBrno, Short bctlLogon, Short bctlBrkind, Short bctlRtcls, Short bctlFcrtcls, String bctlBrname, String bctlAlias, String bctlAddr, String bctlWsip, Short bctlDeleteFg, Integer updateUserid, Date updateTime, Short bctlPhase) {
        this.bctlBrno = bctlBrno;
        this.bctlLogon = bctlLogon;
        this.bctlBrkind = bctlBrkind;
        this.bctlRtcls = bctlRtcls;
        this.bctlFcrtcls = bctlFcrtcls;
        this.bctlBrname = bctlBrname;
        this.bctlAlias = bctlAlias;
        this.bctlAddr = bctlAddr;
        this.bctlWsip = bctlWsip;
        this.bctlDeleteFg = bctlDeleteFg;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
        this.bctlPhase = bctlPhase;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BCTL
     *
     * @mbg.generated
     */
    public Bctl() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_BRNO
     *
     * @return the value of BCTL.BCTL_BRNO
     *
     * @mbg.generated
     */
    public String getBctlBrno() {
        return bctlBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_BRNO
     *
     * @param bctlBrno the value for BCTL.BCTL_BRNO
     *
     * @mbg.generated
     */
    public void setBctlBrno(String bctlBrno) {
        this.bctlBrno = bctlBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_LOGON
     *
     * @return the value of BCTL.BCTL_LOGON
     *
     * @mbg.generated
     */
    public Short getBctlLogon() {
        return bctlLogon;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_LOGON
     *
     * @param bctlLogon the value for BCTL.BCTL_LOGON
     *
     * @mbg.generated
     */
    public void setBctlLogon(Short bctlLogon) {
        this.bctlLogon = bctlLogon;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_BRKIND
     *
     * @return the value of BCTL.BCTL_BRKIND
     *
     * @mbg.generated
     */
    public Short getBctlBrkind() {
        return bctlBrkind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_BRKIND
     *
     * @param bctlBrkind the value for BCTL.BCTL_BRKIND
     *
     * @mbg.generated
     */
    public void setBctlBrkind(Short bctlBrkind) {
        this.bctlBrkind = bctlBrkind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_RTCLS
     *
     * @return the value of BCTL.BCTL_RTCLS
     *
     * @mbg.generated
     */
    public Short getBctlRtcls() {
        return bctlRtcls;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_RTCLS
     *
     * @param bctlRtcls the value for BCTL.BCTL_RTCLS
     *
     * @mbg.generated
     */
    public void setBctlRtcls(Short bctlRtcls) {
        this.bctlRtcls = bctlRtcls;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_FCRTCLS
     *
     * @return the value of BCTL.BCTL_FCRTCLS
     *
     * @mbg.generated
     */
    public Short getBctlFcrtcls() {
        return bctlFcrtcls;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_FCRTCLS
     *
     * @param bctlFcrtcls the value for BCTL.BCTL_FCRTCLS
     *
     * @mbg.generated
     */
    public void setBctlFcrtcls(Short bctlFcrtcls) {
        this.bctlFcrtcls = bctlFcrtcls;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_BRNAME
     *
     * @return the value of BCTL.BCTL_BRNAME
     *
     * @mbg.generated
     */
    public String getBctlBrname() {
        return bctlBrname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_BRNAME
     *
     * @param bctlBrname the value for BCTL.BCTL_BRNAME
     *
     * @mbg.generated
     */
    public void setBctlBrname(String bctlBrname) {
        this.bctlBrname = bctlBrname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_ALIAS
     *
     * @return the value of BCTL.BCTL_ALIAS
     *
     * @mbg.generated
     */
    public String getBctlAlias() {
        return bctlAlias;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_ALIAS
     *
     * @param bctlAlias the value for BCTL.BCTL_ALIAS
     *
     * @mbg.generated
     */
    public void setBctlAlias(String bctlAlias) {
        this.bctlAlias = bctlAlias;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_ADDR
     *
     * @return the value of BCTL.BCTL_ADDR
     *
     * @mbg.generated
     */
    public String getBctlAddr() {
        return bctlAddr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_ADDR
     *
     * @param bctlAddr the value for BCTL.BCTL_ADDR
     *
     * @mbg.generated
     */
    public void setBctlAddr(String bctlAddr) {
        this.bctlAddr = bctlAddr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_WSIP
     *
     * @return the value of BCTL.BCTL_WSIP
     *
     * @mbg.generated
     */
    public String getBctlWsip() {
        return bctlWsip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_WSIP
     *
     * @param bctlWsip the value for BCTL.BCTL_WSIP
     *
     * @mbg.generated
     */
    public void setBctlWsip(String bctlWsip) {
        this.bctlWsip = bctlWsip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_DELETE_FG
     *
     * @return the value of BCTL.BCTL_DELETE_FG
     *
     * @mbg.generated
     */
    public Short getBctlDeleteFg() {
        return bctlDeleteFg;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_DELETE_FG
     *
     * @param bctlDeleteFg the value for BCTL.BCTL_DELETE_FG
     *
     * @mbg.generated
     */
    public void setBctlDeleteFg(Short bctlDeleteFg) {
        this.bctlDeleteFg = bctlDeleteFg;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.UPDATE_USERID
     *
     * @return the value of BCTL.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.UPDATE_USERID
     *
     * @param updateUserid the value for BCTL.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.UPDATE_TIME
     *
     * @return the value of BCTL.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.UPDATE_TIME
     *
     * @param updateTime the value for BCTL.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BCTL.BCTL_PHASE
     *
     * @return the value of BCTL.BCTL_PHASE
     *
     * @mbg.generated
     */
    public Short getBctlPhase() {
        return bctlPhase;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BCTL.BCTL_PHASE
     *
     * @param bctlPhase the value for BCTL.BCTL_PHASE
     *
     * @mbg.generated
     */
    public void setBctlPhase(Short bctlPhase) {
        this.bctlPhase = bctlPhase;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BCTL
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", bctlBrno=").append(bctlBrno);
        sb.append(", bctlLogon=").append(bctlLogon);
        sb.append(", bctlBrkind=").append(bctlBrkind);
        sb.append(", bctlRtcls=").append(bctlRtcls);
        sb.append(", bctlFcrtcls=").append(bctlFcrtcls);
        sb.append(", bctlBrname=").append(bctlBrname);
        sb.append(", bctlAlias=").append(bctlAlias);
        sb.append(", bctlAddr=").append(bctlAddr);
        sb.append(", bctlWsip=").append(bctlWsip);
        sb.append(", bctlDeleteFg=").append(bctlDeleteFg);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", bctlPhase=").append(bctlPhase);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BCTL
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
        Bctl other = (Bctl) that;
        return (this.getBctlBrno() == null ? other.getBctlBrno() == null : this.getBctlBrno().equals(other.getBctlBrno()))
            && (this.getBctlLogon() == null ? other.getBctlLogon() == null : this.getBctlLogon().equals(other.getBctlLogon()))
            && (this.getBctlBrkind() == null ? other.getBctlBrkind() == null : this.getBctlBrkind().equals(other.getBctlBrkind()))
            && (this.getBctlRtcls() == null ? other.getBctlRtcls() == null : this.getBctlRtcls().equals(other.getBctlRtcls()))
            && (this.getBctlFcrtcls() == null ? other.getBctlFcrtcls() == null : this.getBctlFcrtcls().equals(other.getBctlFcrtcls()))
            && (this.getBctlBrname() == null ? other.getBctlBrname() == null : this.getBctlBrname().equals(other.getBctlBrname()))
            && (this.getBctlAlias() == null ? other.getBctlAlias() == null : this.getBctlAlias().equals(other.getBctlAlias()))
            && (this.getBctlAddr() == null ? other.getBctlAddr() == null : this.getBctlAddr().equals(other.getBctlAddr()))
            && (this.getBctlWsip() == null ? other.getBctlWsip() == null : this.getBctlWsip().equals(other.getBctlWsip()))
            && (this.getBctlDeleteFg() == null ? other.getBctlDeleteFg() == null : this.getBctlDeleteFg().equals(other.getBctlDeleteFg()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getBctlPhase() == null ? other.getBctlPhase() == null : this.getBctlPhase().equals(other.getBctlPhase()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BCTL
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getBctlBrno() == null) ? 0 : getBctlBrno().hashCode());
        result = 31 * result + ((getBctlLogon() == null) ? 0 : getBctlLogon().hashCode());
        result = 31 * result + ((getBctlBrkind() == null) ? 0 : getBctlBrkind().hashCode());
        result = 31 * result + ((getBctlRtcls() == null) ? 0 : getBctlRtcls().hashCode());
        result = 31 * result + ((getBctlFcrtcls() == null) ? 0 : getBctlFcrtcls().hashCode());
        result = 31 * result + ((getBctlBrname() == null) ? 0 : getBctlBrname().hashCode());
        result = 31 * result + ((getBctlAlias() == null) ? 0 : getBctlAlias().hashCode());
        result = 31 * result + ((getBctlAddr() == null) ? 0 : getBctlAddr().hashCode());
        result = 31 * result + ((getBctlWsip() == null) ? 0 : getBctlWsip().hashCode());
        result = 31 * result + ((getBctlDeleteFg() == null) ? 0 : getBctlDeleteFg().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = 31 * result + ((getBctlPhase() == null) ? 0 : getBctlPhase().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BCTL
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<BCTL_BRNO>").append(this.bctlBrno).append("</BCTL_BRNO>");
        sb.append("<BCTL_LOGON>").append(this.bctlLogon).append("</BCTL_LOGON>");
        sb.append("<BCTL_BRKIND>").append(this.bctlBrkind).append("</BCTL_BRKIND>");
        sb.append("<BCTL_RTCLS>").append(this.bctlRtcls).append("</BCTL_RTCLS>");
        sb.append("<BCTL_FCRTCLS>").append(this.bctlFcrtcls).append("</BCTL_FCRTCLS>");
        sb.append("<BCTL_BRNAME>").append(this.bctlBrname).append("</BCTL_BRNAME>");
        sb.append("<BCTL_ALIAS>").append(this.bctlAlias).append("</BCTL_ALIAS>");
        sb.append("<BCTL_ADDR>").append(this.bctlAddr).append("</BCTL_ADDR>");
        sb.append("<BCTL_WSIP>").append(this.bctlWsip).append("</BCTL_WSIP>");
        sb.append("<BCTL_DELETE_FG>").append(this.bctlDeleteFg).append("</BCTL_DELETE_FG>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        sb.append("<BCTL_PHASE>").append(this.bctlPhase).append("</BCTL_PHASE>");
        return sb.toString();
    }
}