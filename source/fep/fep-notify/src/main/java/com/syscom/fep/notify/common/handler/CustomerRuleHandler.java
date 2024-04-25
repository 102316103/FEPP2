package com.syscom.fep.notify.common.handler;

import org.springframework.stereotype.Service;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.notify.common.CustomerRuleBase;
import com.syscom.fep.notify.dto.request.NotifyRequestContent;

@Service
public class CustomerRuleHandler {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    public boolean proccess(String clazz, NotifyRequestContent templateParmVars) throws Exception {
        try {
            Class<?> clz = Class.forName(clazz);
            CustomerRuleBase customerRule = (CustomerRuleBase) SpringBeanFactoryUtil.getBean(clz);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", customerRule.getClass().getSimpleName(), ".proccess()...");
            return customerRule.process(templateParmVars);

        } catch (Exception e) {
            logger.exceptionMsg(e, clazz + "run failed.");
            throw e;
        }
    }
}
