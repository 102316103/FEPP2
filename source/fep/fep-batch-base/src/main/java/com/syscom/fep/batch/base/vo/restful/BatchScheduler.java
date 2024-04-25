package com.syscom.fep.batch.base.vo.restful;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.quartz.Trigger;

import java.io.Serializable;
import java.util.Date;

@XStreamAlias("BatchScheduler")
public class BatchScheduler implements Serializable {
    @XStreamAlias("BatchId")
    private String batchId;
    @XStreamAlias("TriggerState")
    private Trigger.TriggerState triggerState;
    @XStreamAlias("CronExpression")
    private String cronExpression;
    @XStreamAlias("NextQuartzFireTime")
    private Date nextQuartzFireTime;
    @XStreamAlias("NextExecutedDateTime")
    private Date nextExecutedDateTime;
    @XStreamAlias("ExecutedHostName")
    private String executedHostName;

    public BatchScheduler() {
    }

    public BatchScheduler(String batchId, String executedHostName) {
        this.batchId = batchId;
        this.executedHostName = executedHostName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Trigger.TriggerState getTriggerState() {
        return triggerState;
    }

    public void setTriggerState(Trigger.TriggerState triggerState) {
        this.triggerState = triggerState;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Date getNextQuartzFireTime() {
        return nextQuartzFireTime;
    }

    public void setNextQuartzFireTime(Date nextQuartzFireTime) {
        this.nextQuartzFireTime = nextQuartzFireTime;
    }

    public Date getNextExecutedDateTime() {
        return nextExecutedDateTime;
    }

    public void setNextExecutedDateTime(Date nextExecutedDateTime) {
        this.nextExecutedDateTime = nextExecutedDateTime;
    }

    public String getExecutedHostName() {
        return executedHostName;
    }

    public void setExecutedHostName(String executedHostName) {
        this.executedHostName = executedHostName;
    }
}
