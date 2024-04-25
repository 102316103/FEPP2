package com.syscom.fep.batch.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.batch.job.BatchJobManager;
import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.batch.job.impl.BatchJobInvoker;
import com.syscom.fep.batch.policy.impl.DailyRepetitionPolicy;
import com.syscom.fep.batch.policy.impl.MonthDayOfWeekPolicy;
import com.syscom.fep.batch.policy.impl.WeeklyPolicy;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.scheduler.CronExpressionGenerator;
import com.syscom.fep.frmcommon.scheduler.enums.WeeksOfTheMonth;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class JobScheduler extends FEPBase {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private BatchConfiguration configuration;
    @Autowired
    private BatchJobManager manager;
    @Autowired
    private CronExpressionGenerator generator;
    @Autowired
    private JobHelper jobHelper;
    private ScheduledExecutorService executor;

    @PostConstruct
    public void updateBatchNextRunTime() {
        // 每隔1分鐘更新下次執行時間
        executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(StringUtils.join(ProgramName, "-updateNextRunTime")));
        executor.scheduleAtFixedRate(() -> {
            LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
            Map<String, Date> taskNextExecutedDateTimeMap = manager.getAllTaskNextExecutedDateTimeMap();
            if (MapUtils.isNotEmpty(taskNextExecutedDateTimeMap)) {
                for (Entry<String, Date> entry : taskNextExecutedDateTimeMap.entrySet()) {
                    String key = entry.getKey();
                    if (StringUtils.isBlank(key)) {
                        continue;
                    }
                    Date nextExecutedDateTime = entry.getValue();
                    try {
                        int batchId = Integer.parseInt(key);
                        jobHelper.updateBatchNextRunTime(batchId, nextExecutedDateTime);
                        logger.info("[updateBatchNextRunTime]batchId = [", batchId, "]", ", nextExecutedDateTime = [", FormatUtil.dateTimeFormat(nextExecutedDateTime), "]");
                    } catch (NumberFormatException e) {
                        logger.warn(e, "BatchId格式不正確:", key);
                    }
                }
            }
        }, configuration.getUpdateNextRunTimeInitialDelayMilliseconds(), configuration.getUpdateNextRunTimeIntervalMilliseconds(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        logger.trace(ProgramName, " start to destroy...");
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                logger.trace(ProgramName, " executor terminate all runnable successful");
            else
                logger.trace(ProgramName, " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }

    public void createDailyTask(String hostName, String batchId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int daysInterval) throws SchedulerException {
        manager.unscheduleJob(batchId);
        Date triggerStartTime = this.parseDateTime(startTime);
        BatchJobParameter parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        String cronExpression = generator.generateCronExpressionByDaily(triggerStartTime, daysInterval);
        manager.scheduleJob(
                parameter,
                new BatchJobInvoker(),
                getTrigger(batchId, triggerStartTime, cronExpression),
                "taskName = [", taskName, "], ",
                "startTime = [", startTime, "], ",
                "daysInterval = [", daysInterval, "], ",
                "cronExpression = [", cronExpression, "]");
    }

    public void createDailyRepetitionTask(String hostName, String batchId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int daysInterval, int minutesInterval, int hoursDuration) throws SchedulerException {
        manager.unscheduleJob(batchId);
        Date triggerStartTime = this.parseDateTime(startTime);
        String cronExpression = null;
        BatchJobParameter parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        try {
            cronExpression = generator.generateCronExpressionByDailyRepetition(triggerStartTime, hoursDuration == 0 ? 0 : daysInterval, minutesInterval, hoursDuration);
        } catch (UnsupportedOperationException e) {
            String errorMessage = e.getMessage();
            // minutesInterval超過60天並且還有零頭, 那麼只能先按照零頭分鐘數
            if (CronExpressionGenerator.CANNOT_GENERATE_CAUSE_MINUTES_INTERVAL_GREATER_AND_EQUALS_THAN_60.equals(errorMessage)) {
                cronExpression = generator.generateCronExpressionByDailyRepetition(triggerStartTime, hoursDuration == 0 ? 0 : daysInterval, minutesInterval % 60, hoursDuration);
                parameter.setPolicyClassname(DailyRepetitionPolicy.class.getName());
                parameter.setHourInterval(minutesInterval / 60);
                parameter.setMinuteInterval(minutesInterval % 60);
            }
        }
        manager.scheduleJob(
                parameter,
                new BatchJobInvoker(),
                getTrigger(batchId, triggerStartTime, cronExpression),
                "taskName = [", taskName, "], ",
                "startTime = [", startTime, "], ",
                "daysInterval = [", hoursDuration == 0 ? 0 : daysInterval, "], ",
                "minutesInterval = [", minutesInterval, "], ",
                "hoursDuration = [", hoursDuration, "], ",
                "cronExpression = [", cronExpression, "]");
    }

    public void createWeeklyTask(String hostName, String batchId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int daysOfWeek, int weeksInterval) throws SchedulerException {
        manager.unscheduleJob(batchId);
        Date triggerStartTime = this.parseDateTime(startTime);
        // 每隔幾周運行一次, 所以要加上1
        if (weeksInterval > 0) {
            weeksInterval += 1;
        }
        BatchJobParameter parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        String cronExpression = null;
        // quartz沒有辦法同時指定daysOfWeek和weeksInterval
        if (daysOfWeek > 0 && weeksInterval > 0) {
            cronExpression = generator.generateCronExpressionByWeekly(this.parseDateTime(startTime), daysOfWeek, 0);
            parameter.setPolicyClassname(WeeklyPolicy.class.getName());
            parameter.setDaysOfWeek(daysOfWeek);
            parameter.setWeeksInterval(weeksInterval);
        } else {
            cronExpression = generator.generateCronExpressionByWeekly(this.parseDateTime(startTime), daysOfWeek, weeksInterval);
        }
        manager.scheduleJob(
                parameter,
                new BatchJobInvoker(),
                getTrigger(batchId, triggerStartTime, cronExpression),
                "taskName = [", taskName, "], ",
                "startTime = [", startTime, "], ",
                "daysOfWeek = [", daysOfWeek, (CollectionUtils.isNotEmpty(parameter.getDaysOfTheWeekList()) ?
                        StringUtils.join("(", StringUtils.join(parameter.getDaysOfTheWeekList().stream().map(t -> t.name()).toArray(), ","), ")") : StringUtils.EMPTY), "], ",
                "weeksInterval = [", weeksInterval, "], ",
                "cronExpression = [", cronExpression, "]");
    }

    public void createMonthlyTask(String hostName, String batchId, String taskName, String taskDescription, String action, String actionArguments, String startTime, String daysOfMonth, int monthsOfYear, boolean runOnLastDayOfMonth) throws SchedulerException {
        manager.unscheduleJob(batchId);
        Date triggerStartTime = this.parseDateTime(startTime);
        BatchJobParameter parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        String cronExpression = null;
        if (StringUtils.isNotBlank(daysOfMonth) && runOnLastDayOfMonth) {
            cronExpression = generator.generateCronExpressionByMonthly(this.parseDateTime(startTime), monthsOfYear, null, false);
            parameter.setPolicyClassname(MonthDayOfWeekPolicy.class.getName());
            parameter.setMonthsOfYear(monthsOfYear);
            parameter.setDaysOfMonth(daysOfMonth);
            parameter.setRunOnLastDayOfMonth(true);
        } else {
            cronExpression = generator.generateCronExpressionByMonthly(this.parseDateTime(startTime), monthsOfYear, daysOfMonth, runOnLastDayOfMonth);
        }
        manager.scheduleJob(
                parameter,
                new BatchJobInvoker(),
                getTrigger(batchId, triggerStartTime, cronExpression),
                "taskName = [", taskName, "], ",
                "startTime = [", startTime, "], ",
                "daysOfMonth = [", daysOfMonth, "], ",
                "monthsOfYear = [", monthsOfYear, (CollectionUtils.isNotEmpty(parameter.getMonthsOfTheYearList()) ?
                        StringUtils.join("(", StringUtils.join(parameter.getMonthsOfTheYearList().stream().map(t -> t.name()).toArray(), ","), ")") : StringUtils.EMPTY), "], ",
                "runOnLastDayOfMonth = [", runOnLastDayOfMonth, "], ",
                "cronExpression = [", cronExpression, "]");
    }

    public void createMonthDayOfWeekTask(String hostName, String batchId, String taskName, String taskDescription, String action, String actionArguments, String startTime, int monthsOfYear, int weekOfMonth, int daysOfWeek, boolean runOnLastWeekOfMonth) throws SchedulerException {
        manager.unscheduleJob(batchId);
        Date triggerStartTime = this.parseDateTime(startTime);
        String cronExpression = null;
        // 2022-09-02 Richard modified for Fortify scan [Null Dereference(High)]
        BatchJobParameter parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments);
        try {
            cronExpression = generator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, weekOfMonth, daysOfWeek, runOnLastWeekOfMonth);
        } catch (UnsupportedOperationException e) {
            String errorMessage = e.getMessage();
            // 又指定某月第幾周的第幾天, 還要指定某月最後一周的第幾天
            if (CronExpressionGenerator.CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK_AND_DAY_OF_WEEK.equals(errorMessage)) {
                cronExpression = generator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, 0, daysOfWeek, false);
                parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments, MonthDayOfWeekPolicy.class);
                parameter.setMonthsOfYear(monthsOfYear);
                parameter.setDaysOfWeek(daysOfWeek);
                parameter.setWeeksOfMonth(weekOfMonth);
                parameter.setRunOnLastWeekOfMonth(true);
            }
            // 任意幾周的任意幾天
            else if (CronExpressionGenerator.CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_DAY_OF_WEEK.equals(errorMessage)) {
                cronExpression = generator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, 0, daysOfWeek, false);
                parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments, MonthDayOfWeekPolicy.class);
                parameter.setMonthsOfYear(monthsOfYear);
                parameter.setDaysOfWeek(daysOfWeek);
                parameter.setWeeksOfMonth(weekOfMonth);
            }
            // 最後一周的第幾天
            else if (CronExpressionGenerator.CANNOT_GENERATE_CAUSE_BOTH_LAST_WEEK_AND_DAY_OF_WEEK.equals(errorMessage)) {
                cronExpression = generator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, 0, daysOfWeek, false);
                parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments, MonthDayOfWeekPolicy.class);
                parameter.setMonthsOfYear(monthsOfYear);
                parameter.setDaysOfWeek(daysOfWeek);
                parameter.setRunOnLastWeekOfMonth(true);
            }
            // 又指定某月第幾周, 還要指定某月最後一周
            else if (CronExpressionGenerator.CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK.equals(errorMessage)) {
                cronExpression = generator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, 0, 0, false);
                parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments, MonthDayOfWeekPolicy.class);
                parameter.setMonthsOfYear(monthsOfYear);
                parameter.setWeeksOfMonth(weekOfMonth);
                parameter.setRunOnLastWeekOfMonth(true);
            }
            // 指定某月第幾周
            else if (CronExpressionGenerator.CANNOT_GENERATE_CAUSE_WEEK.equals(errorMessage)) {
                cronExpression = generator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, 0, 0, false);
                parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments, MonthDayOfWeekPolicy.class);
                parameter.setMonthsOfYear(monthsOfYear);
                parameter.setWeeksOfMonth(weekOfMonth);
            }
            // 指定某月最後一周
            else if (CronExpressionGenerator.CANNOT_GENERATE_CAUSE_LAST_WEEK.equals(errorMessage)) {
                cronExpression = generator.generateCronExpressionByMonthDayOfWeek(triggerStartTime, monthsOfYear, 0, 0, false);
                parameter = BatchJobParameter.create(hostName, batchId, taskName, taskDescription, triggerStartTime, action, actionArguments, MonthDayOfWeekPolicy.class);
                parameter.setMonthsOfYear(monthsOfYear);
                parameter.setRunOnLastWeekOfMonth(true);
            }
        }
        manager.scheduleJob(
                parameter,
                new BatchJobInvoker(),
                getTrigger(batchId, triggerStartTime, cronExpression),
                "taskName = [", taskName, "], ",
                "startTime = [", startTime, "], ",
                "monthsOfYear = [", monthsOfYear, (CollectionUtils.isNotEmpty(parameter.getMonthsOfTheYearList()) ?
                        StringUtils.join("(", StringUtils.join(parameter.getMonthsOfTheYearList().stream().map(t -> t.name()).toArray(), ","), ")") : StringUtils.EMPTY), "], ",
                "weekOfMonth = [", weekOfMonth, (CollectionUtils.isNotEmpty(parameter.getWeeksOfTheMonthList()) ?
                        StringUtils.join("(", StringUtils.join(parameter.getWeeksOfTheMonthList().stream().map(t -> t.name()).toArray(), ","), ")") : StringUtils.EMPTY), "], ",
                "daysOfWeek = [", daysOfWeek, (CollectionUtils.isNotEmpty(parameter.getDaysOfTheWeekList()) ?
                        StringUtils.join("(", StringUtils.join(parameter.getDaysOfTheWeekList().stream().map(t -> t.name()).toArray(), ","), ")") : StringUtils.EMPTY), "], ",
                "runOnLastWeekOfMonth = [", (weekOfMonth == WeeksOfTheMonth.LastWeek.getValue()) || runOnLastWeekOfMonth, "], ",
                "cronExpression = [", cronExpression, "]",
                StringUtils.isNotBlank(parameter.getPolicyClassname()) ? StringUtils.join(", PolicyClassname = [", parameter.getPolicyClassname(), "]") : StringUtils.EMPTY);
    }

    private CronTrigger getTrigger(String batchId, Date triggerStartTime, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .withIdentity(manager.getTriggerKey(batchId, batchId))
                .withDescription(batchId)
                .startAt(triggerStartTime)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

    private Date parseDateTime(String dateTime) {
        if (StringUtils.isNotBlank(dateTime)) {
            try {
                return FormatUtil.parseDataTime(dateTime, FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS);
            } catch (ParseException e) {
                logger.warn("Parse startTime = [", dateTime, "] with ParseException occur, exception message = [", e.getMessage(), "]");
            }
        }
        return Calendar.getInstance().getTime();
    }
}
