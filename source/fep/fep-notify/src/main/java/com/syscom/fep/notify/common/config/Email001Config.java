package com.syscom.fep.notify.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fep.notify.email001")
@RefreshScope
@Component
public class Email001Config {
    private String smtp;
    private int port;
    private String account;
    private String sscode;
    private String from;
    private String classname;
    
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
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
    
}
