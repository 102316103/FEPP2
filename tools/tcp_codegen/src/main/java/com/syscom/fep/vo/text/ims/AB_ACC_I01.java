package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;

public class AB_ACC_I01 extends IMSTextBase {
    @Field(length = 2)
    private String INLL = StringUtils.EMPTY;

    @Field(length = 2)
    private String ZZ = StringUtils.EMPTY;

    @Field(length = 8)
    private String INTRAN = StringUtils.EMPTY;

    @Field(length = 1)
    private String INRSV1 = StringUtils.EMPTY;

    @Field(length = 24)
    private String INMSGID = StringUtils.EMPTY;

    @Field(length = 10)
    private String INSTAN = StringUtils.EMPTY;

    @Field(length = 8)
    private String INDATE = StringUtils.EMPTY;

    @Field(length = 6)
    private String INTIME = StringUtils.EMPTY;

    @Field(length = 4)
    private String INSERV = StringUtils.EMPTY;

    @Field(length = 1)
    private String MQIDCNV = StringUtils.EMPTY;

    @Field(length = 8)
    private String INTD = StringUtils.EMPTY;

    @Field(length = 5)
    private String INAP = StringUtils.EMPTY;

    @Field(length = 10)
    private String INID = StringUtils.EMPTY;

    @Field(length = 1)
    private String INFF = StringUtils.EMPTY;

    @Field(length = 3)
    private String INPGNO = StringUtils.EMPTY;

    @Field(length = 4)
    private String INV1CT = StringUtils.EMPTY;

    @Field(length = 4)
    private String INRTC = StringUtils.EMPTY;

    @Field(length = 21)
    private String INRSV2 = StringUtils.EMPTY;

    @Field(length = 1)
    private String RECTYPE = StringUtils.EMPTY;

    @Field(length = 1)
    private String TRNSFLAG = StringUtils.EMPTY;

    @Field(length = 7)
    private String ACCDATE = StringUtils.EMPTY;

    @Field(length = 1)
    private String TRNSWAY = StringUtils.EMPTY;

    @Field(length = 1)
    private String BUSINESSTYPE = StringUtils.EMPTY;

    @Field(length = 10)
    private String TRNSFROUTIDNO = StringUtils.EMPTY;

    @Field(length = 7)
    private String TRNSFROUTBANK = StringUtils.EMPTY;

    @Field(length = 16)
    private String TRNSFROUTACCNT = StringUtils.EMPTY;

    @Field(length = 10)
    private String TRNSFRINIDNO = StringUtils.EMPTY;

    @Field(length = 7)
    private String TRNSFRINBANK = StringUtils.EMPTY;

    @Field(length = 16)
    private String TRNSFRINACCNT = StringUtils.EMPTY;

    @Field(length = 16)
    private BigDecimal TRANS_AMT;

    @Field(length = 16)
    private BigDecimal BLOCK_AMT;

    @Field(length = 16)
    private BigDecimal UNBLOCK_AMT;

    @Field(length = 24)
    private String ORIBLOCK_MSGID = StringUtils.EMPTY;

    @Field(length = 5)
    private String ORIBLOCK_INAP = StringUtils.EMPTY;

    @Field(length = 2)
    private String FEEPAYMENTTYPE = StringUtils.EMPTY;

    @Field(length = 7)
    private BigDecimal CUSTPAY_FEE;

    @Field(length = 7)
    private BigDecimal FISC_FEE;

    @Field(length = 7)
    private BigDecimal OTHERBANK_FEE;

    @Field(length = 4)
    private String CHAFEE_BRANCH = StringUtils.EMPTY;

    @Field(length = 7)
    private BigDecimal CHAFEE_AMT;

    @Field(length = 1)
    private String APP_FIANCE_FLAG = StringUtils.EMPTY;

    @Field(length = 18)
    private String TRNSFRIN_NOTE = StringUtils.EMPTY;

    @Field(length = 18)
    private String TRNSFROUT_NOTE = StringUtils.EMPTY;

    @Field(length = 42)
    private String TRNSFRINNAME = StringUtils.EMPTY;

    @Field(length = 10)
    private String ORI_INSTAN = StringUtils.EMPTY;

    @Field(length = 1)
    private String CHAFEE_TYPE = StringUtils.EMPTY;

    @Field(length = 7)
    private BigDecimal FAXFEE;

    @Field(length = 7)
    private BigDecimal TRANSFEE;

    @Field(length = 3)
    private String AFFAIRSCODE = StringUtils.EMPTY;

    @Field(length = 12)
    private String CUSTCODE = StringUtils.EMPTY;

    @Field(length = 42)
    private String TRNSFROUTNAME = StringUtils.EMPTY;

    @Field(length = 1)
    private String SSLTYPE = StringUtils.EMPTY;

    @Field(length = 2)
    private String LIMITTYPE = StringUtils.EMPTY;

    @Field(length = 1)
    private String FUNCTIONTYPE = StringUtils.EMPTY;

    @Field(length = 37)
    private String DRVS = StringUtils.EMPTY;

    public String getINLL(){
        return this.INLL;
    }
    public void setINLL(String INLL){
        this.INLL = INLL;
    }
    public String getZZ(){
        return this.ZZ;
    }
    public void setZZ(String ZZ){
        this.ZZ = ZZ;
    }
    public String getINTRAN(){
        return this.INTRAN;
    }
    public void setINTRAN(String INTRAN){
        this.INTRAN = INTRAN;
    }
    public String getINRSV1(){
        return this.INRSV1;
    }
    public void setINRSV1(String INRSV1){
        this.INRSV1 = INRSV1;
    }
    public String getINMSGID(){
        return this.INMSGID;
    }
    public void setINMSGID(String INMSGID){
        this.INMSGID = INMSGID;
    }
    public String getINSTAN(){
        return this.INSTAN;
    }
    public void setINSTAN(String INSTAN){
        this.INSTAN = INSTAN;
    }
    public String getINDATE(){
        return this.INDATE;
    }
    public void setINDATE(String INDATE){
        this.INDATE = INDATE;
    }
    public String getINTIME(){
        return this.INTIME;
    }
    public void setINTIME(String INTIME){
        this.INTIME = INTIME;
    }
    public String getINSERV(){
        return this.INSERV;
    }
    public void setINSERV(String INSERV){
        this.INSERV = INSERV;
    }
    public String getMQIDCNV(){
        return this.MQIDCNV;
    }
    public void setMQIDCNV(String MQIDCNV){
        this.MQIDCNV = MQIDCNV;
    }
    public String getINTD(){
        return this.INTD;
    }
    public void setINTD(String INTD){
        this.INTD = INTD;
    }
    public String getINAP(){
        return this.INAP;
    }
    public void setINAP(String INAP){
        this.INAP = INAP;
    }
    public String getINID(){
        return this.INID;
    }
    public void setINID(String INID){
        this.INID = INID;
    }
    public String getINFF(){
        return this.INFF;
    }
    public void setINFF(String INFF){
        this.INFF = INFF;
    }
    public String getINPGNO(){
        return this.INPGNO;
    }
    public void setINPGNO(String INPGNO){
        this.INPGNO = INPGNO;
    }
    public String getINV1CT(){
        return this.INV1CT;
    }
    public void setINV1CT(String INV1CT){
        this.INV1CT = INV1CT;
    }
    public String getINRTC(){
        return this.INRTC;
    }
    public void setINRTC(String INRTC){
        this.INRTC = INRTC;
    }
    public String getINRSV2(){
        return this.INRSV2;
    }
    public void setINRSV2(String INRSV2){
        this.INRSV2 = INRSV2;
    }
    public String getRECTYPE(){
        return this.RECTYPE;
    }
    public void setRECTYPE(String RECTYPE){
        this.RECTYPE = RECTYPE;
    }
    public String getTRNSFLAG(){
        return this.TRNSFLAG;
    }
    public void setTRNSFLAG(String TRNSFLAG){
        this.TRNSFLAG = TRNSFLAG;
    }
    public String getACCDATE(){
        return this.ACCDATE;
    }
    public void setACCDATE(String ACCDATE){
        this.ACCDATE = ACCDATE;
    }
    public String getTRNSWAY(){
        return this.TRNSWAY;
    }
    public void setTRNSWAY(String TRNSWAY){
        this.TRNSWAY = TRNSWAY;
    }
    public String getBUSINESSTYPE(){
        return this.BUSINESSTYPE;
    }
    public void setBUSINESSTYPE(String BUSINESSTYPE){
        this.BUSINESSTYPE = BUSINESSTYPE;
    }
    public String getTRNSFROUTIDNO(){
        return this.TRNSFROUTIDNO;
    }
    public void setTRNSFROUTIDNO(String TRNSFROUTIDNO){
        this.TRNSFROUTIDNO = TRNSFROUTIDNO;
    }
    public String getTRNSFROUTBANK(){
        return this.TRNSFROUTBANK;
    }
    public void setTRNSFROUTBANK(String TRNSFROUTBANK){
        this.TRNSFROUTBANK = TRNSFROUTBANK;
    }
    public String getTRNSFROUTACCNT(){
        return this.TRNSFROUTACCNT;
    }
    public void setTRNSFROUTACCNT(String TRNSFROUTACCNT){
        this.TRNSFROUTACCNT = TRNSFROUTACCNT;
    }
    public String getTRNSFRINIDNO(){
        return this.TRNSFRINIDNO;
    }
    public void setTRNSFRINIDNO(String TRNSFRINIDNO){
        this.TRNSFRINIDNO = TRNSFRINIDNO;
    }
    public String getTRNSFRINBANK(){
        return this.TRNSFRINBANK;
    }
    public void setTRNSFRINBANK(String TRNSFRINBANK){
        this.TRNSFRINBANK = TRNSFRINBANK;
    }
    public String getTRNSFRINACCNT(){
        return this.TRNSFRINACCNT;
    }
    public void setTRNSFRINACCNT(String TRNSFRINACCNT){
        this.TRNSFRINACCNT = TRNSFRINACCNT;
    }
    public BigDecimal getTRANS_AMT(){
        return this.TRANS_AMT;
    }
    public void setTRANS_AMT(BigDecimal TRANS_AMT){
        this.TRANS_AMT = TRANS_AMT;
    }
    public BigDecimal getBLOCK_AMT(){
        return this.BLOCK_AMT;
    }
    public void setBLOCK_AMT(BigDecimal BLOCK_AMT){
        this.BLOCK_AMT = BLOCK_AMT;
    }
    public BigDecimal getUNBLOCK_AMT(){
        return this.UNBLOCK_AMT;
    }
    public void setUNBLOCK_AMT(BigDecimal UNBLOCK_AMT){
        this.UNBLOCK_AMT = UNBLOCK_AMT;
    }
    public String getORIBLOCK_MSGID(){
        return this.ORIBLOCK_MSGID;
    }
    public void setORIBLOCK_MSGID(String ORIBLOCK_MSGID){
        this.ORIBLOCK_MSGID = ORIBLOCK_MSGID;
    }
    public String getORIBLOCK_INAP(){
        return this.ORIBLOCK_INAP;
    }
    public void setORIBLOCK_INAP(String ORIBLOCK_INAP){
        this.ORIBLOCK_INAP = ORIBLOCK_INAP;
    }
    public String getFEEPAYMENTTYPE(){
        return this.FEEPAYMENTTYPE;
    }
    public void setFEEPAYMENTTYPE(String FEEPAYMENTTYPE){
        this.FEEPAYMENTTYPE = FEEPAYMENTTYPE;
    }
    public BigDecimal getCUSTPAY_FEE(){
        return this.CUSTPAY_FEE;
    }
    public void setCUSTPAY_FEE(BigDecimal CUSTPAY_FEE){
        this.CUSTPAY_FEE = CUSTPAY_FEE;
    }
    public BigDecimal getFISC_FEE(){
        return this.FISC_FEE;
    }
    public void setFISC_FEE(BigDecimal FISC_FEE){
        this.FISC_FEE = FISC_FEE;
    }
    public BigDecimal getOTHERBANK_FEE(){
        return this.OTHERBANK_FEE;
    }
    public void setOTHERBANK_FEE(BigDecimal OTHERBANK_FEE){
        this.OTHERBANK_FEE = OTHERBANK_FEE;
    }
    public String getCHAFEE_BRANCH(){
        return this.CHAFEE_BRANCH;
    }
    public void setCHAFEE_BRANCH(String CHAFEE_BRANCH){
        this.CHAFEE_BRANCH = CHAFEE_BRANCH;
    }
    public BigDecimal getCHAFEE_AMT(){
        return this.CHAFEE_AMT;
    }
    public void setCHAFEE_AMT(BigDecimal CHAFEE_AMT){
        this.CHAFEE_AMT = CHAFEE_AMT;
    }
    public String getAPP_FIANCE_FLAG(){
        return this.APP_FIANCE_FLAG;
    }
    public void setAPP_FIANCE_FLAG(String APP_FIANCE_FLAG){
        this.APP_FIANCE_FLAG = APP_FIANCE_FLAG;
    }
    public String getTRNSFRIN_NOTE(){
        return this.TRNSFRIN_NOTE;
    }
    public void setTRNSFRIN_NOTE(String TRNSFRIN_NOTE){
        this.TRNSFRIN_NOTE = TRNSFRIN_NOTE;
    }
    public String getTRNSFROUT_NOTE(){
        return this.TRNSFROUT_NOTE;
    }
    public void setTRNSFROUT_NOTE(String TRNSFROUT_NOTE){
        this.TRNSFROUT_NOTE = TRNSFROUT_NOTE;
    }
    public String getTRNSFRINNAME(){
        return this.TRNSFRINNAME;
    }
    public void setTRNSFRINNAME(String TRNSFRINNAME){
        this.TRNSFRINNAME = TRNSFRINNAME;
    }
    public String getORI_INSTAN(){
        return this.ORI_INSTAN;
    }
    public void setORI_INSTAN(String ORI_INSTAN){
        this.ORI_INSTAN = ORI_INSTAN;
    }
    public String getCHAFEE_TYPE(){
        return this.CHAFEE_TYPE;
    }
    public void setCHAFEE_TYPE(String CHAFEE_TYPE){
        this.CHAFEE_TYPE = CHAFEE_TYPE;
    }
    public BigDecimal getFAXFEE(){
        return this.FAXFEE;
    }
    public void setFAXFEE(BigDecimal FAXFEE){
        this.FAXFEE = FAXFEE;
    }
    public BigDecimal getTRANSFEE(){
        return this.TRANSFEE;
    }
    public void setTRANSFEE(BigDecimal TRANSFEE){
        this.TRANSFEE = TRANSFEE;
    }
    public String getAFFAIRSCODE(){
        return this.AFFAIRSCODE;
    }
    public void setAFFAIRSCODE(String AFFAIRSCODE){
        this.AFFAIRSCODE = AFFAIRSCODE;
    }
    public String getCUSTCODE(){
        return this.CUSTCODE;
    }
    public void setCUSTCODE(String CUSTCODE){
        this.CUSTCODE = CUSTCODE;
    }
    public String getTRNSFROUTNAME(){
        return this.TRNSFROUTNAME;
    }
    public void setTRNSFROUTNAME(String TRNSFROUTNAME){
        this.TRNSFROUTNAME = TRNSFROUTNAME;
    }
    public String getSSLTYPE(){
        return this.SSLTYPE;
    }
    public void setSSLTYPE(String SSLTYPE){
        this.SSLTYPE = SSLTYPE;
    }
    public String getLIMITTYPE(){
        return this.LIMITTYPE;
    }
    public void setLIMITTYPE(String LIMITTYPE){
        this.LIMITTYPE = LIMITTYPE;
    }
    public String getFUNCTIONTYPE(){
        return this.FUNCTIONTYPE;
    }
    public void setFUNCTIONTYPE(String FUNCTIONTYPE){
        this.FUNCTIONTYPE = FUNCTIONTYPE;
    }
    public String getDRVS(){
        return this.DRVS;
    }
    public void setDRVS(String DRVS){
        this.DRVS = DRVS;
    }

    public void parseCbsTele(String tita) throws ParseException{
        this.setINLL(tita.substring(0, 2));
        this.setZZ(tita.substring(2, 4));
        this.setINTRAN(tita.substring(4, 12));
        this.setINRSV1(tita.substring(12, 13));
        this.setINMSGID(tita.substring(13, 37));
        this.setINSTAN(tita.substring(37, 47));
        this.setINDATE(tita.substring(47, 55));
        this.setINTIME(tita.substring(55, 61));
        this.setINSERV(tita.substring(61, 65));
        this.setMQIDCNV(tita.substring(65, 66));
        this.setINTD(tita.substring(66, 74));
        this.setINAP(tita.substring(74, 79));
        this.setINID(tita.substring(79, 89));
        this.setINFF(tita.substring(89, 90));
        this.setINPGNO(tita.substring(90, 93));
        this.setINV1CT(tita.substring(93, 97));
        this.setINRTC(tita.substring(97, 101));
        this.setINRSV2(tita.substring(101, 122));
        this.setRECTYPE(tita.substring(122, 123));
        this.setTRNSFLAG(tita.substring(123, 124));
        this.setACCDATE(tita.substring(124, 131));
        this.setTRNSWAY(tita.substring(131, 132));
        this.setBUSINESSTYPE(tita.substring(132, 133));
        this.setTRNSFROUTIDNO(tita.substring(133, 143));
        this.setTRNSFROUTBANK(tita.substring(143, 150));
        this.setTRNSFROUTACCNT(tita.substring(150, 166));
        this.setTRNSFRINIDNO(tita.substring(166, 176));
        this.setTRNSFRINBANK(tita.substring(176, 183));
        this.setTRNSFRINACCNT(tita.substring(183, 199));
        this.setTRANS_AMT(CodeGenUtil.asciiToBigDecimal(tita.substring(199, 215).trim(), false, 2));
        this.setBLOCK_AMT(CodeGenUtil.asciiToBigDecimal(tita.substring(215, 231).trim(), false, 2));
        this.setUNBLOCK_AMT(CodeGenUtil.asciiToBigDecimal(tita.substring(231, 247).trim(), false, 2));
        this.setORIBLOCK_MSGID(tita.substring(247, 271));
        this.setORIBLOCK_INAP(tita.substring(271, 276));
        this.setFEEPAYMENTTYPE(tita.substring(276, 278));
        this.setCUSTPAY_FEE(CodeGenUtil.asciiToBigDecimal(tita.substring(278, 285).trim(), false, 0));
        this.setFISC_FEE(CodeGenUtil.asciiToBigDecimal(tita.substring(285, 292).trim(), false, 0));
        this.setOTHERBANK_FEE(CodeGenUtil.asciiToBigDecimal(tita.substring(292, 299).trim(), false, 0));
        this.setCHAFEE_BRANCH(tita.substring(299, 303));
        this.setCHAFEE_AMT(CodeGenUtil.asciiToBigDecimal(tita.substring(303, 310).trim(), false, 0));
        this.setAPP_FIANCE_FLAG(tita.substring(310, 311));
        this.setTRNSFRIN_NOTE(tita.substring(311, 329));
        this.setTRNSFROUT_NOTE(tita.substring(329, 347));
        this.setTRNSFRINNAME(tita.substring(347, 389));
        this.setORI_INSTAN(tita.substring(389, 399));
        this.setCHAFEE_TYPE(tita.substring(399, 400));
        this.setFAXFEE(CodeGenUtil.asciiToBigDecimal(tita.substring(400, 407).trim(), false, 0));
        this.setTRANSFEE(CodeGenUtil.asciiToBigDecimal(tita.substring(407, 414).trim(), false, 0));
        this.setAFFAIRSCODE(tita.substring(414, 417));
        this.setCUSTCODE(tita.substring(417, 429));
        this.setTRNSFROUTNAME(tita.substring(429, 471));
        this.setSSLTYPE(tita.substring(471, 472));
        this.setLIMITTYPE(tita.substring(472, 474));
        this.setFUNCTIONTYPE(tita.substring(474, 475));
        this.setDRVS(tita.substring(475, 512));
    }

    public String makeMessage() {
        return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINLL(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getZZ(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINTRAN(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINRSV1(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINMSGID(), 24) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINSTAN(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINDATE(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINTIME(), 6) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINSERV(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMQIDCNV(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINTD(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINAP(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINID(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINFF(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINPGNO(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINV1CT(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINRTC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getINRSV2(), 21) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRECTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACCDATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSWAY(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBUSINESSTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUTIDNO(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUTBANK(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUTACCNT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINIDNO(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINBANK(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINACCNT(), 16) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTRANS_AMT(), 14, false, 2, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getBLOCK_AMT(), 14, false, 2, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getUNBLOCK_AMT(), 14, false, 2, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORIBLOCK_MSGID(), 24) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORIBLOCK_INAP(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEEPAYMENTTYPE(), 2) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getCUSTPAY_FEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getFISC_FEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getOTHERBANK_FEE(), 7, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCHAFEE_BRANCH(), 4) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getCHAFEE_AMT(), 7, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAPP_FIANCE_FLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRIN_NOTE(), 18) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUT_NOTE(), 18) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFRINNAME(), 42) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORI_INSTAN(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCHAFEE_TYPE(), 1) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getFAXFEE(), 7, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTRANSFEE(), 7, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAFFAIRSCODE(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCUSTCODE(), 12) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRNSFROUTNAME(), 42) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSSLTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getLIMITTYPE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFUNCTIONTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 37) //
            ;
    }
}
