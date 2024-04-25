package com.syscom.fep.batch.policy;

import com.syscom.fep.batch.job.BatchJobParameter;
import org.quartz.JobExecutionContext;

public interface BatchJobPolicy {

    BatchJobPolicyConclusion execute(JobExecutionContext context, BatchJobParameter parameter);

}
