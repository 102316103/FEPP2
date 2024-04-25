package com.syscom.fep.server;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.vo.enums.FISCPCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SyscomFepServerApplication.class)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
public class ServerBaseTest {

    static {
        System.setProperty("management.metrics.tags.application", "fep-server");
    }

    protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();

    @Test
    public void test() {
        UnitTestLogger.info(String.valueOf(FISCPCode.PCode2521));
    }
}
