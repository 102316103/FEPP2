package com.syscom.fep.notify.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "fep.notify.notclassified001")
@RefreshScope
@Component
public class NotClassified001Config {
    private String url;
    private String appIds;
    private String classname;
    
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAppIds() {
		return appIds;
	}
	public void setAppIds(String appIds) {
		this.appIds = appIds;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
    
}
