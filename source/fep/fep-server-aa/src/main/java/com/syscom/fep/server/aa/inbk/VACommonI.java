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
import com.syscom.fep.mybatis.mapper.VacateMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.mybatis.model.Vacate;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.AcctChkStatus;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.VAChkItem;
import com.syscom.fep.vo.constant.VAChkType;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * <p>
 * 負責處理財金發動的約定及核驗服務交易Req電文
 * </p>
 * 
 * <p>
 * AA程式撰寫原則:
 * </p>
 * <ul>
 * <li>AA的程式主要為控制交易流程,Main為AA程式的進入點,在Main中的程式為控制交易的過程該如何進行</li>
 * <li>請不要在Main中去撰寫實際的處理細節,儘可能將交易過程中的每一個"步驟",以副程式的方式來撰寫,</li>
 * <li>而這些步驟,如果可以共用的話,請將該步驟寫在相關的Business物件中</li>
 * <li>如果該步驟只有該AA會用到的話,再寫至自己AA中的類別中</li>
 * </ul>
 * 
 * <history>
 * <modify>
 * <modifier>Fly</modifier>
 * <reason>2566約定及核驗服務類別項目</reason>
 * <date>2018/09/17</date>
 * </modify>
 * </history>
 * 
 * @author Richard
 */
@Deprecated
public class VACommonI extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode strFISCRc = CommonReturnCode.Normal;
	@SuppressWarnings("unused")
	private boolean isIC = false;
	private Vatxn defVATXN = new Vatxn();
	@SuppressWarnings("unused")
	private Intltxn oriINTLTXN = new Intltxn();
	private VatxnMapper dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);

	private boolean isEC = false;
	private boolean isVA = false;

	/**
	 * AA的建構式,在這邊初始化及設定其他相關變數
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 *        <p>
	 *        初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	 *        ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	 *        FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	 * @throws Exception
	 */
	public VACommonI(FISCData txnData) throws Exception {
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
			// 拆解並檢核財金電文
			_rtnCode = processRequestHeader();

			// 換日後該筆交易應重抓DBFepTxn在INSERT FEPTXN時才會寫入換日後的FEPTXNXX
			if (!SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8).equals(this.feptxnDao.getTableNameSuffix())) {
				this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, ".processRequestData"));// 2021-06-16 Richard add
				getTxData().setFeptxnDao(this.feptxnDao);
				getFiscBusiness().setFeptxnDao(this.feptxnDao);
			}

			// PrepareFEPTXN
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().prepareFEPTXN();
			}
			if (isVA) {
				if (_rtnCode == CommonReturnCode.Normal) {
					RefBase<Vatxn> vatxnRefBase = new RefBase<>(defVATXN);
					_rtnCode = getFiscBusiness().prepareVATXN(vatxnRefBase);
					defVATXN = vatxnRefBase.get();
				}
				if (_rtnCode != CommonReturnCode.Normal) {
					getLogContext().setRemark("PrepareVATXN-收到財金發動之約定及核驗服務在準備VATXN發生異常");
					logMessage(getLogContext());
					_rtnCode = CommonReturnCode.Normal;
					strFISCRc = FISCReturnCode.MessageFormatError;
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // R 拒絶
				}
			}

			// 新增交易記錄
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = addTxData();
			}

			if (strFISCRc == CommonReturnCode.Normal) {
				strFISCRc = _rtnCode;
			}

			// 商業邏輯檢核 & 電文Body檢核
			if (_rtnCode == CommonReturnCode.Normal && strFISCRc == CommonReturnCode.Normal) {
				strFISCRc = checkBusinessRule();
				// SendToCBS/ASC
				if (strFISCRc == CommonReturnCode.Normal) {
					_rtnCode = sendToCBS();
				}
			}

			// 組回傳財金Response電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = prepareForFISC();
			}

			// 更新交易記錄(FEPTXN & VATXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = updateTxData();
			}

			// 2018/12/19 Modify by Ruling for 原存金融帳戶核驗(類別=10)，新增寫入APTOT
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processAPTOT();
			}

			// 將組好的財金電文送給財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			}

		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	// ''' <summary>
	// ''' 更新跨行代收付
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <date>2018/12/19</date>
	// ''' <reason>原存金融帳戶核驗(類別=10)，新增寫入APTOT</reason>
	// ''' </modify>
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())
				&& defVATXN != null
				&& "10".equals(defVATXN.getVatxnCate().trim())
				&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processAptot(isEC);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return rtnCode;
	}

	// ''' <summary>
	// ''' UpdateTxData部份
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

		try {
			dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);

			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) { // '(3 way)
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // 'Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); // 'Pending
				} else { // '(2 way)
					getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // '成功
				}
			} else if ("0".equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // '拒絕
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // 'F2-FISC Response

			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != CommonReturnCode.Normal) { // '若更新失敗則不送回應電文，人工處理
				transactionManager.rollback(txStatus);
				return rtnCode;
			}

			// '判斷是否需更新 VATXN
			if (isVA) {
				// 'defVATXN.VATXN_TX_CUR_ACT = fiscBusiness.FepTxn.FEPTXN_TX_CUR_ACT
				// 'defVATXN.VATXN_TX_AMT_ACT = fiscBusiness.FepTxn.FEPTXN_TX_AMT_ACT
				defVATXN.setVatxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
				defVATXN.setVatxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
				defVATXN.setVatxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defVATXN.setVatxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				// '2019/02/21 Modify by Ruling for 2566約定及核驗服務類別10
				if (!StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnRemark())
						&& getFiscBusiness().getFeptxn().getFeptxnRemark().length() >= 6) {
					defVATXN.setVatxnResult(getFiscBusiness().getFeptxn().getFeptxnRemark().substring(0, 2));
					defVATXN.setVatxnAcresult(getFiscBusiness().getFeptxn().getFeptxnRemark().substring(2, 4));
					defVATXN.setVatxnAcstat(getFiscBusiness().getFeptxn().getFeptxnRemark().substring(4, 6));
				}
				if (dbVATXN.updateByPrimaryKey(defVATXN) < 1) { // '若更新失敗則不送回應電文，人工處理
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}
			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".updateTxData"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	// ''' <summary>
	// ''' 組回傳財金Response電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		if (strFISCRc != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(strFISCRc.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				this.logContext.setProgramName(StringUtils.join(ProgramName));
				getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(strFISCRc, FEPChannel.FISC, getLogContext()));
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
		}

		// '統一發票入帳帳號將回給財金錯誤代碼由 4501(轉出帳號錯誤) 改為 4507(轉入帳號錯誤)
		if (FISCPCode.PCode2566.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
				&& "11".equals(defVATXN.getVatxnCate())) {
			if ("4501".equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getFiscBusiness().getFeptxn().setFeptxnRepRc("4507");
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

	// ''' <summary>
	// ''' 組財金電文Body部份
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String wk_BITMAP = "";

		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				// '+REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else {
				// '-REP
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}

			// '依據wk_BITMAP(判斷是否搬值)
			for (int i = 2; i <= 63; i++) {
				if ("1".equals(wk_BITMAP.substring(i, i + 1))) {
					switch (i) {
						case 5: { // '代付單位 CD/ATM 代號
							getFiscRes().setATMNO(getFiscBusiness().getFeptxn().getFeptxnAtmno());
							break;
						}
						case 14: { // Case 14 '跨行手續費
							getFiscRes().setFeeAmt(getFiscBusiness().getFeptxn().getFeptxnFeeCustpayAct().toString());
							break;
						}
						case 45: { // '附言欄
							switch (defVATXN.getVatxnCate()) {
								case "11": // '統一發票中奬入帳帳號檢核
									getFiscRes().setMEMO(getFiscReq().getMEMO());
									break;
								case "10": // '跨行金融帳戶資訊核驗
									getFiscRes().setMEMO(getFiscReq().getMEMO().substring(0, 90) + getFeptxn().getFeptxnRemark().trim());
									break;
							}
						}
					}
				}
			}

			// '產生 MAC
			RefString refMac = new RefString(getFiscRes().getMAC());
			rtnCode = encHelper.makeFiscMac(getFiscRes().getMessageType(), refMac);
			getFiscRes().setMAC(refMac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscRes().setMAC("00000000");
			}

			return rtnCode;

		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	// ''' <summary>
	// ''' 6. SendToCBS/ASC(if need): 帳務主機處理
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode sendToCBS() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		T24 hostT24 = new T24(getTxData());

		try {
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
				boolean ProcessTag;
				byte TxType;

				if (getTxData().getMsgCtl().getMsgctlCbsFlag() != 0) {
					// '轉出交易
					TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().byteValue();
					ProcessTag = true;
				} else {
					// '轉入交易
					TxType = 1;
					ProcessTag = false;
				}

				switch (defVATXN.getVatxnCate()) {
					case "11": { // '統一發票中奬入帳帳號檢核
						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, ProcessTag);
						break;
					}
					case "10": { // '跨行金融帳戶資訊核驗
						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(), TxType, ProcessTag);
						break;
					}
				}

				if (rtnCode != CommonReturnCode.Normal) {
					if (!StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
						strFISCRc = rtnCode;
						return CommonReturnCode.Normal;
					} else {
						getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
						getFiscBusiness().getFeptxn().setFeptxnTxrust("S");
						getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
						getFiscBusiness().updateTxData();
						return rtnCode;
					}
				} else {
					// '送T24主機成功比對身份証號
					if ("11".equals(defVATXN.getVatxnCate())) {
						if (getTxData().getT24Response() != null
								&& getTxData().getT24Response().getTotaEnquiryContents().size() > 0
								&& !getTxData().getT24Response().getTotaEnquiryContents().get(0).containsKey("IDNO")) {
							getFeptxn().setFeptxnIdno(getTxData().getT24Response().getTotaEnquiryContents().get(0).get("IDNO"));
							if (defVATXN.getVatxnIdno().length() + 1 != getFeptxn().getFeptxnIdno().length()) {
								this.logContext.setRemark("身份証號／營利事業統一編號長度不符 VATXN_IDNO[" + defVATXN.getVatxnIdno() + "] FEPTXN_IDNO[" + getFeptxn().getFeptxnIdno() + "]");
								logMessage(getLogContext());
								strFISCRc = FISCReturnCode.CheckIDNOError; // 'rtncode要回Normal才會送REP電文給財金，所以比對不符要寫入strFISCRc，再組財金電文時才會轉成財金錯誤代碼
							}

							if (defVATXN.getVatxnIdno().equals(getFeptxn().getFeptxnIdno().substring(0, defVATXN.getVatxnIdno().trim().length()))) {
								this.logContext.setRemark("身份証號／營利事業統一編號長度不符 VATXN_IDNO[" + defVATXN.getVatxnIdno() + "] FEPTXN_IDNO[" + getFeptxn().getFeptxnIdno() + "]");
								logMessage(getLogContext());
								strFISCRc = FISCReturnCode.CheckIDNOError; // 'rtncode要回Normal才會送REP電文給財金，所以比對不符要寫入strFISCRc，再組財金電文時才會轉成財金錯誤代碼
							}
						}
					} else {
						this.logContext.setRemark("T24下行電文IDNO無此欄位或無值");
						logMessage(getLogContext());
						strFISCRc = FISCReturnCode.CheckIDNOError; // 'rtncode要回Normal才會送REP電文給財金，所以比對不符要寫入strFISCRc，再組財金電文時才會轉成財金錯誤代碼
					}

					// '2019/01/28 Modify by Ruling for 2566約定及核驗服務類別10，依項目及帳戶核驗結果回給財金不同的錯誤代碼
					if ("10".equals(defVATXN.getVatxnCate())) {
						this.logContext.setRemark("項目核驗結果" + StringUtils.rightPad(getFiscBusiness().getFeptxn().getFeptxnRemark(), 6, " ").substring(0, 2));
						logMessage(getLogContext());

						if (VAChkType.TWDNoCard.equals(defVATXN.getVatxnType())
								|| VAChkType.ForeignNoCard.equals(defVATXN.getVatxnType())) {
							if (StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnRemark())
									|| getFiscBusiness().getFeptxn().getFeptxnRemark().trim().length() < 4) {
								this.logContext.setRemark("帳戶核驗結果為NULL或空值或欄位長度不足4位");
								logMessage(getLogContext());
								strFISCRc = FISCReturnCode.CheckIDNOError;
							}

							if (!AcctChkStatus.ChkSuccess.equals(getFiscBusiness().getFeptxn().getFeptxnRemark().substring(2, 2))) {
								this.logContext.setRemark("帳戶核驗結果為" + getFiscBusiness().getFeptxn().getFeptxnRemark().substring(2, 2));
								logMessage(getLogContext());
								strFISCRc = FEPReturnCode.NotICCard;
							}
						}
					}
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToCBS"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	// ''' <summary>
	// ''' 商業邏輯檢核
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.ProgramException;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			// '2019/02/19 Modify by Ruling for 調整檢核順序及Log說明
			// '檢核約定及核驗服務業務
			Vacate vacate = new Vacate();
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnNoticeId())) {
				vacate.setVacateNo(getFiscBusiness().getFeptxn().getFeptxnNoticeId().substring(0, 2));
			}
			VacateMapper vacateMapper = SpringBeanFactoryUtil.getBean(VacateMapper.class);
			if (vacateMapper.selectByPrimaryKey(vacate.getVacateNo()) == null) {
				this.logContext.setRemark("業務類別未上線 VACATE_NO=" + vacate.getVacateNo());
				logMessage(getLogContext());
				return FEPReturnCode.MessageFormatError;
			}
			// '2019/02/19 Modify by Ruling for 調整檢核順序及Log說明
			switch (defVATXN.getVatxnType()) {
				// 跨行金融帳戶資訊核驗
				case "10":
					switch (defVATXN.getVatxnType()) {
						case VAChkType.ICCard: { // 金融卡核驗
							if (!(VAChkItem.ACCT.equals(defVATXN.getVatxnItem())
									|| VAChkItem.ID.equals(defVATXN.getVatxnItem())
									|| VAChkItem.Mobile.equals(defVATXN.getVatxnItem())
									|| VAChkItem.Birthday.equals(defVATXN.getVatxnItem())
									|| VAChkItem.HPhone.equals(defVATXN.getVatxnItem()))) {
								this.logContext.setRemark("金融卡核驗項目為" + defVATXN.getVatxnItem() + "非[00,01,02,03,04]核驗項目");
								logMessage(getLogContext());
								return FEPReturnCode.MessageFormatError;
							}
							break;
						}
						// 台幣或外幣帳戶核驗
						case VAChkType.TWDNoCard:
						case VAChkType.ForeignNoCard:
							if (!(VAChkItem.IDMobile.equals(defVATXN.getVatxnItem())
									|| VAChkItem.IDMobileBirthday.equals(defVATXN.getVatxnItem())
									|| VAChkItem.IDMobileHPhone.equals(defVATXN.getVatxnItem())
									|| VAChkItem.IDMobileBirthdayHPhone.equals(defVATXN.getVatxnItem()))) {
								this.logContext.setRemark("台幣或外幣帳戶核驗項目為" + defVATXN.getVatxnItem() + "非[11,12,13,14]核驗項目");
								logMessage(getLogContext());
								return FEPReturnCode.MessageFormatError;
							}
							break;
					}
					// 統一發票中獎獎金入帳帳號核驗
				case "11":
					if (StringUtils.isBlank(defVATXN.getVatxnIdno())) {
						this.logContext.setRemark("問題帳戶  VATXN_IDNO為NULL或空值");
						logMessage(getLogContext());
						return FEPReturnCode.CheckIDNOError;
					}
					break;
			}

			// '檢核MAC & IC TAC
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFiscBusiness().getFeptxn().getFeptxnBkno())
					&& !StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnIcTac())) {
				if (getFiscReq().getTAC().length() < 16) { // '長度小於16
					return FISCReturnCode.LengthError;
				}

				if (StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnIcmark())
						|| (StringUtils.leftPad("0", getTxData().getTxObject().getINBKRequest().getICMARK().length(), "0")).equals(getTxData().getTxObject().getINBKRequest().getICMARK())) {
					this.logContext.setRemark("財金電文ICMARK為空白=" + getTxData().getTxObject().getINBKRequest().getICMARK());
					logMessage(getLogContext());
					return ATMReturnCode.ICMARKError;
				}

				rtnCode = encHelper.checkFiscMacAndTac(getFiscReq().getMAC(), getTxData().getMsgCtl().getMsgctlTacType());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
				// '檢核 Card Status
				if (StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnMajorActno())) {
					this.logContext.setRemark("卡片帳號(FEPTXN_MAJOR_ACTNO)為空白=" + getFiscBusiness().getFeptxn().getFeptxnMajorActno());
					logMessage(getLogContext());
					return ATMReturnCode.NotICCard; // '4401-失效卡片
				}

				// '一般金融卡
				rtnCode = getFiscBusiness().checkCardStatus();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				// '海外卡不提供跨國晶片卡交易
				if (!ZoneCode.TWN.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode())) {
					this.logContext.setRemark("海外卡不提供跨國晶片卡交易, FEPTXN_ZONE_CODE=" + getFiscBusiness().getFeptxn().getFeptxnZoneCode());
					logMessage(getLogContext());
					return FISCReturnCode.CCardServiceNotAllowed; // '4501:該卡片之帳戶為問題帳戶
				}

				// 被代理不可執行信用卡交易
				if (!StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnTroutKind())) {
					getLogContext().setRemark("被代理不可執行信用卡交易, FEPTXN_TROUT_KIND=" + getFiscBusiness().getFeptxn().getFeptxnTroutKind());
					logMessage(Level.INFO, getLogContext());
					return FISCReturnCode.CCardServiceNotAllowed; // 4501:該卡片之帳戶為問題帳戶
				}
			}

			if (getFiscBusiness().getFeptxn().getFeptxnBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
					&& StringUtils.isWhitespace(getFiscBusiness().getFeptxn().getFeptxnIcTac())) {
				rtnCode = encHelper.checkFiscMac(getFiscReq().getMessageType(), getFiscReq().getMAC());
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	// ''' <summary>
	// ''' 新增交易記錄
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			VatxnMapper dbVATXN = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
			rtnCode = getFiscBusiness().insertFEPTxn();
			if (rtnCode != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				return rtnCode;
			}

			if (isVA) {
				if (dbVATXN.insert(defVATXN) < 1) {
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}
			transactionManager.commit(txStatus);
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			this.logContext.setProgramException(ex);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".addTxData"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	// ''' <summary>
	// ''' 拆解並檢核由財金發動的Request電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// '檢核財金電文 Header
		rtnCode = getFiscBusiness().checkHeader(getFiscReq(), true);

		if (rtnCode == FEPReturnCode.MessageTypeError
				|| rtnCode == FISCReturnCode.TraceNumberDuplicate
				|| rtnCode == FISCReturnCode.OriginalMessageError
				|| rtnCode == FISCReturnCode.STANError
				|| rtnCode == FISCReturnCode.CheckBitMapError
				|| rtnCode == FISCReturnCode.SenderIdError) {
			getFiscBusiness().sendGarbledMessage(getFiscReq().getEj(), rtnCode, getFiscReq());
			return rtnCode;
		}

		if (StringUtils.isNotBlank(getFiscReq().getMEMO())) {
			isVA = true;
		}

		if (rtnCode != CommonReturnCode.Normal) {
			strFISCRc = rtnCode;
			rtnCode = CommonReturnCode.Normal;
		}
		return rtnCode;
	}
}
