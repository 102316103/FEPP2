package com.syscom.fep.batch.policy.impl;

import com.syscom.fep.batch.policy.BasePolicyTest;
import com.syscom.fep.batch.policy.BatchJobPolicyConclusion;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeeklyPolicyTest extends BasePolicyTest {
    @Test
    public void test1() throws Exception {
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksInterval(2);
        executePolicy();
    }

    @Test
    public void test2() throws Exception {
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksInterval(2);
        // 塞入下一個執行日期
        parameter.putNextExecuteDateTimeForDayOfWeek(today.getDayOfWeek(),
                CalendarUtil.dateValue(CalendarUtil.add(Calendar.getInstance(), Calendar.DAY_OF_MONTH, parameter.getWeeksInterval() * 7)));
        executePolicy();
    }

    @Test
    public void test3() throws Exception {
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksInterval(2);
        // 塞入下一個執行日期
        parameter.putNextExecuteDateTimeForDayOfWeek(today.getDayOfWeek(), CalendarUtil.dateValue(Calendar.getInstance()));
        executePolicy();
    }

    @Test
    public void test4() throws Exception {
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksInterval(2);
        // 上一次執行完成後塞入的下一個執行日期
        parameter.putNextExecuteDateTimeForDayOfWeek(today.getDayOfWeek(),
                CalendarUtil.dateValue(CalendarUtil.add(Calendar.getInstance(), Calendar.DAY_OF_MONTH, -7)));
        executePolicy();
    }

    @Test
    public void test5() throws Exception {
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        parameter.setDaysOfWeek(today.getValue());
        parameter.setWeeksInterval(2);
        // 上一次執行完成後塞入的下一個執行日期
        parameter.putNextExecuteDateTimeForDayOfWeek(today.getDayOfWeek(),
                CalendarUtil.dateValue(CalendarUtil.add(Calendar.getInstance(), Calendar.DAY_OF_MONTH, parameter.getWeeksInterval() * -7)));
        executePolicy();
    }

    @Test
    public void test6() throws Exception {
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        // 前一天
        DaysOfTheWeek before = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), -1);
        parameter.setDaysOfWeek(before.getValue() + today.getValue());
        parameter.setWeeksInterval(2);
        executePolicy();
    }

    @Test
    public void test7() throws Exception {
        // 當天
        DaysOfTheWeek today = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), 0);
        // 前一天
        DaysOfTheWeek before = DaysOfTheWeek.getDaysOfTheWeek(Calendar.getInstance(), -1);
        parameter.setDaysOfWeek(before.getValue() + today.getValue());
        parameter.setWeeksInterval(2);
        parameter.putNextExecuteDateTimeForDayOfWeek(today.getDayOfWeek(), CalendarUtil.dateValue(Calendar.getInstance()));
        parameter.putNextExecuteDateTimeForDayOfWeek(before.getDayOfWeek(), CalendarUtil.dateValue(CalendarUtil.add(Calendar.getInstance(), Calendar.DAY_OF_MONTH, -1 + parameter.getWeeksInterval() * 7)));
        executePolicy();
    }

    private void executePolicy() throws InterruptedException {
        WeeklyPolicy policy = new WeeklyPolicy();
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
        }, 0, 10, TimeUnit.SECONDS);
        Thread.sleep(Long.MAX_VALUE);
    }
}
