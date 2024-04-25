package com.syscom.fep.frmcommon;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class FrmcommonTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}