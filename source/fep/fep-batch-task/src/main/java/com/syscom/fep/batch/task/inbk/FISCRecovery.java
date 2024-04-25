package com.syscom.fep.batch.task.inbk;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FeptxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.InbkdatavalidExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.mapper.InbkdatavalidMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Inbkdatavalid;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.ATMZone;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.CMNConfig;

/**
 * 
 * 
 * @author Han
 */
public class FISCRecovery extends FEPBase implements Task {
	private String _programName = FISCRecovery.class.getSimpleName(); // 程式名稱

	protected ATMGeneralRequest atmReq;
	private BatchJobLibrary job = null;
	private LogData _logData;
	private String _batchLogPath = StringUtils.EMPTY;



	//--ben start 1/3(2022/9/14)--
	// 2022/9/14 Daniel指示更改使用參數(TXDATE_B,TXDATE_E,TXTIME_B,TXTIME_E)，而其中 TXTIME_B,TXTIME_E 暫無使用。
	//private String _txdate = StringUtils.EMPTY;
	//private String _beginTime = StringUtils.EMPTY;
	//private String _endTime = StringUtils.EMPTY;
	private String txdate_b = StringUtils.EMPTY;	//起始日期
	private String txdate_e = StringUtils.EMPTY;	//截止日期
	private String txtime_b = StringUtils.EMPTY;	//起始時間
	private String txtime_e = StringUtils.EMPTY;	//截止時間

	private FEPReturnCode _batchResult = CommonReturnCode.Abnormal;

	private String _BatchOutputPath = CMNConfig.getInstance().getBatchOutputPath();
//	private String _filename = "RETFR2_YYYYMMDD_REP.TXT"; // 產上要上傳的檔案txt
//	private boolean _isWorkDay = true; // 工作日
	private String _fiscTbsdy = ""; // 財金本營業日
	private String _fiscNbsdy = ""; // 財金次一營業日
	List<Map<String, Object>> _defSYSSTAT = null;

	private String strFileName_CD;
	private String strFileName_TF;
	private String strFileName_TA;
	private String strFileName_BT;
	private String strFileName_BF;
	private int _cntCD =0;
	private int _cntTF =0;
	private int _cntTA =0;
	private int _cntBT =0;
	private int _cntBF =0;

	private ArrayList _fiscBusDate =new ArrayList();
	private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
	private SysstatExtMapper npschkExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
	private FeptxnExtMapper feptxnExtMapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);

	private InbkdatavalidExtMapper inbkdatavalidExtMapper =SpringBeanFactoryUtil.getBean(InbkdatavalidExtMapper.class);
	// <summary>
	// Batch主流程, 從主流程呼叫各子流程, 若子流程發生ex則throw至主流程並結束AbortTask
	// 主流程每一個步驟依據_batchResult判斷是否往下執行
	// </summary>
	// <remarks></remarks>
	@Override
	public BatchReturnCode execute(String[] args) {
		// 自動生成的方法存根
		try {
			String _programName="FISCRecovery";
			// 0. 檢查Batch Log Path參數

			_batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();

			if (StringUtils.isBlank(_batchLogPath)) {
				job.writeLog("Batch Log目錄未設定，請修正");
				return BatchReturnCode.Succeed;
			}

			// 1. 初始化BatchJob物件,傳入工作執行參數, 檢核Batch所需參數
			job = new BatchJobLibrary(this, args, _batchLogPath);

			// 2. 開始工作內容
			job.writeLog("------------------------------------------------------------------");
			job.writeLog(ProgramName + "開始");
			job.startTask();

			// modified by maxine 檢核失敗,則終止Batch
			if (!CheckConfig()) {
				job.stopBatch();
				return BatchReturnCode.Succeed;
			}

			// 3. 開始執行商業邏輯
			_batchResult = DoBusiness();

			// 4. 通知批次作業管理系統工作正常結束
			job.writeLog("ProgramName" + "結束!!");
			job.writeLog("------------------------------------------------------------------");
			job.endTask();
			return BatchReturnCode.Succeed;
		} catch (Exception e) {
			logContext.setProgramException(e);
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
			if (job != null) {
				// Send to System Event
				job.writeLog("Batch Log目錄未設定，請修正");
				System.out.println("Batch Log目錄未設定，請修正");
			} else {

				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批w作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					logContext.setProgramException(e);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
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
			_logData = null;
		}
	}

	private boolean CheckConfig() {

		//String internalFileName = StringUtils.EMPTY;
		//Sysstat dbSYSSTAT = new Sysstat();

		try {
			_logData = new LogData();
			_logData.setChannel(FEPChannel.BATCH);
			_logData.setEj(0);
			_logData.setProgramName(_programName);
			_logData.setSubSys(SubSystem.INBK);

			// 2021/09/11 Modify by Ruling for 財金營運資料補遺
			_logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
			// .TxDate = _businessDate

			// add by Maxine on 2011/12/0 for
			// 1.判斷執行日(ZONE=’TWN’)非營業日, 不執行批次
			// 2.增加參數FORCERUN(Optional), 如輸入(/FORCERUN:’Y’), 非營業日亦可執行批次
			if (job.getArguments().containsKey("FORCERUN") && "Y".equals(job.getArguments().get("FORCERUN"))) {
				// 不管是否為營業日，強制執行。
			} else {
				if (!(job.isBsDay(ATMZone.TWN.toString()))) {
					job.writeLog("非營業日不執行批次");
					// job.abortTask();
					return false;
				}
			}
			List<Map<String, Object>> _defSYSSTAT2 = npschkExtMapper.getQueryAll();
			_defSYSSTAT = _defSYSSTAT2;
			if (_defSYSSTAT.size() < 1) {
				job.writeLog(ProgramName + "-SYSSTAT No Record");
				_logData.setRemark(ProgramName + "-SYSSTAT No Record");
				sendEMS(logContext);
				return false;
			}

			job.writeLog(ProgramName + "-_defSYSSTAT.SYSSTAT_LBSDY_FISC=" + _defSYSSTAT.get(0).get("SYSSTAT_LBSDY_FISC")
					+ ";");
			job.writeLog(ProgramName + "-_defSYSSTAT.SYSSTAT_HBKNO=" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + ";");
			
			Bsdays defBSDAYS = new Bsdays();
			if (job.getArguments().containsKey("TXDATE_B")) {
				txdate_b=job.getArguments().get("TXDATE_B");
			}else {
				txdate_b = new SimpleDateFormat("yyyyMMdd").format(new Date());
			}
			if (job.getArguments().containsKey("TXDATE_E")) {
				txdate_e=job.getArguments().get("TXDATE_E");
			}else {
				txdate_e = new SimpleDateFormat("yyyyMMdd").format(new Date());
			}
			if (job.getArguments().containsKey("TXTIME_B")) {	//未指定時預設為系統時間前35分鐘，必須有值且為6碼(暫無使用)
				txtime_b = job.getArguments().get("TXTIME_B");
			} else {
				Calendar beginTime = Calendar.getInstance();
				beginTime.add(Calendar.MINUTE, -35); //35分鐘前	
				Date date = beginTime.getTime();
				txtime_b = new SimpleDateFormat("HHmm").format(date) + "00";
			}
			if (job.getArguments().containsKey("TXTIME_E")) {	//未指定時預設為系統時間前5分鐘，必須有值且為6碼(暫無使用)
				txtime_e = job.getArguments().get("TXTIME_E");
			} else {
				Calendar endTime = Calendar.getInstance();
				endTime.add(Calendar.MINUTE, -5); 	//5分鐘前
				Date date = endTime.getTime();
				txtime_e = new SimpleDateFormat("HHmm").format(date) + "59";
			}
			//job.writeLog(ProgramName + "參數: TXDATE={" + _txdate + "}, BEGINTIME={" + _beginTime + "}, ENDTIME={"+ _endTime + "}");
			job.writeLog(ProgramName + "參數: TXDATE_B={" + txdate_b + "}, TXDATE_E={" + txdate_e + "}, TXTIME_B={"+txdate_b + "}, TXTIME_E={"+txtime_e);

			/* 讀取<BSDAY> */
			if(!getFISCTbsdy(txdate_b)){
				return false;
			}
			if(!getFISCTbsdy(txdate_e)){
				return false;
			}
			_fiscBusDate=new ArrayList<>(new HashSet<>(_fiscBusDate));
			Collections.sort(_fiscBusDate);
		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog(ProgramName + "-CheckConfig Exception=" + e.getMessage());
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			sendEMS(_logData);
			return false;
		} finally {
			//dbSYSSTAT = null;
			if (job != null) {
				job.writeLog(ProgramName + "結束!!");
				job.writeLog("------------------------------------------------------------------");
				job.dispose();
			}
		}
		return true;
	}

	private boolean getFISCTbsdy(String txdate){
		ArrayList tbsdy=new ArrayList() ;
		try{
			Bsdays defBSDAYS = new Bsdays();
			defBSDAYS.setBsdaysDate(txdate);
			defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
			Bsdays dtBSDAYS = bsdaysExtMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(),
					defBSDAYS.getBsdaysDate());
			if (dtBSDAYS == null) {
				job.writeLog("取不到日曆檔, BSDAYS_DATE={" + defBSDAYS.getBsdaysDate() + "}, BSDAYS_ZONE_CODE={"
						+ defBSDAYS.getBsdaysZoneCode() + "}");
				return false;
			} else {
				// 取到日曆檔，判斷工作日 1:工作日。0:假日
				if (dtBSDAYS.getBsdaysWorkday() == 1) {
					// 財金營業日=本營業日
					tbsdy.add(dtBSDAYS.getBsdaysDate());
					tbsdy.add(dtBSDAYS.getBsdaysNbsdy());
				} else {
					// 財金營業日=下營業日
					tbsdy.add(dtBSDAYS.getBsdaysNbsdy());
				}
				if(tbsdy != null){
					for(int i=0;i<tbsdy.size();i++){
						_fiscBusDate.add(tbsdy.get(i));
					}
				}
				job.writeLog("FEPTXN取財金營業日={" + _fiscTbsdy + "}, 財金次營業日={" + _fiscNbsdy + "}");
			}
		}catch (Exception e) {
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
		} finally {
		}
		return true;
	}

	private FEPReturnCode DoBusiness() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			Path p = Paths.get(_BatchOutputPath); // 路徑設定
			/* 確認資料夾是否存在  資料夾已存在 */
			if (Files.exists(p)) {
			}
			if (!Files.exists(p)) {
				/* 不存在的話,直接建立資料夾 */
				Files.createDirectory(p);
			}
			strFileName_CD = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "CD.TXT";
			File file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_CD));
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			file = null;
			strFileName_TF = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "TF.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_TF));
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			file = null;
			strFileName_TA = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "TA.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_TA));
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			file = null;
			strFileName_BT = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "BT.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_BT));
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			file = null;
			strFileName_BF = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "BF.TXT";
			file = new File(CleanPathUtil.cleanString(_BatchOutputPath + strFileName_BF));
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			file = null;

			//刪除INBK資料驗證檔
			if(!DeleteINBKDATAVALID()){
				return IOReturnCode.DeleteFail;
			}
			//寫入INBK資料驗證檔
			if(!InsertINBKDATAVALID()){
				return IOReturnCode.InsertFail;
			}

			if(_fiscBusDate !=null){
				for (Object tx:_fiscBusDate) {
					String txdate =tx.toString();
					// 查詢FEPTXN,取出BusinessDate,並且FEPTXN_REQ_RC有值的資料
					List<Map<String, Object>> dtFEPTXN = GetFEPTXN(txdate);
					if (null == dtFEPTXN) {
						job.writeLog("無FEPTXN資料");
						try {
							job.abortTask();
							job.stopBatch();
							if (job != null) {
								job.writeLog(txdate + "無資料!!");
								job.writeLog("------------------------------------------------------------------");
								job.dispose();
							}
							if (logContext != null) {
								logContext = null;
							}
						} catch (Exception ex) {
							logContext.setProgramException(ex);
							logContext.setProgramName(ProgramName);
							sendEMS(logContext);
						}
					}else{
						// 3.產生跨行提款交易資料補全明細檔
						GenerateBXXXCDFile(dtFEPTXN);
						// 4.產生跨行轉帳交易資料補全明細檔
						GenerateBXXXTFFile(dtFEPTXN);
						// 5.產生繳納稅費交易資料補全明細檔
						GenerateBXXXTAXFile(dtFEPTXN);
						// 6.產生全國性繳稅交易資料補全明細檔
						GenerateBXXXEBPPTAXFile(dtFEPTXN);
						// 7.產生全國性繳費交易資料補全明細檔
						GenerateBXXXEBPPPAYFile(dtFEPTXN);
					}
				}
			}

			//寫入INBK資料驗證檔
			if(!updateINBKDATAVALID()){
				return IOReturnCode.UpdateFail;
			}
		} catch (Exception e) {
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
		} finally {
		}
		return rtnCode;
	}

	private Boolean DeleteINBKDATAVALID() {
		String tdate = new SimpleDateFormat("yyyyMMdd").format(new Date());

		List<Inbkdatavalid> inbkdatavalid1 = inbkdatavalidExtMapper.select(tdate,_programName);
		if(inbkdatavalid1 != null){
			inbkdatavalidExtMapper.deleteINBKDATAVALID(tdate,_programName);
			return true;
		}
		return true;
	}

	private Boolean InsertINBKDATAVALID(){
		try{
			String tdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
			Inbkdatavalid inbkdatavalid=new Inbkdatavalid();
			inbkdatavalid.setInbkdatavalidDate(tdate);
			inbkdatavalid.setInbkdatavalidProgram(_programName);
			inbkdatavalid.setInbkdatavalidFilename(strFileName_CD);
			inbkdatavalid.setInbkdatavalidCompleteflag((long)0);
			inbkdatavalid.setInbkdatavalidRecord(0);
			//CD
			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
			//TF
			inbkdatavalid.setInbkdatavalidFilename(strFileName_TF);
			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
			//TA
			inbkdatavalid.setInbkdatavalidFilename(strFileName_TA);
			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
			//BT
			inbkdatavalid.setInbkdatavalidFilename(strFileName_BT);
			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
			//BF
			inbkdatavalid.setInbkdatavalidFilename(strFileName_BF);
			inbkdatavalidExtMapper.insertSelective(inbkdatavalid);
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			job.writeLog("寫入INBK資料驗證檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("寫入INBK資料驗證檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
					if (job != null) {
						job.writeLog(ProgramName + "結束!!");
						job.writeLog("------------------------------------------------------------------");
						job.dispose();
					}
					if (logContext != null) {
						logContext = null;
					}
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}
			return false;
		}
	}
	private Boolean updateINBKDATAVALID(){
		try{
			String tdate = new SimpleDateFormat("yyyyMMdd").format(new Date());
			Inbkdatavalid inbkdatavalid=new Inbkdatavalid();
			inbkdatavalid.setInbkdatavalidRecord(_cntCD);
			inbkdatavalid.setInbkdatavalidCompleteflag((long)1);
			inbkdatavalid.setUpdateTime(new Date());
			inbkdatavalid.setInbkdatavalidDate(tdate);
			inbkdatavalid.setInbkdatavalidProgram(_programName);
			inbkdatavalid.setInbkdatavalidFilename(strFileName_CD);
			//CD
			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
			//TF
			inbkdatavalid.setInbkdatavalidRecord(_cntTF);
			inbkdatavalid.setInbkdatavalidFilename(strFileName_TF);
			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
			//TA
			inbkdatavalid.setInbkdatavalidRecord(_cntTA);
			inbkdatavalid.setInbkdatavalidFilename(strFileName_TA);
			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
			//BT
			inbkdatavalid.setInbkdatavalidRecord(_cntBT);
			inbkdatavalid.setInbkdatavalidFilename(strFileName_BT);
			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
			//BF
			inbkdatavalid.setInbkdatavalidRecord(_cntBF);
			inbkdatavalid.setInbkdatavalidFilename(strFileName_BF);
			inbkdatavalidExtMapper.updateByPrimaryKeySelective(inbkdatavalid);
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			job.writeLog("更新INBK資料驗證檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("更新INBK資料驗證檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
					if (job != null) {
						job.writeLog(ProgramName + "結束!!");
						job.writeLog("------------------------------------------------------------------");
						job.dispose();
					}
					if (logContext != null) {
						logContext = null;
					}
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}
			return false;
		}
	}
	// 3.
	private void GenerateBXXXCDFile(List<Map<String, Object>> dtFEPTXN) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String strFileName = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "CD.TXT";

		// 2021/09/11 Modify by Ruling for 財金營運資料補遺：移除PCODE 2552
		StringBuilder sLine = null;
		String[] strQueryWhere = new String[] { "2510", "2541", "2542", "2543" };
		List<Map<String, Object>> drResult = dtFEPTXN.stream()
				.filter(t -> ArrayUtils.contains(strQueryWhere, t.get("FEPTXN_PCODE"))).collect(Collectors.toList());

		try {

			if (null == drResult || drResult.size() < 1 || "".equals(dtFEPTXN)) {
				job.writeLog("沒有跨行提款交易資料補全明細檔資料" + strFileName+"共0筆");
				return;
			} else {
				_cntCD += drResult.size();
				job.writeLog("跨行提款交易資料補全明細檔資料" + strFileName + "共" + drResult.size() + "筆");
			}

			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os, "Big5");
				sLine = null;
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				// 轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				// 交易代號 文數字 4 FEPTXN_PCODE
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				
				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				// 交易序號 文數字 10 FEPTXN_BKNO + FEPTXN_STAN
				// modified by Maxine on 2011/08/23 for spec Update
				sLine.append(StringUtils.rightPad(" ", 3, ' ')
						+ StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				// 交易時間 文數字 6 FEPTXN_TX_TIME
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				if(null == drResult.get(i).get("FEPTXN_TROUT_ACTNO")) {
					drResult.get(i).put("FEPTXN_TROUT_ACTNO", "");
				}
				// 帳號 文數字 16 FEPTXN_TROUT_ACTNO
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				// 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				// 空白
				sLine.append(" ");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 存款單位送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 財金送代理單位回應訊息 文數字 4 IF FEPTXN_BKNO ＝SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 代理單位送財金確認訊息 文數字 4 IF FEPTXN_BKNO ＝ SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 財金送存款單位確認訊息 文數字 4 IF FEPTXN_BKNO <> SYSSTAT_HBKNO
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}
				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				// 提款金額 數字 7 FEPTXN_TX_AMT(右靠左補0)
				sLine.append(
						StringUtils.leftPad(
								(new BigDecimal(Math.abs(Math.floor(Double.parseDouble(
										drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")))))) + "",
								7, '0'));

				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				// 交易日期 文數字 6 FEPTXN_TX_DATE
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 7, '0'));

				// 代理單位分行代號 文數字 4 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				// 存款單位分行代號 文數字 4 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));
				sLine.append(System.getProperty("line.separator"));
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生跨行提款交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生跨行提款交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					if (job != null) {
						job.writeLog(ProgramName + "結束!!");
						job.writeLog("------------------------------------------------------------------");
						job.dispose();
					}
					if (logContext != null) {
						logContext = null;
					}
					job.abortTask();
					job.stopBatch();
					
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

		} finally {
			
		}
	}

	// 4.
	private void GenerateBXXXTFFile(List<Map<String, Object>> dtFEPTXN) {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		String strFileName = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "TF.TXT";

		// modified by Maxine on 2011/08/23 for 8/22 修改, 含 2525 交易

		// Dim strQueryWhere As String = "FEPTXN_PCODE IN ('2521','2522','2523','2524')"
		StringBuilder sLine = null;
		String[] strQueryWhere = new String[] { "2521", "2522", "2523", "2524", "2525" };

		List<Map<String, Object>> drResult = dtFEPTXN.stream()
				.filter(t -> ArrayUtils.contains(strQueryWhere, t.get("FEPTXN_PCODE"))).collect(Collectors.toList());

		try {
			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有跨行提款交易資料補全明細檔資料" + strFileName+"共0筆");
				return;
			} else {
				_cntTF += drResult.size();
				job.writeLog("跨行提款交易資料補全明細檔資料" + strFileName + "共" + drResult.size() + "筆");
			}

			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os, "Big5");
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				// 轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				if(null == drResult.get(i).get("FEPTXN_TRIN_BKNO")) {
					drResult.get(i).put("FEPTXN_TRIN_BKNO", "");
				}
				// 轉入單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TRIN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				// 交易序號 文數字 10 FEPTXN_BKNO + FEPTXN_STAN
				// modified by Maxine on 2011/08/23 for spec Update
				sLine.append(StringUtils.rightPad(" ", 3, ' ')
						+ StringUtils.rightPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));
				// sLine.Append(dr("FEPTXN_BKNO").ToString().PadRight(3, " "c) &
				// dr("FEPTXN_STAN").ToString().PadLeft(7, "0"c))

				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				// 交易代號 文數字 4 FEPTXN_PCODE
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				// 交易日期及時間 文數字 6 FEPTXN_TX_DATE(轉成民國年取6位) + FEPTXN_TX_TIME
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 7, '0'));
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				// 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_TRIN_ACTNO")) {
					drResult.get(i).put("FEPTXN_TRIN_ACTNO", "");
				}
				// 轉入帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TRIN_ACTNO").toString(), 16, ' '));

				// 轉出帳號 文數字 16 FEPTXN_TROUT_ACTNO
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				// 空白
				sLine.append(StringUtils.rightPad(" ", 23, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 轉出(轉入)送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
						&& (drResult.get(i).get("FEPTXN_TROUT_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
								|| !("4".equals(
										StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ')
												.toString().substring(3, 4)))))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));

				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 財金送代理單位回應訊息 文數字 4 IF FEPTXN_BKNO ＝SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 代理單位送財金確認訊息 文數字 4 IF FEPTXN_BKNO ＝ SYSSTAT_HBKNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 財金送轉出(轉入)確認訊息 文數字 4 IF FEPTXN_BKNO <> SYSSTAT_HBKNO
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TROUT_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
								|| !("4".equals(drResult.get(i).get("FEPTXN_PCODE").toString().substring(3, 4))))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 轉入單位送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TROUT_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
								|| ("4".equals(drResult.get(i).get("FEPTXN_PCODE").toString().substring(3, 4))))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 財金送轉入單位確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TROUT_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))
								|| ("4".equals(drResult.get(i).get("FEPTXN_PCODE").toString().substring(3, 4))))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 空白
				sLine.append(StringUtils.rightPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				// 轉帳金額 數字 7 FEPTXN_TX_AMT(右靠左補0)
				sLine.append(
						StringUtils.leftPad(
								(new BigDecimal(Math.abs(Math.floor(Double.parseDouble(
										drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")))))) + "",
								7, '0'));
				sLine.append(System.getProperty("line.separator"));
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生跨行提款交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生跨行提款交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					
					job.abortTask();
					job.stopBatch();
					if (job != null) {
						job.writeLog(ProgramName + "結束!!");
						job.writeLog("------------------------------------------------------------------");
						job.dispose();
					}
					if (logContext != null) {
						logContext = null;
					}
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}
		} finally {
			
		}
	}

	/**
	 * <summary> 5. 產生繳納稅費交易資料補全明細檔 </summary> <param name="dtFEPTXN"></param>
	 * <remarks></remarks>
	 *
	 * @param dtFEPTXN
	 */
	private void GenerateBXXXTAXFile(List<Map<String, Object>> dtFEPTXN) {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 2012/07/10 Modify by Ruling for 修改繳納稅費交易資料補全明細檔名: B807TA.TXT
		String strFileName = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "TA.TXT";
		// Dim strFileName As String = "B" & _defSYSSTAT.SYSSTAT_HBKNO & "TAX.TXT"
		String[] strQueryWhere = new String[] { "2531", "2532" };
		StringBuilder sLine = null;
		List<Map<String, Object>> drResult = dtFEPTXN.stream()
				.filter(t -> ArrayUtils.contains(strQueryWhere, t.get("FEPTXN_PCODE"))).collect(Collectors.toList());

		try {

			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有繳納稅費交易資料補全明細檔資料" + strFileName+"共0筆");
				return;
			} else {
				_cntTA += drResult.size();
				job.writeLog("繳納稅費交易資料補全明細檔資料" + strFileName + "共" + drResult.size() + "筆");
			}
			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os, "Big5");
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				// 轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 轉入單位代號
				sLine.append(StringUtils.leftPad("000", 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 10, ' '));

				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				// 跨行交易序號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				// 交易代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TBSDY_FISC")) {
					drResult.get(i).put("FEPTXN_TBSDY_FISC", "");
				}
				// 交易營業日期
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TBSDY_FISC").toString()), 7, '0'));

				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				// 交易日期
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 7, '0'));

				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				// 交易時間
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				// 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				// 轉入帳號
				sLine.append(StringUtils.rightPad(" ", 16, ' '));

				if(null == drResult.get(i).get("FEPTXN_TROUT_ACTNO")) {
					drResult.get(i).put("FEPTXN_TROUT_ACTNO", "");
				}
				// 轉出帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 轉出送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				// 財金送代理單位回應訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 代理單位送財金確認訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				// 財金送轉出確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				// 轉帳金額
				sLine.append(StringUtils.leftPad((new BigDecimal(Math.abs(Math.floor(
						(Double.parseDouble(drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")) * 100)))))
						+ "", 13, '0'));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 15, ' '));

				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				// 繳款類別
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 5, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 6, ' '));

				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_RECON_SEQNO")) {
					drResult.get(i).put("FEPTXN_RECON_SEQNO", "");
				}
				// 銷帳編號
				if (!"15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString().substring(0, 2),
						2, ' '))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_RECON_SEQNO").toString(), 16, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 16, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TAX_UNIT")) {
					drResult.get(i).put("FEPTXN_TAX_UNIT", "");
				}
				// 機關代號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString().substring(0, 2),
						2, ' '))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TAX_UNIT").toString(), 3, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 3, ' '));
				}

				if(null == drResult.get(i).get("FEPTXN_IDNO")) {
					drResult.get(i).put("FEPTXN_IDNO", "");
				}
				// 身分證字號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString().substring(0, 2),
						2, ' '))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_IDNO").toString(), 11, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 11, ' '));
				}
				sLine.append(System.getProperty("line.separator"));
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生跨行提款交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生跨行提款交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
					if (job != null) {
						job.writeLog(ProgramName + "結束!!");
						job.writeLog("------------------------------------------------------------------");
						job.dispose();
					}
					if (logContext != null) {
						logContext = null;
					}
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

		} finally {
			
		}
	}

	/**
	 * 6. <summary> 產生全國性繳稅交易資料補全 </summary> <param name="dtFEPTXN"></
	 * <remarks></remarks>
	 * 
	 * @param dtFEPTXN
	 */
	private void GenerateBXXXEBPPTAXFile(List<Map<String, Object>> dtFEPTXN) {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 2012/07/10 Modify by Ruling for 修改全國性繳稅交易資料補全明細檔名: B807BT.TXT
		String strFileName = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "BT.TXT";
		// Dim strFileName As String = "B" & _defSYSSTAT.SYSSTAT_HBKNO & "EBPPTAX.TXT"
		String[] strQueryWhere = new String[] { "2567", "2568", "2569" };
		StringBuilder sLine = null;

		List<Map<String, Object>> drResult = dtFEPTXN.stream()
				.filter(t -> ArrayUtils.contains(strQueryWhere, t.get("FEPTXN_PCODE"))).collect(Collectors.toList());

		try {

			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有全國性繳稅交易資料補全明細檔資料" + strFileName+"共0筆");
				return;
			} else {
				_cntBT += drResult.size();
				job.writeLog("全國性繳稅交易資料補全明細檔資料" + strFileName + "共" + drResult.size() + "筆");
			}
			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os, "Big5");
				sLine = new StringBuilder("");

				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				
				if(null == drResult.get(i).get("FEPTXN_TBSDY_FISC")) {
					drResult.get(i).put("FEPTXN_TBSDY_FISC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_ACTNO")) {
					drResult.get(i).put("FEPTXN_TROUT_ACTNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_RECON_SEQNO")) {
					drResult.get(i).put("FEPTXN_RECON_SEQNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TAX_UNIT")) {
					drResult.get(i).put("FEPTXN_TAX_UNIT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_IDNO")) {
					drResult.get(i).put("FEPTXN_IDNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATM_TYPE")) {
					drResult.get(i).put("FEPTXN_ATM_TYPE", "");
				}
				
				// 代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 5.轉入單位代號
				sLine.append("000");

				// 6.空白
				sLine.append(StringUtils.leftPad(" ", 10, ' '));

				// 7.跨行交易序號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				// '8.交易代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				// '9.交易營業日期 todo
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TBSDY_FISC").toString()), 7, '0'));

				// '10.交易日期 todo
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 7, '0'));

				// '11 交易時間
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				// 12 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}

				// 13 轉入帳號
				sLine.append(StringUtils.rightPad(" ", 16, ' '));

				// 14 轉出帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				// 15 轉出送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 16 財金送代理單位回應訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 17 代理單位送財金確認訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 18 財金送轉出確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 19 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				// 20 轉帳金額
				sLine.append(StringUtils.leftPad((new BigDecimal(Math.abs(Math.floor(
						Double.parseDouble(drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")) * 100))))
						+ "", 13, '0'));
				
				// 21 空白
				sLine.append(StringUtils.leftPad(" ", 15, ' '));

				// 22 繳款類別
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 5, ' '));

				// 23 空白
				sLine.append(StringUtils.leftPad(" ", 6, ' '));

				// 24 銷帳編號
				if (!"15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 2, ' ')
						.substring(0, 2))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_RECON_SEQNO").toString(), 16, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 16, ' '));
				}
				
				// 25 機關代號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 2, ' ')
						.substring(0, 2))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TAX_UNIT").toString(), 3, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 3, ' '));
				}

				// 26 身分證字號
				if ("15".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 2, ' ')
						.substring(0, 2))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_IDNO").toString(), 11, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 11, ' '));
				}

				// 27 空白
				sLine.append(StringUtils.leftPad(" ", 27, ' '));

				// 28 終端機設備型態
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATM_TYPE").toString(), 4, ' '));
				sLine.append(System.getProperty("line.separator"));
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;

			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生全國性繳稅交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生全國性繳稅交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
					if (job != null) {
						job.writeLog(ProgramName + "結束!!");
						job.writeLog("------------------------------------------------------------------");
						job.dispose();
					}
					if (logContext != null) {
						logContext = null;
					}
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

		} finally {
			
		}

	}

	/**
	 * <summary> 7. 產生全國性繳費交易資料補全明細檔 </summary> <param name="dtFEPTXN"></param>
	 * <remarks></remarks>
	 *
	 * @param dtFEPTXN
	 */
	private void GenerateBXXXEBPPPAYFile(List<Map<String, Object>> dtFEPTXN) {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 2012/07/10 Modify by Ruling for 修改全國性繳費交易資料補全明細檔檔名: B807BF.TXT
		String strFileName = "B" + _defSYSSTAT.get(0).get("SYSSTAT_HBKNO") + "BF.TXT";
		// Dim strFileName As String = "B" & _defSYSSTAT.SYSSTAT_HBKNO & "EBPPPAY.TXT"
		String[] strQueryWhere = new String[] { "2261", "2262", "2263", "2264", "2561", "2562", "2563", "2564" };
		List<Map<String, Object>> drResult = dtFEPTXN.stream()
				.filter(t -> ArrayUtils.contains(strQueryWhere, t.get("FEPTXN_PCODE"))).collect(Collectors.toList());
		StringBuilder sLine = null;

		try {

			if (null == drResult || drResult.size() < 1) {
				job.writeLog("沒有全國性繳費交易資料補全明細檔資料" + strFileName+"共0筆");
				return;
			} else {
				_cntBF += drResult.size();
				job.writeLog("全國性繳費交易資料補全明細檔資料" + strFileName + "共" + drResult.size() + "筆");
			}
			OutputStream os = new FileOutputStream(_BatchOutputPath + strFileName);
			Writer sw = null;

			for (int i = 0; i < drResult.size(); i++) {
				sw = new OutputStreamWriter(os, "Big5");
				sLine = new StringBuilder("");

				
				if(null == drResult.get(i).get("FEPTXN_BKNO")) {
					drResult.get(i).put("FEPTXN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TRIN_BKNO")) {
					drResult.get(i).put("FEPTXN_TRIN_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_STAN")) {
					drResult.get(i).put("FEPTXN_STAN", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PCODE")) {
					drResult.get(i).put("FEPTXN_PCODE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TBSDY_FISC")) {
					drResult.get(i).put("FEPTXN_TBSDY_FISC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_DATE")) {
					drResult.get(i).put("FEPTXN_TX_DATE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_TIME")) {
					drResult.get(i).put("FEPTXN_TX_TIME", "");
				}
				if(null == drResult.get(i).get("FEPTXN_ATMNO")) {
					drResult.get(i).put("FEPTXN_ATMNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TRIN_ACTNO")) {
					drResult.get(i).put("FEPTXN_TRIN_ACTNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TROUT_BKNO")) {
					drResult.get(i).put("FEPTXN_TROUT_BKNO", "");
				}
				if(null == drResult.get(i).get("FEPTXN_REP_RC")) {
					drResult.get(i).put("FEPTXN_REP_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}
				if(null == drResult.get(i).get("FEPTXN_TX_AMT")) {
					drResult.get(i).put("FEPTXN_TX_AMT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PAYTYPE")) {
					drResult.get(i).put("FEPTXN_PAYTYPE", "");
				}
				if(null == drResult.get(i).get("FEPTXN_BUSINESS_UNIT")) {
					drResult.get(i).put("FEPTXN_BUSINESS_UNIT", "");
				}
				if(null == drResult.get(i).get("FEPTXN_PAYNO")) {
					drResult.get(i).put("FEPTXN_PAYNO", "");
				}
				
				// 1.代理單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BKNO").toString(), 3, ' '));

				// 2.空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 3.轉出單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_BKNO").toString(), 3, ' '));

				// 4.空白
				sLine.append(StringUtils.leftPad(" ", 7, ' '));

				// 5.轉入單位代號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TRIN_BKNO").toString(), 3, ' '));

				// 6 空白
				sLine.append(StringUtils.leftPad(" ", 10, ' '));

				// 7 跨行交易序號
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_STAN").toString(), 7, '0'));

				// 8 交易代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' '));

				// 9 交易營業日期
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TBSDY_FISC").toString()), 7, '0'));
				
				// 10 交易日期
				sLine.append(StringUtils.leftPad(
						CalendarUtil.adStringToROCString(drResult.get(i).get("FEPTXN_TX_DATE").toString()), 7, '0'));

				// 11 交易時間
				sLine.append(StringUtils.leftPad(drResult.get(i).get("FEPTXN_TX_TIME").toString(), 6, '0'));

				// '12 自動付款機代號 文數字 8 FEPTXN_ATMNO
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, '0'));
				} else {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_ATMNO").toString(), 8, ' '));
				}
				
				// 13 轉入帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TRIN_ACTNO").toString(), 16, ' '));

				// 14 轉出帳號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_TROUT_ACTNO").toString(), 16, ' '));

				// 15 轉出(轉入)送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& ((drResult.get(i).get("FEPTXN_TROUT_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))))
						|| (!"4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ')
								.substring(3, 4)))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 16 財金送代理單位回應訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 因 object 轉字串會出線null跳錯誤 加上
				if (null == drResult.get(i).get("FEPTXN_CON_RC")) {
					drResult.get(i).put("FEPTXN_CON_RC", "");
				}

				// 17 代理單位送財金確認訊息
				if (drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO"))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}
				
				// 18 財金送轉出(轉入)確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& ((drResult.get(i).get("FEPTXN_TROUT_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
								|| (!"4".equals(
										StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ')
												.substring(3, 4))))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 19 空白
				sLine.append(StringUtils.leftPad(" ", 4, ' '));

				// 20 轉帳金額
				sLine.append(StringUtils.leftPad((new BigDecimal(Math.abs(Math.floor(
						Double.parseDouble(drResult.get(i).get("FEPTXN_TX_AMT").toString().replace(",", "")) * 100))))
						+ "", 13, '0'));
				
				// 21 空白
				sLine.append(" ");

				// 22 轉入單位送財金回應訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TRIN_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& ("4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ')
								.substring(3, 4)))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_REP_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 23 財金送轉入單位確認訊息
				if (!(drResult.get(i).get("FEPTXN_BKNO").toString().equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& (drResult.get(i).get("FEPTXN_TRIN_BKNO").toString()
								.equals(_defSYSSTAT.get(0).get("SYSSTAT_HBKNO")))
						&& ("4".equals(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PCODE").toString(), 4, ' ')
								.substring(3, 4)))) {
					sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_CON_RC").toString(), 4, ' '));
				} else {
					sLine.append(StringUtils.rightPad(" ", 4, ' '));
				}

				// 24 空白
				sLine.append(StringUtils.leftPad(" ", 6, ' '));

				// 25 繳款類別
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYTYPE").toString(), 5, ' '));

				// 26 委託單位代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_BUSINESS_UNIT").toString(), 8, ' '));

				// 27 費用代號
				sLine.append(StringUtils.rightPad(drResult.get(i).get("FEPTXN_PAYNO").toString(), 4, ' '));
				sLine.append(System.getProperty("line.separator"));
				sw.write(sLine.toString());
				sw.flush();
				if(i == (drResult.size()-1)) {
					if (sw != null) {
						sw.close();
						sw = null;
					}
				}

				sLine = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			job.writeLog("產生全國性繳費交易資料補全明細檔發生異常錯誤");
			job.writeLog(e.getMessage());
			_logData.setProgramException(e);
			_logData.setRemark("產生全國性繳費交易資料補全明細檔發生異常錯誤");
			sendEMS(_logData);
			if (job != null) {
				job.writeErrorLog(e, e.getMessage());
				job.writeLog(ProgramName + "失敗!!");
				job.writeLog("------------------------------------------------------------------");
				// 通知批次作業管理系統工作失敗,暫停後面流程
				try {
					job.abortTask();
					job.stopBatch();
					if (job != null) {
						job.writeLog(ProgramName + "結束!!");
						job.writeLog("------------------------------------------------------------------");
						job.dispose();
					}
					if (logContext != null) {
						logContext = null;
					}
				} catch (Exception ex) {
					logContext.setProgramException(ex);
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
				}
			}

		} finally {
			
		}
	}

	// <summary>
	// Query FEPTXN
	// </summary>
	private List<Map<String, Object>> GetFEPTXN(String txdate) {

		// 2021/09/11 Modify by Ruling for 財金營運資料補遺 _fiscTbsdy.substring(6, 8) _fiscTbsdy
		
		//--ben start 3/3(2022/9/14)----
		String feptxnSuffix = txdate.substring(6, 8);	//讀取的資料表名 ex. FEPTXN01、FEPTXN15
		String txdateb = txdate_b+txtime_b;
		String txdatee = txdate_e+txtime_e;

		//List<Map<String, Object>> dtFEPTXN = feptxnExtMapper.GetFEPTXNForBT010080(_fiscTbsdy.substring(6, 8),
		//		_isWorkDay,_fiscTbsdy, _fiscNbsdy, _beginTime, _endTime);
		List<Map<String, Object>> dtFEPTXN = feptxnExtMapper.GetFEPTXNForBT010080(feptxnSuffix,txdateb,txdatee);
		//--ben end 3/3(2022/9/14)----
		
		if (dtFEPTXN == null || dtFEPTXN.size() == 0) {
			job.writeLog("FEPTXN查無資料");
			return null;
		}
		return dtFEPTXN;
	}
}
