package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.NwdtxnMapper;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.AB_TR_VO_I01;
import com.syscom.fep.vo.text.ims.AB_TR_VO_O01;
import com.syscom.fep.vo.text.ims.AB_TR_VO_O02;
import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
//import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;

public class ABTRVOI01 extends ACBSAction {

    public ABTRVOI01(MessageBase txData) {
        super(txData, new AB_TR_VO_I01());
    }

    RCV_VO_GeneralTrans_RQ atmRequest = this.getVORequest();

    FISC_INBK inbkReq = this.getInbkRequest();
//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
    private NwdtxnMapper nwdtxnMapper = SpringBeanFactoryUtil.getBean(NwdtxnMapper.class);

    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        /* TITA 請參考合庫主機電文規格(AB_TR_VO_I01) */
        // Header
        AB_TR_VO_I01 cbstita = new AB_TR_VO_I01();
        cbstita.setIMS_TRANS("MFEPEA00");
        cbstita.setSYSCODE("FEP");
        cbstita.setSYS_DATETIME(
                FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));
        // TXN_FLOW
        if ("0".equals(feptxn.getFeptxnFiscFlag())) {
            cbstita.setTXN_FLOW("C"); // 自行
        } else {
            cbstita.setTXN_FLOW("A"); // 代理
        }
        cbstita.setMSG_CAT(atmRequest.getBody().getRq().getSvcRq().getTXNTYPE().trim());
        cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbstita.setPCODE(feptxn.getFeptxnPcode());
        cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        // PROCESS_TYPE
        if ("0".equals(txType)) { //查詢、檢核
            cbstita.setPROCESS_TYPE("CHK");
        } else if ("1".equals(txType)) { //入扣帳
            cbstita.setPROCESS_TYPE("ACCT");
        } else if ("2".equals(txType)) { //沖正
            cbstita.setPROCESS_TYPE("RVS");
        }
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbstita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
        cbstita.setTXNSTAN(feptxn.getFeptxnStan());
        cbstita.setTERMINALID(feptxn.getFeptxnAtmno());
        cbstita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
        cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
        cbstita.setCARDTYPE("N");
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        } else {
            cbstita.setRESPONSE_CODE("0000"); //正常才上送
        }
        //Detail
        cbstita.setICCHIPSTAN(" ");
        cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbstita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
        cbstita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        cbstita.setTXNICCTAC("40404040404040404040");
        cbstita.setTXNAMT(feptxn.getFeptxnTxAmtAct()); /* 轉為S9(11)V99 */
        cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
        cbstita.setTOACT(feptxn.getFeptxnTrinActno());
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
        } else {
            cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        }
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
        } else {
            cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        }
        cbstita.setTOACT(feptxn.getFeptxnTrinActno());
        cbstita.setTR_SPECIAL_FLAG(" ");
        cbstita.setTXMEMO(" ");
        cbstita.setI_ACT(feptxn.getFeptxnAcctSup());

        cbstita.setAE_TRNSFLAG(atmRequest.getBody().getRq().getSvcRq().getTRNSFLAG());
        cbstita.setAE_TRNSFROUTIDNO(feptxn.getFeptxnIdno());
        cbstita.setAE_BUSINESSTYPE(atmRequest.getBody().getRq().getSvcRq().getBUSINESSTYPE());

        //手續費負擔別
        if (feptxn.getFeptxnNpsClr() == 2) {
            cbstita.setAE_AEIEFEET("13");
        } else if (feptxn.getFeptxnNpsClr() == 3) {
            cbstita.setAE_AEIEFEET("14");
        } else if (feptxn.getFeptxnNpsClr() == 1) {
            cbstita.setAE_AEIEFEET("15");
        }

        cbstita.setAE_TRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
        cbstita.setAE_AEISLLTY(atmRequest.getBody().getRq().getSvcRq().getSSLTYPE());
        cbstita.setAE_LIMITTYPE(atmRequest.getBody().getRq().getSvcRq().getLIMITTYPE());
        cbstita.setAE_AEIFIXFE(atmRequest.getBody().getRq().getSvcRq().getFAXFEE());
        cbstita.setAE_AEINETFE(atmRequest.getBody().getRq().getSvcRq().getTRANSFEE());
        cbstita.setAE_AEIEDIFE(atmRequest.getBody().getRq().getSvcRq().getOTHERBANKFEE());
        cbstita.setAE_AEICIRCU(atmRequest.getBody().getRq().getSvcRq().getCUSTCODE());

        this.setoTita(cbstita);
        this.setTitaToString(cbstita.makeMessage());
        return FEPReturnCode.Normal;
    }

    /**
     * 拆解CBS回應電文
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 拆解主機回應電文 */
        AB_TR_VO_O01 tota = new AB_TR_VO_O01();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);//塞入拆解後的tota讓AA取得

        /* 更新交易 */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota);

        /* 新增交易通知 */
        if (StringUtils.isNotBlank(tota.getNOTICE_TYPE())) {
            insertSMSMSG(tota);
        }

        return rtnCode;

    }

    /**
     * 更新交易
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(AB_TR_VO_O01 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        if (StringUtils.isNotBlank(cbsTota.getSEND_FISC2160())) {
            feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160());
        }
        /* 變更FEPTXN交易記錄 */
        // IMSRC_TCB = "000" or empty表交易成功
        // IMSRC_FISC = "0000" or "4001" 表交易成功
        if (!"000".equals(cbsTota.getIMSRC_TCB()) && StringUtils.isNotBlank(cbsTota.getIMSRC_TCB())) {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            feptxn.setFeptxnAccType((short) 0); // 未記帳
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
            //CBS 帳務日
            feptxn.setFeptxnTbsdy(cbsTota.getIMSBUSINESS_DATE()); // 轉西元年
            // 記帳類別
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            } else {
                feptxn.setFeptxnAccType((short) 0);
            }
            //帳務分行
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 主機交易時間
            feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME().toString());

            //主機回傳的手續費
            //手續費
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());

            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
            }

            //實際轉出金額(含手續費)
            if (cbsTota.getTRANSAMTOUT() != null) {
                feptxn.setFeptxnTxAmtAct(cbsTota.getTRANSAMTOUT());
            }

            //帳戶餘額
            if (feptxn.getFeptxnPcode().equals("2532")) { //本行帳戶繳稅
                if (cbsTota.getACTBALANCE() != null) {
                    feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
                }
                feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
            }

            //轉出分行
            if (feptxn.getFeptxnPcode().equals("2532") //自行繳稅
                    && atmRequest.getBody().getRq().getSvcRq().getTXNTYPE().equals("RQ")) { //第一道電文
                feptxn.setFeptxnTroutBkno7(cbsTota.getCHAFEE_BRANCH());
            }

            if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCHOUT())) {
                feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCHOUT());
            }
            rtnCode = FEPReturnCode.Normal;
        }
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        return rtnCode;
    }

    /**
     * 新增交易通知
     *
     * @param cbsTota
     * @throws ParseException
     */
    private void insertSMSMSG(AB_TR_VO_O01 cbsTota) throws ParseException {
//        String txDate = feptxn.getFeptxnTxDate();
//        Integer ejfno = feptxn.getFeptxnEjfno();
//        Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//        // 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//        if (smsmsg == null) {
//            smsmsg = new Smsmsg();
//            smsmsg.setSmsmsgTxDate(txDate);
//            smsmsg.setSmsmsgEjfno(ejfno);
//            smsmsg.setSmsmsgStan(feptxn.getFeptxnStan());
//            smsmsg.setSmsmsgPcode(feptxn.getFeptxnPcode());
//            smsmsg.setSmsmsgTroutActno(feptxn.getFeptxnTroutActno());
//            smsmsg.setSmsmsgTxTime(feptxn.getFeptxnTxTime());
//            smsmsg.setSmsmsgBrno(feptxn.getFeptxnBrno());
//            smsmsg.setSmsmsgZone("TWN");
//            smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//            smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//            smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//            smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//            smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//            smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//            smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//            smsmsg.setSmsmsgNotifyFg("Y");
//            smsmsg.setSmsmsgSendType(cbsTota.getNOTICE_TYPE());
//            smsmsg.setSmsmsgChannel(feptxn.getFeptxnChannel());
//            Date datenow = (Date) new SimpleDateFormat("yyyy/MM/DD-HH.mm.ss.SSS").parse(
//                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_TAX));
//            smsmsg.setUpdateTime(datenow);
//            smsmsg.setSmsmsgNoticeNo(cbsTota.getNOTICE_NUMBER());
//
//            if (smsmsgExtMapper.insert(smsmsg) <= 0) {
//                getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//                this.logMessage(getLogContext());
//            }
//        }
    }

    private void insertSMSMSG(AB_TR_VO_O02 cbsTota) throws ParseException {
//        String txDate = feptxn.getFeptxnTxDate();
//        Integer ejfno = feptxn.getFeptxnEjfno();
//        Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//        // 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//        if (smsmsg == null) {
//            smsmsg = new Smsmsg();
//            smsmsg.setSmsmsgTxDate(txDate);
//            smsmsg.setSmsmsgEjfno(ejfno);
//            smsmsg.setSmsmsgStan(feptxn.getFeptxnStan());
//            smsmsg.setSmsmsgPcode(feptxn.getFeptxnPcode());
//            smsmsg.setSmsmsgTroutActno(feptxn.getFeptxnTroutActno());
//            smsmsg.setSmsmsgTxTime(feptxn.getFeptxnTxTime());
//            smsmsg.setSmsmsgBrno(feptxn.getFeptxnBrno());
//            smsmsg.setSmsmsgZone("TWN");
//            smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//            smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//            smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//            smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//            smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//            smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//            smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//            smsmsg.setSmsmsgNotifyFg("Y");
//            smsmsg.setSmsmsgSendType(cbsTota.getNOTICE_TYPE());
//            smsmsg.setSmsmsgChannel(feptxn.getFeptxnChannel());
//            Date datenow = (Date) new SimpleDateFormat("yyyy/MM/DD-HH.mm.ss.SSS").parse(
//                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_TAX));
//            smsmsg.setUpdateTime(datenow);
//            smsmsg.setSmsmsgNoticeNo(cbsTota.getNOTICE_NUMBER());
//
//            if (smsmsgExtMapper.insert(smsmsg) <= 0) {
//                getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//                this.logMessage(getLogContext());
//            }
//        }
    }
}
