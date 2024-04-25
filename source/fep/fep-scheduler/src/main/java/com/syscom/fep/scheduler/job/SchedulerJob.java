package com.syscom.fep.scheduler.job;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.GenericTypeUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * 單例模式下的任務
 *
 * @param <JobConfig>
 */
public abstract class SchedulerJob<JobConfig extends SchedulerJobConfig> extends FEPBase implements SchedulerJobConstant, Job {
    protected static final LogHelper ScheduleLogger = LogHelperFactory.getSchedulerLogger();
    private final Class<JobConfig> genericClass = GenericTypeUtil.getGenericSuperClass(this.getClass(), 0);
    @Autowired
    private SchedulerJobManager manager;
    @Autowired
    protected JobConfig config;

    @PostConstruct
    public void init() {
        manager.addJob(this);
    }

    /**
     * 執行任務
     *
     * @param context
     */
    protected abstract void executeJob(JobExecutionContext context, JobConfig config) throws Exception;

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
        JobDetail jobDetail = JobBuilder.newJob(this.getClass()).withIdentity(this.getJobKey()).build();
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(config.getCronExpression());
        CronTrigger cronTrigger = getCronTrigger(scheduleBuilder);
        ScheduleLogger.info(ProgramName, " start to schedule...");
        try {
            scheduler.scheduleJob(jobDetail, cronTrigger);
            ScheduleLogger.info(ProgramName, " has been scheduled by CronExpression = [", config.getCronExpression(), "]");
        } catch (SchedulerException e) {
            ScheduleLogger.exceptionMsg(e, ProgramName, " schedule failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 獲取配置項
     *
     * @return
     */
    protected JobConfig getJobConfig() {
        if (config == null) {
            config = SpringBeanFactoryUtil.getBean(genericClass);
        }
        return config;
    }

    /**
     * 如果要自定義觸發器，可以在子類中覆寫此方法
     *
     * @return
     */
    protected CronTrigger getCronTrigger(CronScheduleBuilder scheduleBuilder) {
        return TriggerBuilder.newTrigger().withIdentity(this.getTriggerKey()).withSchedule(scheduleBuilder).build();
    }

    protected JobKey getJobKey() {
        return JobKey.jobKey(ProgramName, GROUP_NAME);
    }

    protected TriggerKey getTriggerKey() {
        return TriggerKey.triggerKey(ProgramName, GROUP_NAME);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        putMDC();
        ScheduleLogger.info(ProgramName, " start to execute...");
        try {
            this.executeJob(context, this.config);
        } catch (Exception e) {
            ScheduleLogger.exceptionMsg(e, e.getMessage());
        }
        ScheduleLogger.info(ProgramName, " execute finished...");
    }
}
