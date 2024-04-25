package com.syscom.fep.common.monitor;

import com.sun.management.OperatingSystemMXBean;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class MonitorDataCollector {

    private MonitorDataCollector() {
    }

    /**
     * 獲取系統CPU使用率
     *
     * @param httpClient
     * @param url
     * @return
     * @throws Throwable
     */
    public static Number fetchSystemCpuUsage(final HttpClient httpClient, final String url) throws Throwable {
        String promQL = "/actuator/metrics/system.cpu.usage";
        String jsonStr = httpClient.doGet(StringUtils.join(url, promQL), false);
        JSONObject rootObject = new JSONObject(jsonStr);
        JSONArray measurementsJsonArray = rootObject.getJSONArray("measurements");
        if (measurementsJsonArray != null && !measurementsJsonArray.isEmpty()) {
            JSONObject resultObject = measurementsJsonArray.getJSONObject(0);
            return resultObject.getDouble("value") * 100 * 100;
        }
        return 0;
    }

    /**
     * 獲取系統記憶體已經使用MB
     *
     * @return
     * @throws Throwable
     */
    @SuppressWarnings({"deprecation"})
    public static Number fetchSystemMemoryUsage() throws Throwable {
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return (double) (bean.getTotalPhysicalMemorySize() - bean.getFreePhysicalMemorySize()) / 1024;
    }

    /**
     * 獲取磁盤信息
     *
     * @param hostName
     * @param hostIP
     * @return
     * @throws Throwable
     */
    public static List<MonitorDataDisk> fetchSystemHardDisk(final String hostName, final String hostIP) throws Throwable {
        List<MonitorDataDisk> list = new ArrayList<>();
        File[] roots = File.listRoots();
        for (File root : roots) {
            MonitorDataDisk disk = new MonitorDataDisk();
            disk.setHostName(hostName);
            disk.setName(root.getPath());
            disk.setIp(hostIP);
            disk.setTotal(root.getTotalSpace());
            disk.setFree(root.getFreeSpace());
            disk.setUsed(disk.getTotal() - disk.getFree());
            list.add(disk);
        }
        return list;
    }
}
