package com.syscom.fep.server.gateway.ims;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * IMS Gateway主程式
 * <p>
 * 2024-04-24 Richard modified 改成Sender&Receiver非同步方式
 *
 * @author Richard & Ashiang
 */
@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGateway extends FEPBase {
    @Autowired
    private IMSGatewayConfiguration configuration;
    private final List<IMSGatewayProcessorGroup> processorGroups = new ArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(new SimpleThreadFactory(SvrConst.SVR_IMS_GATEWAY));

    @PostConstruct
    public void initialize() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".initialize"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Enter Initialize"));
        this.logMessage(this.logContext);
        // String hostName = CMNConfig.getInstance().getIMSHostName(); // "localhost"
        // int hostPort = CMNConfig.getInstance().getIMSPort();
        // String clientIds = configuration.getClientId();
        int timeout = CMNConfig.getInstance().getCBSTimeout() * 1000;
        // String dsName = CMNConfig.getInstance().getIMSDatastoreName();
        String tranCode = StringUtils.EMPTY; // 給空白即可 by Ashiang 2024/01/29
        // List<String> clientIdList = this.parseClientIds(this.logContext, clientIds);
        // 一個clientId對應建一個Thread, 注意下面new的時候, Tread就會自動運行
        // for (String clientId : clientIdList) {
        //     processors.add(new IMSGatewayProcessor(configuration, hostName, hostPort, clientId, dsName, tranCode, timeout));
        // }
        // 2024-04-24 Richard modified 改成Sender&Receiver非同步方式
        for (int i = 0; i < configuration.getSender().size(); i++) {
            IMSGatewayProcessorConfiguration senderConfiguration = configuration.getSender().get(i);
            IMSGatewayProcessorConfiguration receiverConfiguration = configuration.getReceiver().get(i);
            IMSGatewayProcessor senderProcessor =
                    new IMSGatewayProcessor(configuration, IMSGatewayProcessorType.SENDER,
                            senderConfiguration.getHost(),
                            senderConfiguration.getPort(),
                            senderConfiguration.getClientId(),
                            senderConfiguration.getDataStore(),
                            tranCode,
                            senderConfiguration.getResumeInterval(),
                            senderConfiguration.getReestablishConnectionInterval(),
                            null,
                            executor);
            IMSGatewayProcessor receiverProcessor =
                    new IMSGatewayProcessor(configuration, IMSGatewayProcessorType.RECEIVER,
                            receiverConfiguration.getHost(),
                            receiverConfiguration.getPort(),
                            receiverConfiguration.getClientId(),
                            receiverConfiguration.getDataStore(),
                            tranCode,
                            receiverConfiguration.getResumeInterval(),
                            receiverConfiguration.getReestablishConnectionInterval(),
                            senderProcessor,
                            executor);
            processorGroups.add(new IMSGatewayProcessorGroup(senderProcessor, receiverProcessor));
        }
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".initialize"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Exit Initialize"));
        this.logMessage(this.logContext);
    }

    @PreDestroy
    public void terminate() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Enter Terminate"));
        this.logMessage(this.logContext);
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                LogHelperFactory.getTraceLogger().trace(ProgramName, " executor terminate all runnable successful");
            else
                LogHelperFactory.getTraceLogger().trace(ProgramName, " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
        }
        for (IMSGatewayProcessorGroup processorGroup : processorGroups) {
            processorGroup.terminate();
            processorGroup = null;
        }
        this.logContext.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        this.logContext.setRemark(StringUtils.join(ProgramName, " Exit Terminate"));
        this.logMessage(this.logContext);
    }

    /**
     * 解讀ClientId
     *
     * @param logData
     * @param clientIds
     * @return
     */
    private List<String> parseClientIds(LogData logData, String clientIds) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        logData.setProgramName(StringUtils.join(ProgramName, ".parseClientIds"));
        logData.setRemark(StringUtils.join("Start to parse clientIds:", clientIds));
        this.logMessage(logData);
        List<String> list = new ArrayList<>();
        String[] groups = StringUtils.split(clientIds, ';');
        for (String group : groups) {
            try {
                String[] parts = StringUtils.split(group, ',');
                if (parts.length >= 3) {
                    String pattern = parts[0];
                    int start = Integer.parseInt(parts[1]);
                    int end = Integer.parseInt(parts[2]);
                    for (int value = start; value <= end; value++) {
                        list.add(MessageFormat.format(pattern, StringUtils.leftPad(Integer.toString(value), 2, '0')));
                    }
                }
            } catch (Exception e) {
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".parseClientIds"));
                logData.setRemark(StringUtils.join("Parse clientId = [", group, "] with exception occur"));
                sendEMS(logData);
            }
        }
        logData.setProgramName(StringUtils.join(ProgramName, ".parseClientIds"));
        logData.setRemark(StringUtils.join("Parse clientIds succeed, client Id List = [", StringUtils.join(list, ','), "]"));
        this.logMessage(logData);
        return list;
    }
}
