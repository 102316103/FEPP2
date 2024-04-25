package com.syscom.fep.common.mail;

import com.syscom.fep.frmcommon.util.EmailUtil;
import com.syscom.fep.frmcommon.util.EmailUtil.MailPriority;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailSender {
    @Autowired
    private MailConfiguration mailConfiguration;

    public void sendSimpleEmail(String from, String to, String cc, String subject, String body) throws Exception {
        sendSimpleEmail(from, to, cc, subject, body, MailPriority.Normal);
    }

    public void sendSimpleEmail(String from, String to, String cc, String subject, String body, MailPriority mailPriority) throws Exception {
        sendSimpleEmail(from, ArrayUtils.toArray(to), StringUtils.isNotBlank(cc) ? ArrayUtils.toArray(cc) : null, subject, body, mailPriority);
    }

    public void sendSimpleEmail(String from, String[] to, String[] cc, String subject, String body) throws Exception {
        sendSimpleEmail(from, to, cc, subject, body, MailPriority.Normal);
    }

    public void sendSimpleEmail(String from, String[] to, String[] cc, String subject, String body, MailPriority mailPriority) throws Exception {
        sendSimpleEmail(from, to, cc, null, subject, body, mailPriority);
    }

    public void sendSimpleEmail(String from, String[] to, String[] cc, String[] bcc, String subject, String body, MailPriority mailPriority) throws Exception {
        EmailUtil.sendSimpleEmail(
                mailConfiguration.getMail().getSmtp(),
                mailConfiguration.getMail().getPort(),
                mailConfiguration.getMail().getAccount(),
                mailConfiguration.getMail().getSscode(),
                from, to, cc, bcc, subject, body, mailPriority);
    }

    public void sendHtmlEmail(String from, String to, String cc, String subject, String html) throws Exception {
        sendHtmlEmail(from, to, cc, subject, html, MailPriority.Normal);
    }

    public void sendHtmlEmail(String from, String to, String cc, String subject, String html, MailPriority mailPriority) throws Exception {
        sendHtmlEmail(from, ArrayUtils.toArray(to), StringUtils.isNotBlank(cc) ? ArrayUtils.toArray(cc) : null, subject, html, mailPriority);
    }

    public void sendHtmlEmail(String from, String[] to, String[] cc, String subject, String html) throws Exception {
        sendHtmlEmail(from, to, cc, subject, html, MailPriority.Normal);
    }

    public void sendHtmlEmail(String from, String[] to, String[] cc, String subject, String html, MailPriority mailPriority) throws Exception {
        sendHtmlEmail(from, to, cc, null, subject, html, mailPriority);
    }

    public void sendHtmlEmail(String from, String[] to, String[] cc, String[] bcc, String subject, String html, MailPriority mailPriority) throws Exception {
        EmailUtil.sendHtmlEmail(
                mailConfiguration.getMail().getSmtp(),
                mailConfiguration.getMail().getPort(),
                mailConfiguration.getMail().getAccount(),
                mailConfiguration.getMail().getSscode(),
                from, to, cc, bcc, subject, html, mailPriority);
    }
}
