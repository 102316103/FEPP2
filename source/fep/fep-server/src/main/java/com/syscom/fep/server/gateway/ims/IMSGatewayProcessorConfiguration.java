package com.syscom.fep.server.gateway.ims;

public class IMSGatewayProcessorConfiguration {
    private String host;
    private int port;
    private long reestablishConnectionInterval = 10000;
    private String clientId;
    private int resumeInterval = 120000;
    private String dataStore;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getReestablishConnectionInterval() {
        return reestablishConnectionInterval;
    }

    public void setReestablishConnectionInterval(long reestablishConnectionInterval) {
        this.reestablishConnectionInterval = reestablishConnectionInterval;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getResumeInterval() {
        return resumeInterval;
    }

    public void setResumeInterval(int resumeInterval) {
        this.resumeInterval = resumeInterval;
    }

    public String getDataStore() {
        return dataStore;
    }

    public void setDataStore(String dataStore) {
        this.dataStore = dataStore;
    }
}
