package com.syscom.fep.batch.task.rm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 批次程式案例, 用於測試
 *
 * @author Richard
 */
public class SampleBatch extends FEPBase implements Task {
    private String batchLogPath = StringUtils.EMPTY;
    private BatchJobLibrary job = null;
    private boolean batchResult = false;

    @Override
    public BatchReturnCode execute(String[] args) {
        // 自動生成的方法存根
        try {
            // 1. 初始化相關批次物件及拆解傳入參數
            initialBatch(args);

            if (ArrayUtils.isNotEmpty(args)) {
                for (String arg : args) {
                    job.writeLog("接收的參數:", arg);
                }
            }

            // 2. 檢核批次參數是否正確, 若正確則啟動批次工作
            job.writeLog("------------------------------------------------------------------");
            job.writeLog(ProgramName + "開始");
            job.startTask();
            // 3. 批次主要處理流程
            batchResult = mainProcess();
            // 4. 通知批次作業管理系統工作正常結束
            if (batchResult) {
                job.writeLog(ProgramName + "正常結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.endTask();
            } else {
                job.writeLog(ProgramName + "不正常結束，停止此批次作業!!");
                job.writeLog("------------------------------------------------------------------");
                job.abortTask();
            }
            return BatchReturnCode.Succeed;
        } catch (Exception e) {
            logContext.setProgramException(e);
            logContext.setProgramName(ProgramName);
            sendEMS(logContext);
            if (job != null) {
                job.writeErrorLog(e, e.getMessage());
                job.writeLog(ProgramName + "失敗!!");
                job.writeLog("------------------------------------------------------------------");
                // 通知批w作業管理系統工作失敗,暫停後面流程
                try {
                    job.abortTask();
                } catch (Exception ex) {
                    logContext.setProgramException(ex);
                    logContext.setProgramName(ProgramName);
                    sendEMS(logContext);
                }
            }
            return BatchReturnCode.ProgramException;
        } finally {
            if (job != null) {
                job.writeLog(ProgramName + "結束!!");
                job.writeLog("------------------------------------------------------------------");
                job.dispose();
                job = null;
            }
            if (logContext != null) {
                logContext = null;
            }
        }
    }

    private boolean mainProcess() {
        job.writeLog(ProgramName + ", 我在執行批次程序HaHaHaHaHaHaHaHaHa");
        job.writeLog(ProgramName + ", 我是新版本，我今天更新了哈哈哈哈哈啊哈哈");
        logContext.setRemark("測試一下，記第一筆FEPLOGGER");
        this.logMessage(this.logContext);
        logContext.setRemark("測試一下，記第二筆FEPLOGGER");
        this.logMessage(this.logContext);
        logContext.setRemark("測試一下，記第三筆FEPLOGGER");
        this.logMessage(this.logContext);
        SubTask subTask = new SubTask();
        subTask.doProcess();
        return true;
    }

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     */
    private void initialBatch(String[] args) {
        // 0. 初始化logContext物件,傳入工作執行參數
        logContext = new LogData();
        logContext.setChannel(FEPChannel.BATCH);
        logContext.setEj(0);
        logContext.setProgramName(ProgramName);
        // 1. 檢查Batch Log目錄參數
        batchLogPath = "/home/syscom/RM/Batch/Log/"; // RMConfig.getInstance().getBatchLogPath().trim();
        if (StringUtils.isBlank(batchLogPath)) {
            LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
            return;
        }
        // 2. 初始化BatchJob物件,傳入工作執行參數
        job = new BatchJobLibrary(this, args, batchLogPath);
    }

    private class SubTask {
        public void doProcess() {
            job.writeLog(ProgramName + ", 我是內部類開始執行任務嘍!!!!!");
            job.writeLog(ProgramName + ", 我是內部類開始執行任務嘍11!!!!!");
            job.writeLog(ProgramName + ", 我是內部類開始執行任務嘍22!!!!!");
            job.writeLog(ProgramName + ", 我是內部類開始執行任務嘍33!!!!!");
            job.writeLog(ProgramName + ", 我是內部類開始執行任務嘍44!!!!!");
            job.writeLog(ProgramName + ", 我是內部類開始執行任務嘍55!!!!!");
        }
    }
}
