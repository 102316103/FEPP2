package com.syscom.fep.invoker;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class InvokerTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}