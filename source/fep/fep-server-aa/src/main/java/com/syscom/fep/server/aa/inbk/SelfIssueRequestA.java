package com.syscom.fep.server.aa.inbk;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.ATMAdapter;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.CreditTxType;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.T24TxType;

/**
 * @author Richard
 */
public class SelfIssueRequestA extends INBKAABase {
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	String rtnMessage = StringUtils.EMPTY;
	private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);
	private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
	private boolean needResponseMsg = true;

	// AA的建構式,在這邊初始化及設定其他相關變數
	// @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
	// 初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	// ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	// FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	public SelfIssueRequestA(ATMData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			// 1. 準備FEP交易記錄檔
			_rtnCode = getATMBusiness().prepareFEPTXN();

			// 2. 新增FEP交易記錄檔
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getATMBusiness().addTXData();
			}

			// 3. 商業邏輯檢核(ATM電文)
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = checkBusinessRule();
			}

			// 4. 帳務主機處理
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = sendToCBS();
			}

			// 5. 組送 FISC 之 Request 電文並等待財金之 Response 電文
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
			}

			// 6. 檢核 FISC 之 Response電文是否正確
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().checkResponseMessage();
			}

			// 7. ProcessAPTOT:更新跨行代收付
			if (_rtnCode == FEPReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				_rtnCode = processAPTOT();
			}

			// 8. 組回應電文回給 ATM &判斷是否需組 CON 電文回財金/上主機沖正
			_rtnCode2 = sendToConfirm();

		} catch (Exception ex) {
			rtnMessage = "";
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "processRequestData");
			sendEMS(getLogContext());
			// Finally
			// '12.組回應電文(當海外主機有給TOTA或DES發生例外或主機逾時不組回應)
		} finally {
			// 9. 更新交易記錄(getFeptxn())
			updateFEPTXN();

			// 2020/07/24 Modify by Ruling for 三萬元轉帳主動通知
			// 10. 發送簡訊及EMAIL
			sendToMailSMS();
			getATMtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getATMtxData().getLogContext().setMessage(rtnMessage);
			getATMtxData().getLogContext().setProgramName(this.aaName);
			getATMtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			getLogContext().setProgramName(this.aaName);
			logMessage(Level.DEBUG, getLogContext());
		}
		// 先組回應ATM 故最後return空字串
		return rtnMessage;
	}

	private FEPReturnCode checkBusinessRule() {
		Channel defCHANNEL = new Channel();
		try {
			if (FEPChannel.ATM.toString().equals(getFeptxn().getFeptxnChannel())
					|| FEPChannel.WEBATM.toString().equals(getFeptxn().getFeptxnChannel())) {
				_rtnCode = checkRequestFromATM(getATMtxData());
				if (_rtnCode != FEPReturnCode.Normal) {
					return _rtnCode;
				}
			} else {
				// 新增外圍 Channel 之檢核條件
				_rtnCode = checkRequestFromOtherChannel(getATMtxData());
				if (_rtnCode != FEPReturnCode.Normal) {
					return _rtnCode;
				}

				// 檢核合法Channel
				//--ben-20220922-//defCHANNEL.setChannelName(getATMRequest().getCHLCODE());
				if (channelExtMapper.selectByChannelName(defCHANNEL.getChannelName()).size() == 0) {
					getLogContext().setRemark("檢核合法 Channel 失敗，CHANNEL_NAME=" + defCHANNEL.getChannelName());
					logMessage(Level.INFO, getLogContext());
					return FEPReturnCode.ChannelNameError;
				}
			}
			return _rtnCode;
		} catch (Exception ex) {
			// 異常時要Return ProgramException
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "checkBusinessRule");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	private FEPReturnCode sendToCBS() {
		T24 hostT24 = new T24(getATMtxData());
		Credit hostCredit = new Credit(getATMtxData());
		try {
			if (StringUtils.isBlank(getFeptxn().getFeptxnTroutKind())) {// 一般金融卡帳號
				// 一般交易進CBS主機

				// 先取 STAN 以供主機電文使用
				getFeptxn().setFeptxnStan(getFiscBusiness().getStan());
				_rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(),
						T24TxType.Accounting.getValue(), true);
				if (_rtnCode != FEPReturnCode.Normal) {
					return _rtnCode;
				}
			} else {
				// Combo卡及信用卡進信用卡主機進行預借現金授權
				_rtnCode = hostCredit.sendToCredit(getATMtxData().getMsgCtl().getMsgctlAsctxid(),
						Byte.valueOf(CreditTxType.Accounting.getValue() + ""));
				if (_rtnCode != FEPReturnCode.Normal) {
					return _rtnCode;
				}

				// 先取 STAN 以供主機電文使用
				getFeptxn().setFeptxnStan(getFiscBusiness().getStan());

				// 授權成功進CBS主機掛帳
				_rtnCode = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(),
						T24TxType.Accounting.getValue(), true);
				if (_rtnCode != FEPReturnCode.Normal) {
					// 主機掛帳失敗需上信用卡主機進行預借現金解圏
					_rtnCode2 = hostCredit.sendToCredit(getATMtxData().getMsgCtl().getMsgctlAsctxid(),
							Byte.valueOf(CreditTxType.EC.getValue() + ""));
					return _rtnCode;
				}
			}
			return FEPReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "sendToCBS");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	// 更新跨行代收付
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtncode = FEPReturnCode.Normal;
		if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
			rtncode = getFiscBusiness().processAptot(false);
			return rtncode;
		}
		return rtncode;
	}

	private FEPReturnCode sendToConfirm() {
		T24 hostT24 = new T24(getATMtxData());
		Credit hostCredit = new Credit(getATMtxData());
		try {
			if (_rtnCode == FEPReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) {
					getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
					getFeptxn().setFeptxnReplyCode("    ");
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);// 成功
					// 轉帳交易直接送 Confirm 給財金(FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal)
					getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
					_rtnCode2 = getFiscBusiness().sendConfirmToFISC();
				} else {
					getFeptxn().setFeptxnReplyCode("    ");
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending);// PENDING
				}
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
				getFiscBusiness().updateTxData();
				sendToATM();

				// 寫入 ATM 清算資料
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmc())) {
					_rtnCode2 = getATMBusiness().insertATMC(1);
				}

				// 判斷是實體ATM，才更新鈔匣資料
				// 更新 ATM 鈔匣資料(含ATMCASH/ATMSTAT)
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAtmcash())
						&& FEPChannel.ATM.toString().equals(getFeptxn().getFeptxnChannel())) {
					_rtnCode2 = getATMBusiness().updateATMCash(1);
				}
			} else {
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())
						&& StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
					getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
					if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {// +REP
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse);// Accept-Reverse

						_rtnCode2 = getFiscBusiness().sendConfirmToFISC(); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
					} else {// -REP
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
					}

				} else {// fepReturnCode <> Normal
					getLogContext()
							.setMessage("getFeptxn().FEPTXN_REPLY_CODE before:" + getFeptxn().getFeptxnReplyCode());
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
						getLogContext().setProgramName(ProgramName);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						getLogContext()
								.setMessage("getFeptxn().FEPTXN_REPLY_CODE IN:" + getFeptxn().getFeptxnReplyCode());
						getLogContext().setProgramName(ProgramName);
						logMessage(Level.DEBUG, getLogContext());
					}
					getLogContext()
							.setMessage("getFeptxn().FEPTXN_REPLY_CODE after:" + getFeptxn().getFeptxnReplyCode());
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
				}
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
				getFiscBusiness().updateTxData();
				sendToATM();
				if (getFeptxn().getFeptxnAccType()!=null && getFeptxn().getFeptxnAccType() == 1) {// 已記帳
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnTroutKind())
							&& StringUtils.isNotBlank(getFeptxn().getFeptxnTroutKind())) {
						// Combo卡及信用卡進信用卡主機進行預借現金解圏
						_rtnCode2 = hostCredit.sendToCredit(getATMtxData().getMsgCtl().getMsgctlAsctxid(),
								Byte.valueOf(CreditTxType.EC.getValue() + "")); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal
					}
					// CBS主機沖正
					_rtnCode2 = hostT24.sendToT24(getATMtxData().getMsgCtl().getMsgctlTwcbstxid(),
							T24TxType.EC.getValue(), true); // FEPReturnCode2<>Normal仍要繼續執行，所以_rtnCode2沒判斷是否為Normal

				}
			}
			return FEPReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "sendToConfirm");
			sendEMS(getLogContext());
			return FEPReturnCode.ProgramException;
		}
	}

	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = null;

		if (_rtnCode != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		} else if (_rtnCode2 != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
		} else {
			getFeptxn().setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}

		getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

		// 豐掌櫃：以前端系統實際帶的Channel Code更新
		if (FEPChannel.NETBANK.name().equals(getFeptxn().getFeptxnChannel())) {
			//--ben-20220922-//getFeptxn().setFeptxnChannel(getATMRequest().getCHLCODE());
		}
		// 於交易失敗時, 回傳錯誤代碼及錯誤說明
		if (FEPChannel.NETBANK.toString().equals(getFeptxn().getFeptxnChannel())
				|| FEPChannel.MOBILBANK.toString().equals(getFeptxn().getFeptxnChannel())
//				|| FEPChannel.MMAB2C.toString().equals(getFeptxn().getFeptxnChannel())
				|| FEPChannel.IVR.toString().equals(getFeptxn().getFeptxnChannel())) {
			String rc = StringUtils.EMPTY;
			if (_rtnCode != FEPReturnCode.Normal) {
				rc = String.valueOf(_rtnCode.getValue());
			} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())
					&& !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				rc = getFeptxn().getFeptxnRepRc();
			}
			//ben20221118  
			/*
			if (StringUtils.isBlank(getATMtxData().getTxObject().getResponse().getRsStatDesc())
					&& StringUtils.isNotBlank(rc)) {
				try {
					List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(rc);
					if (CollectionUtils.isNotEmpty(msgfileList)) {
						getATMtxData().getTxObject().getResponse().setRsStatDesc(msgfileList.get(0).getMsgfileShortmsg().toString());
					}
				} catch (Exception ex) {
					getLogContext().setProgramException(ex);
					getLogContext().setProgramName(ProgramName + "." + "updateFEPTXN");
					sendEMS(getLogContext());
				}
			}
			*/
		}
		rtnCode = getFiscBusiness().updateTxData();
		if (rtnCode != FEPReturnCode.Normal) {
			return rtnCode;
		}
		return FEPReturnCode.Normal;
	}

	private void sendToMailSMS() {
		try {
			if (NormalRC.FISC_ATM_OK.equals(getATMBusiness().getFeptxn().getFeptxnRepRc())
					&& NormalRC.FISC_ATM_OK.equals(getATMBusiness().getFeptxn().getFeptxnConRc())) {
				if (ATMTXCD.IFT.name().equals(getATMBusiness().getFeptxn().getFeptxnTxCode())
						&& "FT".equals(getATMBusiness().getFeptxn().getFeptxnRsCode())) {
					getATMBusiness().prepareATMFTEMAIL();
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "sendToMailSMS");
			sendEMS(getLogContext());
		}
	}

	private FEPReturnCode checkRequestFromOtherChannel(ATMData txnData) {

		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;

		// (1) 檢核外圍 EJ 是否重覆
		rtnCode = getFiscBusiness().checkChannelEJFNO();
		if (rtnCode != FEPReturnCode.Normal) {
			return rtnCode;
		}

		if (!FEPChannel.FCS.toString().equals(getFeptxn().getFeptxnChannel())) {
			if (!getFeptxn().getFeptxnAtmod().equals(getFeptxn().getFeptxnTxnmode())) {
				rtnCode = FEPReturnCode.FISCBusinessDateChanged;
				return rtnCode;
			}
		}

		// (3) 檢核財金及參加單位之系統狀態
		rtnCode = getFiscBusiness().checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
		if (rtnCode != FEPReturnCode.Normal) {
			return rtnCode;
		}

		// 外圍 Channel 檢核交易連線狀態
		if (!getATMtxData().isTxStatus()) {
			rtnCode = FEPReturnCode.InterBankServiceStop;
			return rtnCode;
		}

		// (4) 檢核委託單位代號 或繳款類別
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnBusinessUnit())) {
			rtnCode = getFiscBusiness().checkNpsunit(getFeptxn());
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}
		} else if ((StringUtils.isNotBlank(getFeptxn().getFeptxnPaytype()))
				&& !"00000".equals(getFeptxn().getFeptxnPaytype())) {
			rtnCode = getFiscBusiness().checkPAYTYPE();
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}
		}

		// (5) 檢核本行信用卡只可執行轉帳/繳稅交易-2521,2532
		if (BINPROD.Debit.equals(getFeptxn().getFeptxnTroutKind())) {
			getLogContext().setRemark("轉出帳號為本行簽帳金融卡(DEBIT卡)");
			logMessage(Level.INFO, getLogContext());
			rtnCode = FEPReturnCode.CCardServiceNotAllowed; // CREDIT CARD SERVICE IS NOT ALLOWED
			return rtnCode;
		} else {
			if ((BINPROD.Credit.equals(getFeptxn().getFeptxnTroutKind())
					|| BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind()))
					&& !FISCPCode.PCode2521.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
				rtnCode = FEPReturnCode.CCardServiceNotAllowed; // CREDIT CARD SERVICE IS NOT ALLOWED
				return rtnCode;
			}
		}

		if (txnData.getMsgCtl().getMsgctlCheckLimit() != 0) {
			rtnCode = getATMBusiness().checkTransLimit(txnData.getMsgCtl());
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}
		}
		// (7) 檢核卡片狀況 (for ATM 交易)
		//--ben-20220922-//getLogContext().setRemark("ATMRequest.CHLCODE=" + getATMRequest().getCHLCODE());
		getLogContext().setProgramName(ProgramName);
		logMessage(Level.DEBUG, getLogContext());

		// Fly 2019/05/24 For 中文附言欄
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())) {
			if (getFeptxn().getFeptxnChrem().length() > 14) {
				rtnCode = FEPReturnCode.OtherCheckError;
				getLogContext().setRemark("CheckBody-中文附言欄不得超過14個中文字  CHREM:" + getFeptxn().getFeptxnChrem());
				logMessage(Level.INFO, getLogContext());
				return rtnCode;
			}

			RefString output = new RefString("");
			getFiscBusiness().convertFiscEncode(getFeptxn().getFeptxnChrem(), output);
			if (output.get().length() > 80) {
				rtnCode = FEPReturnCode.OtherCheckError;
				getLogContext().setRemark("CheckBody-中文摘要轉成CNS11643超過80位 " + output.get());
				logMessage(Level.INFO, getLogContext());
				return rtnCode;
			}
		}
		return rtnCode;
	}

	private FEPReturnCode sendToATM() {
		ATMAdapter oatmAdapter = new ATMAdapter(getATMtxData());
		@SuppressWarnings("unused")
		FEPReturnCode rtncode = null;
		try {
			//--ben-20220922-//oatmAdapter.setAtmNo(getATMRequest().getBRNO() + getATMRequest().getWSNO());
			if (StringUtils.isBlank(getATMtxData().getTxResponseMessage()) && needResponseMsg) {
				rtnMessage = prepareATMResponseData();
			} else {
				rtnMessage = getATMtxData().getTxResponseMessage();
			}

			// modify 判斷channel是否為ATM,WEBATM決定是否送MQ
			if (FEPChannel.ATM.toString().equals(getFeptxn().getFeptxnChannel())
					|| FEPChannel.WEBATM.toString().equals(getFeptxn().getFeptxnChannel())) {
				if (StringUtils.isNotBlank(rtnMessage)) {
					oatmAdapter.setMessageToATM(rtnMessage);
					rtncode = oatmAdapter.sendReceive();
				} else {
					// 若需要回ATM電文，但rtnMessage是空的表示有問題需alert
					getLogContext().setRemark("ATM組出來的回應電文為空字串");
					getLogContext().setProgramName(ProgramName);
					logMessage(Level.DEBUG, getLogContext());
				}
				// 因為先送給ATM了所以要將回應字串清成空字串
				rtnMessage = "";
			}

			return _rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + "." + "sendToATM");
			sendEMS(getLogContext());
		}
		return null;
	}
}
