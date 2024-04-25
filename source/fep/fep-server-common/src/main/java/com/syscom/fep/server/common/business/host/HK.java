package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.IsocodeExtMapper;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.T24Adapter;
import com.syscom.fep.server.common.adapter.UnisysAdapter;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.T24Version;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.*;
import com.syscom.fep.vo.text.t24.T24PreClass;
import com.syscom.fep.vo.text.t24.T24TITAA0000;
import com.syscom.fep.vo.text.t24.T24TITAB0000;
import com.syscom.fep.vo.text.unisys.hk.HKGeneral;
import com.syscom.fep.vo.text.unisys.hk.HKUnisysTextBase;
import com.syscom.fep.vo.text.unisys.hk.request.*;
import com.syscom.fep.vo.text.unisys.hk.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

public class HK extends HostBase {
	private String _ProgramName = "HK";
	IntltxnMapper dbINTLTXN = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
	CbspendMapper cbspendMapper = SpringBeanFactoryUtil.getBean(CbspendMapper.class);
	IsocodeExtMapper isocodeExtMapper = SpringBeanFactoryUtil.getBean(IsocodeExtMapper.class);
	AllbankMapper allbankMapper = SpringBeanFactoryUtil.getBean(AllbankMapper.class);
//	SmsmsgMapper dbSMSMSG = SpringBeanFactoryUtil.getBean(SmsmsgMapper.class);
	ZoneMapper zoneMapper = SpringBeanFactoryUtil.getBean(ZoneMapper.class);
	BsdaysMapper bsdaysMapper = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
	AtmstatMapper atmstatMapper = SpringBeanFactoryUtil.getBean(AtmstatMapper.class);

	public HK(MessageBase txData) {
		super(txData);
	}

	/**
	 * 組送香港優利主機電文
	 * 1.依據不同送主機電文編號，組送主機電文欄位
	 * 2.以UnysisAdapter作為介面和海外主機溝通
	 * 3.若主機回應成功，更新部分FEPTXN欄位
	 * 4.若Timeout，新增CBSPEND檔作為記錄
	 * 
	 * @param hkCBSTxid 送主機電文id
	 * @param txType 電文類型:交易/沖正
	 * @return FEPReturnCode
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>ATM Business</reason>
	 *         <date>2009/11/20</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>BRSID1(001B0244):海外ATM跨區提款，會回E960錯誤。</reason>
	 *         <date>2010/4/15</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>BRSID1(001B0251):在香港ATM進行澳門卡提款，出現E374(BACKDATE期間餘額不足)。</reason>
	 *         <date>2010/4/16</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>BRSID1(001B0244):FEPTXN_ZONE_CODE 改成FEPTXN_ATM_ZONE</reason>
	 *         <date>2010/4/16</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>BugReport(001B0462):澳門ATM+香港卡：使用港幣帳戶提葡幣(IFE/IFW)，電文送主機時帶的幣別都是「0303」，應該一個是帶港幣，一個葡幣。</reason>
	 *         <date>2010/5/13</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>BugReport(001B0475):台灣ATM+澳門卡查詢(IAC/IIQ)：在IAC發生例外錯誤</reason>
	 *         <date>2010/5/18</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>修改寫法 trmno改到prepare中使用</reason>
	 *         <date>2010/6/21</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode sendToCBSHK(String hkCBSTxid, byte txType) {
		// BugReport(001B0475):若此參數不滿五位，會發生例外因此直接回其他類檢核錯誤
		if (hkCBSTxid.length() < 5) {
			return ATMReturnCode.OtherCheckError;
		}

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		UnisysAdapter adapter = new UnisysAdapter(getGeneralData());
		HKGeneral hkGeneralForReq = new HKGeneral();
		HKGeneral hkGeneralForRes = new HKGeneral();
		RefBase<HKUnisysTextBase> reqClass = new RefBase<HKUnisysTextBase>(null);
		RefBase<HKUnisysTextBase> resClass = new RefBase<HKUnisysTextBase>(null);
		UnisysTXCD unysisTxcd = UnisysTXCD.valueOf(hkCBSTxid);
		String tita = "";
		// 2010-06-21 by kyo 修改寫法 改到prepare中使用
		// Dim trmno As String = "" '輸入行機台號
		// Dim isCrossArea As Boolean = False

		try {
			// 2010-06-21 by kyo for 修改程式類似SPEC寫法:拿掉isCrossArea的使用
			// 判斷跨區/同區交易

			// 準備TITA相關欄位資料
			// 2010-06-21 by kyo for 修改程式類似SPEC寫法:拿掉isCrossArea的使用
			rtnCode = prepareCBSHKRequest(hkCBSTxid, txType, hkGeneralForReq, reqClass, resClass);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			tita = reqClass.get().makeMessageFromGeneral(hkGeneralForReq);

			// Communicate with Unisys by adapter & update FEPTXN
			HostBase hostUtil = new HostBase(getGeneralData());
			rtnCode = hostUtil.sendToHostByAdapter(unysisTxcd, txType, adapter, tita, UnisysType.HKG);
			if (rtnCode == IOReturnCode.FEPTXNUpdateError || rtnCode == CommonReturnCode.ProgramException) {
				return rtnCode;
			}

			// 處理主機回應的電文
			// 2010-06-21 by kyo for 修改程式類似SPEC寫法:拿掉isCrossArea的使用
			rtnCode = processCBSHKResponse(unysisTxcd, txType, rtnCode, hkGeneralForReq, hkGeneralForRes, reqClass.get(), resClass.get(), adapter, tita);

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 組送T24電文
	 * 1.依據不同送主機電文編號，組送主機電文欄位
	 * 2.以HKT24Adapter作為介面和海外主機溝通
	 * 3.若主機回應成功，更新部分FEPTXN欄位
	 * 4.若Timeout，新增CBSPEND檔作為記錄
	 * 
	 * @param CBSTxid 送主機電文id
	 * @param txType 電文類型:交易/沖正
	 * @param processFlag PROCESS(True)/VALIDATE(False)
	 * @return FEPReturnCode
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Ruling</modifier>
	 *         <reason>HK送T24電文使用</reason>
	 *         <date>2013/05/02</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode sendToCBSHKT24(String CBSTxid, byte txType, boolean processFlag) {
		FEPReturnCode rtnCode = CommonReturnCode.ProgramException;
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode2 = null; // INSERT CBSPEND的結果
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

		try {
			if (txType != T24TxType.Accounting.getValue() && txType != T24TxType.EC.getValue()) {
				getLogContext().setRemark("SendToCBSHKT24-TYPE <> 1扣帳 2沖正-TYPE=" + txType);
				this.logMessage(getLogContext());
				return ATMReturnCode.OtherCheckError;
			}

			// 1. 檢核香港T24主機連線狀態
			FEPCache.reloadCache(CacheItem.SYSSTAT);
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatT24Hkg())) {
//				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
//					// 跨行
//					if (StringUtils.isBlank(getFeptxn().getFeptxnTxCode())) {
//						// 原存行交易
//						return FISCReturnCode.ReceiverBankOperationStop; // 0206 收信單位主機未在跨行作業運作狀態
//					} else {
//						// 代理行交易
//						return FISCReturnCode.SenderBankServiceStop; // 0202 發信單位該項跨行業務停止或暫停營業
//					}
//				} else {
//					// 自行
//					return CommonReturnCode.WithdrawServiceStop; // E948 提款暫停服務
//				}
//			}

			// 2. 組香港T24主機電文CBS_TITA(共用)
			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
				// 跨行交易
				company = ATMPConfig.getInstance().getHKDCompanyCode();
			} else {
				switch (CBSTxid) {
					case T24Version.A1000:
					case T24Version.A1050:
						// 自行ATM現金類交易(自行提款/代理提款)
						company = ATMPConfig.getInstance().getHKDCompanyCode().substring(0, 6) + getFeptxn().getFeptxnAtmBrno();
						break;
					case T24Version.A1030:
					case T24Version.A1070:
						// 香港卡至台灣/澳門跨區提款(A1030)、自行轉帳(A1070)
						// 2013/10/24 Modify by Ruling for 固定為'HK8070090'
						company = ATMPConfig.getInstance().getHKDCompanyCode();
						// company = ATMPConfig.Instance.HKDCompanyCode.Substring(0, 6) + getFeptxn().getFeptxnTroutActno().Substring(2, 3)
						break;
					case T24Version.B0001:
						company = ATMPConfig.getInstance().getHKDCompanyCode();
						break;
					default:
						company = "";
						break;
				}
			}

			// version
			if ((int) txType == T24TxType.EC.getValue()) {
				version = T24Version.REVERSE;
			} else {
				version = CBSTxid;
			}

			// reversetag
			if ((int) txType == T24TxType.EC.getValue()) {
				reversetag = true;
			} else {
				reversetag = false;
			}

			// tranactionID
			if ((int) txType == T24TxType.EC.getValue()) {
				if (CBSTxid.equals(T24Version.A1050)) {
					// 代理跨區提款(A1050)抓取不冋欄位
					tranactionID = getFeptxn().getFeptxnVirCbsRrn();
				} else {
					tranactionID = getFeptxn().getFeptxnCbsRrn();
				}
			} else {
				tranactionID = "";
			}

			// ProcessTag
			if (processFlag) {
				processTag = "PROCESS/NULL";
			} else {
				processTag = "VALIDATE/NULL";
			}

			t24ReqMessage = new T24PreClass(company, version, reversetag, tranactionID, processTag);
			t24ReqMessage.setUserName(ATMPConfig.getInstance().getHKT24UserName());
			t24ReqMessage.setPassword(ATMPConfig.getInstance().getT24Password());
			t24ReqMessage.setTxType(t24Type);
			// 3. 取得FEP電子日誌序號
			if (txType == T24TxType.Accounting.getValue()) {
				wEJ = getFeptxn().getFeptxnEjfno().intValue();
			} else if (txType == T24TxType.EC.getValue()) {
				noTITA = true;
				t24ReqMessage.setReverseTag(true);
				t24ReqMessage.setTransactionId(tranactionID);
			} else {
				return rtnCode;
			}

			// 4. 組香港 T24 TITA 電文
			if (!noTITA) {
				genT24TITA(t24ReqMessage, wEJ, CBSTxid);
			}

			// 5. 將組好的T24電文送往香港T24主機
			// 5.1送T24主機前，先更新交易記錄
			if (txType == T24TxType.Accounting.getValue()) {
				if (Integer.parseInt(CBSTxid.substring(1, 2)) == AccountOpType.Withdraw.getValue()) {
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Request);
				} else {
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_INQ_Request);
				}
			} else if (txType == T24TxType.EC.getValue()) {
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_EC_Request);
			}

			if (CBSTxid.equals(T24Version.A1050)) {
				// 代理跨區提款(A1050)存入不冋欄位
				getFeptxn().setFeptxnVirCbsTxCode(CBSTxid);
				getFeptxn().setFeptxnVirCbsTimeout(DbHelper.toShort(true));
			} else {
				getFeptxn().setFeptxnCbsTxCode(CBSTxid);
				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(true));
			}

			if (processFlag) {
				getFeptxn().setFeptxnNeedSendCbs((short) txType);
				if ("A".equals(CBSTxid.substring(0, 1))) {// T24 version A 類 - 帳務類交易
					if (CBSTxid.equals(T24Version.A1050)) {
						// 代理跨區提款(A1050)存入不冋欄位
						getFeptxn().setFeptxnVirAccType((short) AccountingType.UnKnow.getValue());
					} else {
						getFeptxn().setFeptxnAccType((short) AccountingType.UnKnow.getValue());
					}
				}
			}

			rtnCode = FEPReturnCode.HostResponseTimeout;

			if (this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()) < 1) {
				return IOReturnCode.FEPTXNUpdateError;
			}

			// 傳送電文至T24
			adapter.setMessageToT24(t24ReqMessage.getGenT24ReqOFS());
			adapter.setArea(ZoneCode.HKG);
			rtnCode = adapter.sendReceive();

			// 5.2設定TIMER等待香港T24主機回應訊息
			if (rtnCode == CommonReturnCode.HostResponseTimeout || rtnCode == CommonReturnCode.ProgramException || rtnCode == CommonReturnCode.CBSResponseError) {
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {
					// 帳務類T24電文TimeOut時，寫入HKCBSPEND
					rtnCode2 = t24TimeOutProcess(CBSTxid, t24ReqMessage.getReverseTag(), adapter.getMessageToT24());
				}
				return rtnCode;
			}

			boolean t24ResponseNormal = false;
			if (CBSTxid.equals(T24Version.B0001)) {
				t24ResponseNormal = t24ReqMessage.parseT24RspOfsForBType(adapter.getMessageFromT24(), CBSTxid);
			} else {
				t24ResponseNormal = t24ReqMessage.parseT24RspOFS(adapter.getMessageFromT24());
			}

			// 判斷T24Response 之Dictionary是否有值，若沒有值則視為T24回應異常
			if (t24ReqMessage.getTOTATransResult() == null || t24ReqMessage.getTOTATransResult().size() == 0) {
				t24ResponseNormal = false;
			}

			// 設定TIMER等待T24主機回應訊息
			if (!t24ResponseNormal) {
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {
					rtnCode2 = t24TimeOutProcess(CBSTxid, t24ReqMessage.getReverseTag(), adapter.getMessageToT24());
				}
				return CommonReturnCode.CBSResponseError;
			}

			// 5.3收到香港T24主機回應電文
			getFeptxn().setFeptxnMsgflow(getFeptxn().getFeptxnMsgflow().substring(0, 1) + "2");
			if (CBSTxid.equals(T24Version.A1050)) {
				// 代理跨區提款(A1050)存入不冋欄位
				getFeptxn().setFeptxnVirCbsTimeout(DbHelper.toShort(false)); // 收到回應正常CBS逾時FLAG設為False
				getFeptxn().setFeptxnVirCbsRc(t24ReqMessage.getTOTATransResult().get("EB.ERROR"));
			} else {
				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false)); // 收到回應正常CBS逾時FLAG設為False
				getFeptxn().setFeptxnCbsRc(t24ReqMessage.getTOTATransResult().get("EB.ERROR"));
			}

			rtnCode = FEPReturnCode.Normal; // 收到主機回應，改成Normal

			String channelRC = "";
			if (!NormalRC.FEP_OK.equals(t24ReqMessage.getTOTATransResult().get("EB.ERROR"))) {
				// 失敗
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {// T24 version A 類 - 帳務類交易
					if (CBSTxid.equals(T24Version.A1050)) {
						// 代理跨區提款(A1050)存入不冋欄位
						if (txType == T24TxType.EC.getValue()) {
							// 沖正交易
							getFeptxn().setFeptxnVirAccType((short) AccountingType.ECFail.getValue());
						} else {
							// 扣帳或代理類交易
							getFeptxn().setFeptxnVirAccType((short) AccountingType.UnAccounting.getValue());
						}
					} else {
						if (txType == T24TxType.EC.getValue()) {
							// 沖正交易
							getFeptxn().setFeptxnAccType((short) AccountingType.ECFail.getValue());
						} else {
							// 扣帳或代理類交易
							getFeptxn().setFeptxnAccType((short) AccountingType.UnAccounting.getValue());
						}
					}
				}

				getLogContext().setProgramName(_ProgramName);

				if (CBSTxid.equals(T24Version.A1050)) {
					// 代理跨區提款(A1050)存入不冋欄位
					channelRC = TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnVirCbsRc(), FEPChannel.T24, getGeneralData().getTxChannel(), getLogContext());
					getFeptxn().setFeptxnVirCbsRc(getLogContext().getExternalCode());
					getFeptxn().setFeptxnVirErrMsg(getLogContext().getResponseMessage());
				} else {
					channelRC = TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnCbsRc(), FEPChannel.T24, getGeneralData().getTxChannel(), getLogContext());
					getFeptxn().setFeptxnCbsRc(getLogContext().getExternalCode());
					getFeptxn().setFeptxnErrMsg(getLogContext().getResponseMessage());
				}

				if ((!CBSTxid.substring(0, 2).equals("A2") || !processFlag) && txType != 2) {// 入帳及沖正交易若上主機失敗亦需視為失功，其他交易則回覆錯誤
					if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
						getFeptxn().setFeptxnReplyCode(channelRC);
					} else {
						getFeptxn().setFeptxnRepRc(channelRC);
					}
				}

				return CommonReturnCode.CBSResponseError;
			} else {
				// 成功
				if ("A".equals(CBSTxid.substring(0, 1))) {
					if (processFlag) {// T24 version A類 - 帳務類交易
						if (CBSTxid.equals(T24Version.A1050)) {
							// 代理跨區提款(A1050)存入不冋欄位
							getFeptxn().setFeptxnVirAccType((short) txType);
							getFeptxn().setFeptxnVirCbsRrn(t24ReqMessage.getTOTATransContent().get("transactionId"));
						} else {
							getFeptxn().setFeptxnAccType((short) txType);
							getFeptxn().setFeptxnCbsRrn(t24ReqMessage.getTOTATransContent().get("transactionId"));
							// 2014/10/27 Modify by Ruling for HKSMS:香港境外提款交易發送簡訊
							// 2015/01/08 Modify by Ruling for 沖正時不需再寫簡訊資料檔，避免發生PK重覆的錯誤訊息
							if ((CBSTxid.equals(T24Version.A1010) || CBSTxid.equals(T24Version.A1030) || CBSTxid.equals(T24Version.A1020)) && txType == T24TxType.Accounting.getValue()) {
//								// If CBSTxid = T24Version.A1010 OrElse CBSTxid = T24Version.A1030 OrElse CBSTxid = T24Version.A1020 Then
//								// 拆解手機號碼
//								Set<String> keyName = null;
//								int icount = 0;
//								String telPhone = "";
//								Smsmsg defSMSMSG = new Smsmsg();
//
//								try {
//									// 2014/11/16 Modify by Ruling for HKSMS:加拆解單筆電話號碼
//									if (t24ReqMessage.getTOTATransResult().containsKey("T.PHONE") && !StringUtils.isBlank(t24ReqMessage.getTOTATransResult().get("T.PHONE"))) {
//										// 單筆
//										telPhone = telPhone + t24ReqMessage.getTOTATransResult().get("T.PHONE").replace("-", "");
//									} else {
//										// 多筆(最多5組電話號碼)
//										keyName = t24ReqMessage.getTOTATransResult().keySet();
//										for (String str : keyName) {
//											if (str.contains("T.PHONE_")) {
//												icount += 1;
//											}
//										}
//										// 2014/12/27 Modify by Ruling for HKSMS:最多5組電話號碼
//										if (icount > 5) {
//											icount = 5;
//										}
//										for (int jcount = 1; jcount <= icount; jcount++) {
//											if (t24ReqMessage.getTOTATransResult().containsKey("T.PHONE_" + jcount)
//													&& !StringUtils.isBlank(t24ReqMessage.getTOTATransResult().get("T.PHONE_" + jcount))) {
//												if (jcount != icount) {
//													telPhone = telPhone + t24ReqMessage.getTOTATransResult().get("T.PHONE_" + jcount).replace("-", "") + ";";
//												} else {
//													telPhone = telPhone + t24ReqMessage.getTOTATransResult().get("T.PHONE_" + jcount).replace("-", "");
//												}
//											}
//										}
//									}
//
//									// 2014/11/16 Modify by Ruling for HKSMS:即使T24無電話號碼，也要寫一筆電話號碼為空的資料到簡訊資料檔
//									// 將手機號碼寫入簡訊資料檔
//									// If Not String.IsNullOrWhiteSpace(telPhone) Then
//									defSMSMSG.setSmsmsgTxDate(getFeptxn().getFeptxnTxDate());
//									defSMSMSG.setSmsmsgEjfno(getFeptxn().getFeptxnEjfno());
//									if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
//										// 跨行交易填入STAN及PCODE
//										defSMSMSG.setSmsmsgStan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
//										defSMSMSG.setSmsmsgPcode(getFeptxn().getFeptxnPcode());
//									} else {
//										// 自行交易填入ATM電文代號
//										defSMSMSG.setSmsmsgPcode(getFeptxn().getFeptxnTxCode());
//									}
//									defSMSMSG.setSmsmsgTroutActno(getFeptxn().getFeptxnTroutActno());
//									defSMSMSG.setSmsmsgTxTime(getFeptxn().getFeptxnTxTime());
//									defSMSMSG.setSmsmsgNumber(telPhone);
//									defSMSMSG.setSmsmsgZone(getFeptxn().getFeptxnZoneCode());
//									defSMSMSG.setSmsmsgSend("N");
//									if (dbSMSMSG.insertSelective(defSMSMSG) <= 0) {
//										getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//										this.logMessage(getLogContext());
//									}
//									// End If
//								} catch (Exception ex) {
//									getLogContext().setProgramException(ex);
//									getLogContext().setProgramName(_ProgramName + ".sendToCBSHKT24");
//									sendEMS(getLogContext());
//								}
							}
						}
					}
				}
			}

			if (txType == T24TxType.EC.getValue()) {
				return rtnCode;
			}

			// 5.4依交易類別更新 FEPTXN
			rtnCode = updateFEPTXNbyCBSTxid(CBSTxid, t24ReqMessage);

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(_ProgramName + ".sendToCBSHKT24");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * T24電文處理
	 * 
	 * @param t24ReqMessage
	 * @param wEJ
	 * @param CBSTxid
	 *        4
	 *        <history>
	 *        <modify>
	 *        <modifier>Ruling</modifier>
	 *        <reason></reason>
	 *        <date>2013/06/19</date>
	 *        </modify>
	 *        </history>
	 */
	private void genT24TITA(T24PreClass t24ReqMessage, int wEJ, String CBSTxid) throws Exception {
		t24ReqMessage.getTITAHeader().setTiChnnCodeS(getFeptxn().getFeptxnChannel());
		t24ReqMessage.getTITAHeader().setTiChnnCode("FEP");
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
			t24ReqMessage.getTITAHeader().setTRMNO(getFeptxn().getFeptxnAtmno());
		} else {
			t24ReqMessage.getTITAHeader().setTRMNO(getFeptxn().getFeptxnAtmnoVir());
		}
		t24ReqMessage.getTITAHeader().setEJFNO(getFeptxn().getFeptxnTxDate() + StringUtils.leftPad(String.valueOf(wEJ), 12, '0'));
		// 2014/01/01 Modify by Ruling 應永豐要求國際卡提款(A1020/A140)增加國際清算日
		t24ReqMessage.getTITAHeader().setFiscDate(getFeptxn().getFeptxnTbsdyFisc());
		t24ReqMessage.getTITAHeader().setRegFlag("");
		t24ReqMessage.getTITAHeader().setFepUserId("");
		t24ReqMessage.getTITAHeader().setFepPassword("");

		switch (CBSTxid) {
			case T24Version.B0001: // 帳戶餘額查詢(IIQ/IQ2)
				t24ReqMessage.setEnquiryTag(true);
				t24ReqMessage.setEnquiryName("TMB.ENQ.CHL.ACCT.BAL");
				genB0001Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1000: // 自行提款(IFW)
				genA1000Body(t24ReqMessage.getTITABody(), CBSTxid);
				break;
			case T24Version.A1010: // 跨行提款(2510)
				genA1010Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1030: // 香港卡跨區提款(IFW)
				genA1030Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1070: // 自行轉帳(IFT)
				genA1070Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1050: // 代理現金-台灣/澳門卡至香港跨區提款(IFW)
				genA1050Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1470: // 國際卡餘額查詢(2411)
				genA1470Body(t24ReqMessage.getTITABody());
				break;
			case T24Version.A1020: // 國際金融卡提款(2410)
				genA1020Body(t24ReqMessage.getTITABody());
				break;
		}
	}

	/**
	 * B0001 帳戶餘額查詢 (IIQ/IQ2)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/17</date>
	 * </modify>
	 * </history>
	 */
	private void genB0001Body(HashMap<String, String> titaBody) {
		T24TITAB0000 titaB0000 = new T24TITAB0000();

		// 利用General class將值正確填入
		titaB0000.setAcctNo(getFeptxn().getFeptxnTroutActno().substring(2, 16));
		titaB0000.setCURRENCY(getFeptxn().getFeptxnTxCurAct());

		// 利用General class的GenDictionary建立T24的titaBody
		titaB0000.genDictionary(titaBody);

	}

	/**
	 * A1000 自行提款(IFW)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/17</date>
	 * </modify>
	 * </history>
	 * 
	 * @throws Exception
	 */
	private void genA1000Body(HashMap<String, String> titaBody, String CBSTxid) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());

		if (getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCur().trim() + getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13) + ATMPConfig.getInstance().getHKDept());
		} else {
			titaA0000.setCreditAcctNo("");
		}
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());

		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		titaA0000.setTPsbRemSD("ATM CASH");
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmno());
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.setTRegTfrType("NW");

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	/**
	 * A1010 跨行提款(2510)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/17</date>
	 * </modify>
	 * <modify>
	 * <modifier>ChenLi</modifier>
	 * <reason>A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號</reason>
	 * <date>2013/10/28</date>
	 * </modify>
	 * </history>
	 */
	private void genA1010Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		Allbank defALLBANK = new Allbank();
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 利用General class將值正確填入
		switch (getFeptxn().getFeptxnBoxCnt()) {
			case 0: // 不減免手續費
				// 讀取 INBKPARM 取得客戶應付手續費
				if (getInbkparm() == null) {
					rtnCode = getInbkparmByPK();
					if (rtnCode != CommonReturnCode.Normal) {
						String remark = "GenA1010Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur() + " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
								+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE=" + getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
						getLogContext().setRemark(remark);
						this.logMessage(getLogContext());
						throw ExceptionUtil.createException(remark);
					}
				}
				getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
				break;
			case 1: // 全額減免手續費
				getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
				break;
			case 2: // 依金額減免手續費
				// 減免金額 已於 CheckCardStatus 將 CARD_FREE_TW 借欄存入 FEPTXN
				break;
		}
		getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnFeeCustpayAct());

		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());

		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurAct() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getHKDept());
		} else {
			titaA0000.setCreditAcctNo("");
		}

		titaA0000.setChgCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setChgAccount(ATMPConfig.getInstance().getHKDFeeAccount());
		// ChenLi, 2013/10/28, A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號
		getFeptxn().setFeptxnCbsFeeActno(titaA0000.getChgAccount());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		// 存褶欄位顯示行庫全名及簡稱
		defALLBANK.setAllbankBkno(getFeptxn().getFeptxnBkno());
		defALLBANK.setAllbankBrno("000");
		defALLBANK = allbankMapper.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());

		// 2014/01/08 Modify by Ruling for 調整存摺備註(T.PSB.REM.S.D)為提領幣別+提領金額
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		// titaA0000.T_PSB_REM_S_D = "ATM CASH"
		// 2014/11/28 Modify by Ruling for 調整存褶備註(T.PSB.REM.F.D)分2項，第1組為提領幣別+提領金額;第3組為帳戶幣別@帳戶對台幣匯率$帳戶金額
		titaA0000.getTPsbRemFDMuti().put(1, getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		titaA0000.getTPsbRemFDMuti().put(3, getFeptxn().getFeptxnTxCurAct() + "@" + getFeptxn().getFeptxnExrate().toString() + "$" + getFeptxn().getFeptxnTxAmtAct().toString());
		// titaA0000.T_PSB_REM_F_D = defALLBANK.ALLBANK_FULLNAME
		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.setTRegTfrType("NW");

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	/**
	 * A1030 香港卡跨區提款(IFW)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/18</date>
	 * </modify>
	 * <modify>
	 * <modifier>ChenLi</modifier>
	 * <reason>A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號</reason>
	 * <date>2013/10/28</date>
	 * </modify>
	 * </history>
	 * 
	 * @throws Exception
	 */
	private void genA1030Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() - getFeptxn().getFeptxnFeeCustpayAct().intValue()));
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() - getFeptxn().getFeptxnFeeCustpayAct().intValue()));

		// 2015/03/11 Modify by Ruling for 調整存褶備註：跨區提款(IFW)提款幣別=帳戶幣別，匯率=1
		if (getFeptxn().getFeptxnTxCur().equals(getFeptxn().getFeptxnTxCurAct())) {
			getFeptxn().setFeptxnExrate(BigDecimal.valueOf(1));
		}
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());

		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());

		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurAct() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getHKDept());
		} else {
			titaA0000.setCreditAcctNo("");
		}
		getFeptxn().setFeptxnCbsSupActno(titaA0000.getCreditAcctNo()); // 貸方帳號

		titaA0000.setChgCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setChgAccount(ATMPConfig.getInstance().getHKDFeeAccount());
		// ChenLi, 2013/10/28, A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號
		getFeptxn().setFeptxnCbsFeeActno(titaA0000.getChgAccount());
		titaA0000.setTPsbMemoD("境外提款");
		titaA0000.setTPsbRemSD("ATM CASH");
		// 2014/11/28 Modify by Ruling for 調整存褶備註(T.PSB.REM.F.D)分2項，第1組為提領幣別+提領金額;第3組為帳戶幣別@帳戶對台幣匯率$帳戶金額(不含手續費)
		titaA0000.getTPsbRemFDMuti().put(1, getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		titaA0000.getTPsbRemFDMuti().put(3, getFeptxn().getFeptxnTxCurAct() + "@" + getFeptxn().getFeptxnExrate().toString() + "$"
				+ (getFeptxn().getFeptxnTxAmtAct().intValue() - getFeptxn().getFeptxnFeeCustpayAct().intValue()));
		// titaA0000.T_PSB_REM_F_D = getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt();.ToString
		titaA0000.setTRegTfrType("NW");

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	/**
	 * A1070 自行轉帳(IFT)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/18</date>
	 * </modify>
	 * </history>
	 * 
	 * @throws Exception
	 */
	private void genA1070Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 利用General class將值正確填入
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTrinActno());
		titaA0000.setTPsbMemoD("ATM轉帳");
		titaA0000.setTPsbMemoC("ATM轉帳");
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());

		// 判斷CARD檔約定轉帳記號(CARD_TFR_FLAG)
		if (getCard().getCardTfrFlag() == 2) {// 限制約轉
			titaA0000.setTRegTfrType("CR");
		}

		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTrinActno().substring(4, 16));
		titaA0000.setTPsbRemSC(getFeptxn().getFeptxnTroutActno().substring(4, 16));
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTrinBkno() + getFeptxn().getFeptxnTrinActno());
		titaA0000.setTPsbRemFC(getFeptxn().getFeptxnTroutBkno() + getFeptxn().getFeptxnTroutActno());

		titaA0000.getTPsbRinfD().put(1, getFeptxn().getFeptxnTrinActno().substring(2, 16));
		titaA0000.getTPsbRinfD().put(2, "FUT^" + getFeptxn().getFeptxnTrinBkno() + "^" + getFeptxn().getFeptxnTrinActno().substring(2, 16) + "^^");
		titaA0000.getTPsbRinfD().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.getTPsbRinfD().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");

		titaA0000.getTPsbRinfC().put(1, getFeptxn().getFeptxnTroutActno().substring(2, 16));
		titaA0000.getTPsbRinfC().put(2, "FUT^" + getFeptxn().getFeptxnTroutBkno() + "^" + getFeptxn().getFeptxnTroutActno().substring(2, 16) + "^^");
		titaA0000.getTPsbRinfC().put(3, getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnAtmno());
		titaA0000.getTPsbRinfC().put(4, "!ACCOUNT>ACCOUNT.TITLE.1");

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	/**
	 * A1050 代理現金-台灣/澳門卡至香港跨區提款(IFW)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/18</date>
	 * </modify>
	 * </history>
	 * 
	 * @throws Exception
	 */
	private void genA1050Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();

		// 利用General class將值正確填入

		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTxCur() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getHKDept());
		} else {
			titaA0000.setDebitAcctNo("");
		}

		titaA0000.setTPsbRemSD("ATM CASH");
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmBrno());
		titaA0000.setTPsbMemoD("ATM 現金");
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCur()); // 提領幣別
		titaA0000.setDebitAcctNo(String.valueOf(getFeptxn().getFeptxnTxAmt()));
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur()); // 提領幣別
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());

		if (getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCur() + getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13) + ATMPConfig.getInstance().getHKDept());
		} else {
			titaA0000.setCreditAcctNo("");
		}

		getFeptxn().setFeptxnCbsSupActno(titaA0000.getDebitAcctNo()); // 借方帳號
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo()); // 貸方帳號

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	/**
	 * A1470 國際卡餘額查詢(2411)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/18</date>
	 * </modify>
	 * </history>
	 */
	private void genA1470Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		Intltxn defINTLTXN = new Intltxn();
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		if (getInbkparm() == null) {
			rtnCode = getInbkparmByPK();
			if (rtnCode != CommonReturnCode.Normal) {
				String remark = "GenA1470Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur() + " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
						+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE=" + getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
		}
		getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
		getFeptxn().setFeptxnFeeCustpay(getInbkparm().getInbkparmFeeCustpay());

		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurAct() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getHKDept());
		} else {
			titaA0000.setCreditAcctNo("");
		}

		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnFeeCustpayAct());

		// 清算幣別為美金
		titaA0000.setCreditCurrency(CurrencyType.USD.toString());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
		titaA0000.setTPsbMemoD("國外查詢");
		titaA0000.setTRegTfrType("NN");

		// 取得代理單位國別碼
		defINTLTXN.setIntltxnTxDate(getFeptxn().getFeptxnTxDate());
		defINTLTXN.setIntltxnEjfno(getFeptxn().getFeptxnEjfno());
		defINTLTXN = dbINTLTXN.selectByPrimaryKey(defINTLTXN.getIntltxnTxDate(), defINTLTXN.getIntltxnEjfno());
		if (defINTLTXN != null) {
			// 取得代理單位國別碼
			titaA0000.setTPsbRemSD(defINTLTXN.getIntltxnAcqCntry()); // 地區簡稱
			titaA0000.setTPsbRemFD(defINTLTXN.getIntltxnAcqCntry()); // 地區簡稱
		}

		// 2013/12/16 Modify by Ruling 應永豐要求國際卡提款(A1020/A140)增加國際清算日
		titaA0000.setPlusDate(getFeptxn().getFeptxnTbsdyIntl());

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

	}

	/**
	 * A1020 國際金融卡提款(2410)
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/18</date>
	 * </modify>
	 * <modify>
	 * <modifier>ChenLi</modifier>
	 * <reason>A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號</reason>
	 * <date>2013/10/28</date>
	 * </modify>
	 * </history>
	 */
	private void genA1020Body(HashMap<String, String> titaBody) throws Exception {
		T24TITAA0000 titaA0000 = new T24TITAA0000();
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Intltxn defINTLTXN = new Intltxn();
		String wSetExrate = "";

		// 利用General class將值正確填入
		switch (getFeptxn().getFeptxnBoxCnt()) {
			case 0: // 不減免手續費
				// 讀取 INBKPARM 取得客戶應付手續費
				if (getInbkparm() == null) {
					rtnCode = getInbkparmByPK();
					if (rtnCode != CommonReturnCode.Normal) {
						String remark = "GenA1010Body 讀不到INBKPARM, INBKPARM_CUR=" + getInbkparm().getInbkparmCur() + " INBKPARM_APID=" + getInbkparm().getInbkparmApid() + " INBKPARM_ACQ_FLAG="
								+ getInbkparm().getInbkparmAcqFlag() + " INBKPARM_EFFECT_DATE=" + getInbkparm().getInbkparmEffectDate() + " INBKPARM_PCODE=" + getInbkparm().getInbkparmPcode();
						getLogContext().setRemark(remark);
						this.logMessage(getLogContext());
						throw ExceptionUtil.createException(remark);
					}
				}
				getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
				break;
			case 1: // 全額減免手續費
				getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
				break;
			case 2: // 依金額減免手續費
				// 減免金額 已於 CheckCardStatus 將 CARD_FREE_C1 借欄存入 FEPTXN
				break;
		}

		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurSet()); // 清算幣別為美金
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtSet()); // 財金清算金額
		titaA0000.setChgCurrency(getFeptxn().getFeptxnTxCurAct());
		// 2014/02/21 Modify by Ruling for 配合永豐修改，香港國際提款手續費帳號改為PL56042
		titaA0000.setChgAccount(ATMPConfig.getInstance().getHK2410FeeAccount());
		// titaA0000.CHG_ACCOUNT = ATMPConfig.Instance.HKDFeeAccount
		// 2013/10/28 Modify by ChenLi for A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號
		getFeptxn().setFeptxnCbsFeeActno(titaA0000.getChgAccount());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());

		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurSet() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getHKDept());
		} else {
			titaA0000.setCreditAcctNo("");
		}

		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		// 2014/01/08 Modify by Ruling for 調整存摺備註(T.PSB.REM.S.D)為提領幣別+提領金額
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		// titaA0000.T_PSB_REM_S_D = "ATM CASH"
		// 2014/11/28 Modify by Ruling for 調整存褶備註(T.PSB.REM.F.D)分3項，第1組為提領幣別+提領金額;第2組為清算幣別@清算匯率$清算金額;第3組為帳戶幣別@帳戶對美金匯率$帳戶金額
		defINTLTXN.setIntltxnTxDate(getFeptxn().getFeptxnTxDate());
		defINTLTXN.setIntltxnEjfno(getFeptxn().getFeptxnEjfno());
		Intltxn intltxn = dbINTLTXN.selectByPrimaryKey(defINTLTXN.getIntltxnTxDate(), defINTLTXN.getIntltxnEjfno());
		if (intltxn == null) {
			String remark = "GenA1020Body 讀不到INTLTXN, INTLTXN_TX_DATE=" + defINTLTXN.getIntltxnTxDate() + " INTLTXN_EJFNO=" + defINTLTXN.getIntltxnEjfno().toString();
			getLogContext().setRemark(remark);
			this.logMessage(getLogContext());
			throw ExceptionUtil.createException(remark);
		} else {
			if (StringUtils.isBlank(intltxn.getIntltxnSetExrate()) || intltxn.getIntltxnSetExrate().trim().length() != 8 || !PolyfillUtil.isNumeric(intltxn.getIntltxnSetExrate())
					|| "00000000".equals(intltxn.getIntltxnSetExrate())) {
				String remark = "GenA1020Body INTLTXN_SET_EXRATE 為空白或資料的格式、長度不對, INTLTXN_SET_EXRATE=" + intltxn.getIntltxnSetExrate();
				getLogContext().setRemark(remark);
				this.logMessage(getLogContext());
				throw ExceptionUtil.createException(remark);
			}
			int pointRight = Integer.parseInt(intltxn.getIntltxnSetExrate().trim().substring(0, 1)); // 第一位表示後續7位之小數位
			// 2015/05/05 Modify by Ruling for 修正清算匯率之Double形態的長度超過10(含)會轉成科學記號的問題
			BigDecimal sWord = new BigDecimal(intltxn.getIntltxnSetExrate().trim().substring(1)); // 第二位～第八位表示要拆解的數值
			BigDecimal tenN = BigDecimal.valueOf(Math.pow(10, pointRight)); // 10的N次方
			// Dim sWord As Double = CDec(defINTLTXN.INTLTXN_SET_EXRATE.Trim.Substring(1)) '第二位～第八位表示要拆解的數值
			// Dim tenN As Double = Math.Pow(10, pointRight) '10的N次方
			wSetExrate = String.valueOf(sWord.divide(tenN));

			// 2018/10/19 Modify by Ruling for 香港分行禁止交易國家需求:應永豐要求國際卡提款(A1020)增加代理單位國別碼
			if (StringUtils.isNotBlank(intltxn.getIntltxnAcqCntry())) {
				if (intltxn.getIntltxnAcqCntry().trim().length() == 3) {
					// 3位國別碼，讀ISOCOUNTRY做轉換
					Isocode defISOCODE = new Isocode();
					defISOCODE.setIsocodeAlpha3(intltxn.getIntltxnAcqCntry().trim());
					Isocode isocode = isocodeExtMapper.queryByAlpha3(defISOCODE.getIsocodeAlpha3());
					if (isocode == null) {
						// 讀不到ISOCODE，應永豐要求國別碼送ZZ給T24
						getLogContext().setRemark("GenA1020Body 讀不到ISOCODE, ISOCODE_ALPHA3=" + defISOCODE.getIsocodeAlpha3());
						this.logMessage(getLogContext());
						titaA0000.setTCrsCountry("ZZ");
					} else {
						// 有讀到ISOCODE
						titaA0000.setTCrsCountry(isocode.getIsocodeAlpha2());
					}
				} else {
					// 2位國別碼
					titaA0000.setTCrsCountry(intltxn.getIntltxnAcqCntry().trim());
				}
			} else {
				titaA0000.setTCrsCountry("ZZ");
			}
		}

		titaA0000.getTPsbRemFDMuti().put(1, getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		titaA0000.getTPsbRemFDMuti().put(2, getFeptxn().getFeptxnTxCurSet() + "@" + wSetExrate + "$" + getFeptxn().getFeptxnTxAmtSet().toString());
		titaA0000.getTPsbRemFDMuti().put(3, getFeptxn().getFeptxnTxCurAct() + "@" + getFeptxn().getFeptxnExrate().toString() + "$" + getFeptxn().getFeptxnTxAmtAct().toString());
		// titaA0000.T_PSB_REM_F_D = getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt();.ToString
		titaA0000.setTRegTfrType("NWP");

		// 2013/12/16 Modify by Ruling 應永豐要求國際卡提款(A1020/A140)增加國際清算日
		titaA0000.setOriTxDate(getFeptxn().getFeptxnTbsdyIntl());

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

		// 2014/11/28 Modify by Ruling for 調整存褶備註(T.PSB.REM.F.D)分3項
	}

	/**
	 * 帳務類交易共用
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason></reason>
	 * <date>2013/06/18</date>
	 * </modify>
	 * <modify>
	 * <modifier>ChenLi</modifier>
	 * <reason>FEPTXN_CBS_FEE_ACTNO移到前面放入</reason>
	 * <date>2013/10/28</date>
	 * </modify>
	 * </history>
	 * 
	 * @throws Exception
	 */
	private void addGeneralField(T24TITAA0000 titaA0000) throws Exception {
		if (StringUtils.isNotBlank(titaA0000.getDebitAcctNo()) && titaA0000.getDebitAcctNo().substring(0, 2).equals("00")) {
			titaA0000.setDebitAcctNo(titaA0000.getDebitAcctNo().substring(2, 16));
		}

		if (StringUtils.isNotBlank(titaA0000.getCreditAcctNo()) && titaA0000.getCreditAcctNo().substring(0, 2).equals("00")) {
			titaA0000.setCreditAcctNo(titaA0000.getCreditAcctNo().substring(2, 16));
		} else {
			titaA0000.setCreditAcctNo(getVirtualAccountPrefix(titaA0000.getCreditAcctNo()) + titaA0000.getCreditAcctNo());
		}

		if (StringUtils.isNotBlank(titaA0000.getIcActno())) {
			titaA0000.setIcActno(titaA0000.getIcActno().substring(2, 16));
		}

		if (StringUtils.isBlank(titaA0000.getFeePayer())) {
			// 除全國性繳費自行轉帳以外，其他皆為借方負擔手續費
			titaA0000.setFeePayer("1");
		}

		/// *他行之銀行代號才需搬值-for 借方or貸方銀行代號*/
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

		if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno()) && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
			titaA0000.setBankCodeCr(getFeptxn().getFeptxnTrinBkno());
		}

		titaA0000.setAcctTxnCr(getFeptxn().getFeptxnTrinActno());

		if (StringUtils.isBlank(titaA0000.getDebitCurrency())) {
			titaA0000.setDebitCurrency(String.valueOf(CurrencyType.HKD.name()));
		}

		if (StringUtils.isBlank(titaA0000.getCreditCurrency())) {
			titaA0000.setCreditCurrency(String.valueOf(CurrencyType.HKD.name()));
		}

		/// *將財金 PCODE 置於 AUTH_CODE 以便每日統計金額*/
		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
			titaA0000.setAuthCode(getFeptxn().getFeptxnPcode());
		}

		if (StringUtils.isBlank(getFeptxn().getFeptxnCbsSupActno())) {
			getFeptxn().setFeptxnCbsSupActno(getGeneralData().getMsgCtl().getMsgctlCbsSupActno());
		}
		// ChenLi, 2013/10/28, FEPTXN_CBS_FEE_ACTNO移到前面放入
		// getFeptxn().FEPTXN_CBS_FEE_ACTNO = getGeneralData().getMsgCtl().MSGCTL_CBS_FEE_ACTNO
	}

	/**
	 * 入扣帳電文TimeOut時，才寫入CBSPEND
	 * 
	 * @param CBSTxid
	 * @param reverseTag
	 * @param msgToT24
	 * @return
	 * 
	 */
	private FEPReturnCode t24TimeOutProcess(String CBSTxid, boolean reverseTag, String msgToT24) {
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode = CommonReturnCode.ProgramException;
		Cbspend defCBSPEND = new Cbspend();
		// MessageQueue cbsPENDQueue = new MessageQueue(INBKConfig.Instance().getCBSPEND());

		try {
			defCBSPEND.setCbspendZone(ZoneCode.HKG);
			defCBSPEND.setCbspendTxDate(getFeptxn().getFeptxnTxDate());
			defCBSPEND.setCbspendEjfno(getFeptxn().getFeptxnEjfno());
			defCBSPEND.setCbspendCbsTxCode(CBSTxid);
			defCBSPEND.setCbspendReverseFlag(DbHelper.toShort(reverseTag));
			defCBSPEND.setCbspendSubsys(getFeptxn().getFeptxnSubsys());
			defCBSPEND.setCbspendTbsdy(getFeptxn().getFeptxnTbsdy());
			defCBSPEND.setCbspendTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
			defCBSPEND.setCbspendSuccessFlag((short) 0); // 必須重送
			defCBSPEND.setCbspendResendCnt((short) 0);
			defCBSPEND.setCbspendAccType((short) AccountingType.UnKnow.getValue());
			defCBSPEND.setCbspendTxAmt(getFeptxn().getFeptxnTxAmtAct());

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
			}
			defCBSPEND.setCbspendTita(msgToT24);
			defCBSPEND.setCbspendTxTime(getFeptxn().getFeptxnTxTime());

			cbspendMapper.updateByPrimaryKeySelective(defCBSPEND);

			// 寫入 Queue todo 未實作
			/*
			 * Message msg = new Message();
			 * msg.Recoverable = true;
			 * msg.Label = "CBSPEND";
			 * msg.Body = getFeptxn().getFeptxnTxDate() + ":" + getFeptxn().FEPTXN_EJFNO.toString();
			 * cbsPENDQueue.Send(msg);
			 */

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(_ProgramName + ".T24TimeOutProcess");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 更新FEPTXN
	 * 
	 * @param CBSTxid
	 * @param t24ReqMessage
	 * @return
	 * 
	 */
	private FEPReturnCode updateFEPTXNbyCBSTxid(String CBSTxid, T24PreClass t24ReqMessage) {
		HashMap<String, String> cbs_TOTA = null;
		ArrayList<HashMap<String, String>> enquire_TOTA = null;
		Zone defZONE = new Zone();
		Bsdays defBSDAYS = new Bsdays();

		try {
			if ("A".equals(CBSTxid.substring(0, 1))) {
				// 帳務類交易
				cbs_TOTA = t24ReqMessage.getTOTATransResult();

				if (!CBSTxid.equals(T24Version.A1050) && "A1".equals(CBSTxid.substring(0, 2)) && StringUtils.isBlank(getFeptxn().getFeptxnTroutKind())) {
					// 非代理提款/轉出交易/非信用卡交易

					// 國際卡之手續費併入交易金額，故不需回饋手續費
					if (!getFeptxn().getFeptxnCbsTxCode().equals(T24Version.A1020) && cbs_TOTA.containsKey("CAP.CHG.AMT")) {
						getFeptxn().setFeptxnFeeCustpayAct(new BigDecimal(cbs_TOTA.get("CAP.CHG.AMT"))); // 帳戶幣別手續費
					}

					// 國際卡餘額查詢(2411)原存行手續費不能蓋掉
					if (CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCur()) && !getFeptxn().getFeptxnCbsTxCode().equals(T24Version.A1470)
							&& cbs_TOTA.containsKey("CAP.CHG.AMT")) {
						getFeptxn().setFeptxnFeeCustpay(new BigDecimal(cbs_TOTA.get("CAP.CHG.AMT"))); // 提領幣別手續費
					}

					if (cbs_TOTA.containsKey("AVBAL.AFT.TXN")) {
						//getFeptxn().setFeptxnBala(new BigDecimal(cbs_TOTA.get("AVBAL.AFT.TXN"))); // 可用餘額
						getFeptxn().setFeptxnBala(new BigDecimal(cbs_TOTA.get("AVBAL.AFT.TXN"))); // 可用餘額
					}
					if (cbs_TOTA.containsKey("BAL.AFT.TXN")) {
						getFeptxn().setFeptxnBalb(new BigDecimal(cbs_TOTA.get("BAL.AFT.TXN"))); // 帳戶餘額
					}
				}

				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno()) && cbs_TOTA.containsKey("CREDIT.ACCT.NO")) {
					getFeptxn().setFeptxnTrinActnoActual(StringUtils.leftPad(cbs_TOTA.get("CREDIT.ACCT.NO").trim(), 16, '0'));
				}

				if (CBSTxid.equals(T24Version.A1050)) {
					if (cbs_TOTA.containsKey("DR.CO.CODE") && cbs_TOTA.get("DR.CO.CODE").trim().length() >= 9) {
						getFeptxn().setFeptxnVirBrno(cbs_TOTA.get("DR.CO.CODE").trim().substring(6, 9)); // 借方開戶行
					}

					if (cbs_TOTA.containsKey("CR.CO.CODE") && cbs_TOTA.get("CR.CO.CODE").trim().length() >= 9) {
						getFeptxn().setFeptxnVirTrinBrno(cbs_TOTA.get("CR.CO.CODE").trim().substring(6, 9)); // 貸方開戶行
					}
				} else {
					if (cbs_TOTA.containsKey("VALUE.DATE")) {
						getFeptxn().setFeptxnCbsValueDate(cbs_TOTA.get("VALUE.DATE")); // 起息日
					}

					if (cbs_TOTA.containsKey("T24.TXTIME")) {
						getFeptxn().setFeptxnCbsTxTime(cbs_TOTA.get("T24.TXTIME"));
					}

					if (cbs_TOTA.containsKey("DR.CO.CODE") && cbs_TOTA.get("DR.CO.CODE").trim().length() >= 9) {
						getFeptxn().setFeptxnBrno(cbs_TOTA.get("DR.CO.CODE").substring(6, 9)); // 借方開戶行
					}

					if (cbs_TOTA.containsKey("CR.CO.CODE") && cbs_TOTA.get("CR.CO.CODE").trim().length() >= 9) {
						getFeptxn().setFeptxnTrinBrno(cbs_TOTA.get("CR.CO.CODE").substring(6, 9)); // 貸方開戶行
					}

					if (cbs_TOTA.containsKey("DR.PERM.BRH")) {
						getFeptxn().setFeptxnDept(cbs_TOTA.get("DR.PERM.BRH")); // 借方績效行
					}

					if (cbs_TOTA.containsKey("CR.PERM.BRH")) {
						getFeptxn().setFeptxnTrinDept(cbs_TOTA.get("CR.PERM.BRH")); // 貸方績效行
					}
				}

				if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
						&& getFeptxn().getFeptxnNpsClr() != null && getFeptxn().getFeptxnNpsClr().intValue() != FeptxnNPSCLR.TRIn.getValue()) {
					getFeptxn().setFeptxnTxBrno(getFeptxn().getFeptxnBrno());
					getFeptxn().setFeptxnTxDept(getFeptxn().getFeptxnDept());
					getFeptxn().setFeptxnTxActno(getFeptxn().getFeptxnTroutActno());
				} else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
					getFeptxn().setFeptxnTxBrno(getFeptxn().getFeptxnTrinBrno());
					getFeptxn().setFeptxnTxDept(getFeptxn().getFeptxnTrinDept());
					getFeptxn().setFeptxnTxActno(getFeptxn().getFeptxnTrinActno());
				}

				// 香港地區自行換日
				if (cbs_TOTA.containsKey("BOOKING.DATE")) {
					if (((!T24Version.A1030.equals(CBSTxid) || !T24Version.A1010.equals(CBSTxid)) && (cbs_TOTA.get("BOOKING.DATE").compareTo(getFeptxn().getFeptxnTbsdy()) > 0))
							|| ((T24Version.A1030.equals(CBSTxid) || T24Version.A1010.equals(CBSTxid)) && (cbs_TOTA.get("BOOKING.DATE").compareTo(getFeptxn().getFeptxnTbsdyAct()) > 0))) {
						// 香港卡至台灣/澳門跨區交易
						if (T24Version.A1030.equals(CBSTxid) || T24Version.A1010.equals(CBSTxid)) {
							getFeptxn().setFeptxnTbsdyAct(cbs_TOTA.get("BOOKING.DATE")); // 卡片所在地區營業日
						} else {
							getFeptxn().setFeptxnTbsdy(cbs_TOTA.get("BOOKING.DATE"));
						}
						defZONE = getZoneByZoneCode(ATMZone.HKG.toString());
						if (cbs_TOTA.get("BOOKING.DATE").compareTo(defZONE.getZoneTbsdy()) > 0) {
							defZONE.setZoneLlbsdy(defZONE.getZoneLbsdy()); // 上營業日搬入上上營業日
							defZONE.setZoneLbsdy(defZONE.getZoneTbsdy()); // 本營業日搬入上營業日
							defZONE.setZoneTbsdy(cbs_TOTA.get("BOOKING.DATE")); // 香港自行本營業日
							// 取得次營業日
							defBSDAYS.setBsdaysZoneCode(ATMZone.HKG.toString());
							defBSDAYS.setBsdaysDate(defZONE.getZoneNbsdy());
							Bsdays bsdays = bsdaysMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
							if (bsdays != null) {
								defZONE.setZoneNbsdy(bsdays.getBsdaysNbsdy());
								defBSDAYS = bsdays;
							}
							// 取得本營業日之星期幾
							defBSDAYS.setBsdaysZoneCode(ATMZone.HKG.name());
							defBSDAYS.setBsdaysDate(defZONE.getZoneTbsdy());
							defBSDAYS = bsdaysMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
							if (defBSDAYS != null) {
								defZONE.setZoneWeekno(defBSDAYS.getBsdaysWeekno());
							}
							defZONE.setZoneChgdayTime(Integer.parseInt(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
							defZONE.setZoneChgday(DbHelper.toShort(false));
							defZONE.setZoneCbsMode((short) 1);
							zoneMapper.updateByPrimaryKeySelective(defZONE);

							// 顯示換日成功訊息於EMS
							getLogContext().setpCode(getFeptxn().getFeptxnPcode());
							getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
							getLogContext().setFiscRC(NormalRC.FISC_OK);
							getLogContext().setMessageGroup("1"); // OPC
							getLogContext().setMessageParm13("香港自行");
							getLogContext().setMessageParm14(defZONE.getZoneTbsdy());
							getLogContext().setProgramName(_ProgramName);
							getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.CBSBusinessDateChangeToYMD, getLogContext()));
							getLogContext().setProgramName(_ProgramName);
							logMessage(Level.DEBUG, getLogContext());
						}
					}
				}
			} else {
				// 其他非帳務類交易
				if (CBSTxid.equals(T24Version.B0001)) // 帳戶餘額查詢(IIQ/IQ2)
				{
					enquire_TOTA = t24ReqMessage.getTotaEnquiryContents();
					//getFeptxn().setFeptxnBala(new BigDecimal(enquire_TOTA.get(0).get("AVAILABLE.BAL"))); // 可用餘額
					getFeptxn().setFeptxnBala(new BigDecimal(enquire_TOTA.get(0).get("AVAILABLE.BAL"))); // 可用餘額
					getFeptxn().setFeptxnBalb(new BigDecimal(enquire_TOTA.get(0).get("WORKING.BAL"))); // 帳戶餘額
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(_ProgramName + ".updateFEPTXNbyCBSTxid");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 處理HK主機回應的電文
	 * 
	 * @return FEPReturnCode
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>ATM Business</reason>
	 *         <date>2010/2/25</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>修改主機回應錯誤時的處理</reason>
	 *         <date>2010/4/19</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>TRMNO僅取前五位搬移</reason>
	 *         <date>2010/6/08</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>寫入FEPTXN_CON_TXSEQ需補滿七位</reason>
	 *         <date>2010/6/21</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>修改程式與SPEC一致</reason>
	 *         <date>2010/6/22</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>改為跟SPEC一致使用港澳電文判斷
	 *         搬移欄位錯誤修正</reason>
	 *         <date>2010/6/24</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>CBSPEND修改為僅記錄TITA</reason>
	 *         <date>2010/7/12</date>
	 *         </modify>
	 *         </history>
	 * @throws Exception
	 */
	private FEPReturnCode processCBSHKResponse(UnisysTXCD unysisTxcd, byte txType, FEPReturnCode rtnCode, HKGeneral hkGeneralForReq, HKGeneral hkGeneralForRes, HKUnisysTextBase reqClass,
			HKUnisysTextBase resClass, UnisysAdapter adapter, String tita) throws Exception {
		@SuppressWarnings("unused")
		Cbspend defCBSPEND = new Cbspend();
		String[] actnos = { "", "", "", "", "", "" };
		// 2010-06-22 幣別取法改為透過getCurrencyByAlpha3取得
		String hkd = getCurrencyByAlpha3(CurrencyType.HKD.name()).getCurcdCurBsp();

		if (rtnCode == CommonReturnCode.Normal) {
			String tota = "";

			tota = adapter.getMessageFromUnisys();
			if (tota.length() > unisysTotaLengthError) {
				try {
					hkGeneralForRes = resClass.parseFlatfile(tota.substring(24));
				} catch (Exception e) {
					e.printStackTrace();
				}
				switch (unysisTxcd) {
					case J00001:
					case K00001:

						if (hkGeneralForRes.getmResponse().getACTNO1().length() > 13) {
							actnos[0] = hkGeneralForRes.getmResponse().getACTNO1().substring(0, 13);
						}
						if (hkGeneralForRes.getmResponse().getACTNO2().length() > 13) {
							actnos[1] = hkGeneralForRes.getmResponse().getACTNO2().substring(0, 13);
						}
						if (hkGeneralForRes.getmResponse().getACTNO3().length() > 13) {
							actnos[2] = hkGeneralForRes.getmResponse().getACTNO3().substring(0, 13);
						}
						if (hkGeneralForRes.getmResponse().getACTNO4().length() > 13) {
							actnos[3] = hkGeneralForRes.getmResponse().getACTNO4().substring(0, 13);
						}
						if (hkGeneralForRes.getmResponse().getACTNO5().length() > 13) {
							actnos[4] = hkGeneralForRes.getmResponse().getACTNO5().substring(0, 13);
						}
						if (hkGeneralForRes.getmResponse().getACTNO6().length() > 13) {
							actnos[5] = hkGeneralForRes.getmResponse().getACTNO6().substring(0, 13);
						}
						// 回傳6組資料, 逐一檢核
						String actno = getFeptxn().getFeptxnTroutActno().substring(2, 8) + "03" + getFeptxn().getFeptxnTroutActno().substring(10, 15);
						if (actnos[0].equals(actno)) {
							if (hkd.equals(hkGeneralForRes.getmResponse().getCURCD1())) {
								//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getAMT1());
								getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getPRIBAL1());
							}
						} else if (actnos[1].equals(actno)) {
							if (hkd.equals(hkGeneralForRes.getmResponse().getCURCD2())) {
								//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getAMT2());
								getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getPRIBAL2());
							}
						} else if (actnos[2].equals(actno)) {
							if (hkd.equals(hkGeneralForRes.getmResponse().getCURCD3())) {
								//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getAMT3());
								getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getPRIBAL3());
							}
						} else if (actnos[3].equals(actno)) {
							if (hkd.equals(hkGeneralForRes.getmResponse().getCURCD4())) {
								//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getAMT4());
								getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getPRIBAL4());
							}
						} else if (actnos[4].equals(actno)) {
							if (hkd.equals(hkGeneralForRes.getmResponse().getCURCD5())) {
								//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getAMT5());
								getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getPRIBAL5());
							}
						} else if (actnos[5].equals(actno)) {
							if (hkd.equals(hkGeneralForRes.getmResponse().getCURCD6())) {
								//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getAMT6());
								getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getPRIBAL6());
							}
						} else {
							rtnCode = CommonReturnCode.CBSResponseError;
						}
						break;
					case J10033:
					case K10033:
					case J10206:
					case K10206:
					case J10060:
					case K10060:
						// 異常回應
						if (unisysErrorRspMsgId1.equals(hkGeneralForRes.getmResponse().getMSGID()) || unisysErrorRspMsgId2.equals(hkGeneralForRes.getmResponse().getMSGID())
								|| unisysErrorRspMsgId3.equals(hkGeneralForRes.getmResponse().getMSGID())) {
							// 2010-11-24 by kyo for 修正與SPEC不符段落
							resClass = new HKErrResponse();
							try {
								hkGeneralForRes = resClass.parseFlatfile(tota.substring(24));
							} catch (Exception e) {
								e.printStackTrace();
							}
							//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getBAL()); // 沒扣款到,只需回傳一個帳面餘額
						} else {
							// 正常回應
							//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getHWBAL());
							getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getAVBAL());
						}

						break;
					case J10207:
					case K10207:
					case J10208:
					case K10208:
					case J10209:
					case K10209: // 提款及轉帳
						if (!unisysRspErrorFlag.equals(hkGeneralForRes.getmResponse().getMSGID().substring(0, 1))) {
							//getFeptxn().setFeptxnBala(hkGeneralForRes.getmResponse().getHWBAL());
							getFeptxn().setFeptxnBalb(hkGeneralForRes.getmResponse().getAVBAL());
							// 2010-11-19 by kyo for Connie update spec.
							if (hkGeneralForReq.getmRequest().getDSCPT1().equals("208")) {
								/// *因下行電文之實際扣款金額不含手續費,故不需扣除*/
								getFeptxn().setFeptxnTxAmtAct(hkGeneralForRes.getmResponse().getDPAMT()); // - getFeptxn().getFeptxnFeeCustpayAct()
								getFeptxn().setFeptxnExrate(hkGeneralForRes.getmResponse().getDBFXRATE());
							}
							// 2011-07-26 by kyo for 補上FEPTXN_ACC_TYPE的更新
							if (txType == CBSTxType.Accounting.getValue()) {
								getFeptxn().setFeptxnAccType((short) AccountingType.Accounting.getValue());
							} else {
								getFeptxn().setFeptxnAccType((short) AccountingType.EC.getValue());
							}
						} else {
							if (txType == CBSTxType.Accounting.getValue()) {
								getFeptxn().setFeptxnAccType((short) AccountingType.UnAccounting.getValue());
							} else {
								getFeptxn().setFeptxnAccType((short) AccountingType.ECFail.getValue());
							}
						}

						break;
					case G08002:
						// 只取第一組匯率使用
						getFeptxn().setFeptxnExrate(hkGeneralForRes.getmResponse().getCSRATE1());
						getFeptxn().setFeptxnScash(hkGeneralForRes.getmResponse().getSCASH1());
						getFeptxn().setFeptxnAcrate(hkGeneralForRes.getmResponse().getACRAT1());
						getFeptxn().setFeptxnDifrate(hkGeneralForRes.getmResponse().getDISRAT1());
						getFeptxn().setFeptxnTxAmtAct(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmt().doubleValue() * getFeptxn().getFeptxnExrate().doubleValue()));
						break;
					default:
						break;
				}
				// 改為使用列舉
				if (txType == CBSTxType.Accounting.getValue()) {
					// 2010-06-24 by kyo for 改為跟SPEC一致使用港澳電文
					if (unysisTxcd == UnisysTXCD.J00001 || unysisTxcd == UnisysTXCD.K00001 || unysisTxcd == UnisysTXCD.G08002) {
						// 查詢類交易
						// 2010-06-24 by kyo for 應該是I2(CBS_INQ_Response)才對
						getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_INQ_Response);
					} else {
						// 轉出類交易
						getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Response);
					}
				} else if (txType == CBSTxType.EC.getValue()) {
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_EC_Response);
				}

				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false));
				getFeptxn().setFeptxnCbsRc(hkGeneralForRes.getmResponse().getMSGID());

				if (unisysRspErrorFlag.equals(hkGeneralForRes.getmResponse().getMSGID().substring(0, 1))) {
					// 2010-04-12 modified by kyo for destinationChannel 需要用adata.txchannel
					// 2010-04-19 modified by Jim for RC處理修改
					if (txType == CBSTxType.Accounting.getValue()) {// 記帳
						if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
							// 2010-09-23 by kyo for SPEC修改:/*ATM發動交易需將 CBS RC 轉換為 ATM RC*/
//							getFeptxn().setFeptxnReplyCode(
//									TxHelper.getRCFromErrorCode(hkGeneralForRes.getmResponse().getMSGID(), FEPChannel.UATMP, getGeneralData().getTxChannel(), getGeneralData().getLogContext())); // FEPReturnCode
																																																	// =
																																																	// CBS_TOTA.MSGID
						} else {
							// 2010-09-23 by kyo for SPEC修改:/*FISC發動交易需將 CBS RC 轉換為 FISC RC*/
//							getFeptxn().setFeptxnRepRc(
//									TxHelper.getRCFromErrorCode(hkGeneralForRes.getmResponse().getMSGID(), FEPChannel.UATMP, getGeneralData().getTxChannel(), getGeneralData().getLogContext()));
						}
					}
					rtnCode = CommonReturnCode.CBSResponseError;
				}
			} else {
				rtnCode = CommonReturnCode.CBSResponseFormatError;
			}
		} else {// 交易忙碌
//			getFeptxn().setFeptxnCbsRc(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.UATMP, getGeneralData().getTxChannel(), getGeneralData().getLogContext())); // TxHelper.getRCFromErrorCode(rtnCode,
																																																// FEPChannel.UATMP)
																																																// 'rc =
																																																// 0601
																																																// 交易忙碌
			// 2010-06-22 by kyo for 修改程式與SPEC一致
			// 206跨區交易，033同區交易
			if ("206".equals(unysisTxcd.toString().substring(3, 6)) || "033".equals(unysisTxcd.toString().substring(3, 6)) || DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {

				getFeptxn().setFeptxnAccType((short) AccountingType.UnKnow.getValue()); /// *未明 by Connie*/
			}
		}

		try {
			if (this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {
				return IOReturnCode.FEPTXNUpdateError; // L013
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rtnCode;
	}

	/**
	 * 準備送HK主機資料
	 * 
	 * @return FEPReturnCode
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>ATM Business</reason>
	 *         <date>2010/2/25</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>BRSID1(001B0024):執行香港卡(台灣ATM)進行查詢，主機沒有回應。</reason>
	 *         <date>2010/3/09</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>BRSID1(001B0141):切換到mode=3(夜間模式)，執行海外卡的提款交易，都是Timeout</reason>
	 *         <date>2010/4/07</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>BRSID1(001B0251):G08002組電文內容修改</reason>
	 *         <date>2010/4/16</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>BRSID1(001B0257):產生海外主機電文時, FEPTXN_TXSEQ 只取後五位</reason>
	 *         <date>2010/4/19</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>J10033,J10206,J10060 組電文邏輯修改</reason>
	 *         <date>2010/4/21</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>modified by kyo for SPEC 修改 for 送主機時取交易傳輸編號邏輯變更</reason>
	 *         <date>2010/5/6</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>BugReport(001B0462):澳門ATM+香港卡：使用港幣帳戶提葡幣(IFE/IFW)，電文送主機時帶的幣別都是「0303」，應該一個是帶港幣，一個葡幣。</reason>
	 *         <date>2010/5/13</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>BugReport(001B0503):澳門ATM+香港卡：(1)進行IIQ、IFE(提葡幣)、IQ2皆出現「無輸入行資料」，拆送主機電文(AdapterIn)，在欄位TRMNO(ATM代號)皆為「00000」，由ATM送上來的電文欄位BRNO、WSNO皆有值存在。(2)IFW(提港幣)，出現「Syscom.FEP10.Common.Business.ATM.SendToCBSHK：索引和長度必須參考字串中的位置」</reason>
	 *         <date>2010/5/21</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>BugReport(001B0534):澳門ATM+香港卡提葡幣(IFW)，出現「Syscom.FEP10.Common.Business.ATM.SendToCBSHK：索引和長度必須參考字串中的位置」錯誤</reason>
	 *         <date>2010/5/25</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>補上CODEING與SPEC不合的地方</reason>
	 *         <date>2010/6/15</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>1.SPEC修改:Stanssn 應補左補0滿七位
	 *         2.移掉錯誤的CODE
	 *         3.BugReport(001B0756):Coding Error:補上CODING的與SPEC不合的地方
	 *         4.修改程式寫法: 改為較類似SPEC的寫法，避免邏輯遺漏</reason>
	 *         <date>2010/6/21</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>1.SPEC 修改:6/22依 ATM 所在地, 取帳務行基礎幣別
	 *         2.將10033與10206邏輯分開</reason>
	 *         <date>2010/6/21</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>SPEC修改:永豐通知修正主機電文</reason>
	 *         <date>2010/6/29</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>spec 修改查詢類交易非G08002的主管記號使用FEPTXN_CARD_SEQ</reason>
	 *         <date>2010/6/30</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>1.永豐通知修正主管記號
	 *         2.spec修改:G08002帳務別使用"00"而不是帶空白" ",TXAMT須為0</reason>
	 *         <date>2010/7/02</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>spec修改: 沖正時所需電文欄位與記帳不同</reason>
	 *         <date>2010/7/05</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>txcd=J00001 or K00001 時不應該只組成J00001 應該要判斷是J類還是K類</reason>
	 *         <date>2010/7/27</date>
	 *         </modify>
	 *         </history>
	 * @throws Exception
	 */
	private FEPReturnCode prepareCBSHKRequest(String hkCBSTxid, byte txType, HKGeneral hkGeneralForReq, RefBase<HKUnisysTextBase> reqClass, RefBase<HKUnisysTextBase> resClass) throws Exception {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		UnisysTXCD txcd = UnisysTXCD.valueOf(hkCBSTxid);

		Zone defZone = new Zone();
		Atmstat defATMSTAT = new Atmstat();
		Curcd defCurcd = new Curcd();
		String txtno = ""; // 交易傳輸編號
		String trmno = "";
		// Dim hkd As String = "0" & CInt(CurrencyType.HKD).ToString

		// 以提領幣別為 KEY, 讀取幣別檔, 取得 ISO 幣別文字碼
		if (getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()) != null) {
			defCurcd = getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur());
		} else {
			return IOReturnCode.CURCDNotFound;
		}

		/// *Phase 2 for INBK Update by Connie 2010/10/1 */
		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
			/// *跨行交易*/
			/// *取得跨行交易之虛擬櫃員機編號*/
			defZone = getZoneByZoneCode(ATMZone.TWN.toString());
			if (defZone == null) {
				return IOReturnCode.ZONENotFound;
			} else {
//				trmno = defZone.getZoneVirtualBrno() + "00";
			}
			// 以ATM機號(W_TRMNO), 取得交易傳輸編號(W_TXTNO)
			defATMSTAT.setAtmstatAtmno(trmno);
			defATMSTAT = atmstatMapper.selectByPrimaryKey(defATMSTAT.getAtmstatAtmno());
			if (defATMSTAT == null) {
				return CommonReturnCode.GetATMSeqNoError;
			}
			txtno = StringUtils.leftPad(defATMSTAT.getAtmstatTxSeq().toString(), 7, '0');
			if (txType == CBSTxType.Accounting.getValue()) {// 入扣帳
				getFeptxn().setFeptxnVirTxseq(txtno);
			} else if (txType == CBSTxType.EC.getValue()) {
				getFeptxn().setFeptxnConVirTxseq(txtno);
			}
			txtno = txtno.substring(txtno.length() - 5, 5);
		} else {
			/// *自行交易*/
			/// * 10/1 修改, 取消 FEPTXN_TXSEQ/FEPTXN_CON_TXSEQ 取號 */
			// 取得交易傳輸編號
			// BugReport(001B0264):FEPTXN_TXSEQ交易序號重覆取號，修正為取FEPTXN_ATMNO
			// 2010-06-21 by kyo for 修改程式寫法: 改為較類似SPEC的寫法，避免邏輯遺漏
			/// * 判斷跨區/同區交易 */
			if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
				if (ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())) {
					trmno = getFeptxn().getFeptxnAtmnoVir();
					defATMSTAT.setAtmstatAtmno(trmno);
					defATMSTAT = atmstatMapper.selectByPrimaryKey(defATMSTAT.getAtmstatAtmno());
					if (defATMSTAT == null) {
						return CommonReturnCode.GetATMSeqNoError;
					}
					txtno = StringUtils.leftPad(defATMSTAT.getAtmstatTxSeq().toString(), 7, '0');
					if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
						getFeptxn().setFeptxnVirTxseq(txtno);
					} else if (txType == (byte) CBSTxType.EC.getValue()) {
						getFeptxn().setFeptxnConVirTxseq(txtno);
					}
					txtno = txtno.substring(txtno.length() - 5, 5);
				} else {
					/// * 10/1 修改, 取消 FEPTXN_TXSEQ/FEPTXN_CON_TXSEQ 取號 */
					trmno = getFeptxn().getFeptxnAtmno();
				}
			} else {
				trmno = getFeptxn().getFeptxnAtmno();
			}
		}

		// 組上香港優利主機TITA電文
		// 2010-04-19 modified by Jim for 程式重新編排，因為所有交易都一樣的內容，所以提出來
		// .DSCPT1 = hkCBSTxid.Substring(3)
		// BugReport(001B0503):2010-05-21 跨區又非台灣區需要塞ATMNO
		// 2010-06-08 by kyo for 送主機邏輯僅能取5位
		if (StringUtils.isNotBlank(trmno)) {
			trmno = trmno.substring(0, 5);
		} else {
			trmno = getFeptxn().getFeptxnAtmno().substring(0, 5);
		}

		// 根據不同的主機電文代號呼叫不同的主機電文類別組電文
		switch (txcd) {
			case J00001:
			case K00001:
				/// * CBS_TITA HEADER */
				hkGeneralForReq.getmRequest().setTRMNO(trmno); // 輸入行機台號
				hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5, getFeptxn().getFeptxnTxseq().length())); // 交易傳輸編號
				hkGeneralForReq.getmRequest().setTTSKID(hkCBSTxid.substring(0, 1) + "I"); // 端末TASK ID
				hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "00"); // 交易代號
				hkGeneralForReq.getmRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				hkGeneralForReq.getmRequest().setPTYPE("0"); // 處理型態
				hkGeneralForReq.getmRequest().setDSCPT1(hkCBSTxid.substring(3)); // 摘要前三碼
				hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					hkGeneralForReq.getmRequest().setYCODE("0"); // "1"補送記號
				} else {
					hkGeneralForReq.getmRequest().setYCODE("0");
				}
				hkGeneralForReq.getmRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				hkGeneralForReq.getmRequest().setTXTYPE(String.valueOf(UnysisAccountType.M.getValue())); // 帳務別
				hkGeneralForReq.getmRequest().setCRDB("0"); // 借貸別
				hkGeneralForReq.getmRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				hkGeneralForReq.getmRequest().setNBCD("1"); // 無摺記號
				hkGeneralForReq.getmRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				hkGeneralForReq.getmRequest().setTRNMOD("0"); // 訓練／代登記號
				hkGeneralForReq.getmRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp()); // 幣別
				hkGeneralForReq.getmRequest().setTXAMT(BigDecimal.valueOf(0)); // 交易金額
				hkGeneralForReq.getmRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				hkGeneralForReq.getmRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());

				if ("J".equals(hkCBSTxid.substring(0, 1))) {
					reqClass.set(new HKJ00001Request());
					resClass.set(new HKJ00001Response());
				} else {
					reqClass.set(new HKK00001Request());
					resClass.set(new HKK00001Response());
				}

				break;
			case J10033:
			case K10033:
				/// * CBS_TITA HEADER */
				hkGeneralForReq.getmRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (txType == CBSTxType.Accounting.getValue()) {// 入扣帳
					hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5));
				} else if (txType == CBSTxType.EC.getValue()) {
					hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnConTxseq().substring(getFeptxn().getFeptxnConTxseq().length() - 5));
				}
				hkGeneralForReq.getmRequest().setTTSKID(hkCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				hkGeneralForReq.getmRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				hkGeneralForReq.getmRequest().setPTYPE("0"); // 處理型態
				hkGeneralForReq.getmRequest().setDSCPT1(hkCBSTxid.substring(3)); // 摘要前三碼
				hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					hkGeneralForReq.getmRequest().setYCODE("0"); // "1"補送記號
				} else {
					hkGeneralForReq.getmRequest().setYCODE("0");
				}
				hkGeneralForReq.getmRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				hkGeneralForReq.getmRequest().setTXTYPE(String.valueOf(UnysisAccountType.C.getValue())); // 帳務別
				hkGeneralForReq.getmRequest().setCRDB("1"); // 借貸別
				hkGeneralForReq.getmRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				hkGeneralForReq.getmRequest().setNBCD("1"); // 無摺記號
				hkGeneralForReq.getmRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				hkGeneralForReq.getmRequest().setTRNMOD("0"); // 訓練／代登記號
				hkGeneralForReq.getmRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp()); // 幣別
				hkGeneralForReq.getmRequest().setTXAMT(getFeptxn().getFeptxnTxAmt()); // 交易金額
				if (txType == CBSTxType.EC.getValue()) {// 沖正
					// 2010-11-17 by kyo for spec update
					hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.NonNormal.getValue())); // 更正記號
					hkGeneralForReq.getmRequest().setFXABRN(getFeptxn().getFeptxnAtmno().substring(0, 3));
					hkGeneralForReq.getmRequest().setFXWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					hkGeneralForReq.getmRequest().setFXTXTNO(getFeptxn().getFeptxnTxseq().substring(2, 7));
				}
				hkGeneralForReq.getmRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				hkGeneralForReq.getmRequest().setBXAMT(getFeptxn().getFeptxnTxAmt());
				// 2010-07-05 by kyo for spec修改: 7/2 以下欄位, 沖正時不需放入以下電文欄位
				if (txType == CBSTxType.Accounting.getValue()) {
					hkGeneralForReq.getmRequest().setBDTXD(getFeptxn().getFeptxnTbsdy());
					hkGeneralForReq.getmRequest().setCASH(getFeptxn().getFeptxnTxAmt());;
					// 2010-07-05 by kyo for spec修改:7/2 K類交易放入銀行代號
					if (txcd == UnisysTXCD.K10033) {
						hkGeneralForReq.getmRequest().setSTANBKNO(SysStatus.getPropertyValue().getSysstatHbkno());
					}
					// 2010-06-21 by kyo for SPEC修改:Stanssn 應補左補0滿七位
					// 2010-07-02 by kyo for SPEC修改:Stanssn 後來發現不需左補0滿七位
					hkGeneralForReq.getmRequest().setSTANSSN(txtno);
					hkGeneralForReq.getmRequest().setSECNO(unisysSecno);
					hkGeneralForReq.getmRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());
					hkGeneralForReq.getmRequest()
							.setATMID(StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(0, 3), 4, '0') + StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(3, 5), 4, '0'));
				}
				hkGeneralForReq.getmRequest().setASeacur(getFeptxn().getFeptxnTxCur()); /// * 取款時當地幣別 */
				hkGeneralForReq.getmRequest().setASeaamt((BigDecimal) getFeptxn().getFeptxnTxAmt());

				if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) == 1) {
					hkGeneralForReq.getmRequest().setTRDATE("0");
					hkGeneralForReq.getmRequest().setTRBRNO("0");
					hkGeneralForReq.getmRequest().setTRWSNO("0");
					hkGeneralForReq.getmRequest().setTRTXTNO("0");
				} else if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) > 1) {
					hkGeneralForReq.getmRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
					// BugReport(001B0534):2010-05-25 by kyo for coding error TRMNO可能為空 需要額外判斷避開
					if (StringUtils.isNotBlank(trmno) && trmno.length() >= 5) {
						hkGeneralForReq.getmRequest().setTRBRNO(trmno.substring(0, 3));
						hkGeneralForReq.getmRequest().setTRWSNO(trmno.substring(3, 5));
					} else {
						hkGeneralForReq.getmRequest().setTRBRNO(getFeptxn().getFeptxnAtmno().substring(0, 3));
						hkGeneralForReq.getmRequest().setTRWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					}
					hkGeneralForReq.getmRequest().setTRTXTNO(txtno);
				}

				hkGeneralForReq.getmRequest().setLOCBRNO(getFeptxn().getFeptxnAtmBrno());
				hkGeneralForReq.getmRequest().setATMCUR(defCurcd.getCurcdCurBsp());
				hkGeneralForReq.getmRequest().setATMAMT(getFeptxn().getFeptxnTxAmt());
				hkGeneralForReq.getmRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp());

				if ("J".equals(hkCBSTxid.substring(0, 1))) {
					reqClass.set(new HKJ10033Request());
					resClass.set(new HKJ10033Response());
				} else {
					reqClass.set(new HKK10033Request());
					resClass.set(new HKK10033Response());
				}

				break;
			case J10206:
			case K10206:
				/// * CBS_TITA HEADER */
				hkGeneralForReq.getmRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())
						&& ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())) {
					if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
						hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnVirTxseq().substring(getFeptxn().getFeptxnVirTxseq().length() - 5));
					} else if (txType == (byte) CBSTxType.EC.getValue()) {
						hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnConVirTxseq().substring(getFeptxn().getFeptxnConVirTxseq().length() - 5));
					}
				} else {
					if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
						hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5));
					} else if (txType == (byte) CBSTxType.EC.getValue()) {
						hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnConTxseq().substring(getFeptxn().getFeptxnConTxseq().length() - 5));
					}
				}
				hkGeneralForReq.getmRequest().setTTSKID(hkCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				hkGeneralForReq.getmRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				hkGeneralForReq.getmRequest().setPTYPE("0"); // 處理型態
				hkGeneralForReq.getmRequest().setDSCPT1(hkCBSTxid.substring(3)); // 摘要前三碼
				hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					hkGeneralForReq.getmRequest().setYCODE("0"); // "1"補送記號
				} else {
					hkGeneralForReq.getmRequest().setYCODE("0");
				}
				hkGeneralForReq.getmRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				hkGeneralForReq.getmRequest().setTXTYPE(String.valueOf(UnysisAccountType.C.getValue())); // 帳務別
				hkGeneralForReq.getmRequest().setCRDB("1"); // 借貸別
				hkGeneralForReq.getmRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				hkGeneralForReq.getmRequest().setNBCD("1"); // 無摺記號
				hkGeneralForReq.getmRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				hkGeneralForReq.getmRequest().setTRNMOD("0"); // 訓練／代登記號
				hkGeneralForReq.getmRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp()); // 幣別
				hkGeneralForReq.getmRequest().setTXAMT(getFeptxn().getFeptxnTxAmtAct()); // 交易金額
				if (txType == (byte) CBSTxType.EC.getValue()) {// 沖正
					// 2010-11-17 by kyo for spec update
					hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.NonNormal.getValue())); // 更正記號
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())
							&& ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())) {
						hkGeneralForReq.getmRequest().setFXABRN(getFeptxn().getFeptxnAtmnoVir().substring(0, 3)); // 交易輸入行
						hkGeneralForReq.getmRequest().setFXWSNO(getFeptxn().getFeptxnAtmnoVir().substring(3, 5)); // 原交易櫃台機號
						hkGeneralForReq.getmRequest().setFXTXTNO(getFeptxn().getFeptxnVirTxseq().substring(2, 7)); // 原交易傳輸編號
					} else {
						hkGeneralForReq.getmRequest().setFXABRN(getFeptxn().getFeptxnAtmno().substring(0, 3));
						hkGeneralForReq.getmRequest().setFXWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
						hkGeneralForReq.getmRequest().setFXTXTNO(getFeptxn().getFeptxnTxseq().substring(2, 7));
					}
				}
				hkGeneralForReq.getmRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				hkGeneralForReq.getmRequest().setBXAMT(getFeptxn().getFeptxnTxAmtAct());
				// 2010-07-05 by kyo for spec修改: 7/2 以下欄位, 沖正時不需放入以下電文欄位
				if (txType == (byte) CBSTxType.Accounting.getValue()) {
					hkGeneralForReq.getmRequest().setBDTXD(getFeptxn().getFeptxnTbsdyAct());
					hkGeneralForReq.getmRequest().setCASH(getFeptxn().getFeptxnTxAmt());
					// 2010-07-05 by kyo for spec修改:7/2 K類交易放入銀行代號
					if (txcd == UnisysTXCD.K10206) {
						hkGeneralForReq.getmRequest().setSTANBKNO(SysStatus.getPropertyValue().getSysstatHbkno());
					}
					hkGeneralForReq.getmRequest().setSTANSSN(txtno); // 長度5位
					hkGeneralForReq.getmRequest().setSECNO(unisysSecno);
					hkGeneralForReq.getmRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());
					hkGeneralForReq.getmRequest()
							.setATMID(StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(0, 3), 4, '0') + StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(3, 5), 4, '0'));
				}
				hkGeneralForReq.getmRequest().setASeacur(getFeptxn().getFeptxnTxCur());
				hkGeneralForReq.getmRequest().setASeaamt(getFeptxn().getFeptxnTxAmt());
				hkGeneralForReq.getmRequest().setCUSPAY(getFeptxn().getFeptxnFeeCustpayAct());
				if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) == 1) {
					hkGeneralForReq.getmRequest().setTRDATE("0");
					hkGeneralForReq.getmRequest().setTRBRNO("0");
					hkGeneralForReq.getmRequest().setTRWSNO("0");
					hkGeneralForReq.getmRequest().setTRTXTNO("0");
				} else if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) > 1) {
					hkGeneralForReq.getmRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
					// BugReport(001B0534):2010-05-25 by kyo for coding error TRMNO可能為空 需要額外判斷避開
					if (StringUtils.isNotBlank(trmno) && trmno.length() >= 5) {
						hkGeneralForReq.getmRequest().setTRBRNO(trmno.substring(0, 3));
						hkGeneralForReq.getmRequest().setTRWSNO(trmno.substring(3, 5));
					} else {
						hkGeneralForReq.getmRequest().setTRBRNO(getFeptxn().getFeptxnAtmno().substring(0, 3));
						hkGeneralForReq.getmRequest().setTRWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					}
					hkGeneralForReq.getmRequest().setTRTXTNO(txtno);
				}
				hkGeneralForReq.getmRequest().setLOCBRNO(getFeptxn().getFeptxnAtmBrno());
				hkGeneralForReq.getmRequest().setATMCUR(defCurcd.getCurcdCurBsp());
				hkGeneralForReq.getmRequest().setATMAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
				// SPEC 修改:6/22依 ATM 所在地, 取帳務行基礎幣別
				if (ATMZone.MAC.name().equals(getFeptxn().getFeptxnAtmZone())) {
					hkGeneralForReq.getmRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.MOP.name())).getCurcdCurBsp());
				} else {
					hkGeneralForReq.getmRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.TWD.name())).getCurcdCurBsp());
				}

				if ("J".equals(hkCBSTxid.substring(0, 1))) {
					reqClass.set(new HKJ10206Request());
					resClass.set(new HKJ10206Response());
				} else {
					reqClass.set(new HKK10206Request());
					resClass.set(new HKK10206Response());
				}

				break;
			case J10060:
			case K10060: // IFT /* 晶片卡轉帳 */
				/// * CBS_TITA HEADER */
				hkGeneralForReq.getmRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
					hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5));
				} else if (txType == (byte) CBSTxType.EC.getValue()) {
					hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnConTxseq().substring(getFeptxn().getFeptxnConTxseq().length() - 5));
				}
				hkGeneralForReq.getmRequest().setTTSKID(hkCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				hkGeneralForReq.getmRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				hkGeneralForReq.getmRequest().setPTYPE("0"); // 處理型態
				hkGeneralForReq.getmRequest().setDSCPT1(hkCBSTxid.substring(3)); // 摘要前三碼
				hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					hkGeneralForReq.getmRequest().setYCODE("0"); // "1"補送記號
				} else {
					hkGeneralForReq.getmRequest().setYCODE("0");
				}
				hkGeneralForReq.getmRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				hkGeneralForReq.getmRequest().setTXTYPE(String.valueOf(UnysisAccountType.M.getValue())); // 帳務別
				// 2012/10/22 Modify by Ruling for 香港卡無法作轉帳交易，轉帳電文CRDB要放1
				hkGeneralForReq.getmRequest().setCRDB("1"); // 借貸別
				// .CRDB = "0" '借貸別
				hkGeneralForReq.getmRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				hkGeneralForReq.getmRequest().setNBCD("1"); // 無摺記號
				hkGeneralForReq.getmRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				hkGeneralForReq.getmRequest().setTRNMOD("0"); // 訓練／代登記號
				hkGeneralForReq.getmRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp()); // 幣別
				hkGeneralForReq.getmRequest().setTXAMT(getFeptxn().getFeptxnTxAmt()); // 交易金額
				hkGeneralForReq.getmRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				hkGeneralForReq.getmRequest().setBDTXD(getFeptxn().getFeptxnTbsdy());
				hkGeneralForReq.getmRequest().setBXAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
				hkGeneralForReq.getmRequest().setPBAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
				if (getFeptxn().getFeptxnTrinActno().length() > 15) {
					hkGeneralForReq.getmRequest().setPACTNO(getFeptxn().getFeptxnTrinActno().substring(2, 16));
				}
				hkGeneralForReq.getmRequest().setPFG("1");
				hkGeneralForReq.getmRequest().setREMARK(getFeptxn().getFeptxnTrinActno());
				hkGeneralForReq.getmRequest().setRemarkM(getFeptxn().getFeptxnTroutActno());
				hkGeneralForReq.getmRequest().setSECNO(unisysSecno);
				hkGeneralForReq.getmRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());

				if (Integer.parseInt(hkGeneralForReq.getmRequest().getMODE()) == 1) {
					hkGeneralForReq.getmRequest().setTRDATE("0");
					hkGeneralForReq.getmRequest().setTRBRNO("0");
					hkGeneralForReq.getmRequest().setTRWSNO("0");
					hkGeneralForReq.getmRequest().setTRTXTNO("0");
				} else if (Integer.parseInt(hkGeneralForReq.getmRequest().getMODE()) > 1) {
					hkGeneralForReq.getmRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
					// BugReport(001B0534):2010-05-25 by kyo for coding error TRMNO可能為空 需要額外判斷避開
					if (StringUtils.isNotBlank(trmno) && trmno.length() >= 5) {
						hkGeneralForReq.getmRequest().setTRBRNO(trmno.substring(0, 3));
						hkGeneralForReq.getmRequest().setTRWSNO(trmno.substring(3, 5));
					} else {
						hkGeneralForReq.getmRequest().setTRBRNO(getFeptxn().getFeptxnAtmno().substring(0, 3));
						hkGeneralForReq.getmRequest().setTRWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					}
					// BugReport(001B0257):送主機的交易傳輸編號只需要5位
					hkGeneralForReq.getmRequest().setTRTXTNO(txtno);
				}
				hkGeneralForReq.getmRequest()
						.setATMID(StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(0, 3), 4, '0') + StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(3, 5), 4, '0'));
				// .LOCBRNO = ATMMSTR.ATM_BRNO_ST
				hkGeneralForReq.getmRequest().setLOCBRNO(getFeptxn().getFeptxnAtmBrno());
				hkGeneralForReq.getmRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp());

				if ("J".equals(hkCBSTxid.substring(0, 1))) {
					reqClass.set(new HKJ10060Request());
					resClass.set(new HKJ10060Response());
				} else {
					reqClass.set(new HKK10060Request());
					resClass.set(new HKK10060Response());
				}

				break;
			case G08002: // IFE
				/// * CBS_TITA HEADER */
				hkGeneralForReq.getmRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5)); // 交易傳輸編號
				hkGeneralForReq.getmRequest().setTTSKID("GA"); // 端末TASK ID
				hkGeneralForReq.getmRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 3)); // 交易代號
				hkGeneralForReq.getmRequest().setPTYPE("0"); // 處理型態
				hkGeneralForReq.getmRequest().setDSCPT1(hkCBSTxid.substring(3)); // 摘要前三碼
				hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					hkGeneralForReq.getmRequest().setYCODE("0"); // "1"補送記號
				} else {
					hkGeneralForReq.getmRequest().setYCODE("0");
				}
				defZone = getZoneByZoneCode(getFeptxn().getFeptxnZoneCode());
				if (defZone == null) {
					return IOReturnCode.ZONENotFound;
				} else {
//					hkGeneralForReq.getmRequest().setACTNO(defZone.getZoneVirtualBrno() + getFeptxn().getFeptxnTroutActno().substring(5, 16));
				}
				hkGeneralForReq.getmRequest().setTXTYPE("00"); // 帳務別
				hkGeneralForReq.getmRequest().setCRDB("0"); // 借貸別
				hkGeneralForReq.getmRequest().setSPCD("00"); // 主管記號
				hkGeneralForReq.getmRequest().setNBCD("1"); // 無摺記號
				hkGeneralForReq.getmRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				hkGeneralForReq.getmRequest().setTRNMOD("0"); // 訓練／代登記號
				hkGeneralForReq.getmRequest().setCURCD(getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()).getCurcdCurBsp()); // 幣別
				hkGeneralForReq.getmRequest().setTXAMT(BigDecimal.valueOf(0)); // 交易金額
				hkGeneralForReq.getmRequest().setFXFLAG("00");

				reqClass.set(new HKG08002Request());
				resClass.set(new HKG08002Response());

				// 207:/*海外分行在台灣跨行提款*/, 208:/*海外分行PLUS卡跨國提款*/, 209:/*海外分行PLUS卡跨行提款*/
				break;
			case J10207:
			case K10207:
			case J10208:
			case K10208:
			case J10209:
			case K10209:
				/// * CBS_TITA HEADER */
				hkGeneralForReq.getmRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
					hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnVirTxseq().substring(getFeptxn().getFeptxnVirTxseq().length() - 5));
				} else if (txType == (byte) CBSTxType.EC.getValue()) {
					hkGeneralForReq.getmRequest().setTXTNO(getFeptxn().getFeptxnConVirTxseq().substring(getFeptxn().getFeptxnConVirTxseq().length() - 5));
				}
				hkGeneralForReq.getmRequest().setTTSKID(hkCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				hkGeneralForReq.getmRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					hkGeneralForReq.getmRequest().setTXCD(hkCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				hkGeneralForReq.getmRequest().setPTYPE("0"); // 處理型態
				hkGeneralForReq.getmRequest().setDSCPT1(hkCBSTxid.substring(3)); // 摘要前三碼
				hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					hkGeneralForReq.getmRequest().setYCODE("0"); // "1"補送記號
				} else {
					hkGeneralForReq.getmRequest().setYCODE("0");
				}
				hkGeneralForReq.getmRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				hkGeneralForReq.getmRequest().setTXTYPE(String.valueOf(UnysisAccountType.M.getValue())); // 帳務別
				hkGeneralForReq.getmRequest().setCRDB("1"); // 借貸別
				hkGeneralForReq.getmRequest().setSPCD("00"); // 主管記號
				hkGeneralForReq.getmRequest().setNBCD("1"); // 無摺記號
				hkGeneralForReq.getmRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				hkGeneralForReq.getmRequest().setTRNMOD("0"); // 訓練／代登記號
				hkGeneralForReq.getmRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp()); // 幣別

				if (txType == (byte) CBSTxType.EC.getValue()) {
					// 2010-11-17 by kyo for spec update
					hkGeneralForReq.getmRequest().setHCODE(String.valueOf(HCODE.NonNormal.getValue())); // 更正記號
					hkGeneralForReq.getmRequest().setFXABRN(trmno.substring(0, 3));
					hkGeneralForReq.getmRequest().setFXWSNO(trmno.substring(3, 5));
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnVirTxseq().trim()) && getFeptxn().getFeptxnVirTxseq().length() > 5) {
						hkGeneralForReq.getmRequest().setFXTXTNO(getFeptxn().getFeptxnVirTxseq().substring(getFeptxn().getFeptxnVirTxseq().length() - 5)); // 原交易傳輸編號
					}
				}
				hkGeneralForReq.getmRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				/// * 10/20 修改為卡片所在地區營業日 by Connie*/
				hkGeneralForReq.getmRequest().setBDTXD(getFeptxn().getFeptxnTbsdyAct());
				hkGeneralForReq.getmRequest().setSTANBKNO(getFeptxn().getFeptxnBkno());
				hkGeneralForReq.getmRequest().setSTANSSN(getFeptxn().getFeptxnStan());
				hkGeneralForReq.getmRequest().setSECNO(unisysSecno);
				hkGeneralForReq.getmRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());
				hkGeneralForReq.getmRequest().setASeacur(getFeptxn().getFeptxnTxCur()); /// * 取款時當地幣別 */
				hkGeneralForReq.getmRequest().setASeaamt(getFeptxn().getFeptxnTxAmt());
				hkGeneralForReq.getmRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
				hkGeneralForReq.getmRequest().setTRBRNO(trmno.substring(0, 3));
				hkGeneralForReq.getmRequest().setTRWSNO(trmno.substring(3, 5));
				hkGeneralForReq.getmRequest().setTRTXTNO(txtno); // 已經在前面取後五位
				hkGeneralForReq.getmRequest().setATMID(getFeptxn().getFeptxnAtmno());
				hkGeneralForReq.getmRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp()); /// * 帳務行基礎幣別 */

				switch (hkGeneralForReq.getmRequest().getDSCPT1()) {
					case "207": /// *台灣地區非本行 ATM(跨行)提款, 要收手續費*/
						if (txType == (byte) CBSTxType.Accounting.getValue()) {
							/// *CheckCardStatus 已將 CARD_FREE_TW 借欄存入 FEPTXN*/
							switch (getFeptxn().getFeptxnBoxCnt()) {
								case 0:
									// 不減免手續費
									rtnCode = getInbkparmByPK();
									if (rtnCode != CommonReturnCode.Normal) {
										return rtnCode;
									}
									getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
									break;
								case 1:
									// 不收手續費
									getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
									break;
								case 2:
									/// *減免金額已於 CheckCardStatus 搬入 FEPTXN*/
									// 依金額減免
									// getFeptxn().getFeptxnFeeCustpayAct() = Card.CARD_WV_TW
									break;
							}
							RefString txAmtAct = new RefString(getFeptxn().getFeptxnTxAmtAct().toString());
							RefString exrate = new RefString(getFeptxn().getFeptxnExrate().toString());
							rtnCode = getExchangeAmount(ATMZone.HKG.toString(), getFeptxn().getFeptxnTxCur(), getFeptxn().getFeptxnTxCurAct(), getFeptxn().getFeptxnTxAmt(), txAmtAct, exrate);
							getFeptxn().setFeptxnTxAmtAct(new BigDecimal(txAmtAct.get()));
							getFeptxn().setFeptxnExrate(new BigDecimal(exrate.get()));
							if (rtnCode != CommonReturnCode.Normal) {
								return rtnCode;
							}
						}

						/// *TXAMT 為 HEADER 交易金額*/
						hkGeneralForReq.getmRequest().setTXAMT(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() + getFeptxn().getFeptxnFeeCustpayAct().intValue()));
						hkGeneralForReq.getmRequest().setBXAMT(hkGeneralForReq.getmRequest().getTXAMT());
						hkGeneralForReq.getmRequest().setBORATE(getFeptxn().getFeptxnExrate());
						hkGeneralForReq.getmRequest().setCUSPAY(getFeptxn().getFeptxnFeeCustpayAct());

						if ("J".equals(hkCBSTxid.substring(0, 1))) {
							reqClass.set(new HKJ10207Request());
							resClass.set(new HKJ10207Response());
						} else {
							reqClass.set(new HKK10207Request());
							resClass.set(new HKK10207Response());
						}

						break;
					case "208": /// *非本地銀行 ATM 提款, 要收手續費*/
						if (txType == (byte) CBSTxType.Accounting.getValue()) {
							/// *CheckCardStatus 已將 CARD_FREE_TW 借欄存入 FEPTXN*/
							switch (getFeptxn().getFeptxnBoxCnt()) {
								case 0:
									// 不減免手續費
									rtnCode = getInbkparmByPK();
									if (rtnCode != CommonReturnCode.Normal) {
										return rtnCode;
									}
									getFeptxn().setFeptxnFeeCustpayAct(getInbkparm().getInbkparmFeeCustpay());
									break;
								case 1:
									// 不收手續費
									getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
									break;
								case 2:
									/// *減免金額已於 CheckCardStatus 搬入 FEPTXN*/
									// 依金額減免
									break;
							}
						}

						// 2010-11-19 by kyo for Connie update spec:/*TXAMT 為 HEADER 交易金額*/
						if (txType == (byte) CBSTxType.Accounting.getValue()) {
							hkGeneralForReq.getmRequest().setTXAMT(BigDecimal.valueOf(0));
						} else {
							hkGeneralForReq.getmRequest().setTXAMT(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() + getFeptxn().getFeptxnFeeCustpayAct().intValue()));
						}
						hkGeneralForReq.getmRequest().setBXAMT(hkGeneralForReq.getmRequest().getTXAMT());
						hkGeneralForReq.getmRequest().setBORATE(BigDecimal.valueOf(0));
						hkGeneralForReq.getmRequest().setCUSPAY(getFeptxn().getFeptxnFeeCustpayAct());
						hkGeneralForReq.getmRequest().setACQCNTRY2(getFeptxn().getFeptxnRemark()); // 由於搬離businessbase，借用feptxn_remark
						// .ACQCNTRY2 = IntlTxn.INTLTXN_ACQ_CNTRY.Substring(1, 2)
						hkGeneralForReq.getmRequest().setAPluscur(getCurrencyByAlpha3(String.valueOf(CurrencyType.USD.name())).getCurcdCurBsp());
						hkGeneralForReq.getmRequest().setAPlusamt(getFeptxn().getFeptxnTxAmtSet());
						// 2012/11/09 Modify by Ruling for 香港晶片卡:修改香港主機電文(208)，增加限額欄位供Unisys判斷每日限額(7位)，在入扣帳時才帶入限額
						if (txType == (byte) CBSTxType.Accounting.getValue()) {
							hkGeneralForReq.getmRequest().setAPlusLmt(BigDecimal.valueOf(getCard().getCardTxLimit()));
						}

						if ("J".equals(hkCBSTxid.substring(0, 1))) {
							reqClass.set(new HKJ10208Request());
							resClass.set(new HKJ10208Response());
						} else {
							reqClass.set(new HKK10208Request());
							resClass.set(new HKK10208Response());
						}

						break;
					case "209": /// *本地他行 ATM 提款, 不收手續費*/
						/// *TXAMT 為 HEADER 交易金額*/
						hkGeneralForReq.getmRequest().setTXAMT(getFeptxn().getFeptxnTxAmt());
						hkGeneralForReq.getmRequest().setBXAMT(hkGeneralForReq.getmRequest().getTXAMT());
						hkGeneralForReq.getmRequest().setCUSPAY(BigDecimal.valueOf(0));
						hkGeneralForReq.getmRequest().setACQCNTRY2(getFeptxn().getFeptxnRemark()); // 由於搬離businessbase，借用feptxn_remark
						// .ACQCNTRY2 = IntlTxn.INTLTXN_ACQ_CNTRY.Substring(1, 2)
						hkGeneralForReq.getmRequest().setAPluscur(getCurrencyByAlpha3(String.valueOf(CurrencyType.USD.name())).getCurcdCurBsp());
						hkGeneralForReq.getmRequest().setAPlusamt(getFeptxn().getFeptxnTxAmtSet());

						if ("J".equals(hkCBSTxid.substring(0, 1))) {
							reqClass.set(new HKJ10209Request());
							resClass.set(new HKJ10209Response());
						} else {
							reqClass.set(new HKK10209Request());
							resClass.set(new HKK10209Response());
						}
						break;
				}
				break;
			default:
				break;
		}
		return rtnCode;
	}
}
