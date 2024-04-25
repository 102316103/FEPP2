package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Sms extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_SERVICENAME
     *
     * @mbg.generated
     */
    private String smsServicename = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_SERVICEIP
     *
     * @mbg.generated
     */
    private String smsServiceip = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_HOSTNAME
     *
     * @mbg.generated
     */
    private String smsHostname = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_UPDATETIME
     *
     * @mbg.generated
     */
    private Date smsUpdatetime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_SERVICESTATE
     *
     * @mbg.generated
     */
    private String smsServicestate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_STARTTIME
     *
     * @mbg.generated
     */
    private Date smsStarttime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_PID
     *
     * @mbg.generated
     */
    private Integer smsPid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_CPU
     *
     * @mbg.generated
     */
    private Integer smsCpu;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_CPU_THRESHOLD
     *
     * @mbg.generated
     */
    private Integer smsCpuThreshold;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_RAM
     *
     * @mbg.generated
     */
    private Integer smsRam;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_RAM_THRESHOLD
     *
     * @mbg.generated
     */
    private Integer smsRamThreshold;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_THREADS
     *
     * @mbg.generated
     */
    private Integer smsThreads;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_THREADS_ACTIVE
     *
     * @mbg.generated
     */
    private Integer smsThreadsActive;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_THREADS_THRESHOLD
     *
     * @mbg.generated
     */
    private Integer smsThreadsThreshold;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_STOPTIME
     *
     * @mbg.generated
     */
    private Date smsStoptime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SMS.SMS_OTHERS
     *
     * @mbg.generated
     */
    private String smsOthers;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table SMS
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SMS
     *
     * @mbg.generated
     */
    public Sms(String smsServicename, String smsServiceip, String smsHostname, Date smsUpdatetime, String smsServicestate, Date smsStarttime, Integer smsPid, Integer smsCpu, Integer smsCpuThreshold, Integer smsRam, Integer smsRamThreshold, Integer smsThreads, Integer smsThreadsActive, Integer smsThreadsThreshold, Date smsStoptime) {
        this.smsServicename = smsServicename;
        this.smsServiceip = smsServiceip;
        this.smsHostname = smsHostname;
        this.smsUpdatetime = smsUpdatetime;
        this.smsServicestate = smsServicestate;
        this.smsStarttime = smsStarttime;
        this.smsPid = smsPid;
        this.smsCpu = smsCpu;
        this.smsCpuThreshold = smsCpuThreshold;
        this.smsRam = smsRam;
        this.smsRamThreshold = smsRamThreshold;
        this.smsThreads = smsThreads;
        this.smsThreadsActive = smsThreadsActive;
        this.smsThreadsThreshold = smsThreadsThreshold;
        this.smsStoptime = smsStoptime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SMS
     *
     * @mbg.generated
     */
    public Sms(String smsServicename, String smsServiceip, String smsHostname, Date smsUpdatetime, String smsServicestate, Date smsStarttime, Integer smsPid, Integer smsCpu, Integer smsCpuThreshold, Integer smsRam, Integer smsRamThreshold, Integer smsThreads, Integer smsThreadsActive, Integer smsThreadsThreshold, Date smsStoptime, String smsOthers) {
        this.smsServicename = smsServicename;
        this.smsServiceip = smsServiceip;
        this.smsHostname = smsHostname;
        this.smsUpdatetime = smsUpdatetime;
        this.smsServicestate = smsServicestate;
        this.smsStarttime = smsStarttime;
        this.smsPid = smsPid;
        this.smsCpu = smsCpu;
        this.smsCpuThreshold = smsCpuThreshold;
        this.smsRam = smsRam;
        this.smsRamThreshold = smsRamThreshold;
        this.smsThreads = smsThreads;
        this.smsThreadsActive = smsThreadsActive;
        this.smsThreadsThreshold = smsThreadsThreshold;
        this.smsStoptime = smsStoptime;
        this.smsOthers = smsOthers;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SMS
     *
     * @mbg.generated
     */
    public Sms() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_SERVICENAME
     *
     * @return the value of SMS.SMS_SERVICENAME
     *
     * @mbg.generated
     */
    public String getSmsServicename() {
        return smsServicename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_SERVICENAME
     *
     * @param smsServicename the value for SMS.SMS_SERVICENAME
     *
     * @mbg.generated
     */
    public void setSmsServicename(String smsServicename) {
        this.smsServicename = smsServicename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_SERVICEIP
     *
     * @return the value of SMS.SMS_SERVICEIP
     *
     * @mbg.generated
     */
    public String getSmsServiceip() {
        return smsServiceip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_SERVICEIP
     *
     * @param smsServiceip the value for SMS.SMS_SERVICEIP
     *
     * @mbg.generated
     */
    public void setSmsServiceip(String smsServiceip) {
        this.smsServiceip = smsServiceip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_HOSTNAME
     *
     * @return the value of SMS.SMS_HOSTNAME
     *
     * @mbg.generated
     */
    public String getSmsHostname() {
        return smsHostname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_HOSTNAME
     *
     * @param smsHostname the value for SMS.SMS_HOSTNAME
     *
     * @mbg.generated
     */
    public void setSmsHostname(String smsHostname) {
        this.smsHostname = smsHostname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_UPDATETIME
     *
     * @return the value of SMS.SMS_UPDATETIME
     *
     * @mbg.generated
     */
    public Date getSmsUpdatetime() {
        return smsUpdatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_UPDATETIME
     *
     * @param smsUpdatetime the value for SMS.SMS_UPDATETIME
     *
     * @mbg.generated
     */
    public void setSmsUpdatetime(Date smsUpdatetime) {
        this.smsUpdatetime = smsUpdatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_SERVICESTATE
     *
     * @return the value of SMS.SMS_SERVICESTATE
     *
     * @mbg.generated
     */
    public String getSmsServicestate() {
        return smsServicestate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_SERVICESTATE
     *
     * @param smsServicestate the value for SMS.SMS_SERVICESTATE
     *
     * @mbg.generated
     */
    public void setSmsServicestate(String smsServicestate) {
        this.smsServicestate = smsServicestate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_STARTTIME
     *
     * @return the value of SMS.SMS_STARTTIME
     *
     * @mbg.generated
     */
    public Date getSmsStarttime() {
        return smsStarttime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_STARTTIME
     *
     * @param smsStarttime the value for SMS.SMS_STARTTIME
     *
     * @mbg.generated
     */
    public void setSmsStarttime(Date smsStarttime) {
        this.smsStarttime = smsStarttime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_PID
     *
     * @return the value of SMS.SMS_PID
     *
     * @mbg.generated
     */
    public Integer getSmsPid() {
        return smsPid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_PID
     *
     * @param smsPid the value for SMS.SMS_PID
     *
     * @mbg.generated
     */
    public void setSmsPid(Integer smsPid) {
        this.smsPid = smsPid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_CPU
     *
     * @return the value of SMS.SMS_CPU
     *
     * @mbg.generated
     */
    public Integer getSmsCpu() {
        return smsCpu;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_CPU
     *
     * @param smsCpu the value for SMS.SMS_CPU
     *
     * @mbg.generated
     */
    public void setSmsCpu(Integer smsCpu) {
        this.smsCpu = smsCpu;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_CPU_THRESHOLD
     *
     * @return the value of SMS.SMS_CPU_THRESHOLD
     *
     * @mbg.generated
     */
    public Integer getSmsCpuThreshold() {
        return smsCpuThreshold;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_CPU_THRESHOLD
     *
     * @param smsCpuThreshold the value for SMS.SMS_CPU_THRESHOLD
     *
     * @mbg.generated
     */
    public void setSmsCpuThreshold(Integer smsCpuThreshold) {
        this.smsCpuThreshold = smsCpuThreshold;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_RAM
     *
     * @return the value of SMS.SMS_RAM
     *
     * @mbg.generated
     */
    public Integer getSmsRam() {
        return smsRam;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_RAM
     *
     * @param smsRam the value for SMS.SMS_RAM
     *
     * @mbg.generated
     */
    public void setSmsRam(Integer smsRam) {
        this.smsRam = smsRam;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_RAM_THRESHOLD
     *
     * @return the value of SMS.SMS_RAM_THRESHOLD
     *
     * @mbg.generated
     */
    public Integer getSmsRamThreshold() {
        return smsRamThreshold;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_RAM_THRESHOLD
     *
     * @param smsRamThreshold the value for SMS.SMS_RAM_THRESHOLD
     *
     * @mbg.generated
     */
    public void setSmsRamThreshold(Integer smsRamThreshold) {
        this.smsRamThreshold = smsRamThreshold;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_THREADS
     *
     * @return the value of SMS.SMS_THREADS
     *
     * @mbg.generated
     */
    public Integer getSmsThreads() {
        return smsThreads;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_THREADS
     *
     * @param smsThreads the value for SMS.SMS_THREADS
     *
     * @mbg.generated
     */
    public void setSmsThreads(Integer smsThreads) {
        this.smsThreads = smsThreads;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_THREADS_ACTIVE
     *
     * @return the value of SMS.SMS_THREADS_ACTIVE
     *
     * @mbg.generated
     */
    public Integer getSmsThreadsActive() {
        return smsThreadsActive;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_THREADS_ACTIVE
     *
     * @param smsThreadsActive the value for SMS.SMS_THREADS_ACTIVE
     *
     * @mbg.generated
     */
    public void setSmsThreadsActive(Integer smsThreadsActive) {
        this.smsThreadsActive = smsThreadsActive;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_THREADS_THRESHOLD
     *
     * @return the value of SMS.SMS_THREADS_THRESHOLD
     *
     * @mbg.generated
     */
    public Integer getSmsThreadsThreshold() {
        return smsThreadsThreshold;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_THREADS_THRESHOLD
     *
     * @param smsThreadsThreshold the value for SMS.SMS_THREADS_THRESHOLD
     *
     * @mbg.generated
     */
    public void setSmsThreadsThreshold(Integer smsThreadsThreshold) {
        this.smsThreadsThreshold = smsThreadsThreshold;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_STOPTIME
     *
     * @return the value of SMS.SMS_STOPTIME
     *
     * @mbg.generated
     */
    public Date getSmsStoptime() {
        return smsStoptime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_STOPTIME
     *
     * @param smsStoptime the value for SMS.SMS_STOPTIME
     *
     * @mbg.generated
     */
    public void setSmsStoptime(Date smsStoptime) {
        this.smsStoptime = smsStoptime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SMS.SMS_OTHERS
     *
     * @return the value of SMS.SMS_OTHERS
     *
     * @mbg.generated
     */
    public String getSmsOthers() {
        return smsOthers;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SMS.SMS_OTHERS
     *
     * @param smsOthers the value for SMS.SMS_OTHERS
     *
     * @mbg.generated
     */
    public void setSmsOthers(String smsOthers) {
        this.smsOthers = smsOthers;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SMS
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", smsServicename=").append(smsServicename);
        sb.append(", smsServiceip=").append(smsServiceip);
        sb.append(", smsHostname=").append(smsHostname);
        sb.append(", smsUpdatetime=").append(smsUpdatetime);
        sb.append(", smsServicestate=").append(smsServicestate);
        sb.append(", smsStarttime=").append(smsStarttime);
        sb.append(", smsPid=").append(smsPid);
        sb.append(", smsCpu=").append(smsCpu);
        sb.append(", smsCpuThreshold=").append(smsCpuThreshold);
        sb.append(", smsRam=").append(smsRam);
        sb.append(", smsRamThreshold=").append(smsRamThreshold);
        sb.append(", smsThreads=").append(smsThreads);
        sb.append(", smsThreadsActive=").append(smsThreadsActive);
        sb.append(", smsThreadsThreshold=").append(smsThreadsThreshold);
        sb.append(", smsStoptime=").append(smsStoptime);
        sb.append(", smsOthers=").append(smsOthers);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SMS
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
        Sms other = (Sms) that;
        return (this.getSmsServicename() == null ? other.getSmsServicename() == null : this.getSmsServicename().equals(other.getSmsServicename()))
            && (this.getSmsServiceip() == null ? other.getSmsServiceip() == null : this.getSmsServiceip().equals(other.getSmsServiceip()))
            && (this.getSmsHostname() == null ? other.getSmsHostname() == null : this.getSmsHostname().equals(other.getSmsHostname()))
            && (this.getSmsUpdatetime() == null ? other.getSmsUpdatetime() == null : this.getSmsUpdatetime().equals(other.getSmsUpdatetime()))
            && (this.getSmsServicestate() == null ? other.getSmsServicestate() == null : this.getSmsServicestate().equals(other.getSmsServicestate()))
            && (this.getSmsStarttime() == null ? other.getSmsStarttime() == null : this.getSmsStarttime().equals(other.getSmsStarttime()))
            && (this.getSmsPid() == null ? other.getSmsPid() == null : this.getSmsPid().equals(other.getSmsPid()))
            && (this.getSmsCpu() == null ? other.getSmsCpu() == null : this.getSmsCpu().equals(other.getSmsCpu()))
            && (this.getSmsCpuThreshold() == null ? other.getSmsCpuThreshold() == null : this.getSmsCpuThreshold().equals(other.getSmsCpuThreshold()))
            && (this.getSmsRam() == null ? other.getSmsRam() == null : this.getSmsRam().equals(other.getSmsRam()))
            && (this.getSmsRamThreshold() == null ? other.getSmsRamThreshold() == null : this.getSmsRamThreshold().equals(other.getSmsRamThreshold()))
            && (this.getSmsThreads() == null ? other.getSmsThreads() == null : this.getSmsThreads().equals(other.getSmsThreads()))
            && (this.getSmsThreadsActive() == null ? other.getSmsThreadsActive() == null : this.getSmsThreadsActive().equals(other.getSmsThreadsActive()))
            && (this.getSmsThreadsThreshold() == null ? other.getSmsThreadsThreshold() == null : this.getSmsThreadsThreshold().equals(other.getSmsThreadsThreshold()))
            && (this.getSmsStoptime() == null ? other.getSmsStoptime() == null : this.getSmsStoptime().equals(other.getSmsStoptime()))
            && (this.getSmsOthers() == null ? other.getSmsOthers() == null : this.getSmsOthers().equals(other.getSmsOthers()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SMS
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getSmsServicename() == null) ? 0 : getSmsServicename().hashCode());
        result = 31 * result + ((getSmsServiceip() == null) ? 0 : getSmsServiceip().hashCode());
        result = 31 * result + ((getSmsHostname() == null) ? 0 : getSmsHostname().hashCode());
        result = 31 * result + ((getSmsUpdatetime() == null) ? 0 : getSmsUpdatetime().hashCode());
        result = 31 * result + ((getSmsServicestate() == null) ? 0 : getSmsServicestate().hashCode());
        result = 31 * result + ((getSmsStarttime() == null) ? 0 : getSmsStarttime().hashCode());
        result = 31 * result + ((getSmsPid() == null) ? 0 : getSmsPid().hashCode());
        result = 31 * result + ((getSmsCpu() == null) ? 0 : getSmsCpu().hashCode());
        result = 31 * result + ((getSmsCpuThreshold() == null) ? 0 : getSmsCpuThreshold().hashCode());
        result = 31 * result + ((getSmsRam() == null) ? 0 : getSmsRam().hashCode());
        result = 31 * result + ((getSmsRamThreshold() == null) ? 0 : getSmsRamThreshold().hashCode());
        result = 31 * result + ((getSmsThreads() == null) ? 0 : getSmsThreads().hashCode());
        result = 31 * result + ((getSmsThreadsActive() == null) ? 0 : getSmsThreadsActive().hashCode());
        result = 31 * result + ((getSmsThreadsThreshold() == null) ? 0 : getSmsThreadsThreshold().hashCode());
        result = 31 * result + ((getSmsStoptime() == null) ? 0 : getSmsStoptime().hashCode());
        result = 31 * result + ((getSmsOthers() == null) ? 0 : getSmsOthers().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SMS
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<SMS_SERVICENAME>").append(this.smsServicename).append("</SMS_SERVICENAME>");
        sb.append("<SMS_SERVICEIP>").append(this.smsServiceip).append("</SMS_SERVICEIP>");
        sb.append("<SMS_HOSTNAME>").append(this.smsHostname).append("</SMS_HOSTNAME>");
        sb.append("<SMS_UPDATETIME>").append(this.smsUpdatetime).append("</SMS_UPDATETIME>");
        sb.append("<SMS_SERVICESTATE>").append(this.smsServicestate).append("</SMS_SERVICESTATE>");
        sb.append("<SMS_STARTTIME>").append(this.smsStarttime).append("</SMS_STARTTIME>");
        sb.append("<SMS_PID>").append(this.smsPid).append("</SMS_PID>");
        sb.append("<SMS_CPU>").append(this.smsCpu).append("</SMS_CPU>");
        sb.append("<SMS_CPU_THRESHOLD>").append(this.smsCpuThreshold).append("</SMS_CPU_THRESHOLD>");
        sb.append("<SMS_RAM>").append(this.smsRam).append("</SMS_RAM>");
        sb.append("<SMS_RAM_THRESHOLD>").append(this.smsRamThreshold).append("</SMS_RAM_THRESHOLD>");
        sb.append("<SMS_THREADS>").append(this.smsThreads).append("</SMS_THREADS>");
        sb.append("<SMS_THREADS_ACTIVE>").append(this.smsThreadsActive).append("</SMS_THREADS_ACTIVE>");
        sb.append("<SMS_THREADS_THRESHOLD>").append(this.smsThreadsThreshold).append("</SMS_THREADS_THRESHOLD>");
        sb.append("<SMS_STOPTIME>").append(this.smsStoptime).append("</SMS_STOPTIME>");
        sb.append("<SMS_OTHERS>").append(this.smsOthers).append("</SMS_OTHERS>");
        return sb.toString();
    }
}