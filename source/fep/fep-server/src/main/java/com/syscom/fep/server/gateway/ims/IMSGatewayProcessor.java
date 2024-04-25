package com.syscom.fep.server.gateway.ims;

import com.ibm.ims.connect.*;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.server.common.handler.CBSHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.net.SocketException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 每個IMS Gateway Thread實例
 * <p>
 * 針對clientIds每一個clientId, 分別建立不同的Thread
 *
 * @author Richard & Ashiang
 */
@StackTracePointCut(caller = SvrConst.SVR_IMS_GATEWAY)
public class IMSGatewayProcessor extends Thread {
    private static final String ProgramName = IMSGatewayProcessor.class.getSimpleName();
    private final IMSGatewayConfiguration configuration;
    private final String hostName;
    private final int hostPort;
    private final String clientId;
    private final String dsName;
    private final String tranCode;
    private final int _timeout;
    private final long reestablishConnectionInterval;
    private boolean running = true;
    private final LogData logData = new LogData();
    private Connection connection = null;
    private final RefBoolean waitProceed = new RefBoolean(Boolean.FALSE);
    private static final ApiLoggingConfiguration apiLoggingConfig = new ApiLoggingConfiguration();
    private final IMSGatewayProcessorType type;
    private final IMSGatewayProcessor sender;
    private final List<String> messageToIMSList = Collections.synchronizedList(new ArrayList<>());
    private final Executor executor;

    public IMSGatewayProcessor(IMSGatewayConfiguration configuration, IMSGatewayProcessorType type, String hostName, int hostPort, String clientId, String dsName, String tranCode, int timeout, long reestablishConnectionInterval, IMSGatewayProcessor sender, Executor executor) {
        super(StringUtils.join("IMSGateway-", type.name(), "-", clientId));
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        logData.setProgramName(StringUtils.join(ProgramName, ".Constructor()"));
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " start to run..."));
        FEPBase.logMessage(Level.INFO, logData);
        this.configuration = configuration;
        this.hostName = hostName;
        this.hostPort = hostPort;
        this.clientId = clientId;
        this.dsName = dsName;
        this.tranCode = tranCode;
        this._timeout = timeout;
        this.reestablishConnectionInterval = reestablishConnectionInterval;
        this.type = type;
        this.sender = sender;
        this.executor = executor;
        try {
            String logPath = "/fep/logs/";
            String apName = "fep-server-imsgw";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String apiLog = Paths.get(logPath, LocalDate.now().format(formatter), apName, "IMSConnect.log").toString();
            apiLoggingConfig.configureApiLogging(apiLog, ApiProperties.TRACE_LEVEL_INTERNAL);
        } catch (ImsConnectApiException e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
        }
        this.start();
    }

    public void terminate() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " start to stopping..."));
        FEPBase.logMessage(Level.INFO, logData);
        this.running = false;
        // messageToIMSList要notifyAll一下
        synchronized (this.messageToIMSList) {
            this.messageToIMSList.notifyAll();
        }
        // 如果有業務正在處理中, 等待業務處理完畢, 再終止線程
        if (configuration.isWaitTransactionExecutedFinishedBeforeTerminate()) {
            synchronized (waitProceed) {
                // 有業務正在處理中則等待
                if (waitProceed.get()) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
                    logData.setRemark(StringUtils.join("Thread ", this.getName(), " Wait Transaction executed finished",
                            configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds() > 0 ?
                                    StringUtils.join(" in ", configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds(), " milliseconds at the most") : StringUtils.EMPTY,
                            "..."));
                    FEPBase.logMessage(Level.INFO, logData);
                    try {
                        // 設置一個超時時間, 避免出現等待時間過長的情況
                        if (configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds() > 0) {
                            waitProceed.wait(configuration.getWaitTransactionExecutedFinishedAtMostBeforeTerminateInMilliseconds());
                        } else {
                            waitProceed.wait();
                        }
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                }
            }
        }
        this.closeConnection(connection);
        logData.setProgramName(StringUtils.join(ProgramName, ".terminate"));
        logData.setRemark(StringUtils.join("Thread ", this.getName(), " has stopped"));
        FEPBase.logMessage(Level.INFO, logData);
    }

    @Override
    public void run() {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        final String messageForParameter = StringUtils.join("hostName:", hostName, ",hostPort:", hostPort, ",clientId:", clientId, ",dsName:", dsName, ",tranCode:", tranCode);
        ConnectionFactory connectionFactory = null;
        TmInteraction interaction = null;

        boolean first = true; // 是否首次進入while
        while (running) {
            this.setWaitProceedState(Boolean.FALSE, false);
            logData.setStep(0); // step從0開始重新記錄
            this.closeConnection(connection); // 每次先把上一次的Connection關閉
            if (!first && this.reestablishConnectionInterval > 0) {
                logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                logData.setRemark(StringUtils.join("Sleep ", this.reestablishConnectionInterval, " milliseconds before rebuild IMS Connection, ", messageForParameter));
                FEPBase.logMessage(Level.INFO, logData);
                try {
                    sleep(this.reestablishConnectionInterval);
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
            first = false;
            // (1) 	建立IMS ConnectionFactory物件
            try {
                connectionFactory = buildConnectionFactory(this.hostName, this.hostPort, this.clientId);
                logData.setProgramName(StringUtils.join(ProgramName, ".buildConnectionFactory"));
                logData.setRemark(StringUtils.join("Build IMS Connection Factory Succeed, ", messageForParameter));
                FEPBase.logMessage(Level.INFO, logData);
            } catch (Exception e) {
                if (running) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".buildConnectionFactory"));
                    logData.setRemark(StringUtils.join("Build IMS Connection Factory Failed, ", messageForParameter));
                    logData.setProgramException(e);
                    FEPBase.sendEMS(logData);
                } else {
                    logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    logData.setRemark(StringUtils.join("Build IMS Connection Factory Failed cause Terminate, ", messageForParameter));
                    FEPBase.logMessage(Level.WARN, logData);
                }
                continue; // build失敗, 則從(1)開始嘗試
            }
            // (2) 	建立IMS Connection物件
            try {
                connection = buildConnection(connectionFactory);
                logData.setProgramName(StringUtils.join(ProgramName, ".buildConnection"));
                logData.setRemark(StringUtils.join("Build IMS Connection Succeed, ", messageForParameter));
                FEPBase.logMessage(Level.INFO, logData);
            } catch (Exception e) {
                if (running) {
                    logData.setProgramName(StringUtils.join(ProgramName, ".buildConnection"));
                    logData.setRemark(StringUtils.join("Build IMS Connection Failed, ", messageForParameter));
                    logData.setProgramException(e);
                    FEPBase.sendEMS(logData);
                } else {
                    logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                    logData.setRemark(StringUtils.join("Build IMS Connection Failed cause Terminate, ", messageForParameter));
                    FEPBase.logMessage(Level.WARN, logData);
                }
                continue; // build失敗, 則從(1)開始嘗試
            }
            while (running) {
                if (this.type == IMSGatewayProcessorType.RECEIVER) {
                    // 失敗, 則從(1)開始嘗試
                    if (!doReceiverProcess(messageForParameter))
                        break;
                } else if (this.type == IMSGatewayProcessorType.SENDER) {
                    // 失敗, 則從(1)開始嘗試
                    if (!doSenderProcess(messageForParameter))
                        break;
                }
            }
        }
        this.setWaitProceedState(Boolean.FALSE, true);
    }

    /**
     * 接收電文
     *
     * @param messageForParameter
     * @return
     */
    private boolean doReceiverProcess(String messageForParameter) {
        this.setWaitProceedState(Boolean.FALSE, false);
        // (3) 	建立IMS Interaction物件
        // try {
        //     interaction = buildInteraction(connection, clientId, dsName, tranCode);
        //     logData.setProgramName(StringUtils.join(ProgramName, ".buildInteraction"));
        //     logData.setRemark(StringUtils.join("Build IMS TmInteraction Succeed, ", messageForParameter));
        //     FEPBase.logMessage(Level.INFO, logData);
        // } catch (ImsConnectApiException e) {
        //     if (running) {
        //         logData.setProgramName(StringUtils.join(ProgramName, ".buildInteraction"));
        //         logData.setRemark(StringUtils.join("Build IMS TmInteraction Failed, ", messageForParameter));
        //         logData.setProgramException(e);
        //         FEPBase.sendEMS(logData);
        //     } else {
        //         logData.setProgramName(StringUtils.join(ProgramName, ".run"));
        //         logData.setRemark(StringUtils.join("Build IMS TmInteraction Failed cause Terminate, ", messageForParameter));
        //         FEPBase.logMessage(Level.WARN, logData);
        //     }
        //     break;  // build失敗, 則從(1)開始嘗試
        // }
        // 模擬收送訊息, only for Test start
        // FEPReturnCode rtnCode = this.simulatorReceiveAndSend(interaction, messageForParameter);
        // if (rtnCode == FEPReturnCode.Normal) {
        //     continue;
        // } else if (rtnCode == FEPReturnCode.ProgramException) {
        //     break;
        // }
        // 模擬收送訊息, only for Test end
        try {
            // logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            // logData.setRemark(StringUtils.join("IMS TmInteraction start to Execute, ", messageForParameter));
            // FEPBase.logMessage(Level.INFO, logData);
            // (4) 	開始等待接收資料
            resumeClearTpipe(connection, clientId, ApiProperties.RETRIEVE_SYNC_OR_ASYNC_MESSAGE);
            //interaction.execute();
        } catch (Exception e) {
            if (running) {
                logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                logData.setRemark(StringUtils.join("IMS TmInteraction Execute Failed, ", messageForParameter));
                logData.setProgramException(e);
                FEPBase.sendEMS(logData);
            } else {
                logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                logData.setRemark(StringUtils.join("IMS TmInteraction Execute Failed cause Terminate, ", messageForParameter));
                FEPBase.logMessage(Level.WARN, logData);
            }
            return false;  // 失敗, 則從(1)開始嘗試
        }
        this.setWaitProceedState(Boolean.TRUE, false);
        // try {
        //     // (5) 	取得接收資料
        //     outMsg = interaction.getOutputMessage();
        //     req = outMsg.getDataAsString();
        //     LogHelperFactory.getTraceLogger().info(Const.MESSAGE_IN, req);
        //     // (6) 	記錄FEPLOG內容
        //     logData.setProgramFlowType(ProgramFlow.IMSGWIn);
        //     logData.setMessageFlowType(MessageFlow.Request);
        //     logData.setProgramName(StringUtils.join(ProgramName, ".run"));
        //     logData.setMessage(req);
        //     logData.setRemark(StringUtils.join("IMS TmInteraction GetOutputMessage Succeed, ", messageForParameter));
        //     // 如果有空白電文, 忽略
        //     if (StringUtils.isBlank(req)) {
        //         logData.setRemark(StringUtils.join("IMS TmInteraction GetOutputMessage Succeed, Ignore Empty String, ", messageForParameter));
        //         FEPBase.logMessage(Level.WARN, logData);
        //         continue;
        //     }
        //     // 21024-02-19 Richard add for *REQSTS*這個訊息代表是IMS的RSM訊息,非交易訊息, LOG後請直接忽略不用往後傳送 by Ashiang
        //     else if (req.startsWith(configuration.getMessagePrefixRSM())) {
        //         logData.setRemark(StringUtils.join("IMS TmInteraction GetOutputMessage Succeed, Ignore \"RSM\" Message", messageForParameter));
        //         FEPBase.logMessage(Level.WARN, logData);
        //         continue;
        //     }
        //     FEPBase.logMessage(Level.INFO, logData);
        // } catch (ImsConnectApiException e) {
        //     logData.setProgramException(e);
        //     logData.setProgramName(StringUtils.join(ProgramName, ".run"));
        //     logData.setRemark(StringUtils.join("IMS TmInteraction GetOutputMessage Failed, ", messageForParameter));
        //     FEPBase.sendEMS(logData);
        //     break;  // 失敗, 則從(1)開始嘗試
        // }
        // (7) 	CALL  CBSHandler
        return true;
    }

    /**
     * 傳送電文
     *
     * @param messageForParameter
     * @return
     */
    private boolean doSenderProcess(String messageForParameter) {
        this.setWaitProceedState(Boolean.FALSE, false);
        if (this.messageToIMSList.isEmpty()) {
            synchronized (this.messageToIMSList) {
                try {
                    this.messageToIMSList.wait(600000);
                } catch (InterruptedException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                }
            }
        }
        String[] messages = new String[this.messageToIMSList.size()];
        this.messageToIMSList.toArray(messages);
        this.messageToIMSList.clear();
        if (ArrayUtils.isNotEmpty(messages)) {
            logData.setProgramName(StringUtils.join(ProgramName, ".doSenderProcess"));
            logData.setRemark(StringUtils.join("Start to SendToIMS, message count = [", messages.length, "]", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            boolean success = true;
            for (String message : messages) {
                try {
                    this.SendToIMS(connection, message);
                } catch (Exception e) {
                    logData.setProgramException(e);
                    logData.setMessage(message);
                    logData.setProgramName(StringUtils.join(ProgramName, ".doSenderProcess"));
                    logData.setRemark(StringUtils.join("SendToIMS Failed"));
                    FEPBase.sendEMS(logData);
                    success = false;
                }
            }
            return success;
        }
        return true;
    }

    /**
     * 將要回應給IMS的電文放入列表中
     *
     * @param message
     */
    private void addMessageToIMS(String message) {
        this.messageToIMSList.add(message);
        if (this.messageToIMSList.size() == 1) {
            synchronized (this.messageToIMSList) {
                this.messageToIMSList.notifyAll();
            }
        }
    }

    private void setWaitProceedState(final boolean wait, final boolean notifyAll) {
        if (configuration.isWaitTransactionExecutedFinishedBeforeTerminate()) {
            synchronized (waitProceed) {
                waitProceed.set(wait);
                if (notifyAll) {
                    waitProceed.notifyAll();
                }
            }
        }
    }

    /**
     * (1) 建立IMS ConnectionFactory物件
     * <p>
     * 以_hostName, _hostPort, _clientId變數值建立ConnectionFactory物件
     *
     * @param hostName
     * @param portNumber
     * @param clientId
     * @return
     * @throws ImsConnectApiException
     */
    private ConnectionFactory buildConnectionFactory(String hostName, int portNumber, String clientId) throws ImsConnectApiException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHostName(hostName);
        connectionFactory.setPortNumber(portNumber);
        connectionFactory.setClientId(clientId);
        return connectionFactory;
    }

    /**
     * (2) 建立IMS Connection物件
     * <p>
     * 分別設定連線逾時及傳輸逾時屬性
     *
     * @param connectionFactory
     * @return
     * @throws ImsConnectApiException
     * @throws SocketException
     */
    private Connection buildConnection(ConnectionFactory connectionFactory) throws ImsConnectApiException, SocketException {
        Connection connection = connectionFactory.getConnection();
        connection.setSocketType(ApiProperties.SOCKET_TYPE_PERSISTENT); //長連接
        connection.setSocketConnectTimeout(5000);  //連線逾時,單位毫秒
        connection.connect();
        return connection;
    }

    /**
     * (3) 建立IMS Interaction物件
     *
     * @param connection
     * @param ltermOverrideName
     * @param imsDatastoreName
     * @param tranCode
     * @return
     * @throws ImsConnectApiException
     */
    // private TmInteraction buildInteraction(Connection connection, String ltermOverrideName, String imsDatastoreName, String tranCode) throws ImsConnectApiException {
    //     TmInteraction interaction = connection.createInteraction();
    //     interaction.setLtermOverrideName(ltermOverrideName);
    //     interaction.setImsDatastoreName(imsDatastoreName);
    //     interaction.setTrancode(tranCode);
    //     interaction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE);
    //     interaction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_SINGLE_WAIT);
    //     interaction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_MESSAGE_ONLY);  //接收訊息模式
    //     return interaction;
    // }

    /**
     * 關閉連線
     *
     * @param connection
     */
    private void closeConnection(Connection connection) {
        if (connection != null && connection.isConnected()) {
            try {
                connection.disconnect();
                logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                logData.setRemark(StringUtils.join("Close IMS Connection Succeed"));
                FEPBase.logMessage(Level.INFO, logData);
            } catch (ImsConnectApiException e) {
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".closeConnection"));
                logData.setRemark(StringUtils.join("Close IMS Connection Failed"));
                FEPBase.sendEMS(logData);
            }
        }
    }

    private void InvokeAAProcess(Connection conn, String req) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_IMS_GATEWAY);
        this.setWaitProceedState(Boolean.TRUE, false);

        try {
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".InvokeAAProcess"));
            logData.setMessage(req);
            logData.setRemark("Begin InvokeAAProcess");
            FEPBase.logMessage(Level.INFO, logData);

            CBSHandler handler = new CBSHandler();
            String rtnData = handler.dispatch(FEPChannel.CBS, req);
            // (8) 	記錄FEPLOG內容
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".InvokeAAProcess"));
            logData.setMessage(rtnData);
            logData.setRemark(StringUtils.join("Get Response from CBSHandler Succeed "));
            FEPBase.logMessage(Level.INFO, logData);
            if (StringUtils.isNotBlank(rtnData)) {
                // 2024-04-24 Richard modified
                // SendToIMS(conn, rtnData);
                // 透過sender回應給IMS
                sender.addMessageToIMS(rtnData);
            }
            //LogHelperFactory.getTraceLogger().info(Const.MESSAGE_OUT, rtnData);
            //模擬測試原電文丟回去試看看
            //SendToIMS(conn, req);

        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".InvokeAAProcess"));
            logData.setRemark(StringUtils.join("SendToIMS Failed"));
            FEPBase.sendEMS(logData);
            //break;  // 失敗, 則從(1)開始嘗試
        }
    }

    private void SendToIMS(Connection myConn, String inputData) throws Exception {
        this.setWaitProceedState(Boolean.TRUE, false);
        TmInteraction myTmInteraction;

        myTmInteraction = myConn.createInteraction();

        //SENDONLY
        myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_SENDONLY);
        myTmInteraction.setLtermOverrideName(this.clientId);
        myTmInteraction.setImsDatastoreName(this.dsName);
        myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_5_SECONDS);
        myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_10_SECONDS);
        // logger.debug(ApiProperties.INTERACTION_TYPE_DESC_SENDONLY + " set Timeout as "
        // 		+ myTmInteraction.getImsConnectTimeout() + "ms");
        myTmInteraction.setCommitMode(ApiProperties.COMMIT_MODE_0);
        myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);
        myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_NOT_INCLUDE_LLZZ_AND_TRANCODE);
        String tCode = "";
        if (StringUtils.isNotBlank(inputData)) {
            String pCode = StringUtils.substring(inputData, 14, 22);
            pCode = EbcdicConverter.fromHex(CCSID.English, pCode);
            tCode = this.getTransCode(pCode) + " ";
            //Trancode = StringUtils.rightPad(tCode, 8, " ");
            //財金電文前面3個byte改為TranCode + 空白
            inputData = EbcdicConverter.toHex(CCSID.English, tCode.length(), tCode) + inputData.substring(6);
        }

        myTmInteraction.setTrancode("");
        // get InputMessage instance from myTMInteraction
        InputMessage inMsg = myTmInteraction.getInputMessage();
        // populate input message data with indata byte array
        byte[] inputData_ = hexToBytes(inputData);

        inMsg.setInputMessageData(inputData_);

        // byte[] _inputData = inMsg.getDataAsByteArray();

        // String fileContent = "Hex=";
        // for (int i = 0; i < _inputData.length; i++) {
        //     fileContent += String.format("%02x", _inputData[i]);
        // }

        logData.setMessage(inputData);
        logData.setRemark("before Send data to IMS (Trancode:" + tCode + ")");
        FEPBase.logMessage(Level.INFO, logData);

        // execute the transaction
        myTmInteraction.execute();

        // get output from myTMInteraction
        OutputMessage outMsg = myTmInteraction.getOutputMessage();
        // get data from outMsg as a string
        String outData = outMsg.getDataAsString();

        logData.setProgramName(StringUtils.join(ProgramName, ".SendToIMS"));
        logData.setRemark("Send data to IMS Succeed, return code = " +
                myTmInteraction.getOutputMessage().getImsConnectReturnCode());
        logData.setMessage(outData);
        FEPBase.logMessage(Level.INFO, logData);

    }

    private void resumeClearTpipe(Connection myConn, String clientId, byte _retrieveType) throws Exception {

        try {
            logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            logData.setRemark("begin resumeClearTpipe -" + clientId);
            FEPBase.logMessage(Level.INFO, logData);

            TmInteraction myTmInteraction = myConn.createInteraction();
            myTmInteraction.setImsDatastoreName(dsName);// Change "MYDATASTORENAME" to your corresponding datastore
            myTmInteraction.setLtermOverrideName(clientId);

            // myTmInteractionAttr.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_NOT_INCLUDE_LLZZ_AND_TRANCODE);
            myTmInteraction.setTrancode("");
            //myTmInteraction.setTrancode(tranCode);            
            myTmInteraction.setCommitMode(ApiProperties.COMMIT_MODE_0);
            myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);
            //40 IRM_TIMEOUT continue loop 
            //do {
            String result = resumeTPipe(myTmInteraction, clientId, tranCode, ApiProperties.RESUME_TPIPE_NOAUTO, _retrieveType);

            if (myTmInteraction.isAckNakNeeded()) {
                myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_1_SECOND);
                String intType;
                if ((myTmInteraction.getOutputMessage()
                        .getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS)) {
                    intType = ApiProperties.INTERACTION_TYPE_DESC_ACK;
                } else {
                    intType = ApiProperties.INTERACTION_TYPE_DESC_NAK;
                }
                myTmInteraction.setInteractionTypeDescription(intType);
                // Execute interaction
                myTmInteraction.execute();
                //OutputMessage outMsg = myTmInteraction.getOutputMessage();
            }

            //檢查收到內容是否符合下送格式   
            String req = checkData(result);
            if (StringUtils.isNotBlank(req)) {
                // 2024-04-24 Richard modified 以非同步方式呼叫InvokeAAProcess
                this.executor.execute(() -> {
                    InvokeAAProcess(myConn, req);
                });
            }

            logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            logData.setRemark("exit resumeClearTpipe:" + clientId);
            FEPBase.logMessage(Level.INFO, logData);
        } catch (ImsConnectApiException e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            logData.setRemark(e.getMessage());
            FEPBase.sendEMS(logData);
            throw e;

        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            logData.setRemark(e.getMessage());
            FEPBase.sendEMS(logData);
            throw e;
        }

    }

    private String resumeTPipe(TmInteraction myTmInteraction, String _ovn, String _trancode, int _resumeType, byte _retrieveType)
            throws Exception {
        logData.setProgramName(StringUtils.join(ProgramName, ".resumeTPipe"));
        logData.setRemark(StringUtils.join("Begin resumeTPipe"));
        FEPBase.logMessage(Level.INFO, logData);

        byte[] emptyByteArray = {};
        myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(
                ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_INCLUDE_LLZZ_AND_TRANCODE);
        myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE);
        //當connectionTimeout > interactionTimeout時, 會引發receive Timeout例外
        //若未超過, 則到connectionTimeout時間未收到資料會getImsConnectReturnCode=40的rc
        //myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_1_MINUTE);
        //myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_1_MINUTE + ApiProperties.TIMEOUT_5_SECONDS);
        myTmInteraction.setImsConnectTimeout(this._timeout);
        myTmInteraction.setInteractionTimeout(this._timeout + ApiProperties.TIMEOUT_5_SECONDS);//直到收到資料再往下
        myTmInteraction.setResumeTpipeAlternateClientId(_ovn);
        myTmInteraction.setTrancode("");
        myTmInteraction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_SINGLE_WAIT);
        myTmInteraction.setResumeTpipeRetrievalType(_retrieveType);

        myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);

        InputMessage inMsg = (InputMessage) myTmInteraction.getInputMessage();
        inMsg.setInputMessageData(emptyByteArray);
        myTmInteraction.execute();
        // if (myTmInteraction.isAckNakNeeded()) {
        //     myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_ACK);
        //     // Execute interaction
        //     myTmInteraction.execute();
        // }
        OutputMessage outMsg = myTmInteraction.getOutputMessage();
        String result = StringUtil.toHex(outMsg.getDataAsByteArray());

        logData.setProgramName(StringUtils.join(ProgramName, ".resumeTPipe"));
        logData.setMessage(result);
        logData.setRemark(StringUtils.join("resumeTPipe get outData,getImsConnectReturnCode():",
                myTmInteraction.getOutputMessage().getImsConnectReturnCode()));
        FEPBase.logMessage(Level.INFO, logData);
        return result;

    }

    private String checkData(String result) {
        int csmPos = result.indexOf("5CC3E2D4D6D2E85C");
        //LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] csmPos: " + csmPos);
        int dfs2082 = result.indexOf("C4C6E2");
        //LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] dfs2082: " + dfs2082);
        // System.out.println("Have *CSMOKY*:"+csmPos);
        // this.setMessageFromIMS(result);
        if (dfs2082 >= 0) {
            //LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] dfs2082 >= 0 return null ");
            return "";
        } else {
            if (csmPos >= 0) {
                // LogHelperFactory.getGeneralLogger()
                //         .info(" [MethodName:getOutputString] csmPos >= 0 return result.substring(0, csmPos) end ");
                return result.substring(0, csmPos);
            } else {
                //LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString] csmPos < 0 return null ");
                return "";
            }
        }
    }

    /**
     * 模擬收送訊息, only for Test
     */
    private FEPReturnCode simulatorReceiveAndSend(TmInteraction interaction, String messageForParameter) {
        // 模擬收送訊息
        if (configuration.isSimulatorReceiveAndSend()) {
            logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            logData.setRemark(StringUtils.join("IMS TmInteraction start to Execute, ", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            // (4) 	開始等待接收資料
            try {
                sleep(3000L);
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            this.setWaitProceedState(Boolean.TRUE, false);
            // (5) 	取得接收資料
            String req = StringUtil.toHex("Hello IMS Gateway");
            LogHelperFactory.getTraceLogger().info(Const.MESSAGE_IN, req);
            // (6) 	記錄FEPLOG內容
            logData.setProgramFlowType(ProgramFlow.IMSGWIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            logData.setMessage(req);
            logData.setRemark(StringUtils.join("IMS TmInteraction GetOutputMessage Succeed, ", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            // (7) 	CALL  CBSHandler
            try {
                sleep(3000L);
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            String rtnData = StringUtil.toHex("Hello IMS Center");
            // (8) 	記錄FEPLOG內容
            logData.setProgramFlowType(ProgramFlow.IMSGWOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".run"));
            logData.setMessage(rtnData);
            logData.setRemark(StringUtils.join("Get Response from CBSHandler Succeed, ", messageForParameter));
            FEPBase.logMessage(Level.INFO, logData);
            // (9) 	準備送回IMS
            try {
                interaction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_SENDONLY_CALLOUT_RESPONSE);
                interaction.setTrancode(StringUtils.EMPTY);
                InputMessage inMsg = interaction.getInputMessage();
                inMsg.setInputMessageData(rtnData);
                interaction.execute();
                LogHelperFactory.getTraceLogger().info(Const.MESSAGE_OUT, rtnData);
            } catch (Exception e) {
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".run"));
                logData.setRemark(StringUtils.join("IMS TmInteraction setInputMessageData or Execute Failed, ", messageForParameter));
                FEPBase.sendEMS(logData);
                return FEPReturnCode.ProgramException;// 失敗, 則從(1)開始嘗試
            }
            return FEPReturnCode.Normal;
        }
        return null;
    }

    private String getTransCode(String pCode) {
        String transCode = "FG";

        if (StringUtils.trimToEmpty(pCode).length() != 4) {
            return transCode;
        }

        if (StringUtils.indexOf(pCode, "24") == 0) {
            transCode = "WW";
        } else if (StringUtils.indexOf(pCode, "26") == 0) {
            transCode = "WW";
        } else if (StringUtils.indexOf(pCode, "252") == 0) {
            transCode = "TR";
        } else if (StringUtils.indexOf(pCode, "256") == 0) {
            transCode = "TX";
        } else {
            switch (pCode) {
                case "2510":
                    transCode = "WD";
                    break;
                case "2555":
                case "2556":
                    transCode = "TR";
                    break;
                case "2120":
                case "2130":
                case "2140":
                case "2150":
                case "2160":
                case "2270":
                case "2280":
                case "2290":
                case "2547":
                case "2549":
                case "2573":
                case "2574":
                    transCode = "RV";
                    break;
                case "2531":
                case "2532":
                case "2541":
                case "2542":
                case "2543":
                case "2551":
                case "2552":
                    transCode = "TX";
                    break;
                case "2261":
                case "2262":
                case "2263":
                case "2264":
                case "2566":
                    transCode = "PY";
                    break;
                case "2505":
                case "2545":
                case "2546":
                case "2571":
                case "2572":
                    transCode = "WW";
                    break;
            }
        }

        return transCode;
    }

    /**
     * 十六進制字串轉字節數組
     *
     * @param hexString
     * @return
     */
    private static final String HEX_STRING = "0123456789ABCDEF";

    public static byte[] hexToBytes(String hexString) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hexString.length() / 2);
        for (int i = 0; i < hexString.length(); i += 2) {
            baos.write((HEX_STRING.indexOf(hexString.charAt(i)) << 4 | HEX_STRING.indexOf(hexString.charAt(i + 1))));
        }
        return baos.toByteArray();
    }
}
