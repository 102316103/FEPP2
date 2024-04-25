package com.syscom.fep.batch.policy;

import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.junit.jupiter.api.BeforeEach;

import java.util.Calendar;

public class BasePolicyTest {
    protected LogHelper logger = LogHelperFactory.getUnitTestLogger();
    protected BatchJobParameter parameter;

    @BeforeEach
    protected void setup() {
        parameter = new BatchJobParameter();
        parameter.setBatchId("999");
        parameter.setName("DailyRepetition");
        parameter.setDescription("DailyRepetition測試");
    }

    /**
     * 必要時根據policy校正更新一些資訊
     *
     * @param parameter
     * @param conclusion
     */
    protected void updateByPolicy(BatchJobParameter parameter, BatchJobPolicyConclusion conclusion) {
        if (conclusion != null) {
            // 最近一次執行時間
            if (conclusion.getLatestExecutedDateTime() != null) {
                parameter.setLatestExecutedDateTime(conclusion.getLatestExecutedDateTime());
            }
            // 預計下一次執行時間
            if (conclusion.getNextExecutedDateTime() != null) {
                parameter.setNextExecutedDateTime(conclusion.getNextExecutedDateTime());
            }
        }
    }
}
