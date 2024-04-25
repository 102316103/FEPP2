package com.syscom.fep.batch.task;

import com.syscom.fep.batch.BatchBaseTest;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "mybatis", "jms", "la"})
public class CallTaskTest extends BatchBaseTest {
    @Test
    public void callSampleBatch() throws Exception {
        String jarPath = "E:\\home\\syscom\\fep-app\\fep-batch-task\\fep-batch-task-SampleBatch.jar";
        String classname = "com.syscom.fep.batch.task.rm.SampleBatch";
        Task task = ReflectUtil.dynamicLoadClass(jarPath, classname);
        task.execute(new String[]{});
    }
}
