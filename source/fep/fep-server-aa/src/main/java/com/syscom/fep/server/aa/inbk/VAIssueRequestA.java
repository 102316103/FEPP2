package com.syscom.fep.server.aa.inbk;

import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.mapper.VacateMapper;
import com.syscom.fep.mybatis.mapper.VatxnMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Vacate;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.ims.IMSTextBase;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA;
import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs;
//import com.syscom.fep.vo.text.nb.SEND_VA_GeneralTrans_RS.SEND_VA_GeneralTrans_RS_Header;

/**
 * @author Jaime
 */
public class VAIssueRequestA extends INBKAABase {
	private Object tota = null;
	private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
	private FEPReturnCode _rtnCode4 = FEPReturnCode.Normal;
	private String rtnMessage = "";
	private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
	private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
	private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
	private VacateMapper vacateMapper = SpringBeanFactoryUtil.getBean(VacateMapper.class);

	public VAIssueRequestA(ATMData txnData) throws Exception {
		super(txnData, "api");
	}

	@Override
	public String processRequestData() {
		Vatxn vatxn = new Vatxn();
		try {
			boolean gotoLable = false;
//			RCV_HCE_GeneralTrans_RQ atmReq = this.getTxData().getTxHceObject().getRequest();
			RCV_VA_GeneralTrans_RQ tita = getATMBusiness().getVaReq();
			// 1. 準備FEP交易記錄檔
			_rtnCode = getATMBusiness().VAPrepareFEPTXN();
			//--ben-20220922-//getFeptxn().setFeptxnNoticeId(StringUtils.join("10", getATMRequest().getVATYPE()));
			getFeptxn().setFeptxnAtmType("6598");

			// 2. 新增FEP交易記錄檔
			if (_rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getATMBusiness().addTXData();
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

			if (!gotoLable && _rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) && StringUtils.isNotBlank(getFiscRes().getMEMO())) {
				RefBase<Vatxn> vatxnRefBase = new RefBase<>(vatxn);
				getFiscBusiness().prepareVATXNforVA(vatxnRefBase, getATMBusiness().getVaReq());
				vatxn = vatxnRefBase.get();

				vatxnMapper.insertSelective(vatxn);
			}

//			5. 	SendToCBS/ASC: 帳務主機處理
			//SendToCBS/ASC: 帳務主機處理
			String notice12 = feptxn.getFeptxnNoticeId().substring(0, 2);//'業務類別代號’
    		String notice34 = feptxn.getFeptxnNoticeId().substring(2, 4);//‘交易類別’
    		if(!gotoLable && ("02".equals(notice12) && "03".equals(notice34)) || (!"03".equals(notice34) && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno()))) {
    			getFeptxn().setFeptxnTxrust("S");/* Reject-abnormal */
    	   		/*一般金融卡帳號*/
        		/*進CBS主機檢核*/
        		getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/
        		/* CBSProcess_ABVAI001(組送CBS 主機撤銷通知Request交易電文).doc*/

        		String AATxTYPE = "3"; // 上CBS入扣帳
        		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
        		ACBSAction aaObject = (ACBSAction) this.getInstanceObject(AA, getTxData());
        		_rtnCode = new CBS(aaObject, getTxData()).sendToCBS(AATxTYPE);
        		Object tota = aaObject.getTota();
    			if (_rtnCode != FEPReturnCode.Normal) {
    				gotoLable = true;
    			}else {
    				if(("02".equals(notice12) && "03".equals(notice34))) {
    					 StringBuffer str = new StringBuffer();
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.VACATE.getValue()), 2, ' ');
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.AEIPYTP.getValue()), 2, ' ');
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.TAXIDNO.getValue()).substring(1,10));
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.CLCPYCI.getValue()), 8, ' ');
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.PAYUNTNO.getValue()), 8, ' ');
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.TAXTYPE.getValue()), 5, ' ');
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.PAYFEENO.getValue()), 4, ' ');
    					str.append(this.getImsPropertiesValue(tota, ImsMethodName.MOBILENO.getValue()), 10, ' ');
    					str.append(StringUtils.rightPad(this.getImsPropertiesValue(tota, ImsMethodName.AEIPYAC2.getValue()), 16, ' '));
    					str.append(StringUtils.leftPad(this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue()), 16, '0'));
    					getFeptxn().setFeptxnTrk3(str.toString());
    				}
    			}
    		}


			// 6. 	組送往 FISC 之 Request 電文並等待財金之 Response( if need)
			//Npsunit npsunits = dbNPSUNIT.selectByPrimaryKey(tita.getPAYUNTNO(), tita.getTAXTYPE(), tita.getPAYFEENO());
			Npsunit npsunits = new  Npsunit();

			if (!gotoLable && _rtnCode == FEPReturnCode.Normal) {
	    		_rtnCode = getFiscBusiness().sendRequestToFISC(getATMRequest());
			}



			// 7. 	CheckResponseFromFISC:檢核回應電文是否正確
			if (!gotoLable && _rtnCode == FEPReturnCode.Normal) {
				_rtnCode = getFiscBusiness().checkResponseMessage();

	    		if("02".equals(notice12)) {
	    			gotoLable = true;
	    		}

	    		if (_rtnCode != FEPReturnCode.Normal || !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
	    			if(!"10".equals(notice12)) {
	    				gotoLable = true;
	    			}
	    		}
			}else {
				gotoLable = true;
			}

			// 8. ProcessAPTOT:更新跨行代收付
//			if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
			if (!gotoLable && _rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())
					&& getTxData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
				_rtnCode2 = processAPTOT(false);
			}else {
				gotoLable = true;
			}

			if(_rtnCode2 != CommonReturnCode.Normal) {
				gotoLable = true;
			}

			//9. 	SendToCBS/ASC(if need): 進帳務主機手續費分潤
			if(!gotoLable) {
				if("10".equals(vatxn.getVatxnCate())) {
	        		String AATxTYPE = "3"; // 上CBS入扣帳
	        		String AA = getTxData().getMsgCtl().getMsgctlTwcbstxid();
	        		ACBSAction aaObject = (ACBSAction) this.getInstanceObject(AA, getTxData());
	        		_rtnCode2 = new CBS(aaObject, getTxData()).sendToCBS(AATxTYPE);
	        		Object tota = aaObject.getTota();
					if (_rtnCode2 != FEPReturnCode.Normal) {
						if(getTxData().getMsgCtl().getMsgctlUpdateAptot() == 1) {
							/* 沖回跨行代收付(APTOT) */
							processAPTOT(true);
						}
					}
				}
			}


//			10. 	判斷是否需組 CON 電文回財金
			_rtnCode2 = sendToConfirm();



			// 11. 	@新增約定及核驗交易(VATXN)記錄(if need)
//			if (_rtnCode == FEPReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
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
					    /* 將財金回應結果寫入 */
						getFiscRes().setREMARK(vatxn.getVatxnResult()+vatxn.getVatxnAcresult()+vatxn.getVatxnAcstat());

					}
				}
				vatxnMapper.insertSelective(vatxn);
//			}


		} catch (Exception ex) {
			rtnMessage = "";
			_rtnCode = FEPReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			try {
				// 12. 	更新交易記錄(FEPTXN)
				updateFEPTXN();
				//13. 	組ATM回應電文 & 回 ATMMsgHandler
				this.response(vatxn);
			} catch (Exception e) {
				rtnMessage = "";
				_rtnCode = FEPReturnCode.ProgramException;
				getLogContext().setProgramException(e);
				getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
				sendEMS(getLogContext());
			}
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

	// 帳務主機處理
	// '' <summary>
	// '' 組回應電文回給 ATM以及 判斷是否需組 CON 電文回財金上/主機沖正
	// '' </summary>
	// '' <returns></returns>
	// '' <remarks>判斷欄位是否有值,必須用Not IsNullorEmpty(Item)判斷,之後用Not IsNullorEmpty(Item.Trim)</remarks>
	private FEPReturnCode sendToConfirm() {
		// T24 hostT24 = new T24(getATMtxData());
		// Credit hostCredit = new Credit(getATMtxData());
		try {
			if (_rtnCode == CommonReturnCode.Normal && _rtnCode2 == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
				getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
				if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) {
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
						if (!DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlAtm2way())) {

							getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext()));
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse); // Accept-Reverse
							_rtnCode4 = getFiscBusiness().sendConfirmToFISC();
						} else {
							getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
							getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
						}
					} else { // -REP
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnRepRc(), FEPChannel.FISC, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
					}
				} else { // fepReturnCode <> Normal
					getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
					if(StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
					}else {
						getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
					}

					if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
						getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, getATMtxData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
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
	 * "3. 商業邏輯檢核"
	 *
	 * @return
	 */
	private FEPReturnCode checkBusinessRule() {
		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
		feptxn.setFeptxnIcSeqno(StringUtils.EMPTY);
		feptxn.setFeptxnIcmark(StringUtils.EMPTY);
		try {
//			RCV_HCE_GeneralTrans_RQ atmReq = this.getTxData().getTxHceObject().getRequest();
			RCV_VA_GeneralTrans_RQ tita = getATMBusiness().getVaReq();

			// (1) 檢核外圍 EJ 是否重覆
			rtnCode = getFiscBusiness().checkChannelEJFNO();
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

//			getATMtxData().get
//			getATMRequest()

//			3.1	檢核若前端上送時間與FEP時間已超過90秒,該筆交易不處理
//			20170801+xxxxxx
			String timeStart="";
			String timeEnd="";
			//if(StringUtils.isNotBlank(feptxn.getFeptxnTxDateAtm()) && StringUtils.isNotBlank(tita.getInTime())
			if(StringUtils.isNotBlank(feptxn.getFeptxnTxDateAtm())
					&& StringUtils.isNotBlank(feptxn.getFeptxnTxDate()) && StringUtils.isNotBlank(feptxn.getFeptxnTxTime())) {
				//timeStart = feptxn.getFeptxnTxDateAtm()+tita.getInTime();
				timeEnd = feptxn.getFeptxnTxDate()+feptxn.getFeptxnTxTime();

				long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
				long end = FormatUtil.parseDataTime(timeEnd, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
				int diffseconds = (int)((end-srt)/1000);
				/*回覆前端交易失敗0601, 合庫三碼代號: 126 */
				//前端上送時間與FEP時間已超過90秒,該筆交易不處理
				if(diffseconds > 90) {
					getFeptxn().setFeptxnReplyCode("0601");
					getFeptxn().setFeptxnCbsRc("126");
					return FEPReturnCode.OtherCheckError;
				}
			}

			// (3) 檢核財金及參加單位之系統狀態
			rtnCode = getFiscBusiness().checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

			if (!this.getATMtxData().isTxStatus()) {
				rtnCode = CommonReturnCode.InterBankServiceStop;
				return rtnCode;
			}

			// 3.3	檢核跨行金融帳戶資訊核驗電文
			/*
			if("10".equals(tita.getVACATE())) {
				if(StringUtils.isNotBlank(tita.getAEIPYBK()) || StringUtils.isNotBlank(tita.getAEIPCRBK())) {
					return FEPReturnCode.OtherCheckError;
				}
			}


			if("02".equals(tita.getVACATE())) {
				if(StringUtils.isNotBlank(tita.getTAXIDNO())&&StringUtils.isNotBlank(tita.getMOBILENO())&&StringUtils.isNotBlank(tita.getPAYUNTNO())
						&&StringUtils.isNotBlank(tita.getTAXTYPE())&&StringUtils.isNotBlank(tita.getPAYFEENO())&&StringUtils.isNotBlank(tita.getCLCPYCI())
						&&StringUtils.isNotBlank(tita.getAEIPYAC2()))
				return FEPReturnCode.OtherCheckError;
			}else if("10".equals(tita.getVACATE())) {
				if(StringUtils.isNotBlank(tita.getTAXIDNO())&&StringUtils.isNotBlank(tita.getMOBILENO())&&StringUtils.isNotBlank(tita.getBIRTHDAY())
						&&StringUtils.isNotBlank(tita.getTELHOME())) {
					return FEPReturnCode.OtherCheckError;
				}

				if(StringUtils.isNotBlank(tita.getBIRTHDAY())) {
					try {
						FormatUtil.parseDataTime(tita.getBIRTHDAY(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
					}catch(Exception e) {
						return FEPReturnCode.OtherCheckError;
					}
				}

			}

			if(tita.getMOBILENO().length() < 10) {
				return FEPReturnCode.OtherCheckError;
			}

			if(StringUtils.isNotBlank(tita.getVACATE()) ||StringUtils.isNotBlank(tita.getAEIPYTP())) {
				return FEPReturnCode.OtherCheckError;
			}
			*/
//			3.4	檢核委託單位檔
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
					if(npsunits.getNpsunitUnit() == 1 || npsunits.getNpsunitUnit() == 2) {
						return FEPReturnCode.OtherCheckError;
						/* 交易類別 若為 03 (撤銷通知) , 「約定授理單位」需為 1,3 */
					}else if(npsunits.getNpsunitUnit() != 1 && npsunits.getNpsunitUnit() != 3) {
						return FEPReturnCode.OtherCheckError;
					}else {
						getFeptxn().setFeptxnTroutBkno(npsunits.getNpsunitBkno());
					}

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
	 * 9. 更新交易記錄檔
	 *
	 * @return
	 */
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtncode = FEPReturnCode.Normal;

		if (_rtnCode != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		} else if (_rtnCode2 != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode2.getValue());
		} else if (_rtnCode4 != FEPReturnCode.Normal) {
			getFeptxn().setFeptxnAaRc(_rtnCode4.getValue());
		} else {
			getFeptxn().setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		}
		getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

		// Fly 2019/09/17 VAA2566允許其他Channel進來
		//--ben-20220922-//getFeptxn().setFeptxnChannel(getATMRequest().getCHLCODE());

		String rc = StringUtils.EMPTY;
		if (_rtnCode != FEPReturnCode.Normal) {
			rc = String.valueOf(_rtnCode.getValue());
		} else if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
			rc = getFeptxn().getFeptxnRepRc();
		}

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
		rtncode = getATMBusiness().updateTxData();
		if (rtncode != FEPReturnCode.Normal) {
			return rtncode;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 8. 組回應電文
	 *
	 * @return
	 */
	private FEPReturnCode sendToATM() {
		try {
			// 先送給ATM主機
			boolean needResponseMsg = true;
			if (StringUtils.isBlank(getATMtxData().getTxResponseMessage()) && needResponseMsg) {
				this.rtnMessage = prepareATMResponseData();
			} else {
				this.rtnMessage = getATMtxData().getTxResponseMessage();
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
		if (DbHelper.toBoolean(getATMtxData().getMsgCtl().getMsgctlUpdateAptot())) {
			rtncode = getFiscBusiness().processAptot(isEC);
			return rtncode;
		}
		return rtncode;
	}

	private String response(Vatxn vatxn) throws Exception {
		String rtnMessage = "";
		try {
			/* 組 ATM Response OUT-TEXT */
//			ATMGeneralRequest tita = this.getATMRequest();
			String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
					FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
			String feptxnTxCode = feptxn.getFeptxnTxCode();
			ATMENCHelper atmEncHelper = new ATMENCHelper(this.getTxData());
			RefString rfs = new RefString();
			String totaToact;

			RCV_VA_GeneralTrans_RQ tita = getATMBusiness().getVaReq();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
			SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

			header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
			header.setCHANNEL(feptxn.getFeptxnChannel());
			header.setMSGID(feptxn.getFeptxnMsgid());
			header.setCLIENTDT(tita.getBody().getRq().getHeader().getCLIENTDT());
			header.setSYSTEMID("FEP");

			if(!"000".equals(feptxn.getFeptxnCbsRc())) {
				header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
				body.setRSPRESULT(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));
				header.setSEVERITY("ERROR");

				if(StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.ERR_MEMO.getValue()))) {
					header.setSTATUSDESC(this.getImsPropertiesValue(tota, ImsMethodName.ERR_MEMO.getValue()));
				}else {
					header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
				}
			}else if(NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc())) {
				header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnRepRc()));
				header.setSEVERITY("ERROR");
				header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
			}else if(StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
				header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnReplyCode()));
				header.setSEVERITY("ERROR");
				header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
			}else if(feptxn.getFeptxnAaRc() != FEPReturnCode.Normal.getValue()) {
				header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnAaRc()));
				header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
				header.setSEVERITY("ERROR");
			}else {
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

			if("02".equals(vatxn.getVatxnCate())) {
				replydata.setPAYUNTNO(vatxn.getVatxnBusinessUnit());
				replydata.setTAXTYPE(vatxn.getVatxnPaytype());
				replydata.setPAYFEENO(vatxn.getVatxnFeeno());
			}else {
				replydata.setAELFTP(vatxn.getVatxnItem());
				replydata.setACRESULT(feptxn.getFeptxnRemark().substring(0,2));
				replydata.setRESULT(feptxn.getFeptxnRemark().substring(2,4));
				replydata.setACSTAT(feptxn.getFeptxnRemark().substring(5,7));
				replydata.setAEIPYUES(this.getImsPropertiesValue(tota, ImsMethodName.AEIPYUES.getValue()));
			}

			body.setREPLYDATA(replydata);
			SEND_VA_GeneralTrans_RS va_rs = new SEND_VA_GeneralTrans_RS();
			va_rs.setBody(new SEND_VA_GeneralTrans_RS_Body());
			va_rs.getBody().setRs(new SEND_VA_GeneralTrans_RS_Body_MsgRs());
			va_rs.getBody().getRs().setHeader(header);
			va_rs.getBody().getRs().setSvcRs(body);

			rtnMessage = new IMSTextBase().makeMessage(va_rs, "0");
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
		return rtnMessage;
	}
}
