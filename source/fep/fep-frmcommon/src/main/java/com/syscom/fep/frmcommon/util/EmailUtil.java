package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class EmailUtil {
    public static enum MailPriority {
        // The email has high priority.
        High(1),
        // The email has normal priority.
        Normal(3),
        // The email has lowest priority.
        Lowest(5);

        private int value;

        private MailPriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        /**
         * 根據SMLPARM.SMLPARM_PRIORITY表的值轉換結果
         *
         * @param smlparmPriority
         * @return
         */
        public static MailPriority fromSmlparmPriority(Short smlparmPriority) {
            if (smlparmPriority == null) return Normal;
            switch (smlparmPriority) {
                case 0:
                    return Lowest;
                case 9:
                    return High;
                default:
                    return Normal;
            }
        }
    }

    private EmailUtil() {}

    private static SimpleEmail createSimpleEmail(String smtp, int port, String account, String sscode) {
        if (StringUtils.isBlank(smtp) || port == -1 || StringUtils.isBlank(account)) {
            return null;
        }
        SimpleEmail email = new SimpleEmail();
        email.setHostName(smtp);
        email.setSmtpPort(port);
        email.setAuthentication(account, sscode);
        email.setCharset(StandardCharsets.UTF_8.displayName());
        return email;
    }

    private static HtmlEmail createHtmlEmail(String smtp, int port, String account, String sscode) {
        if (StringUtils.isBlank(smtp) || port == -1 || StringUtils.isBlank(account)) {
            return null;
        }
        HtmlEmail email = new HtmlEmail();
        email.setHostName(smtp);
        email.setSmtpPort(port);
        email.setAuthentication(account, sscode);
        email.setCharset(StandardCharsets.UTF_8.displayName());
        return email;
    }

    public static void sendSimpleEmail(String smtp, int port, String account, String sscode, String from, String to, String cc, String subject, String body) throws Exception {
        Email email = createSimpleEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setMsg(body);
        sendEmail(email, from, to, cc, subject, MailPriority.Normal);
    }

    public static void sendSimpleEmail(String smtp, int port, String account, String sscode, String from, String to, String cc, String subject, String body, MailPriority mailPriority) throws Exception {
        Email email = createSimpleEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setMsg(body);
        sendEmail(email, from, to, cc, subject, mailPriority);
    }

    public static void sendSimpleEmail(String smtp, int port, String account, String sscode, String from, String[] to, String[] cc, String subject, String body) throws Exception {
        Email email = createSimpleEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setMsg(body);
        sendEmail(email, from, to, cc, null, subject, MailPriority.Normal);
    }

    public static void sendSimpleEmail(String smtp, int port, String account, String sscode, String from, String[] to, String[] cc, String[] bcc, String subject, String body, MailPriority mailPriority) throws Exception {
        Email email = createSimpleEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setMsg(body);
        sendEmail(email, from, to, cc, bcc, subject, mailPriority);
    }


    public static void sendHtmlEmail(String smtp, int port, String account, String sscode, String from, String to, String cc, String subject, String html) throws Exception {
        HtmlEmail email = createHtmlEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setHtmlMsg(html);
        sendEmail(email, from, to, cc, subject, MailPriority.Normal);
    }

    public static void sendHtmlEmail(String smtp, int port, String account, String sscode, String from, String to, String cc, String subject, String html, MailPriority mailPriority) throws Exception {
        HtmlEmail email = createHtmlEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setHtmlMsg(html);
        sendEmail(email, from, to, cc, subject, mailPriority);
    }

    public static void sendHtmlEmail(String smtp, int port, String account, String sscode, String from, String[] to, String[] cc, String subject, String html) throws Exception {
        HtmlEmail email = createHtmlEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setHtmlMsg(html);
        sendEmail(email, from, to, cc, null, subject, MailPriority.Normal);
    }

    public static void sendHtmlEmail(String smtp, int port, String account, String sscode, String from, String[] to, String[] cc, String[] bcc, String subject, String html, MailPriority mailPriority) throws Exception {
        HtmlEmail email = createHtmlEmail(smtp, port, account, sscode);
        if (email == null) return;
        email.setHtmlMsg(html);
        sendEmail(email, from, to, cc, bcc, subject, mailPriority);
    }

    private static void sendEmail(Email email, String from, String to, String cc, String subject) throws Exception {
        sendEmail(email, from, to, cc, subject, MailPriority.Normal);
    }

    private static void sendEmail(Email email, String from, String to, String cc, String subject, MailPriority mailPriority) throws Exception {
        sendEmail(email, from, ArrayUtils.toArray(to), StringUtils.isNotBlank(cc) ? ArrayUtils.toArray(cc) : null, null, subject, mailPriority);
    }

    private static void sendEmail(Email email, String from, String[] to, String[] cc, String subject) throws Exception {
        sendEmail(email, from, to, cc, null, subject, MailPriority.Normal);
    }

    private static void sendEmail(Email email, String from, String[] to, String[] cc, String[] bcc, String subject, MailPriority mailPriority) throws Exception {
        if (ArrayUtils.isEmpty(to) || Arrays.stream(to).filter(StringUtils::isBlank).count() == to.length) {
            return;
        }
        try {
            if (email == null) {
                return;
            }
            email.setFrom(from);
            for (String t : to) {
                if (StringUtils.isNotBlank(t)) {
                    List<String> tts = StringUtil.split(t, ',', ';');
                    for (String tt : tts) {
                        email.addTo(tt);
                    }
                }
            }
            if (ArrayUtils.isNotEmpty(cc)) {
                for (String c : cc) {
                    if (StringUtils.isNotBlank(c)) {
                        List<String> ccs = StringUtil.split(c, ',', ';');
                        for (String ccc : ccs) {
                            email.addCc(ccc);
                        }
                    }
                }
            }
            if (ArrayUtils.isNotEmpty(bcc)) {
                for (String bc : bcc) {
                    if (StringUtils.isNotBlank(bc)) {
                        List<String> bcs = StringUtil.split(bc, ',', ';');
                        for (String bbc : bcs) {
                            email.addBcc(bbc);
                        }
                    }
                }
            }
            email.setSubject(subject);
            if (mailPriority != null) {
                email.addHeader("X-Priority", String.valueOf(mailPriority.getValue()));
            }
            email.send();
        } catch (Exception e) {
            String message = StringUtils.join("Send mail from [", from, "] to [", StringUtils.join(to, ","), "] and cc [", StringUtils.join(cc, ","), "] with subject [", subject, "] failed!!!");
            throw ExceptionUtil.createException(e, message);
        }
    }
}
