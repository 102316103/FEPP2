package com.syscom.fep.gateway.agent.fisc;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.gateway.agent.fisc.job.FISCGatewayClientAgentProcessCommandJobConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "spring.fep.gateway.agent.fisc")
@RefreshScope
public class FISCGatewayClientAgentConfiguration {
    @NotNull
    private String cmdStart;
    @NotNull
    private String httpTerminate;
    @NotNull
    private String httpOperate;
    private boolean recordHttpLog = false;
    private boolean printInputStream = false;
    private List<FISCGatewayClientAgentProcessCommandJobConfig> cmdJobConfig;

    public String getCmdStart() {
        return cmdStart;
    }

    public void setCmdStart(String cmdStart) {
        this.cmdStart = cmdStart;
    }

    public String getHttpTerminate() {
        return httpTerminate;
    }

    public void setHttpTerminate(String httpTerminate) {
        this.httpTerminate = httpTerminate;
    }

    public String getHttpOperate() {
        return httpOperate;
    }

    public void setHttpOperate(String httpOperate) {
        this.httpOperate = httpOperate;
    }

    public boolean isRecordHttpLog() {
        return recordHttpLog;
    }

    public void setRecordHttpLog(boolean recordHttpLog) {
        this.recordHttpLog = recordHttpLog;
    }

    public boolean isPrintInputStream() {
        return printInputStream;
    }

    public void setPrintInputStream(boolean printInputStream) {
        this.printInputStream = printInputStream;
    }

    public List<FISCGatewayClientAgentProcessCommandJobConfig> getCmdJobConfig() {
        return cmdJobConfig;
    }

    public void setCmdJobConfig(List<FISCGatewayClientAgentProcessCommandJobConfig> cmdJobConfig) {
        this.cmdJobConfig = cmdJobConfig;
    }

    @PostConstruct
    public void print() {
        LogHelperFactory.getGeneralLogger().info(ConfigurationPropertiesUtil.info(this, "FISCGateway Agent Configuration"));
    }
}
