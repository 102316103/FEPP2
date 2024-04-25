package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.Protocol;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsProperty;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.invoker.FEPInvoker;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.instance.fisc.receiver.FISCQueueReceiverOperator;
import com.syscom.fep.jms.instance.fisc.receiver.alternative.FISCQueueReceiverAlternativeOperator;
import com.syscom.fep.vo.communication.ToFISCCommu;
import com.syscom.fep.vo.enums.RestfulResultCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本模組目的為傳送電文至財金,並接收財金回應訊息,拆解過後傳回前端
 */
public class FISCAdapter extends AdapterBase {
    /**
     * 從財金回來的電文
     */
    private String messageFromFISC;
    /**
     * 送給財金的電文
     */
    private String messageToFISC;
    /**
     * 財金STAN
     */
    private String stan;
    /**
     * 目前交易財金電文序號
     */
    private String fiscNo;
    /**
     * 目前交易之匯款行
     */
    private String bankNo;
    /**
     * 目前交易之通匯序號
     */
    private String rmNo;

    private MessageBase txData;
    // 當前財金電文交易序號
    private static final AtomicInteger currentFiscNo = new AtomicInteger();
    private static final Map<String, String> currentRmNo = new ConcurrentHashMap<String, String>();
    // private Protocol toFiscProtocol = INBKConfig.getInstance().getToFiscProtocol();
    // private Protocol toRMProtocol = RMConfig.getInstance().getToFiscProtocol();
    // private String toFiscUrl = INBKConfig.getInstance().getToFisc();
    // private String toRMUrl = RMConfig.getInstance().getToFisc();
    // private String toFiscHost = INBKConfig.getInstance().getToFiscHost();
    // private String toRMFiscHost = RMConfig.getInstance().getToFiscHost();
    // private int toFiscPort = INBKConfig.getInstance().getToFiscPort();
    // private int toRMFiscPort = RMConfig.getInstance().getToFiscPort();
    /**
     * 送給財經的電文物件
     */
    private ToFISCCommu request;
    private final FEPInvoker invoker = SpringBeanFactoryUtil.getBean(FEPInvoker.class);
    private static final FISCAdapterConfiguration configuration;
    private static final RefBoolean lock = new RefBoolean(Boolean.FALSE);
    private static final RefBase<FISCGatewayHost> currentFISCGatewayHost = new RefBase<>(null);
    private static final ExecutorService executor = Executors.newCachedThreadPool(new SimpleThreadFactory("FISCAdapter"));

    static {
        configuration = SpringBeanFactoryUtil.registerBean(FISCAdapterConfiguration.class);
        LogHelperFactory.getGeneralLogger().info(configuration.toString());
    }

    public String getMessageFromFISC() {
        return messageFromFISC;
    }

    public void setMessageFromFISC(String messageFromFISC) {
        this.messageFromFISC = messageFromFISC;
    }

    public String getMessageToFISC() {
        return messageToFISC;
    }

    public void setMessageToFISC(String messageToFISC) {
        this.messageToFISC = messageToFISC;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getFiscNo() {
        return fiscNo;
    }

    public void setFiscNo(String fiscNo) {
        this.fiscNo = fiscNo;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getRmNo() {
        return rmNo;
    }

    public void setRmNo(String rmNo) {
        this.rmNo = rmNo;
    }

    public FISCAdapter(MessageBase txData) {
        this.txData = txData;
    }

    @Override
    public FEPReturnCode sendReceive() {
        // 如果之前發送失敗需要切換不同的FISCGatewayHost, 則這裡需要wait一下
        // 避免其他thread此時來讀取currentFISCGatewayHost, lock wait設為fisc timeout秒數
        synchronized (lock) {
            if (lock.get()) {
                long lockTime = (configuration.getTimeout() <= 0 ? this.timeout : configuration.getTimeout()) * 1000L;
                LogHelperFactory.getTraceLogger().warn("******************[", ProgramName, "]Start to wait to unlock in [", lockTime, "] milliseconds...");
                long currentTimeMillis = System.currentTimeMillis();
                try {
                    lock.wait(lockTime);
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
                LogHelperFactory.getTraceLogger().warn("******************[", ProgramName, "]After waiting unlock in [", System.currentTimeMillis() - currentTimeMillis, "] milliseconds, start to sendReceive");
            }
        }
        FEPReturnCode returnCode = null;
        while (true) {
            synchronized (currentFISCGatewayHost) {
                // 將currentFISCGatewayHost輪流方式取下一組
                currentFISCGatewayHost.set(configuration.getFISCGatewayHost(((FISCData) this.txData).getRClientID()));
                // 再傳送直到成功為止
                returnCode = sendReceive(currentFISCGatewayHost.get());
                // 如果連線失敗
                if (returnCode == FEPReturnCode.CanNotConnectRemoteHost) {
                    // Delay 5秒後
                    try {
                        currentFISCGatewayHost.wait(configuration.getDelay() * 1000L);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                } else {
                    break;
                }
            }
            // 連線失敗則lock住
            synchronized (lock) {
                lock.set(Boolean.TRUE);
            }
        }
        // 重置一下, 便於下一次送訊息取FISCGW host
        configuration.reset(((FISCData) this.txData).getRClientID());
        // 一旦連線及傳送成功後再解除lock
        synchronized (lock) {
            lock.set(Boolean.FALSE);
            lock.notifyAll();
        }
        return returnCode;
    }

    private FEPReturnCode sendReceive(FISCGatewayHost host) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        String pcode = this.messageToFISC.substring(14, 16);
        try {
            // Pcode 2XXX走Web Service,其他Queue 送RM
            if ("32".equals(pcode)) {
                this.messageFromFISC = this.sendMsgToFISC(configuration.getProtocol(), configuration.getSvcUrl(), host, this.messageToFISC, this.timeout);
            } else {
                // 2021-06-09 Recommend by Ashiang
                // 另外, 丟queue的流程, 只有永豐才有, java版不會有丟queue, 但未來可能會有ATM與匯款送不同Gateway的可能性,
                // 建議你們可以把丟queue那段流程改成WebService, 只是目前送同一個URL就好
                // this.messageFromFISC = this.sendToQueueAndGetResponse(this.messageToFISC);
                this.messageFromFISC = this.sendMsgToFISC(configuration.getProtocol(), configuration.getSvcUrl(), host, this.messageToFISC, this.timeout);
            }
            return rtnCode;
        } catch (Exception e) {
            if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
                rtnCode = FEPReturnCode.FISCGWATMSendError;
            } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
                rtnCode = FEPReturnCode.FISCTimeout;
            } else if (e instanceof TimeoutException) {
                rtnCode = FEPReturnCode.FISCTimeout;
            }
            // 走socket無法連線FISCGateway
            else if ("io.netty.channel.AbstractChannel$AnnotatedConnectException".equals(e.getClass().getName())) {
                rtnCode = FEPReturnCode.CanNotConnectRemoteHost;
            } else {
                rtnCode = CommonReturnCode.ProgramException;
            }
            this.txData.getLogContext().setProgramException(e);
            this.txData.getLogContext().setReturnCode(rtnCode);
            sendEMS(this.txData.getLogContext());
            return rtnCode;
        }
    }

    /**
     * 初始化財金電文交易序號
     *
     * @param fiscNo
     */
    public static void initialFiscNo(String fiscNo) {
        LogHelperFactory.getTraceLogger().info("before set currentFiscNo, input fiscNo = [", fiscNo, "], currentFiscNo = [", currentFiscNo.get(), "]");
        currentFiscNo.set(Integer.parseInt(fiscNo));
        LogHelperFactory.getTraceLogger().info("after set currentFiscNo, input fiscNo = [", fiscNo, "], currentFiscNo = [", currentFiscNo.get(), "]");
    }

    /**
     * 取得目前成功傳送之最後一筆財金電文序號
     *
     * @return
     */
    public static String getCurrentFiscNo() {
        return StringUtils.leftPad(String.valueOf(currentFiscNo.get()), 7, '0');
    }

    public static String getRMNNoByBank(String bankNo) {
        String rmNo = currentRmNo.get(bankNo);
        return rmNo == null ? StringUtils.EMPTY : rmNo;
    }

    private String sendMsgToFISC(Protocol protocol, String svcUrl, FISCGatewayHost fiscGatewayHost, String reqData, int timeout) throws Exception {
        String pcode = reqData.substring(14, 16);
        // Pcode 2XXX送到ATM Channel,其他送到RM Channel
        // Protocol protocol;
        // String svcUrl;
        // String host;
        // int port;
        // if ("32".equals(pcode)) {
        //     protocol = this.toFiscProtocol;
        //     svcUrl = this.toFiscUrl;
        //     host = this.toFiscHost;
        //     port = this.toFiscPort;
        // } else {
        //     protocol = this.toRMProtocol;
        //     svcUrl = this.toRMUrl;
        //     host = this.toRMFiscHost;
        //     port = this.toRMFiscPort;
        // }
        this.txData.getLogContext().setMessage(reqData);
        this.txData.getLogContext().setStan(this.stan);
        this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
        this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
        this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
        this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " Before Send Message to FISCGateway:", protocol == Protocol.restful ? svcUrl : StringUtils.join(fiscGatewayHost.getHost(), ":", fiscGatewayHost.getPort()), ",Timeout:", this.timeout));
        logMessage(this.txData.getLogContext());
        boolean bAllowSend = false;
        int sendFiscNo = 0;
        // 如果有傳FISCNo則先檢查要送出的FiscNo是否連續,若不連續則等進入迴圈等至Timeout
        if (StringUtils.isNotBlank(this.fiscNo)) {
            sendFiscNo = Integer.parseInt(this.fiscNo);
            Calendar now = Calendar.getInstance();
            LogHelperFactory.getTraceLogger().info("fiscNo:", Integer.parseInt(this.fiscNo), ", currentFiscNo:", currentFiscNo.get());
            while (CalendarUtil.getDiffMilliseconds(Calendar.SECOND, Calendar.getInstance().getTimeInMillis() - now.getTimeInMillis()) < this.timeout) {
                if (sendFiscNo == currentFiscNo.get()) {
                    throw ExceptionUtil.createException("財金電文序號重覆,不允許傳送");
                } else if (sendFiscNo == currentFiscNo.get() + 1) {
                    bAllowSend = true;
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        } else {
            bAllowSend = true; // 沒傳FiscNo直接放行
        }
        if (bAllowSend) {
            this.txData.getLogContext().setMessage(reqData);
            LogHelperFactory.getTraceLogger().trace("FISCAdapter Before SendReceive:", reqData);
            // 送出電文後,不管是用socket或restful,都不用等回應, 所以timeout直接塞入0就好
            invoker.sendReceiveToFISCGW(this.createFISCRestFulRequestOut(protocol, svcUrl, fiscGatewayHost.getHost(), fiscGatewayHost.getPort()), 0);
            if (sendFiscNo > 0) {
                currentFiscNo.set(sendFiscNo);
                LogHelperFactory.getTraceLogger().info("sendFiscNo > 0  fiscNo:", sendFiscNo, ", currentFiscNo:", currentFiscNo.get());
                currentRmNo.put(this.bankNo, this.rmNo);
            }
            // 連至本機MQ等待fiscgw送回Response訊息
            String rtn = this.receiveFromQueue(this.stan, this.timeout);
            this.txData.getLogContext().setMessage(rtn);
            this.txData.getLogContext().setStan(this.stan);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendMsgToFISC"));
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Response);
            this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " Receive Message from Queue"));
            logMessage(this.txData.getLogContext());
            // 收取到訊息後, 再連至連一台AP Server的Resposnse Queue, 收取同一筆correlationId的訊息, Timeout時間設為5秒, 此收取動作請以非同步方式進行,不影響原交易的處理動作
            this.receiveFromQueueAlternative(this.stan, this.timeout);
            if (this.timeout == 0) { // 2021-06-17 Richard add mark, timeout為0表示不需要等待回應
                return StringUtils.EMPTY;
            } else {
                return rtn;
            }
        } else {
            throw ExceptionUtil.createException("財金電文序號不連續,不允許傳送");
        }
    }

    @SuppressWarnings("unused")
    @Deprecated
    private String sendToQueueAndGetResponse(String reqData) {
        throw ExceptionUtil.createUnsupportedOperationException("MS MessageQueue not supported");
    }

    private ToFISCCommu createFISCRestFulRequestOut(Protocol protocol, String svcUrl, String host, int port) {
        request = new ToFISCCommu();
        request.setEj(this.ej);
        request.setMessage(this.messageToFISC);
        request.setMessageId(this.txData.getLogContext().getMessageId());
        request.setStan(this.stan);
        request.setTimeout(this.timeout);
        request.setTxRquid(this.txData.getLogContext().getTxRquid());
        // 以下幾個用於不同的Protocol
        request.setProtocol(protocol);
        request.setRestfulUrl(svcUrl);
        request.setHost(host);
        request.setPort(port);
        return request;
    }

    /**
     * 1. 但若timeout>0代表會有Response, 連至本機MQ等待fiscgw送回Response訊息
     * 2. 收取訊息的correlationId為此筆交易的Stan
     * 3. 等待MQ的時間就是該筆交易的Timeout時間, 若超過時間未收到則引發TimeoutException
     *
     * @param stan
     * @param timeout
     * @return
     * @throws Exception
     */
    private String receiveFromQueue(String stan, int timeout) throws Exception {
        String response = StringUtils.EMPTY;
        // timeout為0表示不需要等待回應
        if (timeout == 0) {
            return response;
        }
        FISCQueueReceiverOperator operator = SpringBeanFactoryUtil.getBean(FISCQueueReceiverOperator.class);
        long receiveTimeout = timeout * 1000L;
        long currentTimeMillis = System.currentTimeMillis();
        String destination = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class).getQueueNames().getFisc().getDestination();
        // 記錄FEPLOG
        this.txData.getLogContext().setStan(stan);
        this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
        this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " wait to receive from Queue, stan:", stan, ",receiveTimeout:", receiveTimeout, ",destination:", destination));
        this.logMessage(this.txData.getLogContext());
        try {
            JmsProperty property = new JmsProperty();
            property.setMessageSelector(JmsFactory.getSelectorForCorrelationID(stan, StandardCharsets.UTF_8.name()));
            property.setReceiveTimeout(receiveTimeout);
            // 注意下面的receiveQueueSelected方法會一直wait到timeout, 但是不會丟異常訊息, 所以這裡要自己判斷
            PlainTextMessage payload = (PlainTextMessage) operator.receiveQueue(destination, property, null);
            if (System.currentTimeMillis() - currentTimeMillis > receiveTimeout) {
                response = StringUtils.EMPTY; // 這裡直接給空字串
                this.txData.getLogContext().setStan(stan);
                this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
                this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " receive from Queue timeout, stan:", stan, ",receiveTimeout:", receiveTimeout, ",destination:", destination));
                logMessage(Level.ERROR, this.txData.getLogContext());
                throw ExceptionUtil.createTimeoutException("Receive Queue timeout, destination:", destination);
            } else {
                response = payload.getPayload();
                this.txData.getLogContext().setStan(stan);
                this.txData.getLogContext().setMessage(response);
                this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
                this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " receive from Queue, stan:", stan, ",receiveTimeout:", receiveTimeout, ",destination:", destination));
                this.logMessage(this.txData.getLogContext());
            }
        } catch (Exception e) {
            this.txData.getLogContext().setStan(stan);
            this.txData.getLogContext().setProgramException(e);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueue"));
            this.txData.getLogContext().setRemark(StringUtils.join("receive from Queue with exception occur, ", e.getMessage()));
            sendEMS(this.txData.getLogContext());
            throw e;
        }
        return response;
    }

    /**
     * 連至連一台AP Server的Resposnse Queue, 收取同一筆correlationId的訊息, Timeout時間設為5秒, 此收取動作請以非同步方式進行,不影響原交易的處理動作
     *
     * @param stan
     * @param timeout
     * @return
     * @throws Exception
     */
    private void receiveFromQueueAlternative(String stan, int timeout) throws Exception {
        // timeout為0表示不需要等待回應
        if (timeout == 0) {
            return;
        }
        // 這裡先將預設的MDC取出來, 下面再put進去, 否則log檔名會跑掉
        final Map<String, String> map = getMDCKeptValue();
        executor.execute(() -> {
            LogMDC.put(map);
            FISCQueueReceiverAlternativeOperator operator = SpringBeanFactoryUtil.getBean(FISCQueueReceiverAlternativeOperator.class);
            long receiveTimeout = configuration.getAlternativeQueueReceiveTimeout();
            long currentTimeMillis = System.currentTimeMillis();
            String destination = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class).getQueueNames().getFisc().getDestination();
            // 記錄FEPLOG
            this.txData.getLogContext().setStan(stan);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
            this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " wait to receive from Queue, stan:", stan, ",receiveTimeout:", receiveTimeout, ",destination:", destination));
            this.logMessage(this.txData.getLogContext());
            try {
                JmsProperty property = new JmsProperty();
                property.setMessageSelector(JmsFactory.getSelectorForCorrelationID(stan, StandardCharsets.UTF_8.name()));
                property.setReceiveTimeout(receiveTimeout);
                // 注意下面的receiveQueueSelected方法會一直wait到timeout, 但是不會丟異常訊息, 所以這裡要自己判斷
                PlainTextMessage payload = (PlainTextMessage) operator.receiveQueue(destination, property, null);
                if (System.currentTimeMillis() - currentTimeMillis > receiveTimeout) {
                    String response = StringUtils.EMPTY; // 這裡直接給空字串
                    this.txData.getLogContext().setStan(stan);
                    this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
                    this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " receive from Queue timeout, stan:", stan, ",receiveTimeout:", receiveTimeout, ",destination:", destination));
                    logMessage(Level.WARN, this.txData.getLogContext());
                } else {
                    String response = payload.getPayload();
                    this.txData.getLogContext().setStan(stan);
                    this.txData.getLogContext().setMessage(response);
                    this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
                    this.txData.getLogContext().setRemark(StringUtils.join(ProgramName, " receive from Queue, stan:", stan, ",receiveTimeout:", receiveTimeout, ",destination:", destination));
                    this.logMessage(this.txData.getLogContext());
                }
            } catch (Exception e) {
                this.txData.getLogContext().setStan(stan);
                this.txData.getLogContext().setProgramException(e);
                this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".receiveFromQueueAlternative"));
                this.txData.getLogContext().setRemark(StringUtils.join("receive from Queue with exception occur, ", e.getMessage()));
                sendEMS(Level.WARN, this.txData.getLogContext());
            }
        });
    }

    public ToFISCCommu getFISCRestFulRequestOut() {
        return request;
    }

    @ConfigurationProperties(prefix = "spring.fep.fisc-adapter")
    @RefreshScope
    public static class FISCAdapterConfiguration {
        @NestedConfigurationProperty
        private List<FISCGatewayHost> primary = new ArrayList<>();
        @NestedConfigurationProperty
        private List<FISCGatewayHost> secondary = new ArrayList<>();
        private int timeout;
        private int delay;
        private long alternativeQueueReceiveTimeout = 5000L;
        private RoundRobin<FISCGatewayHost> roundRobinForPrimary;
        private RoundRobin<FISCGatewayHost> roundRobinForSecondary;
        /**
         * 默認socket
         */
        private Protocol protocol = Protocol.socket;
        /**
         * Just for Use VFISC Testing
         */
        private String svcUrl = "http://10.3.101.4:8913/api/VFISC/SendReceive";
        private final Map<String, RoundRobin<FISCGatewayHost>> clientIdToFISCGatewayHostMap = new HashMap<>();

        @PostConstruct
        public void init() {
            roundRobinForPrimary = new RoundRobin<>(this.primary);
            roundRobinForSecondary = new RoundRobin<>(this.secondary);
            // 下面這兩個順序不能反
            putMap(this.primary);
            putMap(this.secondary);
        }

        private void putMap(List<FISCGatewayHost> list) {
            for (FISCGatewayHost host : list) {
                RoundRobin<FISCGatewayHost> roundRobin = this.clientIdToFISCGatewayHostMap.get(host.getClientId());
                if (roundRobin == null) {
                    roundRobin = new RoundRobin<>();
                }
                roundRobin.add(host);
                this.clientIdToFISCGatewayHostMap.put(host.getClientId(), roundRobin);
            }
        }

        /**
         * 取出FISCGW主機資料, 用於建立連線發送電文
         * <p>
         * 1. 若clientId有值, 則找出對應的Primary host跟port, 送出給FISCGW, 若連不上, Delay  N秒後, 改用clientId所對應的secondary來送
         * 2. 若發送電文為Request或Confirm(clientId無值),  則一律只送Primary那組, 若連不上再送secondary那組
         * 3. 若發送電文為Request或Confirm(RClientID無值), Primary又有多組時, 以Round robin方式2組Primary輪流送
         *
         * @param clientId
         * @return
         */
        public FISCGatewayHost getFISCGatewayHost(String clientId) {
            // 根據Client ID取出對應的host
            if (StringUtils.isNotBlank(clientId)) {
                RoundRobin<FISCGatewayHost> roundRobin = this.clientIdToFISCGatewayHostMap.get(clientId);
                if (roundRobin != null) {
                    FISCGatewayHost host = roundRobin.select();
                    LogHelperFactory.getTraceLogger().info("select next FISCGateway, host = [", host.getHost(), "], port = [", host.getPort(), "] for clientId = [", clientId, "]");
                    return host;
                }
            }
            // 若發送電文為Request或Confirm(RClientID無值), 則一律只送primary那組, 若連不上再送secondary那組
            // 另外, primary又有多組時, 以Round Robin方式2組Primary輪流送
            FISCGatewayHost host = null;
            if (!roundRobinForPrimary.isRoundRobinAll()) { // 如果primary沒有輪詢完, 則優先從primary中取
                host = roundRobinForPrimary.select();
            } else if (!roundRobinForSecondary.isRoundRobinAll()) { // 如果primary輪詢完, 一直是連線失敗, 則再次從secondary中取
                host = roundRobinForSecondary.select();
            } else {
                // 如果primary和secondary都輪詢完, 一直是連線失敗, 則再次從primary中取
                roundRobinForPrimary.reset();
                roundRobinForSecondary.reset();
                return this.getFISCGatewayHost(clientId);
            }
            LogHelperFactory.getTraceLogger().info("select next FISCGateway, host = [", host.getHost(), "], port = [", host.getPort(), "]");
            return host;
        }

        /**
         * 在建立連線成功後, 需要重置RoundRobin輪詢, 用於下一次取數據
         * <p>
         * 注意, 這個方法只能在上一次取出數據並且建立連線發送電文成功後呼叫
         *
         * @param clientId
         */
        public void reset(String clientId) {
            // 根據Client ID取出對應的host
            if (StringUtils.isNotBlank(clientId)) {
                RoundRobin<FISCGatewayHost> roundRobin = this.clientIdToFISCGatewayHostMap.get(clientId);
                if (roundRobin != null) {
                    roundRobin.reset(); // 每次都是從primary開始取
                    return;
                }
            }
            if (roundRobinForPrimary.isRoundRobinAll()) { // 如果primary輪詢完, 則重置primary, 下一次優先從primary中取
                roundRobinForPrimary.reset();
            }
            roundRobinForSecondary.reset(); // secondary每次都是從頭開始輪詢
        }

        public List<FISCGatewayHost> getPrimary() {
            return primary;
        }

        public List<FISCGatewayHost> getSecondary() {
            return secondary;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public long getAlternativeQueueReceiveTimeout() {
            return alternativeQueueReceiveTimeout;
        }

        public void setAlternativeQueueReceiveTimeout(long alternativeQueueReceiveTimeout) {
            this.alternativeQueueReceiveTimeout = alternativeQueueReceiveTimeout;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public void setProtocol(Protocol protocol) {
            this.protocol = protocol;
        }

        public String getSvcUrl() {
            return svcUrl;
        }

        public void setSvcUrl(String svcUrl) {
            this.svcUrl = svcUrl;
        }

        @Override
        public String toString() {
            return ConfigurationPropertiesUtil.info(this, "FISC Adapter Configuration",
                    "roundRobinForPrimary", "roundRobinForSecondary", "protocol", "svcUrl", "clientIdToFISCGatewayHostMap");
        }
    }

    private static class FISCGatewayHost {
        private String clientId;
        private String host;
        private int port;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

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
    }
}
