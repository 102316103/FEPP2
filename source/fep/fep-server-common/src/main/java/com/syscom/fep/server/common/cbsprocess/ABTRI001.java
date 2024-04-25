package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_TR_I001;
import com.syscom.fep.vo.text.ims.AB_TR_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ABTRI001 extends ACBSAction {

    public ABTRI001(MessageBase txData) {
        super(txData, new AB_TR_O001());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);

    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        /* TITA 請參考合庫主機電文規格(AB_TR_I001) */
        // Header
        AB_TR_I001 cbsTita = new AB_TR_I001();
        if("EAT".equals(feptxn.getFeptxnChannel())){
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getAtmData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getATMDATA().substring(758,778)); // LL+DATA
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            cbsTita.setTR_SPECIAL_FLAG(atmReq.getTACODE());
            /* 預約交易 */
            if ("RV".equals(atmReq.getLANGID())
                    && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {
                cbsTita.setEATM_RESERVE_FLAG("Y");
                cbsTita.setORIGINAL_TX_DAYTIME(atmReq.getPIEODT());
            }
            cbsTita.setTO_ACT_MEMO(feptxn.getFeptxnPsbremFC());
            cbsTita.setFROM_ACT_MEMO(feptxn.getFeptxnPsbremFD());
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
            // ATM交易序號
            cbsTita.setATMTRANSEQ(atmReq.getTRANSEQ());
        }else{
            ATMGeneralRequest atmReq = this.getAtmRequest();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); //TAC(未轉ASC，為原始電文值)
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            cbsTita.setTR_SPECIAL_FLAG(atmReq.getTACODE());
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
            // ATM交易序號
            cbsTita.setATMTRANSEQ(atmReq.getTRANSEQ());
        }
        cbsTita.setIMS_TRANS("MFEPAT00");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        // TXN_FLOW
        if (0 == feptxn.getFeptxnFiscFlag()) {
            cbsTita.setTXN_FLOW("C"); // 自行
        } else {
            cbsTita.setTXN_FLOW("A"); // 代理
        }
        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        // PROCESS TYPE
        if ("0".equals(txType)) { // 查詢、檢核
            cbsTita.setPROCESS_TYPE("CHK");
        } else if ("1".equals(txType)) { // 入扣帳
            cbsTita.setPROCESS_TYPE("ACCT");
        } else if ("2".equals(txType)) { // 沖正
            cbsTita.setPROCESS_TYPE("RVS");
        }
        // 財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan());
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        } else {
            cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        }

        // Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
        cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno());
        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
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
        /* 電文內容格式請參照TOTA電文格式(AB_TR_O001) */
        /* 拆解主機回應電文 */
        AB_TR_O001 tota = new AB_TR_O001();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);

        /* 更新交易 */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota);

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新交易
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(AB_TR_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        if (!cbsTota.getIMSRC_TCB().equals("000")) {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            feptxn.setFeptxnAccType((short) 0); //未記帳
            rtnCode = FEPReturnCode.CBSCheckError;
        }else if(!cbsTota.getIMSRC4_FISC().equals("4001") && !cbsTota.getIMSRC4_FISC().equals("0000")) {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
            feptxn.setFeptxnAccType((short) 0); //未記帳
            rtnCode = FEPReturnCode.CBSCheckError;
        }else {
            if(cbsTota.getIMSRC_TCB().equals("000")) {
                feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
            }else {
                feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            }
            // CBS 帳務日(民國年須轉西元年)
            String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
            if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                    || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
                imsbusinessDate = "00000000";
            } else {
                imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
            }
            feptxn.setFeptxnTbsdy(imsbusinessDate);
            // 記帳類別
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            }
            // 合庫轉出帳務分行
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            // 合庫轉入帳務分行
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // ATM清算分行別取合庫轉出帳務分行
            feptxn.setFeptxnAtmBrno(feptxn.getFeptxnBrno());
            // 交易帳號掛帳行
            if (feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                feptxn.setFeptxnTxBrno(feptxn.getFeptxnBrno());
            }else if (feptxn.getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                feptxn.setFeptxnTxBrno(feptxn.getFeptxnTrinBrno());
            }
            // 主機交易時間
            feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
            //帳戶餘額(下送空白不更新，本行轉出主機才下送)
            if(cbsTota.getACTBALANCE() != null){
                feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            }
            // 手續費
            if(cbsTota.getTXNCHARGE() != null){
                feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
                feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            }

            //實際轉出金額(含手續費)
            // if(cbsTota.getTRANSAMTOUT() != null && cbsTota.getTRANSAMTOUT().compareTo(BigDecimal.ZERO) != 0){
            //     feptxn.setFeptxnTxAmtAct(cbsTota.getTRANSAMTOUT());
            // }

            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                //傳送通知代碼(CBS摘要)
				feptxn.setFeptxnCbsDscpt(cbsTota.getNOTICE_NUMBER());
            }
            // 帳戶補充資訊
            if (StringUtils.isNotBlank(cbsTota.getO_ACT())) {
                feptxn.setFeptxnAcctSup(cbsTota.getO_ACT());
            }

            // 促銷應用訊息
            if(StringUtils.isNotBlank(cbsTota.getLUCKYNO())){
                feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO());
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
    private void insertSMSMSG(AB_TR_O001 cbsTota) throws ParseException {
//        Smsmsg defSMSMSG = new Smsmsg();
//        Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(this.feptxn.getFeptxnTxDate(), this.feptxn.getFeptxnEjfno());
//        // 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//        // 以SMSMSG_TX_DATE及SMSMSG_EJFNO 為key讀取 Table
//        if (smsmsg == null) {
//            defSMSMSG.setSmsmsgTxDate(this.feptxn.getFeptxnTxDate());
//            defSMSMSG.setSmsmsgEjfno(this.feptxn.getFeptxnEjfno());
//            defSMSMSG.setSmsmsgTxDate(this.feptxn.getFeptxnTxDate());
//            defSMSMSG.setSmsmsgEjfno(this.feptxn.getFeptxnEjfno());
//            defSMSMSG.setSmsmsgStan(this.feptxn.getFeptxnStan());
//            defSMSMSG.setSmsmsgPcode(this.feptxn.getFeptxnPcode());
//            defSMSMSG.setSmsmsgTroutActno(this.feptxn.getFeptxnTroutActno());
//            defSMSMSG.setSmsmsgTxTime(this.feptxn.getFeptxnTxTime());
//            defSMSMSG.setSmsmsgZone("TWN");
//            defSMSMSG.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//            defSMSMSG.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//            defSMSMSG.setSmsmsgTxCur(this.feptxn.getFeptxnTxCur());
//            defSMSMSG.setSmsmsgTxAmt(this.feptxn.getFeptxnTxAmt());
//            defSMSMSG.setSmsmsgTxCurAct(this.feptxn.getFeptxnTxCurAct());
//            defSMSMSG.setSmsmsgTxAmtAct(this.feptxn.getFeptxnTxAmtAct());
//            defSMSMSG.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//            defSMSMSG.setSmsmsgNotifyFg("Y");
//            defSMSMSG.setSmsmsgSendType(cbsTota.getNOTICE_TYPE());
//            defSMSMSG.setSmsmsgChannel(this.feptxn.getFeptxnChannel());
//            Date datenow = (Date) new SimpleDateFormat("yyyy/MM/DD-HH.mm.ss.SSS").parse(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_TAX));
//            defSMSMSG.setUpdateTime(datenow);
//            defSMSMSG.setSmsmsgNoticeNo(cbsTota.getNOTICE_NUMBER());
//
//            if (smsmsgExtMapper.insert(defSMSMSG) <= 0) {
//                getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//                this.logMessage(getLogContext());
//            }
//        }
    }
}
