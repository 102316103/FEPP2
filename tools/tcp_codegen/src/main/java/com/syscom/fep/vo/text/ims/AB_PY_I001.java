package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;

public class AB_PY_I001 extends IMSTextBase {
    @Field(length = 8)
    private String IMS_TRANS = StringUtils.EMPTY;

    @Field(length = 4)
    private String SYSCODE = StringUtils.EMPTY;

    @Field(length = 14)
    private String SYS_DATETIME = StringUtils.EMPTY;

    @Field(length = 8)
    private String FEP_EJNO = StringUtils.EMPTY;

    @Field(length = 1)
    private String TXN_FLOW = StringUtils.EMPTY;

    @Field(length = 2)
    private String MSG_CAT = StringUtils.EMPTY;

    @Field(length = 3)
    private String SOURCE_CHANNEL = StringUtils.EMPTY;

    @Field(length = 4)
    private String PCODE = StringUtils.EMPTY;

    @Field(length = 2)
    private String FSCODE = StringUtils.EMPTY;

    @Field(length = 4)
    private String PROCESS_TYPE = StringUtils.EMPTY;

    @Field(length = 7)
    private String BUSINESS_DATE = StringUtils.EMPTY;

    @Field(length = 3)
    private String ACQUIRER_BANK = StringUtils.EMPTY;

    @Field(length = 7)
    private String TXNSTAN = StringUtils.EMPTY;

    @Field(length = 8)
    private String TERMINALID = StringUtils.EMPTY;

    @Field(length = 4)
    private String TERMINAL_TYPE = StringUtils.EMPTY;

    @Field(length = 3)
    private String CARDISSUE_BANK = StringUtils.EMPTY;

    @Field(length = 1)
    private String CARDTYPE = StringUtils.EMPTY;

    @Field(length = 4)
    private String RESPONSE_CODE = StringUtils.EMPTY;

    @Field(length = 33)
    private String HRVS = StringUtils.EMPTY;

    @Field(length = 8)
    private String ICCHIPSTAN = StringUtils.EMPTY;

    @Field(length = 8)
    private String TERM_CHECKNO = StringUtils.EMPTY;

    @Field(length = 14)
    private String TERMTXN_DATETIME = StringUtils.EMPTY;

    @Field(length = 30)
    private String ICMEMO = StringUtils.EMPTY;

    @Field(length = 10)
    private String TXNICCTAC = StringUtils.EMPTY;

    @Field(length = 14)
    private BigDecimal TXNAMT;

    @Field(length = 16)
    private String FROMACT = StringUtils.EMPTY;

    @Field(length = 7)
    private String TRIN_BANKNO = StringUtils.EMPTY;

    @Field(length = 7)
    private String TROUT_BANKNO = StringUtils.EMPTY;

    @Field(length = 16)
    private String TOACT = StringUtils.EMPTY;

    @Field(length = 2)
    private String PY_SPECIAL_FLAG = StringUtils.EMPTY;

    @Field(length = 7)
    private String PY_HOST_BRANCH = StringUtils.EMPTY;

    @Field(length = 11)
    private String PY_IDNO = StringUtils.EMPTY;

    @Field(length = 8)
    private String PY_PAYUNTNO = StringUtils.EMPTY;

    @Field(length = 5)
    private String PY_TAXTYPE = StringUtils.EMPTY;

    @Field(length = 4)
    private String PY_PAYFEENO = StringUtils.EMPTY;

    @Field(length = 16)
    private String PY_PAYTXNOL = StringUtils.EMPTY;

    @Field(length = 8)
    private String PY_PAYDDATE = StringUtils.EMPTY;

    @Field(length = 12)
    private String PY_PAYMEMO_FILL = StringUtils.EMPTY;

    @Field(length = 4)
    private BigDecimal PY_CHARGCUS;

    @Field(length = 20)
    private BigDecimal PY_CHARGUNT;

    @Field(length = 24)
    private String PY_PAYTXNOL1 = StringUtils.EMPTY;

    @Field(length = 1)
    private String EATM_RESERVE_FLAG = StringUtils.EMPTY;

    @Field(length = 14)
    private String ORIGINAL_TX_DAYTIME = StringUtils.EMPTY;

    @Field(length = 1)
    private String PY_AEISLLTY = StringUtils.EMPTY;

    @Field(length = 10)
    private String AE_TRNSFROUTIDNO = StringUtils.EMPTY;

    @Field(length = 1)
    private String AE_TRNSFLAG = StringUtils.EMPTY;

    @Field(length = 1)
    private String AE_BUSINESSTYPE = StringUtils.EMPTY;

    @Field(length = 2)
    private String AE_AEIEFEET = StringUtils.EMPTY;

    @Field(length = 18)
    private String AE_TRNSFROUTNOTE = StringUtils.EMPTY;

    @Field(length = 2)
    private String AE_LIMITTYPE = StringUtils.EMPTY;

    @Field(length = 18)
    private String AE_FILLER = StringUtils.EMPTY;

    @Field(length = 4)
    private String AE_AEIAQBRH = StringUtils.EMPTY;

    @Field(length = 157)
    private String DRVS = StringUtils.EMPTY;

    public String getIMS_TRANS(){
        return this.IMS_TRANS;
    }
    public void setIMS_TRANS(String IMS_TRANS){
        this.IMS_TRANS = IMS_TRANS;
    }
    public String getSYSCODE(){
        return this.SYSCODE;
    }
    public void setSYSCODE(String SYSCODE){
        this.SYSCODE = SYSCODE;
    }
    public String getSYS_DATETIME(){
        return this.SYS_DATETIME;
    }
    public void setSYS_DATETIME(String SYS_DATETIME){
        this.SYS_DATETIME = SYS_DATETIME;
    }
    public String getFEP_EJNO(){
        return this.FEP_EJNO;
    }
    public void setFEP_EJNO(String FEP_EJNO){
        this.FEP_EJNO = FEP_EJNO;
    }
    public String getTXN_FLOW(){
        return this.TXN_FLOW;
    }
    public void setTXN_FLOW(String TXN_FLOW){
        this.TXN_FLOW = TXN_FLOW;
    }
    public String getMSG_CAT(){
        return this.MSG_CAT;
    }
    public void setMSG_CAT(String MSG_CAT){
        this.MSG_CAT = MSG_CAT;
    }
    public String getSOURCE_CHANNEL(){
        return this.SOURCE_CHANNEL;
    }
    public void setSOURCE_CHANNEL(String SOURCE_CHANNEL){
        this.SOURCE_CHANNEL = SOURCE_CHANNEL;
    }
    public String getPCODE(){
        return this.PCODE;
    }
    public void setPCODE(String PCODE){
        this.PCODE = PCODE;
    }
    public String getFSCODE(){
        return this.FSCODE;
    }
    public void setFSCODE(String FSCODE){
        this.FSCODE = FSCODE;
    }
    public String getPROCESS_TYPE(){
        return this.PROCESS_TYPE;
    }
    public void setPROCESS_TYPE(String PROCESS_TYPE){
        this.PROCESS_TYPE = PROCESS_TYPE;
    }
    public String getBUSINESS_DATE(){
        return this.BUSINESS_DATE;
    }
    public void setBUSINESS_DATE(String BUSINESS_DATE){
        this.BUSINESS_DATE = BUSINESS_DATE;
    }
    public String getACQUIRER_BANK(){
        return this.ACQUIRER_BANK;
    }
    public void setACQUIRER_BANK(String ACQUIRER_BANK){
        this.ACQUIRER_BANK = ACQUIRER_BANK;
    }
    public String getTXNSTAN(){
        return this.TXNSTAN;
    }
    public void setTXNSTAN(String TXNSTAN){
        this.TXNSTAN = TXNSTAN;
    }
    public String getTERMINALID(){
        return this.TERMINALID;
    }
    public void setTERMINALID(String TERMINALID){
        this.TERMINALID = TERMINALID;
    }
    public String getTERMINAL_TYPE(){
        return this.TERMINAL_TYPE;
    }
    public void setTERMINAL_TYPE(String TERMINAL_TYPE){
        this.TERMINAL_TYPE = TERMINAL_TYPE;
    }
    public String getCARDISSUE_BANK(){
        return this.CARDISSUE_BANK;
    }
    public void setCARDISSUE_BANK(String CARDISSUE_BANK){
        this.CARDISSUE_BANK = CARDISSUE_BANK;
    }
    public String getCARDTYPE(){
        return this.CARDTYPE;
    }
    public void setCARDTYPE(String CARDTYPE){
        this.CARDTYPE = CARDTYPE;
    }
    public String getRESPONSE_CODE(){
        return this.RESPONSE_CODE;
    }
    public void setRESPONSE_CODE(String RESPONSE_CODE){
        this.RESPONSE_CODE = RESPONSE_CODE;
    }
    public String getHRVS(){
        return this.HRVS;
    }
    public void setHRVS(String HRVS){
        this.HRVS = HRVS;
    }
    public String getICCHIPSTAN(){
        return this.ICCHIPSTAN;
    }
    public void setICCHIPSTAN(String ICCHIPSTAN){
        this.ICCHIPSTAN = ICCHIPSTAN;
    }
    public String getTERM_CHECKNO(){
        return this.TERM_CHECKNO;
    }
    public void setTERM_CHECKNO(String TERM_CHECKNO){
        this.TERM_CHECKNO = TERM_CHECKNO;
    }
    public String getTERMTXN_DATETIME(){
        return this.TERMTXN_DATETIME;
    }
    public void setTERMTXN_DATETIME(String TERMTXN_DATETIME){
        this.TERMTXN_DATETIME = TERMTXN_DATETIME;
    }
    public String getICMEMO(){
        return this.ICMEMO;
    }
    public void setICMEMO(String ICMEMO){
        this.ICMEMO = ICMEMO;
    }
    public String getTXNICCTAC(){
        return this.TXNICCTAC;
    }
    public void setTXNICCTAC(String TXNICCTAC){
        this.TXNICCTAC = TXNICCTAC;
    }
    public BigDecimal getTXNAMT(){
        return this.TXNAMT;
    }
    public void setTXNAMT(BigDecimal TXNAMT){
        this.TXNAMT = TXNAMT;
    }
    public String getFROMACT(){
        return this.FROMACT;
    }
    public void setFROMACT(String FROMACT){
        this.FROMACT = FROMACT;
    }
    public String getTRIN_BANKNO(){
        return this.TRIN_BANKNO;
    }
    public void setTRIN_BANKNO(String TRIN_BANKNO){
        this.TRIN_BANKNO = TRIN_BANKNO;
    }
    public String getTROUT_BANKNO(){
        return this.TROUT_BANKNO;
    }
    public void setTROUT_BANKNO(String TROUT_BANKNO){
        this.TROUT_BANKNO = TROUT_BANKNO;
    }
    public String getTOACT(){
        return this.TOACT;
    }
    public void setTOACT(String TOACT){
        this.TOACT = TOACT;
    }
    public String getPY_SPECIAL_FLAG(){
        return this.PY_SPECIAL_FLAG;
    }
    public void setPY_SPECIAL_FLAG(String PY_SPECIAL_FLAG){
        this.PY_SPECIAL_FLAG = PY_SPECIAL_FLAG;
    }
    public String getPY_HOST_BRANCH(){
        return this.PY_HOST_BRANCH;
    }
    public void setPY_HOST_BRANCH(String PY_HOST_BRANCH){
        this.PY_HOST_BRANCH = PY_HOST_BRANCH;
    }
    public String getPY_IDNO(){
        return this.PY_IDNO;
    }
    public void setPY_IDNO(String PY_IDNO){
        this.PY_IDNO = PY_IDNO;
    }
    public String getPY_PAYUNTNO(){
        return this.PY_PAYUNTNO;
    }
    public void setPY_PAYUNTNO(String PY_PAYUNTNO){
        this.PY_PAYUNTNO = PY_PAYUNTNO;
    }
    public String getPY_TAXTYPE(){
        return this.PY_TAXTYPE;
    }
    public void setPY_TAXTYPE(String PY_TAXTYPE){
        this.PY_TAXTYPE = PY_TAXTYPE;
    }
    public String getPY_PAYFEENO(){
        return this.PY_PAYFEENO;
    }
    public void setPY_PAYFEENO(String PY_PAYFEENO){
        this.PY_PAYFEENO = PY_PAYFEENO;
    }
    public String getPY_PAYTXNOL(){
        return this.PY_PAYTXNOL;
    }
    public void setPY_PAYTXNOL(String PY_PAYTXNOL){
        this.PY_PAYTXNOL = PY_PAYTXNOL;
    }
    public String getPY_PAYDDATE(){
        return this.PY_PAYDDATE;
    }
    public void setPY_PAYDDATE(String PY_PAYDDATE){
        this.PY_PAYDDATE = PY_PAYDDATE;
    }
    public String getPY_PAYMEMO_FILL(){
        return this.PY_PAYMEMO_FILL;
    }
    public void setPY_PAYMEMO_FILL(String PY_PAYMEMO_FILL){
        this.PY_PAYMEMO_FILL = PY_PAYMEMO_FILL;
    }
    public BigDecimal getPY_CHARGCUS(){
        return this.PY_CHARGCUS;
    }
    public void setPY_CHARGCUS(BigDecimal PY_CHARGCUS){
        this.PY_CHARGCUS = PY_CHARGCUS;
    }
    public BigDecimal getPY_CHARGUNT(){
        return this.PY_CHARGUNT;
    }
    public void setPY_CHARGUNT(BigDecimal PY_CHARGUNT){
        this.PY_CHARGUNT = PY_CHARGUNT;
    }
    public String getPY_PAYTXNOL1(){
        return this.PY_PAYTXNOL1;
    }
    public void setPY_PAYTXNOL1(String PY_PAYTXNOL1){
        this.PY_PAYTXNOL1 = PY_PAYTXNOL1;
    }
    public String getEATM_RESERVE_FLAG(){
        return this.EATM_RESERVE_FLAG;
    }
    public void setEATM_RESERVE_FLAG(String EATM_RESERVE_FLAG){
        this.EATM_RESERVE_FLAG = EATM_RESERVE_FLAG;
    }
    public String getORIGINAL_TX_DAYTIME(){
        return this.ORIGINAL_TX_DAYTIME;
    }
    public void setORIGINAL_TX_DAYTIME(String ORIGINAL_TX_DAYTIME){
        this.ORIGINAL_TX_DAYTIME = ORIGINAL_TX_DAYTIME;
    }
    public String getPY_AEISLLTY(){
        return this.PY_AEISLLTY;
    }
    public void setPY_AEISLLTY(String PY_AEISLLTY){
        this.PY_AEISLLTY = PY_AEISLLTY;
    }
    public String getAE_TRNSFROUTIDNO(){
        return this.AE_TRNSFROUTIDNO;
    }
    public void setAE_TRNSFROUTIDNO(String AE_TRNSFROUTIDNO){
        this.AE_TRNSFROUTIDNO = AE_TRNSFROUTIDNO;
    }
    public String getAE_TRNSFLAG(){
        return this.AE_TRNSFLAG;
    }
    public void setAE_TRNSFLAG(String AE_TRNSFLAG){
        this.AE_TRNSFLAG = AE_TRNSFLAG;
    }
    public String getAE_BUSINESSTYPE(){
        return this.AE_BUSINESSTYPE;
    }
    public void setAE_BUSINESSTYPE(String AE_BUSINESSTYPE){
        this.AE_BUSINESSTYPE = AE_BUSINESSTYPE;
    }
    public String getAE_AEIEFEET(){
        return this.AE_AEIEFEET;
    }
    public void setAE_AEIEFEET(String AE_AEIEFEET){
        this.AE_AEIEFEET = AE_AEIEFEET;
    }
    public String getAE_TRNSFROUTNOTE(){
        return this.AE_TRNSFROUTNOTE;
    }
    public void setAE_TRNSFROUTNOTE(String AE_TRNSFROUTNOTE){
        this.AE_TRNSFROUTNOTE = AE_TRNSFROUTNOTE;
    }
    public String getAE_LIMITTYPE(){
        return this.AE_LIMITTYPE;
    }
    public void setAE_LIMITTYPE(String AE_LIMITTYPE){
        this.AE_LIMITTYPE = AE_LIMITTYPE;
    }
    public String getAE_FILLER(){
        return this.AE_FILLER;
    }
    public void setAE_FILLER(String AE_FILLER){
        this.AE_FILLER = AE_FILLER;
    }
    public String getAE_AEIAQBRH(){
        return this.AE_AEIAQBRH;
    }
    public void setAE_AEIAQBRH(String AE_AEIAQBRH){
        this.AE_AEIAQBRH = AE_AEIAQBRH;
    }
    public String getDRVS(){
        return this.DRVS;
    }
    public void setDRVS(String DRVS){
        this.DRVS = DRVS;
    }

    public void parseCbsTele(String tita) throws ParseException{
        this.setIMS_TRANS(tita.substring(0, 8));
        this.setSYSCODE(tita.substring(8, 12));
        this.setSYS_DATETIME(tita.substring(12, 26));
        this.setFEP_EJNO(tita.substring(26, 34));
        this.setTXN_FLOW(tita.substring(34, 35));
        this.setMSG_CAT(tita.substring(35, 37));
        this.setSOURCE_CHANNEL(tita.substring(37, 40));
        this.setPCODE(tita.substring(40, 44));
        this.setFSCODE(tita.substring(44, 46));
        this.setPROCESS_TYPE(tita.substring(46, 50));
        this.setBUSINESS_DATE(tita.substring(50, 57));
        this.setACQUIRER_BANK(tita.substring(57, 60));
        this.setTXNSTAN(tita.substring(60, 67));
        this.setTERMINALID(tita.substring(67, 75));
        this.setTERMINAL_TYPE(tita.substring(75, 79));
        this.setCARDISSUE_BANK(tita.substring(79, 82));
        this.setCARDTYPE(tita.substring(82, 83));
        this.setRESPONSE_CODE(tita.substring(83, 87));
        this.setHRVS(tita.substring(87, 120));
        this.setICCHIPSTAN(tita.substring(120, 128));
        this.setTERM_CHECKNO(tita.substring(128, 136));
        this.setTERMTXN_DATETIME(tita.substring(136, 150));
        this.setICMEMO(tita.substring(150, 180));
        this.setTXNICCTAC(tita.substring(180, 190));
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(tita.substring(190, 204).trim(), false, 2));
        this.setFROMACT(tita.substring(204, 220));
        this.setTRIN_BANKNO(tita.substring(220, 227));
        this.setTROUT_BANKNO(tita.substring(227, 234));
        this.setTOACT(tita.substring(234, 250));
        this.setPY_SPECIAL_FLAG(tita.substring(250, 252));
        this.setPY_HOST_BRANCH(tita.substring(252, 259));
        this.setPY_IDNO(tita.substring(259, 270));
        this.setPY_PAYUNTNO(tita.substring(270, 278));
        this.setPY_TAXTYPE(tita.substring(278, 283));
        this.setPY_PAYFEENO(tita.substring(283, 287));
        this.setPY_PAYTXNOL(tita.substring(287, 303));
        this.setPY_PAYDDATE(tita.substring(303, 311));
        this.setPY_PAYMEMO_FILL(tita.substring(311, 323));
        this.setPY_CHARGCUS(CodeGenUtil.asciiToBigDecimal(tita.substring(323, 327).trim(), false, 0));
        this.setPY_CHARGUNT(CodeGenUtil.asciiToBigDecimal(tita.substring(327, 347).trim(), false, 0));
        this.setPY_PAYTXNOL1(tita.substring(347, 371));
        this.setEATM_RESERVE_FLAG(tita.substring(371, 372));
        this.setORIGINAL_TX_DAYTIME(tita.substring(372, 386));
        this.setPY_AEISLLTY(tita.substring(386, 387));
        this.setAE_TRNSFROUTIDNO(tita.substring(387, 397));
        this.setAE_TRNSFLAG(tita.substring(397, 398));
        this.setAE_BUSINESSTYPE(tita.substring(398, 399));
        this.setAE_AEIEFEET(tita.substring(399, 401));
        this.setAE_TRNSFROUTNOTE(tita.substring(401, 419));
        this.setAE_LIMITTYPE(tita.substring(419, 421));
        this.setAE_FILLER(tita.substring(421, 439));
        this.setAE_AEIAQBRH(tita.substring(439, 443));
        this.setDRVS(tita.substring(443, 600));
    }

    public String makeMessage() {
        return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXN_FLOW(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSG_CAT(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSOURCE_CHANNEL(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPROCESS_TYPE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getBUSINESS_DATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACQUIRER_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNSTAN(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINALID(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMINAL_TYPE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDISSUE_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRESPONSE_CODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 33) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICCHIPSTAN(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERM_CHECKNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMTXN_DATETIME(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICMEMO(), 30) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXNICCTAC(), 10) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROMACT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRIN_BANKNO(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTROUT_BANKNO(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTOACT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_SPECIAL_FLAG(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_HOST_BRANCH(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_IDNO(), 11) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYUNTNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_TAXTYPE(), 5) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYFEENO(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYTXNOL(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYDDATE(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYMEMO_FILL(), 12) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getPY_CHARGCUS(), 4, false, 0, false) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getPY_CHARGUNT(), 20, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_PAYTXNOL1(), 24) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getEATM_RESERVE_FLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORIGINAL_TX_DAYTIME(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPY_AEISLLTY(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_TRNSFROUTIDNO(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_TRNSFLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_BUSINESSTYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEIEFEET(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_TRNSFROUTNOTE(), 18) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_LIMITTYPE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_FILLER(), 18) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEIAQBRH(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 157) //
            ;
    }
}
