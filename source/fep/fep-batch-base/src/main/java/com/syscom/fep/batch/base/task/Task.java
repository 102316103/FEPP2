package com.syscom.fep.batch.base.task;

import com.syscom.fep.batch.base.enums.BatchReturnCode;

public interface Task {

    /**
     * Batch主流程, 從主流程呼叫各子流程, 若子流程發生ex則throw至主流程並結束AbortTask
     * 主流程每一個步驟依據_batchResult判斷是否往下執行
     *
     * 2023-01-30 Richard modified 加入返回值返回code用於fep-batch-cmdline
     *
     * @param args
     * @return
     */
    BatchReturnCode execute(String[] args);

}
