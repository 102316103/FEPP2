package com.syscom.fep.server.aa.nb;

import java.math.BigDecimal;
import java.util.Objects;

import com.syscom.fep.server.aa.inbk.INBKAABase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.mapper.EaitxnMapper;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs;

/**
 * @author
 */
public class NBPYSelfRequestA extends INBKAABase {
    private Object tota = null;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode3 = FEPReturnCode.Normal;
    private String AATxTYPE = "";
    private EaitxnMapper eaitxnMapper = SpringBeanFactoryUtil.getBean(EaitxnMapper.class);



    public NBPYSelfRequestA(NBData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try {
            // 1. Prepare : 交易記錄初始資料
            rtnCode = getFiscBusiness().nb_PrepareFEPTxn();

            if (rtnCode == FEPReturnCode.Normal) {
                // 2. AddTxData: 新增交易記錄(FEPTXN)
                this.addTxData();
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 3. CheckBusinessRule: 商業邏輯檢核
            	RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = getnBData().getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
                rtnCode = getFiscBusiness().checkRequestFromOtherChannel(getnBData(), tita.getINTIME());
            }

            if (rtnCode == FEPReturnCode.Normal) {
                // 4. SendToCBS/ASC: 送主機檢核帳戶資料
                this.sendToCBS();
            }

            FISC fiscBusiness = getFiscBusiness();
            if (feptxn.getFeptxnFiscFlag() != 0 && rtnCode == FEPReturnCode.Normal) {
                // 5. 組送往 FISC 之 Request 電文並等待財金之 Response
                rtnCode = fiscBusiness.sendRequestToFISC(getATMRequest());
            }

            boolean repRcEq4001 = true;
            if (feptxn.getFeptxnFiscFlag() != 0 && rtnCode == FEPReturnCode.Normal) {
                // 6. CheckResponseFromFISC:檢核回應電文是否正確
                rtnCode = fiscBusiness.checkResponseMessage();
                repRcEq4001 = "4001".equals(feptxn.getFeptxnRepRc());
            }

         // 7. ProcessAPTOT:更新跨行代收付
            if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()&& repRcEq4001) {
                rtnCode = fiscBusiness.processAptot(false);
            }

            if (rtnCode == FEPReturnCode.Normal&& repRcEq4001) {
                // 8. SendToCBS/ASC: 送主機檢核帳戶資料
            	if(getnBData().getMsgCtl().getMsgctlCbsFlag() == 1) {
            		this.sendToCBS2();

                    if (rtnCode == FEPReturnCode.Normal && rtnCode2 == FEPReturnCode.Normal && repRcEq4001 && "000".equals(feptxn.getFeptxnCbsRc()) && "1".equals(feptxn.getFeptxnAccType())) {
                    	if (1 == getnBData().getMsgCtl().getMsgctlUpdateAptot()&& repRcEq4001) {
                            /* 沖回跨行代收付(APTOT) */
                            rtnCode2 = fiscBusiness.processAptot(true);
                        }
                    }
            	}
            }


            // 9. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
            rtnCode = this.labelEndOfFunc();

            //10. 	更新交易記錄(FEPTXN)
            this.updateTxData();

//			11. 	組NB回應電文 & 回 NBMsgHandler
//		    電文內容格式請參照: SEND_HCE_GeneralTrans_RS
            rtnMessage = getFiscBusiness().prepareNBResponseData(tota);

            // 12. 交易通知 (if need)
            this.sendToMailHunter();

            //13. 交易結束通知主機(By PCODE)
            this.transactionCloseConnect();

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getFiscBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {

            getnBData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getnBData().getLogContext().setMessage(rtnMessage);
            getnBData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getnBData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    /**
	 * 1. 	組回應電文回給 NB
	 *
	 */
	protected String prepareNBResponseDataNBSelfIssueRequestA(Object tota) throws Exception {
		String rtnMessage = "";
		try {

			RCV_NB_GeneralTrans_RQ atmReq = this.getmNBReq();

			SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
			SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
			SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
			SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
			SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
			msgrs.setHeader(header);
			msgrs.setSvcRs(body);
			rsbody.setRs(msgrs);
			rs.setBody(rsbody);

			rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
			rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
			rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
			rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());
			rs.getBody().getRs().getHeader().setSYSTEMID("FEP");


			if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
					rs.getBody().getRs().getHeader().setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
					rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
					rs.getBody().getRs().getSvcRs().setTCBRTNCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));
					rs.getBody().getRs().getHeader().setSTATUSDESC(feptxn.getFeptxnErrMsg());

			}else if(StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
				rs.getBody().getRs().getHeader().setSTATUSCODE(String.valueOf(feptxn.getFeptxnRepRc()));
				rs.getBody().getRs().getHeader().setSTATUSDESC(feptxn.getFeptxnErrMsg());
				rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
			}else if(StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
				rs.getBody().getRs().getHeader().setSTATUSCODE(String.valueOf(feptxn.getFeptxnReplyCode()));
				rs.getBody().getRs().getHeader().setSTATUSDESC(feptxn.getFeptxnErrMsg());
				rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
			}else {
				rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
				rs.getBody().getRs().getHeader().setSTATUSDESC("");
				rs.getBody().getRs().getHeader().setSEVERITY("INFO");
				rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(new BigDecimal(StringUtils.leftPad(this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue()), 16, '0')));
				rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(new BigDecimal(StringUtils.leftPad(this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue()), 16, '0')));

				if(StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.TRANSAMTOUT.getValue()))) {
					BigDecimal amt = new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.TRANSAMTOUT.getValue()));
					if(amt.compareTo(BigDecimal.ZERO) >= 0 ) {
						rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(new BigDecimal("+"+StringUtils.leftPad(this.getImsPropertiesValue(tota, ImsMethodName.TRANSAMTOUT.getValue()), 16, '0').replace(".", "")));
					}else {
						rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(new BigDecimal("-"+StringUtils.leftPad(this.getImsPropertiesValue(tota, ImsMethodName.TRANSAMTOUT.getValue()), 16, '0').replace(".", "")));
					}

				}else {
					if(feptxn.getFeptxnTxAmt() != null && feptxn.getFeptxnFeeCustpay() != null) {
						BigDecimal amt = feptxn.getFeptxnTxAmt().add(feptxn.getFeptxnFeeCustpay());
						if(amt.compareTo(BigDecimal.ZERO) >= 0 ) {
							rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(new BigDecimal("+"+StringUtils.leftPad(amt.setScale(2).toString().replace(".", ""), 16, '0').replace(".", "")));
						}else {//有小數的負數
							rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(new BigDecimal("-"+StringUtils.leftPad(amt.setScale(2).toString().replace(".", ""), 16, '0').replace(".", "")));
						}
					}
				}

				if(feptxn.getFeptxnFeeCustpay() != null)
				rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());


				rs.getBody().getRs().getSvcRs().setFISCFEE(atmReq.getBody().getRq().getSvcRq().getFISCFEE());
				rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(atmReq.getBody().getRq().getSvcRq().getOTHERBANKFEE());
				rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(this.getImsPropertiesValue(tota, ImsMethodName.CHAFEE_BRANCH.getValue()));
				rs.getBody().getRs().getSvcRs().setCHAFEEAMT(new BigDecimal(this.getImsPropertiesValue(tota, ImsMethodName.E_CHAFEEAMT.getValue())));
			}

			rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
			rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
			rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
			rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
			rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
			rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
			rs.getBody().getRs().getSvcRs().setACCTDATE(feptxn.getFeptxnTbsdy());
			if(StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
				rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()));
			}
			if(StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()))) {
				rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()));
			}

			rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());

			if(feptxn.getFeptxnTxAmt() != null) {
				BigDecimal amt = feptxn.getFeptxnTxAmt();
				if(amt.compareTo(BigDecimal.ZERO) >= 0 ) {
					rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal("+"+StringUtils.leftPad(amt.setScale(2).toString().replace(".", ""), 15, '0')));
				}else {
					rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal("-"+StringUtils.leftPad(amt.setScale(2).toString().replace(".", ""), 15, '0')));
				}
			}

			rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
			rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(feptxn.getFeptxnIdno());

			rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
			rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
			rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
			rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
			rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBkno());
			rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
			rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
			rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()));
			if(StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.E_TRANSOUTCUST.getValue()))) {
				rs.getBody().getRs().getSvcRs().setCUSTOMERNATURE(this.getImsPropertiesValue(tota, ImsMethodName.E_TRANSOUTCUST.getValue()));
			}

			rtnMessage =XmlUtil.toXML(rs);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return "";
		}
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
                rtnCode = FEPReturnCode.FEPTXNInsertError;
            }
        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramName(ProgramName + ".addTxData");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNInsertError;
        }
    }


    /**
     * 4. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS() throws Exception {
    	feptxn.setFeptxnTxrust("S"); /* Reject-abnormal */
        getFeptxn().setFeptxnStan(getFiscBusiness().getStan());/*先取 STAN 以供主機電文使用*/

        AATxTYPE = "0"; // 上CBS查詢、檢核

        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        String charge = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
        String brch = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
        String chargeFlag = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
        if (StringUtils.isNotBlank(charge)) {
            BigDecimal hostCharge = new BigDecimal(charge);
            getnBData().setCharge(hostCharge);
        }
        getnBData().setBrch(brch);
        getnBData().setChargeFlag(chargeFlag);
    }

    /**
     * 8. SendToCBS/ASC: 送主機檢核帳戶資料
     *
     * @throws Exception
     */
    private void sendToCBS2() throws Exception {
        feptxn.setFeptxnTxrust("S");

        /* 進主機入扣帳/手續費 */
        AATxTYPE = "1"; // 上CBS入扣帳

        String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid();
        feptxn.setFeptxnCbsTxCode(AA);
        ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
        rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE);
        tota = hostAA.getTota();
        String charge = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
        String brch = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
        String chargeFlag = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
        if (StringUtils.isNotBlank(charge)) {
            BigDecimal hostCharge = new BigDecimal(charge);
            getnBData().setCharge(hostCharge);
        }
        getnBData().setBrch(brch);
        getnBData().setChargeFlag(chargeFlag);
    }

    /**
     * 9. label_END_OF_FUNC :判斷是否需組 CON 電文回財金
     *
     * @throws Exception
     */
    private FEPReturnCode labelEndOfFunc() throws Exception {
        try {
            if (rtnCode == CommonReturnCode.Normal && NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                if (DbHelper.toBoolean(getnBData().getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                    getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                    rtnCode3 = getFiscBusiness().sendConfirmToFISC();
                } else {
                    /* for代理提款-ATM_3way 交易 */
                    feptxn.setFeptxnTxrust("B"); /* PENDING */
                    getFeptxn().setFeptxnReplyCode("    ");/*回覆 ATM正常*/
                }
            } else {
                getLogContext().setProgramName(ProgramName);
                // 交易失敗
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnRepRc())) {
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) { // +REP
                        if(StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                            feptxn.setFeptxnConRc(this.getImsPropertiesValue(tota,ImsMethodName.IMSRC4_FISC.getValue()));
                        }else {
                            feptxn.setFeptxnConRc(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
                                    FEPChannel.FEP, FEPChannel.FISC, getnBData().getLogContext()));
                        }
    					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(Objects.toString(rtnCode.getValue()),
    							FEPChannel.FEP, getnBData().getTxChannel(), getnBData().getLogContext()));
                        feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());
    					feptxn.setFeptxnTxrust("C"); /* Accept-Reverse */
    					rtnCode3 = getFiscBusiness().sendConfirmToFISC();

                    } else { // -REP
                    	feptxn.setFeptxnTxrust("R"); /* Reject-normal */
    					feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(feptxn.getFeptxnRepRc(), FEPChannel.FISC,
    							getnBData().getTxChannel(), getnBData().getLogContext()));
    					feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());

                    }
                } else { // fepReturnCode <> Normal
                    getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE before:", getFeptxn().getFeptxnReplyCode()));
                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
                    } else {
                        getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectAbnormal);
                    }

                    if (StringUtils.isBlank(getFeptxn().getFeptxnReplyCode())) {
                        getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getnBData().getTxChannel(), getLogContext())); // 將ReturnCode轉成前端對應通道,但會有WEBATM的通道必須先轉成ATM通道
                    }
                    getLogContext().setMessage(StringUtils.join("FepTxn.FEPTXN_REPLY_CODE after:", getFeptxn().getFeptxnReplyCode()));
                    logMessage(Level.DEBUG, getLogContext());
                    feptxn.setFeptxnErrMsg(getLogContext().getResponseMessage().length()>256?getLogContext().getResponseMessage().substring(0, 256):getLogContext().getResponseMessage());
                }
                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);
                getFiscBusiness().updateTxData();
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
     * 10. 更新交易記錄(FEPTXN)
     */
    private void updateTxData() {
        if (rtnCode != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode.getValue());
        } else if (rtnCode2 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode2.getValue());
        } else if (rtnCode3 != FEPReturnCode.Normal) {
            feptxn.setFeptxnAaRc(rtnCode3.getValue());
        }
        else {
            feptxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
        }
        feptxn.setFeptxnAaComplete((short) 1); /* AA Close */
        feptxn.setFeptxnMsgflow("A2"); /* ATM Response*/
        this.updateFeptxn(); /* 檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] */
    }

    /**
     * 更新feptxn
     *
     * @return
     */
    private void updateFeptxn() {
        try {
            String tbsdy = SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8);
            feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".processRequestData"));
            feptxnDao.updateByPrimaryKeySelective(this.feptxn); // 更新資料
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFeptxn");
            sendEMS(getLogContext());
        }
    }

    /**
     * 轉民國年,格式化為YY/MM/DD,年度取後碼,ex :11/06/29
     *
     * @param dateStr
     * @return
     */
    private String dateStrToYYMMDD(String dateStr) {
        String rtnDate;
        if ("00000000".equals(dateStr) || dateStr.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            rtnDate = "00/00/00";
        } else {
            dateStr = CalendarUtil.adStringToROCString(dateStr);
            int dateStrLength = dateStr.length();
            dateStr = dateStr.substring(dateStrLength - 6, dateStrLength).replaceAll("(.{2})", "$1/");
            rtnDate = dateStr.substring(0, dateStr.length() - 1);
        }
        return rtnDate;
    }

    /**
     * 12. 交易通知 (if need)
     */
    private void sendToMailHunter() {
        try {
            String noticeType = feptxn.getFeptxnNoticeType();
            if (StringUtils.isNotBlank(noticeType) && NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnRepRc()) && NormalRC.FISC_ATM_OK.equals(feptxn.getFeptxnConRc())) {
                switch (noticeType) {
                    case "P": /* 送推播 */
                        getFiscBusiness().preparePush(this.feptxn);
                        break;
                    case "M": /* 簡訊 */
                        getFiscBusiness().prepareSms(this.feptxn);
                        break;
                    case "E": /* Email */
                        getFiscBusiness().prepareMail(this.feptxn);
                        break;
                }
            }
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }

    /**
     * 13. 交易結束通知主機(By PCODE)
     */
    public void transactionCloseConnect() {
        try {
            String AATxTYPE = ""; //不需提供此值
            String AATxRs = "N";//不需等待主機回應
            String AA = getnBData().getMsgCtl().getMsgctlTwcbstxid1();
            feptxn.setFeptxnCbsTxCode(AA);
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getnBData());
            rtnCode = new CBS(hostAA, getnBData()).sendToCBS(AATxTYPE, AATxRs);
        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToMailHunter"));
            sendEMS(this.logContext);
        }
    }

}
