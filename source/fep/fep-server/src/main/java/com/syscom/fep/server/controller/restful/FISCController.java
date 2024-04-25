package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.server.controller.BaseController;
import com.syscom.fep.vo.communication.BaseXmlCommu;
import com.syscom.fep.vo.communication.ToFEPCommuAction;
import com.syscom.fep.vo.communication.ToFEPCommuSysconf;
import com.syscom.fep.vo.communication.ToFEPFISCCommu;
import com.syscom.fep.vo.enums.FISCSubSystem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 接收來自FISC GW的Restful請求
 *
 * @author Richard
 */
@StackTracePointCut(caller = SvrConst.SVR_FISC)
public class FISCController extends BaseController {
    private final ExecutorService executor = Executors.newCachedThreadPool(new SimpleThreadFactory(SvrConst.SVR_FISC));

    @Override
    public String getName() {
        return SvrConst.SVR_FISC;
    }

    @RequestMapping(value = "/recv/fisc", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sendReceive(@RequestBody String messageIn) {
        // Restful進來的電文
        return this.processRequestData(ProgramFlow.RESTFulIn, messageIn);
    }

    /**
     * 處理進來的電文並回應
     *
     * @param programFlow
     * @param messageIn
     * @return
     */
    @Override
    protected String processRequestData(final ProgramFlow programFlow, final String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Receive Message:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        Object commu = null;
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            commu = BaseXmlCommu.fromXML(messageIn);
            // 為null, 說明電文xml中沒有<classname />, 則預設按照ToFEPFISCCommu來轉
            if (commu == null) {
                commu = ToFEPFISCCommu.fromXML(messageIn, ToFEPFISCCommu.class);
            }
            // FISCGW進來的交易類電文, 收到的是ToFEPFISCCommu對應的XML字串, 送出的是HEX String
            if (commu instanceof ToFEPFISCCommu) {
                this.processRequestData(programFlow, logData, (ToFEPFISCCommu) commu);
            }
            // FISCGW進來的查詢Sysconf資料的電文, 收到的是ToFEPCommuSysconf對應的XML字串, 送出的是ToGWCommuSysconf對應的XML字串
            else if (commu instanceof ToFEPCommuSysconf) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuSysconf) commu);
            }
            // FISCGW進來的沒有請求參數查詢資料的電文, 收到的是ToFEPCommuAction對應的XML字串, 送出的是ToGWCommuAction對應的XML字串
            else if (commu instanceof ToFEPCommuAction) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuAction) commu);
            }
            // 視情況是否需要補充
            else {
                throw ExceptionUtil.createIllegalArgumentException("無效的請求, ", commu.getClass().getName());
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setRemark(e.getMessage());
            logData.setMessage(messageIn);
            sendEMS(logData);
        } finally {
            if (!(commu instanceof ToFEPFISCCommu)) {
                if (StringUtils.isNotBlank(messageOut)) {
                    LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Message: ", messageOut);
                } else {
                    LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Empty Message");
                }
            }
        }
        return messageOut;
    }

    /**
     * FISCGW進來的交易類電文, 收到的是ToFEPFISCCommu對應的XML字串, 送出的是HEX String
     *
     * @param programFlow
     * @param logData
     * @param toFEPFISCCommu
     * @return
     */
    private void processRequestData(final ProgramFlow programFlow, final LogData logData, final ToFEPFISCCommu toFEPFISCCommu) {
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setStep(toFEPFISCCommu.getStep());
        logData.setEj(toFEPFISCCommu.getEj());
        logData.setTxRquid(toFEPFISCCommu.getTxRquid());
        logData.setStan(toFEPFISCCommu.getStan());
        logData.setMessage(toFEPFISCCommu.getMessage());
        logData.setRemark(
                StringUtils.join(this.getName(), " Get FISC Request",
                        " EJ:", logData.getEj(),
                        " Stan:", logData.getStan(),
                        " Step:", logData.getStep(),
                        " TxRquid:", logData.getTxRquid(),
                        " Message:", logData.getMessage())
        );
        this.logMessage(logData);
        RefBase<SubSystem> refSubSystem = new RefBase<SubSystem>(null);
        RefBase<FISCSubSystem> refFISCSubSystem = new RefBase<FISCSubSystem>(null);
        this.getFISCSubSystem(toFEPFISCCommu.getMessage(), refSubSystem, refFISCSubSystem);
        logData.setSubSys(refSubSystem.get());
        if (toFEPFISCCommu.isSync()) {
            this.invokeAA(programFlow, logData, refSubSystem.get(), refFISCSubSystem.get(), toFEPFISCCommu);
        } else {
            executor.execute(() -> {
                // 因為是新起了一個線程在跑，所以這裡必須要再塞入一次
                LogMDC.put(Const.MDC_PROFILE, this.getName());
                this.invokeAA(programFlow, logData, refSubSystem.get(), refFISCSubSystem.get(), toFEPFISCCommu);
            });
        }
    }

    private void getFISCSubSystem(final String data, RefBase<SubSystem> refSubSystem, RefBase<FISCSubSystem> refFISCSubSystem) {
        String msgId = data.substring(14, 16);
        switch (msgId) {
            case "30":
            case "33":
            case "F0":
            case "F3":
                refSubSystem.set(SubSystem.INBK);
                refFISCSubSystem.set(FISCSubSystem.OPC);
                break;
            case "32":
            case "F2":
                refSubSystem.set(SubSystem.INBK);
                // 2016-04-14 Modify by Ruling for EMV晶片卡26類原存交易：子系統代號為EMVIC
                refFISCSubSystem.set(data.substring(14, 18).equals("3236") ? FISCSubSystem.EMVIC : FISCSubSystem.INBK);
                break;
            case "31":
            case "F1":
                refSubSystem.set(SubSystem.RM);
                refFISCSubSystem.set(data.substring(14, 18).equals("3136") ? FISCSubSystem.FCRM : FISCSubSystem.RM);
                break;
            case "35":
            case "F5":
                refSubSystem.set(SubSystem.INBK);
                refFISCSubSystem.set(data.substring(14, 18).equals("3538") ? FISCSubSystem.FCCLR : FISCSubSystem.CLR);
                break;
        }
    }

    private void invokeAA(final ProgramFlow programFlow, final LogData logData, final SubSystem subSystem, final FISCSubSystem fiscSubSystem, final ToFEPFISCCommu toFEPFISCCommu) {
        final String message = toFEPFISCCommu.getMessage();
        String messageOut = StringUtils.EMPTY;
        try {
            FISCHandler fiscHandler = new FISCHandler();
            fiscHandler.setEj(logData.getEj());
            fiscHandler.setTxRquid(logData.getTxRquid());
            fiscHandler.setLogContext(logData);
            fiscHandler.setRClientID(toFEPFISCCommu.getRClientID()); // 把這個值傳給fiscHandler
            String rtn = fiscHandler.dispatch(subSystem, fiscSubSystem, FEPChannel.FISC, message);
            logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".invokeAA"));
            logData.setRemark(StringUtils.join(this.getName(), " Get AA Response OK"));
            logData.setMessage(rtn);
            if (StringUtils.isNotBlank(rtn)) {
                FISCData data = new FISCData();
                data.setLogContext(logData);
                data.setRClientID(toFEPFISCCommu.getRClientID()); // 放入fiscData中
                FISCAdapter adapter = new FISCAdapter(data);
                adapter.setMessageToFISC(rtn);
                adapter.sendReceive();
                messageOut = adapter.getFISCRestFulRequestOut().toString();
                logData.setRemark(StringUtils.join(this.getName(), " Send FISC Response STAN=", logData.getStan()));
            } else {
                logData.setRemark(StringUtils.join(this.getName(), " Empty FISC Response STAN=", logData.getStan()));
            }
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramFlowType(programFlow);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setRemark(StringUtils.join(this.getName(), " Get FISC Response with exception occur, ", e.getMessage()));
            logData.setMessage(message);
            this.logMessage(logData);
            throw e;
        } finally {
            if (StringUtils.isNotBlank(messageOut)) {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Message: ", messageOut);
            } else {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Empty Message");
            }
        }
    }

    @PreDestroy
    public void destroy() {
        LogHelper logger = LogHelperFactory.getTraceLogger();
        logger.trace(getName(), " start to destroy...");
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                logger.trace(getName(), " executor terminate all runnable successful");
            else
                logger.trace(getName(), " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }
}
