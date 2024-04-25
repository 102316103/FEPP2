package com.syscom.fep.batch.job;

import com.syscom.fep.batch.BatchBaseTest;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.batch.job.impl.BatchJobInvoker;
import com.syscom.fep.batch.job.impl.BatchRepeatCountJob;
import com.syscom.fep.frmcommon.scheduler.CronExpressionGenerator;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Calendar;
import java.util.Date;

public class BatchJobManagerTester extends BatchBaseTest {
    @Autowired
    private BatchJobManager manager;
    @Autowired
    private CronExpressionGenerator generator;
    @Autowired
    @Qualifier(BatchConfiguration.SCHEDULER_JOB_FACTORY_SCHEDULER_NAME)
    private SchedulerFactoryBean batchSchedulerFactoryBean;

    @Test
    public void test() throws Exception {
        Date triggerStartTime = CalendarUtil.add(Calendar.getInstance(), Calendar.SECOND, 10).getTime();
        BatchJobParameter parameter = BatchJobParameter.create("FEPAP1", "999", "Test", "Test", triggerStartTime, "", "");
        parameter.setTotalTriggered(1);
        String cronExpression = generator.generateCronExpressionByDaily(triggerStartTime, 0);
        unscheduleJob(parameter, batchSchedulerFactoryBean.getScheduler());
        manager.scheduleJob(
                parameter,
                new BatchJobTest(),
                getTrigger("999", triggerStartTime, cronExpression),
                "taskName = [test], ",
                "startTime = [now], ",
                "daysInterval = [", 0, "], ",
                "cronExpression = [", cronExpression, "]");
        Thread.sleep(Long.MAX_VALUE);
    }

    private void unscheduleJob(BatchJobParameter parameter, Scheduler scheduler) throws SchedulerException {
        logger.info(parameter.getLogContent(), "start to unscheduled and delete...");
        JobKey jobKey = manager.getJobKey(parameter.getBatchId(), parameter.getGroup());
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
            logger.info(parameter.getLogContent(), "paused successful");
            scheduler.unscheduleJob(manager.getTriggerKey(parameter.getBatchId(), parameter.getGroup()));
            logger.info(parameter.getLogContent(), "unscheduled successful");
            scheduler.deleteJob(jobKey);
            logger.info(parameter.getLogContent(), "deleted successful");
        } else {
            logger.info(parameter.getLogContent(), "triggerKey not exist, no need to unschedule");
        }
    }

    private CronTrigger getTrigger(String batchId, Date triggerStartTime, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .withIdentity(manager.getTriggerKey(batchId, batchId))
                .withDescription(batchId)
                .startAt(triggerStartTime)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }
}
