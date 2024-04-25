package com.syscom.fep.mybatis.his.configuration;

import com.syscom.fep.base.configurer.PKIConfig;
import com.syscom.fep.base.enums.FEPDBName;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.cryptography.Jasypt;
import com.syscom.fep.frmcommon.jdbc.DataSourceConnectionDetector;
import com.syscom.fep.frmcommon.jdbc.DynamicDataSource;
import com.syscom.fep.frmcommon.jdbc.DynamicDataSourceType;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

@Configuration
@MapperScan(basePackages = {"com.syscom.fep.mybatis.his.mapper", "com.syscom.fep.mybatis.his.ext.mapper"}, annotationClass = Resource.class, sqlSessionTemplateRef = DataSourceHisConstant.BEAN_NAME_SQL_SESSION_TEMPLATE)
@RefreshScope
public class DataSourceHisConfiguration implements DataSourceHisConstant {
    // @Value("${spring.fep.mybatis.his.interceptor.connection.detected.enable:false}")
    // private boolean connectionDetectedEnable;
    @Value("${" + CONFIGURATION_PROPERTIES_JNDI_NAME + ":}")
    private String jndiName;
    // @Value("${" + CONFIGURATION_PROPERTIES_MASTER_JNDI_NAME + ":}")
    // private String jndiNameMaster;
    // @Value("${" + CONFIGURATION_PROPERTIES_SLAVE_JNDI_NAME + ":}")
    // private String jndiNameSlave;
    @Autowired
    private PKIConfig pkiConfig;

    @Bean(name = BEAN_NAME_HIKARI_CONFIG)
    @ConfigurationProperties(prefix = HIKARI_CONFIG_PROPERTIES_PREFIX)
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = BEAN_NAME_DATASOURCE)
    // @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES_PREFIX)
    @RefreshScope
    public DataSource dataSource(@Qualifier(BEAN_NAME_HIKARI_CONFIG) HikariConfig hikariConfig) throws Exception {
        // 先嘗試抓JNDI的設置, 如果沒有則抓JDBC的設置
        if (StringUtils.isNotBlank(this.jndiName)) {
            try {
                JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
                bean.setJndiName(this.jndiName);
                bean.setProxyInterface(DataSource.class);
                bean.setLookupOnStartup(false);
                bean.afterPropertiesSet();
                return DataSourceConnectionDetector.connectionDetect((DataSource) bean.getObject());
            } catch (Exception e) {
                LogHelperFactory.getTraceLogger().exceptionMsg(e, "bind JNDI name = [", this.jndiName, "] failed!!!");
                throw e; // 直接丟異常出去, 啟動就會失敗
            }
        }
        File acctFile = pkiConfig.getPkiAcctFile(FEPDBName.FEPHIS);
        File sscodeFile = pkiConfig.getPkiSscodeFile(FEPDBName.FEPHIS);
        try (InputStream acctIn = IOUtil.openInputStream(acctFile.getPath());
             InputStream sscodeIn = IOUtil.openInputStream(sscodeFile.getPath());) {
            Properties acctProp = new Properties();
            Properties sscodeProp = new Properties();
            acctProp.load(acctIn);
            sscodeProp.load(sscodeIn);
            if (hikariConfig == null) {
                hikariConfig = new HikariConfig();
            }
            hikariConfig.setPoolName(POOL_NAME);
            hikariConfig.setUsername(acctProp.getProperty(CONFIGURATION_PROPERTIES_USERNAME));
            hikariConfig.setPassword(Jasypt.decrypt(sscodeProp.getProperty(CONFIGURATION_PROPERTIES_PASSWORD)));
            hikariConfig.setDriverClassName(EnvPropertiesUtil.getProperty(CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME, null));
            hikariConfig.setJdbcUrl(EnvPropertiesUtil.getProperty(CONFIGURATION_PROPERTIES_JDBC_URL, null));
            printHikariConfig(hikariConfig);
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            return DataSourceConnectionDetector.connectionDetect(hikariDataSource);
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().error(e, "Load PKI file with exception occur, ", e.getMessage());
            // return DataSourceBuilder.create().build();
            throw e; // 直接丟異常出去, 啟動就會失敗
        }
    }

    // @Bean(name = BEAN_NAME_DATASOURCE_MASTER)
    // @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES_PREFIX_MASTER)
    // @RefreshScope
    // public DataSource masterDataSource() throws Exception {
    //     // 先嘗試抓JNDI的設置, 如果沒有則抓JDBC的設置
    //     if (StringUtils.isNotBlank(this.jndiNameMaster)) {
    //         try {
    //             JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
    //             bean.setJndiName(this.jndiNameMaster);
    //             bean.setProxyInterface(DataSource.class);
    //             bean.setLookupOnStartup(false);
    //             bean.afterPropertiesSet();
    //             return (DataSource) bean.getObject();
    //         } catch (Exception e) {
    //             LogHelperFactory.getTraceLogger().exceptionMsg(e, "bind JNDI name = [", this.jndiNameMaster, "] failed!!!");
    //             throw e; // 直接丟異常出去, 啟動就會失敗
    //         }
    //     }
    //     return DataSourceBuilder.create().build();
    // }
    //
    // @Bean(name = BEAN_NAME_DATASOURCE_SLAVE)
    // @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES_PREFIX_SLAVE)
    // @RefreshScope
    // public DataSource slaveDataSource() throws Exception {
    //     // 先嘗試抓JNDI的設置, 如果沒有則抓JDBC的設置
    //     if (StringUtils.isNotBlank(this.jndiNameSlave)) {
    //         try {
    //             JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
    //             bean.setJndiName(this.jndiNameSlave);
    //             bean.setProxyInterface(DataSource.class);
    //             bean.setLookupOnStartup(false);
    //             bean.afterPropertiesSet();
    //             return (DataSource) bean.getObject();
    //         } catch (Exception e) {
    //             LogHelperFactory.getTraceLogger().exceptionMsg(e, "bind JNDI name = [", this.jndiNameSlave, "] failed!!!");
    //             throw e; // 直接丟異常出去, 啟動就會失敗
    //         }
    //     }
    //     return DataSourceBuilder.create().build();
    // }
    //
    // @Bean(BEAN_NAME_DATASOURCE_DYNAMIC)
    // @RefreshScope
    // public DataSource dynamicDataSource(@Qualifier(BEAN_NAME_DATASOURCE) DataSource dataSource,
    //                                     @Qualifier(BEAN_NAME_DATASOURCE_MASTER) DataSource masterDataSource,
    //                                     @Qualifier(BEAN_NAME_DATASOURCE_SLAVE) DataSource slaveDataSource) {
    //     // 如果沒有設置master/slave, 則預設取single設置
    //     if (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_JNDI_NAME)
    //             ||
    //             (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_DRIVER_CLASS_NAME)
    //                     && EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_JDBC_URL))) {
    //     return dataSource;
    //     }
    //     // 如果有設置主從DataSource, 則採用動態DataSource
    //     DynamicDataSource dynamicDataSource = new DynamicDataSource(FEPDBName.DESDB.name());
    //     // 將 master 資料源作為預設指定的資料源
    //     dynamicDataSource.setDefaultDataSource(masterDataSource);
    //     // 將 master 和 slave 資料源作為指定的資料源
    //     Map<Object, Object> dataSourceMap = new HashMap<>(2);
    //     dataSourceMap.put(DynamicDataSourceType.MASTER, masterDataSource);
    //     // 如果有設置slave, 則put
    //     if (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_SLAVE_JNDI_NAME)
    //             ||
    //             (EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_SLAVE_DRIVER_CLASS_NAME)
    //                     && EnvPropertiesUtil.hasProperty(CONFIGURATION_PROPERTIES_SLAVE_JDBC_URL))) {
    //         // 如果Slave DataSource有配置, 則放到DynamicDataSource中
    //         dataSourceMap.put(DynamicDataSourceType.SLAVE, slaveDataSource);
    //     }
    //     dynamicDataSource.setDataSources(dataSourceMap);
    //     return dynamicDataSource;
    // }

    @Bean(name = BEAN_NAME_SQL_SESSION_FACTORY)
    @RefreshScope
    public SqlSessionFactory testSqlSessionFactory(@Qualifier(BEAN_NAME_DATASOURCE) DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(
                ArrayUtils.addAll(
                        new PathMatchingResourcePatternResolver().getResources("classpath*:com/syscom/fep/mybatis/his/mapper/xml/*.xml"),
                        new PathMatchingResourcePatternResolver().getResources("classpath*:com/syscom/fep/mybatis/his/ext/mapper/xml/*.xml")));
        bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml"));
        // 增加攔截器
        List<Interceptor> interceptorList = new ArrayList<Interceptor>();
        // if (dataSource instanceof DynamicDataSource && this.connectionDetectedEnable) {
        //     HisConnectionDetectedInterceptor hisConnectionDetectedInterceptor = new HisConnectionDetectedInterceptor();
        //     interceptorList.add(hisConnectionDetectedInterceptor);
        // }
        if (interceptorList.size() > 0) {
            Interceptor[] interceptors = new Interceptor[interceptorList.size()];
            interceptorList.toArray(interceptors);
            bean.setPlugins(interceptors);
        }
        return bean.getObject();
    }

    @Bean(name = BEAN_NAME_SQL_SESSION_TEMPLATE)
    @RefreshScope
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier(BEAN_NAME_SQL_SESSION_FACTORY) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = BEAN_NAME_TRANSACTION_MANAGER)
    public DataSourceTransactionManager testTransactionManager(@Qualifier(BEAN_NAME_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = BEAN_NAME_TRANSACTION_TEMPLATE)
    public TransactionTemplate testTransactionTemplate(@Qualifier(BEAN_NAME_TRANSACTION_MANAGER) DataSourceTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @PostConstruct
    public void print() {
        printConstant();
        Field[] fields = DataSourceHisConfiguration.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("Data Source ").append(DS_NAME.toUpperCase()).append(" Configuration:\r\n");
            for (Field field : fields) {
                if ("pkiConfig".equals(field.getName())) continue;
                ReflectionUtils.makeAccessible(field);
                Value annotation = field.getAnnotation(Value.class);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat));
                if (annotation != null) {
                    sb.append(annotation.value().substring(annotation.value().indexOf("${") + 2, annotation.value().contains(":") ? annotation.value().indexOf(":") : annotation.value().length() - 1));
                } else {
                    ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
                    if (annotation2 != null) {
                        String prefix = annotation2.prefix();
                        if (StringUtils.isNotBlank(prefix)) {
                            sb.append(prefix).append(".");
                        }
                    }
                    sb.append(field.getName());
                }
                sb.append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
            }
            LogHelperFactory.getGeneralLogger().info(sb.toString());
        }
    }
}