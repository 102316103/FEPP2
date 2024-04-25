package com.syscom.fep.server.aa.va;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.mapper.VacateMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Vacate;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @author Jaime
 */
public class LEIssueRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode4 = FEPReturnCode.Normal;
	private String rtnMessage = "";
	private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
	private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
	private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
	private VacateMapper vacateMapper = SpringBeanFactoryUtil.getBean(VacateMapper.class);

	public LEIssueRequestA(NBData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		Vatxn vatxn = new Vatxn();
		try {
			getFiscBusiness().setmNBtxData(getnBData());
			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = tita.getBody().getRq().getHeader();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = tita.getBody().getRq().getSvcRq();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = tita.getBody().getRq().getSvcRq().getSENDDATA();
			// 1. 準備FEP交易記錄檔
			_rtnCode = getFiscBusiness().VAPrepareFEPTXN();

			// 2. 新增FEP交易記錄檔
			if (_rtnCode == FEPReturnCode.Normal) {
				addTxData();
			}

			// 3. 商業邏輯檢核(ATM電文)
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = checkBusinessRule();
			}

			// 4. Prepare約定及核驗交易(VATXN)記錄
//			if (!gotoLable && _rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) && StringUtils.isNotBlank(getFiscRes().getMEMO())) {
			if (_rtnCode == CommonReturnCode.Normal) {
				RefBase<Vatxn> vatxnRefBase = new RefBase<>(vatxn);
				prepareVATXNforVALE(vatxnRefBase, tita);
				vatxn = vatxnRefBase.get();
				vatxnMapper.insertSelective(vatxn);
			}

			// 5. SendToCBS/ASC(if need): 進帳務主機手續費分潤
			if (_rtnCode == FEPReturnCode.Normal) {
				if("03".equals(feptxn.getFeptxnNoticeId().substring(2, 4))) {
					feptxn.setFeptxnTxrust("S");
					String AATxTYPE = "3"; // 上CBS入扣帳
	        		String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
	        		feptxn.setFeptxnCbsTxCode(AA);
					this.getnBData().setVatxn(vatxn);
	        		ACBSAction aaObject = (ACBSAction) this.getInstanceObject(AA, getnBData());
	        		_rtnCode2 = new CBS(aaObject, getnBData()).sendToCBS(AATxTYPE);
//					vatxn = aaObject.getNBData().getVatxn();
	        		Object tota = aaObject.getTota();
				}
			}

			// 6. 	組送往 FISC 之 Request 電文並等待財金之 Response( if need)
			Npsunit npsunits = dbNPSUNIT.selectByPrimaryKey(senddata.getPAYUNTNO(), senddata.getTAXTYPE(), senddata.getPAYFEENO());

			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
			}

			// 7. 	CheckResponseFromFISC:檢核回應電文是否正確
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().checkResponseMessage();

			}

			// 8. ProcessAPTOT:更新跨行代收付
			if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())
					&& getnBData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
				_rtnCode2 = processAPTOT(false);
			}

			// 9. label_END_OF_FUNC : 判斷是否需組 CON 電文回財金
			_rtnCode2 = labelEndOfFunc();

			// 10. 	@新增約定及核驗交易(VATXN)記錄(if need)
			if(vatxn != null) {
				vatxn.setVatxnStan(feptxn.getFeptxnStan());
				vatxn.setVatxnConRc(getFeptxn().getFeptxnConRc());
				vatxn.setVatxnTxrust(getFeptxn().getFeptxnTxrust());
				vatxn.setVatxnReqRc(feptxn.getFeptxnReqRc());
				vatxn.setVatxnRepRc(feptxn.getFeptxnRepRc());

				if(StringUtils.isNotBlank(getFiscRes().getMEMO()) && "4001".equals(feptxn.getFeptxnRepRc())) {
					feptxn.setFeptxnTrk3(getFiscRes().getMEMO());
				}

				Vatxn po = vatxnMapper.selectByPrimaryKey(vatxn.getVatxnTxDate(), vatxn.getVatxnEjfno());
				vatxnMapper.updateByPrimaryKeySelective(vatxn);
			}

			// 11. 	更新交易記錄(FEPTXN)
			updateFEPTXN();

			// 12. 	組ATM回應電文 & 回 ATMMsgHandler
			rtnMessage = this.response(vatxn);

		} catch (Exception ex) {

			rtnMessage = "";
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());

		} finally {

			getnBData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getnBData().getLogContext().setMessage(rtnMessage);
			getnBData().getLogContext().setProgramName(this.aaName);
			getnBData().getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			getLogContext().setProgramName(this.aaName);
			logMessage(Level.DEBUG, getLogContext());

		}
		// 先組回應ATM 故最後return空字串
		return rtnMessage;
	}

	/**
	 * 2. AddTxData: 新增交易記錄( FEPTxn)
	 *
	 * @return
	 * @throws Exception
	 */
	private void addTxData() throws Exception {
		try {
			// 新增交易記錄(FEPTxn) Returning FEPReturnCode
			/* 檔名SEQ為 SYSSTAT_TBSDY_FISC[7:2] */
			String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
			feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
			int insertCount = feptxnDao.insertSelective(this.feptxn); // 新增資料
			if (insertCount <= 0) { // 新增失敗
				_rtnCode = FEPReturnCode.FEPTXNInsertError;
			}
		} catch (Exception ex) { // 新增失敗
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".addTxData");
			sendEMS(getLogContext());
			_rtnCode = FEPReturnCode.FEPTXNInsertError;
		}
	}

	/**
	 * 4. Prepare約定及核驗交易(VATXN)記錄
	 * @param vatxn
	 * @param tita
	 * @return
	 */
	public FEPReturnCode prepareVATXNforVALE(RefBase<Vatxn> vatxn, RCV_VA_GeneralTrans_RQ tita) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            vatxn.get().setVatxnTxDate(getFeptxn().getFeptxnTxDate());
            vatxn.get().setVatxnEjfno(getFeptxn().getFeptxnEjfno());
            vatxn.get().setVatxnBkno(getFeptxn().getFeptxnBkno());
            vatxn.get().setVatxnPcode(getFeptxn().getFeptxnPcode());
            vatxn.get().setVatxnTxTime(getFeptxn().getFeptxnTxTime());
//            vatxn.get().setVatxnStan(getFeptxn().getFeptxnStan());
            vatxn.get().setVatxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            vatxn.get().setVatxnReqRc(getFeptxn().getFeptxnReqRc());
            vatxn.get().setVatxnRepRc(getFeptxn().getFeptxnRepRc());
            vatxn.get().setVatxnConRc(getFeptxn().getFeptxnConRc());
            vatxn.get().setVatxnTxrust(getFeptxn().getFeptxnTxrust());
            String notice12 = feptxn.getFeptxnNoticeId().substring(0, 2);//'業務類別代號’
            String notice34 = feptxn.getFeptxnNoticeId().substring(2, 4);//‘交易類別’
            vatxn.get().setVatxnCate(notice12);
            vatxn.get().setVatxnType(notice34);
            vatxn.get().setVatxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            vatxn.get().setVatxnTroutActno(getFeptxn().getFeptxnTroutActno());
            vatxn.get().setVatxnTroutKind(null == getFeptxn().getFeptxnTroutKind() ? StringUtils.SPACE : getFeptxn().getFeptxnTroutKind());
            vatxn.get().setVatxnBrno(getFeptxn().getFeptxnBrno());
            vatxn.get().setVatxnZoneCode(getFeptxn().getFeptxnZoneCode());

            vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
            vatxn.get().setVatxnBusino(tita.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI());
            vatxn.get().setVatxnBusinessUnit(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO());
            vatxn.get().setVatxnPaytype(tita.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE());
            vatxn.get().setVatxnFeeno(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());
            vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
            vatxn.get().setVatxnPactno(tita.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYAC2());
            vatxn.get().setVatxnActno(getFeptxn().getFeptxnTroutActno());

            vatxn.get().setUpdateUserid(0);
            vatxn.get().setUpdateTime(new Date());


            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareVATXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

	/**
	 * 9.  label_END_OF_FUNC : 判斷是否需組 CON 電文回財金
	 * @return
	 */
	private FEPReturnCode labelEndOfFunc() {
		// T24 hostT24 = new T24(getnBData());
		// Credit hostCredit = new Credit(getnBData());
		try {
			if (_rtnCode == CommonReturnCode.Normal && _rtnCode2 == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
				if (DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlAtm2way())) {
					getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
					getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
					getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
					_rtnCode4 = getFiscBusiness().sendConfirmToFISC();
				}
			} else {
				getLogContext().setProgramName(ProgramName);
				// 交易失敗
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
					getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
					if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { // +REP
						if (!DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlAtm2way())) { /*3WAY*/

							if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
								feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota,ImsMethodName.IMSRC4_FISC.getValue()));
							}else {
								feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(_rtnCode.getValue()),
										FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
							}
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getnBData().getTxChannel(), getLogContext()));
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
							_rtnCode4 = getFiscBusiness().sendConfirmToFISC();
						} else {
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getnBData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						}
					} else { // -REP
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getnBData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
					}
				} else { // fepReturnCode <> Normal
					getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
					if(StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
					}else {
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					}

					if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getnBData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
					}
					getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE after:", getFeptxn().getFeptxnReplyCode()));
					logMessage(Level.DEBUG, getLogContext());
				}
				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
				getFiscBusiness().updateTxData();
				sendToATM();
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToConfirm"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 3. 商業邏輯檢核(ATM電文)
	 *
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		feptxn.setFeptxnIcSeqno(StringUtils.EMPTY);
		feptxn.setFeptxnIcmark(StringUtils.EMPTY);
		try {
			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = tita.getBody().getRq().getHeader();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = tita.getBody().getRq().getSvcRq();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = tita.getBody().getRq().getSvcRq().getSENDDATA();

			// 檢核外圍 EJ 是否重覆
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}


			// 3.2 檢核財金及參加單位之系統狀態
			rtnCode = getFiscBusiness().checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

			/* MsgHandler 檢核交易連線狀態 */
			if (!this.getnBData().isTxStatus()) {
				rtnCode = CommonReturnCode.InterBankServiceStop;
				return rtnCode;
			}

			// 	檢核跨行金融帳戶資訊核驗電文
			if("10".equals(nbbody.getVACATE())) {
				if(StringUtils.isNotBlank(nbbody.getAEIPYBK()) || StringUtils.isNotBlank(nbbody.getAEIPCRBK())) {
					return FEPReturnCode.OtherCheckError;
				}
			}

			// 檢核前端電文
			if("02".equals(nbbody.getVACATE())){
				if(StringUtils.isBlank(nbbody.getTAXIDNO())
						&& StringUtils.isBlank(nbbody.getMOBILENO())
						&& StringUtils.isBlank(senddata.getPAYUNTNO())
						&& StringUtils.isBlank(senddata.getTAXTYPE())
						&& StringUtils.isBlank(senddata.getPAYFEENO())
						&& StringUtils.isBlank(senddata.getCLCPYCI())
						&& StringUtils.isBlank(senddata.getAEIPYAC2())
				) {
					rtnCode = FEPReturnCode.OtherCheckError;/* 其他類檢核錯誤 */
				}
			}

			if(nbbody.getMOBILENO().length() < 10) {
				return FEPReturnCode.OtherCheckError;
			}
			if(StringUtils.isBlank(nbbody.getVACATE()) ||StringUtils.isBlank(nbbody.getAEIPYTP())) {
				return FEPReturnCode.OtherCheckError;
			}

//			3.3	檢核委託單位檔
			Vacate vacate = vacateMapper.selectByPrimaryKey(tita.getBody().getRq().getSvcRq().getVACATE());
			if(vacate == null) {
				return FEPReturnCode.VACATENONotFound;   /* 約定及核驗服務業務類別不存在 */
			}

			if("02".equals(tita.getBody().getRq().getSvcRq().getVACATE())) {
				Npsunit npsunits = dbNPSUNIT.selectByPrimaryKey(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), tita.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE(), tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());
				if(npsunits == null) {
					return FEPReturnCode.NPSUNITNotFound;
				}

				if("00".equals(tita.getBody().getRq().getSvcRq().getAEIPYTP()) || "01".equals(tita.getBody().getRq().getSvcRq().getAEIPYTP())) {
					/* 交易類別 若為 00 (約定)、01(撤銷) , 「約定授理單位」需為 1,2 */
					if(npsunits.getNpsunitUnit() != 1 && npsunits.getNpsunitUnit() != 2) {
						return FEPReturnCode.OtherCheckError;
						/* 交易類別 若為 03 (撤銷通知) , 「約定授理單位」需為 1,3 */
					}
				}else if(npsunits.getNpsunitUnit() != 1 && npsunits.getNpsunitUnit() != 3) {
					return FEPReturnCode.OtherCheckError;
				}else {
					getFeptxn().setFeptxnTroutBkno(npsunits.getNpsunitBkno().substring(0,3));
					getFeptxn().setFeptxnTroutBkno7(npsunits.getNpsunitBkno());
				}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkBusinessRule"));
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 11. 	更新交易記錄(FEPTXN)
	 *
	 * @return
	 */
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtncode = FEPReturnCode.Normal;

		feptxn.setFeptxnMsgflow("A2");
		if (_rtnCode != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		} else if (_rtnCode2 != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
		}  else {
			getFeptxn().setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}
		getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

		rtncode = getFiscBusiness().updateTxData();
		if (rtncode != FEPReturnCode.Normal) {
			return rtncode;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 *  組回應電文
	 *
	 * @return
	 */
	private FEPReturnCode sendToATM() {
		try {
			// 先送給ATM主機
			boolean needResponseMsg = true;
			if (StringUtils.isBlank(getnBData().getTxResponseMessage()) && needResponseMsg) {
				this.rtnMessage = prepareATMResponseData();
			} else {
				this.rtnMessage = getnBData().getTxResponseMessage();
			}
			return _rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToATM"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 8. ProcessAPTOT:更新跨行代收付
	 *
	 * @return
	 */
	private FEPReturnCode processAPTOT(boolean isEC) {
		FEPReturnCode rtncode = FEPReturnCode.Normal;
		if (DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlUpdateAptot())) {
			rtncode = getFiscBusiness().processAptot(isEC);
			return rtncode;
		}
		return rtncode;
	}

	/**
	 * 12. 	組ATM回應電文 & 回 ATMMsgHandler
	 * @param vatxn
	 * @return
	 * @throws Exception
	 */
	private String response(Vatxn vatxn) throws Exception {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
//			ATMGeneralRequest tita = this.getATMRequest();
			String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
					FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
			String feptxnTxCode = feptxn.getFeptxnTxCode();
			ATMENCHelper atmEncHelper = new ATMENCHelper(this.getnBData());
			RefString rfs = new RefString();
			String totaToact;

			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

			header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
			header.setCHANNEL(feptxn.getFeptxnChannel());
			header.setMSGID(header.getMSGID());
			header.setCLIENTDT(tita.getBody().getRq().getHeader().getCLIENTDT());
			header.setSYSTEMID("FEP");

			if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) &&  !"000".equals(feptxn.getFeptxnCbsRc())) {
				header.setSYSTEMID("ATM");
				header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
				body.setRSPRESULT(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));
				header.setSEVERITY("ERROR");
			}else if(!NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc())) {
				header.setSYSTEMID("ATM");
				header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnRepRc()));
				header.setSEVERITY("ERROR");
			}else if(StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
				header.setSYSTEMID("FEP");
				header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnReplyCode()));
				header.setSEVERITY("ERROR");
			} else {
				header.setSYSTEMID("ATM");
				header.setSTATUSCODE(NormalRC.FISC_ATM_OK);
				header.setSTATUSDESC("");
				header.setSEVERITY("INFO");
			}
			if(StringUtils.isNotBlank(feptxn.getFeptxnErrMsg()) && "ERROR".equals(header.getSEVERITY())) {
				header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
			}
			body.setOUTDATE(feptxn.getFeptxnTxDate());
			body.setOUTTIME(feptxn.getFeptxnTxTime());
			body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
			body.setTXNSTAN(feptxn.getFeptxnStan());
			body.setCUSTOMERID(feptxn.getFeptxnIdno());
			body.setTXNTYPE(tita.getBody().getRq().getSvcRq().getTXNTYPE());
			body.setFSCODE(feptxn.getFeptxnTxCode());
			body.setTRANSAMT(new BigDecimal(String.valueOf(tita.getBody().getRq().getSvcRq().getTRANSAMT())));
			body.setAEIPYTP(tita.getBody().getRq().getSvcRq().getAEIPYTP());
			body.setTAXIDNO(vatxn.getVatxnIdno());
			body.setAEIPCRBK(tita.getBody().getRq().getSvcRq().getAEIPCRBK());
			body.setCLACTNO(feptxn.getFeptxnTroutActno());
			replydata.setPAYUNTNO(vatxn.getVatxnBusinessUnit());
			replydata.setTAXTYPE(vatxn.getVatxnPaytype());
			replydata.setPAYFEENO(vatxn.getVatxnFeeno());

			SEND_VA_GeneralTrans_RS va_rs = new SEND_VA_GeneralTrans_RS();
			va_rs.setBody(new SEND_VA_GeneralTrans_RS_Body());
			va_rs.getBody().setRs(new SEND_VA_GeneralTrans_RS_Body_MsgRs());
			va_rs.getBody().getRs().setHeader(header);
			va_rs.getBody().getRs().setSvcRs(body);
			body.setREPLYDATA(replydata);

			rtnMessage =XmlUtil.toXML(va_rs);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}
}
