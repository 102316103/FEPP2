package com.syscom.fep.common;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class CommonTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}