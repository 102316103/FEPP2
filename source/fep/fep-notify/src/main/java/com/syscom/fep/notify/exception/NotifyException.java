package com.syscom.fep.notify.exception;

import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_MESSAGE_DESC;

import java.util.Collections;
import java.util.Map;

import com.syscom.fep.notify.enums.NotifyStatusCode;

public class NotifyException extends Exception {
    private static final long serialVersionUID = 1L;
    private String code;
    private String clientId;
    private Map<String, Object> msg;
    
    public String getCode() {
		return code;
	}

	public Map<String, Object> getMsg() {
		return msg;
	}

	public String getClientId() {
		return clientId;
	}

	public NotifyException() {
        super();
    }

    public NotifyException(String message) {
        super(message);
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, message);
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, message);

    }


    public NotifyException(NotifyStatusCode notifyCode, String clientId, String errorMsg) {
        this.clientId = clientId;
        this.code = notifyCode.getCode();
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, errorMsg);
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, errorMsg);
    }

    public NotifyException(NotifyStatusCode notifyCode, String errorMsg) {
        this.code = notifyCode.getCode();
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, errorMsg);
                = Collections.singletonMap(NOTIFY_MESSAGE_DESC, errorMsg);
    }

    public NotifyException(NotifyStatusCode notifyCode, String clientId, Map<String, Object> msg) {
        this.code = notifyCode.getCode();
        this.clientId = clientId;
        this.msg = msg;
    }

    public NotifyException(RuntimeException e, NotifyStatusCode notifyCode, String clientId) {
        this.code = notifyCode.getCode();
        this.clientId = clientId;
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
    }

    public NotifyException(Throwable e, NotifyStatusCode notifyCode, String clientId) {
        this.code = notifyCode.getCode();
        this.clientId = clientId;
        this.msg //= Map.of(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
        = Collections.singletonMap(NOTIFY_MESSAGE_DESC, e.fillInStackTrace().toString());
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
