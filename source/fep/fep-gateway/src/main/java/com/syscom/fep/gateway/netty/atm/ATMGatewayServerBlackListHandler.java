package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import com.syscom.fep.gateway.entity.Gateway;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 用來處理黑名單
 */
public class ATMGatewayServerBlackListHandler extends FEPBase {
    @Autowired
    private ATMGatewayServerConfiguration configuration;
    private final Map<String, ConnectFailedData> atmIpToFailedTimesLimitMap = Collections.synchronizedMap(new HashMap<>());
    private ScheduledFuture<?> future = null;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SimpleThreadFactory(StringUtils.join(Gateway.ATMGW.name(), "-BlackListHandler")));

    @PostConstruct
    public void init() {
        // 每隔1分鐘檢查一次黑名單, 超過clearBlackListInterval分鐘則從黑名單中移除
        future = executor.scheduleAtFixedRate(() -> {
            long currentTimeMillis = System.currentTimeMillis();
            for (Iterator<String> it = atmIpToFailedTimesLimitMap.keySet().iterator(); it.hasNext(); ) {
                String atmIp = it.next();
                ConnectFailedData connectFailedData = atmIpToFailedTimesLimitMap.get(atmIp);
                long diffTimeMillis = currentTimeMillis - connectFailedData.getLastTime().getTimeInMillis();
                if (diffTimeMillis > (long) configuration.getClearBlackListInterval() * 60 * 1000) {
                    it.remove();
                    LogHelperFactory.getTraceLogger().info("Removed from Black List after ", diffTimeMillis, " milliseconds, ATM IP = [", atmIp, "]");
                }
            }
        }, 5000L, 30000, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void destroy() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        LogHelper logger = LogHelperFactory.getTraceLogger();
        try {
            this.executor.shutdown(); // 記得要關閉
            if (this.executor.awaitTermination(60, TimeUnit.SECONDS))
                logger.trace(ProgramName, " executor terminate all runnable successful");
            else
                logger.trace(ProgramName, " executor terminate all runnable timeout occur");
        } catch (Throwable e) {
            logger.warn(e, e.getMessage());
        }
    }

    public void incrementFailedTimes(String atmIp) {
        ConnectFailedData connectFailedData = atmIpToFailedTimesLimitMap.get(atmIp);
        if (connectFailedData == null) {
            connectFailedData = new ConnectFailedData();
            atmIpToFailedTimesLimitMap.put(atmIp, connectFailedData);
        }
        connectFailedData.setLastTime(Calendar.getInstance());
        connectFailedData.accumulateCount();
        if (connectFailedData.getCount() >= configuration.getCheckClientFailedTimesLimit()) {
            LogHelperFactory.getTraceLogger().info("Error Count was >= ", configuration.getCheckClientFailedTimesLimit(), ", ATM IP = [", atmIp, "]");
        }
    }

    public void remove(String atmIp) {
        atmIpToFailedTimesLimitMap.remove(atmIp);
        LogHelperFactory.getTraceLogger().info("Removed from Black List, ATM IP = [", atmIp, "]");
    }

    public boolean exist(String atmIp) {
        ConnectFailedData connectFailedData = atmIpToFailedTimesLimitMap.get(atmIp);
        return connectFailedData != null && connectFailedData.getCount() >= configuration.getCheckClientFailedTimesLimit();
    }

    private class ConnectFailedData {
        private Calendar lastTime;
        private int count;

        public Calendar getLastTime() {
            return lastTime;
        }

        public void setLastTime(Calendar lastTime) {
            this.lastTime = lastTime;
        }

        public int getCount() {
            return count;
        }

        public void accumulateCount() {
            this.count += 1;
        }
    }
}
