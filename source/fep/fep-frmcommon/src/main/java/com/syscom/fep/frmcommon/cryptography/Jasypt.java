package com.syscom.fep.frmcommon.cryptography;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Jasypt {
    private static LogHelper logger = new LogHelper();
    private static StringEncryptor jasyptStringEncryptor = createJasyptStringEncryptor();
    private static final String DefEncryptorKey = "Syscom@123";

    private Jasypt() {
    }

    private static StringEncryptor createJasyptStringEncryptor() {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getEncryptorKey());
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        // config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }

    public static String encrypt(String input, boolean... includeMark) {
        if (ArrayUtils.isNotEmpty(includeMark) && includeMark[0]) {
            return StringUtils.join("ENC(", jasyptStringEncryptor.encrypt(input), ")");
        }
        return jasyptStringEncryptor.encrypt(input);
    }

    public static String decrypt(String input) {
        if (input.startsWith("ENC(") && input.endsWith(")")) {
            return jasyptStringEncryptor.decrypt(StringUtils.substring(input, 4, input.lastIndexOf(")")));
        }
        return jasyptStringEncryptor.decrypt(input);
    }

    public static void loadEncryptorKey(String path) {
        String encryptorKey = null;
        if (StringUtils.isNotBlank(path)) {
            File file = new File(path);
            if (!file.exists()) {
                logger.warn("[loadEncryptorKey]File \"", path, "\" is not exist!!!");
            } else if (!file.canRead()) {
                logger.warn("[loadEncryptorKey]File \"", path, "\" is not readable!!!");
            } else {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(file)) {
                    prop.load(fis);
                } catch (Exception e) {
                    logger.warn(e, "[loadEncryptorKey]File \"", path, "\" load failed!!!");
                }
                encryptorKey = prop.getProperty(getEncryptorKeyName());
            }
        } else {
            // logger.warn("[loadEncryptorKey]File \"", path, "\" is empty!!!");
        }
        if (StringUtils.isBlank(encryptorKey)) {
            encryptorKey = DefEncryptorKey;
        }
        System.setProperty(getEncryptorKeyName(), encryptorKey);
    }

    private static String getEncryptorKeyName() {
        return new String(Base64Utils.decodeFromString("amFzeXB0LmVuY3J5cHRvci5wYXNzd29yZA=="), StandardCharsets.UTF_8);
    }

    private static String getEncryptorKey() {
        String key = System.getProperty(getEncryptorKeyName(), DefEncryptorKey);
        if (StringUtils.isBlank(key)) {
            key = EnvPropertiesUtil.getProperty(getEncryptorKeyName(), StringUtils.EMPTY);
        }
        return key;
    }
}
