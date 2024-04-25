package com.syscom.fep.batch.invoker;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Bsdays;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Component("jobSchedulerInvoker")
public class JobSchedulerInvoker extends FEPBase {
    private static final String PROGRAM_NAME = JobSchedulerInvoker.class.getSimpleName();

    @Autowired
    private BatchConfiguration configuration;
    @Autowired
    private BatchExtMapper batchMapper;
    @Autowired
    private BsdaysExtMapper bsdaysMapper;
    @Autowired
    private FEPConfig fepConfig;

    public void invoke(String[] args) {
        LogHelperFactory.getTraceLogger().info("BatchJobSchedulerInvoker Begin");
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.CMN);
        logData.setChannel(FEPChannel.BATCH);
        logData.setProgramName(StringUtils.join(PROGRAM_NAME, ".invoke"));
        logData.setMessage(StringUtils.join(args, StringUtils.SPACE));
        logData.setRemark("BatchJobSchedulerInvoker Begin");
        this.logMessage(logData);
        try {
            Map<String, String> arguments = this.extractBatchParameter(args);
            Batch batch = batchMapper.selectByPrimaryKey(Integer.parseInt(arguments.get("batchid")));
            if (batch != null) {
                // 檢核可執行的系統別
                if (batch.getBatchSubsys() != null && configuration.getSubSys().indexOf(batch.getBatchSubsys().toString()) < 0) {
                    logData.setRemark(LogHelperFactory.getTraceLogger().warn("BatchJobSchedulerInvoker Cancel run Batch ", arguments.get("batchid"), " because Subsys is not allowed."));
                    this.logMessage(logData);
                }
                // 批次平台在接收批次啟動要求時,如果BATCH_EXECUTE_HOST_NAME欄位是空值(不指定), 或符合本機的計算機名稱, 才執行此批次
                if (StringUtils.isNotBlank(batch.getBatchExecuteHostName()) && !fepConfig.getHostName().equals(batch.getBatchExecuteHostName())) {
                    logData.setRemark(LogHelperFactory.getTraceLogger().warn("BatchJobSchedulerInvoker Cancel run Batch ", arguments.get("batchid"), " because Batch Execute HostName Inconsistently."));
                    this.logMessage(logData);
                    return;
                }
                if (!DbHelper.toBoolean(batch.getBatchEnable())) {
                    logData.setRemark(LogHelperFactory.getTraceLogger().warn("BatchJobSchedulerInvoker Cancel run Batch ", arguments.get("batchid"), " because Batch is disabled."));
                    this.logMessage(logData);
                    return;
                }
                // 檢核是否營業日才執行
                if (DbHelper.toBoolean(batch.getBatchCheckbusinessdate())) {
                    if (!this.checkBusinessDate(batch.getBatchZone())) {
                        logData.setRemark(LogHelperFactory.getTraceLogger().warn("BatchJobService Cancel Run by CheckBusinessDay fail, batchId:", arguments.get("batchid")));
                        this.logMessage(logData);
                        return;
                    }
                }
                // 一天只能做一次且今天已做過則不做直接離開
                if (DbHelper.toBoolean(batch.getBatchSingletime())
                        && (batch.getBatchLastruntime() != null && CalendarUtil.equals(batch.getBatchLastruntime(), Calendar.getInstance().getTime(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN))
                        && "1".equals(batch.getBatchResult())) {
                    logData.setRemark(LogHelperFactory.getTraceLogger().warn("BatchJobSchedulerInvoker Cancel run Batch ", arguments.get("batchid"), " because already run once."));
                    this.logMessage(logData);
                    return;
                }
                BatchJobLibrary batchLib = new BatchJobLibrary();
                batchLib.startBatch(
                        fepConfig.getHostName(),
                        arguments.get("batchid"),
                        arguments.get("jobid"));
                logData.setRemark(LogHelperFactory.getTraceLogger().warn("BatchJobSchedulerInvoker Start Batch OK:", arguments.get("batchid")));
                this.logMessage(logData);
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logMessage(Level.ERROR, logData);
        }
    }

    private boolean checkBusinessDate(String zone) {
        try {
            Bsdays bsdays = bsdaysMapper.selectByPrimaryKey(zone, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            if (bsdays != null) {
                return DbHelper.toBoolean(bsdays.getBsdaysWorkday());
            }
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
        }
        return false;
    }

    private Map<String, String> extractBatchParameter(String[] args) {
        LogHelperFactory.getTraceLogger().info("BatchJobSchedulerInvoker ExtractBatchParameter ", args.length);
        Map<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String[] arg = args[i].split(":");
            arguments.put(arg[0].substring(1), arg[1]);
        }
        return arguments;
    }
}
