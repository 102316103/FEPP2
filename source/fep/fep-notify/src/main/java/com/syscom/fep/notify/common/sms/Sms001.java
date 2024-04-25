package com.syscom.fep.notify.common.sms;

import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Sms001Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_PHONE_PARM_NAME;

@Component
public class Sms001<T> extends SenderBase<T> {

    @Autowired
    private Sms001Config smsConfig;

    private static final Map<String, String> replacementMap = new HashMap<String, String>() {{
        // 若有換行的需求，請填入ASCII Code 6 代表換⾏。
        put("\n", String.valueOf((char) 6));
    }};


    @Override
    public void send(Map<String, T> content) throws Exception {
        HttpClient httpClient = new HttpClient();
        Map<String, String> params = new HashMap<>();
        params.put("username", smsConfig.getUsername());                 //發送部門或單位
        params.put("password", smsConfig.getSscode());                   //固定參數
        params.put("dstaddr", (String) content.get(NOTIFY_PHONE_PARM_NAME));    //手機號碼
        params.put("destname", (String) content.get("##destname##"));           //系統名稱
        params.put("dlvtime", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //簡訊預約時間，立即發送
        params.put("vldtime", (String) content.get("##vldtime##"));             //簡訊有效時間
        params.put("smbody", this.replaceBody((String) content.get("Body")));
        params.put("clientID", (String) content.get("ClientId"));
        httpClient.doPost(smsConfig.getDomain(), MediaType.APPLICATION_FORM_URLENCODED, params, StandardCharsets.UTF_8);
    }


    /**
     * 對簡訊內容進行特殊字串的替換
     *
     * @param smbody
     * @return
     * @throws UnsupportedEncodingException
     */
    private String replaceBody(String smbody) throws UnsupportedEncodingException {
        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            smbody = StringUtils.replace(smbody, entry.getKey(), entry.getValue());
        }
        return smbody;
    }
}
