package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.enums.RelationalOperators;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class NpschkExtMapperTest extends MybatisBaseTest {
    @Autowired
    private NpschkExtMapper mapper;

    @Test
    public void testQueryByEffDateCompare() {
        mapper.queryByEffDateCompare("20220825", RelationalOperators.GT);
    }
}
