package com.syscom.fep.batch.policy.impl;

import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.batch.policy.AbstractBatchJobPolicy;
import com.syscom.fep.batch.policy.BatchJobPolicyConclusion;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.scheduler.enums.MonthsOfTheYear;
import com.syscom.fep.frmcommon.scheduler.enums.WeeksOfTheMonth;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 用於檢核月周日的規則
 *
 * @author Richard
 */
@Component
public class MonthDayOfWeekPolicy extends AbstractBatchJobPolicy {
    @Override
    protected void execute(JobExecutionContext context, BatchJobParameter parameter, BatchJobPolicyConclusion conclusion) {
        Calendar today = Calendar.getInstance();
        // 獲取下一個執行日期
        this.setNextExecutedDateTime(parameter, conclusion, today);
        // 如果符合規則, 則skip為false
        if (verifyPolicy(parameter, today)) {
            logger.info(parameter.getLogContent(), "executed on date = [", FormatUtil.dateFormat(today.getTime()), "] and update next execute date = [", FormatUtil.dateFormat(conclusion.getNextExecutedDateTime().getTime()), "]...");
            conclusion.setSkip(false);
        }
        // 否則, 則skip為true
        else {
            logger.warn(parameter.getLogContent(), "incorrect execute date = [", FormatUtil.dateFormat(today.getTime()), "], should execute on = [", FormatUtil.dateFormat(conclusion.getNextExecutedDateTime().getTime()), "], batch job skipped...");
            conclusion.setSkip(true);
        }
    }

    /**
     * 獲取下一個執行日期
     *
     * @param parameter
     * @param conclusion
     * @param today
     */
    private void setNextExecutedDateTime(BatchJobParameter parameter, BatchJobPolicyConclusion conclusion, Calendar today) {
        Calendar nextExecuteDateCal = CalendarUtil.add(today, Calendar.DAY_OF_MONTH, 1);
        while (!this.verifyPolicy(parameter, nextExecuteDateCal)) {
            nextExecuteDateCal = CalendarUtil.add(nextExecuteDateCal, Calendar.DAY_OF_MONTH, 1);
        }
        conclusion.setNextExecutedDateTime(nextExecuteDateCal);
    }

    /**
     * 日期是否符合規則
     *
     * @param parameter
     * @param cal
     * @return
     */
    protected boolean verifyPolicy(BatchJobParameter parameter, Calendar cal) {
        // 檢核是否符合月份
        List<MonthsOfTheYear> monthsOfTheYearList = parameter.getMonthsOfTheYearList();
        if (CollectionUtils.isNotEmpty(monthsOfTheYearList)) {
            int monthOfYear = cal.get(Calendar.MONTH);
            if (monthsOfTheYearList.stream().filter(t -> monthOfYear == t.getMonthOfYear()).findFirst().orElse(null) == null) {
                return false;
            }
        }
        List<Boolean> matchWeekPolicyList = new ArrayList<>();
        List<Boolean> matchDaysPolicyList = new ArrayList<>();
        // 檢核是否符合月份中的第幾周
        List<WeeksOfTheMonth> weeksOfTheMonthList = parameter.getWeeksOfTheMonthList();
        if (CollectionUtils.isNotEmpty(weeksOfTheMonthList)) {
            int weekOfMonth = CalendarUtil.getWeekOfMonth(cal);
            if (weeksOfTheMonthList.stream().filter(t -> weekOfMonth == t.getWeekOfMonth()).findFirst().orElse(null) == null) {
                matchWeekPolicyList.add(false);
            } else {
                matchWeekPolicyList.add(true);
            }
        }
        // 檢核是否符合月份中的最後一周
        if (parameter.isRunOnLastWeekOfMonth()) {
            if (!CalendarUtil.isLastWeekOfMonth(cal)) {
                matchWeekPolicyList.add(false);
            } else {
                matchWeekPolicyList.add(true);
            }
        }
        // 檢核是否符合月份中的某天
        List<Integer> daysOfTheMonthList = parameter.getDaysOfTheMonthList();
        if (CollectionUtils.isNotEmpty(daysOfTheMonthList)) {
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            if (daysOfTheMonthList.stream().filter(t -> dayOfMonth == t.intValue()).findFirst().orElse(null) == null) {
                matchDaysPolicyList.add(false);
            } else {
                matchDaysPolicyList.add(true);
            }
        }
        // 檢核是否符合周內的某天
        List<DaysOfTheWeek> daysOfTheWeekList = parameter.getDaysOfTheWeekList();
        if (CollectionUtils.isNotEmpty(daysOfTheWeekList)) {
            int dayOfWeek = CalendarUtil.getDayOfWeek(cal);
            if (daysOfTheWeekList.stream().filter(t -> dayOfWeek == t.getDayOfWeek()).findFirst().orElse(null) == null) {
                matchDaysPolicyList.add(false);
            } else {
                matchDaysPolicyList.add(true);
            }
        }
        // 檢核是否符合月份中的最後一天
        if (parameter.isRunOnLastDayOfMonth()) {
            if (!CalendarUtil.isLastDayOfMonth(cal)) {
                matchDaysPolicyList.add(false);
            } else {
                matchDaysPolicyList.add(true);
            }
        }
        if (matchWeekPolicyList.size() > 0) {
            // 如果不符合weeks, 則直接return false
            if (matchWeekPolicyList.stream().filter(t -> t == false).count() == matchWeekPolicyList.size()) {
                return false;
            } else {
                // 符合weeks, 還要看days是否也符合
                if (matchDaysPolicyList.size() > 0 && matchDaysPolicyList.stream().filter(t -> t == false).count() == matchDaysPolicyList.size()) {
                    return false;
                }
            }
        } else {
            // 沒有weeks, 那麼只看days就好
            if (matchDaysPolicyList.size() > 0 && matchDaysPolicyList.stream().filter(t -> t == false).count() == matchDaysPolicyList.size()) {
                return false;
            }
        }
        return true;
    }
}
