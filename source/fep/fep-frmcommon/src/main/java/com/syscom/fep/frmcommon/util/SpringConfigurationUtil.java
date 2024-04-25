package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class SpringConfigurationUtil {
    private final LogHelper logger = new LogHelper();
    private ContextRefresher contextRefresher;
    private static final ExecutorService executor = Executors.newFixedThreadPool(1, new SimpleThreadFactory("SpringConfiguration"));

    @EventListener
    public void envListener(EnvironmentChangeEvent event) {
        logger.info("Configuration has been changed, source is [", event.getSource(), "], key is [", StringUtils.join(event.getKeys(), ","), "]");
    }

    @PreDestroy
    public void destroy() {
        logger.trace("SpringConfigurationUtil start to destroy...");
        try {
            executor.shutdown(); // 記得要關閉
            if (executor.awaitTermination(60, TimeUnit.SECONDS))
                logger.trace("SpringConfigurationUtil executor terminate all runnable successful");
            else
                logger.trace("SpringConfigurationUtil executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }

    public void refreshManually() {
        contextRefresher = SpringBeanFactoryUtil.getBean(ContextRefresher.class);
        executor.execute(() -> {
            contextRefresher.refresh();
        });
    }
}
