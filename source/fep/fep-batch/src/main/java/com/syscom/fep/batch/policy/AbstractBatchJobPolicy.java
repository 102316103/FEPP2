package com.syscom.fep.batch.policy;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import org.quartz.JobExecutionContext;

public abstract class AbstractBatchJobPolicy implements BatchJobPolicy {
    protected LogHelper logger = LogHelperFactory.getGeneralLogger();

    /**
     * @param context
     * @param parameter
     * @return
     */
    @Override
    public BatchJobPolicyConclusion execute(JobExecutionContext context, BatchJobParameter parameter) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        logger.debug("Enter ", this.getClass().getSimpleName(), " for ", parameter.getScheduleInfo());
        BatchJobPolicyConclusion conclusion = new BatchJobPolicyConclusion();
        this.execute(context, parameter, conclusion);
        return conclusion;
    }

    protected abstract void execute(JobExecutionContext context, BatchJobParameter parameter, BatchJobPolicyConclusion conclusion);
}
