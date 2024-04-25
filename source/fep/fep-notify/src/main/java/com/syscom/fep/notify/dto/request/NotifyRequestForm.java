package com.syscom.fep.notify.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotifyRequestForm {
    @JsonProperty("RuleSetId")
    private String ruleSetId;
    @JsonProperty("EJNo")
    private String eJNo;
    @JsonProperty("TxDate")
    private String tXDate;
    @JsonProperty("ClientId")
    private String clientId;

    @JsonProperty("Contents")
    private List<NotifyRequestContent> contents;

	public String getRuleSetId() {
		return ruleSetId;
	}

	public void setRuleSetId(String ruleSetId) {
		this.ruleSetId = ruleSetId;
	}

	public String geteJNo() {
		return eJNo;
	}

	public void seteJNo(String eJNo) {
		this.eJNo = eJNo;
	}

	public String gettXDate() {
		return tXDate;
	}

	public void settXDate(String tXDate) {
		this.tXDate = tXDate;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public List<NotifyRequestContent> getContents() {
		return contents;
	}

	public void setContents(List<NotifyRequestContent> contents) {
		this.contents = contents;
	}
    
}
