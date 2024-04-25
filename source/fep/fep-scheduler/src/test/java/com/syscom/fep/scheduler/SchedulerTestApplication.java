package com.syscom.fep.scheduler;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
public class SchedulerTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}