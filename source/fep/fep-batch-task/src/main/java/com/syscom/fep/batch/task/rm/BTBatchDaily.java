package com.syscom.fep.batch.task.rm;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.Rmfiscin1ExtMapper;
import com.syscom.fep.mybatis.ext.mapper.Rmfiscin4ExtMapper;
import com.syscom.fep.mybatis.ext.mapper.Rmfiscout1ExtMapper;
import com.syscom.fep.mybatis.ext.mapper.Rmfiscout4ExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminsnoExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmintExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmnoctlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutsnoExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmstatExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Fcrmstat;
import com.syscom.fep.mybatis.model.Rmint;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 負責處理通匯相關序號及通匯狀態維護
 *
 * @author xingyun_yang
 * @create 2022/1/3
 */
public class BTBatchDaily implements Task {

	private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
	private RmouttExtMapper rmouttExtMapper = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
	private RmintExtMapper rmintExtMapper = SpringBeanFactoryUtil.getBean(RmintExtMapper.class);
	private SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
	private RmstatExtMapper rmstatExtMapper = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
	private Rmfiscout1ExtMapper rmfiscout1ExtMapper = SpringBeanFactoryUtil.getBean(Rmfiscout1ExtMapper.class);
	private Rmfiscout4ExtMapper rmfiscout4ExtMapper = SpringBeanFactoryUtil.getBean(Rmfiscout4ExtMapper.class);
	private Rmfiscin1ExtMapper rmfiscin1ExtMapper = SpringBeanFactoryUtil.getBean(Rmfiscin1ExtMapper.class);
	private Rmfiscin4ExtMapper rmfiscin4ExtMapper = SpringBeanFactoryUtil.getBean(Rmfiscin4ExtMapper.class);
	private RminsnoExtMapper rminsnoExtMapper = SpringBeanFactoryUtil.getBean(RminsnoExtMapper.class);
	private RmoutsnoExtMapper rmoutsnoExtMapper = SpringBeanFactoryUtil.getBean(RmoutsnoExtMapper.class);
	private RmnoctlExtMapper rmnoctlExtMapper = SpringBeanFactoryUtil.getBean(RmnoctlExtMapper.class);
	private AllbankExtMapper allbankExtMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);

	private BatchJobLibrary job = null;
	@SuppressWarnings("unused")
	private Boolean _batchResult = false;
	private OutputStreamWriter _batchLog;
	private String _batchLogPath = "";
	private LogData _logData = null;
	Sysstat _defSYSSTAT = new Sysstat();
	Fcrmstat _defFCRMSTAT = new Fcrmstat();
	Rmstat _defRMSTAT = new Rmstat();

	// 判斷是否要清RMNOCTL, RMOUTT, RMINT
	private Boolean _ClearFlag = true;

	private String notification = StringUtils.EMPTY;
	private FEPReturnCode rtnCode;
	private Boolean _stopBatch = false;

	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			// 自動生成的方法存根
			// 0.
			// 初始化BatchJob物件,傳入工作執行參數
			_batchLogPath = RMConfig.getInstance().getBatchLogPath().trim();
			if (StringUtils.isBlank(_batchLogPath)) {
				System.out.println("Batch Log目錄未設定，請修正");
				return BatchReturnCode.Succeed;
			}

			job = new BatchJobLibrary(this, args, _batchLogPath);
			job.writeLog("BTBatchDaily start!");
			// 開始工作內容
			try {
				job.startTask();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Modify by Jim, 2012/2/17, 檢核是否有帶CLEAR參數
			if (!checkConfig()) {
				try {
					job.abortTask();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return BatchReturnCode.Succeed;
			}

			if (!_stopBatch) {
				// 檢核財金是否Check Out
				if (!checkSTAT()) {
					job.writeLog(notification);
					try {
						job.abortTask();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return BatchReturnCode.Succeed;
				}

				// 更新序號相關檔
				if (updateBTBatch()) {
					job.writeLog("Update data success!");
				} else {
					job.writeLog(notification);
					job.writeLog(TxHelper.getMessageFromFEPReturnCode(rtnCode));
				}
				// Truncate 暫存及異常檔
				if (_ClearFlag) {
					if (truncateBTBatch()) {
						_batchResult = true;
						job.writeLog("Truncate data success!");
					} else {
						job.writeLog(notification);
						job.writeLog(TxHelper.getMessageFromFEPReturnCode(rtnCode));
					}
				} else {
					job.writeLog("CLEAR=" + _ClearFlag + ", 不Truncate RMOUTT and RMINT");
				}
				// 通知批次作業管理系統工作正常結束
				job.writeLog("BTBatchDaily end!");
				try {
					job.endTask();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				job.stopBatch();
				job.writeLog("BTBatchDaily end by StopBatch!");
			}
			return BatchReturnCode.Succeed;
		} catch (Exception ex) {
			// Send EMS
			// 通知批次作業管理系統工作失敗,暫停後面流程
			_logData.setRemark("RM_BatchDaily-Main, 批次執行發生例外, ex=" + ex.toString());
			BatchJobLibrary.sendEMS(_logData);
			job.writeLog("BTBatchDaily Exception, ex=" + ex.toString());
			try {
				job.abortTask();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (job != null) {
				job.dispose();
				job = null;
			}
			if (_batchLog != null) {
				try {
					_batchLog.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				_batchLog = null;
			}
		}
	}

	// 檢核Batch所需參數是否足夠
	private boolean checkConfig() {
		boolean rtn = false;
		Bsdays defBsdays = new Bsdays();
		try {
			// Jim, 2012/3/5, 判斷是否為營業日
			defBsdays.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBsdays.setBsdaysZoneCode(ZoneCode.TWN);
			defBsdays = bsdaysExtMapper.selectByPrimaryKey(defBsdays.getBsdaysZoneCode(), defBsdays.getBsdaysDate());
			if (defBsdays != null) {
				if (!DbHelper.toBoolean(defBsdays.getBsdaysWorkday())) {
					job.writeLog("非營業日不能執行");
					_stopBatch = true;
					return true;
				}
			}

			if (job.getArguments().containsKey("CLEAR")) {
				_ClearFlag = job.getArguments().containsKey("CLEAR");
				job.writeLog("傳入參數CLEAR=" + _ClearFlag.toString());
			} else {
				_ClearFlag = true;
				job.writeLog("未傳入參數CLEAR");
			}
			return rtn;
		} catch (RuntimeException ex) {
			job.writeLog("CheckConfig exception, ex=" + ex.toString());
			// 這邊出錯還是要可以執行
			return true;
		}
	}

	/**
	 * 檢核財金是否Check Out
	 * syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值
	 */
	private boolean checkSTAT() {

		try {
			_defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			_defSYSSTAT = sysstatExtMapper.selectByPrimaryKey(_defSYSSTAT.getSysstatHbkno());
			if (!"7".equals(_defSYSSTAT.getSysstatAoct1100())) {
				notification = "財金匯款尚未checkout不能執行本程式";
				return false;
			}
			// dtFCRMSTAT = dbFCRMSTAT.QueryAllData("")
			// For Each dr As DataRow In dtFCRMSTAT.Rows
			// If dr.Item("FCRMSTAT_AOCTRM").ToString <> "7" Then
			// Notification = "財金外幣匯款尚未checkout不能執行本程式"
			// Return False
			// End If
			// Next
			return true;
		} catch (Exception ex) {
			job.writeLog("checkSTAT 發生例外, ex=" + ex.getMessage());
			_logData.setRemark("RM_BatchDaily-checkSTAT, 檢核財金是否Check Out發生例外, ex=" + ex.toString());
			BatchJobLibrary.sendEMS(_logData);
			return false;
		}
	}

	/**
	 * 資料庫相關
	 * 更新相關序號檔
	 * syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值
	 */
	private Boolean updateBTBatch() {

		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

		try {
			// 更新系統狀態檔
			_defSYSSTAT.setSysstatAoct1000("1");
			_defSYSSTAT.setSysstatAoct1100("1");
			_defSYSSTAT.setSysstatAoct1200("1");
			_defSYSSTAT.setSysstatAoct1300("1");
			_defSYSSTAT.setSysstatAoct1400("1");
			_defSYSSTAT.setSysstatMbact1000("0");
			_defSYSSTAT.setSysstatMbact1100("0");
			_defSYSSTAT.setSysstatMbact1200("0");
			_defSYSSTAT.setSysstatMbact1300("0");
			_defSYSSTAT.setSysstatMbact1400("0");
			if (sysstatExtMapper.updateByPrimaryKeySelective(_defSYSSTAT) <= 0) {
				notification = "更新系統狀態檔失敗";
				rtnCode = IOReturnCode.SYSSTATUpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("SYSSTAT Update OK");
			}

			// 更新匯款狀態檔
			_defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			_defRMSTAT.setRmstatFisciFlag1("Y");
			_defRMSTAT.setRmstatFisciFlag4("Y");
			_defRMSTAT.setRmstatFiscoFlag1("Y");
			_defRMSTAT.setRmstatFiscoFlag4("Y");
			if (rmstatExtMapper.updateByPrimaryKeySelective(_defRMSTAT) <= 0) {
				notification = "更新匯款狀態檔失敗";
				rtnCode = IOReturnCode.RMSTATUpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("RMSTAT Update OK");
			}
			// 更新外幣匯款狀態檔
			// _defFCRMSTAT.FCRMSTAT_FISCI_FLAG1 = "Y"
			// _defFCRMSTAT.FCRMSTAT_FISCI_FLAG4 = "Y"
			// _defFCRMSTAT.FCRMSTAT_FISCO_FLAG1 = "Y"
			// _defFCRMSTAT.FCRMSTAT_FISCO_FLAG4 = "Y"
			// _defFCRMSTAT.FCRMSTAT_AOCTRM = "1"
			// _defFCRMSTAT.FCRMSTAT_AOCTRM1 = "1"
			// _defFCRMSTAT.FCRMSTAT_AOCTRM4 = "1"
			// _defFCRMSTAT.FCRMSTAT_MBACTRM = "0"
			// _defFCRMSTAT.FCRMSTAT_MBACTRM1 = "0"
			// _defFCRMSTAT.FCRMSTAT_MBACTRM4 = "0"
			// If dbFCRMSTAT.UpdateFCRMSTATAll(_defFCRMSTAT) < 0 Then
			// Notification = "更新外幣匯款狀態檔失敗"
			// rtnCode = IOReturnCode.FCRMSTATUpdateError
			// db.RollbackTransaction()
			// Return False
			// End If
			//
			// RMFISCOUT1(匯出電文序號檔)
			if (rmfiscout1ExtMapper.updateRMFISCOUT1forBatchDaily() <= 0) {
				notification = "更新匯出電文序號檔失敗";
				rtnCode = IOReturnCode.RMFISCOUT1UpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("RMFISCOUT1 Update OK");
			}

			// RMFISCOUT4(一般通訊匯出電文序號檔)
			if (rmfiscout4ExtMapper.updateRMFISCOUT4forBatchDaily() <= 0) {
				notification = "更新一般通訊匯出電文序號檔檔失敗";
				rtnCode = IOReturnCode.RMFISCOUT4UpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("RMFISCOUT4 Update OK");
			}

			// RMFISCIN1(匯入電文序號檔)
			if (rmfiscin1ExtMapper.updateRMFISCIN1forBatchDaily() <= 0) {
				notification = "更新匯入電文序號檔失敗";
				rtnCode = IOReturnCode.RMFISCIN1UpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("RMFISCIN1 Update OK");
			}

			// RMFISCIN4(一般通訊匯入電文序號檔)
			if (rmfiscin4ExtMapper.updateRMFISCIN4forBatchDaily() <= 0) {
				notification = "更新一般通訊匯入電文序號檔失敗";
				rtnCode = IOReturnCode.RMFISCIN4UpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("RMFISCIN4 Update OK");
			}

			// RMINSNO(匯入通匯序號檔)
			if (rminsnoExtMapper.updateRMINSNOforBatchDaily() <= 0) {
				notification = "更新匯入通匯序號檔失敗";
				rtnCode = IOReturnCode.RMINSNOUpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("RMINSNO Update OK");
			}

			// RMOUTSNO(匯出通匯序號檔)
			if (rmoutsnoExtMapper.updateRMOUTSNOforBatchDaily() <= 0) {
				notification = "更新匯出通匯序號檔失敗";
				rtnCode = IOReturnCode.RMOUTSNOUPDATEOERROR;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("RMOUTSNO Update OK");
			}

			// Jim, 2012/2/17, 判斷是否需要清RMNOCTL
			if (_ClearFlag) {
				// Candy add Update RMNOCTL/FCRMNOCTL
				// RMNOCTL(匯款登錄序號控制檔)
				if (rmnoctlExtMapper.updateRMNOCTLforBatchDaily() <= 0) {
					notification = "更新匯款登錄序號控制檔失敗";
					rtnCode = IOReturnCode.RMNOCTLUpdateError;
					transactionManager.rollback(txStatus);
					return false;
				} else {
					job.writeLog("RMNOCTL Update OK");
				}
			} else {
				job.writeLog("CLEAR=" + _ClearFlag.toString() + ", RMNOCTL 不更新");
			}
			// 'FCRMNOCTL(外幣匯款登錄序號控制檔)
			// If dbFCRMNOCTL.UpdateFCRMNOCTLforBatchDaily < 0 Then
			// Notification = "更新外幣匯款登錄序號控制檔失敗"
			// rtnCode = IOReturnCode.FCRMNOCTLUPDATEFERROR
			// db.RollbackTransaction()
			// Return False
			// End If
			// 'FCRMOUTSNO(外幣匯出通匯序號檔)
			// If dbFCRMOUTSNO.UpdateFCRMOUTSNOforBatchDaily < 0 Then
			// Notification = "更新外幣匯出通匯序號檔失敗"
			// rtnCode = IOReturnCode.FCRMOUTSNOUpdateError
			// db.RollbackTransaction()
			// Return False
			// End If
			//
			// 'FCRMINSNO(外幣匯入通匯序號檔)
			// If dbFCRMINSNO.UpdateFCRMINSNOforBatchDaily < 0 Then
			// Notification = "更新外幣匯入通匯序號檔失敗"
			// rtnCode = IOReturnCode.FCRMINSNOUpdateError
			// db.RollbackTransaction()
			// Return False
			// End If
			//
			// ALLBANK(全國銀行檔)
			if (allbankExtMapper.updateALLBANKforBatchDaily() <= 0) {
				notification = "更新全國銀行檔失敗";
				rtnCode = IOReturnCode.ALLBANKUpdateError;
				transactionManager.rollback(txStatus);
				return false;
			} else {
				job.writeLog("ALLBANK Update OK");
			}

			transactionManager.commit(txStatus);
			return true;
		} catch (Exception ex) {
			rtnCode = CommonReturnCode.ProgramException;
			job.writeLog("RM_BatchDaily-UpdateBTBatch, 更新相關序號檔發生例外, ex=" + ex.toString());
			transactionManager.rollback(txStatus);
			_logData.setRemark("RM_BatchDaily-UpdateBTBatch, 更新相關序號檔發生例外, ex=" + ex.toString());
			BatchJobLibrary.sendEMS(_logData);
			return false;
		}
	}

	/**
	 * Truncate暫存檔及失敗檔
	 */
	private boolean truncateBTBatch() {
		@SuppressWarnings("unused")
		Boolean rtn = true;
		@SuppressWarnings("unused")
		String errorMsg = "";
		Rmoutt rmoutt = new Rmoutt();
		Rmint rmint = new Rmint();

		try {
			// Fly 2017/10/02 為了Replication，改用Delete
			rmouttExtMapper.deleteByPrimaryKey(rmoutt);

			rmintExtMapper.deleteByPrimaryKey(rmint);
			return true;
		} catch (Exception ex) {
			notification = "Truncate失敗";
			job.writeLog("RM_BatchDaily-TruncateBTBatch Exception:  ex=" + ex.toString());
			rtnCode = CommonReturnCode.ProgramException;
			_logData.setRemark("RM_BatchDaily-TruncateBTBatch, Truncate暫存檔及失敗檔發生例外, ex="
					+ ex.toString());
			BatchJobLibrary.sendEMS(_logData);
			return false;
		}
	}

}
