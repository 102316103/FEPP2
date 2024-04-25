package com.syscom.fep.server.aa.atmp;

import java.util.Map;

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
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMCTxType;
import com.syscom.fep.vo.enums.ATMTXCD;

/**
 * 負責處理ATM現金繳費找零交易電文 CFT: 現金繳費找零交易 <history> <modify> <modifier>Fly</modifier>
 * <reason>OKI硬幣機功能</reason> <date>2018/10/01</date> </modify> </history>
 * 
 * @author Han 2022/5/16
 */
public class SelfChange extends ATMPAABase {

	private ATMTXCD txCode; // ATM交易代號
	private String rtnMessage = StringUtils.EMPTY;
	private boolean needUpdateFEPTXN = false;
	private FEPReturnCode rtnCode = CommonReturnCode.Normal;
	private Map<String, String> creditWSRsp = null;
	private Map<String, String> smtpWSRsp = null;

	/**
	 * AA的建構式,在這邊初始化及設定好相關變數
	 * 
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 * @throws Exception
	 */
	public SelfChange(ATMData txnData) throws Exception {
		super(txnData);
		//--ben-20220922-//this.txCode = ATMTXCD.parse(this.getATMRequest().getTXCD());
	}

	/**
	 * AA進入點主程式
	 */
	@Override
	public String processRequestData() {
		try {
			// 1.準備FEP交易記錄檔
			this.rtnCode = this.getATMBusiness().prepareFEPTXN();
			// 2.新增FEP交易記錄檔
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.rtnCode = this.getATMBusiness().addTXData();
			}
			// 3.商業邏輯檢核
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.needUpdateFEPTXN = true;
				this.rtnCode = this.checkBusinessRule();
			}
			// 4. 帳務主機處理
			if (this.rtnCode == CommonReturnCode.Normal) {
				this.rtnCode = this.SendToHost();
			}
			// 5. 更新交易記錄(FEPTXN)
			this.updateFEPTxn();
		} catch (Exception e) {
			this.rtnCode = CommonReturnCode.ProgramException;
			this.getATMBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
		} finally {
			// 6.組回應電文(當海外主機有給TOTA或DES發生例外或主機逾時不組回應)
			if (StringUtils.isBlank(this.getTxData().getTxResponseMessage())) {
				this.rtnMessage = this.prepareATMResponseData();
			} else {
				this.rtnMessage = this.getTxData().getTxResponseMessage();
			}
			this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			this.getTxData().getLogContext().setMessage(this.rtnMessage);
			this.getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			this.logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(this.rtnCode));
			logMessage(Level.DEBUG, this.logContext);
		}
		return this.rtnMessage;
	}

	/**
	 * 3. 商業邏輯檢核 相關程式
	 * 
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		ENCHelper encHelper = new ENCHelper(this.getATMBusiness().getFeptxn());
		try {
			// 3.1 CheckHeader
			rtnCode = this.getATMBusiness().checkHeader();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// 3.2 CheckBody
			rtnCode = this.getATMBusiness().checkBody();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			// 2019/05/03 Modify by Ruling for OKI硬幣機功能第二階段：增加IQQ
			//--ben-20220922-//if (ATMTXCD.IQQ.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())
			//--ben-20220922-//		&& StringUtils.isBlank(this.getATMRequest().getQRCNO())) {
			//--ben-20220922-//	this.logContext.setRemark("轉入帳號不能為NULL或空白");
			//--ben-20220922-//	logMessage(Level.INFO, this.logContext);
			//--ben-20220922-//	return ATMReturnCode.OtherCheckError;
			//--ben-20220922-//}

			// 2020/12/30 Modify by Ruling for 手機門號跨行轉帳(第二階段)
			if (ATMTXCD.MTQ.name().equals(this.getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				//--ben-20220922-//if (StringUtils.isBlank(this.getATMRequest().getMOBILE())) {
				//--ben-20220922-//	this.logContext.setRemark("CheckBusinessRule-MOBILE行動電話號碼為NULL或空白");
				//--ben-20220922-//	logMessage(Level.INFO, this.logContext);
				//--ben-20220922-//	return ATMReturnCode.OtherCheckError;
				//--ben-20220922-//}
				//--ben-20220922-//if (this.getATMRequest().getMOBILE().length() < 10) {
				//--ben-20220922-//	this.logContext.setRemark("CheckBusinessRule-MOBILE行動電話號碼輸入長度小於10位");
				//--ben-20220922-//	logMessage(Level.INFO, this.logContext);
				//--ben-20220922-//	return ATMReturnCode.OtherCheckError;
				//--ben-20220922-//}
			}

			// 3.3 本行卡片，檢核卡片狀態
			if (SysStatus.getPropertyValue().getSysstatHbkno()
					.equals(this.getATMBusiness().getFeptxn().getFeptxnTroutBkno())
					&& !(StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnIcmark().trim()))) {

				rtnCode = this.getATMBusiness().checkCardStatus();

				if (rtnCode != CommonReturnCode.Normal) {
					// 檢核卡片狀況失敗,更新交易記錄
					return rtnCode;
				}
			}

			// 3.4 更新 ATM 狀態
			rtnCode = this.getATMBusiness().updateATMStatus();

			if (rtnCode != CommonReturnCode.Normal) {
				// 更新ATM狀態失敗,更新交易記錄
				return rtnCode;
			}

			// 3.5 檢核ATM電文訊息押碼(MAC)
			if (this.getTxData().getMsgCtl().getMsgctlReqmacType() != null
					&& StringUtils.isNotBlank(this.getTxData().getMsgCtl().getMsgctlReqmacType().toString())) {
				//--ben-20220922-//if (this.getATMRequest().getMAC() != null && StringUtils.isNotBlank(this.getATMRequest().getMAC())) {
				//--ben-20220922-//	this.logContext.setRemark("CheckBusinessRule-ATM電文的MAC為空白");
				//--ben-20220922-//	logMessage(Level.INFO, this.logContext);
				//--ben-20220922-//	return FEPReturnCode.ENCCheckMACError;
				//--ben-20220922-//}
				//--ben-20220922-//rtnCode = encHelper.checkATMMAC(this.getATMRequest().getMAC());
			}
			return rtnCode;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, "checkBusinessRule"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 4. 帳務主機處理 ''' <summary> 4. 送主機 </summary> <returns></returns>
	 * <remarks></remarks>
	 * 
	 * @return
	 */
	private FEPReturnCode SendToHost() {
//		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		T24 hostT24 = new T24(this.getTxData());
		try {
			// 2019/05/03 Modify by Ruling for OKI硬幣機功能第二階段：增加IQQ
			// 2020/12/30 Modify by Ruling for 手機門號跨行轉帳(第二階段)：增加MTQ
			switch (txCode) {
			case CFT:
				//--ben-20220922-//if ("1".equals(this.getATMRequest().getPAYTYPE().toString())) {
				//--ben-20220922-//	rtnCode = hostT24.sendToT24(this.getTxData().getMsgCtl().getMsgctlTwcbstxid(),
				//--ben-20220922-//			T24TxType.Accounting.getValue(), false);
				//--ben-20220922-//}
				break;
			case IQQ:
				// to do
				//--ben-20220922-//rtnCode = this.getATMBusiness().sendToCreditWS(this.getATMRequest().getQRCNO(), creditWSRsp);
				break;
			case MTQ:
				// SMTP todo SMTP改用 改用IMS Connect
				// SMTP hostSMTP = new SMTP(this.getTxData());
				// smtpWSRsp = new HashMap<String, String>();
				// _rtnCode = hostSMTP.SendToSMTP(ATMRequest.MOBILE, smtpWSRsp)
				break;
			default:
				break;
			}

			// 2021/02/08 Modify by Ruling for 無卡及硬幣機交易筆數與金額寫入ATMC
			if (this.rtnCode == CommonReturnCode.Normal) {

				// 寫入ATM清算資料
				if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {

					// Fly 2021/04/06 調整ReturnCode
					FEPReturnCode rtnCode2 = this.getATMBusiness().insertATMC(ATMCTxType.Accounting.getValue());

					if (rtnCode2 != CommonReturnCode.Normal) {
						this.logContext.setRemark("InsertATMC寫入失敗!");
						logMessage(Level.INFO, this.logContext);
					}
				}
			}
			return rtnCode;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, "SendToHost"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 6. 更新交易記錄 相關程式"
	 * 
	 * <summary> 5. 更新交易記錄(FEPTxn) </summary> <remarks></remarks>
	 */
	private void updateFEPTxn() {
		this.getATMBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
		this.getATMBusiness().getFeptxn().setFeptxnAaRc(this.rtnCode.getValue());

		if (StringUtils.isBlank(this.getATMBusiness().getFeptxn().getFeptxnReplyCode())) {
			if (this.rtnCode == FEPReturnCode.Normal) {
				this.getATMBusiness().getFeptxn().setFeptxnReplyCode(NormalRC.ATM_OK);
			} else {
				this.getATMBusiness().getFeptxn()
						.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(this.rtnCode.getValue()),
								FEPChannel.FEP, this.getTxData().getTxChannel(), this.getTxData().getLogContext()));
			}
		}

		this.getATMBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /* AA Complete */

		// For報表, 寫入處理結果
		ReportTXRUST();

		if (this.rtnCode == CommonReturnCode.Normal
				&& this.getATMBusiness().checkEXPCD(this.getATMBusiness().getFeptxn().getFeptxnReplyCode())) {
			if (this.getATMBusiness().getFeptxn().getFeptxnWay() == 3) {
				this.getATMBusiness().getFeptxn().setFeptxnTxrust("B"); // 處理結果=Pending
			} else {
				this.getATMBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
			}
		} else {
			this.getATMBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
		}

		if (this.needUpdateFEPTXN) {
			FEPReturnCode rtncode = CommonReturnCode.Normal;
			rtncode = this.getATMBusiness().updateTxData();
			if (rtncode != CommonReturnCode.Normal) {
				this.getATMBusiness().getFeptxn().setFeptxnReplyCode("L013"); // 回寫檔案(FEPTxn)發生錯誤
			}
		}
	}

	/**
	 * 7. 組回應電文 相關程式
	 * 
	 * <summary> 6. 組ATM回應電文 </summary> <remarks>
	 * 組ATM回應電文,Response物件的值已經在AA中MapGeneralResponseFromGeneralRequest搬好Header的值了
	 * 這裏只要處理Response的body的欄位值即可 </remarks>
	 * 
	 * @return String
	 */
	private String prepareATMResponseData() {
		String atmResponseString = StringUtils.EMPTY;
		return "";
		
		//ben20221118  
		/*
		try {
			// 組 ATM_TOTA HEADER 
			this.getATMBusiness().mapResponseFromRequest();
			this.loadCommonATMResponse();
			//--ben-20220922-//ATMTXCD txCode2 = ATMTXCD.parse(this.getATMRequest().getTXCD());
			ATMTXCD txCode2 = ATMTXCD.parse("");

			// 2019/05/03 Modify by Ruling for OKI硬幣機功能第二階段：增加IQQ
			switch (txCode2) {
			case CFT: {
				CFTResponse atmRsp = new CFTResponse();
				atmResponseString = atmRsp.makeMessageFromGeneral(this.getTxData().getTxObject());
				atmRsp = null;
				break;
			}
			case IQQ: {
				if (rtnCode == CommonReturnCode.Normal) {
					if (null != creditWSRsp) {
						BigDecimal stringToBigDecimal = null;

						if (creditWSRsp.containsKey("PID") && StringUtils.isNotEmpty(creditWSRsp.get("PID"))) {
							this.getATMResponse().setPID(creditWSRsp.get("PID").trim());
						}
						if (creditWSRsp.containsKey("IDTYPE") && StringUtils.isNotEmpty(creditWSRsp.get("IDTYPE"))) {
							this.getATMResponse().setIDTYPE(creditWSRsp.get("IDTYPE").trim());
						}
						if (creditWSRsp.containsKey("ACCTID") && StringUtils.isNotEmpty(creditWSRsp.get("ACCTID"))) {
							this.getATMResponse()
									.setPAYCNO(StringUtils.leftPad(creditWSRsp.get("ACCTID").trim(), 16, '0'));
						}
						if (creditWSRsp.containsKey("CURRBAL") && StringUtils.isNotEmpty(creditWSRsp.get("CURRBAL"))) {
							stringToBigDecimal = new BigDecimal(creditWSRsp.get("CURRBAL").trim());
							this.getATMResponse().setCURRBAL(stringToBigDecimal);
						}
						if (creditWSRsp.containsKey("DUEAMT") && StringUtils.isNotEmpty(creditWSRsp.get("DUEAMT"))) {
							stringToBigDecimal = new BigDecimal(creditWSRsp.get("DUEAMT").trim());
							this.getATMResponse().setDUEAMT(stringToBigDecimal);
						}
						if (creditWSRsp.containsKey("DUEDATE") && StringUtils.isNotEmpty(creditWSRsp.get("DUEDATE"))) {
							this.getATMResponse().setDUEDATE(creditWSRsp.get("DUEDATE").trim());
						}
						if (creditWSRsp.containsKey("STMTDATE")
								&& StringUtils.isNotEmpty(creditWSRsp.get("STMTDATE"))) {
							this.getATMResponse().setSTMTDATE(creditWSRsp.get("STMTDATE").trim());
						}
						if (creditWSRsp.containsKey("LPYMTDT") && StringUtils.isNotEmpty(creditWSRsp.get("LPYMTDT"))) {
							this.getATMResponse().setLPYMTDT(creditWSRsp.get("LPYMTDT").trim());
						}
						if (creditWSRsp.containsKey("LPYMTAMT")
								&& StringUtils.isNotEmpty(creditWSRsp.get("LPYMTAMT"))) {
							stringToBigDecimal = new BigDecimal(creditWSRsp.get("LPYMTAMT").trim());
							this.getATMResponse().setDUEAMT(stringToBigDecimal);
						}
						if (creditWSRsp.containsKey("PAYSUM") && StringUtils.isNotEmpty(creditWSRsp.get("PAYSUM"))) {
							stringToBigDecimal = new BigDecimal(creditWSRsp.get("PAYSUM").trim());
							this.getATMResponse().setDUEAMT(stringToBigDecimal);
						}
						stringToBigDecimal = null;
					}
				}
				IQQResponse atmRsp = new IQQResponse();
				atmResponseString = atmRsp.makeMessageFromGeneral(this.getTxData().getTxObject());
				break;
			}
			case MTQ: {
				if (rtnCode == CommonReturnCode.Normal) {
					if (null != smtpWSRsp) {
						if (smtpWSRsp.containsKey("BankCode") && StringUtils.isNotEmpty(smtpWSRsp.get("BankCode"))) {
							this.getATMResponse().setBKNO(smtpWSRsp.get("BankCode").trim());
						}
						if (smtpWSRsp.containsKey("AccountName")
								&& StringUtils.isNotEmpty(smtpWSRsp.get("AccountName"))) {
							byte b[] = Big5BytesParse(StringUtil
									.toHex(StringUtils.rightPad(smtpWSRsp.get("AccountName").trim(), 54, ' ')), 0, 54);
							this.getATMResponse().setNAME(StringUtil.toHex(b));
						}
					}
				}

				// 電文如NAME沒有資料要預設54個空白並轉HEX
				if (StringUtils.isNotEmpty(this.getATMResponse().getNAME())) {
					this.getATMResponse().setNAME(StringUtil.toHex(ConvertUtil
							.toBytes(StringUtils.repeat(StringUtils.SPACE, 54), PolyfillUtil.toCharsetName("big5"))));
				}
				MTQResponse atmRsp = new MTQResponse();
				atmResponseString = atmRsp.makeMessageFromGeneral(this.getTxData().getTxObject());
				break;
			}
			default:
				break;
			}
			return atmResponseString;
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			sendEMS(this.logContext);
			return StringUtils.EMPTY;
		}
		*/
	}

	/**
	 * ''' <summary> ''' 組ATM回應電文(共同) ''' </summary> '''
	 * <remarks>回應電文共同的部分T</remarks>
	 */
	private void loadCommonATMResponse() {
		ENCHelper encHelper = new ENCHelper(this.getATMBusiness().getFeptxn());
		RefString desMACData = new RefString(StringUtils.EMPTY);
		FEPReturnCode rtnCode = FEPReturnCode.Normal;

		//ben20221118  this.getATMResponse().setREJCD(this.getATMBusiness().getFeptxn().getFeptxnReplyCode());
		//ben20221118  this.getATMResponse().setTXSEQ(this.getATMBusiness().getFeptxn().getFeptxnTxseq());
		//--ben-20220922-//this.getATMResponse().setYYMMDD(this.getATMRequest().getYYMMDD()); // 壓碼日期
		//ben20221118  this.getATMResponse().setHC(this.getFeptxn().getFeptxnFeeCustpayAct());

		// 產生ATM電文MAC資料
		//--ben-20220922-//if (StringUtils.isNotBlank(this.getATMRequest().getMAC())) {
		if (StringUtils.isNotBlank("")) {
			rtnCode = encHelper.makeAtmMac("", desMACData);

			if (rtnCode != CommonReturnCode.Normal) {
				//ben20221118  this.getATMResponse().setMAC("");
			} else {
				//ben20221118  this.getATMResponse().setMAC(desMACData.toString()); // 壓碼訊息
			}

			//--ben-20220922-//this.getATMResponse().setBKNO(this.getATMRequest().getBKNO());
			//--ben-20220922-//this.getATMResponse().setCHACT(this.getATMRequest().getCHACT());
			//--ben-20220922-//this.getATMResponse().setTXACT(this.getATMRequest().getTXACT());
			//--ben-20220922-//this.getATMResponse().setTXAMT(this.getATMRequest().getTXAMT());
			//--ben-20220922-//this.getATMResponse().setCASHWAMT(this.getATMRequest().getCASHWAMT());
			//--ben-20220922-//this.getATMResponse().setCOINWAMT(this.getATMRequest().getCOINWAMT());
			//--ben-20220922-//this.getATMResponse().setCASHAMT(this.getATMRequest().getCASHAMT());
			//--ben-20220922-//this.getATMResponse().setCOINAMT(this.getATMRequest().getCOINAMT());
			//--ben-20220922-//this.getATMResponse().setPAYTYPE(this.getATMRequest().getPAYTYPE());
		}

	}

	/**
	 * <summary> For報表, 寫入處理結果n) </summary> <remarks></remarks>
	 */
	private void ReportTXRUST() {
		if (this.rtnCode == CommonReturnCode.Normal) {

			if (this.getATMBusiness().getFeptxn().getFeptxnWay() == 3) {

				if (this.getATMBusiness().getFeptxn().getFeptxnWay() == 3) {
					this.getATMBusiness().getFeptxn().setFeptxnTxrust("B"); // 處理結果=Pending
				} else {
					this.getATMBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
				}

			} else {
				this.getATMBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
			}
		}
	}

	private byte[] Big5BytesParse(String strfSource, Integer intStart, Integer intLen) {
		byte byteAry[] = ConvertUtil.toBytes(strfSource, PolyfillUtil.toCharsetName("big5"));
		byte[] newAry = new byte[intLen];
		Integer i = 0, j = 0;
		while (j < intLen) {
			newAry[i] = byteAry[intStart];
			i = i + 1;
			j = j + 1;
			intStart = intStart + 1;
		}
		return newAry;
	}

}
