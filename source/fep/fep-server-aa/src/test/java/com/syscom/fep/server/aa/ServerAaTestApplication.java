package com.syscom.fep.server.aa;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class ServerAaTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}