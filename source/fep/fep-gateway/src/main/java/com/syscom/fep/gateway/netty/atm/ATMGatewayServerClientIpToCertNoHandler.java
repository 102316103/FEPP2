package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.gateway.job.atm.ATMGatewayServerReloadDbCacheJob;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ATMGatewayServerClientIpToCertNoHandler extends FEPBase {
    @Autowired
    private ATMGatewayServerReloadDbCacheJob atmGatewayServerReloadDbCacheJob;
    private final Map<String, String> clientIpToCertNoMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Boolean> clientIpToIsTryoutAllCertNo = Collections.synchronizedMap(new HashMap<>());

    /**
     * 依次獲取ATMCertNo和ATMCertNoOld
     *
     * @param logData
     * @param clientIp
     * @return
     * @throws Exception
     */
    public String getCertNo(LogData logData, String clientIp) throws Exception {
        logData.setProgramName(StringUtils.join(ProgramName, ".getCertNo"));
        String atmCertNo = atmGatewayServerReloadDbCacheJob.getAtmCertNo();
        String atmCertNoOld = atmGatewayServerReloadDbCacheJob.getAtmCertNoOld();
        if (StringUtils.isBlank(atmCertNo) && StringUtils.isBlank(atmCertNoOld)) {
            logData.setRemark(StringUtils.join("Both ATMCertNo and ATMCertNoOld not setup, reject ATM connection, atmIp = [", clientIp, "], ATMCertNo = [", atmCertNo, "], AtmCertNoOld = [", atmCertNoOld, "]"));
            Exception e = ExceptionUtil.createException(logData.getRemark());
            sendEMS(logData);
            // 2023-12-25 Richard modified for 在ATMCertNo和ATMCertNo_Old都沒有值的情況下, 是只能拒絕所有ATM連線 by Ashiang
            throw e;
        }
        String certNo = this.clientIpToCertNoMap.get(clientIp);
        // 沒有cache表示第一次取, 則取ATMCertNo
        if (StringUtils.isBlank(certNo)) {
            certNo = atmCertNo;
            if (StringUtils.isNotBlank(certNo)) {
                this.clientIpToCertNoMap.put(clientIp, certNo);
                logData.setRemark(StringUtils.join("Get ATMCertNo from SYSCONF, atmIp = [", clientIp, "], ATMCertNo = [", certNo, "]"));
                // 如果AtmCertNoOld不存在, 則表示已經try完所有的alias
                if (StringUtils.isBlank(atmCertNoOld)) {
                    clientIpToIsTryoutAllCertNo.put(clientIp, true); // 表示已經try完所有的alias
                }
            } else {
                // 如果ATMCertNo不存在, 則直接取ATMCertNoOld
                certNo = atmCertNoOld;
                this.clientIpToCertNoMap.put(clientIp, certNo);
                logData.setRemark(StringUtils.join("Get AtmCertNoOld from SYSCONF, atmIp = [", clientIp, "], AtmCertNoOld = [", certNo, "]"));
                clientIpToIsTryoutAllCertNo.put(clientIp, true); // 表示已經try完所有的alias
            }
        }
        // 如果cache中是ATMCertNo, 則表示第一次有取過, 則第二次取ATMCertNoOld
        else if (certNo.equals(atmCertNo)) {
            certNo = atmCertNoOld;
            if (StringUtils.isNotBlank(certNo)) {
                this.clientIpToCertNoMap.put(clientIp, certNo);
                logData.setRemark(StringUtils.join("Get AtmCertNoOld from SYSCONF, atmIp = [", clientIp, "], AtmCertNoOld = [", certNo, "]"));
                clientIpToIsTryoutAllCertNo.put(clientIp, true); // 表示已經try完所有的alias
            } else {
                // 如果ATMCertNoOld不存在, 則清掉cache重新取
                this.clientIpToCertNoMap.remove(clientIp);
                logData.setRemark(StringUtils.join("Clear Cache, atmIp = [", clientIp, "]"));
                logMessage(Level.WARN, logData);
                return this.getCertNo(logData, clientIp);
            }
        }
        // 如果cache中是ATMCertNoOld, 表示都有取過, 則清掉cache重新取
        else if (certNo.equals(atmCertNoOld)) {
            this.clientIpToCertNoMap.remove(clientIp);
            logData.setRemark(StringUtils.join("Clear Cache, atmIp = [", clientIp, "]"));
            logMessage(Level.WARN, logData);
            return this.getCertNo(logData, clientIp);
        }
        logData.setRemark(StringUtils.join(logData.getRemark(), ", IsTryoutAllCertNo = [", clientIpToIsTryoutAllCertNo.get(clientIp), "]"));
        this.logMessage(logData);
        return certNo;
    }


    /**
     * 是否有嘗試所有的ATMCertNo
     *
     * @param clientIp
     * @return
     */
    public boolean isTryoutAllCertNo(String clientIp) {
        Boolean isTryoutAllCertNo = clientIpToIsTryoutAllCertNo.get(clientIp);
        if (isTryoutAllCertNo == null) {
            return true;
        } else if (isTryoutAllCertNo) {
            clientIpToIsTryoutAllCertNo.remove(clientIp);
            LogHelperFactory.getTraceLogger().warn("Clear Cache for clientIpToIsTryoutAllCertNo, atmIp = [", clientIp, "]");
        }
        return isTryoutAllCertNo;
    }

    /**
     * 獲取當前的ATMCertNo並移除
     *
     * @param clientIp
     * @return
     */
    public String getAndRemoveCertNo(String clientIp) {
        try {
            return clientIpToCertNoMap.remove(clientIp);
        } finally {
            LogHelperFactory.getTraceLogger().warn("Clear Cache for clientIpToCertNoMap, atmIp = [", clientIp, "]");
        }
    }
}
