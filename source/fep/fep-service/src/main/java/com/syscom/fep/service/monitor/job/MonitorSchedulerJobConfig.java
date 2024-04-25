package com.syscom.fep.service.monitor.job;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import com.syscom.fep.service.monitor.vo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "spring.fep.service.monitor")
@RefreshScope
public class MonitorSchedulerJobConfig extends SchedulerJobConfig {
    private boolean recordHttpLog = false;
    private String mailSender;
    @Value("#{'${spring.fep.service.monitor.mailList:}'.split(',')}")
    private String[] mailList;
    private double ruleDiskRate;
    private String notifyInterval;
    @Value("#{'${spring.fep.service.monitor.processNameList:}'.split(',')}")
    private List<String> processNameList;
    @NestedConfigurationProperty
    private MonitorServerInfo system = new MonitorServerInfo();
    @NestedConfigurationProperty
    private List<MonitorServerInfo> services = Collections.synchronizedList(new ArrayList<>());
    @NestedConfigurationProperty
    private List<MonitorMQServerInfo> mqs = Collections.synchronizedList(new ArrayList<>());
    @NestedConfigurationProperty
    private List<MonitorServerLauncherInfo> launchers = Collections.synchronizedList(new ArrayList<>());
    @NestedConfigurationProperty
    private List<MonitorNetstatInfo> netstat = Collections.synchronizedList(new ArrayList<>());
    @NestedConfigurationProperty
    private List<MonitorSuipConnectionInfo> localSuip = Collections.synchronizedList(new ArrayList<>());
    @NestedConfigurationProperty
    private List<MonitorSuipConnectionInfo> remoteSuip = Collections.synchronizedList(new ArrayList<>());
    @NestedConfigurationProperty
    private List<MonitorHSMInfo> hsm = Collections.synchronizedList(new ArrayList<>());
    private boolean enableAutoRestart = true;
    private boolean stopNotification = false;

    public String getMailSender() {
        return mailSender;
    }

    public void setMailSender(String mailSender) {
        this.mailSender = mailSender;
    }

    public String[] getMailList() {
        return mailList;
    }

    public void setMailList(String[] mailList) {
        this.mailList = mailList;
    }

    public double getRuleDiskRate() {
        return ruleDiskRate;
    }

    public void setRuleDiskRate(double ruleDiskRate) {
        this.ruleDiskRate = ruleDiskRate;
    }

    public String getNotifyInterval() {
        return notifyInterval;
    }

    public void setNotifyInterval(String notifyInterval) {
        this.notifyInterval = notifyInterval;
    }

    public List<String> getProcessNameList() {
        return processNameList;
    }

    public void setProcessNameList(List<String> processNameList) {
        this.processNameList = processNameList;
    }

    public MonitorServerInfo getSystem() {
        return system;
    }

    public List<MonitorServerInfo> getServices() {
        return services;
    }

    public List<MonitorMQServerInfo> getMqs() {
        return mqs;
    }

    public List<MonitorNetstatInfo> getNetstat() {
        return netstat;
    }

    public List<MonitorSuipConnectionInfo> getLocalSuip() {
        return localSuip;
    }

    public List<MonitorSuipConnectionInfo> getRemoteSuip() {
        return remoteSuip;
    }

    public List<MonitorHSMInfo> getHsm() {
        return hsm;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    public List<MonitorServerLauncherInfo> getLaunchers() {
        return launchers;
    }

    public boolean isEnableAutoRestart() {
        return enableAutoRestart;
    }

    public void setEnableAutoRestart(boolean enableAutoRestart) {
        this.enableAutoRestart = enableAutoRestart;
    }

    public boolean isStopNotification() {
        return stopNotification;
    }

    public void setStopNotification(boolean stopNotification) {
        this.stopNotification = stopNotification;
    }

    @PostConstruct
    public void print(){
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "AppMonitor Service Configuration", true));
    }
}
