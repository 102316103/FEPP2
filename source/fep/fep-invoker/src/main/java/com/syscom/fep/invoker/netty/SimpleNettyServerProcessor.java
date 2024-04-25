package com.syscom.fep.invoker.netty;

public abstract class SimpleNettyServerProcessor<MessageIn, MessageOut> extends SimpleNettyBaseMethod implements SimpleNettyConnStateListener {
    /**
     * 客戶端的IP
     */
    private String clientIP;
    /**
     * 客戶端的Port
     */
    private int clientPort;

    /**
     * 處理進來的電文並回應
     *
     * @param messageIn
     * @return
     * @throws Exception
     */
    public abstract MessageOut processRequestData(MessageIn messageIn) throws Exception;

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }
}
