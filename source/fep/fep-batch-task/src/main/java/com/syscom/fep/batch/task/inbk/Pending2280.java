package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.server.common.handler.FEPHandler;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;


/**
 * 負責處理通匯相關序號及通匯狀態維護
 * @author Ben
 *
 */
public class Pending2280 extends FEPBase implements Task {
	private String batchLogPath = StringUtils.EMPTY;
	private BatchJobLibrary job = null;
    private boolean batchResult = false;
    
	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			// 1. 初始化相關批次物件及拆解傳入參數
			initialBatch(args);
			
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
				// 通知批次作業管理系統工作失敗,暫停後面流程
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
		boolean rtnflag = true;
		FISCGeneral aData= new FISCGeneral();
		FEPHandler fepHandler = new FEPHandler();
		job.writeLog("FISCGeneral [AA 2280]");
		aData.setINBKRequest(new FISC_INBK());
		aData.setSubSystem(FISCSubSystem.INBK);
		aData.getINBKRequest().setMessageKind(MessageFlow.Request);
		aData.getINBKRequest().setProcessingCode("2280");
		aData.getINBKRequest().setMessageType("0200");
		try {
			job.writeLog("FEPHandlerDispatch [AA 2280]");
			fepHandler.dispatch(FEPChannel.FEP, aData);
			
			// 將AA RC 顯示(參考 UI_012280Controller)
            if (StringUtils.isBlank(aData.getDescription())) {
                aData.setDescription("無此錯誤訊息!!請洽資訊人員!");
            }else {
            	job.writeLog(aData.getDescription());	
            }
            
            //String[] message = aData.getDescription().split("-");
		} catch (Exception e) {
			job.writeLog("FISCHandlerDispatch [AA 2280] Fail!!!");
			job.writeLog(e.getMessage());
			return false;
		}
		return rtnflag;
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
		batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
		if (StringUtils.isBlank(batchLogPath)) {
			LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
			return;
		}
		// 2. 初始化BatchJob物件,傳入工作執行參數
		job = new BatchJobLibrary(this, args, batchLogPath);
	}
}
