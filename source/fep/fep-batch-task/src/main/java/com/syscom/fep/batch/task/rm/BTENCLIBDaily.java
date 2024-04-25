package com.syscom.fep.batch.task.rm;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.syscom.fep.batch.base.enums.BatchReturnCode;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminsnoExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;

/**
 * 負責處理通匯ENCLIB KEY生效維護
 */
public class BTENCLIBDaily implements Task {

	private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
	private RminsnoExtMapper rminsnoExtMapper = SpringBeanFactoryUtil.getBean(RminsnoExtMapper.class);

	private BatchJobLibrary job;
	private Boolean _batchResult = false;
	private OutputStreamWriter _batchLog;
	private String _batchLogPath = "";
	private LogData _logData;

	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private List<Rminsno> dtRminSno = null;
	private Boolean _stopBatch = false;

	@Override
	public BatchReturnCode execute(String[] args) {
		_batchLogPath = RMConfig.getInstance().getBatchLogPath();
		// 自動生成的方法存根
		try {
			if (StringUtils.isBlank(_batchLogPath)){
				System.out.println("Batch Log目錄未設定，請修正");
				return BatchReturnCode.Succeed;
			}
            job = new BatchJobLibrary(this, args,_batchLogPath);
			job.writeLog("BTENCLIBDaily start!");

			job.setLogContext(new LogData());
			job.getLogContext().setProgramName("BTENCLIBDaily");
			job.getLogContext().setChannel(FEPChannel.BATCH);
			job.getLogContext().setSubSys(SubSystem.RM);
			job.getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			//開始工作內容
			job.startTask();

			if (!checkConfig()){
				 job.abortTask();
				return BatchReturnCode.Succeed;
			}
			//主流程
			if (!_stopBatch){
				doBusiness();

				//通知批次作業管理系統工作正常結束
				job.writeLog("BTENCLIBDaily End!");
				job.endTask();
			}else {
				job.stopBatch();
				job.writeLog("BTENCLIBDaily End by StopBatch!");
			}
			return BatchReturnCode.Succeed;
		} catch (Exception ex) {
			//Send EMS
			//通知批次作業管理系統工作失敗,暫停後面流程
			_logData.setRemark("BTENCLIBDaily-Main, 批次執行發生例外 Exception-"+ex.getMessage());
			BatchJobLibrary.sendEMS(_logData);
			job.writeLog("BTENCLIBDaily-Main Exception-"+ex.getMessage());
			try {
				job.abortTask();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (job!=null){
				job.dispose();
				job = null;
			}
			if (_batchLog!=null){
				try {
					_batchLog.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				_batchLog = null;
			}
		}
	}
	private boolean checkConfig(){
		Bsdays defBsdays = new Bsdays();

		try {
			//Jim, 2012/3/5, 判斷是否為營業日
			defBsdays.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBsdays.setBsdaysZoneCode(ZoneCode.TWN);
			defBsdays = bsdaysExtMapper.selectByPrimaryKey(defBsdays.getBsdaysZoneCode(),defBsdays.getBsdaysDate());
			if (defBsdays != null){
				if (!DbHelper.toBoolean(defBsdays.getBsdaysWorkday())){
					job.writeLog("非營業日不能執行");
					_stopBatch = true;
					return true;
				}
			}
			return true;
		} catch (Exception ex) {
			_logData.setProgramException(ex);
			_logData.setRemark("BTENCLIBDaily-CheckConfig, 查詢BSDAYS檔發生例外, ex="+ex.toString());
			BatchJobLibrary.sendEMS(_logData);
			job.writeLog("BTENCLIBDaily-CheckConfig Exception-"+ex.toString());
			return false;
		}
	}
	private FEPReturnCode doBusiness(){

		try {
			//1.讀取RMINSNO通匯序號檔
			_rtnCode = queryRMINSNO();

			//2. 	執行ENCHelper.CHANGECDEKY(FN000103) KEY生效功能
			if (_rtnCode.equals(CommonReturnCode.Normal)){
				callDesChangeCDKEY();
			}
			return _rtnCode;
		} catch (Exception ex) {
			job.writeLog("BTENCLIBDaily-doBusiness Exception-"+ex.getMessage());
			return CommonReturnCode.Normal;
		}
	}

	private FEPReturnCode queryRMINSNO(){
		Rminsno defRmInSno = new Rminsno();

		try {
			defRmInSno.setRminsnoCdkeyFlag("1");
			dtRminSno = rminsnoExtMapper.queryRMINSNOByCDKEY_FLAG(defRmInSno);
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			job.writeLog("BTENCLIBDaily-QueryRMINSNO Exception-"+ex.getMessage());
			_logData.setRemark("BTENCLIBDaily-QueryRMINSNO, 查詢RMINSNO資料發生例外 Exception-"+ex.getMessage());
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}
	private FEPReturnCode callDesChangeCDKEY(){
		Rminsno defRmInSno = new Rminsno();
		FISCData fiscData;
		FISC_RM reqFISCData;
		FISC_RM fisc_rm;
		ENCHelper encHelper;
		FEPReturnCode rtnCode;

		try {
			for (Rminsno dr: dtRminSno) {
				job.writeLog("呼叫ChangeCDKEY, 匯款行:"+dr.getRminsnoSenderBank());
				defRmInSno = new Rminsno();
				defRmInSno.setRminsnoReceiverBank(dr.getRminsnoReceiverBank());
				defRmInSno.setRminsnoSenderBank(dr.getRminsnoSenderBank());

				fiscData = new FISCData();
				fiscData.setTxObject(new FISCGeneral());
				fiscData.setTxChannel(FEPChannel.Unknown);
				fiscData.setLogContext(job.getLogContext());
				fiscData.getLogContext().setPrimaryKeys("RMINSNO_RECEIVER_BANK:"+ defRmInSno.getRminsnoReceiverBank()
						+";RMINSNO_SENDER_BANK" +defRmInSno.getRminsnoSenderBank());
				fiscData.getLogContext().setReturnCode(null);
				fiscData.getLogContext().setRemark("");
				fiscData.getLogContext().setTableName("");
				reqFISCData = new FISC_RM();

				reqFISCData.setEnglishMemo("REP3");
				reqFISCData.setProcessingCode("1411");
				reqFISCData.setReceiverBank(defRmInSno.getRminsnoSenderBank());
				fiscData.getTxObject().setRMRequest(reqFISCData);

				encHelper = new ENCHelper(fiscData);

				rtnCode = encHelper.changeRMCDKey("");
				if (!rtnCode.equals(CommonReturnCode.Normal)){
					fiscData.getLogContext().setRemark("Call Des ChangeCDKey Error");
					fiscData.getLogContext().setReturnCode(rtnCode);
					BatchJobLibrary.sendEMS(fiscData.getLogContext());
					job.writeLog(TxHelper.getMessageFromFEPReturnCode(rtnCode));
					continue;
				}

				defRmInSno.setRminsnoCdkeyFlag("0");
				if (rminsnoExtMapper.updateByPrimaryKeySelective(defRmInSno)!=1){
					fiscData.getLogContext().setRemark("更新匯入通匯序號檔失敗");
					fiscData.getLogContext().setTableName("RMINSNO");
					fiscData.getLogContext().setReturnCode(IOReturnCode.UpdateFail);
					BatchJobLibrary.sendEMS(fiscData.getLogContext());
					job.writeLog("更新匯入通匯序號檔失敗");
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			job.writeLog("BTENCLIBDaily-CallDesChangeCDKEY Exception-"+ex.getMessage());
			_logData.setRemark("BTENCLIBDaily-CallDesChangeCDKEY Exception-"+ex.getMessage());
			BatchJobLibrary.sendEMS(_logData);
			return CommonReturnCode.ProgramException;
		}
	}
}
