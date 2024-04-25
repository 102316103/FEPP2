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
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Msgfile;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Jaime
 */
public class LFIssueRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;

	private FEPReturnCode _rtnCode3 =FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode4 = FEPReturnCode.Normal;
	private String rtnMessage = "";
	private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
	private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
	private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

	public LFIssueRequestA(NBData txnData) throws Exception {
		super(txnData);
	}

	@Override
	public String processRequestData() {
		Vatxn vatxn = new Vatxn();
		try {
			getFiscBusiness().setmNBtxData(getnBData());
			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
			boolean gotoLable = false;
			// 1. 準備FEP交易記錄檔
			_rtnCode = getFiscBusiness().VAPrepareFEPTXN();

			// 2. 新增FEP交易記錄檔
			if (_rtnCode == FEPReturnCode.Normal) {
				addTxData();
			}else {
				gotoLable = true;
			}

			// 3. 商業邏輯檢核(ATM電文)
			if (!gotoLable && _rtnCode == FEPReturnCode.Normal) {
				_rtnCode = checkBusinessRule();
			}else {
				gotoLable = true;
			}

			// 4. Prepare約定及核驗交易(VATXN)記錄
			if (_rtnCode == CommonReturnCode.Normal) {
				RefBase<Vatxn> vatxnRefBase = new RefBase<>(vatxn);
				prepareVATXNforVALF(vatxnRefBase, tita);
				vatxn = vatxnRefBase.get();
				vatxnMapper.insertSelective(vatxn);
			}

			// 5. 	組送往 FISC 之 Request 電文並等待財金之 Response( if need)
			if (!gotoLable && _rtnCode == FEPReturnCode.Normal) {
	    		_rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
			}

			// 6. 	CheckResponseFromFISC:檢核回應電文是否正確
			if (!gotoLable && _rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().checkResponseMessage();

			}else {
				gotoLable = true;
			}

			// 7. ProcessAPTOT:更新跨行代收付
//			if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
			if (!gotoLable && _rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())
					&& getnBData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
				_rtnCode2 = processAPTOT(false);
			}else {
				gotoLable = true;
			}

			if(_rtnCode2 != CommonReturnCode.Normal) {
				gotoLable = true;
			}

			// 8. 	SendToCBS/ASC(if need): 進帳務主機手續費分潤
			if(!gotoLable && StringUtils.isBlank(tita.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYUES())) { /*FIDO 不用上主機*/
	        		String AATxTYPE = "3"; // 上CBS入扣帳
	        		String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
	        		feptxn.setFeptxnCbsTxCode(AA);
					this.getnBData().setVatxn(vatxn);
	        		ACBSAction aaObject = (ACBSAction) this.getInstanceObject(AA, getnBData());
	        		_rtnCode2 = new CBS(aaObject, getnBData()).sendToCBS(AATxTYPE);
	        		tota = aaObject.getTota();
	        		if(feptxn.getFeptxnCbsTimeout() == 1) {
	        			_rtnCode2 = FEPReturnCode.Normal;
	        		}else {
	        			if(_rtnCode2 != FEPReturnCode.Normal) {
	        				if(this.getTxData().getMsgCtl().getMsgctlUpdateAptot() == 1 && _rtnCode3 == FEPReturnCode.Normal) {
								/* 沖回跨行代收付(APTOT) */
								_rtnCode3 = this.getFiscBusiness().processAptot(true);
	        				}
	        				gotoLable = false;
	        			}
	        		}
			}
			if(!gotoLable&&_rtnCode2!=CommonReturnCode.Normal) {
				_rtnCode3=getFiscBusiness().processAptot(true);
			}

			// 9. 	label_END_OF_FUNC : 判斷組 CON 電文回財金
			_rtnCode2 = labelEndOfFunc();

			// 10. 	@新增約定及核驗交易(VATXN)記錄(if need)
//			if (_rtnCode == FEPReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				if(vatxn != null) {
					vatxn.setVatxnStan(feptxn.getFeptxnStan());
					vatxn.setVatxnConRc(getFeptxn().getFeptxnConRc());
					vatxn.setVatxnTxrust(getFeptxn().getFeptxnTxrust());
					vatxn.setVatxnReqRc(feptxn.getFeptxnReqRc());
					vatxn.setVatxnRepRc(feptxn.getFeptxnRepRc());

					if(StringUtils.isNotBlank(getFiscRes().getMEMO())) {
						feptxn.setFeptxnTrk3(getFiscRes().getMEMO());
						if("10".equals(vatxn.getVatxnCate())) {
							vatxn.setVatxnResult(getFiscRes().getMEMO().substring(90,92));
							vatxn.setVatxnAcresult(getFiscRes().getMEMO().substring(92,94));
							vatxn.setVatxnAcstat(getFiscRes().getMEMO().substring(94,96));
							vatxn.setVatxnTelresult(getFiscRes().getMEMO().substring(96,97));
						    /* 將財金回應結果寫入 */
							feptxn.setFeptxnRemark(vatxn.getVatxnResult()+vatxn.getVatxnAcresult()+vatxn.getVatxnAcstat()+vatxn.getVatxnTelresult());
//							getFiscRes().setREMARK(vatxn.getVatxnResult()+vatxn.getVatxnAcresult()+vatxn.getVatxnAcstat());

						}
					}

					Vatxn po = vatxnMapper.selectByPrimaryKey(vatxn.getVatxnTxDate(), vatxn.getVatxnEjfno());
					vatxnMapper.updateByPrimaryKeySelective(vatxn);
				}

//			}

			// 11. 	更新交易記錄(FEPTXN)
			updateFEPTXN();

			// 12. 	組ATM回應電文 & 回 ATMMsgHandler
			this.response(vatxn);

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
	public FEPReturnCode prepareVATXNforVALF(RefBase<Vatxn> vatxn, RCV_VA_GeneralTrans_RQ tita) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            vatxn.get().setVatxnTxDate(getFeptxn().getFeptxnTxDate());
            vatxn.get().setVatxnEjfno(getFeptxn().getFeptxnEjfno());
            vatxn.get().setVatxnBkno(getFeptxn().getFeptxnBkno());
            vatxn.get().setVatxnPcode(getFeptxn().getFeptxnPcode());
            vatxn.get().setVatxnTxTime(getFeptxn().getFeptxnTxTime());
            vatxn.get().setVatxnStan(getFeptxn().getFeptxnStan());
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

            // TODO: 2021/6/22
            vatxn.get().setUpdateUserid(0);
            vatxn.get().setUpdateTime(new Date());

            vatxn.get().setVatxnItem(tita.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP());
            // Fly 2019/01/03 將財金回應結果寫入
            switch (vatxn.get().getVatxnItem()) {
                case "00": //'除卡片及帳號外，無其他核驗項目
                    // 代財金回覆4001 異動
//                    getFeptxn().setFeptxnTrk3(getFiscReq().getMEMO());
                case "01": // 身份證號或外國人統一編號
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    break;
                case "02": // 持卡人之行動電話號碼
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    break;
                case "03": // 持卡人之出生年月日
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    break;
                case "04": // 持卡人之住家電話號碼
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "11": // 持卡人之身分證號及行動電話號碼
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    break;
                case "12": // 持卡人之身分證號、行動電話號碼及出生年月
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    break;
                case "13": // 持卡人之身分證號、行動電話號碼及住家電話
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
                case "14": // 持卡人之身分證號、行動電話號碼、出生年月日及住家電話號碼
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                    vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                    break;
				case "15": // 持卡人之身份證號或外國人統一編號
					vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
            }

			vatxn.get().setVatxnUse(tita.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYUES());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareVATXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

	/**
	 * 9. 	label_END_OF_FUNC : 判斷組 CON 電文回財金
	 * @return
	 */
	private FEPReturnCode labelEndOfFunc() {
		// T24 hostT24 = new T24(getATMtxData());
		// Credit hostCredit = new Credit(getATMtxData());
		try {
			if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) && _rtnCode2 == CommonReturnCode.Normal ) { /*+REP*/
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
							try {
								List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(_rtnCode.getValue()));
								if (CollectionUtils.isNotEmpty(msgfileList)) {
									feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
								}
							} catch (Exception ex) {
							getLogContext().setProgramException(ex);
							getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateFEPTXN"));
							sendEMS(getLogContext());
							}
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
							_rtnCode4 = getFiscBusiness().sendConfirmToFISC();
						} else {
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getnBData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
							try {
								List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(_rtnCode.getValue()));
								if (CollectionUtils.isNotEmpty(msgfileList)) {
									feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
								}
							} catch (Exception ex) {
								getLogContext().setProgramException(ex);
								getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateFEPTXN"));
								sendEMS(getLogContext());
							}
						}
					} else { // -REP
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getnBData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						try {
							List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(_rtnCode.getValue()));
							if (CollectionUtils.isNotEmpty(msgfileList)) {
								feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
							}
						} catch (Exception ex) {
							getLogContext().setProgramException(ex);
							getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateFEPTXN"));
							sendEMS(getLogContext());
						}
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
					try {
						List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(String.valueOf(_rtnCode.getValue()));
						if (CollectionUtils.isNotEmpty(msgfileList)) {
							feptxn.setFeptxnErrMsg(msgfileList.get(0).getMsgfileShortmsg());
						}
					} catch (Exception ex) {
						getLogContext().setProgramException(ex);
						getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateFEPTXN"));
						sendEMS(getLogContext());
					}
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
		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
		feptxn.setFeptxnIcSeqno(StringUtils.EMPTY);
		feptxn.setFeptxnIcmark(StringUtils.EMPTY);
		try {
//			RCV_HCE_GeneralTrans_RQ atmReq = this.getnBData().getTxHceObject().getRequest();
			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = tita.getBody().getRq().getHeader();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = tita.getBody().getRq().getSvcRq();
			RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = tita.getBody().getRq().getSvcRq().getSENDDATA();

			// 檢核外圍 EJ 是否重覆
			rtnCode = getFiscBusiness().checkChannelEJFNO();
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}


			// 3.1 檢核財金及參加單位之系統狀態
			rtnCode = getFiscBusiness().checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

			/* MsgHandler 檢核交易連線狀態 */
			if (!this.getnBData().isTxStatus()) {
				rtnCode = CommonReturnCode.InterBankServiceStop;
				return rtnCode;
			}

			// 3.2	檢核跨行金融帳戶資訊核驗電文
			if(nbbody.getSENDDATA().getAEIPYUES().equals("01")){ /*FIDO*/
				if(!nbbody.getAEIPYTP().equals("00") || !nbbody.getSENDDATA().getAELFTP().equals("01")){
					return FEPReturnCode.OtherCheckError;
				}
			}

			/*晶片卡核驗*/
			if(nbbody.getAEIPYTP().equals("00") && !nbbody.getSENDDATA().getAELFTP().equals("00") && !nbbody.getSENDDATA().getAELFTP().equals("01")
					&& !nbbody.getSENDDATA().getAELFTP().equals("02") && !nbbody.getSENDDATA().getAELFTP().equals("03") && !nbbody.getSENDDATA().getAELFTP().equals("04")){
				return FEPReturnCode.OtherCheckError;
			}

			/*無卡核驗*/
			if(!nbbody.getAEIPYTP().equals("00") && !nbbody.getSENDDATA().getAELFTP().equals("11") && !nbbody.getSENDDATA().getAELFTP().equals("12")
					&& !nbbody.getSENDDATA().getAELFTP().equals("13") && !nbbody.getSENDDATA().getAELFTP().equals("14") && !nbbody.getSENDDATA().getAELFTP().equals("15")){
				return FEPReturnCode.OtherCheckError;
			}

			if(StringUtils.isBlank(nbbody.getVACATE()) ||StringUtils.isBlank(nbbody.getAEIPYTP()) ||StringUtils.isBlank(nbbody.getSENDDATA().getAELFTP())) {
				return FEPReturnCode.OtherCheckError;
			}

			/* 檢核前端電文 */
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

//			3.3	檢核委託單位檔
			if(StringUtils.isBlank(tita.getBody().getRq().getSvcRq().getVACATE())) {
				return FEPReturnCode.VACATENONotFound;   /* 約定及核驗服務業務類別不存在 */
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

		// Fly 2019/09/17 VAA2566允許其他Channel進來
		//--ben-20220922-//getFeptxn().setFeptxnChannel(getATMRequest().getCHLCODE());
//
//		String rc = StringUtils.EMPTY;
//		if (_rtnCode != FEPReturnCode.Normal) {
//			rc = String.valueOf(_rtnCode.getValue());
//		} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
//			rc = getFeptxn().getFeptxnRepRc();
//		}

		//ben20221118
		/*
		if (StringUtils.isBlank(getATMtxData().getTxObject().getResponse().getRsStatDesc()) && StringUtils.isNotBlank(rc)) {
			try {
				List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(rc);
				if (CollectionUtils.isNotEmpty(msgfileList)) {
					getATMtxData().getTxObject().getResponse().setRsStatDesc(msgfileList.get(0).getMsgfileShortmsg());
				}
			} catch (Exception ex) {
				getLogContext().setProgramException(ex);
				getLogContext().setProgramName(StringUtils.join(ProgramName, ".updateFEPTXN"));
				sendEMS(getLogContext());
			}
		}
		*/
		rtncode = getFiscBusiness().updateTxData();
		if (rtncode != FEPReturnCode.Normal) {
			return rtncode;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 9. 組回應電文
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
	 * 7. 更新跨行代收付
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
		try {
			/* 組 ATM Response OUT-TEXT */
//			ATMGeneralRequest tita = this.getATMRequest();
			String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
					FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
			String feptxnTxCode = feptxn.getFeptxnTxCode();
			ATMENCHelper atmEncHelper = new ATMENCHelper(this.getnBData());
			RefString rfs = new RefString();

			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

			header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
			header.setCHANNEL(feptxn.getFeptxnChannel());
			header.setMSGID(header.getMSGID());
			header.setCLIENTDT(tita.getBody().getRq().getHeader().getCLIENTDT());

			if(!"000".equals(feptxn.getFeptxnCbsRc())) {
				header.setSYSTEMID("ATM");
				header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
				body.setRSPRESULT(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));
				header.setSEVERITY("ERROR");
			} else if(!NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc())) {
				header.setSYSTEMID("ATM");
				header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnRepRc()));
				header.setSEVERITY("ERROR");
			} else if(StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
				header.setSYSTEMID("FEP");
				header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnReplyCode()));
				header.setSEVERITY("ERROR");
			} else {
				header.setSYSTEMID("ATM");
				header.setSTATUSCODE(NormalRC.FISC_ATM_OK);
				header.setSEVERITY("INFO");
				header.setSTATUSDESC("");
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
			replydata.setAELFTP(vatxn.getVatxnItem());
			replydata.setACRESULT(vatxn.getVatxnAcresult());
			replydata.setRESULT(vatxn.getVatxnResult());
			replydata.setACSTAT(vatxn.getVatxnAcstat());
			replydata.setCHKCELL(vatxn.getVatxnTelresult());
			replydata.setAEIPYUES(vatxn.getVatxnUse());

			body.setREPLYDATA(replydata);
			SEND_VA_GeneralTrans_RS va_rs = new SEND_VA_GeneralTrans_RS();
			va_rs.setBody(new SEND_VA_GeneralTrans_RS_Body());
			va_rs.getBody().setRs(new SEND_VA_GeneralTrans_RS_Body_MsgRs());
			va_rs.getBody().getRs().setHeader(header);
			va_rs.getBody().getRs().setSvcRs(body);

			rtnMessage =XmlUtil.toXML(va_rs);
//			rtnMessage = new IMSTextBase().makeMessage(va_rs, "0");
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}
}
