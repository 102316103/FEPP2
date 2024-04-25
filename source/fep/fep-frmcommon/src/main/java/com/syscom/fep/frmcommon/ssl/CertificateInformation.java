package com.syscom.fep.frmcommon.ssl;

import com.syscom.fep.frmcommon.util.FormatUtil;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateInformation implements Serializable {
    private String fileName;
    private String alias;
    private X509Certificate cert;

    public CertificateInformation() {
    }

    public CertificateInformation(String fileName, String alias, X509Certificate cert) {
        this.fileName = fileName;
        this.alias = alias;
        this.cert = cert;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public void setCert(X509Certificate cert) {
        this.cert = cert;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("憑證檔名:").append(fileName).append("\r\n");
        sb.append("憑證別名:").append(alias).append("\r\n");
        // 獲得憑證版本
        String info = String.valueOf(cert.getVersion());
        sb.append("憑證版本:").append(info).append("\r\n");
        // 獲得憑證序列號
        info = cert.getSerialNumber().toString(16);
        sb.append("憑證序列號:").append(info).append("\r\n");
        // 獲得憑證有效期
        Date beforedate = cert.getNotBefore();
        info = FormatUtil.dateFormat(beforedate);
        sb.append("憑證生效日期:").append(info).append("\r\n");
        Date afterdate = cert.getNotAfter();
        info = FormatUtil.dateFormat(afterdate);
        sb.append("憑證失效日期:").append(info).append("\r\n");
        // 獲得憑證主體信息
        info = cert.getSubjectDN().getName();
        sb.append("憑證擁有者:").append(info).append("\r\n");
        // 獲得憑證頒發者信息
        info = cert.getIssuerDN().getName();
        sb.append("憑證頒發者:").append(info).append("\r\n");
        // 獲得憑證籤名算法名稱
        info = cert.getSigAlgName();
        sb.append("憑證籤名算法:").append(info).append("\r\n");
        return sb.toString();
    }
}
