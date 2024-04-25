package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.common.util.PolyfillUtil.VbStrConv;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.T24Adapter;
import com.syscom.fep.vo.constant.*;
import com.syscom.fep.vo.enums.*;
import com.syscom.fep.vo.text.t24.T24PreClass;
import com.syscom.fep.vo.text.t24.T24TITAA0000;
import com.syscom.fep.vo.text.t24.T24TITAB0000;
import com.syscom.fep.vo.text.t24.T24TITAC0000;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class T24 extends HostBase {
	private static final String ProgramName = T24.class.getSimpleName();
	private ATMData mATMTxData;
	private FISCData mFISCData;
	private String mCBSTxid;
	private CompanyMapper companyMapper = SpringBeanFactoryUtil.getBean(CompanyMapper.class);
	T24Adapter adapter = new T24Adapter(getGeneralData());
	private CbspendMapper cbspendMapper = SpringBeanFactoryUtil.getBean(CbspendMapper.class);
//	private SmsmsgMapper smsmsgMapper = SpringBeanFactoryUtil.getBean(SmsmsgMapper.class);
	private BsdaysMapper bsdaysMapper = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
	private ZoneMapper zoneMapper = SpringBeanFactoryUtil.getBean(ZoneMapper.class);
	private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
	private AllbankMapper allbankMapper = SpringBeanFactoryUtil.getBean(AllbankMapper.class);
	private IntltxnMapper intltxnMapper = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
	private MerchantMapper merchantMapper = SpringBeanFactoryUtil.getBean(MerchantMapper.class);
	private NpsunitMapper npsunitMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);

	public T24(MessageBase txData) {

		super(txData);

		// 新增MDAWHO Channel Code：增加MMAB2B、MDAWHO、B2CAPP三個Channel
		if (getGeneralData().getTxChannel() != null) {
			switch (getGeneralData().getTxChannel()) {
				case ATM:
				case FCS:
				case NETBANK:
//				case MMAB2C:
				case MOBILBANK:
				case IVR:
				case WEBATM:
//				case MMAB2B:
//				case MDAWHO:
//				case B2CAPP:
					// WEBATM的IIQ電文要將T24主機回傳之身份證號再回給WEBATM
					mATMTxData = (ATMData) getGeneralData();
					break;
				case FISC:
//				case EBILL:
//					mFISCData = (FISCData) getGeneralData();
//					break;
				default:
					break;
			}
		}

	}

	public FEPReturnCode sendToT24(String CBSTxid, int txType, Boolean processFlag) throws Exception {
		FEPReturnCode rtnCode = FEPReturnCode.ProgramException;
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode2 = null; // 新增CBSPEND的結果
		T24TxType t24Type = T24TxType.fromValue(txType);
		int wEJ = 0;
		boolean noTITA = false;
		T24PreClass t24ReqMessage = null;
		T24Adapter adapter = new T24Adapter(getGeneralData());
		String company = null;
		String version = null;
		String tranactionID = null;
		String processTag = null;
		boolean reversetag = false;
		Company defCOMPANY = new Company();
		LogHelperFactory.getTraceLogger().trace("sendToT24 CBSTxid=", CBSTxid, ", txType=", String.valueOf(txType), ", processFlag=", processFlag.toString());
		try {
			mCBSTxid = CBSTxid;

			// 港澳NCB:檢核台灣T24主機連線狀態
			// 檢核台灣T24主機連線狀態
			FEPCache.reloadCache(CacheItem.SYSSTAT);
			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatT24Twn())) {
				// 加文字檔log
				getLogContext().setRemark("sendToT24-台灣T24主機暫停服務不含CDF（SYSSTAT_T24_TWN = False）");
				this.logMessage(getLogContext());
				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
					// 跨行
					if (StringUtils.isBlank(getFeptxn().getFeptxnTxCode())) {
						// 原存行交易
						return FEPReturnCode.ReceiverBankOperationStop; // 0206 收信單位主機未在跨行作業運作狀態
					} else {
						// 代理行交易
						return FEPReturnCode.SenderBankServiceStop; // 0202 發信單位該項跨行業務停止或暫停營業
					}
				} else {
					// 自行
					// 全行暫停服務存款確認仍需送T24主機
					if (!ATMTXCD.CDF.name().equals(getFeptxn().getFeptxnConTxCode())) {
						return FEPReturnCode.WithdrawServiceStop; // E948 提款暫停服務
					}
				}
			}

			// SendToCBS多傳一個參數processFlag來判斷是PROCESS或VALIDATE
			// company 由 SYSCONF帶入

			// 1. CBS_TITA 輸入參數
			// for Enquiry電文要帶company
			// for T24 company 異動
			// company
			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
				// 跨行交易
				// 跨行提領外幣：company為TW8070+ATM帳務分行
				// 跨行無卡提款
				if (T24Version.A1051.equals(CBSTxid) || T24Version.A1052.equals(CBSTxid)) {
					company = StringUtils.join(ATMPConfig.getInstance().getTWDCompanyCode().substring(0, 6), getFeptxn().getFeptxnAtmBrno());
				} else {
					company = ATMPConfig.getInstance().getTWDCompanyCode();
				}
			} else {
				// 自行交易
				// 人民幣:company為TW8070+ATM帳務分行
				// 行動金融信用卡eBILL繳費手續費優惠：company為TW8070+轉出帳號分行別
				// ATM新功能-跨行存款
				// 無卡提款(A1001)
				// 外幣無卡提款(A1042)
				// 2566約定及核驗服務類別10
				switch (CBSTxid) {
					case T24Version.A1040:
					case T24Version.A1000:
					case T24Version.A1050:
					case T24Version.A2910:
					case T24Version.A2920:
					case T24Version.A2930:
					case T24Version.A1041:
					case T24Version.A1001:
					case T24Version.A1042:
						// 自行ATM現金類交易(外幣提款/台幣提款/代理提款/存款)
						company = StringUtils.join(ATMPConfig.getInstance().getTWDCompanyCode().substring(0, 6), getFeptxn().getFeptxnAtmBrno());
						break;
					case T24Version.A1070:
					case T24Version.A1270:
					case T24Version.A1271:
					case T24Version.B5000:
					case T24Version.A1030:
						// 自行ATM轉帳類交易(自行轉帳/全國性繳費/預約轉帳)及台灣卡海外跨區提款
						// 悠遊Debit卡
						if (FEPChannel.SVCS.name().equals(getFeptxn().getFeptxnChannel())) {
							company = StringUtils.join(ATMPConfig.getInstance().getTWDCompanyCode().substring(0, 6), getFeptxn().getFeptxnMajorActno().substring(2, 5));
						} else {
							company = StringUtils.join(ATMPConfig.getInstance().getTWDCompanyCode().substring(0, 6), getFeptxn().getFeptxnTroutActno().substring(2, 5));
						}
						break;
					case T24Version.B0001:
					case T24Version.B0002:
					case T24Version.B0003:
					case T24Version.B0005:
						// 自行查詢類交易固定值為TW8079999
						company = StringUtils.join(ATMPConfig.getInstance().getTWDCompanyCode().substring(0, 5), "9999");
						break;
					case T24Version.A1370:
						// 自行ATM信用卡預現轉帳交易
						company = ATMPConfig.getInstance().getTWDCompanyCode();
						break;
					default:
						company = "";
						break;
				}
			}
			// 分行撤銷:分行撤銷生效日<=自行營業日，改送撤銷後績效行

			defCOMPANY.setRecid(company);
			Company com = companyMapper.selectByPrimaryKey(defCOMPANY.getRecid());
			if (com != null) {
				if (StringUtils.isNotBlank(com.getRseffdate()) && com.getRseffdate().compareTo(getFeptxn().getFeptxnTbsdy()) <= 0) {
					company = com.getRscompany();
				} else {
					// 原來的company
				}
			} else {
				getLogContext().setRemark(StringUtils.join("COMPANY 無資料, COMPANY.RECID=", defCOMPANY.getRecid()));
				this.logMessage(getLogContext());
				return FEPReturnCode.OtherCheckError;
			}

			// version
			if (txType == T24TxType.EC.getValue()) {
				version = T24Version.REVERSE;
			} else {
				version = CBSTxid;
			}
			// reversetag
			if ((txType == T24TxType.EC.getValue() || txType == T24TxType.UnLock.getValue())) {
				reversetag = true;
			} else {
				reversetag = false;
			}
			// tranactionID
			if (txType == T24TxType.EC.getValue() || txType == T24TxType.UnLock.getValue()) {
				// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
				if (T24Version.A1050.equals(CBSTxid) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
					tranactionID = getFeptxn().getFeptxnVirCbsRrn();
				} else {
					tranactionID = getFeptxn().getFeptxnCbsRrn();
				}
			} else {
				tranactionID = "";
			}
			// ProcessTag
			if (processFlag) {
				// 配合T24電文GTS.CONTROL欄位改送NULL
				// Ruling SPEC異動
				processTag = "PROCESS/NULL";
			} else {
				processTag = "VALIDATE/NULL";
			}

			t24ReqMessage = new T24PreClass(company, version, reversetag, tranactionID, processTag);
			// username & password改成用SYSCONF資料
			t24ReqMessage.setUserName(ATMPConfig.getInstance().getT24UserName());
			t24ReqMessage.setPassword(ATMPConfig.getInstance().getT24Password());
			t24ReqMessage.setTxType(t24Type);

			// 3. 取得FEP電子日誌序號
			switch (T24TxType.fromValue(txType)) {
				case Accounting:
				case Authorized:
					wEJ = getFeptxn().getFeptxnEjfno().intValue();
					break;
				case EC:
				case UnLock:
					noTITA = true;
					t24ReqMessage.setReverseTag(true);
					// 港澳NCB
					t24ReqMessage.setTransactionId(tranactionID);
					// t24ReqMessage.TransactionId = getFeptxn().FEPTXN_CBS_RRN
					break;
				default:
					return rtnCode;
			}

			if (!noTITA) {
				genT24TITA(t24ReqMessage, wEJ, CBSTxid);
			}

			// 5. 將組好的T24電文送往T24主機
			// 送T24主機前，先更新交易記錄
			switch (T24TxType.fromValue(txType)) {
				case Accounting:
					switch (Integer.parseInt(CBSTxid.substring(1, 2))) {
						case 1:
							getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Request);
							break;
						case 2:
							// 2010/11/23 add by ashiang
							if (processFlag) {
								getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_CR_Request);
							}
							break;
						default:
							getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_INQ_Request);
							break;
					}
					break;
				case EC:
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_EC_Request);
					break;
				case Authorized:
				case UnLock:
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_INQ_Request);
					break;
				default:
					break;
			}

			// getFeptxn().FEPTXN_NEED_SEND_CBS = txType
			// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
			if (T24Version.A1050.equals(CBSTxid) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
				getFeptxn().setFeptxnVirCbsTxCode(CBSTxid);
				getFeptxn().setFeptxnVirCbsTimeout(DbHelper.toShort(true));
			} else {
				getFeptxn().setFeptxnCbsTxCode(CBSTxid);
				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(true));
			}

			if (processFlag) {
				getFeptxn().setFeptxnNeedSendCbs((short) txType);
				if ("A".equals(CBSTxid.substring(0, 1))) {// T24 version A類 - 帳務類交易
					// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
					if (T24Version.A1050.equals(CBSTxid) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
						getFeptxn().setFeptxnVirAccType((short) AccountingType.UnKnow.getValue());
					} else {
						getFeptxn().setFeptxnAccType((short) AccountingType.UnKnow.getValue());
					}
				}
			}

			rtnCode = FEPReturnCode.HostResponseTimeout;

			if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) < 1) {
				return FEPReturnCode.FEPTXNUpdateError;
			}
			adapter.setMessageToT24(t24ReqMessage.getGenT24ReqOFS());

			rtnCode = adapter.sendReceive();

			// 設定TIMER等待T24主機回應訊息
			if (rtnCode == FEPReturnCode.HostResponseTimeout || rtnCode == FEPReturnCode.ProgramException
					|| rtnCode == FEPReturnCode.CBSResponseError) {
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {
					rtnCode2 = t24TimeOutProcess(CBSTxid, t24ReqMessage.getReverseTag(), adapter.getMessageToT24());
				}
				return rtnCode;
			}

			boolean t24ResponseNormal = false;
			if (T24Version.B0001.equals(CBSTxid) || T24Version.B0002.equals(CBSTxid) || T24Version.B0003.equals(CBSTxid)
					|| T24Version.B0005.equals(CBSTxid) || T24Version.B4000.equals(CBSTxid)) {
				t24ResponseNormal = t24ReqMessage.parseT24RspOfsForBType(adapter.getMessageFromT24(), CBSTxid.toString());
				if (mATMTxData != null) {
					mATMTxData.setT24Response(t24ReqMessage);
				}
				if (mFISCData != null) {
					mFISCData.setT24Response(t24ReqMessage);
				}
			} else {
				t24ResponseNormal = t24ReqMessage.parseT24RspOFS(adapter.getMessageFromT24());
			}

			// 增加判斷T24Response 之Dictionary是否有值,若沒有值則視為T24回應異常
			if (t24ReqMessage.getTOTATransResult() == null || t24ReqMessage.getTOTATransResult().size() == 0) {
				t24ResponseNormal = false;
			}

			// 設定TIMER等待T24主機回應訊息
			// 增加判斷如果T24回應異常則也進入TimeoutProcess
			if (!t24ResponseNormal) {
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {
					rtnCode2 = t24TimeOutProcess(CBSTxid, t24ReqMessage.getReverseTag(), adapter.getMessageToT24());
				}
				return FEPReturnCode.CBSResponseError;
			}

			getFeptxn().setFeptxnMsgflow(getFeptxn().getFeptxnMsgflow().substring(0, 1) + "2");
			// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
			if (T24Version.A1050.equals(CBSTxid) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
				getFeptxn().setFeptxnVirCbsTimeout(DbHelper.toShort(false)); // 收到回應正常CBS逾時 FLAG設為False
				getFeptxn().setFeptxnCbsRc(t24ReqMessage.getTOTATransResult().get("EB.ERROR"));
			} else {
				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false)); // 收到回應正常CBS逾時 FLAG設為False
				getFeptxn().setFeptxnCbsRc(t24ReqMessage.getTOTATransResult().get("EB.ERROR"));
			}

			rtnCode = FEPReturnCode.Normal; /// * 收到主機回應, 改成 Normal */

			String channelRC = "";
			if (!NormalRC.FEP_OK.equals(t24ReqMessage.getTOTATransResult().get("EB.ERROR"))) {
				// 失敗
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {// T24 version A類 - 帳務類交易
					// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
					if (T24Version.A1050.equals(CBSTxid) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
						if ("A2".equals(CBSTxid.substring(0, 2)) || txType == T24TxType.EC.getValue()) {// T24 version
																										// A類// - 帳務類交易
																										// -// 入帳類交易
							getFeptxn().setFeptxnAccType((short) AccountingType.ECFail.getValue());
						} else if ("A1".equals(CBSTxid.substring(0, 2))) {
							getFeptxn().setFeptxnVirAccType((short) AccountingType.UnAccounting.getValue());
						}
					} else {
						if ("A2".equals(CBSTxid.substring(0, 2)) || txType == T24TxType.EC.getValue()) {// T24 version
																										// A類// - 帳務類交易
																										// -// 入帳類交易
							getFeptxn().setFeptxnAccType((short) AccountingType.ECFail.getValue());
						} else if ("A1".equals(CBSTxid.substring(0, 2))) {
							getFeptxn().setFeptxnAccType((short) AccountingType.UnAccounting.getValue());
						}
					}
				}
				getLogContext().setProgramName(ProgramName);

				// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
				if (T24Version.A1050.equals(CBSTxid) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
					channelRC = TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnVirCbsRc(), FEPChannel.T24, getGeneralData().getTxChannel(), getLogContext());
					getFeptxn().setFeptxnVirCbsRc(getLogContext().getExternalCode());
					getFeptxn().setFeptxnVirErrMsg(getLogContext().getResponseMessage());
				} else {
					channelRC = TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnCbsRc(), FEPChannel.T24, getGeneralData().getTxChannel(), getLogContext());
					getFeptxn().setFeptxnCbsRc(getLogContext().getExternalCode());
					getFeptxn().setFeptxnErrMsg(getLogContext().getResponseMessage());
				}

				if ((!"A2".equals(CBSTxid.substring(0, 2)) || !processFlag) && txType != 2) {// 入帳及沖正交易若上主機失敗亦需視為失功，其他交易則回覆錯誤
					if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
						getFeptxn().setFeptxnReplyCode(channelRC);
					} else {
						getFeptxn().setFeptxnRepRc(channelRC);
					}
				}

				return FEPReturnCode.CBSResponseError;
			} else {
				// 成功
				if ("A".equals(CBSTxid.substring(0, 1))) {
					if (processFlag) {// T24 version A類 - 帳務類交易
						// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
						if (T24Version.A1050.equals(CBSTxid)
								&& StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
							getFeptxn().setFeptxnVirAccType((short) txType);
							getFeptxn().setFeptxnCbsRrn(t24ReqMessage.getTOTATransContent().get("transactionId"));
						} else {
							getFeptxn().setFeptxnAccType((short) txType);
							getFeptxn().setFeptxnCbsRrn(t24ReqMessage.getTOTATransContent().get("transactionId"));

							// 國外提款提醒及管控機制
							// 修正財金-CON時送T24沖正成功，會重覆Insert SMSMSG，導致EMS有一筆PK重覆的異常訊息的問題
							// CASH OUTBOUND
							if ((T24Version.A1020.equals(CBSTxid) || T24Version.A1021.equals(CBSTxid)
									|| T24Version.A1060.equals(CBSTxid)) && txType == T24TxType.Accounting.getValue()) {
								// A1020國際金融卡提款(台幣)與A1021晶片金融卡跨國提款，拆解T24主機回應之手機號碼，濾掉手機號碼中的"-"，寫入簡訊資料檔
								insertSMSMSG(CBSTxid, t24ReqMessage);
							}

							// 三萬元轉帳主動通知
							if ((T24Version.A1070.equals(CBSTxid) || T24Version.A1170.equals(CBSTxid)
									|| T24Version.A1110.equals(CBSTxid)) && txType == T24TxType.Accounting.getValue()) {
								if (StringUtils.isBlank(getFeptxn().getFeptxnTroutKind())
										&& "6011".equals(getFeptxn().getFeptxnAtmType())
										&& getFeptxn().getFeptxnTxAmt().compareTo(
												new BigDecimal(INBKConfig.getInstance().getATMFTNoticeAmt())) >= 0) {
									// 只通知個人，公司不通知(T.ATM.SECTOR欄位P:個人 C:公司)
									if (t24ReqMessage.getTOTATransContent().containsKey("T.ATM.SECTOR")
											&& StringUtils.isNotBlank(t24ReqMessage.getTOTATransContent().get("T.ATM.SECTOR"))
											&& t24ReqMessage.getTOTATransContent().get("T.ATM.SECTOR").equals("P")) {
										getFeptxn().setFeptxnRsCode("FT");
										insertSMSMSG(CBSTxid, t24ReqMessage);
									}
								}
							}
						}
					} else {
						// CASH OUTBOUND：電子支付退貨交易(2556)在VALIDATE時寫入簡訊資料檔
						if (T24Version.A2060.equals(CBSTxid)) {
							insertSMSMSG(CBSTxid, t24ReqMessage);
						}
					}
				} else {
					// B5000 時要將transactionId的值寫入FEPTXN_CBS_RRN
					if (T24Version.B5000.equals(CBSTxid)) {
						getFeptxn().setFeptxnCbsRrn(t24ReqMessage.getTOTATransContent().get("transactionId"));
					}
				}

			}

			if (txType == T24TxType.EC.getValue() || txType == T24TxType.UnLock.getValue()) {
				return rtnCode;
			}

			// 線上比對T24主機資料：帳務類交易比對T24主機回應電文借/貸方帳號及金額
			// 線上比對T24主機資料：增加入帳類(A2XX)比對
			// 線上比對T24主機資料：全部比對
			if ("A".equals(CBSTxid.substring(0, 1)) && processFlag && txType == T24TxType.Accounting.getValue()
					&& NormalRC.FEP_OK.equals(t24ReqMessage.getTOTATransResult().get("EB.ERROR"))) {

				compareTOTA(CBSTxid, t24ReqMessage);
			}

			// 依交易類別更新 getFeptxn()
			rtnCode = updateFEPTXNbyCBSTxid(CBSTxid, t24ReqMessage);

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToT24"));
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	private void genT24TITA(T24PreClass t24ReqMessage, Integer wEJ, String CBSTxid) throws Exception {
		// 4. 組T24 TITA電文, 電文內容格式請參照: T24 電文規格書
		// 2011/07/05 modify by Ruling 根據2011/06/28開會結論將預約交易之通道別改為原交易之通道而非FCS
		// 2014/04/10 modify by Ruling for 豐掌櫃：以原交易之通道而非NETBANK
		if (!FEPChannel.FCS.name().equals(getFeptxn().getFeptxnChannel())
				&& !FEPChannel.NETBANK.name().equals(getFeptxn().getFeptxnChannel())) {
			t24ReqMessage.getTITAHeader().setTiChnnCodeS(getFeptxn().getFeptxnChannel());
		} else {
			//--ben-20220922-//t24ReqMessage.getTITAHeader().setTiChnnCodeS(mATMTxData.getTxObject().getRequest().getCHLCODE());
		}

		t24ReqMessage.getTITAHeader().setTiChnnCode("FEP");
		// .TRMNO = getFeptxn().getFeptxnAtmno().Trim.PadLeft(8, "0"c)
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
			t24ReqMessage.getTITAHeader().setTRMNO(getFeptxn().getFeptxnAtmno());
		} else {
			t24ReqMessage.getTITAHeader().setTRMNO(getFeptxn().getFeptxnAtmnoVir());
		}
		t24ReqMessage.getTITAHeader().setEJFNO(getFeptxn().getFeptxnTxDate() + StringUtils.leftPad(wEJ.toString(), 12, "0"));
		t24ReqMessage.getTITAHeader().setFiscDate(getFeptxn().getFeptxnTbsdyFisc());

		// 2011/09/20 Modify by Ruling 跨行預約交易REG.FLA="Y"
		if (FEPChannel.FCS.name().equals(getFeptxn().getFeptxnChannel())) {
			t24ReqMessage.getTITAHeader().setRegFlag("Y");
		} else {
			t24ReqMessage.getTITAHeader().setRegFlag("");
		}

		t24ReqMessage.getTITAHeader().setFepUserId("");
		t24ReqMessage.getTITAHeader().setFepPassword("");
		t24ReqMessage.setTITAHeader(t24ReqMessage.getTITAHeader());
		switch (CBSTxid) {
			case T24Version.B0001: // 帳戶餘額查詢(IIQ/IQ2)
				t24ReqMessage.setEnquiryTag(true);
				t24ReqMessage.setEnquiryName("TMB.ENQ.CHL.ACCT.BAL");
				genB0001Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.B0002: // 現金存款調戶名及帳號(INM)
				t24ReqMessage.setEnquiryTag(true);
				t24ReqMessage.setEnquiryName("TMB.AC.BAL.DTL.LIST.ENQ");
				genB0002Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.B0003: // ATM調帳號(IAC)
				t24ReqMessage.setEnquiryTag(true);
				t24ReqMessage.setEnquiryName("TMB.ENQ.CHL.ACCT.REG");
				genB0003Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.B0005: // 跨行金融帳戶資訊核驗(2566)
				t24ReqMessage.setEnquiryTag(true);
				t24ReqMessage.setEnquiryName("TMB.FEP.ACCT.PROVE.ENQ");
				genB0005Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1000:
			case T24Version.A1040: // 自行提款-台幣/外幣(IWD/IFW)
				// 2010/10/12 modify by Ruling SPEC 異動
				genA1000Body(t24ReqMessage.getTITABody(), CBSTxid);
				break;
			case T24Version.A1001: // 自行無卡提款(NWD)
				genA1001Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1010: // 跨行提款(台幣)
				genA1010Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1011: // 跨行無卡提款(2510)
				genA1011Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1030: // 跨區提款(台幣)台灣卡在海外分行提款
				genA1030Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1041: // 外幣提款(人民幣)
				genA1041Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1042: // 外幣無卡提款
				genA1042Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1110: // 跨行轉出
				genA1110Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1120: // 跨行轉出-ODT2521
				genA1120Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2110: // 跨行轉入(含虛擬帳號)
				genA2110Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1070:
			case T24Version.A1170: // 自行轉帳(含虛擬帳號)、跨行自轉
				// 2014/02/10 Modify by Ruling for 悠遊Debit卡
				if (!FEPChannel.SVCS.name().equals(getFeptxn().getFeptxnChannel())) {
					genA1070Body(t24ReqMessage.getTITABody());
				} else {
					genA1070BodyForSVCS(t24ReqMessage.getTITABody());
				}
				break;
			case T24Version.A1210:
			case T24Version.A1211: // 全國性繳費跨行轉出、行動金融信用卡eBILL繳費手續費優惠(2564)
				genA1210Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2210: // 全國性繳費跨行轉入
				genA2210Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1270:
			case T24Version.A1271: // 全國性繳費(含被代理)自行轉帳、行動金融信用卡eBILL繳費手續費優惠(2563)
				genA1270Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1310: // 轉出繳稅
				genA1310Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1050: // 代理提款/信用卡預借現金(IWD/CAV/CAM/CAA/CAJ)海外卡至台灣跨區提款(IFW)
				genA1050Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1051: // 代理跨行外幣提款(FAW)
				genA1051Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1052: // 代理無卡提款(NWD2510)
				genA1052Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1370: // 信用卡預借現金轉帳(IFT/ATF/BFT/C07)
				genA1370Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1470: // 國際卡餘額查詢(2411/2451)
				genA1470Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2910:
			case T24Version.A2920: // 現金加值GIFT卡/存款/繳信用卡款(CDF)
				// 2016/02/16 Modify by Ruling for 菓菜市場企業入金機:通路來源由ATM修正為BDM
				if (ATMTXCD.BDF.name().equals(getFeptxn().getFeptxnTxCode())) {
					if (StringUtils.isNoneBlank(getFeptxn().getFeptxnRemark())) {
						t24ReqMessage.getTITAHeader().setTiChnnCodeS("BDM");
					}
				}
				genA2910Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2930: // 跨行存款(ODR)
				genA2930Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1020: // 國際金融卡提款(台幣)
				genA1020Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1021: // 晶片金融卡跨國提款(2571/2572)
				genA1021Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2021: // 晶片金融卡跨國提款沖正(2572)
				genA2021Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1710: // 消費扣款轉出
				genA1710Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1711: // 跨國消費扣款(2545)
				genA1711Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2710: // 消費性扣款沖正/退貨交易
				genA2710Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2711: // 跨國消費扣款沖正(2546)
				genA2711Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1060: // 跨境電子支付(2555)
				genA1060Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A2060: // 跨境電子支付退貨(2556)
				genA2060Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.B4000: // 授權交易
				t24ReqMessage.setApplication("AC.LOCKED.EVENTS"); // B類交易的Application名稱要調整
				genB4000Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.B5000: // 預約交易設定(IPA)
				genB5000Body(t24ReqMessage.getTITABody());
				break;
			default:
				throw ExceptionUtil.createException("找不到CBSTXID:" + CBSTxid);
		}
	}

	private FEPReturnCode t24TimeOutProcess(String CBSTxid, boolean reverseTag, String msgToT24) {
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode = FEPReturnCode.ProgramException;
		Cbspend defCBSPEND = new Cbspend();
		try {
			defCBSPEND.setCbspendTxDate(getFeptxn().getFeptxnTxDate());
			// 港澳NCB:增加地區別及交易時間
			defCBSPEND.setCbspendZone(ZoneCode.TWN);
			defCBSPEND.setCbspendTxTime(getFeptxn().getFeptxnTxTime());
			defCBSPEND.setCbspendEjfno(getFeptxn().getFeptxnEjfno());
			defCBSPEND.setCbspendCbsTxCode(CBSTxid);
			defCBSPEND.setCbspendReverseFlag(DbHelper.toShort(reverseTag));
			defCBSPEND.setCbspendSubsys(getFeptxn().getFeptxnSubsys());
			defCBSPEND.setCbspendTbsdy(getFeptxn().getFeptxnTbsdy());
			defCBSPEND.setCbspendTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
			defCBSPEND.setCbspendSuccessFlag((short) 0);
			defCBSPEND.setCbspendResendCnt((short) 0);
			defCBSPEND.setCbspendAccType((short) AccountingType.UnKnow.getValue());
			// 修正代理提款(A1050)時寫入的交易金額=0
			if (getFeptxn().getFeptxnTxAmtAct().compareTo(BigDecimal.ZERO) != 0) {
				defCBSPEND.setCbspendTxAmt(getFeptxn().getFeptxnTxAmtAct());
			} else {
				defCBSPEND.setCbspendTxAmt(getFeptxn().getFeptxnTxAmt());
			}

			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
				defCBSPEND.setCbspendActno(getFeptxn().getFeptxnTroutActno());
				defCBSPEND.setCbspendIbBkno(getFeptxn().getFeptxnTrinBkno());
				defCBSPEND.setCbspendIbActno(getFeptxn().getFeptxnTrinActno());
			} else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
				defCBSPEND.setCbspendActno(getFeptxn().getFeptxnTrinActno());
				defCBSPEND.setCbspendIbBkno(getFeptxn().getFeptxnTroutBkno());
				defCBSPEND.setCbspendIbActno(getFeptxn().getFeptxnTroutActno());
			} else {
				defCBSPEND.setCbspendActno(getFeptxn().getFeptxnAtmno());
				defCBSPEND.setCbspendIbBkno(getFeptxn().getFeptxnTroutBkno());
				defCBSPEND.setCbspendIbActno(getFeptxn().getFeptxnTroutActno());
			}

			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
				defCBSPEND.setCbspendPcode(getFeptxn().getFeptxnPcode());
			} else {
				// 自行交易填入交易代號
				defCBSPEND.setCbspendPcode(getFeptxn().getFeptxnTxCode());
			}
			defCBSPEND.setCbspendTita(msgToT24);

			cbspendMapper.insertSelective(defCBSPEND);

			// TODO BY WJ 寫入Queue 暫時不寫
			// // 寫入 Queue
			// Message msg = new Message();
			// msg.Recoverable = true;
			// msg.Label = "CBSPEND";
			// msg.Body = getFeptxn().getFeptxnTxDate() + ":" + getFeptxn().getFeptxnEjfno().toString();
			// CBSPENDQueue.Send(msg);

			return FEPReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "T24TimeOutProcess");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	private void insertSMSMSG(String CBSTxid, T24PreClass t24ReqMessage) {
//		HashMap<String, String> CBS_TOTA = t24ReqMessage.getTOTATransResult();
//		Smsmsg defSMSMSG = new Smsmsg();
//		String telPhone = "";
//		try {
//			defSMSMSG.setSmsmsgTxDate(getFeptxn().getFeptxnTxDate());
//			defSMSMSG.setSmsmsgEjfno(getFeptxn().getFeptxnEjfno());
//
//			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
//				// 跨行交易填入STAN及PCODE
//				defSMSMSG.setSmsmsgStan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
//				defSMSMSG.setSmsmsgPcode(getFeptxn().getFeptxnPcode());
//			} else {
//				// 自行交易填入ATM電文代號
//				defSMSMSG.setSmsmsgPcode(getFeptxn().getFeptxnTxCode());
//			}
//
//			defSMSMSG.setSmsmsgTroutActno(getFeptxn().getFeptxnTroutActno());
//			defSMSMSG.setSmsmsgTxTime(getFeptxn().getFeptxnTxTime());
//			defSMSMSG.setSmsmsgBrno(getFeptxn().getFeptxnBrno());
//
//			if (CBS_TOTA.containsKey("T.MNEMONIC") && StringUtils.isNotBlank(CBS_TOTA.get("T.MNEMONIC"))) {
//				defSMSMSG.setSmsmsgIdno(CBS_TOTA.get("T.MNEMONIC"));
//			}
//
//			defSMSMSG.setSmsmsgCbsRrn(getFeptxn().getFeptxnCbsRrn());
//
//			if (CBS_TOTA.containsKey("T.EMAIL") && StringUtils.isNotBlank(CBS_TOTA.get("T.EMAIL"))) {
//				defSMSMSG.setSmsmsgEmail(CBS_TOTA.get("T.EMAIL"));
//			}
//
//			if (CBS_TOTA.containsKey("T.PHONE") && StringUtils.isNotBlank(CBS_TOTA.get("T.PHONE"))) {
//				telPhone = telPhone + CBS_TOTA.get("T.PHONE").replace("-", "").substring(3);
//			}
//			defSMSMSG.setSmsmsgNumber(telPhone);
//
//			// 2020/07/24 Modify by Ruling for 三萬元轉帳主動通知
//			if (CBS_TOTA.containsKey("T.DIGITAL.FG") && StringUtils.isNotBlank(CBS_TOTA.get("T.DIGITAL.FG"))) {
//				defSMSMSG.setSmsmsgDigitalFg(CBS_TOTA.get("T.DIGITAL.FG"));
//			}
//
//			if (CBS_TOTA.containsKey("T.EMAIL.SIGN") && StringUtils.isNotBlank(CBS_TOTA.get("T.EMAIL.SIGN"))) {
//				defSMSMSG.setSmsmsgEtouchFg(CBS_TOTA.get("T.EMAIL.SIGN"));
//			}
//
//			if (CBS_TOTA.containsKey("T.SMS.SIGN") && StringUtils.isNotBlank(CBS_TOTA.get("T.SMS.SIGN"))) {
//				defSMSMSG.setSmsmsgStouchFg(CBS_TOTA.get("T.SMS.SIGN"));
//			}
//
//			if (CBS_TOTA.containsKey("T.SMS") && StringUtils.isNotBlank(CBS_TOTA.get("T.SMS"))
//					&& CBS_TOTA.get("T.SMS").replace("-", "").length() > 3) {
//				defSMSMSG.setSmsmsgSmsPhone(CBS_TOTA.get("T.SMS").replace("-", "").substring(3));
//			}
//
//			if (CBS_TOTA.containsKey("T.STMT.211") && StringUtils.isNotBlank(CBS_TOTA.get("T.STMT.211"))) {
//				defSMSMSG.setSmsmsgNotifyFg(CBS_TOTA.get("T.STMT.211"));
//			}
//
//			defSMSMSG.setSmsmsgChannel(getFeptxn().getFeptxnChannel());
//			defSMSMSG.setSmsmsgZone(getFeptxn().getFeptxnZoneCode());
//			defSMSMSG.setSmsmsgTxCur(getFeptxn().getFeptxnTxCur());
//			defSMSMSG.setSmsmsgTxAmt(getFeptxn().getFeptxnTxAmt());
//			defSMSMSG.setSmsmsgTxCurAct(getFeptxn().getFeptxnTxCurAct());
//
//			// 扣款金額=提領金額加上手續費
//			// 三萬元轉帳主動通知：轉帳交易金額不加手續費
//			if (CurrencyType.TWD.toString().equals(getFeptxn().getFeptxnTxCur())) {
//				defSMSMSG.setSmsmsgTxAmtAct(getFeptxn().getFeptxnTxAmtAct());
//			} else {
//				defSMSMSG.setSmsmsgTxAmtAct(getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct()));
//			}
//
//			if (CBS_TOTA.containsKey("DR.CO.CODE") && StringUtils.isNotBlank(CBS_TOTA.get("DR.CO.CODE"))
//					&& CBS_TOTA.get("DR.CO.CODE").trim().length() >= 9) {
//				defSMSMSG.setSmsmsgBrno(CBS_TOTA.get("DR.CO.CODE").trim().substring(6, 15)); // 借方開戶行
//			}
//
//			defSMSMSG.setSmsmsgSend("N");
//			if (smsmsgMapper.insert(defSMSMSG) <= 0) {
//				getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//				this.logMessage(getLogContext());
//			}
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			getLogContext().setProgramName(ProgramName + "." + "insertSMSMSG");
//			// SendEMS(getLogContext());
//		}
	}

	private FEPReturnCode compareTOTA(String CBSTxid, T24PreClass t24ReqMessage) {
		// 2021-06-22 Richard add
		if (!FEPConfig.getInstance().isT24MethodCompareTOTAEnable()) {
			// 只是為了測試，如果disable則直接返回normal
			return FEPReturnCode.Normal;
		}
		HashMap<String, String> CBS_TOTA = t24ReqMessage.getTOTATransResult();
		String debitAcctNo = "";
		String creditAcctNo = "";
		BigDecimal debitAmount = new BigDecimal(0);
		BigDecimal creditAmount = new BigDecimal(0);

		try {
			getLogContext().setProgramName(ProgramName + "CompareTOTA");

			// 比對DEBIT_AMOUNT
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A2XX)比對
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A1XX)比對
			if (CBS_TOTA.containsKey("DEBIT.AMOUNT") && StringUtils.isNotBlank(CBS_TOTA.get("DEBIT.AMOUNT"))) {
				switch (CBSTxid) {
					case T24Version.A1000:
					case T24Version.A1001:
					case T24Version.A1010:
					case T24Version.A1011:
					case T24Version.A2110:
					case T24Version.A2210:
					case T24Version.A2710:
					case T24Version.A2711:
					case T24Version.A1110:
					case T24Version.A1270:
					case T24Version.A1310:
					case T24Version.A1051:
					case T24Version.A1710:
					case T24Version.A1210:
					case T24Version.A1211:
					case T24Version.A1271:
					case T24Version.A1711:
						debitAmount = getFeptxn().getFeptxnTxAmtAct();
						break;
					case T24Version.A2910:
					case T24Version.A2920:
					case T24Version.A2930:
					case T24Version.A1070:
					case T24Version.A1170:
					case T24Version.A1370:
					case T24Version.A1052:
					case T24Version.A1050:
						debitAmount = getFeptxn().getFeptxnTxAmt();

						break;
					case T24Version.A2021:
					case T24Version.A1021:
						debitAmount = MathUtil.roundUp(getFeptxn().getFeptxnTxAmt().multiply(getFeptxn().getFeptxnExrate()),
								0); // 四捨五入至元

						break;
					case T24Version.A2060:
					case T24Version.A1060:
						debitAmount = getFeptxn().getFeptxnTxAmtSet();

						break;
					case T24Version.A1040:
					case T24Version.A1041:
					case T24Version.A1042:
						if (CurrencyType.TWD.toString().equals(getFeptxn().getFeptxnTxCurAct())) {
							debitAmount = getFeptxn().getFeptxnTxAmtAct();
						} else {
							debitAmount = getFeptxn().getFeptxnTxAmt();
						}

						break;
					case T24Version.A1030:
						debitAmount = getFeptxn().getFeptxnTxAmtAct().subtract(getFeptxn().getFeptxnFeeCustpayAct());

						break;
					case T24Version.A1120:
						debitAmount = getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct());

						break;
					case T24Version.A1470:
						debitAmount = getFeptxn().getFeptxnFeeCustpayAct();

						break;
					case T24Version.A1020:
						if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
							debitAmount = getFeptxn().getFeptxnTxAmtAct();
						} else {
							debitAmount = getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct());
						}
						break;
				}

				if (new BigDecimal(CBS_TOTA.get("DEBIT.AMOUNT")).compareTo(debitAmount) != 0) {
					getLogContext().setRemark("T24.TOTA欄位[DEBIT.AMOUNT]比對不合");
					this.logMessage(getLogContext()); // 寫Log
					getLogContext().setMessageParm13("[DEBIT.AMOUNT]");
					TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
					return FEPReturnCode.CompareTOTANotMatch;
				}
			} else {
				getLogContext().setRemark("無法與T24.TITA比對，TOTA沒有[DEBIT.AMOUNT]欄位或欄位沒值");
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[DEBIT.AMOUNT]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 比對CREDIT_AMOUNT
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A2XX)比對
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A1XX)比對
			if (CBS_TOTA.containsKey("CREDIT.AMOUNT") && StringUtils.isNotBlank(CBS_TOTA.get("CREDIT.AMOUNT"))) {
				switch (CBSTxid) {
					case T24Version.A1000:
					case T24Version.A1001:
					case T24Version.A1010:
					case T24Version.A1011:
					case T24Version.A2110:
					case T24Version.A2210:
					case T24Version.A2710:
					case T24Version.A2711:
					case T24Version.A1110:
					case T24Version.A1270:
					case T24Version.A1310:
					case T24Version.A1710:
					case T24Version.A1210:
					case T24Version.A1211:
					case T24Version.A1271:
					case T24Version.A1711:
						creditAmount = getFeptxn().getFeptxnTxAmtAct();

						break;
					case T24Version.A2910:
					case T24Version.A2920:
					case T24Version.A2930:
					case T24Version.A1040:
					case T24Version.A1041:
					case T24Version.A1042:
					case T24Version.A1070:
					case T24Version.A1170:
					case T24Version.A1051:
					case T24Version.A1370:
					case T24Version.A1052:
					case T24Version.A1050:
						creditAmount = getFeptxn().getFeptxnTxAmt();

						break;
					case T24Version.A2021:
					case T24Version.A1021:
						creditAmount = MathUtil
								.roundUp(getFeptxn().getFeptxnTxAmt().multiply(getFeptxn().getFeptxnExrate()), 0); // 四捨五入至元

						break;
					case T24Version.A2060:
					case T24Version.A1060:
						creditAmount = getFeptxn().getFeptxnTxAmtSet();

						break;
					case T24Version.A1030:
						creditAmount = getFeptxn().getFeptxnTxAmtAct().subtract(getFeptxn().getFeptxnFeeCustpayAct());

						break;
					case T24Version.A1120:
						creditAmount = getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct());

						break;
					case T24Version.A1470:
						creditAmount = getFeptxn().getFeptxnFeeCustpayAct();

						break;
					case T24Version.A1020:
						if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
							creditAmount = getFeptxn().getFeptxnTxAmtAct();
						} else {
							creditAmount = getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct());
						}
						break;
				}

				if (new BigDecimal(CBS_TOTA.get("CREDIT.AMOUNT")).compareTo(creditAmount) != 0) {
					getLogContext().setRemark("T24.TOTA欄位[CREDIT.AMOUNT]比對不合");
					this.logMessage(getLogContext()); // 寫Log
					getLogContext().setMessageParm13("[CREDIT.AMOUNT]");
					TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
					return FEPReturnCode.CompareTOTANotMatch;
				}
			} else {
				getLogContext().setRemark("無法與T24.TITA比對，TOTA沒有[CREDIT.AMOUNT]欄位或欄位沒值");
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[CREDIT.AMOUNT]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 比對DEBIT.ACCT.NO
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A2XX)比對
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A1XX)比對
			if (CBS_TOTA.containsKey("DEBIT.ACCT.NO") && StringUtils.isNotBlank(CBS_TOTA.get("DEBIT.ACCT.NO"))
					&& CBS_TOTA.get("DEBIT.ACCT.NO").trim().length() >= 14) {
				switch (CBSTxid) {
					case T24Version.A1000:
					case T24Version.A1001:
					case T24Version.A1010:
					case T24Version.A1011:
					case T24Version.A1040:
					case T24Version.A1041:
					case T24Version.A1042:
					case T24Version.A1030:
					case T24Version.A1070:
					case T24Version.A1170:
					case T24Version.A1210:
					case T24Version.A1270:
					case T24Version.A1470:
					case T24Version.A1020:
					case T24Version.A1710:
					case T24Version.A1211:
					case T24Version.A1271:
					case T24Version.A1021:
					case T24Version.A1711:
					case T24Version.A1060:

						if (CBSTxid.equals(T24Version.A1070)
								&& FEPChannel.SVCS.toString().equals(getFeptxn().getFeptxnChannel())) {
							if (SVCSTXCD.SD2.toString().equals(getFeptxn().getFeptxnTxCode())) {
								// /*自動加值*/
								debitAcctNo = getFeptxn().getFeptxnMajorActno().substring(2, 16);
							} else {
								// /*餘額轉置*/
								if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId())) {
									switch (getFeptxn().getFeptxnNoticeId().trim()) {
										case TOSVCSType.BalanceTransNegative:
										case TOSVCSType.MonthlyRefundNegative:
											debitAcctNo = getFeptxn().getFeptxnMajorActno().substring(2, 16);
											break;
										case TOSVCSType.BalanceTransPositive:
										case TOSVCSType.MonthlyRefundPositive:
											debitAcctNo = ATMPConfig.getInstance().getSVCSOutActno();
											break;
										case TOSVCSType.AutoLoadRefund:
											debitAcctNo = ATMPConfig.getInstance().getSVCSIntActno();
											break;
									}
								}
							}
						} else {
							debitAcctNo = getFeptxn().getFeptxnTroutActno().substring(2, 16);
						}
						break;
					case T24Version.A2021:
					case T24Version.A2060:
					case T24Version.A2110:
					case T24Version.A2210:
					case T24Version.A2710:
					case T24Version.A2711:
					case T24Version.A1051:
					case T24Version.A1370:
					case T24Version.A1052:
						debitAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsSupActno();

						break;
					case T24Version.A2910:
					case T24Version.A2920:
					case T24Version.A2930:
						if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && "2".equals(getFeptxn().getFeptxnNoticeId().trim())) {
							debitAcctNo = ATMPConfig.getInstance().getOutATMIntActno();
						} else {
							if (getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
								debitAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
										+ getFeptxn().getFeptxnAtmBrno();
							} else {
								debitAcctNo = "";
							}
						}

						break;
					case T24Version.A1110:
					case T24Version.A1310:
						if (!BINPROD.Credit.equals(getFeptxn().getFeptxnTroutKind())
								&& !BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())) {
							/// * 轉出帳號為一般帳號 */
							debitAcctNo = getFeptxn().getFeptxnTroutActno().substring(2, 16);
						} else {
							/// * 信用卡預現專戶 */
							debitAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsIntActno();
						}

						break;
					case T24Version.A1050:
						if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())
								&& CurrencyType.TWD.toString().equals(getFeptxn().getFeptxnTxCurSet())) {
							debitAcctNo = getFeptxn().getFeptxnCbsFeeActno();
						} else {
							debitAcctNo = getFeptxn().getFeptxnCbsSupActno();
						}

						break;
					case T24Version.A1120:
						debitAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsIntActno();
						break;
				}

				// 2020/02/17 Modify by Ruling for 信用卡預借現金轉帳(C07)交易，因資料庫存14位+2位空白，要Trim掉再比對
				if (!CBS_TOTA.get("DEBIT.ACCT.NO").trim().equals(debitAcctNo.trim())) {
					if ((CBSTxid.equals(T24Version.A1050)) || (CBSTxid.equals(T24Version.A1070)
							&& FEPChannel.SVCS.toString().equals(getFeptxn().getFeptxnChannel()))) {
						if (CBS_TOTA.containsKey("ACCT.TXN.DR") && StringUtils.isNotBlank(CBS_TOTA.get("ACCT.TXN.DR"))
								&& CBS_TOTA.get("ACCT.TXN.DR").trim().length() >= 16) {
							if (!CBS_TOTA.get("ACCT.TXN.DR").trim().equals(getFeptxn().getFeptxnTroutActno().trim())) {
								getLogContext().setRemark("T24.TOTA欄位[ACCT.TXN.DR]比對不合");
								this.logMessage(getLogContext()); // 寫Log
								getLogContext().setMessageParm13("[ACCT.TXN.DR]");
								TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch,
										getLogContext()); // 寄EMail送EMS
								return FEPReturnCode.CompareTOTANotMatch;
							}
						} else {
							getLogContext().setRemark("無法與T24.TITA比對，TOTA沒有[ACCT.TXN.DR]欄位或欄位沒值或欄位長度不足16位");
							this.logMessage(getLogContext()); // 寫Log
							getLogContext().setMessageParm13("[ACCT.TXN.DR]");
							TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
							return FEPReturnCode.CompareTOTANotMatch;
						}

					} else if (CBSTxid.equals(T24Version.A1040) || CBSTxid.equals(T24Version.A1041)) {
						// 2020/02/18 Modify by Ruling for
						// 組存戶提領外幣送主機會轉換為交易幣別的子帳號，再回給FEP，故調整改比T.TX.O.ACCT.D欄位
						if (CBS_TOTA.containsKey("T.TX.O.ACCT.D")
								&& StringUtils.isNotBlank(CBS_TOTA.get("T.TX.O.ACCT.D"))
								&& CBS_TOTA.get("T.TX.O.ACCT.D").trim().length() >= 14) {
							if (!CBS_TOTA.get("T.TX.O.ACCT.D").trim().equals(debitAcctNo.trim())) {
								getLogContext().setRemark("T24.TOTA欄位[T.TX.O.ACCT.D]比對不合");
								this.logMessage(getLogContext()); // 寫Log
								getLogContext().setMessageParm13("[T.TX.O.ACCT.D]");
								TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch,
										getLogContext()); // 寄EMail送EMS
								return FEPReturnCode.CompareTOTANotMatch;
							}
						} else {
							getLogContext().setRemark("無法與T24.TITA比對，TOTA沒有[T.TX.O.ACCT.D]欄位或欄位沒值或欄位長度不足14位");
							this.logMessage(getLogContext()); // 寫Log
							getLogContext().setMessageParm13("[T.TX.O.ACCT.D]");
							TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
							return FEPReturnCode.CompareTOTANotMatch;
						}

					} else {
						getLogContext().setRemark("T24.TOTA欄位[DEBIT.ACCT.NO]比對不合");
						this.logMessage(getLogContext()); // 寫Log
						getLogContext().setMessageParm13("[DEBIT.ACCT.NO]");
						TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
						return FEPReturnCode.CompareTOTANotMatch;
					}
				}
			} else {
				getLogContext().setRemark("無法與T24.TITA比對，TOTA沒有[DEBIT.ACCT.NO]欄位或欄位沒值或欄位長度不足14位");
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[DEBIT.ACCT.NO]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 比對CREDIT_ACCT_NO
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A2XX)比對
			// 線上比對主機回應電文帳號及金額資料：增加入帳類(A1XX)比對
			if (CBS_TOTA.containsKey("CREDIT.ACCT.NO") && StringUtils.isNotBlank(CBS_TOTA.get("CREDIT.ACCT.NO"))
					&& CBS_TOTA.get("CREDIT.ACCT.NO").trim().length() >= 14) {
				switch (CBSTxid) {
					case T24Version.A1000:
					case T24Version.A1001:
					case T24Version.A1052:
					case T24Version.A1050:
						// 借用 FEPTXN_NOTICE_ID 欄位存放 ATM 行外記號
						if ("2".equals(getFeptxn().getFeptxnNoticeId())) {
							creditAcctNo = ATMPConfig.getInstance().getOutATMIntActno();
						} else {
							creditAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
									+ getFeptxn().getFeptxnAtmBrno();
						}

						break;
					case T24Version.A1010:
					case T24Version.A1011:
					case T24Version.A2930:
					case T24Version.A1110:
					case T24Version.A1120:
					case T24Version.A1210:
					case T24Version.A1310:
					case T24Version.A1470:
					case T24Version.A1020:
					case T24Version.A1710:
					case T24Version.A1211:
					case T24Version.A1021:
					case T24Version.A1711:
					case T24Version.A1060:
						creditAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsSupActno();

						break;
					case T24Version.A2021:
					case T24Version.A2060:
					case T24Version.A2710:
					case T24Version.A2711:
						creditAcctNo = getFeptxn().getFeptxnTroutActno().substring(2, 16);

						break;
					case T24Version.A2110:
					case T24Version.A2210:
					case T24Version.A2910:
					case T24Version.A2920:
					case T24Version.A1070:
					case T24Version.A1170:
					case T24Version.A1270:
					case T24Version.A1370:
					case T24Version.A1271:

						if (CBSTxid.equals(T24Version.A1070)
								&& FEPChannel.SVCS.toString().equals(getFeptxn().getFeptxnChannel())) {
							if (SVCSTXCD.SD2.toString().equals(getFeptxn().getFeptxnTxCode())) {
								// /*自動加值*/
								creditAcctNo = getFeptxn().getFeptxnTrinActno();
							} else {
								// /*餘額轉置*/
								if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId())) {
									switch (getFeptxn().getFeptxnNoticeId().trim()) {
										case TOSVCSType.BalanceTransNegative:
										case TOSVCSType.MonthlyRefundNegative:
											creditAcctNo = ATMPConfig.getInstance().getSVCSOutActno();
											break;
										case TOSVCSType.BalanceTransPositive:
										case TOSVCSType.MonthlyRefundPositive:
										case TOSVCSType.AutoLoadRefund:
											creditAcctNo = getFeptxn().getFeptxnMajorActno().substring(2, 16);
											break;
									}
								}
							}
						} else {
							if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
								if (INBKConfig.getInstance().getEBillTrinActNo().equals(getFeptxn().getFeptxnTrinActno())) {
									creditAcctNo = INBKConfig.getInstance().getEBillT24ActNo();
								} else {
									creditAcctNo = getFeptxn().getFeptxnTrinActno().substring(2, 16);
								}
							} else {
								switch (getFeptxn().getFeptxnTrinKind()) {
									case BINPROD.Credit:
									case BINPROD.Combo:
										creditAcctNo = "CRD" + getFeptxn().getFeptxnTrinActno();
										break;
									case BINPROD.Gift:
										creditAcctNo = "GIF" + getFeptxn().getFeptxnTrinActno();
										break;
								}
							}
						}

						break;
					case T24Version.A1040:
					case T24Version.A1041:
					case T24Version.A1042:
					case T24Version.A1051:
						creditAcctNo = getFeptxn().getFeptxnTxCur().trim()
								+ getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13)
								+ getFeptxn().getFeptxnAtmBrno();

						break;
					case T24Version.A1030:
						switch (getFeptxn().getFeptxnAtmZone()) {
							case ZoneCode.HKG:
								/// * 台灣卡至香港分行跨區提款 */
								creditAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsSupActno();
								break;
							case ZoneCode.MAC:
								/// * 台灣卡至澳門分行跨區提款 */
								creditAcctNo = getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(0, 7) + "2"
										+ getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(8, 16);
								break;
						}
						break;
				}

				// 信用卡預借現金轉帳(C07)交易，因資料庫存14位+2位空白，要Trim掉再比對
				if (!CBS_TOTA.get("CREDIT.ACCT.NO").trim().equals(creditAcctNo.trim())) {
					// A2110/A2210/A2910/A2920 比對不合時需再比對ACCT.TXN.CR
					if (CBSTxid.equals(T24Version.A2110) || CBSTxid.equals(T24Version.A2210)
							|| CBSTxid.equals(T24Version.A2910) || CBSTxid.equals(T24Version.A2920)
							|| CBSTxid.equals(T24Version.A1070) || CBSTxid.equals(T24Version.A1170)
							|| CBSTxid.equals(T24Version.A1270) || CBSTxid.equals(T24Version.A1370)
							|| CBSTxid.equals(T24Version.A1271) || (CBSTxid.equals(T24Version.A1070)
									&& FEPChannel.SVCS.toString().equals(getFeptxn().getFeptxnChannel()))) {
						if (CBS_TOTA.containsKey("ACCT.TXN.CR") && StringUtils.isNotBlank(CBS_TOTA.get("ACCT.TXN.CR"))
								&& CBS_TOTA.get("ACCT.TXN.CR").trim().length() >= 16) {
							if (!CBS_TOTA.get("ACCT.TXN.CR").trim().equals(getFeptxn().getFeptxnTrinActno().trim())) {
								getLogContext().setRemark("T24.TOTA欄位[ACCT.TXN.CR]比對不合");
								this.logMessage(getLogContext()); // 寫Log
								getLogContext().setMessageParm13("[ACCT.TXN.CR]");
								TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch,
										getLogContext()); // 寄EMail送EMS
								return FEPReturnCode.CompareTOTANotMatch;
							}
						} else {
							getLogContext().setRemark("無法與T24.TITA比對，TOTA沒有[ACCT.TXN.CR]欄位或欄位沒值或欄位長度不足16位");
							this.logMessage(getLogContext()); // 寫Log
							getLogContext().setMessageParm13("[ACCT.TXN.CR]");
							TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
							return FEPReturnCode.CompareTOTANotMatch;
						}
					} else {
						getLogContext().setRemark("T24.TOTA欄位[CREDIT.ACCT.NO]比對不合");
						this.logMessage(getLogContext()); // 寫Log
						getLogContext().setMessageParm13("[CREDIT.ACCT.NO]");
						TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
						return FEPReturnCode.CompareTOTANotMatch;
					}
				}
			} else {
				getLogContext().setRemark("無法與T24.TITA比對，TOTA沒有[CREDIT.ACCT.NO]欄位或欄位沒值或欄位長度不足14位");
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[CREDIT.ACCT.NO]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 不影響交易仍Normal
			return FEPReturnCode.Normal;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "CompareTOTA");
			// SendEMS(getLogContext());
			// TITA與TOTA比對有Exception，為了不影響交易仍回Normal
			return FEPReturnCode.Normal;
		}

	}

	private FEPReturnCode updateFEPTXNbyCBSTxid(String CBSTxid, T24PreClass t24ReqMessage) {

		HashMap<String, String> CBS_TOTA = t24ReqMessage.getTOTATransResult();
		Zone defZONE = new Zone();
		Bsdays defBSDAYS = new Bsdays();
		try {
			if (CBSTxid.substring(0, 1).equals("A")) {
				// SPEC異動：拿掉非信用卡的邏輯
				// SPEC異動
				// SPEC異動
				// 港澳NCB
				// 跨行提領外幣：不能用T24的餘額回寫可用餘額(FEPTXN_BALA)
				// 跨行無卡提款：不能用T24的餘額回寫可用餘額(FEPTXN_BALA)
				// 帳務類交易
				if ((!CBSTxid.equals(T24Version.A1050) && !CBSTxid.equals(T24Version.A1051)
						&& !CBSTxid.equals(T24Version.A1052))
						&& (CBSTxid.substring(0, 2).equals("A1") || CBSTxid.equals(T24Version.A2920))
						&& StringUtils.isBlank(getFeptxn().getFeptxnTroutKind())) {
					if (!T24Version.A1020.equals(getFeptxn().getFeptxnCbsTxCode())
							&& !T24Version.A1021.equals(getFeptxn().getFeptxnCbsTxCode())
							&& !T24Version.A1060.equals(getFeptxn().getFeptxnCbsTxCode())
							&& !T24Version.A2060.equals(getFeptxn().getFeptxnCbsTxCode())) {// 非國際卡交易
						getFeptxn().setFeptxnFeeCustpayAct(new BigDecimal(CBS_TOTA.get("CAP.CHG.AMT"))); // 帳戶幣別手續費
					}
					// 國際卡餘額查詢(2451)原存行手續費不能蓋掉
					if (CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())
							&& !T24Version.A1470.equals(getFeptxn().getFeptxnCbsTxCode())) {
						getFeptxn().setFeptxnFeeCustpay(new BigDecimal(CBS_TOTA.get("CAP.CHG.AMT")));// 提領幣別手續費
					}
					// 身心障礙跨行提款手續費減免
					if (T24Version.A1010.equals(getFeptxn().getFeptxnCbsTxCode())
							&& getFeptxn().getFeptxnFeeCustpayAct().compareTo(BigDecimal.ZERO) == 0) {
						if (CBS_TOTA.containsKey("T.DISABILITY") && StringUtils.isNotBlank(CBS_TOTA.get("T.DISABILITY"))
								&& CBS_TOTA.get("T.DISABILITY").equals("Y")) {
							getFeptxn().setFeptxnNoticeId("0101");
						}
					}
					// 修正A2920交易時，以下兩個欄位為NULL造成exception
					if (CBS_TOTA.containsKey("AVBAL.AFT.TXN")) {
						//getFeptxn().setFeptxnBala(new BigDecimal(CBS_TOTA.get("AVBAL.AFT.TXN"))); // 可用餘額
						getFeptxn().setFeptxnBala(new BigDecimal(CBS_TOTA.get("AVBAL.AFT.TXN"))); // 可用餘額
					}
					if (CBS_TOTA.containsKey("BAL.AFT.TXN")) {
						getFeptxn().setFeptxnBalb(new BigDecimal(CBS_TOTA.get("BAL.AFT.TXN"))); // 帳戶餘額
					}
				}

				// 卡國際提款(2450)/餘額查詢(2451)，將帳戶餘額傳給財金
				// 卡EMV國際提款(2630)/餘額查詢(2631)，將帳戶餘額傳給財金
				if ((T24Version.A1470.equals(getFeptxn().getFeptxnCbsTxCode())
						|| T24Version.A1020.equals(getFeptxn().getFeptxnCbsTxCode()))
						&& StringUtils.isNotBlank(getFeptxn().getFeptxnTroutKind())) {
					if (!T24Version.A1020.equals(getFeptxn().getFeptxnCbsTxCode())) {
						if (CBS_TOTA.containsKey("CAP.CHG.AMT")) {
							getFeptxn().setFeptxnFeeCustpayAct(new BigDecimal(CBS_TOTA.get("CAP.CHG.AMT"))); // 提領幣別手續費
						}
					}
					if (CBS_TOTA.containsKey("AVBAL.AFT.TXN")) {
						//getFeptxn().setFeptxnBala(new BigDecimal(CBS_TOTA.get("AVBAL.AFT.TXN"))); // 可用餘額
						getFeptxn().setFeptxnBala(new BigDecimal(CBS_TOTA.get("AVBAL.AFT.TXN"))); // 可用餘額
					}
					if (CBS_TOTA.containsKey("BAL.AFT.TXN")) {
						getFeptxn().setFeptxnBalb(new BigDecimal(CBS_TOTA.get("BAL.AFT.TXN"))); // 帳戶餘額
					}
				}

				// 長度不足16位時再左補0
				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
					getFeptxn().setFeptxnTrinActnoActual(
							StringUtils.leftPad(CBS_TOTA.get("CREDIT.ACCT.NO").trim(), 16, '0'));
				}

				// 港澳NCB
				if (CBSTxid.equals(T24Version.A1050) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmno())) {
					if (CBS_TOTA.containsKey("DR.CO.CODE") && CBS_TOTA.get("DR.CO.CODE").trim().length() >= 9) {
						getFeptxn().setFeptxnVirBrno(CBS_TOTA.get("DR.CO.CODE").trim().substring(6, 9)); // 借方開戶行
					}

					if (CBS_TOTA.containsKey("CR.CO.CODE") && CBS_TOTA.get("CR.CO.CODE").trim().length() >= 9) {
						getFeptxn().setFeptxnTrinBrno(CBS_TOTA.get("CR.CO.CODE").trim().substring(6, 9));// 貸方開戶行
					}
				} else {
					getFeptxn().setFeptxnCbsValueDate(CBS_TOTA.get("VALUE.DATE"));// 起息日
					getFeptxn().setFeptxnCbsTxTime(CBS_TOTA.get("T24.TXTIME"));
					if (CBS_TOTA.containsKey("DR.CO.CODE") && CBS_TOTA.get("DR.CO.CODE").trim().length() >= 9) {
						getFeptxn().setFeptxnBrno(CBS_TOTA.get("DR.CO.CODE").substring(6, 9));// 借方開戶行
					}
					if (CBS_TOTA.containsKey("CR.CO.CODE") && CBS_TOTA.get("CR.CO.CODE").trim().length() >= 9) {
						if (!T24Version.A2710.equals(getFeptxn().getFeptxnCbsTxCode())
								&& !T24Version.A2711.equals(getFeptxn().getFeptxnCbsTxCode())
								&& !T24Version.A2021.equals(getFeptxn().getFeptxnCbsTxCode())) {
							getFeptxn().setFeptxnTrinBrno(CBS_TOTA.get("CR.CO.CODE").substring(6, 9));// 貸方開戶行
						} else {
							getFeptxn().setFeptxnBrno(CBS_TOTA.get("CR.CO.CODE").substring(6, 9)); // 借方開戶行
						}
					}

					if (CBS_TOTA.containsKey("DR.PERM.BRH")) {
						getFeptxn().setFeptxnDept(CBS_TOTA.get("DR.PERM.BRH")); // 借方績效行
					}

					// 新增晶片金融卡跨國提款及消費扣款交易
					if (CBS_TOTA.containsKey("CR.PERM.BRH")) {
						if (!T24Version.A2710.equals(getFeptxn().getFeptxnCbsTxCode())
								&& !T24Version.A2711.equals(getFeptxn().getFeptxnCbsTxCode())
								&& !T24Version.A2021.equals(getFeptxn().getFeptxnCbsTxCode())) {
							getFeptxn().setFeptxnTrinDept(CBS_TOTA.get("CR.PERM.BRH")); // 貸方績效行
						} else {
							getFeptxn().setFeptxnDept(CBS_TOTA.get("CR.PERM.BRH")); // 借方績效行
						}
					}
				}

				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
						&& getFeptxn().getFeptxnNpsClr() != null // 2021-06-16 Richard add
						&& FeptxnNPSCLR.TRIn.getValue() != getFeptxn().getFeptxnNpsClr().intValue()) {
					getFeptxn().setFeptxnTxBrno(getFeptxn().getFeptxnBrno());
					getFeptxn().setFeptxnTxDept(getFeptxn().getFeptxnDept());
					getFeptxn().setFeptxnTxActno(getFeptxn().getFeptxnTroutActno());
				} else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
					getFeptxn().setFeptxnTxBrno(getFeptxn().getFeptxnTrinBrno());
					getFeptxn().setFeptxnTxDept(getFeptxn().getFeptxnTrinDept());
					getFeptxn().setFeptxnTxActno(getFeptxn().getFeptxnTrinActno());
				}

				// 跨行轉帳小額交易手續費調降
				// 轉帳繳納口罩費用免手續費：因跨行轉帳小額交易手續費已生效，拿掉判斷小額手續費生效日的程式
				if (CBSTxid.equals(T24Version.A1110) || CBSTxid.equals(T24Version.A1170)
						|| CBSTxid.equals(T24Version.A2110)) {
					// 小額跨轉優惠記號
					if (CBS_TOTA.containsKey("T.ONETIME.FREE")
							&& StringUtils.isNotBlank(CBS_TOTA.get("T.ONETIME.FREE"))) {
						if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
							// 修改 for 轉帳繳納口罩費用免手續費
							if (StringUtils.isBlank(getFeptxn().getFeptxnBenefit())) {
								getFeptxn().setFeptxnBenefit(CBS_TOTA.get("T.ONETIME.FREE"));
							}
						}
					}
					// 轉出或轉入帳號是個人或企業
					if (CBS_TOTA.containsKey("T.ATM.SECTOR") && StringUtils.isNotBlank(CBS_TOTA.get("T.ATM.SECTOR"))) {
						if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
							if ("P".equals(CBS_TOTA.get("T.ATM.SECTOR"))) {
								getFeptxn().setFeptxnAcctSup(StringUtils.rightPad("1", 12, ' ')); // 個人戶
							} else {
								getFeptxn().setFeptxnAcctSup(StringUtils.rightPad(" ", 12, ' ')); // 法人戶
							}
						}
					}
				}
				// 修改by Sarah, 自行換日
				if ((!CBSTxid.equals(T24Version.A1030)
						&& CBS_TOTA.get("BOOKING.DATE").compareTo(getFeptxn().getFeptxnTbsdy()) > 0)
						|| (CBSTxid.equals(T24Version.A1030)
								&& CBS_TOTA.get("BOOKING.DATE").compareTo(getFeptxn().getFeptxnTbsdyAct()) > 0)) {
					/// * 台灣卡跨區交易 */
					if (CBSTxid.equals(T24Version.A1030)) {
						getFeptxn().setFeptxnTbsdyAct(CBS_TOTA.get("BOOKING.DATE"));/// * 卡片所在地區營業日 */
					} else {
						getFeptxn().setFeptxnTbsdy(CBS_TOTA.get("BOOKING.DATE"));
					}

					defZONE = getZoneByZoneCode(ATMZone.TWN.toString());
					if (CBS_TOTA.get("BOOKING.DATE").compareTo(defZONE.getZoneTbsdy()) > 0) {
						defZONE.setZoneLlbsdy(defZONE.getZoneLbsdy());// 上營業日搬入上上營業日
						defZONE.setZoneLbsdy(defZONE.getZoneTbsdy()); // 本營業日搬入上營業日
						defZONE.setZoneTbsdy(CBS_TOTA.get("BOOKING.DATE"));// 自行本營業日
						defZONE.setZoneNbsdy(SysStatus.getPropertyValue().getSysstatNbsdyFisc()); // 取得財金次營業日

						// 補星期幾和FEP換日時間
						defBSDAYS.setBsdaysZoneCode(ATMZone.TWN.toString());
						defBSDAYS.setBsdaysDate(defZONE.getZoneTbsdy());
						Bsdays queryBsdays = bsdaysMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(),
								defBSDAYS.getBsdaysDate());
						if (queryBsdays != null) {
							defZONE.setZoneWeekno(defBSDAYS.getBsdaysWeekno());
						}
						defZONE.setZoneChgdayTime(Integer.parseInt(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
						defZONE.setZoneChgday(DbHelper.toShort(false));
						defZONE.setZoneCbsMode((short) 1);
						zoneMapper.updateByPrimaryKeySelective(defZONE);

						// 需顯示換日成功訊息於EMS
						// 港澳NCB:訊息內容加台灣
						getLogContext().setpCode(getFeptxn().getFeptxnPcode());
						getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
						getLogContext().setFiscRC(NormalRC.FISC_OK);
						getLogContext().setMessageGroup("1");
						getLogContext().setMessageParm13("台灣自行");
						getLogContext().setMessageParm14(defZONE.getZoneTbsdy());

						getLogContext().setProgramName(ProgramName);
						getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(
								FEPReturnCode.CBSBusinessDateChangeToYMD, getLogContext()));

						logMessage(Level.DEBUG, getLogContext());
					}
				}
			} else {
				// 非帳務類交易
				ArrayList<HashMap<String, String>> enquire_TOTA = t24ReqMessage.getTotaEnquiryContents();
				switch (CBSTxid) {
					case T24Version.B0001: // 帳戶餘額查詢(IIQ/IQ2)
						//getFeptxn().setFeptxnBala(new BigDecimal(enquire_TOTA.get(0).get("AVAILABLE.BAL")));// 可用餘額
						getFeptxn().setFeptxnBala(new BigDecimal(enquire_TOTA.get(0).get("AVAILABLE.BAL")));// 可用餘額
						getFeptxn().setFeptxnBalb(new BigDecimal(enquire_TOTA.get(0).get("WORKING.BAL")));// 帳戶餘額
						break;
					case T24Version.B0003: // ATM調帳號(IAC)
						break;
					case T24Version.B4000: // 授權交易
						getFeptxn().setFeptxnCbsRrn(t24ReqMessage.getTOTATransResult().get("transactionId"));
						break;
					case T24Version.B0005: // 跨行金融帳戶核驗(2566)
						getFeptxn().setFeptxnRemark(
								enquire_TOTA.get(0).get("E.CIF.CHK.STATUS") + enquire_TOTA.get(0).get("E.ACCT.CHK.STATUS")
										+ enquire_TOTA.get(0).get("E.ACCT.TYPE.STATUS"));

						// 寫入帳務行
						if (enquire_TOTA.get(0).containsKey("E.CO.CODE")
								&& StringUtils.isNotBlank(enquire_TOTA.get(0).get("E.CO.CODE"))
								&& enquire_TOTA.get(0).get("E.CO.CODE").length() >= 7) {
							getFeptxn().setFeptxnBrno(enquire_TOTA.get(0).get("E.CO.CODE").substring(
									enquire_TOTA.get(0).get("E.CO.CODE").trim().length() - 3,
									enquire_TOTA.get(0).get("E.CO.CODE").trim().length()));
							FEPReturnCode rtnCode = checkCompany(getFeptxn().getFeptxnBrno());
							if (rtnCode != FEPReturnCode.Normal) {
								getLogContext().setRemark("帳務行檢核Company失敗，將rtnCode轉換為CBSResponseFormatError");
								this.logMessage(getLogContext());
								return FEPReturnCode.CBSResponseFormatError;
							}
						} else {
							if ("99".equals(enquire_TOTA.get(0).get("E.ACCT.CHK.STATUS"))) {
								// 帳號核驗結果=99，未回傳帳務行避免APTOT的帳務行為空白寫入888
								getFeptxn().setFeptxnBrno(String.valueOf(ATMPConfig.getInstance().getHeadOffice()));
							} else {
								// 與GL拋帳有關沒帶回前端錯誤
								getLogContext().setRemark("B0005下行電文，無帳務行(E.CO.CODE)欄位或值為空白或長度小於7位");
								this.logMessage(getLogContext());
								return FEPReturnCode.CBSResponseFormatError;
							}
						}

						// 寫入績效單位
						if (enquire_TOTA.get(0).containsKey("E.DEPT.CODE")
								&& StringUtils.isNotBlank(enquire_TOTA.get(0).get("E.DEPT.CODE"))) {
							getFeptxn().setFeptxnDept(enquire_TOTA.get(0).get("E.DEPT.CODE").trim());
						} else {
							if (!"99".equals(enquire_TOTA.get(0).get("E.ACCT.CHK.STATUS"))) {
								// 與GL拋帳有關沒帶回前端錯誤
								getLogContext().setRemark("B0005下行電文，無績效單位(E.DEPT.CODE)欄位或值為空白");
								this.logMessage(getLogContext());
								return FEPReturnCode.CBSResponseFormatError;
							}
						}
						break;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "UpdateFEPTXNbyCBSTxid");
			// SendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}

		return FEPReturnCode.Normal;
	}

	private void genB0001Body(HashMap<String, String> titaBody) {
		T24TITAB0000 titaB0000 = new T24TITAB0000();
		// 利用General class將值正確填入
		titaB0000.setAcctNo(getFeptxn().getFeptxnTroutActno().substring(2, 16));
		titaB0000.setCURRENCY(getFeptxn().getFeptxnTxCurAct());
		// 利用General class的GenDictionary建立T24的titaBody
		titaB0000.genDictionary(titaBody);
	}

	private void genB0002Body(HashMap<String, String> titaBody) {
		T24TITAB0000 titaB0000 = new T24TITAB0000();
		// 利用General class將值正確填入
		if ("2".equals(getFeptxn().getFeptxnRemark().substring(0, 1))) {
			// 查詢方式 by 帳號
			titaB0000.setEnqAcType("SINGLE");// 查詢帳號種類=單一帳號
			titaB0000.setEnqAcctId(getFeptxn().getFeptxnTroutActno().substring(2, 16));
		} else {
			// 查詢方式 by 身份證號
			titaB0000.setEnqAcType("ALL");// 查詢帳號種類=所有帳號
			titaB0000.setEnqCustId(getFeptxn().getFeptxnIdno());
		}
		// 利用General class的GenDictionary建立T24的titaBody
		titaB0000.genDictionary(titaBody);
	}

	private void genB0003Body(HashMap<String, String> titaBody) {
		T24TITAB0000 titaB0000 = new T24TITAB0000();
		// 利用General class將值正確填入
		// 掌靜脈調帳號(PAC)
		if (ATMTXCD.PAC.toString().equals(getFeptxn().getFeptxnTxCode())) {
			titaB0000.setCustomerNo(getFeptxn().getFeptxnIdno());
			titaB0000.setCURRENCY("ALL");
		} else {
			titaB0000.setAcctNo(getFeptxn().getFeptxnTroutActno().substring(2, 16));
			titaB0000.setCURRENCY("ALL");
		}
		titaB0000.genDictionary(titaBody);
	}

	private void genB0005Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAB0000 titaB0000 = new T24TITAB0000();
		Vatxn defVATXN = new Vatxn();
		defVATXN.setVatxnTxDate(getFeptxn().getFeptxnTxDate());
		defVATXN.setVatxnEjfno(getFeptxn().getFeptxnEjfno());
		Vatxn queryVatxn = vatxnMapper.selectByPrimaryKey(defVATXN.getVatxnTxDate(), defVATXN.getVatxnEjfno());
		if (queryVatxn == null) {
			String remark = "GenB0005Body 讀不到VATXN, VATXN_TX_DATE=" + defVATXN.getVatxnTxDate() + " VATXN_EJFNO="
					+ defVATXN.getVatxnEjfno().toString();
			getLogContext().setRemark(remark);
			this.logMessage(getLogContext());
			throw ExceptionUtil.createException(remark);
		}
		defVATXN = queryVatxn;
		// 利用General class將值正確填入
		titaB0000.setEnqAcctNo(defVATXN.getVatxnTroutActno().substring(2, 16));
		titaB0000.setEnqChkType(defVATXN.getVatxnType()); // 交易類別
		titaB0000.setEnqChkItem(defVATXN.getVatxnItem()); // 核驗項目
		// 身份證字號
		if (StringUtils.isNotBlank(defVATXN.getVatxnIdno())) {
			titaB0000.setEnqTMnemonic(defVATXN.getVatxnIdno());
		} else {
			titaB0000.setEnqTMnemonic("");
		}
		// 出生年月日
		if (StringUtils.isNotBlank(defVATXN.getVatxnBirthday())) {
			titaB0000.setEnqDateOfBirth(defVATXN.getVatxnBirthday());
		} else {
			titaB0000.setEnqDateOfBirth("");
		}
		// 手機號碼
		if (StringUtils.isNotBlank(defVATXN.getVatxnMobile())) {
			titaB0000.setEnqSms_1(defVATXN.getVatxnMobile());
		} else {
			titaB0000.setEnqSms_1("");
		}
		// 住家電話
		if (StringUtils.isNotBlank(defVATXN.getVatxnHphone())) {
			titaB0000.setEnqTPhone(defVATXN.getVatxnHphone());
		} else {
			titaB0000.setEnqTPhone("");
		}
		// 子帳號
		if (VAChkType.ICCard.equals(defVATXN.getVatxnType())) {
			titaB0000.setEnqSubAcctNo(getFeptxn().getFeptxnMajorActno().substring(2, 16));
		} else {
			titaB0000.setEnqSubAcctNo("");
		}

		// 利用General class的GenDictionary建立T24的titaBody
		titaB0000.genDictionary(titaBody);
	}

	private void genA1001Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());

		// 借用 FEPTXN_NOTICE_ID 欄位存放 ATM 行外記號
		if ("2".equals(getFeptxn().getFeptxnNoticeId())) {
			titaA0000.setCreditAcctNo(ATMPConfig.getInstance().getOutATMIntActno());
		} else {
			titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
					+ getFeptxn().getFeptxnAtmBrno());;
		}
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());

		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt()); // 帳戶交易金額
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur()); // 提領幣別
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());// 提領金額
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate()); // 交易匯率
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpay()); // 提領幣別手續費
		titaA0000.setIcActno("");
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		// 掌靜脈提款(PWD2521)
		if (ATMTXCD.PWD.toString().equals(getFeptxn().getFeptxnTxCode())) {
			titaA0000.setTPsbRemSD("掌靜脈" + getFeptxn().getFeptxnAtmno());
		} else {
			titaA0000.setTPsbRemSD("無卡" + getFeptxn().getFeptxnAtmno());
		}
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmno());
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.getTPsbRinfD().put(2, getFeptxn().getFeptxnMajorActno());
		titaA0000.setTRegTfrType("NCW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void addGeneralField(T24TITAA0000 titaA0000) throws Exception {
		if (StringUtils.isNotBlank(titaA0000.getDebitAcctNo())
				&& "00".equals(titaA0000.getDebitAcctNo().substring(0, 2))) {
			titaA0000.setDebitAcctNo(titaA0000.getDebitAcctNo().substring(2, 16));
		}

		if (StringUtils.isNotBlank(titaA0000.getCreditAcctNo())
				&& "00".equals(titaA0000.getCreditAcctNo().substring(0, 2))) {
			titaA0000.setCreditAcctNo(titaA0000.getCreditAcctNo().substring(2, 16));
		} else {
			titaA0000.setCreditAcctNo(
					getVirtualAccountPrefix(titaA0000.getCreditAcctNo()) + titaA0000.getCreditAcctNo());
		}

		if (StringUtils.isNotBlank(titaA0000.getIcActno())) {
			titaA0000.setIcActno(titaA0000.getIcActno().substring(2, 16));
		}

		if (StringUtils.isBlank(titaA0000.getFeePayer())) {
			// 除全國性繳費自行轉帳以外，其他皆為借方負擔手續費
			titaA0000.setFeePayer("1");
		}

		/// *他行之銀行代號才需搬值 - for 借方or貸方銀行代號*/
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno())) {
			if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
				titaA0000.setBankCodeDr(getFeptxn().getFeptxnTroutBkno());
			}
		} else {
			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
				titaA0000.setBankCodeDr(getFeptxn().getFeptxnDesBkno());
			}
		}
		titaA0000.setAcctTxnDr(getFeptxn().getFeptxnTroutActno());

		if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
				&& !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
			titaA0000.setBankCodeCr(getFeptxn().getFeptxnTrinBkno());
		}
		titaA0000.setAcctTxnCr(getFeptxn().getFeptxnTrinActno());
		if (StringUtils.isBlank(titaA0000.getDebitCurrency())) {
			titaA0000.setDebitCurrency("TWD");
		}
		if (StringUtils.isBlank(titaA0000.getCreditCurrency())) {
			titaA0000.setCreditCurrency("TWD");
		}

		// 將財金 PCODE 置於 AUTH_CODE 以便每日統計金額*/
		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
			titaA0000.setAuthCode(getFeptxn().getFeptxnPcode());
		}

		if (StringUtils.isBlank(getFeptxn().getFeptxnCbsSupActno())) {
			getFeptxn().setFeptxnCbsSupActno(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		}

		getFeptxn().setFeptxnCbsFeeActno(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());

		if (StringUtils.isNotBlank(getFeptxn().getFeptxnClientip())) {
			titaA0000.setTIpAddress(getFeptxn().getFeptxnClientip());
		}
	}

	private void genA1010Body(HashMap<String, String> titaBody) throws Exception {

		T24TITAA0000 titaA0000 = new T24TITAA0000();
		Allbank defALLBANK = new Allbank();

		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());

		// 讀取 getInbkparm() 取得客戶應付手續費
		if (getInbkparm() == null) {
			// 修正當讀不到INBKPARM的資料時要丟Exception出來
			rtnCode = getInbkparmByPK();
			if (rtnCode != FEPReturnCode.Normal) {
				String remark = "GenA1010Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur()
						+ " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
						+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE="
						+ getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
		}
		getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
		getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());

		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		defALLBANK.setAllbankBkno(getFeptxn().getFeptxnBkno());
		defALLBANK.setAllbankBrno("000");
		Allbank allbank = allbankMapper.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
		if (allbank != null) {
			defALLBANK = allbank;
		}
		titaA0000.setTPsbRemSD(defALLBANK.getAllbankAliasname());
		titaA0000.setTPsbRemFD(defALLBANK.getAllbankFullname());
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.setTRegTfrType("NW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1011Body(HashMap<String, String> titaBody) throws Exception {

		T24TITAA0000 titaA0000 = new T24TITAA0000();
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		Allbank defALLBANK = new Allbank();

		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTroutActno());// 帳號
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());// 帳戶交易金額
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());// 提領金額

		// 讀取 getInbkparm() 取得客戶應付手續費
		if (getInbkparm() == null) {
			// 2012/10/04 Modify by Ruling for 修正當讀不到INBKPARM的資料時要丟Exception出來
			rtnCode = getInbkparmByPK();
			if (rtnCode != FEPReturnCode.Normal) {
				String remark = "GenA1011Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur()
						+ " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
						+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE="
						+ getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
		}
		getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
		getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());

		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());// 財金序號
		titaA0000.setIcActno(""); // 卡片主帳號
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		defALLBANK.setAllbankBkno(getFeptxn().getFeptxnBkno());
		defALLBANK.setAllbankBrno("000");
		Allbank allbank = allbankMapper.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
		if (allbank != null) {
			defALLBANK = allbank;
		}
		titaA0000.setTPsbRemSD("無卡" + defALLBANK.getAllbankAliasname());
		titaA0000.setTPsbRemFD(defALLBANK.getAllbankFullname());
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.setPAYTYPE("NCW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1030Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct().subtract(getFeptxn().getFeptxnFeeCustpayAct()));
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct().subtract(getFeptxn().getFeptxnFeeCustpayAct()));
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());

		// CREDIT_ACCT_NO
		switch (getFeptxn().getFeptxnAtmZone()) {
			case ZoneCode.HKG: // 台灣卡至香港跨區提款
				titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
				break;
			case ZoneCode.MAC: // 台灣卡至澳門跨區提款
				titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(0, 7) + "2"
						+ getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(8, 16)); // 因澳門與香港帳號不同，借用手續費帳號欄位
				break;
		}
		// 港澳NCB
		getFeptxn().setFeptxnCbsSupActno(titaA0000.getCreditAcctNo()); // 貸方帳號

		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		titaA0000.setTPsbMemoD("境外提款");
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnFeeCur() + getFeptxn().getFeptxnTxAmt().toString());
		titaA0000.setTRegTfrType("nw");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1041Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCur().trim()
				+ getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13)
				+ getFeptxn().getFeptxnAtmBrno());
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());

		if (CURTWD.equals(getFeptxn().getFeptxnTxCurAct())) {
			// 本行台幣帳戶，交易金額=人民幣*匯率
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		} else {
			// 本行原幣帳戶，交易金額=人民幣
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		}

		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpay());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		if (!"002".equals(getFeptxn().getFeptxnTroutActno().substring(5, 8))
				&& !"008".equals(getFeptxn().getFeptxnTroutActno().substring(5, 8))) {
			// 本行台幣戶帳戶
			titaA0000.setTPsbRemSD(getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()).getCurcdCurName().trim());
			titaA0000.setTPsbRemFD(getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()).getCurcdCurName().trim());
		} else {
			// 本行原幣帳戶
			titaA0000.setTPsbRemSD(getFeptxn().getFeptxnAtmno());
			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmno());
		}
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.setTRegTfrType("NW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1042Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCur().trim()
				+ getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13)
				+ getFeptxn().getFeptxnAtmBrno());
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());

		if (CURTWD.equals(getFeptxn().getFeptxnTxCurAct())) {
			// 本行台幣帳戶，交易金額=折台幣交易金額
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		} else {
			// 本行原幣帳戶，交易金額=提領金額
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		}

		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpay());
		titaA0000.setIcActno("");
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno().trim());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		titaA0000.setTPsbRemSD("無卡" + getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()).getCurcdCurName().trim());
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmno());
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno()
				+ getFeptxn().getFeptxnMajorActno().substring(8, 16));
		titaA0000.setTRegTfrType("NCW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1110Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		// 利用General class將值正確填入

		// 讀取 getInbkparm() 取得客戶應付手續費
		if (getInbkparm() == null) {
			// 修正當讀不到INBKPARM的資料時要丟Exception出來
			rtnCode = getInbkparmByPK();
			if (rtnCode != FEPReturnCode.Normal) {
				String remark = "GenA1110Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur()
						+ " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
						+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE="
						+ getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
		}

		if (!BINPROD.Credit.equals(getFeptxn().getFeptxnTroutKind()) && !BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())) {
			titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
			titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
			// add by ashiang
			// 非ATM交易或CHICODE無值或=ATM
			//--ben-20220922-//if (mATMTxData == null || (StringUtils.isBlank(mATMTxData.getTxObject().getRequest().getCHLCODE())
			if (mATMTxData == null || (StringUtils.isBlank("")
					//--ben-20220922-//|| "ATM".equals(mATMTxData.getTxObject().getRequest().getCHLCODE()))) {
					|| "ATM".equals(""))) {
				// 掌靜脈轉帳(PFT2521)
				if (ATMTXCD.PFT.name().equals(getFeptxn().getFeptxnTxCode())) {
					titaA0000.setIcActno("");
				} else {
					titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
				}

				titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
				titaA0000.setTPsbRemSD(
						getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno().substring(7, 16));
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());

				// 掌靜脈轉帳(PFT2521)
				if (ATMTXCD.PFT.name().equals(getFeptxn().getFeptxnTxCode())) {
					titaA0000.setTRegTfrType("");
				} else {
					// 行動金融卡轉帳交易
					if (getFeptxn().getFeptxnIcmark() != null && "3".equals(StringUtils.leftPad(getFeptxn().getFeptxnIcmark(), 30, '0').substring(28, 29))) {
						// 行動金融卡
						if (StringUtils.isBlank(getCardTFRFlag())) {
							String remark = "GenA1110Body-CardTFRFlag物件為Null或空白";
							getLogContext().setRemark(remark);
							this.logMessage(getLogContext());
							throw ExceptionUtil.createException(remark);
						}
						if ("2".equals(getCardTFRFlag())) {// 限制約轉
							titaA0000.setTRegTfrType("CR");
						}
						titaA0000.setTPsbRemFD(titaA0000.getTPsbRemFD() + "行動金融卡");
						titaA0000.setTPsbRemFC(titaA0000.getTPsbRemFC() + "行動金融卡");
					} else {
						// 一般金融卡
						if (getCard() == null) {
							String remark = "GenA1110Body-Card檔物件為Nothing";
							getLogContext().setRemark(remark);
							this.logMessage(getLogContext());
							throw ExceptionUtil.createException(remark);
						}
						if (getCard().getCardTfrFlag() == 2) // 限制約轉
						{
							titaA0000.setTRegTfrType("CR");
						}
					}
				}
				// ps.避掉預現轉帳於這裡調整而不是加到最後面
				if (FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel())) {
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
						titaA0000.setTPsbRemSD(getFeptxn().getFeptxnChrem());
						titaA0000.getTPsbRemFDMuti().put(1, titaA0000.getTPsbRemFD());
						titaA0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnChrem());
					}
				}

			} else {
				// 避免預約交易重覆入帳，將預約序號寫入T24電文欄位
				if (FEPChannel.FCS.name().equals(getFeptxn().getFeptxnChannel())) {
					titaA0000.setTBrSeqno(getFeptxn().getFeptxnChannelEjfno());
				}
				//--ben-20220922-//titaA0000.setTPsbMemoD(mATMTxData.getTxObject().getRequest().getPsbmemoD());
				//--ben-20220922-//titaA0000.setTPsbMemoC(mATMTxData.getTxObject().getRequest().getPsbmemoC());
				//--ben-20220922-//titaA0000.setTPsbRemSC(mATMTxData.getTxObject().getRequest().getPsbremSC());
				//--ben-20220922-//titaA0000.setTPsbRemSD(mATMTxData.getTxObject().getRequest().getPsbremSD());
				//--ben-20220922-//titaA0000.setTPsbRemFC(mATMTxData.getTxObject().getRequest().getPsbremFC());
				// 網銀 T_PSB_REM_F_D 改為 Multi-Value
				//--ben-20220922-//if (StringUtils.isNotBlank(mATMTxData.getTxObject().getRequest().getPsbremFD())
				if (StringUtils.isNotBlank("")
						//--ben-20220922-//&& mATMTxData.getTxObject().getRequest().getPsbremFD().indexOf("~") > -1) {
						&& "".indexOf("~") > -1) {
					//--ben-20220922-//String[] multiValue = mATMTxData.getTxObject().getRequest().getPsbremFD().split("[~]", -1);
					String[] multiValue = "".split("[~]", -1);
					for (int i = 0; i < multiValue.length; i++) {
						if (StringUtils.isNotBlank(multiValue[i])) {
							titaA0000.getTPsbRemFDMuti().put(i + 1, multiValue[i]);
						}
					}
				} else {
					//--ben-20220922-//titaA0000.setTPsbRemFD(mATMTxData.getTxObject().getRequest().getPsbremFD());
				}
				//--ben-20220922-//titaA0000.setTRegTfrType(mATMTxData.getTxObject().getRequest().getRegTfrType());
			}
			// *INTB/行庫代號/帳號/戶名,其中戶名為空
			titaA0000.getTPsbRinfC().put(1,
					"INTB^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^");
			titaA0000.getTPsbRinfD().put(1,
					"FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
			titaA0000.getTPsbRinfD().put(2, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		} else {
			// 信用卡預借現金跨行轉帳
			titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno()); // 信用卡預現專戶
			getFeptxn().setFeptxnCbsIntActno(getGeneralData().getMsgCtl().getMsgctlCbsIntActno());
			titaA0000.setTPsbMemoD("ATM預現");
			titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
			titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTroutActno().substring(4, 16));
			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno());
			titaA0000.setTRegTfrType("NN");
		}
		if ("M".equals(getFeptxn().getFeptxnBenefit())) {
			// 轉帳繳納口罩，上主機電文，口罩手續費放0元
			getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.ZERO);
			getFeptxn().setFeptxnFeeCustpay(BigDecimal.ZERO);
		} else {
			// 非轉帳繳納口罩
			if (getFeptxn().getFeptxnTxAmt().doubleValue() <= INBKConfig.getInstance().getTFRFeeReductTxAmt()
					&& !ATMTXCD.BFT.name().equals(getFeptxn().getFeptxnTxCode())) {
				// 轉帳金額小於1000，小額優免手續費放10元
				getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(INBKConfig.getInstance().getTFRReductFee()));
				getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(INBKConfig.getInstance().getTFRReductFee()));
			} else {
				getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
				getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());
			}
		}
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());

		// WEBATM帶來的【給自己】中文備註
		if (FEPChannel.WEBATM.name().equals(getFeptxn().getFeptxnChannel())
				&& ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode())
				&& StringUtils.isNotBlank(getFeptxn().getFeptxnFmrem())) {
			titaA0000.getTPsbRemFDMuti().put(1, titaA0000.getTPsbRemFD());
			titaA0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnFmrem());
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1120Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();

		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno());
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getDebitAcctNo());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno().substring(7, 16));
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnTrinActno());
		titaA0000.getTPsbRinfC().put(1,
				"INTB^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^");
		titaA0000.getTPsbRinfD().put(1,
				"FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
		titaA0000.getTPsbRinfD().put(2, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		// ATM新功能-跨行存款轉帳
		getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct()));
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct()));
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		titaA0000.setFeePayer("2"); // 手續費付費單位-轉入單位付費

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA2110Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		// 利用General class將值正確填入
		if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
			// FEPTXN_MAJOR_ACTNO改FEPTXN_TRIN_ACTNO
			// 手機門號跨行轉帳
			if ("Y".equals(((FeptxnExt) getFeptxn()).getFeptxnMtp())) {
				titaA0000.setCreditAcctNo("TEL" + getFeptxn().getFeptxnTrinActno().substring(6, 16));
			} else {
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
			}

			// 永豐書妙要求調整轉出電文內容
			if (CMNConfig.getInstance().getVirtualActno().equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
				// 轉入帳號前五碼=虛擬帳號(00598)，比照繳信用卡款
				titaA0000.setTPsbMemoC("信用卡費");
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				// 跨行轉帳交易新增中文附言欄位：往來明細(T.PSB.REM.F.C)改用Muti
				titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
				titaA0000.setTRegTfrType("NN");
			} else {
				// 轉入一般帳號
				titaA0000.setTPsbMemoC(getGeneralData().getMsgCtl().getMsgctlCbsMemoCr());
				titaA0000.setTPsbRemSC(
						getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno().substring(7, 16));
				// 跨行轉帳交易新增中文附言欄位：往來明細(T.PSB.REM.F.C)改用Muti
				titaA0000.getTPsbRemFCMuti().put(1,
						getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());
			}
		} else {
			switch (getFeptxn().getFeptxnTrinKind()) {
				case BINPROD.Credit:
				case BINPROD.Combo:
					titaA0000.setCreditAcctNo("CRD" + getFeptxn().getFeptxnTrinActno());
					// 永豐書妙要求調整轉出電文內容
					titaA0000.setTPsbMemoC("信用卡費");
					titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
					// 跨行轉帳交易新增中文附言欄位：往來明細(T.PSB.REM.F.C)改用Muti
					titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno());
					titaA0000.setTRegTfrType("NN");
					break;
				case BINPROD.Gift:
					titaA0000.setCreditAcctNo("GIF" + getFeptxn().getFeptxnTrinActno());
					titaA0000.setTPsbMemoC("Gift 卡");
					titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
					titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno());
					break;
			}
		}

		// 測試虛擬帳號
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());

		// 修改ATM跨行存款功能
		titaA0000.setTSavingChk("");
		if (FISCPCode.PCode2521.getValueStr().equals(getFeptxn().getFeptxnPcode())
				&& ("9999".equals(getFeptxn().getFeptxnNoticeId()) || "9998".equals(getFeptxn().getFeptxnNoticeId()))
				&& StringUtils.isNotBlank(getFeptxn().getFeptxnIcmark())
				&& getFeptxn().getFeptxnIcmark().trim().length() >= 20) {
			// 跨行存款
			// 存入非本人帳戶每日限額3萬
			if ("9998".equals(getFeptxn().getFeptxnNoticeId())) {
				titaA0000.setTSavingChk("Y");
			}
			// 記錄晶片卡備註欄位之發卡行及帳號
			titaA0000.setTPsbMemoC("跨行存款");
			titaA0000.setTPsbRemSC(
					getFeptxn().getFeptxnIcmark().substring(1, 4) + getFeptxn().getFeptxnIcmark().substring(11, 20));
			// 3/2配合永豐跨行存款若存入帳號為本行信用卡卡號或598+ID，調整長備註內容改為信用卡卡號或598+ID，以利信用卡銷帳
			if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
				if (CMNConfig.getInstance().getVirtualActno().equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
					// 轉入帳號前五碼=虛擬帳號(00598)
					// 跨行轉帳交易新增中文附言欄位：調整先判斷T.PSB.REM.F.C第一組若存在先移除在重新加入
					if (titaA0000.getTPsbRemFCMuti().containsKey(1)) {
						titaA0000.getTPsbRemFCMuti().remove(1);
						titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
					} else {
						titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
					}
				} else {
					// 轉入一般帳號
					// 跨行轉帳交易新增中文附言欄位：調整先判斷T.PSB.REM.F.C第一組若存在先移除在重新加入
					if (titaA0000.getTPsbRemFCMuti().containsKey(1)) {
						titaA0000.getTPsbRemFCMuti().remove(1);
						titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnIcmark().substring(1, 4)
								+ getFeptxn().getFeptxnIcmark().substring(4, 20));
					} else {
						titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnIcmark().substring(1, 4)
								+ getFeptxn().getFeptxnIcmark().substring(4, 20));
					}
				}
			} else {
				// 轉入信用卡號
				// 跨行轉帳交易新增中文附言欄位：調整先判斷T.PSB.REM.F.C第一組若存在先移除在重新加入
				if (titaA0000.getTPsbRemFCMuti().containsKey(1)) {
					titaA0000.getTPsbRemFCMuti().remove(1);
					titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno());
				} else {
					titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno());
				}
			}

			titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnIcmark().substring(4, 20));
			titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnIcmark().substring(1, 4) + "^"
					+ getFeptxn().getFeptxnIcmark().substring(4, 20) + "^^");
		} else {
			// 非跨行存款
			titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno());
			titaA0000.getTPsbRinfC().put(2,
					"FUT^" + getFeptxn().getFeptxnTroutBkno() + "^" + getFeptxn().getFeptxnTroutActno() + "^^");
		}
		titaA0000.getTPsbRinfC().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());

		// 約定及金融卡簽收邏輯調整，統一發票中奬存入調整往來明細
		if (FISCPCode.PCode2524.getValueStr().equals(getFeptxn().getFeptxnPcode())
				&& getFeptxn().getFeptxnBkno().equals(ATMPConfig.getInstance().getINVBank())
				&& StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno())
				&& ATMPConfig.getInstance().getINVTroutBank().indexOf(getFeptxn().getFeptxnTroutBkno()) > -1
				&& getFeptxn().getFeptxnTroutActno().equals(ATMPConfig.getInstance().getINVTroutAcct())
				&& getFeptxn().getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
			titaA0000.setTPsbMemoC("統一發票");
			titaA0000.setTPsbRemSC("發票中獎獎金");
		}

		// 跨行轉帳交易新增中文附言欄位：轉帳交易(IFT2522/2521/2524)，中文附言欄帶入往來明細(T.PSB.REM.F.C)第二組
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
				&& StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
			titaA0000.getTPsbRemFCMuti().put(2, getFeptxn().getFeptxnChrem());
		}

		// 配合財金公司現金回饋紅包平台調整2521入帳行交易的存摺備註(T_PSB_REM_S_C)
		if (FISCPCode.PCode2521.getValueStr().equals(getFeptxn().getFeptxnPcode())
				&& getFeptxn().getFeptxnBkno().equals(ATMPConfig.getInstance().getTPBank())
				&& getFeptxn().getFeptxnTroutBkno().equals(getFeptxn().getFeptxnBkno())) {
			titaA0000.setTPsbRemSC(ATMPConfig.getInstance().getTPMemo());
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1070Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
			// 手機門號跨行轉帳
			if ("Y".equals(((FeptxnExt) getFeptxn()).getFeptxnMtp())) {
				titaA0000.setCreditAcctNo("TEL" + getFeptxn().getFeptxnTrinActno().substring(6, 16));
			} else {
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
			}
			titaA0000.setTPsbMemoD("ATM轉帳");
			titaA0000.setTPsbMemoC("ATM轉帳");
		} else {
			switch (getFeptxn().getFeptxnTrinKind()) {
				case BINPROD.Credit:
				case BINPROD.Combo:
					titaA0000.setCreditAcctNo("CRD" + getFeptxn().getFeptxnTrinActno());
					titaA0000.setTPsbMemoD("信用卡費");
					titaA0000.setTPsbMemoC("信用卡費");
					break;
				case BINPROD.Gift:
					titaA0000.setCreditAcctNo("GIF" + getFeptxn().getFeptxnTrinActno());
					titaA0000.setTPsbMemoD("Gift卡");
					titaA0000.setTPsbMemoC("Gift卡");
					break;
			}
		}

		// 測試虛擬帳號
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		// 掌靜脈轉帳(PFT)
		if (ATMTXCD.PFT.name().equals(getFeptxn().getFeptxnTxCode())) {
			titaA0000.setIcActno("");
		} else {
			titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		}

		// Mobile Bank GIFT卡儲值:存摺摘要/往來明細等資料由外圍系統的電文提供
		// 現金儲值卡自動加值
//		if (FEPChannel.SINOCARD.name().equals(getFeptxn().getFeptxnChannel())) {
//			// GIFT 卡自動加值，存褶欄位
//			titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
//			titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
//			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno());
//			titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
//			titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
//			titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
//		} else {
			//--ben-20220922-//if (mATMTxData == null || (StringUtils.isBlank(mATMTxData.getTxObject().getRequest().getCHLCODE())
			if (mATMTxData == null || (StringUtils.isBlank("")
					//--ben-20220922-//|| "ATM".equals(mATMTxData.getTxObject().getRequest().getCHLCODE()))) {
					|| "ATM".equals(""))) {
				if (ATMTXCD.PFT.name().equals(getFeptxn().getFeptxnTxCode())) {
					titaA0000.setTRegTfrType("");
				} else {
					if ("3".equals(StringUtils.leftPad(getFeptxn().getFeptxnIcmark(), 30, '0').substring(28, 29))) {
						// 行動金融卡
						if (StringUtils.isBlank(getCardTFRFlag())) {
							String remark = "GenA1070Body-CardTFRFlag物件為Null或空白";
							getLogContext().setRemark(remark);
							this.logMessage(getLogContext());
							throw ExceptionUtil.createException(remark);
						}
						if ("2".equals(getCardTFRFlag())) {// 限制約轉
							titaA0000.setTRegTfrType("CR");
						} else {
							if ((BINPROD.Credit.equals(getFeptxn().getFeptxnTrinKind())
									|| BINPROD.Combo.equals(getFeptxn().getFeptxnTrinKind()))
									|| CMNConfig.getInstance().getVirtualActno()
											.equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
								titaA0000.setTRegTfrType("NR");
							}
						}
					} else {
						// 一般金融卡
						if (getCard() == null) {
							String remark = "GenA1070Body-Card檔物件為Nothing";
							getLogContext().setRemark(remark);
							this.logMessage(getLogContext());
							throw ExceptionUtil.createException(remark);
						}
						if (getCard().getCardTfrFlag() == 2) {// 限制約轉
							titaA0000.setTRegTfrType("CR");
						} else {
							// 自行轉帳繳永豐信用卡款及轉入帳號前五碼或繳信用卡款之虛擬帳號為00598時，改為NR
							if ((BINPROD.Credit.equals(getFeptxn().getFeptxnTrinKind())
									|| BINPROD.Combo.equals(getFeptxn().getFeptxnTrinKind()))
									|| CMNConfig.getInstance().getVirtualActno()
											.equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
								titaA0000.setTRegTfrType("NR");
							}
						}
					}
				}
				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
					// 跨行交易 (A1170)
					if (getInbkparm() == null) {
						// 修正當讀不到INBKPARM的資料時要丟Exception出來
						rtnCode = getInbkparmByPK();
						if (rtnCode != FEPReturnCode.Normal) {
							// 修正當讀不到INBKPARM的資料時要丟Exception出來
							String remark = "GenA1070Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur()
									+ " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
									+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE="
									+ getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE="
									+ getInbkparm().getInbkparmPcode();
							getLogContext().setRemark(remark);
							this.logMessage(getLogContext());
							throw ExceptionUtil.createException(remark);
						}
					}
					// 跨行轉帳小額交易手續費調降
					if (getFeptxn().getFeptxnTxDate().compareTo(INBKConfig.getInstance().getTFRSEffectDate()) >= 0
							&& getFeptxn().getFeptxnTxAmt().doubleValue() <= INBKConfig.getInstance().getTFRFeeReductTxAmt()) {
						getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(INBKConfig.getInstance().getTFRReductFee()));
						getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(INBKConfig.getInstance().getTFRReductFee()));
					} else {
						getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
						getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());
					}
					titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
					titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
					titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());

					titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
					titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnBkno() + "^"
							+ getFeptxn().getFeptxnTrinActno().substring(2, 16) + "^^");
					titaA0000.getTPsbRinfD().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
					titaA0000.getTPsbRinfD().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");
					// 永豐書妙要求調整轉出電文內容
					if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind()) && !getFeptxn().getFeptxnTrinActno()
							.substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
						// 轉入一般帳號
						titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
						titaA0000.setTPsbRemFD(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnTrinActno());
						titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTroutActno().substring(4, 16));
						// 跨行轉帳交易新增中文附言欄位：往來明細(T.PSB.REM.F.C)改用Muti
						titaA0000.getTPsbRemFCMuti().put(1,
								getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());
					} else {
						titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
						titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));

						if ("00".equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
							titaA0000.setTPsbMemoD("信用卡費");
							titaA0000.setTPsbMemoC("信用卡費");
							titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno().substring(2, 16));
							// 跨行轉帳交易新增中文附言欄位：往來明細(T.PSB.REM.F.C)改用Muti
							titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
						} else {
							titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno());
							// 跨行轉帳交易新增中文附言欄位：往來明細(T.PSB.REM.F.C)改用Muti
							titaA0000.getTPsbRemFCMuti().put(1, getFeptxn().getFeptxnTrinActno());
						}
					}
					// 永豐要求 Psb.Rinf.C、Psb.Rinf.D 的帳號前不可以多加兩個"00"
					titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno().substring(2, 16));
					titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^"
							+ getFeptxn().getFeptxnTroutActno().substring(2, 16) + "^^");
					titaA0000.getTPsbRinfC().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
					titaA0000.getTPsbRinfC().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");

					// 跨行轉帳交易新增中文附言欄位：原存自轉交易(2523)，中文附言欄帶入往來明細(T.PSB.REM.F.C)第二組
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
						titaA0000.getTPsbRemFCMuti().put(2, getFeptxn().getFeptxnChrem());
					}

					// 將原存自轉交易(2523)借/貸方長備註欄位寫入FEPTXN
					getFeptxn().setFeptxnPsbremFD(titaA0000.getTPsbRemFD());
					getFeptxn().setFeptxnPsbremFC(titaA0000.getTPsbRemFCMuti().get(1));

					// 行動金融卡轉帳交易
					if ("3".equals(StringUtils.leftPad(getFeptxn().getFeptxnIcmark(), 30, '0').substring(28, 29))) {
						titaA0000.setTPsbRemFD(titaA0000.getTPsbRemFD() + "行動金融卡");
						titaA0000.setTPsbRemFC(titaA0000.getTPsbRemFC() + "行動金融卡");
					}
				} else {
					if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind()) && !CMNConfig.getInstance().getVirtualActno()
							.equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
						// 轉入一般帳號
						titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
						titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTroutActno().substring(4, 16));
						titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());
						// 永豐要求 Psb.Rinf.C、Psb.Rinf.D 的帳號前不可以多加兩個"00"
						titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
						titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^"
								+ getFeptxn().getFeptxnTrinActno().substring(2, 16) + "^^");
						titaA0000.getTPsbRinfD().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
						titaA0000.getTPsbRinfD().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");
						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());
						// 永豐要求 Psb.Rinf.C、Psb.Rinf.D 的帳號前不可以多加兩個"00"
						titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno().substring(2, 16));
						titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^"
								+ getFeptxn().getFeptxnTroutActno().substring(2, 16) + "^^");
						titaA0000.getTPsbRinfC().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
						titaA0000.getTPsbRinfC().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");
					} else {
						// 繳永豐信用卡款
						titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
						titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
						// 永豐書妙要求調整轉出電文內容
						if (getFeptxn().getFeptxnTrinActno().substring(0, 2).equals("00")) {
							titaA0000.setTPsbMemoD("信用卡費");
							titaA0000.setTPsbMemoC("信用卡費");
							titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno().substring(2, 16));
							titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno().substring(2, 16));
						} else {
							titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno());
							titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
						}
						titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
						titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
					}

					// 將自行轉交易(IFT)借/貸方長備註欄位寫入FEPTXN
					getFeptxn().setFeptxnPsbremFD(titaA0000.getTPsbRemFD());
					getFeptxn().setFeptxnPsbremFC(titaA0000.getTPsbRemFC());

					// 帶來的【給自己】中文備註
					if (FEPChannel.WEBATM.toString().equals(getFeptxn().getFeptxnChannel())
							&& ATMTXCD.IFT.toString().equals(getFeptxn().getFeptxnTxCode())) {
						if (StringUtils.isNotBlank(getFeptxn().getFeptxnFmrem())) {
							titaA0000.getTPsbRemFDMuti().put(1, titaA0000.getTPsbRemFD());
							titaA0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnFmrem());
						}

						if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
							titaA0000.getTPsbRemFCMuti().put(1, titaA0000.getTPsbRemFC());
							titaA0000.getTPsbRemFCMuti().put(2, getFeptxn().getFeptxnChrem());
						}
					}
					if (FEPChannel.ATM.toString().equals(getFeptxn().getFeptxnChannel())) {
						if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
							// ATM中文備註優化：寫入借貸方存摺欄位(T.PSB.REM.S.X)
							titaA0000.setTPsbRemSD(getFeptxn().getFeptxnChrem());
							titaA0000.setTPsbRemSC(getFeptxn().getFeptxnChrem());
							titaA0000.getTPsbRemFDMuti().put(1, titaA0000.getTPsbRemFD());
							titaA0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnChrem());
							titaA0000.getTPsbRemFCMuti().put(1, titaA0000.getTPsbRemFC());
							titaA0000.getTPsbRemFCMuti().put(2, getFeptxn().getFeptxnChrem());
						}
					}
				}
			} else {
				// 外圍 Channel上 T24 時，存摺摘要/往來明細等資料由外圍系統的電文提供
				//--ben-20220922-//titaA0000.setTPsbMemoD(mATMTxData.getTxObject().getRequest().getPsbmemoD());
				//--ben-20220922-//titaA0000.setTPsbMemoC(mATMTxData.getTxObject().getRequest().getPsbmemoC());
				//--ben-20220922-//titaA0000.setTPsbRemSC(mATMTxData.getTxObject().getRequest().getPsbremSC());
				//--ben-20220922-//titaA0000.setTPsbRemSD(mATMTxData.getTxObject().getRequest().getPsbremSD());
				//--ben-20220922-//titaA0000.setTPsbRemFC(mATMTxData.getTxObject().getRequest().getPsbremFC());
				//--ben-20220922-//titaA0000.setTPsbRemFD(mATMTxData.getTxObject().getRequest().getPsbremFD());
				//--ben-20220922-//titaA0000.setTRegTfrType(mATMTxData.getTxObject().getRequest().getRegTfrType());
			}
//		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1070BodyForSVCS(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		if (SVCSTXCD.SD2.toString().equals(getFeptxn().getFeptxnTxCode())) {
			// 自動加值
			titaA0000.setDebitAcctNo(getFeptxn().getFeptxnMajorActno());// 借方帳號
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno()); // 貸方帳號
			titaA0000.setTPsbMemoD("悠遊加值");
			titaA0000.setTPsbMemoC("悠遊加值");
			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno() + "(悠遊卡自動加值)");
			titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutActno() + "(悠遊卡自動加值)");
			// 配合新版悠遊卡人檔調整及相關功能優化：主機控管悠遊Debit卡自動加值限額當日限額由"NN"改為"YC"
			titaA0000.setTRegTfrType("YC");
		} else {
			// 餘額轉置
			titaA0000.setTPsbMemoD("餘額轉置");
			titaA0000.setTPsbMemoC("餘額轉置");
			// 配合新版悠遊卡人檔調整及相關功能優化：餘額轉置維持不變仍為"NN"
			titaA0000.setTRegTfrType("NN");
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && TOSVCSType.BalanceTransNegative.equals(getFeptxn().getFeptxnNoticeId().trim())) {
				// 退費金額 < 0，自客戶帳戶扣款
				titaA0000.setDebitAcctNo(getFeptxn().getFeptxnMajorActno()); // 借方帳號
				titaA0000.setCreditAcctNo(ATMPConfig.getInstance().getSVCSOutActno());// 貸方帳號
				titaA0000.setTPsbMemoD("悠遊負值");
				titaA0000.setTPsbMemoC("悠遊負值");
				// Fly 2018/03/26 備註調整一致
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno() + "(悠遊卡餘額負值扣款)");
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutActno() + "(悠遊卡餘額負值扣款)");
				// Fly 2018/03/12 悠遊卡月票退卡需求
			} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && TOSVCSType.MonthlyRefundNegative.equals(getFeptxn().getFeptxnNoticeId().trim())) {
				// 退費金額 < 0，自客戶帳戶扣款
				titaA0000.setDebitAcctNo(getFeptxn().getFeptxnMajorActno());// 借方帳號
				titaA0000.setCreditAcctNo(ATMPConfig.getInstance().getSVCSOutActno()); // 貸方帳號
				// 北捷忠誠回饋金
				titaA0000.setTPsbMemoD("北捷負值");
				titaA0000.setTPsbMemoC("北捷負值");
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno() + "(北捷餘額負值扣款)");
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutActno() + "(北捷餘額負值扣款)");
			} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && TOSVCSType.BalanceTransPositive.equals(getFeptxn().getFeptxnNoticeId().trim())) {
				// 退費金額 > 0，轉入客戶帳戶
				titaA0000.setDebitAcctNo(ATMPConfig.getInstance().getSVCSOutActno());// 借方帳號
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnMajorActno());// 貸方帳號
				titaA0000.setTPsbMemoD("悠遊返回");
				titaA0000.setTPsbMemoC("悠遊返回");
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno() + "(悠遊卡餘額返回)");
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutActno() + "(悠遊卡餘額返回)");
				// Fly 2018/03/12 悠遊卡月票退卡需求
			} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && TOSVCSType.MonthlyRefundPositive.equals(getFeptxn().getFeptxnNoticeId().trim())) {
				// 退費金額 > 0，轉入客戶帳戶
				titaA0000.setDebitAcctNo(ATMPConfig.getInstance().getSVCSOutActno());// 借方帳號
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnMajorActno()); // 貸方帳號
				// 北捷忠誠回饋金
				titaA0000.setTPsbMemoD("北捷返回");
				titaA0000.setTPsbMemoC("北捷返回");
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno() + "(北捷餘額返回)");
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutActno() + "(北捷餘額返回)");

			} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && TOSVCSType.AutoLoadRefund.equals(getFeptxn().getFeptxnNoticeId().trim())) {
				// 配合新版悠遊卡人檔調整及相關功能優化：自動加值(180天)餘額轉置
				// 退費金額 > 0，轉入客戶帳戶
				titaA0000.setDebitAcctNo(ATMPConfig.getInstance().getSVCSIntActno());// 借方帳號
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnMajorActno()); // 貸方帳號
				titaA0000.setTPsbMemoD("悠遊返回");
				titaA0000.setTPsbMemoC("悠遊返回");
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno() + "(悠遊卡餘額返回)");
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutActno() + "(悠遊卡餘額返回)");
			}
		}
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCur());// 借方幣別
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());// 借方金額
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());// 貸方幣別
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());// 貸方金額
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());// 卡片主帳號
		// 配合新版悠遊卡人檔調整及相關功能優化：點掉移至上面，分自動加值/餘額轉置，帶的T.REG.TFR.TYPE值不一樣
		// titaA0000.T_REG_TFR_TYPE = "NN"
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTroutActno().substring(4, 16));
		titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTroutActno().substring(4, 16));
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTroutActno());
		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno());

		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1210Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		Npsunit defNPSUNIT = new Npsunit();

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		// 2011/10/04 Modify by Ruling for 身份證字號過濾空白
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnIdno())) {
			if (getFeptxn().getFeptxnIdno().trim().length() == 11) {
				titaA0000.setIDNO(mappingFirstDigitIdno(getFeptxn().getFeptxnIdno().trim()));
			} else {
				titaA0000.setIDNO(getFeptxn().getFeptxnIdno().trim());
			}
		}

		titaA0000.setVPID(getFeptxn().getFeptxnBusinessUnit());
		titaA0000.setPAYTYPE(getFeptxn().getFeptxnPaytype());
		titaA0000.setPAYNO(getFeptxn().getFeptxnPayno());

		// 晶片金融卡繳費改為NC
		if ("256".equals(getFeptxn().getFeptxnPcode().substring(0, 3))) {
			titaA0000.setTRegTfrType("NC");
		}

		if (RRN30000Trans.equals(getFeptxn().getFeptxnFiscRrn())
				&& PAYTYPE30000Trans.equals(getFeptxn().getFeptxnPaytype())) {
			if (getInbkparm() == null) {
				// 修正當讀不到INBKPARM的資料時要丟Exception出來
				rtnCode = getInbkparmByPK();
				if (rtnCode != FEPReturnCode.Normal) {
					String remark = "GenA1210Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur()
							+ " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
							+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE="
							+ getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE="
							+ getInbkparm().getInbkparmPcode();
					getLogContext().setRemark(remark);
					logMessage(Level.INFO, getLogContext());
					// throw new Exception(remark);
				}
			}
			getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
			getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());
			titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
			titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
			titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno().substring(7, 16));
			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());
		} else {
			// 226X 交易，才判斷約轉或非約轉
			if ("226".equals(getFeptxn().getFeptxnPcode().substring(0, 3))
					&& (getFeptxn().getFeptxnPaytype().substring(4, 5).compareTo("0") >= 0
							&& getFeptxn().getFeptxnPaytype().substring(4, 5).compareTo("4") <= 0)) {
				// Ruling 改為NS
				titaA0000.setTRegTfrType("NS");

				// 2020/11/02 Modify by Ruling for 全國性繳費業務之保險費強化機制：226X繳保險費交易，增加保險識別編號送T24主機檢核
				String[] insPayType = INBKConfig.getInstance().getINSPayType().split("[;]", -1);
				for (int i = 0; i < insPayType.length; i++) {
					if (insPayType[i].equals(getFeptxn().getFeptxnPaytype())) {
						titaA0000.setTInsuIdeNo(getFeptxn().getFeptxnRemark().substring(28, 38).trim());
					}
				}
			}

//			if (FEPChannel.EBILL.toString().equals(getFeptxn().getFeptxnChannel())) {
//				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnReconSeqno().substring(4, 16));
//
//				// 2013/05/17 Modify by Ruling for 帳務代理(第一建金)
//				if ("00".equals(getFeptxn().getFeptxnReconSeqno().substring(0, 2))) {
//					if (getFeptxn().getFeptxnReconSeqno().substring(0, 5)
//							.equals(CMNConfig.getInstance().getVirtualActno())) {
//						// 598+ID 14位
//						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//					} else {
//						// 不能將銷帳編號(FEPTXN_RECON_SEQNO)轉入實帳號(CREDIT_ACCT_NO)
//						// '轉入帳號為一般帳號，放入銷帳編號
//						// titaA0000.CREDIT_ACCT_NO = FepTxn.FEPTXN_RECON_SEQNO
//						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//					}
//				} else {
//					// 信用卡號
//					titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno());
//				}
//			} else {
				// 2015/10/28 Modify by Ruling for 繳費網PHASEI:直接讀NPSUNIT
				// 2016/01/20 Modify by Ruline for 繳費網PHASEI:New DefNPSUNIT 移到前面
				// Dim defNPSUNIT As New DefNPSUNIT
				// Npsunit dbNPSUNIT = new Npsunit(FEPConfig.DBName);
				defNPSUNIT.setNpsunitNo(getFeptxn().getFeptxnBusinessUnit());// 委託單位代號
				defNPSUNIT.setNpsunitPaytype(getFeptxn().getFeptxnPaytype()); // 繳款類別
				defNPSUNIT.setNpsunitFeeno(getFeptxn().getFeptxnPayno()); // 費用代號
				Npsunit npsunit = npsunitMapper.selectByPrimaryKey(defNPSUNIT.getNpsunitNo(),
						defNPSUNIT.getNpsunitPaytype(), defNPSUNIT.getNpsunitFeeno());
				if (npsunit == null) {
					titaA0000.setTPsbRemSD(" ");
					titaA0000.setTPsbRemFD(" ");
				} else {
					titaA0000.setTPsbRemSD(StringUtils.rightPad(npsunit.getNpsunitPayName(), 6, ' ').substring(0, 6));
					titaA0000.setTPsbRemFD(npsunit.getNpsunitPayName());
				}
//			}

			if (getFeptxn().getFeptxnNpsClr() != null && getFeptxn().getFeptxnNpsClr().intValue() == FeptxnNPSCLR.TROut.getValue()
					&& !DbHelper.toBoolean(getFeptxn().getFeptxnNpsMonthlyFg())) {// 轉出單位付費/非月結
				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
					// 2016/01/20 Modify by Ruling for 繳費網PHASEI
					if (mFISCData != null && mFISCData.getNpsunit() != null) {
						getFeptxn().setFeptxnNpsFeeCustpay(mFISCData.getNpsunit().getNpsunitFee());
					} else {
						getFeptxn().setFeptxnNpsFeeCustpay(defNPSUNIT.getNpsunitFee());
					}
				}
				getFeptxn().setFeptxnFeeCustpayAct(getFeptxn().getFeptxnNpsFeeCustpay());
				getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnNpsFeeCustpay());
				titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
				titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
			}
		}

		// 2016/10/20 Modify by Ruling for 應永豐修改全繳API摘要欄位
		if ("948".equals(getFeptxn().getFeptxnBkno())
				&& SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnVirBrno())) {
			titaA0000.setTPsbMemoD("永豐繳費");
		} else {
			titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		}

		// 2013/05/17 Modify by Ruling for 帳務代理(第一建金)
		HashMap<Integer, String> rinf_d = new HashMap<Integer, String>();
//		if (FEPChannel.EBILL.toString().equals(getFeptxn().getFeptxnChannel())
//				&& getFeptxn().getFeptxnReconSeqno().substring(0, 2).equals("00")
//				&& !getFeptxn().getFeptxnReconSeqno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
//			// 放入虛擬帳號
//			rinf_d.put(1, getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//			rinf_d.put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^"
//					+ getFeptxn().getFeptxnReconSeqno().substring(2, 16) + "^^");
//		} else {
			rinf_d.put(1, getFeptxn().getFeptxnTrinActno());
			rinf_d.put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
//		}
		titaA0000.setTPsbRinfD(rinf_d);

		// 三萬移轉計畫存摺摘要顯示ATM繳費
		if (RRN30000Trans.equals(getFeptxn().getFeptxnFiscRrn())
				&& PAYTYPE30000Trans.equals(getFeptxn().getFeptxnPaytype())) {
			titaA0000.setTPsbMemoD("ATM繳費");
		}

		// 2015/06/08 Modify by Ruling for 行動金融信用卡eBILL繳費手續費優惠
		if (mCBSTxid.equals(T24Version.A1211)) {
			titaA0000.setTPsbRemFD(titaA0000.getTPsbRemFD() + "EBILL-行動金融");
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA2210Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		String wkAmtDesc = "";

		// 2016/08/19 Modify by Ruling for FEP信用卡費代收通路移轉(全繳)專案
		// 利用General class將值正確填入
		if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
			if (INBKConfig.getInstance().getEBillTrinActNo().equals(getFeptxn().getFeptxnTrinActno())) {
				titaA0000.setCreditAcctNo(INBKConfig.getInstance().getEBillT24ActNo());
			} else {
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
			}
		} else {
			switch (getFeptxn().getFeptxnTrinKind()) {
				case BINPROD.Credit:
				case BINPROD.Combo:
					titaA0000.setCreditAcctNo("CRD" + getFeptxn().getFeptxnTrinActno());
					break;
			}
		}
		// modify 新增幣別 2010/12/13
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());

		if ((getFeptxn().getFeptxnNpsClr() != null && FeptxnNPSCLR.TRIn.getValue() == getFeptxn().getFeptxnNpsClr().intValue())
				&& !DbHelper.toBoolean(getFeptxn().getFeptxnNpsMonthlyFg())) {
			titaA0000.setFeePayer(getFeptxn().getFeptxnNpsClr().toString());
			getFeptxn().setFeptxnFeeCustpayAct(getFeptxn().getFeptxnNpsFeeCustpay());
			getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnNpsFeeCustpay());
			titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
			titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
			wkAmtDesc = " 扣" + getFeptxn().getFeptxnFeeCustpayAct().toString();
		}

		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		// 2011/10/04 Modify by Ruling for 身份證字號過濾空白
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnIdno())) {
			if (getFeptxn().getFeptxnIdno().trim().length() == 11) {
				titaA0000.setIDNO(mappingFirstDigitIdno(getFeptxn().getFeptxnIdno().trim()));
			} else {
				titaA0000.setIDNO(getFeptxn().getFeptxnIdno().trim());
			}
		}

		titaA0000.setVPID(getFeptxn().getFeptxnBusinessUnit());
		titaA0000.setPAYTYPE(getFeptxn().getFeptxnPaytype());
		titaA0000.setPAYNO(getFeptxn().getFeptxnPayno());

		// 2015/10/28 Modify by Ruling for 繳費網PHASEI:直接讀NPSUNIT
		if (RRN30000Trans.equals(getFeptxn().getFeptxnFiscRrn())
				&& PAYTYPE30000Trans.equals(getFeptxn().getFeptxnPaytype())) {
			titaA0000.setTPsbRemSC(
					getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno().substring(7, 16));
			titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());
		} else {
			Npsunit defNPSUNIT = new Npsunit();
			// DBNPSUNIT dbNPSUNIT = new DBNPSUNIT(FEPConfig.DBName);
			defNPSUNIT.setNpsunitNo(getFeptxn().getFeptxnBusinessUnit());// 委託單位代號
			defNPSUNIT.setNpsunitPaytype(getFeptxn().getFeptxnPaytype()); // 繳款類別
			defNPSUNIT.setNpsunitFeeno(getFeptxn().getFeptxnPayno());// 費用代號
			Npsunit npsunit = npsunitMapper.selectByPrimaryKey(defNPSUNIT.getNpsunitNo(),
					defNPSUNIT.getNpsunitPaytype(), defNPSUNIT.getNpsunitFeeno());
			if (npsunit == null) {

				titaA0000.setTPsbRemSC(" ");
				titaA0000.setTPsbRemFC(" ");
			} else {
				if (FEPChannel.FCS.toString().equals(getFeptxn().getFeptxnChannel())) {
					titaA0000.setTPsbRemSC(StringUtils.rightPad(npsunit.getNpsunitPayName(), 6, ' ').substring(0, 6));
					titaA0000.setTPsbRemFC("全國繳費");
				} else {
					titaA0000.setTPsbRemSC(StringUtils.rightPad(npsunit.getNpsunitPayName(), 6, ' ').substring(0, 6));
					titaA0000.setTPsbRemFC(npsunit.getNpsunitPayName() + wkAmtDesc);
				}
			}
		}

		// 2011/07/29 modify by Ruling for 2011/07/26 永豐書妙要求調整轉出電文內容
		if ((BINPROD.Credit.equals(getFeptxn().getFeptxnTrinKind())
				|| BINPROD.Combo.equals(getFeptxn().getFeptxnTrinKind()))
				|| getFeptxn().getFeptxnTrinActno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
			titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
			if (getFeptxn().getFeptxnTrinActno().substring(0, 2).equals("00")) {
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno().substring(2, 16));
			} else {
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
			}
		}

		// EBILL 繳信用卡費
		// 帳務代理(第一建金)
//		if (FEPChannel.EBILL.toString().equals(getFeptxn().getFeptxnChannel())) {
//			titaA0000.setTPsbRemSC(getFeptxn().getFeptxnReconSeqno().substring(4, 16));
//			if (getFeptxn().getFeptxnReconSeqno().substring(0, 2).equals("00")) {
//				if (getFeptxn().getFeptxnReconSeqno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
//					// 598+ID 14位
//					titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//				} else {
//					// 2017/03/17 Modify by Ruling for
//					// 不能將銷帳編號(FEPTXN_RECON_SEQNO)轉入實帳號(CREDIT_ACCT_NO)
//					// '轉入帳號為一般帳號，放入銷帳編號
//					titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//				}
//			} else {
//				// 信用卡號
//				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno());
//			}
//		}

		// 2016/10/20 Modify by Ruling for 應永豐修改全繳API摘要欄位
		if ("948".equals(getFeptxn().getFeptxnBkno())
				&& SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnVirBrno())) {
			titaA0000.setTPsbMemoC("永豐繳費");
		} else {
			titaA0000.setTPsbMemoC(getGeneralData().getMsgCtl().getMsgctlCbsMemoCr());
		}
		// '2010/10/04 modify by Ruling for SPEC異動：增加T_PSB_RINF_C
		// titaA0000.T_PSB_MEMO_C = GeneralData.MsgCtl.MSGCTL_CBS_MEMO_CR

		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno());
		titaA0000.getTPsbRinfC().put(2,
				"FUT^" + getFeptxn().getFeptxnTroutBkno() + "^" + getFeptxn().getFeptxnTroutActno() + "^^");
		// 2014/05/28 Modify by Ruling for 整批轉即時
		if (FEPChannel.FCS.toString().equals(getFeptxn().getFeptxnChannel())) {
			titaA0000.getTPsbRinfC().put(3, getFeptxn().getFeptxnRemark().substring(4, 24).trim());
			titaA0000.getTPsbRinfC().put(4, getFeptxn().getFeptxnReconSeqno().trim());
		}

		// 2017/06/15 Modify by Ruling for 三萬移轉計畫存摺摘要顯示ATM繳費
		if (RRN30000Trans.equals(getFeptxn().getFeptxnFiscRrn())
				&& PAYTYPE30000Trans.equals(getFeptxn().getFeptxnPaytype())) {
			titaA0000.setTPsbMemoC("ATM繳費");
		}

		if ((FEPChannel.NETBANK.toString().equals(getFeptxn().getFeptxnChannel())
				|| FEPChannel.WEBATM.toString().equals(getFeptxn().getFeptxnChannel())) && StringUtils.isNotBlank(getFeptxn().getFeptxnPbtype())) {
			switch (getFeptxn().getFeptxnPbtype()) {
				case "03":
					getFeptxn()
							.setFeptxnReconSeqno(StringUtils.rightPad(getFeptxn().getFeptxnReconSeqno().trim(), 16, '0'));
					titaA0000.setTPsbRemSC(getFeptxn().getFeptxnReconSeqno().substring(4, 16));
					if ("00".equals(getFeptxn().getFeptxnReconSeqno().substring(0, 2))) {
						// 598 + ID
						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
					} else {
						// 信用卡號(外國人的銷帳編號會放入信用卡號)
						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno());
					}

					break;
				case "05":
					if (StringUtils.isBlank(getFeptxn().getFeptxnReconSeqno())) {
						titaA0000.setTPsbRemFC("");
					} else {
						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().trim());
					}
					break;
			}
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1270Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		String wkAmtDesc = "";

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
		} else {
			switch (getFeptxn().getFeptxnTrinKind()) {
				case BINPROD.Credit:
				case BINPROD.Combo:
					titaA0000.setCreditAcctNo("CRD" + getFeptxn().getFeptxnTrinActno());
					break;
			}
		}

		// 2010-12-10 by kyo for 測試虛擬帳號
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());

		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && !DbHelper.toBoolean(getFeptxn().getFeptxnNpsMonthlyFg())) {// 非月結
			if (getFeptxn().getFeptxnNpsClr() != null) {
				titaA0000.setFeePayer(getFeptxn().getFeptxnNpsClr().toString());
			}
			getFeptxn().setFeptxnFeeCustpayAct(getFeptxn().getFeptxnNpsFeeCustpay());
			getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnFeeCustpay());
			titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
			titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());

			if (getFeptxn().getFeptxnNpsClr() != null &&
					getFeptxn().getFeptxnNpsClr().intValue() == FeptxnNPSCLR.TRIn.getValue()) {// 轉入單位付費
				wkAmtDesc = " 扣" + getFeptxn().getFeptxnFeeCustpayAct().toString();
			}
		}

		// 2011/10/04 Modify by Ruling for 身份證字號過濾空白
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnIdno())) {
			if (getFeptxn().getFeptxnIdno().trim().length() == 11) {
				titaA0000.setIDNO(mappingFirstDigitIdno(getFeptxn().getFeptxnIdno().trim()));
			} else {
				titaA0000.setIDNO(getFeptxn().getFeptxnIdno().trim());
			}
		}

		titaA0000.setVPID(getFeptxn().getFeptxnBusinessUnit());
		titaA0000.setPAYTYPE(getFeptxn().getFeptxnPaytype());
		titaA0000.setPAYNO(getFeptxn().getFeptxnPayno());

		// 2012/08/21 modify by Ruling for 晶片金融卡繳費改為NC
		if (getFeptxn().getFeptxnPcode().substring(0, 3).equals("256")) {
			titaA0000.setTRegTfrType("NC");
		}

		if (RRN30000Trans.equals(getFeptxn().getFeptxnFiscRrn()) && PAYTYPE30000Trans.equals(getFeptxn().getFeptxnPaytype())) {
			titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno().substring(7, 16));
			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());
			titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno().substring(7, 16));
			titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());
		} else {
			// 2012/08/21 modify by Ruling for 226X 交易，才判斷約轉或非約轉
			if ("226".equals(getFeptxn().getFeptxnPcode().substring(0, 3))
					&& (getFeptxn().getFeptxnPaytype().substring(4, 5).compareTo("0") >= 0 && getFeptxn().getFeptxnPaytype().substring(4, 5).compareTo("4") <= 0)) {
				// 2011/07/13 modify by Ruling 改為NS
				titaA0000.setTRegTfrType("NS");

				// 2020/11/02 Modify by Ruling for 全國性繳費業務之保險費強化機制：226X繳保險費交易，增加保險識別編號送T24主機檢核
				String[] insPayType = INBKConfig.getInstance().getINSPayType().split("[;]", -1);
				for (int i = 0; i < insPayType.length; i++) {
					if (insPayType[i].equals(getFeptxn().getFeptxnPaytype())) {
						titaA0000.setTInsuIdeNo(getFeptxn().getFeptxnRemark().substring(28, 38).trim());
					}
				}
			}
			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
//				if (FEPChannel.EBILL.toString().equals(getFeptxn().getFeptxnChannel())) {
//					// 2011/11/18 Modify by Ruling for EBILL繳信用卡費
//					titaA0000.setTPsbRemSC(getFeptxn().getFeptxnReconSeqno().substring(4, 16));
//					// 2013/05/17 Modify by Ruling for 帳務代理(第一建金)
//					if ("00".equals(getFeptxn().getFeptxnReconSeqno().substring(0, 2))) {
//						if (getFeptxn().getFeptxnReconSeqno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
//							// 598+ID 14 位
//							titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//							titaA0000.setTPsbRemFD(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//						} else {
//							// 2017/03/17 Modify by Ruling for 不能將銷帳編號(FEPTXN_RECON_SEQNO)轉入實帳號(CREDIT_ACCT_NO)
//							// '轉入帳號為一般帳號，放入銷帳編號
//							// titaA0000.CREDIT_ACCT_NO = FepTxn.FEPTXN_RECON_SEQNO
//							titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//							titaA0000.setTPsbRemFD(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//						}
//					} else {
//						// 信用卡號
//						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno());
//						titaA0000.setTPsbRemFD(getFeptxn().getFeptxnReconSeqno());
//					}
//				} else {
					// 全國性繳費-非帳務代理交易
					if (mFISCData.getNpsunit() == null) {
						titaA0000.setTPsbRemSD(" ");
						titaA0000.setTPsbRemFD(" ");
						titaA0000.setTPsbRemSC(" ");
						titaA0000.setTPsbRemFC(" ");
					} else {
						titaA0000.setTPsbRemSD(StringUtils.rightPad(mFISCData.getNpsunit().getNpsunitPayName(), 6, ' ').substring(0, 6));
						titaA0000.setTPsbRemFD(mFISCData.getNpsunit().getNpsunitPayName());
						titaA0000.setTPsbRemSC(StringUtils.rightPad(mFISCData.getNpsunit().getNpsunitPayName(), 6, ' ').substring(0, 6));
						titaA0000.setTPsbRemFC(mFISCData.getNpsunit().getNpsunitPayName() + wkAmtDesc);
					}
//				}
			}
		}

		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
			// 2016/08/19 Modify by Ruling for FEP信用卡費代收通路移轉(全繳)專案
			if (INBKConfig.getInstance().getEBillTrinActNo().equals(getFeptxn().getFeptxnTrinActno())) {
				titaA0000.setCreditAcctNo(INBKConfig.getInstance().getEBillT24ActNo());
			}
			// 2011/10/12 modify by Ruling for 跨行交易才需要財金序號
			titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());

			// 2016/10/20 Modify by Ruling for 應永豐修改全繳API摘要欄位
			if ("948".equals(getFeptxn().getFeptxnBkno()) && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnVirBrno())) {
				titaA0000.setTPsbMemoD("永豐繳費");
				titaA0000.setTPsbMemoC("永豐繳費");
			} else {
				titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
				titaA0000.setTPsbMemoC(getGeneralData().getMsgCtl().getMsgctlCbsMemoCr());
			}

			// 2011/07/29 modify by Ruling for 2011/07/26 永豐書妙要求調整轉出電文內容
			if ((BINPROD.Credit.equals(getFeptxn().getFeptxnTrinKind()) || BINPROD.Combo.equals(getFeptxn().getFeptxnTrinKind()))
					|| getFeptxn().getFeptxnTrinActno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
				titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				if ("00".equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
					titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno().substring(2, 16));
					titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno().substring(2, 16));
				} else {
					// 信用卡號
					titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno());
					titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
				}
			}
			// 2013/05/17 Modify by Ruling for 帳務代理(第一建金)
//			if (FEPChannel.EBILL.toString().equals(getFeptxn().getFeptxnChannel()) && "00".equals(getFeptxn().getFeptxnReconSeqno().substring(0, 2))
//					&& !getFeptxn().getFeptxnReconSeqno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())) {
//				// 放入虛擬帳號
//				titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnReconSeqno().substring(2, 16));
//				titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnReconSeqno().substring(2, 16) + "^^");
//			} else {
				titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTrinActno());
				titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
//			}
			titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno());
			titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^" + getFeptxn().getFeptxnTroutActno() + "^^");
		} else {
			// 2011/07/29 modify by Ruling for 2011/07/26 永豐書妙要求調整轉出電文內容
			if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind()) && !CMNConfig.getInstance().getVirtualActno().equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
				// 2016/06/22 modify by Ruling for 繳費網PHASE2-全繳API:由「ATM轉帳」改為「全國繳費」
				titaA0000.setTPsbMemoD("全國繳費");
				titaA0000.setTPsbMemoC("全國繳費");
				// titaA0000.T_PSB_MEMO_D = "ATM轉帳"
				// titaA0000.T_PSB_MEMO_C = "ATM轉帳"
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());
				titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTrinActno());
				titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
				titaA0000.getTPsbRinfD().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
				titaA0000.getTPsbRinfD().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());
				titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno());
				titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^" + getFeptxn().getFeptxnTroutActno() + "^^");
				titaA0000.getTPsbRinfC().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
				titaA0000.getTPsbRinfC().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");
				titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTroutActno().substring(4, 16));
			} else {
				titaA0000.setTPsbMemoD("信用卡費");
				titaA0000.setTPsbMemoC("信用卡費");
				titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				if ("00".equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
					titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno().substring(2, 16));
					titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno().substring(2, 16));
				} else {
					// 信用卡號
					titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno());
					titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
				}

				titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
				titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
			}
		}

		// 2017/06/15 Modify by Ruling for 三萬移轉計畫存摺摘要顯示ATM繳費
		if (RRN30000Trans.equals(getFeptxn().getFeptxnFiscRrn()) && PAYTYPE30000Trans.equals(getFeptxn().getFeptxnPaytype())) {
			titaA0000.setTPsbMemoD("ATM繳費");
			titaA0000.setTPsbMemoC("ATM繳費");
		}

		// 2015/06/08 Modify by Ruling for 行動金融信用卡eBILL繳費手續費優惠
		if (mCBSTxid.equals(T24Version.A1271)) {
			titaA0000.setTPsbRemFD(titaA0000.getTPsbRemFD() + "EBILL-行動金融");
			titaA0000.setTPsbRemFC(titaA0000.getTPsbRemFC() + "EBILL-行動金融");
		}

		// 2017/08/15 Modify by Ruling for 配合繳費網，調整存褶欄位
		// 2018/07/13 Modify by Ruling for 配合繳費網05其他費用，調整存褶欄位先補足16位再取後12位
		// 2018/08/16 Modify by Ruling for WebATM三萬移轉計畫FEPTXN_PBTYPE為空值，使用IndexOf會回傳0，誤判為永豐繳費網的繳費交易，導致存褶欄位皆被改為0000000000000，調整不用IndexOf直接用FEPTXN_PBTYPE=03,05來判斷
		// 2018/09/03 Modify by Ruling for APP 收款服務(慈濟-其他費用帳務代理)
		if (FEPChannel.NETBANK.toString().equals(getFeptxn().getFeptxnChannel()) || FEPChannel.WEBATM.toString().equals(getFeptxn().getFeptxnChannel())) {
			switch (getFeptxn().getFeptxnPbtype()) {
				case "03":
					if (INBKConfig.getInstance().getEBillTrinActNo().equals(getFeptxn().getFeptxnTrinActno())) {
						titaA0000.setCreditAcctNo(INBKConfig.getInstance().getEBillT24ActNo());
					}

					getFeptxn().setFeptxnReconSeqno(StringUtils.leftPad(getFeptxn().getFeptxnReconSeqno().trim(), 16, '0'));
					titaA0000.setTPsbRemSC(getFeptxn().getFeptxnReconSeqno().substring(4, 16));
					titaA0000.setTPsbRemSD(getFeptxn().getFeptxnReconSeqno().substring(4, 16));

					if ("00".equals(getFeptxn().getFeptxnReconSeqno().substring(0, 2))) {
						// 598 + ID
						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
						titaA0000.setTPsbRemFD(getFeptxn().getFeptxnReconSeqno().substring(2, 16));
					} else {
						// 信用卡號(外國人的銷帳編號會放入信用卡號)
						titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno());
						titaA0000.setTPsbRemFD(getFeptxn().getFeptxnReconSeqno());
					}

					break;
				case "05":
					Npsunit defNPSUNIT = new Npsunit();
					// Tables.DBNPSUNIT dbNPSUNIT = new Tables.DBNPSUNIT(FEPConfig.DBName);

					defNPSUNIT.setNpsunitNo(getFeptxn().getFeptxnBusinessUnit());
					defNPSUNIT.setNpsunitPaytype(getFeptxn().getFeptxnPaytype());
					defNPSUNIT.setNpsunitFeeno(getFeptxn().getFeptxnPayno());
					Npsunit npsunit = npsunitMapper.selectByPrimaryKey(defNPSUNIT.getNpsunitNo(), defNPSUNIT.getNpsunitPaytype(), defNPSUNIT.getNpsunitFeeno());
					if (npsunit == null) {
						String remark = String.format("GenA1270Body 繳費類別05-讀不到NPSUNIT, NPSUNIT_NO=%1$s NPSUNIT_PAYTYPE=%2$s NPSUNIT_FEENO=%3$s", defNPSUNIT.getNpsunitNo(),
								defNPSUNIT.getNpsunitPaytype(), defNPSUNIT.getNpsunitFeeno());
						getLogContext().setRemark(remark);
						logMessage(Level.INFO, getLogContext());

					} else {
						if (npsunit.getNpsunitPayName().trim().length() > 6) {
							titaA0000.setTPsbRemSD(PolyfillUtil.strConv(npsunit.getNpsunitPayName().trim(), VbStrConv.Wide, 1028).substring(0, 6));
							titaA0000.setTPsbRemSC(PolyfillUtil.strConv(npsunit.getNpsunitPayName().trim(), VbStrConv.Wide, 1028).substring(0, 6));
						} else {
							titaA0000.setTPsbRemSD(npsunit.getNpsunitPayName().trim());
							titaA0000.setTPsbRemSC(npsunit.getNpsunitPayName().trim());
						}

						if (npsunit.getNpsunitNameS().trim().length() > 20) {
							titaA0000.setTPsbRemFD(PolyfillUtil.strConv(npsunit.getNpsunitNameS().trim(), VbStrConv.Wide, 1028).substring(0, 20));
						} else {
							titaA0000.setTPsbRemFD(npsunit.getNpsunitNameS().trim());
						}

						if (StringUtils.isBlank(getFeptxn().getFeptxnReconSeqno())) {
							titaA0000.setTPsbRemFC("");
						} else {
							titaA0000.setTPsbRemFC(getFeptxn().getFeptxnReconSeqno().trim());
						}
					}
					break;
			}
		}

		// 2020/05/17 Modify by Ruling for ATM中文備註
		if (FEPChannel.ATM.toString().equals(getFeptxn().getFeptxnChannel()) && ATMTXCD.EFT.toString().equals(getFeptxn().getFeptxnTxCode())) {
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
				titaA0000.getTPsbRemFDMuti().put(1, titaA0000.getTPsbRemFD());
				titaA0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnChrem());
				titaA0000.getTPsbRemFCMuti().put(1, titaA0000.getTPsbRemFC());
				titaA0000.getTPsbRemFCMuti().put(2, getFeptxn().getFeptxnChrem());
			}
		}

		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1310Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		// 2010/11/23 add by Ashiang
		if (!BINPROD.Credit.equals(getFeptxn().getFeptxnTrinKind()) && !BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())) {// 一般帳號
			titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
			//--ben-20220922-//if (mATMTxData == null || (StringUtils.isBlank(mATMTxData.getTxObject().getRequest().getCHLCODE()) || "ATM".equals(mATMTxData.getTxObject().getRequest().getCHLCODE()))) {
			if (mATMTxData == null || (StringUtils.isBlank("") || "ATM".equals(""))) {
				titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
				// 2011/08/15 modify by Ruling 調整MEMO說明
				titaA0000.setTPsbMemoD("ATM繳稅");
				if (checkPAYTYPE() != FEPReturnCode.Normal) {
					titaA0000.setTPsbRemSD("");
					titaA0000.setTPsbRemFD("");
				} else {
					titaA0000.setTPsbRemSD(getPaytype().getPaytypeAliasname());
					titaA0000.setTPsbRemFD(getPaytype().getPaytypeFullname());
				}
			} else {
				//--ben-20220922-//titaA0000.setTPsbMemoD(mATMTxData.getTxObject().getRequest().getPsbmemoD());
				//--ben-20220922-//titaA0000.setTPsbMemoC(mATMTxData.getTxObject().getRequest().getPsbmemoC());
				//--ben-20220922-//titaA0000.setTPsbRemSD(mATMTxData.getTxObject().getRequest().getPsbremSD());
				//--ben-20220922-//titaA0000.setTPsbRemSC(mATMTxData.getTxObject().getRequest().getPsbremSC());
				//--ben-20220922-//titaA0000.setTPsbRemFD(mATMTxData.getTxObject().getRequest().getPsbremFD());
				//--ben-20220922-//titaA0000.setTPsbRemFC(mATMTxData.getTxObject().getRequest().getPsbremFC());
			}

			titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		} else {// 信用卡預借現金
			titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno());
			getFeptxn().setFeptxnCbsIntActno(getGeneralData().getMsgCtl().getMsgctlCbsIntActno());
			// 2011/07/29 modify by Ruling for 2011/07/26 永豐書妙要求調整轉出電文內容
			titaA0000.setTPsbMemoD("ATM繳稅");
			titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTroutActno().substring(4, 16));
			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno());
			titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
			if (getPaytype() != null) {
				titaA0000.getTPsbRinfD().put(2, getPaytype().getPaytypeFullname());
			}

		}
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		// modify by husan 2010/12/15
		if (FISCPCode.PCode2531.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2532.getValueStr().equals(getFeptxn().getFeptxnPcode())) {// 繳款
			// for 繳款 2531, 2532 讀取 INBKPARM 取得客戶應付手續費
			if (getInbkparm() == null) {
				// 2012/10/04 Modify by Ruling for 修正當讀不到INBKPARM的資料時要丟Exception出來
				rtnCode = getInbkparmByPK();
				if (rtnCode != FEPReturnCode.Normal) {
					String remark = "GenA1310Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur() + " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
							+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE=" + getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
					getLogContext().setRemark(remark);
					logMessage(Level.INFO, getLogContext());
				}
			}
			getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
			getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());
		} else {// for 全國性繳稅 2568, 2569
			// modify by Ruling 2011/03/07
			// If FepTxn.FEPTXN_NPS_CLR = 1 Then
			// 2011/07/29 modify by Ruling for 2011/07/26 永豐書妙要求調整轉出電文內容
			titaA0000.setTPsbMemoD("全國繳稅");
			if ("01".equals(getFeptxn().getFeptxnRsCode())) {// 轉出單位付費
				getFeptxn().setFeptxnFeeCustpayAct(getFeptxn().getFeptxnFeeCustpay());
				getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnNpsFeeCustpay());
			}

			// 2011/08/23 modify by Ruling for 補T_PSB_RINF_D
			if (titaA0000.getTPsbRinfD().containsKey(1)) {
				titaA0000.getTPsbRinfD().clear();
			}
			titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTrinActno());
			titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
		}

		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		if ("2569".equals(getFeptxn().getFeptxnPcode()) && StringUtils.isNotBlank(getFeptxn().getFeptxnIdno())) {
			// '2011/10/04 Modify by Ruling for 身份證字號過濾空白
			if (getFeptxn().getFeptxnIdno().trim().length() == 11) {
				titaA0000.setIDNO(mappingFirstDigitIdno(getFeptxn().getFeptxnIdno().trim()));
			} else {
				titaA0000.setIDNO(getFeptxn().getFeptxnIdno().trim());
			}
		}
		titaA0000.setPAYTYPE(getFeptxn().getFeptxnPaytype());
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		titaA0000.setTRegTfrType("NN");
		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1050Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		// 利用General class將值正確填入
		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
			if (CURTWD.equals(getFeptxn().getFeptxnTxCurSet())) {// 國內預借現金
				// 因國內信用卡預借現金之帳號與國際卡帳號不同，借用手續費帳號欄位
				titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
			} else {
				titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
			}
			titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		} else {
			if (StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir())) {
				titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
				titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTroutActno().substring(4, 16));
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno());
				titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
				titaA0000.setTPsbMemoC("ATM預現");
			} else {
				// 海外分行至台灣跨區提款交易
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnZoneCode())) {
					switch (getFeptxn().getFeptxnZoneCode()) {
						case ZoneCode.HKG: // 香港卡至台灣跨區提款
							titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
							break;
						case ZoneCode.MAC: // 澳門卡至台灣跨區提款
							// 因澳門與香港帳號不同，借用手續費帳號欄位
							titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(0, 7) + "2"
									+ getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(8, 16)); // 因澳門與香港帳號不同，借用手續費帳號欄位
							break;
					}
				}
				titaA0000.setTPsbRemSD(getFeptxn().getFeptxnAtmno());
				titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmno());
				titaA0000.setTPsbMemoD("ATM現金");
			}
		}
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && "2".equals(getFeptxn().getFeptxnNoticeId().trim())) {
			titaA0000.setCreditAcctNo(ATMPConfig.getInstance().getOutATMIntActno());
		} else {
			titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
					+ getFeptxn().getFeptxnAtmBrno());
		}

		// 港澳NCB:代理跨區提款(A1050)存入不冋欄位
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
			// 代理跨區提款(A1050)
			getFeptxn().setFeptxnVirCbsSupActno(titaA0000.getDebitAcctNo());// 借方帳號
			getFeptxn().setFeptxnVirCbsIntActno(titaA0000.getCreditAcctNo());// 貸方帳號
		} else {
			// 其他代理提款
			getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1051Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAcctNo(
				getFeptxn().getFeptxnTxCur() + getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13)
						+ getFeptxn().getFeptxnAtmBrno());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setTForeignerFlg(getFeptxn().getFeptxnRemark());
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1052Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt()); // 交易金額
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt()); // 提領金額

		// 借用 FEPTXN_NOTICE_ID 欄位存放 ATM 行外記號
		if ("2".equals(getFeptxn().getFeptxnNoticeId())) {
			titaA0000.setCreditAcctNo(ATMPConfig.getInstance().getOutATMIntActno());
		} else {
			titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
					+ getFeptxn().getFeptxnAtmBrno());
		}
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());

		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1370Body(HashMap<String, String> titaBody) throws Exception {

		T24TITAA0000 titaA0000 = new T24TITAA0000();

		// 利用General class將值正確填入
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		// 轉入帳號前五碼=虛擬帳號(00598)，比照信用卡
		if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())
				&& !CMNConfig.getInstance().getVirtualActno().equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
			// FEPTXN_MAJOR_ACTNO改FEPTXN_TRIN_ACTNO
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
			titaA0000.setTPsbMemoD("ATM預現");
			titaA0000.setTPsbMemoC("ATM轉帳");
		} else {
			titaA0000.setTPsbMemoD("ATM預現");
			titaA0000.setTPsbMemoC("信用卡費");
			if ("00".equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno().substring(2, 16));
			} else {
				titaA0000.setCreditAcctNo("CRD" + getFeptxn().getFeptxnTrinActno());
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
			}
		}

		// 2011/12/21 Modify by Ruling for C07電文補存褶欄位
		if (CreditTXCD.C07.toString().equals(getFeptxn().getFeptxnTxCode())) {
			switch (getFeptxn().getFeptxnRemark().trim()) {
				case "0001": // 轉入期貨帳號
					titaA0000.setTPsbMemoD("期貨轉");
					titaA0000.setTPsbMemoC("期貨轉");
					break;
				case "0002": // 轉入證券帳號
					titaA0000.setTPsbMemoD("證券轉");
					titaA0000.setTPsbMemoC("證券轉");
					break;
				case "0003": // 網路預現
					titaA0000.setTPsbMemoD("網路預現");
					titaA0000.setTPsbMemoC("網路預現");
					break;
			}
		}

		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		// T.PSB.RINF.C(2)放FUT/轉出行庫代號/轉出帳號/戶名/ID
		if (CreditTXCD.C07.toString().equals(getFeptxn().getFeptxnTxCode())) {
			// C07 轉出帳號固定是03900100001883
			titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^"
					+ getFeptxn().getFeptxnMajorActno().substring(2, 16) + "^^" + getFeptxn().getFeptxnIdno());
		} else {
			if ("00".equals(getFeptxn().getFeptxnTroutActno().substring(0, 2))) {
				// 一般帳號
				titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^"
						+ getFeptxn().getFeptxnTroutActno().substring(2, 16) + "^^");
			} else {
				// 信用卡號
				titaA0000.getTPsbRinfC().put(2,
						"FUT^" + getFeptxn().getFeptxnTroutBkno() + "^" + getFeptxn().getFeptxnTroutActno() + "^^");
			}
		}
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTroutActno().substring(4, 16));
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTroutActno());
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		// (2)放FUT/轉入行庫代號/轉入帳號/戶名/ID
		if (CreditTXCD.C07.toString().equals(getFeptxn().getFeptxnTxCode())) {
			// C07 固定轉入本行一般帳號
			titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^"
					+ getFeptxn().getFeptxnTrinActno().substring(2, 16) + "^^" + getFeptxn().getFeptxnIdno());
		} else {
			if ("00".equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
				// 一般帳號
				titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^"
						+ getFeptxn().getFeptxnTrinActno().substring(2, 16) + "^^");
			} else {
				// 信用卡號
				titaA0000.getTPsbRinfD().put(2,
						"FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
			}
		}
		titaA0000.setTRegTfrType("NN");

		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1470Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		Intltxn defINTLTXN = new Intltxn();
		// DBINTLTXN dbINTLTXN = new DBINTLTXN(FEPConfig.DBName);
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		if (getInbkparm() == null) {
			// 修正當讀不到INBKPARM的資料時要丟Exception出來
			rtnCode = getInbkparmByPK();
			if (rtnCode != FEPReturnCode.Normal) {
				String remark = "GenA1470Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur()
						+ " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
						+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE="
						+ getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
		}
		getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
		getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());
		// 借貸帳號相反
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setTPsbMemoD("國外查詢");
		titaA0000.setTRegTfrType("NN");

		// 取得代理單位國別碼
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
			defINTLTXN.setIntltxnTxDate(getFeptxn().getFeptxnTxDate());
			defINTLTXN.setIntltxnEjfno(getFeptxn().getFeptxnEjfno());
			Intltxn intltxn = intltxnMapper.selectByPrimaryKey(defINTLTXN.getIntltxnTxDate(),
					defINTLTXN.getIntltxnEjfno());
			if (intltxn != null) {
				// 2013/07/10 Modify by Ruling for 加Trim去掉地區簡稱前面的空白
				// 取得代理單位國別碼
				titaA0000.setTPsbRemSD(intltxn.getIntltxnAcqCntry().trim()); // 地區簡稱
				titaA0000.setTPsbRemFD(intltxn.getIntltxnAcqCntry().trim());// 地區簡稱
			}
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA2910Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();

		if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
			// 企業入金存摺欄位&T_REG_TFR_TYPE要放"NN"
			// 2011/07/26 永豐書妙要求調整轉出電文內容
			if (getFeptxn().getFeptxnTrinActno().substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno())
					|| ATMTXCD.BDR.toString().equals(getFeptxn().getFeptxnTxCode())) {
				// ATM無卡繳信用卡費註記調整：存摺摘要(T.PSB.MEMO.C)顯示「無卡繳費」
				if (ATMTXCD.BDR.toString().equals(getFeptxn().getFeptxnTxCode())) {
					titaA0000.setTPsbMemoC("ATM 現金");
				} else if ((ATMTXCD.CDR.toString().equals(getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.CFT.toString().equals(getFeptxn().getFeptxnTxCode()))) {
					titaA0000.setTPsbMemoD("無卡繳費");
				} else {
					titaA0000.setTPsbMemoC("信用卡費");
				}
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno().substring(2, 16));
				titaA0000.setTRegTfrType("NN");
			} else {
				// 現金存款
				titaA0000.setTPsbRemSC(getFeptxn().getFeptxnAtmno());
				titaA0000.setTPsbRemFC(getFeptxn().getFeptxnAtmno());
				// ATM中文備註：T_PSB_REM_F_C改為Muti，另外應永豐要求存款交易ATM中文備註多寫入存摺(T_PSB_REM_S_C)
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
					titaA0000.setTPsbRemSC(getFeptxn().getFeptxnChrem());
					titaA0000.getTPsbRemFCMuti().put(1, titaA0000.getTPsbRemFC());
					titaA0000.getTPsbRemFCMuti().put(2, getFeptxn().getFeptxnChrem());
				}
				titaA0000.setTPsbMemoC("ATM 現金");
				titaA0000.getTPsbRinfC().put(2,
						"FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
			}
		} else if ("C".equals(getFeptxn().getFeptxnTrinKind()) || "M".equals(getFeptxn().getFeptxnTrinKind())) {
			titaA0000.setCreditAcctNo("CRD" + getFeptxn().getFeptxnTrinActno());
			titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
			titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
			// ATM無卡繳信用卡費註記調整：存摺摘要(T.PSB.MEMO.C)顯示「無卡繳費」
			if ((ATMTXCD.CDR.toString().equals(getFeptxn().getFeptxnTxCode()))
					|| (ATMTXCD.CFT.toString().equals(getFeptxn().getFeptxnTxCode()))) {
				titaA0000.setTPsbMemoC("無卡繳費");
			} else {
				titaA0000.setTPsbMemoC("信用卡費");
			}
			titaA0000.setTRegTfrType("NN");
		} else if ("G".equals(getFeptxn().getFeptxnTrinKind())) {
			titaA0000.setCreditAcctNo("GIF" + getFeptxn().getFeptxnTrinActno());
			titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));
			titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
			titaA0000.setTPsbMemoC("Gift卡");
			titaA0000.setTRegTfrType("NN");
		}
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());

		// ATM新功能-現金損款
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
			// 修改 for 掌靜脈存款(PDR/PCR)
			if (ATMTXCD.PDR.toString().equals(getFeptxn().getFeptxnTxCode())
					|| ATMTXCD.PCR.toString().equals(getFeptxn().getFeptxnTxCode())) {
				titaA0000.setIcActno("");
			} else {
				titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
			}
		}
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && "2".equals(getFeptxn().getFeptxnNoticeId().trim())) {
			titaA0000.setDebitAcctNo(ATMPConfig.getInstance().getOutATMIntActno());
		} else {
			if (getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
				titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
						+ getFeptxn().getFeptxnAtmBrno());
			} else {
				titaA0000.setDebitAcctNo("");
			}
		}

		getFeptxn().setFeptxnCbsIntActno(titaA0000.getDebitAcctNo());

		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());

		// 修改 FOR 菓菜市場企業入金機
		if ("BDF".equals(getFeptxn().getFeptxnTxCode())) {
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnRemark())) {
				// 將銷帳編號寫入T24電文欄位
				titaA0000.setTBrSeqno(getFeptxn().getFeptxnRemark().trim());
			}
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	private void genA2930Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		// 借用 FEPTXN_NOTICE_ID 欄位存放 ATM 行外記號
		if ("2".equals(getFeptxn().getFeptxnNoticeId())) {
			titaA0000.setDebitAcctNo(ATMPConfig.getInstance().getOutATMIntActno());
		} else {
			if (StringUtils.isNotBlank(getGeneralData().getMsgCtl().getMsgctlCbsIntActno())
					&& getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
				titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
						+ getFeptxn().getFeptxnAtmBrno());
			} else {
				titaA0000.setDebitAcctNo("");
			}
		}
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getDebitAcctNo());

		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCur());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());

		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
			titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		}

		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());

		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnAtmProfit());
		if (titaA0000.getAccrChgAmt().compareTo(BigDecimal.ZERO) < 0) {
			titaA0000.setAccrChgAmt(BigDecimal.ZERO);
		}

		titaA0000.setFeePayer("2"); // 手續費付費單位-轉入單位付費
		titaA0000.setTPsbRemSC(getFeptxn().getFeptxnAtmno());
		titaA0000.setTPsbRemFC(getFeptxn().getFeptxnAtmno());
		titaA0000.setTPsbMemoC("ATM 現金");
		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.getTPsbRinfC().put(2,
				"FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno() + "^^");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1020Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		if (getInbkparm() == null) {
			// 2012/10/04 Modify by Ruling for 修正當讀不到INBKPARM的資料時要丟Exception出來
			rtnCode = getInbkparmByPK();
			if (rtnCode != FEPReturnCode.Normal) {
				String remark = "GenA1020Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur()
						+ " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
						+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE="
						+ getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
		}
		getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());

		// 2011/12/05 Modify by Ruling for 本行卡國際提款，本金及手續費分開
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct()); // 帳戶交易金額
			titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());// 提領金額
			titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
			titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct()); // 手續費
		} else {
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct()));
			titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct()));
		}
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString()); // 存摺備註
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString()); // 備註說明
		titaA0000.setTRegTfrType("NW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1021Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		BigDecimal w_TOT_AMT = new BigDecimal(0);
		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());// 借方帳號
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno()); // 卡片主帳號

		// 調整借方金額=提領金額(外幣)*匯率(四捨五入至元)
		w_TOT_AMT = MathUtil.roundUp(getFeptxn().getFeptxnTxAmt().multiply(getFeptxn().getFeptxnExrate()), 0);
		titaA0000.setDebitAmount(w_TOT_AMT); // 借方金額
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno()); // 貸方帳號
		// 調整貸方金額=提領金額(外幣)*匯率(四捨五入至元)
		titaA0000.setCreditAmount(w_TOT_AMT); // 貸方金額
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());// 交易匯率
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());// 手續費帳號
		// 調整手續費=清算金額+台方手續費-借方金額
		titaA0000.setAccrChgAmt(
				getFeptxn().getFeptxnTxAmtAct().add(getFeptxn().getFeptxnFeeCustpayAct()).subtract(w_TOT_AMT));

		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan()); // 財金序號
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr()); // 存褶摘要
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());// 存褶備註
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString()); // 往來明細
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnRemark().trim()); // 借用欄位=收單國別碼(3)+代理行代號(8)+端末設備代號(8)
		titaA0000.getTPsbRinfD().put(2, getFeptxn().getFeptxnExrate().toString());
		titaA0000.setTRegTfrType("NW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA2021Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		BigDecimal w_TOT_AMT = new BigDecimal(0);
		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno()); // 借方帳號
		// 調整借方金額=提領金額(外幣)*匯率(四捨五入至元)
		w_TOT_AMT = MathUtil.roundUp(getFeptxn().getFeptxnTxAmt().multiply(getFeptxn().getFeptxnExrate()), 0);
		titaA0000.setDebitAmount(w_TOT_AMT); // 借方金額
		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTroutActno());// 貸方帳號
		// 調整借方金額=提領金額(外幣)*匯率(四捨五入至元)
		titaA0000.setCreditAmount(w_TOT_AMT); // 貸方金額
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate()); // 交易匯率
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());// 財金序號
		titaA0000.setOriTxDate(getFeptxn().getFeptxnDueDate());// 原交易日期

		if (StringUtils.isNotBlank(getFeptxn().getFeptxnDueDate())
				&& getFeptxn().getFeptxnDueDate().equals(getFeptxn().getFeptxnTxDate())) {
			titaA0000.setTRegTfrType("NW"); // 當日沖正需更新限額
		} else {
			titaA0000.setTRegTfrType("NN"); // 隔日沖正或退貨交易不需更新限額
		}

		titaA0000.setTPsbMemoC(getGeneralData().getMsgCtl().getMsgctlCbsMemoCr());
		titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno()); // 手續費帳號
		titaA0000.setAccrChgAmt(BigDecimal.ZERO); // 手續費金額
		titaA0000.setFeePayer("1");
		// 存摺備註(T_PSB_REM_S_C)及往來明細(T_PSB_REM_F_C)加提領金額
		titaA0000.setTPsbRemSC("提領金額" + getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		titaA0000.setTPsbRemFC("提領金額" + getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnRemark().trim()); // 借用欄位=收單國別碼(3)+代理行代號(8)+端末設備代號(8)
		titaA0000.getTPsbRinfC().put(2, getFeptxn().getFeptxnExrate().toString());
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	private void genA1710Body(HashMap<String, String> titaBody) throws Exception {

		T24TITAA0000 titaA0000 = new T24TITAA0000();
		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmType())
				&& (FISCType.Type659A.equals(getFeptxn().getFeptxnAtmType())
						|| ("6".equals(getFeptxn().getFeptxnAtmType().substring(0, 1))
								&& ("B".equals(getFeptxn().getFeptxnAtmType().substring(3, 4))
										|| "C".equals(getFeptxn().getFeptxnAtmType().substring(3, 4)))))) {
			// 豐錢包APP交易
			titaA0000.setTPsbRemSD("台灣Pay");
			titaA0000.setTPsbRemFD("台灣Pay購物付款");
		} else {
			// 晶片交易
			if (mFISCData != null && StringUtils.isNotBlank(getFeptxn().getFeptxnMerchantId())) {
				if (mFISCData.getMerchant().getMerchantAbbnm().length() > 6) {
					titaA0000.setTPsbRemSD(mFISCData.getMerchant().getMerchantAbbnm().substring(0, 6));
				} else {
					titaA0000.setTPsbRemSD(mFISCData.getMerchant().getMerchantAbbnm());
				}
				titaA0000.setTPsbRemFD(mFISCData.getMerchant().getMerchantName());
			}
		}
		titaA0000.setTRegTfrType("NW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1711Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno()); // 借方帳號
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno()); // 卡片主帳號
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct()); // 借方金額
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno()); // 貸方帳號
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());// 貸方金額
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate()); // 交易匯率
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan()); // 財金序號
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		if (mFISCData != null && StringUtils.isNotBlank(getFeptxn().getFeptxnMerchantId())) {
			if (mFISCData.getMerchant().getMerchantAbbnm().length() > 6) {
				titaA0000.setTPsbRemSD(mFISCData.getMerchant().getMerchantAbbnm().substring(0, 6));
			} else {
				titaA0000.setTPsbRemSD(mFISCData.getMerchant().getMerchantAbbnm());
			}
			titaA0000.setTPsbRemFD(mFISCData.getMerchant().getMerchantName());
		}

		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnRemark().trim()); // 借用欄位=收單國別碼(3)+代理行代號(8)+端末設備代號(8)
		titaA0000.getTPsbRinfD().put(2, getFeptxn().getFeptxnExrate().toString());
		titaA0000.setTRegTfrType("NW");

		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA2710Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();

		// 利用General class將值正確填入
		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setOriTxDate(getFeptxn().getFeptxnDueDate());
		// 2011/5/30 modify by Ashiang SPEC異動
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnDueDate())
				&& getFeptxn().getFeptxnTxDate().equals(getFeptxn().getFeptxnDueDate())) {
			titaA0000.setTRegTfrType("NW"); /// *當日沖正需更新限額*/
		} else {
			titaA0000.setTRegTfrType("NN"); /// *隔日沖正或退貨交易不需更新限額*/
		}
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		titaA0000.setTPsbMemoC(getGeneralData().getMsgCtl().getMsgctlCbsMemoCr());

		if (FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmType())
					&& ("6".equals(getFeptxn().getFeptxnAtmType().substring(0, 1))
							&& ("B".equals(getFeptxn().getFeptxnAtmType().substring(3, 4))
									|| "C".equals(getFeptxn().getFeptxnAtmType().substring(3, 4))))) {
				// 豐錢包APP交易
				titaA0000.setTPsbRemSC("台灣Pay");
				titaA0000.setTPsbRemFC("台灣Pay購物取消");
			} else {
				// 晶片交易
				if (mFISCData.getMerchant() == null) {
					Merchant merchant = new Merchant();
					// DBMERCHANT dbTxn = new DBMERCHANT(FEPConfig.DBName);

					if (!(StringUtils.isBlank(getFeptxn().getFeptxnMerchantId()))
							&& getFeptxn().getFeptxnMerchantId().length() >= 15) {
						merchant.setMerchantId(getFeptxn().getFeptxnMerchantId().substring(0, 15));
					} else {
						merchant.setMerchantId(getFeptxn().getFeptxnMerchantId());
					}
					merchant = merchantMapper.selectByPrimaryKey(merchant.getMerchantId());
					if (merchant == null) {
						// 2012/10/04 Modify by Ruling for 將錯誤訊息顯示在文字紀錄檔內
						String remark = "特店代號錯誤, MERCHANT_ID=" + getFeptxn().getFeptxnMerchantId();
						getLogContext().setRemark(remark);
						this.logMessage(getLogContext());
						throw ExceptionUtil.createException(remark);
					}
					mFISCData.setMerchant(merchant);
				}

				if (mFISCData != null && mFISCData.getMerchant().getMerchantAbbnm().length() > 6) {
					titaA0000.setTPsbRemSC(mFISCData.getMerchant().getMerchantAbbnm().substring(0, 6));
				} else {
					titaA0000.setTPsbRemSC(mFISCData.getMerchant().getMerchantAbbnm());
				}

				titaA0000.setTPsbRemFC(mFISCData.getMerchant().getMerchantName());
			}

		} else if (FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
			Merchant merchant = new Merchant();
			// DBMERCHANT dbTxn = new DBMERCHANT(FEPConfig.DBName);
			if (!(StringUtils.isBlank(getFeptxn().getFeptxnMerchantId()))
					&& getFeptxn().getFeptxnMerchantId().length() >= 15) {
				merchant.setMerchantId(getFeptxn().getFeptxnMerchantId().substring(0, 15));
			} else {
				merchant.setMerchantId(getFeptxn().getFeptxnMerchantId());
			}
			merchant = merchantMapper.selectByPrimaryKey(merchant.getMerchantId());
			if (merchant == null) {
				// 讀不到特店檔
				titaA0000.setTPsbRemSC("消費退費");
				titaA0000.setTPsbRemFC(
						StringUtils.rightPad(getFeptxn().getFeptxnMerchantId(), 30, ' ').substring(0, 15));
			} else {
				// 有讀到特店檔
				if (merchant.getMerchantAbbnm().length() > 6) {
					titaA0000.setTPsbRemSC(merchant.getMerchantAbbnm().substring(0, 6));
				} else {
					titaA0000.setTPsbRemSC(merchant.getMerchantAbbnm());
				}
				titaA0000.setTPsbRemFC(merchant.getMerchantName());
			}
		}
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA2711Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno()); // 借方帳號
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct()); // 借方金額
		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTroutActno()); // 貸方帳號
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());// 貸方金額
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate()); // 交易匯率
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan()); // 財金序號
		titaA0000.setOriTxDate(getFeptxn().getFeptxnDueDate());// 原交易日期
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnDueDate())
				&& getFeptxn().getFeptxnDueDate().equals(getFeptxn().getFeptxnTxDate())) {
			titaA0000.setTRegTfrType("NW"); // 當日沖正需更新限額
		} else {
			titaA0000.setTRegTfrType("NN"); // 隔日沖正或退貨交易不需更新限額
		}
		titaA0000.setTPsbMemoC(getGeneralData().getMsgCtl().getMsgctlCbsMemoCr());
		if (mFISCData.getMerchant() == null) {
			Merchant merchant = new Merchant();
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnMerchantId())
					&& getFeptxn().getFeptxnMerchantId().length() >= 15) {
				merchant.setMerchantId(getFeptxn().getFeptxnMerchantId().substring(0, 15));
			} else {
				merchant.setMerchantId(getFeptxn().getFeptxnMerchantId());
			}
			merchant = merchantMapper.selectByPrimaryKey(merchant.getMerchantId());
			if (merchant == null) {
				// 2012/10/04 Modify by Ruling for 將錯誤訊息顯示在文字紀錄檔內
				String remark = "特店代號錯誤, MERCHANT_ID=" + getFeptxn().getFeptxnMerchantId();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
			mFISCData.setMerchant(merchant);
		}
		if (mFISCData != null && mFISCData.getMerchant().getMerchantAbbnm().length() > 6) {
			titaA0000.setTPsbRemSC(mFISCData.getMerchant().getMerchantAbbnm().substring(0, 6));
		} else {
			titaA0000.setTPsbRemSC(mFISCData.getMerchant().getMerchantAbbnm());
		}
		titaA0000.setTPsbRemFC(mFISCData.getMerchant().getMerchantName());
		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnRemark().trim()); // 借用欄位=收單國別碼(3)+代理行代號(8)+端末設備代號(8)
		titaA0000.getTPsbRinfC().put(2, getFeptxn().getFeptxnExrate().toString());
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA1060Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno()); // 借方帳號
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());// 卡片主帳號
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtSet()); // 借方金額
		titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno()); // 貸方帳號
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtSet());// 貸方金額
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());// 財金序號
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		titaA0000.setTPsbRemSD("支付寶付款");
		titaA0000.setTPsbRemFD("支付寶付款");
		titaA0000.setTRegTfrType("NB");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genA2060Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		titaA0000.setDebitAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsSupActno()); // 借方帳號
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtSet()); // 借方金額
		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTroutActno()); // 貸方帳號
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtSet()); // 貸方金額
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan()); // 財金序號
		titaA0000.setOriTxDate(getFeptxn().getFeptxnDueDate()); // 原交易日期
		titaA0000.setTRegTfrType("NB");
		titaA0000.setTPsbMemoC(getGeneralData().getMsgCtl().getMsgctlCbsMemoCr());
		titaA0000.setTPsbRemSC("支付寶退款");
		titaA0000.setTPsbRemFC("支付寶退款");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	private void genB4000Body(HashMap<String, String> titaBody) {
		T24TITAB0000 titaB0000 = new T24TITAB0000();
		// 利用General class將值正確填入
		titaB0000.setAccountNumber(getFeptxn().getFeptxnTroutActno().substring(2, 16));
		titaB0000.setLockedAmount(getFeptxn().getFeptxnTxAmtAct());
		titaB0000.setDESCRIPTION(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		// 利用General class的GenDictionary建立T24的titaBody
		titaB0000.genDictionary(titaBody);
	}

	private void genB5000Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAB0000 titaB0000 = new T24TITAB0000();
		// 利用General class將值正確填入
		titaB0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno().substring(2, 16));
		titaB0000.setDebitCurrency(getFeptxn().getFeptxnTxCur());
		titaB0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		titaB0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());
		titaB0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaB0000.setTxnDate(getFeptxn().getFeptxnOrderDate());
		titaB0000.setFiscPcode(getFeptxn().getFeptxnPcode());
		titaB0000.setAtmIcSeqno(getFeptxn().getFeptxnIcSeqno());
		titaB0000.setAtmIcMark(getFeptxn().getFeptxnIcmark());
		titaB0000.setAtmChkType(getFeptxn().getFeptxnAtmChk());
		titaB0000.setAtmMchType(getFeptxn().getFeptxnAtmType());
		titaB0000.setTNwfInst(getFeptxn().getFeptxnBusinessUnit());
		titaB0000.setTNwfType(getFeptxn().getFeptxnPaytype());
		titaB0000.setTNwfCode(getFeptxn().getFeptxnPayno());
		titaB0000.setAtmTaxUnit(getFeptxn().getFeptxnTaxUnit());
		titaB0000.setAtmDueDate(getFeptxn().getFeptxnDueDate());
		titaB0000.setAtmRcnSeqno(getFeptxn().getFeptxnReconSeqno());
		titaB0000.setAtmCustId(getFeptxn().getFeptxnIdno());
		// 2012-01-06 Modify by Ruling for B5000 +欄位
		titaB0000.setTTxnCode("T1000");
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {

			if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
				// 轉入一般帳號
				titaB0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
				titaB0000.setVrAcPrefC(getVirtualAccountPrefix(titaB0000.getCreditAcctNo()));
			} else {
				// 繳永豐信用卡款
				switch (getFeptxn().getFeptxnTrinKind()) {
					case BINPROD.Credit:
					case BINPROD.Combo:
						titaB0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
						titaB0000.setVrAcPrefC("CRD");
						break;
				}
			}

			// 當前兩位為00時，才substring
			if ("00".equals(titaB0000.getCreditAcctNo().substring(0, 2))) {
				titaB0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno().substring(2, 16));
			}
			// 永豐書妙要求調整轉出電文內容
			if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind()) && !CMNConfig.getInstance().getVirtualActno()
					.equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
				titaB0000.setTPsbMemoD("ATM預約");
				titaB0000.setTPsbMemoC("ATM轉帳");
				titaB0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaB0000.setTPsbRemSC(getFeptxn().getFeptxnTroutActno().substring(4, 16));

				titaB0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());
				// 2011/08/01 modify by Ruling 永豐要求 Psb.Rinf.C、Psb.Rinf.D 的帳號前不可以多加兩個"00"
				titaB0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
				titaB0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^"
						+ getFeptxn().getFeptxnTrinActno().substring(2, 16) + "^^");
				titaB0000.getTPsbRinfD().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
				titaB0000.getTPsbRinfD().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");
				titaB0000.setTPsbRemFC(getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());
				// 2011/08/01 modify by Ruling 永豐要求 Psb.Rinf.C、Psb.Rinf.D 的帳號前不可以多加兩個"00"
				titaB0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno().substring(2, 16));
				titaB0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^"
						+ getFeptxn().getFeptxnTroutActno().substring(2, 16) + "^^");
				titaB0000.getTPsbRinfC().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
				titaB0000.getTPsbRinfC().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");
			} else {
				// 繳永豐信用卡款
				titaB0000.setTPsbMemoD("信用卡費");
				titaB0000.setTPsbMemoC("信用卡費");
				titaB0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
				titaB0000.setTPsbRemSC(getFeptxn().getFeptxnTrinActno().substring(4, 16));

				if ("00".equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
					titaB0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno().substring(2, 16));
					titaB0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno().substring(2, 16));
				} else {
					// 信用卡號
					titaB0000.setTPsbRemFD(getFeptxn().getFeptxnTrinActno());
					titaB0000.setTPsbRemFC(getFeptxn().getFeptxnTrinActno());
				}

				titaB0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
				titaB0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
			}

			if (getCard().getCardTfrFlag() == 2) {/// *限制約轉*/
				titaB0000.setTRegTfrType("CR");
			} else {
				// 2011/07/12 modify by Ruling 自行轉帳繳永豐信用卡款及轉入帳號前五碼或繳信用卡款之虛擬帳號為00598時，改為NR
				// Card.CARD_TFR_FLAG = 3
				if ((BINPROD.Credit.equals(getFeptxn().getFeptxnTrinKind())
						|| BINPROD.Combo.equals(getFeptxn().getFeptxnTrinKind()))
						|| CMNConfig.getInstance().getVirtualActno()
								.equals(getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
					titaB0000.setTRegTfrType("NR");
				}
			}

		} else {// 跨行交易
			// 2014/10/21 modify by Ruling for 跨行預約交易只能做轉帳IFT2521
			titaB0000.setFiscPcode("2521");
			titaB0000.setCreditBankCode(getFeptxn().getFeptxnTrinBkno());
			if (getCard().getCardTfrFlag() == 2) {/// *限制約轉*/
				titaB0000.setTRegTfrType("CR");
			}
			// 2011/10/31 modify by Ruling for 跨行預約交易存褶欄位同A1110
			titaB0000.setTPsbRemSD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno().substring(7, 16));
			titaB0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());
			// 2012/03/06 modify by Ruling for T.PSB.RINF.D(1)放ATM NO、T.PSB.RINF.C(2)放交易日期
			titaB0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnAtmno());
			titaB0000.getTPsbRinfD().put(2, getFeptxn().getFeptxnTxDate());
			// 2011/10/31 modify by Ruling for 跨行預約交易
			titaB0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
			titaB0000.setTPsbMemoD("ATM預約");
		}

		// 因自跨行預約皆要寫件備註，移至這裡
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
			if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
				// 預約自行轉帳
				titaB0000.setTPsbRemSD(getFeptxn().getFeptxnChrem());
				titaB0000.setTPsbRemSC(getFeptxn().getFeptxnChrem());
				titaB0000.getTPsbRemFDMuti().put(1, titaB0000.getTPsbRemFD());
				titaB0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnChrem());
				titaB0000.getTPsbRemFDMuti().put(1, titaB0000.getTPsbRemFC());
				titaB0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnChrem());
			} else {
				// 預約跨行轉帳交易
				titaB0000.setTPsbRemSD(getFeptxn().getFeptxnChrem());
				titaB0000.getTPsbRemFDMuti().put(1, titaB0000.getTPsbRemFD());
				titaB0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnChrem());
			}
		}
		// 利用General class的GenDictionary建立T24的titaBody
		titaB0000.genDictionary(titaBody);
	}

	private void genA1000Body(HashMap<String, String> titaBody, String CBSTxid) throws Exception {

		T24TITAA0000 titaA0000 = new T24TITAA0000();

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());

		if (getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
			if (CBSTxid.equals(T24Version.A1000)) {
				// 自行存款
				// 自行提款(IWD) 改用FEPTXN_NOTICE_ID存放ATM行外記號 &
				// 帳號改抓共用參數-行外ATM庫現內部帳號
				// 行外ATM自行提款(IWD)借用手續費帳號欄位
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId()) && "2".equals(getFeptxn().getFeptxnNoticeId().trim())) {
					// 行外ATM自行提款(IWD)借用手續費帳號欄位
					titaA0000.setCreditAcctNo(ATMPConfig.getInstance().getOutATMIntActno());
				} else {
					titaA0000.setCreditAcctNo(getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(0, 13)
							+ getFeptxn().getFeptxnAtmBrno());
				}
			} else {
				// 外幣提款MSGCTL_CBS_INT_ACTNO.substring(3, 13)
				titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCur().trim()
						+ getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13)
						+ getFeptxn().getFeptxnAtmBrno());
			}
		} else {
			titaA0000.setCreditAcctNo("");
		}

		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());

		if (CURTWD.equals(getFeptxn().getFeptxnTxCurAct())) {
			// 本行台幣帳戶
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		} else {
			// 本行原幣帳戶
			titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		}

		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpay());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());

		// 外幣提款才有手續費帳號
		if (CBSTxid.equals(T24Version.A1040)) {
			titaA0000.setChgAccount(getGeneralData().getMsgCtl().getMsgctlCbsFeeActno());
		}

		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		// 調整台幣戶提領外幣(A1040)之存褶備註
		if (CBSTxid.equals(T24Version.A1040) && !"002".equals(getFeptxn().getFeptxnTroutActno().substring(5, 8))
				&& !"008".equals(getFeptxn().getFeptxnTroutActno().substring(5, 8))) {
			titaA0000.setTPsbRemSD(getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()).getCurcdCurName().trim());
			titaA0000.setTPsbRemFD(getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()).getCurcdCurName().trim());
		} else {
			titaA0000.setTPsbRemSD(getFeptxn().getFeptxnAtmno());
			titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmno());
		}

		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.setTRegTfrType("NW");
		addGeneralField(titaA0000);
		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);
	}

	/**
	 * 組送T24電文
	 * 1.依據不同送主機電文編號，組送主機電文欄位
	 * 2.以UnysisAdapter作為介面和海外主機溝通
	 * 3.若主機回應成功，更新部分FEPTXN欄位
	 * 4.若Timeout，新增CBSPEND檔作為記錄
	 * 
	 * @param defFWDRST 預約交易結果檔
	 * @return FEPReturnCode
	 * 
	 */
	public FEPReturnCode sendToT24forRETFR(Fwdrst defFWDRST) {
		FEPReturnCode rtnCode = CommonReturnCode.ProgramException;
		String company = null;
		T24PreClass t24ReqMessage = null;
		T24Adapter adapter = new T24Adapter(getGeneralData());

		try {
			company = ATMPConfig.getInstance().getTWDCompanyCode().substring(0, 5) + "9999";
			t24ReqMessage = new T24PreClass();
			t24ReqMessage.setUserName(ATMPConfig.getInstance().getT24UserName());
			t24ReqMessage.setPassword(ATMPConfig.getInstance().getT24Password());
			t24ReqMessage.setCompany(company);
			t24ReqMessage.setTransactionId(defFWDRST.getFwdrstTxId()); // 借用存放預約序號

			T24TITAC0000 titaC0000 = new T24TITAC0000();
			switch (defFWDRST.getFwdrstTxrust()) {
				case "A":
					titaC0000.setProcTxnSts("31"); // FEP Processed Successfully
					break;
				case "R":
				case "C":
					titaC0000.setProcTxnSts("33"); // FEP Corrected
					break;
				default:
					titaC0000.setProcTxnSts("32"); // FEP Processed Fail
					break;
			}

			if (StringUtils.isBlank(defFWDRST.getFwdrstReplyCode()) || "0000".equals(defFWDRST.getFwdrstReplyCode())) {
				titaC0000.setProcRetCode("");
			} else {
				titaC0000.setProcRetCode(defFWDRST.getFwdrstReplyCode() + defFWDRST.getFwdrstErrMsg());
			}
			titaC0000.setProcAppName("TMB.CHL.FUNDS.TRANSFER?" + defFWDRST.getFwdrstCbsTxCode());
			titaC0000.setProcAppId(defFWDRST.getFwdrstCbsRrn());
			titaC0000.setProcDate(new SimpleDateFormat("yyyyMMdd").format(defFWDRST.getUpdateTime()));
			titaC0000.setProcTime(new SimpleDateFormat("HHmmss").format(defFWDRST.getUpdateTime()));

			// 利用General class的GenDictionary建立T24的titaBody
			titaC0000.genDictionary(t24ReqMessage.getTITABody());

			adapter.messageToT24 = t24ReqMessage.genT24ReqOFSForRETFR();

			getLogContext().setRemark("發送T24通知電文單筆重發處理結果");
			logMessage(Level.INFO, getLogContext());

			rtnCode = adapter.sendReceive();

			// 設定TIMER等待T24主機回應訊息
			if (rtnCode == CommonReturnCode.HostResponseTimeout || rtnCode == CommonReturnCode.ProgramException || rtnCode == CommonReturnCode.CBSResponseError) {
				sendEmail("預約序號=" + defFWDRST.getFwdrstTxId() + " 通知T24電文，發送失敗");
				return rtnCode;
			}

			if (adapter.getMessageFromT24().contains("1 record processed :")) {
				return rtnCode;
			} else {
				sendEmail("預約序號=" + defFWDRST.getFwdrstTxId() + " 通知T24電文，Response回應的格式不對");
				return CommonReturnCode.CBSResponseError;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".sendToT24forRETFR");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 發Mail
	 * 
	 * @return Boolean
	 * 
	 */
	private boolean sendEmail(String mailContext) {
		return true;
	}

}
