package com.syscom.fep.web.form.dbmaintain;

import com.syscom.fep.web.form.BaseForm;

public class UI_070060_FormMain extends BaseForm{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 畫面按鈕由系統判斷是否要顯示
	 */
	private String webType;
	
	/**
	 * 來源通道(查詢條件)
	 */
	private String channel;
	
	/**
	 * 訊息代碼(查詢條件)
	 */
	private String errorCode;
	
	/**
	 * 子系統(查詢條件)
	 */
	private String subSys;
	
	/**
	 * 訊息嚴重性(查詢條件)
	 */
	private String severity;
	
	/**
	 * 訊息簡述(查詢條件)
	 */
	private String shortMsg;

	public String getWebType() {
		return webType;
	}

	public void setWebType(String webType) {
		this.webType = webType;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getSubSys() {
		return subSys;
	}

	public void setSubSys(String subSys) {
		this.subSys = subSys;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getShortMsg() {
		return shortMsg;
	}

	public void setShortMsg(String shortMsg) {
		this.shortMsg = shortMsg;
	}
}
