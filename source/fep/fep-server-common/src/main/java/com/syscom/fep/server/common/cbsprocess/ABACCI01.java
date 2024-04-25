package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
//import com.syscom.fep.mybatis.ext.mapper.SmsmsgExtMapper;
import com.syscom.fep.mybatis.mapper.EaitxnMapper;
import com.syscom.fep.mybatis.model.Eaitxn;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.ims.AB_ACC_I01;
import com.syscom.fep.vo.text.ims.AB_ACC_O01;
import com.syscom.fep.vo.text.ims.AB_ACC_O02;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ABACCI01 extends ACBSAction {

    public ABACCI01(MessageBase txType) {
        super(txType, new AB_ACC_O01());
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
        /* TITA 請參考合庫主機電文規格(AB_ACC_I01) */
        // Header
        AB_ACC_I01 cbsTita = new AB_ACC_I01();

        Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

        String aa = cbsTita.makeMessage(cbsTita, "0");
        cbsTita.setINTRAN("EAIACC00");
        cbsTita.setINMSGID(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() + StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));
        cbsTita.setINSTAN(feptxn.getFeptxnStan());
        cbsTita.setINDATE(feptxn.getFeptxnTxDate());
        cbsTita.setINTIME(feptxn.getFeptxnTxTime());
        cbsTita.setINSERV("FEP1");
        cbsTita.setINTD("61000004");
        cbsTita.setINAP(feptxn.getFeptxnChannel());
        cbsTita.setINID(feptxn.getFeptxnIdno());
        cbsTita.setINFF("F");
        cbsTita.setINPGNO("001");
        cbsTita.setINV1CT("0000");

        //BODY
        if (txType.equals("0")) {
            cbsTita.setRECTYPE("A");
        } else {
            cbsTita.setRECTYPE("B");
        }

        cbsTita.setTRNSFLAG(tita.getBody().getRq().getSvcRq().getTRNSFLAG());
        //財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setACCDATE(feptxnTbsdyFisc);
        cbsTita.setTRNSWAY("4");
        cbsTita.setBUSINESSTYPE(tita.getBody().getRq().getSvcRq().getBUSINESSTYPE());
        cbsTita.setTRNSFROUTIDNO(StringUtils.rightPad(feptxn.getFeptxnIdno(), 10, " "));
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbsTita.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
        } else {
            cbsTita.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno() + "0000");
        }
        cbsTita.setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
        cbsTita.setTRNSFRINIDNO(StringUtils.rightPad(tita.getBody().getRq().getSvcRq().getTRNSFRINIDNO(), 10, " "));
        if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
            cbsTita.setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
        } else {
            cbsTita.setTRNSFRINBANK(feptxn.getFeptxnTrinBkno() + "0000");
        }
        cbsTita.setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
        cbsTita.setTRANS_AMT(feptxn.getFeptxnTxAmt());
        cbsTita.setBLOCK_AMT(new BigDecimal(0));
        cbsTita.setUNBLOCK_AMT(new BigDecimal(0));
        if (txType.equals("4")) {
            cbsTita.setORIBLOCK_MSGID(feptxn.getFeptxnMsgid());
            cbsTita.setORIBLOCK_INAP(feptxn.getFeptxnChannel());
            cbsTita.setORI_INSTAN(feptxn.getFeptxnStan());
            cbsTita.setCHAFEE_TYPE(eaitxn.getEaitxnChafeeType());
            cbsTita.setCHAFEE_BRANCH(eaitxn.getEaitxnChafeeBranch());
            cbsTita.setCHAFEE_AMT(eaitxn.getEaitxnChafeeAmt());
        } else {
            cbsTita.setORIBLOCK_MSGID(" ");
            cbsTita.setORIBLOCK_INAP(" ");
            cbsTita.setORI_INSTAN(" ");
        }
        cbsTita.setFEEPAYMENTTYPE(tita.getBody().getRq().getSvcRq().getFEEPAYMENTTYPE());
        cbsTita.setCUSTPAY_FEE(feptxn.getFeptxnFeeCustpay());
        cbsTita.setFISC_FEE(tita.getBody().getRq().getSvcRq().getFISCFEE());
        cbsTita.setOTHERBANK_FEE(tita.getBody().getRq().getSvcRq().getOTHERBANKFEE());
        cbsTita.setAPP_FIANCE_FLAG(" ");
        cbsTita.setTRNSFRIN_NOTE(tita.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
        cbsTita.setTRNSFROUT_NOTE(tita.getBody().getRq().getSvcRq().getTRNSFROUTNOTE());
        cbsTita.setTRNSFRINNAME(" ");
        cbsTita.setFAXFEE(tita.getBody().getRq().getSvcRq().getFAXFEE());
        cbsTita.setTRANSFEE(tita.getBody().getRq().getSvcRq().getTRANSFEE());

        //for EDI
        cbsTita.setAFFAIRSCODE(tita.getBody().getRq().getSvcRq().getAFFAIRSCODE());
        cbsTita.setCUSTCODE(tita.getBody().getRq().getSvcRq().getCUSTCODE());
        cbsTita.setTRNSFROUTNAME(tita.getBody().getRq().getSvcRq().getTRNSFROUTNAME());

        cbsTita.setSSLTYPE(tita.getBody().getRq().getSvcRq().getSSLTYPE());
        cbsTita.setLIMITTYPE(tita.getBody().getRq().getSvcRq().getLIMITTYPE());
        cbsTita.setFUNCTIONTYPE(tita.getBody().getRq().getSvcRq().getTRANSTYPEFLAG());

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
        FEPReturnCode rtnCode = FEPReturnCode.Normal;

        if (CodeGenUtil.ebcdicToAsciiDefaultEmpty(cbsTota).substring(88,92).equals("4001")) {
            AB_ACC_O01 tota = new AB_ACC_O01();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            rtnCode = this.updateFEPTxn(tota);
            if (tota.getOUTRTC().equals("4001") && "0".equals(type)) {
                insertEAITXN(tota);
            }
        } else {
            AB_ACC_O02 tota = new AB_ACC_O02();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            rtnCode = this.updateFEPTxn(tota);
            if ("0".equals(type)) {
                insertEAITXN(tota);
            }
            rtnCode = FEPReturnCode.CBSCheckError;
        }

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    public FEPReturnCode insertEAITXN(AB_ACC_O01 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;

        Eaitxn InsertEaitxn = new Eaitxn();

        InsertEaitxn.setEaitxnTxDate(feptxn.getFeptxnTxDate());
        InsertEaitxn.setEaitxnTxTime(feptxn.getFeptxnTxTime());
        InsertEaitxn.setEaitxnEjfno(feptxn.getFeptxnEjfno());
        InsertEaitxn.setEaitxnStan(feptxn.getFeptxnStan());
        InsertEaitxn.setEaitxnMsgid(feptxn.getFeptxnMsgid());
        InsertEaitxn.setEaitxnServ("FEP1");
        InsertEaitxn.setEaitxnTd("61000004");
        InsertEaitxn.setEaitxnAp(feptxn.getFeptxnChannel());
        InsertEaitxn.setEaitxnIdno(feptxn.getFeptxnIdno());
        InsertEaitxn.setEaitxnOutrtc(cbsTota.getOUTRTC());
        InsertEaitxn.setEaitxnOutbill(cbsTota.getOUTBILL());
        InsertEaitxn.setEaitxnAccdate(cbsTota.getACCDATE());
        InsertEaitxn.setEaitxnTrnsfroutidno(cbsTota.getTRNSFRIDNO());
        InsertEaitxn.setEaitxnTrnsfroutbank(cbsTota.getTRNSFROUTBANK());
        InsertEaitxn.setEaitxnTrnsfroutaccnt(cbsTota.getTRNSFROUTACCNT());
        InsertEaitxn.setEaitxnTrnsfrinidno(cbsTota.getTRNSFRINIDNO());
        InsertEaitxn.setEaitxnTrnsfrinbank(cbsTota.getTRNSFRINBANK());
        InsertEaitxn.setEaitxnTrnsfrinaccnt(cbsTota.getTRNSFRINACCNT());
        InsertEaitxn.setEaitxnTxAmt(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANS_AMT(), "##0.00")));
        InsertEaitxn.setEaitxnFeepaymenttype(cbsTota.getFEEPAYMENTTYPE());
        InsertEaitxn.setEaitxnCustpayFee(cbsTota.getCUSTPAY_FEE());
        if (cbsTota.getFISC_FEE() != null) {
            InsertEaitxn.setEaitxnFiscFee(cbsTota.getFISC_FEE());
        }
        if (cbsTota.getOTHERBANK_FEE() != null) {
            InsertEaitxn.setEaitxnOtherbankFee(cbsTota.getOTHERBANK_FEE());
            InsertEaitxn.setEaitxnChafeeBranch(cbsTota.getCHAFEE_BRANCH());
        }
        if (cbsTota.getCHAFEE_AMT() != null) {
            InsertEaitxn.setEaitxnChafeeAmt(cbsTota.getCHAFEE_AMT());
        }
        InsertEaitxn.setEaitxnTransfroutbal(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANSFROUTBAL(), "##0.00")));
        InsertEaitxn.setEaitxnTransamtout(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANSAMTOUT(), "##0.00")));
        InsertEaitxn.setEaitxnTransamtin(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANSAMTIN(), "##0.00")));
        InsertEaitxn.setEaitxnCleanbranchOut(cbsTota.getCLEANBRANCH_OUT());
        InsertEaitxn.setEaitxnCleanbranchIn(cbsTota.getCLEANBRANCH_IN());
        InsertEaitxn.setEaitxnReceivename(cbsTota.getRECEIVENAME());
        InsertEaitxn.setEaitxnPayernote(cbsTota.getTRNSFRINNAME());
        InsertEaitxn.setEaitxnTrnsfrinname(cbsTota.getTRNSFRINNAME());
        InsertEaitxn.setEaitxnTrnsfroutname(cbsTota.getTRNSFROUTNAME());
        InsertEaitxn.setEaitxnPayeremail(cbsTota.getPAYEREMAIL());
        InsertEaitxn.setEaitxnPwatxday(cbsTota.getPWATXDAY());
        InsertEaitxn.setEaitxnChafeeType(cbsTota.getCHAFEE_TYPE());

        if (cbsTota.getFAXFEE() != null) {
            InsertEaitxn.setEaitxnFaxfee(cbsTota.getFAXFEE());
        }
        if (cbsTota.getTRANSFEE() != null) {
            InsertEaitxn.setEaitxnTransfee(cbsTota.getTRANSFEE());
            InsertEaitxn.setEaitxnTransoutcust(cbsTota.getTRANSOUTCUST());
            InsertEaitxn.setEaitxnTaxBranch(cbsTota.getTAX_BRANCH());
        }

        if (eaitxnMapper.insertSelective(InsertEaitxn) < 1) {
            return FEPReturnCode.InsertFail;
        }
        return rtnCode;
    }

    public FEPReturnCode insertEAITXN(AB_ACC_O02 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;

        Eaitxn InsertEaitxn = new Eaitxn();

        InsertEaitxn.setEaitxnTxDate(feptxn.getFeptxnTxDate());
        InsertEaitxn.setEaitxnTxTime(feptxn.getFeptxnTxTime());
        InsertEaitxn.setEaitxnEjfno(feptxn.getFeptxnEjfno());
        InsertEaitxn.setEaitxnStan(feptxn.getFeptxnStan());
        InsertEaitxn.setEaitxnMsgid(feptxn.getFeptxnMsgid());
        InsertEaitxn.setEaitxnServ("FEP1");
        InsertEaitxn.setEaitxnTd("61000004");
        InsertEaitxn.setEaitxnAp(feptxn.getFeptxnChannel());
        InsertEaitxn.setEaitxnIdno(feptxn.getFeptxnIdno());
        InsertEaitxn.setEaitxnOutrtc(cbsTota.getOUTRTC());
        InsertEaitxn.setEaitxnOutbill(cbsTota.getOUTBILL());

        if (eaitxnMapper.insertSelective(InsertEaitxn) < 1) {
            return FEPReturnCode.InsertFail;
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
    public FEPReturnCode updateFEPTxn(AB_ACC_O01 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
        feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
        if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCH_OUT())) {
            feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCH_OUT());
        }
        if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCH_IN())) {
            feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCH_IN());
        }
        //主機交易時間
        feptxn.setFeptxnCbsTxTime(cbsTota.getOUTTIME());
        //手續費
        if (cbsTota.getCUSTPAY_FEE() != null) {
            feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE());
            if ("TWD".equals(feptxn.getFeptxnTxCur())) { //台幣手續費
                feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE());
            } else { //外幣手續費
                feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE().divide(feptxn.getFeptxnExrate(), 2, RoundingMode.HALF_UP));
            }
        }
        if (StringUtils.isNotBlank(cbsTota.getPAYERNOTE())) {
            feptxn.setFeptxnPsbremFD(cbsTota.getPAYERNOTE());
        }
        if (StringUtils.isNotBlank(cbsTota.getRECEIVENAME())) {
            feptxn.setFeptxnPsbremFC(cbsTota.getRECEIVENAME());
        }
        if (StringUtils.isNotBlank(cbsTota.getTAX_BRANCH())) {
            feptxn.setFeptxnTroutBkno7(feptxn.getFeptxnTroutBkno() + cbsTota.getTAX_BRANCH());
        }

        feptxn.setFeptxnCbsRc("000");

        rtnCode = FEPReturnCode.Normal; /* 收到主機回應, 改成 Normal */
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
        return rtnCode;
    }

    public FEPReturnCode updateFEPTxn(AB_ACC_O02 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        feptxn.setFeptxnErrMsg(cbsTota.getMEMO());
        feptxn.setFeptxnCbsTxTime(cbsTota.getOUTTIME());
        feptxn.setFeptxnCbsRc(cbsTota.getOUTRTC());
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
        return rtnCode;
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
