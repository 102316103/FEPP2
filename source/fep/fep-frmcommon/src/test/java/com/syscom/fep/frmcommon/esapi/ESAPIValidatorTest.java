package com.syscom.fep.frmcommon.esapi;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.junit.jupiter.api.Test;
import org.owasp.esapi.errors.ValidationException;

public class ESAPIValidatorTest {
    private final LogHelper logger = new LogHelper();

    @Test
    public void test() throws ValidationException {
        String path = "";

        path = "SampleBatch_88263ed4-c4b1-4041-9620-33d5e7c772af_1.log";
        path = ESAPIValidator.getValidFileName(path);

        path = "E:/home/syscom/RM/Batch/Log/2023-09-1/fep-batch-task/";
        path = ESAPIValidator.getValidDirectoryName(path);

        path = "/home/syscom/RM/Batch/Log/2023-09-13/fep-batch-task/";
        path = ESAPIValidator.getValidDirectoryName(path);

        path = "E:/a/b";
        path = ESAPIValidator.getValidDirectoryName(path);

        path = "E:/home/syscom/RM/Batch/Log/2023-09-13/fep-batch-task/SampleBatch_88263ed4-c4b1-4041-9620-33d5e7c772af_1.log";
        path = ESAPIValidator.getValidDirectoryName(path);

        path = "E:\\home\\syscom\\RM\\Batch\\Log\\2023-09-13\\fep-batch-task\\SampleBatch_88263ed4-c4b1-4041-9620-33d5e7c772af_1.log";
        path = ESAPIValidator.getValidDirectoryName(path);

        logger.info(path);
    }
}
