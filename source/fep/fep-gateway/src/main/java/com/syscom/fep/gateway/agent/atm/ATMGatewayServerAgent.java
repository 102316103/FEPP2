package com.syscom.fep.gateway.agent.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.GetApLogFilesUtil;
import com.syscom.fep.common.util.GetApLogFilesUtil.ApLogType;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.gateway.entity.Gateway;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ATMGatewayServerAgent extends FEPBase {
    @Autowired
    private ATMGatewayServerAgentConfiguration atmGatewayServerAgentConfiguration;
    private final ExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(Gateway.ATMGW.name()));

    private void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, StringUtils.join(Gateway.ATMGW.name()));
    }

    /**
     * 可以使用如下指令啟動ATMGW
     * <p>
     * curl -X POST http://localhost:8302/recv/atm/start
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/recv/atm/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String onMessageStart(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("Start to execute command = [", atmGatewayServerAgentConfiguration.getCmdStart(), "], operator = [", operator, "]"));
        this.logMessage(logData);
        ProcessBuilder processBuilder = new ProcessBuilder().command(atmGatewayServerAgentConfiguration.getCmdStart().split("\\s+"));
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            Consumer<String> consumer = atmGatewayServerAgentConfiguration.isPrintInputStream() ? LogHelperFactory.getTraceLogger()::debug : null;
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), consumer);
            executor.submit(streamGobbler);
            logData.setOperator(operator);
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setRemark(StringUtils.join("Execute successful, command = [", atmGatewayServerAgentConfiguration.getCmdStart(), "], operator = [", operator, "]"));
            this.logMessage(logData);
            return Const.REPLY_OK;
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".onMessageStart"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("Execute command = [", atmGatewayServerAgentConfiguration.getCmdStart(), "], operator = [", operator, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
            return StringUtils.join("Start ATMGateway Server failed with exception occur, ", e.getMessage());
        }
    }

    /**
     * 可以使用如下指令
     * <p>
     * curl "http://localhost:8302/fepgwagent/GetAPLog?operator=richard&logType=aplog&logDate=2023-10-07&fepLogPath=%2Ffep%2Flogs&fepLogArchivesPath=%2Ffep%2Flogs%2Farchives"
     *
     * @param operator
     * @param logType
     * @param logDate
     * @param fepLogPath
     * @param fepLogArchivesPath
     * @return
     */
    @RequestMapping(value = "/fepgwagent/GetAPLog", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getApLog(
            @RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator,
            @RequestParam(value = "logType") String logType,
            @RequestParam(value = "logDate") String logDate,
            @RequestParam(value = "fepLogPath") String fepLogPath,
            @RequestParam(value = "fepLogArchivesPath") String fepLogArchivesPath) {
        putMDC();
        LogData logData = new LogData();
        logData.setOperator(operator);
        logData.setProgramName(StringUtils.join(ProgramName, ".getApLog"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setRemark(StringUtils.join("GetAPLog, operator = [", operator, "], logType = [", logType, "], logDate = [", logDate, "], fepLogPath = [", fepLogPath, "], fepLogArchivesPath = [", fepLogArchivesPath, "]"));
        this.logMessage(logData);
        try {
            byte[] bytes = GetApLogFilesUtil.getApLogFiles(ApLogType.valueOf(logType), logDate, fepLogPath, null, fepLogArchivesPath);
            if (ArrayUtils.isNotEmpty(bytes)) {
                return bytes;
            }
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".getApLog"));
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("GetAPLog, operator = [", operator, "], logType = [", logType, "], logDate = [", logDate, "], fepLogPath = [", fepLogPath, "], fepLogArchivesPath = [", fepLogArchivesPath, "], with exception occur, ", e.getMessage()));
            sendEMS(logData);
        }
        return new byte[0];
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
