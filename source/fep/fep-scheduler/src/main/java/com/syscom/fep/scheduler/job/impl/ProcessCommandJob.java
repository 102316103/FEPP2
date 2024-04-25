package com.syscom.fep.scheduler.job.impl;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.scheduler.job.SchedulerSimpleJob;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProcessCommandJob<JobConfig extends ProcessCommandJobConfig> extends SchedulerSimpleJob<JobConfig> {
    private final ExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(this.getClass().getSimpleName()));

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, JobConfig config) throws Exception {
        LogData logData = new LogData();
        logData.setProgramName(StringUtils.join(ProgramName, "-", config.getIdentity(), ".executeJob"));
        logData.setRemark(StringUtils.join("Start to execute command = [", config.getCommand(), "]"));
        this.logMessage(logData);
        ProcessBuilder processBuilder = new ProcessBuilder().command(config.getCommand().split("\\s+"));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            Consumer<String> consumer = config.isOutput() ? LogHelperFactory.getTraceLogger()::debug : null;
            String charsetName = StringUtils.isBlank(config.getOutputCharsetName()) ? StandardCharsets.UTF_8.displayName() : config.getOutputCharsetName();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), charsetName, consumer);
            executor.submit(streamGobbler);
            logData.setRemark(StringUtils.join("Execute successful, command = [", config.getCommand(), "]"));
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Execute command = [", config.getCommand(), "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
        }
    }

    @PreDestroy
    public void destroy() {
        String name = ProgramName;
        ScheduleLogger.trace(name, " start to destroy...");
        try {
            this.executor.shutdown(); // 記得要關閉
            if (executor.awaitTermination(60, TimeUnit.SECONDS))
                ScheduleLogger.trace(name, " executor terminate all runnable successful");
            else
                ScheduleLogger.trace(name, " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            ScheduleLogger.warn(e, e.getMessage());
        }
    }
}
