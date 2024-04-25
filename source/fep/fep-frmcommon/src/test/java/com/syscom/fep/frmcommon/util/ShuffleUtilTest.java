package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.junit.jupiter.api.Test;

public class ShuffleUtilTest {
    private LogHelper logger = new LogHelper();

    @Test
    public void test() throws Exception {
        String data = "049611";
        String key = "02319B8680FD928F";

        String shuffle = ShuffleUtil.shuffle(data, key);
        logger.info(data, " shuffle result ", shuffle);
        logger.info(shuffle, " shuffle result ", ShuffleUtil.shuffle(shuffle, key));
    }
}
