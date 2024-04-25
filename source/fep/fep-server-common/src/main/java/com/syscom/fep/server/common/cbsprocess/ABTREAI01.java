package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.EaitxnMapper;
import com.syscom.fep.mybatis.model.Eaitxn;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.ims.AB_TR_I001;
import com.syscom.fep.vo.text.ims.AB_TR_O001;
import com.syscom.fep.vo.text.ims.CB_TR_I001;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ABTREAI01 extends ACBSAction {

    public ABTREAI01(MessageBase txType) {
        super(txType, new AB_TR_O001());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
    private EaitxnMapper eaitxnMapper = SpringBeanFactoryUtil.getBean(EaitxnMapper.class);

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

        if ("HCA".equals(feptxn.getFeptxnChannel())) {
            CB_TR_I001 cbsTita = new CB_TR_I001();
            buildHCE(cbsTita, txType);
        } else {
            // Header
            AB_TR_I001 cbsTita = new AB_TR_I001();
            cbsTita.setIMS_TRANS("MFEPEA00");
            cbsTita.setSYSCODE("FEP");
            cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //格式:YYYYMMDDHHMMSS
            cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));

            //TXN FLOW
            if (0 == feptxn.getFeptxnFiscFlag()) {
                cbsTita.setTXN_FLOW("C"); // 自行
            } else {
                cbsTita.setTXN_FLOW("A"); // 代理
            }

            cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
            cbsTita.setPCODE(feptxn.getFeptxnPcode());
            cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());

            //PROCESS_TYPE
            if ("0".equals(txType)) { //查詢、檢核
                cbsTita.setPROCESS_TYPE("CHK");
            } else if ("1".equals(txType)) { //入扣帳
                cbsTita.setPROCESS_TYPE("ACCT");
            } else if ("2".equals(txType)) { //沖正
                cbsTita.setPROCESS_TYPE("RVS");
            } else if ("4".equals(txType)) { //解圈
                cbsTita.setPROCESS_TYPE("REL");
            }

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

            /*第一道圈存為 “0000”，財金回覆後，上送 FEPTXN_REP_RC，扣帳或解圈 */
            if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
                cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
            } else {
                cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
            }

            //Detail
            cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
            cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
            cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());

            cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
            if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
                cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
            } else {
                cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
            }
            if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
                cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
            } else {
                cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
            }

            cbsTita.setTOACT(feptxn.getFeptxnTrinActno());
            cbsTita.setTXMEMO(feptxn.getFeptxnChrem());  /*若是Unicode轉成EBCDIC*/
            cbsTita.setI_ACT(feptxn.getFeptxnAcctSup());

            if ("HCA".equals(feptxn.getFeptxnChannel())) {
                RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getHCERequest().getBody().getRq().getSvcRq();
                cbsTita.setMSG_CAT(tita.getTXNTYPE().trim());
                cbsTita.setCARDTYPE("K"); // 交易卡片型態

                if (StringUtils.isNotBlank(tita.getICMARK())) {
                    cbsTita.setICMEMO(tita.getICMARK());
                } else {
                    cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
                }

                // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno()) || "K".equals(cbsTita.getCARDTYPE()) || "RQ".equals(tita.getTXNTYPE())) { //第一次上送CBS
                    cbsTita.setTXNICCTAC(tita.getIC_TAC_LEN() + tita.getIC_TAC().substring(0, 16)); //LL+DATA
                    cbsTita.setTERMTXN_DATETIME(tita.getIC_TAC_DATE() + tita.getIC_TAC_TIME());
                } else {
                    cbsTita.setTXNICCTAC("40404040404040404040");
                }

                /*TXNAMT 格式S9(11)V99 右靠左補0，後面補上二位小數位，前面有正負*/
                cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
                cbsTita.setTR_SPECIAL_FLAG(tita.getTRANSTYPEFLAG());

            } else {
                RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getNBRequest().getBody().getRq().getSvcRq();
                cbsTita.setMSG_CAT(tita.getTXNTYPE().trim());
                /*無卡交易，ICMEMO、ICTAC 放入空白*/
                Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

                cbsTita.setCARDTYPE("N"); // 交易卡片型態
                cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
                cbsTita.setTXNICCTAC("40404040404040404040");

                if (eaitxn != null && StringUtils.isNotBlank(String.valueOf(eaitxn.getEaitxnTransamtin()))) {
                    cbsTita.setTXNAMT(eaitxn.getEaitxnTransamtin());
                } else {
                    cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
                }
                if (eaitxn != null && StringUtils.isNotBlank(String.valueOf(eaitxn.getEaitxnCustpayFee()))) {
                    cbsTita.setCHANNEL_CHARGE(eaitxn.getEaitxnCustpayFee());
                } else {
                    cbsTita.setCHANNEL_CHARGE(feptxn.getFeptxnFeeCustpay());
                }

                cbsTita.setAE_TRNSFROUTIDNO(feptxn.getFeptxnIdno());
                cbsTita.setAE_TRNSFLAG(tita.getTRNSFLAG());
                cbsTita.setAE_BUSINESSTYPE(tita.getBUSINESSTYPE());

                /* 手續費負擔別*/
                if (eaitxn != null && StringUtils.isNotBlank(String.valueOf(eaitxn.getEaitxnFeepaymenttype()))) {
                    cbsTita.setAE_AEIEFEET(eaitxn.getEaitxnFeepaymenttype());
                } else {
                    cbsTita.setAE_AEIEFEET(tita.getFEEPAYMENTTYPE());
                }

                cbsTita.setAE_TRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
                cbsTita.setAE_AEISLLTY(tita.getSSLTYPE());
                cbsTita.setAE_LIMITTYPE(tita.getLIMITTYPE());
                cbsTita.setAE_AEIFIXFE(tita.getFAXFEE());
                cbsTita.setAE_AEINETFE(tita.getTRANSFEE());
                cbsTita.setAE_AEIEDIFE(tita.getOTHERBANKFEE());
                cbsTita.setAE_AEICIRCU(tita.getCUSTCODE());

                if (eaitxn != null && StringUtils.isNotBlank(eaitxn.getEaitxnChafeeType())) {
                    cbsTita.setAE_AEIDSTYP(eaitxn.getEaitxnChafeeType());
                } else {
                    cbsTita.setAE_AEIDSTYP(tita.getCHAFEE_TYPE());
                }
                if (eaitxn != null && StringUtils.isNotBlank(eaitxn.getEaitxnChafeeBranch())) {
                    cbsTita.setAE_AEIDSBRH(eaitxn.getEaitxnChafeeBranch());
                } else {
                    cbsTita.setAE_AEIDSBRH(tita.getCHAFEE_BRANCH());
                }
                if (eaitxn != null && StringUtils.isNotBlank(String.valueOf(eaitxn.getEaitxnChafeeAmt()))) {
                    cbsTita.setAE_AEIDSCRG(eaitxn.getEaitxnChafeeAmt());
                } else {
                    cbsTita.setAE_AEIDSCRG(tita.getCHAFEE_AMT());
                }

                // AE_TRANSAMTOUT 格式 S9(11)V99(兩位小數) 右靠左補0，前面有正負
                if (eaitxn != null && StringUtils.isNotBlank(String.valueOf(eaitxn.getEaitxnTransamtout()))) {
                    cbsTita.setAE_TRANSAMTOUT(eaitxn.getEaitxnTransamtout());
                } else {
                    BigDecimal plus = feptxn.getFeptxnTxAmt().add(feptxn.getFeptxnFeeCustpay());
                    cbsTita.setAE_TRANSAMTOUT(plus);
                }

                cbsTita.setTR_SPECIAL_FLAG(tita.getTRANSTYPEFLAG());

            }

            this.setoTita(cbsTita);
            this.setTitaToString(cbsTita.makeMessage());
            this.setASCIItitaToString(cbsTita.makeMessageAscii());
        }

        return FEPReturnCode.Normal;

    }

    /**
     * 組合HCE
     *
     * @param cbsTita
     * @return
     * @throws Exception
     */
    private CB_TR_I001 buildHCE(CB_TR_I001 cbsTita, String txType) throws Exception {
        cbsTita.setIMS_TRANS("MFEPEA00");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //格式:YYYYMMDDHHMMSS
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));

        //TXN FLOW
        if (0 == feptxn.getFeptxnFiscFlag()) {
            cbsTita.setTXN_FLOW("C"); // 自行
        } else {
            cbsTita.setTXN_FLOW("A"); // 代理
        }

        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());

        //PROCESS_TYPE
        if ("0".equals(txType)) { //查詢、檢核
            cbsTita.setPROCESS_TYPE("CHK");
        } else if ("1".equals(txType)) { //入扣帳
            cbsTita.setPROCESS_TYPE("ACCT");
        } else if ("2".equals(txType)) { //沖正
            cbsTita.setPROCESS_TYPE("RVS");
        } else if ("4".equals(txType)) { //解圈
            cbsTita.setPROCESS_TYPE("REL");
        }

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

        /*第一道圈存為 “0000”，財金回覆後，上送 FEPTXN_REP_RC，扣帳或解圈 */
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        } else {
            cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        }

        //Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());

        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
        if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
        } else {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        }
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
        } else {
            cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        }

        cbsTita.setTOACT(feptxn.getFeptxnTrinActno());
        cbsTita.setTXMEMO(feptxn.getFeptxnChrem());  /*若是Unicode轉成EBCDIC*/
        cbsTita.setI_ACT(feptxn.getFeptxnAcctSup());

        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getHCERequest().getBody().getRq().getSvcRq();
        cbsTita.setMSG_CAT(tita.getTXNTYPE().trim());
        cbsTita.setCARDTYPE("K"); // 交易卡片型態

        if (StringUtils.isNotBlank(tita.getICMARK())) {
            cbsTita.setICMEMO(tita.getICMARK());
        } else {
            cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        }

        // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno()) || "K".equals(cbsTita.getCARDTYPE()) || "RQ".equals(tita.getTXNTYPE())) { //第一次上送CBS
            cbsTita.setTXNICCTAC(tita.getIC_TAC_LEN() + tita.getIC_TAC().substring(0, 16)); //LL+DATA
            cbsTita.setTERMTXN_DATETIME(tita.getIC_TAC_DATE() + tita.getIC_TAC_TIME());
        } else {
            cbsTita.setTXNICCTAC("40404040404040404040");
        }

        /*TXNAMT 格式S9(11)V99 右靠左補0，後面補上二位小數位，前面有正負*/
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
        cbsTita.setTR_SPECIAL_FLAG(tita.getTRANSTYPEFLAG());
        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());

        return cbsTita;
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
    public FEPReturnCode updateFEPTxn(AB_TR_O001 cbsTota) throws Exception {
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
//            feptxn.setFeptxnErrMsg(cbsTota.getERR_MEMO());
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
            // 訊息流程
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
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            } else {
                feptxn.setFeptxnAccType((short) 0); // 未記帳
            }
            // 帳務分行
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 主機交易時間
            feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
            // 繳費作業手續費(轉出客戶)
            feptxn.setFeptxnNpsFeeCustpay(cbsTota.getTXNCHARGE());
            // 可用餘額
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
            // 帳戶餘額
            feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            // 手續費
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
            /* 主機回傳的手續費 */
            if ("TWD".equals(feptxn.getFeptxnTxCur())) { // 台幣手續費
                feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            } else { // 外幣手續費
                BigDecimal fee = BigDecimal.ZERO;
                if (feptxn.getFeptxnFeeCustpayAct() != null && feptxn.getFeptxnFeeCustpayAct().compareTo(BigDecimal.ZERO) == 1
                        && feptxn.getFeptxnExrate() != null && feptxn.getFeptxnExrate().compareTo(BigDecimal.ZERO) == 1) {
                    fee = (feptxn.getFeptxnFeeCustpayAct()).divide(feptxn.getFeptxnExrate(), 2, RoundingMode.HALF_UP);
                }
                feptxn.setFeptxnFeeCustpay(fee);
                /* 依匯率及CBS算出的手續費回推提領幣別手續費(此為參考值) */
            }

            //實際轉出金額(含手續費)
            // if (cbsTota.getTRANSAMTOUT() != null && StringUtils.isNotBlank(cbsTota.getTRANSAMTOUT().toString())) {
            //     feptxn.setFeptxnTxAmtAct(cbsTota.getTRANSAMTOUT());
            // }

            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                /* 寫入簡訊資料檔 */
                this.insertSMSMSG(cbsTota);
            }
            // 帳戶補充資訊
            if (StringUtils.isNotBlank(cbsTota.getO_ACT())) {
                feptxn.setFeptxnAcctSup(cbsTota.getO_ACT());
            }

            rtnCode = FEPReturnCode.Normal; /* 收到主機回應, 改成 Normal */
        }
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
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

    public Object getInstanceObject(String cbsProcessName, MessageBase atmData) {
        Class<?> c = null;
        //java.lang.reflect.Field[] fields = null;
        //String imsPackageName = "";
        //Class<?> imsClassName = null;
        Class<?>[] params = {MessageBase.class};
        Object o = null;
        try {
            //獲得cbsProcessn物件名稱
            c = Class.forName("com.syscom.fep.server.common.cbsprocess." + cbsProcessName);
            //獲得cbsProcessn物件裡所有欄位名稱
            //fields = c.getDeclaredFields();
            //使用cbsProcessn以獲得ims電文物件名稱
            //imsPackageName = fields[0].toString().split(" ")[1];
            //獲得ims電文物件名稱
            //imsClassName = Class.forName(imsPackageName);
            //獲得ims實例化電文物件
            o = c.getConstructor(params).newInstance(atmData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return o;
    }
}
