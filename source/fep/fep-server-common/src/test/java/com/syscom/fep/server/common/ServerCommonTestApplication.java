package com.syscom.fep.server.common;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class ServerCommonTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}