package com.syscom.fep.server.common.notify;

public class MailData {
    private String txDate;
    private int ej;
    private int oriEj;
    private String tbsdyFisc;
    private String proj;
    private String email;
    private String fromName;
    private String fromEmail;
    private String subject;
    private String body;
    private String channel;
    private String pgCode;
    private String pCode;
    private String idNo;
    private Short priority;

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

    public String getProj() {
        return proj;
    }

    public void setProj(String proj) {
        this.proj = proj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getPgCode() {
        return pgCode;
    }

    public void setPgCode(String pgCode) {
        this.pgCode = pgCode;
    }

    public String getpCode() {
        return pCode;
    }

    public void setpCode(String pCode) {
        this.pCode = pCode;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }
}
