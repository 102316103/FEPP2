package com.syscom.fep.enclib;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.enclib.configurer.ENCConfiguration;
import com.syscom.fep.enclib.enums.ENCMessageFlow;
import com.syscom.fep.enclib.enums.ENCProgramFlow;
import com.syscom.fep.enclib.enums.ENCRC;
import com.syscom.fep.enclib.function.ENCFunction;
import com.syscom.fep.enclib.vo.ENCConfig;
import com.syscom.fep.enclib.vo.ENCLogData;
import com.syscom.fep.enclib.vo.SuipData;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.EnumUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.enc.ext.mapper.WhitelistExtMapper;
import com.syscom.fep.mybatis.enc.model.Whitelist;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.lang.reflect.Constructor;
import java.util.*;

public class ENCLib implements Const {
    private static final String ProgramName = ENCLib.class.getSimpleName();
    private static final LogHelper ENCLogger = LogHelperFactory.getENCLogger();
    private ENCLogData logData;
    private String callObj;
    private static Map<String, Whitelist> _whiteList = new HashMap<>();

    // 20210920 update by ashiang: add whitelist feature
    static {
        getWhiteList();
    }

    public ENCLib(ENCLogData logData, int encRetryCount, int encRetryInterval, String suipAddress, int suipTimeout) {
        this.logData = logData;
        ENCConfig.EncRetryCount = encRetryCount;
        ENCConfig.EncRetryInterval = encRetryInterval;
        ENCConfig.SuipAddress = suipAddress;
        ENCConfig.SuipTimeout = suipTimeout;
        List<String> callObjList = this.getCallObjList();
        this.callObj = StringUtils.join(callObjList, "==>");
        // 20210920 update by ashiang: add whitelist feature
        String objName = callObjList.get(0);
        Whitelist whitelist = _whiteList.get(objName);
        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        if (whitelist == null || currentTimeInMillis < whitelist.getEffectdate().getTime() || currentTimeInMillis > whitelist.getExpiredate().getTime()) {
            throw ExceptionUtil.createRuntimeException("Access Denied for ", objName);
        }
    }

    public int callSuip(String function, String keyid, String inputStr1, String inputStr2, RefString outStr1, RefString outStr2) {
        this.logData.setProgramName(StringUtils.join(ProgramName, ".callSuip"));
        this.logData.setMessageFlowType(ENCMessageFlow.Request);
        this.logData.setProgramFlowType(ENCProgramFlow.ENCIn);
        this.logData.setRemark(StringUtils.join(ProgramName, " Enter"));
        writeLog(Level.DEBUG, this.logData, function, keyid, inputStr1, inputStr2, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, callObj, StringUtils.EMPTY);
        int rc = ENCRC.ENCLibError.getValue();
        try {
            SuipData suip = new SuipData();
            suip.setFunctionNo(function);
            suip.setKeyIdentity(keyid);
            suip.setInputData1(inputStr1);
            suip.setInputData2(inputStr2);
            suip.setOutputData1(StringUtils.EMPTY);
            suip.setOutputData2(StringUtils.EMPTY);
            suip.setTxLog(logData);
            suip.setRc(rc);

            // 2021-07-29 Richard add
            // 這裡根據設置的開關判斷是否需要call suip via socket
            boolean needCallFunction = ENCConfiguration.getInstance().isEncLibEnable();
            if (needCallFunction) {
                // 如果是代理行交易, 則需要根據config檔來判斷
                if (FEPChannel.ATM.name().equals(this.logData.getChannel())
                        || FEPChannel.NETBANK.name().equals(this.logData.getChannel())
                        || FEPChannel.WEBATM.name().equals(this.logData.getChannel())
                        || FEPChannel.FCS.name().equals(this.logData.getChannel())
                        || FEPChannel.IVR.name().equals(this.logData.getChannel())
//                        || FEPChannel.MMAB2C.name().equals(this.logData.getChannel())
                        || FEPChannel.MOBILBANK.name().equals(this.logData.getChannel())) {
                    needCallFunction = ENCConfiguration.getInstance().isEncLibAtmAgentEnable();
                }
            }

            if (needCallFunction) {
                // 通過反射的方式，依據function取得對應要處理的類
                Class<?> cls = Class.forName(StringUtils.join("com.syscom.fep.enclib.function.", function));
                Constructor<?> constructor = cls.getConstructor(SuipData.class);
                ENCFunction target = (ENCFunction) constructor.newInstance(suip);
                SuipData rtn = target.process();
                outStr1.set(rtn.getOutputData1());
                outStr2.set(rtn.getOutputData2());
                rc = rtn.getRc();
            } else {
                String mac = getDefaultMacForENCLibDisable(function);
                outStr1.set(mac);
                outStr2.set(mac);
                rc = ENCRC.Normal.getValue();
                LogHelperFactory.getTraceLogger().info("[", ProgramName, "]No need call suip for channel = [", this.logData.getChannel(), "], return mac = [", mac, "], rc = [", rc, "] by constant");
            }

            this.logData.setProgramName(StringUtils.join(ProgramName, ".callSuip"));
            this.logData.setProgramFlowType(ENCProgramFlow.ENCOut);
            this.logData.setMessageFlowType(ENCMessageFlow.Response);
            this.logData.setRemark(StringUtils.join(ProgramName, " Exit Normally"));
            writeLog(Level.DEBUG, this.logData, function, keyid, inputStr1, inputStr2, outStr1.get(), outStr2.get(), StringUtils.EMPTY, callObj, String.valueOf(rc));
        } catch (Exception e) {
            this.logData.setRemark(StringUtils.join(ProgramName, " error function=", function, ",keyid=", keyid, ",inputStr1=", inputStr1, ",inputStr2=", inputStr2));
            this.logData.setProgramException(e);
            writeLog(Level.ERROR, this.logData, function, keyid, inputStr1, inputStr2, "rc = ["+ rc+ "]", StringUtils.EMPTY, StringUtils.EMPTY, callObj, StringUtils.EMPTY);
            ENCSendEMS.sendEMS(this.logData);
        }
        return rc;
    }

    public static void writeLog(
            Level loglevel,
            ENCLogData logData,
            String function,
            String keyid,
            String inputStr1,
            String inputStr2,
            String outStr1,
            String outStr2,
            String suipCommand,
            String callingprogram,
            String rc) {
        LogMDC.put(MDC_LOGDATE, FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSSS));
        LogMDC.put(MDC_EJ, String.valueOf(logData.getEj()));
        LogMDC.put(MDC_PROGRAMFLOW, EnumUtil.getEnumName(logData.getProgramFlowType()));
        LogMDC.put(MDC_PROGRAMNAME, logData.getProgramName());
        LogMDC.put(MDC_FUNCNO, function);
        LogMDC.put(MDC_KEYID, keyid);
        LogMDC.put(MDC_INPUT1, inputStr1);
        LogMDC.put(MDC_INPUT2, inputStr2);
        LogMDC.put(MDC_OUTPUT1, outStr1);
        LogMDC.put(MDC_OUTPUT2, outStr2);
        LogMDC.put(MDC_SUIPCOMMAND, suipCommand);
        LogMDC.put(MDC_RC, rc);
        LogMDC.put(MDC_CALLOBJ, callingprogram);
        LogMDC.put(Const.MDC_TXRQUID, logData.getTxRquid());
        LogMDC.put(Const.MDC_HOSTNAME, logData.getHostName());
        LogMDC.put(Const.MDC_HOSTIP, logData.getHostIp());
        switch (loglevel) {
            case DEBUG:
                ENCLogger.debug(logData.getRemark());
                break;
            case INFO:
                ENCLogger.info(logData.getRemark());
                break;
            case WARN:
                ENCLogger.warn(logData.getRemark());
                break;
            case ERROR:
                ENCLogger.error(logData.getRemark());
                break;
            case TRACE:
                ENCLogger.trace(logData.getRemark());
                break;
        }
    }

    public static String getDefaultMacForENCLibDisable(String function) {
        int size = 12;
        if ("FN000101".equals(function)) {
            // 2021-07-08 Richard add
            // 後面的程式會進行截取，所以這裡塞夠長度
            size = 36;
        } else if ("FN000102".equals(function)) {
            size = 44;
        } else if ("FN000204".equals(function)) {
            size = 20;
        }
        return StringUtils.rightPad("0008", size, "F");
    }

    /**
     * 取出呼叫ENCLib最原始的服務名稱, 主要取的是有@StackTracePointCut注入的類
     *
     * @return
     */
    private List<String> getCallObjList() {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        if (ArrayUtils.isNotEmpty(elements)) {
            List<String> callerObjList = new ArrayList<>();
            for (int i = 1; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                String className = element.getClassName();
                // 只看fep的程式
                if (!className.startsWith("com.syscom.fep")) {
                    continue;
                }
                // 有一些特殊的caller, 後面Class.forName會出異常, 這裡先filter出來
                if (this.filterSpecialCallObj(callerObjList, className)) {
                    break;
                }
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    break;
                }
                StackTracePointCut annotation = clazz.getAnnotation(StackTracePointCut.class);
                if (annotation == null) {
                    Class<?> superClazz = clazz.getSuperclass();
                    while (superClazz != null) {
                        annotation = superClazz.getAnnotation(StackTracePointCut.class);
                        if (annotation == null) {
                            superClazz = superClazz.getSuperclass();
                        } else {
                            break;
                        }
                    }
                    if (annotation == null) {
                        continue;
                    }
                }
                if (!callerObjList.contains(annotation.caller())) {
                    callerObjList.add(0, annotation.caller());
                }
            }
            if (!callerObjList.isEmpty()) {
                return callerObjList;
            }
            return Collections.singletonList(elements[2].getClassName());
        }
        return Collections.emptyList();
    }

    private boolean filterSpecialCallObj(List<String> callerObjList, String className) {
        // 批次的task程式, 因為採用動態class, 所以Class.forName是找不到的, 所以這裡預設就是task程式的名字
        if (className.startsWith("com.syscom.fep.batch.task")) {
            int beginIndex = className.lastIndexOf(".");
            String callObj = className.substring(beginIndex + 1);
            if (!callerObjList.contains(callObj)) {
                callerObjList.add(0, callObj);
            }
            // 找到事Task程式, 則不再繼續往下找了
            return true;
        }
        return false;
    }

    private static void getWhiteList() {
        try {
            WhitelistExtMapper mapper = SpringBeanFactoryUtil.getBean(WhitelistExtMapper.class);
            List<Whitelist> whitelistList = mapper.selectAll();
            if (CollectionUtils.isNotEmpty(whitelistList)) {
                whitelistList.forEach(t -> _whiteList.put(t.getAssemblyname(), t));
            }
        } catch (Throwable t) {
            LogHelperFactory.getTraceLogger().error(t, t.getMessage());
        }
    }
}
