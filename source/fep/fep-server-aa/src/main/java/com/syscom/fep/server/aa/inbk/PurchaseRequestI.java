package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.AllbankMapper;
import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.mapper.HotacqcntryMapper;
import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.mapper.QrptxnMapper;
import com.syscom.fep.mybatis.mapper.SysconfMapper;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Hotacqcntry;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.mybatis.model.Qrptxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.App;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.Dapp;
import com.syscom.fep.server.common.business.host.Ncnb;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.CBSHostType;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.T24Version;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.ATMNCCardStatus;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.enums.DAPPAppMsg;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author Richard
 */
public class PurchaseRequestI extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode3 = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode4 = CommonReturnCode.Normal;
	private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
	private boolean isPlusCirrus = false;
	private Intltxn defINTLTXN = new Intltxn();
	private Intltxn oriINTLTXN = new Intltxn();
	private IntltxnMapper dbINTLTXN = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	private boolean isEC = false;
	private boolean isNWD = false;
	private Nwdtxn defNWDTXN;
	private NwdtxnMapper dbNWDTXN = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
	private String rsCARD = "N";
	private String _OWDCount;
	private boolean isQRPScaned = false; // 豐錢包被掃交易
	private boolean isQRPMain = false; // 豐錢包主掃交易
	private Qrptxn defQRPTXN;
	private QrptxnMapper dbQRPTXN = SpringBeanFactoryUtil.getBean(QrptxnMapper.class);

	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 *
	 *        初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	 *        ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	 *        FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	 * @throws Exception
	 */
	public PurchaseRequestI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 */
	@Override
	public String processRequestData() {
		try {
			// 拆解並檢核財金電文
			_rtnCode = this.processRequestHeader();

			//2.新增交易記錄
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode2 = addTxData();
				if (_rtnCode2 != CommonReturnCode.Normal) {
					return _rtnCode2.toString();
				}
			}

			//3.商業邏輯檢核 & 電文Body檢核
			if (_rtnCode == CommonReturnCode.Normal && _rtnCode2 == CommonReturnCode.Normal) { /*CheckHeader Error*/
				_rtnCode = checkBusinessRule();
			}

			//4.SendToCBS
			if(_rtnCode ==CommonReturnCode.Normal){
				_rtnCode = sendToCBS();
			}

			//6.PrepareFISC:準備回財金的相關資料
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareForFISC();
			}

			//7.UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.updateTxData();
			}

			//8.ProcessAPTOT:更新跨行代收付
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.processAPTOT();
			}

			//9.將組好的財金電文送給財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			}

			//10.判斷是否需傳送2160電文給財金
			if(_rtnCode == CommonReturnCode.Normal){
				if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnSend2160())
						&& "Y".equals(getFiscBusiness().getFeptxn().getFeptxnSend2160())){
					_rtnCode =insertINBK2160();
				}
			}
		} catch (Exception e) {
			this._rtnCode = CommonReturnCode.ProgramException;
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(this.logContext);
		} finally {
			this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			this.getTxData().getLogContext().setMessage("FiscResponse:"+this.getFiscRes().getFISCMessage());
			this.getTxData().getLogContext().setProgramName(this.aaName);
			this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, this.logContext);
		}
		// 2011/03/17 modified by Ruling for 若回rtnCode給Handler，FISCGW會將此值回給財金，但此時AA已結束不需在回財金，故改成回空白
		return StringUtils.EMPTY;
	}
	private FEPReturnCode insertINBK2160() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			//檢核Header
			rtnCode = getFiscBusiness().prepareInbk2160();
			if(rtnCode != CommonReturnCode.Normal){
				getLogContext().setMessage(rtnCode.toString());
				getLogContext().setRemark("寫入檔案發生錯誤!!");
				logMessage(Level.INFO, getLogContext());
				return FEPReturnCode.INBK2160InsertError;
			}else{
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
	 * 拆解並檢核由財金發動的Request電文
	 *
	 * @return
	 *
	 */
	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// '檢核財金電文 Header
		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

		if (rtnCode !=CommonReturnCode.Normal) {  /*FISC RC:Garbled Message*/
			getFiscBusiness().setFeptxn(null);
			getFiscBusiness().sendGarbledMessage(getFiscCon().getEj(), rtnCode, getFiscCon());
			return rtnCode;
		}

		return rtnCode;
	}

	/**
	 * 新增交易記錄
	 *
	 * @return
	 *
	 */
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			//Prepare()交易記錄初始資料
			_rtnCode2 = getFiscBusiness().prepareFEPTXN();
			if (_rtnCode2 != CommonReturnCode.Normal) {
				return _rtnCode2;
			}
			//以TRANSACTION 新增交易記錄
			_rtnCode3 = getFiscBusiness().insertFEPTxn();
			if (_rtnCode3 != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				return _rtnCode3;
			}

			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".addTxData");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
	/**
	 * 商業邏輯檢核
	 *
	 * @return
	 *
	 */

	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnMerchantId())){
				if(!FISCPCode.PCode2543.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())){
					rtnCode=getFiscBusiness().checkMerchant();
					if(rtnCode != CommonReturnCode.Normal){
						return rtnCode;
					}
				}
			}

			//20220726 只CHECK MAC, TAC 由CBS檢核
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
			logMessage(this.logContext);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			//檢核&更新原始交易狀態
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				rtnCode = checkoriFEPTXN();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "checkBusinessRule");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * 6. SendToCBS/ASC(if need): 帳務主機處理
	 *
	 * <history>
	 * <modify>
	 * <modifer>Husan </modifer>
	 * <time>2010/12/01</time>
	 * <reason>修正上主機部分改由參考HostBusiness</reason>
	 * <time>2010/12/7</time>
	 * <reason>Feptxn_Remark記錄INTLTXN_ACQ_CNTRY</reason>
	 * </modify>
	 * </history>
	 *
	 * @return
	 *
	 */
	private FEPReturnCode sendToCBS() {
		// modify 2010/12/01
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			String TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().toString();
			switch(getFiscBusiness().getFeptxn().getFeptxnPcode()){
				case "2541":
				case "2542":
					TxType="1";
					break;
				case "2543":
					TxType="0";
					break;
			}
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
			tota = hostAA.getTota();
			if (rtnCode != CommonReturnCode.Normal) {
				if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
					return CommonReturnCode.Normal;
				} else {
					getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
					getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
					getFiscBusiness().updateTxData();
					if(StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())){
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); /*成功*/
						if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
							// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
							return IOReturnCode.FEPTXNUpdateError;
						}
					}
					return  rtnCode;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".SendToCBSAndAsc");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	private FEPReturnCode prepareForFISC() throws Exception {
		if (_rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				this.logContext.setProgramName(StringUtils.join(ProgramName));
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC, getLogContext()));
			} else {
			}
		} else if (_rtnCode2 != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				this.logContext.setProgramName(StringUtils.join(ProgramName));
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode2, FEPChannel.FISC, getLogContext()));
			} else {
			}
		} else if (_rtnCode3 != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode3.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				this.logContext.setProgramName(StringUtils.join(ProgramName));
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCode3, FEPChannel.FISC, getLogContext()));
			} else {
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
		}
		_rtnCode4 = getFiscBusiness().prepareHeader("0210");
		if (_rtnCode4 != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
		}

		_rtnCode = prepareBody();

		_rtnCode4 = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
		this.logContext.setMessage("after makeBitmap RC:" + _rtnCode4.toString());
		logMessage(this.logContext);
		if (_rtnCode4 != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
			getFiscRes().setBitMapConfiguration("0000000000000000");
		}
		_rtnCode=getFiscRes().makeFISCMsg();

		return _rtnCode4;
	}

	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {// (3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // Pending
				} else {// (2 way)
					if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
							&& !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
						getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
					} else {
						getFiscBusiness().getFeptxn().setFeptxnTxrust("D"); // 已沖銷成功
					}
				}
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					if (!FISCPCode.PCode2430.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
							&& !FISCPCode.PCode2470.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 非國際提款沖銷
						isEC = false;
						getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
					} else {
						isEC = true;
						getFiscBusiness().getFeptxn().setFeptxnClrType((short) 2);
					}
				}
			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {// spec change 20101124
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // 拒絕
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /*AA Close*/

			rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
			if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
				transactionManager.rollback(txStatus);
				return rtnCode;
			}
			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			if (!txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "updateTxData");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			_rtnCode4 = getFiscBusiness().processAptot(isEC);
			if (_rtnCode4 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}

	private FEPReturnCode sendToNB() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 2020/07/02 Modify by Ruling for 大戶APP無卡提款推播
		// 2020/09/09 Modify by Ruling for 新增MDAWHO Channel Code：依預約Channel為MDAWHO，發送大戶APP WebService
		if (FEPChannel.MOBILBANK.name().equals(defNWDTXN.getNwdtxnRegChannel())) {
			Ncnb hostNB = new Ncnb(getTxData());
			rtnCode = hostNB.sendToNCNB("2", "");

		}
//		else if (FEPChannel.DAPP.name().equals(defNWDTXN.getNwdtxnRegChannel())
//				|| FEPChannel.MDAWHO.name().equals(defNWDTXN.getNwdtxnRegChannel())) {
//			Dapp hostDAPP = new Dapp(getTxData());
//			rtnCode = hostDAPP.sendToDAPP(DAPPAppMsg.SSCodeErrorLimit.getValue());
//		}
		return rtnCode;
	}


	/**
	 * 組財金電文Body部份
	 *
	 * @return
	 *
	 *         <modify>
	 *         <modifier>HusanYin</modifier>
	 *         <reason>修正Const RC</reason>
	 *         <date>2010/11/25</date>
	 *         <reason>connie spec modify</reason>
	 *         <date>2010/11/29</date>
	 *         </modify>
	 */
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_BITMAP = null;

		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) { /*+REP*/
				/*跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組*/
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else {  /*-REP*/
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}
			// 依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				// Loop IDX from 3 to 64
				if (wk_BITMAP.charAt(i) == '1') {
					switch (i) {
						case 2: { /* 交易金額 */
							getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmt().toString());
							break;
						}
						case 5: { /* 代付單位 CD/ATM 代號 */
							getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
							break;
						}
						case 6: { /* 可用餘額 */
							/* 11/19 配合永豐修改, 改送帳戶餘額(FEPTXN_BALB) */
							getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						}
						case 13: { /*TROUT_BKNO for 2531,2568,2569繳稅交易*/
							//20220927以主機回應回傳
							getFiscRes().setTroutBkno(getFiscBusiness().getFeptxn().getFeptxnTroutBkno7());
							break;
						}
						case 14: { /* 跨行手續費 */
							getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
							break;
						}
						case 16: { /* 狀況代號 */
							getFiscRes().setRsCode(getFiscBusiness().getFeptxn().getFeptxnRsCode());
							break;
						}
						case 21: {
							//20220927 改用CBS_TOTA.LUCKYNO
							getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnLuckyno());
							break;
						}
						case 37: {
							getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						}
					}
				}
			}
			RefString refMac = new RefString(getFiscRes().getMAC());
			_rtnCode4 = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
			getFiscRes().setMAC(refMac.get());
			if (_rtnCode4 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
				getFiscRes().setMAC("00000000");
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "prepareBody");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 檢核更新原始交易狀態
	 *
	 * @return
	 *
	 */
	private FEPReturnCode checkoriFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String I_TX_DATE = "";
		// QueryFEPTXNByStan:
		try {
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
			// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 本營業日檔
			getFiscBusiness().setOriginalFEPTxn(oriDBFEPTXN.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				if (FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
					I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8);
				}
				rtnCode = searchOriginalFEPTxn(I_TX_DATE, getFiscBusiness().getFeptxn().getFeptxnBkno(), getFiscBusiness().getFeptxn().getFeptxnOriStan());
				if (rtnCode != CommonReturnCode.Normal) {
					rtnCode = FISCReturnCode.TransactionNotFound; // 無此交易 spec change 20100720
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					getLogContext().setRemark("SearchFeptxn 無此交易");
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
					return rtnCode;
				}
			}

			/// *檢核原交易是否成功*/
			if (!"A".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()) && !"B".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// /*交易成功*/
				rtnCode = FISCReturnCode.TransactionNotFound; // 無此交易 'spec change 20100720
				getFiscBusiness().getFeptxn().setFeptxnTxrust("I"); // 原交易已拒絕
				return rtnCode;
			}

			/*檢核原交易之 MAPPING 欄位是否相同*/
			if (getFiscBusiness().getFeptxn().getFeptxnTxAmt().doubleValue() != getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt().doubleValue()
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnIcSeqno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmChk().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmType().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnMerchantId().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnIcmark().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcmark().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnIcTac().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcTac().trim())){
				rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
				return rtnCode;
			}
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("T"); // 沖銷或授權完成進行中
			oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());

			//檢核單筆限額
			if(getFiscBusiness().getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
					&& !"0".equals(getTxData().getMsgCtl().getMsgctlCheckLimit().toString())){
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if(rtnCode != CommonReturnCode.Normal){
					return rtnCode;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "checkoriFEPTXN");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * 以日期搜尋 FEPTXN
	 *
	 * @return
	 *
	 */
	private FEPReturnCode searchOriginalFEPTxn(String txDate, String bkno, String stan) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
		Bsdays aBSDAYS = new Bsdays();
		BsdaysMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
		String wk_TBSDY = null;
		String wk_NBSDY = "";
		// Dim i As Int32
		try {
			db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
			getFiscBusiness().setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				aBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
				aBSDAYS.setBsdaysDate(txDate);
				aBSDAYS = dbBSDAYS.selectByPrimaryKey(aBSDAYS.getBsdaysZoneCode(), aBSDAYS.getBsdaysDate());
				if (aBSDAYS == null) {
					return IOReturnCode.BSDAYSNotFound;
				}
				// ASK CONNIE
				if (DbHelper.toBoolean(aBSDAYS.getBsdaysWorkday())) {// 工作日
					wk_TBSDY = aBSDAYS.getBsdaysDate();
					wk_NBSDY = aBSDAYS.getBsdaysNbsdy();
				} else {
					wk_TBSDY = aBSDAYS.getBsdaysNbsdy();
				}
				if (wk_TBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
					db.setTableNameSuffix(wk_TBSDY.substring(6, 6 + 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
					getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
					getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
					getFiscBusiness()
							.setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
					if (getFiscBusiness().getOriginalFEPTxn() == null) {
						if (StringUtils.isNotBlank(wk_NBSDY) && wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
							db.setTableNameSuffix(wk_NBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
							getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
							getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
							getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
							getFiscBusiness().setOriginalFEPTxn(
									db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
							if (getFiscBusiness().getOriginalFEPTxn() == null) {
								rtnCode = IOReturnCode.FEPTXNNotFound;
							}
						} else {
							rtnCode = IOReturnCode.FEPTXNNotFound;
						}
					}
				} else {
					rtnCode = IOReturnCode.FEPTXNNotFound;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "searchOriginalFEPTxn");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
