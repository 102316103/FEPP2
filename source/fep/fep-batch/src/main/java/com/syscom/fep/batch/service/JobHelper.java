package com.syscom.fep.batch.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.batch.base.enums.JobState;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.notify.NotifyHelperTemplateId;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.HistoryExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.History;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JobHelper extends FEPBase {
    @Autowired
    private BatchConfiguration configuration;
    @Autowired
    private BsdaysExtMapper bsdaysMapper;
    @Autowired
    private BatchExtMapper batchMapper;
    @Autowired
    private HistoryExtMapper historyMapper;
    // @Autowired
    // private MailSender mailSender;
    @Autowired
    private FEPConfig fepConfig;
    @Autowired
    private NotifyHelper notifyHelper;
    private static final String subject = "FEP批次管理系統通知";

    public boolean runProcess(String programName, String arguments, RefString refErrMsg) {
        BatchJobLibrary batchLib = new BatchJobLibrary();
        try {
            Task task = batchLib.getBatchTask(programName);
            LogMDC.put(Const.MDC_PROFILE, task.getClass().getSimpleName()); // 這裡要改變一下FEPLOGGER記錄的檔名, 記錄到對應的批次程式中
            task.execute(arguments.split(StringUtils.SPACE));
            return true;
        } catch (Exception e) {
            refErrMsg.set(e.getMessage());
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setProgramName("JobHelper.runProcess");
            log.setProgramException(e);
            sendEMS(log);
        } finally {
            LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE); // 這裡要還原FEPLOGGER記錄的檔名
        }
        return false;
    }

    /**
     * 因為在上線前Table先不動,所以先把此Function修改內容先移至這裏,待上線後再改回呼叫Table的
     *
     * @param batchId
     * @param jobId
     * @param jobSeq
     * @param stepId
     * @return
     */
    public List<Map<String, Object>> getBatchContext(int batchId, int jobId, int jobSeq, int stepId) {
        try {
            return batchMapper.getBatchContext(batchId, jobId, jobSeq, stepId);
        } catch (Exception e) {
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setProgramName("JobHelper.getBatchContext");
            log.setProgramException(e);
            sendEMS(log);
        }
        return Collections.emptyList();
    }

    public void updateBatchResult(int batchId, String batchName, Calendar batchStartTime, String instanceId, BatchResult status, int notifyType, String notifyMail) {
        Batch batch = new Batch();
        batch.setBatchBatchid(batchId);
        batch.setBatchCurrentid(instanceId);
        batch.setBatchResult(String.valueOf(status.getValue()));
        if (status == BatchResult.Running) {
            batch.setBatchLastruntime(batchStartTime.getTime());
        }
        batch.setBatchZone(null); // 避免原本的欄位被更新掉
        try {
            batchMapper.updateByPrimaryKeySelective(batch);
            LogHelperFactory.getTraceLogger().info("UpdateBatchResult batchId = [", batchId, "], status = [", status, "]");
            // 通知
            if (notifyType > 0) {
                this.notifyResult(batchName, batchStartTime, notifyType, notifyMail, status);
            }
        } catch (Exception e) {
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setProgramName("JobHelper.updateBatchResult");
            log.setProgramException(e);
            sendEMS(log);
        }
    }

    public void updateBatchNextRunTime(int batchId, Date nextRuntime) {
        Batch batch = new Batch();
        batch.setBatchBatchid(batchId);
        batch.setBatchNextruntime(nextRuntime);
        batch.setBatchZone(null); // 這裡只更新next run time, 所以把zone設置為null, 避免zone被更新掉
        try {
            batchMapper.updateByPrimaryKeySelective(batch);
        } catch (Exception e) {
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setProgramName("JobHelper.updateBatchNextRunTime");
            log.setProgramException(e);
            sendEMS(log);
        }
    }

    public long addJobLog(String hostName, String instanceId, int batchId, int jobId, int taskId, int stepId, String message, JobState jobStatus, Calendar batchStartTime, String logFile) {
        History history = new History();
        history.setHistoryInstanceid(instanceId);
        history.setHistoryBatchid(batchId);
        history.setHistoryJobid(jobId);
        history.setHistoryTaskid(taskId);
        history.setHistoryStepid(stepId);
        history.setHistoryStarttime(batchStartTime.getTime());
        history.setHistoryTaskbegintime(Calendar.getInstance().getTime());
        history.setHistoryMessage(message);
        history.setHistoryStatus(String.valueOf(jobStatus.getValue()));
        history.setHistoryLogfile(logFile);
        history.setHistoryRunhost(hostName);
        if (StringUtils.isNotBlank(history.getHistoryMessage()) && history.getHistoryMessage().length() > 100) {
            history.setHistoryMessage(history.getHistoryMessage().substring(0, 100));
        }
        LogData log = new LogData();
        log.setRemark(StringUtils.join(Arrays.asList(
                instanceId,
                batchId,
                jobId,
                taskId,
                stepId,
                batchStartTime.getTime(),
                Calendar.getInstance().getTime(),
                message,
                String.valueOf(jobStatus.getValue()),
                logFile,
                hostName,
                history.getHistoryMessage()), ','));
        this.logMessage(log);
        try {
            historyMapper.insertSelective(history);
            LogHelperFactory.getTraceLogger().info("AddJobLog BatchId:", batchId, " JobId:", jobId, " TaskId:", taskId, " StepId:", stepId, " LogFile:", logFile);
            return history.getHistorySeq();
        } catch (Exception e) {
            log.setSubSys(SubSystem.CMN);
            log.setProgramName("JobHelper.addJobLog");
            log.setProgramException(e);
            sendEMS(log);
        }
        return 0;
    }

    public void updateJobLog(String instanceId, String message, JobState status, BatchResult batchStatus, String batchId, String jobId, String taskId, long historySeq, String jobLog) {
        History history = new History();
        history.setHistorySeq(historySeq);
        history.setHistoryInstanceid(instanceId);
        history.setHistoryBatchid(Integer.parseInt(batchId));
        history.setHistoryJobid(Integer.parseInt(jobId));
        history.setHistoryTaskid(Integer.parseInt(taskId));
        history.setHistoryMessage(StringUtils.join(status == JobState.End ? "工作執行成功." : "工作執行失敗.", message));
        if (StringUtils.isNotBlank(history.getHistoryMessage()) && history.getHistoryMessage().length() > 100) {
            history.setHistoryMessage(history.getHistoryMessage().substring(0, 100));
        }
        if (status == JobState.Running) {
            history.setHistoryTaskbegintime(Calendar.getInstance().getTime());
        } else if (status == JobState.End || status == JobState.Failed || status == JobState.Abort) {
            history.setHistoryTaskendtime(Calendar.getInstance().getTime());
        }
        history.setHistoryStatus(String.valueOf(status.getValue()));
        try {
            historyMapper.updateByPrimaryKeySelective(history);
            LogHelperFactory.getTraceLogger().info("UpdateJobLog BatchId:", history.getHistoryBatchid(), " JobId:", history.getHistoryJobid(), " TaskId:", history.getHistoryTaskid());
            if (status == JobState.End || status == JobState.Failed || status == JobState.Abort) {
                history.setHistoryTaskendtime(history.getHistoryTaskendtime());
                if (StringUtils.isNotBlank(jobLog)) {
                    File file = new File(CleanPathUtil.cleanString(jobLog));
                    if (file.exists()) {
                        try {
                            // 為避免Task還沒關閉檔案先Sleep 1秒再開
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                        int bufferSize = 8 * 1024;
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), bufferSize)) {
                            StringBuilder sb = new StringBuilder();
                            String line = null;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("<br/>").append("\r\n");
                            }
                            if (sb.length() < 64000) {
                                history.setHistoryLogfilecontent(sb.toString());
                            }
                        } catch (Exception e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                        if (StringUtils.isNotBlank(history.getHistoryLogfilecontent())) {
                            try {
                                historyMapper.updateByPrimaryKeySelective(history);
                            } catch (Exception e) {
                                LogData log = new LogData();
                                log.setSubSys(SubSystem.CMN);
                                log.setProgramName("JobHelper.UpdateLOGFILE");
                                log.setProgramException(e);
                                sendEMS(log);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setProgramName("JobHelper.UpdateLog");
            log.setProgramException(e);
            sendEMS(log);
        }
    }

    public void updateJobLog(String instanceId, String message, JobState status, BatchResult batchStatus, Map<String, Object> map, long historySeq, String jobLog) {
        this.updateJobLog(instanceId, message, status, batchStatus,
                String.valueOf(((Integer) map.get("BATCH_BATCHID")).longValue()),
                String.valueOf(((Integer) map.get("JOBS_JOBID")).longValue()),
                String.valueOf(((Integer) map.get("JOBTASK_TASKID")).longValue()),
                historySeq, jobLog);
    }

    public void log(String progName, String instanceId, String msg) {
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.CMN);
        logData.setChannel(FEPChannel.BATCH);
        logData.setProgramName(StringUtils.join("BatchJobService.", progName));
        logData.setMessage(instanceId);
        logData.setRemark(msg);
        this.logMessage(logData);
    }

    private void notifyResult(String batchName, Calendar batchStartTime, int notifyType, String mailAddress, BatchResult result) throws Exception {
        if (StringUtils.isBlank(mailAddress)) {
            return;
        }
        String execStartTime = FormatUtil.dateTimeFormat(batchStartTime, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS);
        String execEndTime = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS);
        String body = StringUtils.join("批次名稱:", batchName, "\n執行主機:", fepConfig.getHostName(), "\n批次開始時間:", execStartTime, "\n批次結束時間:", execEndTime, "\n執行結果:");
        switch (notifyType) {
            // 成功時通知
            case 1:
                if (result != BatchResult.Successful)
                    return;
                body = StringUtils.join(body, "成功");
                break;
            // 失敗時通知
            case 2:
                if (result != BatchResult.Failed && result != BatchResult.PartialFailed)
                    return;
                body = StringUtils.join(body, "失敗");
                break;
            // 成功失敗都通知
            case 3:
                body = StringUtils.join(body, result == BatchResult.Successful ? "成功" : "失敗");
                break;
        }
        LogData logData = new LogData();
        logData.setRemark(StringUtils.join(configuration.getMailSender(), mailAddress));
        logMessage(logData);
        // mailSender.sendSimpleEmail(configuration.getMailSender(), mailAddress, StringUtils.EMPTY, subject, body);
        notifyHelper.sendSimpleMail(NotifyHelperTemplateId.BATCH, mailAddress, body, true);
    }

    public boolean checkBusinessDate(String zone, String batchName) {
        if (StringUtils.isBlank(zone)) {
            String body = StringUtils.join("批次名稱:", batchName, "檢核營業日失敗!未傳入地區別\n");
            try {
                // mailSender.sendSimpleEmail(configuration.getMailSender(), configuration.getMailList(), StringUtils.EMPTY, subject, body);
                notifyHelper.sendSimpleMail(NotifyHelperTemplateId.BATCH, configuration.getMailList(), body, true);
            } catch (Exception e) {
                LogData log = new LogData();
                log.setSubSys(SubSystem.CMN);
                log.setProgramName("JobHelper.checkBusinessDate");
                log.setProgramException(e);
                sendEMS(log);
            }
            return false;
        }
        try {
            Bsdays bsdays = bsdaysMapper.selectByPrimaryKey(zone, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            if (bsdays != null) {
                return DbHelper.toBoolean(bsdays.getBsdaysWorkday());
            }
        } catch (Exception e) {
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setProgramName("JobHelper.checkBusinessDate");
            log.setProgramException(e);
            sendEMS(log);
        }
        return false;
    }
}