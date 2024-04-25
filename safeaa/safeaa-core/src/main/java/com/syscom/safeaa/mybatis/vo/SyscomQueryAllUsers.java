package com.syscom.safeaa.mybatis.vo;

import java.io.Serializable;
import java.util.Date;

public class SyscomQueryAllUsers implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer UserId;
	private String LogonId;
	private String UserName;
	private String EmployeeId;
	private String IDNO;
	private String Birthday;
	private Date EffectDate;
	private Date ExpiredDate;
	private String EmailAddress;
	private Integer UpdateUserId;
	private Date UpdateTime;
	private Date LogOnTime;
	private String LogOnIP;
	private Short IsLogOn;
	private Short LogOnChangeSscode;
	private Date LastSscodeChangeTime;
	private Integer SscodeErrorCount;
	private Date LastLogOnTime;
	private String LastLogOnIP;
	private Date LastLogoutTime;
	private String Sscode;
	private Integer SscodeFormat;
	private String SscodeSalt;
	private String PKISerialNo;
	private String SscodeHintQuestion;
	private String SscodeHintAnswer;
	private Integer FailedPswAnsAttemptCount;
	private Short IsLockout;
	private Date LastLockoutTime;
	private Date FailedPswAttemptTime;
	private Date FailedPswAnsAttemptTime;

	public Integer getUserId() {
		return UserId;
	}

	public void setUserId(Integer userId) {
		UserId = userId;
	}

	public String getLogonId() {
		return LogonId;
	}

	public void setLogonId(String logonId) {
		LogonId = logonId;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getEmployeeId() {
		return EmployeeId;
	}

	public void setEmployeeId(String employeeId) {
		EmployeeId = employeeId;
	}

	public String getIDNO() {
		return IDNO;
	}

	public void setIDNO(String IDNO) {
		this.IDNO = IDNO;
	}

	public String getBirthday() {
		return Birthday;
	}

	public void setBirthday(String birthday) {
		Birthday = birthday;
	}

	public Date getEffectDate() {
		return EffectDate;
	}

	public void setEffectDate(Date effectDate) {
		EffectDate = effectDate;
	}

	public Date getExpiredDate() {
		return ExpiredDate;
	}

	public void setExpiredDate(Date expiredDate) {
		ExpiredDate = expiredDate;
	}

	public String getEmailAddress() {
		return EmailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		EmailAddress = emailAddress;
	}

	public Integer getUpdateUserId() {
		return UpdateUserId;
	}

	public void setUpdateUserId(Integer updateUserId) {
		UpdateUserId = updateUserId;
	}

	public Date getUpdateTime() {
		return UpdateTime;
	}

	public void setUpdateTime(Date updateTime) {
		UpdateTime = updateTime;
	}

	public Date getLogOnTime() {
		return LogOnTime;
	}

	public void setLogOnTime(Date logOnTime) {
		LogOnTime = logOnTime;
	}

	public String getLogOnIP() {
		return LogOnIP;
	}

	public void setLogOnIP(String logOnIP) {
		LogOnIP = logOnIP;
	}

	public Short getIsLogOn() {
		return IsLogOn;
	}

	public void setIsLogOn(Short isLogOn) {
		IsLogOn = isLogOn;
	}

	public Short getLogOnChangeSscode() {
		return LogOnChangeSscode;
	}

	public void setLogOnChangeSscode(Short logOnChangeSscode) {
		LogOnChangeSscode = logOnChangeSscode;
	}

	public Date getLastSscodeChangeTime() {
		return LastSscodeChangeTime;
	}

	public void setLastSscodeChangeTime(Date lastSscodeChangeTime) {
		LastSscodeChangeTime = lastSscodeChangeTime;
	}

	public Integer getSscodeErrorCount() {
		return SscodeErrorCount;
	}

	public void setSscodeErrorCount(Integer sscodeErrorCount) {
		SscodeErrorCount = sscodeErrorCount;
	}

	public Date getLastLogOnTime() {
		return LastLogOnTime;
	}

	public void setLastLogOnTime(Date lastLogOnTime) {
		LastLogOnTime = lastLogOnTime;
	}

	public String getLastLogOnIP() {
		return LastLogOnIP;
	}

	public void setLastLogOnIP(String lastLogOnIP) {
		LastLogOnIP = lastLogOnIP;
	}

	public Date getLastLogoutTime() {
		return LastLogoutTime;
	}

	public void setLastLogoutTime(Date lastLogoutTime) {
		LastLogoutTime = lastLogoutTime;
	}

	public String getSscode() {
		return Sscode;
	}

	public void setSscode(String sscode) {
		Sscode = sscode;
	}

	public Integer getSscodeFormat() {
		return SscodeFormat;
	}

	public void setSscodeFormat(Integer sscodeFormat) {
		SscodeFormat = sscodeFormat;
	}

	public String getSscodeSalt() {
		return SscodeSalt;
	}

	public void setSscodeSalt(String sscodeSalt) {
		SscodeSalt = sscodeSalt;
	}

	public String getPKISerialNo() {
		return PKISerialNo;
	}

	public void setPKISerialNo(String PKISerialNo) {
		this.PKISerialNo = PKISerialNo;
	}

	public String getSscodeHintQuestion() {
		return SscodeHintQuestion;
	}

	public void setSscodeHintQuestion(String sscodeHintQuestion) {
		SscodeHintQuestion = sscodeHintQuestion;
	}

	public String getSscodeHintAnswer() {
		return SscodeHintAnswer;
	}

	public void setSscodeHintAnswer(String sscodeHintAnswer) {
		SscodeHintAnswer = sscodeHintAnswer;
	}

	public Integer getFailedPswAnsAttemptCount() {
		return FailedPswAnsAttemptCount;
	}

	public void setFailedPswAnsAttemptCount(Integer failedPswAnsAttemptCount) {
		FailedPswAnsAttemptCount = failedPswAnsAttemptCount;
	}

	public Short getIsLockout() {
		return IsLockout;
	}

	public void setIsLockout(Short isLockout) {
		IsLockout = isLockout;
	}

	public Date getLastLockoutTime() {
		return LastLockoutTime;
	}

	public void setLastLockoutTime(Date lastLockoutTime) {
		LastLockoutTime = lastLockoutTime;
	}

	public Date getFailedPswAttemptTime() {
		return FailedPswAttemptTime;
	}

	public void setFailedPswAttemptTime(Date failedPswAttemptTime) {
		FailedPswAttemptTime = failedPswAttemptTime;
	}

	public Date getFailedPswAnsAttemptTime() {
		return FailedPswAnsAttemptTime;
	}

	public void setFailedPswAnsAttemptTime(Date failedPswAnsAttemptTime) {
		FailedPswAnsAttemptTime = failedPswAnsAttemptTime;
	}
}