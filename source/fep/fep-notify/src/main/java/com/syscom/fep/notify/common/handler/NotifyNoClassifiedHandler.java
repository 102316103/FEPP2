package com.syscom.fep.notify.common.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.NotClassified001Config;

@Service
public class NotifyNoClassifiedHandler {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private NotClassified001Config notClassified001Config;

    public void send(Map<String, String> content) throws Exception {
        try {
            Class<?> clz = Class.forName(notClassified001Config.getClassname());
            SenderBase sender = (SenderBase) SpringBeanFactoryUtil.getBean(clz);
            logger.info("start to envoke ", sender.getClass().getSimpleName(), ".send()...");
            sender.send(content);
        } catch (Exception e) {
            e.printStackTrace();
            logger.exceptionMsg(e,  notClassified001Config.getClassname() + " - send Exception=" + e.getMessage());
            throw e;
        }
    }

}
