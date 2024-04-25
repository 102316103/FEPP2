package com.syscom.fep.batch.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.log.LogMDC;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.batch.base.enums.JobState;
import com.syscom.fep.batch.base.vo.FEPBatch;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;

public class JobInstance {
    private List<Map<String, Object>> jobData;
    private Map<String, Boolean> doneFlag = new HashMap<>();
    private List<JobState> eachJobResult = new ArrayList<JobState>();

    private String batchName;
    private Calendar batchStartTime;
    private String jobInstanceId;
    private int currentBatch;
    private int currentJob;
    private int currentTask;
    private int currentStep;
    private int currentJobStep;
    private String currentJobLog;
    private JobState currentState;
    private BatchResult currentResult;
    private String currentMessage;
    private long historySeq;
    private int notifyType;
    private String notifyMail;
    private String customParameters;
    private String hostName;

    public JobInstance(FEPBatch jobInfo) {
        this.jobInstanceId = jobInfo.getTaskParameters().getInstanceId();
        this.currentState = JobState.parse(jobInfo.getTaskParameters().getState());
        this.currentBatch = Integer.parseInt(jobInfo.getTaskParameters().getBatchId());
        this.currentStep = Integer.parseInt(jobInfo.getTaskParameters().getStepId());
        this.currentJob = Integer.parseInt(jobInfo.getTaskParameters().getJobId());
        this.currentJobLog = jobInfo.getTaskParameters().getLogFile();
        this.hostName = jobInfo.getTaskParameters().getHostName();
    }

    /**
     * 本方法為Thread safe方法,同一時間只允許一個Thread進入
     */
    public synchronized void run() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        BatchConfiguration configuration = SpringBeanFactoryUtil.getBean(BatchConfiguration.class);
        JobHelper jobHelper = SpringBeanFactoryUtil.getBean(JobHelper.class);
        List<Map<String, Object>> dtJob;
        jobHelper.log("JobInstance.Run", this.jobInstanceId,
                StringUtils.join("Begin to run job BatchId:", this.currentBatch, ",JobId:", this.currentJob, ",JobState:", this.currentState));
        switch (this.currentState) {
            case Start:
                // 20151027update:先查出Batch資料,檢查是否每天只能做一次再決定要不要往下做
                // 取得batch所有job task相關資料
                this.jobData = jobHelper.getBatchContext(this.currentBatch, 0, 0, 0);
                if (CollectionUtils.isNotEmpty(this.jobData)) {
                    // 檢核可執行的系統別
                    Map<String, Object> batch = this.jobData.get(0);
                    // 批次平台在接收批次啟動要求時,如果BATCH_EXECUTE_HOST_NAME欄位是空值(不指定), 或符合本機的計算機名稱, 才執行此批次
                    Object hostName = batch.get("BATCH_EXECUTE_HOST_NAME");
                    if (hostName != null && StringUtils.isNotBlank(hostName.toString()) && !FEPConfig.getInstance().getHostName().equals(hostName.toString())) {
                        jobHelper.log("JobInstance.Run", this.jobInstanceId, StringUtils.join("Current BatchId:", this.currentBatch, " Batch Execute HostName Inconsistently"));
                        return;
                    }
                    Object batchSubsys = batch.get("BATCH_SUBSYS");
                    if (batchSubsys != null && StringUtils.isNotBlank(batchSubsys.toString()) && configuration.getSubSys().indexOf(batchSubsys.toString()) < 0) {
                        jobHelper.log("JobInstance.Run", this.jobInstanceId, StringUtils.join("Current BatchId:", this.currentBatch, " Subsys is not allowed"));
                        return;
                    }
                    Object batchEnable = batch.get("BATCH_ENABLE");
                    if (batchEnable == null || !DbHelper.toBoolean(((Integer) batchEnable).shortValue())) {
                        jobHelper.log("JobInstance.Run", this.jobInstanceId, StringUtils.join("Current BatchId:", this.currentBatch, " is disable"));
                        return;
                    }
                    // 檢核是否營業日才執行
                    Object batchCheckBusinessDate = batch.get("BATCH_CHECKBUSINESSDATE");
                    if (batchCheckBusinessDate != null && DbHelper.toBoolean(((Integer) batchCheckBusinessDate).shortValue())) {
                        if (!jobHelper.checkBusinessDate(batch.get("BATCH_ZONE").toString(), batch.get("BATCH_NAME").toString())) {
                            jobHelper.log("JobInstance.Run", this.jobInstanceId, StringUtils.join("BatchJobService Cancel Run by CheckBusinessDay fail, batchId:", this.currentBatch));
                            return;
                        }
                    }
                    // 一天只能做一次且今天已做過則不做直接離開
                    Object batchSingleTime = batch.get("BATCH_SINGLETIME");
                    if (batchSingleTime != null && DbHelper.toBoolean(((Integer) batchSingleTime).shortValue())) {
                        Object batchLastRunTime = batch.get("BATCH_LASTRUNTIME");
                        if (batchLastRunTime != null && CalendarUtil.equals((Date) batchLastRunTime, Calendar.getInstance().getTime(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)) {
                            jobHelper.log("JobInstance.Run", this.jobInstanceId, StringUtils.join("BatchJobService Cancel SingleTime batchId:", this.currentBatch));
                            return;
                        }
                    }
                }
                this.batchStartTime = Calendar.getInstance();
                // batch開始,先更新Batch執行狀態及開始時間
                jobHelper.updateBatchResult(this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, this.currentResult, this.notifyType, this.notifyMail);
                if (CollectionUtils.isNotEmpty(this.jobData)) {
                    Map<String, Object> batch = this.jobData.get(0);
                    this.batchName = (String) batch.get("BATCH_NAME");
                    this.notifyType = ((BigDecimal) batch.get("BATCH_NOTIFYTYPE")).intValue();
                    this.notifyMail = (String) batch.get("BATCH_NOTIFYMAIL");
                    // 找出要執行Job中的第一個step
                    List<Map<String, Object>> list = this.jobData.stream().filter(t -> ((Integer) t.get("JOBS_JOBID")).intValue() == this.currentJob).collect(Collectors.toList());
                    // 執行同一Step中的所有Task
                    if (CollectionUtils.isNotEmpty(list)) {
                        for (Map<String, Object> map : list) {
                            // DELAY FOR JOB
                            try {
                                Thread.sleep(((Integer) map.get("JOBS_DELAY")).intValue() * 1000);
                            } catch (InterruptedException e) {
                                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                            }
                            this.currentJobStep = ((Integer) map.get("JOBS_SEQ")).intValue();
                            boolean bResult = this.runTask(
                                    FEPConfig.getInstance().getHostName(),
                                    this.currentBatch,
                                    (String) map.get("BATCH_NAME"),
                                    this.currentJob,
                                    ((Integer) map.get("JOBTASK_TASKID")).intValue(),
                                    ((Integer) map.get("JOBTASK_STEPID")).shortValue(),
                                    (String) map.get("TASK_COMMAND"),
                                    (String) map.get("TASK_COMMANDARGS"));
                            if (!bResult) {
                                break;
                            }
                        }
                    }
                }
                break;
            case Running:
                this.historySeq = jobHelper.addJobLog(
                        FEPConfig.getInstance().getHostName(),
                        this.jobInstanceId,
                        this.currentBatch,
                        this.currentJob,
                        this.currentTask,
                        this.currentStep,
                        "工作開始執行",
                        this.currentState,
                        this.batchStartTime,
                        this.currentJobLog);
                break;
            case End:
            case Failed:
            case Abort:
                doneFlag.put(StringUtils.join(this.currentJob, "_", this.currentTask), true);
                // 找出目前結束的工作
                jobHelper.log("currentJob",this.jobInstanceId,StringUtils.join("currentJob",currentJob,"currentStep",currentStep,"currentTask",currentTask));
                List<Map<String, Object>> list = this.jobData.stream().filter(t -> ((Integer) t.get("JOBTASK_JOBID")).intValue() == this.currentJob
                        && ((Integer) t.get("JOBTASK_STEPID")).intValue() == this.currentStep
                        && ((Integer) t.get("JOBTASK_TASKID")).intValue() == this.currentTask).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Map<String, Object> map : list) {
                        jobHelper.updateJobLog(this.jobInstanceId, this.currentMessage, this.currentState, BatchResult.Running, map, this.historySeq, this.currentJobLog);
                        // 檢查等待的Task是否已完成
                        if (!checkWaitFlag(this.currentJob, (String) map.get("JOBTASK_WAITFORTASK")))
                            return;
                    }
                }
                if (this.currentState == JobState.Abort) {
                    this.currentResult = BatchResult.Failed;
                    this.eachJobResult.add(this.currentState);
                    jobHelper.updateBatchResult(this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, this.currentResult, this.notifyType, this.notifyMail);
                    // AbortTask,結束批次執行
                    break;
                } else if (this.currentState == JobState.Failed) {
                    this.currentResult = BatchResult.PartialFailed;
                    this.eachJobResult.add(this.currentState);
                    // Failed繼續往下做
                } else {
                    // CurrentResult = BatchResult.Successful;
                    this.eachJobResult.add(this.currentState);
                }
                this.currentStep += 1; // 執行成功目前步驟加1
                // 找出Job下一步驟的Task
                dtJob = jobHelper.getBatchContext(this.currentBatch, this.currentJob, this.currentJobStep, this.currentStep);
                //
                if (CollectionUtils.isEmpty(dtJob)) {
                    // 無下一步驟,檢查是否有下一個Job
                    this.currentJobStep += 1;
                    this.currentStep = 1;
                    this.currentJob = 0;
                    dtJob = jobHelper.getBatchContext(this.currentBatch, this.currentJob, this.currentJobStep, this.currentStep);
                }
                if (CollectionUtils.isEmpty(dtJob)) {
                    // 無下一Job,更新Batch執行結果
                    if (this.eachJobResult.stream().filter(t -> t == JobState.End).count() == this.eachJobResult.size()) {
                        this.currentResult = BatchResult.Successful;
                    } else if (this.eachJobResult.stream().filter(t -> t == JobState.Abort).count() > 1) {
                        this.currentResult = BatchResult.Failed;
                    } else if (this.eachJobResult.stream().filter(t -> t == JobState.Failed).count() > 1) {
                        this.currentResult = BatchResult.PartialFailed;
                    }
                    jobHelper.log("JobInstance.Run", this.jobInstanceId, StringUtils.join("no more jobs, Update batch result ", this.currentResult));
                    jobHelper.updateBatchResult(this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, this.currentResult, this.notifyType, this.notifyMail);
                } else {
                    // DELAY FOR THIS JOB
                    int delay = ((Integer) dtJob.get(0).get("JOBS_DELAY")).intValue() * 1000;
                    this.currentJob = ((Integer) dtJob.get(0).get("JOBTASK_JOBID")).intValue();
                    LogHelperFactory.getTraceLogger().info("Job Id ", this.currentJob, " Wait for ", delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                    for (Map<String, Object> map : dtJob) {
                        // 如果批次被停用則不執行此task
                        if (map.get("BATCH_ENABLE") == null || !DbHelper.toBoolean(((Integer) map.get("BATCH_ENABLE")).shortValue())) {
                            return;
                        }
                        boolean bResult = this.runTask(
                                FEPConfig.getInstance().getHostName(),
                                this.currentBatch,
                                (String) map.get("BATCH_NAME"),
                                this.currentJob,
                                ((Integer) map.get("JOBTASK_TASKID")).intValue(),
                                ((Integer) map.get("JOBTASK_STEPID")).shortValue(),
                                (String) map.get("TASK_COMMAND"),
                                (String) map.get("TASK_COMMANDARGS"));
                        if (!bResult) {
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private boolean checkWaitFlag(int jobId, String waitFlag) {
        if ("0".equals(waitFlag)) {
            return true;
        }
        String[] waitTask = waitFlag.split(",");
        for (int i = 0; i < waitTask.length; i++) {
            Boolean flag = this.doneFlag.get(StringUtils.join(jobId, "_", Integer.parseInt(waitTask[i])));
            if (flag == null) {
                LogHelperFactory.getTraceLogger().warn("[JobInstance]Cannot find flag from map by key = [", jobId, "_", waitTask[i], "], the doneFlag = [", doneFlag.toString(), "]");
            }
            if (flag == null || !flag) {
                return false;
            }
        }
        return true;
    }

    private boolean runTask(String hostName, int batchId, String batchName, int jobId, int taskId, int stepId, String taskCommand, String taskArgs) {
        String errMsg = StringUtils.EMPTY;
        String args = StringUtils.join(
                "/hostName:", StringUtils.isBlank(hostName) ? FEPConfig.getInstance().getHostName() : hostName,
                " /instanceid:", this.jobInstanceId,
                " /taskid:", taskId,
                " /stepid:", stepId,
                " /batchname:", batchName,
                " /batchid:", batchId,
                this.getUniqueArgs(this.customParameters, taskArgs));
        JobHelper jobHelper = SpringBeanFactoryUtil.getBean(JobHelper.class);
        jobHelper.log("JobInstance.RunTask", this.jobInstanceId, StringUtils.join("Begin to run Task Command:", taskCommand, StringUtils.SPACE, args));
        String tskKey = StringUtils.join(jobId, "_", taskId);
        this.doneFlag.put(tskKey, false);
        RefString refErrMsg = new RefString(errMsg);
        boolean result = jobHelper.runProcess(taskCommand, args, refErrMsg);
        errMsg = refErrMsg.get();
        jobHelper.log("JobInstance.RunTask", this.jobInstanceId, StringUtils.join("Run task complete. Result:", result, ",ErrMsg:", errMsg));
        if (!result) {
            this.historySeq = jobHelper.addJobLog(
                    FEPConfig.getInstance().getHostName(),
                    this.jobInstanceId,
                    batchId,
                    jobId,
                    taskId,
                    stepId,
                    StringUtils.join("工作執行發生異常:", errMsg),
                    JobState.End,
                    this.batchStartTime,
                    this.currentJobLog);
            this.eachJobResult.add(JobState.Failed);
            jobHelper.updateBatchResult(this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, BatchResult.Failed, this.notifyType, this.notifyMail);
            return false;
        }
        return true;
    }

    /**
     * 傳進來2組參數字串排除重覆部分再回傳,以第一個參數為主,第2個參數重覆會被排除掉
     *
     * @param arg1
     * @param arg2
     * @return
     */
    private String getUniqueArgs(String arg1, String arg2) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(arg1)) {
            String[] args1 = arg1.split("\\s+");
            for (String arg : args1) {
                if (StringUtils.isNotBlank(arg)) {
                    String[] ary = arg.split(":");
                    if (!map.containsKey(ary[0])) {
                        map.put(ary[0], ary[1]);
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(arg2)) {
            String[] args2 = arg2.split("\\s+");
            for (String arg : args2) {
                String[] ary = arg.split(":");
                if (!map.containsKey(ary[0])) {
                    map.put(ary[0], ary[1]);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : map.entrySet()) {
            sb.append(StringUtils.SPACE).append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    public int getCurrentBatch() {
        return currentBatch;
    }

    public int getCurrentJob() {
        return currentJob;
    }

    public int getCurrentTask() {
        return currentTask;
    }

    public String getCurrentJobLog() {
        return currentJobLog;
    }

    public long getHistorySeq() {
        return historySeq;
    }

    public String getBatchName() {
        return batchName;
    }

    public Calendar getBatchStartTime() {
        return batchStartTime;
    }

    public int getNotifyType() {
        return notifyType;
    }

    public String getNotifyMail() {
        return notifyMail;
    }

    public void setCurrentJob(int currentJob) {
        this.currentJob = currentJob;
    }

    public void setCurrentJobLog(String currentJobLog) {
        this.currentJobLog = currentJobLog;
    }

    public void setCurrentState(JobState currentState) {
        this.currentState = currentState;
    }

    public void setCurrentResult(BatchResult currentResult) {
        this.currentResult = currentResult;
    }

    public void setCurrentBatch(int currentBatch) {
        this.currentBatch = currentBatch;
    }

    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }

    public void setCurrentTask(int currentTask) {
        this.currentTask = currentTask;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public BatchResult getCurrentResult() {
        return currentResult;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
