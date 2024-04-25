package com.syscom.fep.common.mail;

import com.syscom.fep.common.CommonBaseTest;
import com.syscom.fep.frmcommon.util.EmailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MailSenderTest extends CommonBaseTest {
    @Autowired
    private MailSender mailSender;

    @Test
    public void testSend() throws Exception {
        mailSender.sendSimpleEmail(
                "fep2022@qq.com",
                "richard_yu@email.lingan.com.cn",
                null,
                "這是一封測試郵件(High)",
                "這是一封測試郵件(High)",
                EmailUtil.MailPriority.High);
        mailSender.sendSimpleEmail(
                "fep2022@qq.com",
                "richard_yu@email.lingan.com.cn",
                null,
                "這是一封測試郵件(Normal)",
                "這是一封測試郵件(Normal)",
                EmailUtil.MailPriority.Normal);
        mailSender.sendSimpleEmail(
                "fep2022@qq.com",
                "richard_yu@email.lingan.com.cn",
                null,
                "這是一封測試郵件(Lowest)",
                "這是一封測試郵件(Lowest)",
                EmailUtil.MailPriority.Lowest);
    }
}
