package com.syscom.fep.notify.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


public class NotifyResponse {
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @JsonProperty("ClientId")
    private String clientId;

    @JsonProperty("EJNo")
    private String eJNo;

    @JsonProperty("TxDaTe")
    private String tXDate;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Message")
    private Map<String,Object> message;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Map<String, Object> getMessage() {
		return message;
	}

	public void setMessage(Map<String, Object> message) {
		this.message = message;
	}
    
}
