package com.syscom.fep.mybatis.mapper;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Vatxn;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "mybatis", "taipei"})
public class VatxnMapperTest extends MybatisBaseTest {
    @Autowired
    private VatxnMapper mapper;

    @Test
    public void testSelectByPrimaryKey() {
        Vatxn vatxn = mapper.selectByPrimaryKey("20181005", 123456);
        UnitTestLogger.info(ReflectionToStringBuilder.toString(vatxn, ToStringStyle.MULTI_LINE_STYLE));
    }
}
