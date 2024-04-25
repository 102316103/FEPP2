package com.syscom.fep.batch.policy.impl;

import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.batch.policy.AbstractBatchJobPolicy;
import com.syscom.fep.batch.policy.BatchJobPolicyConclusion;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * 用於檢核小時 + 分鐘 間隔的規則
 * <p>
 * 如果當前時間 = 上一次執行時間 + 間隔的小時 + 間隔的分, 則skip為false, 表示執行本次應該執行的任務
 * 如果當前時間 < 上一次執行時間 + 間隔的小時 + 間隔的分, 則skip為true, 表示跳過本次不應該執行的任務
 * 如果當前時間 > 上一次執行時間 + 間隔的小時 + 間隔的分, 則skip為false, 表示補做一次之前misfire的任務
 *
 * @author Richard
 */
@Component
public class DailyRepetitionPolicy extends AbstractBatchJobPolicy {
    @Override
    protected void execute(JobExecutionContext context, BatchJobParameter parameter, BatchJobPolicyConclusion conclusion) {
        // 當前時間
        Calendar now = Calendar.getInstance();
        // 如果LatestExecutedTime為null, 表示第一次運行, 則skip為false
        // 如果和上一次執行時間比較跨天了, 則表示是新的一天第一次運行, 則skip為false
        if (parameter.getLatestExecutedDateTime() == null ||
                CalendarUtil.dateValue(now) > CalendarUtil.dateValue(parameter.getLatestExecutedDateTime())) {
            conclusion.setSkip(false);
            // 計算塞入下一次執行時間
            conclusion.setNextExecutedDateTime(
                    CalendarUtil.add(
                            CalendarUtil.add(now, Calendar.MINUTE, parameter.getMinuteInterval()),
                            Calendar.HOUR_OF_DAY, parameter.getHourInterval()));
            logger.info(parameter.getLogContent(), "executed on time = [", FormatUtil.dateTimeFormat(now.getTime()), "] and update next execute date = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "]...");
            return;
        }
        // 計算下一次執行時間
        // 加入小時
        Calendar nextExecutedTime = CalendarUtil.add(parameter.getLatestExecutedDateTime(), Calendar.HOUR_OF_DAY, parameter.getHourInterval());
        // 加入分
        nextExecutedTime = CalendarUtil.add(nextExecutedTime, Calendar.MINUTE, parameter.getMinuteInterval());
        // 計算差值
        long diff = CalendarUtil.dateTimeValueInHourMinute(now) - CalendarUtil.dateTimeValueInHourMinute(nextExecutedTime);
        // 如果當前時間 = 上一次執行時間 + 間隔的小時 + 間隔的分, 則skip為false, 表示執行本次應該執行的任務
        if (diff == 0) {
            conclusion.setSkip(false);
            // 計算塞入下一次執行時間
            conclusion.setNextExecutedDateTime(
                    CalendarUtil.add(
                            CalendarUtil.add(nextExecutedTime, Calendar.MINUTE, parameter.getMinuteInterval()),
                            Calendar.HOUR_OF_DAY, parameter.getHourInterval()));
            logger.info(parameter.getLogContent(), "executed on time = [", FormatUtil.dateTimeFormat(now.getTime()), "] and update next execute date = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "]...");
        }
        // 如果當前時間 < 上一次執行時間 + 間隔的小時 + 間隔的分, 則skip為true, 表示跳過本次不應該執行的任務
        else if (diff < 0) {
            conclusion.setSkip(true);
            // 塞入下一次執行時間
            conclusion.setNextExecutedDateTime(nextExecutedTime);
            logger.warn(parameter.getLogContent(), "incorrect run time = [", FormatUtil.dateTimeFormat(now.getTime()), "], should run on = [", FormatUtil.dateTimeFormat(nextExecutedTime.getTime()), "], batch job skipped...");
        }
        // 如果當前時間 > 上一次執行時間 + 間隔的小時 + 間隔的分, 則skip為false, misfire為true
        else {
            conclusion.setSkip(false);
            conclusion.setMisfired(true);
            // 循環計算出下一次應該要執行的時間
            while (diff > 0) {
                // 加入小時
                nextExecutedTime = CalendarUtil.add(nextExecutedTime, Calendar.HOUR_OF_DAY, parameter.getHourInterval());
                // 加入分
                nextExecutedTime = CalendarUtil.add(nextExecutedTime, Calendar.MINUTE, parameter.getMinuteInterval());
                // 計算差值
                diff = CalendarUtil.dateTimeValueInHourMinute(now) - CalendarUtil.dateTimeValueInHourMinute(nextExecutedTime);
            }
            // 塞入下一次執行時間
            conclusion.setNextExecutedDateTime(nextExecutedTime);
            // 塞入上一次應該要執行時間
            conclusion.setLatestExecutedDateTime(
                    CalendarUtil.add(
                            CalendarUtil.add(nextExecutedTime, Calendar.MINUTE, -1 * parameter.getMinuteInterval()),
                            Calendar.HOUR_OF_DAY, -1 * parameter.getHourInterval())
            );
            logger.warn(parameter.getLogContent(), "was misfire latest = [", FormatUtil.dateTimeFormat(conclusion.getLatestExecutedDateTime().getTime()), "], and next executed = [", FormatUtil.dateTimeFormat(conclusion.getNextExecutedDateTime().getTime()), "] on time");
        }
    }
}
