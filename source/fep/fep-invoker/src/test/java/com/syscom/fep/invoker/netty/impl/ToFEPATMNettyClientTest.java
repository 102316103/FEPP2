package com.syscom.fep.invoker.netty.impl;

import com.syscom.fep.invoker.InvokerBaseTest;
import com.syscom.fep.invoker.SimpleNettyClientFactory;
import com.syscom.fep.vo.communication.ToFEPATMCommuAtmmstr;
import com.syscom.fep.vo.enums.ClientType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ToFEPATMNettyClientTest extends InvokerBaseTest {
    @Autowired
    private SimpleNettyClientFactory simpleNettyClientFactory;

    @Test
    public void send() throws Exception {
        final ToFEPATMCommuAtmmstr request = new ToFEPATMCommuAtmmstr();
        request.setAtmIp("127.0.0.1");
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        simpleNettyClientFactory.sendReceive(ClientType.TO_FEP_ATM, request, 5000);
                        sleep(1000L);
                    } catch (Exception e) {
                        UnitTestLogger.error(e, e.getMessage());
                    }
                }
            }
        }.start();
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        simpleNettyClientFactory.sendReceive(ClientType.TO_FEP_ATM, request, 5000);
                        sleep(1000L);
                    } catch (Exception e) {
                        UnitTestLogger.error(e, e.getMessage());
                    }
                }
            }
        }.start();
        Thread.sleep(Long.MAX_VALUE);
    }
}
