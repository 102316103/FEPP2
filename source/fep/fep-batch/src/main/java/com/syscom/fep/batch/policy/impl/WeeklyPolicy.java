package com.syscom.fep.batch.policy.impl;

import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.batch.policy.AbstractBatchJobPolicy;
import com.syscom.fep.batch.policy.BatchJobPolicyConclusion;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 用於檢核當前日期是否是每隔weeksInterval的DaysOfWeek
 *
 * @author Richard
 */
@Component
public class WeeklyPolicy extends AbstractBatchJobPolicy {
    @Override
    protected void execute(JobExecutionContext context, BatchJobParameter parameter, BatchJobPolicyConclusion conclusion) {
        Calendar now = Calendar.getInstance();
        int current = CalendarUtil.dateValue(now);
        int dayOfWeek = CalendarUtil.getDayOfWeek(now);
        // 先為其他的DayOfWeek塞入下一次執行時間
        this.putNextExecuteDateTimeForOtherDayOfWeek(parameter, conclusion, now, dayOfWeek);
        // 在看當日的
        int nextExecuteDate = parameter.getNextExecutedDateTimeForDayOfWeek(dayOfWeek);
        if (nextExecuteDate != -1) {
            // 未到執行日期, 則skip為true
            if (current < nextExecuteDate) {
                conclusion.setSkip(true);
                conclusion.setNextExecutedDateTime(CalendarUtil.parseDateValue(parameter.getLatestNextRunTimeForDayOfWeek()));
                logger.warn(parameter.getLogContent(), "incorrect execute date = [", FormatUtil.dateFormat(now.getTime()), "], should execute on = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "], batch job skipped...");
            }
            // 該dayOfWeek正常執行, 則skip為false
            else if (current == nextExecuteDate) {
                executeOnDate(parameter, conclusion, now, dayOfWeek);
                logger.info(parameter.getLogContent(), "executed on date = [", FormatUtil.dateFormat(now.getTime()), "] and update next execute date = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "]...");
            }
            // 超過執行日期
            else {
                executeOverDate(parameter, conclusion, now, current, dayOfWeek, nextExecuteDate);
            }
        } else {
            // 該dayOfWeek第一次執行, 則skip為false
            executeOnDate(parameter, conclusion, now, dayOfWeek);
            logger.info(parameter.getLogContent(), "first executed on date = [", FormatUtil.dateFormat(now.getTime()), "] and update next execute date = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "]...");
        }
    }

    /**
     * 正常執行, skip為false, 同時下一個weeksInterval * 7天, 為該dayOfWeek的執行日期
     *
     * @param parameter
     * @param conclusion
     * @param now
     * @param dayOfWeek
     */
    private void executeOnDate(BatchJobParameter parameter, BatchJobPolicyConclusion conclusion, Calendar now, int dayOfWeek) {
        Calendar nextExecutedDate = CalendarUtil.add(now, Calendar.DAY_OF_MONTH, parameter.getWeeksInterval() * 7);
        parameter.putNextExecuteDateTimeForDayOfWeek(dayOfWeek, CalendarUtil.dateValue(nextExecutedDate));
        logger.info(parameter.getLogContent(), "NextExecuteDateTimeForDayOfWeekMap has been update = [", parameter.getNextExecuteDateTimeForDayOfWeekMap(), "]");
        conclusion.setSkip(false);
        conclusion.setNextExecutedDateTime(CalendarUtil.parseDateValue(parameter.getLatestNextRunTimeForDayOfWeek()));
    }

    /**
     * 超過了上一次應該要執行的日期
     *
     * @param parameter
     * @param conclusion
     * @param now
     * @param current
     * @param dayOfWeek
     * @param nextExecuteDate
     */
    private void executeOverDate(BatchJobParameter parameter, BatchJobPolicyConclusion conclusion, Calendar now, int current, int dayOfWeek, int nextExecuteDate) {
        Calendar nextExecuteDateCal = CalendarUtil.add(CalendarUtil.parseDateValue(nextExecuteDate), Calendar.DAY_OF_MONTH, parameter.getWeeksInterval() * 7);
        while (true) {
            // 如果距上一次執行日期, 剛好在WeeksInterval, 則skip為false
            if (current == CalendarUtil.dateValue(nextExecuteDateCal)) {
                executeOnDate(parameter, conclusion, now, dayOfWeek);
                conclusion.setSkip(false);
                conclusion.setMisfired(true);
                logger.info(parameter.getLogContent(), "executed on date (over latest) = [", FormatUtil.dateFormat(now.getTime()), "] and update next execute date = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "]...");
                break;
            }
            // 如果距上一次執行日期, 不在WeeksInterval, 並且已經超過當前日期, 則skip為true
            else if (current < CalendarUtil.dateValue(nextExecuteDateCal)) {
                parameter.putNextExecuteDateTimeForDayOfWeek(dayOfWeek, CalendarUtil.dateValue(nextExecuteDateCal));
                conclusion.setSkip(true);
                conclusion.setMisfired(true);
                conclusion.setNextExecutedDateTime(CalendarUtil.parseDateValue(parameter.getLatestNextRunTimeForDayOfWeek()));
                logger.warn(parameter.getLogContent(), "incorrect execute date (over latest) = [", FormatUtil.dateFormat(now.getTime()), "], should execute on = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "], batch job skipped...");
                break;
            }
            nextExecuteDateCal = CalendarUtil.add(nextExecuteDateCal, Calendar.DAY_OF_MONTH, parameter.getWeeksInterval() * 7);
        }
    }

    /**
     * 因為有可能執行時, 其他非當天的DayOfWeek沒有對應的下次執行時間, 則需要判斷塞入下一次執行時間
     *
     * @param parameter
     * @param conclusion
     * @param now
     * @param dayOfWeek
     */
    private void putNextExecuteDateTimeForOtherDayOfWeek(BatchJobParameter parameter, BatchJobPolicyConclusion conclusion, Calendar now, int dayOfWeek) {
        if (CollectionUtils.isNotEmpty(parameter.getDaysOfTheWeekList())) {
            for (DaysOfTheWeek daysOfTheWeek : parameter.getDaysOfTheWeekList()) {
                // DayOfWeek為當日, 或者是對應DayOfWeek已經有下一個執行時間的資料, 則跳過不處理
                if (daysOfTheWeek.getDayOfWeek() == dayOfWeek || parameter.getNextExecutedDateTimeForDayOfWeek(daysOfTheWeek.getDayOfWeek()) != -1) {
                    continue;
                }
                Calendar nextExecuteDateTime = null;
                // 小於當日的DayOfWeek, 則從下一周算起
                if (daysOfTheWeek.getDayOfWeek() < dayOfWeek) {
                    nextExecuteDateTime = CalendarUtil.add(now, Calendar.DAY_OF_MONTH, 7 - (dayOfWeek - daysOfTheWeek.getDayOfWeek()));
                }
                // 大於當日的DayOfWeek, 則從本週算起
                else {
                    nextExecuteDateTime = CalendarUtil.add(now, Calendar.DAY_OF_MONTH, daysOfTheWeek.getDayOfWeek() - dayOfWeek);
                }
                parameter.putNextExecuteDateTimeForDayOfWeek(daysOfTheWeek.getDayOfWeek(), CalendarUtil.dateValue(nextExecuteDateTime));
                logger.info(parameter.getLogContent(), "set next execute date = [", FormatUtil.dateTimeFormat(nextExecuteDateTime.getTime()), "] for day of week = [", daysOfTheWeek.name(), "]...");
            }
            conclusion.setNextExecutedDateTime(CalendarUtil.parseDateValue(parameter.getLatestNextRunTimeForDayOfWeek()));
        }
    }
}
