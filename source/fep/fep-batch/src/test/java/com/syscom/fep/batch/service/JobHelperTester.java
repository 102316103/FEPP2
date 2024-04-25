package com.syscom.fep.batch.service;

import com.syscom.fep.batch.BatchBaseTest;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.notify.NotifyHelperTemplateId;
import com.syscom.fep.notify.dto.response.NotifyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JobHelperTester extends BatchBaseTest {
    @Autowired
    private NotifyHelper notifyHelper;

    @Test
    public void sendMail() throws Exception {
        NotifyResponse response = notifyHelper.sendSimpleMail(NotifyHelperTemplateId.BATCH, "richard_yu@email.lingan.com.cn", "這是一封測試郵件, This a test Mail!!!", false);
        // notifyHelper.logNotify(response.getClientId(), null, null);
    }
}
