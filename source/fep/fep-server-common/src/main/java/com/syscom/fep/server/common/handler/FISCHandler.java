package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.*;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 本類別為處理財金發動的交易,用來判斷該叫起何支AA
 * 1.寫MessageLog
 * 2.將財金電文拆解
 * 3.判斷要叫那一支AA
 * 4.呼叫AA
 * 5.將AA回傳值寫MessageLog
 *
 * @author Richard
 */
public class FISCHandler extends HandlerBase {
    private static final String ProgramName = StringUtils.join(FISCHandler.class.getSimpleName(), ".");
    private FISCGeneral general;
    private String RClientID;

    public FISCHandler() {}

    @Override
    public String dispatch(FEPChannel channel, String data) {
        return StringUtils.EMPTY;
    }

    /**
     * @param channel
     * @param atmNo
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) throws Exception {
        return null;
    }

    /**
     * 財金發動交易專用
     *
     * @param subSystem
     * @param fiscSubSystem
     * @param channel
     * @param data
     * @return
     */
    public String dispatch(SubSystem subSystem, FISCSubSystem fiscSubSystem, FEPChannel channel, String data) {
        FISCData fData;
        general = new FISCGeneral();
        general.setSubSystem(fiscSubSystem);
        String fiscRes = StringUtils.EMPTY;
        // 2023-11-13 Richard add start
        // 前端沒帶進來就取, 有就直接用
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
        }
        if (StringUtils.isBlank(this.txRquid)) {
            this.txRquid = UUIDUtil.randomUUID(true);
        }
        // 2023-11-13 Richard add end
        // 記MessageLog
        LogData logData = null;
        if (this.logContext == null) {
            logData = new LogData();
            logData.setSubSys(subSystem);
            logData.setChannel(channel);
            logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logData.setEj(this.getEj());
            logData.setTxRquid(this.txRquid);
            logData.setMessage(data);
        } else {
            // 2012/06/18 Modify by Ruling for SendEMS子系統為GW改用FiscService傳進來的subSystem
            this.logContext.setSubSys(subSystem);
            logData = this.logContext;
        }
        logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logMessage(Level.DEBUG, logData);
        try {
            // 拆財金電文Header
            RefBase<FISCHeader> refMsgReq = new RefBase<FISCHeader>(null);
            FEPReturnCode rtn = this.getFISCRequestHeader(fiscSubSystem, data, refMsgReq);
            FISCHeader msgReq = refMsgReq.get();

            // 準備財金交易通用物件
            fData = this.prepareFISCData(subSystem, fiscSubSystem, channel, data);
            fData.setLogContext(logData);
            fData.setMessageFlowType(msgReq.getMessageKind());

            // Get MsgID & MsgCtl
            String msgId = this.getMsgId(msgReq);
            LogHelperFactory.getTraceLogger().trace("FISCHandler GetMsgId ", msgId);

            fData.setMessageID(msgId);
            fData.setStan(msgReq.getSystemTraceAuditNo());
            logData.setMessageId(msgId);
            logData.setStan(msgReq.getSystemTraceAuditNo());

            // add by maxine on 2011/07/12 for 拆解財金電文 Header將 LogData.Stan 給值, 請同時將 LogData.PCODE & LogData.DesBKNO 給值.
            logData.setpCode(msgReq.getProcessingCode());
            logData.setDesBkno(StringUtils.rightPad(msgReq.getTxnDestinationInstituteId(), 7, ' ').substring(0, 3));

            if (StringUtils.isNotBlank(msgReq.getTxnSourceInstituteId()) && msgReq.getTxnSourceInstituteId().length() >= 3) {
                logData.setBkno(msgReq.getTxnSourceInstituteId().substring(0, 3));
            }

            // 2019-11-14 Modify by Ruling for 處理EJ為零時送EMS
            if (this.getEj() == 0) {
                logData.setMessage(data);
                logData.setReturnCode(FEPReturnCode.EJFNOTakeNumberError);
                logData.setExternalCode("EF2217");
                logData.setRemark("EJFNO取號有誤, EJ = 0");
                sendEMS(logData);
                return StringUtils.EMPTY;
            }

            this.messageId = msgId;
            fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
            if (fData.getMsgCtl() == null) {
                fData.setMsgCtl(FEPCache.getMsgctrl("GARBLED"));
            }

            this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            fData.setAaName(fData.getMsgCtl().getMsgctlAaName());
            fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);

            logData.setProgramName(fData.getAaName());
            if (rtn == FEPReturnCode.Normal) {
                // modified by maxine for 不同系統呼叫不同的RunAA
                switch (fiscSubSystem) {
                    case INBK:
                    case EMVIC:
                        fiscRes = this.runAA(msgId, msgReq, fData);
                        break;
                    case CLR:
                    case FCCLR:
                    case OPC:
                        fiscRes = this.runAA(msgId, msgReq, fData);
                        break;
                    case RM:
                        fiscRes = this.runRMAA(msgId, msgReq, fData);
                        break;
                    default:
                        break;
                }
            } else {
                return StringUtils.EMPTY;
            }

            return fiscRes;
        } catch (Throwable e) {
            logData.setProgramException(e);
            sendEMS(logData);
            return fiscRes;
        } finally {
            // 記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logData.setMessage(fiscRes);
            logMessage(Level.DEBUG, logData);
        }
    }

    /**
     * @throws Exception
     */
    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }

    /**
     * add By Maxine on 2011/09/02 for UI_015313 logData加上TxUser
     *
     * @param channel
     * @param data
     * @param txcd
     * @param txuser
     * @return
     */
    public boolean dispatch(FEPChannel channel, INBKGeneral data, String txcd, String txuser) {
        INBKData txINBKData;
        @SuppressWarnings("unused")
        String rtnMsg = StringUtils.EMPTY;
        this.ej = TxHelper.generateEj();
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
        logData.setEj(this.ej);
        logData.setTxRquid(this.txRquid);
        try {
            txINBKData = new INBKData();
            txINBKData.setEj(this.ej);
            txINBKData.setTxRquid(this.txRquid);
            txINBKData.setTxObject(data);
            txINBKData.setTxChannel(channel);
            txINBKData.setTxSubSystem(SubSystem.INBK);
            txINBKData.setTxRequestMessage(StringUtils.EMPTY);
            logData.setMessageId(txcd);
            logData.setTxUser(txuser);

            txINBKData.setLogContext(logData);
            txINBKData.setMessageID(txcd);
            txINBKData.setMsgCtl(FEPCache.getMsgctrl(txcd));
            this.setChannel(txINBKData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            txINBKData.setEj(this.ej);
            txINBKData.setTxRquid(this.txRquid);
            txINBKData.setAaName(txINBKData.getMsgCtl().getMsgctlAaName());
            txINBKData.setTxStatus(txINBKData.getMsgCtl().getMsgctlStatus() == 1);
            rtnMsg = this.runINBKAA(txcd, txINBKData);
        } catch (Throwable e) {
            logData.setProgramException(e);
            sendEMS(logData);
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logData.setMessage(StringUtils.EMPTY);
            logMessage(Level.DEBUG, logData);
        }
        return true;
    }

    @SuppressWarnings("unused")
    private FISCHeader getFISCRequestHeader(FISCSubSystem fiscSubSystem, String data) {
        switch (fiscSubSystem) {
            case INBK:
                this.general.setINBKRequest(new FISC_INBK(data));
                return this.general.getINBKRequest();
            case OPC:
                this.general.setOPCRequest(new FISC_OPC(data));
                this.general.setOPCResponse(new FISC_OPC());
                return this.general.getOPCRequest();
            case RM:
                this.general.setRMRequest(new FISC_RM(data));
                this.general.setRMResponse(new FISC_RM());
                return this.general.getRMRequest();

            default:
                return null;
        }
    }

    private FEPReturnCode getFISCRequestHeader(FISCSubSystem fiscSubSystem, String data, RefBase<FISCHeader> msgReq) {
        FISC_RM tmpFISC_RM;
        FISC_INBK tmpFISC_INBK;
        FISC_OPC tmpFISC_OPC;
        FISC_CLR tmpFISC_CLR;
        FISC_USDCLR tmpFISC_FCCLR;
        FISC_EMVIC tmpFISC_EMVIC;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        switch (fiscSubSystem) {
            case INBK:
                tmpFISC_INBK = new FISC_INBK(data);
                rtnCode = tmpFISC_INBK.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Request) {
                        general.setINBKRequest(tmpFISC_INBK);
                        general.setINBKResponse(new FISC_INBK());
                        general.setINBKConfirm(new FISC_INBK());
                        msgReq.set(general.getINBKRequest());
                    }
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Response) {
                        general.setINBKRequest(new FISC_INBK());
                        general.setINBKResponse(tmpFISC_INBK);
                        general.setINBKConfirm(new FISC_INBK());
                        msgReq.set(general.getINBKResponse());
                    }
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Confirmation) {
                        general.setINBKRequest(new FISC_INBK());
                        general.setINBKResponse(new FISC_INBK());
                        general.setINBKConfirm(tmpFISC_INBK);
                        msgReq.set(general.getINBKConfirm());
                    }
                }
                break;
            case EMVIC:
                tmpFISC_EMVIC = new FISC_EMVIC(data);
                rtnCode = tmpFISC_EMVIC.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Request) {
                        general.setEMVICRequest(tmpFISC_EMVIC);
                        general.setEMVICResponse(new FISC_EMVIC());
                        general.setEMVICConfirm(new FISC_EMVIC());
                        msgReq.set(general.getEMVICRequest());
                    }
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Response) {
                        general.setEMVICRequest(new FISC_EMVIC());
                        general.setEMVICResponse(tmpFISC_EMVIC);
                        general.setEMVICConfirm(new FISC_EMVIC());
                        msgReq.set(general.getEMVICResponse());
                    }
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Confirmation) {
                        general.setEMVICRequest(new FISC_EMVIC());
                        general.setEMVICResponse(new FISC_EMVIC());
                        general.setEMVICConfirm(tmpFISC_EMVIC);
                        msgReq.set(general.getEMVICConfirm());
                    }
                }
                break;
            case OPC:
                tmpFISC_OPC = new FISC_OPC(data);
                rtnCode = tmpFISC_OPC.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_OPC.getMessageKind() == MessageFlow.Request) {
                        general.setOPCRequest(tmpFISC_OPC);
                        general.setOPCResponse(new FISC_OPC());
                        general.setOPCConfirm(new FISC_OPC());
                        msgReq.set(general.getOPCRequest());
                    }
                    if (tmpFISC_OPC.getMessageKind() == MessageFlow.Response) {
                        general.setOPCRequest(new FISC_OPC());
                        general.setOPCResponse(tmpFISC_OPC);
                        general.setOPCConfirm(new FISC_OPC());
                        msgReq.set(general.getOPCResponse());
                    }
                }
                break;
            case RM:
                tmpFISC_RM = new FISC_RM(data);
                rtnCode = tmpFISC_RM.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_RM.getMessageKind() == MessageFlow.Request) {
                        general.setRMRequest(tmpFISC_RM);
                        general.setRMResponse(new FISC_RM());
                        msgReq.set(general.getRMRequest());
                    }
                    if (tmpFISC_RM.getMessageKind() == MessageFlow.Response) {
                        general.setRMRequest(new FISC_RM());
                        general.setRMResponse(tmpFISC_RM);
                        msgReq.set(general.getRMResponse());
                    }
                }
                break;
            case CLR:
                tmpFISC_CLR = new FISC_CLR(data);
                rtnCode = tmpFISC_CLR.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_CLR.getMessageKind() == MessageFlow.Request) {
                        general.setCLRRequest(tmpFISC_CLR);
                        general.setCLRResponse(new FISC_CLR());
                        msgReq.set(general.getCLRRequest());
                    }
                    if (tmpFISC_CLR.getMessageKind() == MessageFlow.Response) {
                        general.setCLRRequest(new FISC_CLR());
                        general.setCLRResponse(tmpFISC_CLR);
                        msgReq.set(general.getCLRResponse());
                    }
                }
                break;
            case FCCLR:
                tmpFISC_FCCLR = new FISC_USDCLR(data);
                rtnCode = tmpFISC_FCCLR.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_FCCLR.getMessageKind() == MessageFlow.Request) {
                        general.setFCCLRRequest(tmpFISC_FCCLR);
                        general.setFCCLRResponse(new FISC_USDCLR());
                        msgReq.set(general.getFCCLRRequest());
                    }
                    if (tmpFISC_FCCLR.getMessageKind() == MessageFlow.Response) {
                        general.setFCCLRRequest(new FISC_USDCLR());
                        general.setFCCLRResponse(tmpFISC_FCCLR);
                        msgReq.set(general.getFCCLRResponse());
                    }
                }
                break;
            default:
                rtnCode = FEPReturnCode.Abnormal;
                break;
        }
        return rtnCode;
    }

    private FISCData prepareFISCData(SubSystem subSystem, FISCSubSystem fiscSubSystem, FEPChannel channel, String data) {
        FISCData fData = new FISCData();
        fData.setTxObject(this.general);
        fData.setTxChannel(channel);
        fData.setTxSubSystem(subSystem);
        fData.setFiscTeleType(fiscSubSystem);
        fData.setTxRequestMessage(data);
        fData.setEj(this.getEj());
        fData.setTxRquid(this.txRquid);
        fData.setRClientID(this.RClientID);
        return fData;
    }

    private String getMsgId(FISCHeader msgReq) {
        // 讀出交易控制檔本筆交易資料
        // INBK MsgID=PCODE+MsgType後2碼
        String msgId = StringUtils.join(msgReq.getProcessingCode(), msgReq.getMessageType().substring(2));
        if ("0202".equals(msgReq.getMessageType()) && "2000".equals(msgReq.getProcessingCode())) {
            msgId = msgReq.getSyncCheckItem();
        }
        return msgId;
    }

    private String runAA(String msgId, FISCHeader msgReq, FISCData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", String.class, FISCHeader.class, FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, msgReq, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private String runRMAA(String msgId, FISCHeader msgReq, FISCData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRmRequestData", String.class, FISCHeader.class, FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, msgReq, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private String runINBKAA(String msgId, INBKData txINBKData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", String.class, INBKData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, txINBKData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    public String getRClientID() {
        return RClientID;
    }

    public void setRClientID(String RClientID) {
        this.RClientID = RClientID;
    }
}
