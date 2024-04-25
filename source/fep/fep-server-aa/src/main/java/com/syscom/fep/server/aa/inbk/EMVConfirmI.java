package com.syscom.fep.server.aa.inbk;

import java.util.Calendar;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

public class EMVConfirmI extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode1 = CommonReturnCode.Normal;

	public EMVConfirmI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.拆解並檢核財金電文
			_rtnCode = processRequest();

			// 2.商業邏輯檢核＆電文Body檢核
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = checkBusinessRule();
			}

			// 3.更新交易記錄 (FEPTXN & INTLTXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = updateTxData();
			}

			// 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processAPTOTSendToCBSASC();
			}

			// 6.更新交易記錄
			if (_rtnCode1 == CommonReturnCode.Normal && getFiscBusiness().getFeptxn() != null) {
				_rtnCode = updateFEPTXN();
			}

			// 2018/12/26 Modify by Ruling for 避免2633沖正交易，原交易結果已更新為D沖正，但因2630
			// Confirm發送MailHunter逾時後又將交易結果更新為A成功，將此段移至後面發簡訊及EMAIL
			// 7.發送簡訊及EMAIL
			sendToMailHunter();

		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			logContext.setProgramException(ex);
			logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(logContext);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 1.拆解並檢核由財金發動的Request電文
	 */
	private FEPReturnCode processRequest() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 檢核Header
			rtnCode = getFiscBusiness().checkHeader(getFiscEMVICCon(), true);
			if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate
					|| rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
					|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
				// 查不到原始資料則不更新FEPTXN
				getFiscBusiness().setFeptxn(new FeptxnExt());
				getFiscBusiness().sendGarbledMessage(getFiscEMVICCon().getEj(), rtnCode, getFiscEMVICCon());
				return rtnCode;
			}
			getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());// for txdata can read originalFeptxn
			getTxData().setFeptxn(getFiscBusiness().getFeptxn());

			// FISCRC=6101要寄Email需將值塞入LogContext.TroutActno
			logContext.setAtmNo(getFiscBusiness().getFeptxn().getFeptxnAtmno());
			logContext.setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
			logContext.setTrinBank(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
			logContext.setTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
			logContext.setTroutBank(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());

			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
		} catch (Exception ex) {
			logContext.setProgramException(ex);
			logContext.setProgramName(StringUtils.join(ProgramName, ".processRequest"));
			sendEMS(logContext);
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 2.商業邏輯檢核,電文Body檢核
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_TX_DATE = null;
		try {
			// 2.1.檢核 Mapping 欄位
			wk_TX_DATE = CalendarUtil.rocStringToADString(
					StringUtils.leftPad(getFiscEMVICCon().getTxnInitiateDateAndTime().substring(0, 6), 7, "0"));
			// 改抓 FEPTXN_REQ_DATETIME欄位
			if (!wk_TX_DATE.equals(getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(0, 8))
					|| !getFiscBusiness().getFeptxn().getFeptxnReqDatetime().substring(8, 14)
							.equals(getFiscEMVICCon().getTxnInitiateDateAndTime().substring(6, 12))
					|| !getFiscBusiness().getFeptxn().getFeptxnDesBkno()
							.equals(getFiscEMVICCon().getTxnDestinationInstituteId().substring(0, 3))
					|| (StringUtils.isNotBlank(getFiscEMVICCon().getATMNO())
							&& !getFiscEMVICCon().getATMNO().equals(getFiscBusiness().getFeptxn().getFeptxnAtmno()))
					|| (StringUtils.isNotBlank(getFiscEMVICCon().getTxAmt())
							&& MathUtil.compareTo(getFiscBusiness().getFeptxn().getFeptxnTxAmt(),
									getFiscEMVICCon().getTxAmt()) != 0)) {
				return FISCReturnCode.OriginalMessageDataError;
			}

			// 2.2.檢核交易是否未完成
			if (!FeptxnTxrust.Pending.equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				// 檢核電文欄位內容有誤，應由CardReturnCode.CheckFieldError改用FISCReturnCode.MessageFormatError(0101:訊息格式或內容編輯錯誤)
				rtnCode = FISCReturnCode.MessageFormatError; // **相關欄位檢查錯誤
				return rtnCode;
			}

			// 為了避免送兩次Confrim電文加檢核FEPTXN_TRACE_EJFNO<>0
			if (getFiscBusiness().getFeptxn().getFeptxnTraceEjfno() != 0) {
				this.logContext.setRemark("已有Confirm電文, FEPTXN_TRACE_EJFNO="
						+ getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().toString());
				logMessage(Level.INFO, getLogContext());
				rtnCode = FISCReturnCode.MessageFormatError; // ' **相關欄位檢查錯誤
				getFiscBusiness().setFeptxn(new FeptxnExt()); // ' 第2次Confirm則不更新FEPTXN
				getFiscBusiness().sendGarbledMessage(getFiscEMVICCon().getEj(), rtnCode, getFiscEMVICCon());
				return rtnCode;
			}

			// 2.3.檢核 MAC
			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscEMVICCon().getResponseCode());
			// 2017/11/17 Modify by Ruling for 收到財金確認電文時間寫入FEPTXN
			getFiscBusiness().getFeptxn().setFeptxnConTxTime(
					FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			rtnCode = encHelper.checkFiscMac(getFiscEMVICCon().getMessageType(), getFiscEMVICCon().getMAC());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnConRc(null);
				return rtnCode;
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			logContext.setProgramException(ex);
			logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(logContext);
		}
		return rtnCode;
	}
	
	/**  
	 3.UpdateTxData:更新交易記錄
	*/
	private FEPReturnCode updateTxData()
	{
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (NormalRC.FISC_ATM_OK.equals(getFiscEMVICCon().getResponseCode())) {
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);
				} else {
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
				}
				getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // F3
			// 修正被代理交易Confirm的ej number沒寫入FEPTXN_TRACE_EJNO的問題
			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscEMVICCon().getResponseCode());
			this.feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8),
					StringUtils.join(ProgramName, "updateTxData"));
			// 換日後fiscBusiness.DBFEPTXN,以及txData.FEPTXN都應該使用實際交易日期,避免後面更新不到FEPTXN
			getTxData().setFeptxnDao(this.feptxnDao);
			getFiscBusiness().setFeptxnDao(this.feptxnDao);
			int i = this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn());
			if (i <= 0) {
				transactionManager.rollback(txStatus);
				_rtnCode1 = IOReturnCode.FEPTXNUpdateError;
				return _rtnCode1;
			}

			if ("26".equals(getFiscEMVICCon().getProcessingCode().substring(0, 2))) {
				Intltxn intltxn = new Intltxn();
				IntltxnMapper intltxnMapper = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
				int iRes = 0;
				intltxn.setIntltxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
				intltxn.setIntltxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
				intltxn = intltxnMapper.selectByPrimaryKey(intltxn.getIntltxnTxDate(), intltxn.getIntltxnEjfno());
				if (intltxn != null) {
					intltxn.setIntltxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
					intltxn.setIntltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
					iRes = intltxnMapper.updateByPrimaryKey(intltxn);
					if (iRes <= 0) {
						transactionManager.rollback(txStatus);
						_rtnCode1 = IOReturnCode.UpdateFail;
						return _rtnCode1;
					}
				}
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			// 若失敗則復原
			transactionManager.rollback(txStatus);
			logContext.setProgramException(ex);
			logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(logContext);
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}
	
	// 6.更新交易記錄
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = null;
		getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
		rtnCode = getFiscBusiness().updateTxData();
		if (getFiscBusiness().getFeptxn().getFeptxnAaRc() == CommonReturnCode.Normal.getValue()) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		}
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		return rtnCode;
	}

	/** 
	 4.判斷是否沖轉跨行代收付ProcessAPTOT及主機帳務SendToCBS/ASC)
	*/
	private FEPReturnCode processAPTOTSendToCBSASC() throws Exception {
		T24 hostT24 = new T24(getTxData());
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
				// 沖轉跨行代收付
				rtnCode = getFiscBusiness().processAptot(true);
				getLogContext().setProgramName(ProgramName);
				// 沖轉主機帳務
				if (SysStatus.getPropertyValue().getSysstatHbkno()
						.equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
					switch (getFiscBusiness().getFeptxn().getFeptxnZoneCode()) {
					case ZoneCode.TWN:
						getLogContext()
								.setRemark("FEPTXN_CBS_TX_CODE=" + getFiscBusiness().getFeptxn().getFeptxnCbsTxCode());
						logMessage(Level.DEBUG, getLogContext());
						rtnCode = hostT24.sendToT24(getFiscBusiness().getFeptxn().getFeptxnCbsTxCode(), 2, true);
						break;
					}
				}
				getLogContext().setProgramName(ProgramName);
				TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC,
						getLogContext());
			}
		}
		return rtnCode;
	}
	
	/** 
	 CON(+)發送簡訊及EMAIL
	 <modify>
		 <modifier>Ruling</modifier>
		 <reason>避免2633沖正交易，原交易結果已更新為D沖正，但因2630 Confirm發送MailHunter逾時後又將交易結果更新為A成功，將此段移至後面發簡訊及EMAIL</reason>
		 <date>2018/12/26</date>
	 </modify>
	*/
	private void sendToMailHunter() {
		try {
			if (getFiscBusiness().getFeptxn() != null
					&& FISCPCode.PCode2630.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
					&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
					&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
				getFiscBusiness().prepareSMSMAIL();
			}
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
			sendEMS(this.logContext);

		}
	}

}
