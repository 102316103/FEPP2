package com.syscom.fep.frmcommon.pool;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;

public class GenericPoolTest {
    private final LogHelper logger = new LogHelper();
    private GenericPooledFactory<String> factory = new GenericPooledFactory<>();
    private GenericObjectPoolConfig<String> config = new GenericObjectPoolConfig<>();
    private GenericPool<String> pool;

    @BeforeEach
    public void setup() {
        factory.addAll(Arrays.asList("1", "2"));
        config.setMaxTotal(factory.size());
        config.setMaxIdle(factory.size());
        config.setMaxWait(Duration.ofMillis(5000));
        pool = new GenericPool<>(factory, config);
    }

    @Test
    public void test() {
        for (int i = 0; i < 10; i++) {
            borrow(String.valueOf(i));
        }
    }

    @Test
    public void testBySequence() {
        pool.setBorrowObjectBySequence();
        for (int i = 0; i < 10; i++) {
            borrow(String.valueOf(i));
        }
    }

    private void borrow(String str) {
        String obj = null;
        try {
            obj = pool.borrowObject();
            logger.info(str, " borrow : ", obj);
        } catch (Exception e) {
            logger.error(e, str, " borrow with exception occur: ", e.getMessage());
        } finally {
            if (obj != null)
                pool.returnObject(obj);
        }
    }
}
