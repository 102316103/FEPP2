package com.syscom.fep.vo.monitor;

/**
 * 網路狀態-CLIENT
 *
 * @author ZK
 *
 */
public class ClientNetworkStatus extends ServiceStatus {
    /**
     * 本機連接埠
     */
    private String localEndPoint;
    /**
     * 遠端連接埠
     */
    private String remoteEndPoint;
    /**
     * 連線狀態
     */
    private String state;
    /**
     * 目前連接數
     */
    private String socketCount;

    public String getLocalEndPoint() {
        return localEndPoint;
    }

    public void setLocalEndPoint(String localEndPoint) {
        this.localEndPoint = localEndPoint;
    }

    public String getRemoteEndPoint() {
        return remoteEndPoint;
    }

    public void setRemoteEndPoint(String remoteEndPoint) {
        this.remoteEndPoint = remoteEndPoint;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSocketCount() {
        return socketCount;
    }

    public void setSocketCount(String socketCount) {
        this.socketCount = socketCount;
    }
}
