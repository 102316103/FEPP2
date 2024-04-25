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
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
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

import java.util.Calendar;
import java.util.Date;

/**
 * @author Jaime
 */
public class LESelfIssue extends ATMPAABase {
    private Object tota = null;
    private RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
    private FEPReturnCode _rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = FEPReturnCode.Normal;
    private String rtnMessage = "";
    private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
    private MsgfileExtMapper msgfileExtMapper = SpringBeanFactoryUtil.getBean(MsgfileExtMapper.class);
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

    public LESelfIssue(NBData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        Vatxn vatxn = new Vatxn();
        try {
//			RCV_HCE_GeneralTrans_RQ atmReq = this.getTxData().getTxHceObject().getRequest();
//			RCV_VA_GeneralTrans_RQ tita = this.getmVAReq();
            // 1. 準備FEP交易記錄檔
            _rtnCode = getATMBusiness().VAPrepareFEPTXN();

            // 2. 新增FEP交易記錄檔
            if (_rtnCode == FEPReturnCode.Normal) {
                addTxData();
            }

            // 3. 商業邏輯檢核(ATM電文)
            _rtnCode = getATMBusiness().checkChannelEJFNO();
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode = checkBusinessRule();
            }

            // 4. Prepare約定及核驗交易(VATXN)記錄
            if (_rtnCode == CommonReturnCode.Normal) {
                RefBase<Vatxn> vatxnRefBase = new RefBase<>(vatxn);
//				getATMBusiness().prepareVATXNforVA(vatxnRefBase, getATMBusiness().getVaReq());
                prepareVATXNforVALE(vatxnRefBase, tita);
                vatxn = vatxnRefBase.get();
                vatxnMapper.insertSelective(vatxn);
            }

//			5. 	SendToCBS/ASC: 帳務主機處理
            if (_rtnCode == CommonReturnCode.Normal) {
                //SendToCBS/ASC: 帳務主機處理
                String notice12 = feptxn.getFeptxnNoticeId().substring(0, 2);//'業務類別代號’
                String notice34 = feptxn.getFeptxnNoticeId().substring(2, 4);//‘交易類別’
                getFeptxn().setFeptxnTxrust("S");/* Reject-abnormal */
                /*一般金融卡帳號*/
                /*進CBS主機檢核*/
                getFeptxn().setFeptxnStan(getATMBusiness().getStan());/*先取 STAN 以供主機電文使用*/
                /* CBSProcess_ABVAI001(組送CBS 主機撤銷通知Request交易電文).doc*/
                String AATxTYPE = "3"; // 上CBS入扣帳
                String AA = getmNBtxData().getMsgCtl().getMsgctlTwcbstxid();
                feptxn.setFeptxnCbsTxCode(AA);
                ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getmNBtxData());
                this.getmNBtxData().setVatxn(vatxn);
                _rtnCode = new CBS(hostAA, getmNBtxData()).sendToCBS(AATxTYPE);
                tota = hostAA.getTota();
            }

//			6. 	判斷是否需組 CON 電文回財金
            if (_rtnCode == FEPReturnCode.Normal) {
                _rtnCode2 = labelEndOfFunc();
            }

            // 7. 	@新增約定及核驗交易(VATXN)記錄(if need)
            if (_rtnCode == FEPReturnCode.Normal) {
                vatxn.setVatxnTxrust(getFeptxn().getFeptxnTxrust());
                Vatxn po = vatxnMapper.selectByPrimaryKey(vatxn.getVatxnTxDate(), vatxn.getVatxnEjfno());
                vatxnMapper.updateByPrimaryKeySelective(vatxn);
            }

            // 8. 	組ATM回應電文 & 回 ATMMsgHandler
            this.response(vatxn);

        } catch (Exception ex) {
            rtnMessage = "";
            _rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(getLogContext());
        } finally {
            getmNBtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmNBtxData().getLogContext().setMessage(rtnMessage);
            getmNBtxData().getLogContext().setProgramName(this.aaName);
            getmNBtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG, getLogContext());
        }
        // 先組回應ATM 故最後return空字串m
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

    // 帳務主機處理
    // 6. 	判斷是否需組 CON 電文回財金
    private FEPReturnCode labelEndOfFunc() {
        try {
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Response);

            if (_rtnCode != CommonReturnCode.Normal) {
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(String.valueOf(_rtnCode.getValue()), FEPChannel.FEP, FEPChannel.ATM,
                        getmNBtxData().getLogContext()));
                feptxn.setFeptxnErrMsg(msgfileExtMapper.select(feptxn.getFeptxnReplyCode()).getMsgfileShortmsg());
            } else {
                getFeptxn().setFeptxnReplyCode("    ");
            }

            getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
            getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));

            if (_rtnCode == CommonReturnCode.Normal) {
                if ("3".equals(getFeptxn().getFeptxnWay())) {
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Pending); // PENDING
                } else {
                    getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed); // 成功
                }
            } else {
                getFeptxn().setFeptxnTxrust(FeptxnTxrust.RejectNormal);
            }

            try {
                if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) > 0) {
                    return FEPReturnCode.Normal;
                } else {
                    return FEPReturnCode.FEPTXNUpdateError;
                }
            } catch (Exception ex) {
                getFeptxn().setFeptxnReplyCode("L013");
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(ProgramName + ".UpdateTxData");
                sendEMS(getLogContext());
                return FEPReturnCode.FEPTXNUpdateError;
            }
//			sendToATM();

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
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header = tita.getBody().getRq().getHeader();
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq nbbody = tita.getBody().getRq().getSvcRq();
            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SENDDATA senddata = tita.getBody().getRq().getSvcRq().getSENDDATA();

            // 3.1 檢核外圍 EJ 是否重覆
            rtnCode = getATMBusiness().checkChannelEJFNO();
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnCode;
            }

//			3.2	   檢核前端電文
            if (StringUtils.isBlank(nbbody.getTAXIDNO())
                    && StringUtils.isBlank(nbbody.getMOBILENO())
                    && StringUtils.isBlank(senddata.getPAYUNTNO())
                    && StringUtils.isBlank(senddata.getTAXTYPE())
                    && StringUtils.isBlank(senddata.getPAYFEENO())
                    && StringUtils.isBlank(senddata.getCLCPYCI())
                    && StringUtils.isBlank(senddata.getAEIPYAC2())
            ) {
                rtnCode = FEPReturnCode.OtherCheckError;/* 其他類檢核錯誤 */
            }

            if (nbbody.getMOBILENO().length() < 10) {
                return FEPReturnCode.OtherCheckError;
            }
            if (StringUtils.isBlank(nbbody.getVACATE()) || StringUtils.isBlank(nbbody.getAEIPYTP())) {
                return FEPReturnCode.OtherCheckError;
            }

//			3.3	檢核委託單位檔
            if (StringUtils.isBlank(tita.getBody().getRq().getSvcRq().getVACATE())) {
                return FEPReturnCode.VACATENONotFound;   /* 約定及核驗服務業務類別不存在 */
            }

            Npsunit npsunits = dbNPSUNIT.selectByPrimaryKey(senddata.getPAYUNTNO(), senddata.getTAXTYPE(), senddata.getPAYFEENO());
            if (npsunits == null) {
                return FEPReturnCode.NPSUNITNotFound;
            } else {
                if ("00".equals(tita.getBody().getRq().getSvcRq().getAEIPYTP()) || "01".equals(tita.getBody().getRq().getSvcRq().getAEIPYTP())) {
                    /* 交易類別 若為 00 (約定)、01(撤銷) , 「約定授理單位」需為 1,2 */
                    if (npsunits.getNpsunitUnit() != 1 && npsunits.getNpsunitUnit() != 2) {
                        return FEPReturnCode.OtherCheckError;
                    }
                } else {
                    /* 交易類別 若為 03 (撤銷通知) , 「約定授理單位」需為 1,3 */
                    if (npsunits.getNpsunitUnit() != 1 && npsunits.getNpsunitUnit() != 3) {
                        return FEPReturnCode.OtherCheckError;
                    } else {
                        getFeptxn().setFeptxnTroutBkno(npsunits.getNpsunitBkno().substring(0, 3));
                        getFeptxn().setFeptxnTroutBkno7(npsunits.getNpsunitBkno());
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
		if (StringUtils.isBlank(getTxData().getTxObject().getResponse().getRsStatDesc()) && StringUtils.isNotBlank(rc)) {
			try {
				List<Msgfile> msgfileList = msgfileExtMapper.selectByMsgfileErrorcode(rc);
				if (CollectionUtils.isNotEmpty(msgfileList)) {
					getTxData().getTxObject().getResponse().setRsStatDesc(msgfileList.get(0).getMsgfileShortmsg());
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
            if (StringUtils.isBlank(getmNBtxData().getTxResponseMessage()) && needResponseMsg) {
//				this.rtnMessage = prepareATMResponseData();
            } else {
                this.rtnMessage = getmNBtxData().getTxResponseMessage();
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
     * 8. 	組ATM回應電文 & 回 ATMMsgHandler
     *
     * @param vatxn
     * @return
     * @throws Exception
     */
    private String response(Vatxn vatxn) throws Exception {
        try {
            /* 組 ATM Response OUT-TEXT */
//			ATMGeneralRequest atmReq = this.getATMRequest();
            String systemTime = FormatUtil.dateTimeFormat(Calendar.getInstance(),
                    FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            String feptxnTxCode = feptxn.getFeptxnTxCode();
            ATMENCHelper atmEncHelper = new ATMENCHelper(this.getmNBtxData());
            RefString rfs = new RefString();
            String totaToact;

            RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getmVAReq().getBody().getRq().getSvcRq();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VA_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs();
            SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA replydata = new SEND_VA_GeneralTrans_RS_Body_MsgRs_REPLYDATA();

            header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(header.getMSGID());
            header.setCLIENTDT(header.getCLIENTDT());

            if (!"000".equals(feptxn.getFeptxnCbsRc())) {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue()));
                body.setRSPRESULT(this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue()));
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnReplyCode())) {
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnReplyCode()).substring(2,6));
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else {
                header.setSYSTEMID("ATM");
                header.setSTATUSCODE(NormalRC.FISC_ATM_OK);
                header.setSTATUSDESC("");
                header.setSEVERITY("INFO");
            }

            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setTXNSTAN(feptxn.getFeptxnStan());
            body.setCUSTOMERID(feptxn.getFeptxnIdno());
            body.setTXNTYPE(tita.getTXNTYPE());
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setTRANSAMT(tita.getTRANSAMT());
            body.setAEIPYTP(tita.getAEIPYTP());
            body.setTAXIDNO(vatxn.getVatxnIdno());
            body.setAEIPCRBK(tita.getAEIPCRBK());
            body.setCLACTNO(feptxn.getFeptxnTroutActno());
            replydata.setPAYUNTNO(vatxn.getVatxnBusinessUnit());
            replydata.setTAXTYPE(vatxn.getVatxnPaytype());
            replydata.setPAYFEENO(vatxn.getVatxnFeeno());

            body.setREPLYDATA(replydata);
            SEND_VA_GeneralTrans_RS va_rs = new SEND_VA_GeneralTrans_RS();
            va_rs.setBody(new SEND_VA_GeneralTrans_RS_Body());
            va_rs.getBody().setRs(new SEND_VA_GeneralTrans_RS_Body_MsgRs());
            va_rs.getBody().getRs().setHeader(header);
            va_rs.getBody().getRs().setSvcRs(body);

            rtnMessage = XmlUtil.toXML(va_rs);
//			rtnMessage = new IMSTextBase().makeMessage(va_rs, "0");
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }


    /**
     * 4. Prepare約定及核驗交易(VATXN)記錄
     *
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
}
