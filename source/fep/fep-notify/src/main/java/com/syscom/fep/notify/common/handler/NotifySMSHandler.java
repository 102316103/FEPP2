package com.syscom.fep.notify.common.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Sms001Config;

@Service

public class NotifySMSHandler {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private Sms001Config smsConfig;

    public void send(Map<String, String> content) throws Exception {
        try {
            Class<?> clz = Class.forName(smsConfig.getClassname());
            SenderBase sender = (SenderBase) SpringBeanFactoryUtil.getBean(clz);
            logger.info("start to envoke ", sender.getClass().getSimpleName(), ".send()...");
            sender.send(content);
        } catch (Exception e) {
            e.printStackTrace();
            logger.exceptionMsg(e, smsConfig.getClassname() + " - send Exception =" + e.getMessage());
            throw e;
        }
    }

}
