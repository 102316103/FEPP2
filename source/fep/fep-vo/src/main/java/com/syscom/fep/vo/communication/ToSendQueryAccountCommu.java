package com.syscom.fep.vo.communication;

import com.syscom.fep.base.enums.Protocol;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 發送給FISCGW的請求電文
 *
 * @author Richard
 */
@XStreamAlias("response")
public class ToSendQueryAccountCommu extends BaseXmlCommu {
	
	
	private String idNo;
	private String mobilePhone;
	private String bankCode;
	
    private int timeout;
    private int step;
    @XStreamOmitField
    private Protocol protocol;
    @XStreamOmitField
    private String restfulUrl;
    @XStreamOmitField
    private String host;
    @XStreamOmitField
    private int port;

    public ToSendQueryAccountCommu() {
    	this.idNo = idNo;
    	this.mobilePhone = mobilePhone;
    	this.bankCode = bankCode;
    }

    public ToSendQueryAccountCommu(String message, String stan, int ej, int timeout, String messageId) {
        this.timeout = timeout;
    }


    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }



    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getRestfulUrl() {
        return restfulUrl;
    }

    public void setRestfulUrl(String restfulUrl) {
        this.restfulUrl = restfulUrl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("idNo=").append(this.idNo).append("&");
        sb.append("mobilePhone=").append(this.mobilePhone).append("&");
        sb.append("bankCode=").append(this.bankCode).append("&");
        sb.append("timeout=").append(this.timeout).append("&");
        return sb.toString();
    }
}
