package com.syscom.fep.batch.task.inbk;

import java.io.OutputStreamWriter;
import java.util.List;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.BrapExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npschk;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.vo.enums.ATMZone;

public class ProcBRAP extends FEPBase implements Task{
	
    private BatchJobLibrary job = null;
    private OutputStreamWriter batchLog = null;
    private String batchLogPath = CMNConfig.getInstance().getBatchLogPath();
    private String batchOutputPath = CMNConfig.getInstance().getBatchOutputPath();

    private String notification = StringUtils.EMPTY;
    private FEPReturnCode rtnCode = CommonReturnCode.Normal;;
    private String beginDate = "";//起始日期
    private String endDate = "";//截止日期;
    private LogData logContext = null;
//    private FepTxn As DefFEPTXN
//    Private IctlTxn As DefICTLTXN
//    Private _dtBRAPTmp As DataTable
    private String sysstatHbkno = "";
    
	private BrapExtMapper brapExtMapper = SpringBeanFactoryUtil.getBean(BrapExtMapper.class);

	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			//初始化相關批次物件及拆解傳入參數
			this.initialBatch(args);
			sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
		    //開始工作內容
			job.startTask();
			job.writeLog("---------------------------------------------------------------------------------------");
			job.writeLog(ProgramName + "開始!");
			this.checkConfig();
//            if(!this.checkConfig()) {
//            	job.stopBatch();
//                return;
//            }
            //1.判斷執行日(ZONE="TWN")非營業日, 不執行批次
            //2.增加參數FORCERUN(Optional), 如輸入(/FORCERUN:’Y’), 非營業日亦可執行批次
    		if(!job.getArguments().containsKey("FORCERUN") && !job.getArguments().get("FORCERUN").equals("Y")) {
    			if(!job.isBsDay(ATMZone.TWN.toString())) {
    				job.writeLog("非營業日不執行批次");
    				job.stopBatch();
					return BatchReturnCode.Succeed;
    			}
    		}
    		
    		//主流程
            if(doBusiness()) {
	            //'通知批次作業管理系統工作正常結束
	            job.writeLog(ProgramName + " 結束!!");
	            job.endTask();
            }else { 
            	//'Send EMS
                //'通知批次作業管理系統工作失敗,暫停後面流程
                job.writeLog(ProgramName + " 失敗!!");
                job.abortTask();
            }
			return BatchReturnCode.Succeed;
		}catch(Exception e) {
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
	 * 檢核Batch所需參數是否足夠
	 * @return
	 * @throws Exception 
	 */
	private boolean checkConfig() throws Exception {
		if(job.getArguments().containsKey("BEGINDATE")) {
			beginDate = job.getArguments().get("BEGINDATE");
		}else {
			//若沒輸入參數則使用系統日期
			beginDate = SysStatus.getPropertyValue().getSysstatLbsdyFisc();
		}
		if(job.getArguments().containsKey("ENDDATE")) {
			endDate = job.getArguments().get("ENDDATE");
		}else {
			//若沒輸入參數則使用系統日期
			endDate = SysStatus.getPropertyValue().getSysstatLbsdyFisc();
		}
		job.writeLog(ProgramName + " BEGINDATE=" + beginDate + "  " + "ENDDATE=" + endDate);
		return true;
	}
	
	/**
	 * 批次主要處理流程
	 * @return
	 */
	private boolean doBusiness() {
		boolean rtflag = true;
		try {
            //2. 刪除分行清算日結檔BRAP 已存在資料
            rtflag = this.deleteBRAPByDate();
            if(rtflag) {
                job.writeLog("刪除分行清算日結檔 BRAP success!");
            }else {
                job.writeLog(this.notification);
            }	
            //3. 讀取交易明細檔FEPTXN寫入 BRAP
            if(rtflag) {
                rtflag = this.insertBRAPByFEPTXN();
                if(rtflag) {
                    job.writeLog("FEPTXN 寫入 BRAP success!");
                }else {
                    job.writeLog(this.notification);
                }
            }
            //4. 讀取前營業日之FEPTXN未完成交易-2290 寫入 BRAP
            if(rtflag) {
                //rtflag = this.insertBRAPByFEPTXN2290();
                if(rtflag) {
                    job.writeLog("2290 寫入 BRAP success!");
                }else {
                    job.writeLog(this.notification);
                }
            }
            //5. 讀取人工沖正交易 - 2150 寫入 BRAP
            if(rtflag) {
                //rtflag = this.insertBRAPByFEPTXN2150();
                if(rtflag) {
                    job.writeLog("2150 寫入 BRAP success!");
                }else {
                    job.writeLog(this.notification);
                }
            }
            //6. 讀取人工沖正交易 - 2573 2549 寫入 BRAP
            if(rtflag) {
                //rtflag = this.insertBRAPByFEPTXN2573();
                if(rtflag) {
                    job.writeLog("2573 2549 寫入 BRAP success!");
                }else {
                    job.writeLog(this.notification);
                }
            }
		}catch(Exception e) {
			return false;
		}
		return rtflag;
	}
	
	/**
	 * 刪除分行清算日結檔BRAP 已存在資料
	 * @return
	 */
	private boolean deleteBRAPByDate() {
		try {
// 2022-09-02 Richard modified start for Fortify Scan
//			String sqlString = "BRAP_ST_DATE BETWEEN '"+beginDate+"' AND '"+endDate+"' AND SUBSTRING(BRAP_PCODE,1,1) = '2' ";
//			brapExtMapper.deleteByDatePCode(sqlString);
			brapExtMapper.deleteByDatePCode(beginDate, endDate);
// 2022-09-02 Richard modified end for Fortify Scan
		}catch(Exception e) {
            this.notification = "刪除分行清算日結檔BRAP發生Exception";
            logContext.setProgramException(e);
            return false;			
		}
		return true;
	}
	
	/**
	 * 讀取交易明細檔FEPTXN寫入BRAP
	 * @return
	 */
	private boolean insertBRAPByFEPTXN() {
		
		return true;
	}
}
