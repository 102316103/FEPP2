package com.syscom.fep.notify.model;

public class NotifyContentResponse {
    private String contentIndex;
    private String message;
    private String contentStatus;
    
	public String getContentIndex() {
		return contentIndex;
	}
	public void setContentIndex(String contentIndex) {
		this.contentIndex = contentIndex;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getContentStatus() {
		return contentStatus;
	}
	public void setContentStatus(String contentStatus) {
		this.contentStatus = contentStatus;
	}
    
}
