package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TaskExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.mchange.lang.IntegerUtils;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author Richard
 */
public class PendingAskFromFISC extends INBKAABase {

	private Inbkpend defINBKPEND;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private InbkpendExtMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
	private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
	private BatchExtMapper batchExtMapper = SpringBeanFactoryUtil.getBean(BatchExtMapper.class);
	private TaskExtMapper taskExtMapper = SpringBeanFactoryUtil.getBean(TaskExtMapper.class);

	private FeptxnExt desFEPTXN = new FeptxnExt();
	private Zone zone = new Zone();

	public PendingAskFromFISC(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		FEPReturnCode rtnCode2 = CommonReturnCode.Normal;
		try {
			defINBKPEND = new Inbkpend();

			// 1. 檢核財金電文 Header
			rtnCode2 = processRequestHeader();
			if (rtnCode2 != CommonReturnCode.Normal) {
				return "";
			}

			// 2. @Prepare : 交易記錄初始資料 INBKPEND& FEPTXN
			rtnCode2 = prepareINBKPEND();
			if(rtnCode2 != CommonReturnCode.Normal){
				return "";
			}

			// 3. @AddTxData: 新增滯留交易記錄(INBKPEND)
			rtnCode2 = addINBKPEND();
			if (rtnCode2 != CommonReturnCode.Normal) {
				return "";
			}

			if (_rtnCode == CommonReturnCode.Normal) {
				// 4. 檢核訊息押碼(MAC)
				this.checkFISCMACPend();
			}

			if (_rtnCode == CommonReturnCode.Normal) {
				// 5. 檢核原交易記錄 FEPTXN
				_rtnCode = checkFepTxn();
			}

			// 7. 產生財金回應訊息電文
			_rtnCode = prepareForFISC();

			// 8. @更新交易記錄(INBKPEND &FEPTXN)
			this.updateInbkpendAndFeptxn();

			// 9. 送回覆電文到財金(SendToFISC)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			}

			// 10. 送 Confirm 電文到財金(for 2270)
			rtnCode2 = sendConfirmToFISC();
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = rtnCode2;
			}

			//11. 組2130電文到財金(for 2120)
			if("2120".equals(defINBKPEND.getInbkpendPcode())
					&& !"000".equals(defINBKPEND.getInbkpendOriBkno())
					&& !"0000000".equals(defINBKPEND.getInbkpendOriStan())
					&& "0001".equals(defINBKPEND.getInbkpendRepRc())){
				BatchJobLibrary batchLib = new BatchJobLibrary();
				String batchName="Pending2130";
				List<Batch> dt = batchExtMapper.queryBatchByName(batchName);
				List<Task> list = taskExtMapper.getTaskByName("Pending2130","ASC");
				Task task = new Task();
				task.setTaskId(list.get(0).getTaskId());
				String txdate=defINBKPEND.getInbkpendTxDate();
				String oribkno=defINBKPEND.getInbkpendOriBkno();
				String oriStan=defINBKPEND.getInbkpendOriStan();
				task.setTaskCommandargs("/TBSDY:"+txdate+" /ORI_BKNO:"+oribkno+" /ORI_STAN:"+oriStan);
				task.setTaskName(null);
				task.setTaskCommand(null);
				taskExtMapper.updateByPrimaryKeySelective(task);
				Map<String, String> arguments = new HashMap<>();
				arguments.put("TBSDY",txdate);
				arguments.put("ORI_BKNO",oribkno);
				arguments.put("ORI_STAN",oriStan);
				batchLib.setArguments(arguments);
				batchLib.startBatch(
						dt.get(0).getBatchExecuteHostName(),
						dt.get(0).getBatchBatchid().toString(),
						dt.get(0).getBatchStartjobid().toString());
			}

		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName + ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getFiscRes().setResponseCode(MathUtil.toString(BigDecimal.valueOf(_rtnCode.getValue()), "0000"));
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";

	}

	/**
	 * 檢核財金電文 Header
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processRequestHeader() {
		_rtnCode = getFiscBusiness().checkHeader(getFiscReq(), false);
		if ("10".equals(StringUtils.substring(Objects.toString(_rtnCode.getValue()), 0, 2))) {
			/* Garbled Message */
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), _rtnCode, getFiscReq());
		}
		return _rtnCode;

	}

	/**
	 * 交易記錄初始資料 INBKPEND& FEPTXN
	 * 
	 * @return
	 */
	private FEPReturnCode prepareINBKPEND() {
		// PrepareINBKPEND: 跨行延遲交易檔初始資料(INBKPEND)
		try {
			String TX_DATETIME = getFiscReq().getTxnInitiateDateAndTime();
			if (IntegerUtils.parseInt(TX_DATETIME, 0) < 90) {
				defINBKPEND.setInbkpendTxDate(String.valueOf(20110000+Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
			} else {
				defINBKPEND.setInbkpendTxDate(String.valueOf(19110000+Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
			}
			defINBKPEND.setInbkpendTxTime(StringUtils.substring(TX_DATETIME, 6, 12)); // 交易時間
			defINBKPEND.setInbkpendEjfno(getTxData().getEj()); // 電子日誌序號
			defINBKPEND.setInbkpendStan(getFiscReq().getSystemTraceAuditNo()); // 財金交易序號
			defINBKPEND.setInbkpendBkno(getFiscReq().getTxnSourceInstituteId().substring(0, 3)); // 交易啟動銀行
			defINBKPEND.setInbkpendDesBkno(getFiscReq().getTxnDestinationInstituteId().substring(0, 3)); // 交易接收銀行
			defINBKPEND.setInbkpendAtmno(getFiscReq().getATMNO()); // 櫃員機代號
			defINBKPEND.setInbkpendSubsys(getTxData().getMsgCtl().getMsgctlSubsys()); // 系統別
			defINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Request); // ‘F1’ (FISC REQUEST)
			defINBKPEND.setInbkpendPcode(getFiscReq().getProcessingCode());
			defINBKPEND.setInbkpendReqRc(getFiscReq().getResponseCode());
			if (StringUtils.isNotBlank(getFiscReq().getTxAmt())) {
				defINBKPEND.setInbkpendTxAmt(new BigDecimal(getFiscReq().getTxAmt())); // 交易金額
			}
			if (StringUtils.isNotBlank(getFiscReq().getOriStan())) { // 原始交易序號
				defINBKPEND.setInbkpendOriBkno(getFiscReq().getOriStan().substring(0, 3));
				defINBKPEND.setInbkpendOriStan(getFiscReq().getOriStan().substring(3, 10));
			}
			if (StringUtils.isNotBlank(getFiscReq().getTroutBkno())) {
				defINBKPEND.setInbkpendTroutbkno(getFiscReq().getTroutBkno().substring(0, 3)); // 原交易存款單位代號
			}
			defINBKPEND.setInbkpendTroutActno(getFiscReq().getTrinActno());
			/* PS:原交易帳號/卡號存於 INBK.TRIN_ACTNO */
			if (StringUtils.isNotBlank(getFiscReq().getDueDate())) {
				/* 若.DUE_DATE 為0 則不需轉西元年 */
				if ("000000".equals(getFiscReq().getDueDate())) {
					defINBKPEND.setInbkpendOriTxDate(StringUtils.repeat( '0',8));
				} else {
					defINBKPEND.setInbkpendOriTxDate(
							CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0'))); // 轉西元年)
				}
			}
			defINBKPEND.setInbkpendPrcResult(getFiscReq().getRsCode()); // 處理結果
			defINBKPEND.setInbkpendCount(Integer.parseInt(getFiscReq().getCOUNT())); // 件數
			defINBKPEND.setInbkpendEcInstruction(getFiscReq().getMODE()); // 沖正指示
			/* 2021/1/13 應永豐要求, 增加寫入 FEPTXN */
			String inbkpendTxDate = defINBKPEND.getInbkpendTxDate(); // 交易日期(西元年)
			String inbkpendTxTime = defINBKPEND.getInbkpendTxTime(); // 交易時間
			getFiscBusiness().getFeptxn().setFeptxnTxDate(inbkpendTxDate);
			getFiscBusiness().getFeptxn().setFeptxnTxTime(inbkpendTxTime);
			getFiscBusiness().getFeptxn().setFeptxnReqDatetime(inbkpendTxDate + inbkpendTxTime);
			getFiscBusiness().getFeptxn().setFeptxnBkno(defINBKPEND.getInbkpendBkno());
			getFiscBusiness().getFeptxn().setFeptxnStan(defINBKPEND.getInbkpendStan());
			getFiscBusiness().getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
			getFiscBusiness().getFeptxn().setFeptxnAtmno(defINBKPEND.getInbkpendAtmno());
			getFiscBusiness().getFeptxn().setFeptxnTxAmt(defINBKPEND.getInbkpendTxAmt());
			getFiscBusiness().getFeptxn().setFeptxnEjfno(defINBKPEND.getInbkpendEjfno());
			getFiscBusiness().getFeptxn().setFeptxnPcode(defINBKPEND.getInbkpendPcode());
			getFiscBusiness().getFeptxn().setFeptxnReqRc(defINBKPEND.getInbkpendReqRc());
			getFiscBusiness().getFeptxn().setFeptxnSubsys((short) 1);
			getFiscBusiness().getFeptxn().setFeptxnFiscFlag((short) 1);
			// (條件ZONE_CODE = “TWN”)
			zone = zoneExtMapper.selectByPrimaryKey("TWN");
			getFiscBusiness().getFeptxn().setFeptxnTbsdy(zone.getZoneTbsdy());
			getFiscBusiness().getFeptxn().setFeptxnChannel(getFiscBusiness().getFISCTxData().getTxChannel().name()); // 通道別
			getFiscBusiness().getFeptxn().setFeptxnMsgid(getTxData().getMsgCtl().getMsgctlMsgid());
			getFiscBusiness().getFeptxn().setFeptxnTxrust("0");
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareINBKPEND"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 新增滯留交易記錄(INBKPEND & FEPTXN)
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	private FEPReturnCode addINBKPEND() throws Exception {
		if (dbINBKPEND.insertSelective(defINBKPEND) < 1) {
			return IOReturnCode.INBKPENDInsertError;
		}
		/* 2021/1/13 應永豐要求, 增加寫入 FEPTXN */
		String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8); // 檔名SEQ為
		feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
		if (feptxnDao.insertSelective(getFiscBusiness().getFeptxn()) < 1) {
			return IOReturnCode.FEPTXNInsertError;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 檢核訊息押碼(MAC)
	 * 
	 */
	private void checkFISCMACPend() {
		/* Prepare FEPTXN for DES, pls new FEPTXN just for DES */
		desFEPTXN.setFeptxnTxDate(defINBKPEND.getInbkpendTxDate());
		desFEPTXN.setFeptxnBkno(defINBKPEND.getInbkpendBkno());
		desFEPTXN.setFeptxnStan(defINBKPEND.getInbkpendStan());
		desFEPTXN.setFeptxnDesBkno(defINBKPEND.getInbkpendOriBkno());
		desFEPTXN.setFeptxnOriStan(defINBKPEND.getInbkpendOriStan());
		desFEPTXN.setFeptxnTbsdyFisc(defINBKPEND.getInbkpendOriTxDate());
		desFEPTXN.setFeptxnRsCode(defINBKPEND.getInbkpendPrcResult());
		desFEPTXN.setFeptxnRemark(defINBKPEND.getInbkpendEcInstruction());
		desFEPTXN.setFeptxnReqRc(defINBKPEND.getInbkpendReqRc());
		ENCHelper encHelper = new ENCHelper(desFEPTXN, getTxData());
		FEPReturnCode rtnCode = encHelper.checkFISCMACPend(getFiscReq().getMessageType().substring(2, 4),
				getFiscReq().getMAC());
		if (rtnCode != CommonReturnCode.Normal) {
			_rtnCode = FEPReturnCode.ENCCheckMACError; /* 訊息押碼錯誤 */
		}
	}

	/**
	 * 檢核原交易記錄 FEPTXN
	 * 
	 * @return
	 */
	private FEPReturnCode checkFepTxn() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			if ("2270".equals(getFiscReq().getProcessingCode())) {
				String oriStan1 = StringUtils.substring(getFiscReq().getOriStan(), 0, 3);
				String oriStan2 = StringUtils.substring(getFiscReq().getOriStan(), 3, 10);
				// 讀取 FEPTXN (檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2]) /*本營業日*/
				getFiscBusiness().setFeptxn(feptxnDao.getFEPTXNByStanAndBkno(oriStan1, oriStan2));
				if (getFiscBusiness().getFeptxn() == null) {
					// 以相同條件讀取 FEPTXN (檔名SEQ為 FEPTXN_LBSDY_FISC[7:2])
					feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8),
							StringUtils.join(ProgramName, ".checkFepTxn"));
					getFiscBusiness().setFeptxn(feptxnDao.getFEPTXNByStanAndBkno(oriStan1, oriStan2));
					if (getFiscBusiness().getFeptxn() == null) {
						return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
					} else {
						// 2014/05/16 Modify by Ruling for
						// 修正測試整批轉即時發現的問題：找前一營業日的DBFEPTXN要同步到Business否則無法更新原交易
						getFiscBusiness().setFeptxnDao(feptxnDao);
					}
				}
				if (FEPTxnMessageFlow.FISC_Request.equals(getFiscBusiness().getFeptxn().getFeptxnMsgflow())) { // "F1"
					return FISCReturnCode.ProtocalError; /* 4702:交易狀態不明或 Protocal 錯誤 */
				}

				/* 5/16 修改 FOR 2270, 將原交易資料回寫 INBKPEND */
				defINBKPEND.setInbkpendTroutbkno(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());
				defINBKPEND.setInbkpendTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
				defINBKPEND.setInbkpendMajorActno(getFiscBusiness().getFeptxn().getFeptxnMajorActno());
				defINBKPEND.setInbkpendTrinBkno(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
				defINBKPEND.setInbkpendTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
				defINBKPEND.setInbkpendTrinActnoActual(getFiscBusiness().getFeptxn().getFeptxnTrinActnoActual());
				/* 1/9 修改 */
				defINBKPEND.setInbkpendOriTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
				defINBKPEND.setInbkpendOriPcode(getFiscBusiness().getFeptxn().getFeptxnPcode());
				defINBKPEND.setInbkpendOriTbsdyFisc(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc());
				defINBKPEND.setInbkpendOriEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
				defINBKPEND.setInbkpendOriReqRc(getFiscBusiness().getFeptxn().getFeptxnReqRc());
				defINBKPEND.setInbkpendOriRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defINBKPEND.setInbkpendOriConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
				if (getFiscBusiness().getFeptxn().getFeptxnClrType() == 1) {
					defINBKPEND.setInbkpendOriTxFlag(DbHelper.toShort(true)); /* 己更新跨行代收付 */
				}
				if ("A".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /* 成功 */
					defINBKPEND.setInbkpendPrcResult("00"); /* 處理結果=成功 */
				} else {
					defINBKPEND.setInbkpendPrcResult("01"); /* 處理結果=失敗 */
				}
			} else if ("2120".equals(getFiscReq().getProcessingCode())
					&& Double.parseDouble(getFiscReq().getCOUNT()) != 0) {
				rtnCode = getFiscBusiness().searchFeptxn(
						CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0')),
						getFiscReq().getOriStan().substring(0, 3), getFiscReq().getOriStan().substring(3, 10));
				if (rtnCode != CommonReturnCode.Normal) {
					return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
				}
				/* 因為送財金之 ATMNO 已右補0, 故不再判斷ATMNO 是否相同 */
				/* 1/29 修改 for 跨行提領外幣 */
				if (!ATMTXCD.US.toString().equals(getFiscBusiness().getFeptxn().getFeptxnTxCode()) ||
						!ATMTXCD.JP.toString().equals(getFiscBusiness().getFeptxn().getFeptxnTxCode())) {
					/* 跨行台幣交易 */
					if (!getFiscBusiness().getFeptxn().getFeptxnTxDate().equals(
							CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0')))
							|| getFiscBusiness().getFeptxn().getFeptxnTxAmt().doubleValue() != Double.parseDouble(getFiscReq().getTxAmt())
							|| !getFiscReq().getTroutBkno().substring(0, 3).equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())
							|| !getFiscReq().getTrinActno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnTroutActno())) {
						/* PS:原交易帳號存於 ReqINBK.TRIN_ACTNO */
						return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
					}
				} else {
					/* 跨行提領外幣交易(US/JP) */
					if (!getFiscBusiness().getFeptxn().getFeptxnTxDate().equals(
							CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0')))
							|| getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().doubleValue() != Double.parseDouble(getFiscReq().getTxAmt())
							|| !getFiscReq().getTroutBkno().substring(0, 3).equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())
							|| !getFiscReq().getTrinActno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnTroutActno())) {
						/* PS:原交易帳號存於 ReqINBK.TRIN_ACTNO */
						return FISCReturnCode.TransactionNotFound; /* 4701:無此交易 */
					}
				}
				defINBKPEND.setInbkpendMajorActno(getFiscBusiness().getFeptxn().getFeptxnMajorActno());
				defINBKPEND.setInbkpendTrinBkno(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
				defINBKPEND.setInbkpendTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
				defINBKPEND.setInbkpendTrinActnoActual(getFiscBusiness().getFeptxn().getFeptxnTrinActnoActual());
				defINBKPEND.setInbkpendOriPcode(getFiscBusiness().getFeptxn().getFeptxnPcode());
				defINBKPEND.setInbkpendOriTbsdyFisc(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc());
				defINBKPEND.setInbkpendOriEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
				defINBKPEND.setInbkpendOriReqRc(getFiscBusiness().getFeptxn().getFeptxnReqRc());
				defINBKPEND.setInbkpendOriRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defINBKPEND.setInbkpendOriConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
				if (getFiscBusiness().getFeptxn().getFeptxnClrType() == 1) {
					defINBKPEND.setInbkpendOriTxFlag(DbHelper.toShort(true)); /* 己更新跨行代收付 */
				}
				if ("A".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) { /* 成功 */
					defINBKPEND.setInbkpendPrcResult("00"); /* 處理結果=成功 */
				} else {
					defINBKPEND.setInbkpendPrcResult("01"); /* 處理結果=失敗 */
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkFepTxn"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 產生財金回應訊息電文
	 * 
	 * @return
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = null;
		try {
			// (1) 判斷 FEPReturnCod
			if (_rtnCode != CommonReturnCode.Normal) {
				getLogContext().setProgramName(ProgramName);
				defINBKPEND.setInbkpendRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
			} else {
				defINBKPEND.setInbkpendRepRc(NormalRC.FISC_OK);
			}
			// (2) 產生 RESPONSE (CDPF02, CDHI02, CDHI08)電文訊息,其格式內容如下:
			// Header (RepHEAD):
			rtnCode = getFiscBusiness().preparePendHeader("0210", defINBKPEND);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// Application Data Elements(RepINBK) :
			if ("2120".equals(getFiscReq().getProcessingCode()) || "2150".equals(getFiscReq().getProcessingCode())) {
				getFiscRes().setDueDate(getFiscReq().getDueDate());
			}
			getFiscRes().setOriStan(getFiscReq().getOriStan());

			// (3) 產生訊息押碼(MAC)
			/* Prepare FEPTXN for DES */
			desFEPTXN.setFeptxnTxDate(defINBKPEND.getInbkpendTxDate());
			desFEPTXN.setFeptxnBkno(defINBKPEND.getInbkpendBkno());
			desFEPTXN.setFeptxnStan(defINBKPEND.getInbkpendStan());
			desFEPTXN.setFeptxnDesBkno(defINBKPEND.getInbkpendOriBkno());
			desFEPTXN.setFeptxnOriStan(defINBKPEND.getInbkpendOriStan());
			desFEPTXN.setFeptxnTbsdyFisc(defINBKPEND.getInbkpendOriTxDate());
			desFEPTXN.setFeptxnRepRc(defINBKPEND.getInbkpendRepRc());
			encHelper = new ENCHelper(desFEPTXN, getTxData());
			RefString refString = new RefString();
			rtnCode = encHelper.makeFISCMACPend(getFiscRes().getMessageType().substring(2, 4), refString);
			getFiscReq().setMAC(refString.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscRes().setMAC("00000000");
			}

			// (4) Make Bit Map
			rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(),
					MessageFlow.Response);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscRes().setBitMapConfiguration(StringUtils.leftPad("0", 16, '0'));
			}

			_rtnCode=getFiscRes().makeFISCMsg();

			// (5) SendEMS
			if (("2120".equals(defINBKPEND.getInbkpendPcode()) || "2150".equals(defINBKPEND.getInbkpendPcode()))
					&& NormalRC.FISC_OK.equals(defINBKPEND.getInbkpendRepRc())) {
				// 20110621 需顯示成功訊息於EMS, 為此LogData新增欄位如下
				FEPReturnCode InfoRC = null;
				getLogContext().setpCode(defINBKPEND.getInbkpendPcode());
				getLogContext().setDesBkno(defINBKPEND.getInbkpendDesBkno());
				getLogContext().setFiscRC(NormalRC.FISC_OK);
				getLogContext().setMessageGroup("3"); // INBK
				if (defINBKPEND.getInbkpendPcode().equals("2120")) {
					InfoRC = FISCReturnCode.FISCUnfinishedTransaction;
				} else {
					InfoRC = FISCReturnCode.FISCErrorCorrectionResult;
				}
				getLogContext().setMessageParm13(getFiscReq().getCOUNT()); /* 筆數 */
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
				/* 由GetMessageFromFEPReturnCode執行 SendEMS */
				logMessage(Level.DEBUG, getLogContext());
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareForFISC"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 更新交易記錄(INBKPEND &FEPTXN)
	 * 
	 * @throws Exception
	 */
	private void updateInbkpendAndFeptxn() throws Exception {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			defINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0'));
			defINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Response); // ‘F2’(FISC RESPONSE)
			defINBKPEND.setInbkpendPending((short) 2); // (解除PENDING)
			int updCount = dbINBKPEND.updateByPrimaryKeySelective(defINBKPEND);
			if (updCount < 1) {
				transactionManager.rollback(txStatus);
				return;
			}
			/* 2021/1/13 應永豐要求, 增加寫入 FEPTXN */
			getFiscBusiness().getFeptxn().setFeptxnPending(defINBKPEND.getInbkpendPending());
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(defINBKPEND.getInbkpendMsgflow());
			getFiscBusiness().getFeptxn().setFeptxnFiscTimeout(defINBKPEND.getInbkpendFiscTimeout());
			getFiscBusiness().getFeptxn().setFeptxnRepRc(defINBKPEND.getInbkpendRepRc());
			getFiscBusiness().getFeptxn().setFeptxnAaRc(IntegerUtils.parseInt(defINBKPEND.getInbkpendAaRc(), 0));
			/* 9/28 修改, 寫入處理結果 */
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /* AA Complete */
			if ("0001".equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); /* 處理結果=成功 */
			} else {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); /* 處理結果=Reject */
			}
			feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8),
					StringUtils.join(ProgramName, ".updateInbkpendAndFeptxn"));
			/* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]) */
			if (feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getFeptxn()) < 1) {
				transactionManager.rollback(txStatus);
				return;
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "updateInbkpendAndFeptxn");
			sendEMS(getLogContext());
		}
	}

	/**
	 * 送 Confirm 電文到財金(for 2270)
	 * 
	 * @return
	 */
	private FEPReturnCode sendConfirmToFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			if ("2270".equals(defINBKPEND.getInbkpendPcode())
					&& NormalRC.FISC_OK.equals(defINBKPEND.getInbkpendRepRc())) {
				if (feptxn != null) {
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
						rtnCode = getFiscBusiness().sendConfirmToFISC();
					} else if (DbHelper.toBoolean(getFiscBusiness().getFeptxn().getFeptxnFiscTimeout())) {
						getLogContext().setProgramName(ProgramName);
						getFiscBusiness().getFeptxn().setFeptxnConRc("0601"); //TIMEOUT
						getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); //解除 PENDING
						getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); //Reverse
						rtnCode = getFiscBusiness().sendConfirmToFISC();
					}
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendConfirmToFISC"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
