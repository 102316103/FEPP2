package com.syscom.fep.server.aa.inbk;


import java.math.BigDecimal;
import java.util.Calendar;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.enums.FISCPCode;
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
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 處理財金發動約定及核驗服務確認電文
 @author Joseph
 */

public class VAConfirmI extends INBKAABase {
	///#Region "共用變數宣告"
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode1 = CommonReturnCode.Normal; //for主流程第三點更新交易記錄(FEPTXN & VATXN)，儲存更新失敗的FEPReturnCode
	private Vatxn defvatxn = new Vatxn();

	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 * @throws Exception
	 */
	public VAConfirmI(FISCData txnData) throws Exception {
		super(txnData);
	}


	/**
	 * 程式進入點
	 *
	 * @return Response電文
	 */
	@Override
	public String processRequestData() {
		try {
			//1.拆解並檢核財金電文
			_rtnCode = processRequest();

			//2.商業邏輯檢核＆電文Body檢核
			if (_rtnCode == CommonReturnCode.Normal){
				checkBusinessRule();
			}

			//3.更新交易記錄 (FEPTXN & VATXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = updateTxData();
			}

			//4.判斷是否沖轉跨行代收付ProcessAPTOT
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processAPTOT();
			}

			//5.更新交易記錄 (查不到原始資料則不更新FEPTXN)
			_rtnCode = updateFEPTXN();

			//6.判斷是否需傳送2160電文給財金
			if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnSend2160()) && "Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160())){
				_rtnCode=insertINBK2160();
			}
			//7.FEP通知主機交易結束
			_rtnCode = SendToCBS();
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(this.logContext);
		}
		return "";

	}
	private FEPReturnCode SendToCBS() throws Exception {
		FEPReturnCode rc2 = CommonReturnCode.Normal;
		/*沖轉主機帳務*/
		String AATxTYPE = "";
		String AATxRs = "N";
		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid1();
		ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
		rc2 = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE,AATxRs);
		return rc2;
	}
	private FEPReturnCode insertINBK2160() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			rtnCode = getFiscBusiness().prepareInbk2160();
			if (rtnCode != CommonReturnCode.Normal) {
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setRemark("寫入檔案發生錯誤!!");
				logMessage(Level.INFO, getLogContext());
				return FEPReturnCode.INBK2160InsertError;
			} else {
				return FEPReturnCode.Normal;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequest");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}


	/**
	 * "拆解並檢核由財金發動的Request電文"
	 *
	 * @return
	 */
	private FEPReturnCode processRequest() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			//檢核Header
			rtnCode = getFiscBusiness().checkHeader(getFiscCon(), true);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().setFeptxn(null);
				getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
				return rtnCode;
			}

			getFiscBusiness().setFeptxn(getFiscBusiness().getOriginalFEPTxn());
			getTxData().setFeptxn(getFiscBusiness().getFeptxn());

			//FISCRC=6101要寄Email需將值塞入LogContext.TroutActno
			getLogContext().setAtmNo(getFiscBusiness().getFeptxn().getFeptxnAtmno());
			getLogContext().setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
			getLogContext().setTrinBank(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
			getLogContext().setTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
			getLogContext().setTroutBank(getFiscBusiness().getFeptxn().getFeptxnTroutBkno());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequest");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * "商業邏輯檢核,電文Body檢核"
	 *
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_TX_DATE = "";

		try {
			//檢核 Mapping 欄位
			if(Integer.parseInt(this.getFiscCon().getTxnInitiateDateAndTime().substring(0,2)) < 90) {
				wk_TX_DATE = CalendarUtil.rocStringToADString("20110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
			}else {
				wk_TX_DATE = CalendarUtil.rocStringToADString("19110000" + this.getFiscCon().getTxnInitiateDateAndTime().substring(0, 6));
			}

			String aa=  (CalendarUtil.rocStringToADString(StringUtils.leftPad(this.getFiscCon().getTxnInitiateDateAndTime().substring(0,6),7,"0"))) + this.getFiscCon().getTxnInitiateDateAndTime().substring(6,12);
			if(!getFiscBusiness().getFeptxn().getFeptxnReqDatetime().equals(aa) ||
					(!getFiscBusiness().getFeptxn().getFeptxnDesBkno().equals(this.getFiscCon().getTxnDestinationInstituteId().substring(0,3))) ||
					(StringUtils.isNotBlank(this.getFiscCon().getATMNO()) && !getFiscBusiness().getFeptxn().getFeptxnAtmno().equals(this.getFiscCon().getATMNO()))) {
				return FEPReturnCode.MessageFormatError;
			}

			//檢核交易是否未完成
			if(!getFiscBusiness().getFeptxn().getFeptxnTxrust().equals("B")) {
				/* 10/20 修改, 財金錯誤代碼改為 ‘0101’ */
				return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
			}
			/* 9/22 修改 for CON 送2次 */
			if(!getFiscBusiness().getFeptxn().getFeptxnTraceEjfno().equals(0)) {
				this.getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
				return FEPReturnCode.MessageFormatError; //11011  **相關欄位檢查錯誤
			}

			//檢核 MAC
			getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscCon().getResponseCode());
			/* 11/16 修改, 收到財金確認電文時間寫入FEPTXN */
			getFiscBusiness().getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(),FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
			rtnCode = encHelper.checkFiscMac(this.getFiscCon().getMessageType(), getFiscCon().getMAC());
			this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
			if(rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnConRc(null);
				return FEPReturnCode.ENCCheckMACError;//**訊息押碼錯誤
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	//	    '''<summary>
//		''' UpdateTxData更新交易記錄(FEPTXN及FAVTXN)
//		''' </summary>
//		''' <returns></returns>
//		''' <remarks></remarks>
	private FEPReturnCode updateTxData() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false));/*AA Initial*/
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
				if (NormalRC.FISC_ATM_OK.equals(this.getFiscCon().getResponseCode())) { /*+CON*/
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); /*成功*/
				} else {
					getFiscBusiness().getFeptxn().setFeptxnTxrust("C"); /*Accept-Reverse*/
				}
				getFiscBusiness().getFeptxn().setFeptxnPending((short) 2); /*解除 PENDING */
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // 'F3  FISC CONFIRM
			getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
			getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscCon().getResponseCode());

			this.feptxnDao.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData")); // 2021-06-16 Richard add

			// 'add by maxine on 2012/03/05 for 換日後fiscBusiness.DBFEPTXN,以及txData.FEPTXN都應該使用實際交易日期,避免後面更新不到FEPTXN
			getTxData().setFeptxnDao(this.feptxnDao);
			getFiscBusiness().setFeptxnDao(this.feptxnDao);

			int i = this.feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getFeptxn());
			if (i <= 0) {
				transactionManager.rollback(txStatus);
				_rtnCode = IOReturnCode.FEPTXNUpdateError;
				return _rtnCode;
			}

			VatxnMapper dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
			defvatxn =dbVATXN.selectByPrimaryKey(getFiscBusiness().getFeptxn().getFeptxnTxDate(),getFiscBusiness().getFeptxn().getFeptxnEjfno());
			int iRes = 0;
			defvatxn.setVatxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
			defvatxn.setVatxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
			defvatxn.setVatxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
			defvatxn.setVatxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
			iRes = dbVATXN.updateByPrimaryKey(defvatxn);
			if (iRes <= 0) {
				transactionManager.rollback(txStatus);
				_rtnCode1 = IOReturnCode.UpdateFail;
				return _rtnCode1;
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			// 若失敗則復原
			transactionManager.rollback(txStatus);
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
		return FEPReturnCode.Normal;
	}

	//	    ''' <summary>
//		''' 更新FEPTXN
//		''' </summary>
//		''' <returns></returns>
//		''' <remarks></remarks>
	private FEPReturnCode updateFEPTXN() throws Exception {
		if (getFiscBusiness().getFeptxn().getFeptxnAaRc().equals(FEPReturnCode.Normal.getValue())) {
			if(this._rtnCode != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode.getValue());
			}else if(this._rtnCode1 != FEPReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(this._rtnCode1.getValue());
			}
		}
		getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
		if (this.feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getFeptxn()) > 0) {
			return FEPReturnCode.Normal;
		}
		return FEPReturnCode.UpdateFail;
	}


	//	    ''' <summary>
//		''' 判斷是否沖轉跨行代收付ProcessAPTOT
//		''' </summary>
//		''' <returns></returns>
//		''' <remarks></remarks>
//		''' <history>
//		'''   <modify>
//		'''     <modifier>Ruling</modifier>
//		'''     <reason>2566約定及核驗服務類別10</reason>
//		'''     <date>2019/03/20</date>
//		'''   </modify>
//		''' </history>
	private FEPReturnCode processAPTOT() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())){ /* +REP */
			if (!NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())){ /* -CON */
				/*沖轉跨行代收付*/
				rtnCode = getFiscBusiness().processAptot(true);
				this.logContext.setProgramName(ProgramName);
				TxHelper.getMessageFromFEPReturnCode(getFiscBusiness().getFeptxn().getFeptxnConRc(), FEPChannel.FISC, getLogContext());
			}
			//20221028 -conf 也必須送CBS
			if(getTxData().getMsgCtl().getMsgctlCbsFlag().equals((short)2)){
				String AATxTYPE= "2"; //沖正
				this.getTxData().setVatxn(defvatxn);
				String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
				ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, this.getTxData());
				rtnCode = new CBS(hostAA, this.getTxData()).sendToCBS(AATxTYPE);
			}
		}
		return rtnCode;
	}

}

