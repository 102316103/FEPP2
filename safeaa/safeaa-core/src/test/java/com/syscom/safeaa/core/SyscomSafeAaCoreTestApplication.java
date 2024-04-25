package com.syscom.safeaa.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SyscomSafeAaCoreTestApplication {
    private final static Logger log= LoggerFactory.getLogger(SyscomSafeAaCoreTestApplication.class);

    public static void main(String[] args) {
        log.info("test slf4j is ok");
        SpringApplication.run(SyscomSafeAaCoreTestApplication.class, args);
    }
}