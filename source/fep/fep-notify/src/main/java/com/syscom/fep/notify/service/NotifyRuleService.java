package com.syscom.fep.notify.service;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.NotifyruleMapper;
import com.syscom.fep.mybatis.model.Notifyrule;
import com.syscom.fep.notify.exception.NotifyException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class NotifyRuleService {
    private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @CacheEvict(value = "notifyRuleCache", allEntries = true)
    public void cleanNotifyRuleCache() {
        // 清除全部的 Cache 不動到 JPA
        logger.info("清除全部的 NotifyRule Cache");
    }

    @Cacheable(value = "notifyRuleCache")
    public Notifyrule getNotifyRuleById(Long inputRuleId) throws NotifyException {
        // 資料從 DB 取得
        NotifyruleMapper notifyruleMapper = SpringBeanFactoryUtil.getBean(NotifyruleMapper.class);
        Notifyrule notifyrule = notifyruleMapper.selectByPrimaryKey(inputRuleId);
        logger.info("根據 ruleId:", inputRuleId, "獲取 Rule");
        return notifyrule;
    }

}
