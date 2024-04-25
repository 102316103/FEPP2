package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.Inbk2160ExtMapper;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * INBK2160
 * @version 00.00
 * @Designer Aster
 * @author Joseph
 *
 */
public class Sending2160 extends FEPBase implements Task {

    private BatchJobLibrary job = null;
    private String batchLogPath = StringUtils.EMPTY;
    private String TX_DATE = StringUtils.EMPTY;
    private String PCODE = "";

	private boolean batchResult = false;

	private Inbk2160ExtMapper inbk2160ExtMapper = SpringBeanFactoryUtil.getBean(Inbk2160ExtMapper.class);
    private FeptxnDao _dbFEPTXN = SpringBeanFactoryUtil.getBean(FeptxnDao.class);
    
	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			TX_DATE = args[1];
			
			//初始化相關批次物件及拆解傳入參數
			logContext = new LogData();
			logContext.setChannel(FEPChannel.BATCH);
			logContext.setEj(0);
			logContext.setProgramName(ProgramName);
			//檢查Batch Log目錄參數
			batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
			if (StringUtils.isBlank(batchLogPath)) {
				LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
				return BatchReturnCode.Succeed;
			}
			
			//初始化BatchJob物件,傳入工作執行參數
			job = new BatchJobLibrary(this, args, batchLogPath);
			job.writeLog("Sending2160 start!");
			
			//開始工作內容
			job.writeLog("開始工作內容");
			job.startTask();
			
			/*檢核是否收到財金未完成交易明細 (PCODE=2160)*/
			PCODE = "2160";
			
			job.writeLog("檢核是否收到財金未完成交易明細");
			if(null == args[0] || "".equals(args[0])) {
				TX_DATE =SysStatus.getPropertyValue().getSysstatTbsdyFisc();
			}else {
				for (int i = 0; i < args.length; i++) {
					if (StringUtils.startsWithIgnoreCase(args[i], "/TBSDY")) {
						job.writeLog("args:["+i+"]:"+args[i]);
						TX_DATE = args[i].toString().split(":")[1];
					} 
				}
				
			}

			job.writeLog("批次參數日期:" + TX_DATE);
			job.writeLog("撈取INBK2160資料");
			List<Map<String,Object>> INBK2160 = inbk2160ExtMapper.getTodayFinishTradeData(SysStatus.getPropertyValue().getSysstatHbkno(), (short) 1, "");

			if(null == INBK2160 || INBK2160.size() < 1) {
				job.writeLog("今日無2160 SENDING 交易");

				job.endTask();
				return BatchReturnCode.Succeed;
			}
			batchResult = mainProcess();

			// 通知批次作業管理系統工作正常結束
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
	private boolean mainProcess() throws Exception {
		boolean rtnflag = true;
		FISCGeneral aData= new FISCGeneral();
		FEPHandler fepHandler = new FEPHandler();
		job.writeLog("FISCGeneral [AA 2160]");
		aData.setINBKRequest(new FISC_INBK());
		aData.setSubSystem(FISCSubSystem.INBK);
		aData.getINBKRequest().setMessageKind(MessageFlow.Request);
		aData.getINBKRequest().setProcessingCode("2160");
		aData.getINBKRequest().setMessageType("0200");
		try {
			job.writeLog("FEPHandlerDispatch [AA 2160]");
			fepHandler.dispatch(FEPChannel.FEP, aData);

			// 將AA RC 顯示(參考 UI_012280Controller)
			if (StringUtils.isBlank(aData.getDescription())) {
				aData.setDescription("無此錯誤訊息!!請洽資訊人員!");
			}else {
				job.writeLog(aData.getDescription());
			}

			//String[] message = aData.getDescription().split("-");
		} catch (Exception e) {
			job.writeLog("FISCHandlerDispatch [AA 2160] Fail!!!");
			job.writeLog(e.getMessage());
			return false;
		}
		return rtnflag;
	}
	
}
