package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.MybatisBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "mybatis", "taipei"})
public class StanExtMapperTest extends MybatisBaseTest {
    @Autowired
    private StanExtMapper mapper;

    @Test
    public void testGetStanSequence() {
        UnitTestLogger.info("Stan = [", mapper.getStanSequence(), "]");
    }

    @Test
    public void testResetStanSequence() {
        mapper.resetStanSequence();
        testGetStanSequence();
    }
}
