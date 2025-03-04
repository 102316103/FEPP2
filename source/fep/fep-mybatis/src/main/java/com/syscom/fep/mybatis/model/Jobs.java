package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;

public class Jobs extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column JOBS.JOBS_JOBID
     *
     * @mbg.generated
     */
    private Integer jobsJobid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column JOBS.JOBS_NAME
     *
     * @mbg.generated
     */
    private String jobsName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column JOBS.JOBS_BATCHID
     *
     * @mbg.generated
     */
    private Integer jobsBatchid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column JOBS.JOBS_DESCRIPTION
     *
     * @mbg.generated
     */
    private String jobsDescription;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column JOBS.JOBS_SEQ
     *
     * @mbg.generated
     */
    private Integer jobsSeq;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column JOBS.JOBS_STARTTASKID
     *
     * @mbg.generated
     */
    private Integer jobsStarttaskid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column JOBS.JOBS_DELAY
     *
     * @mbg.generated
     */
    private Integer jobsDelay;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table JOBS
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table JOBS
     *
     * @mbg.generated
     */
    public Jobs(Integer jobsJobid, String jobsName, Integer jobsBatchid, String jobsDescription, Integer jobsSeq, Integer jobsStarttaskid, Integer jobsDelay) {
        this.jobsJobid = jobsJobid;
        this.jobsName = jobsName;
        this.jobsBatchid = jobsBatchid;
        this.jobsDescription = jobsDescription;
        this.jobsSeq = jobsSeq;
        this.jobsStarttaskid = jobsStarttaskid;
        this.jobsDelay = jobsDelay;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table JOBS
     *
     * @mbg.generated
     */
    public Jobs() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column JOBS.JOBS_JOBID
     *
     * @return the value of JOBS.JOBS_JOBID
     *
     * @mbg.generated
     */
    public Integer getJobsJobid() {
        return jobsJobid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column JOBS.JOBS_JOBID
     *
     * @param jobsJobid the value for JOBS.JOBS_JOBID
     *
     * @mbg.generated
     */
    public void setJobsJobid(Integer jobsJobid) {
        this.jobsJobid = jobsJobid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column JOBS.JOBS_NAME
     *
     * @return the value of JOBS.JOBS_NAME
     *
     * @mbg.generated
     */
    public String getJobsName() {
        return jobsName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column JOBS.JOBS_NAME
     *
     * @param jobsName the value for JOBS.JOBS_NAME
     *
     * @mbg.generated
     */
    public void setJobsName(String jobsName) {
        this.jobsName = jobsName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column JOBS.JOBS_BATCHID
     *
     * @return the value of JOBS.JOBS_BATCHID
     *
     * @mbg.generated
     */
    public Integer getJobsBatchid() {
        return jobsBatchid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column JOBS.JOBS_BATCHID
     *
     * @param jobsBatchid the value for JOBS.JOBS_BATCHID
     *
     * @mbg.generated
     */
    public void setJobsBatchid(Integer jobsBatchid) {
        this.jobsBatchid = jobsBatchid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column JOBS.JOBS_DESCRIPTION
     *
     * @return the value of JOBS.JOBS_DESCRIPTION
     *
     * @mbg.generated
     */
    public String getJobsDescription() {
        return jobsDescription;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column JOBS.JOBS_DESCRIPTION
     *
     * @param jobsDescription the value for JOBS.JOBS_DESCRIPTION
     *
     * @mbg.generated
     */
    public void setJobsDescription(String jobsDescription) {
        this.jobsDescription = jobsDescription;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column JOBS.JOBS_SEQ
     *
     * @return the value of JOBS.JOBS_SEQ
     *
     * @mbg.generated
     */
    public Integer getJobsSeq() {
        return jobsSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column JOBS.JOBS_SEQ
     *
     * @param jobsSeq the value for JOBS.JOBS_SEQ
     *
     * @mbg.generated
     */
    public void setJobsSeq(Integer jobsSeq) {
        this.jobsSeq = jobsSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column JOBS.JOBS_STARTTASKID
     *
     * @return the value of JOBS.JOBS_STARTTASKID
     *
     * @mbg.generated
     */
    public Integer getJobsStarttaskid() {
        return jobsStarttaskid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column JOBS.JOBS_STARTTASKID
     *
     * @param jobsStarttaskid the value for JOBS.JOBS_STARTTASKID
     *
     * @mbg.generated
     */
    public void setJobsStarttaskid(Integer jobsStarttaskid) {
        this.jobsStarttaskid = jobsStarttaskid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column JOBS.JOBS_DELAY
     *
     * @return the value of JOBS.JOBS_DELAY
     *
     * @mbg.generated
     */
    public Integer getJobsDelay() {
        return jobsDelay;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column JOBS.JOBS_DELAY
     *
     * @param jobsDelay the value for JOBS.JOBS_DELAY
     *
     * @mbg.generated
     */
    public void setJobsDelay(Integer jobsDelay) {
        this.jobsDelay = jobsDelay;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table JOBS
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", jobsJobid=").append(jobsJobid);
        sb.append(", jobsName=").append(jobsName);
        sb.append(", jobsBatchid=").append(jobsBatchid);
        sb.append(", jobsDescription=").append(jobsDescription);
        sb.append(", jobsSeq=").append(jobsSeq);
        sb.append(", jobsStarttaskid=").append(jobsStarttaskid);
        sb.append(", jobsDelay=").append(jobsDelay);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table JOBS
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
        Jobs other = (Jobs) that;
        return (this.getJobsJobid() == null ? other.getJobsJobid() == null : this.getJobsJobid().equals(other.getJobsJobid()))
            && (this.getJobsName() == null ? other.getJobsName() == null : this.getJobsName().equals(other.getJobsName()))
            && (this.getJobsBatchid() == null ? other.getJobsBatchid() == null : this.getJobsBatchid().equals(other.getJobsBatchid()))
            && (this.getJobsDescription() == null ? other.getJobsDescription() == null : this.getJobsDescription().equals(other.getJobsDescription()))
            && (this.getJobsSeq() == null ? other.getJobsSeq() == null : this.getJobsSeq().equals(other.getJobsSeq()))
            && (this.getJobsStarttaskid() == null ? other.getJobsStarttaskid() == null : this.getJobsStarttaskid().equals(other.getJobsStarttaskid()))
            && (this.getJobsDelay() == null ? other.getJobsDelay() == null : this.getJobsDelay().equals(other.getJobsDelay()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table JOBS
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getJobsJobid() == null) ? 0 : getJobsJobid().hashCode());
        result = 31 * result + ((getJobsName() == null) ? 0 : getJobsName().hashCode());
        result = 31 * result + ((getJobsBatchid() == null) ? 0 : getJobsBatchid().hashCode());
        result = 31 * result + ((getJobsDescription() == null) ? 0 : getJobsDescription().hashCode());
        result = 31 * result + ((getJobsSeq() == null) ? 0 : getJobsSeq().hashCode());
        result = 31 * result + ((getJobsStarttaskid() == null) ? 0 : getJobsStarttaskid().hashCode());
        result = 31 * result + ((getJobsDelay() == null) ? 0 : getJobsDelay().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table JOBS
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<JOBS_JOBID>").append(this.jobsJobid).append("</JOBS_JOBID>");
        sb.append("<JOBS_NAME>").append(this.jobsName).append("</JOBS_NAME>");
        sb.append("<JOBS_BATCHID>").append(this.jobsBatchid).append("</JOBS_BATCHID>");
        sb.append("<JOBS_DESCRIPTION>").append(this.jobsDescription).append("</JOBS_DESCRIPTION>");
        sb.append("<JOBS_SEQ>").append(this.jobsSeq).append("</JOBS_SEQ>");
        sb.append("<JOBS_STARTTASKID>").append(this.jobsStarttaskid).append("</JOBS_STARTTASKID>");
        sb.append("<JOBS_DELAY>").append(this.jobsDelay).append("</JOBS_DELAY>");
        return sb.toString();
    }
}