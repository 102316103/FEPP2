package com.syscom.fep.mybatis.util;

import com.syscom.fep.mybatis.MybatisBaseTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ActiveProfiles({"integration", "mybatis", "taipei"})
public class StanGeneratorTest extends MybatisBaseTest {
    @Autowired
    private StanGenerator generator;

    @Test
    public void testGenerate() throws IOException {
        UnitTestLogger.info("stan = ", generator.generate());
//        StringBuilder sb = new StringBuilder();
//        int stan = 9000000;
//        try {
//            while (true) {
//                sb.append(stan).append("\t").append(generator.generate(stan++)).append("\r\n");
//            }
//        } catch (Exception e) {
//        }
//        FileUtils.write(new File("C:/Users/Richard/Desktop/stan.txt"), sb.toString(), StandardCharsets.UTF_8, false);
    }
}
