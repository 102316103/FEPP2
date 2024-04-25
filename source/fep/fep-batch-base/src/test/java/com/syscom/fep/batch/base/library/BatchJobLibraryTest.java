package com.syscom.fep.batch.base.library;

import org.junit.jupiter.api.Test;

import com.syscom.fep.batch.base.BatchBaseBaseTest;
import com.syscom.fep.batch.base.vo.FEPBatch;
import com.syscom.fep.batch.base.vo.FEPBatch.FEPBatchTaskParameters;
import com.syscom.fep.frmcommon.util.ReflectUtil;

public class BatchJobLibraryTest extends BatchBaseBaseTest {

	@Test
	public void testWriteLog() {
		BatchJobLibrary batchJob = new BatchJobLibrary();
		batchJob.writeLog("Hello batchJob!!!");
	}

	@Test
	public void testWriteLogForThread() {
		new Thread(new Runnable() {
			private BatchJobLibrary library = getBatchJobLibrary("Richard", "Richard");

			@Override
			public void run() {
				while (true) {
					library.writeLog("Richard" + String.valueOf(System.currentTimeMillis()));
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {}
				}
			}
		}).start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
		new Thread(new Runnable() {
			private BatchJobLibrary library = getBatchJobLibrary("Annie", "Annie");

			@Override
			public void run() {
				while (true) {
					library.writeLog("Annie" + String.valueOf(System.currentTimeMillis()));
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {}
				}
			}
		}).start();
	}

	private BatchJobLibrary getBatchJobLibrary(String _batchName, String _logPath) {
		BatchJobLibrary batchJob = new BatchJobLibrary();
		ReflectUtil.setFieldValue(batchJob, "_batchName", _batchName);
		ReflectUtil.setFieldValue(batchJob, "_logPath", _logPath);
		FEPBatch jobData = new FEPBatch();
		String id = String.valueOf(System.currentTimeMillis());
		jobData.setTaskParameters(new FEPBatchTaskParameters() {
			{
				setInstanceId("INS" + id);
				setStepId("STEP" + id);
			}
		});
		ReflectUtil.setFieldValue(batchJob, "jobData", jobData);
		return batchJob;
	}
}
