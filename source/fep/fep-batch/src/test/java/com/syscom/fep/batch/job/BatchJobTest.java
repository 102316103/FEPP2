package com.syscom.fep.batch.job;

import com.syscom.fep.batch.job.impl.BatchRepeatCountJob;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.quartz.JobExecutionContext;

public class BatchJobTest extends BatchRepeatCountJob {
    @Override
    protected void executeJobOnce(JobExecutionContext context, BatchJobParameter parameter, int timesTriggered) throws Exception {
        LogHelperFactory.getGeneralLogger().error(ExceptionUtil.createNullPointException("我開始執行任務哦" + FormatUtil.dateTimeInMillisFormat(System.currentTimeMillis())));
    }
}
