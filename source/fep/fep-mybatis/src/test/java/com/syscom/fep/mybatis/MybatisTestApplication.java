package com.syscom.fep.mybatis;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class MybatisTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}
