package com.syscom.fep.notify.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotifyRequestContent {
    @JsonProperty("TemplateId")
    private String templateId;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("ParmVars")
    private Map<String, String> parmVars;

    @JsonProperty("ContentIndex")
    private String contentIndex;

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public Map<String, String> getParmVars() {
		return parmVars;
	}

	public void setParmVars(Map<String, String> parmVars) {
		this.parmVars = parmVars;
	}

	public String getContentIndex() {
		return contentIndex;
	}

	public void setContentIndex(String contentIndex) {
		this.contentIndex = contentIndex;
	}
    
}
