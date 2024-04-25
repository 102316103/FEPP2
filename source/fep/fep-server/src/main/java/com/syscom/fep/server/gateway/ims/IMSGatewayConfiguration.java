package com.syscom.fep.server.gateway.ims;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * IMS Gateway配置檔對應的Spring物件
 * <p>
 * 配置檔名為application-server-imsgw.properties
 *
 * @author Richard & Ashiang
 */
@ConfigurationProperties(prefix = "spring.fep.server.gateway.ims")
@RefreshScope
public class IMSGatewayConfiguration {
    /**
     * 出現異常後需要重新建立連線前先wait毫秒數, 避免一直失敗立刻就重連太頻繁
     */
    private long sleepForRebuildConnectionInMilliseconds = 100L;
    /**
     * 在terminate時, 如果有業務正在處理中, 是否等待業務處理完畢, 再終止線程
     */
    private boolean waitTransactionExecutedFinishedBeforeTerminate = true;
    /**
     * 在terminate時, 如果有業務正在處理中, 等待業務處理完畢的最長時間, 避免等待的時間過長
     */
    private long waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds = 10000L;
    /**
     * IMS Client Id
     */
    private String clientId;
    /**
     * IMS的RSM訊息,非交易訊息
     */
    private String messagePrefixRSM = "*REQSTS*";
    /**
     * 模擬收送訊息, only for Test
     */
    private boolean simulatorReceiveAndSend = false;
    /**
     * sender
     */
    @NestedConfigurationProperty()
    private List<IMSGatewayProcessorConfiguration> sender = new ArrayList<IMSGatewayProcessorConfiguration>();
    /**
     * receiver
     */
    @NestedConfigurationProperty()
    private List<IMSGatewayProcessorConfiguration> receiver = new ArrayList<IMSGatewayProcessorConfiguration>();

    public long getSleepForRebuildConnectionInMilliseconds() {
        return sleepForRebuildConnectionInMilliseconds;
    }

    public void setSleepForRebuildConnectionInMilliseconds(long sleepForRebuildConnectionInMilliseconds) {
        this.sleepForRebuildConnectionInMilliseconds = sleepForRebuildConnectionInMilliseconds;
    }

    public boolean isWaitTransactionExecutedFinishedBeforeTerminate() {
        return waitTransactionExecutedFinishedBeforeTerminate;
    }

    public void setWaitTransactionExecutedFinishedBeforeTerminate(boolean waitTransactionExecutedFinishedBeforeTerminate) {
        this.waitTransactionExecutedFinishedBeforeTerminate = waitTransactionExecutedFinishedBeforeTerminate;
    }

    public long getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds() {
        return waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds;
    }

    public void setWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds(long waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds) {
        this.waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds = waitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMessagePrefixRSM() {
        return messagePrefixRSM;
    }

    public void setMessagePrefixRSM(String messagePrefixRSM) {
        this.messagePrefixRSM = messagePrefixRSM;
    }

    public boolean isSimulatorReceiveAndSend() {
        return simulatorReceiveAndSend;
    }

    public void setSimulatorReceiveAndSend(boolean simulatorReceiveAndSend) {
        this.simulatorReceiveAndSend = simulatorReceiveAndSend;
    }

    public List<IMSGatewayProcessorConfiguration> getReceiver() {
        return receiver;
    }

    public List<IMSGatewayProcessorConfiguration> getSender() {
        return sender;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "IMS Gateway Configuration"));
    }
}
