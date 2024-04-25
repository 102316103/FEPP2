package com.syscom.fep.server.aa.inbk;

import com.mchange.lang.IntegerUtils;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Joseph
 */
public class PendingAskFromBank2290 extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private String fiscResRC; // 財金RESPONSE電文的RC + Description
	private Inbkpend defINBKPEND;
	private List<Feptxn> dsFeptxn = new ArrayList<>();
	private String uiPCODE = "";
	private String uiTXDATE = "";
	private String uiBKNO = "";
	private String uiSTAN = "";
	private InbkpendExtMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
	private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
	private ENCHelper encHelper;
	private FeptxnExt desFEPTXN = new FeptxnExt();
	private String wk_DATE = "";
	private Zone zone = new Zone();

	public PendingAskFromBank2290(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			// 1. Input Parameter: 輸入參數
			this.start();

			// 2. 檢核財金跨行狀態
			_rtnCode = getFiscBusiness().checkINBKStatus("2290", true);

			// 3. 讀取交易記錄檔: FEPTXN
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = readFeptxn();
			}

			// 4. LOOP -- 將 FEPTXN 逐筆讀出並送至財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = readDataAndSendToFISC();
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getLogContext().setProgramName(ProgramName);
			if (_rtnCode != CommonReturnCode.Normal) {
				getTxData().getTxObject()
						.setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			} else {
				getTxData().getTxObject().setDescription(StringUtils.join(fiscResRC, "-",
						TxHelper.getMessageFromFEPReturnCode(fiscResRC, FEPChannel.FISC, getLogContext())));
			}
			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(getFiscReq().getFISCMessage());
			getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(getTxData().getTxObject().getDescription());
			getLogContext().setProgramName(this.aaName);
			logMessage(Level.DEBUG, getLogContext());
			defINBKPEND = null;
		}
		return "";
	}

	/**
	 * Start
	 *
	 *
	 */
	private void start() {
		defINBKPEND = new Inbkpend();
		desFEPTXN = new FeptxnExt();

		uiPCODE = getFiscReq().getProcessingCode();
		if (StringUtils.isNotBlank(getFiscReq().getTxnInitiateDateAndTime())) {
			uiTXDATE = CalendarUtil.rocStringToADString(getFiscReq().getTxnInitiateDateAndTime().substring(0, 7));
		}

		if (StringUtils.isNotBlank(getFiscReq().getTxnSourceInstituteId())) {
			uiBKNO = getFiscReq().getTxnSourceInstituteId().substring(0, 3);
		}

		if (StringUtils.isNotBlank(getFiscReq().getSystemTraceAuditNo())) {
			uiSTAN = getFiscReq().getSystemTraceAuditNo();
		}
	}

	/**
	 * 讀取交易記錄檔FEPTXN
	 *
	 * @return
	 *
	 *         <modify> <modifier>HusanYin</modifier> <reason>修正Const RC</reason>
	 *         <date>2010/11/25</date> </modify>
	 */
	private FEPReturnCode readFeptxn() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String wk_SYS_DATETIME = null;
		try {
			// 讀取 FEPTXN(2280單筆或多筆, 2290單筆)
			/* 若 UI.TX_DATE, UI.BKNO, UI.STAN 為NULL則讀取所有PENDING交易 */
			if (uiPCODE.equals("2290")) {
				// 讀取 FEPTXN /*檔名SEQ為 wk_DATE [7:2]*/
				String tbsdy = SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
				feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
				if (StringUtils.isNotBlank(uiTXDATE)) {
					getFiscBusiness().getFeptxn().setFeptxnTxDate(uiTXDATE);
				}
				if (StringUtils.isNotBlank(uiBKNO)) {
					getFiscBusiness().getFeptxn().setFeptxnBkno(uiBKNO);
				}
				if (StringUtils.isNotBlank(uiSTAN)) {
					getFiscBusiness().getFeptxn().setFeptxnStan(uiSTAN);
				}
				dsFeptxn = feptxnDao.getFEPTXNFor2290(getFiscBusiness().getFeptxn().getFeptxnTxDate(), getFiscBusiness().getFeptxn().getFeptxnBkno(),
						SysStatus.getPropertyValue().getSysstatHbkno(),getFiscBusiness().getFeptxn().getFeptxnStan(),
						SysStatus.getPropertyValue().getSysstatLbsdyFisc());
				if (dsFeptxn.size() == 0) {
					rtnCode = IOReturnCode.FEPTXNNotFound;
					return rtnCode;
				}
			} else { /* 前日 PENDING */
				rtnCode = getFiscBusiness().searchFeptxn(uiTXDATE, uiBKNO, uiSTAN);
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
				setFeptxn(getFiscBusiness().getFeptxn());
				if (!uiTXDATE.equals(getFiscBusiness().getFeptxn().getFeptxnTxDate())
						|| getFiscBusiness().getFeptxn().getFeptxnSubsys() != SubSystem.INBK.getValue() /* INBK */
						|| getFiscBusiness().getFeptxn().getFeptxnPending() != 1 || !NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc()) // +REP
						|| getFiscBusiness().getFeptxn().getFeptxnTraceEjfno() != 0) {
					return IOReturnCode.FEPTXNNotFound;
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName + ".readFeptxn"));
			sendEMS(getLogContext());
			return IOReturnCode.FEPTXNNotFound;
		}
	}

	/**
	 * 將 FEPTXN 逐筆讀出並送至財金
	 *
	 * @return
	 *
	 *         <modifier>HusanYin</modifier> <reason>修正Const RC</reason>
	 *         <date>2010/11/25</date> </modify>
	 */
	private FEPReturnCode readDataAndSendToFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		int i = 0;
		int FeptxnCount = 0;
		try {
			if (String.valueOf(FISCPCode.PCode2290.getValueStr()).equals(uiPCODE)) {
				FeptxnCount = dsFeptxn.size();
			}

			for (i = 0; i < FeptxnCount; i++) {
				if (FISCPCode.PCode2290.getValueStr().equals(uiPCODE)) {
					getFiscBusiness().setFeptxn(dsFeptxn.get(i));
					if (feptxn == null) {
						break;
					}
				}

				// (1) Prepare : 跨行延遲交易檔初始資料 INBKPEND
				rtnCode = prepareINBKPEND(i);
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				// (2) 產生 2280/2290 財金通知電文
				rtnCode = prepareForFISC();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				// (3) @AddTxData: 新增滯留交易記錄(INBKPEND & FEPTXN)
				rtnCode = addINBKPEND();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				// (4) 送 REQ 電文至財金並等待回應
				// 將財金電文轉成 String (ReqFISCMessage)
				RefBase<Inbkpend> inbkpendRefBase = new RefBase<Inbkpend>(defINBKPEND);
				rtnCode = getFiscBusiness().sendPendingRequestToFISC(inbkpendRefBase);
				defINBKPEND = inbkpendRefBase.get();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				// (5) @檢核財金Response電文 Header
				rtnCode = getFiscBusiness().checkHeader(getFiscReq(), false);
				if ("10".equals(StringUtils.substring(Objects.toString(rtnCode.getValue()), 0, 2))) {
					/* Garbled Message */
					getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
					// NEXT LOOP
					break;
				}

				if (rtnCode != CommonReturnCode.Normal) {
					defINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
				} else {
					defINBKPEND.setInbkpendRepRc(getFiscReq().getResponseCode());
					if("2290".equals(getFiscReq().getProcessingCode())){
						defINBKPEND.setInbkpendPrcResult(getFiscReq().getRsCode());
						/* 2019/01/31 修改 for跨行轉帳小額交易手續費調降 */
						if(Integer.parseInt(getFiscBusiness().getFeptxn().getFeptxnTxDate()) >=Integer.parseInt(INBKConfig.getInstance().getTFRSEffectDate())){
							if(StringUtils.isNotBlank(getFiscReq().getAcctSup())){
								defINBKPEND.setInbkpendAcctSup(getFiscReq().getAcctSup());
							}
						}
					}
				}

				// (6) 檢核訊息押碼(MAC)
				/* Prepare FEPTXN for DES */
				desFEPTXN.setFeptxnRepRc(defINBKPEND.getInbkpendRepRc());
				encHelper = new ENCHelper(desFEPTXN, getTxData());
				rtnCode = encHelper.checkFISCMACPend(getFiscReq().getMessageType().substring(2, 4),
						getFiscReq().getMAC());
				if (rtnCode != CommonReturnCode.Normal) {
					defINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
				}
				// (7) 檢核 ORI_STAN
				if (!getFiscReq().getOriStan().equals(getFiscReq().getOriStan())) {
					rtnCode = FISCReturnCode.OriginalMessageDataError; /* 1003: 欄位 MAPPING 不符 */
					defINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
				}
				// (8) 檢核財金回應 RC
				if (!NormalRC.FISC_OK.equals(getFiscReq().getResponseCode())) { /* -REP */
					fiscResRC = getFiscReq().getResponseCode(); /* 將財金RC 顯示於畫面 */
				}

				// (9) 更新FEPTXN(if need)
				if("2290".equals(uiPCODE)){
					rtnCode=updateFEPTXN();
					if(rtnCode != CommonReturnCode.Normal){
						break;
					}
				}
				// (10) @更新交易記錄(INBKPEND &FEPTXN)(PCODE 2280/2290)
				rtnCode = updateInbkpendAndFeptxn(rtnCode);
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".readDataAndSendToFISC"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 跨行延遲交易檔初始資料
	 *
	 * @param drCount 單筆(=0)/多筆(>0)
	 * @return
	 *
	 *         <modify> <modifier>HusanYin</modifier> <reason>修正Const RC</reason>
	 *         <date>2010/11/25</date> </modify>
	 */
	private FEPReturnCode prepareINBKPEND(int drCount) {
		try {

			defINBKPEND
					.setInbkpendTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))); /* 交易日期 */

			if (drCount > 0) {
				// 多筆取新的電子日誌序號
				defINBKPEND.setInbkpendEjfno(TxHelper.generateEj()); /* 電子日誌序號 */
			} else {
				// 單筆或多筆的第一筆
				defINBKPEND.setInbkpendEjfno(getTxData().getEj()); /* 電子日誌序號 */
			}

			defINBKPEND.setInbkpendBkno(SysStatus.getPropertyValue().getSysstatHbkno()); /* 交易啟動銀行 */
			defINBKPEND.setInbkpendTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))); /* 交易時間 */
			if("2261".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                    ||"2262".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                    ||"2263".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
                    ||"2264".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())){
                defINBKPEND.setInbkpendAtmno(StringUtils.rightPad("ID",6,' '));
            }else{
                defINBKPEND.setInbkpendAtmno(getFiscBusiness().getFeptxn().getFeptxnAtmno());
            }
			defINBKPEND.setInbkpendMajorActno(getFiscBusiness().getFeptxn().getFeptxnMajorActno());
			defINBKPEND.setInbkpendTroutbkno(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());
			defINBKPEND.setInbkpendTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
			defINBKPEND.setInbkpendTrinBkno(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
			defINBKPEND.setInbkpendTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
			defINBKPEND.setInbkpendTrinActnoActual(getFiscBusiness().getFeptxn().getFeptxnTrinActnoActual());
			/* 1/29 修改 for 跨行提領外幣交易(FAW) */
			if (!ATMTXCD.FAW.toString().equals(getFiscBusiness().getFeptxn().getFeptxnTxCode())) {
				/* 跨行台幣交易 */
				defINBKPEND.setInbkpendTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmt());
			} else {
				/* 跨行提領外幣交易(FAW) */
				defINBKPEND.setInbkpendTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
			}
			defINBKPEND.setInbkpendStan(getFiscBusiness().getStan());
			defINBKPEND.setInbkpendReqRc(NormalRC.FISC_REQ_RC);
			defINBKPEND.setInbkpendSubsys((short) SubSystem.INBK.getValue());
			defINBKPEND.setInbkpendPcode(uiPCODE);
			defINBKPEND.setInbkpendCbsRrn(
					StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRrn()) ? getFiscBusiness().getFeptxn().getFeptxnCbsRrn() : StringUtils.SPACE);
			defINBKPEND.setInbkpendOriBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
			defINBKPEND.setInbkpendOriStan(getFiscBusiness().getFeptxn().getFeptxnStan());
			defINBKPEND.setInbkpendOriTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
			defINBKPEND.setInbkpendOriPcode(getFiscBusiness().getFeptxn().getFeptxnPcode());
			defINBKPEND.setInbkpendOriTxFlag(DbHelper.toShort(true));
			defINBKPEND.setInbkpendOriEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
			defINBKPEND.setInbkpendOriTbsdyFisc(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc());
			defINBKPEND.setInbkpendDesBkno(SysStatus.getPropertyValue().getSysstatFbkno()); /* 財金代號 */
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
			getFiscBusiness().getFeptxn().setFeptxnTbsdy(zone.getZoneTbsdy());
			getFiscBusiness().getFeptxn().setFeptxnChannel(FEPChannel.FISC.name()); // 通道別
			getFiscBusiness().getFeptxn().setFeptxnMsgid(getTxData().getMsgCtl().getMsgctlMsgid());
			getFiscBusiness().getFeptxn().setFeptxnTxrust("0");
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName + ".prepareINBKPEND"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 產生 2280/2290 財金通知電文
	 *
	 * @return
	 *
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// (2.a) Prepare Header & Body, 格式(2280:CDPM01, 2290:CDMI01) 內容如下:
		// Header (ReqHEAD)
		rtnCode = getFiscBusiness().preparePendHeader("0200", defINBKPEND);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		if("2290".equals(uiPCODE)){
			getFiscReq().setTxAmt(defINBKPEND.getInbkpendTxAmt().toString());
			getFiscReq().setATMNO(defINBKPEND.getInbkpendAtmno());
		}

		getFiscReq().setOriStan(StringUtils.join(defINBKPEND.getInbkpendOriBkno(), defINBKPEND.getInbkpendOriStan()));

		// (2.b)產生訊息押碼(MAC) */
		/* Prepare FEPTXN for DES, pls new FEPTXN just for DES */
		rtnCode = prepareDesFEPTXN();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		encHelper = new ENCHelper(desFEPTXN, getTxData());
		RefString mac = new RefString(getFiscReq().getMAC());
		rtnCode = encHelper.makeFISCMACPend(getFiscReq().getMessageType().substring(2, 4), mac);
		getFiscReq().setMAC(mac.get());
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// (2.c)Make Bit Map
		rtnCode = getFiscBusiness().makeBitmap(getFiscReq().getMessageType(), getFiscReq().getProcessingCode(),
				MessageFlow.Request);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
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
		defINBKPEND.setInbkpendPending((short) 1); /* PENDING */
		defINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Request); /* FISC REQUEST */
		defINBKPEND.setInbkpendAaRc("0601");
		defINBKPEND.setInbkpendFiscTimeout(DbHelper.toShort(true));
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
	 * 更新FEPTXN
	 *
	 * @return
	 *
	 */
	private FEPReturnCode updateFEPTXN() throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); /* 取消Pending */
		getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_2290_INQ); /* 已發動2290 */
		getFiscBusiness().getFeptxn().setFeptxnConRc(defINBKPEND.getInbkpendPrcResult()); /* 處理結果 */
		/* 2019/01/31 修改 for跨行轉帳小額交易手續費調降 */
		if (StringUtils.isNotBlank(defINBKPEND.getInbkpendAcctSup())) {
			getFiscBusiness().getFeptxn().setFeptxnAcctSup(defINBKPEND.getInbkpendAcctSup()); /* 帳戶補充資訊 */
		}
		if ("00".equals(defINBKPEND.getInbkpendPrcResult())) { /* 成功 */
			getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); /* 處理結果=成功 */
		} else { /* 處理結果-失敗 */
			getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); /* 交易Reverse */
			getFiscBusiness().getFeptxn().setFeptxnClrType((short) 2); /* 跨行清算Reverse */
		}

		feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8),
				StringUtils.join(ProgramName, ".updateFEPTXN"));
		/* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]) */
		if (feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getFeptxn()) < 1) {
			return IOReturnCode.FEPTXNUpdateError;
		}
		return rtnCode;
	}

	/**
	 * (10) 更新交易記錄(INBKPEND &FEPTXN)(PCODE 2280/2290)
	 *
	 * @param rtnCode
	 * @throws Exception
	 */
	private FEPReturnCode updateInbkpendAndFeptxn(FEPReturnCode rtnCode) throws Exception {
		_rtnCode =CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			defINBKPEND.setInbkpendAaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
			int updCount = dbINBKPEND.updateByPrimaryKeySelective(defINBKPEND);
			if (updCount < 1) {
				transactionManager.rollback(txStatus);
				return IOReturnCode.UpdateFail;
			}
			getFiscBusiness().getFeptxn().setFeptxnPending(defINBKPEND.getInbkpendPending());
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(defINBKPEND.getInbkpendMsgflow());
			getFiscBusiness().getFeptxn().setFeptxnFiscTimeout(defINBKPEND.getInbkpendFiscTimeout());
			getFiscBusiness().getFeptxn().setFeptxnRepRc(defINBKPEND.getInbkpendRepRc());
			getFiscBusiness().getFeptxn().setFeptxnAaRc(IntegerUtils.parseInt(defINBKPEND.getInbkpendAaRc(), 0));
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
				return IOReturnCode.UpdateFail;
			}
			transactionManager.commit(txStatus);
			return _rtnCode;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "updateInbkpendAndFeptxn");
			sendEMS(getLogContext());
		}
		return _rtnCode;
	}

	/**
	 * Prepare FEPTXN for DES
	 *
	 * @return
	 *
	 */
	private FEPReturnCode prepareDesFEPTXN() {
		try {
			desFEPTXN.setFeptxnTxDate(defINBKPEND.getInbkpendTxDate());
			desFEPTXN.setFeptxnBkno(defINBKPEND.getInbkpendBkno());
			desFEPTXN.setFeptxnStan(defINBKPEND.getInbkpendStan());
			desFEPTXN.setFeptxnDesBkno(defINBKPEND.getInbkpendOriBkno());
			desFEPTXN.setFeptxnOriStan(defINBKPEND.getInbkpendOriStan());
			desFEPTXN.setFeptxnReqRc(defINBKPEND.getInbkpendReqRc());
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareDesFEPTXN"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
