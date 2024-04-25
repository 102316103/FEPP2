package com.syscom.fep.batch.quartz;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Calendar;
import java.util.HashSet;

public class QuartzTest {
    private final static LogHelper logger = LogHelperFactory.getUnitTestLogger();

    @Test
    public void test() throws Exception {
        EnvokerJob job = new EnvokerJob();
        JobDetail jobDetail = JobBuilder.newJob(job.getClass())
                .build();
        CalendarIntervalTrigger calendarIntervalTrigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                        .withInterval(1, IntervalUnit.MINUTE))
                .build();
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduler.scheduleJob(jobDetail, calendarIntervalTrigger);
        scheduler.start();
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void test1() {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            // 實例化job
            JobDetail job1 = JobBuilder.newJob(SimpleJob.class)
                    .withIdentity("job1", "group1")
                    .build();
            // 定義Contrigger
            CronTrigger cronTrigger1 = TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(" 0/5 * * * * ? "))
                    .build();
            CronTrigger cronTrigger2 = TriggerBuilder.newTrigger()
                    .withIdentity("trigger2", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(" 0/10 * * * * ? "))
                    .build();
            // 調度器綁定Trigger
            scheduler.scheduleJob(job1, SetUtils.hashSet(cronTrigger1, cronTrigger2), false);
            scheduler.scheduleJob(job1, cronTrigger2);
            scheduler.start();
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void verifyCronExpression() {
        logger.info(CronExpression.isValidExpression("0 30 12 1/7 * ?"));
        logger.info(CronExpression.isValidExpression("0 30 12 ? * MON-THU"));
        logger.info(CronExpression.isValidExpression("0 30 12 L JAN,FEB ?"));
        logger.info(CronExpression.isValidExpression("0 30 12 1,2,3 JAN,FEB ?"));
        logger.info(CronExpression.isValidExpression("0 30 12 ? JAN MON/2"));
        logger.info(CronExpression.isValidExpression("0 30 12 ? JAN L"));
    }

    @Test
    public void test3() throws Exception {
        SimpleJob job = new SimpleJob();
        JobDetail jobDetail = JobBuilder.newJob(job.getClass()).build();
        CalendarIntervalTrigger calendarIntervalTrigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                        .withIntervalInMinutes(61)
                        .withIntervalInDays(2)
                )
                .build();
        DailyTimeIntervalTrigger dailyTimeIntervalTrigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                        .startingDailyAt(TimeOfDay.hourAndMinuteOfDay(0, 0))
                        .endingDailyAt(TimeOfDay.hourAndMinuteOfDay(12, 0))
                )
                .build();
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduler.scheduleJob(jobDetail, calendarIntervalTrigger);
        scheduler.start();
        Thread.sleep(Long.MAX_VALUE);
    }

    public static class EnvokerJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                SchedulerFactory schedulerFactory = new StdSchedulerFactory();
                Scheduler scheduler = schedulerFactory.getScheduler();
                SimpleJob job = new SimpleJob();
                JobDetail jobDetail = JobBuilder.newJob(job.getClass())
                        .withIdentity("test")
                        .build();
                SimpleTrigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("test")
                        .startNow()
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(1)
                                .repeatForever())
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
                scheduler.start();
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SimpleJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            logger.info(FormatUtil.dateTimeFormat(Calendar.getInstance().getTime()));
            // logger.info(((SimpleTrigger) context.getTrigger()).getTimesTriggered());
            logger.info(FormatUtil.dateTimeFormat(context.getNextFireTime()));
        }
    }
}
