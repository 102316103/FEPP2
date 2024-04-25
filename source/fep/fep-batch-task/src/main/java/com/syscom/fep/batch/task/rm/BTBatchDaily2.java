package com.syscom.fep.batch.task.rm;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Sysstat;
import org.apache.commons.lang3.StringUtils;

public class BTBatchDaily2 implements Task {
	private String _ProgramName = BTBatchDaily2.class.getSimpleName(); //程式名稱
	private boolean _BatchResult = false;
	private String _BatchLogPath = "";
	private BatchJobLibrary _Job = null;
	private LogData _LogData = null;


	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			//1. 初始化相關批次物件及拆解傳入參數
			initialBatch(args);

			//2. 檢核批次參數是否正確, 若正確則啟動批次工作
			_Job.writeLog("------------------------------------------------------------------");
			_Job.writeLog(_ProgramName + "開始!");

			_Job.startTask();

			//3. 批次主要處理流程
			_BatchResult = mainProcess();

			//4. 通知批次作業管理系統工作正常結束
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
				_LogData.setProgramName(_ProgramName + ".execute");
				_LogData.setProgramException(ex);
				BatchJobLibrary.sendEMS(_LogData);
				_Job.writeLog(ex.toString());
				_Job.writeLog(_ProgramName + "失敗!!");
				_Job.writeLog("------------------------------------------------------------------");
				//通知批次作業管理系統工作失敗,暫停後面流程
				try {
					_Job.abortTask();
				} catch (Exception e) {
					_LogData.setProgramName(_ProgramName + ".execute");
					_LogData.setProgramException(ex);
					BatchJobLibrary.sendEMS(_LogData);
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

	public void initialBatch(String[] args) {
		//0. 初始化_logData物件,傳入工作執行參數
		_LogData = new LogData();
		_LogData.setChannel(FEPChannel.BATCH);
		_LogData.setEj(0);
		_LogData.setProgramName(_ProgramName);

		//1. 檢查Batch Log目錄參數
		//modified by 榮升 2020/07/31 改用 Syscom.FEP10.Common.Config
		_BatchLogPath = RMConfig.getInstance().getBatchLogPath().trim();
		//_BatchLogPath = RMConfig.Instance.BatchLogPath.Trim
		if (StringUtils.isBlank(_BatchLogPath)) {
			System.out.println("Batch Log目錄未設定，請修正");
			return;
		}
		RefBase<String []> refBase = new RefBase<>(args);
		toUpperArgs(refBase);
		args = refBase.get();
		//2. 初始化BatchJob物件,傳入工作執行參數
		_Job = new BatchJobLibrary(this, args, _BatchLogPath);

		//3. 顯示help說明
		if (_Job.getArguments().containsKey("?")) {
			displayUsage();
			return;
		}

	}

	private boolean mainProcess() {
		boolean result = true;
		SysstatExtMapper dbSYSSTAT = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
		AllbankExtMapper allbankExtMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
		int rtn = 0;

		Sysstat _defSYSSTAT = new Sysstat();

		try {
			_defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			Sysstat sysstat = dbSYSSTAT.selectByPrimaryKey(_defSYSSTAT.getSysstatHbkno());
			if(sysstat != null){
				_defSYSSTAT = sysstat;
			}
			//Fly 2016/11/15 條件調整，應判斷本行狀態為0-House Keeping完成
			if (!"0".equals(_defSYSSTAT.getSysstatMbact1000())) {
				if (_Job.getArguments().containsKey("FORCERUN") && _Job.getArguments().get("FORCERUN").equalsIgnoreCase("TRUE")) {
					_Job.writeLog("強制執行程式");
				} else {
					_Job.writeLog("本日匯款已CheckIN不能執行本程式");
					return true;
				}
			}
			rtn = allbankExtMapper.updateAllbankRMforward();
			_Job.writeLog("執行完成 共" + rtn + "筆");
		}
		catch (Exception ex) {
			_Job.writeLog(ex.toString());
			return false;
		} finally {

		}
		return true;
	}

	private void toUpperArgs(RefBase<String[]> args) {
		//從第5個參數開始 才是我們自定義的參數，前4個(0,1,2,3共4個)是batchjob Service使用的
		for (int i = 4; i < args.get().length; i++) {
			args.get()[i] = args.get()[i].toUpperCase();
		}
	}

	private void displayUsage() {
		System.out.println("USAGE:");
		System.out.println("  BTBatchDaily2 Options");
		System.out.println();
		System.out.println("  Options:");
		System.out.println("      /?             Display this help message.");
		System.out.println();
	}
}
