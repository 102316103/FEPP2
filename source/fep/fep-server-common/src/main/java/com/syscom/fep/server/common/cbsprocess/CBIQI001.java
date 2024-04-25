package com.syscom.fep.server.common.cbsprocess;

import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.CB_IQ_I001;
import com.syscom.fep.vo.text.ims.CB_IQ_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;

public class CBIQI001 extends ACBSAction {

    public CBIQI001(MessageBase txType) {
        super(txType, new CB_IQ_O001());
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
        /* TITA 請參考合庫主機電文規格(AB_IQ_I001) */
        // Header
        CB_IQ_I001 cbsTita = new CB_IQ_I001();
        if ("EAT".equals(feptxn.getFeptxnChannel())) {
            RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq = this.getEATMRequest().getBody().getRq().getSvcRq();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄(未轉ASC，為原始電文)
            cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); // TAC(未轉ASC，為原始電文值)
        } else {
            ATMGeneralRequest atmReq = this.getAtmRequest();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄(未轉ASC，為原始電文)
            cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); // TAC(未轉ASC，為原始電文值)
        }
        cbsTita.setIMS_TRANS("MFEPAT00");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        cbsTita.setTXN_FLOW("C"); // 自行
        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        cbsTita.setPROCESS_TYPE("CHK");
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
        cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        cbsTita.setATMTRANSEQ(feptxn.getFeptxnAtmSeqno());
        // Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片帳號
        // I2(同ID其他帳戶查詢餘額)、I4(金融卡存款前置查詢的存入帳號)
        if ("I2".equals(feptxn.getFeptxnTxCode()) || "I4".equals(feptxn.getFeptxnTxCode())) {
            cbsTita.setINQELSEACT(feptxn.getFeptxnTxActno());
            // I4交易要提供存款之存入銀行
            if ("I4".equals(feptxn.getFeptxnTxCode())) {
                cbsTita.setI4_CDMBANK(feptxn.getFeptxnTrinBkno());
            }
        }
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
        /* 電文內容格式請參照TOTA電文格式(AB_IQ_O001) */
        /* 拆解主機回應電文 */
        CB_IQ_O001 tota = new CB_IQ_O001();
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
    private FEPReturnCode updateFEPTxn(CB_IQ_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
        // IMSRC_TCB = "000" or empty表交易成功
        // IMSRC_FISC = "0000" or "4001" 表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        if (!cbsTota.getIMSRC_TCB().equals("000")) {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            feptxn.setFeptxnAccType((short) 0); //未記帳
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            // CBS 帳務日(本行營業日,民國年須轉西元年)
            String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
            if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                    || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
                imsbusinessDate = "00000000";
            } else {
                imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
            }
            feptxn.setFeptxnTbsdy(imsbusinessDate);
            // 記帳類別
            feptxn.setFeptxnAccType((short)0);
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            }
            // 主機交易時間
            feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
            if (cbsTota.getAVAILABLE_BALANCE() != null) {
                // 可用餘額
                feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
            }
            if (cbsTota.getACTBALANCE() != null) {
                // 帳戶餘額
                feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            }

            // 手續費
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
              //傳送通知代碼(CBS摘要)
				feptxn.setFeptxnCbsDscpt(cbsTota.getNOTICE_NUMBER());
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
    private void insertSMSMSG(CB_IQ_O001 cbsTota) throws ParseException {
//        Smsmsg defSMSMSG = new Smsmsg();
//        Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(this.feptxn.getFeptxnTxDate(), this.feptxn.getFeptxnEjfno());
//        // 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//        // 以SMSMSG_TX_DATE及SMSMSG_EJFNO 為key讀取 Table
//        if (smsmsg == null) {
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
