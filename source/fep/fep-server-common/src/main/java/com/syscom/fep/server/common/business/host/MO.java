package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
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
import com.syscom.fep.vo.text.unisys.mo.MOGeneral;
import com.syscom.fep.vo.text.unisys.mo.MOUnisysTextBase;
import com.syscom.fep.vo.text.unisys.mo.request.*;
import com.syscom.fep.vo.text.unisys.mo.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MO extends HostBase {
	private String _ProgramName = "MO";
	IntltxnMapper dbINTLTXN = SpringBeanFactoryUtil.getBean(IntltxnMapper.class);
	AllbankMapper allbankMapper = SpringBeanFactoryUtil.getBean(AllbankMapper.class);
	CbspendMapper cbspendMapper = SpringBeanFactoryUtil.getBean(CbspendMapper.class);
	BsdaysMapper bsdaysMapper = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
	ZoneMapper zoneMapper = SpringBeanFactoryUtil.getBean(ZoneMapper.class);
	AtmstatMapper atmstatMapper = SpringBeanFactoryUtil.getBean(AtmstatMapper.class);

	public MO(MessageBase txData) {
		super(txData);
	}

	/**
	 * 組送澳門優利主機電文
	 * 1.依據不同送主機電文編號，組送主機電文欄位
	 * 2.以UnysisAdapter作為介面和海外主機溝通
	 * 3.若主機回應成功，更新部分FEPTXN欄位
	 * 4.若Timeout，新增CBSPEND檔作為記錄
	 * 
	 * @param moCBSTxid 送主機電文id
	 * @param txType 電文類型: 1:交易/0:沖正
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
	public FEPReturnCode sendToCBSMO(String moCBSTxid, byte txType) {
		// BugReport(001B0475):若此參數不滿五位，會發生例外因此直接回其他類檢核錯誤
		if (moCBSTxid.length() < 5) {
			return ATMReturnCode.OtherCheckError;
		}

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		UnisysAdapter adapter = new UnisysAdapter(getGeneralData());
		MOGeneral moGeneralForReq = new MOGeneral();
		MOGeneral moGeneralForRes = new MOGeneral();
		@SuppressWarnings("unused")
		Zone defZone = new Zone();
		RefBase<MOUnisysTextBase> reqClass = new RefBase<MOUnisysTextBase>(null);
		RefBase<MOUnisysTextBase> resClass = new RefBase<MOUnisysTextBase>(null);
		UnisysTXCD unysisTxcd = UnisysTXCD.valueOf(moCBSTxid);
		String tita = StringUtils.EMPTY;
		// 2010-06-21 by kyo 修改寫法 改到prepare中使用
		// Dim trmno As String = StringUtils.EMPTY '輸入行機台號
		// Dim isCrossArea As Boolean = False

		try {
			// 2010-06-21 by kyo for 修改程式類似SPEC寫法:拿掉isCrossArea的使用
			rtnCode = prepareCBSMORequest(moCBSTxid, txType, moGeneralForReq, reqClass, resClass);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			tita = reqClass.get().makeMessageFromGeneral(moGeneralForReq);

			// Communicate with Unisys by adapter & update FEPTXN
			HostBase hostUtil = new HostBase(getGeneralData());
			rtnCode = hostUtil.sendToHostByAdapter(unysisTxcd, txType, adapter, tita, UnisysType.MAC);
			if (rtnCode == IOReturnCode.FEPTXNUpdateError || rtnCode == CommonReturnCode.ProgramException) {
				return rtnCode;
			}

			// 處理主機回應的電文
			// 2010-06-21 by kyo for 修改程式類似SPEC寫法:拿掉isCrossArea的使用
			rtnCode = processCBSMOResponse(unysisTxcd, txType, rtnCode, moGeneralForReq, moGeneralForRes, reqClass.get(), resClass.get(), adapter, tita);

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
	 * 2.以MOT24Adapter作為介面和海外主機溝通
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
	 *         <modifier>榮升</modifier>
	 *         <reason>MO送T24電文使用</reason>
	 *         <date>2013/06/19</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode sendToCBSMOT24(String CBSTxid, byte txType, boolean processFlag) {
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
				getLogContext().setRemark("SendToCBSMOT24-TYPE <> 1扣帳 2沖正-TYPE=" + String.valueOf(txType));
				this.logMessage(getLogContext());
				return ATMReturnCode.OtherCheckError;
			}

			// 2. 檢核澳門T24主機連線狀態
			FEPCache.reloadCache(CacheItem.SYSSTAT);
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatT24Mac())) {
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

			// -----Start Of 4. 組澳門T24主機電文CBS_TITA(共用)
			if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
				// 跨行交易
				company = ATMPConfig.getInstance().getMOPCompanyCode();
			} else {
				switch (CBSTxid) {
					case T24Version.A1000:
					case T24Version.A1050:
						// 自行ATM現金類交易(自行提款/代理提款)
						company = ATMPConfig.getInstance().getMOPCompanyCode().substring(0, 6) + getFeptxn().getFeptxnAtmBrno();
						break;
					case T24Version.A1030:
					case T24Version.A1070:
						// 澳門卡至台灣/香港跨區提款(A1030)、自行轉帳(A1070)
						company = ATMPConfig.getInstance().getMOPCompanyCode().substring(0, 6) + getFeptxn().getFeptxnTroutActno().substring(2, 5);
						break;
					case T24Version.B0001:
						company = ATMPConfig.getInstance().getMOPCompanyCode();
						break;
					default:
						company = StringUtils.EMPTY;
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
				tranactionID = StringUtils.EMPTY;
			}

			// ProcessTag
			if (processFlag) {
				processTag = "PROCESS/NULL";
			} else {
				processTag = "VALIDATE/NULL";
			}

			t24ReqMessage = new T24PreClass(company, version, reversetag, tranactionID, processTag);
			t24ReqMessage.setUserName(ATMPConfig.getInstance().getMOT24UserName());
			t24ReqMessage.setPassword(ATMPConfig.getInstance().getMOT24Password());
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

			// 4. 組澳門 T24 TITA 電文
			if (!noTITA) {
				genT24TITA(t24ReqMessage, wEJ, CBSTxid);
			}
			// -----End Of 4. 組澳門T24主機電文CBS_TITA(共用)

			// 5. 將組好的T24電文送往澳門T24主機
			// 5.1送T24主機前，先更新交易記錄
			if ((int) txType == T24TxType.Accounting.getValue()) { // 入扣帳
				if (Integer.parseInt(CBSTxid.substring(1, 2)) == AccountOpType.Withdraw.getValue()) // 扣帳類
				{
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Request); // CBS Req H1
				} else {
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_INQ_Request); // CBS INQ Req I1
				}
			} else if ((int) txType == T24TxType.EC.getValue()) { // 沖正
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_EC_Request); // CBS EC Req X1
			}

			if (T24Version.A1050.equals(CBSTxid)) {
				// 代理跨區提款(A1050)存入不冋欄位
				getFeptxn().setFeptxnVirCbsTxCode(CBSTxid);
				getFeptxn().setFeptxnVirCbsTimeout(DbHelper.toShort(true)); // CBS逾時 FLAG
			} else {
				getFeptxn().setFeptxnCbsTxCode(CBSTxid);
				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(true)); // CBS逾時 FLAG
			}

			if (processFlag) {
				getFeptxn().setFeptxnNeedSendCbs((short) txType);
				if ("A".equals(CBSTxid.substring(0, 1))) {// T24 version A 類 - 帳務類交易
					if (T24Version.A1050.equals(CBSTxid)) {
						// 代理跨區提款(A1050)存入不冋欄位
						getFeptxn().setFeptxnVirAccType((short) AccountingType.UnKnow.getValue()); // 未明
					} else {
						getFeptxn().setFeptxnAccType((short) AccountingType.UnKnow.getValue()); // 未明
					}
				}
			}

			rtnCode = FEPReturnCode.HostResponseTimeout;

			if (this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()) < 1) {
				return IOReturnCode.FEPTXNUpdateError;
			}

			// 傳送電文至T24
			adapter.setMessageToT24(t24ReqMessage.getGenT24ReqOFS());
			adapter.setArea(ZoneCode.MAC);
			rtnCode = adapter.sendReceive();
			// SPEC 5.1後半段(Company、Version、Reversetag等等)程式碼寫在前面的第四點

			// 5.2設定TIMER等待澳門T24主機回應訊息
			if (rtnCode == CommonReturnCode.HostResponseTimeout || rtnCode == CommonReturnCode.ProgramException || rtnCode == CommonReturnCode.CBSResponseError) {
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {
					// 帳務類T24電文TimeOut時，寫入MOCBSPEND
					rtnCode2 = t24TimeOutProcess(CBSTxid, t24ReqMessage.getReverseTag(), adapter.getMessageToT24());
				}
				return rtnCode;
			}

			boolean t24ResponseNormal = false;
			if (T24Version.B0001.equals(CBSTxid)) {
				t24ResponseNormal = t24ReqMessage.parseT24RspOfsForBType(adapter.getMessageFromT24(), CBSTxid.toString());
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

			// 5.3 收到澳門T24主機回應電文
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

			String channelRC = StringUtils.EMPTY;
			if (!NormalRC.FEP_OK.equals(t24ReqMessage.getTOTATransResult().get("EB.ERROR"))) {// "0000"
				// 失敗
				if ("A".equals(CBSTxid.substring(0, 1)) && processFlag) {// T24 version A 類 - 帳務類交易
					if (CBSTxid.equals(T24Version.A1050)) {
						// 代理跨區提款(A1050), 抓取不冋欄位
						if ("A2".equals(CBSTxid.substring(0, 2)) || txType == T24TxType.EC.getValue()) {
							getFeptxn().setFeptxnVirAccType((short) AccountingType.ECFail.getValue()); // 3 入帳或沖正失敗
						} else if ("A1".equals(CBSTxid.substring(0, 2))) {
							getFeptxn().setFeptxnVirAccType((short) AccountingType.UnAccounting.getValue()); // 3 未記帳
						}
					} else {
						if (txType == T24TxType.EC.getValue()) {
							// 沖正交易
							getFeptxn().setFeptxnAccType((short) AccountingType.ECFail.getValue()); // 3 入帳或沖正失敗
						} else {
							// 扣帳或代理類交易
							getFeptxn().setFeptxnAccType((short) AccountingType.UnAccounting.getValue()); // 0 未記帳
						}
					}
				}

				getLogContext().setProgramName(_ProgramName);
				// 直接將 T24 之 Error Message 傳入以取得 Channel RC, T24 RC, T24 ErrMsg
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

				if ((!"A2".equals(CBSTxid.substring(0, 2)) || processFlag) && txType != 2) {// 入帳及沖正交易若上主機失敗亦需視為失功，其他交易則回覆錯誤
					if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
						getFeptxn().setFeptxnReplyCode(channelRC); // ATM發動交易需將 CBS RC 轉換為 ATM RC
					} else {
						getFeptxn().setFeptxnRepRc(channelRC); // FISC發動交易需將 CBS RC 轉換為 FISC RC
					}
				}

				return CommonReturnCode.CBSResponseError;
			} else {
				// CBS回覆成功 EB.ERROR = 0000
				if ("A".equals(CBSTxid.substring(0, 1))) {
					if (processFlag) {// T24 version A類 - 帳務類交易
						if (CBSTxid.equals(T24Version.A1050)) {
							// 代理跨區提款(A1050)存入不冋欄位
							getFeptxn().setFeptxnVirAccType((short) txType);
							getFeptxn().setFeptxnVirCbsRrn(t24ReqMessage.getTOTATransContent().get("transactionId"));
						} else {
							getFeptxn().setFeptxnAccType((short) txType);
							getFeptxn().setFeptxnCbsRrn(t24ReqMessage.getTOTATransContent().get("transactionId"));
						}
					}
				}
			}

			if (txType == T24TxType.EC.getValue()) {// 2 沖正交易
				return rtnCode;
			}

			// 5.4依交易類別更新 FEPTXN
			rtnCode = updateFEPTXNbyCBSTxid(CBSTxid, t24ReqMessage);

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(_ProgramName + ".sendToCBSMOT24");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 組送澳門優利主機電文
	 * 1.依據不同送主機電文編號，組送主機電文欄位
	 * 2.以UnysisAdapter作為介面和海外主機溝通
	 * 3.若主機回應成功，更新部分FEPTXN欄位
	 * 4.若Timeout，新增CBSPEND檔作為記錄
	 * 
	 * @param moCBSTxid 送主機電文id
	 * @param txType 電文類型: 1:交易/0:沖正
	 * @return FEPReturnCode
	 * 
	 *         <history>
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
	 *         <reason>BugReport(001B0452):(1)澳門ATM+澳門卡(同區)：提款HKD100元，換算MOP103.8，餘額只扣除100元，應扣除換算成帳戶幣別的金額。(ej=6980)
	 *         (2)澳門ATM+香港卡(跨區)：提款HKD100元，手續費20元，餘額只扣除100元，應扣除提款金額+手續費。(ej=7054)</reason>
	 *         <date>2010/5/12</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>修改Bug462時一併修改SPEC邏輯</reason>
	 *         <date>2010/5/13</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>BugReport(001B0469):澳門ATM+澳門卡：提款HKD500元，FEPTXN_EXRATE顯示「0」，餘額只扣除500元，ej=8984</reason>
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
	 *         <reason>TRMNO僅取前五位搬移</reason>
	 *         <date>2010/6/08</date>
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
	private FEPReturnCode prepareCBSMORequest(String moCBSTxid, byte txType, MOGeneral moGeneralForReq, RefBase<MOUnisysTextBase> reqClass, RefBase<MOUnisysTextBase> resClass) throws Exception {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		UnisysTXCD txcd = UnisysTXCD.valueOf(moCBSTxid);

		Zone defZone = new Zone();
		Atmstat defATMSTAT = new Atmstat();
		@SuppressWarnings("unused")
		Curcd defCurcd = new Curcd();
		String txtno = StringUtils.EMPTY; // 交易傳輸編號
		String trmno = StringUtils.EMPTY;
		// Dim hkd As String = "0" & CInt(CurrencyType.HKD).ToString

		// 以提領幣別為 KEY, 讀取幣別檔, 取得 ISO 幣別文字碼
		if (getCurrencyByAlpha3(getFeptxn().getFeptxnTxCurAct()) != null) {
			defCurcd = getCurrencyByAlpha3(getFeptxn().getFeptxnTxCurAct());
		} else {
			return IOReturnCode.CURCDNotFound;
		}

		// 取得交易傳輸編號
		// 2010-10-27 by kyo for spec update:
		/// *Phase 2 for INBK Update by Connie 2010/10/25 */
		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
			/// *跨行交易*/
			/// *取得跨行交易之虛擬櫃員機編號*/
			defZone = getZoneByZoneCode(ATMZone.TWN.toString());
			if (defZone == null) {
				return IOReturnCode.ZONENotFound;
			}
//			trmno = defZone.getZoneVirtualBrno() + "00";
			// 以ATM機號(W_TRMNO), 取得交易傳輸編號(W_TXTNO)
			if (txType == CBSTxType.Accounting.getValue()) {// 入扣帳
				defATMSTAT.setAtmstatAtmno(trmno);
				defATMSTAT = atmstatMapper.selectByPrimaryKey(defATMSTAT.getAtmstatAtmno());
				if (defATMSTAT == null) {
					return CommonReturnCode.GetATMSeqNoError;
				}
				txtno = StringUtils.leftPad(defATMSTAT.getAtmstatTxSeq().toString(), 7, '0');
				getFeptxn().setFeptxnVirTxseq(txtno);
			} else if (txType == CBSTxType.EC.getValue()) {
				defATMSTAT.setAtmstatAtmno(trmno);
				defATMSTAT = atmstatMapper.selectByPrimaryKey(defATMSTAT.getAtmstatAtmno());
				if (defATMSTAT == null) {
					return CommonReturnCode.GetATMSeqNoError;
				}
				txtno = StringUtils.leftPad(defATMSTAT.getAtmstatTxSeq().toString(), 7, '0');
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
					// 澳門卡, 以香港 ATMNO 傳入主機
					trmno = getFeptxn().getFeptxnAtmno();
				}
			} else {
				// 同區交易
				trmno = getFeptxn().getFeptxnAtmno();
			}
		}

		// 組上澳門優利主機TITA電文
		// 2010-04-19 modified by Jim for 程式重新編排，因為所有交易都一樣的內容，所以提出來
		// .DSCPT1 = moCBSTxid.Substring(3)
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
				moGeneralForReq.getRequest().setTRMNO(trmno); // 輸入行機台號
				moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5)); // 交易傳輸編號
				moGeneralForReq.getRequest().setTTSKID(moCBSTxid.substring(0, 1) + "I"); // 端末TASK ID
				moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "00"); // 交易代號
				moGeneralForReq.getRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				moGeneralForReq.getRequest().setPTYPE("0"); // 處理型態
				moGeneralForReq.getRequest().setDSCPT1(moCBSTxid.substring(3)); // 摘要前三碼
				moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					moGeneralForReq.getRequest().setYCODE("0"); // "1"補送記號
				} else {
					moGeneralForReq.getRequest().setYCODE("0");
				}
				moGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				moGeneralForReq.getRequest().setTXTYPE(String.valueOf(UnysisAccountType.M.getValue())); // 帳務別
				moGeneralForReq.getRequest().setCRDB("0"); // 借貸別
				moGeneralForReq.getRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				moGeneralForReq.getRequest().setNBCD("1"); // 無摺記號
				moGeneralForReq.getRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				moGeneralForReq.getRequest().setTRNMOD("0"); // 訓練／代登記號
				moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(getFeptxn().getFeptxnTxCurAct())).getCurcdCurBsp()); // 幣別
				moGeneralForReq.getRequest().setTXAMT(BigDecimal.valueOf(0)); // 交易金額
				moGeneralForReq.getRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				moGeneralForReq.getRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());

				if ("J".equals(moCBSTxid.substring(0, 1))) {
					reqClass.set(new MOJ00001Request());
					resClass.set(new MOJ00001Response());
				} else {
					reqClass.set(new MOK00001Request());
					resClass.set(new MOK00001Response());
				}

				break;
			case J10033:
			case K10033:
				/// * CBS_TITA HEADER */
				moGeneralForReq.getRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (txType == CBSTxType.Accounting.getValue()) {// 入扣帳
					moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5));
				} else if (txType == CBSTxType.EC.getValue()) {
					moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnConTxseq().substring(getFeptxn().getFeptxnConTxseq().length() - 5));
				}
				moGeneralForReq.getRequest().setTTSKID(moCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				moGeneralForReq.getRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				moGeneralForReq.getRequest().setPTYPE("0"); // 處理型態
				moGeneralForReq.getRequest().setDSCPT1(moCBSTxid.substring(3)); // 摘要前三碼
				moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					moGeneralForReq.getRequest().setYCODE("0"); // "1"補送記號
				} else {
					moGeneralForReq.getRequest().setYCODE("0");
				}
				moGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				moGeneralForReq.getRequest().setTXTYPE(String.valueOf(UnysisAccountType.C.getValue())); // 帳務別
				moGeneralForReq.getRequest().setCRDB("1"); // 借貸別
				moGeneralForReq.getRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				moGeneralForReq.getRequest().setNBCD("1"); // 無摺記號
				moGeneralForReq.getRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				moGeneralForReq.getRequest().setTRNMOD("0"); // 訓練／代登記號
				moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(getFeptxn().getFeptxnTxCurAct())).getCurcdCurBsp()); // 幣別
				if (!getFeptxn().getFeptxnTxCurAct().equals(getFeptxn().getFeptxnTxCur())) {
					moGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmtAct()); /// * 帳戶交易金額 */
				} else {
					moGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt()); /// * 提領金額 */
				}
				moGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt()); // 交易金額
				if (txType == CBSTxType.EC.getValue()) {// 沖正
					// 2010-11-17 by kyo for spec update
					moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.NonNormal.getValue())); // 更正記號
					moGeneralForReq.getRequest().setFXABRN(getFeptxn().getFeptxnAtmno().substring(0, 3));
					moGeneralForReq.getRequest().setFXWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					moGeneralForReq.getRequest().setFXTXTNO(getFeptxn().getFeptxnTxseq().substring(2, 7));
				}
				moGeneralForReq.getRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				if (!getFeptxn().getFeptxnTxCurAct().equals(getFeptxn().getFeptxnTxCur())) {
					moGeneralForReq.getRequest().setBXAMT(getFeptxn().getFeptxnTxAmtAct()); /// * 帳戶交易金額 */
				} else {
					moGeneralForReq.getRequest().setBXAMT(getFeptxn().getFeptxnTxAmt()); /// * 提領金額 */
				}
				moGeneralForReq.getRequest().setBXAMT(getFeptxn().getFeptxnTxAmt());
				// 2010-07-05 by kyo for spec修改: 7/2 以下欄位, 沖正時不需放入以下電文欄位
				if (txType == CBSTxType.Accounting.getValue()) {
					moGeneralForReq.getRequest().setBDTXD(getFeptxn().getFeptxnTbsdy());
					moGeneralForReq.getRequest().setCASH(getFeptxn().getFeptxnTxAmt());;
					// 2010-07-05 by kyo for spec修改:7/2 K類交易放入銀行代號
					if (txcd == UnisysTXCD.K10033) {
						moGeneralForReq.getRequest().setSTANBKNO(SysStatus.getPropertyValue().getSysstatHbkno());
					}
					// 2010-06-21 by kyo for SPEC修改:Stanssn 應補左補0滿七位
					// 2010-07-02 by kyo for SPEC修改:Stanssn 後來發現不需左補0滿七位
					moGeneralForReq.getRequest().setSTANSSN(txtno);
					moGeneralForReq.getRequest().setSECNO(unisysSecno);
					moGeneralForReq.getRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());
					moGeneralForReq.getRequest()
							.setATMID(StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(0, 3), 4, '0') + StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(3, 5), 4, '0'));
				}
				moGeneralForReq.getRequest().setASeacur(getFeptxn().getFeptxnTxCur()); /// * 取款時當地幣別 */
				moGeneralForReq.getRequest().setASeaamt((BigDecimal) getFeptxn().getFeptxnTxAmt());

				if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) == 1) {
					moGeneralForReq.getRequest().setTRDATE("0");
					moGeneralForReq.getRequest().setTRBRNO("0");
					moGeneralForReq.getRequest().setTRWSNO("0");
					moGeneralForReq.getRequest().setTRTXTNO("0");
				} else if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) > 1) {
					moGeneralForReq.getRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
					// BugReport(001B0534):2010-05-25 by kyo for coding error TRMNO可能為空 需要額外判斷避開
					if (StringUtils.isNotBlank(trmno) && trmno.length() >= 5) {
						moGeneralForReq.getRequest().setTRBRNO(trmno.substring(0, 3));
						moGeneralForReq.getRequest().setTRWSNO(trmno.substring(3, 5));
					} else {
						moGeneralForReq.getRequest().setTRBRNO(getFeptxn().getFeptxnAtmno().substring(0, 3));
						moGeneralForReq.getRequest().setTRWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					}
					moGeneralForReq.getRequest().setTRTXTNO(txtno);
				}

				moGeneralForReq.getRequest().setLOCBRNO(getFeptxn().getFeptxnAtmBrno());
				Curcd curcdtmp = new Curcd();
				if (getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()) != null) {
					curcdtmp = getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur());
				} else {
					return IOReturnCode.CURCDNotFound;
				}
				moGeneralForReq.getRequest().setATMCUR(curcdtmp.getCurcdCurBsp());
				moGeneralForReq.getRequest().setATMAMT(getFeptxn().getFeptxnTxAmt());
				moGeneralForReq.getRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.MOP.name())).getCurcdCurBsp());

				if ("J".equals(moCBSTxid.substring(0, 1))) {
					reqClass.set(new MOJ10033Request());
					resClass.set(new MOJ10033Response());
				} else {
					reqClass.set(new MOK10033Request());
					resClass.set(new MOK10033Response());
				}

				break;
			case J10206:
			case K10206:
				/// * CBS_TITA HEADER */
				moGeneralForReq.getRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir()) && ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())) {
					if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
						moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnVirTxseq().substring(getFeptxn().getFeptxnVirTxseq().length() - 5));
					} else if (txType == (byte) CBSTxType.EC.getValue()) {
						moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnConVirTxseq().substring(getFeptxn().getFeptxnConVirTxseq().length() - 5));
					}
				} else {
					if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
						moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5));
					} else if (txType == (byte) CBSTxType.EC.getValue()) {
						moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnConTxseq().substring(getFeptxn().getFeptxnConTxseq().length() - 5));
					}
				}
				moGeneralForReq.getRequest().setTTSKID(moCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				moGeneralForReq.getRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				moGeneralForReq.getRequest().setPTYPE("0"); // 處理型態
				moGeneralForReq.getRequest().setDSCPT1(moCBSTxid.substring(3)); // 摘要前三碼
				moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					moGeneralForReq.getRequest().setYCODE("0"); // "1"補送記號
				} else {
					moGeneralForReq.getRequest().setYCODE("0");
				}
				moGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				moGeneralForReq.getRequest().setTXTYPE(String.valueOf(UnysisAccountType.C.getValue())); // 帳務別
				moGeneralForReq.getRequest().setCRDB("1"); // 借貸別
				moGeneralForReq.getRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				moGeneralForReq.getRequest().setNBCD("1"); // 無摺記號
				moGeneralForReq.getRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				moGeneralForReq.getRequest().setTRNMOD("0"); // 訓練／代登記號
				moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(getFeptxn().getFeptxnTxCurAct())).getCurcdCurBsp()); // 幣別
				moGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmtAct()); // 交易金額
				if (txType == (byte) CBSTxType.EC.getValue()) {// 沖正
					// 2010-11-17 by kyo for spec update
					moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.NonNormal.getValue())); // 更正記號
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir()) && ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())) {
						moGeneralForReq.getRequest().setFXABRN(getFeptxn().getFeptxnAtmnoVir().substring(0, 3)); // 交易輸入行
						moGeneralForReq.getRequest().setFXWSNO(getFeptxn().getFeptxnAtmnoVir().substring(3, 5)); // 原交易櫃台機號
						moGeneralForReq.getRequest().setFXTXTNO(getFeptxn().getFeptxnVirTxseq().substring(2, 7)); // 原交易傳輸編號
					} else {
						moGeneralForReq.getRequest().setFXABRN(getFeptxn().getFeptxnAtmno().substring(0, 3));
						moGeneralForReq.getRequest().setFXWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
						moGeneralForReq.getRequest().setFXTXTNO(getFeptxn().getFeptxnTxseq().substring(2, 7));
					}
				}
				moGeneralForReq.getRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				moGeneralForReq.getRequest().setBXAMT(getFeptxn().getFeptxnTxAmtAct());
				// 2010-07-05 by kyo for spec修改: 7/2 修正與永豐提供LOG比對後修正作法
				if (CurrencyType.MOP.toString().equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setFOAMT1(BigDecimal.valueOf(0));
					moGeneralForReq.getRequest().setBXAMT(getFeptxn().getFeptxnTxAmtAct());
				} else if (CurrencyType.HKD.toString().equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setFOAMT1(getFeptxn().getFeptxnTxAmtAct());
					moGeneralForReq.getRequest().setBXAMT(BigDecimal.valueOf(0));
				}

				// 2010-07-05 by kyo for spec修改: 7/2 修正與永豐提供LOG比對後修正作法
				if (ATMZone.HKG.toString().equals(getFeptxn().getFeptxnAtmZone())) {
					moGeneralForReq.getRequest().setBORATE(getFeptxn().getFeptxnExrate());
				} else {
					moGeneralForReq.getRequest().setBORATE(BigDecimal.valueOf(0));
				}
				if (txType == (byte) CBSTxType.Accounting.getValue()) {
					moGeneralForReq.getRequest().setBDTXD(getFeptxn().getFeptxnTbsdyAct());
					moGeneralForReq.getRequest().setCASH(getFeptxn().getFeptxnTxAmt());
					// 2010-07-05 by kyo for spec修改:7/2 K類交易放入銀行代號
					if (txcd == UnisysTXCD.K10206) {
						moGeneralForReq.getRequest().setSTANBKNO(SysStatus.getPropertyValue().getSysstatHbkno());
					}
					// 2010-06-21 by kyo for SPEC修改:Stanssn 應補左補0滿七位
					// 2010-07-02 by kyo for SPEC修改:Stanssn 後來發現不需左補0滿七位
					moGeneralForReq.getRequest().setSTANSSN(txtno); // 長度5位
					moGeneralForReq.getRequest().setSECNO(unisysSecno);
					moGeneralForReq.getRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());
					moGeneralForReq.getRequest()
							.setATMID(StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(0, 3), 4, '0') + StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(3, 5), 4, '0'));
				}
				moGeneralForReq.getRequest().setASeacur(getFeptxn().getFeptxnTxCur());
				moGeneralForReq.getRequest().setASeaamt(getFeptxn().getFeptxnTxAmt());
				moGeneralForReq.getRequest().setCUSPAY(getFeptxn().getFeptxnFeeCustpayAct());
				if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) == 1) {
					moGeneralForReq.getRequest().setTRDATE("0");
					moGeneralForReq.getRequest().setTRBRNO("0");
					moGeneralForReq.getRequest().setTRWSNO("0");
					moGeneralForReq.getRequest().setTRTXTNO("0");
				} else if (Integer.parseInt(getFeptxn().getFeptxnTxnmode().toString()) > 1) {
					moGeneralForReq.getRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
					// BugReport(001B0534):2010-05-25 by kyo for coding error TRMNO可能為空 需要額外判斷避開
					if (StringUtils.isNotBlank(trmno) && trmno.length() >= 5) {
						moGeneralForReq.getRequest().setTRBRNO(trmno.substring(0, 3));
						moGeneralForReq.getRequest().setTRWSNO(trmno.substring(3, 5));
					} else {
						moGeneralForReq.getRequest().setTRBRNO(getFeptxn().getFeptxnAtmno().substring(0, 3));
						moGeneralForReq.getRequest().setTRWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					}
					moGeneralForReq.getRequest().setTRTXTNO(txtno);
				}
				moGeneralForReq.getRequest().setLOCBRNO(getFeptxn().getFeptxnAtmBrno());
				curcdtmp = new Curcd();
				if (getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()) != null) {
					curcdtmp = getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur());
				} else {
					return IOReturnCode.CURCDNotFound;
				}

				moGeneralForReq.getRequest().setATMCUR(curcdtmp.getCurcdCurBsp());
				moGeneralForReq.getRequest().setATMAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
				// SPEC 修改:6/22依 ATM 所在地, 取帳務行基礎幣別
				if (ATMZone.MAC.name().equals(getFeptxn().getFeptxnAtmZone())) {
					moGeneralForReq.getRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp());
				} else {
					moGeneralForReq.getRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.TWD.name())).getCurcdCurBsp());
				}
				// BugReport(001B0452):提領幣別與主帳號幣別不同時，要用交易金額
				// 2010-07-05 by kyo for spec修改:7/2 修正澳門跨區提款做法
				if (String.valueOf(CurrencyType.MOP.name()).equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setABcuspay(BigDecimal.valueOf(0));
				} else if (String.valueOf(CurrencyType.HKD.name()).equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setABcuspay(getFeptxn().getFeptxnFeeCustpay());
				}

				// 2010-07-05 by kyo for spec修改:7/2 修正澳門跨區提款做法
				if (String.valueOf(CurrencyType.MOP.name()).equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setABcusprat(BigDecimal.valueOf(0));
				} else if (String.valueOf(CurrencyType.HKD.name()).equals(getFeptxn().getFeptxnTxCurAct()) 
						&& ATMZone.HKG.name().equals(getFeptxn().getFeptxnAtmZone())) {
					// 澳門卡港幣戶於香港跨區提款
					moGeneralForReq.getRequest().setABcusprat(getFeptxn().getFeptxnExrate());
				} else if (String.valueOf(CurrencyType.HKD.name()).equals(getFeptxn().getFeptxnTxCurAct()) 
						&& ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())) {
					// 澳門卡港幣戶於台灣跨區提款
					moGeneralForReq.getRequest().setABcusprat(BigDecimal.valueOf(ATMPConfig.getInstance().getHKDMOPRate()));
				}
				if ("J".equals(moCBSTxid.substring(0, 1))) {
					reqClass.set(new MOJ10206Request());
					resClass.set(new MOJ10206Response());
				} else {
					reqClass.set(new MOK10206Request());
					resClass.set(new MOK10206Response());
				}

				break;
			case J10060:
			case K10060: // IFT /* 晶片卡轉帳 */
				/// * CBS_TITA HEADER */
				moGeneralForReq.getRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
					moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5));
				} else if (txType == (byte) CBSTxType.EC.getValue()) {
					moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnConTxseq().substring(getFeptxn().getFeptxnConTxseq().length() - 5));
				}
				moGeneralForReq.getRequest().setTTSKID(moCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				moGeneralForReq.getRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				moGeneralForReq.getRequest().setPTYPE("0"); // 處理型態
				moGeneralForReq.getRequest().setDSCPT1(moCBSTxid.substring(3)); // 摘要前三碼
				moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					moGeneralForReq.getRequest().setYCODE("0"); // "1"補送記號
				} else {
					moGeneralForReq.getRequest().setYCODE("0");
				}
				moGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				moGeneralForReq.getRequest().setTXTYPE(String.valueOf(UnysisAccountType.M.getValue())); // 帳務別
				// 2012/10/22 Modify by Ruling for 香港卡無法作轉帳交易，轉帳電文CRDB要放1
				moGeneralForReq.getRequest().setCRDB("1"); // 借貸別
				// .CRDB = "0" '借貸別
				moGeneralForReq.getRequest().setSPCD(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0')); // 主管記號
				moGeneralForReq.getRequest().setNBCD("1"); // 無摺記號
				moGeneralForReq.getRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				moGeneralForReq.getRequest().setTRNMOD("0"); // 訓練／代登記號
				moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(getFeptxn().getFeptxnTxCurAct())).getCurcdCurBsp()); // 幣別
				moGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt()); // 交易金額
				moGeneralForReq.getRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				moGeneralForReq.getRequest().setBDTXD(getFeptxn().getFeptxnTbsdy());
				if (String.valueOf(CurrencyType.MOP.name()).equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setFPBAMT(BigDecimal.valueOf(0));
					moGeneralForReq.getRequest().setFACTNO("0");
					moGeneralForReq.getRequest().setFFG("0");
					moGeneralForReq.getRequest().setBXAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
					moGeneralForReq.getRequest().setPBAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
					moGeneralForReq.getRequest().setPACTNO(getFeptxn().getFeptxnTrinActno().substring(2));
					moGeneralForReq.getRequest().setPFG("1");
				} else {
					moGeneralForReq.getRequest().setFPBAMT(getFeptxn().getFeptxnTxAmt());
					moGeneralForReq.getRequest().setFACTNO(getFeptxn().getFeptxnTrinActno().substring(2));
					moGeneralForReq.getRequest().setFFG("1");
					moGeneralForReq.getRequest().setBXAMT(BigDecimal.valueOf(0));
					moGeneralForReq.getRequest().setPBAMT(BigDecimal.valueOf(0));
					moGeneralForReq.getRequest().setPACTNO("0");
					moGeneralForReq.getRequest().setPFG("0");
				}

				moGeneralForReq.getRequest().setBXAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
				moGeneralForReq.getRequest().setPBAMT(new BigDecimal(getFeptxn().getFeptxnTxAmt().toString()));
				if (getFeptxn().getFeptxnTrinActno().length() > 15) {
					moGeneralForReq.getRequest().setPACTNO(getFeptxn().getFeptxnTrinActno().substring(2, 16));
				}
				moGeneralForReq.getRequest().setREMARK(getFeptxn().getFeptxnTrinActno());
				moGeneralForReq.getRequest().setRemarkM(getFeptxn().getFeptxnTroutActno());
				moGeneralForReq.getRequest().setSECNO(unisysSecno);
				moGeneralForReq.getRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());

				if ("1".equals(moGeneralForReq.getRequest().getMODE())) {
					moGeneralForReq.getRequest().setTRDATE("0");
					moGeneralForReq.getRequest().setTRBRNO("0");
					moGeneralForReq.getRequest().setTRWSNO("0");
					moGeneralForReq.getRequest().setTRTXTNO("0");
				} else if (Integer.parseInt(moGeneralForReq.getRequest().getMODE()) > 1) {
					moGeneralForReq.getRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
					// BugReport(001B0534):2010-05-25 by kyo for coding error TRMNO可能為空 需要額外判斷避開
					if (StringUtils.isNotBlank(trmno) && trmno.length() >= 5) {
						moGeneralForReq.getRequest().setTRBRNO(trmno.substring(0, 3));
						moGeneralForReq.getRequest().setTRWSNO(trmno.substring(3, 5));
					} else {
						moGeneralForReq.getRequest().setTRBRNO(getFeptxn().getFeptxnAtmno().substring(0, 3));
						moGeneralForReq.getRequest().setTRWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
					}
					// BugReport(001B0257):送主機的交易傳輸編號只需要5位
					moGeneralForReq.getRequest().setTRTXTNO(txtno);
				}
				moGeneralForReq.getRequest()
						.setATMID(StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(0, 3), 4, '0') + StringUtils.leftPad(getFeptxn().getFeptxnAtmno().substring(3, 5), 4, '0'));
				// .LOCBRNO = ATMMSTR.ATM_BRNO_ST
				moGeneralForReq.getRequest().setLOCBRNO(getFeptxn().getFeptxnAtmBrno());
				moGeneralForReq.getRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.MOP.name())).getCurcdCurBsp());

				if ("J".equals(moCBSTxid.substring(0, 1))) {
					reqClass.set(new MOJ10060Request());
					resClass.set(new MOJ10060Response());
				} else {
					reqClass.set(new MOJ10060Request());
					resClass.set(new MOK10060Response());
				}

				break;
			case G08002: // IFE
				/// * CBS_TITA HEADER */
				moGeneralForReq.getRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5)); // 交易傳輸編號
				moGeneralForReq.getRequest().setTTSKID("GA"); // 端末TASK ID
				moGeneralForReq.getRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 3)); // 交易代號
				moGeneralForReq.getRequest().setPTYPE("0"); // 處理型態
				moGeneralForReq.getRequest().setDSCPT1(moCBSTxid.substring(3)); // 摘要前三碼
				moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					moGeneralForReq.getRequest().setYCODE("0"); // "1"補送記號
				} else {
					moGeneralForReq.getRequest().setYCODE("0");
				}
				defZone = getZoneByZoneCode(getFeptxn().getFeptxnZoneCode());
				if (defZone == null) {
					return IOReturnCode.ZONENotFound;
				} else {
//					moGeneralForReq.getRequest().setACTNO(defZone.getZoneVirtualBrno() + getFeptxn().getFeptxnTroutActno().substring(5, 16));
				}
				moGeneralForReq.getRequest().setTXTYPE("00"); // 帳務別
				moGeneralForReq.getRequest().setCRDB("0"); // 借貸別
				moGeneralForReq.getRequest().setSPCD(getCurrencyByAlpha3(getFeptxn().getFeptxnTxCurAct()).getCurcdCurBsp()); // 主管記號
				moGeneralForReq.getRequest().setNBCD("1"); // 無摺記號
				moGeneralForReq.getRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				moGeneralForReq.getRequest().setTRNMOD("0"); // 訓練／代登記號
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir()) && String.valueOf(CurrencyType.HKD.name()).equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(CurrencyType.MOP.toString()).getCurcdCurBsp());
				} else {
					moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(getFeptxn().getFeptxnTxCur()).getCurcdCurBsp()); // 取得交易幣別
				}
				moGeneralForReq.getRequest().setTXAMT(BigDecimal.valueOf(0)); // 交易金額
				moGeneralForReq.getRequest().setFXFLAG("00");

				reqClass.set(new MOG08002Request());
				resClass.set(new MOG08002Response());

				// 207:/*海外分行在台灣跨行提款*/, 208:/*海外分行PLUS卡跨國提款*/, 209:/*海外分行PLUS卡跨行提款*/
				break;
			case J10207:
			case K10207:
			case J10208:
			case K10208:
			case J10209:
			case K10209:
			case J10205:
			case K10205:
				/// * CBS_TITA HEADER */
				moGeneralForReq.getRequest().setTRMNO(trmno); // 輸入行機台號
				// 交易傳輸編號
				if (txType == (byte) CBSTxType.Accounting.getValue()) {// 入扣帳
					moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnVirTxseq().substring(getFeptxn().getFeptxnVirTxseq().length() - 5));
				} else if (txType == (byte) CBSTxType.EC.getValue()) {
					moGeneralForReq.getRequest().setTXTNO(getFeptxn().getFeptxnConVirTxseq().substring(getFeptxn().getFeptxnConVirTxseq().length() - 5));
				}
				moGeneralForReq.getRequest().setTTSKID(moCBSTxid.substring(0, 1) + "U"); // 端末TASK ID
				moGeneralForReq.getRequest().setTRMTYP(unisysTrmType); // 櫃台機種類
				if (getFeptxn().getFeptxnTxnmode() < 2) {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "10"); // 交易代號日間
				} else {
					moGeneralForReq.getRequest().setTXCD(moCBSTxid.substring(0, 1) + "75"); // 交易代號夜間
				}
				moGeneralForReq.getRequest().setPTYPE("0"); // 處理型態
				moGeneralForReq.getRequest().setDSCPT1(moCBSTxid.substring(3)); // 摘要前三碼
				moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.Normal.getValue())); // 更正記號
				if (getFeptxn().getFeptxnTxnmode() == 2) {
					// 2010-12-15 by kyo for 永豐要求修正:當送TITA電文進海外主機(香港/澳門)扣帳時,IT-TITA-YCODE 欄位放 1時會造成超過限額還可以提款。因此修改為放0。
					moGeneralForReq.getRequest().setYCODE("0"); // "1"補送記號
				} else {
					moGeneralForReq.getRequest().setYCODE("0");
				}
				moGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 交易帳號
				moGeneralForReq.getRequest().setTXTYPE(String.valueOf(UnysisAccountType.M.getValue())); // 帳務別
				moGeneralForReq.getRequest().setCRDB("1"); // 借貸別
				moGeneralForReq.getRequest().setSPCD("00"); // 主管記號
				moGeneralForReq.getRequest().setNBCD("1"); // 無摺記號
				moGeneralForReq.getRequest().setTLRNO(unisysTlrNo); // 櫃員編號
				moGeneralForReq.getRequest().setTRNMOD("0"); // 訓練／代登記號
				if (String.valueOf(CurrencyType.HKD.name()).equals(getFeptxn().getFeptxnTxCurAct())) {
					moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(CurrencyType.HKD.name())).getCurcdCurBsp()); // 幣別
				} else {
					moGeneralForReq.getRequest().setCURCD(getCurrencyByAlpha3(String.valueOf(CurrencyType.MOP.name())).getCurcdCurBsp()); // 幣別
				}

				if (txType == (byte) CBSTxType.EC.getValue()) {
					// 2010-11-17 by kyo for spec update
					moGeneralForReq.getRequest().setHCODE(String.valueOf(HCODE.NonNormal.getValue())); // 更正記號
					moGeneralForReq.getRequest().setFXABRN(trmno.substring(0, 3));
					moGeneralForReq.getRequest().setFXWSNO(trmno.substring(3, 5));
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnVirTxseq().trim()) && getFeptxn().getFeptxnVirTxseq().length() > 5) {
						moGeneralForReq.getRequest().setFXTXTNO(getFeptxn().getFeptxnVirTxseq().substring(getFeptxn().getFeptxnVirTxseq().length() - 5)); // 原交易傳輸編號
					}
				}
				moGeneralForReq.getRequest().setFXFLAG("00");

				/// * CBS_TITA DETAIL */
				/// * 10/20 修改為卡片所在地區營業日 by Connie*/
				moGeneralForReq.getRequest().setBDTXD(getFeptxn().getFeptxnTbsdyAct());
				moGeneralForReq.getRequest().setSTANBKNO(getFeptxn().getFeptxnBkno());
				moGeneralForReq.getRequest().setSTANSSN(getFeptxn().getFeptxnStan());
				moGeneralForReq.getRequest().setSECNO(unisysSecno);
				moGeneralForReq.getRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());
				moGeneralForReq.getRequest().setASeacur(getFeptxn().getFeptxnTxCur()); /// * 取款時當地幣別 */
				moGeneralForReq.getRequest().setASeaamt(getFeptxn().getFeptxnTxAmt());
				moGeneralForReq.getRequest().setTRDATE(getFeptxn().getFeptxnTxDate());
				moGeneralForReq.getRequest().setTRBRNO(trmno.substring(0, 3));
				moGeneralForReq.getRequest().setTRWSNO(trmno.substring(3, 5));
				moGeneralForReq.getRequest().setTRTXTNO(txtno); // 已經在前面取後五位
				moGeneralForReq.getRequest().setATMID(getFeptxn().getFeptxnAtmno());
				moGeneralForReq.getRequest().setBASCUR(getCurrencyByAlpha3(String.valueOf(CurrencyType.MOP.name())).getCurcdCurBsp()); /// * 帳務行基礎幣別 */

				switch (moGeneralForReq.getRequest().getDSCPT1()) {
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
							/// *港幣帳戶需將手續費金額由葡幣轉為港幣*/
							if (String.valueOf(CurrencyType.HKD.name()).equals(getFeptxn().getFeptxnTxCurAct())) {
								moGeneralForReq.getRequest().setABcuspay(getFeptxn().getFeptxnFeeCustpayAct()); /// *基礎幣扣帳手續費*/
								moGeneralForReq.getRequest().setABcusprat(BigDecimal.valueOf(ATMPConfig.getInstance().getHKDMOPRate())); /// *港幣對葡幣的比值*/
								if (ATMPConfig.getInstance().getHKDMOPRate() != 0) {
									getFeptxn().setFeptxnFeeCustpayAct(MathUtil.roundUp(getFeptxn().getFeptxnFeeCustpayAct().doubleValue() / ATMPConfig.getInstance().getHKDMOPRate(), 2));
								}
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
						moGeneralForReq.getRequest().setTXAMT(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() + getFeptxn().getFeptxnFeeCustpayAct().intValue()));
						moGeneralForReq.getRequest().setBXAMT(moGeneralForReq.getRequest().getTXAMT());
						moGeneralForReq.getRequest().setBORATE(getFeptxn().getFeptxnExrate());
						moGeneralForReq.getRequest().setCUSPAY(getFeptxn().getFeptxnFeeCustpayAct());

						if ("J".equals(moCBSTxid.substring(0, 1))) {
							reqClass.set(new MOJ10207Request());
							resClass.set(new MOJ10207Response());
						} else {
							reqClass.set(new MOJ10207Request());
							resClass.set(new MOK10207Response());
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
						/// *港幣帳戶需將手續費金額由葡幣轉為港幣*/
						if (CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct())) {
							moGeneralForReq.getRequest().setABcuspay(getFeptxn().getFeptxnFeeCustpayAct()); /// *基礎幣扣帳手續費*/
							moGeneralForReq.getRequest().setABcusprat(BigDecimal.valueOf(ATMPConfig.getInstance().getHKDMOPRate())); /// *港幣對葡幣的比值*/
							if (ATMPConfig.getInstance().getHKDMOPRate() != 0) {
								getFeptxn().setFeptxnFeeCustpayAct(MathUtil.roundUp(getFeptxn().getFeptxnFeeCustpayAct().doubleValue() / ATMPConfig.getInstance().getHKDMOPRate(), 2));
							}
						}
						// 2010-11-19 by kyo for Connie update spec:/*TXAMT 為 HEADER 交易金額*/
						if (txType == (byte) CBSTxType.Accounting.getValue()) {
							moGeneralForReq.getRequest().setTXAMT(BigDecimal.valueOf(0));
						} else {
							moGeneralForReq.getRequest().setTXAMT(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() + getFeptxn().getFeptxnFeeCustpayAct().intValue()));
						}
						moGeneralForReq.getRequest().setBXAMT(moGeneralForReq.getRequest().getTXAMT());
						moGeneralForReq.getRequest().setBORATE(BigDecimal.valueOf(0));
						moGeneralForReq.getRequest().setCUSPAY(getFeptxn().getFeptxnFeeCustpayAct());
						moGeneralForReq.getRequest().setACQCNTRY2(getFeptxn().getFeptxnRemark()); // 由於搬離businessbase，借用feptxn_remark
						// .ACQCNTRY2 = IntlTxn.INTLTXN_ACQ_CNTRY.Substring(1, 2)
						moGeneralForReq.getRequest().setAPluscur(getCurrencyByAlpha3(String.valueOf(CurrencyType.USD.name())).getCurcdCurBsp());
						moGeneralForReq.getRequest().setAPlusamt(getFeptxn().getFeptxnTxAmtSet());

						if ("J".equals(moCBSTxid.substring(0, 1))) {
							reqClass.set(new MOJ10208Request());
							resClass.set(new MOJ10208Response());
						} else {
							reqClass.set(new MOJ10208Request());
							resClass.set(new MOK10208Response());
						}

						break;
					case "209": /// *本地他行 ATM 提款, 不收手續費*/
						/// *TXAMT 為 HEADER 交易金額*/
						moGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt());
						moGeneralForReq.getRequest().setBXAMT(moGeneralForReq.getRequest().getTXAMT());
						moGeneralForReq.getRequest().setCUSPAY(BigDecimal.valueOf(0));
						moGeneralForReq.getRequest().setACQCNTRY2(getFeptxn().getFeptxnRemark()); // 由於搬離businessbase，借用feptxn_remark
						// .ACQCNTRY2 = IntlTxn.INTLTXN_ACQ_CNTRY.Substring(1, 2)
						moGeneralForReq.getRequest().setAPluscur(getCurrencyByAlpha3(String.valueOf(CurrencyType.USD.name())).getCurcdCurBsp());
						moGeneralForReq.getRequest().setAPlusamt(getFeptxn().getFeptxnTxAmtSet());

						if ("J".equals(moCBSTxid.substring(0, 1))) {
							reqClass.set(new MOJ10209Request());
							resClass.set(new MOJ10209Response());
						} else {
							reqClass.set(new MOJ10209Request());
							resClass.set(new MOK10209Response());
						}
						break;
					case "205": /// *本地他行 ATM 提款(不同幣別), 不收手續費*/
						// 2010-11-19 by kyo for Connie update spec:/*TXAMT 為 HEADER 交易金額*/
						if (txType == CBSTxType.Accounting.getValue()) {
							moGeneralForReq.getRequest().setTXAMT(BigDecimal.valueOf(0));
						} else {
							moGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmtAct());
						}
						moGeneralForReq.getRequest().setBXAMT(moGeneralForReq.getRequest().getTXAMT());
						moGeneralForReq.getRequest().setCUSPAY(BigDecimal.valueOf(0));
						// 2011-03-15 by kyo :修正漏改的部分
						moGeneralForReq.getRequest().setACQCNTRY2(getFeptxn().getFeptxnRemark()); // 由於搬離businessbase，借用feptxn_remark
						// .ACQCNTRY2 = IntlTxn.INTLTXN_ACQ_CNTRY.Substring(1, 2)
						moGeneralForReq.getRequest().setAPluscur(getCurrencyByAlpha3(String.valueOf(CurrencyType.USD.name())).getCurcdCurBsp()); /// *美金*/
						moGeneralForReq.getRequest().setAPlusamt(getFeptxn().getFeptxnTxAmtSet());

						if ("J".equals(moCBSTxid.substring(0, 1))) {
							reqClass.set(new MOJ10205Request());
							resClass.set(new MOJ10205Response());
						} else {
							reqClass.set(new MOK10205Request());
							resClass.set(new MOK10205Response());
						}
						break;
				}
				break;
		}
		return rtnCode;
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
	 *        <modifier>榮升</modifier>
	 *        <reason></reason>
	 *        <date>2013/06/19</date>
	 *        </modify>
	 *        </history>
	 */
	private void genT24TITA(T24PreClass t24ReqMessage, int wEJ, String CBSTxid) throws Exception {
		t24ReqMessage.getTITAHeader().setTiChnnCodeS(getFeptxn().getFeptxnChannel());// 交易來源系統代號
		t24ReqMessage.getTITAHeader().setTiChnnCode("FEP");// 處理系統代號
		if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
			t24ReqMessage.getTITAHeader().setTRMNO(getFeptxn().getFeptxnAtmno());
		} else {
			t24ReqMessage.getTITAHeader().setTRMNO(getFeptxn().getFeptxnAtmnoVir());
		}
		t24ReqMessage.getTITAHeader().setEJFNO(getFeptxn().getFeptxnTxDate() + StringUtils.leftPad(String.valueOf(wEJ), 12, '0'));// FEP電子日誌序號
		// 2014/01/01 Modify by Ruling 應永豐要求國際卡提款(A1020/A140)增加國際清算日
		// FEP營業日期
		t24ReqMessage.getTITAHeader().setFiscDate(getFeptxn().getFeptxnTbsdyFisc());
		// If CBSTxid = T24Version.A1020 OrElse CBSTxid = T24Version.A1470 Then
		// .FISC_DATE = getFeptxn().FEPTXN_TBSDY_INTL
		// Else
		// .FISC_DATE = getFeptxn().FEPTXN_TBSDY_FISC
		// End If
		t24ReqMessage.getTITAHeader().setRegFlag(StringUtils.EMPTY);// 預約記號
		t24ReqMessage.getTITAHeader().setFepUserId(StringUtils.EMPTY);// FEP使用者代號
		t24ReqMessage.getTITAHeader().setFepPassword(StringUtils.EMPTY);// FEP使用者密碼

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
			case T24Version.A1030: // 澳門卡跨區提款(IFW)
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
		// 澳門現金帳之帳號=MOP1000299990060
		if (getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
			// 2013/12/11 Modify by Ruling for 貸方帳號放交易幣別
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCur().trim() + getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13) + ATMPConfig.getInstance().getMODept());
		} else {
			titaA0000.setCreditAcctNo(StringUtils.EMPTY);
		}
		getFeptxn().setFeptxnCbsIntActno(titaA0000.getCreditAcctNo());

		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());// 帳戶交易金額
		// 2013/12/11 Modify by Ruling for 貸方幣別放交易幣別/貸方金額放交易金額
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur());// 提領幣別
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());// 提領金額
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());// 交易匯率
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());// 卡片主帳號
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
		// 取得跨行手續費優惠
		// CheckCardStatus 已將 CARD_FREE_TW 借欄存入 FEPTXN
		// 利用General class將值正確填入
		switch (getFeptxn().getFeptxnBoxCnt()) { // for台灣地區提款
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

		// 港幣帳戶需將手續費金額由葡幣轉為港幣
		if ("HKD".equals(getFeptxn().getFeptxnTxCurAct()) && getFeptxn().getFeptxnFeeCustpayAct().intValue() > 0) {
			// 以系統代碼(3)及變數名稱(HKDMOPRate)為KEY, 讀取系統參數檔(SYSCONF), 取得變數值(SYSCONF_VALUE)
			if (ATMPConfig.getInstance().getHKDMOPRate() != 0) {
				// 不要用Round避免Banker's Rounding造成四捨六入、奇入偶不入
				getFeptxn().setFeptxnFeeCustpayAct(
						new BigDecimal(FormatUtil.doubleFormat(getFeptxn().getFeptxnFeeCustpayAct().doubleValue() / ATMPConfig.getInstance().getHKDMOPRate(), ".00")));
			} else {
				getLogContext().setRemark("ATMPConfig.Instance.HKDMOPRate = 0");
				this.logMessage(getLogContext());
			}
		}

		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());// 帳號
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct());// 帳戶交易金額
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtAct());// 提領金額
		getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());// 財金序號
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());// 卡片主帳號
		// 澳門過渡現金帳之帳號=MOP1251000010060
		// 貸方帳號
		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurAct() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getMODept());
		} else {
			titaA0000.setCreditAcctNo(StringUtils.EMPTY);
		}
		// 手續費幣別
		titaA0000.setChgCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		// 澳門手續費帳號=PL451608210
		titaA0000.setChgAccount(ATMPConfig.getInstance().getMOPFeeAccount());// 手續費帳號
		// ChenLi, 2013/10/28, A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號
		getFeptxn().setFeptxnCbsFeeActno(titaA0000.getChgAccount());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());

		// 存褶欄位顯示行庫全名及簡稱
		defALLBANK.setAllbankBkno(getFeptxn().getFeptxnBkno());
		defALLBANK.setAllbankBrno("000");
		Allbank allbank = allbankMapper.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
		if(allbank != null){
			defALLBANK = allbank;
		}
		// 2014/01/08 Modify by Ruling for 調整存摺備註(T.PSB.REM.S.D)為提領幣別+提領金額
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		// titaA0000.T_PSB_REM_S_D = "ATM CASH"
		titaA0000.setTPsbRemFD(defALLBANK.getAllbankFullname());
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
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());// 借方帳號
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct());// 幣別
		titaA0000.setDebitAmount(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() - getFeptxn().getFeptxnFeeCustpayAct().intValue()));// 帳戶交易金額
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());// 提領幣別
		titaA0000.setCreditAmount(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().intValue() - getFeptxn().getFeptxnFeeCustpayAct().intValue()));// 提領金額
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate());
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct());
		titaA0000.setIcActno(getFeptxn().getFeptxnMajorActno());

		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurAct() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 7) + "2"
					+ getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(8, 13) + ATMPConfig.getInstance().getMODept());
		} else {
			titaA0000.setCreditAcctNo(StringUtils.EMPTY);
		}
		getFeptxn().setFeptxnCbsSupActno(titaA0000.getCreditAcctNo()); // 貸方帳號

		titaA0000.setChgCurrency(getFeptxn().getFeptxnTxCurAct());
		titaA0000.setChgAccount(ATMPConfig.getInstance().getHKDFeeAccount());
		// ChenLi, 2013/10/28, A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號
		getFeptxn().setFeptxnCbsFeeActno(titaA0000.getChgAccount());
		titaA0000.setTPsbMemoD("境外提款");
		titaA0000.setTPsbRemSD("ATM CASH");
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
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
			titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTxCur() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 7) + "2"
					+ getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(8, 13) + ATMPConfig.getInstance().getMODept());
		} else {
			titaA0000.setDebitAcctNo(StringUtils.EMPTY);
		}

		titaA0000.setTPsbRemSD("ATM CASH");
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnAtmBrno());
		titaA0000.setTPsbMemoD("ATM 現金");
		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCur()); // 提領幣別
		titaA0000.setDebitAcctNo(String.valueOf(getFeptxn().getFeptxnTxAmt()));
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCur()); // 提領幣別
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmt());

		if (getGeneralData().getMsgCtl().getMsgctlCbsIntActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCur() + getGeneralData().getMsgCtl().getMsgctlCbsIntActno().substring(3, 13) + ATMPConfig.getInstance().getMODept());
		} else {
			titaA0000.setCreditAcctNo(StringUtils.EMPTY);
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
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurAct() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getMODept());
		} else {
			titaA0000.setCreditAcctNo(StringUtils.EMPTY);
		}

		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno());
		titaA0000.setDebitCurrency(CurrencyType.USD.toString());
		titaA0000.setDebitAmount(getFeptxn().getFeptxnFeeCustpayAct());

		// 清算幣別為美金
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurAct());
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

		// 取得國際提款手續費優惠
		// CheckCardStatus 已將 CARD_FREE_C1 借欄存入 FEPTXN
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
		// 國際提款交易提領港幣或葡幣, 不收手續費
		if ("209".equals(getFeptxn().getFeptxnCbsDscpt())) {
			// 本地他行 ATM 國際提款(209), 不收手續費
			// Fly 3/21 修改 for MO NCB(209)交易 匯率=交易金額/清算金額
			getFeptxn().setFeptxnTxAmtAct(getFeptxn().getFeptxnTxAmt());
			getFeptxn().setFeptxnExrate(MathUtil.roundUp(getFeptxn().getFeptxnTxAmt().doubleValue() / getFeptxn().getFeptxnTxAmtSet().doubleValue(), 5));

			getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
		} else if ("205".equals(getFeptxn().getFeptxnCbsDscpt())) {
			if ("HKD".equals(getFeptxn().getFeptxnTxCur()) || "MOP".equals(getFeptxn().getFeptxnTxCur())) {
				// 本地他行 ATM 國際提款(205/209), 不收手續費
				getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
			} else {
				// 本地他行提領非港幣/葡幣
				// 判斷澳門本地他行跨國提款(2410)提領外幣收費記號
				if (CMNConfig.getInstance().getMO205ChargeFG().equalsIgnoreCase("N")) {
					getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
				}
			}

		}
		// Fly 3/21 修改 for MO 208交易當地提領手續費未顯示
		getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnFeeCustpayAct());
		// 港幣帳戶需將手續費金額由葡幣轉為港幣
		if ("HKD".equals(getFeptxn().getFeptxnTxCurAct()) && getFeptxn().getFeptxnFeeCustpayAct().intValue() > 0) {
			// 以系統代碼(3)及變數名稱(HKDMOPRate)為KEY, 讀取系統參數檔(SYSCONF), 取得變數值(SYSCONF_VALUE)
			if (ATMPConfig.getInstance().getHKDMOPRate() != 0) {
				// 不要用Round避免Banker's Rounding造成四捨六入、奇入偶不入
				getFeptxn().setFeptxnFeeCustpayAct(
						new BigDecimal(FormatUtil.doubleFormat(getFeptxn().getFeptxnFeeCustpayAct().doubleValue() / ATMPConfig.getInstance().getHKDMOPRate(), ".00")));

			} else {
				getLogContext().setRemark("ATMPConfig.Instance.HKDMOPRate = 0");
				this.logMessage(getLogContext());
			}
		}

		titaA0000.setDebitCurrency(getFeptxn().getFeptxnTxCurAct()); // 借方幣別
		titaA0000.setDebitAcctNo(getFeptxn().getFeptxnTroutActno()); // 借方帳號
		titaA0000.setDebitAmount(getFeptxn().getFeptxnTxAmtAct()); // 帳戶交易金額
		titaA0000.setCreditCurrency(getFeptxn().getFeptxnTxCurSet()); // 貸方幣別 清算幣別為美金
		titaA0000.setCreditAmount(getFeptxn().getFeptxnTxAmtSet()); // 提領金額 財金清算金額
		titaA0000.setChgCurrency(getFeptxn().getFeptxnTxCurAct()); // 手續費幣別
		// 2014/02/21 Modify by Ruling for 配合永豐修改，澳門國際提款手續費帳號改為PL56042
		titaA0000.setChgAccount(ATMPConfig.getInstance().getMO2410FeeAccount());
		// titaA0000.CHG_ACCOUNT = ATMPConfig.Instance.MOPFeeAccount
		// 2013/10/28 Modify by ChenLi for A1010/A1030/A1020 FEPTXN_CBS_FEE_ACTNO寫入手續費帳號
		getFeptxn().setFeptxnCbsFeeActno(titaA0000.getChgAccount());
		titaA0000.setExchRate(getFeptxn().getFeptxnExrate()); // 交易匯率
		titaA0000.setAccrChgAmt(getFeptxn().getFeptxnFeeCustpayAct()); // 手續費金額
		titaA0000.setFiscBknostan(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan()); // 財金序號
		// 澳門過渡現金帳之帳號=USD1241000010060
		// 貸方帳號
		if (getGeneralData().getMsgCtl().getMsgctlCbsSupActno().length() >= 13) {
			titaA0000.setCreditAcctNo(getFeptxn().getFeptxnTxCurSet() + getGeneralData().getMsgCtl().getMsgctlCbsSupActno().substring(3, 13) + ATMPConfig.getInstance().getMODept());
		} else {
			titaA0000.setCreditAcctNo(StringUtils.EMPTY);
		}

		titaA0000.setTPsbMemoD(getGeneralData().getMsgCtl().getMsgctlCbsMemoDr());
		// 2014/01/08 Modify by Ruling for 調整存摺備註(T.PSB.REM.S.D)為提領幣別+提領金額
		titaA0000.setTPsbRemSD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString());
		// titaA0000.T_PSB_REM_S_D = "ATM CASH"
		titaA0000.setTPsbRemFD(getFeptxn().getFeptxnTxCur() + getFeptxn().getFeptxnTxAmt().toString()); // FEPTXN_TX_AMT取整數
		// 2013/12/05 Modify by Ruling for 澳門地區依主帳戶幣別，放不同國際卡限額記號(永豐要求修改)
		// 2014/03/21 Fly 永豐要求修改, 澳門國際卡限額記號與台灣相同, 放 NW
		titaA0000.setTRegTfrType("NW");

		// 2013/12/16 Modify by Ruling 應永豐要求國際卡提款(A1020/A140)增加國際清算日
		titaA0000.setPlusDate(getFeptxn().getFeptxnTbsdyIntl());

		addGeneralField(titaA0000);

		// 利用General class的GenDictionary建立T24的titaBody
		titaA0000.genDictionary(titaBody);

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
		if (StringUtils.isNotBlank(titaA0000.getDebitAcctNo()) && "00".equals(titaA0000.getDebitAcctNo().substring(0, 2))) {
			titaA0000.setDebitAcctNo(titaA0000.getDebitAcctNo().substring(2, 16));
		}

		if (StringUtils.isNotBlank(titaA0000.getCreditAcctNo()) && "00".equals(titaA0000.getCreditAcctNo().substring(0, 2))) {
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
			titaA0000.setDebitCurrency(CurrencyType.MOP.name());
		}

		if (StringUtils.isBlank(titaA0000.getCreditCurrency())) {
			titaA0000.setCreditCurrency(CurrencyType.MOP.name());
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
			defCBSPEND.setCbspendZone(ZoneCode.MAC);
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
					if (!getFeptxn().getFeptxnCbsTxCode().equals(T24Version.A1020) && cbs_TOTA.containsKey("CAP.CHG.AMT")) {// 這邊和SPEC不同，多了CAP.CHG.AMT存在與否的檢查
						// 非國際卡交易
						getFeptxn().setFeptxnFeeCustpayAct(new BigDecimal(cbs_TOTA.get("CAP.CHG.AMT"))); // 帳戶幣別手續費
					}

					// 國際卡餘額查詢(2411)原存行手續費不能蓋掉
					if (CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCur()) && !getFeptxn().getFeptxnCbsTxCode().equals(T24Version.A1470)
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
					if (cbs_TOTA.containsKey("DR.CO.CODE") && cbs_TOTA.get("DR.CO.CODE").length() >= 9) {
						getFeptxn().setFeptxnVirBrno(cbs_TOTA.get("DR.CO.CODE").trim().substring(6, 9)); // 借方開戶行
					}

					if (cbs_TOTA.containsKey("CR.CO.CODE") && cbs_TOTA.get("CR.CO.CODE").length() >= 9) {
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
					// FEPTXN_NPS_CLR 手續費清算(NPS) <> FeptxnNPSCLR.TRIn 轉入負擔手續費
					// 自行轉出
					getFeptxn().setFeptxnTxBrno(getFeptxn().getFeptxnBrno());
					getFeptxn().setFeptxnTxDept(getFeptxn().getFeptxnDept());
					getFeptxn().setFeptxnTxActno(getFeptxn().getFeptxnTroutActno());
				} else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
					// 自行轉入
					getFeptxn().setFeptxnTxBrno(getFeptxn().getFeptxnTrinBrno());
					getFeptxn().setFeptxnTxDept(getFeptxn().getFeptxnTrinDept());
					getFeptxn().setFeptxnTxActno(getFeptxn().getFeptxnTrinActno());
				}

				// 澳門地區自行換日
				if (cbs_TOTA.containsKey("BOOKING.DATE")) {// 先檢查有沒有 BOOKING.DATE
					// (跨區提款(台幣) andalso > 本行營業日) orelse (跨區提款(台幣) andalso > 卡片所在地區營業日)
					if (((!CBSTxid.equals(T24Version.A1030) || !CBSTxid.equals(T24Version.A1010)) && (cbs_TOTA.get("BOOKING.DATE").compareTo(getFeptxn().getFeptxnTbsdy()) > 0))
							|| ((CBSTxid.equals(T24Version.A1030) || CBSTxid.equals(T24Version.A1010)) && (cbs_TOTA.get("BOOKING.DATE").compareTo(getFeptxn().getFeptxnTbsdyAct()) > 0))) {
						// 澳門卡至台灣/香港跨區交易
						if (CBSTxid.equals(T24Version.A1030) || CBSTxid.equals(T24Version.A1010)) {
							getFeptxn().setFeptxnTbsdyAct(cbs_TOTA.get("BOOKING.DATE")); // 卡片所在地區營業日
						} else {
							getFeptxn().setFeptxnTbsdy(cbs_TOTA.get("BOOKING.DATE"));
						}
						defZONE = getZoneByZoneCode(ATMZone.MAC.toString());
						if (cbs_TOTA.get("BOOKING.DATE").compareTo(defZONE.getZoneTbsdy()) > 0) {
							defZONE.setZoneLlbsdy(defZONE.getZoneLbsdy()); // 上營業日搬入上上營業日
							defZONE.setZoneLbsdy(defZONE.getZoneTbsdy()); // 本營業日搬入上營業日
							defZONE.setZoneTbsdy(cbs_TOTA.get("BOOKING.DATE")); // 自行本營業日
							// 取得次營業日
							defBSDAYS.setBsdaysZoneCode(ATMZone.MAC.toString());
							defBSDAYS.setBsdaysDate(defZONE.getZoneNbsdy());
							Bsdays bsdays = bsdaysMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
							if (bsdays != null) {
								defZONE.setZoneNbsdy(bsdays.getBsdaysNbsdy());
								defBSDAYS = bsdays;
							}
							// 取得本營業日之星期幾
							defBSDAYS.setBsdaysZoneCode(ATMZone.MAC.name());
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
							getLogContext().setMessageParm13("澳門自行");
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
				if (CBSTxid.equals(T24Version.B0001)) {// 帳戶餘額查詢(IIQ/IQ2)
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
	 * 處理MO主機回應的電文
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
	 *         <reason>寫入FEPTXN_CON_TXSEQ需補滿七位</reason>
	 *         <date>2010/6/21</date>
	 *         </modify>2010-06-22 by kyo for 修改程式與SPEC一致
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>修改程式與SPEC一致</reason>
	 *         <date>2010/6/22</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>改為跟SPEC一致使用港澳電文判斷</reason>
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
	private FEPReturnCode processCBSMOResponse(UnisysTXCD unysisTxcd, byte txType, FEPReturnCode rtnCode, MOGeneral moGeneralForReq, MOGeneral moGeneralForRes, MOUnisysTextBase reqClass,
			MOUnisysTextBase resClass, UnisysAdapter adapter, String tita) throws Exception {
		@SuppressWarnings("unused")
		Cbspend defCBSPEND = new Cbspend();
		// 改為使用GetCurrencyByAlpha3取得幣別
		String hkd = getCurrencyByAlpha3(CurrencyType.HKD.toString()).getCurcdCurBsp();
		String mop = getCurrencyByAlpha3(CurrencyType.MOP.toString()).getCurcdCurBsp();
		String[] actnos = { StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY };

		if (rtnCode == CommonReturnCode.Normal) {
			String tota = StringUtils.EMPTY;

			tota = adapter.getMessageFromUnisys();
			if (tota.length() > unisysTotaLengthError) {

				try {
					moGeneralForRes = resClass.parseFlatfile(tota.substring(24));
				} catch (Exception e) {
					e.printStackTrace();
				}

				// If Integer.Parse(moGeneralForReq.Request.TXTNO) <> Integer.Parse(moGeneralForRes.Response.TXTNO) Then
				// Return CommonReturnCode.CBSTXSeqNoNotMatch '收到訊息交易序號不符
				// End If
				switch (unysisTxcd) {
					case J00001:
					case K00001:

						if (moGeneralForRes.getResponse().getACTNO1().length() > 13) {
							actnos[0] = moGeneralForRes.getResponse().getACTNO1().substring(0, 13);
						}
						if (moGeneralForRes.getResponse().getACTNO2().length() > 13) {
							actnos[1] = moGeneralForRes.getResponse().getACTNO2().substring(0, 13);
						}
						if (moGeneralForRes.getResponse().getACTNO3().length() > 13) {
							actnos[2] = moGeneralForRes.getResponse().getACTNO3().substring(0, 13);
						}
						if (moGeneralForRes.getResponse().getACTNO4().length() > 13) {
							actnos[3] = moGeneralForRes.getResponse().getACTNO4().substring(0, 13);
						}
						if (moGeneralForRes.getResponse().getACTNO5().length() > 13) {
							actnos[4] = moGeneralForRes.getResponse().getACTNO5().substring(0, 13);
						}
						if (moGeneralForRes.getResponse().getACTNO6().length() > 13) {
							actnos[5] = moGeneralForRes.getResponse().getACTNO6().substring(0, 13);
						}
						// 回傳6組資料, 逐一檢核
						String actno = StringUtils.EMPTY;
						if (hkd.equals(moGeneralForRes.getResponse().getCURCD1())) {
							actno = getFeptxn().getFeptxnTroutActno().substring(2, 8) + "03" + getFeptxn().getFeptxnTroutActno().substring(10, 15);
						} else {
							actno = getFeptxn().getFeptxnTroutActno().substring(2, 8) + "30" + getFeptxn().getFeptxnTroutActno().substring(10, 15);
						}

						if (actnos[0].equals(actno)) {
							if ((hkd.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct()))
									|| (mop.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCurAct()))) {
								//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getAMT1());
								getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getPRIBAL1());
							}
						} else if (actnos[1].equals(actno)) {
							if ((hkd.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct()))
									|| (mop.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCurAct()))) {
								//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getAMT2());
								getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getPRIBAL2());
							}
						} else if (actnos[2].equals(actno)) {
							if ((hkd.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct()))
									|| (mop.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCurAct()))) {
								//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getAMT3());
								getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getPRIBAL3());
							}
						} else if (actnos[3].equals(actno)) {
							if ((hkd.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct()))
									|| (mop.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCurAct()))) {
								//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getAMT4());
								getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getPRIBAL4());
							}
						} else if (actnos[4].equals(actno)) {
							if ((hkd.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct()))
									|| (mop.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCurAct()))) {
								//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getAMT5());
								getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getPRIBAL5());
							}
						} else if (actnos[5].equals(actno)) {
							if ((hkd.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct()))
									|| (mop.equals(moGeneralForRes.getResponse().getCURCD1()) && CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCurAct()))) {
								//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getAMT6());
								getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getPRIBAL6());
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
						if (unisysErrorRspMsgId1.equals(moGeneralForRes.getResponse().getMSGID()) || unisysErrorRspMsgId2.equals(moGeneralForRes.getResponse().getMSGID())
								|| unisysErrorRspMsgId3.equals(moGeneralForRes.getResponse().getMSGID())) {
							// 2010-11-24 by kyo for 修正與SPEC不符段落
							resClass = new MOErrResponse();
							try {
								moGeneralForRes = resClass.parseFlatfile(tota.substring(24));
							} catch (Exception e) {
								e.printStackTrace();
							}
							//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getBAL());
						} else {
							// 正常回應
							//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getHWBAL());
							getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getAVBAL());

						}
						break;
					case J10207:
					case K10207:
					case J10208:
					case K10208:
					case J10209:
					case K10209:
					case J10205:
					case K10205: // 提款及轉帳
						if (!moGeneralForRes.getResponse().getMSGID().substring(0, 1).equals(unisysRspErrorFlag)) {
							//getFeptxn().setFeptxnBala(moGeneralForRes.getResponse().getHWBAL());
							getFeptxn().setFeptxnBalb(moGeneralForRes.getResponse().getAVBAL());
							// 2010-11-19 by kyo for Connie update spec.
							if ("205".equals(moGeneralForReq.getRequest().getDSCPT1()) || "208".equals(moGeneralForReq.getRequest().getDSCPT1())) {
								/// *因下行電文之實際扣款金額不含手續費,故不需扣除*/
								getFeptxn().setFeptxnTxAmtAct(moGeneralForRes.getResponse().getDPAMT()); // - getFeptxn().FEPTXN_FEE_CUSTPAY_ACT
								getFeptxn().setFeptxnExrate(moGeneralForRes.getResponse().getDBFXRATE());
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
						getFeptxn().setFeptxnExrate(moGeneralForRes.getResponse().getCSRATE1());
						getFeptxn().setFeptxnScash(moGeneralForRes.getResponse().getSCASH1());
						getFeptxn().setFeptxnAcrate(moGeneralForRes.getResponse().getACRAT1());
						getFeptxn().setFeptxnDifrate(moGeneralForRes.getResponse().getDISRAT1());
						getFeptxn().setFeptxnTxAmtAct(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmt().intValue() * getFeptxn().getFeptxnExrate().intValue()));
						break;
				}
				// 使用列舉
				if (txType == CBSTxType.Accounting.getValue()) {
					// 2010-06-24 by kyo for 改為跟SPEC一致使用港澳電文
					if (unysisTxcd == UnisysTXCD.J00001 || unysisTxcd == UnisysTXCD.K00001 || unysisTxcd == UnisysTXCD.G08002) {
						getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_INQ_Response);
					} else {
						getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Response);
					}
				} else if (txType == CBSTxType.EC.getValue()) {
					getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_EC_Response);
				}

				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false));
				getFeptxn().setFeptxnCbsRc(moGeneralForRes.getResponse().getMSGID());

				// 當主機回hex值為"00000000"時轉成ascii會等於" "，但新版codeGen會Trim掉空白，因此新增判斷長度
				if (moGeneralForRes.getResponse().getMSGID().length() >= 4 && moGeneralForRes.getResponse().getMSGID().substring(0, 1).equals(unisysRspErrorFlag)) {
					// 2010-04-12 modified by kyo for destinationChannel 需要用adata.txchannel
					// 2010-04-19 modified by Jim for RC處理修改
					if (txType == CBSTxType.Accounting.getValue()) {// 記帳
						// 2010-09-27 by kyo for spec modify:/*9/21 Update By Connie*/
						if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) {
							/// *ATM發動交易需將 CBS RC 轉換為 ATM RC*/
//							getFeptxn().setFeptxnReplyCode(
//									TxHelper.getRCFromErrorCode(moGeneralForRes.getResponse().getMSGID(), FEPChannel.UATMP, getGeneralData().getTxChannel(), getGeneralData().getLogContext())); // FEPReturnCode
																																																// =
																																																// CBS_TOTA.MSGID
						} else {
							/// *FISC發動交易需將 CBS RC 轉換為 FISC RC*/
//							getFeptxn().setFeptxnRepRc(
//									TxHelper.getRCFromErrorCode(moGeneralForRes.getResponse().getMSGID(), FEPChannel.UATMP, getGeneralData().getTxChannel(), getGeneralData().getLogContext())); // FEPReturnCode
																																																// =
																																																// CBS_TOTA.MSGID
						}
					}
					rtnCode = CommonReturnCode.CBSResponseError;
				}
			} else {
				rtnCode = CommonReturnCode.CBSResponseFormatError;
			}

		} else {// timeout
//			getFeptxn().setFeptxnCbsRc(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.UATMP, getGeneralData().getTxChannel(), getGeneralData().getLogContext())); // TxHelper.getRCFromErrorCode(rtnCode,
																																																// FEPChannel.UATMP)
																																																// '"0601"
																																																// '交易忙碌
			// 2010-06-22 by kyo for 修改程式與SPEC一致
			// 206跨區交易，033同區交易
			if ("206".equals(unysisTxcd.toString().substring(3, 6)) || "033".equals(unysisTxcd.toString().substring(3, 6)) || DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
				getFeptxn().setFeptxnAccType((short) AccountingType.UnKnow.getValue());
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

}
