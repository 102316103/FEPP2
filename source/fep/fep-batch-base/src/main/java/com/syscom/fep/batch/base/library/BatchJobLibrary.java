package com.syscom.fep.batch.base.library;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.batch.base.enums.JobState;
import com.syscom.fep.batch.base.enums.ScheduleType;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.batch.base.vo.FEPBatch;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.delegate.ActionListener;
import com.syscom.fep.frmcommon.jms.JmsKind;
import com.syscom.fep.frmcommon.jms.JmsPayloadOperator;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.instance.batch.BatchQueueOperator;
import com.syscom.fep.jms.instance.batch.hosts.BatchQueueHostConfigurationProperties;
import com.syscom.fep.jms.instance.batch.hosts.BatchQueueHostConstant;
import com.syscom.fep.jms.instance.batch.hosts.BatchQueueHostOperator;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TwslogExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Twslog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.*;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

public class BatchJobLibrary extends FEPBase {
    private static final LogHelper log = LogHelperFactory.getBatchLogger();
    // @SuppressWarnings("unused")
    // private static final String ProgramName = "BatchJobService.JobReceiver.";
    private FEPBatch jobData = new FEPBatch();
    private String _logPath;
    private String _batchName;
    private String logFile;
    // @SuppressWarnings("unused")
    // private static final String _wcfUri = CMNConfig.getInstance().getBranchBroadcastUrl();
    private static final String _batchInvokerName = "jobSchedulerInvoker";
    // 開始執行時間
    long startTimeMillis;

    private String message;
    private Map<String, String> arguments;
    private Task task;

    public BatchJobLibrary() {
    }

    /**
     * 本建構式專門給批次程式使用
     *
     * @param task
     * @param args
     * @param logPath
     */
    public BatchJobLibrary(Task task, String[] args, String logPath) {
        this.task = task;
        this.arguments = new HashMap<String, String>();
        this.setLogPath(logPath);
        this.extractBatchParameter(args);
        LogHelperFactory.getTraceLogger().trace("Get batchName after extract parameter, _batchName = [", this._batchName, "]");
        List<String> argsList = new ArrayList<>();
        for (Entry<String, String> entry : this.arguments.entrySet()) {
            argsList.add(StringUtils.join("/", entry.getKey(), ":", entry.getValue()));
        }
        this.writeLog("Batch Parameters = [", StringUtils.join(argsList, StringUtils.SPACE), "], LogPath = [", this._logPath, "]");
        this.startTimeMillis = System.currentTimeMillis();
    }

    private void extractBatchParameter(String[] args) {
        for (int i = 0; i < args.length; i++) {
            LogHelperFactory.getTraceLogger().trace("BatchJobLibrary extract parameter = [", args[i], "]");
            String[] arg = args[i].split(":", 2);
            if (StringUtils.startsWithIgnoreCase(arg[0], "/instanceid")) {
                this.jobData.getTaskParameters().setInstanceId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/hostname")) {
                this.jobData.getTaskParameters().setHostName(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/batchid")) {
                this.jobData.getTaskParameters().setBatchId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/batchname")) {
                this._batchName = arg[1];
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/jobid")) {
                this.jobData.getTaskParameters().setJobId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/taskid")) {
                this.jobData.getTaskParameters().setTaskId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/stepid")) {
                this.jobData.getTaskParameters().setStepId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/BatchLogPath")) {
                this.setLogPath(arg[1]);
            } else {
                if (arg.length > 1) {
                    if (StringUtils.isNotBlank(arg[0]))
                        this.arguments.put(arg[0].substring(1), arg[1]);
                } else {
                    if (StringUtils.isNotBlank(arg[0]))
                        this.arguments.put(arg[0].substring(1), StringUtils.EMPTY);
                }
            }
        }
    }

    private void setLogPath(String logPath) {
        this._logPath = logPath;
        this.arguments.put("BatchLogPath", this._logPath);
    }

    public void startBatch(String hostName, String batchId, String jobId) throws Exception {
        this.startBatch(hostName, batchId, jobId, StringUtils.EMPTY);
    }

    public void startBatch(String hostName, String batchId, String jobId, String customParameters) throws Exception {
        this.startBatch(hostName, batchId, jobId, customParameters, 0);
    }

    public void startBatch(String hostName, String batchId, String jobId, int sleepInMillisecondsAfterStart) throws Exception {
        this.startBatch(hostName, batchId, jobId, StringUtils.EMPTY, sleepInMillisecondsAfterStart);
    }

    public void startBatch(String hostName, String batchId, String jobId, String customParameters, int sleepInMillisecondsAfterStart) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        jobData.getTaskParameters().setJobId(jobId);
        jobData.getTaskParameters().setStepId("1");
        jobData.getTaskParameters().setInstanceId(UUID.randomUUID().toString());
        jobData.getTaskParameters().setState(String.valueOf(JobState.Start.getValue()));
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Running.getValue()));
        jobData.getTaskParameters().setCustomParameters(customParameters);
        sendBatchQueue(hostName);
        if (sleepInMillisecondsAfterStart > 0) {
            try {
                Thread.sleep(sleepInMillisecondsAfterStart);
            } catch (InterruptedException e) {
            }
        }
    }

    public void stopBatch() throws Exception {
        jobData.getTaskParameters().setBatchId(jobData.getTaskParameters().getBatchId());
        jobData.getTaskParameters().setInstanceId(jobData.getTaskParameters().getInstanceId());
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Successful.getValue()));
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    public void rerunBatch(String hostName, String instanceId, String batchId, String jobId) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        jobData.getTaskParameters().setJobId(jobId);
        jobData.getTaskParameters().setStepId("1");
        jobData.getTaskParameters().setInstanceId(instanceId);
        jobData.getTaskParameters().setState(String.valueOf(JobState.Start.getValue()));
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Running.getValue()));
        sendBatchQueue(hostName);
    }

    public void stopJob() {
    }

    /**
     * 開始批次工作
     *
     * @throws Exception
     */
    public void startTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.Running.getValue()));
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Running.getValue()));
        jobData.getTaskParameters().setLogFile(logFile);
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    /**
     * 回報批次工作為失敗,批次將會中止
     *
     * @throws Exception
     */
    public void abortTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.Abort.getValue()));
        jobData.getTaskParameters().setMessage(this.message);
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    /**
     * 回報批次工作為失敗,但批次仍會繼續下一個Task
     *
     * @throws Exception
     */
    public void failedTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.Failed.getValue()));
        jobData.getTaskParameters().setMessage(this.message);
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    /**
     * 回報批次工作為成功結束
     *
     * @throws Exception
     */
    public void endTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.End.getValue()));
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    public void createDailyTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                String startTime, short daysInterval, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(_batchInvokerName);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.Daily.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getDailyTrigger().setDaysInterval(String.valueOf(daysInterval));
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createDailyRepetitionTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                          String startTime, short daysInterval, boolean enable, String repetitionInterval, String repetitionDuration) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(_batchInvokerName);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.DailyRepetition.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getDailyTrigger().setDaysInterval(String.valueOf(daysInterval));
        jobData.getScheduleTask().getDailyTrigger().setRepetitionDuration(repetitionDuration);
        jobData.getScheduleTask().getDailyTrigger().setRepetitionInterval(repetitionInterval);
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createWeeklyTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                 String startTime, int daysOfWeek, int weeksInterval, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(_batchInvokerName);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.Weekly.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getWeeklyTrigger().setDaysOfWeek(String.valueOf(daysOfWeek));
        jobData.getScheduleTask().getWeeklyTrigger().setWeeksInterval(String.valueOf(weeksInterval));
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createMonthlyTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                  String startTime, String daysOfMonth, int monthsOfYear, boolean runOnLastDayOfMonth, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(_batchInvokerName);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.Monthly.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getMonthlyTrigger().setDaysOfMonth(daysOfMonth);
        jobData.getScheduleTask().getMonthlyTrigger().setMonthsOfYear(String.valueOf(monthsOfYear));
        jobData.getScheduleTask().getMonthlyTrigger().setRunOnLastDayOfMonth(String.valueOf(runOnLastDayOfMonth));
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createMonthlyDayOfWeekTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                           String startTime, int daysOfWeek, int monthsOfYear, int weeksOfMonth, boolean runOnLastWeekOfMonth, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(_batchInvokerName);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.MonthDayOfWeek.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setDaysOfWeek(String.valueOf(daysOfWeek));
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setMonthsOfYear(String.valueOf(monthsOfYear));
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setWeeksOfMonth(String.valueOf(weeksOfMonth));
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setRunOnLastWeekOfMonth(String.valueOf(runOnLastWeekOfMonth));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        sendBatchQueue(hostName);
    }

    /**
     * 在create Batch排程之前, 先要通知其他主機刪除有可能存在的Batch排程
     * 這個method先暫時mark掉留著
     *
     * @param hostName
     * @param batchId
     * @param batchName
     */
    private void deleteBatchOnOthersHost(String hostName, String batchId, String batchName) {
        // if (StringUtils.isBlank(hostName)) return;
        // FEPBatch jobData = new FEPBatch();
        // jobData.getTaskParameters().setBatchId(batchId);
        // // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        // jobData.getScheduleTask().setTaskName(batchName);
        // jobData.getScheduleTask().setDelete(Boolean.TRUE.toString());
        // Map<String, BatchQueueHostOperator> map = SpringBeanFactoryUtil.getBean(BatchQueueHostConstant.JMS_OPERATOR_MAP);
        // JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        // PlainTextMessage payload = new PlainTextMessage(JmsKind.QUEUE, configuration.getQueueNames().getBatch(), StringUtils.EMPTY);
        // LogData log = new LogData();
        // for (Entry<String, BatchQueueHostOperator> entry : map.entrySet()) {
        //     if (entry.getKey().equals(hostName)) continue;
        //     jobData.getTaskParameters().setHostName(entry.getKey());
        //     payload.setPayload(serializeToXml(jobData));
        //     BatchQueueHostOperator operator = entry.getValue();
        //     try {
        //         operator.sendQueue(payload, null, null);
        //         log.setProgramName(StringUtils.join(ProgramName, ".deleteTaskBeforeCreate"));
        //         log.setRemark(StringUtils.join("sendQueue to delete batch successful before create, hostName = [", hostName, "], batchId = [", batchId, "], batchName = [", batchName, "]!!!"));
        //         this.logMessage(log);
        //     } catch (Exception e) {
        //         log.setProgramName(StringUtils.join(ProgramName, ".deleteTaskBeforeCreate"));
        //         log.setRemark(StringUtils.join("sendQueue to delete batch failed before create, hostName = [", hostName, "], batchId = [", batchId, "], batchName = [", batchName, "]!!!"));
        //         log.setProgramException(e);
        //         sendEMS(log);
        //     }
        // }
    }

    public void deleteTask(String hostName, String batchId, String batchName) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setDelete(Boolean.TRUE.toString());
        sendBatchQueue(hostName);
    }

    public void writeLog(Object... logContent) {
        this.writeLog(message -> {
            log.info(message);
        }, logContent);
    }

    public void writeErrorLog(Throwable t, Object... logContent) {
        this.writeLog(message -> {
            log.error(t, message);
        }, logContent);
    }

    public void writeDebugLog(Throwable t, Object... logContent) {
        this.writeLog(message -> {
            log.debug(t, message);
        }, logContent);
    }

    public void writeWarnLog(Throwable t, Object... logContent) {
        this.writeLog(message -> {
            log.warn(t, message);
        }, logContent);
    }

    private void writeLog(ActionListener<String> listener, Object... logContent) {
        if (StringUtils.isBlank(this._batchName)) {
            this._batchName = this.task.getClass().getSimpleName();
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(this._logPath)) {
            sb.append(this._logPath).append("/");
        } else {
            sb.append("logs/");
        }
        sb.append(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2))
                .append("/fep-batch-task/")
                .append(this._batchName);
        if (jobData.getTaskParameters() != null) {
            if (StringUtils.isNotBlank(jobData.getTaskParameters().getInstanceId()))
                sb.append("_").append(jobData.getTaskParameters().getInstanceId());
            if (StringUtils.isNotBlank(jobData.getTaskParameters().getStepId()))
                sb.append("_").append(jobData.getTaskParameters().getStepId());
        }
        sb.append(".log");
        // System.out.println("BatchLog Path:" + sb.toString());
        this.logFile = new File(CleanPathUtil.cleanString(sb.toString())).getAbsolutePath();
        LogMDC.put(Const.MDC_BATCHJOB, StringUtils.replace(logFile, "/", "-"));
        LogMDC.put(Const.MDC_BATCHJOB_FILENAME, logFile);
        listener.actionPerformed(StringUtils.join(logContent));
        this.clearMDC();
    }

    public boolean isBsDay(String zone) {
        return this.isBsDay(zone, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
    }

    public boolean isBsDay(String zone, String bsday) {
        BsdaysExtMapper mapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
        try {
            Bsdays record = mapper.selectByPrimaryKey(zone, bsday);
            if (record != null) {
                return DbHelper.toBoolean(record.getBsdaysWorkday());
            } else {
                return false;
            }
        } catch (Exception e) {
            this.writeErrorLog(e, "BatchJobLibrary判斷營業日發生例外:", e.getMessage());
            return false;
        }
    }

    /**
     * 通知分行系統
     *
     * @param userId     要通知的櫃員編號
     * @param fileName   已完成的批號
     * @param systemName 系統名稱，請先填FCS，若確定要做再請分行增加FEP
     * @return
     */
    public boolean notifyBranch(String userId, String fileName, String systemName) {
        // TODO
        throw ExceptionUtil.createNotImplementedException("Not finished yet");
    }

    /**
     * 丟訊息到Batch Queue中
     *
     * @param hostName
     * @throws Exception
     */
    private void sendBatchQueue(String hostName) throws Exception {
        JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        // 預設Destination取JmsMsgConfiguration中的配置
        PlainTextMessage payload = new PlainTextMessage(JmsKind.QUEUE, configuration.getQueueNames().getBatch().getDestination(), StringUtils.EMPTY);
        boolean send = false;
        if (SpringBeanFactoryUtil.isBeanExist(BatchQueueHostConstant.JMS_OPERATOR_MAP)) {
            Map<String, BatchQueueHostOperator> hostNameToOperatorMap = SpringBeanFactoryUtil.getBean(BatchQueueHostConstant.JMS_OPERATOR_MAP);
            Map<String, BatchQueueHostConfigurationProperties> hostNameToPropertiesMap = SpringBeanFactoryUtil.getBean(BatchQueueHostConstant.JMS_PROPERTIES_MAP);
            if (hostNameToOperatorMap != null) {
                // 如果hostName沒有指定, 則所有的hostName都要丟
                if (StringUtils.isBlank(hostName)) {
                    Exception thrown = null;
                    for (Entry<String, BatchQueueHostOperator> entry : hostNameToOperatorMap.entrySet()) {
                        // 這裡要clone一份, 改變裡面的hostName再送出去
                        FEPBatch clone = new FEPBatch();
                        BeanUtils.copyProperties(this.jobData.getScheduleTask(), clone.getScheduleTask());
                        BeanUtils.copyProperties(this.jobData.getTaskParameters(), clone.getTaskParameters());
                        // 根據Batch Host配置檔自定義的Batch的Queue Name塞入Destination
                        this.setDestination(payload, hostNameToPropertiesMap, entry.getKey());
                        try {
                            // sendBatchQueue
                            this.sendBatchQueue(entry.getValue(), entry.getKey(), clone, payload);
                        } catch (Exception e) {
                            thrown = e;
                        }
                    }
                    if (thrown != null) {
                        throw thrown;
                    }
                    send = true;
                } else {
                    // 丟訊息到指定的hostName所在的Queue上
                    BatchQueueHostOperator operator = hostNameToOperatorMap.get(hostName);
                    if (operator != null) {
                        // 根據Batch Host配置檔自定義的Batch的Queue Name塞入Destination
                        this.setDestination(payload, hostNameToPropertiesMap, hostName);
                        // sendBatchQueue
                        this.sendBatchQueue(operator, hostName, this.jobData, payload);
                        send = true;
                    }
                }
            }
        }
        if (!send) {
            BatchQueueOperator batchQueueOperator = SpringBeanFactoryUtil.getBean(BatchQueueOperator.class);
            this.sendBatchQueue(batchQueueOperator, hostName, this.jobData, payload);
        }
    }

    /**
     * 根據Batch Host配置檔自定義的Batch的Queue Name塞入Destination
     *
     * @param payload
     * @param hostNameToPropertiesMap
     * @param hostName
     */
    private void setDestination(PlainTextMessage payload, Map<String, BatchQueueHostConfigurationProperties> hostNameToPropertiesMap, String hostName) {
        if (hostNameToPropertiesMap != null) {
            BatchQueueHostConfigurationProperties properties = hostNameToPropertiesMap.get(hostName);
            if (properties != null && properties.getQueueNames() != null
                    && properties.getQueueNames().getBatch() != null && StringUtils.isNotBlank(properties.getQueueNames().getBatch().getDestination())) {
                payload.setDestination(properties.getQueueNames().getBatch().getDestination());
            }
        }
    }

    private void sendBatchQueue(JmsPayloadOperator operator, String hostName, FEPBatch jobData, PlainTextMessage payload) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        if (StringUtils.isNotBlank(jobData.getScheduleTask().getActionArguments())
                && !jobData.getScheduleTask().getActionArguments().contains("/hostName:"))
            jobData.getScheduleTask().setActionArguments(
                    StringUtils.join("/hostName:", hostName, StringUtils.SPACE, jobData.getScheduleTask().getActionArguments()));
        payload.setPayload(serializeToXml(jobData));
        LogData log = new LogData();
        try {
            operator.sendQueue(payload, null, null);
            log.setProgramName(StringUtils.join(ProgramName, ".sendBatchQueue"));
            log.setRemark(StringUtils.join("sendBatchQueue successful",
                    " hostName = [", hostName, "],",
                    " queueName = [", payload.getDestination(), "],",
                    " batchId = [", jobData.getTaskParameters().getBatchId(), "],",
                    " batchName = [", jobData.getScheduleTask().getTaskName(), "]!!!"));
            this.logMessage(log);
        } catch (Exception e) {
            log.setProgramName(StringUtils.join(ProgramName, ".sendBatchQueue"));
            log.setRemark(StringUtils.join(
                    "sendBatchQueue failed,",
                    " hostName = [", hostName, "],",
                    " queueName = [", payload.getDestination(), "],",
                    " batchId = [", jobData.getTaskParameters().getBatchId(), "],",
                    " batchName = [", jobData.getScheduleTask().getTaskName(), "]!!!"));
            log.setProgramException(e);
            sendEMS(log);
            throw e;
        }
    }

    /**
     * 獲取Task實例
     *
     * @param programName
     * @return
     * @throws Exception
     */
    public Task getBatchTask(String programName) throws Exception {
        // 先嘗試用內部引用方式獲取
        try {
            Class<?> taskClazz = Class.forName(programName);
            return (Task) taskClazz.newInstance();
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().trace("standalone環境下找不到是正常的, ", e.getMessage());
        }
        // 如果取不到, 則改用外部jar檔獲取
        BatchBaseConfiguration configuration = SpringBeanFactoryUtil.getBean(BatchBaseConfiguration.class);
        File jarPath = new File(CleanPathUtil.cleanString(configuration.getTask().getPath()),
                CleanPathUtil.cleanString(MessageFormat.format(configuration.getTask().getJarNameTemplate(), programName.substring(programName.lastIndexOf(".") + 1))));
        return ReflectUtil.dynamicLoadClass(jarPath.getAbsolutePath(), programName);
    }

    public void dispose() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public void InsertTWSLog(String hostName, byte batchResult, String taskname, String jarfile) {
        // 執行時間
        long nowTimeMillis = System.currentTimeMillis();
        long datetimeLong = nowTimeMillis - this.startTimeMillis;
        int datetime = (datetimeLong <= Integer.MAX_VALUE) ? (int) datetimeLong : 0;
        this.writeLog("startTimeMillis : " + this.startTimeMillis);
        this.writeLog("nowTimeMillis : " + nowTimeMillis);
        this.writeLog("datetime(Long) : " + datetimeLong);

        // 系統時間
        Timestamp systemDateTime = new Timestamp(nowTimeMillis);
        this.writeLog("systemDateTime : " + systemDateTime);

        // 主機名稱
        if (StringUtils.length(hostName) > 10) {
            hostName = StringUtils.substring(hostName, 0, 9);
        }
        this.writeLog("hostName : " + hostName);

        // 執行結果
        String batchResultStr = BatchResult.fromValue(batchResult).name();
        if (StringUtils.length(batchResultStr) > 10) {
            batchResultStr = StringUtils.substring(batchResultStr, 0, 9);
        }
        this.writeLog("batchResultStr : " + batchResultStr);

        // LOG內容
        this.writeLog("this.logFile : " + this.logFile);
        File file = new File(this.logFile);
        StringBuilder sb = new StringBuilder();
        try ( //
              FileInputStream fis = new FileInputStream(file); //
              InputStreamReader isr = new InputStreamReader(fis); //
              BufferedReader br = new BufferedReader(isr); //
        ) {
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            this.writeErrorLog(e, "BatchJobLibrary寫入TWSLog發生例外:", e.getMessage());
        }

        Twslog twslog = new Twslog();
        twslog.setTwsDatetime(systemDateTime);
        twslog.setTwsHostname(hostName);
        twslog.setTwsDuration(datetime);
        twslog.setTwsResult(batchResultStr);
        twslog.setTwsTaskname(taskname);
        twslog.setTwsJarfile(jarfile);
        twslog.setTwsLogfilecontent(sb.toString());

        TwslogExtMapper twslogMapper = SpringBeanFactoryUtil.getBean(TwslogExtMapper.class);
        int count = twslogMapper.insertTwslog(twslog);
        this.writeLog("BatchJobLibrary寫入TWSLog筆數為 : " + count);
    }
}
