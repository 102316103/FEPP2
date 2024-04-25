package com.syscom.fep.gateway.job.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.util.GatewayCommuHelper;
import com.syscom.fep.scheduler.job.SchedulerJob;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.event.Level;

import javax.annotation.PostConstruct;

/**
 * 透過ATM Service刷新Cache資料
 */
public class ATMGatewayServerReloadDbCacheJob extends SchedulerJob<ATMGatewayServerReloadDbCacheJobConfig> {
    private static final String SPACE = "#EMPTY#";
    private static String atmCertNo, atmCertNoOld;

    @Override
    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.ATMGW.name());
    }

    @PostConstruct
    @Override
    public void init() {
        putMDC();
        super.init();
        this.reloadATMCertNo(new LogData());
    }

    /**
     * 執行任務
     *
     * @param context
     * @param config
     */
    @Override
    protected void executeJob(JobExecutionContext context, ATMGatewayServerReloadDbCacheJobConfig config) throws Exception {
        LogData logData = new LogData();
        this.reloadATMCertNo(logData);
    }

    public String getAtmCertNo() {
        // 如果測試資料取不到, 則回傳真實SYSCONF檔中的值
        String atmCertNoForTesting = this.getJobConfig().getAtmCertNoForTesting();
        if (StringUtils.isNotBlank(atmCertNoForTesting)) return SPACE.equals(atmCertNoForTesting) ? StringUtils.EMPTY : atmCertNoForTesting;
        return atmCertNo;
    }

    public String getAtmCertNoOld() {
        // 如果測試資料取不到, 則回傳真實SYSCONF檔中的值
        String atmCertNoOldForTesting = this.getJobConfig().getAtmCertNoOldForTesting();
        if (StringUtils.isNotBlank(atmCertNoOldForTesting)) return SPACE.equals(atmCertNoOldForTesting) ? StringUtils.EMPTY : atmCertNoOldForTesting;
        return atmCertNoOld;
    }

    private void reloadATMCertNo(LogData logData) {
        GatewayCommuHelper commuHelper = SpringBeanFactoryUtil.getBean(GatewayCommuHelper.class);
        logData.setRemark(StringUtils.join("Before cached data, ATMCertNo:", atmCertNo, ", ATMCertNoOld:", atmCertNoOld));
        FEPBase.logMessage(Level.INFO, logData);
        atmCertNo = commuHelper.getSysconfValueFromFEPATM(logData, (short) 8, "ATMCertNo", FEPConfig.getInstance().getRestfulTimeout());
        atmCertNoOld = commuHelper.getSysconfValueFromFEPATM(logData, (short) 8, "ATMCertNo_Old", FEPConfig.getInstance().getRestfulTimeout());
        logData.setRemark(StringUtils.join("After reload data, ATMCertNo:", atmCertNo, ", ATMCertNoOld:", atmCertNoOld));
        FEPBase.logMessage(Level.INFO, logData);
    }
}
