package com.syscom.fep.gateway.job.atm;

import com.syscom.fep.scheduler.job.SchedulerJobConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "spring.fep.gateway.job.atm.reload-db-cache")
@RefreshScope
public class ATMGatewayServerReloadDbCacheJobConfig extends SchedulerJobConfig {
    /**
     * 僅用於測試
     */
    private String atmCertNoForTesting, atmCertNoOldForTesting;

    public String getAtmCertNoForTesting() {
        return atmCertNoForTesting;
    }

    public void setAtmCertNoForTesting(String atmCertNoForTesting) {
        this.atmCertNoForTesting = atmCertNoForTesting;
    }

    public String getAtmCertNoOldForTesting() {
        return atmCertNoOldForTesting;
    }

    public void setAtmCertNoOldForTesting(String atmCertNoOldForTesting) {
        this.atmCertNoOldForTesting = atmCertNoOldForTesting;
    }
}
