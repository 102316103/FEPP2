package com.syscom.fep.common.sms;

import com.syscom.fep.frmcommon.net.http.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class SmsSender {
    private static final Map<String, String> replacementMap = new HashMap<String, String>() {{
        // 若有換行的需求，請填入ASCII Code 6 代表換⾏。
        put("\n", String.valueOf((char) 6));
    }};

    @Autowired
    private SmsConfiguration smsConfiguration;

    /**
     * 單筆簡訊發送
     *
     * @param dstaddr 收訊⼈之⼿機號碼，格式為：0912345678
     * @param smbody  簡訊內容
     */
    public void send(String dstaddr, String smbody) throws Exception {
        String url = StringUtils.join("https://", smsConfiguration.getDomain(), "/b2c/mtk/SmSend?CharsetURL=UTF-8");
        Map<String, String> params = new HashMap<>();
        params.put("username", smsConfiguration.getUsername());
        params.put("password", smsConfiguration.getSscode());
        params.put("dstaddr", dstaddr);
        params.put("smbody", this.replaceBody(smbody));
        HttpClient httpClient = new HttpClient();
        httpClient.doPost(url, MediaType.APPLICATION_FORM_URLENCODED, params, StandardCharsets.UTF_8);
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
