package com.syscom.fep.gateway;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SyscomFepGatewayApplication.class)
public class GatewayBaseTest {
    protected LogHelper logger = LogHelperFactory.getUnitTestLogger();
}