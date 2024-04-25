package com.syscom.fep.mybatis.deslog;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class MybatisDeslogTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}
