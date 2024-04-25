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
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.ims.AB_TAX_I001;
import com.syscom.fep.vo.text.ims.AB_TAX_O001;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ABTAXI001 extends ACBSAction {

    public ABTAXI001(MessageBase txData) {
        super(txData, new AB_TAX_O001());
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
        /* TITA 請參考合庫主機電文規格(AB_TAX_I001) */
        //Header
        AB_TAX_I001 cbsTita = new AB_TAX_I001();
        if ("EAT".equals(feptxn.getFeptxnChannel())) {
        	RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getEATMRequest().getBody().getRq().getSvcRq();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getATMDATA().substring(758,778)); // LL+DATA
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            if ("T6".equals(feptxn.getFeptxnTxCode()) || "T8".equals(feptxn.getFeptxnTxCode())) {
                cbsTita.setTAX_END_DATE(atmReq.getFADATA().substring(7,13));
                cbsTita.setTAX_BILL_NO(feptxn.getFeptxnReconSeqno());
            }
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
        } else {
            ATMGeneralRequest atmReq = this.getAtmRequest();
            cbsTita.setMSG_CAT(atmReq.getMSGTYP());
            cbsTita.setCARDTYPE(atmReq.getPICCDID()); // 交易卡片型態
            cbsTita.setTERMTXN_DATETIME(atmReq.getPICCBI19());
            // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                    && "K".equals(atmReq.getPICCDID())
                    && "AA".equals(atmReq.getMSGTYP())) { // 第一次上送CBS
                cbsTita.setTXNICCTAC(atmReq.getPICCTAC()); 
            }else{
                cbsTita.setTXNICCTAC(StringUtils.repeat("40",10));
            }
            if ("T6".equals(feptxn.getFeptxnTxCode()) || "T8".equals(feptxn.getFeptxnTxCode())) {
                cbsTita.setTAX_END_DATE(atmReq.getFADATA().substring(7,13));
                cbsTita.setTAX_BILL_NO(feptxn.getFeptxnReconSeqno());
            }
            cbsTita.setICMEMO(atmReq.getPICCBI55()); // IC卡備註欄 (未轉ASC，為原始電文)
        }
        cbsTita.setIMS_TRANS("MFEPAT00");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        cbsTita.setTXN_FLOW("A"); // 代理
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
        // ATM交易序號
        RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getEATMRequest().getBody().getRq().getSvcRq();
        cbsTita.setATMTRANSEQ(atmReq.getTRANSEQ());
        // Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmtAct());
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
        cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
        cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        cbsTita.setTAX_TYPE(feptxn.getFeptxnPaytype()); // 繳款類別
        if ("T5".equals(feptxn.getFeptxnTxCode()) || "T7".equals(feptxn.getFeptxnTxCode())) {
            cbsTita.setTAX_ORGAN(feptxn.getFeptxnTaxUnit());
            cbsTita.setTAX_CID(feptxn.getFeptxnIdno());
        }
        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
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
        /* 電文內容格式請參照TOTA電文格式(AB_TAX_O001) */
        /* 拆解主機回應電文 */
        AB_TAX_O001 tota = new AB_TAX_O001();
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
    private FEPReturnCode updateFEPTxn(AB_TAX_O001 cbsTota) throws Exception {
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
            // 帳務分行
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 主機交易時間
            feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
            /* 主機回傳的手續費 */
            // 手續費
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            // 帳戶餘額
            if ("2532".equals(feptxn.getFeptxnPcode())) { // 本行帳戶繳稅
                feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            }
            if ("EAT".equals(feptxn.getFeptxnChannel())) {
                RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReq =this.getEATMRequest().getBody().getRq().getSvcRq();
                if (FISCPCode.PCode2532.getValueStr().equals(feptxn.getFeptxnPcode()) && ("AA".equals(atmReq.getMSGTYP()) || "RQ".equals(atmReq.getMSGTYP()))) {
                    feptxn.setFeptxnTroutBkno7(cbsTota.getTAX_FROM_BRANCH());
                }
            } else {
                ATMGeneralRequest atmReq = this.getAtmRequest();
                if (FISCPCode.PCode2532.getValueStr().equals(feptxn.getFeptxnPcode()) && ("AA".equals(atmReq.getMSGTYP()) || "RQ".equals(atmReq.getMSGTYP()))) {
                    feptxn.setFeptxnTroutBkno7(cbsTota.getTAX_FROM_BRANCH());
                }
            }

            // 促銷應用訊息
            if(StringUtils.isNotBlank(cbsTota.getLUCKYNO())){
                feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO());
            }
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
    private void insertSMSMSG(AB_TAX_O001 cbsTota) throws ParseException {
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
