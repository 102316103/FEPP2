package com.syscom.fep.batch.job;

import org.quartz.JobKey;
import org.quartz.Scheduler;

public interface BatchSchedulerHandler {
    void handler(Scheduler scheduler, JobKey jobKey);
}
