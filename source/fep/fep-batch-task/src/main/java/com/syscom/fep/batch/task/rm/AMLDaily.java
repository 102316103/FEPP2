package com.syscom.fep.batch.task.rm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmstatExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.vo.constant.ZoneCode;

/**
 * 負責處理Reset AML傳送Flag
 *
 * @author xingyun_yang
 * @create 2021/12/27
 */
public class AMLDaily implements Task {

	private SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
	private RmstatExtMapper rmstatExtMapper = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
	private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
	// 程式名稱
	private String _ProgramName = AMLDaily.class.getSimpleName();
	private Boolean _BatchResult = false;
	private String _BatchLogPath = StringUtils.EMPTY;
	private BatchJobLibrary _Job = null;
	private LogData _LogData = null;
	/**
	 * 主流程
	 * Batch主流程, 從主流程呼叫各子流程, 若子流程發生ex則throw至主流程並結束AbortTask
	 * 主流程每一個步驟依據_batchResult判斷是否往下執行
	 *
	 * @param args args
	 */
	@Override
	public BatchReturnCode execute(String[] args) {
		// 自動生成的方法存根
		try {
			// 1. 初始化相關批次物件及拆解傳入參數
			initialBatch(args);

			// 2. 檢核批次參數是否正確, 若正確則啟動批次工作
			_Job.writeLog("------------------------------------------------------------------");
			_Job.writeLog(_ProgramName + "開始");

			_Job.startTask();
			Bsdays defBsdays = new Bsdays();
			defBsdays.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBsdays.setBsdaysZoneCode(ZoneCode.TWN);
			defBsdays = bsdaysExtMapper.selectByPrimaryKey(defBsdays.getBsdaysZoneCode(), defBsdays.getBsdaysDate());
			if (defBsdays != null) {
				if (!DbHelper.toBoolean(defBsdays.getBsdaysWorkday())) {
					_Job.writeLog("非營業日不能執行");
					_Job.endTask();
				}
			}

			// 3. 批次主要處理流程
			_BatchResult = mainProcess();

			// 4. 通知批次作業管理系統工作正常結束
			if (_BatchResult) {
				_Job.writeLog(_ProgramName + "正常結束!!");
				_Job.writeLog("------------------------------------------------------------------");
				_Job.endTask();
			} else {
				_Job.writeLog(_ProgramName + "不正常結束，停止此批次作業!!");
				_Job.writeLog("------------------------------------------------------------------");
				_Job.abortTask();
			}
			return BatchReturnCode.Succeed;
		} catch (Exception ex) {
			if (_Job == null) {
				System.out.println(ex.toString());
			} else {
				_LogData.setProgramName(_ProgramName);
				_LogData.setProgramException(ex);
				BatchJobLibrary.sendEMS(_LogData);
				_Job.writeLog(ex.toString());
				_Job.writeLog(_ProgramName + "失敗!!");
				_Job.writeLog("------------------------------------------------------------------");
				// 通知批w作業管理系統工作失敗,暫停後面流程
				try {
					_Job.abortTask();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (_Job != null) {
				_Job.writeLog(_ProgramName + "結束!!");
				_Job.writeLog("------------------------------------------------------------------");
				_Job.dispose();
				_Job = null;
			}
			if (_LogData != null) {
				_LogData = null;
			}

		}

	}

	/**
	 * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
	 *
	 * @param args
	 */
	private void initialBatch(String[] args) {

		// 0. 初始化_logData物件,傳入工作執行參數
		_LogData = new LogData();
		_LogData.setChannel(FEPChannel.BATCH);
		_LogData.setEj(0);
		_LogData.setProgramName(_ProgramName);

		// 1. 檢查Batch Log目錄參數
		_BatchLogPath = RMConfig.getInstance().getBatchLogPath().trim();
		if (StringUtils.isBlank(_BatchLogPath)) {
			System.out.println("Batch Log目錄未設定，請修正");
			return;
		}
		RefBase<String []> refBase = new RefBase<>(args);
		toUpperArgs(refBase);
		args = refBase.get();

		// 2. 初始化BatchJob物件,傳入工作執行參數
		_Job = new BatchJobLibrary(this, args, _BatchLogPath);

		// 3. 顯示help說明
		if (_Job.getArguments().containsKey("?")) {
			displayUsage();
			return;
		}
	}

	/**
	 * 資料處理
	 * 將委託單位代號檔匯入繳費網
	 */
	private Boolean mainProcess() {
		Sysstat _defSYSSTAT = new Sysstat();
		try {
			_defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			_defSYSSTAT = sysstatExtMapper.selectByPrimaryKey(SysStatus.getPropertyValue().getSysstatHbkno());
			if (!"7".equals(_defSYSSTAT.getSysstatAoct1100())) {
				_Job.writeLog("財金匯款尚未checkout不能執行本程式");
				return false;
			}
			rmstatExtMapper.updateAmlDaily();
			_Job.writeLog(String.format("執行成功"));
		} catch (Exception e) {
			_Job.writeLog(e.toString());
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Other Function
	 * <p>
	 * 針對批次的參數，全部轉換成大寫
	 */
	private void toUpperArgs(RefBase<String[]> args) {
		//從第5個參數開始 才是我們自定義的參數，前4個(0,1,2,3共4個)是batchjob Service使用的
		for (int i = 4; i < args.get().length; i++) {
			args.get()[i] = args.get()[i].toUpperCase();
		}
	}

	/**
	 * 針對批次的參數，撰寫說明
	 */
	private void displayUsage() {
		System.out.println("USAGE:");
		System.out.println("  AMLDaily Options");
		System.out.println();
		System.out.println("  Options:");
		System.out.println("      /?             Display this help message.");
		System.out.println();
	}

}
