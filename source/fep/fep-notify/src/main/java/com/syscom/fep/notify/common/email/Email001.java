package com.syscom.fep.notify.common.email;

import com.syscom.fep.frmcommon.util.EmailUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Email001Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.syscom.fep.notify.cnst.NotifyConstant.NOTIFY_EMAIL_PARM_NAME;

@Component
public class Email001<T> extends SenderBase<T> {
    @Autowired
    private Email001Config emailConfig;

    @Override
    public void send(Map<String, T> content) throws Exception {
        EmailUtil.sendHtmlEmail(emailConfig.getSmtp(), emailConfig.getPort(), emailConfig.getAccount(), emailConfig.getSscode(), emailConfig.getFrom(), (String) content.get(NOTIFY_EMAIL_PARM_NAME), null, (String) content.get("Subject"), (String) content.get("Body"), EmailUtil.MailPriority.High);
    }
}
