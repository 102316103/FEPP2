package com.syscom.fep.server.aa.atmp;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.ATMAdapter;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMCTxType;
import com.syscom.fep.vo.enums.ATMCashTxType;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.ATMType;
import com.syscom.fep.vo.enums.CBSTxType;
import com.syscom.fep.vo.text.atm.response.DDFResponse;
import com.syscom.fep.vo.text.atm.response.ODFResponse;

public class SelfODRC extends ATMPAABase {
	private String rtnMessage = StringUtils.EMPTY;
	private FEPReturnCode rtnCode = CommonReturnCode.Normal;
	private boolean needUpdateFEPTXN = false;
	private ATMTXCD txCode = null; // ATM交易代號

	public SelfODRC(ATMData txnData) throws Exception {
		super(txnData);
		//--ben-20220922-//this.txCode = ATMTXCD.parse(this.getATMRequest().getTXCD());
	}

	@Override
	public String processRequestData() {
		try {
			// 1.商業邏輯檢核
			this.rtnCode = checkBusinessRule();

			// 2.組回應電文回給ATM
			if (txCode != ATMTXCD.DDF) {
				// 組回應電文
				rtnMessage = prepareATMResponseData();
				// 回應ATM電文
				sendToATM();
			}

			// 3.帳務主機處理
			if (this.rtnCode != CommonReturnCode.OriginalMessageNotFound) {
				sendToHost();
			}

			// 若為DDF電文且上主機有正常回應，將回應ATM訊息改為主機回應電文
			if (txCode == ATMTXCD.DDF) {
				// 組回應電文
				rtnMessage = prepareATMResponseData();
			} else {
				rtnMessage = "";
			}

			// 4.更新FEP交易記錄檔
			if (needUpdateFEPTXN) {
				updateFEPTxn();
			}

		} catch (Exception ex) {
			rtnMessage = "";
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(rtnMessage);
			getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(this.rtnCode));
			logMessage(Level.DEBUG, getLogContext());
		}

		return rtnMessage;
	}

	// 商業邏輯檢核
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 1.1 檢核原交易帳號及金額
		getATMBusiness().setFeptxn(getATMBusiness().checkConData());
		if (getATMBusiness().getFeptxn() == null) {
			// 取得交易明細失敗，更新交易記錄
			return CommonReturnCode.OriginalMessageNotFound; // 回E944
		}

		// 若取得原交易才設定更新FEPTXN
		needUpdateFEPTXN = true;

		// 1.2 將ATM確認電文，準備寫入原交易FEPTXN欄位
		rtnCode = getATMBusiness().prepareConFEPTXN();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 避免ATM重覆送Comfirm電文先更新FEPTXN
		//--ben-20220922-//if (ATMTXCD.ODF.toString().equals(getATMRequest().getTXCD())
		//--ben-20220922-//		|| ATMTXCD.DDF.toString().equals(getATMRequest().getTXCD())) {
		//--ben-20220922-//	rtnCode = getATMBusiness().updateTxData();
		//--ben-20220922-//	if (rtnCode != CommonReturnCode.Normal) {
		//--ben-20220922-//		return rtnCode;
		//--ben-20220922-//	}
		//--ben-20220922-//}

		// 1.3 如ATM為存提款機時，更新錢箱資料
		if (getATMBusiness().getAtmStr().getAtmAtmtype() == ATMType.ADM.getValue()) {
			//--ben-20220922-//if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmbox())
			//--ben-20220922-//		&& getATMBusiness().checkADMData(getATMRequest().getAdmboxArea())) {
			//--ben-20220922-//	rtnCode = getATMBusiness().updateATMCash(ATMCashTxType.Box.getValue());
			//--ben-20220922-//	if (rtnCode != CommonReturnCode.Normal) {
			//--ben-20220922-//		return rtnCode;
			//--ben-20220922-//	}
			//--ben-20220922-//}
		}
		return rtnCode;
	}

	// 組ATM回應電文,Response物件的值已經在AA中MapGeneralResponseFromGeneralRequest搬好Header的值了
	// 這裏只要處理Response的body的欄位值即可
	private String prepareATMResponseData() throws Exception {
		String atmResponseString = "";
		getATMBusiness().mapResponseFromRequest();
		try {
			// 載入共用的回應電文
			loadCommonResponse();

			switch (txCode) {
				case ODF:
					//--ben-20220922-//getATMResponse().setCURCD(getATMRequest().getCURCD());

					break;
				case DDF:
					//--ben-20220922-//getATMResponse().setCURCD(getATMRequest().getCURCD());
					if (getATMBusiness().getFeptxn() == null) {
						//ben20221118  getATMResponse().setBal11S("+");
						//ben20221118  getATMResponse().setBAL11(BigDecimal.ZERO);
						//ben20221118  getATMResponse().setBal12S("+");
						//ben20221118  getATMResponse().setBAL12(BigDecimal.ZERO);
					}
					break;
				default:
					break;
			}
			atmResponseString = parstToText();
			return atmResponseString;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
	}

	// 提早送回應給ATM
	private void sendToATM() {

		try {
			ATMAdapter atmAdapter = new ATMAdapter(getTxData());
			FEPReturnCode rtnCode = FEPReturnCode.Normal;

			//--ben-20220922-//atmAdapter.setAtmNo(getATMRequest().getBRNO() + getATMRequest().getWSNO());

			if (StringUtils.isNotBlank(rtnMessage.trim())) {
				atmAdapter.setMessageToATM(rtnMessage);
				rtnCode = atmAdapter.sendReceive();
				if (rtnCode != CommonReturnCode.Normal) {
					this.rtnCode = rtnCode;
				}
			} else {
				// 若需要回ATM電文，但rtnMessage是空的表示有問題需alert
				getLogContext().setRemark("ATM組出來的回應電文為空字串");
				logMessage(Level.DEBUG, getLogContext());
			}

			// 因為先送給ATM了所以要將回應字串清成空字串
			rtnMessage = "";

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			this.rtnCode = CommonReturnCode.ProgramException;
		}
	}

	// 海外卡提款確認電文,如為Con(-), 須組扣帳沖正電文, 送往海外優利主機
	// 台灣卡,如為Con(-), 須組扣帳沖正電文,送往T24/ASC主機
	private void sendToHost() {
		T24 hostT24 = new T24(this.getTxData());
		try {
			//--ben-20220922-//if (getATMBusiness().checkEXPCD(getATMRequest().getEXPCD())) {
				// 台灣卡信用卡交易Con(+)
			//--ben-20220922-//	if (ATMTXCD.DDF.toString().equals(getATMRequest().getTXCD())) {
					// 組 T24 電文 (A2920)
			//--ben-20220922-//		this.rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(),
			//--ben-20220922-//				CBSTxType.Accounting.getValue(), true);

					// 寫入 ATM 清算資料
			//--ben-20220922-//		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
			//--ben-20220922-//			this.rtnCode = getATMBusiness().insertATMC(ATMCTxType.Accounting.getValue());
			//--ben-20220922-//			if (this.rtnCode != CommonReturnCode.Normal) {
							// Exit Sub
			//--ben-20220922-//			}
			//--ben-20220922-//		}

					// 更新 ATM 鈔匣資料
			//--ben-20220922-//		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())) {
			//--ben-20220922-//			this.rtnCode = getATMBusiness().updateATMCash(ATMCashTxType.Accounting.getValue());
			//--ben-20220922-//			if (this.rtnCode != CommonReturnCode.Normal) {
							// Exit Sub
			//--ben-20220922-//			}
			//--ben-20220922-//		}
			//--ben-20220922-//	}
			//--ben-20220922-//} else {
				// 台灣卡信用卡交易Con(-)
				// 無
			//--ben-20220922-//}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			this.rtnCode = CommonReturnCode.ProgramException;
		}
	}

	// 更新交易記錄 相關程式
	private void updateFEPTxn() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(
				getTxData().getMessageID(),
				getTxData().getTxChannel(),
				getTxData().getTxSubSystem(),
				getTxData().getEj(),
				//--ben-20220922-//StringUtils.join(getATMRequest().getBRNO(), getATMRequest().getWSNO()),
				StringUtils.join("",""),
				getTxData().getAtmSeq(),
				getTxData());

		// 交易確認電文檢核 MAC
		//--ben-20220922-//if (FEPChannel.ATM.toString().equals(getATMBusiness().getFeptxn().getFeptxnChannel())
		//--ben-20220922-//		&& StringUtils.isNotBlank(getATMRequest().getMAC())) {
		//--ben-20220922-//	rtnCode = encHelper.checkConMac(
		//--ben-20220922-//			StringUtils.join(getATMRequest().getBRNO(), getATMRequest().getWSNO()),
		//--ben-20220922-//			txCode,
		//--ben-20220922-//			getATMRequest().getActD(),
		//--ben-20220922-//			getATMRequest().getAtmseq_2(),
		//--ben-20220922-//			getATMRequest().getEXPCD(),
		//--ben-20220922-//			getATMRequest().getYYMMDD(),
		//--ben-20220922-//			getATMRequest().getMAC());
		//--ben-20220922-//}

		getATMBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Response);

		if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnConReplyCode())) {
			if (this.rtnCode == FEPReturnCode.Normal) {
				getATMBusiness().getFeptxn().setFeptxnConReplyCode(NormalRC.ATM_OK);
			} else {
				getATMBusiness().getFeptxn().setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(this.rtnCode.getValue()),
						FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
			}
		}

		getATMBusiness().getFeptxn().setFeptxnAaRc(this.rtnCode.getValue());
		getATMBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

		if (this.rtnCode == CommonReturnCode.Normal) {
			if (ATMTXCD.DDF.toString().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())) {
				//--ben-20220922-//if (getATMBusiness().checkEXPCD(getATMRequest().getEXPCD())) {
				//--ben-20220922-//	getATMBusiness().getFeptxn().setFeptxnTxrust("A");// Con(+), 處理結果=成功
				//--ben-20220922-//} else {
				//--ben-20220922-//	getATMBusiness().getFeptxn().setFeptxnTxrust("C"); // Con(-), 處理結果=沖正
				//--ben-20220922-//}
			} else {
				getATMBusiness().getFeptxn().setFeptxnTxrust("A"); // Con(+), 處理結果=成功
			}
		}

		if (needUpdateFEPTXN) {
			rtnCode = getATMBusiness().updateTxData();
			if (rtnCode != CommonReturnCode.Normal) {
				getATMBusiness().getFeptxn().setFeptxnReplyCode("L013"); // 回寫檔案(FEPTxn)發生錯誤
			}
		}
	}

	// 組ATM_TOTA OUT-TEXT
	private void loadCommonResponse() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(
				getTxData().getMessageID(),
				getTxData().getTxChannel(),
				getTxData().getTxSubSystem(),
				getTxData().getEj(),
				//--ben-20220922-//StringUtils.join(getATMRequest().getBRNO(), getATMRequest().getWSNO()),
				StringUtils.join("",""),
				getTxData().getAtmSeq(), getTxData());
		RefString desReturnData = new RefString(StringUtils.EMPTY);
		if (getATMBusiness().getFeptxn() == null) {
			// 找不到原交易
			//ben20221118  getATMResponse().setREJCD(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
			//--ben-20220922-//getATMResponse().setTXSEQ(getATMRequest().getTXSEQ()); // 中心交易序號
		} else {
			// 找得到原交易
			if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnConReplyCode())) {
				if (this.rtnCode == FEPReturnCode.Normal) {
					getATMBusiness().getFeptxn().setFeptxnConReplyCode(NormalRC.ATM_OK);
				} else {
					getATMBusiness().getFeptxn()
							.setFeptxnConReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(this.rtnCode.getValue()), FEPChannel.FEP, getTxData().getTxChannel(), getTxData().getLogContext()));
				}
			}
			//ben20221118  getATMResponse().setREJCD(getATMBusiness().getFeptxn().getFeptxnConReplyCode());
			if (StringUtils.isBlank(getATMBusiness().getFeptxn().getFeptxnConTxseq())) {
				//ben20221118  getATMResponse().setTXSEQ(getATMBusiness().getFeptxn().getFeptxnTxseq()); // 中心交易序號
			} else {
				//ben20221118  getATMResponse().setTXSEQ(getATMBusiness().getFeptxn().getFeptxnConTxseq()); // 確認中心交易序號
			}
		}

		//--ben-20220922-//getATMResponse().setYYMMDD(getATMRequest().getYYMMDD());// 壓碼日期
		//ben20221118  getATMResponse().setBal13S("+");
		//ben20221118  getATMResponse().setBal14S("+");

		// 產生ATM電文MAC資料
		//--ben-20220922-//if (getATMBusiness().getFeptxn() != null
		//--ben-20220922-//		&& FEPChannel.ATM.toString().equals(getATMBusiness().getFeptxn().getFeptxnChannel())
		//--ben-20220922-//		&& StringUtils.isNotBlank(getATMRequest().getMAC())) {
			// 新增一個rtncode變數來承接MAKEMAC是否成功，避免前面有檢核錯誤，但是被MAKEMAC給蓋回NORMAL
		//--ben-20220922-//	rtnCode = encHelper.makeConMac(
		//--ben-20220922-//			StringUtils.join(getATMRequest().getBRNO(), getATMRequest().getWSNO()),
		//--ben-20220922-//			txCode,
		//--ben-20220922-//			getATMRequest().getTXACT(),
		//--ben-20220922-//			getATMRequest().getAtmseq_2(),
		//--ben-20220922-//			getATMResponse().getREJCD(),
		//--ben-20220922-//			getATMRequest().getYYMMDD(),
		//--ben-20220922-//			getATMRequest().getTXAMT().toString(),
		//--ben-20220922-//			desReturnData);
		//--ben-20220922-//	if (rtnCode != CommonReturnCode.Normal) {
		//--ben-20220922-//		getATMResponse().setMAC("");
		//--ben-20220922-//	} else {
		//--ben-20220922-//		getATMResponse().setMAC(desReturnData.get());// 壓碼訊息8碼
		//--ben-20220922-//	}
		//--ben-20220922-//}

		//--ben-20220922-//getATMResponse().setBKNO(getATMRequest().getBKNO());
		//--ben-20220922-//getATMResponse().setCHACT(getATMRequest().getCHACT());
		//--ben-20220922-//getATMResponse().setTXACT(getATMRequest().getTXACT());
		//--ben-20220922-//getATMResponse().setTXAMT(getATMRequest().getTXAMT());
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

	private String parstToText() throws Exception {
		String atmResponseString = StringUtils.EMPTY;
		// 將大class搬至特定電文類別並轉成flatfile
		switch (txCode) {
			case ODF: {
				ODFResponse atmRsp = new ODFResponse();
				atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
				break;
			}
			case DDF: {
				DDFResponse atmRsp = new DDFResponse();
				atmResponseString = atmRsp.makeMessageFromGeneral(getTxData().getTxObject());
				break;
			}
			default:
				break;
		}
		return atmResponseString;
	}
}
