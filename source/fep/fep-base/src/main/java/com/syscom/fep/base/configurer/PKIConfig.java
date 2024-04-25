package com.syscom.fep.base.configurer;

import com.syscom.fep.base.enums.FEPDBName;
import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.pki")
@RefreshScope
public class PKIConfig {
    private String path;
    private String fileAcdu;
    private String fileAcdp;
    private String dbInstanceName1;
    private String dbInstanceName1Db;
    private String dbInstanceName2;
    private String dbInstanceName2Db;
    private Map<FEPDBName, File> dbNameToPkiAcctFileMap = new HashMap<>();
    private Map<FEPDBName, File> dbNameToPkiSscodeFileMap = new HashMap<>();

    @PostConstruct
    private void init() {
        put(dbInstanceName1, dbInstanceName1Db);
        put(dbInstanceName2, dbInstanceName2Db);
        print();
    }

    private void put(String dbInstanceName, String dbInstanceNameDb) {
        if (StringUtils.isNotBlank(dbInstanceName)) {
            if (StringUtils.isNotBlank(dbInstanceNameDb)) {
                for (String dbName : dbInstanceNameDb.split(",")) {
                    if (StringUtils.isBlank(dbName)) continue;
                    dbNameToPkiAcctFileMap.put(Enum.valueOf(FEPDBName.class, dbName),
                            new File(path, StringUtils.join(Arrays.asList(dbInstanceName, fileAcdu), "/")));
                    dbNameToPkiSscodeFileMap.put(Enum.valueOf(FEPDBName.class, dbName),
                            new File(path, StringUtils.join(Arrays.asList(dbInstanceName, fileAcdp), "/")));
                }
            }
        }
    }

    public File getPkiAcctFile(FEPDBName fepDBName) {
        return dbNameToPkiAcctFileMap.get(fepDBName);
    }

    public File getPkiSscodeFile(FEPDBName fepDBName) {
        return dbNameToPkiSscodeFileMap.get(fepDBName);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFileAcdu(String fileAcdu) {
        this.fileAcdu = fileAcdu;
    }

    public void setFileAcdp(String fileAcdp) {
        this.fileAcdp = fileAcdp;
    }

    public void setDbInstanceName1(String dbInstanceName1) {
        this.dbInstanceName1 = dbInstanceName1;
    }

    public void setDbInstanceName1Db(String dbInstanceName1Db) {
        this.dbInstanceName1Db = dbInstanceName1Db;
    }

    public void setDbInstanceName2(String dbInstanceName2) {
        this.dbInstanceName2 = dbInstanceName2;
    }

    public void setDbInstanceName2Db(String dbInstanceName2Db) {
        this.dbInstanceName2Db = dbInstanceName2Db;
    }

    private void print() {
        Field[] fields = PKIConfig.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("PKI Configuration:\r\n");
            for (Field field : fields) {
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
