package com.syscom.fep.batch.task.inbk;

import java.util.List;
import java.util.Map;

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
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;

/**
 * 整批傳送未完成交易處理結果2130
 * @version 00.00
 * @Designer CandyHsieh
 * @author Han
 *
 */
public class Pending2130 extends FEPBase implements Task {

    private BatchJobLibrary job = null;
    private String batchLogPath = StringUtils.EMPTY;
    private String INBKPEND_TX_DATE = StringUtils.EMPTY;
    private String INBKPEND_PCODE = "";
    private String oriBkno;
	private String oriStan;
    
    private InbkpendExtMapper inbkpendExtMapper = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
    private FeptxnDao _dbFEPTXN = SpringBeanFactoryUtil.getBean(FeptxnDao.class);
    
	@Override
	public BatchReturnCode execute(String[] args) {
		try {
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

			job.writeLog("Pending2130 start!");
			
			//開始工作內容
			job.writeLog("開始工作內容");
			job.startTask();
			
			/*檢核是否收到財金未完成交易明細 (PCODE=2120)*/
			INBKPEND_PCODE = "2120";
			
			job.writeLog("檢核是否收到財金未完成交易明細");
			if(job.getArguments().containsKey("TBSDY")) {
				INBKPEND_TX_DATE = job.getArguments().get("TBSDY");
			}else{
				INBKPEND_TX_DATE =SysStatus.getPropertyValue().getSysstatTbsdyFisc();
			}
			if(job.getArguments().containsKey("ORI_BKNO")) {
				oriBkno = job.getArguments().get("ORI_BKNO");
			}
			if(job.getArguments().containsKey("ORI_STAN")) {
				oriStan = job.getArguments().get("ORI_STAN");
			}
			job.writeLog("批次參數日期:" + INBKPEND_TX_DATE);
			job.writeLog("撈取 INBKPEND2120資料");
			List<Map<String,Object>> INBKPEND2120 = inbkpendExtMapper.getTodayFinishTradeData(INBKPEND_TX_DATE,INBKPEND_PCODE,SysStatus.getPropertyValue().getSysstatFbkno(),oriBkno,oriStan);
			
			if(null == INBKPEND2120 || INBKPEND2120.size() < 1) {
				job.writeLog("今日無2120 PENDING 交易");
				
				job.writeLog(ProgramName + "結束!!");
				job.writeLog("------------------------------------------------------------------");
				job.dispose();
				if (job != null) {
					job.dispose();
					job = null;
				}
				if (logContext != null) {
					logContext = null;
				}
				return BatchReturnCode.Succeed;
			}
			
			INBKPEND_PCODE = "2130";
			for(int i = 0; i < INBKPEND2120.size(); i++) {
				job.writeLog(i+":檢核是否已傳送交易處理結果給財金");
				/* 檢核是否已傳送交易處理結果給財金 (PCode=2130) */
				job.writeLog("檢核是否已傳送交易處理結果給財金 (PCode=2130)");
				List<Map<String,Object>> INBKPEND2130 = inbkpendExtMapper.getIsSendTrade(INBKPEND_TX_DATE,INBKPEND_PCODE,oriBkno,oriStan);
				
				if(null == INBKPEND2130 || INBKPEND2130.size() > 0) {
					job.writeLog(i+":test");
					if("F2".equals(INBKPEND2130.get(0).get("INBKPEND_MSGFLOW").toString()) 
							&& "0001".equals(INBKPEND2130.get(0).get("INBKPEND_REP_RC").toString())
							&& "Normal".equals(INBKPEND2130.get(0).get("INBKPEND_AA_RC").toString())) {
						job.writeLog("已傳送交易處理結果, 不可再次傳送");
						job.abortTask();
						return BatchReturnCode.Succeed;
					}

				}
				INBKPEND2130 = null;

				/* 組CALL AA2130 電文內容, 可參考UI_012130 寫法 */
				job.writeLog("組CALL AA2130 電文內容");
				FISCGeneral aData = new FISCGeneral();
				aData.setINBKRequest(new FISC_INBK());
				aData.setSubSystem(FISCSubSystem.INBK);
				aData.getINBKRequest().setProcessingCode("2130");
				aData.getINBKRequest().setMessageType("0200");
				aData.getINBKRequest().setMessageKind(MessageFlow.Request);
				aData.getINBKRequest().setTxAmt(INBKPEND2120.get(i).get("INBKPEND_ORI_TX_DATE").toString());
				aData.getINBKRequest().setATMNO(INBKPEND2120.get(i).get("INBKPEND_ATMNO").toString());

				if(null == INBKPEND2120.get(i).get("INBKPEND_ORI_PCODE")) {
					INBKPEND2120.get(i).put("INBKPEND_ORI_PCODE","");
				}
				
				List<Map<String,Object>> callAAData = _dbFEPTXN.getgetCallAa2130Data(
						INBKPEND_TX_DATE.substring(6, 8),
						INBKPEND2120.get(i).get("INBKPEND_ORI_TX_DATE").toString(),
						INBKPEND2120.get(i).get("INBKPEND_ORI_BKNO").toString(),
						INBKPEND2120.get(i).get("INBKPEND_ORI_STAN").toString(),
						INBKPEND2120.get(i).get("INBKPEND_ORI_PCODE").toString()
						);
				job.writeLog("getgetCallAa2130Data End");
				job.writeLog("callAAData size:"+callAAData.size());
				if(callAAData.size() != 0) {
					//20220624合庫提供規則 : 有記帳未沖正視為交易成功，未記帳或記帳已沖正視為交易失敗
					//FEPTXN_ACC_TYP 0:未記帳 1:已記帳 2:已更正 3:更正/轉入失敗 4:未明 5:待解
					if("1".equals(callAAData.get(0).get("FEPTXN_ACC_TYPE"))) {
						aData.getINBKRequest().setRsCode("00");
					}else {
						aData.getINBKRequest().setRsCode("01");
					}
				}else {
					aData.getINBKRequest().setRsCode("01");
				}
				
				
				
				if(null != INBKPEND2120.get(i).get("INBKPEND_ORI_TX_DATE")) {
					aData.getINBKRequest().setDueDate(CalendarUtil.adStringToROCString(INBKPEND2120.get(i).get("INBKPEND_ORI_TX_DATE").toString()));
				}
				if(null != INBKPEND2120.get(i).get("INBKPEND_ORI_STAN") && null != INBKPEND2120.get(i).get("INBKPEND_ORI_BKNO")) {
					aData.getINBKRequest().setOriStan(INBKPEND2120.get(i).get("INBKPEND_ORI_BKNO").toString()+StringUtils.leftPad(INBKPEND2120.get(i).get("INBKPEND_ORI_STAN").toString(), 7, '0'));
				}
				if(null != INBKPEND2120.get(i).get("INBKPEND_MAJOR_ACTNO")) {
					aData.getINBKRequest().setICMARK(StringUtils.leftPad(INBKPEND2120.get(i).get("INBKPEND_MAJOR_ACTNO").toString(), 16, ' '));
				}
				
				if(null != INBKPEND2120.get(i).get("INBKPEND_TROUT_BKNO")) {
					aData.getINBKRequest().setTroutBkno(INBKPEND2120.get(i).get("INBKPEND_TROUT_BKNO").toString());
				}
				
				if(null != INBKPEND2120.get(i).get("INBKPEND_TROUT_ACTNO")) {
					aData.getINBKRequest().setTroutBkno(INBKPEND2120.get(i).get("INBKPEND_TROUT_ACTNO").toString());
				}
				if(null != INBKPEND2120.get(i).get("INBKPEND_TRIN_BKNO")) {
					aData.getINBKRequest().setTrinBkno(INBKPEND2120.get(i).get("INBKPEND_TRIN_BKNO").toString());
				}
				if(null != INBKPEND2120.get(i).get("INBKPEND_TRIN_ACTNO")) {
					aData.getINBKRequest().setTrinActno(INBKPEND2120.get(i).get("INBKPEND_TRIN_ACTNO").toString());
				}

				//暫存入REMARK傳給AA
				if(null != INBKPEND2120.get(i).get("INBKPEND_ORI_PCODE") && null != INBKPEND2120.get(i).get("INBKPEND_TRIN_ACTNO_ACTUL")) {
					aData.getINBKRequest().setREMARK(StringUtils.leftPad(callAAData.get(0).get("INBKPEND_TRIN_ACTNO_ACTUL").toString().trim(), 16, ' ')+StringUtils.leftPad(callAAData.get(0).get("INBKPEND_ORI_PCODE").toString().trim(), 4, '0'));
				}

				// 呼叫 AA - 2130
				this.infoMessage("Start to Call AA  by condition = ", INBKPEND2120.get(i).toString());
				job.writeLog("Start to Call AA  by condition =" + INBKPEND2120.get(i).toString());
				FEPHandler fepHandler = new FEPHandler();
				fepHandler.dispatch(FEPChannel.FEP, aData);
				
				// 將AA RC 顯示在UI上
				if (StringUtils.isBlank(aData.getDescription())) {
					job.writeLog("無此錯誤訊息!!請洽資訊人員!");
					aData.setDescription("無此錯誤訊息!!請洽資訊人員!");
				}
			}
			
			//通知批次作業管理系統工作正常結束
			job.writeLog("Pending2130 end!");
			job.endTask();
			return BatchReturnCode.Succeed;
		}catch(Exception ex) {
			//通知批次作業管理系統工作失敗,暫停後面流程
			try {
				job.writeLog("失敗:"+ex);
				job.writeLog("失敗:Pending2130 end!");
				job.abortTask();
			} catch (Exception e) {
				logContext.setProgramException(ex);
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (job != null) {
				job.dispose();
				job = null;
			}
			if (logContext != null) {
				logContext = null;
			}
		}	
	}
	
	
}
