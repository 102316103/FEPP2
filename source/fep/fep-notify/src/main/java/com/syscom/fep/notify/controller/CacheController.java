package com.syscom.fep.notify.controller;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.notify.service.NotifyRuleService;
import com.syscom.fep.notify.service.NotifyTemplateService;
import com.syscom.fep.notify.service.NotifyRuleSetService;
import com.syscom.fep.notify.service.SystemVarsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheController {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private NotifyTemplateService notifyTemplateService;

    @Autowired
    private NotifyRuleSetService notifyRuleSetService;

    @Autowired
    private NotifyRuleService notifyRuleService;

    @Autowired
    private SystemVarsService systemVarsService;

    @GetMapping("cleanAllCache")
    public void cleanAllCache() {
        notifyTemplateService.cleanNotifyTemplateCache();
        notifyRuleSetService.cleanNotifyRuleSetCache();
        notifyRuleService.cleanNotifyRuleCache();
        systemVarsService.cleanSystemVarsCache();
    }

    @GetMapping("cleanSystemVarsCache")
    public void cleanSystemVarsCache() {
        systemVarsService.cleanSystemVarsCache();
    }

    @GetMapping("cleanRuleSetCache")
    public void cleanRuleSetCache() {
        notifyRuleSetService.cleanNotifyRuleSetCache();
    }

    @GetMapping("cleanNoifyTemplateCache")
    public void cleanNoifyTemplateCache() {
        notifyTemplateService.cleanNotifyTemplateCache();
    }
}