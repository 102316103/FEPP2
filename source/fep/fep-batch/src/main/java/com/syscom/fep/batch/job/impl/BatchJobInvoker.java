package com.syscom.fep.batch.job.impl;

import org.quartz.JobExecutionContext;

import com.syscom.fep.batch.invoker.JobSchedulerInvoker;
import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;

public class BatchJobInvoker extends BatchRepeatCountJob {

	@Override
	protected void executeJobOnce(JobExecutionContext context, BatchJobParameter parameter, int timesTriggered) throws Exception {
		JobSchedulerInvoker jobSchedulerInvoker = SpringBeanFactoryUtil.getBean(parameter.getAction());
		jobSchedulerInvoker.invoke(parameter.getActionArguments().split("\\s+"));
	}
}
