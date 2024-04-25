package com.syscom.fep.enclib;

import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.server.common.ServerCommonBaseTest;
import com.syscom.fep.enclib.vo.ENCLogData;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@StackTracePointCut(caller = "DESTest.exe")
@ActiveProfiles({"integration", "mybatis", "taipei"})
public class ENCLibTest extends ServerCommonBaseTest {
    @Test
    public void test() {
        try {
            ENCLib encLib = new ENCLib(new ENCLogData(), 0, 0, "", 0);
        } catch (Exception e) {
            UnitTestLogger.error(e, e.getMessage());
            assertTrue(e instanceof RuntimeException);
        }
    }
}
