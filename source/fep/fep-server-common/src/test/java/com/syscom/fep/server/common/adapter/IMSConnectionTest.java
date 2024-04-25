package com.syscom.fep.server.common.adapter;

import com.ibm.ims.connect.ApiProperties;
import com.ibm.ims.connect.Connection;
import com.ibm.ims.connect.ConnectionFactory;
import com.ibm.ims.connect.ImsConnectApiException;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import java.security.Provider;
import java.security.Security;


public class IMSConnectionTest {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Test
    public void test() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setSslEncryptionType(ApiProperties.SSL_ENCRYPTIONTYPE_STRONG);
            connectionFactory.setUseSslConnection(true);

            Connection connection = connectionFactory.getConnection();

            // String sslCertTypeFieldName = "sslCertType";
            // logger.info("change sslCertType before: ", ReflectUtil.getFieldValue(connection, sslCertTypeFieldName, null));
            // ReflectUtil.setFieldValue(connection, sslCertTypeFieldName, KeyManagerFactory.getDefaultAlgorithm()); // SunX509
            // logger.info("change sslCertType after: ", ReflectUtil.getFieldValue(connection, sslCertTypeFieldName, null));

            System.setProperty("java.vendor", "Oracle Corporation");
            System.out.println(System.getProperty("java.vendor"));
            System.out.println(KeyManagerFactory.getDefaultAlgorithm());

            Provider[] providers = Security.getProviders();
            for (Provider provider : providers) {
                System.out.println(provider.getName());
            }
        } catch (ImsConnectApiException e) {
            logger.error(e, e.getMessage());
        }
    }
}
