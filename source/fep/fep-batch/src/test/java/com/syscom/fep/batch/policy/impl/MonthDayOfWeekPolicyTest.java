package com.syscom.fep.batch.policy.impl;

import com.syscom.fep.batch.policy.BasePolicyTest;
import com.syscom.fep.batch.policy.BatchJobPolicyConclusion;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.scheduler.enums.MonthsOfTheYear;
import com.syscom.fep.frmcommon.scheduler.enums.WeeksOfTheMonth;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonthDayOfWeekPolicyTest extends BasePolicyTest {
    @Test
    public void test1() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.AllMonths.getValue());
        parameter.setDaysOfMonth(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
        parameter.setRunOnLastDayOfMonth(false);
        executePolicy(1);
    }

    @Test
    public void test2() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.getMonthsOfTheYear(Calendar.getInstance(), 0).getValue());
        parameter.setDaysOfMonth(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
        parameter.setRunOnLastDayOfMonth(false);
        executePolicy(1);
    }

    @Test
    public void test3() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.AllMonths.getValue());
        parameter.setDaysOfMonth(String.valueOf(CalendarUtil.add(Calendar.getInstance(), Calendar.DAY_OF_MONTH, 1).get(Calendar.DAY_OF_MONTH)));
        parameter.setRunOnLastDayOfMonth(false);
        executePolicy(1);
    }

    @Test
    public void test4() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.getMonthsOfTheYear(Calendar.getInstance(), 0).getValue());
        parameter.setDaysOfMonth(String.valueOf(CalendarUtil.add(Calendar.getInstance(), Calendar.DAY_OF_MONTH, 1).get(Calendar.DAY_OF_MONTH)));
        parameter.setRunOnLastDayOfMonth(false);
        executePolicy(1);
    }

    @Test
    public void test5() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.AllMonths.getValue());
        parameter.setDaysOfMonth(StringUtils.join(new String[]  {
                String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),
                String.valueOf(CalendarUtil.add(Calendar.getInstance(), Calendar.DAY_OF_MONTH, 2).get(Calendar.DAY_OF_MONTH))
        }, ","));
        parameter.setRunOnLastDayOfMonth(false);
        executePolicy(1);
    }

    @Test
    public void test6() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.AllMonths.getValue());
        parameter.setDaysOfWeek(DaysOfTheWeek.AllDays.getValue());
        parameter.setWeeksOfMonth(WeeksOfTheMonth.AllWeeks.getValue());
        parameter.setRunOnLastDayOfMonth(true);
        executePolicy(1);
    }

    @Test
    public void test7() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.AllMonths.getValue());
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksOfMonth(WeeksOfTheMonth.AllWeeks.getValue());
        parameter.setRunOnLastDayOfMonth(true);
        executePolicy(1);
    }

    @Test
    public void test8() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.getMonthsOfTheYear(Calendar.getInstance(), 1).getValue());
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksOfMonth(WeeksOfTheMonth.AllWeeks.getValue());
        parameter.setRunOnLastDayOfMonth(true);
        executePolicy(1);
    }

    @Test
    public void test9() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.getMonthsOfTheYear(Calendar.getInstance(), 1).getValue());
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 1);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksOfMonth(WeeksOfTheMonth.AllWeeks.getValue());
        parameter.setRunOnLastDayOfMonth(true);
        executePolicy(1);
    }

    @Test
    public void test10() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.getMonthsOfTheYear(Calendar.getInstance(), 1).getValue());
        parameter.setDaysOfWeek(DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0).getValue());
        parameter.setWeeksOfMonth(WeeksOfTheMonth.getWeeksOfTheMonth(Calendar.getInstance(),1).getValue());
        parameter.setRunOnLastDayOfMonth(true);
        executePolicy(1);
    }

    @Test
    public void test11() throws Exception {
        parameter.setMonthsOfYear(MonthsOfTheYear.getMonthsOfTheYear(Calendar.getInstance(), 0).getValue());
        parameter.setDaysOfWeek(DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 1).getValue());
        parameter.setWeeksOfMonth(WeeksOfTheMonth.getWeeksOfTheMonth(Calendar.getInstance(),-1).getValue());
        executePolicy(1);
    }

    private void executePolicy(int period) throws InterruptedException {
        MonthDayOfWeekPolicy policy = new MonthDayOfWeekPolicy();
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            BatchJobPolicyConclusion conclusion = new BatchJobPolicyConclusion();
            try {
                policy.execute(null, parameter, conclusion);
                if (conclusion.isSkip()) {
                    logger.info(parameter.getLogContent(), "skip to execute...");
                    return;
                }
                parameter.setLatestExecutedDateTime(Calendar.getInstance());
            } finally {
                this.updateByPolicy(parameter, conclusion);
            }
        }, 0, period, TimeUnit.SECONDS);
        Thread.sleep(Long.MAX_VALUE);
    }
}
