package com.syscom.fep.batch.cmdline;

import org.junit.jupiter.api.Test;

public class AppMainTest {

    @Test
    public void testSampleBatch() throws Exception {
        String[] args = new String[]{"-p", "com.syscom.fep.batch.task.rm.SampleBatch"};
        SyscomFepBatchCmdLineApplication.main(args);
    }

    @Test
    public void testArchivingLogFileBatch() throws Exception {
        String[] args = new String[]{"-p","com.syscom.fep.batch.task.cmn.ArchivingLogFile",
                "-a","/SourceDir:/home/syscom/fep-app/logs/ /TargetDir:/home/syscom/tar /ArchiveDay:13 /ReserveDay:15 /CallBatchJob:false"};
        SyscomFepBatchCmdLineApplication.main(args);
    }

    @Test
    public void testDisplayUsage() throws Exception {
        String[] args = new String[]{"-h"};
        SyscomFepBatchCmdLineApplication.main(args);
    }
}
