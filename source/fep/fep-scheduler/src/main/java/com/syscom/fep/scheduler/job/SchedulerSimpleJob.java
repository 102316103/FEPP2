package com.syscom.fep.scheduler.job;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;

/**
 * 非單例模式下的的任務
 *
 * @param <JobConfig>
 */
public abstract class SchedulerSimpleJob<JobConfig extends SchedulerJobConfig> extends FEPBase implements SchedulerJobConstant, Job {
    protected static final LogHelper ScheduleLogger = LogHelperFactory.getSchedulerLogger();
    private JobConfig config;

    /**
     * 執行任務
     *
     * @param context
     */
    protected abstract void executeJob(JobExecutionContext context, JobConfig config) throws Exception;

    public void setConfig(JobConfig config) {
        this.config = config;
    }

    protected void putMDC() {
        String mdcProfile = LogMDC.get(Const.MDC_PROFILE);
        LogMDC.put(Const.MDC_PROFILE, StringUtils.isBlank(mdcProfile) ? ProgramName : mdcProfile);
    }

    /**
     * 建立排程
     *
     * @param scheduler
     */
    protected void scheduleJob(Scheduler scheduler) {
        if (config == null) throw ExceptionUtil.createNullPointException("Please set config first!!!");
        JobDetail jobDetail = JobBuilder.newJob(this.getClass()).withIdentity(this.getJobKey(config)).build();
        jobDetail.getJobDataMap().put(JOB_DATA_MAP_KEY_IS_SIMPLE, true);
        jobDetail.getJobDataMap().put(JOB_DATA_MAP_KEY_JOB_CONFIG, config);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(config.getCronExpression());
        CronTrigger cronTrigger = getCronTrigger(scheduleBuilder, config);
        ScheduleLogger.info(ProgramName, "-", config.getIdentity(), " start to schedule...");
        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
            ScheduleLogger.info(ProgramName, "-", config.getIdentity(), " has been scheduled by CronExpression = [", config.getCronExpression(), "]");
        } catch (SchedulerException e) {
            ScheduleLogger.exceptionMsg(e, ProgramName, "-", config.getIdentity(), " schedule failed with exception occur, ", e.getMessage());
        }
    }

    protected CronTrigger getCronTrigger(CronScheduleBuilder scheduleBuilder, JobConfig config) {
        return TriggerBuilder.newTrigger().withIdentity(this.getTriggerKey(config)).withSchedule(scheduleBuilder).build();
    }

    protected JobKey getJobKey(JobConfig config) {
        return JobKey.jobKey(config.getIdentity(), GROUP_NAME);
    }

    protected TriggerKey getTriggerKey(JobConfig config) {
        return TriggerKey.triggerKey(config.getIdentity(), GROUP_NAME);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        putMDC();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        JobConfig config = (JobConfig) jobDataMap.get(JOB_DATA_MAP_KEY_JOB_CONFIG);
        ScheduleLogger.info(ProgramName, "-", config.getIdentity(), " start to execute...");
        try {
            this.executeJob(context, config);
        } catch (Exception e) {
            ScheduleLogger.exceptionMsg(e, ProgramName, "-", config.getIdentity(), " execute failed with exception occur, ", e.getMessage());
        }
        ScheduleLogger.info(ProgramName, "-", config.getIdentity(), " execute finished...");
    }
}
