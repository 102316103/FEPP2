package com.syscom.fep.notify.common.handler;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Email001Config;

@Service
public class NotifyEmailHandler {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private Email001Config email001Config;

    public void send(Map<String, String> content) throws Exception {
        try {
            Class<?> clz = Class.forName(email001Config.getClassname());
            SenderBase sender = (SenderBase) SpringBeanFactoryUtil.getBean(clz);
            logger.info("start to envoke ", sender.getClass().getSimpleName(), ".send()...");
            sender.send(content);
        } catch (Exception e) {
            e.printStackTrace();
            logger.exceptionMsg(e,  email001Config.getClassname() + " - send Exception =" + e.getMessage());
            throw e;
        }
    }
}
