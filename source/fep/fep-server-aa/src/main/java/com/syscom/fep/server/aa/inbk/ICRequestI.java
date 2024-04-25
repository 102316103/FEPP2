package com.syscom.fep.server.aa.inbk;

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
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.IctltxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * 處理財金發動晶片金融卡跨國交易電文
 * @author Richard	-> Ben (SA=Sarah)
 */
public class ICRequestI extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode rc1 = CommonReturnCode.Normal;
	private FEPReturnCode rc2 = CommonReturnCode.Normal;
	private FEPReturnCode rc3 = CommonReturnCode.Normal;
	private FEPReturnCode rc4 = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
	private boolean isIC = false;
	private boolean isExitProgram = false;
	private Ictltxn defICTLTXN = new Ictltxn();
	private Ictltxn oriICTLTXN = new Ictltxn();
	private IctltxnExtMapper dbICTLTXN = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	private boolean isEC = false;
	
	public ICRequestI(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * <summary>
	 * ''' 程式進入點
	 * ''' </summary>
	 * ''' <returns>Response電文</returns>
	 * ''' <remarks></remarks>
	 */
	@Override
	public String processRequestData() {
		try {
			
			//1.拆解並檢核財金電文(CheckHeader內含CheckBitMap)，若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
			rc1 = processRequestHeader();
			//程式結束
			if(isExitProgram) {
				return StringUtils.EMPTY;	
			}
			
			//2.AddTxData:新增交易記錄(FEPTXN& ICTLTXN)
			this.addTxData();
			//程式結束
			if(isExitProgram) {
				getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rc2, getFiscReq());
				getLogContext().setMessage(rc2.toString());
				getLogContext().setRemark("新增交易記錄有誤!!");
				logMessage(Level.INFO, getLogContext());
				return StringUtils.EMPTY;	
			}
			//2.3 判斷 CheckHeader 之 RC, 若有誤則不繼續執行
			if (CommonReturnCode.Normal.equals(rc1) && CommonReturnCode.Normal.equals(rc2) ) {
				//3.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核 
				strFISCRc = checkBusinessRule();
				if(CommonReturnCode.Normal.equals(strFISCRc)) {
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
			//5.label_END_OF_FUNC
			
			//6.PrepareFISC:準備回財金的相關資料
			//6.1 判斷 RC1,RC2,RC3
			if (rc1 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc1.getValue());
				if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
					getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc1, FEPChannel.FISC, getLogContext()));
				}	
			}else if (rc2 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc2.getValue());
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc2, FEPChannel.FISC, getLogContext()));
			}else if (rc3 != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rc3.getValue());
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(rc3, FEPChannel.FISC, getLogContext()));
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

			rc4=getFiscRes().makeFISCMsg();

			//7.UpdateTxData: 更新交易記錄(FEPTXN& ICTLTXN)
			this.updateTxData();
			//程式結束
			if(isExitProgram) {
				getLogContext().setMessage("updateTxData error");
				logMessage(getLogContext());
				return StringUtils.EMPTY;	
			}
			
			//8.ProcessAPTOT:更新跨行代收付
			this.processAPTOT();
			
			//9.SendToFISC送回覆電文到財金
			rc4 = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);

			//10.判斷是否需傳送2160電文給財金
			if(StringUtils.isNotBlank(feptxn.getFeptxnSend2160())
					&& "A".equals(feptxn.getFeptxnSend2160())) {
				/* 寫入2160發送資料檔 */
				_rtnCode=insertINBK2160();
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage("FiscResponse:"+getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	/**
	 * 拆解並檢核由財金發動的Request電文
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		//1.1 檢核財金電文 Header
		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

		if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError
				|| rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
			getFiscBusiness().setFeptxn(null);
			isExitProgram = true;
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
			return rtnCode;
		}
		
		//1.2 判斷是否為晶片金融卡跨國交易
		if (StringUtils.isNotBlank(getFiscReq().getIcData())) {
			isIC = true;
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
		//FEPReturnCode rtnCode = CommonReturnCode.Normal;
		rc2 = CommonReturnCode.Normal;
		rc3 = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			//2.1 Prepare() 交易記錄初始資料
			rc2 = getFiscBusiness().prepareFEPTXN_IC();
			if (rc2 != CommonReturnCode.Normal) {
				isExitProgram = true;
				return rc2;
			}
			if (isIC) {
				RefBase<Ictltxn> ictltxnRefBase = new RefBase<>(defICTLTXN);
				RefBase<Ictltxn> oriictltxnRefBase = new RefBase<>(oriICTLTXN);
				rc2 = getFiscBusiness().prepareIctltxn(ictltxnRefBase, oriictltxnRefBase, MessageFlow.Request);
				defICTLTXN = ictltxnRefBase.get();
				oriICTLTXN = oriictltxnRefBase.get();
				if (rc2 != CommonReturnCode.Normal) {
					if (FISCReturnCode.OriginalMessageDataError.equals(rc2)) {// MAPPING 欄位資料不符
						//rtnCode = CommonReturnCode.Normal;
						//strFISCRc = FISCReturnCode.OriginalMessageDataError;
						getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					}else {
						isExitProgram = true;
						return rc2;
					}
				}
			}
			//2.2 以TRANSACTION 新增交易記錄
			rc3 = getFiscBusiness().insertFEPTxn();
			if (rc3 != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				isExitProgram = true;
				return rc3;
			}
			if (isIC) {
				if (dbICTLTXN.insertSelective(defICTLTXN) < 1) {
					transactionManager.rollback(txStatus);
					isExitProgram = true;
					return IOReturnCode.UpdateFail;
				}
			}
			// 2010-11-01 by kyo for Business 屬性為Assign因此造成例外
			// fiscBusiness.IntlTxn = defICTLTXN
			transactionManager.commit(txStatus);
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
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
		FEPReturnCode rtnCode  = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		try {
			//3.1 檢核特約商店代號for 2545,2546
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnMerchantId())) {
				rtnCode = getFiscBusiness().checkMerchant();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}

			//3.2 檢核提款金額及單筆限額
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno()) && getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}

			//3.3 檢核MAC
			//註解合庫開發環境暫時不測試
//			rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
//			this.logContext.setMessage("after checkFiscMac RC:" + rtnCode.toString());
//			logMessage(this.logContext);
//			if (rtnCode != CommonReturnCode.Normal) {
//				return rtnCode;
//			}
			//20221019 合庫不檢核CARD檔

			//3.4 檢核&更新原始交易狀態(for 2572/2546)
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				rtnCode = checkoriFEPTXN();
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 檢核和更新原始交易狀態
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode checkoriFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String I_TX_DATE = "";

		try {
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
			// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 本營業日檔
			getFiscBusiness().setOriginalFEPTxn(oriDBFEPTXN.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8); // for 2572/2546

				getLogContext().setRemark(StringUtils.join("SearchFeptxn 以FEPTXN_TX_DATETIME_FISC[1,8]=", getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8), " FEPTXN_BKNO=",
						getFiscBusiness().getFeptxn().getFeptxnBkno(), " FEPTXN_ORI_STAN=", getFiscBusiness().getFeptxn().getFeptxnOriStan(), " 找原交易"));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());

				rtnCode = searchOriginalFEPTxn(I_TX_DATE, getFiscBusiness().getFeptxn().getFeptxnBkno(), getFiscBusiness().getFeptxn().getFeptxnOriStan());
				if (rtnCode != CommonReturnCode.Normal) {
					rtnCode = FISCReturnCode.TransactionNotFound;
					getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
					getLogContext().setRemark("SearchFeptxn 找不到原交易");
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}else {
					//將SearchFEPTXN 之資料存至 oriFEPTXN
					oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
				}
			}

			// 檢核原交易是否成功
			if (!"A".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()) && !"B".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {
				rtnCode = FISCReturnCode.StatusNotMatch; // 交易狀態有誤
				getFiscBusiness().getFeptxn().setFeptxnTxrust("I"); // 原交易已拒絕
				getLogContext().setRemark(StringUtils.join("原交易不成功, FEPTXN_TXRUST=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				return rtnCode;
			}

			// 檢核原交易之 MAPPING 欄位是否相同
			if (!getFiscBusiness().getOriginalFEPTxn().getFeptxnDesBkno().equals(getFiscBusiness().getFeptxn().getFeptxnDesBkno())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnIcTac().equals(getFiscBusiness().getFeptxn().getFeptxnIcTac())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnIcmark().equals(getFiscBusiness().getFeptxn().getFeptxnIcmark())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnMerchantId().equals(getFiscBusiness().getFeptxn().getFeptxnMerchantId())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType().equals(getFiscBusiness().getFeptxn().getFeptxnAtmType())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().equals(getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmChk().equals(getFiscBusiness().getFeptxn().getFeptxnAtmChk())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnIcSeqno().equals(getFiscBusiness().getFeptxn().getFeptxnIcSeqno())
					|| getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtAct().doubleValue() != getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().doubleValue()
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnAtmno().trim())
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno().trim().equals(getFiscBusiness().getFeptxn().getFeptxnTroutActno().trim())) {
				rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust("N"); // 無帳務沖正
				getLogContext()
						.setRemark(StringUtils.join("與原交易欄位不相同, DES_BKNO=", getFiscBusiness().getFeptxn().getFeptxnDesBkno(), " 原DES_BKNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnDesBkno(),
								", TX_DATETIME_FISC=", getFiscBusiness().getFeptxn().getFeptxnTxDatetimeFisc(), " 原TX_DATETIME_FISC=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc(),
								", TX_AMT_ACT=", getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString(), " 原TX_AMT_ACT=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmtAct().toString(),
								", ATMNO=", getFiscBusiness().getFeptxn().getFeptxnAtmno(), " 原ATMNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno(), ", TROUT_ACTNO=",
								getFiscBusiness().getFeptxn().getFeptxnTroutActno(), " 原TROUT_ACTNO=", getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.INFO, getLogContext());
				return rtnCode;
			}
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("T"); // 沖銷或授權完成進行中
			oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());

			getFiscBusiness().getFeptxn().setFeptxnTroutActno(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno());
			getFiscBusiness().getFeptxn().setFeptxnTroutKind(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutKind());
			getFiscBusiness().getFeptxn().setFeptxnDueDate(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxDatetimeFisc().substring(0, 8));

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkoriFEPTXN"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
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
		stan ="0002357";
		try {
			db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
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
					db.setTableNameSuffix(wk_TBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
					getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
					getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
					getFiscBusiness().setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
					if (getFiscBusiness().getOriginalFEPTxn() == null) {
						if (StringUtils.isNotBlank(wk_NBSDY) && wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
							db.setTableNameSuffix(wk_NBSDY.substring(6, 8), StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
							getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
							getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
							getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
							getFiscBusiness()
									.setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
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
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".searchOriginalFEPTxn"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 6. SendToCBS/ASC(if need): 帳務主機處理
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String TxType;
		try {
			if("2505".equals(getFiscBusiness().getFeptxn().getFeptxnPcode())){
				TxType="0";
			}else{
				TxType="1";
			}
			String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
			this.getTxData().setIctlTxn(defICTLTXN);
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
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 成功
						oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn());
					}
					isExitProgram = true;
					return rtnCode;
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBS"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 組回傳財金Response電文
	 * 
	 * @return
	 * 
	 */
	@SuppressWarnings("unused")
	private FEPReturnCode PrepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		if (strFISCRc != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getLogContext().setProgramName(ProgramName);
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
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
	 * 組財金電文Body部份
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_BITMAP = null;

		try {
			//6.3 產生 Response 電文Body
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				// +REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else {
				// -REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}

			// 依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				if (wk_BITMAP.charAt(i) == '1') {
					switch (i) {
						case 2: // 交易金額
							getFiscRes().setTxAmt(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct().toString());
							break;
						case 5: // 代付單位 CD/ATM 代號
							getFiscRes().setATMNO(StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnAtmno(),8,"0"));
							break;
						case 6: // 可用餘額
							getFiscRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						case 14: // 跨行手續費
							getFiscRes().setFeeAmt(String.valueOf(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().doubleValue()));
							break;
						case 21: // 促銷訊息
							getFiscRes().setPromMsg("");
							Sysconf defSYSCONF = new Sysconf();
							SysconfExtMapper dbSYSCONF = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
							defSYSCONF.setSysconfSubsysno((short) 3);
							defSYSCONF.setSysconfName("AD");
							defSYSCONF = dbSYSCONF.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
							if (defSYSCONF != null) {
								if (StringUtils.isNotBlank(defSYSCONF.getSysconfValue())) {
									getFiscRes().setPromMsg(defSYSCONF.getSysconfValue());
								}
							}
							break;
					}
				}
			}

			//6.4 產生 MAC
			RefString mac = new RefString(getFiscRes().getMAC());
			rtnCode = encHelper.makeFiscMac(getFiscRes().getMessageType(), mac);
			getFiscRes().setMAC(mac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscRes().setMAC("00000000");
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 組回傳財金Response電文
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		if (strFISCRc != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getLogContext().setProgramName(ProgramName);
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
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
	 * UpdateTxData部份
	 * 
	 * @return
	 * 
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
			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R");// 拒絕
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response

			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != CommonReturnCode.Normal) {// 若更新失敗則不送回應電文，人工處理
				transactionManager.rollback(txStatus);
				isExitProgram = true;
				return rtnCode;
			}

			// 判斷是否需更新 ICTLTXN
			if (isIC) {
				// defICTLTXN.ICTLTXN_TX_CUR_ACT = fiscBusiness.FepTxn.FEPTXN_TX_CUR_ACT
				// defICTLTXN.ICTLTXN_TX_AMT_ACT = fiscBusiness.FepTxn.FEPTXN_TX_AMT_ACT
				// defICTLTXN.ICTLTXN_EXRATE = fiscBusiness.FepTxn.FEPTXN_EXRATE
				defICTLTXN.setIctltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
				defICTLTXN.setIctltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
				defICTLTXN.setIctltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defICTLTXN.setIctltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				defICTLTXN.setIctltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
				if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
					defICTLTXN.setIctltxnOriStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
				}
				if (dbICTLTXN.updateByPrimaryKeySelective(defICTLTXN) < 1) {// 若更新失敗則不送回應電文，人工處理
					transactionManager.rollback(txStatus);
					isExitProgram = true;
					return IOReturnCode.UpdateFail;
				}
			}

			// (3) 判斷是否需更新原始交易 for 2572/2546
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				if (getFiscBusiness().getOriginalFEPTxn() != null) {
					oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "updateTxData"));
					if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("D");
						getFiscBusiness().getOriginalFEPTxn().setFeptxnTraceEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());

						if (isIC) {// 國際卡交易需同時更新ICTLTXN
							oriICTLTXN.setIctltxnTxrust("D");
							if (dbICTLTXN.updateByPrimaryKeySelective(oriICTLTXN) < 1) {// 若更新失敗則不送回應電文，人工處理
								transactionManager.rollback(txStatus);
								isExitProgram = true;
								return IOReturnCode.UpdateFail;
							}
						}

						if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
							// 若更新失敗則不送回應電文, 人工處理
							transactionManager.rollback(txStatus);
							isExitProgram = true;
							return IOReturnCode.FEPTXNUpdateError;
						}
					} else {// -REP
							// 授權交易需先上主機解圏, 若解圏成功則 TXRUST =“C”
							// 所以若TXRUST =“T”進行中, 即可將原交易之狀態改回 Active
						if ("T".equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) {// 進行中for沖銷
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust("A"); // 將原始交易之狀態改為Active
							if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
								// 若更新失敗則不送回應電文，人工處理
								transactionManager.rollback(txStatus);
								isExitProgram = true;
								return IOReturnCode.FEPTXNUpdateError;
							}
						}
					}
				}
			}

			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 更新跨行代收付
	 * 
	 * @return
	 * 
	 */
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot()) && NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processICAptot(isEC);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}
	
	public FEPReturnCode insertINBK2160() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			rtnCode = getFiscBusiness().prepareInbk2160();
			if (rtnCode != CommonReturnCode.Normal) {
				if (rtnCode != CommonReturnCode.Normal) {
					getLogContext().setMessage(rtnCode.toString());
					getLogContext().setRemark("寫入檔案(INBK2160)發生錯誤!!");
					logMessage(Level.INFO, getLogContext());
					return FEPReturnCode.INBK2160InsertError;
				} else {
					return FEPReturnCode.Normal;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".AddTXData");
			sendEMS(getLogContext());
		}
		return FEPReturnCode.Normal;
	}
}
