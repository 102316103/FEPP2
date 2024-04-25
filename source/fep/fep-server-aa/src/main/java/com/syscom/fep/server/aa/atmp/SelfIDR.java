package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.AtmiacMapper;
import com.syscom.fep.mybatis.model.Atmiac;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.HK;
import com.syscom.fep.server.common.business.host.MO;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.CBSHostType;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.T24Version;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.ATMCTxType;
import com.syscom.fep.vo.enums.ATMCashTxType;
import com.syscom.fep.vo.enums.ATMReturnCode;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.ATMZone;
import com.syscom.fep.vo.enums.CBSTxType;
import com.syscom.fep.vo.enums.CardKind;
import com.syscom.fep.vo.enums.CreditTxType;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.T24TxType;

/**
 * @author Richard
 */
public class SelfIDR extends ATMPAABase {
	private ATMTXCD txCode; // ATM交易代號
	private FEPReturnCode rtnCode = CommonReturnCode.Normal;
	private boolean needUpdateFEPTXN = false;
	private boolean needSendToHost = true;
	private String rtnMessage = "";
	private String wStaff = "";
	private static final String CurcdFlag = "Y003";
	private static final String SetAccount1 = "002";
	private static final String SetAccount2 = "008";
	private static final String CardSelfFlag = "Y001";
	private static final String BINTW = "601551";

	public SelfIDR(ATMData txnData) throws Exception {
		super(txnData);
		//--ben-20220922-//txCode = ATMTXCD.parse(getATMRequest().getTXCD());
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.準備FEP交易記錄檔
			rtnCode = getATMBusiness().prepareFEPTXN();

			// 2013/01/16 Modify by Ruling for 拉霸
			// If getATMBusiness().getFeptxn().FEPTXN_TX_CODE = ATMTXCD.BAR.ToString Then
			// rtnCode = getATMBusiness().PrepareBARTXN()
			// End If

			// 2.新增FEP交易記錄檔
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = getATMBusiness().addTXData();
			}
			// 2013/01/16 Modify by Ruling for 拉霸
			// If rtnCode = CommonReturnCode.Normal Then rtnCode =
			// getATMBusiness().AddTXDataAndBARTXData()

			// 3.商業邏輯檢核
			if (rtnCode == CommonReturnCode.Normal) {
				needUpdateFEPTXN = true;
				rtnCode = checkBusinessRule();
				// If rtnCode = ENCReturnCode.ENCLibError Then needResponseMsg = False
			}

			// 4.帳務主機處理
			if (rtnCode == CommonReturnCode.Normal && needSendToHost) {
				rtnCode = sendToHost();
				// If rtnCode = CommonReturnCode.HostResponseTimeout Then needResponseMsg =
				// False
			}

			// 5.更新FEP交易記錄檔
			updateFEPTxn();

			// 2020/07/24 Modify by Ruling for 三萬元轉帳主動通知
			// 6.發送簡訊及EMAIL
			sendToMailSMS();

		} catch (Exception ex) {
			// 2010-04-23 modified by kyo for 防止程式中發生例外但沒CATCH到時，會回ATM正常的這種異常情形
			rtnCode = CommonReturnCode.ProgramException;
			// 2011-07-05 by kyo for exception不需要call GetRCFromErrorCode，避免送兩次EMS
			// getATMBusiness().getFeptxn().FEPTXN_REPLY_CODE =
			// TxHelper.GetRCFromErrorCode(CInt(rtnCode).ToString, FEPChannel.FEP,
			// TxData.TxChannel, TxData.LogContext)
			getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			logContext.setProgramException(ex);
			sendEMS(logContext);
		} finally {
			// 6.組回應電文(當海外主機有給TOTA或DES發生例外或主機逾時不組回應)
			if (StringUtils.isBlank(getTxData().getTxResponseMessage())) {
				rtnMessage = prepareATMResponseData();
			} else {
				rtnMessage = getTxData().getTxResponseMessage();
			}
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		}
		return rtnMessage;
	}

	/**
	 * 3. 商業邏輯檢核
	 * 
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode checkBusinessRule() throws Exception {
		// 3.1 CheckHeader
		rtnCode = getATMBusiness().checkHeader();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 3.2 CheckBody
		rtnCode = getATMBusiness().checkBody();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 3.3 更新ATM狀態
		rtnCode = getATMBusiness().updateATMStatus();
		if (rtnCode != CommonReturnCode.Normal) {
			// 更新ATM狀態失敗,更新交易記錄
			return rtnCode;
		}

		// 3.4 若不為「無卡現金存款」檢核卡片狀況
		// modified by Maxine on 2011/11/01 for SPEC修改: 10/31 修改,INM不需檢核卡檔
		// 2012/08/10 Modify by Ruling for 企業入金，INI不需檢核卡檔
		// 2013/01/16 Modify by Ruling for 拉霸，BAR不需檢核卡檔
		// 2013/05/03 Modify by Ruling for ATM條碼
		// 2014/11/19 Modify by Ruling for ATM優惠Coupon兌換券統計
		// 2016/08/10 Modify by Ruling for ATM新功能：跨行存款(IDR)之轉出行=807需檢核卡檔
		if (ATMTXCD.IDR.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
			if (SysStatus.getPropertyValue().getSysstatHbkno()
					.equals(this.getATMBusiness().getFeptxn().getFeptxnTroutBkno())) {
				rtnCode = getATMBusiness().checkCardStatus();
				if (rtnCode != CommonReturnCode.Normal) {
					// 檢核卡片狀況失敗,更新交易記錄
					return rtnCode;
				}
			}
		} else {
			if (!ATMTXCD.INM.name().equals(getFeptxn().getFeptxnTxCode())
					&& !ATMTXCD.INI.name().equals(getFeptxn().getFeptxnTxCode())
					&& !ATMTXCD.BCD.name().equals(getFeptxn().getFeptxnTxCode())
					&& !ATMTXCD.CPN.toString().equals(getFeptxn().getFeptxnTxCode())) {
				rtnCode = getATMBusiness().checkCardStatus();
				if (rtnCode != CommonReturnCode.Normal) {
					// 檢核卡片狀況失敗,更新交易記錄
					return rtnCode;
				}
			}
		}

		/// * 7/1 新增 for SMS/ANB */
		if ((ATMTXCD.SMS.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
				|| ATMTXCD.ANB.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
			getATMBusiness().getFeptxn().setFeptxnIdno(
					getATMBusiness().getCard().getCardIdno1() + getATMBusiness().getCard().getCardIdno2()); /// *身份證號:統一編號+檢查碼*/
		}

		// 3.5 檢核海外卡自行交易
		// 2015/06/18 Modify by Ruling for 增加檢核海外卡自行交易
		if (!ZoneCode.TWN.equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())) {
			// 海外卡不能使用WEBATM做交易
			if (FEPChannel.WEBATM.name().equals(getATMBusiness().getFeptxn().getFeptxnChannel())) {
				rtnCode = FISCReturnCode.ReceiverBankServiceStop; // 0205 收信單位該項跨行業務停止或暫停營業
				logContext.setRemark("海外卡不能使用WEBATM做交易");
				logMessage(Level.INFO, logContext);
				return rtnCode;
			}
			// 海外卡於台灣地區只能提領台幣
			if (ZoneCode.TWN.equals(getATMBusiness().getFeptxn().getFeptxnAtmZone())
					&& ATMTXCD.IFW.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
					&& !CurrencyType.TWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCur())) {
				rtnCode = FISCReturnCode.ReceiverBankServiceStop; // 0205 收信單位該項跨行業務停止或暫停營業
				logContext.setRemark("海外卡於台灣地區只能提領台幣");
				logMessage(Level.INFO, logContext);
				return rtnCode;
			}
			// 2019/01/15 Modify by Ruling for 澳門卡PLUS BIN回收，拒絕IQ2/PNP交易
			// 澳門卡PLUS BIN回收，拒絕IQ2/PNP
			if (ZoneCode.MAC.equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())
					&& (ATMTXCD.IQ2.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
							|| ATMTXCD.PNP.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
					&& getATMBusiness().getFeptxn().getFeptxnTxDate()
							.compareTo(INBKConfig.getInstance().getMOBinStopDate()) >= 0) {
				rtnCode = FISCReturnCode.ReceiverBankServiceStop; // 0205 收信單位該項跨行業務停止或暫停營業
				logContext.setRemark(String.format("交易日大於等於澳門卡PLUS BIN回收日(%1$s)，拒絕IQ2/PNP交易",
						INBKConfig.getInstance().getMOBinStopDate()));
				logMessage(Level.INFO, logContext);
				return rtnCode;
			}
		}

		// 3.6 檢核本行卡提款單筆限額
		// 如為提款交易(IFW/IWD), 檢核單筆限額
		if (getTxData().getMsgCtl().getMsgctlCheckLimit() == 1) {
			rtnCode = getATMBusiness().checkTransLimit(getTxData().getMsgCtl());
			if (rtnCode != CommonReturnCode.Normal) {
				// 檢核卡片狀況失敗,更新交易記錄
				return rtnCode;
			}
		}

		// 2013/02/22 Modify by Ruling for 預約跨轉交易(IPA), 檢核單筆限額
		// 如為預約跨轉交易(IPA), 檢核單筆限額
		if (ATMTXCD.IPA.name().equals(getFeptxn().getFeptxnTxCode())
				&& DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
			rtnCode = getATMBusiness().checkTransLimit(getTxData().getMsgCtl());
			if (rtnCode != CommonReturnCode.Normal) {
				// 檢核卡片狀況失敗,更新交易記錄
				return rtnCode;
			}
		}

		// 2010-11-18 by kyo for spec :/* 11/17 修改 */
		// 2013/06/21 Modify by Ruling for 港澳NCB:改為轉出帳號錯誤(TranOutACTNOError)
		/// * COMBO現金儲值卡不得作預借現金/預現轉帳交易(B11) */
		if ((ATMTXCD.IWD.name().equals(getFeptxn().getFeptxnTxCode())
				|| ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode()))
				&& BINPROD.Gift.equals(getFeptxn().getFeptxnTroutKind())) {
			rtnCode = FEPReturnCode.TranOutACTNOError; // E625
			// rtnCode = FEPReturnCode.TranInACTNOError 'E074
			return rtnCode;
		}

		// 3.7 檢核本行卡Track2及密碼
		RefString returnData = new RefString("");
		ENCHelper encHelper = new ENCHelper(getATMBusiness().getFeptxn(), getTxData());
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlCheckfpb())) {
			switch (txCode) {
			case PNP:
				// 2020/10/21 Modify by Ruling for 香港分行臨櫃變更國際提款密碼
				if (ZoneCode.HKG.equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())
						&& StringUtils.isNotBlank(getATMBusiness().getCard().getCardAuth())) {
					//--ben-20220922-//rtnCode = encHelper.changeHKAuth(getATMRequest().getFPB2(), getATMRequest().getFpb2New(),
					//--ben-20220922-//		getATMBusiness().getCard().getCardAuth(), returnData);
				} else {
					//--ben-20220922-//rtnCode = encHelper.changePassword(getATMRequest().getFPB2(), getATMRequest().getFpb2New(),
					//--ben-20220922-//		getATMBusiness().getFeptxn().getFeptxnTrk2().substring(28, 32), returnData);
				}
				break;
			case PNB:
			case PN3:
				//--ben-20220922-//rtnCode = encHelper.changePassword(getATMRequest().getFPB(), getATMRequest().getFpbNew(),
				//--ben-20220922-//		getATMBusiness().getFeptxn().getFeptxnTrk3().substring(45, 49), returnData);
				// 2011-05-04 by kyo for 開卡流程新電文 /* 3/22 新增電文(PN0) */
				break;
			case PN0:
				//--ben-20220922-//rtnCode = encHelper.checkPassword(getATMRequest().getFPB(), "0000", returnData);
				if (rtnCode == FEPReturnCode.Normal) {
					rtnCode = FEPReturnCode.ENCCheckPasswordError;
					return rtnCode;
				} else /// *密碼檢核錯誤 */
				{
					if (StringUtils.isBlank(returnData.get())) {
						rtnCode = FEPReturnCode.ENCCheckPasswordError;
						return rtnCode;
					} else {
						getATMBusiness().getFeptxn().setFeptxnIncre(returnData.get());
						rtnCode = FEPReturnCode.Normal;
						/// * 變更密碼檢核 */
						//--ben-20220922-//rtnCode = encHelper.changePassword(getATMRequest().getFPB(), getATMRequest().getFpbNew(),
						//--ben-20220922-//		getATMBusiness().getFeptxn().getFeptxnIncre(), returnData);
					}
				}
				break;
			case IQ2:
				// 2020/10/21 Modify by Ruling for 香港分行臨櫃變更國際提款密碼
				if (ZoneCode.HKG.equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())
						&& StringUtils.isNotBlank(getATMBusiness().getCard().getCardAuth())) {
					//--ben-20220922-//rtnCode = encHelper.checkHKAuth(getATMRequest().getFPB(), getATMBusiness().getCard().getCardAuth());
				} else {
					//--ben-20220922-//rtnCode = encHelper.checkPassword(getATMRequest().getFPB(),
					//--ben-20220922-//		getATMBusiness().getFeptxn().getFeptxnTrk2().substring(28, 32), new RefString(""));
				}
				// 2010-07-29 by kyo for SPEC新增: 7/29 修改 for APP密碼檢核
				break;
			case APP:
				//--ben-20220922-//rtnCode = encHelper.checkPassword(getATMRequest().getFPB(),
				//--ben-20220922-//		getATMBusiness().getFeptxn().getFeptxnTrk3().substring(45, 49), new RefString(""));
				break;
			default:
				break;
			}

			// If TxCode = ATMTXCD.APP Then
			// getATMBusiness().getFeptxn().FEPTXN_TRK3.Substring(45, 4), "")
			// End If

			if (rtnCode != CommonReturnCode.Normal) {
				rtnCode = getATMBusiness().checkPWErrCnt(1); // 密碼可錯誤次數-1
			} else {
				// 2020/10/21 Modify by Ruling for 香港分行臨櫃變更國際提款密碼
				if (ZoneCode.HKG.equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())
						&& StringUtils.isNotBlank(getATMBusiness().getCard().getCardAuth())) {
					// 取得 Offset for 香港分行臨櫃變更國際提款密碼
					if (ATMTXCD.PNP.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
						getATMBusiness().getCard().setCardAuth(returnData.get());
					}
					rtnCode = getATMBusiness().checkPWErrCnt(2); // 回復密碼錯誤次數
				} else {
					// 取得 Offset Increment
					getATMBusiness().getFeptxn().setFeptxnIncre(returnData.get());
					rtnCode = getATMBusiness().checkPWErrCnt(2); // 回復密碼錯誤次數
				}
			}

			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

		}

		// 3.8 如為WEB ATM 只檢核 TAC
		if (FEPChannel.WEBATM.name().equals(getATMBusiness().getFeptxn().getFeptxnChannel())) {
			// 2017/05/04 Modify by Ruling for 配合二代晶片讀卡機調整WEBATM自行轉帳及繳費驗TAC功能
			if (ATMTXCD.EFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				getATMBusiness().getFeptxn().setFeptxnPcode(FISCPCode.PCode2563.getValueStr());
			} else if (ATMTXCD.IFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				getATMBusiness().getFeptxn().setFeptxnPcode(FISCPCode.PCode2523.getValueStr());
			}
			rtnCode = encHelper.checkATMTAC((int) getTxData().getMsgCtl().getMsgctlTacType());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			// 2017/03/25 Modify by Ruling for WebATM晶片卡繳信用卡款
			if (ATMTXCD.EFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
					&& !(BusinessBase.RRN30000Trans.equals(getATMBusiness().getFeptxn().getFeptxnFiscRrn())
							&& BusinessBase.PAYTYPE30000Trans
									.equals(getATMBusiness().getFeptxn().getFeptxnPaytype()))) {
				// 檢核是否為晶片卡繳費
				// 2018/06/27 Modify by Ruling for
				// 自行、代理行邏輯一致，將代理行(FISC)程式移至BusinessBase，統一呼叫CheckBPUNIT
				rtnCode = getATMBusiness().checkBPUNIT();
				// rtnCode = getATMBusiness().CheckPayBillWebATM()
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}

				if ("03".equals(getATMBusiness().getFeptxn().getFeptxnPbtype())) {
					// WEBATM電文未帶銷帳編號，將轉入帳號填入
					if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnReconSeqno())) {
						getATMBusiness().getFeptxn()
								.setFeptxnReconSeqno(getATMBusiness().getFeptxn().getFeptxnTrinActno());
					}

					// 檢核是否為繳本行信用卡費
					rtnCode = getATMBusiness().checkEFTCCard(getATMBusiness().getFeptxn().getFeptxnReconSeqno());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}
			}

		} else {
			// 如為晶片卡交易檢核MAC及TAC
			// 2013/07/24 Modify by Ruling for 修正ATM電文的MAC為空白時不Call DES
			if (StringUtils.isNotBlank(getTxData().getMsgCtl().getMsgctlReqmacType().toString())) {
				//--ben-20220922-//if (StringUtils.isBlank(getATMRequest().getMAC())) {
				//--ben-20220922-//	getLogContext().setRemark("CheckBusinessRule-ATM電文的MAC為空白");
				//--ben-20220922-//	logMessage(Level.INFO, getLogContext());
				//--ben-20220922-//	return ENCReturnCode.ENCCheckMACError;
				//--ben-20220922-//}

				/// * ATM電文MAC有值，檢核ATM電文TAC */
				if (StringUtils.isNotBlank(getTxData().getMsgCtl().getMsgctlTacType().toString())) {
					//--ben-20220922-//rtnCode = encHelper.checkATMMACAndTAC(getATMRequest().getMAC(),
					//--ben-20220922-//		(int) getTxData().getMsgCtl().getMsgctlTacType());
				} else {
					/// * 不檢核TAC，但仍需檢核MAC */
					//--ben-20220922-//rtnCode = encHelper.checkATMMAC(getATMRequest().getMAC());
				}

				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}
		}

		// 3.9 如為晶片卡調匯率(IFE), 取得價系統匯率
		if (ATMTXCD.IFE.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
			Atmiac defATMIAC = new Atmiac();
			AtmiacMapper dbATMIAC = SpringBeanFactoryUtil.getBean(AtmiacMapper.class);
			// Tables.DBATMIAC dbATMIAC = new Tables.DBATMIAC(FEPConfig.DBName);
			if (ATMZone.TWN.name().equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())) {

				// getFeptxn() tempVar = getATMBusiness().getFeptxn();
				if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnAtmnoVir())) // 外幣
				{
					/// * 11/30 修改 - 取得調帳號結果 */
					defATMIAC.setAtmiacActno(getATMBusiness().getFeptxn().getFeptxnTroutActno());
					defATMIAC.setAtmiacCurcd(getATMBusiness().getFeptxn().getFeptxnTxCurAct());
					Atmiac atmiacQuery = dbATMIAC.selectByPrimaryKey(defATMIAC.getAtmiacActno(),
							defATMIAC.getAtmiacCurcd());
					if (dbATMIAC.selectByPrimaryKey(defATMIAC.getAtmiacActno(), defATMIAC.getAtmiacCurcd()) == null) {
						defATMIAC.setAtmiacActno(getATMBusiness().getFeptxn().getFeptxnTroutActno());
						defATMIAC.setAtmiacCurcd(getATMBusiness().getFeptxn().getFeptxnTxCur());
						atmiacQuery = dbATMIAC.selectByPrimaryKey(defATMIAC.getAtmiacActno(),
								defATMIAC.getAtmiacCurcd());
						if (dbATMIAC.selectByPrimaryKey(defATMIAC.getAtmiacActno(),
								defATMIAC.getAtmiacCurcd()) == null) {
							return ATMReturnCode.TranOutACTNOError; // /* 轉出帳號錯誤 */
						} else {
							/// * 原幣戶代入原幣幣別及手續費幣別 */
							getATMBusiness().getFeptxn().setFeptxnTxCurAct(atmiacQuery.getAtmiacCurcd());
							getATMBusiness().getFeptxn().setFeptxnFeeCur(atmiacQuery.getAtmiacCurcd());
						}
					}
					// dbATMIAC.Dispose();
					/// * 台幣戶提領外幣 */
					if (CurrencyType.TWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCurAct())) {
						if (DbHelper.toBoolean(atmiacQuery.getAtmiacRateDisc())) /// * 匯率優惠 */
						{
							wStaff = "1";
						} else {
							wStaff = "0";
						}
						// wStaff = CInt(defATMIAC.ATMIAC_RATE_DISC).ToString
						getATMBusiness().getFeptxn().setFeptxnFeeCustpayAct(new BigDecimal(0));
					} else {
						/// * 原幣戶提領原幣 */
						wStaff = "0";
						getATMBusiness().getFeptxn().setFeptxnTxAmtAct(new BigDecimal(0));
						if (DbHelper.toBoolean(atmiacQuery.getAtmiacFeeDisc())) {
							/// * 手續費減免 */
							getATMBusiness().getFeptxn().setFeptxnFeeCustpayAct(new BigDecimal(0));
						} else {
							/// * 計算原幣戶手續費 */
							rtnCode = getATMBusiness().getFee();
							if (rtnCode != FEPReturnCode.Normal) {
								return rtnCode;
							}
						}
					}
				} else {
					/// * 台灣卡至海外跨區提款 */
					wStaff = "0";
				}

				RefString mdFlag = new RefString("");
				/// * 11/30主帳戶與提領幣別相同, 調外幣對台幣匯率 */
				if (getATMBusiness().getFeptxn().getFeptxnTxCurAct()
						.equals(getATMBusiness().getFeptxn().getFeptxnTxCur())) {

					rtnCode = getATMBusiness().getExchangeRate(getATMBusiness().getFeptxn().getFeptxnZoneCode(),
							getATMBusiness().getFeptxn().getFeptxnTxCur(), CurrencyType.TWD.name(), wStaff,
							new RefString("0"), mdFlag);
				} else {
					rtnCode = getATMBusiness().getExchangeRate(getATMBusiness().getFeptxn().getFeptxnZoneCode(),
							getATMBusiness().getFeptxn().getFeptxnTxCur(),
							getATMBusiness().getFeptxn().getFeptxnTxCurAct(), wStaff, new RefString("0"), mdFlag);
				}

				// 2010-10-12 by kyo for spec update:/* 10/12 修正台灣卡海外跨區提款/外幣提款 */
				/// * 11/30 修正 */
				if (CurrencyType.TWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCurAct())) // 台幣戶
				{
					if (mdFlag.get().equals("0")) {
						/// *乘記號*/
						getATMBusiness().getFeptxn()
								.setFeptxnTxAmtAct(MathUtil
										.roundUp(getATMBusiness().getFeptxn().getFeptxnTxAmt()
												.multiply(getATMBusiness().getFeptxn().getFeptxnExrate()), 0)
										.add(getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct()));
					} else {
						/// *除記號*/
						if (getATMBusiness().getFeptxn().getFeptxnExrate().compareTo(new BigDecimal(0)) != 0) {
							getATMBusiness().getFeptxn()
									.setFeptxnTxAmtAct(MathUtil
											.roundUp(getATMBusiness().getFeptxn().getFeptxnTxAmt()
													.divide(getATMBusiness().getFeptxn().getFeptxnExrate()), 0)
											.add(getATMBusiness().getFeptxn().getFeptxnFeeCustpayAct()));
						}
					}
				}
				// GO TO 5 /* 更新交易記錄 */ 不走 4 上主機邏輯
				needSendToHost = false;
			} else {
				/// * 海外卡至台灣跨區提款 */
				// var tempVar2 = getATMBusiness().getFeptxn();
				if (CurrencyType.TWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCur())) {
					rtnCode = getATMBusiness().getExchangeRate(getATMBusiness().getFeptxn().getFeptxnZoneCode(),
							getATMBusiness().getFeptxn().getFeptxnTxCur(),
							getATMBusiness().getFeptxn().getFeptxnTxCurAct(), "0", new RefString("0"),
							new RefString(""));
					// GO TO 5 /* 更新交易記錄 */ 不走 4 上主機邏輯
					needSendToHost = false;
				}
			}
		}

		/// * 10/21 與永豐確認, 轉帳類交易檢核是否轉入GIFT卡 */
		// 3.10 轉帳類交易(IFT/IPA/EFT)檢核是否轉入GIFT卡
		if (ATMTXCD.IPA.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
				|| ATMTXCD.EFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
				|| ATMTXCD.IFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
			rtnCode = getATMBusiness().checkTrfGCard();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
		}

		// add by Maxine on 2011/08/22 for 8/17 修改, 檢核預約轉帳交易之預約日期
		if (ATMTXCD.IPA.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
			// 預約日期必須大於本行營業日
			if (getATMBusiness().getFeptxn().getFeptxnOrderDate()
					.compareTo(getATMBusiness().getFeptxn().getFeptxnTbsdy()) <= 0) {
				rtnCode = ATMReturnCode.InputDateError; // E798 輸入日期錯誤 */
				return rtnCode;
			}

			// 比對預約日期是否大於預約轉帳期限(6個月) */
			Calendar dateTmp = Calendar.getInstance();
			dateTmp.set(Integer.valueOf(getATMBusiness().getFeptxn().getFeptxnTbsdy().substring(0, 4)),
					Integer.valueOf(getATMBusiness().getFeptxn().getFeptxnTbsdy().substring(4, 6)),
					Integer.valueOf(getATMBusiness().getFeptxn().getFeptxnTbsdy().substring(6, 8)));
			// If getATMBusiness().getFeptxn().FEPTXN_ORDER_DATE > DateAdd(DateInterval.Month, 6,
			// dateTmp).ToString("yyyyMMdd") Then
			dateTmp.add(Calendar.MONTH, CMNConfig.getInstance().getIPADueDay());
			if (getATMBusiness().getFeptxn().getFeptxnOrderDate()
					.compareTo(FormatUtil.dateTimeFormat(dateTmp, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)) > 0) {
				rtnCode = ATMReturnCode.DueDateError; // E799 輸入日期不得大於六個月 */
				return rtnCode;
			}

		}

		/// * 1/19 Combob卡預借現金交易增加檢核BIN */
		// 3.11 COMBO卡預借現金交易檢核BIN
		if ((ATMTXCD.IWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
				|| ATMTXCD.IFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
				&& SysStatus.getPropertyValue().getSysstatHbkno()
						.equals(getATMBusiness().getFeptxn().getFeptxnTroutBkno())
				&& !"00".equals(getATMBusiness().getFeptxn().getFeptxnTroutActno().substring(0, 2))) {
			if (getTxData().getBin() == null) {
				rtnCode = FEPReturnCode.OtherCheckError;
				return rtnCode;
			}
		}

		/// * 6/20 修正 for 敲價系統回饋資料 */
		// 3.12 台幣戶外幣提款, 將交易身份別(客戶/行員)寫入FEPTXN
		if (ATMTXCD.IFW.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
				&& ATMZone.TWN.name().equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())
				&& StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnAtmnoVir())
				&& CurrencyType.TWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCurAct())) {
			// Tables.DefATMIAC defATMIAC = new Tables.DefATMIAC();
			// Tables.DBATMIAC dbATMIAC = new Tables.DBATMIAC(FEPConfig.DBName);
			Atmiac defATMIAC = new Atmiac();
			AtmiacMapper dbATMIAC = SpringBeanFactoryUtil.getBean(AtmiacMapper.class);
			defATMIAC.setAtmiacActno(getATMBusiness().getFeptxn().getFeptxnTroutActno());
			defATMIAC.setAtmiacCurcd(getATMBusiness().getFeptxn().getFeptxnTxCurAct());
			Atmiac defATMIACQuery = dbATMIAC.selectByPrimaryKey(defATMIAC.getAtmiacActno(), defATMIAC.getAtmiacCurcd());
			if (defATMIACQuery == null) {
				// 2011-08-04 by kyo for Sarah update spec:/* 8/4 修改 for 組存戶提外幣 */
				defATMIAC.setAtmiacActno(getATMBusiness().getFeptxn().getFeptxnTroutActno());
				defATMIAC.setAtmiacCurcd(getATMBusiness().getFeptxn().getFeptxnTxCur());
				defATMIACQuery = dbATMIAC.selectByPrimaryKey(defATMIAC.getAtmiacActno(), defATMIAC.getAtmiacCurcd());
				if (defATMIACQuery == null) {
					rtnCode = FEPReturnCode.TranOutACTNOError;
					return rtnCode;
				}
			}
			/// * 台幣戶提領外幣 */
			if (DbHelper.toBoolean(defATMIACQuery.getAtmiacRateDisc()) == false) {
				getATMBusiness().getFeptxn().setFeptxnApid("1"); /// *一般客戶 */
			} else {
				getATMBusiness().getFeptxn().setFeptxnApid("2"); /// * 行員 */
			}

		}

		return rtnCode;
	}

	/**
	 * 4. 送主機
	 * 
	 * @return
	 */
	private FEPReturnCode sendToHost() {
		Credit hostCredit = new Credit(getTxData());
		T24 hostT24 = new T24(getTxData());
		MO hostMo = new MO(getTxData());
		HK hostHk = new HK(getTxData());

		try {
			FEPReturnCode rtn = FEPReturnCode.Abnormal;
			String wTxid = "";
			if (ATMZone.HKG.name().equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())
					|| ATMZone.MAC.name().equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())) // 海外卡
			{

				if (ATMTXCD.PNP.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					return CommonReturnCode.Normal;
				}

				if (ATMTXCD.IFE.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					// 提領幣別為台幣, 送至 ATMP
					if (!CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
						// 2010-04-19 modify by kyo for SPEC修改
						// 主帳戶與提領幣別相同, 不須送至海外主機
						RefString exRate = new RefString("0");
						RefString mdFlag = new RefString("");
						String wStaff = "0";
						if (getATMBusiness().getFeptxn().getFeptxnTxCur()
								.equals(getATMBusiness().getFeptxn().getFeptxnTxCurAct())) {
							// 2010-07-02 by kyo for spec修改:澳門卡港幣戶跨區提港幣,須送海外主機
							if (!(ATMZone.MAC.name().equals(getATMBusiness().getFeptxn().getFeptxnZoneCode())
									&& CurrencyType.HKD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCurAct())
									&& StringUtils
											.isNotBlank(getATMBusiness().getFeptxn().getFeptxnAtmnoVir().trim()))) {
								// ATM不顯示調匯率畫面
								getATMBusiness().getFeptxn().setFeptxnAaRc(CommonReturnCode.Normal.getValue());
								getATMBusiness().getFeptxn().setFeptxnReplyCode(CurcdFlag); // 表正常
								getATMBusiness().getFeptxn().setFeptxnExrate(new BigDecimal(0));
								getATMBusiness().getFeptxn().setFeptxnScash(new BigDecimal(0));
								getATMBusiness().getFeptxn().setFeptxnAcrate(new BigDecimal(0));
								getATMBusiness().getFeptxn().setFeptxnDifrate(new BigDecimal(0));
								// BugReport(001B0433):同區提款才歸零手續費
								if (StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir().trim())) {
									getATMBusiness().getFeptxn().setFeptxnFeeCustpayAct(new BigDecimal(0));
									// 2010-06-29 by kyo for 跨區交易時FEPTXN_FEE_CUR已經有值，因此在同區交易值才需要搬移FEPTXN_TX_CUR
									getATMBusiness().getFeptxn()
											.setFeptxnFeeCur(getATMBusiness().getFeptxn().getFeptxnTxCur());
								}
								return rtnCode;
							} else {
								// Sepc modify:海外卡至敲價系統取匯率
								rtnCode = getATMBusiness().getExchangeRate(
										getATMBusiness().getFeptxn().getFeptxnZoneCode(),
										getATMBusiness().getFeptxn().getFeptxnTxCur(), CurrencyType.MOP.toString(),
										wStaff, exRate, mdFlag);
								return rtnCode;
							}
						} else {
							// Sepc modify:海外卡至敲價系統取匯率
							rtnCode = getATMBusiness().getExchangeRate(getATMBusiness().getFeptxn().getFeptxnZoneCode(),
									getATMBusiness().getFeptxn().getFeptxnTxCur(),
									getATMBusiness().getFeptxn().getFeptxnTxCurAct(), wStaff, exRate, mdFlag);
							return rtnCode;
						}
					}
				}

				// 2013/06/21 Modify by Ruling for 港澳NCB
				// wTxid = hostMo.GetTxid(getATMBusiness().getFeptxn(), getTxData().getMsgCtl())
				switch (ATMZone.valueOf(getATMBusiness().getFeptxn().getFeptxnZoneCode())) {
				case HKG:
// 2024-03-06 Richard modified for SYSSTATE 調整
//					if (CBSHostType.Unisys.equals(SysStatus.getPropertyValue().getSysstatCbsHkg())) {
//						// 送往香港優利主機
//						wTxid = hostHk.getTxid(getATMBusiness().getFeptxn(), getTxData().getMsgCtl());
//						rtnCode = hostHk.sendToCBSHK(wTxid, (byte) CBSTxType.Accounting.getValue());
//					} else {
						// 送往香港T24主機
						if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnAtmnoVir())) {
//							rtnCode = hostHk.sendToCBSHKT24(getTxData().getMsgCtl().getMsgctlHkt24txid(),
//									(byte) T24TxType.Accounting.getValue(), true);
						} else {
							// 跨區交易
//							rtnCode = hostHk.sendToCBSHKT24(getTxData().getMsgCtl().getMsgctlHkt24txid1(),
//									(byte) T24TxType.Accounting.getValue(), true);
						}
//					}
					break;
				case MAC:
// 2024-03-06 Richard modified for SYSSTATE 調整
//					if (CBSHostType.Unisys.equals(SysStatus.getPropertyValue().getSysstatCbsMac())) {
//						// 送往澳門優利主機
//						wTxid = hostMo.getTxid(getATMBusiness().getFeptxn(), getTxData().getMsgCtl());
//						rtnCode = hostMo.sendToCBSMO(wTxid, (byte) CBSTxType.Accounting.getValue());
//					} else {
						// 送往澳門T24主機
						if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnAtmnoVir())) {
//							rtnCode = hostMo.sendToCBSMOT24(getTxData().getMsgCtl().getMsgctlMot24txid(),
//									(byte) T24TxType.Accounting.getValue(), true);
						} else {
							// 跨區交易
//							rtnCode = hostMo.sendToCBSMOT24(getTxData().getMsgCtl().getMsgctlMot24txid1(),
//									(byte) T24TxType.Accounting.getValue(), true);
						}
//					}
					break;
				default:
					break;
				}

				if (rtnCode == CommonReturnCode.Normal) {
					// 寫入ATM清算資料
					if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
						rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
					}
					// 更新ATM鈔匣資料
					if (rtnCode == CommonReturnCode.Normal
							&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
						rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.Accounting.getValue());
					}
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}

					/// * 9/29 新增, 海外卡在台灣跨區提款, 上T24主機 */
					// 2013/06/21 Modify by Ruling for 港澳NCB
					if (StringUtils.isNotBlank(getATMBusiness().getFeptxn().getFeptxnAtmnoVir())
							&& ATMTXCD.IFW.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
						// If getATMBusiness().getFeptxn().FEPTXN_ATM_ZONE = ATMZone.TWN.ToString
						// AndAlso getATMBusiness().getFeptxn().FEPTXN_TX_CODE = ATMTXCD.IFW.ToString
						// Then
						// 台灣ATM代理跨區提款
						if (ATMZone.TWN.name().equals(getATMBusiness().getFeptxn().getFeptxnAtmZone())) {
							/// * 組送台灣T24-代理行現金(A1050)電文 */
							rtnCode = hostT24.sendToT24(T24Version.A1050, (byte) T24TxType.Accounting.getValue(), true);
						}

						// 香港ATM代理跨區提款
// 2024-03-06 Richard modified for SYSSTATE 調整
//						if (ATMZone.HKG.name().equals(getATMBusiness().getFeptxn().getFeptxnAtmZone())
//								&& CBSHostType.T24.equals(SysStatus.getPropertyValue().getSysstatCbsHkg())) {
//							/// * 組送香港T24-代理行現金(A1050)電文 */
//							rtnCode = hostHk.sendToCBSHKT24(T24Version.A1050, (byte) T24TxType.Accounting.getValue(),
//									true);
//						}

						// 澳門ATM代理跨區提款
// 2024-03-06 Richard modified for SYSSTATE 調整
//						if (ATMZone.MAC.name().equals(getATMBusiness().getFeptxn().getFeptxnAtmZone())
//								&& CBSHostType.T24.equals(SysStatus.getPropertyValue().getSysstatCbsMac())) {
//							/// * 組澳門港T24-代理行現金(A1050)電文 */
//							rtnCode = hostMo.sendToCBSMOT24(T24Version.A1050, (byte) T24TxType.Accounting.getValue(),
//									true);
//						}

						if (rtnCode != CommonReturnCode.Normal) {
							/// * T24 主機 Rep(-), 沖正ATMC */
							if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
								// 寫入ATM清算資料
								if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
									rtn = getATMBusiness().insertATMC((byte) ATMCTxType.EC.getValue());
								}
								// 更新ATM鈔匣資料
								if (rtn == CommonReturnCode.Normal
										&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
									rtn = getATMBusiness().updateATMCash((byte) ATMCashTxType.EC.getValue());
								}
								if (rtn != CommonReturnCode.Normal) {
									rtnCode = rtn;
									return rtnCode;
								}

								// 2013/06/21 Modify by Ruling for 港澳NCB
								/// * T24 主機 Rep(-), 送香港優利主機沖正 */
								// wTxid = hostMo.GetTxid(getATMBusiness().getFeptxn(), getTxData().getMsgCtl())
								switch (ATMZone.valueOf(getATMBusiness().getFeptxn().getFeptxnZoneCode())) {
								case HKG:
// 2024-03-06 Richard modified for SYSSTATE 調整
//									if (CBSHostType.Unisys.equals(SysStatus.getPropertyValue().getSysstatCbsHkg())) {
//										// 送往香港優利主機
//										wTxid = hostHk.getTxid(getATMBusiness().getFeptxn(), getTxData().getMsgCtl());
//										rtn = hostHk.sendToCBSHK(wTxid, (byte) CBSTxType.EC.getValue());
//									} else {
										// 送往香港T24主機
//										rtn = hostHk.sendToCBSHKT24(getTxData().getMsgCtl().getMsgctlHkt24txid1(),
//												(byte) T24TxType.EC.getValue(), true);
//									}
									if (rtn != CommonReturnCode.Normal) {
										rtnCode = rtn;
									}
									break;
								case MAC:
// 2024-03-06 Richard modified for SYSSTATE 調整
//									if (CBSHostType.Unisys.equals(SysStatus.getPropertyValue().getSysstatCbsMac())) {
//										// 送往澳門優利主機
//										wTxid = hostMo.getTxid(getATMBusiness().getFeptxn(), getTxData().getMsgCtl());
//										rtn = hostMo.sendToCBSMO(wTxid, (byte) CBSTxType.EC.getValue());
//									} else {
										// 送往澳門T24主機
//										rtn = hostMo.sendToCBSMOT24(getTxData().getMsgCtl().getMsgctlMot24txid1(),
//												(byte) T24TxType.EC.getValue(), true);
//									}
									if (rtn != CommonReturnCode.Normal) {
										rtnCode = rtn;
									}
									break;
								default:
									break;
								}

							}
						}
					}
				}

			} else // 台灣卡
			{
				// 錢卡提款送信用卡主機
				/// * 10/21 修改流程 */
				if (ATMTXCD.IFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					/// * 11/11修改, 晶片卡轉帳 */
					if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnTroutKind().trim())) {
						/// * 晶片卡轉帳加值 GIFT 卡 */
						if (CardKind.G.name().equals(getATMBusiness().getFeptxn().getFeptxnTrinKind())) {
							// 組信用卡電文GIFT卡額度查詢(B16-GFI)
							rtnCode = hostCredit.sendToCredit("B16", (byte) CreditTxType.Accounting.getValue());
						}
						if (rtnCode == CommonReturnCode.Normal) {
							/// * 組T24主機扣帳 */
							rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
									(byte) T24TxType.Accounting.getValue(), true);
							/// * 回應正常, 帳務處理 */
							if (rtnCode == CommonReturnCode.Normal) {
								// 寫入ATM清算資料
								if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
									rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
								}
								// 更新ATM鈔匣資料
								if (rtnCode == CommonReturnCode.Normal
										&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
									rtnCode = getATMBusiness()
											.updateATMCash((byte) ATMCashTxType.Accounting.getValue());
								}
								if (rtnCode != CommonReturnCode.Normal) {
									return rtnCode;
								}
								/// * 10/21 修改 */
								if ("G".equals(getATMBusiness().getFeptxn().getFeptxnTrinKind())) {
									/// * GIFT卡加值 (B20-ADD) */
									rtnCode = hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(),
											(byte) CreditTxType.Accounting.getValue());
									if (rtnCode != CommonReturnCode.Normal) {
										return rtnCode;
									}
								}
							}
						}
					} else {
						/// * 11/11 組T24主機電文, 驗證轉帳交易 */
						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(),
								(byte) T24TxType.Accounting.getValue(), false);
						if (rtnCode == CommonReturnCode.Normal) {
							// /* 組信用卡電文預借現金授權(B11) */
							rtnCode = hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid1(),
									(byte) CreditTxType.Accounting.getValue());
							if (rtnCode == CommonReturnCode.Normal) {
								rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(),
										(byte) T24TxType.Accounting.getValue(), true);
								if (rtnCode == CommonReturnCode.Normal) {
									// 寫入ATM清算資料
									if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
										rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
									}
									// 更新ATM鈔匣資料
									if (rtnCode == CommonReturnCode.Normal
											&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
										rtnCode = getATMBusiness()
												.updateATMCash((byte) ATMCashTxType.Accounting.getValue());
									}
									if (rtnCode != CommonReturnCode.Normal) {
										return rtnCode;
									}
								} else {
									/// * T24 Rep(-), 送信用卡沖正授權 */
									rtn = hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid1(),
											(byte) CreditTxType.EC.getValue());
								}
							}
						}
					}
					/// * 10/21 修改 */
				} else if ((ATMTXCD.EFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.IPA.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))) /// * 繳費/預約轉帳
																										/// */
				{
					/// * 組T24主機電文 */
					rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
							(byte) T24TxType.Accounting.getValue(), true);
					/// * 回應正常, 帳務處理 */
					if (rtnCode == CommonReturnCode.Normal) {
						// 寫入ATM清算資料
						if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
							rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
						}
						// 更新ATM鈔匣資料
						if (rtnCode == CommonReturnCode.Normal
								&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
							rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.Accounting.getValue());
						}
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						}
					}

				} else if (ATMTXCD.IWD.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					if (CardKind.M.name().equals(getATMBusiness().getFeptxn().getFeptxnTroutKind())) {
						// 組信用卡電文預借現金授權(B11)
						rtnCode = hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(),
								(byte) CreditTxType.Accounting.getValue());
						if (rtnCode == CommonReturnCode.Normal) {
							/// * 組 T24 電文-代理行現金 */
							rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(),
									(byte) T24TxType.Accounting.getValue(), true);
							if (rtnCode == CommonReturnCode.Normal) {
								// 寫入ATM清算資料
								if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
									rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
								}
								// 更新ATM鈔匣資料
								if (rtnCode == CommonReturnCode.Normal
										&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
									rtnCode = getATMBusiness()
											.updateATMCash((byte) ATMCashTxType.Accounting.getValue());
								}
								if (rtnCode != CommonReturnCode.Normal) {
									return rtnCode;
								}
							} else {
								/// * 11/5 T24主機Rep(-), 送信用卡沖正授權 */
								// 此沖正結果僅留存在ASC_RC，FEPReturncode不需要留存
								hostCredit.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(),
										(byte) CreditTxType.EC.getValue());
							}
						}
					} else {
						/// * 台幣提款 */
						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
								(byte) T24TxType.Accounting.getValue(), true);
						if (rtnCode == CommonReturnCode.Normal) {
							// 寫入ATM清算資料
							if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
								rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
							}
							// 更新ATM鈔匣資料
							if (rtnCode == CommonReturnCode.Normal
									&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
								rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.Accounting.getValue());
							}
							if (rtnCode != CommonReturnCode.Normal) {
								return rtnCode;
							}
						}
					}

				} else if (ATMTXCD.ICW.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					// 2013/02/01 Modify by Ruling for 新增硬幣機(提款)
					rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
							(byte) T24TxType.Accounting.getValue(), true);
					if (rtnCode == CommonReturnCode.Normal) {
						// 寫入ATM清算資料
						if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
							rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
						}
						// 更新ATM鈔匣資料
						if (rtnCode == CommonReturnCode.Normal
								&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
							rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.Accounting.getValue());
						}
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						}
					}

				} else if (ATMTXCD.SMS.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) /// * 7/1 新增 SMS
																										/// */
				{
					/// * 組 Web Serivce , 送至 NB */
					//--ben-20220922-//rtnCode = getATMBusiness().sendToNB(getATMRequest().getACTION());

				}
				// * 7/1 新增 ANB */
				else if (ATMTXCD.ANB.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) /// * 7/1 新增 ANB */
				{
					/// * 組 Web Serivce , 送至 NB */
					rtnCode = getATMBusiness().sendToNB("A");

				} else if (ATMTXCD.IFW.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					if (StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir())
							&& StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir().trim())) {
						/// * 12/22 修改, 將帳戶幣別代入 */
						//--ben-20220922-//getATMBusiness().getFeptxn().setFeptxnTxCurAct(
						//--ben-20220922-//		getATMBusiness().getCurrencyByBSP(getATMRequest().getBalCur()).getCurcdAlpha3());

						// 2011-08-04 by kyo for sarah update spec:/* 8/4 修改, 原幣戶外幣提款, 將手續費幣別代入 */
						/// * 組存帳號之科目別為002 或 008 */
						if ("002".equals(getFeptxn().getFeptxnTroutActno().substring(5, 8))
								|| "008".equals(getFeptxn().getFeptxnTroutActno().substring(5, 8))) {
							getFeptxn().setFeptxnFeeCur(getFeptxn().getFeptxnTxCur());
						}

						// 2012/10/18 Modify by Ruling for 人民幣提款
						if (CurrencyType.CNY.name().equals(getFeptxn().getFeptxnTxCur())) {
							/// IFW-人民幣提款交易-A1041 */
							rtnCode = hostT24.sendToT24(T24Version.A1041, (byte) T24TxType.Accounting.getValue(), true);
						} else {
							/// IFW-外幣提款(美金/港幣/日幣)交易-A1040 */
							rtnCode = hostT24.sendToT24(T24Version.A1040, (byte) T24TxType.Accounting.getValue(), true);
						}
					} else {
						/// * 台灣卡跨區提款(IFW)-A1030 */
						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(),
								(byte) T24TxType.Accounting.getValue(), true);
					}
					/// * 回應正常, 帳務處理 */
					if (rtnCode == CommonReturnCode.Normal) {
						// 寫入ATM清算資料
						if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
							rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
						}
						// 更新ATM鈔匣資料
						if (rtnCode == CommonReturnCode.Normal
								&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
							rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.Accounting.getValue());
						}
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						}
						// 2013/06/21 Modify by Ruling for 港澳NCB
						// 組香港 T24 電文-代理行現金(A1050)
// 2024-03-06 Richard modified for SYSSTATE 調整
//						if (CBSHostType.T24.equals(SysStatus.getPropertyValue().getSysstatCbsHkg())
//								&& ATMZone.HKG.name().equals(getATMBusiness().getFeptxn().getFeptxnAtmZone())) {
//							rtnCode = hostHk.sendToCBSHKT24(T24Version.A1050, (byte) T24TxType.Accounting.getValue(),
//									true);
//							if (rtnCode != CommonReturnCode.Normal) {
//								// T24 主機 Rep(-), 沖正ATMC
//								// 寫入ATM清算資料
//								if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
//									rtnCode = getATMBusiness().insertATMC(ATMCTxType.EC.getValue());
//								}
//								// 更新ATM鈔匣資料
//								if (rtnCode == CommonReturnCode.Normal
//										&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
//									rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.EC.getValue());
//								}
//								// 香港T24主機Rep(-), 送台灣主機沖正
//								// 台灣卡跨區提款–A1030
//								rtn = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(),
//										(byte) CBSTxType.EC.getValue(), true);
//								if (rtn != CommonReturnCode.Normal) {
//									rtnCode = rtn;
//								}
//							}
//						}
						// 組澳門 T24 電文-代理行現金(A1050)
// 2024-03-06 Richard modified for SYSSTATE 調整
//						if (CBSHostType.T24.equals(SysStatus.getPropertyValue().getSysstatCbsMac())
//								&& ATMZone.MAC.name().equals(getATMBusiness().getFeptxn().getFeptxnAtmZone())) {
//							rtnCode = hostMo.sendToCBSMOT24(T24Version.A1050, (byte) T24TxType.Accounting.getValue(),
//									true);
//							if (rtnCode != CommonReturnCode.Normal) {
//								// T24 主機 Rep(-), 沖正ATMC
//								// 寫入ATM清算資料
//								if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
//									rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.EC.getValue());
//								}
//								// 更新ATM鈔匣資料
//								if (rtnCode == CommonReturnCode.Normal
//										&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
//									rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.EC.getValue());
//								}
//								// 澳門T24主機Rep(-), 送台灣主機沖正
//								// 台灣卡跨區提款–A1030
//								rtn = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(),
//										(byte) CBSTxType.EC.getValue(), true);
//								if (rtn != CommonReturnCode.Normal) {
//									rtnCode = rtn;
//								}
//							}
//						}
					}
					// 2011-05-04 by kyo for 開卡流程新電文
				} else if ((ATMTXCD.PN3.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.PNB.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.APP.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.PN0.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.AP1.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))) {
					/// * 密碼變更 */
					// 不必上主機
				}
				//* 無卡存款語音驗密 */
				else if (ATMTXCD.G51.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) /// * 無卡存款語音驗密 */
				{
					/// * 9/30組 G5100 Web Serivce , 至 IVR 驗密 */
					rtnCode = getATMBusiness().sendToIVR();

				} else if ((ATMTXCD.CDR.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.IDR.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.CCR.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.ICR.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))) {
					// 2012/09/07 Modify by Ruling for 新增硬幣機的業務:CCR/ICR電文送主機
					// 2010-11-04 by kyo for spec update:/* 11/4 修改 */
					// 2012/02/20 Modify by Ruling for 轉入帳號前五碼=虛擬帳號(00598)，比照繳信用卡款
					if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnTrinKind().trim())
							&& !CMNConfig.getInstance().getVirtualActno()
									.equals(getATMBusiness().getFeptxn().getFeptxnTrinActno().substring(0, 5))) {
						/// * 一般帳號 */
						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
								(byte) T24TxType.Accounting.getValue(), false);
					} else {
						/// * 信用卡或GIFT卡 */
						rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid1(),
								(byte) T24TxType.Accounting.getValue(), false);
					}

				} else if (ATMTXCD.IIQ.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					/// * 12/1 修改 判斷外幣餘額查詢是否為組存帳號 */
					if (SetAccount1.equals(getATMBusiness().getFeptxn().getFeptxnTroutActno().substring(5, 8))
							|| SetAccount2.equals(getATMBusiness().getFeptxn().getFeptxnTroutActno().substring(5, 8))) {
						getATMBusiness().getFeptxn().setFeptxnTxCurAct("");
					}
					rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
							(byte) T24TxType.Accounting.getValue(), true);
					/// * 11/29 回應正常, 帳務處理 */
					if (rtnCode == CommonReturnCode.Normal) {
						// 2013/02/25 Modify by Ruling for 組存帳號之外幣餘額查詢
						// 組存帳號之科目別為002或008時FEPTXN_TX_CUR_ACT 改放T24主機帶回的幣別
						if (SetAccount1.equals(getATMBusiness().getFeptxn().getFeptxnTroutActno().substring(5, 8))
								|| SetAccount2
										.equals(getATMBusiness().getFeptxn().getFeptxnTroutActno().substring(5, 8))) {
							getATMBusiness().getFeptxn().setFeptxnTxCurAct(
									getTxData().getT24Response().getTotaEnquiryContents().get(0).get("CURRENCY"));
						}
						// 寫入ATM清算資料
						if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
							rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
						}
						// 更新ATM鈔匣資料
						if (rtnCode == CommonReturnCode.Normal
								&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
							rtnCode = getATMBusiness().updateATMCash((byte) ATMCashTxType.Accounting.getValue());
						}
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						}
					}

					// 2012/08/10 Modify by Ruling for 企業入金，存款固定放 A2910
				} else if (ATMTXCD.BDR.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
					rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
							(byte) T24TxType.Accounting.getValue(), false);

					// modified By Maxine on 2011/11/01 for SPEC修改: 10/31修改 for INM
				} else if ((ATMTXCD.IQ2.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.IAC.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))
						|| (ATMTXCD.INM.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode()))) {
					// Case ATMTXCD.IQ2.ToString, ATMTXCD.IAC.ToString
					/// * 10/15 修改 */
					rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
							(byte) T24TxType.Accounting.getValue(), true);
					/// * 11/29 回應正常, 帳務處理 */
					if (rtnCode == CommonReturnCode.Normal) {
						// 寫入ATM清算資料
						if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
							rtnCode = getATMBusiness().insertATMC((byte) ATMCTxType.Accounting.getValue());
						}
						// 更新ATM鈔匣資料
						if (rtnCode == CommonReturnCode.Normal
								&& DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
							rtnCode = getATMBusiness().updateATMCash(ATMCashTxType.Accounting.getValue());
						}
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						}
						// 2010-01-04 by kyo for spec update/* 1/4 修正, 調帳號(IAC)檢核限制主卡交易 */
						if (ATMTXCD.IAC.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
								&& "1".equals(getATMBusiness().getCard().getCardSelf())) {
							getATMBusiness().getFeptxn().setFeptxnReplyCode(CardSelfFlag);
							getATMBusiness().getFeptxn().setFeptxnAaRc(CommonReturnCode.Normal.getValue());
						}
						// 2012/09/24 Modify by Ruling for 剔除證券交割虛擬帳號(E.MMA.AC若有值就代表是證券戶)
						if (ATMTXCD.INM.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
								&& StringUtils.isNotBlank(getATMBusiness().getFeptxn().getFeptxnRemark())
								&& "2".equals(getATMBusiness().getFeptxn().getFeptxnRemark().substring(0, 1))) {
							if (StringUtils.isNotBlank(
									getTxData().getT24Response().getTotaEnquiryContents().get(0).get("E.MMA.AC"))) {
								rtnCode = ATMReturnCode.TranInACTNOError;
								return rtnCode;
							}
						}
					}
				} else {
					// 2012/10/12 Modify by Ruling for 修改顯示的內容
					getLogContext().setRemark(
							"SentToHost 台灣卡邏輯 " + getATMBusiness().getFeptxn().getFeptxnTxCode() + "交易電文不送T24");
					logMessage(Level.DEBUG, getLogContext());
				}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 5. 更新交易記錄(getFeptxn())
	 */
	private void updateFEPTxn() {
		getATMBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
		getATMBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());

		if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnReplyCode())) {
			// 2010-11-25 by kyo for normal不需Call Txhelper
			if (rtnCode == FEPReturnCode.Normal) {
				getATMBusiness().getFeptxn().setFeptxnReplyCode(NormalRC.ATM_OK);
			} else {
				// 2010-08-11 by kyo for 明祥通知若有修改程式GetRCFromErrorCoe要使用4個參數的版本
				getATMBusiness().getFeptxn().setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(rtnCode.name(),
						FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
			}
		}

		getATMBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /// * AA Complete*/
		// For報表, 寫入處理結果
		reportTXRUST();

		// APP，PN3，IFE，CPN的邏輯處理
		businessBeforeUpdateFEPTXN();

		// Modify by Kyo Lai on 2010-03-11 for updateTxData的rtnCode不可蓋掉之前的rtnCode
		if (needUpdateFEPTXN) {
			FEPReturnCode rtncode = CommonReturnCode.Normal;
			rtncode = getATMBusiness().updateTxData();
			if (rtncode != CommonReturnCode.Normal) {
				getATMBusiness().getFeptxn().setFeptxnReplyCode("L013"); // 回寫檔案(getFeptxn())發生錯誤
			}
		}
	}

	/**
	 * 發送簡訊及EMAIL
	 */
	private void sendToMailSMS() {
		try {
			if (ATMTXCD.IFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
					&& FeptxnTxrust.Successed.equals(getATMBusiness().getFeptxn().getFeptxnTxrust())
					&& "FT".equals(getATMBusiness().getFeptxn().getFeptxnRsCode())) {
				getATMBusiness().prepareATMFTEMAIL();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, "sendToMailSMS"));
			sendEMS(getLogContext());
		}
	}
	
	/**
	 * 6. 組ATM回應電文
	 * 組ATM回應電文,Response物件的值已經在AA中MapGeneralResponseFromGeneralRequest搬好Header的值了
	 * 這裏只要處理Response的body的欄位值即可
	 * @throws Exception 
	 */
	
	//ben20221118  
	private String prepareATMResponseData() {
		String atmResponseString = "";
		return "";
	}


	/**
	 * For報表, 寫入處理結果
	 */
	private void reportTXRUST() {
		// Modify by Kyo Lai on 2010-04-01 For報表, 寫入處理結果
		if (rtnCode == CommonReturnCode.Normal) {
			// 2010-06-02 by kyo for spec修改
			if (getATMBusiness().getFeptxn().getFeptxnWay() == 3) {
				getATMBusiness().getFeptxn().setFeptxnTxrust("B"); // 處理結果=Pending
			} else {
				getATMBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
			}
		} else {
			getATMBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
		}
	}

	/**
	 * 更新FEPTXN前的商業邏輯更新
	 */
	
	private void businessBeforeUpdateFEPTXN() {
		//ben20221118  
	}
	
	/**
	 * 組ATM回應電文(共同)
	 * 
	 * 回應電文共同的部分T
	 */
	private void loadCommonATMResponse() {
		//ben20221118  
	}
	
	/**
	 * 準備吐鈔相關回應電文
	 * 
	 * 
	 * IIQ,IFT,IFW 共同的部分
	 * 
	 */
	private void loadATMResponseForCash() {
		//--ben-20220922-//getATMResponse().setDSPCNT1(getATMRequest().getDSPCNT1());
		//--ben-20220922-//getATMResponse().setDSPCNT2(getATMRequest().getDSPCNT2());
		//--ben-20220922-//getATMResponse().setDSPCNT3(getATMRequest().getDSPCNT3());
		//--ben-20220922-//getATMResponse().setDSPCNT4(getATMRequest().getDSPCNT4());
		//--ben-20220922-//getATMResponse().setDSPCNT5(getATMRequest().getDSPCNT5());
		//--ben-20220922-//getATMResponse().setDSPCNT6(getATMRequest().getDSPCNT6());
		//--ben-20220922-//getATMResponse().setDSPCNT7(getATMRequest().getDSPCNT7());
		//--ben-20220922-//getATMResponse().setDSPCNT8(getATMRequest().getDSPCNT8());
		//--ben-20220922-//getATMResponse().setDSPCNT1T(getATMRequest().getDSPCNT1T());
		//--ben-20220922-//getATMResponse().setDSPCNT2T(getATMRequest().getDSPCNT2T());
		//--ben-20220922-//getATMResponse().setDSPCNT3T(getATMRequest().getDSPCNT3T());
		//--ben-20220922-//getATMResponse().setDSPCNT4T(getATMRequest().getDSPCNT4T());
	}
	
	/**
	 * 組IFE電文
	 * 
	 * 由於程式碼太長，因此分別出來，較好維護
	 */
	private void loadATMResponseForIFE() {
		//ben20221118  
	}

	
	/**
	 * 重組三軌資料
	 * 
	 * 
	 * PN0
	 * @throws Exception 
	 * 
	 */
	private void prepareTRK3() throws Exception {
		//ben20221118  
	}

	
	private void openICCard() throws Exception {
		//ben20221118  
	}

	
	/**
	 * 個資遮蔽
	 * 
	 * @param str  文字字串
	 * @param type 0:公司戶 1:個人戶
	 * @return 遮蔽後的文字字串 公司戶-->第二位用"○"遮蔽 個人戶-->中文字首後面全部以"○"遮蔽；英文第二位後面全部以"*"遮蔽
	 *         PS.英文戶名T24下來的逗號會轉成問號，所以要轉回逗號後在做遮蔽 <history> <modify>
	 *         <modifier>Ruling></modifier> <date>2014/04/25</date>
	 *         <reason>New</reason> </modify> </history>
	 */
	private String maskWord(String str, int type) {
		String chineseMask = "○"; // 中文遮蔽
		String engMask = "*"; // 英文遮蔽
		String engSplit = ""; // 英文分隔符號
		String maskName = "";
		StringBuilder sb = new StringBuilder();
		boolean isChinese = false;
		String[] tmp = null;
		byte[] engChar = null;

		try {
			if (StringUtils.isBlank(str.trim())) {
				return str;
			}

			if (type == 0) {
				// **********公司戶**********
				if (str.trim().length() > 1) {
					sb.append(str.substring(0, 1));
					for (int i = 1; i < str.trim().length(); i++) {
						if (i == 1) {
							sb.append(chineseMask);
						} else {
							sb.append(str.substring(i, i + 1));
						}
					}
					maskName = sb.toString();
				} else {
					maskName = str;
				}
			} else {
				// **********個人戶**********
				// 判斷中英文

				isChinese = (str.substring(0, 1).charAt(0)) > ((char) 128);
				if (isChinese) {
					// 中文字首後面全部遮蔽
					if (str.trim().length() > 0) {
						sb.append(str.substring(0, 1));
						for (int i = 1; i < str.length(); i++) {
							sb.append(chineseMask);
						}
						maskName = sb.toString();
					} else {
						maskName = str;
					}
				} else {
					// T24下來的逗號會轉成問號，所以要轉回逗號後在做遮蔽
					str = str.replace("?", ",");

					// 英文第二位後面全部遮蔽
					if (str.trim().length() > 1) {
						// 抓全部的分隔符號組成一個字串
						for (int i = 0; i < str.length(); i++) {
							engChar = ConvertUtil.toBytes(str.substring(i, i + 1), PolyfillUtil.toCharsetName("Unicode"));
							if (engChar[1] <= 0) {
								// 英文字1個Byte
								// 44(,) 45(-) 46(.) 32(空白)
								if (engChar[0] == 44 || engChar[0] == 45 || engChar[0] == 46 || engChar[0] == 32) {
									engSplit = StringUtils.join(engSplit,
											ConvertUtil.toString(engChar, PolyfillUtil.toCharsetName("Unicode")));
								}
							} else {
								// 中文字2個Byte
							}
						}
						// 將特殊分隔符號取代成@
						str = str.replace(" ", "@");
						str = str.replace("-", "@");
						str = str.replace(",", "@");
						str = str.replace(".", "@");
						// 以@分隔全部的字串
						tmp = str.split("[@]", -1);
						for (int i = 0; i < tmp.length; i++) {
							if (tmp[i].trim().length() > 2) {
								sb.append(tmp[i].substring(0, 2));
								int tempVar = tmp[i].length();
								for (int j = 2; j < tempVar; j++) {
									sb.append(engMask);
								}
								// 判斷已到字尾不需再加分隔符號
								if (i < tmp.length - 1) {
									sb.append(engSplit.substring(i, i + 1));
								}
							} else {
								sb.append(tmp[i]);
								// 判斷已到字尾不需再加分隔符號
								if (i < tmp.length - 1) {
									sb.append(engSplit.substring(i, i + 1));
								}
							}
						}
						maskName = sb.toString();
					} else {
						maskName = str;
					}
				}
			}

			return maskName;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			// 發生例外錯誤時回原傳進來的字串
			return str;
		}
	}
	
	/**
	 * 字串轉Byte
	 * 
	 * @param strfSource 字串
	 * @param intStart   從N Byte開始
	 * @param intLen     取N Byte長度
	 * @return Bytes
	 * 
	 *         <history> <modify> <modifier>Ruling></modifier>
	 *         <date>2014/05/12</date> <reason>New</reason> </modify> </history>
	 */
	private byte[] big5BytesParse(String strfSource, int intStart, int intLen) {
		byte[] byteAry = ConvertUtil.toBytes(strfSource, PolyfillUtil.toCharsetName("big5"));

		byte[] newAry = new byte[intLen];
		int i = 0;
		int j = 0;

		while (j < intLen) {
			newAry[i] = byteAry[intStart];
			i = i + 1;
			j = j + 1;
			intStart = intStart + 1;
		}
		return newAry;
	}
	
	/**
	 * 將 T24 傳回轉出帳號資料逐一儲存
	 * 
	 * 將 T24 傳回轉出帳號資料逐一儲存
	 */
	private void processGroupSaves(int i) {
		Atmiac defATMIAC = new Atmiac();
		AtmiacMapper dbATMIAC = SpringBeanFactoryUtil.getBean(AtmiacMapper.class);
		boolean typej = false;
		try {
			if (getTxData().getT24Response().getTotaEnquiryContents().get(i).get("TYPEJ").equals("0")) {
				/// * 非組存 */
				defATMIAC.setAtmiacActno(getTxData().getT24Response().getTotaEnquiryContents().get(i).get("ACCT"));
				typej = false;
			} else {
				/// * 組存 */
				defATMIAC.setAtmiacActno(
						getTxData().getT24Response().getTotaEnquiryContents().get(i).get("T.AIO.MAIN.AC"));
				typej = true;
			}
			defATMIAC.setAtmiacCurcd(getTxData().getT24Response().getTotaEnquiryContents().get(i).get("CUR"));

			defATMIAC.setAtmiacKind(DbHelper.toShort(typej));
			defATMIAC.setAtmiacRateDisc(DbHelper.toShort(
					(getTxData().getT24Response().getTotaEnquiryContents().get(i).get("RATE.DISC").equals("0")) ? false
							: true));
			defATMIAC.setAtmiacFeeDisc(DbHelper.toShort(
					(getTxData().getT24Response().getTotaEnquiryContents().get(i).get("FEE.DISC").equals("0")) ? false
							: true));

			if (dbATMIAC.updateByPrimaryKey(defATMIAC) < 1) {
				if (dbATMIAC.insert(defATMIAC) < 1) {
					throw new Exception("ATMIAC INSERT ERROR");
				}
			}
		} catch (Exception ex) {
			rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
		}
	}
	
	/**
	 * IAC電文欄位搬移
	 * 
	 * @param iCase   t24回應電文的index
	 * @param iCnt    依照轉出轉入排序後的index
	 * @param isTrOut true:轉出,false:轉入
	 * 
	 *                IAC電文多為01~20所以另外提出處理
	 * 
	 *                <history> <modify> <modifier>Kyo</modifier> <reason>修改IAC
	 *                轉出時的Bug</reason> <date>2011/01/20</date> </modify> </history>
	 * @throws Exception 
	 */
	private void assignIACTypes(int iCase, int iCnt, boolean isTrOut) throws Exception {
		//ben20221118  
	}
	
	/**
	 * 將字串切成固定長度(不足補空白)
	 * 
	 * @param strfSource 字串
	 * @param intStart   開始位置
	 * @param intLen     取N Byte長度
	 * @return Bytes
	 * 
	 *         <history> <modify> <modifier>Ruling></modifier>
	 *         <date>2020/08/10</date> <reason>New</reason> </modify> </history>
	 */
	public final String cutStr(String strfSource, int intStart, int intLen) {
		String strBig5 = "";
		int strLen = 0;
		try {
			if (StringUtils.isBlank(strfSource)) {
				strBig5 = StringUtils.repeat(StringUtils.SPACE, intLen);
			} else {
				strLen = ConvertUtil.toBytes(strfSource, PolyfillUtil.toCharsetName("big5")).length; // 轉Big5後的長度
				if (strLen <= intLen) {
					// 等於長度或長度不足(右補空白)
					byte[] b = big5BytesParse(strfSource, intStart, strLen);
					strBig5 = ConvertUtil.toString(b, PolyfillUtil.toCharsetName("big5")) + StringUtils.repeat(StringUtils.SPACE, intLen - strLen);
				} else {
					// 超過長度,切割字串
					byte[] b = big5BytesParse(strfSource, intStart, intLen);
					strBig5 = ConvertUtil.toString(b, PolyfillUtil.toCharsetName("big5"));
				}
			}
		} catch (Exception ex) {
			throw ex;
		}
		return strBig5;
	}
}
