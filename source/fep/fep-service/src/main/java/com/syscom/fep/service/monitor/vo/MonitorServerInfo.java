package com.syscom.fep.service.monitor.vo;

import org.apache.commons.lang3.StringUtils;

public class MonitorServerInfo {
    private String name;
    private String hostname;
    private String hostip;
    private String port;
    private String contextPath = StringUtils.EMPTY;
    private String protocol = "http";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostip() {
        return hostip;
    }

    public void setHostip(String hostip) {
        this.hostip = hostip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
