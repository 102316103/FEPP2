package com.syscom.safeaa.jdbc;

import com.syscom.safeaa.log.LogHelper;
import com.syscom.safeaa.utils.ExceptionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DynamicDataSourceContextHolder {
    private LogHelper logger = new LogHelper();

    /**
     * 將master作為預設資料源
     */
    private final AtomicReference<DynamicDataSourceType> dataSourceTypeAtomicReference = new AtomicReference<DynamicDataSourceType>(DynamicDataSourceType.MASTER);

    /**
     * 資料源的key集合，用於切換時判斷資料源是否存在
     */
    public final List<Object> dataSourceTypeList = Collections.synchronizedList(new ArrayList<>());

    private final String dsName;

    public DynamicDataSourceContextHolder(String dsName) {
        this.dsName = dsName;
    }

    /**
     * 切換資料源
     *
     * @throws Exception
     */
    public void switchDataSourceType() throws Exception {
        this.setDataSourceType(null);
    }

    /**
     * 設置資料源
     *
     * @param dynamicDataSourceType
     * @throws Exception
     */
    public void setDataSourceType(DynamicDataSourceType dynamicDataSourceType) throws Exception {
        // 如果傳入的為null, 則自動切換下一個
        if (dynamicDataSourceType == null) {
            DynamicDataSourceType current = obtainDataSourceType();
            int startIndex = current.ordinal() + 1;
            DynamicDataSourceType[] dataSourceTypes = DynamicDataSourceType.values();
            if (startIndex == dataSourceTypes.length) {
                startIndex = 0;
            }
            dynamicDataSourceType = dataSourceTypes[startIndex];
        }
        if (containDataSourceKey(dynamicDataSourceType)) {
            logger.debug("[", this.dsName, "]Switch DataSource to [", dynamicDataSourceType, "]");
            dataSourceTypeAtomicReference.set(dynamicDataSourceType);
        } else {
            throw ExceptionUtil.createException("[", this.dsName, "]Canot switch DynamicDataSourceType = [", dynamicDataSourceType, "], cause not register!!!");
        }
    }

    /**
     * 獲取資料源
     *
     * @return
     */
    public DynamicDataSourceType obtainDataSourceType() {
        return dataSourceTypeAtomicReference.get();
    }

    /**
     * 重置資料源
     */
    public void resetDataSourceType() {
        dataSourceTypeAtomicReference.set(DynamicDataSourceType.MASTER);
    }

    /**
     * 判斷是否包含資料源
     *
     * @param key 資料源key
     * @return
     */
    public boolean containDataSourceKey(DynamicDataSourceType key) {
        return dataSourceTypeList.contains(key);
    }

    /**
     * 增加資料源keys
     *
     * @param keys
     * @return
     */
    public boolean addDataSourceType(Collection<? extends Object> keys) {
        return dataSourceTypeList.addAll(keys);
    }
}
