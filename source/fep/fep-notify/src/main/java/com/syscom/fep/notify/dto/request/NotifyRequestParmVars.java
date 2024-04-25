package com.syscom.fep.notify.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotifyRequestParmVars {
    @JsonProperty("TemplateId")
    private Long templateId;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("ParmVars")
    private Map<String, String> parmVars;

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public Map<String, String> getParmVars() {
		return parmVars;
	}

	public void setParmVars(Map<String, String> parmVars) {
		this.parmVars = parmVars;
	}
    
}
