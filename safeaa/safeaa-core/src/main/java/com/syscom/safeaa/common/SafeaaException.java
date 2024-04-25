package com.syscom.safeaa.common;

import com.syscom.safeaa.enums.SAFEMessageId;

public class SafeaaException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private SAFEMessageId messageId;

    public SAFEMessageId getMessageId() {
		return messageId;
	}

	public void setMessageId(SAFEMessageId messageId) {
		this.messageId = messageId;
	}

	public SafeaaException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new exception with the specified detail message and messageId and 
     * cause. 
     * @param message the detail message.
     * @param messageId msgcode (int value)
     * @param cause the cause.  (A {@code null} value is permitted,
     */
    
    public SafeaaException(String message, int msgcode,Throwable cause ) {
        super(message, cause);
    	this.messageId = SAFEMessageId.fromValue(msgcode);
    }
    
    /**
     * Constructs a new exception with the specified detail message and SAFEMessageId  and 
     * cause. 
     * @param message the detail message.
     * @param messageId SAFEMessageId type of message
     * @param cause the cause.  (A {@code null} value is permitted,
     */
    
    public SafeaaException(String message, SAFEMessageId messageId,Throwable cause ) {
        super(message, cause);
    	this.messageId = messageId;
    }    
	
}
