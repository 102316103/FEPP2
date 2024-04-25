package com.syscom.fep.vo.communication;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ToFEPATMCommuTest {
    private LogHelper logger = LogHelperFactory.getUnitTestLogger();

    private ToFEPATMCommu toFEPATMCommu;

    @BeforeEach
    public void setup() {
        toFEPATMCommu = new ToFEPATMCommu();
        toFEPATMCommu.setAtmno("123456");
        toFEPATMCommu.setEj("0");
        toFEPATMCommu.setMessage("00D9F0F3F9F6F1F1C4404040F1F04BF1F04BF74BF5F540404040404040404040404040404040404040E3C1E3D4F0F0F040404040404040404040C1C9D5D8C3C3C3F0F0F0F3F0D66EF7F66E7AF9F7F740F0F1F17A5E6EF67EF8F8F87A7E5EF04C7E5EF8F0F0F0F0F0F2F6F2303236393030303030303330202020202020202020202020202020202020000A1605FD073EBDB63B40404040404040404040404040404040404040404040404040404040404040404040F2F0F2F2F1F2F2");
        toFEPATMCommu.setSync(false);
    }

    @Test
    public void test() throws Exception {
        String xml = toFEPATMCommu.toString();
        logger.info("xml = [", xml, "]");
        logger.info("fromXML = [", ReflectionToStringBuilder.toString(ToFEPATMCommu.fromXML(xml), ToStringStyle.MULTI_LINE_STYLE), "]");
    }
}
