package com.syscom.safeaa.jdbc;

import com.syscom.safeaa.log.LogHelper;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

public class DynamicDataSource extends AbstractRoutingDataSource {
    private LogHelper logger = new LogHelper();
    private final String dsName;

    private DynamicDataSourceContextHolder contextHolder;

    public DynamicDataSource(String dsName) {
        this.dsName = dsName;
        contextHolder = new DynamicDataSourceContextHolder(this.dsName);
    }

    /**
     * 如果不希望資料源在啟動配置時就加載好，可以定制這個方法，從任何你希望的地方讀取并返回資料源
     * 比如從資料庫、檔案、外部接口等讀取資料源信息，并最終返回一個DataSource實現類對象即可
     */
    @Override
    protected DataSource determineTargetDataSource() {
        return super.determineTargetDataSource();
    }

    /**
     * 如果希望所有資料源在啟動配置時就加載好，這裏通過設置資料源Key值來切換資料，定製這個方法
     *
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        logger.debug("[", this.dsName, "]Determine current DataSource is [", contextHolder.obtainDataSourceType(), "]");
        return contextHolder.obtainDataSourceType();
    }

    /**
     * 設置預設資料源
     *
     * @param defaultDataSource
     */
    public void setDefaultDataSource(Object defaultDataSource) {
        super.setDefaultTargetDataSource(defaultDataSource);
    }

    /**
     * 設置資料源
     *
     * @param dataSources
     */
    public void setDataSources(Map<Object, Object> dataSources) {
        super.setTargetDataSources(dataSources);
        // 將資料源的 key 放到資料源上下文的 key 集合中，用於切換時判斷資料源是否有效
        contextHolder.addDataSourceType(dataSources.keySet());
    }

    /**
     * 切換資料源
     *
     * @throws Exception
     */
    public void switchDataSource() throws Exception {
        contextHolder.switchDataSourceType();
    }

    /**
     * 設置資料源
     *
     * @throws Exception
     */
    public void setDataSourceType(DynamicDataSourceType dynamicDataSourceType) throws Exception {
        contextHolder.setDataSourceType(dynamicDataSourceType);
    }
}
