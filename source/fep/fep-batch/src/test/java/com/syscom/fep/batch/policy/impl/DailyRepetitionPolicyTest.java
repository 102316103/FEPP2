package com.syscom.fep.batch.policy.impl;

import com.syscom.fep.batch.policy.BasePolicyTest;
import com.syscom.fep.batch.policy.BatchJobPolicyConclusion;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyRepetitionPolicyTest extends BasePolicyTest {
    @Test
    public void testWithMinuteInterval() throws Exception {
        parameter.setMinuteInterval(61);
        executePolicy(5);
    }

    @Test
    public void testWithMinuteInterval2() throws Exception {
        parameter.setMinuteInterval(1);
        parameter.setLatestExecutedDateTime(Calendar.getInstance());
        executePolicy(10);
    }

    @Test
    public void testWithMinuteInterval3() throws Exception {
        parameter.setMinuteInterval(1);
        parameter.setLatestExecutedDateTime(CalendarUtil.add(Calendar.getInstance(), Calendar.MINUTE, -10));
        executePolicy(10);
    }

    private void executePolicy(int period) throws InterruptedException {
        DailyRepetitionPolicy policy = new DailyRepetitionPolicy();
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
