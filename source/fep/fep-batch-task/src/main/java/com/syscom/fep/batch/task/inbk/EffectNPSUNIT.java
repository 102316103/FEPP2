package com.syscom.fep.batch.task.inbk;

import java.util.List;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NpschkExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npschk;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.vo.enums.ATMZone;

/**
 * 將生效資料更新全國性繳費委託單位檔
 * @author bruce
 *
 */
public class EffectNPSUNIT extends FEPBase implements Task {

    private boolean batchResult = false;
    private String batchLogPath = StringUtils.EMPTY;
    private BatchJobLibrary job = null;
    private String effDate = "";
    
	private NpschkExtMapper npschkExtMapper = SpringBeanFactoryUtil.getBean(NpschkExtMapper.class);
	private NpsunitExtMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
	
	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			//初始化相關批次物件及拆解傳入參數
			this.initialBatch(args);
			job.writeLog("---------------------------------------------------------------------------------------");
			job.writeLog(ProgramName + "開始!");
			//回報批次平台開始工作
			job.startTask();
			if(!this.checkConfig()) {
				job.stopBatch();
				return BatchReturnCode.Succeed;
			}
			//批次主要處理流程
			batchResult = this.mainProcess();
			//通知批次作業管理系統工作正常結束
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
		}catch(Exception ex) {
			//通知批次作業管理系統工作失敗,暫停後面流程
			try {
				job.abortTask();
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog(ex.toString());				
				job.writeLog("------------------------------------------------------------------");
			} catch (Exception e) {
				logContext.setProgramException(e);
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
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
	
	/**
	 * 初始化相關批次物件及拆解傳入參數
	 * @param args
	 */
	private void initialBatch(String[] args) {
		logContext = new LogData();
		logContext.setChannel(FEPChannel.BATCH);
		logContext.setEj(0);
		logContext.setProgramName(ProgramName);
		//檢查Batch Log目錄參數
		batchLogPath = RMConfig.getInstance().getBatchLogPath().trim();
		if (StringUtils.isBlank(batchLogPath)) {
			LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
			return;
		}
		//初始化BatchJob物件,傳入工作執行參數
		job = new BatchJobLibrary(this, args, batchLogPath);
	}
	
	/**
	 * 檢查是否為營業日
	 * @return
	 * @throws Exception 
	 */
	private boolean checkConfig() throws Exception {
		if(!job.getArguments().containsKey("FORCERUN")) {
			if(!job.isBsDay(ATMZone.TWN.toString())) {
				job.writeLog("非營業日不執行批次");
				return false;
			}
		}
		if(job.getArguments().containsKey("TBSDY_FISC")) {
			this.effDate = job.getArguments().get("TBSDY_FISC");
		}else {
			this.effDate = SysStatus.getPropertyValue().getSysstatTbsdyFisc();			
		}
		job.writeLog("effDate:" + this.effDate);
		return true;
	}
	
	/**
	 * 批次主要處理流程
	 * @return
	 */
	private boolean mainProcess() {
		int dataCnt = 0;//總共幾筆
        int successCnt = 0;//成功筆數
        int failCnt = 0;//失敗筆數
        boolean rtn = true;//是否正常跑完
        try {
    		List<Npschk> npschkList = npschkExtMapper.selectByEffectDate(this.effDate);
    		if(npschkList.size() == 0) {
    			job.writeLog("委託單位代號檢核檔(NPSCHK)筆數為0");
    			return true;
    		}
    		job.writeLog(String.format("共查出%s筆需更新的生效資料", npschkList.size()));
    		Npsunit npsunit = null;
    		for(Npschk npschk : npschkList) {
    			npsunit = new Npsunit();
    			npsunit.setNpsunitNo(npschk.getNpschkNo());
    			npsunit.setNpsunitPaytype(npschk.getNpschkPaytype());
    			npsunit.setNpsunitFeeno(npschk.getNpschkFeeno());
    			npsunit.setNpsunitEffectdate(this.effDate);
    			dataCnt += 1;
    			try {
    				int delete = npsunitExtMapper.deleteByPrimaryKey(npsunit);
    				int insert =  npsunitExtMapper.insertByNpschk(npsunit);
    				if(delete <= 0 || insert <= 0) {
    	                job.writeLog(String.format("NPSUNIT處理失敗 NPSUNIT_NO[%s] NPSUNIT_PAYTYPE[%s] NPSUNIT_FEENO[%s]", npsunit.getNpsunitNo() ,npsunit.getNpsunitPaytype(), npsunit.getNpsunitFeeno()));
    	                failCnt += 1;	
    	                rtn = false;
    				}else {
    					successCnt += 1;
    				}				
    			}catch(Exception ex) {
                    job.writeLog(ex.getMessage());
                    job.writeLog(String.format("NPSUNIT處理失敗 NPSUNIT_NO[%s] NPSUNIT_PAYTYPE[%s] NPSUNIT_FEENO[%s]", npsunit.getNpsunitNo() ,npsunit.getNpsunitPaytype(), npsunit.getNpsunitFeeno()));
                    failCnt += 1;
                    rtn = false;				
    			}
    		}
    		//TODO 這裡有呼叫外部SP，明祥說先不翻        	
        }catch(Exception ex) {
            job.writeLog(ex.getMessage());
            rtn = false;
        }
        job.writeLog(String.format("全國性繳費委託單位檔共%s筆 成功%s筆 失敗%s筆", dataCnt, successCnt, failCnt));
        //TODO 這裡有呼叫外部SP，明祥說先不翻  
        //job.writeLog(String.format("更新LinkedPayBill共{0}筆 成功{1}筆 失敗{2}筆", dataCnt, PayBillsuccessCnt, PayBillfailCnt));
		return rtn;
	}
}
