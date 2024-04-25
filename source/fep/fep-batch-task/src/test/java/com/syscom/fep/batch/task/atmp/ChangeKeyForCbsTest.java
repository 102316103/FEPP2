package com.syscom.fep.batch.task.atmp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import com.syscom.fep.frmcommon.cryptography.Jasypt;

@SpringBootApplication
public class ChangeKeyForCbsTest {

    @Test
    void testExecute() {
        ChangeKeyForCbs obj = new ChangeKeyForCbs();
        String[] args = { "/WSID:99761" };
        obj.execute(args);

    }
}
