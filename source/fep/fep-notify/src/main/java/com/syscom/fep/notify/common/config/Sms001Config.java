package com.syscom.fep.notify.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fep.notify.sms001")
@RefreshScope
@Component
public class Sms001Config {
    private String domain;
    private String username;
    private String sscode;
    private String classname;
    
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getSscode() {
		return sscode;
	}
	public void setSscode(String sscode) {
		this.sscode = sscode;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
    
}
