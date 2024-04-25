package com.syscom.fep.gateway.agent.fisc;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayCmdAction;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayMode;
import com.syscom.fep.vo.enums.RestfulResultCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FISCGatewayClientAgent extends FEPBase {
    @Autowired
    private FISCGatewayClientAgentConfiguration configuration;
    private HttpClient httpClient;
    private final ExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(Gateway.FISCGW.name()));

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, StringUtils.join(Gateway.FISCGW.name()));
    }

    @PostConstruct
    public void initialization() {
        httpClient = new HttpClient(configuration.isRecordHttpLog());
    }

    /**
     * 可以使用如下指令啟動FISCGW
     * <p>
     * curl -X POST http://localhost:8304/recv/fisc/start
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageStart(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to execute command = [", configuration.getCmdStart(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        ProcessBuilder processBuilder = new ProcessBuilder().command(configuration.getCmdStart().split("\\s+"));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            Consumer<String> consumer = configuration.isPrintInputStream() ? LogHelperFactory.getTraceLogger()::debug : null;
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
            executor.submit(streamGobbler);
            logData.setRemark(StringUtils.join("Execute successful, command = [", configuration.getCmdStart(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return Const.REPLY_OK;
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Execute command = [", configuration.getCmdStart(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Start FISCGateway failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令啟動FISCGW
     * <p>
     * curl -X POST http://localhost:8304/recv/fisc/stop
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/stop", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageStop(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStop"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpTerminate(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            String response = httpClient.doPost(configuration.getHttpTerminate(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStop"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpOperate(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStop"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getCmdStart(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Stop FISCGateway failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令操作FISCGW Channel
     * <p>
     * curl -d "mode=primary&action=start" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=secondary&action=start" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=all&action=start" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=primary&action=stop" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=secondary&action=stop" -X POST http://localhost:8304/recv/fisc/channel
     * <p>
     * curl -d "mode=all&action=stop" -X POST http://localhost:8304/recv/fisc/channel
     *
     * @param operator
     * @param mode
     * @param action
     * @return
     */
    @RequestMapping(value = "/recv/fisc/channel", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageChannel(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "mode", required = true) FISCGatewayMode mode,
            @RequestParam(value = "action", required = true) FISCGatewayCmdAction action) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChannel"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpOperate(), "], operator = [", operator, "], mode = [", mode, "], action = [", action, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            args.put("mode", mode.name());
            args.put("action", action.name());
            String response = httpClient.doPost(configuration.getHttpOperate(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChannel"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpOperate(), "], operator = [", operator, "], mode = [", mode, "], action = [", action, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageChannel"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getCmdStart(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "FISCGateway ", mode, " Channel ", action, " failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令check FISCGW
     * <p>
     * curl -d "action=check" -X POST http://localhost:8304/recv/fisc/check
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/fisc/check", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageCheck(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheck"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to do http post = [", configuration.getHttpOperate(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        try {
            Map<String, String> args = new HashMap<>();
            args.put("operator", StringUtils.isNotBlank(operator) ? operator : FEPConfig.getInstance().getApplicationName().toUpperCase());
            args.put("action", FISCGatewayCmdAction.check.name());
            String response = httpClient.doPost(configuration.getHttpOperate(), MediaType.APPLICATION_FORM_URLENCODED, args, true);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheck"));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(response);
            logData.setRemark(StringUtils.join("Get response = [", response, "], url = [", configuration.getHttpOperate(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return response;
        } catch (Throwable e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageCheck"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Do http post = [", configuration.getCmdStart(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join(this.getErrorCode(e), "Check FISCGateway Status failed with exception occur, ", e.getMessage());
        }
    }

    private String getErrorCode(Throwable e) {
        if (RestfulResultCode.CONNECTION_REFUSED.name().equals(e.getMessage())) {
            return StringUtils.join("[", RestfulResultCode.CONNECTION_REFUSED.name(), "]");
        } else if (RestfulResultCode.READ_TIMED_OUT.name().equals(e.getMessage())) {
            return StringUtils.join("[", RestfulResultCode.READ_TIMED_OUT.name(), "]");
        }
        return StringUtils.join("[", ExceptionUtil.EXCEPTION_OCCUR, "]");
    }

    @PreDestroy
    public void destroy() {
        LogHelper logger = LogHelperFactory.getTraceLogger();
        logger.trace(ProgramName, " start to destroy...");
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                logger.trace(ProgramName, " executor terminate all runnable successful");
            else
                logger.trace(ProgramName, " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }
}
