package com.syscom.fep.common.mail;

public class MailSmtp {
    private String smtp;
    private int port;
    private String account;
    private String sscode;

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSscode() {
        return sscode;
    }

    public void setSscode(String sscode) {
        this.sscode = sscode;
    }
}
