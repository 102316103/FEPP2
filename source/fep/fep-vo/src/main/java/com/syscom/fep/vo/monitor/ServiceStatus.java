package com.syscom.fep.vo.monitor;


import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * 服務狀態
 *
 * @author ZK
 */
public class ServiceStatus implements Serializable {
    private String identity = StringUtils.EMPTY;
    /**
     * 服務名稱
     */
    private String serviceName;
    /**
     * 服務主機名稱
     */
    private String serviceHostName;
    /**
     * 服務IP
     */
    private String serviceIP;
    /**
     * 狀態
     */
    private String serviceState;
    /**
     * 啟動時間
     */
    private String startTime;
    /**
     * 停止時間
     */
    private String stopTime;
    /**
     * CPU
     */
    private String serviceCpu;
    /**
     * RAM
     */
    private String serviceRam;
    /**
     * 執行緒
     */
    private String serviceThreads;
    private String monitorPort;
    private String type = StringUtils.EMPTY;
    private String updateDateTime;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceHostName() {
        return serviceHostName;
    }

    public void setServiceHostName(String serviceHostName) {
        this.serviceHostName = serviceHostName;
    }

    public String getServiceIP() {
        return serviceIP;
    }

    public void setServiceIP(String serviceIP) {
        this.serviceIP = serviceIP;
    }

    public String getServiceState() {
        return serviceState;
    }

    public void setServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getServiceCpu() {
        return serviceCpu;
    }

    public void setServiceCpu(String serviceCpu) {
        this.serviceCpu = serviceCpu;
    }

    public String getServiceRam() {
        return serviceRam;
    }

    public void setServiceRam(String serviceRam) {
        this.serviceRam = serviceRam;
    }

    public String getServiceThreads() {
        return serviceThreads;
    }

    public void setServiceThreads(String serviceThreads) {
        this.serviceThreads = serviceThreads;
    }

    public String getMonitorPort() {
        return monitorPort;
    }

    public void setMonitorPort(String monitorPort) {
        this.monitorPort = monitorPort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }
}
