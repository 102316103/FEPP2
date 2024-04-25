package com.syscom.fep.batch.cmdline.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ref.RefString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class JobHelper extends FEPBase {

    public BatchReturnCode runProcess(String programName, String arguments, RefString refErrMsg) {
        if (arguments == null) arguments = StringUtils.EMPTY;
        BatchJobLibrary batchLib = new BatchJobLibrary();
        try {
            Task task = batchLib.getBatchTask(programName);
            LogMDC.put(Const.MDC_PROFILE, task.getClass().getSimpleName()); // 這裡要改變一下FEPLOGGER記錄的檔名, 記錄到對應的批次程式中
            BatchReturnCode rtnCode = task.execute(arguments.split(StringUtils.SPACE));
            return rtnCode;
        } catch (Exception e) {
            refErrMsg.set(e.getMessage());
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setProgramName(StringUtils.join(ProgramName, ".runProcess"));
            log.setProgramException(e);
            sendEMS(log);
            return BatchReturnCode.ProgramException;
        } finally {
            LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_COMMAND_LINE); // 這裡要還原FEPLOGGER記錄的檔名
        }
    }
}