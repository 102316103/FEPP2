package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.T24TxType;

/**
 * 負責處理跨行存款發動的跨行交易Req電文
 * <li>IFT2521:轉入
 * {@link} INBKAABase
 * 
 * @author Jennifer
 * @since 20 May 2021
 * @see com.syscom.fep.server.aa.inbk.INBKAABase
 */
public class ODTRequestA extends INBKAABase {

	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	private String rtnMessage = "";
	private boolean needResponseMsg = true;
	@SuppressWarnings("unused")
	private ATMTXCD txCode; // ATM交易代號
	/**
	 * AA可以透過ATMBusiness變數取得Business.ATM物件, <br>
	 * <p>
	 * ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	 * FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	 *
	 * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	 * @throws Exception
	 * 
	 */
	public ODTRequestA(ATMData txnData) throws Exception {
		super(txnData);
		//--ben-20220922-//this.txCode = ATMTXCD.parse(this.getATMRequest().getTXCD());
	}

	@Override
	public String processRequestData() {
		try {
			// '1.prepare 準備FEP交易記錄檔
			_rtnCode = getATMBusiness().prepareFEPTXN();

			// '2. add new 新增FEP交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getATMBusiness().addTXData();
			}

			// '3. business 商業邏輯檢核(ATM電文)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = checkBusinessRule();
			}

			// '4. account host 帳務主機處理
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = sendToCBS();
			}

			// '5. composit 組送 FISC 之 Request 電文並等待財金之 Response 電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());

			}

			// '6.validate 檢核 FISC 之 Response電文是否正確
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().checkResponseMessage();

			}

			// '7. ProcessAPTOT:更新跨行代收付
			if (_rtnCode == CommonReturnCode.Normal
					&& NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc())) {
				 _rtnCode = processAPTOT();
				//_rtnCode = getFiscBusiness().processAptot(true);
			}

			// '8. composit 組回應電文回給 ATM &判斷是否需組 CON 電文回財金/上主機沖正
			_rtnCode2 = sendToConfirm();

		} catch (Exception ex) {
			rtnMessage = "";
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());

		} finally {

			// 9. update 更新交易記錄(FEPTXN)
			updateFEPTXN();
			getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getATMtxData().getLogContext().setMessage(rtnMessage);
			getATMtxData().getLogContext().setProgramName(this.aaName);
			getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			getLogContext().setProgramName(this.aaName);
			logMessage(Level.DEBUG, getLogContext());
		}
		// 'composite reply 回應ATM 故最後return空字串
		return rtnMessage;
	}

	/**
	 * 3. 商業邏輯檢核(ATM電文)
	 * 
	 * @return FEPReturnCode
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		rtnCode = checkRequestFromATM(this.getATMtxData());

		if (rtnCode != FEPReturnCode.Normal) {
			return rtnCode;
		}
		// 2017/05/26 Modify by Ruling for 跨行存款交易(ODT2521)，送財金電文轉出銀行代號(BITMAP 14)放分行代號
		//--ben-20220922-//this.feptxn.setFeptxnNoticeId(getATMRequest().getBranchID());
		return rtnCode;
	}

	/**
	 * 4. 帳務主機處理 <br>
	 * <p>
	 * 帳務主機處理：本行轉入-進帳務主機查詢帳號
	 * 
	 * @return FEPReturnCode
	 */
	private FEPReturnCode sendToCBS() {

		T24 hostT24 = new T24(this.getATMtxData());
		try {
			// Get STAN 以供主機電文使用
			this.feptxn.setFeptxnStan(this.getFiscBusiness().getStan());

			return hostT24.sendToT24(this.getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.Accounting.getValue(), true);

		} catch (Exception e) {
			getLogContext().setProgramException(e);
			getLogContext().setProgramName(ProgramName + ".sendToCBS");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	/**
	 * 組回應電文回給 ATM 以及判斷是否需組 CON 電文回財金上/主機沖正 <br>
	 * <p>
	 * 判斷欄位是否有值,必須用Not IsNullorEmpty(Item)判斷,之後用Not IsNullorEmpty(Item.Trim)
	 * 
	 * @return FEPReturnCode
	 */
	private FEPReturnCode sendToConfirm() {
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		T24 hostT24 = new T24(this.getATMtxData());
		try {

			if (_rtnCode == FEPReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(this.feptxn.getFeptxnRepRc())) {
				if (DbHelper.toShort(true).equals(this.getTxData().getMsgCtl().getMsgctlAtm2way())) {
					this.feptxn.setFeptxnPending((short) 2); // release PENDING
					this.feptxn.setFeptxnReplyCode("    ");
					this.feptxn.setFeptxnTxrust(FeptxnTxrust.Successed); // '成功
					// '轉帳交易直接送 Confirm 給財金(FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal)
					this.feptxn.setFeptxnConRc(NormalRC.FISC_ATM_OK);
					_rtnCode2 = this.getFiscBusiness().sendConfirmToFISC();
				} else {
					this.feptxn.setFeptxnReplyCode("    ");
					this.feptxn.setFeptxnPending(Short.parseShort(FeptxnTxrust.Pending)); // PENDING
				}
				this.feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
				this.getFiscBusiness().updateTxData();
				this.sendToATM();

				// '寫入 ATM 清算資料
				if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmc()) &&
						(FEPChannel.ATM.toString().equals(this.feptxn.getFeptxnChannel())
								||  FEPChannel.WEBATM.toString().equals(this.feptxn.getFeptxnChannel()))) {
					_rtnCode2 = this.getATMBusiness().insertATMC(1);
				}

				// ATM 鈔匣資料(含ATMCASH/ATMSTAT)-for實體ATM
				if (DbHelper.toBoolean(this.getTxData().getMsgCtl().getMsgctlUpdateAtmcash()) &&
						(FEPChannel.ATM.toString().equals(this.feptxn.getFeptxnChannel() ))) {
					_rtnCode2 = this.getATMBusiness().updateATMCash(1);
				}

			} else {
				if (StringUtils.isNotBlank(this.feptxn.getFeptxnRepRc())) {
					this.feptxn.setFeptxnPending((short) 2); // '解除PENDING
					if (NormalRC.FISC_ATM_OK.equals(this.feptxn.getFeptxnRepRc())) {
						// +REP
						getLogContext().setProgramName(ProgramName);
						feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
						getLogContext().setProgramName(ProgramName);

						// '將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext()));
						feptxn.setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
						_rtnCode2 = getFiscBusiness().sendConfirmToFISC(); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal

					} else {
						// -REP
						feptxn.setFeptxnTxrust(FeptxnTxrust.RejectNormal); // Reject
						getLogContext().setProgramName(ProgramName);

						// '將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext()));

					}
				} else {
					// 'fepReturnCode <> Normal
					getLogContext().setMessage("FepTxn.FEPTXN_REPLY_CODE before:" + feptxn.getFeptxnReplyCode());
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
					feptxn.setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					if (StringUtils.isBlank(feptxn.getFeptxnReplyCode())) {
						getLogContext().setProgramName(ProgramName);

						// '將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext()));
						getLogContext().setMessage("FepTxn.FEPTXN_REPLY_CODE IN:" + feptxn.getFeptxnReplyCode());
						getLogContext().setProgramName(ProgramName);
						logMessage(Level.DEBUG, getLogContext());
					}
					getLogContext().setMessage("FepTxn.FEPTXN_REPLY_CODE after:" + feptxn.getFeptxnReplyCode());
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
				}
				feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
				this.getFiscBusiness().updateTxData();
				this.sendToATM();
				if (feptxn.getFeptxnAccType() == 1) {
					// has recored account
					// CBS主機沖正
					// 'FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
					_rtnCode2 = hostT24.sendToT24(this.getATMtxData().getMsgCtl().getMsgctlTwcbstxid(), T24TxType.EC.getValue(), true);
				}
			}

			return FEPReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".sendToConfirm");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}

	}

	/**
	 * 7. 更新跨行代收付
	 * 
	 * @return FEPReturnCode
	 */
	private FEPReturnCode processAPTOT() {

		FEPReturnCode rtncode = FEPReturnCode.Normal;
		if (DbHelper.toBoolean(this.getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
			rtncode = this.getFiscBusiness().processAptot(false);
		}
		return rtncode;
	}

	/**
	 * 8. 組回應電文
	 * 
	 * @return FEPRetureCode
	 */
	private FEPReturnCode sendToATM() {

		FEPReturnCode rtncode = FEPReturnCode.Normal;
		if (StringUtils.isBlank(this.getATMtxData().getTxResponseMessage()) && needResponseMsg) {
			rtnMessage = this.prepareATMResponseData();
		} else {
			rtnMessage = this.getATMtxData().getTxResponseMessage();
		}
		return rtncode;
	}

	/**
	 * 9. 更新交易記錄檔
	 * 
	 * @return FEPReturnCode
	 */
	private FEPReturnCode updateFEPTXN() {

		if (_rtnCode != FEPReturnCode.Normal) {
			this.feptxn.setFeptxnAaRc(_rtnCode.getValue());
		} else if (_rtnCode2 != FEPReturnCode.Normal) {
			this.feptxn.setFeptxnAaRc(_rtnCode2.getValue());
		} else {
			this.feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}

		this.feptxn.setFeptxnAaComplete(DbHelper.toShort(true));

		return this.getFiscBusiness().updateTxData();
	}

}
