package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
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
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.IntltxnExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.BinkeyMapper;
import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.model.Binkey;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.host.T24;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

/**
 * @author Jie
 */
@Deprecated
public class EMVCommonI extends INBKAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _strFISCRc = CommonReturnCode.Normal;
	private boolean isPlusCirrus = false;
	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	private Intltxn defINTLTXN = new Intltxn();
	private Intltxn oriINTLTXN = new Intltxn();
	private String _oWDCount;
	private boolean isEC = false;
	private IntltxnExtMapper dbINTLTXN = SpringBeanFactoryUtil.getBean(IntltxnExtMapper.class);

	public EMVCommonI(FISCData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		try {
			// 1.拆解並檢核財金電文
			_rtnCode = processRequestHeader();

			// 換日後該筆交易應重抓DBFepTxn在INSERT FEPTXN時才會寫入換日後的FEPTXNXX
			if (!SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8)
					.equals(this.feptxnDao.getTableNameSuffix())) {
				this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8),
						StringUtils.join(ProgramName, "processRequestData"));
				this.getTxData().setFeptxnDao(this.feptxnDao);
				getFiscBusiness().setFeptxnDao(this.feptxnDao);
			}

			// 2.AddTxData:新增交易記錄(FEPTXN & INTLTXN)
			// 2.1.Prepare 交易記錄初始資料
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().prepareFEPTXN_EMV();
			}
			if (isPlusCirrus) {
				if (_rtnCode == CommonReturnCode.Normal) {
					RefBase<Intltxn> intltxnRefBase = new RefBase<>(new Intltxn());
					RefBase<Intltxn> oriintltxnRefBase = new RefBase<>(new Intltxn());
					_rtnCode = getFiscBusiness().prepareIntltxnEMV(intltxnRefBase, oriintltxnRefBase,
							MessageFlow.Request);
					defINTLTXN = intltxnRefBase.get();
					oriINTLTXN = oriintltxnRefBase.get();
				}
				if (_rtnCode != CommonReturnCode.Normal) {
					if (_rtnCode == FISCReturnCode.OriginalMessageDataError) // MAPPING 欄位資料不符
					{
						_rtnCode = CommonReturnCode.Normal;
						_strFISCRc = FISCReturnCode.OriginalMessageDataError;
						getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // 無帳務沖正
					}
				}
			}

			// 2.2.新增交易記錄
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = addTxData();
			}

			if (_strFISCRc == CommonReturnCode.Normal) {
				_strFISCRc = _rtnCode;
			}

			if (_rtnCode == CommonReturnCode.Normal && _strFISCRc == CommonReturnCode.Normal) {
				// 3.商業邏輯檢核 & 電文Body檢核
				_strFISCRc = checkBusinessRule();

				// 4.帳務主機處理
				if (_strFISCRc == CommonReturnCode.Normal) {
					_rtnCode = sendToCBSAndAsc();
				}
			}

			// 6.PrepareFISC:準備回財金的相關資料
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = prepareForFISC();
			}

			// 7.UpdateTxData:更新交易記錄(FEPTXN & INTLTXN)
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = updateTxData();
			}

			// 8.ProcessAPTOT:更新跨行代收付
			// 為了避免Req電文還沒結束Con電文就進來，調整主流程順序先更新ProcessAPTOT再送財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = processAPTOT();
			}

			// 9.SendToFISC送回覆電文到財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Response);
			}

			// Fly 2018/03/26 國外提款超過當日累計限制次數, 發送簡訊訊息及EMAIL
			if ("2630".equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && "Y".equals(_oWDCount)) {
				getFiscBusiness().prepareOWDEMAIL(getFiscBusiness().getFeptxn().getFeptxnTxDate(),
						getFiscBusiness().getFeptxn().getFeptxnTroutActno());
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscEMVICRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		// 若回rtnCode給Handler，FISCGW會將此值回給財金，但此時AA已結束不需在回財金，故改成回空白
		return "";
	}

	// 拆解並檢核由財金發動的Request電文
	private FEPReturnCode processRequestHeader() {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 1.1.檢核財金電文 Header
		rtnCode = getFiscBusiness().checkHeader(getFiscEMVICReq(), true);

		// 判斷是否為Garbled Message
		if (TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext()).substring(0, 2).equals("10")) {
			getFiscBusiness().sendGarbledMessage(getFiscEMVICReq().getEj(), rtnCode, getFiscEMVICReq());
			return rtnCode;
		}

		// 1.2.判斷是否國際卡交易
		// for
		// 國際卡交易，避免OPC(3106)做2000-ExceptionCheckOut時，回的rtnCode=ReceiverBankServiceStop會被蓋掉的問題
		if (StringUtils.isNotBlank(getFiscEMVICReq().getOriData()) && rtnCode == CommonReturnCode.Normal) {
			rtnCode = getFiscBusiness().checkORI_DATA_EMV(MessageFlow.Request);
			isPlusCirrus = true;
		}

		if (rtnCode != CommonReturnCode.Normal) {
			_strFISCRc = rtnCode;
			return CommonReturnCode.Normal;
		}
		return FEPReturnCode.Normal;
	}

	// 新增交易記錄
	private FEPReturnCode addTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			rtnCode = getFiscBusiness().insertFEPTxn();
			if (rtnCode != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				return rtnCode;

			}
			if (isPlusCirrus) {
				if (dbINTLTXN.insertSelective(defINTLTXN) < 1) {
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}
			// Business屬性為Assign因此造成例外
			getFiscBusiness().setmINTLTXN(defINTLTXN);
			transactionManager.commit(txStatus);
			return rtnCode;
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".addTxData"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 商業邏輯檢核
	private FEPReturnCode checkBusinessRule() {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		// Tables.DBBINKEY dbBINKEY = new Tables.DBBINKEY(FEPConfig.DBName);

		try {
			if (SysStatus.getPropertyValue().getSysstatHbkno()
					.equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
				// 3.1 檢核提款金額及單筆限額
				// Fly 2018/04/09 移至轉換匯率後再檢核單筆限額
				// 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊
				// 3.2 檢核Combo國際卡交易之信用卡主機狀態
				if (StringUtils.isNotBlank(getFiscEMVICReq().getSyncPpkey())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//					if ((BINPROD.Combo.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind())
//							|| BINPROD.Debit.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind()))
//							&& !DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAsc())) // 因 Combo
//																									// 國際卡需進信用卡主機驗密,
//																									// 故需先檢核信用卡主機狀態
//					{
//						rtnCode = FISCReturnCode.StopServiceByInternal;
//						return rtnCode;
//					}
				}

				// 3.3 檢核 Card Status
				if (!FISCPCode.PCode2633.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
					rtnCode = getFiscBusiness().checkCardStatus();
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}

					// 2017/09/20 Modify by Ruling for 5801 拒絕EMV FallBack 交易
					getLogContext().setRemark("POS ENTRY MODE=" + defINTLTXN.getIntltxnPosMode());
					logMessage(Level.INFO, getLogContext());
					if (FISCPCode.PCode2630.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())
							|| FISCPCode.PCode2631.getValueStr()
									.equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) {
						if (StringUtils.isBlank(defINTLTXN.getIntltxnPosMode())
								|| defINTLTXN.getIntltxnPosMode().trim().length() < 4) {
							rtnCode = FISCReturnCode.MessageFormatError;
							getLogContext().setRemark("POS ENTRY MODE為NULL或空白或長度小於4位");
							logMessage(Level.INFO, getLogContext());
							return rtnCode;
						}
						if ("80".equals(defINTLTXN.getIntltxnPosMode().substring(1, 3))) {
							rtnCode = FISCReturnCode.RejectFallBack; // 4706:發卡行拒絕FallBack交易
							getLogContext().setRemark("EMV跨國提款交易(2630/2631)拒絕FallBack交易");
							logMessage(Level.INFO, getLogContext());
							return rtnCode;
						}
					}
				}

				// Fly 2018/03/13 for 跨國提款交易補強
				if (FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
					getLogContext().setRemark("Card.CARD_OWD_GP =" + getFiscBusiness().getCard().getCardOwdGp());
					logMessage(Level.INFO, getLogContext());
					if ("1".equals(getFiscBusiness().getCard().getCardOwdGp())) {
						// 2018/10/17 Modify by Ruling for 客服需求調整國外提款交易的錯誤訊息
						return FEPReturnCode.OWDGPClose;
						// Return FEPReturnCode.PlusCirrusNotApply
					} else if ("0".equals(getFiscBusiness().getCard().getCardOwdGp())) {
						String time = null;
						if (getFeptxn().getFeptxnTxDate().equals(getFiscBusiness().getCard().getCardOwdcloseDate())) {
							time = getFiscBusiness().getCard().getCardOwdcloseTime();
						} else {
							time = "000000";
						}
						rtnCode = getFiscBusiness().checkOWDCount(getFeptxn().getFeptxnTxDate(),
								getFeptxn().getFeptxnTroutActno(), time);
						if (rtnCode != CommonReturnCode.Normal) {
							if (rtnCode == FEPReturnCode.OverOWDCnt) {
								_oWDCount = "Y";
							}
							return rtnCode;
						}
					} else {
						// 不檢核跨國提款限制
					}
				}

				// 3.4 檢核 MAC 和 PINBLOCK
				if (StringUtils.isNotBlank(getFiscEMVICReq().getSyncPpkey())) {
					rtnCode = checkMACPINBLOCK();
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}

				// 3.5 檢核ARQC和產生ARPC
				if (StringUtils.isNotBlank(getFiscEMVICReq().getIcCheckdata())) {
					RefString arqc = new RefString("");
					RefString arc = new RefString("");
					RefString atc = new RefString("");
					RefString inputData = new RefString("");
					RefString arpc = new RefString("");
					RefString un = new RefString("");
					RefString newArpc = new RefString("");
					String tmpStr = "";

					String A = getFiscBusiness().check_IC_CHECKDATA("a","b");
					if (rtnCode == CommonReturnCode.Normal) {
						// 2016/06/24 Modify by Ruling for 依BIN 抓取不同基碼
						Binkey defBINKEY = new Binkey();
						defBINKEY.setBinkeyBino(getFiscBusiness().getFeptxn().getFeptxnTrk2().substring(0, 6));
						BinkeyMapper dbBINKEY = SpringBeanFactoryUtil.getBean(BinkeyMapper.class);
						Binkey queryBINKEY = dbBINKEY.selectByPrimaryKey(defBINKEY.getBinkeyBino());
						if (queryBINKEY == null) {
							rtnCode = IOReturnCode.QueryNoData;
							getLogContext().setRemark("找不到信用卡BIN基碼資料, BINKEY_BINO=" + defBINKEY.getBinkeyBino());
							logMessage(Level.INFO, getLogContext());
							return rtnCode;
						}

						rtnCode = encHelper.checkARQCAndMakeARPC(arqc.get(), arc.get(), atc.get(), un.get(),
								inputData.get(), queryBINKEY.getBinkeyKey().trim(), newArpc);
						getLogContext().setRemark("DES NewARPC=" + newArpc.get());
						logMessage(Level.INFO, getLogContext());
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						} else {
							// 2016/06/24 Modify by Ruling for ConvertFromAnyBaseString避免隱含轉換加ToString
							// 組ARPC(00LL91LLDATA3030)
							String newArpcString = newArpc.get() + arc.get();
							newArpcString = "91" + StringUtil.convertFromAnyBaseString(
									String.valueOf(newArpcString.length() / 2), 10, 16, 2) + newArpcString;
							tmpStr = StringUtil.convertFromAnyBaseString(
									String.valueOf((newArpcString.length() + 4) / 2), 10, 16, 4);
							getFiscEMVICRes().setIcCheckresult(tmpStr + newArpc);
						}
					} else {
						getLogContext().setRemark("CheckBusinessRule-拆解IC_CHECKDATA發生Exception");
						logMessage(Level.INFO, getLogContext());
						return rtnCode;
					}
				}

				// 3.6 檢核 MAC 及壓 MAC
				if (StringUtils.isBlank(getFiscEMVICReq().getMAC())) {
					getLogContext().setRemark("財金電文MAC為空白=" + getFiscEMVICReq().getMAC());
					logMessage(Level.INFO, getLogContext());
					return ENCReturnCode.ENCCheckMACError;
				} else {
					RefString mac = new RefString(getFiscEMVICRes().getMAC());
					rtnCode = encHelper.checkFISCMACAndMakeMAC(getFiscEMVICReq().getMAC(), mac);
					getFiscEMVICRes().setMAC(mac.get());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}
			}

			// 3.7 檢核&更新原始交易狀態 FOR 2633
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				rtnCode = checkoriFEPTXN();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			}

			// 3.8 檢核是否需要轉換匯率
			if (SysStatus.getPropertyValue().getSysstatHbkno()
					.equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) // 轉出交易
			{
				RefString tx_AMT_ACT = new RefString("0");
				RefString tx_EXRATE = new RefString("0");
				if (ZoneCode.TWN.equals(getFiscBusiness().getFeptxn().getFeptxnZoneCode())) // 台灣分行
				{
					if (isPlusCirrus) // 國際卡
					{
						if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
							rtnCode = getFiscBusiness().getExchangeAmount(
									getFiscBusiness().getFeptxn().getFeptxnZoneCode(),
									getFiscBusiness().getFeptxn().getFeptxnTxCurSet(),
									getFiscBusiness().getFeptxn().getFeptxnTxCurAct(),
									getFiscBusiness().getFeptxn().getFeptxnTxAmtSet(), tx_AMT_ACT, tx_EXRATE);
							if (rtnCode != CommonReturnCode.Normal) {
								return rtnCode;
							}
							getFiscBusiness().getFeptxn().setFeptxnTxAmtAct(new BigDecimal(tx_AMT_ACT.get()));
							getFiscBusiness().getFeptxn().setFeptxnExrate(new BigDecimal(tx_EXRATE.get()));
							defINTLTXN.setIntltxnTxCurAct(getFiscBusiness().getFeptxn().getFeptxnTxCurAct());
							defINTLTXN.setIntltxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
							defINTLTXN.setIntltxnExrate(getFiscBusiness().getFeptxn().getFeptxnExrate());
						}
					} else // 非國際卡
					{
						getFiscBusiness().getFeptxn().setFeptxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmt());
					}
				}
			} else // 轉入交易
			{
				getFiscBusiness().getFeptxn().setFeptxnTxCurAct(getFiscBusiness().getFeptxn().getFeptxnTxCur());
				getFiscBusiness().getFeptxn().setFeptxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmt());
			}

			// Fly 2018/04/09 EMV跨國提款,單筆提領限額為新台幣2萬元
			if (getTxData().getMsgCtl().getMsgctlCheckLimit() != 0) {
				rtnCode = getFiscBusiness().checkTransLimit(getTxData().getMsgCtl());
				if (rtnCode != CommonReturnCode.Normal) {
					getLogContext().setRemark("超過單筆限額");
					logMessage(Level.INFO, getLogContext());
					return rtnCode;
				}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 4.SendToCBS/ASC(if need): 帳務主機處理
	private FEPReturnCode sendToCBSAndAsc() {
		byte TxType = 0;
		boolean processTag = false;
		T24 hostT24 = new T24(getTxData());
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 若扣帳Timeout則不組回應電文給財金, 程式結束, 若主機回應扣帳失敗則仍需組回應電文給財金
			if (SysStatus.getPropertyValue().getSysstatHbkno()
					.equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) // 轉出交易
			{
				if (getTxData().getMsgCtl().getMsgctlCbsFlag() != 0) {
					TxType = getTxData().getMsgCtl().getMsgctlCbsFlag().byteValue();
					processTag = true; // 實際入扣帳
				} else {
					TxType = 1;
					processTag = false; // 轉入交易檢核帳號存在與否
				}
				getLogContext().setRemark("SendToCBS");
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());
				rtnCode = hostT24.sendToT24(getTxData().getMsgCtl().getMsgctlTwcbstxid(), TxType, processTag);
				if (rtnCode != CommonReturnCode.Normal) {
					if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnCbsRc())) {
						_strFISCRc = rtnCode;
						return CommonReturnCode.Normal;
					} else {
						getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
						getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal); // Reject-abnormal
						getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); // AA Close
						getFiscBusiness().updateTxData();
						// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
						if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
							oriDBFEPTXN.updateByPrimaryKey(getFiscBusiness().getOriginalFEPTxn());
							// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
						}
						return rtnCode;
					}
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBSAndAsc"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return FEPReturnCode.Normal;
	}

	// 6.PrepareFISC:準備回財金的相關資料
	private FEPReturnCode prepareForFISC() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		// 6.1.判斷 RC1,RC2,RC3
		if (_strFISCRc != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_strFISCRc.getValue());
			if (StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				getLogContext().setProgramName(ProgramName);
				getFiscBusiness().getFeptxn()
						.setFeptxnRepRc(TxHelper.getRCFromErrorCode(_strFISCRc, FEPChannel.FISC, getLogContext()));
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_ATM_OK);
		}

		// 6.2.產生 Response 電文Header:
		rtnCode = getFiscBusiness().prepareHeader("0210");
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		}

		// 6.3.產生 Response 電文Body:
		rtnCode = prepareBody();

		// 6.4.產生 MAC
		if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			// 修改 for 換KEY問題，移至下面押MAC
			if (SysStatus.getPropertyValue().getSysstatHbkno()
					.equals(getFiscBusiness().getFeptxn().getFeptxnTroutBkno())) {
				RefString mac = new RefString(getFiscEMVICRes().getMAC());
				rtnCode = encHelper.makeFiscMac(getFiscEMVICRes().getMessageType(), mac);
				getFiscEMVICRes().setMAC(mac.get());
				if (rtnCode != CommonReturnCode.Normal) {
					getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
					getFiscEMVICRes().setMAC("00000000");
				}
			}
		} else {
			RefString mac = new RefString(getFiscEMVICRes().getMAC());
			rtnCode = encHelper.makeFiscMac(getFiscEMVICRes().getMessageType(), mac);
			getFiscEMVICRes().setMAC(mac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscEMVICRes().setMAC("00000000");
			}
		}

		// 6.5.產生Bit Map
		rtnCode = getFiscBusiness().makeBitmap(getFiscEMVICRes().getMessageType(),
				getFiscEMVICRes().getProcessingCode(), MessageFlow.Response);
		if (rtnCode != CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
			getFiscEMVICRes().setBitMapConfiguration("0000000000000000");
		}

		rtnCode = getFiscEMVICRes().makeFISCMsg();
		return rtnCode;
	}

	// 7.UpdateTxData:更新交易記錄(FEPTXN、INTLTXN)
	private FEPReturnCode updateTxData() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			// 7.1.更新 FEPTXN
			// _dbINTLTXN = new Tables.DBINTLTXN(DBFepTxn.Database);
			// DBFepTxn.Database.BeginTransaction();
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
				if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) // (3 way)
				{
					getFiscBusiness().getFeptxn().setFeptxnPending((short) 1); // Pending
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); // Pending
				} else // (2 way)
				{
					if (!FISCPCode.PCode2633.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) // 非國際提款沖銷
					{
						getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
					} else {
						getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.ReverseSuccessed); // 已沖銷成功
					}
				}
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())) {
					if (!FISCPCode.PCode2633.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode())) // 非國際提款沖銷
					{
						isEC = false;
						getFiscBusiness().getFeptxn().setFeptxnClrType((short) 1);
					} else {
						isEC = true;
						getFiscBusiness().getFeptxn().setFeptxnClrType((short) 2);
					}
				}
			} else if (FeptxnTxrust.Initial.equals(getFiscBusiness().getFeptxn().getFeptxnTxrust())) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal); // 拒絕-正常
			}
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2-FISC Response
			getLogContext().setRemark(String.format("FEPTXN_AA_RC=%1$s FEPTXN_TXRUST=%2$s FEPTXN_REP_RC=%3$s",
					getFiscBusiness().getFeptxn().getFeptxnAaRc(), getFiscBusiness().getFeptxn().getFeptxnTxrust(),
					getFiscBusiness().getFeptxn().getFeptxnRepRc()));
			logMessage(Level.INFO, getLogContext());
			rtnCode = getFiscBusiness().updateTxData(); // 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2])
			if (rtnCode != CommonReturnCode.Normal) // 若更新失敗則不送回應電文, 人工處理
			{
				transactionManager.rollback(txStatus);
				return rtnCode;
			}

			// 7.2.判斷是否需更新 INTLTXN
			if (isPlusCirrus) {
				defINTLTXN.setIntltxnTxCurAct(getFiscBusiness().getFeptxn().getFeptxnTxCurAct());
				defINTLTXN.setIntltxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmtAct());
				defINTLTXN.setIntltxnExrate(getFiscBusiness().getFeptxn().getFeptxnExrate());
				defINTLTXN.setIntltxnBrno(getFiscBusiness().getFeptxn().getFeptxnBrno());
				defINTLTXN.setIntltxnZoneCode(getFiscBusiness().getFeptxn().getFeptxnZoneCode());
				defINTLTXN.setIntltxnRepRc(getFiscBusiness().getFeptxn().getFeptxnRepRc());
				defINTLTXN.setIntltxnTxrust(getFiscBusiness().getFeptxn().getFeptxnTxrust());
				defINTLTXN.setIntltxnTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno()); // for Combo卡
				if (dbINTLTXN.updateByPrimaryKeySelective(defINTLTXN) < 1) // 若更新失敗則不送回應電文, 人工處理
				{
					transactionManager.rollback(txStatus);
					return IOReturnCode.UpdateFail;
				}
			}

			// 7.3.判斷是否需更新原始交易 for 2633
			if (StringUtils.isNotBlank(getFiscBusiness().getFeptxn().getFeptxnOriStan())) {
				if (getFiscBusiness().getOriginalFEPTxn() != null) {
					oriDBFEPTXN.setTableNameSuffix(
							getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 8),
							StringUtils.join(ProgramName, "updateTxData"));
					if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
						getFiscBusiness().getOriginalFEPTxn()
								.setFeptxnTraceEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
						if (isPlusCirrus) // 國際卡交易需同時更新INTLTXN
						{
							oriINTLTXN.setIntltxnTxrust(FeptxnTxrust.ReverseSuccessed); // 已沖正成功
							if (dbINTLTXN.updateByPrimaryKeySelective(oriINTLTXN) < 1) // 若更新失敗則不送回應電文, 人工處理
							{
								transactionManager.rollback(txStatus);
								return IOReturnCode.UpdateFail;
							}
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust(FeptxnTxrust.ReverseSuccessed); // 已沖正成功
							getFiscBusiness().getOriginalFEPTxn().setFeptxnNeedSendCbs((short) 2); // 應沖正
							getFiscBusiness().getOriginalFEPTxn().setFeptxnAccType((short) 2); // CBS沖正成功
							getFiscBusiness().getOriginalFEPTxn().setFeptxnClrType((short) 2);
						}
						if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
							// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2]
							// 若更新失敗則不送回應電文, 人工處理
							// 若更新失敗則不送回應電文, 人工處理
							transactionManager.rollback(txStatus);
							return IOReturnCode.FEPTXNUpdateError;
						}
					} else // -REP
					{
						// 授權交易需先上主機解圏, 若解圏成功則 TXRUST =“C”
						// 所以若TXRUST =“T”進行中, 即可將原交易之狀態改回 Active
						if (FeptxnTxrust.Processing.equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) // 進行中for沖銷
						{
							getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 將原始交易之狀態改為Active
							if (oriDBFEPTXN.updateByPrimaryKeySelective(getFiscBusiness().getOriginalFEPTxn()) < 1) {
								// 檔名SEQ為 oriFEPTXN_TBSDY_FISC[7:2])
								// 若更新失敗則不送回應電文, 人工處理
								transactionManager.rollback(txStatus);
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

	// 8.更新跨行代收付
	private FEPReturnCode processAPTOT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlUpdateAptot())
				&& NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			rtnCode = getFiscBusiness().processAptot(isEC);
			if (rtnCode != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().updateTxData();
			}
		}
		return FEPReturnCode.Normal;
	}

	// 3.4 檢核 MAC 和 PINBLOCK
	private FEPReturnCode checkMACPINBLOCK() throws Exception {

		// Credit hostCreidt = new Credit(getTxData());
		// ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// 3.4.a 檢核 PP key 是否同步
		if (!SysStatus.getPropertyValue().getSysstatF3dessync().equals(getFiscEMVICReq().getSyncPpkey())) {
			rtnCode = FISCReturnCode.PPKeySyncError; // 0303-客戶亂碼基碼( PP-KEY )不同步
			return rtnCode;
		}

		// 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊
		// 3.4.b 檢核 MAC & PINBLOCK 及壓 MAC
		if (BINPROD.Combo.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind())
				|| BINPROD.Debit.equals(getFiscBusiness().getFeptxn().getFeptxnTroutKind())) {
			// 2020/11/09 Modify by Ruling for 多幣DEBIT卡：第一階段拒絕多幣Debit卡跨國提款交易
			if ("Y".equals(((FeptxnExt) getFiscBusiness().getFeptxn()).getFeptxnMulticur())) {
				getLogContext().setRemark("拒絕多幣Debit卡跨國提款交易");
				logMessage(Level.INFO, getLogContext());
				rtnCode = FISCReturnCode.StopServiceByInternal; // 0202-銀行內部停止該項跨行業務
				return rtnCode;
			}

			// COMBO卡需至信用卡主機驗密
			// 2021/10/09 by wj 注釋以下內容 依據Candy提供的“20211001-20211007-TEST.docx”
			// rtnCode = hostCreidt.sendToCredit(getTxData().getMsgCtl().getMsgctlAsctxid(), (byte) 1); // 需轉換 PINBLOCK &
			// // 壓MAC
			if (rtnCode != CommonReturnCode.Normal
					&& StringUtils.isBlank(getFiscBusiness().getFeptxn().getFeptxnAscRc())) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);// Reject-abnormal
				getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));// AA Close
				rtnCode = getFiscBusiness().updateTxData();
				// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
				return rtnCode;
			}
		} else {
			// 非COMBO卡不得執行EMV跨國提款及查詢交易
			rtnCode = FISCReturnCode.ReceiverBankServiceStop;
			return rtnCode; // 0205-收信單位該項跨行業務停止或暫停營業
		}

		return rtnCode;
	}

	// 3.6 檢核更新原始交易狀態
	private FEPReturnCode checkoriFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String I_TX_DATE = null;
		// QueryFEPTXNByStan:
		try {
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(getFiscBusiness().getFeptxn().getFeptxnOriStan());
			// 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 本營業日檔
			getFiscBusiness().setOriginalFEPTxn(oriDBFEPTXN.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(), getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				I_TX_DATE = getFiscBusiness().getFeptxn().getFeptxnDueDate(); // for 2633
				rtnCode = searchOriginalFEPTxn(I_TX_DATE, getFiscBusiness().getFeptxn().getFeptxnBkno(),
						getFiscBusiness().getFeptxn().getFeptxnOriStan());
				if (rtnCode != CommonReturnCode.Normal) {
					rtnCode = FISCReturnCode.TransactionNotFound; // 無此交易
					getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // 無帳務沖正
					getLogContext().setRemark(String.format("CheckoriFEPTXN-找不到原FEPTXN交易, FEPTXN_BKNO=%1$s FEPTXN_STAN=%2$s",
							getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno(), getFiscBusiness().getOriginalFEPTxn().getFeptxnStan()));
					logMessage(Level.DEBUG, getLogContext());
					return rtnCode;
				}
			}

			// 檢核原交易是否成功
			if (!FeptxnTxrust.Successed.equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())
					&& !FeptxnTxrust.Pending.equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxrust())) // 交易成功
			{
				rtnCode = FISCReturnCode.TransactionNotFound; // 無此交易
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.OriTxnRejected); // 原交易已拒絕
				getLogContext().setRemark("CheckoriFEPTXN-原FEPTXN_TXRUST <> A或B");
				logMessage(Level.DEBUG, getLogContext());
				return rtnCode;
			}

			// 檢核原交易之 MAPPING 欄位是否相同
			if (getFiscBusiness().getFeptxn().getFeptxnTxAmt().compareTo(getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt()) != 0
					|| !getFiscBusiness().getFeptxn().getFeptxnAtmno().trim().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim())
					|| (getFiscBusiness().getFeptxn().getFeptxnAtmType() != null && !getFiscBusiness().getFeptxn().getFeptxnAtmType().equals(getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType()))
					|| !getFiscBusiness().getOriginalFEPTxn().getFeptxnTrk2().trim().substring(0, 16).equals(getFiscBusiness().getFeptxn().getFeptxnMajorActno().trim())) {
				rtnCode = FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
				getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.NoAcctReverse); // 無帳務沖正
				getLogContext().setRemark("CheckoriFEPTXN-MAPPING原交易欄位不同," + " FEPTXN_TX_AMT="
						+ getFiscBusiness().getFeptxn().getFeptxnTxAmt() + " 原FEPTXN_TX_AMT="
						+ getFiscBusiness().getOriginalFEPTxn().getFeptxnTxAmt() + " FEPTXN_ATMNO="
						+ getFiscBusiness().getFeptxn().getFeptxnAtmno().trim() + " 原FEPTXN_ATMNO="
						+ getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmno().trim() + " FEPTXN_ATM_TYPE="
						+ getFiscBusiness().getFeptxn().getFeptxnAtmType() + " 原FEPTXN_ATM_TYPE="
						+ getFiscBusiness().getOriginalFEPTxn().getFeptxnAtmType() + " FEPTXN_MAJOR_ACTNO="
						+ getFiscBusiness().getFeptxn().getFeptxnMajorActno().trim() + " 原FEPTXN_TRK2="
						+ getFiscBusiness().getOriginalFEPTxn().getFeptxnTrk2().trim().substring(0, 16));
				logMessage(Level.DEBUG, getLogContext());
				return rtnCode;
			}
			oriDBFEPTXN.setTableNameSuffix(getFiscBusiness().getOriginalFEPTxn().getFeptxnTbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, "checkoriFEPTXN"));
			getFiscBusiness().getOriginalFEPTxn().setFeptxnTxrust(FeptxnTxrust.Processing); // 沖銷或授權完成進行中
			oriDBFEPTXN.updateByPrimaryKey(getFiscBusiness().getOriginalFEPTxn());
			getFiscBusiness().getFeptxn().setFeptxnTroutActno(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutActno());
			getFiscBusiness().getFeptxn().setFeptxnTroutKind(getFiscBusiness().getOriginalFEPTxn().getFeptxnTroutKind());
			getFiscBusiness().getFeptxn().setFeptxnDueDate(getFiscBusiness().getOriginalFEPTxn().getFeptxnReqDatetime().substring(0, 8));
			getFiscBusiness().getFeptxn().setFeptxnCbsRrn(getFiscBusiness().getOriginalFEPTxn().getFeptxnCbsRrn()); // 以便進行CBS沖正
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkoriFEPTXN"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return FEPReturnCode.Normal;
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
		// Tables.DBFEPTXN db = new Tables.DBFEPTXN(FEPConfig.DBName, 0,
		// SysStatus.PropertyValue.SYSSTAT_LBSDY_FISC.substring(6, 8));
		Bsdays aBSDAYS = new Bsdays();
		BsdaysMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
		String wk_TBSDY = null;
		String wk_NBSDY = "";
		try {
			db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 6 + 2),
					StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
			getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
			getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
			getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
			getFiscBusiness()
					.setOriginalFEPTxn(db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(),
							getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));

			if (getFiscBusiness().getOriginalFEPTxn() == null) {
				aBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
				aBSDAYS.setBsdaysDate(txDate);
				aBSDAYS = dbBSDAYS.selectByPrimaryKey(aBSDAYS.getBsdaysZoneCode(), aBSDAYS.getBsdaysDate());
				if (aBSDAYS == null) {
					return IOReturnCode.BSDAYSNotFound;
				}

				if (DbHelper.toBoolean(aBSDAYS.getBsdaysWorkday())) {// 工作日
					wk_TBSDY = aBSDAYS.getBsdaysDate();
					wk_NBSDY = aBSDAYS.getBsdaysNbsdy();
				} else {
					wk_TBSDY = aBSDAYS.getBsdaysNbsdy();
				}

				if (wk_TBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
					db.setTableNameSuffix(wk_TBSDY.substring(6, 6 + 2),
							StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
					getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
					getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
					getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
					getFiscBusiness().setOriginalFEPTxn(
							db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(),
									getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
					if (getFiscBusiness().getOriginalFEPTxn() == null) {
						if (StringUtils.isNotBlank(wk_NBSDY)
								&& wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
							db.setTableNameSuffix(wk_NBSDY.substring(6, 2),
									StringUtils.join(ProgramName, "searchOriginalFEPTxn"));
							getFiscBusiness().setOriginalFEPTxn(new FeptxnExt());
							getFiscBusiness().getOriginalFEPTxn().setFeptxnBkno(bkno);
							getFiscBusiness().getOriginalFEPTxn().setFeptxnStan(stan);
							getFiscBusiness().setOriginalFEPTxn(
									db.getFEPTXNByStanAndBkno(getFiscBusiness().getOriginalFEPTxn().getFeptxnStan(),
											getFiscBusiness().getOriginalFEPTxn().getFeptxnBkno()));
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

	private FEPReturnCode prepareBody() {
		String wk_BITMAP = null;
		try {
			if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) // +REP
			{
				// 跨行轉帳-轉入交易讀取第2組 Bit Map, 否則讀取第1組
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap1();
			} else // -REP
			{
				wk_BITMAP = getTxData().getMsgCtl().getMsgctlBitmap2();
			}

			// If Not String.IsNullOrWhiteSpace(fiscEMVICReq.IC_CHECKDATA) AndAlso
			// fiscBusiness.FepTxn.FEPTXN_PCODE <> CStr(FISCPCode.PCode2633) Then
			// wk_BITMAP = wk_BITMAP.Substring(0, 58) + 1 + wk_BITMAP.Substring(60, 3)
			// End If

			// 依據wk_BITMAP(判斷是否搬值)
			for (int IDX = 2; IDX <= 63; IDX++) {
				// Loop IDX from 3 to 64
				if (wk_BITMAP.charAt(IDX) == '1') {
					switch (IDX) {
						case 5: // 代付單位 CD/ATM 代號
							getFiscEMVICRes().setATMNO(getFiscBusiness().getFeptxn().getFeptxnAtmno());
							break;
						case 6: // 可用餘額
							// 財金可用餘額改送帳戶餘額(FEPTXN_BALB)
							getFiscEMVICRes().setBALA(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						case 23:
							getFiscEMVICRes().setNetworkCode(getFiscEMVICReq().getNetworkCode());
							break;
						case 35: // CD/ATM 國際化交易之原始資料
							getLogContext().setRemark(String.format("fiscEMVICReq.ORI_DATA=%1$s fiscEMVICRes.ORI_DATA=%2$s",
									getFiscEMVICReq().getOriData(), getFiscEMVICRes().getOriData()));
							logMessage(Level.INFO, getLogContext());
							getFiscEMVICRes().setOriData(getFiscEMVICReq().getOriData());
							break;
						case 37:
							getFiscEMVICRes().setBALB(getFiscBusiness().getFeptxn().getFeptxnBalb().toString());
							break;
						case 59:
							// 驗ARQC並產生ARPC(FN000501)時已將產生的ARPC塞入fiscEMVICRes.IC_CHECKRESULT
							break;
						case 61: // 授權碼
							String ej = StringUtils.leftPad(String.valueOf(getTxData().getEj()), 6, '0');
							getFiscEMVICRes().setAuthCode(ej.substring(ej.length() - 6, ej.length()));
							break;
					}
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return null;
	}
}
