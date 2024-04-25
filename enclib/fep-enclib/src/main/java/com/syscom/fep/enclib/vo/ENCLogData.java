package com.syscom.fep.enclib.vo;

import com.syscom.fep.enclib.enums.ENCMessageFlow;
import com.syscom.fep.enclib.enums.ENCProgramFlow;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.io.Serializable;

@XStreamAlias("enclogData")
public class ENCLogData implements Serializable {
    private static final long serialVersionUID = 4220665798683959627L;
    private String channel;
    private String subSys;
    private String messageId;
    private String message;
    private String programName;
    private ENCMessageFlow messageFlowType;
    private ENCProgramFlow programFlowType;
    private String stan;
    private String atmNo;
    private String atmSeq;
    private String txDate;
    private String troutBank;
    private String troutActno;
    private String trinBank;
    private String trinActno;
    private int ej;
    private String remark;
    private String bkno;
    @XStreamOmitField
    private Exception programException;
    private String externalCode;
    private String responseMessage;
    private boolean notification;
    private int step;
    private String messageGroup;
    private String responsible;
    private String systemId;
    private String hostIp;
    private String hostName;
    private String txRquid;
    private String app;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubSys() {
        return subSys;
    }

    public void setSubSys(String subSys) {
        this.subSys = subSys;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public ENCMessageFlow getMessageFlowType() {
        return messageFlowType;
    }

    public void setMessageFlowType(ENCMessageFlow messageFlowType) {
        this.messageFlowType = messageFlowType;
    }

    public ENCProgramFlow getProgramFlowType() {
        return programFlowType;
    }

    public void setProgramFlowType(ENCProgramFlow programFlowType) {
        this.programFlowType = programFlowType;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getAtmNo() {
        return atmNo;
    }

    public void setAtmNo(String atmNo) {
        this.atmNo = atmNo;
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getTroutBank() {
        return troutBank;
    }

    public void setTroutBank(String troutBank) {
        this.troutBank = troutBank;
    }

    public String getTroutActno() {
        return troutActno;
    }

    public void setTroutActno(String troutActno) {
        this.troutActno = troutActno;
    }

    public String getTrinBank() {
        return trinBank;
    }

    public void setTrinBank(String trinBank) {
        this.trinBank = trinBank;
    }

    public String getTrinActno() {
        return trinActno;
    }

    public void setTrinActno(String trinActno) {
        this.trinActno = trinActno;
    }

    public int getEj() {
        return ej;
    }

    public void setEj(int ej) {
        this.ej = ej;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBkno() {
        return bkno;
    }

    public void setBkno(String bkno) {
        this.bkno = bkno;
    }

    public Exception getProgramException() {
        return programException;
    }

    public void setProgramException(Exception programException) {
        this.programException = programException;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getMessageGroup() {
        return messageGroup;
    }

    public void setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTxRquid() {
        return txRquid;
    }

    public void setTxRquid(String txRquid) {
        this.txRquid = txRquid;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}