package com.syscom.fep.server.common.notify;

//import com.syscom.fep.mybatis.model.Smsmsg;

import java.util.HashMap;
import java.util.Map;

public class NotifyData {
    private String txDate;
    private String txTime;
    private int ej;
    private String tbsdyFisc;
    private boolean isAsync;
//    private Smsmsg smsmsg;
    private int smlSeqNoForMail;
    private int smlSeqNoForSMS;
    private int smlSeqNoForApp;
    private String smlType;
    private int smlSeqNoForIPAD;
    private Map<String, String> parameterData;

    public NotifyData() {
        parameterData = new HashMap<>();
    }

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getTxTime() {
		return txTime;
	}

	public void setTxTime(String txTime) {
		this.txTime = txTime;
	}

	public int getEj() {
        return ej;
    }

    public void setEj(int ej) {
        this.ej = ej;
    }

    public String getTbsdyFisc() {
        return tbsdyFisc;
    }

    public void setTbsdyFisc(String tbsdyFisc) {
        this.tbsdyFisc = tbsdyFisc;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

//    public Smsmsg getSmsmsg() {
//        return smsmsg;
//    }
//
//    public void setSmsmsg(Smsmsg smsmsg) {
//        this.smsmsg = smsmsg;
//    }

    public int getSmlSeqNoForMail() {
        return smlSeqNoForMail;
    }

    public void setSmlSeqNoForMail(int smlSeqNoForMail) {
        this.smlSeqNoForMail = smlSeqNoForMail;
    }

    public int getSmlSeqNoForSMS() {
        return smlSeqNoForSMS;
    }

    public void setSmlSeqNoForSMS(int smlSeqNoForSMS) {
        this.smlSeqNoForSMS = smlSeqNoForSMS;
    }

    public int getSmlSeqNoForApp() {
        return smlSeqNoForApp;
    }

    public void setSmlSeqNoForApp(int smlSeqNoForApp) {
        this.smlSeqNoForApp = smlSeqNoForApp;
    }

    public String getSmlType() {
        return smlType;
    }

    public void setSmlType(String smlType) {
        this.smlType = smlType;
    }

    public int getSmlSeqNoForIPAD() {
        return smlSeqNoForIPAD;
    }

    public void setSmlSeqNoForIPAD(int smlSeqNoForIPAD) {
        this.smlSeqNoForIPAD = smlSeqNoForIPAD;
    }

    public Map<String, String> getParameterData() {
        return parameterData;
    }

    public void setParameterData(Map<String, String> parameterData) {
        this.parameterData = parameterData;
    }
}
