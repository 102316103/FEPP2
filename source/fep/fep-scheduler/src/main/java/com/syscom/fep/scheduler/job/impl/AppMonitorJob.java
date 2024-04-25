package com.syscom.fep.scheduler.job.impl;

import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.scheduler.job.SchedulerJob;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;

import javax.annotation.PreDestroy;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 執行獲取 Service 監控資料
 *
 * @author Chen Yang
 */
public class AppMonitorJob extends SchedulerJob<AppMonitorJobConfig> {
    private final HttpClient httpClient = new HttpClient(this.getJobConfig().isRecordHttpLog()); // 避免log太多, 這裡設置false不記錄http的log

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, AppMonitorJobConfig config) throws Exception {
        Map<String, Object> smsMap = new HashMap<String, Object>();
        smsMap.put("smsServiceip", config.getServiceHostIp());
        smsMap.put("smsServicename", config.getServiceName());
        smsMap.put("smsHostname", config.getServiceHostName());
        smsMap.put("smsRam", getServiceMemoData(config));
        smsMap.put("smsCpu", getServiceCpuData(config));
        smsMap.put("smsThreads", getServiceThreadData(config));
        String startTime = getServicesStartTime(config);
        if (StringUtils.isNotBlank(startTime)) {
            smsMap.put("smsStarttime", startTime);
            smsMap.put("smsServicestate", "1");
        } else {
            smsMap.put("smsServicestate", "0");
        }
        sendMonitorMessage(smsMap, config);
    }

    @PreDestroy
    public void terminateJob() {
        AppMonitorJobConfig config = this.getJobConfig();
        Map<String, Object> smsMap = new HashMap<>();
        smsMap.put("smsServiceip", config.getServiceHostIp());
        smsMap.put("smsServicename", config.getServiceName());
        smsMap.put("smsHostname", config.getServiceHostName());
        smsMap.put("smsRam", "0");
        smsMap.put("smsCpu", "0");
        smsMap.put("smsThreads", "0");
        smsMap.put("smsServicestate", "0");
        smsMap.put("smsStoptime", FormatUtil.dateTimeFormat(Calendar.getInstance().getTime()));
        sendMonitorMessage(smsMap, config);
    }

    /*
     * 獲取 Service StartTime
     */
    private String getServicesStartTime(AppMonitorJobConfig config) {
        String startTime = "";
        String promQL = "/actuator/metrics/process.start.time";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            String jsonStr = httpClient.doGet(url, false);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                if (rootObject != null) {
                    JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                    if (measurementsJsonArray != null && measurementsJsonArray.length() > 0) {
                        JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                        startTime = FormatUtil.dateTimeFormat((long) (resultObject.getDouble("value") * 1000));
                    }
                }
            } else {
                warn("getServicesStartTime with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServicesStartTime failed with exception occur = [", e.getMessage(), "]");
        }
        return startTime;
    }

    /*
     * 獲取 Service Memo
     */
    private Integer getServiceMemoData(AppMonitorJobConfig config) {
        Integer memo = 0;
        String promQL = "/actuator/metrics/jvm.memory.used";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            String jsonStr = httpClient.doGet(url, false);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                if (rootObject != null) {
                    JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                    if (measurementsJsonArray != null && measurementsJsonArray.length() > 0) {
                        JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                        memo = resultObject.getInt("value") / 1024;
                    }
                }
            } else {
                warn("getServiceMemoData with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServiceMemoData failed with exception occur = [", e.getMessage(), "]");
        }
        return memo;
    }

    /*
     * 獲取 Service Cpu
     */
    private Integer getServiceCpuData(AppMonitorJobConfig config) {
        Integer cpu = 0;
        String promQL = "/actuator/metrics/process.cpu.usage";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            String jsonStr = httpClient.doGet(url, false);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                if (rootObject != null) {
                    JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                    if (measurementsJsonArray != null && measurementsJsonArray.length() > 0) {
                        JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                        if (resultObject.getDouble("value") != -1.0) {
                            cpu = new Integer(new DecimalFormat("0").format(resultObject.getDouble("value") * 100 * 100));
                        }
                    }
                }
            } else {
                warn("getServiceCpuData with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServiceCpuData failed with exception occur = [", e.getMessage(), "]");
        }
        return cpu;
    }

    /*
     * 獲取 Service Thread
     */
    private Integer getServiceThreadData(AppMonitorJobConfig config) {
        Integer thread = 0;
        String promQL = "/actuator/metrics/jvm.threads.states";
        String url = StringUtils.join(config.getServiceUrl(), promQL);
        try {
            String jsonStr = httpClient.doGet(url, false);
            if (StringUtils.isNotBlank(jsonStr)) {
                JSONObject rootObject = new JSONObject(jsonStr);
                if (rootObject != null) {
                    JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
                    if (measurementsJsonArray != null && measurementsJsonArray.length() > 0) {
                        JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
                        thread = resultObject.getInt("value");
                    }
                }
            } else {
                warn("getServiceThreadData with empty response!!!");
            }
        } catch (Exception e) {
            warn("getServiceThreadData failed with exception occur = [", e.getMessage(), "]");
        }
        return thread;
    }

    /*
     * 發送資料到Monitor Server
     */
    private void sendMonitorMessage(Map<String, Object> smsMap, AppMonitorJobConfig config) {
        boolean postFailed = false;
        String primaryUrl = config.getMonitorPrimaryUrl();
        if (StringUtils.isNotBlank(primaryUrl)) {
            try {
                httpClient.doPost(primaryUrl, smsMap, false);
            } catch (Exception e) {
                postFailed = true;
                warn("sendMonitorMessage to primaryUrl = [", primaryUrl, "], failed with exception occur = [", e.getMessage(), "]");
            }
        } else {
            warn("cannot sendMonitorMessage, primaryUrl is blank!!!");
        }
        if (postFailed) {
            postFailed = false;
            String secondaryUrl = config.getMonitorSecondaryUrl();
            if (StringUtils.isNotBlank(secondaryUrl)) {
                try {
                    httpClient.doPost(secondaryUrl, smsMap, false);
                } catch (Exception e) {
                    postFailed = true;
                    warn("sendMonitorMessage to secondaryUrl = [", secondaryUrl, "], failed with exception occur = [", e.getMessage(), "]");
                }
            } else {
                warn("cannot sendMonitorMessage, secondaryUrl is blank!!!");
            }
        }
    }

    private void warn(Object... msgs) {
        if (this.getJobConfig().isRecordHttpLog()) {
            ScheduleLogger.warn(msgs);
        }
    }
}
