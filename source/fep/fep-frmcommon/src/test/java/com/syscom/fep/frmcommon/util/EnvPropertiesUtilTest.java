package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.FrmcommonBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("integration")
@ContextConfiguration
public class EnvPropertiesUtilTest extends FrmcommonBaseTest {

    @Test
    public void test() {
        String tmpKey = EnvPropertiesUtil.getProperty("spring.fep.service.netty.printtogopassword.regKey", "00000000000000000000");
        System.out.println(tmpKey);
    }
}
