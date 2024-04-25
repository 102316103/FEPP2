package com.syscom.fep.server.common.cbsprocess;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

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
import com.syscom.fep.vo.text.ims.AB_TAX_I001;
import com.syscom.fep.vo.text.ims.AB_TAX_O001;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;

public class ABTAXEAI01 extends ACBSAction {

    public ABTAXEAI01(MessageBase txType) {
        super(txType, new AB_TAX_O001());
    }

    RCV_NB_GeneralTrans_RQ tita = this.getNBRequest();
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
        /* TITA 請參考合庫主機電文規格(AB_TAX_I001) */
        // Header
        AB_TAX_I001 cbsTita = new AB_TAX_I001();

        Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

        if (eaitxn == null) {
            eaitxn = new Eaitxn();
        }
        cbsTita.setIMS_TRANS("MFEPEA00");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //格式:YYYYMMDDHHMMSS
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        //TXN_FLOW
        if ("0".equals(feptxn.getFeptxnFiscFlag())) {
            cbsTita.setTXN_FLOW("C"); //自行
        } else {
            cbsTita.setTXN_FLOW("A"); //代理
        }
        cbsTita.setMSG_CAT(tita.getBody().getRq().getSvcRq().getTXNTYPE());
        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        //PROCESS_TYPE
        if ("0".equals(txType)) { //查詢、檢核
            cbsTita.setPROCESS_TYPE("CHK");
        } else if ("1".equals(txType)) { //入扣帳
            cbsTita.setPROCESS_TYPE("ACCT");
        } else if ("2".equals(txType)) { // 沖正
            cbsTita.setPROCESS_TYPE("RVS");
        } else if ("4".equals(txType)) { // 解圈
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
        cbsTita.setCARDTYPE("N"); // 交易卡片型態

        /*第一道圈存為 “0000”，財金回覆後，上送 FEPTXN_REP_RC，扣帳或解圈 */
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        } else {
            cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        }

        // Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
        cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        cbsTita.setTXNICCTAC("40404040404040404040");

        if (StringUtils.isNotBlank(String.valueOf(eaitxn.getEaitxnTransamtin()))) {
            cbsTita.setTXNAMT(eaitxn.getEaitxnTransamtin());
        } else {
            cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
        }

        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno());
        if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
        } else {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        }
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTroutBkno7());
        } else {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        }
        cbsTita.setTAX_TYPE(feptxn.getFeptxnPaytype()); // 繳款類別
        if ("TY".equals(feptxn.getFeptxnTxCode())) { /* 核定稅非15類 */
            String date = String.valueOf(Integer.parseInt(feptxn.getFeptxnDueDate()) - 19110000);
            String duedate = "";
            if (date.length() == 6) {
                duedate = date;
            } else if (StringUtils.isNotBlank(date) && date.length() == 7) {
                duedate = date.substring(1, 7);
            }
            cbsTita.setTAX_END_DATE(duedate); // 繳納截止日
            cbsTita.setTAX_BILL_NO(feptxn.getFeptxnReconSeqno()); // 銷帳編號
        } else if ("TZ".equals(feptxn.getFeptxnTxCode())) { /* ‘TZ’自繳15類 */
            cbsTita.setTAX_ORGAN(feptxn.getFeptxnTaxUnit()); // 稽徵機關
            cbsTita.setTAX_CID(feptxn.getFeptxnIdno()); // 身分證號/統一編號
        }
        cbsTita.setAE_TRNSFLAG(tita.getBody().getRq().getSvcRq().getTRNSFLAG());
        cbsTita.setAE_TRNSFROUTIDNO(feptxn.getFeptxnIdno());
        cbsTita.setAE_BUSINESSTYPE(tita.getBody().getRq().getSvcRq().getBUSINESSTYPE());
        /* 手續費負擔別 */
        if ("2".equals(feptxn.getFeptxnNpsClr())) {
            cbsTita.setAE_AEIEFEET("13");
        } else if ("3".equals(feptxn.getFeptxnNpsClr())) {
            cbsTita.setAE_AEIEFEET("14");
        } else if ("1".equals(feptxn.getFeptxnNpsClr())) {
            cbsTita.setAE_AEIEFEET("15");
        }
        cbsTita.setAE_TRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
        cbsTita.setAE_AEISLLTY(tita.getBody().getRq().getSvcRq().getSSLTYPE());
        cbsTita.setAE_LIMITTYPE(tita.getBody().getRq().getSvcRq().getLIMITTYPE());
        cbsTita.setAE_AEIFIXFE(tita.getBody().getRq().getSvcRq().getFAXFEE());
        cbsTita.setAE_AEINETFE(tita.getBody().getRq().getSvcRq().getTRANSFEE());
        cbsTita.setAE_AEIEDIFE(tita.getBody().getRq().getSvcRq().getOTHERBANKFEE());
        cbsTita.setAE_AEICIRCU(tita.getBody().getRq().getSvcRq().getCUSTCODE());

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
    public FEPReturnCode updateFEPTxn(AB_TAX_O001 cbsTota) throws Exception {
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
//            feptxn.setFeptxnErrMsg(cbsTota.getERR_MEMO());
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
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
            /* 主機回傳的手續費 */
            // 手續費
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                /* 寫入簡訊資料檔 */
                this.insertSMSMSG(cbsTota);
            }
            //實際轉出金額(含手續費)
            if (cbsTota.getTRANSAMTOUT() != null && StringUtils.isNotBlank(cbsTota.getTRANSAMTOUT().toString())){
                feptxn.setFeptxnTxAmtAct(cbsTota.getTRANSAMTOUT());
            }
            // 帳戶餘額
            if ("2532".equals(feptxn.getFeptxnPcode())) { // 本行帳戶繳稅
                feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            }
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
            // 轉出分行
            if ("2532".equals(feptxn.getFeptxnPcode()) && "RQ".equals(tita.getBody().getRq().getSvcRq().getTXNTYPE())) { // 自行繳稅
                feptxn.setFeptxnTroutBkno7(cbsTota.getTAX_FROM_BRANCH());
            }
        }

        if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCHOUT())) {
            feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCHOUT());
        }

//        if ("TZ".equals(feptxn.getFeptxnTxCode()) || "TY".equals(feptxn.getFeptxnTxCode())) {
//            feptxn.setFeptxnMsgflow(feptxn.getFeptxnMsgflow().substring(0, 1) + "2");
//            feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
//            rtnCode = FEPReturnCode.Normal; /* 收到主機回應, 改成 Normal */
//        }
        rtnCode = FEPReturnCode.Normal; /* 收到主機回應, 改成 Normal */
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
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
