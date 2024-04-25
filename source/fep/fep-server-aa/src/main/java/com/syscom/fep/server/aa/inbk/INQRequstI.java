package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.vo.constant.*;
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
//import com.syscom.fep.common.util.PolyfillUtil;
//import com.syscom.fep.configuration.CMNConfig;
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
//import com.syscom.fep.mybatis.mapper.HotacqcntryMapper;
import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
import com.syscom.fep.mybatis.mapper.QrptxnMapper;
import com.syscom.fep.mybatis.mapper.SysconfMapper;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Feptxn;
//import com.syscom.fep.mybatis.model.Hotacqcntry;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Nwdtxn;
import com.syscom.fep.mybatis.model.Qrptxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
//import com.syscom.fep.server.common.adapter.FISCAdapter;
//import com.syscom.fep.server.common.business.host.App;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.Dapp;
import com.syscom.fep.server.common.business.host.Ncnb;
import com.syscom.fep.server.common.business.host.T24;
//import com.syscom.fep.vo.constant.FISCType;
import com.syscom.fep.vo.enums.ATMNCCardStatus;
import com.syscom.fep.vo.enums.ATMReturnCode;
//import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.enums.DAPPAppMsg;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動餘額查詢交易電文
 * @author Richard --> Ben
 */
public class INQRequstI extends INBKAABase {
	private Object tota = null;

	private FEPReturnCode rc1 = CommonReturnCode.Normal;
	private FEPReturnCode rc2 = CommonReturnCode.Normal;
	private FEPReturnCode rc3 = CommonReturnCode.Normal;
	private FEPReturnCode rc4 = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
	private boolean isExitProgram = false;
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
	public INQRequstI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 */
	@Override
	public String processRequestData() {
		try {
			
			//1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
			//檢核財金電文 Header
			rc1 = processRequestHeader();
			//程式結束
			if(isExitProgram) {
				return StringUtils.EMPTY;
			}

			//2.AddTxData:新增交易記錄(FEPTXN & INTLTXN&NWDTXN)
			//2.1 Prepare() 交易記錄初始資料
			rc2 = this.prePare();	
			//程式結束
			if(rc2 != CommonReturnCode.Normal) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc2, getFiscReq());
				getLogContext().setMessage(rc2.toString());
				getLogContext().setRemark("準備FEPTXN物件發生錯誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	
			}
			//2.2 新增交易記錄
			rc3 = this.insertFeptxn();
			//程式結束
			if(rc3 != CommonReturnCode.Normal) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc3, getFiscReq());
				getLogContext().setMessage(rc3.toString());
				getLogContext().setRemark("新增交易記錄有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	
			}
			
			//2.3 判斷 CheckHeader 之 RC, 若有誤則不繼續執行
			if (CommonReturnCode.Normal.equals(rc1) && CommonReturnCode.Normal.equals(rc2)) {
				//3.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核 
				strFISCRc = this.checkBusinessRule();
				if (CommonReturnCode.Normal.equals(strFISCRc)) {
					//4.SendToCBS: 帳務主機處理
				    /*若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗
						則仍需組回應電文給財金 */
					rc1 = this.sendToCBS();
					//程式結束
					if(isExitProgram) {
						getLogContext().setMessage("sendToCBS error");
						logMessage(getLogContext());
						return StringUtils.EMPTY;
					}
				}
			}
			//5.label_END_OF_FUNC:
			/*以下各步驟若 RC4 有誤皆繼續執行 */
			//6.PrepareFISC:準備回財金的相關資料
			//6.1 判斷 RC1,RC2,RC3
			if (rc1 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc1.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc1, FEPChannel.FISC, getLogContext()));
				}
			}else if (rc2 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc2.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc2, FEPChannel.FISC, getLogContext()));
				}
			}else if (rc3 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc3.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc3, FEPChannel.FISC, getLogContext()));
				}
			}else {
				getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);		 /*+REP*/
			}
			//6.2 產生 Response 電文Header:
			rc4 = getFiscBusiness().prepareHeader("0210");
			if (rc4 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc4.getValue());
			}
			
			//6.3 產生 Response 電文Body:
			//6.4 產生 MAC
			this.prepareBody();
			
			//6.5 產生Bit Map 
			rc4 = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
			this.logContext.setMessage("after makeBitmap RC:" + rc4.toString());
			logMessage(this.logContext);
			if (rc4 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc4.getValue());
				getFiscRes().setBitMapConfiguration("0000000000000000");
			}

			rc4 =getFiscRes().makeFISCMsg();
			//7.UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
			this.updateTxData();
			//程式結束
			if(isExitProgram) {
				getLogContext().setMessage("updateTxData error");
				logMessage(getLogContext());
				return StringUtils.EMPTY;
			}
			//8.SendToFISC送回覆電文到財金
			//getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			FISCAdapter fiscAdapter = new FISCAdapter(getTxData());
			fiscAdapter.setMessageToFISC(getFiscRes().getFISCMessage());
			fiscAdapter.sendReceive();

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


	/**
	 * 拆解並檢核由財金發動的Request電文
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		//檢核財金電文 Header
		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
		if(rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
				|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
			getLogContext().setMessage(rtnCode.toString());
			getLogContext().setRemark("檢核由財金發動的Request電文有誤!!");
			logMessage(Level.INFO, getLogContext());
			isExitProgram = true;
			return rtnCode;
		}
		
		/*
		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
		if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
				|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
			return rtnCode;
		}

		// 2014/09/12 Modify by Ruling for 24XX類的國際卡交易，避免OPC(3106)做2000-ExceptionCheckOut時，回的rtnCode=ReceiverBankServiceStop會被蓋掉的問題
		if (StringUtils.isNotBlank(getFiscReq().getOriData()) && rtnCode == CommonReturnCode.Normal) {
			rtnCode = getFiscBusiness().checkORI_DATA(MessageFlow.Request);
			isPlusCirrus = true;
		}

		// 2018/02/12 Modify by Ruling for 跨行無卡提款
		if (FISCPCode.PCode2510.getValueStr().equals(getFiscReq().getProcessingCode()) && "6071".equals(getFiscReq().getAtmType())) {
			isNWD = true;
			getLogContext().setRemark("無卡提款");
			logMessage(getLogContext());
		}

		// 2019/07/23 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：以端末設備型態判斷主被掃交易
		// 被掃交易
		if ((FISCPCode.PCode2541.getValueStr().equals(getFiscReq().getProcessingCode()) || FISCPCode.PCode2542.getValueStr().equals(getFiscReq().getProcessingCode()))
				&& "6".equals(getFiscReq().getAtmType().substring(0, 1)) && ("B".equals(getFiscReq().getAtmType().substring(3, 4)) || "C".equals(getFiscReq().getAtmType().substring(3, 4)))) {
			isQRPScaned = true;
			getLogContext().setRemark("被掃交易");
			logMessage(getLogContext());
		}
		// 2019/12/26 Modify by Ruling for QRP新增需求：配合消費扣款退貨交易(PCODE2543)，財金增加Option欄位(端末設備型態)，因端末設備型態為Option欄位，在substring前要先判斷是否有值，故與2541/2542被掃拆開來寫
		if (FISCPCode.PCode2543.getValueStr().equals(getFiscReq().getProcessingCode()) && StringUtils.isNotBlank(getFiscReq().getAtmType())
				&& "6".equals(getFiscReq().getAtmType().substring(0, 1)) && ("B".equals(getFiscReq().getAtmType().substring(3, 4)) || "C".equals(getFiscReq().getAtmType().substring(3, 4)))) {
			isQRPScaned = true;
			getLogContext().setRemark("被掃交易");
			logMessage(getLogContext());
		}

		// 2019/12/26 Modify by Ruling for QRP新增需求：配合消費扣款退貨交易(PCODE2543)，財金增加Option欄位(端末設備型態)
		// 2019/12/26 Modify by Ruling for 豐錢包新增Paytax繳稅功能：增加QRP繳稅-晶片卡跨行繳稅交易(PCODE2568)
		// 主掃交易
		if ((FISCPCode.PCode2541.getValueStr().equals(getFiscReq().getProcessingCode()) || FISCPCode.PCode2525.getValueStr().equals(getFiscReq().getProcessingCode())
				|| FISCPCode.PCode2564.getValueStr().equals(getFiscReq().getProcessingCode()) || FISCPCode.PCode2543.getValueStr().equals(getFiscReq().getProcessingCode())
				|| FISCPCode.PCode2568.getValueStr().equals(getFiscReq().getProcessingCode())) && FISCType.Type659A.equals(getFiscReq().getAtmType())) {
			isQRPMain = true;
			getLogContext().setRemark("主掃交易");
			logMessage(getLogContext());
		}

		if (rtnCode != CommonReturnCode.Normal) {
			strFISCRc = rtnCode;
			return CommonReturnCode.Normal;
		}
		*/
		return rtnCode;
	}

	/**
	 * 新增交易記錄
	 * (1) 	Prepare() 交易記錄初始資料
	 * 
	 */
	private FEPReturnCode prePare() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		rtnCode = getFiscBusiness().prepareFEPTXN();
		return rtnCode;
	}

	/**
	 * (2)新增交易記錄
	 * 
	 */
	private FEPReturnCode insertFeptxn() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			rtnCode = getFiscBusiness().insertFEPTxn();
			if (rtnCode != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				return rtnCode;
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
	 * 新增交易記錄
	 * backup
	 * @return
	 * 
	 */
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			
			//Prepare() 交易記錄初始資料
			rtnCode = getFiscBusiness().prepareFEPTXN();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			
			//新增交易記錄
			rtnCode = getFiscBusiness().insertFEPTxn();
			if (rtnCode != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				return rtnCode;
			}
			
			//判斷 CheckHeader 之 RC, 若有誤則不繼續執行(檢核財金電文 Header)
			rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
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
	
	private FEPReturnCode checkNCBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			// 檢核單筆限額
			rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			// SYNC_PPKEY(Bitmap 39) 必須有值
			if (StringUtils.isBlank(getFiscReq().getSyncPpkey())) {
				getLogContext().setRemark("CheckBusinessRule-SYNC_PPKEY(Bitmap39)必需有值,目前值為Null或空白");
				logMessage(getLogContext());
				rtnCode = ATMReturnCode.OtherCheckError;
				return rtnCode;
			}

			// PINBLOCK(Bitmap 5) 必須有值
			if (StringUtils.isBlank(getFiscReq().getPINBLOCK())) {
				getLogContext().setRemark("CheckBusinessRule-PINBLOCK(Bitmap5)必需有值,目前值為Null或空白");
				logMessage(getLogContext());
				rtnCode = ATMReturnCode.OtherCheckError;
				return rtnCode;
			}
			RefBase<Nwdtxn> nwdtxnRefBase = new RefBase<>(defNWDTXN);
			// 檢核無卡提款預約
			rtnCode = getFiscBusiness().checkNCNWDData(nwdtxnRefBase);
			defNWDTXN = nwdtxnRefBase.get();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			getLogContext().setRemark("檢杳密碼前");
			logMessage(getLogContext());
			// 檢核密碼
			rtnCode = encHelper.checkNcPassword(getFiscReq().getPINBLOCK(), getFiscBusiness().getCard().getCardNckeykind(), getFiscBusiness().getCard().getCardAuth());
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = getFiscBusiness().checkNCPWErrCnt("2"); // 密碼正確
			} else {
				if (rtnCode == ENCReturnCode.ENCCheckPasswordError) {// 密碼錯誤
					rtnCode = getFiscBusiness().checkNCPWErrCnt("1");
					if (rtnCode == ATMReturnCode.OverPasswordErrorCount) {
						rsCARD = "Y"; // 密碼錯誤達到5次
					}
				}
				return rtnCode;
			}

			// 檢核預約金額
			if (getFiscBusiness().getFeptxn().getFeptxnTxAmt().compareTo(defNWDTXN.getNwdtxnRegAmt()) != 0) {
				getLogContext().setRemark("提款金額與預約金額不符");
				logMessage(getLogContext());
				rtnCode = FEPReturnCode.NWDSeqError;
				return rtnCode;
			}

			// 檢核MAC
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			getFiscBusiness().getFeptxn().setFeptxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmt());

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "checkNCBusinessRule");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 商業邏輯檢核 & 電文Body檢核
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
//		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		try {
			//暫時註解
//			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(),getFiscReq().getMAC());
//			this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
//			logMessage(this.logContext);
//			if (rtnCode != CommonReturnCode.Normal) {
//				return rtnCode;
//			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "checkBusinessRule");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;

	}

	/**
	 * 4.SendToCBS: 帳務主機處理
	 * 
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String TxType = "0";		//查詢
		try {
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
			rtnCode = new CBS(hostAA, getTxData()).sendToCBS(TxType);
			tota = hostAA.getTota();
			if (rtnCode != CommonReturnCode.Normal) {
				if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
					return rtnCode;
				} else {
					getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
					getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
					getFiscBusiness().updateTxData();
					// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 成功
						oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
						// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
					}
					isExitProgram = true;
					return rtnCode;
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
	
	/**
	 * 4.SendToCBS: 帳務主機處理
	 * 
	 */
	private FEPReturnCode sendToCBS_old() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		T24 hostT24 = new T24(getTxData());
		boolean ProcessTag = false;
		byte TxType = 0;		//查詢
		try {
			if (getTxData().getMsgCtl().getMsgctlCbsFlag() != 0) {
				TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().byteValue();
				ProcessTag = true;
			} else {
				TxType = 1;
				ProcessTag = false;
			}
			rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag);
			if (rtnCode != CommonReturnCode.Normal) {
				if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
					//strFISCRc = rtnCode;
					return CommonReturnCode.Normal;
				} else {
					getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
					getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
					getFiscBusiness().updateTxData();
					// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 成功
						oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
						// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
					}
					isExitProgram = true;
					return rtnCode;
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
	private FEPReturnCode sendToCBSAndAsc() {
		// modify 2010/12/01
		Credit hostCredit = new Credit(getTxData());
		T24 hostT24 = new T24(getTxData());
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// 2013/06/24 Modify by Ruling for 港澳NCB:增加W_DSCPT

		try {
			// 若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗則仍需組回應電文給財金
			if ("G".equals(getFiscBusiness().getFeptxn().getFeptxnTrinKind())) {// Gift 卡
				// 上信用卡主機檢核 Gift卡額度
				// 3/20 修改 for 轉入 GIFT 卡, 取得 GIFT 卡開戶行 for APTOT
				getFiscBusiness().getFeptxn().setFeptxnTrinBrno(INBKConfig.getInstance().getGIFCardBRNO());
				getLogContext().setRemark("上信用卡主機檢核 Gift 卡額度SendToCredit");
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());
				rtnCode = hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(), (byte) 1);
				if (rtnCode != CommonReturnCode.Normal) {
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnAscRc())) {
						strFISCRc = rtnCode;
						return CommonReturnCode.Normal;
					} else {
						getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
						getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
						getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
						getFiscBusiness().updateTxData();
						// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
						return rtnCode;
					}
				}
			}
			if (!"G".equals(getFiscBusiness().getFeptxn().getFeptxnTrinKind()) || FISCPCode.PCode2523.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {/// *for自轉-轉入Gift卡*/
				if (getFiscBusiness().getOriginalFEPTxn() != null) {
					oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "sendToCBSAndAsc"));
				}
				// 一般交易上 CBS 主機
				switch (getFiscBusiness().getFeptxn().getFeptxnZoneCode()) {
					case ZoneCode.TWN: {
						// for 跨行轉帳之CBS主機代號有二個, 第一個為轉出扣帳, 第二個為轉入帳號查詢
						getLogContext().setRemark("for 跨行轉帳之CBS主機代號有二個, 第一個為轉出扣帳, 第二個為轉入帳號查詢SendToCBS");
						getLogContext().setProgramName(ProgramName);
						logMessage(Level.DEBUG, getLogContext());
						if (!getFiscBusiness().getFeptxn().getFeptxnPcode().substring(3, 4).equals("4")
								|| SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {/// *轉出交易*/
							// spec change 20101020
							boolean ProcessTag = false;
							// spec change 20101025
							byte TxType = 0;
							if (getTxData().getMsgCtl().getMsgctlCbsFlag() != 0) {
								TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().byteValue();
								ProcessTag = true;
							} else {
								TxType = 1;
								ProcessTag = false;
							}
							getLogContext().setRemark("SendToCBS");
							getLogContext().setProgramName(ProgramName);
							logMessage(Level.DEBUG, getLogContext());
							// 2015/06/09 Modify by Ruling for 行動金融信用卡eBILL繳費手續費優惠
							if (FISCPCode.PCode2563.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
									|| FISCPCode.PCode2564.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
								if ("3".equals(StringUtils.leftPad(getFiscBusiness().getFeptxn().getFeptxnIcmark(), 30, '0').substring(28, 29))
										&& "948".equals(getFiscBusiness().getFeptxn().getFeptxnBkno())
										&& (Integer.parseInt(getFiscBusiness().getFeptxn().getFeptxnPaytype()) >= Integer.parseInt("50000")
												&& Integer.parseInt(getFiscBusiness().getFeptxn().getFeptxnPaytype()) <= Integer.parseInt("99999"))) {
									if (FISCPCode.PCode2563.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
										rtnCode = hostT24.sendToT24(T24Version.A1271, TxType, ProcessTag);
									} else if (FISCPCode.PCode2564.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
										rtnCode = hostT24.sendToT24(T24Version.A1211, TxType, ProcessTag);
									}
									if (rtnCode == CommonReturnCode.Normal && getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().intValue() == 0) {
										// 借用FEPTXN_NOTICE_ID欄位，寫入手續費減免
										getFiscBusiness().getFeptxn().setFeptxnNoticeId("1");
									}
								} else {
									rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag);
								}
							} else {
								// 2018/02/12 Modify by Ruling for 跨行無卡提款
								if (isNWD) {
									// 跨行無卡提款送T24 A1011 電文
									rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(), TxType, ProcessTag);
									if (rtnCode == CommonReturnCode.Normal) {
										// 2020/02/24 Modify by Ruling for 企業戶無卡提款：企業戶無卡提款帳戶餘額清成0
										if (getFiscBusiness().getCard().getCardNcstatusB() == ATMNCCardStatus.Apply.getValue()) {
											getFiscBusiness().getFeptxn().setFeptxnBala(BigDecimal.valueOf(0));
											getFiscBusiness().getFeptxn().setFeptxnBalb(BigDecimal.valueOf(0));
										}
									}
								} else {
									rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag);
								}
							}
							// rtnCode = hostT24.SendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag) 'spec change 20101025
						} else {// 跨行轉帳-轉入交易
							getLogContext().setRemark("SendToCBS2");
							getLogContext().setProgramName(ProgramName);
							logMessage(Level.DEBUG, getLogContext());
							rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(), 1, false); // spec change 20101025
						}
						if (rtnCode != CommonReturnCode.Normal) {
							// spec change 20101020
							if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
								strFISCRc = rtnCode;
								return CommonReturnCode.Normal;
							} else {
								getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
								getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
								getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
								getFiscBusiness().updateTxData();
								// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
								if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
									getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 成功
									oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
									// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
								}
								return rtnCode;
							}
						}

						// 授權完成交易需上主機二次, 第一次解圏, 第二次扣帳
						if (FISCPCode.PCode2552.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 授權完成
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("C"); // 已解圏
							getLogContext().setRemark("授權完成交易需上主機二次, 第一次解圏, 第二次扣帳SendToCBS");
							getLogContext().setProgramName(ProgramName);
							logMessage(Level.DEBUG, getLogContext());
							rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(), 1, true);
							if (rtnCode != CommonReturnCode.Normal) {
								// spec change 20101020
								if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
									strFISCRc = rtnCode;
									return CommonReturnCode.Normal;
								} else {
									getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
									getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
									getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // *AA Close
									getFiscBusiness().updateTxData();;
									// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
									return rtnCode;
								}
							}
						}
						break;
					}
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

	/**
	 * 6.PrepareFISC:準備回財金的相關資料
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode prepareForFISC() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (strFISCRc != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				// modified by Maxine for 2011/07/14 for 傳入LogContext
				// getFiscBusiness().getFeptxn().FEPTXN_REP_RC = TxHelper.GetRCFromErrorCode(strFISCRc, FEPChannel.FISC)
				getLogContext().setProgramName(ProgramName);
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
		}

		// 2013/05/27 Modify by Ruling for 2568/2569交易，將回給財金錯誤代碼4508改為2999
		// 2014/03/18 Modify by Ruling for 2568/2569交易，依財金賴先生建議，將回給財金錯誤代碼由4508改為4501
		if ((FISCPCode.PCode2568.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
				|| FISCPCode.PCode2569.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) && "4508".equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			getFiscBusiness().getFeptxn().setFeptxnRepRc("4501");
			// getFiscBusiness().getFeptxn().FEPTXN_REP_RC = "2999"
		}

		// 2015/04/27 Modify by Ruling for 原存轉入交易回給財金錯誤代碼有誤
		// 2018/01/08 Modify by Ruling for 修改ATM跨行存款功能：跨行存款超過限額交易轉為4206(入帳交易金額，超過累計限額)
		if ((SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())
				&& !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()))
				&& (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())
						&& !NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
						&& "4".equals(getFiscBusiness().getFeptxn().getFeptxnRepRc().substring(0, 1)))) {
			FISCPCode pcode = FISCPCode.parse(getFiscBusiness().getFeptxn().getFeptxnPcode());
			switch (pcode) {
				case PCode2521:
					if ("9998".equals(getFiscBusiness().getFeptxn().getFeptxnNoticeId())
							&& "E12516".equals(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
						getFiscBusiness().getFeptxn().setFeptxnRepRc("4206"); // 入帳交易金額，超過累計限額
					} else {
						getFiscBusiness().getFeptxn().setFeptxnRepRc("4507"); // 轉入帳號錯誤
					}
					break;
				case PCode2524:
					if ("4201,4507".indexOf(getFiscBusiness().getFeptxn().getFeptxnRepRc()) < 0) {
						getFiscBusiness().getFeptxn().setFeptxnRepRc("4507"); // 轉入帳號錯誤
					}
					break;
				case PCode2561:
				case PCode2564:
					if ("4201,4204,4507,4703,4802,4803,4804,4809".indexOf(getFiscBusiness().getFeptxn().getFeptxnRepRc()) < 0) {
						getFiscBusiness().getFeptxn().setFeptxnRepRc("4507"); // 轉入帳號錯誤
					}
					break;
				case PCode2261:
				case PCode2264:
					if ("4201,4204,4507,4703,4802,4803,4804,4806,4809".indexOf(getFiscBusiness().getFeptxn().getFeptxnRepRc()) < 0) {
						getFiscBusiness().getFeptxn().setFeptxnRepRc("4507"); // 轉入帳號錯誤
					}
					break;
				default:
					break;
			}
		}
		rtnCode = getFiscBusiness().prepareHeader("0210");
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		}
		rtnCode = prepareBody();
		rtnCode = getFiscBusiness().makeBitmap(getFiscRes().getMessageType(), getFiscRes().getProcessingCode(), MessageFlow.Response);
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			getFiscRes().setBitMapConfiguration("0000000000000000");
		}
		rtnCode = getFiscRes().makeFISCMsg();
		return rtnCode;
	}

	/**
	 * UpdateTxData: 更新交易記錄(FEPTXN & INTLTXN)
	 * @return
	 */
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
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
				}

			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {	/*初值:0*/
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); 					/*拒絕-正常*/
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)1);	/*AA Close*/
			
			rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
			if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
				transactionManager.rollback(txStatus);
				isExitProgram = true;
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
	
	private FEPReturnCode updateTxData_BK() {
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
				// spec change 20101124
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

			// 2018/02/12 Modify by Ruling for 跨行無卡提款:更新預約檔
			if (isNWD) {
				getFiscBusiness().updateNWDReg();
			}

			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			// connie spec change modify by husan
			rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
			if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文, 人工處理
				transactionManager.rollback(txStatus);
				return rtnCode;
			}
			// (2) 判斷是否需更新 INTLTXN
			if (isPlusCirrus) {
				// spec change 20101109
				defINTLTXN.setIntltxnTxCurAct(getFiscBusiness().getFeptxn().getFeptxnTxCurAct());
				defINTLTXN.setIntltxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
				defINTLTXN.setIntltxnExrate(getFiscBusiness().getFeptxn().getFeptxnExrate());
				defINTLTXN.setIntltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
				defINTLTXN.setIntltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
				defINTLTXN.setIntltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defINTLTXN.setIntltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				// modify 2010/12/09
				defINTLTXN.setIntltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno()); // for Combo卡
				if (dbINTLTXN.updateByPrimaryKeySelective(defINTLTXN) < 1) {// 若更新失敗則不送回應電文, 人工處理
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}
			// 2018/02/12 Modify by Ruling for 跨行無卡提款
			// (3) 判斷是否需更新 NWDTXN
			if (isNWD) {
				defNWDTXN.setNwdtxnTxCurAct(getFiscBusiness().getFeptxn().getFeptxnTxCurAct());
				defNWDTXN.setNwdtxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
				defNWDTXN.setNwdtxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defNWDTXN.setNwdtxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				defNWDTXN.setNwdtxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno()); // for Combo卡
				if (dbNWDTXN.updateByPrimaryKeySelective(defNWDTXN) < 1) {
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}
			// 2019/07/23 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：2541被掃交易更新QRPTXN
			// (4) 判斷是否需更新QRPTXN
			if (isQRPScaned) {
				if (FISCPCode.PCode2541.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
					defQRPTXN.setQrptxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
					defQRPTXN.setQrptxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
					defQRPTXN.setQrptxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
					defQRPTXN.setQrptxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
					defQRPTXN.setQrptxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
					defQRPTXN.setQrptxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
					if (dbQRPTXN.updateByPrimaryKeySelective(defQRPTXN) < 1) {
						transactionManager.rollback(txStatus);
						return IOReturnCode.UpdateFail;
					}
				}
			}
			// (5) 判斷是否需更新原始交易 for 2430, 2470, 2542, 2552
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				if (getFiscBusiness().getOriginalFEPTxn() != null) {
					oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData"));
					if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTraceEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
						if (isPlusCirrus) {// 國際卡交易需同時更新INTLTXN
							oriINTLTXN.setIntltxnTxrust("D");
							if (dbINTLTXN.updateByPrimaryKeySelective(oriINTLTXN) < 1) {// 若更新失敗則不送回應電文, 人工處理
								transactionManager.rollback(txStatus);
								return IOReturnCode.UpdateFail;
							}
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("D");
							getFiscBusiness().getOriginalFEPTxn().setFeptxnNeedSendCbs((short) 2); // 應沖正
							getFiscBusiness().getOriginalFEPTxn().setFeptxnAccType((short) 2);
							getFiscBusiness().getOriginalFEPTxn().setFeptxnClrType((short) 2);
						}

						if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
							// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
							// 若更新失敗則不送回應電文, 人工處理
							transactionManager.rollback(txStatus);
							return IOReturnCode.FEPTXNUpdateError;
						}
					} else {// -REP
							// 授權交易需先上主機解圏, 若解圏成功則 TXRUST =“C”
							// 所以若TXRUST =“T”進行中, 即可將原交易之狀態改回 Active
						if ("T".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// 進行中for沖銷
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 將原始交易之狀態改為Active
							if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
								// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2])
								// 若更新失敗則不送回應電文, 人工處理
								transactionManager.rollback(txStatus);
								return IOReturnCode.FEPTXNUpdateError;
							}
						}
					}
				}
			}
			transactionManager.commit(txStatus);
			if ((FISCPCode.PCode2541.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
					|| FISCPCode.PCode2525.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()))
					&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (getFiscBusiness().getCard() != null && getFiscBusiness().getCard().getCardIcpu() == 0) {
					try {
						getFiscBusiness().getCard().setCardIcpu((short) 1);
						getFiscBusiness().prepareCARDTXN(getFiscBusiness().getCard());
						RefBase<FEPReturnCode> codeRefBase = new RefBase<>(_rtnCode);
						getFiscBusiness().updateCard(getFiscBusiness().getCard(), codeRefBase);
						_rtnCode = codeRefBase.get();
						getLogContext().setRemark("消費扣款功能自動啟用 Card.CARD_ICPU 變更為1");
						logMessage(getLogContext());
					} catch (Exception ex) {
						getLogContext().setProgramException(ex);
						getLogContext().setProgramName(ProgramName + "updateTxData");
						sendEMS(getLogContext());
					}
				}
			}

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
			rtnCode = getFiscBusiness().processAptot(isEC);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
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
	 * 解圈
	 * 
	 * @return
	 * 
	 *         <modify>
	 *         <modifier>Ruling</modifier>
	 *         <date>2015/10/16</date>
	 *         <reason>為了避免Req電文還沒結束Con電文就進來，調整主流程順序先更新ProcessAPTOT再送財金:將原本在ProcessAPTOT的解圈程式移至這裡</reason>
	 *         </modify>
	 */
	private FEPReturnCode processLock() {
		T24 hostT24 = new T24(getTxData());
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (FISCPCode.PCode2551.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 預約授權
			getLogContext().setRemark("Waiting 20 minutes");
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			// Waiting 20 minutes
			try {
				Thread.sleep(1200000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return rtnCode;
			}

			// add by Maxine on 2011/08/15 for 需要重新讀取該筆 FEPTXN,判斷是否需要解圈
			Feptxn feptxn = null;
			try {
				feptxn = this.feptxnDao.selectByPrimaryKey(getFiscBusiness().getFeptxn().getFeptxnTxDate(), getFiscBusiness().getFeptxn().getFeptxnEjfno());
			} catch (Exception e) {
				e.printStackTrace();
				return rtnCode;
			}
			if (feptxn == null) {
				return IOReturnCode.FEPTXNReadError;
			}

			if ("A".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("T");
				getFiscBusiness().updateTxData();
				// modify 20110225
				// rtnCode = hostT24.SendToT24(getTxData().getMsgCtl().MSGCTL_TWCBSTXID, 2, True) '解圏
				try {
					rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), 4, true); // 解圏
				} catch (Exception e) {
					e.printStackTrace();
					return rtnCode;
				}
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
				getFiscBusiness().getFeptxn().setFeptxnTxrust("C");
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}

	/**
	 * 組財金電文Body部份
	 * 6.3 產生 Response 電文Body:
	 * 6.4 產生 MAC
	 * 
	 */
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_BITMAP = null;
		try {
			// 依據wk_BITMAP(判斷是否搬值)
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				// 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			}else {		/*-REP*/
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}
			// 依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				// Loop IDX from 3 to 64
				if (wk_BITMAP.charAt(i) == '1') {
					/* 參考 "財金電文整理_Response.xls" 搬值 (參考FEPTXN MAPPING 欄位)
				    欄位特別說明如下:  */
					switch (i) {
					case 5: {		/* 代付單位 CD/ATM 代號 */
						getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
						break;
							}
					case 6: {		/* 可用餘額 */
						/* 11/19 配合永豐修改, 改送帳戶餘額(FEPTXN_BALB) */
						getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
						break;
							}
					case 14: {		/* 跨行手續費 */
						getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
						break;
							}
					case 37: {		/* 帳戶餘額*/
						getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
						break;
							}
					}
				}
			}
			RefString refMac = new RefString(getFiscRes().getMAC());
			/* 6/27 修改, 產生財金MAC */
			rtnCode = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
			getFiscRes().setMAC(refMac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
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
	private FEPReturnCode prepareBody_BK() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_BITMAP = null;

		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {// +REP
				// 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組
				if ((FISCPCode.PCode2524.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
						|| FISCPCode.PCode2564.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()))
						&& SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())) {
					wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
				} else {
					wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
				}

				// 2018/02/12 Modify by Ruling for 跨行無卡提款:BitMap 22 設為 on (回傳實際扣款帳號)
				if (isNWD && StringUtils.isNotBlank(wk_BITMAP) && wk_BITMAP.length() == 64) {
					wk_BITMAP = wk_BITMAP.substring(0, 21) + "1" + wk_BITMAP.substring(22);
				}

				// 2019/02/11 Modify by Ruling for 跨行轉帳小額交易手續費調降:BitMap 57 設為 on (帳戶補充資訊)
				if ((FISCPCode.PCode2521.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
						|| FISCPCode.PCode2523.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
						|| FISCPCode.PCode2524.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()))
						&& SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTrinBkno())
						&& StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnAcctSup())) {
					wk_BITMAP = wk_BITMAP.substring(0, 56) + "1" + wk_BITMAP.substring(57);
				}
			} else {// -REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}

			// 2016/11/17 Modify by Ruling for 身心障礙跨行提款手續費減免
			if (FISCPCode.PCode2510.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
					&& StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnNoticeId())
					&& "0101".equals(getFiscBusiness().getFeptxn().getFeptxnNoticeId().trim())
					&& StringUtils.isNotBlank(wk_BITMAP) && wk_BITMAP.length() == 64) {
				wk_BITMAP = wk_BITMAP.substring(0, 22) + "1" + wk_BITMAP.substring(23);
			}

			// 依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				// Loop IDX from 3 to 64
				if (wk_BITMAP.charAt(i) == '1') {
					switch (i) {
						case 2: {
							getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmt().toString());
							break;
						}
						case 5: {
							getFiscRes().setATMNO(getFiscBusiness().getFeptxn().getFeptxnAtmno());
							break;
						}
						case 6: {
							// 2012/11/19 Modify by Ruling for 財金可用餘額改送帳戶餘額(FEPTXN_BALB)
							getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							// getFiscRes().BALA = getFiscBusiness().getFeptxn().FEPTXN_BALA.ToString
							break;
						}
						case 7: {
							getFiscRes().setTxAmtPreauth(getFiscBusiness().getFeptxn().getFeptxnTxAmtPreauth().toString());
							break;
						}
						case 13: {/// *TROUT_BKNO for 2531,2568,2569繳稅交易*/
							// connie spec modify 2010/11/29
							// getFiscRes().TROUT_BKNO = "8070003" '/*繳稅交易需回傳轉出銀行及分行別代號*/
							// 2012/11/05 Modify by Ruling for 轉出銀行放807+帳務分行+分行檢查碼
							SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
							Allbank defALLBANK = new Allbank();
							AllbankMapper dbALLBANK = SpringBeanFactoryUtil.getBean(AllbankMapper.class);
							defALLBANK.setAllbankBkno(SysStatus.getPropertyValue().getSysstatHbkno());
							defALLBANK.setAllbankBrno(getFeptxn().getFeptxnBrno());
							Allbank allbank = dbALLBANK.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
							if (allbank != null) {
								getFiscRes().setTroutBkno(SysStatus.getPropertyValue().getSysstatHbkno() + getFeptxn().getFeptxnBrno() + allbank.getAllbankBrnoChkcode());
							} else {
								getLogContext().setRemark(ProgramName + ".PrepareBody-收信行代號-取不得到ALLBANK, ALLBANK_BKNO=" + defALLBANK.getAllbankBkno() + " ALLBANK_BRNO=" + defALLBANK.getAllbankBrno());
								logMessage(getLogContext());
								SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);
								Sysconf defSYSCONF = new Sysconf();
								SysconfMapper dbSYSCONF = SpringBeanFactoryUtil.getBean(SysconfMapper.class);;
								defSYSCONF.setSysconfSubsysno((short) 1);
								defSYSCONF.setSysconfName("BknoChkDight");
								defSYSCONF = dbSYSCONF.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
								if (defSYSCONF != null) {
									getFiscRes().setTroutBkno(defSYSCONF.getSysconfValue());
								} else {
									getFiscRes().setTroutBkno(getFeptxn().getFeptxnTroutBkno() + "0000");
								}
							}
							break;
						}
						case 14: {
							getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
							break;
						}
						case 16: {
							getFiscRes().setRsCode(getFiscBusiness().getFeptxn().getFeptxnRsCode());
							break;
						}
						case 21: {
							// 2018/02/12 Modify by Ruling for 跨行無卡提款:回傳實際扣款帳號
							// 2019/07/23 Modify by Ruling for 豐錢包APP直連財金QRP安控機制：將APP查詢雲端發票手機條碼回給財金BITMAP22(促銷應用訊息)
							if (isNWD) {
								getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
							} else if (isQRPScaned && StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnTelephone())) {
								// 回傳客戶設定的雲端發票手機條碼
								getFiscRes().setPromMsg(getFiscBusiness().getFeptxn().getFeptxnTelephone());
							} else {
								getFiscRes().setPromMsg("");
								Sysconf defSYSCONF = new Sysconf();
								SysconfMapper dbSYSCONF = SpringBeanFactoryUtil.getBean(SysconfMapper.class);
								defSYSCONF.setSysconfSubsysno((short) 3);
								defSYSCONF.setSysconfName("AD");
								defSYSCONF = dbSYSCONF.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
								if (defSYSCONF != null) {
									if (StringUtils.isNotBlank(defSYSCONF.getSysconfValue())) {
										getFiscRes().setPromMsg(defSYSCONF.getSysconfValue());
									}
								}
							}
							break;
						}
						case 22: {
							// 2016/11/17 Modify by Ruling for 身心障礙跨行提款手續費減免
							if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnNoticeId())) {
								getFiscRes().setActType(getFiscBusiness().getFeptxn().getFeptxnNoticeId().trim());
							}
							break;
						}
						case 35: {
							getFiscRes().setOriData(getFiscReq().getOriData());
							break;
						}
						case 37: {
							getFiscRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						}
						case 50: {
							getFiscRes().setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
							break;
						}
						case 56: {
							// 2019/02/11 Modify by Ruling for 跨行轉帳小額交易手續費調降
							getFiscRes().setAcctSup(getFiscBusiness().getFeptxn().getFeptxnAcctSup());
							break;
						}
						case 60: {
							getFiscRes().setNetwkData(getFiscReq().getNetwkData());
							break;
						}
						case 62: {
							getFiscRes().setAuthCode(String.valueOf(getTxData().getEj()));
							break;
						}
					}
				}
			}
			RefString refMac = new RefString(getFiscRes().getMAC());
			rtnCode = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
			getFiscRes().setMAC(refMac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
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
				} else if (FISCPCode.PCode2552.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
					I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimePreauth().substring(0, 8);
				} else {
					I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnDueDate(); // for 2430,2470
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

			/// *檢核原交易之 MAPPING 欄位是否相同*/
			if ((!FISCPCode.PCode2552.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
					&& getFiscBusiness().getFeptxn().getFeptxnTxAmt().doubleValue() != getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt().doubleValue())
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmType().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnIcmark().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcmark().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnMerchantId().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().trim())
					|| !getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim())) {
				rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
				getLogContext().setRemark(StringUtils.join("欄位資料不符, 原 FeptxnTxAmt:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt().doubleValue(), "FeptxnTxAmt",
						getFiscBusiness().getFeptxn().getFeptxnTxAmt().doubleValue(),
						"原 FeptxnAtmno:", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim(), "FeptxnAtmno", getFiscBusiness().getFeptxn().getFeptxnAtmno().trim(),
						"原 FeptxnAtmType:", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().trim(), "FeptxnAtmType", getFiscBusiness().getFeptxn().getFeptxnAtmType().trim(),
						"原 FeptxnIcmark:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcmark().trim(), "FeptxnIcmark", getFiscBusiness().getFeptxn().getFeptxnIcmark().trim(),
						"原 FeptxnMerchantId:", getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().trim(), "FeptxnMerchantId", getFiscBusiness().getFeptxn().getFeptxnMerchantId().trim(),
						"原 FeptxnTroutActno:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim(), "FeptxnTroutActno", getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim()));
				logMessage(Level.INFO, getLogContext());
				getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
				return rtnCode;
			}
			if (FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 消費扣款沖正
				if (!getFiscBusiness().getFeptxn().getFeptxnIcTac().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcTac().trim())
						|| !getFiscBusiness().getFeptxn().getFeptxnIcSeqno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().trim())
						|| !getFiscBusiness().getFeptxn().getFeptxnAtmChk().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().trim())
						|| !getFiscBusiness().getFeptxn().getFeptxnOrderNo().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnOrderNo().trim())
						|| !getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().trim())) {
					rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
					getLogContext().setRemark(StringUtils.join("欄位資料不符, 原 FeptxnIcTac:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcTac().trim(), "FeptxnIcTac",
							getFiscBusiness().getFeptxn().getFeptxnIcTac().trim(),
							"原 FeptxnIcSeqno:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().trim(), "FeptxnIcSeqno", getFiscBusiness().getFeptxn().getFeptxnIcSeqno().trim(),
							"原 FeptxnAtmChk:", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().trim(), "FeptxnAtmChk", getFiscBusiness().getFeptxn().getFeptxnAtmChk().trim(),
							"原 FeptxnOrderNo:", getFiscBusiness().getOriginalFEPTxn().getFeptxnOrderNo().trim(), "FeptxnOrderNo", getFiscBusiness().getFeptxn().getFeptxnOrderNo().trim(),
							"原 FeptxnTxDatetimeFisc:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().trim(), "FeptxnTxDatetimeFisc",
							getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().trim()));
					logMessage(Level.INFO, getLogContext());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					return rtnCode;
				}
			} else if (FISCPCode.PCode2552.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {// 授權完成
				if (!getFiscBusiness().getFeptxn().getFeptxnTxAmtPreauth().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtPreauth())
						|| !getFiscBusiness().getFeptxn().getFeptxnIcSeqnoPreauth().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqnoPreauth().trim())
						|| !getFiscBusiness().getFeptxn().getFeptxnTxDatetimePreauth().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimePreauth().trim())) {
					rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
					getLogContext().setRemark(StringUtils.join("欄位資料不符, 原 FeptxnTxAmtPreauth:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtPreauth(), "FeptxnTxAmtPreauth",
							getFiscBusiness().getFeptxn().getFeptxnTxAmtPreauth(),
							"原 FeptxnIcSeqnoPreauth:", getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqnoPreauth().trim(), "FeptxnIcSeqnoPreauth",
							getFiscBusiness().getFeptxn().getFeptxnIcSeqnoPreauth().trim(),
							"原 FeptxnTxDatetimePreauth:", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimePreauth().trim(), "FeptxnTxDatetimePreauth",
							getFiscBusiness().getFeptxn().getFeptxnTxDatetimePreauth().trim()));
					logMessage(Level.INFO, getLogContext());
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					return rtnCode;
				}
			}
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("T"); // 沖銷或授權完成進行中
			oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
			// modified By Maxine on 2011/10/13 for 財金REQ Time改存入FEPTXN_REQ_DATETIME
			getFiscBusiness().getFeptxn().setFeptxnDueDate(getFiscBusiness().getOriginalFEPTxn().getFeptxnReqDatetime().substring(0, 8));
			// getFiscBusiness().getFeptxn().FEPTXN_DUE_DATE = getFiscBusiness().getOriginalFEPTxn().FEPTXN_TX_DATE
			if (!FISCPCode.PCode2542.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
				getFiscBusiness().getFeptxn().setFeptxnCbsRrn(getFiscBusiness().getOriginalFEPTxn().getFeptxnCbsRrn()); // 以便進行CBS沖正
			}
			// spec change 20101112
			if (!ZoneCode.TWN.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode())) {
				getFiscBusiness().getFeptxn().setFeptxnVirTxseq(getFiscBusiness().getOriginalFEPTxn().getFeptxnVirTxseq()); // 以便進行海外主機沖正
				getFiscBusiness().getFeptxn().setFeptxnFeeCustpayAct(getFiscBusiness().getOriginalFEPTxn().getFeptxnFeeCustpayAct());
				// spec change 20101117
				getFiscBusiness().getFeptxn().setFeptxnTbsdyAct(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyAct());
				getFiscBusiness().getFeptxn().setFeptxnTxnmode(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxnmode());
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
							db.setTableNameSuffix(wk_NBSDY.substring(6, 2), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
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

	/**
	 * (10) 檢核 MAC ， PINBLOCK及壓 MAC
	 * 
	 * <history>
	 * <modify>
	 * <modifer>Husan </modifer>
	 * <time>2010/12/01</time>
	 * <reason>修正上主機部分改由參考HostBusiness</reason>
	 * <modifer>Husan </modifer>
	 * <time>2011/01/30</time>
	 * <reason>connie spec change 檢核 PP key 是否同步改成SYSSTAT_F3DESSYNC</reason>
	 * </modify>
	 * </history>
	 * 
	 * @return
	 * @throws Exception
	 * 
	 */
	private FEPReturnCode checkMACPINBLOCKAndMakeMAC() throws Exception {
		// modify 2010/12/01
		Credit hostCreidt = new Credit(getTxData());
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		// modify 2011/01/30
		// 檢核 PP key 是否同步
		if (!SysStatus.getPropertyValue().getSysstatF3dessync().equals(getFiscReq().getSyncPpkey())) {
			rtnCode = FISCReturnCode.PPKeySyncError; // 0303-客戶亂碼基碼( PP-KEY )不同步
			return rtnCode;
		}

		// 2018/05/22 Modify by Ruling for MASTER DEBIT加悠遊
		// 2020/02/26 Modify by Ruling for Combo卡應該要送信用卡主機驗密，而不是送FN000307驗密造成交易失敗，Credit(C)改為Combo(M)
		// 檢核 MAC & PINBLOCK 及壓 MAC
		if (BINPROD.Combo.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind()) || BINPROD.Debit.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind())) {
			// COMBO卡
			// 2016/06/27 Modify by Ruling for 財金換KEY問題，將MakeMAC移至下面
			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
			// rtnCode = encHelper.CheckFISCMACAndMakeMAC(fiscReq.MAC, fiscRes.MAC)
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// COMBO卡需至信用卡主機驗密
			rtnCode = hostCreidt.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(), (byte) 1); // 需轉換 PINBLOCK & 壓MAC
			// spec change 20101020 add
			if (rtnCode != CommonReturnCode.Normal && StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnAscRc())) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
				getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
				getFiscBusiness().updateTxData();
				// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
				_rtnCode = rtnCode;
				return rtnCode;
			}
		} else {
			// rtnCode = mATMBusiness.CheckPVVCVV '/*國際金融卡PVV/CVV驗證 */ 'todo by henny
			// 2020/10/22 Modify by Ruling for 香港分行臨櫃變更國際提款密碼
			if (ZoneCode.HKG.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode()) && FISCPCode.PCode2410.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
					&& StringUtils.isNotBlank(getFiscBusiness().getCard().getCardAuth())) {
				rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
				rtnCode = encHelper.checkHkAuth(getFiscReq().getPINBLOCK(), getFiscBusiness().getCard().getCardAuth());
			} else {
				RefString mac = new RefString(getFiscRes().getMAC());
				rtnCode = encHelper.checkFiscMacPinAndMakeMac(getFiscReq().getMAC(), mac);
				getFiscRes().setMAC(mac.get());
			}
			// rtnCode = encHelper.CheckFISCMACPINAndMakeMAC(getFiscReq().MAC, fiscRes.MAC)
		}
		// modify by henny 20110317 for spec change
		LogHelperFactory.getTraceLogger().trace("CheckFISCMACPINAndMakeMAC rtnCode=" + rtnCode);

		if (rtnCode == CommonReturnCode.Normal) {
			// 驗MAC及密碼正確
			rtnCode = getFiscBusiness().checkPWErrCnt(2);
		} else {
			LogHelperFactory.getTraceLogger().trace("密碼錯誤 call CheckPWErrCnt(1)");
			if (rtnCode == ENCReturnCode.ENCCheckPasswordError) {// 密碼錯誤
				rtnCode = getFiscBusiness().checkPWErrCnt(1);
				LogHelperFactory.getTraceLogger().trace("密碼錯誤 call CheckPWErrCnt(1) rtnCode=" + rtnCode);
			}
			return rtnCode;
		}
		return rtnCode;
	}
}
