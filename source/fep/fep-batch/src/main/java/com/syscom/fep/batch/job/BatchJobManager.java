package com.syscom.fep.batch.job;

import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.syscom.fep.batch.base.vo.restful.request.ListSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.request.OperateSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.response.ListSchedulerResponse;
import com.syscom.fep.batch.base.vo.restful.response.OperateSchedulerResponse;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.ActionListener;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@DependsOn("batchConfiguration")
public class BatchJobManager {
    private LogHelper logger = LogHelperFactory.getGeneralLogger();
    private final Set<BatchJobParameter> batchJobParameterSet = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    @Qualifier(BatchConfiguration.SCHEDULER_JOB_FACTORY_SCHEDULER_NAME)
    private SchedulerFactoryBean batchSchedulerFactoryBean;

    @Autowired
    private FEPConfig fepConfig;

    @PostConstruct
    public void init() {
        // 初始化的時候, 取出param加入到batchJobParameterSet中
        this.handleAllScheduler((scheduler, jobKey) -> {
            try {
                JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
                BatchJobParameter parameter = (BatchJobParameter) jobDataMap.get(BatchJobParameter.getJobDataMapKey());
                if (parameter != null) {
                    this.batchJobParameterSet.add(parameter);
                }
            } catch (SchedulerException e) {
                logger.error(e, "init failed!!");
            }
        });
    }

    public void scheduleJob(BatchJobParameter parameter, BatchJob job, Trigger trigger, Object... scheduleInfo) throws SchedulerException {
        this.batchJobParameterSet.add(parameter);
        this.scheduleJob(job, parameter, batchSchedulerFactoryBean.getScheduler(), trigger, scheduleInfo);
    }

    public void unscheduleJob(String batchId) throws SchedulerException {
        BatchJobParameter parameter = this.batchJobParameterSet.stream().filter(t -> t.getBatchId().equals(batchId)).findFirst().orElse(null);
        if (parameter != null) {
            this.unscheduleJob(parameter);
        } else {
            logger.warn("Cannot unschedule job cause job not exist, batchId = [", batchId, "]");
        }
    }

    public void unscheduleJob(BatchJobParameter parameter) throws SchedulerException {
        if (this.batchJobParameterSet.contains(parameter)) {
            this.unscheduleJob(parameter, batchSchedulerFactoryBean.getScheduler());
            this.batchJobParameterSet.remove(parameter);
            logger.info("Job has been unscheduled and removed, batchId = [", parameter.getBatchId(), "]");
        } else {
            logger.warn("Cannot unschedule job cause job not exist, batchId = [", parameter.getBatchId(), "]");
        }
    }

    private void scheduleJob(Job job, BatchJobParameter parameter, Scheduler scheduler, Trigger trigger, Object... scheduleInfo) throws SchedulerException {
        parameter.setScheduleInfo(StringUtils.join(scheduleInfo));
        logger.info(parameter.getLogContent(), "start to schedule...");
        JobDetail jobDetail = JobBuilder.newJob(job.getClass())
                .withIdentity(this.getJobKey(parameter.getBatchId(), parameter.getGroup()))
                .withDescription(parameter.getBatchId())
                .storeDurably(true)
                .build();
        jobDetail.getJobDataMap().put(BatchJobParameter.getJobDataMapKey(), parameter);
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
        logger.info(parameter.getLogContent(), "scheduled successful, ", parameter.getScheduleInfo());
    }

    private void unscheduleJob(BatchJobParameter parameter, Scheduler scheduler) throws SchedulerException {
        logger.info(parameter.getLogContent(), "start to unscheduled and delete...");
        JobKey jobKey = this.getJobKey(parameter.getBatchId(), parameter.getGroup());
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
            logger.info(parameter.getLogContent(), "paused successful");
            scheduler.unscheduleJob(this.getTriggerKey(parameter.getBatchId(), parameter.getGroup()));
            logger.info(parameter.getLogContent(), "unscheduled successful");
            scheduler.deleteJob(jobKey);
            logger.info(parameter.getLogContent(), "deleted successful");
        } else {
            logger.info(parameter.getLogContent(), "triggerKey not exist, no need to unschedule");
        }
    }

    public JobKey getJobKey(String name, String group) {
        return JobKey.jobKey(name, group);
    }

    public TriggerKey getTriggerKey(String name, String group) {
        return TriggerKey.triggerKey(name, group);
    }

    public GroupMatcher<JobKey> getGroupMatcher(String group) {
        return GroupMatcher.jobGroupEquals(group);
    }

    public boolean isSchedulerExist(String name, String group) {
        Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
        try {
            JobDetail jobDetail = scheduler.getJobDetail(this.getJobKey(name, group));
            return jobDetail != null;
        } catch (SchedulerException e) {
            return false;
        }
    }

    public Map<String, Date> getAllTaskNextExecutedDateTimeMap() {
        HashMap<String, Date> map = new HashMap<>();
        this.handleAllScheduler((scheduler, jobKey) -> {
            String batchId = jobKey.getGroup(); // 這個group取出來的實際上是batchId
            try {
                Date nextFireTime = null;
                String description = null;
                JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
                BatchJobParameter parameter = (BatchJobParameter) jobDataMap.get(BatchJobParameter.getJobDataMapKey());
                if (parameter != null) {
                    nextFireTime = parameter.getNextExecutedDateTime() != null ? parameter.getNextExecutedDateTime().getTime() : null; // parameter.getNextQuartzFireDateTime();
                    description = parameter.getLogContent();
                }
                if (nextFireTime == null) {
                    @SuppressWarnings("unchecked")
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Trigger trigger = triggers.get(0);
                    nextFireTime = trigger.getNextFireTime();
                    if (StringUtils.isBlank(description))
                        description = trigger.getDescription();
                    parameter.setNextQuartzFireDateTime(nextFireTime);
                    parameter.setNextExecutedDateTime(CalendarUtil.clone(nextFireTime));
                    jobDataMap.put(BatchJobParameter.getJobDataMapKey(), parameter);
                }
                if (nextFireTime == null) {
                    logger.warn(description, ", cannot get next fire time!!!");
                    return;
                }
                Date date = map.get(batchId);
                if (date != null) {
                    if (date.compareTo(nextFireTime) < 0) {
                        return;
                    }
                }
                map.put(batchId, nextFireTime);
                logger.info(description, ", nextFireTime = [", FormatUtil.dateTimeFormat(nextFireTime), "]");
            } catch (SchedulerException e) {
                logger.error(e, "getTaskNextRunTime failed!!");
            }
        });
        return map;
    }

    private void handleAllScheduler(BatchSchedulerHandler handler) {
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(this.getGroupMatcher(groupName))) {
                    handler.handler(scheduler, jobKey);
                }
            }
        } catch (SchedulerException e) {
            logger.error(e, "handleAllScheduler failed!!");
        }
    }

    public ListSchedulerResponse listScheduler(ListSchedulerRequest request) {
        ListSchedulerResponse response = new ListSchedulerResponse();
        if (request == null || CollectionUtils.isEmpty(request.getBatchSchedulerList())) {
            response.setResult(false);
            response.setMessage("Request BatchId List cannot be empty!!!");
        } else {
            List<BatchScheduler> batchSchedulerRespList = new ArrayList<>();
            for (BatchScheduler batchSchedulerReq : request.getBatchSchedulerList()) {
                BatchScheduler batchSchedulerResp = this.getBatchScheduler(batchSchedulerReq.getBatchId());
                if (batchSchedulerResp != null) {
                    batchSchedulerRespList.add(batchSchedulerResp);
                }
            }
            response.setBatchSchedulerList(batchSchedulerRespList);
        }
        return response;
    }

    public OperateSchedulerResponse operateScheduler(OperateSchedulerRequest request) {
        OperateSchedulerResponse response = new OperateSchedulerResponse();
        if (request == null || CollectionUtils.isEmpty(request.getBatchSchedulerList())) {
            response.setResult(false);
            response.setMessage("Request BatchId List cannot be empty!!!");
        } else {
            ArrayList<BatchScheduler> batchSchedulerRespList = new ArrayList<>();
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            for (BatchScheduler batchSchedulerReq : request.getBatchSchedulerList()) {
                // 如果不存在, 則跳出
                if (!this.isSchedulerExist(batchSchedulerReq.getBatchId(), batchSchedulerReq.getBatchId())) {
                    continue;
                }
                GroupMatcher<JobKey> groupMatcher = this.getGroupMatcher(batchSchedulerReq.getBatchId());
                try {
                    switch (request.getAction()) {
                        case pause:
                            scheduler.pauseJobs(groupMatcher);
                            break;
                        case resume:
                            scheduler.resumeJobs(groupMatcher);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    logger.error(e, "operateScheduler failed, batchId = [", batchSchedulerReq.getBatchId(), "]");
                    response.setMessage(e.getMessage());
                    response.setResult(false);
                }
                BatchScheduler batchSchedulerResp = this.getBatchScheduler(batchSchedulerReq.getBatchId());
                if (batchSchedulerResp != null) {
                    batchSchedulerRespList.add(batchSchedulerResp);
                }
            }
            response.setBatchSchedulerList(batchSchedulerRespList);
        }
        return response;
    }

    private BatchScheduler getBatchScheduler(String batchId) {
        // 如果quartz資料不存在, 則直接返回傳入的request物件
        if (!this.isSchedulerExist(batchId, batchId)) {
            return null;
        }
        try {
            Scheduler scheduler = batchSchedulerFactoryBean.getScheduler();
            TriggerKey triggerKey = this.getTriggerKey(batchId, batchId);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            // BatchScheduler
            BatchScheduler batchScheduler = new BatchScheduler();
            batchScheduler.setExecutedHostName(fepConfig.getHostName());
            batchScheduler.setBatchId(batchId);
            batchScheduler.setTriggerState(triggerState);
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                batchScheduler.setCronExpression(cronTrigger.getCronExpression());
            }
            batchScheduler.setNextQuartzFireTime(trigger.getNextFireTime());
            JobKey jobKey = this.getJobKey(batchId, batchId);
            JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
            BatchJobParameter parameter = (BatchJobParameter) jobDataMap.get(BatchJobParameter.getJobDataMapKey());
            if (parameter != null) {
                if (parameter.getNextExecutedDateTime() != null) {
                    batchScheduler.setNextExecutedDateTime(parameter.getNextExecutedDateTime().getTime());
                } else {
                    batchScheduler.setNextExecutedDateTime(CalendarUtil.clone(batchScheduler.getNextQuartzFireTime()).getTime());
                }
            }
            return batchScheduler;
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            return null;
        }
    }
}
