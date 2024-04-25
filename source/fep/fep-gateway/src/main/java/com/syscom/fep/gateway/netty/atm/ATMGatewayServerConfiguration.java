package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionServerConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "spring.fep.gateway.transmission.atm")
@RefreshScope
public class ATMGatewayServerConfiguration extends NettyTransmissionServerConfiguration {
    public static final String BYPASSCHECKATMIP_ALL = "ALL";
    private String bypassCheckAtmIp;
    private boolean checkAtmmstrByAtmNo = false; // 是否以Atmno檢核ATMMSTR檔資料
    private boolean forwardTransmissionToFep = true; // 是否將交易類電文轉發給FEP, 否則直接返回給Client
    private int checkClientFailedTimesLimit = 3; // 連線超過checkClientFailedTimesLimit次error進入黑名單
    private int clearBlackListInterval = 3; // 黑名單每隔clearBlackListInterval分鐘自動清除

    @PostConstruct
    public void initATMGatewayServer() {
        super.setSocketType(SocketType.Server);
        super.setGateway(Gateway.ATMGW);
    }

    /**
     * @return
     */
    @Override
    public final Gateway getGateway() {
        return super.getGateway();
    }

    /**
     * @param gateway
     */
    @Override
    public final void setGateway(Gateway gateway) {
    }

    /**
     * @return
     */
    @Override
    public final SocketType getSocketType() {
        return super.getSocketType();
    }

    /**
     * @param socketType
     */
    @Override
    public final void setSocketType(SocketType socketType) {
    }

    public String getBypassCheckAtmIp() {
        return bypassCheckAtmIp;
    }

    public void setBypassCheckAtmIp(String bypassCheckAtmIp) {
        this.bypassCheckAtmIp = bypassCheckAtmIp;
    }

    public boolean isCheckAtmmstrByAtmNo() {
        return checkAtmmstrByAtmNo;
    }

    public void setCheckAtmmstrByAtmNo(boolean checkAtmmstrByAtmNo) {
        this.checkAtmmstrByAtmNo = checkAtmmstrByAtmNo;
    }

    public boolean isForwardTransmissionToFep() {
        return forwardTransmissionToFep;
    }

    public void setForwardTransmissionToFep(boolean forwardTransmissionToFep) {
        this.forwardTransmissionToFep = forwardTransmissionToFep;
    }

    public int getCheckClientFailedTimesLimit() {
        return checkClientFailedTimesLimit;
    }

    public void setCheckClientFailedTimesLimit(int checkClientFailedTimesLimit) {
        this.checkClientFailedTimesLimit = checkClientFailedTimesLimit;
    }

    public int getClearBlackListInterval() {
        return clearBlackListInterval;
    }

    public void setClearBlackListInterval(int clearBlackListInterval) {
        this.clearBlackListInterval = clearBlackListInterval;
    }
}
