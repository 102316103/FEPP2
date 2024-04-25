package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.IB_TR_I011;
import com.syscom.fep.vo.text.ims.IB_TR_O011;
import com.syscom.fep.vo.text.ims.IB_WD_O001;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存轉帳Request交易電文
 *
 * @author vincent
 */

public class IBTRI011 extends ACBSAction {

    public IBTRI011(MessageBase txData) {
        super(txData, new IB_TR_I011());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);

    /**
     * 組CBS 原存交易Request電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        //組CBS 原存交易Request電文, 電文內容格式請參照: D_I1_合庫FEP_主機電文規格-原存行轉帳交易V1.0(111xxxx).doc
        IB_TR_I011 cbsTita = new IB_TR_I011();
        // HEADER
        cbsTita.setIMS_TRANS("MFEPFG00"); // 主機業務別 長度8
        cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
        cbsTita.setSYS_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime()); // FEP交易日期時間 長度14
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
        cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 固定給00 */
        cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
        cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
        cbsTita.setFSCODE(""); // 合庫FS-CODE 長度2 原存行交易放空白
        if ("1".equals(txType)) {
            cbsTita.setPROCESS_TYPE("ACCT"); // 交易處理類別 長度4 /* 帳務 */
        } else {
            cbsTita.setPROCESS_TYPE("CHK"); // 交易處理類別 長度4 /* 檢核 */
        }
        // 財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno()); // 設備代理行 長度3
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan()); // 跨行交易序號 長度7
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());// 端末機代號 長度8
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType()); // 端末設備型態 長度4
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno()); // 發卡行/扣款行 長度3
        cbsTita.setCARDTYPE("K"); // 交易卡片型態 長度1 /* K:晶片卡 */
        cbsTita.setRESPONSE_CODE(feptxn.getFeptxnReqRc()); // 回應代號(RC) 長度4
        /* CBS Request DETAIL */
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno()); // IC卡交易序號
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk()); // 端末設備查核碼
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDatetimeFisc()); // 交易日期時間
        if(StringUtils.isNotBlank(this.getInbkRequest().getICMARK())){
            cbsTita.setICMEMO(this.getInbkRequest().getICMARK()); // IC卡備註欄
        }else{
            cbsTita.setICMEMO(StringUtils.repeat("40",30)); // IC卡備註欄
        }
        /*
         * FEPTXN 為Varch(16), 財金規格B(V)(LL+DATA)合庫固定DATA為8位LL + X(08) , 必須把FEPTXN_IC_TAC
         * 16 byte pack成8byte
         */
        if (StringUtils.isNotBlank(this.getInbkRequest().getTAC())) {
            cbsTita.setTXNICCTAC("000A" + this.getInbkRequest().getTAC()); // 交易驗證碼
        }else{
            cbsTita.setTXNICCTAC(StringUtils.repeat("40",10)); // 交易驗證碼
        }
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片提款帳號(轉出帳號)
        // 20221006 改用財金INBK Requst電文
        FISC_INBK reqINBK = this.getInbkRequest();
        cbsTita.setTransferee_Bank_ID(reqINBK.getTrinBkno()); // 轉入行庫
        cbsTita.setTransferor_Bank_ID(reqINBK.getTroutBkno()); // 轉出行庫
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno()); // 轉入帳號
        if ("Y".equals(feptxn.getFeptxnMtp())) { /* 手機門號跨行轉帳, 轉入銀行代號放0068999 */
            cbsTita.setTR_SPECIAL_FLAG("TM"); // 特殊轉帳類別註記 /* 手機門號轉帳 */
        } else if (Objects.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
                && "9999".equals(feptxn.getFeptxnNoticeId())) {
            cbsTita.setTR_SPECIAL_FLAG("DC"); // 特殊轉帳類別註記 /* 跨行存款-存卡片 */
        } else if (Objects.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
                && "9998".equals(feptxn.getFeptxnNoticeId())) {
            cbsTita.setTR_SPECIAL_FLAG("DA"); // 特殊轉帳類別註記 /* 跨行存款-存帳號 */
        } else if ("941".equals(feptxn.getFeptxnBkno()) && "NMACNMAC".equals(feptxn.getFeptxnAtmno())) {
            cbsTita.setTR_SPECIAL_FLAG("NM"); // 特殊轉帳類別註記 /* 台灣PAY代發作業 */
        } else if ("949".equals(feptxn.getFeptxnBkno())) {
            cbsTita.setTR_SPECIAL_FLAG("BS"); // 特殊轉帳類別註記 /* 發票獎金即時入帳 */
        } else {
            cbsTita.setTR_SPECIAL_FLAG(StringUtils.repeat(" ", 2)); // 特殊轉帳類別註記 // 二個空白
        }
        // 20221025 Modify, 送IMS前需先轉碼, 不確定是要toBytes還是toHex, 但送IMS應是Hexstring, 故先用toHex
        String feptxnChrem = reqINBK.getCHREM();
        cbsTita.setTXMEMO(feptxnChrem);
        if(StringUtils.isNotBlank(feptxn.getFeptxnAcctSup())){
            cbsTita.setI_ACT_BIT57(feptxn.getFeptxnAcctSup()); // 帳戶補充資訊
        }
        cbsTita.setDRVS(StringUtils.repeat(" ", 296)); // 保留欄位
        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
        return FEPReturnCode.Normal;
    }

    /**
     * 拆解CBS TOTA電文
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 電文內容格式請參照TOTA電文格式(IB_TR_O011) */
        /* 拆解主機回應電文 */
        IB_TR_O011 tota = new IB_TR_O011();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);

        /* 更新FEPTXN */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota, type);

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新FEPTXN
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(IB_TR_O011 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME()); /* 主機交易時間 */
        feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
        feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
        feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 帳戶手續費 */
        feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE()); /* 提領幣別手續費 */
        if (StringUtils.isNotBlank(cbsTota.getAVAILABLE_BALANCE())) {
            feptxn.setFeptxnBala(new BigDecimal(cbsTota.getAVAILABLE_BALANCE())); /* 可用餘額 */
        }
        feptxn.setFeptxnBalb(cbsTota.getACT_BALANCE()); /* 帳戶餘額 */

        // 20221026 已取消不新增此欄位, AA回財金Response 電文直接用CBS_TOTA
        // 20221031 因要寫至INBK2160 故還是UPDATE
        feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO()); /* 促銷應用訊息 */
        feptxn.setFeptxnAcctSup(cbsTota.getO_TR_BIT57()); /* 帳戶補充資訊 */
        feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160());
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
        // IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        if (!cbsTota.getIMSRC_TCB().equals("000")) {
            feptxn.setFeptxnAccType((short) 0); //未記帳
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            /* CBS回覆成功 */
            /* 主機回傳民國年須轉成西元年 */
            String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
            if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                    || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
                imsbusinessDate = "00000000";
            } else {
                imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
            }
            feptxn.setFeptxnTbsdy(imsbusinessDate);
            /* 2022/11/21 寫入帳務分行 */
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 記帳類別
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) { // 主機記帳狀況
                feptxn.setFeptxnAccType((short) 1); /* 已記帳 */
            } else {
                feptxn.setFeptxnAccType((short) 0); /* 未記帳 */
            }
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                /* 寫入簡訊資料檔 */
                this.insertSMSMSG(cbsTota);
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
    private void insertSMSMSG(IB_TR_O011 cbsTota) throws ParseException {
//        String txDate = feptxn.getFeptxnTxDate();
//        Integer ejfno = feptxn.getFeptxnEjfno();
//        Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//        // 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//        // 以SMSMSG_TX_DATE及SMSMSG_EJFNO 為key讀取 Table
//        if (smsmsg == null) {
//            smsmsg = new Smsmsg();
//            smsmsg.setSmsmsgTxDate(txDate);
//            smsmsg.setSmsmsgEjfno(ejfno);
//            smsmsg.setSmsmsgStan(feptxn.getFeptxnStan());
//            smsmsg.setSmsmsgPcode(feptxn.getFeptxnPcode());
//            smsmsg.setSmsmsgTroutActno(feptxn.getFeptxnTroutActno());
//            smsmsg.setSmsmsgTxTime(feptxn.getFeptxnTxTime());
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
