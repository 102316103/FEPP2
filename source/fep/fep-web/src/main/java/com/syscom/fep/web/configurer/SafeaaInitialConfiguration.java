package com.syscom.fep.web.configurer;

import com.syscom.fep.base.configurer.PKIConfig;
import com.syscom.fep.base.enums.FEPDBName;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.safeaa.configuration.DataSourceSafeaaConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@RefreshScope
public class SafeaaInitialConfiguration implements DataSourceSafeaaConstant {
    @Autowired
    private PKIConfig pkiConfig;

    @Bean(name = BEAN_NAME_DATASOURCE_PROPERTIES)
    public Properties buildProperties() throws Exception {
        Properties properties = new Properties();
        String jndiName = EnvPropertiesUtil.getProperty(DataSourceConstant.CONFIGURATION_PROPERTIES_JNDI_NAME, null);
        if (StringUtils.isNotBlank(jndiName)) {
            properties.put(CONFIGURATION_PROPERTIES_JNDI_NAME, jndiName);
        } else {
            File acctFile = pkiConfig.getPkiAcctFile(FEPDBName.FEPDB);
            File sscodeFile = pkiConfig.getPkiSscodeFile(FEPDBName.FEPDB);
            try (InputStream acctIn = IOUtil.openInputStream(acctFile.getPath());
                 InputStream sscodeIn = IOUtil.openInputStream(sscodeFile.getPath());) {
                Properties acctProp = new Properties();
                Properties sscodeProp = new Properties();
                acctProp.load(acctIn);
                sscodeProp.load(sscodeIn);
                properties.put(CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME, EnvPropertiesUtil.getProperty(DataSourceConstant.CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME, null));
                properties.put(CONFIGURATION_PROPERTIES_JDBC_URL, EnvPropertiesUtil.getProperty(DataSourceConstant.CONFIGURATION_PROPERTIES_JDBC_URL, null));
                properties.put(CONFIGURATION_PROPERTIES_USERNAME, acctProp.getProperty(DataSourceConstant.CONFIGURATION_PROPERTIES_USERNAME));
                properties.put(CONFIGURATION_PROPERTIES_PASSWORD, Jasypt.decrypt(sscodeProp.getProperty(DataSourceConstant.CONFIGURATION_PROPERTIES_PASSWORD)));
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().error(e, "Load PKI file with exception occur, ", e.getMessage());
                // return null;
                throw e; // 直接丟異常出去, 啟動就會失敗
            }
        }
        return properties;
    }
}
