package com.syscom.fep.notify.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syscom.fep.notify.model.NotifyContentResponse;

public class LogNotifyResponse{
    @JsonProperty("ClientId")
    private String clientId;

    @JsonProperty("EJNo")
    private String eJNo;

    @JsonProperty("TxDaTe")
    private String tXDate;

    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("NotifyContents")
    private List<NotifyContentResponse> notifyContentResponses;

    @JsonProperty("Status")
    private String status;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
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

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public List<NotifyContentResponse> getNotifyContentResponses() {
		return notifyContentResponses;
	}

	public void setNotifyContentResponses(List<NotifyContentResponse> notifyContentResponses) {
		this.notifyContentResponses = notifyContentResponses;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
