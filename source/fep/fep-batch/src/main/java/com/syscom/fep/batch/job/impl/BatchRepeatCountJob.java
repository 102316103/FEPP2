package com.syscom.fep.batch.job.impl;

import java.lang.reflect.Method;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.springframework.util.ReflectionUtils;

import com.syscom.fep.batch.job.BatchJob;
import com.syscom.fep.batch.job.BatchJobManager;
import com.syscom.fep.batch.job.BatchJobParameter;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;

public abstract class BatchRepeatCountJob extends BatchJob {

    protected abstract void executeJobOnce(JobExecutionContext context, BatchJobParameter parameter, int timesTriggered) throws Exception;

    @Override
    protected void executeJob(JobExecutionContext context, BatchJobParameter parameter) throws Exception {
        BatchJobManager manager = SpringBeanFactoryUtil.getBean(BatchJobManager.class);
        if (parameter.getTotalTriggered() == 1) {
            logger.info(parameter.getLogContent(), "start to execute once...");
            try {
                executeJobOnce(context, parameter, 1);
            } finally {
                // 一定要放在finally中
                manager.unscheduleJob(parameter);
            }
        } else if (parameter.getTotalTriggered() > 1) {
            Trigger trigger = context.getTrigger();
            Method method = ReflectionUtils.findMethod(trigger.getClass(), "getTimesTriggered");
            if (method == null) {
                throw ExceptionUtil.createIllegalArgumentException("Cannot find method = [getTimesTriggered()] for incorrect trigger class = [", trigger.getClass().getName(), "]");
            }
            int timesTriggered = this.getTimesTriggered(context, true);
            logger.info(parameter.getLogContent(), "start to execute, timesTriggered = [", timesTriggered, "] ...");
            try {
                executeJobOnce(context, parameter, timesTriggered);
            } finally {
                // 一定要放在finally中
                if (timesTriggered == parameter.getTotalTriggered()) {
                    manager.unscheduleJob(parameter);
                }
            }
        } else {
            int timesTriggered = this.getTimesTriggered(context, false);
            executeJobOnce(context, parameter, timesTriggered);
        }
    }

    private int getTimesTriggered(JobExecutionContext context, boolean throwException) {
        Trigger trigger = context.getTrigger();
        Method method = ReflectionUtils.findMethod(trigger.getClass(), "getTimesTriggered");
        if (method == null) {
            if (throwException) {
                throw ExceptionUtil.createIllegalArgumentException("Cannot find method = [getTimesTriggered()] for incorrect trigger class = [", trigger.getClass().getName(), "]");
            } else {
                return -1;
            }
        }
        int timesTriggered = (int) ReflectionUtils.invokeMethod(method, trigger);
        return timesTriggered;
    }
}
