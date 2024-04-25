package com.syscom.fep.notify.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.NotifytemplateMapper;
import com.syscom.fep.mybatis.model.Notifytemplate;

@Service
public class NotifyTemplateService {

    private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @CacheEvict(value = "notifyTemplateCache", allEntries = true)
    public void cleanNotifyTemplateCache() {
        // 清除全部的 Cache 不會動到 JPA
        logger.info("清除全部的 NotifyTemplate Cache");
    }


    public void addNotifyTemplate() {
        // 資料新增
    }


    @CacheEvict(value = "notifyTemplateCache", allEntries = true)
    public void updateNotifyTemplate() {
        // 資料庫更新時，要清除全部的 Cache
    }


    @Cacheable(value = "notifyTemplateCache")
    public Notifytemplate getNotifyTemplateById(String inputTemplateId) {
        NotifytemplateMapper notifyTemplateMapper = SpringBeanFactoryUtil.getBean(NotifytemplateMapper.class);
        Notifytemplate notifyTemplate = notifyTemplateMapper.selectByPrimaryKey(inputTemplateId);
        logger.info("根據 templateId:", inputTemplateId, " 獲取 Notifytemplate");
        return notifyTemplate;
    }
}
