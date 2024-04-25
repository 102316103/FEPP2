package com.syscom.fep.server.common.notify;

public class IpadNotifyData {
    private String txDate;
    private int ej;
    private int oriEj;
    private String tbsdyFisc;
    private String message;
    private String url;
    private String apiKey;

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public int getEj() {
        return ej;
    }

    public void setEj(int ej) {
        this.ej = ej;
    }

    public int getOriEj() {
        return oriEj;
    }

    public void setOriEj(int oriEj) {
        this.oriEj = oriEj;
    }

    public String getTbsdyFisc() {
        return tbsdyFisc;
    }

    public void setTbsdyFisc(String tbsdyFisc) {
        this.tbsdyFisc = tbsdyFisc;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
