package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.EaitxnMapper;
import com.syscom.fep.mybatis.model.Eaitxn;
import com.syscom.fep.mybatis.model.Npsunit;
//import com.syscom.fep.mybatis.model.Smsmsg;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.ims.AB_PY_I001;
import com.syscom.fep.vo.text.ims.AB_PY_O001;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 代理全繳交易上送主機
 *
 * @author vincent
 */

public class ABPYEAI01 extends ACBSAction {

    public ABPYEAI01(MessageBase txData) {
        super(txData, new AB_PY_O001());
    }

    RCV_NB_GeneralTrans_RQ atmReq = this.getNBRequest();

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
    private NpsunitExtMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);

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
        /* TITA 請參考合庫主機電文規格(AB_PY_I001) */
        // Header
        AB_PY_I001 cbstita = new AB_PY_I001();

        // Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

        // if (eaitxn == null) {
        //     eaitxn = new Eaitxn();
        // }
        cbstita.setIMS_TRANS("MFEPEA00");
        cbstita.setSYSCODE("FEP");
        cbstita.setSYS_DATETIME(
                FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        // TXN_FLOW
        if (feptxn.getFeptxnFiscFlag() == 0) {
            cbstita.setTXN_FLOW("C"); // 自行
        } else {
            cbstita.setTXN_FLOW("A"); // 代理
        }
        cbstita.setMSG_CAT(atmReq.getBody().getRq().getSvcRq().getTXNTYPE().trim());
        cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbstita.setPCODE(feptxn.getFeptxnPcode());
        cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        // PROCESS TYPE
        if ("0".equals(txType)) { // 查詢、檢核
            cbstita.setPROCESS_TYPE("CHK");
        } else if ("1".equals(txType)) { // 入扣帳
            cbstita.setPROCESS_TYPE("ACCT");
        } else if ("2".equals(txType)) { // 沖正
            cbstita.setPROCESS_TYPE("RVS");
        } else if ("4".equals(txType)) { // 解圈
            cbstita.setPROCESS_TYPE("REL");
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
        if ("NAM".equals(feptxn.getFeptxnChannel())) { /* 批次轉即時 */
            cbstita.setCARDTYPE("I");
            cbstita.setMSG_CAT("BA");
        } else {
            cbstita.setCARDTYPE("N");
        }
        /* 第一道圈存為 “0000”，財金回覆後，上送 FEPTXN_REP_RC，扣帳或解圈 */
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        } else {
            cbstita.setRESPONSE_CODE("0000"); // 正常才上送
        }

        // Detail
        cbstita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbstita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbstita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
        cbstita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        cbstita.setTXNICCTAC("40404040404040404040");
        // if (eaitxn.getEaitxnTransamtin() != null && !eaitxn.getEaitxnTransamtin().setScale(0).equals(new BigDecimal(0))) {
        //     cbstita.setTXNAMT(eaitxn.getEaitxnTransamtin());
        // } else {
            cbstita.setTXNAMT(feptxn.getFeptxnTxAmt());
        // }
        cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
        if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
            cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
        } else {
            cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        }
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
        } else {
            cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        }
        if ("256".equals(StringUtils.substring(feptxn.getFeptxnPcode(), 0, 3)) // 繳費移轉計畫
                && "18888888".equals(feptxn.getFeptxnBusinessUnit()) && "59999".equals(feptxn.getFeptxnPaytype())
                && "9999".equals(feptxn.getFeptxnPayno()) && "99991231".equals(feptxn.getFeptxnDueDate())) {
            cbstita.setPROCESS_TYPE("ACCT");
            cbstita.setPY_SPECIAL_FLAG("ET");
        }
        // 取得財金全繳委託單位檔之「帳務代理銀行」
        // 讀取 委託單位檔 NPSUNIT.NPSUNIT_BKNO
        Npsunit npsunit = npsunitExtMapper.selectByPrimaryKey(feptxn.getFeptxnBusinessUnit(), feptxn.getFeptxnPaytype(),
                feptxn.getFeptxnPayno());
        if (npsunit == null) {
            cbstita.setPY_HOST_BRANCH(" ");
        } else {
            cbstita.setPY_HOST_BRANCH(npsunit.getNpsunitBkno());
        }
        cbstita.setPY_PAYUNTNO(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
        cbstita.setPY_TAXTYPE(feptxn.getFeptxnPaytype()); // 繳費類別
        cbstita.setPY_PAYFEENO(feptxn.getFeptxnPayno()); // 費用代號
        cbstita.setPY_PAYTXNOL(feptxn.getFeptxnReconSeqno()); // 銷帳編號
        cbstita.setPY_PAYDDATE(feptxn.getFeptxnDueDate()); // 繳款期限
        if ("A".equals(cbstita.getTXN_FLOW()) // 代理記帳須提供
                && "ACCT".equals(cbstita.getPROCESS_TYPE())) {
            // 使用 DecimalFormat 将结果格式化为 4 位数
            DecimalFormat df = new DecimalFormat("0000");
            cbstita.setPY_CHARGCUS(new BigDecimal(df.format(feptxn.getFeptxnNpsFeeCustpay().multiply(new BigDecimal("10")))));
            cbstita.setPY_CHARGUNT(new BigDecimal(
                        df.format(feptxn.getFeptxnNpsFeeRcvr().multiply(new BigDecimal("10")))+
                            df.format(feptxn.getFeptxnNpsFeeAgent().multiply(new BigDecimal("10")))+
                            df.format(feptxn.getFeptxnNpsFeeTrout().multiply(new BigDecimal("10")))+
                            df.format(feptxn.getFeptxnNpsFeeTrin().multiply(new BigDecimal("10")))+
                            df.format(feptxn.getFeptxnNpsFeeFisc().multiply(new BigDecimal("10")))));
        }
        //@@@@@cbstita.setPY_IDNO(feptxn.getFeptxnIdno().trim());
        cbstita.setPY_IDNO(atmReq.getBody().getRq().getSvcRq().getPAYDATA().getNPID());
        cbstita.setChannel_Charge("00");
        cbstita.setAE_PYAQBRH(atmReq.getBody().getRq().getSvcRq().getPAYDATA().getNPBRANCH());
        cbstita.setPY_SSLTYPE(atmReq.getBody().getRq().getSvcRq().getSSLTYPE());
        
        this.setoTita(cbstita);
        this.setTitaToString(cbstita.makeMessage());
        this.setASCIItitaToString(cbstita.makeMessageAscii());
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
        /* 電文內容格式請參照TOTA電文格式(AB_PY_O001) */
        /* 拆解主機回應電文 */
        AB_PY_O001 tota = new AB_PY_O001();
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
    public FEPReturnCode updateFEPTxn(AB_PY_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        if(StringUtils.isNotBlank(cbsTota.getSEND_FISC2160())){
            feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160());
        }
        /* 變更FEPTXN交易記錄 */
        // IMSRC_TCB = "000" or empty表交易成功
        // IMSRC_FISC = "0000" or "4001" 表交易成功
        if (!"000".equals(cbsTota.getIMSRC_TCB()) && StringUtils.isNotBlank(cbsTota.getIMSRC_TCB())) {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            feptxn.setFeptxnAccType((short) 0); // 未記帳
//			feptxn.setFeptxnErrMsg(cbsTota.getERR_MEMO());
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
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
            } else {
                feptxn.setFeptxnAccType((short) 0); //為記帳
            }
            // 帳務分行
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 主機交易時間
            feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
            /* 主機回傳的手續費 */
            // 手續費
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
            if ("TWD".equals(feptxn.getFeptxnTxCur())) { //台幣手續費
                feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            } else { //外幣手續費
                if (StringUtils.isNotBlank(String.valueOf(feptxn.getFeptxnExrate()))) {
                    feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE().divide(feptxn.getFeptxnExrate(), 2, RoundingMode.HALF_UP));
                }
            }

            //實際轉出金額(含手續費)
            /* 00.06
            if (cbsTota.getTRANSAMTOUT() != null && StringUtils.isNotBlank(cbsTota.getTRANSAMTOUT().toString())){
                feptxn.setFeptxnTxAmtAct(cbsTota.getTRANSAMTOUT());
            }
            */

            //帳戶餘額
            feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            //可用餘額
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());

            feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
            feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                /* 寫入簡訊資料檔 */
                this.insertSMSMSG(cbsTota);
            }

            /* 00.06
            if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCHOUT())) {
                feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCHOUT());
            }
            */
            
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
    private void insertSMSMSG(AB_PY_O001 cbsTota) throws ParseException {
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
//            smsmsg.setSmsmsgZone("« TWN »");
//            smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//            smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//            smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//            smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//            smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//            smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//            smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//            smsmsg.setSmsmsgNotifyFg("« Y »");
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
