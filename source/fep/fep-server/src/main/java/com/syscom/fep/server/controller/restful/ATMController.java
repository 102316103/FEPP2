package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.GWConfig;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiver;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiverManager;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.AtmmstrExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmstatExtMapper;
import com.syscom.fep.mybatis.model.Atmmstr;
import com.syscom.fep.mybatis.model.Atmstat;
import com.syscom.fep.server.common.handler.ATMHandlerP2;
import com.syscom.fep.server.controller.BaseController;
import com.syscom.fep.vo.communication.*;
import com.syscom.fep.vo.communication.ToATMCommuAtmstatList.ToATMCommuAtmstat;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 接收來自ATM GW的Restful/Socket請求
 *
 * @author Richard
 */
@StackTracePointCut(caller = SvrConst.SVR_ATM)
public class ATMController extends BaseController {
    @Autowired
    private AtmmstrExtMapper atmmstrMapper;
    @Autowired
    private AtmstatExtMapper atmstatMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool(new SimpleThreadFactory(SvrConst.SVR_ATM));

    @Override
    public String getName() {
        return SvrConst.SVR_ATM;
    }

    /**
     * 接口方法
     *
     * @param messageIn
     * @return
     */
    @RequestMapping(value = "/recv/atm", method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
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
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            Object commu = BaseXmlCommu.fromXML(messageIn);
            // 為null, 說明電文xml中沒有<classname />, 則預設按照ToFEPATMCommu來轉
            if (commu == null) {
                commu = ToFEPATMCommu.fromXML(messageIn, ToFEPATMCommu.class);
            }
            // ATMGW進來的交易類電文, 收到的是ToFEPATMCommu對應的XML字串, 送出的是ToATMCommu對應的XML字串
            if (commu instanceof ToFEPATMCommu) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommu) commu);
            }
            // ATMGW進來的沒有請求參數查詢資料的電文, 收到的是ToFEPCommuAction對應的XML字串, 送出的是ToGWCommuAction對應的XML字串
            else if (commu instanceof ToFEPCommuAction) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuAction) commu);
            }
            //  ATMGW進來的更新ATM主檔的電文, 收到的是ToFEPATMCommuUpdateAtmmstr對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
            else if (commu instanceof ToFEPATMCommuUpdateAtmmstr) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuUpdateAtmmstr) commu);
            }
            // ATMGW進來的更新Atmstat的電文, 收到的是ToFEPATMCommuUpdateAtmstat對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
            else if (commu instanceof ToFEPATMCommuUpdateAtmstat) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuUpdateAtmstat) commu);
            }
            // ATMGW進來的查詢Atmmstr的電文, 收到的是ToFEPATMCommuAtmmstr對應的XML字串, 送出的是ToATMCommuAtmmstr對應的XML字串
            else if (commu instanceof ToFEPATMCommuAtmmstr) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuAtmmstr) commu);
            }
            // ATMGW進來查詢Zone資料的電文, 收到的是ToFEPCommuZone對應的XML字串, 送出的是ToGWCommuZone對應的XML字串
            else if (commu instanceof ToFEPCommuZone) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuZone) commu);
            }
            // ATMGW進來查詢Config資料的電文, 收到的是ToFEPCommuConfig對應的XML字串, 送出的是ToGWCommuConfig對應的XML字串
            else if (commu instanceof ToFEPCommuConfig) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuConfig) commu);
            }
            // ATMGW進來查詢Atmstat資料的電文, 收到的是ToFEPATMCommuAtmstatList對應的XML字串, 送出的是ToATMCommuAtmstatList對應的XML字串
            else if (commu instanceof ToFEPATMCommuAtmstatList) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPATMCommuAtmstatList) commu);
            }
            // ATMGW進來的查詢Sysconf資料的電文, 收到的是ToFEPCommuSysconf對應的XML字串, 送出的是ToGWCommuSysconf對應的XML字串
            else if (commu instanceof ToFEPCommuSysconf) {
                messageOut = this.processRequestData(programFlow, logData, (ToFEPCommuSysconf) commu);
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
            if (StringUtils.isNotBlank(messageOut)) {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Message: ", messageOut);
            } else {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send Empty Message");
            }
        }
        return messageOut;
    }

    /**
     * ATMGW進來的交易類電文, 收到的是ToFEPATMCommu對應的XML字串, 送出的是ToATMCommu對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommu
     * @return
     */
    private String processRequestData(final ProgramFlow programFlow, final LogData logData, final ToFEPATMCommu toFEPATMCommu) throws Exception {
        String messageOut;
        String responseFromAA = null;
        logData.setMessageCorrelationId(UUID.randomUUID().toString());
        logData.setProgramFlowType(programFlow);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setAtmNo(toFEPATMCommu.getAtmno());
        logData.setEj(Integer.parseInt(toFEPATMCommu.getEj()));
        logData.setMessage(toFEPATMCommu.getMessage());
        logData.setTxRquid(toFEPATMCommu.getTxRquid());
        logData.setRemark(
                StringUtils.join(this.getName(), " Get ATM Request",
                        " EJ:", logData.getEj(),
                        " ATMNo:", logData.getAtmNo(),
                        " ATMSeq:", logData.getAtmSeq(),
                        " MessageId:", logData.getMessageId(),
                        " Step:", logData.getStep(),
                        " TxRquid:", logData.getTxRquid())
        );
        this.logMessage(logData);
        try {
            // Call Handler
            final ATMHandlerP2 atmHandler = new ATMHandlerP2();
            atmHandler.setEj(logData.getEj());
            atmHandler.setMessageId(logData.getMessageId());
            atmHandler.setAtmNo(logData.getAtmNo());
            atmHandler.setAtmSeq(logData.getAtmSeq());
            atmHandler.setLogContext(logData);
            atmHandler.setMessageCorrelationId(logData.getMessageCorrelationId());
            atmHandler.setTxRquid(logData.getTxRquid());
            // 這裡的callback是for ATMAdapter回response給ATM的
            // 因為有一種情形，ATMHandler在處理完後，直接由ATMAdapter送response，所以這裡的callback要接一下
            final MessageAsynchronousWaitReceiver<String, String> callback = new MessageAsynchronousWaitReceiver<String, String>(logData.getMessageCorrelationId());
            MessageAsynchronousWaitReceiverManager.subscribe(this, callback);
            boolean waitSucceed = true;
            if (toFEPATMCommu.isSync()) {
                LogHelperFactory.getTraceLogger().info(this.getName(), " Process RESTFul logData by sync...");
                this.handlerDispatch(atmHandler, toFEPATMCommu.getMessage(), callback);
            } else {
                LogHelperFactory.getTraceLogger().info(this.getName(), " Process RESTFul logData by async...");
                executor.execute(() -> {
                    // 因為是新起了一個線程在跑，所以這裡必須要再塞入一次
                    LogMDC.put(Const.MDC_PROFILE, this.getName());
                    this.handlerDispatch(atmHandler, toFEPATMCommu.getMessage(), callback);
                });
                // Wait for ATMAdapter callback or Handler return response
                int timeout = GWConfig.getInstance().getAATimeout() * 1000;
                waitSucceed = callback.waitMessage(timeout);
            }
            responseFromAA = callback.getMessage();
            if (!waitSucceed)
                throw ExceptionUtil.createTimeoutException(this.getName(), " Invoke AA Timeout");
            else if (StringUtils.isBlank(responseFromAA)) {
                // throw ExceptionUtil.createException(this.getName(), " AA Response is null");
                logData.setRemark(StringUtils.join(this.getName(), " AA Response is null"));
            } else {
                logData.setRemark(StringUtils.join(this.getName(), " Get ATM Response OK"));
            }
            logData.setProgramFlowType(this.getOutProgramFlow(programFlow));
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setMessage(responseFromAA);
            this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramFlowType(programFlow);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            logData.setRemark(StringUtils.join(this.getName(), " Get ATM Response with exception occur, ", e.getMessage()));
            logData.setMessage(toFEPATMCommu.getMessage());
            this.logMessage(logData);
            throw e;
        } finally {
            // unsubscribe
            MessageAsynchronousWaitReceiverManager.unsubscribe(this, logData.getMessageCorrelationId());
            // ToATMCommu
            ToATMCommu toATMCommu = new ToATMCommu();
            toATMCommu.setAtmno(toFEPATMCommu.getAtmno());
            toATMCommu.setEj(toFEPATMCommu.getEj());
            toATMCommu.setTxRquid(toFEPATMCommu.getTxRquid());
            toATMCommu.setMessage(responseFromAA);
            messageOut = toATMCommu.toString();
        }
        return messageOut;
    }

    private void handlerDispatch(ATMHandlerP2 atmHandler, String request, MessageAsynchronousWaitReceiver<String, String> callback) {
        String atmNo = atmHandler.getAtmNo();
        String response = atmHandler.dispatch(FEPChannel.ATM, atmNo, request);
        callback.messageArrived(this, response);
    }

    /**
     * ATMGW進來的更新ATM主檔的電文, 收到的是ToFEPATMCommuUpdateAtmmstr對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuUpdateAtmmstr
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuUpdateAtmmstr toFEPATMCommuUpdateAtmmstr) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuUpdateAtmmstr);
        ToGWCommuDbOptResult toGWCommuDbOptResult = new ToGWCommuDbOptResult();
        Atmmstr atmmstr = new Atmmstr();
        atmmstr.setAtmAtmno(toFEPATMCommuUpdateAtmmstr.getAtmAtmno());
        atmmstr.setAtmAtmpIp(toFEPATMCommuUpdateAtmmstr.getAtmAtmpIp());
        atmmstr.setAtmAtmpPort(toFEPATMCommuUpdateAtmmstr.getAtmAtmpPort());
        atmmstr.setAtmCertalias(toFEPATMCommuUpdateAtmmstr.getAtmCertalias());
        try {
            int result = atmmstrMapper.updateAtmmstrByAtmNoSelective(atmmstr);
            toGWCommuDbOptResult.setResult(result);
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuDbOptResult, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuDbOptResult);
        return toGWCommuDbOptResult.toString();
    }

    /**
     * ATMGW進來的更新Atmstat的電文, 收到的是ToFEPATMCommuUpdateAtmstat對應的XML字串, 送出的是ToGWCommuDbOptResult對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuUpdateAtmstat
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuUpdateAtmstat toFEPATMCommuUpdateAtmstat) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuUpdateAtmstat);
        ToGWCommuDbOptResult toGWCommuDbOptResult = new ToGWCommuDbOptResult();
        try {
            Atmstat atmstat = new Atmstat();
            atmstat.setAtmstatAtmno(toFEPATMCommuUpdateAtmstat.getAtmstatAtmno());
            atmstat.setAtmstatStatus(toFEPATMCommuUpdateAtmstat.getAtmstatStatus());
            atmstat.setAtmstatSocket(toFEPATMCommuUpdateAtmstat.getAtmstatSocket());
            atmstat.setAtmstatSec(toFEPATMCommuUpdateAtmstat.getAtmstatSec());
            atmstat.setAtmstatInikey(toFEPATMCommuUpdateAtmstat.getAtmstatInikey());
            atmstat.setAtmstatApVersionN(toFEPATMCommuUpdateAtmstat.getAtmstatApVersionN());
            int result = atmstatMapper.updateAtmstatByAtmAtmpIp(atmstat, toFEPATMCommuUpdateAtmstat.getAtmAtmpIp());
            toGWCommuDbOptResult.setResult(result);
        } catch (Exception e) {
            handleException(programFlow, logData, toGWCommuDbOptResult, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toGWCommuDbOptResult);
        return toGWCommuDbOptResult.toString();
    }

    /**
     * ATMGW進來的查詢Atmmstr的電文, 收到的是ToFEPATMCommuAtmmstr對應的XML字串, 送出的是ToATMCommuAtmmstr對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuAtmmstr
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuAtmmstr toFEPATMCommuAtmmstr) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuAtmmstr);
        ToATMCommuAtmmstr toATMCommuAtmmstr = new ToATMCommuAtmmstr();
        try {
            Map<String, Object> atmmstrMap = new HashMap<String, Object>();
            // 查詢by ATM NO
            if (StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmNo())) {
                atmmstrMap = atmmstrMapper.getAtmmstrByAtmNo(toFEPATMCommuAtmmstr.getAtmNo());
            }
            // 查詢by ATM IP
            else if (StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmIp())) {
                atmmstrMap = atmmstrMapper.getAtmmstrByAtmIp(toFEPATMCommuAtmmstr.getAtmIp());
            }
            if (MapUtils.isNotEmpty(atmmstrMap)) {
                toATMCommuAtmmstr.setAtmAtmno((String) atmmstrMap.get("ATM_ATMNO"));
                toATMCommuAtmmstr.setAtmZone((String) atmmstrMap.get("ATM_ZONE"));
                toATMCommuAtmmstr.setAtmCheckMac(DbHelper.toBoolean(((Integer) atmmstrMap.get("ATM_CHECK_MAC")).shortValue()));
                toATMCommuAtmmstr.setAtmstatSec(((BigDecimal) atmmstrMap.get("ATMSTAT_SEC")).shortValue());
                toATMCommuAtmmstr.setAtmstatSocket(((BigDecimal) atmmstrMap.get("ATMSTAT_SOCKET")).intValue());
                toATMCommuAtmmstr.setAtmstatInikey(((BigDecimal) atmmstrMap.get("ATMSTAT_INIKEY")).intValue());
                toATMCommuAtmmstr.setAtmIp((String) atmmstrMap.get("ATM_IP"));
                toATMCommuAtmmstr.setAtmAtmpPort((String) atmmstrMap.get("ATM_ATMP_PORT"));
                toATMCommuAtmmstr.setAtmCertAlias((String) atmmstrMap.get("ATM_CERTALIAS"));
                toATMCommuAtmmstr.setAtmFepConnection(((Integer) atmmstrMap.get("ATM_FEP_CONNECTION")).shortValue());
            } else {
                // 查詢不到資料
                toATMCommuAtmmstr.setErrmsg(
                        StringUtils.join("Cannot found Atmmstr",
                                StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmNo())
                                        ? StringUtils.join(", AtmNo = [", toFEPATMCommuAtmmstr.getAtmNo(), "]")
                                        : StringUtils.EMPTY,
                                StringUtils.isNotBlank(toFEPATMCommuAtmmstr.getAtmIp())
                                        ? StringUtils.join(", AtmIp = [", toFEPATMCommuAtmmstr.getAtmIp(), "]")
                                        : StringUtils.EMPTY));
            }
        } catch (Exception e) {
            handleException(programFlow, logData, toATMCommuAtmmstr, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toATMCommuAtmmstr);
        return toATMCommuAtmmstr.toString();
    }

    /**
     * GW進來查詢Atmstat資料的電文, 收到的是ToFEPATMCommuAtmstatList對應的XML字串, 送出的是ToATMCommuAtmstatList對應的XML字串
     *
     * @param programFlow
     * @param logData
     * @param toFEPATMCommuAtmstatList
     * @return
     */
    private String processRequestData(ProgramFlow programFlow, LogData logData, ToFEPATMCommuAtmstatList toFEPATMCommuAtmstatList) {
        loggingLogData(programFlow, MessageFlow.Request, logData, toFEPATMCommuAtmstatList);
        ToATMCommuAtmstatList toATMCommuAtmstatList = new ToATMCommuAtmstatList();
        try {
            List<Map<String, Object>> atmstatList = atmstatMapper.selectAtmstatList(toFEPATMCommuAtmstatList.getAtmstatAtmnoList(), (short) toFEPATMCommuAtmstatList.getAtmstatStatus());
            if (CollectionUtils.isNotEmpty(atmstatList)) {
                List<ToATMCommuAtmstat> list = new ArrayList<>();
                Calendar calendar = Calendar.getInstance();
                for (Map<String, Object> orig : atmstatList) {
                    ToATMCommuAtmstat dest = new ToATMCommuAtmstat();
                    dest.setAtmstatAtmno((String) orig.get("ATMSTAT_ATMNO"));
                    dest.setAtmstatStatus(((BigDecimal) orig.get("ATMSTAT_STATUS")).intValue());
                    //20230504 Bruce add 加入連線及斷線時間
                    if (orig.get("ATMSTAT_LAST_OPEN") != null) {
                        dest.setAtmstatLastOpen(FormatUtil.dateTimeFormat(CalendarUtil.clone(((Timestamp) orig.get("ATMSTAT_LAST_OPEN")).getTime()), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS));
                    }
                    if (orig.get("ATMSTAT_LAST_CLOSE") != null) {
                        dest.setAtmstatLastClose(FormatUtil.dateTimeFormat(CalendarUtil.clone(((Timestamp) orig.get("ATMSTAT_LAST_CLOSE")).getTime()), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS));
                    }
                    if (orig.get("ATM_ATMP_IP") != null) {
                        dest.setAtmmstrAtmpIp(orig.get("ATM_ATMP_IP").toString());
                    }
                    list.add(dest);
                }
                toATMCommuAtmstatList.setAtmstatList(list);
            }
        } catch (Exception e) {
            handleException(programFlow, logData, toATMCommuAtmstatList, e);
        }
        loggingLogData(programFlow, MessageFlow.Response, logData, toATMCommuAtmstatList);
        return toATMCommuAtmstatList.toString();
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