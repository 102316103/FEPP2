package com.syscom.fep.batch.task.inbk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.syscom.fep.frmcommon.thread.SimpleThreadFactory;
import org.apache.commons.lang3.StringUtils;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.CbspendExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Cbspend;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;

/**
 * 主機回應逾時重送服務
 */
public class CBSTimeoutRerun extends FEPBase implements Task {
	private boolean batchResult = false;
	private String _batchLogPath = StringUtils.EMPTY;
	private BatchJobLibrary job = null;
	private CbspendExtMapper cbspendExtMapper = SpringBeanFactoryUtil.getBean(CbspendExtMapper.class);
	private SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
	private FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");

	@Override
	public BatchReturnCode execute(String[] args) {
		try {
			// 初始化相關批次物件及拆解傳入參數
			this.initialBatch(args);
			this.writeLogWithThreadInfo("------------------------------------------------------------------");
			this.writeLogWithThreadInfo(ProgramName + "開始!");
			// 回報批次平台開始工作
			job.startTask();

			// 顯示help說明
			if (job.getArguments().containsKey("?")) {
				DisplayUsage();
				return BatchReturnCode.Succeed;
			}

			// 批次主要處理流程
			batchResult = this.mainProcess();

			// 通知批次作業管理系統工作正常結束
			if (batchResult) {
				this.writeLogWithThreadInfo(ProgramName + "正常結束!!");
				this.writeLogWithThreadInfo("------------------------------------------------------------------");
				job.endTask();
			} else {
				this.writeLogWithThreadInfo(ProgramName + "不正常結束，停止此批次作業!!");
				this.writeLogWithThreadInfo("------------------------------------------------------------------");
				job.abortTask();
			}
			return BatchReturnCode.Succeed;
		} catch (Exception ex) {
			// 通知批次作業管理系統工作失敗,暫停後面流程
			try {
				job.abortTask();
				this.writeLogWithThreadInfo(ProgramName + "失敗!!");
				this.writeLogWithThreadInfo(ex.toString());
				this.writeLogWithThreadInfo("------------------------------------------------------------------");
			} catch (Exception e) {
				logContext.setProgramException(e);
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (job != null) {
				this.writeLogWithThreadInfo(ProgramName + "結束!!");
				this.writeLogWithThreadInfo("------------------------------------------------------------------");
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
	 *
	 * @param args
	 */
	private void initialBatch(String[] args) {
		logContext = new LogData();
		logContext.setChannel(FEPChannel.BATCH);
		logContext.setEj(0);
		logContext.setProgramName(ProgramName);
		// 檢查Batch Log目錄參數
		_batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();

		if (StringUtils.isBlank(_batchLogPath)) {
			LogHelperFactory.getGeneralLogger().error("Batch Log目錄未設定，請修正");
			return;
		}

		// 初始化BatchJob物件,傳入工作執行參數
		job = new BatchJobLibrary(this, args, _batchLogPath);
	}

	/**
	 * 批次主要處理流程
	 *
	 * @return
	 */
	private boolean mainProcess() {
		boolean rtn = true; // 是否正常跑完
		try {
			// 1. 輸入參數
			this.writeLogWithThreadInfo("輸入參數");
			final String wMinuteStr = job.getArguments().get("wMinute");
			if (StringUtils.isBlank(wMinuteStr)) {
				this.writeLogWithThreadInfo("wMinute參數為空");
				return true;
			}
			final int wMinute = Integer.parseInt(wMinuteStr);

			// 準備線程池
			this.writeLogWithThreadInfo("準備線程池");
			ExecutorService service = Executors.newFixedThreadPool(5, new SimpleThreadFactory(ProgramName));

			// 建立MQ連線
			MQQueue getQueue = getCbspendQueue();
			MQMessage receiveMessage = new MQMessage();
			// 取得MQ其他參數
			MQGetMessageOptions mqGetMessageOptions = new MQGetMessageOptions();
			mqGetMessageOptions.waitInterval = Integer.valueOf(5000); // wait for 5 second

			while (true) {
				// 2. 讀取 CBSPEND QUEUE
				String data = StringUtils.EMPTY;
				try {
					getQueue.get(receiveMessage, mqGetMessageOptions);
					data = receiveMessage.readUTF();
					this.writeLogWithThreadInfo("data : " + data);
				} catch (MQException e) {
					// Queue內無資料，表示沒必要再監聽
					if (StringUtils.equals(e.getMessage(), "MQJE001: Completion Code '2', Reason '2033'.")) {
						this.writeLogWithThreadInfo("Queue查無資料");
						break;
					}
					// 取Queue發生其他錯誤
					this.writeLogWithThreadInfo(ProgramName + " 執行失敗");
					this.writeLogWithThreadInfo(e.getMessage());
					sendEMS(logContext); // 不得直接呼叫FEPBase.SendEMS
				}

				// Queue中資料為空，不處理
				if (data == null) {
					this.writeLogWithThreadInfo("CBSPEND無資料");
					continue;
				}

				// Queue內容格式：CBSPEND_TX_DATE:CBSPEND_EJFNO
				String[] datas = data.split(":");

				// Queue內容不符合預期，不處理
				if (datas.length < 2) {
					this.writeLogWithThreadInfo("CBSPEND資料錯誤：" + data);
					continue;
				}

				// 當Queue有資料且符合預期，開啟線程執行
				service.execute(() -> {
					String cbspendTxDate = datas[0];
					int cbspendEjfno = Integer.parseInt(datas[1]);

					try {
						Object tota = null;
						// 將Queue讀出後，Waiting wMinute 再執行下列步驟(multi-thread)
						TimeUnit.MILLISECONDS.sleep(wMinute/* * 60 * 1000 */); // TODO 測試先注解

						// 3. 讀取上傳主機逾時明細檔
						Cbspend cbspend = this.loadCbspend(cbspendTxDate, cbspendEjfno);

						// 4. 交易狀態查詢
						if (cbspend != null) {
							tota = this.getTransationStatus(cbspend);
						}

						// 5. 重送入帳或沖正交易
						if (tota != null) {
							this.resendAccount(cbspend, tota);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}

			this.writeLogWithThreadInfo(ProgramName + " 執行正常!");
		} catch (Exception ex) {
			this.writeLogWithThreadInfo(ProgramName + " 執行失敗");
			this.writeLogWithThreadInfo(ex.getMessage());
			sendEMS(logContext); // 不得直接呼叫FEPBase.SendEMS
			rtn = false;
		}

		return rtn;
	}

	/**
	 * 讀取上傳主機逾時明細檔
	 *
	 * @param cbspendTxDate
	 * @param cbspendEjfno
	 * @return
	 * @throws ParseException
	 */
	private Cbspend loadCbspend(String cbspendTxDate, Integer cbspendEjfno) throws ParseException {
		// 以 CBSPEND QUEUE 內容讀取上傳主機逾時明細檔(CBSPEND)
		// 條件: CBSPEND_TX_DATE= CBSPEND QUEUE.TX_DATE
		// AND CBSPEND_EJFNO= CBSPEND QUEUE.EJFNO
		Cbspend cbspend = this.cbspendExtMapper.selectByPrimaryKey(cbspendTxDate, cbspendEjfno);

		// 查無資料
		if (cbspend == null) {
			logContext.setProgramException(new RuntimeException("CBSPEND無資料"));
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
			return null;
		}

		// 判斷重送記號(CBSPEND_SUCCESS_FLAG)
		if (cbspend.getCbspendSuccessFlag() != 0) {
			return null;
		}

		// 判斷執行時間
		Date txDate = new SimpleDateFormat("yyyyMMdd").parse(cbspend.getCbspendTxDate());
		Sysstat sysstat = this.sysstatExtMapper.selectFirstByLbsdyFisc();
		Date lbsdyDate = new SimpleDateFormat("yyyyMMdd").parse(sysstat.getSysstatLbsdyFisc());
		// 2021/2/25 判斷執行時間不得超過財金前營業日
		if (txDate.compareTo(lbsdyDate) > 0) {
			// 小於前一財金營業日, 不重送
			// 更新重送記號
			cbspend.setCbspendSuccessFlag((short) 2); // 不再重送
			int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
			if (updateCount == 0) {
				logContext.setProgramException(new RuntimeException("CBSPEND更新失敗-不再重送"));
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
			return null;
		}

		// 判斷重送次數
		Short resendCnt = cbspend.getCbspendResendCnt();
		if (resendCnt >= 5) {
			logContext.setProgramException(new RuntimeException("CBSPEND_RESEND_CNT=5 不回寫該筆 CBSPEND QUEUE"));
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
			// 8/21 刪除, 執行到5次, 不回寫 CBSPEND Queue
			return null;
		}

		return cbspend;
	}

	/**
	 * 交易狀態查詢
	 *
	 * @param cbspend
	 * @return tota
	 * @throws Exception
	 */
	private Object getTransationStatus(Cbspend cbspend) throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		// 電文 Sample
		rtnCode = this.sendToCBS(cbspend, "1"); // 1:入扣帳

		// CBS TIMEOUT OR OTHER ERROR
		if (rtnCode != FEPReturnCode.Normal) {
			// 執行處理TIMEOUT Routine
			this.doTimeOutRoutine(cbspend);
			rtnCode = this.sendToCBS(cbspend, "0"); // // 0:查詢
			return null;
		}

//			Object tota = hostAA.getTota();
		Object tota = new Object();
		if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) {
			// 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
			if (StringUtils.equals("A2", cbspend.getCbspendCbsKind()) && // 入帳類交易
					StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue())) // 已記帳
			) {
				// 入帳類交易已記帳, 不需重送
				cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
				cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
				cbspend.setCbspendAccType((short) 1); // 已記帳
				cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue())); // 成功
				int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
				if (updateCount == 0) {
					// 回寫該筆 CBSPEND QUEUE
					this.putMQ(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
					logContext.setProgramException(new RuntimeException("查詢成功 更新 CBSPEND失敗"));
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
					return null;
				}
				// 執行更新 FEPTXN Routine
				this.updateFeptxnRoutine(cbspend);
			}
			// 扣帳類交易未記帳, 不需重送
			if (StringUtils.equals("A1", cbspend.getCbspendCbsKind()) && // 扣帳類交易
					StringUtils.equals("N", this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue())) // 未記帳
			) {
				// 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
				cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
				cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
				// 判斷是否需沖正
				if (cbspend.getCbspendReverseFlag() == 1 && // 沖正記號 TODO 應如何判斷？
						StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue())) // 已沖正
				) {
					cbspend.setCbspendAccType((short) 2); // 沖正成功
					cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue())); // 成功
				} else {
					cbspend.setCbspendAccType((short) 0); // 未記帳
					cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue())); // 成功
				}
				// UPDATE (CBSPEND)
				int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
				if (updateCount == 0) {
					// 回寫該筆 CBSPEND QUEUE
					this.putMQ(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
					logContext.setProgramException(new RuntimeException("NOT FOUND 更新 CBSPEND失敗"));
					logContext.setProgramName(ProgramName);
					sendEMS(logContext);
					return null;
				}
				// 執行更新 FEPTXN Routine
				this.updateFeptxnRoutine(cbspend);
			}
		}
		return tota;
	}

	/**
	 * 重送入帳或沖正交易
	 *
	 * @param cbspend
	 * @param tota
	 * @throws Exception
	 */
	private void resendAccount(Cbspend cbspend, Object tota) throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		if (StringUtils.equals("N", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACC_FLAG.getValue())) && // 未記帳
				StringUtils.equals("A2", cbspend.getCbspendCbsKind()) // 入帳類交易
		) {
			// 需重送入帳電文至CBS主機
			rtnCode = this.sendToCBS(cbspend, "2"); // 2:沖正
		}

		if (StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACC_FLAG.getValue())) && // 已記帳
				StringUtils.equals("A1", cbspend.getCbspendCbsKind()) // 扣帳交易
		) {
			// 查詢交易成功, 需將沖正電文至CBS主機
			// 將 CBSPEND_TITA電文PROCESS_TYPE內容 改為 ‘RVS’其餘欄位相同, 將沖正電文至CBS主機
			// CBSPEND.CBSPEND_TITA，PROCESS_TYPE 第46位，取4位
			cbspend.setCbspendTita(StringUtils.substring(cbspend.getCbspendTita(), 45) + "RVS"
					+ StringUtils.substring(cbspend.getCbspendTita(), 49, cbspend.getCbspendTita().length()));
		}

		// 自行轉帳, 改為不再重送
		// ATM 入/扣帳Pending，更新CBS記帳結果，不重送入/扣帳
		if (StringUtils.equals("A3", cbspend.getCbspendCbsKind()) && //
				StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.TX_ACC_FLAG.getValue())) //
		) {
			// 將查詢結果回寫 CBSPEND / FEPTXN
			if (StringUtils.equals("Y", this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
				cbspend.setCbspendAccType((short) 1); // 已記帳
			} else {
				cbspend.setCbspendAccType((short) 0); // 未記帳
			}
			cbspend.setCbspendSuccessFlag((short) 2); // 不再重送
			int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
			if (updateCount == 0) {
				logContext.setProgramException(new RuntimeException("自行轉帳交易不再重送，更新CBSPEND_SUCCESS_FLAG=2失敗"));
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
			return;
		}

		if (rtnCode != FEPReturnCode.Normal) {
			// 執行處理TIMEOUT Routine
			this.doTimeOutRoutine(cbspend);
		}

		// 收到CBS主機回應
		if (StringUtils.equals("4001", this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()))) { // 成功
			// 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
			cbspend.setCbspendSuccessFlag((short) 1); // 重送成功
			cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
			cbspend.setCbspendCbsRc("0000"); // 成功
			if (StringUtils.equals("A2", cbspend.getCbspendCbsKind())) { // 入帳類交易
				cbspend.setCbspendAccType((short) 1); // 已記帳
			} else if (StringUtils.equals("A1", cbspend.getCbspendCbsRc())) { // 扣帳交易
				// 扣帳類交易含跨行存款
				cbspend.setCbspendAccType((short) 2); // 已沖正
			}
			// BEGIN TRANSACTION
			int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
			if (updateCount == 0) {
				logContext.setProgramException(new RuntimeException("T24回應OK, 更新 CBSPEND失敗"));
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
			// 執行更新 FEPTXN Routine
			this.updateFeptxnRoutine(cbspend);
		} else if (rtnCode != FEPReturnCode.Normal) { // 失敗
			// 8/21 修改,移至執行完 unlock, 再做 TRANSACTION
			// 以下更新 CBSPEND & FEPTXN 需做 TRANSACTION
			cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
			cbspend.setCbspendCbsRc(this.getImsPropertiesValue(tota, ImsMethodName.OUTRTC.getValue()));
			cbspend.setCbspendAccType((short) 3); // 更正or轉入失敗
			// BEGIN TRANSACTION
			int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
			if (updateCount == 0) {
				logContext.setProgramException(new RuntimeException("CBS Return Error 更新 CBSPEND失敗"));
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
			// 執行更新 FEPTXN Routine
			this.updateFeptxnRoutine(cbspend);
			// 8/21修改
			// 回寫該筆 CBSPEND QUEUE
			this.putMQ(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
		}
	}

	/**
	 * 更新重送次數
	 *
	 * @throws Exception
	 */
	private void doTimeOutRoutine(Cbspend cbspend) throws Exception {
		cbspend.setCbspendResendCnt((short) (cbspend.getCbspendResendCnt() + 1));
		cbspend.setCbspendAccType((short) 4); // 未明
		cbspend.setCbspendCbsRc(StringUtils.EMPTY);
		try {
			int updateCount = this.cbspendExtMapper.updateByPrimaryKey(cbspend);
			if (updateCount == 0) {
				// 回寫該筆 CBSPEND QUEUE
				this.putMQ(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
				logContext.setProgramException(new RuntimeException("TIMEOUT Routine 更新 CBSPEND失敗"));
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
		} catch (Exception e) {
			// 回寫該筆 CBSPEND QUEUE
			this.putMQ(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
			logContext.setProgramException(new RuntimeException("TIMEOUT Routine 更新 CBSPEND失敗"));
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
		}
	}

	/**
	 * 更新 FEPTXN Routine
	 *
	 * @param cbspend
	 * @throws Exception
	 */
	private void updateFeptxnRoutine(Cbspend cbspend) throws Exception {
		// 讀取 FEPTXN (檔名 SEQ 為CBSPEND_TBSDY_FISC [7:2] )
		String tbsdy = cbspend.getCbspendTbsdyFisc().substring(6, 8);
		this.feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
		Feptxn feptxn = this.feptxnDao.selectByPrimaryKey(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
		if (feptxn == null) {
			logContext.setProgramException(new RuntimeException("FEPTXN Routine Query FEPTXN 失敗"));
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
		}
		feptxn.setFeptxnAccType(cbspend.getCbspendAccType()); // 記帳別
		feptxn.setFeptxnCbsTimeout((short) 0);
		feptxn.setFeptxnCbsRc(cbspend.getCbspendCbsRc());
		try {
			int count = feptxnDao.updateByPrimaryKeySelective(feptxn);
			if (count == 0) {
				// 回寫該筆 CBSPEND QUEUE
				this.putMQ(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
				logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
				logContext.setProgramName(ProgramName);
				sendEMS(logContext);
			}
		} catch (Exception e) {
			// 回寫該筆 CBSPEND QUEUE
			this.putMQ(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
			logContext.setProgramException(new RuntimeException("FEPTXN Routine 更新 FEPTXN失敗"));
			logContext.setProgramName(ProgramName);
			sendEMS(logContext);
		}
	}

	private FEPReturnCode sendToCBS(Cbspend cbspend, String AATxTYPE) throws Exception {
		Feptxn feptxn = this.feptxnDao.selectByPrimaryKey(cbspend.getCbspendTxDate(), cbspend.getCbspendEjfno());
		MessageBase txData = new ATMData();
		txData.setFeptxn(feptxn);
		txData.setFeptxnDao(this.feptxnDao);
		txData.setLogContext(this.logContext);
		txData.setEj(cbspend.getCbspendEjfno());
		Class<?> c = Class.forName("com.syscom.fep.server.common.cbsprocess.CBIQTXI001");
		ACBSAction hostAA = (ACBSAction) c.getConstructor(new Class[] { MessageBase.class }).newInstance(txData);
		FEPReturnCode rtnCode = new CBS(hostAA, txData).sendToCBS(AATxTYPE);
		return rtnCode;
	}

	private void putMQ(String CbspendTxDate, int CbspendEjfno) throws Exception {
		// 建立MQ連線
		MQQueue putQueue = this.getCbspendQueue();
		MQMessage putMessage = new MQMessage();
		putMessage.characterSet = 1208;
		// Queue內容格式：CBSPEND_TX_DATE:CBSPEND_EJFNO
		putMessage.writeUTF(String.format("%s:%s", CbspendTxDate, Integer.toString(CbspendEjfno)));
		MQPutMessageOptions mQPutMessageOptions = new MQPutMessageOptions();
		putQueue.put(putMessage, mQPutMessageOptions);
	}

	private MQQueue getCbspendQueue() throws NumberFormatException, MQException {
		// 準備Queue參數
		JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
		String connName = configuration.getProp().getConnName();
		String[] split = connName.split("\\(");
		String connIp = split[0];
		this.writeLogWithThreadInfo("connIp : " + connIp);
		String connPort = StringUtils.substring(split[1], 0, split[1].length() - 1);
		this.writeLogWithThreadInfo("connPort : " + connPort);

		// 建立MQ連線
		MQQueueManager queueManager = JmsFactory.createMQQueueManager( //
				connIp, // hostname
				Integer.parseInt(connPort), // port
				configuration.getProp().getQueueManager(), // queueManagerName
				configuration.getProp().getChannel(), // channel
				configuration.getProp().getUser(), // userID
				configuration.getProp().getPassword() // password
		);
		return queueManager.accessQueue(configuration.getQueueNames().getCbspend().getDestination(), CMQC.MQOO_INPUT_AS_Q_DEF);

	}

	/**
	 * 連同線程信息一起打印至log
	 *
	 * @param log 打印內容
	 */
	private void writeLogWithThreadInfo(String log) {
		job.writeLog(String.format("%s - %s", Thread.currentThread().getName(), log));
	}

	/**
	 * 顯示help說明
	 */
	private void DisplayUsage() {
		System.out.println("USAGE:");
		System.out.println("  CBSTimeoutRerun Options");
		System.out.println();
		System.out.println("  Options:");
		System.out.println("      /?             Display this help message.");
		System.out.println("      /wMinute       Optional, wait minute.");
		System.out.println();
		System.out.println("EXAMPLES:");
		System.out.println("  execute system day        CBSTimeoutRerun");
		System.out.println("  execute specifies day     CBSTimeoutRerun /wMinute:5");
	}
}
