package com.syscom.fep.service.monitor.job;

import com.google.gson.reflect.TypeToken;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPMonitorConfig;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.parse.GsonDateParser;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.SmsExtMapper;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.scheduler.job.SchedulerJob;
import com.syscom.fep.service.monitor.svr.MonitorSchedulerService;
import com.syscom.fep.vo.monitor.MonitorConstant;
import com.syscom.fep.vo.monitor.ServerNetworkStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonitorDumpJob extends SchedulerJob<MonitorDumpJobConfig> implements MonitorConstant {
    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_APPMON);
    }

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, MonitorDumpJobConfig config) throws Exception {
        List<String> lineList = null;
        SmsExtMapper smsExtMapper = SpringBeanFactoryUtil.getBean(SmsExtMapper.class);
        List<Sms> dumpSmsList = new ArrayList<>();
        List<Sms> fepMonitorServiceList = smsExtMapper.queryFEPMonitorService();
        if (CollectionUtils.isNotEmpty(fepMonitorServiceList)) dumpSmsList.addAll(fepMonitorServiceList);
        // 把Process的信息也加入
        List<Sms> smsProcessList = this.fetchProcess(context, config);
        if (CollectionUtils.isNotEmpty(smsProcessList)) dumpSmsList.addAll(smsProcessList);
        if (CollectionUtils.isNotEmpty(dumpSmsList)) {
            // 2023-09-14 Richard add 有一些服務的名字需要用另外的名字顯示, 所以這裡要判斷轉換一下
            this.convertServiceNameToAlias(dumpSmsList);
            String delimiter = config.getDelimiter();
            StringBuilder sb = new StringBuilder(16);
            lineList = new ArrayList<>(dumpSmsList.size());
            for (Sms sms : dumpSmsList) {
                // 如果服務名稱在ExcludeServiceNameList中, 則不要dump到檔案中
                if (CollectionUtils.isNotEmpty(config.getExcludeServiceNameList())
                        && config.getExcludeServiceNameList().contains(sms.getSmsServicename())) {
                    continue;
                }
                // Timestamp
                if (sms.getSmsUpdatetime() != null) {
                    sb.append(FormatUtil.dateTimeFormat(CalendarUtil.clone(sms.getSmsUpdatetime()), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
                } else {
                    sb.append(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
                }
                sb.append(delimiter);
                // Hostname
                sb.append(sms.getSmsHostname()).append("-"); // 2023-09-08 Richard add for 產出apcheck.log時的servicename,前面要加hostname by Ashiang
                sb.append(sms.getSmsServicename()); // 2023-06-27 Richard modified 改為AP Name
                sb.append(delimiter);
                // Status|Message
                if ("0".equals(sms.getSmsServicestate())) {
                    sb.append("Error");
                    sb.append(delimiter);
                    sb.append(sms.getSmsServicename()).append(" is stopped!");
                } else if ("1".equals(sms.getSmsServicestate())) {
                    sb.append("OK");
                    sb.append(delimiter);
                    sb.append(sms.getSmsServicename()).append(" has started!");
                } else {
                    sb.append("Unknown");
                    sb.append(delimiter);
                    sb.append(sms.getSmsServicename()).append(" unknown status!");
                }
                lineList.add(sb.toString());
                sb.setLength(0);
            }
        }
        // 記錄HSM的狀態
        if (lineList == null) {
            lineList = new ArrayList<>();
        }
        this.appendHSM(context, config, lineList);
        File file = new File(config.getPath());
        if (!file.getParentFile().exists()) {
            FileUtils.forceMkdirParent(file);
        }
        if (CollectionUtils.isNotEmpty(lineList)) {
            FileUtils.writeLines(file, StandardCharsets.UTF_8.displayName(), lineList, false);
        }
    }

    private List<Sms> fetchProcess(JobExecutionContext context, MonitorDumpJobConfig config) {
        SmsExtMapper smsExtMapper = SpringBeanFactoryUtil.getBean(SmsExtMapper.class);
        List<Sms> smsProcessList = smsExtMapper.selectByServiceName(SERVICE_NAME_PROCESS);
        List<Sms> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(smsProcessList)) {
            GsonDateParser<List<Sms>> gsonParser = new GsonDateParser<List<Sms>>(new TypeToken<List<Sms>>() {}.getType());
            for (Sms smsProcess : smsProcessList) {
                try {
                    List<Sms> list = gsonParser.readIn(smsProcess.getSmsOthers());
                    if (CollectionUtils.isNotEmpty(list)) {
                        result.addAll(list);
                    }
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 有一些服務的名字需要用另外的名字顯示, 所以這裡要判斷轉換一下
     *
     * @param dumpSmsList
     */
    private void convertServiceNameToAlias(List<Sms> dumpSmsList) {
        FEPMonitorConfig fepMonitorConfig = SpringBeanFactoryUtil.registerBean(FEPMonitorConfig.class, false);
        if (fepMonitorConfig != null && CollectionUtils.isNotEmpty(dumpSmsList)) {
            for (Sms sms : dumpSmsList) {
                sms.setSmsServicename(fepMonitorConfig.getAliasByServiceName(sms.getSmsServicename()));
            }
        }
    }

    /**
     * 記錄HSM的狀態
     *
     * @param context
     * @param config
     * @param lineList
     */
    private void appendHSM(JobExecutionContext context, MonitorDumpJobConfig config, List<String> lineList) {
        MonitorSchedulerService service = SpringBeanFactoryUtil.getBean(MonitorSchedulerService.class, false);
        if (service == null) return;
        List<ServerNetworkStatus> list = service.getHsmMonitorDataList();
        ServerNetworkStatus[] statuses = new ServerNetworkStatus[list.size()];
        list.toArray(statuses);
        if (ArrayUtils.isNotEmpty(statuses)) {
            String delimiter = config.getDelimiter();
            StringBuilder sb = new StringBuilder(16);
            for (ServerNetworkStatus status : statuses) {
                // Timestamp
                sb.append(status.getUpdateDateTime());
                sb.append(delimiter);
                // HSM Name
                sb.append(status.getServiceName());
                sb.append(delimiter);
                // Status
                if ("0".equals(status.getServiceState())) {
                    sb.append("Error");
                } else if ("1".equals(status.getServiceState())) {
                    sb.append("OK");
                }
                sb.append(delimiter);
                // IP & Port & DisConnected or Connected
                sb.append(status.getServiceIP()).append(":").append(status.getServicePort());
                if ("0".equals(status.getServiceState())) {
                    sb.append(" DisConnected!");
                } else if ("1".equals(status.getServiceState())) {
                    sb.append(" Connected!");
                }
                lineList.add(sb.toString());
                sb.setLength(0);
            }
        }
    }
}
