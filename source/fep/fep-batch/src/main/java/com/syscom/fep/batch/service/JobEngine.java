package com.syscom.fep.batch.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.log.LogMDC;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.batch.base.enums.JobState;
import com.syscom.fep.batch.base.vo.FEPBatch;
import com.syscom.fep.frmcommon.util.ExceptionUtil;

@Component
public class JobEngine {
    @Autowired
    private JobHelper jobHelper;

    private final Map<String, JobInstance> jobList = Collections.synchronizedMap(new HashMap<>());

    /**
     * 本方法為Thread safe方法,同一時間只允許一個Thread進入
     *
     * @param jobInfo
     * @throws Exception
     */
    public synchronized void runBatch(FEPBatch jobInfo) throws Exception {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        JobInstance job = null;
        if (StringUtils.isNotBlank(jobInfo.getTaskParameters().getResult())) {
            BatchResult batchResult = BatchResult.parse(jobInfo.getTaskParameters().getResult());
            // 收到批次成功代表批次程式要結束整個批次
            if (batchResult == BatchResult.Successful) {
                jobHelper.log("JobEngine.RunBatch", jobInfo.getTaskParameters().getInstanceId(),
                        StringUtils.join("Receive StopBatch command for BatchId:", jobInfo.getTaskParameters().getBatchId(), ",Update batch Result:", batchResult));
                job = jobList.get(jobInfo.getTaskParameters().getInstanceId());
                if (job != null) {
                    jobHelper.updateJobLog(jobInfo.getTaskParameters().getInstanceId(),
                            StringUtils.EMPTY,
                            JobState.End,
                            BatchResult.Successful,
                            Integer.toString(job.getCurrentBatch()),
                            Integer.toString(job.getCurrentJob()),
                            Integer.toString(job.getCurrentTask()),
                            job.getHistorySeq(),
                            job.getCurrentJobLog());
                    jobHelper.updateBatchResult(Integer.parseInt(jobInfo.getTaskParameters().getBatchId()),
                            job.getBatchName(),
                            job.getBatchStartTime(),
                            jobInfo.getTaskParameters().getInstanceId(),
                            batchResult,
                            job.getNotifyType(),
                            job.getNotifyMail());
                    jobList.remove(jobInfo.getTaskParameters().getInstanceId());
                }
                return;
            }
        }
        JobState jobState = JobState.parse(jobInfo.getTaskParameters().getState());
        job = jobList.get(jobInfo.getTaskParameters().getInstanceId());
        switch (jobState) {
            case Start:
                if (job == null) {
                    job = new JobInstance(jobInfo);
                    jobList.put(jobInfo.getTaskParameters().getInstanceId(), job);
                    job.setCurrentBatch(Integer.parseInt(jobInfo.getTaskParameters().getBatchId()));
                    job.setCustomParameters(jobInfo.getTaskParameters().getCustomParameters());
                    job.setHostName(jobInfo.getTaskParameters().getHostName());
                }
                job.setCurrentJob(Integer.parseInt(jobInfo.getTaskParameters().getJobId()));
                job.setCurrentState(JobState.fromValue(Integer.parseInt(jobInfo.getTaskParameters().getState())));
                job.setCurrentResult(BatchResult.fromValue(Integer.parseInt(jobInfo.getTaskParameters().getResult())));
                job.setCurrentJobLog(jobInfo.getTaskParameters().getLogFile());
                job.run();
                break;
            case Running:
                if (job == null) {
                    throw ExceptionUtil.createException("Job not exists");
                }
                job.setCurrentTask(Integer.parseInt(jobInfo.getTaskParameters().getTaskId()));
                job.setCurrentStep(Integer.parseInt(jobInfo.getTaskParameters().getStepId()));
                job.setCurrentState(JobState.fromValue(Integer.parseInt(jobInfo.getTaskParameters().getState())));
                job.setCurrentJobLog(jobInfo.getTaskParameters().getLogFile());
                job.run();
                break;
            case End:
            case Failed:
            case Abort:
                if (job == null) {
                    throw ExceptionUtil.createException("Job not exists");
                }
                job.setCurrentTask(Integer.parseInt(jobInfo.getTaskParameters().getTaskId()));
                job.setCurrentStep(Integer.parseInt(jobInfo.getTaskParameters().getStepId()));
                job.setCurrentState(JobState.fromValue(Integer.parseInt(jobInfo.getTaskParameters().getState())));
                job.setCurrentMessage(jobInfo.getTaskParameters().getMessage());
                job.setCurrentJobLog(jobInfo.getTaskParameters().getLogFile());
                job.run();
                break;
            default:
                break;
        }
        if (job != null && job.getCurrentResult() == BatchResult.Successful) {
            jobList.remove(jobInfo.getTaskParameters().getInstanceId());
        }
    }
}
