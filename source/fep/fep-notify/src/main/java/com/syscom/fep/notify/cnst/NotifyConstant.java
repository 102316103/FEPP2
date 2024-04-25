package com.syscom.fep.notify.cnst;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotifyConstant {
    public static final String SYSTEM_VAR_SYMBOL = "#@";        // 系統參數 Symbol
    public static final String INDEPENDEN_VAR_SYMBOL = "##";    // 前端傳送變數 Symbol
    public static final String NOTIFY_TYPE_EMAIL = "M";                 // EMAIL
    public static final String NOTIFY_TYPE_SMS = "S";                   // SMS
    public static final String NOTIFY_TYPE_NOCLASSIFIED = "U";          // 不分類
    public static final String NOTIFY_EMAIL_PARM_NAME = "Email";
    public static final String NOTIFY_PHONE_PARM_NAME = "Phone";
    public static final String NOTIFY_MESSAGE_FAILURES = "Failures";
    public static final String NOTIFY_MESSAGE_CONTENT_TYPE = "Type";
    public static final String NOTIFY_MESSAGE_CONTENT_SUBJECT = "Subject";
    public static final String NOTIFY_MESSAGE_CONTENT_BODY = "Body";
    public static final String NOTIFY_MESSAGE_CONTENT_CLIENTID = "ClientId";
    public static final String NOTIFY_MESSAGE_DESC = "Desc";
    public static final int UPDATE_USER = 0;
}
