package com.syscom.safeaa.enums;

/**
 * @author JenniferYin
 *
 */
public class EMSData {
	private ErrorPriority level;

	private String programName;

	private String errorCode;

	private Throwable ProgramException;

	private String apMessage;
	
	private String errDescription;

	public ErrorPriority getLevel() {
		return level;
	}

	public void setLevel(ErrorPriority level) {
		this.level = level;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Throwable getProgramException() {
		return ProgramException;
	}

	public void setProgramException(Throwable programException) {
		ProgramException = programException;
	}

	public String getApMessage() {
		return apMessage;
	}

	public void setApMessage(String apMessage) {
		this.apMessage = apMessage;
	}

	public String getErrDescription() {
		return errDescription;
	}

	public void setErrDescription(String errDescription) {
		this.errDescription = errDescription;
	}
}
