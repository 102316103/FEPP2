package com.syscom.fep.vo.text.ims;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.vo.CodeGenUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;

public class CB_TR_I001 extends IMSTextBase {
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

    @Field(length = 4)
    private String ATMTRANSEQ = StringUtils.EMPTY;

    @Field(length = 25)
    private String HRVS = StringUtils.EMPTY;

    @Field(length = 4)
    private String CBSMAC = StringUtils.EMPTY;

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
    private String TR_SPECIAL_FLAG = StringUtils.EMPTY;

    @Field(length = 40)
    private String TXMEMO = StringUtils.EMPTY;

    @Field(length = 12)
    private String I_ACT = StringUtils.EMPTY;

    @Field(length = 2)
    private BigDecimal CHANNEL_CHARGE;

    @Field(length = 1)
    private String EATM_RESERVE_FLAG = StringUtils.EMPTY;

    @Field(length = 14)
    private String ORIGINAL_TX_DAYTIME = StringUtils.EMPTY;

    @Field(length = 16)
    private String TO_ACT_MEMO = StringUtils.EMPTY;

    @Field(length = 16)
    private String FROM_ACT_MEMO = StringUtils.EMPTY;

    @Field(length = 4)
    private String AE_AEIFSTBH = StringUtils.EMPTY;

    @Field(length = 1)
    private String AE_AEICIRCU = StringUtils.EMPTY;

    @Field(length = 1)
    private String AE_AEIDSTYP = StringUtils.EMPTY;

    @Field(length = 4)
    private String AE_AEIDSBRH = StringUtils.EMPTY;

    @Field(length = 2)
    private BigDecimal AE_AEIDSCRG;

    @Field(length = 2)
    private BigDecimal AE_AEIFIXFE;

    @Field(length = 2)
    private BigDecimal AE_AEINETFE;

    @Field(length = 2)
    private BigDecimal AE_AEIEDIFE;

    @Field(length = 1)
    private String AE_AEISLLTY = StringUtils.EMPTY;

    @Field(length = 2)
    private String AE_AEIEFEET = StringUtils.EMPTY;

    @Field(length = 12)
    private String AE_AEICIRCU_EDI = StringUtils.EMPTY;

    @Field(length = 214)
    private String DRVS = StringUtils.EMPTY;

    public String getIMS_TRANS() {
        return this.IMS_TRANS;
    }

    public void setIMS_TRANS(String IMS_TRANS) {
        this.IMS_TRANS = IMS_TRANS;
    }

    public String getSYSCODE() {
        return this.SYSCODE;
    }

    public void setSYSCODE(String SYSCODE) {
        this.SYSCODE = SYSCODE;
    }

    public String getSYS_DATETIME() {
        return this.SYS_DATETIME;
    }

    public void setSYS_DATETIME(String SYS_DATETIME) {
        this.SYS_DATETIME = SYS_DATETIME;
    }

    public String getFEP_EJNO() {
        return this.FEP_EJNO;
    }

    public void setFEP_EJNO(String FEP_EJNO) {
        this.FEP_EJNO = FEP_EJNO;
    }

    public String getTXN_FLOW() {
        return this.TXN_FLOW;
    }

    public void setTXN_FLOW(String TXN_FLOW) {
        this.TXN_FLOW = TXN_FLOW;
    }

    public String getMSG_CAT() {
        return this.MSG_CAT;
    }

    public void setMSG_CAT(String MSG_CAT) {
        this.MSG_CAT = MSG_CAT;
    }

    public String getSOURCE_CHANNEL() {
        return this.SOURCE_CHANNEL;
    }

    public void setSOURCE_CHANNEL(String SOURCE_CHANNEL) {
        this.SOURCE_CHANNEL = SOURCE_CHANNEL;
    }

    public String getPCODE() {
        return this.PCODE;
    }

    public void setPCODE(String PCODE) {
        this.PCODE = PCODE;
    }

    public String getFSCODE() {
        return this.FSCODE;
    }

    public void setFSCODE(String FSCODE) {
        this.FSCODE = FSCODE;
    }

    public String getPROCESS_TYPE() {
        return this.PROCESS_TYPE;
    }

    public void setPROCESS_TYPE(String PROCESS_TYPE) {
        this.PROCESS_TYPE = PROCESS_TYPE;
    }

    public String getBUSINESS_DATE() {
        return this.BUSINESS_DATE;
    }

    public void setBUSINESS_DATE(String BUSINESS_DATE) {
        this.BUSINESS_DATE = BUSINESS_DATE;
    }

    public String getACQUIRER_BANK() {
        return this.ACQUIRER_BANK;
    }

    public void setACQUIRER_BANK(String ACQUIRER_BANK) {
        this.ACQUIRER_BANK = ACQUIRER_BANK;
    }

    public String getTXNSTAN() {
        return this.TXNSTAN;
    }

    public void setTXNSTAN(String TXNSTAN) {
        this.TXNSTAN = TXNSTAN;
    }

    public String getTERMINALID() {
        return this.TERMINALID;
    }

    public void setTERMINALID(String TERMINALID) {
        this.TERMINALID = TERMINALID;
    }

    public String getTERMINAL_TYPE() {
        return this.TERMINAL_TYPE;
    }

    public void setTERMINAL_TYPE(String TERMINAL_TYPE) {
        this.TERMINAL_TYPE = TERMINAL_TYPE;
    }

    public String getCARDISSUE_BANK() {
        return this.CARDISSUE_BANK;
    }

    public void setCARDISSUE_BANK(String CARDISSUE_BANK) {
        this.CARDISSUE_BANK = CARDISSUE_BANK;
    }

    public String getCARDTYPE() {
        return this.CARDTYPE;
    }

    public void setCARDTYPE(String CARDTYPE) {
        this.CARDTYPE = CARDTYPE;
    }

    public String getRESPONSE_CODE() {
        return this.RESPONSE_CODE;
    }

    public void setRESPONSE_CODE(String RESPONSE_CODE) {
        this.RESPONSE_CODE = RESPONSE_CODE;
    }

    public String getATMTRANSEQ() {
        return this.ATMTRANSEQ;
    }

    public void setATMTRANSEQ(String ATMTRANSEQ) {
        this.ATMTRANSEQ = ATMTRANSEQ;
    }

    public String getHRVS() {
        return this.HRVS;
    }

    public void setHRVS(String HRVS) {
        this.HRVS = HRVS;
    }

    public String getCBSMAC() {
        return this.CBSMAC;
    }

    public void setCBSMAC(String CBSMAC) {
        this.CBSMAC = CBSMAC;
    }

    public String getICCHIPSTAN() {
        return this.ICCHIPSTAN;
    }

    public void setICCHIPSTAN(String ICCHIPSTAN) {
        this.ICCHIPSTAN = ICCHIPSTAN;
    }

    public String getTERM_CHECKNO() {
        return this.TERM_CHECKNO;
    }

    public void setTERM_CHECKNO(String TERM_CHECKNO) {
        this.TERM_CHECKNO = TERM_CHECKNO;
    }

    public String getTERMTXN_DATETIME() {
        return this.TERMTXN_DATETIME;
    }

    public void setTERMTXN_DATETIME(String TERMTXN_DATETIME) {
        this.TERMTXN_DATETIME = TERMTXN_DATETIME;
    }

    public String getICMEMO() {
        return this.ICMEMO;
    }

    public void setICMEMO(String ICMEMO) {
        this.ICMEMO = ICMEMO;
    }

    public String getTXNICCTAC() {
        return this.TXNICCTAC;
    }

    public void setTXNICCTAC(String TXNICCTAC) {
        this.TXNICCTAC = TXNICCTAC;
    }

    public BigDecimal getTXNAMT() {
        return this.TXNAMT;
    }

    public void setTXNAMT(BigDecimal TXNAMT) {
        this.TXNAMT = TXNAMT;
    }

    public String getFROMACT() {
        return this.FROMACT;
    }

    public void setFROMACT(String FROMACT) {
        this.FROMACT = FROMACT;
    }

    public String getTRIN_BANKNO() {
        return this.TRIN_BANKNO;
    }

    public void setTRIN_BANKNO(String TRIN_BANKNO) {
        this.TRIN_BANKNO = TRIN_BANKNO;
    }

    public String getTROUT_BANKNO() {
        return this.TROUT_BANKNO;
    }

    public void setTROUT_BANKNO(String TROUT_BANKNO) {
        this.TROUT_BANKNO = TROUT_BANKNO;
    }

    public String getTOACT() {
        return this.TOACT;
    }

    public void setTOACT(String TOACT) {
        this.TOACT = TOACT;
    }

    public String getTR_SPECIAL_FLAG() {
        return this.TR_SPECIAL_FLAG;
    }

    public void setTR_SPECIAL_FLAG(String TR_SPECIAL_FLAG) {
        this.TR_SPECIAL_FLAG = TR_SPECIAL_FLAG;
    }

    public String getTXMEMO() {
        return this.TXMEMO;
    }

    public void setTXMEMO(String TXMEMO) {
        this.TXMEMO = TXMEMO;
    }

    public String getI_ACT() {
        return this.I_ACT;
    }

    public void setI_ACT(String I_ACT) {
        this.I_ACT = I_ACT;
    }

    public BigDecimal getCHANNEL_CHARGE() {
        return this.CHANNEL_CHARGE;
    }

    public void setCHANNEL_CHARGE(BigDecimal CHANNEL_CHARGE) {
        this.CHANNEL_CHARGE = CHANNEL_CHARGE;
    }

    public String getEATM_RESERVE_FLAG() {
        return this.EATM_RESERVE_FLAG;
    }

    public void setEATM_RESERVE_FLAG(String EATM_RESERVE_FLAG) {
        this.EATM_RESERVE_FLAG = EATM_RESERVE_FLAG;
    }

    public String getORIGINAL_TX_DAYTIME() {
        return this.ORIGINAL_TX_DAYTIME;
    }

    public void setORIGINAL_TX_DAYTIME(String ORIGINAL_TX_DAYTIME) {
        this.ORIGINAL_TX_DAYTIME = ORIGINAL_TX_DAYTIME;
    }

    public String getTO_ACT_MEMO() {
        return this.TO_ACT_MEMO;
    }

    public void setTO_ACT_MEMO(String TO_ACT_MEMO) {
        this.TO_ACT_MEMO = TO_ACT_MEMO;
    }

    public String getFROM_ACT_MEMO() {
        return this.FROM_ACT_MEMO;
    }

    public void setFROM_ACT_MEMO(String FROM_ACT_MEMO) {
        this.FROM_ACT_MEMO = FROM_ACT_MEMO;
    }

    public String getAE_AEIFSTBH() {
        return this.AE_AEIFSTBH;
    }

    public void setAE_AEIFSTBH(String AE_AEIFSTBH) {
        this.AE_AEIFSTBH = AE_AEIFSTBH;
    }

    public String getAE_AEICIRCU() {
        return this.AE_AEICIRCU;
    }

    public void setAE_AEICIRCU(String AE_AEICIRCU) {
        this.AE_AEICIRCU = AE_AEICIRCU;
    }

    public String getAE_AEIDSTYP() {
        return this.AE_AEIDSTYP;
    }

    public void setAE_AEIDSTYP(String AE_AEIDSTYP) {
        this.AE_AEIDSTYP = AE_AEIDSTYP;
    }

    public String getAE_AEIDSBRH() {
        return this.AE_AEIDSBRH;
    }

    public void setAE_AEIDSBRH(String AE_AEIDSBRH) {
        this.AE_AEIDSBRH = AE_AEIDSBRH;
    }

    public BigDecimal getAE_AEIDSCRG() {
        return this.AE_AEIDSCRG;
    }

    public void setAE_AEIDSCRG(BigDecimal AE_AEIDSCRG) {
        this.AE_AEIDSCRG = AE_AEIDSCRG;
    }

    public BigDecimal getAE_AEIFIXFE() {
        return this.AE_AEIFIXFE;
    }

    public void setAE_AEIFIXFE(BigDecimal AE_AEIFIXFE) {
        this.AE_AEIFIXFE = AE_AEIFIXFE;
    }

    public BigDecimal getAE_AEINETFE() {
        return this.AE_AEINETFE;
    }

    public void setAE_AEINETFE(BigDecimal AE_AEINETFE) {
        this.AE_AEINETFE = AE_AEINETFE;
    }

    public BigDecimal getAE_AEIEDIFE() {
        return this.AE_AEIEDIFE;
    }

    public void setAE_AEIEDIFE(BigDecimal AE_AEIEDIFE) {
        this.AE_AEIEDIFE = AE_AEIEDIFE;
    }

    public String getAE_AEISLLTY() {
        return this.AE_AEISLLTY;
    }

    public void setAE_AEISLLTY(String AE_AEISLLTY) {
        this.AE_AEISLLTY = AE_AEISLLTY;
    }

    public String getAE_AEIEFEET() {
        return this.AE_AEIEFEET;
    }

    public void setAE_AEIEFEET(String AE_AEIEFEET) {
        this.AE_AEIEFEET = AE_AEIEFEET;
    }

    public String getAE_AEICIRCU_EDI() {
        return this.AE_AEICIRCU_EDI;
    }

    public void setAE_AEICIRCU_EDI(String AE_AEICIRCU_EDI) {
        this.AE_AEICIRCU_EDI = AE_AEICIRCU_EDI;
    }

    public String getDRVS() {
        return this.DRVS;
    }

    public void setDRVS(String DRVS) {
        this.DRVS = DRVS;
    }


    public void parseCbsTele(String tita) throws ParseException {
        this.setIMS_TRANS(EbcdicConverter.fromHex(CCSID.English, tita.substring(0, 16)));
        this.setSYSCODE(EbcdicConverter.fromHex(CCSID.English, tita.substring(16, 24)));
        this.setSYS_DATETIME(EbcdicConverter.fromHex(CCSID.English, tita.substring(24, 52)));
        this.setFEP_EJNO(EbcdicConverter.fromHex(CCSID.English, tita.substring(52, 68)));
        this.setTXN_FLOW(EbcdicConverter.fromHex(CCSID.English, tita.substring(68, 70)));
        this.setMSG_CAT(EbcdicConverter.fromHex(CCSID.English, tita.substring(70, 74)));
        this.setSOURCE_CHANNEL(EbcdicConverter.fromHex(CCSID.English, tita.substring(74, 80)));
        this.setPCODE(EbcdicConverter.fromHex(CCSID.English, tita.substring(80, 88)));
        this.setFSCODE(EbcdicConverter.fromHex(CCSID.English, tita.substring(88, 92)));
        this.setPROCESS_TYPE(EbcdicConverter.fromHex(CCSID.English, tita.substring(92, 100)));
        this.setBUSINESS_DATE(EbcdicConverter.fromHex(CCSID.English, tita.substring(100, 114)));
        this.setACQUIRER_BANK(EbcdicConverter.fromHex(CCSID.English, tita.substring(114, 120)));
        this.setTXNSTAN(EbcdicConverter.fromHex(CCSID.English, tita.substring(120, 134)));
        this.setTERMINALID(EbcdicConverter.fromHex(CCSID.English, tita.substring(134, 150)));
        this.setTERMINAL_TYPE(EbcdicConverter.fromHex(CCSID.English, tita.substring(150, 158)));
        this.setCARDISSUE_BANK(EbcdicConverter.fromHex(CCSID.English, tita.substring(158, 164)));
        this.setCARDTYPE(EbcdicConverter.fromHex(CCSID.English, tita.substring(164, 166)));
        this.setRESPONSE_CODE(EbcdicConverter.fromHex(CCSID.English, tita.substring(166, 174)));
        this.setATMTRANSEQ(EbcdicConverter.fromHex(CCSID.English, tita.substring(174, 182)));
        this.setHRVS(EbcdicConverter.fromHex(CCSID.English, tita.substring(182, 232)));
        this.setCBSMAC(EbcdicConverter.fromHex(CCSID.English, tita.substring(232, 240)));
        this.setICCHIPSTAN(EbcdicConverter.fromHex(CCSID.English, tita.substring(240, 256)));
        this.setTERM_CHECKNO(EbcdicConverter.fromHex(CCSID.English, tita.substring(256, 272)));
        this.setTERMTXN_DATETIME(EbcdicConverter.fromHex(CCSID.English, tita.substring(272, 300)));
        this.setICMEMO(EbcdicConverter.fromHex(CCSID.English, tita.substring(300, 360)));
        this.setTXNICCTAC(EbcdicConverter.fromHex(CCSID.English, tita.substring(360, 380)));
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English, tita.substring(380, 408).trim()), false, 2));
        this.setFROMACT(EbcdicConverter.fromHex(CCSID.English, tita.substring(408, 440)));
        this.setTRIN_BANKNO(EbcdicConverter.fromHex(CCSID.English, tita.substring(440, 454)));
        this.setTROUT_BANKNO(EbcdicConverter.fromHex(CCSID.English, tita.substring(454, 468)));
        this.setTOACT(EbcdicConverter.fromHex(CCSID.English, tita.substring(468, 500)));
        this.setTR_SPECIAL_FLAG(EbcdicConverter.fromHex(CCSID.English, tita.substring(500, 504)));
        this.setTXMEMO(EbcdicConverter.changeChinese(tita.substring(504, 584)));
        this.setI_ACT(EbcdicConverter.fromHex(CCSID.English, tita.substring(584, 608)));
        this.setCHANNEL_CHARGE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English, tita.substring(608, 612).trim()), false, 0));
        this.setEATM_RESERVE_FLAG(EbcdicConverter.fromHex(CCSID.English, tita.substring(612, 614)));
        this.setORIGINAL_TX_DAYTIME(EbcdicConverter.fromHex(CCSID.English, tita.substring(614, 642)));
        this.setTO_ACT_MEMO(EbcdicConverter.changeChinese(tita.substring(642, 674)));
        this.setFROM_ACT_MEMO(EbcdicConverter.changeChinese(tita.substring(674, 706)));
        this.setAE_AEIFSTBH(EbcdicConverter.fromHex(CCSID.English, tita.substring(706, 714)));
        this.setAE_AEICIRCU(EbcdicConverter.fromHex(CCSID.English, tita.substring(714, 716)));
        this.setAE_AEIDSTYP(EbcdicConverter.fromHex(CCSID.English, tita.substring(716, 718)));
        this.setAE_AEIDSBRH(EbcdicConverter.fromHex(CCSID.English, tita.substring(718, 726)));
        this.setAE_AEIDSCRG(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English, tita.substring(726, 730).trim()), false, 0));
        this.setAE_AEIFIXFE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English, tita.substring(730, 734).trim()), false, 0));
        this.setAE_AEINETFE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English, tita.substring(734, 738).trim()), false, 0));
        this.setAE_AEIEDIFE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English, tita.substring(738, 742).trim()), false, 0));
        this.setAE_AEISLLTY(EbcdicConverter.fromHex(CCSID.English, tita.substring(742, 744)));
        this.setAE_AEIEFEET(EbcdicConverter.fromHex(CCSID.English, tita.substring(744, 748)));
        this.setAE_AEICIRCU_EDI(EbcdicConverter.fromHex(CCSID.English, tita.substring(748, 772)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English, tita.substring(772, 1200)));
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
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getATMTRANSEQ(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 25) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCBSMAC(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getICCHIPSTAN(), 8) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERM_CHECKNO(), 8) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTERMTXN_DATETIME(), 14) //
                + this.getICMEMO()  //
                + this.getTXNICCTAC()  //
                + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROMACT(), 16) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRIN_BANKNO(), 7) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTROUT_BANKNO(), 7) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTOACT(), 16) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTR_SPECIAL_FLAG(), 2) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXMEMO(), 40) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getI_ACT(), 12) //
                + CodeGenUtil.bigDecimalToEbcdic(this.getCHANNEL_CHARGE(), 2, false, 0, false) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getEATM_RESERVE_FLAG(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getORIGINAL_TX_DAYTIME(), 14) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTO_ACT_MEMO(), 16) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFROM_ACT_MEMO(), 16) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEIFSTBH(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEICIRCU(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEIDSTYP(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEIDSBRH(), 4) //
                + CodeGenUtil.bigDecimalToEbcdic(this.getAE_AEIDSCRG(), 2, false, 0, false) //
                + CodeGenUtil.bigDecimalToEbcdic(this.getAE_AEIFIXFE(), 2, false, 0, false) //
                + CodeGenUtil.bigDecimalToEbcdic(this.getAE_AEINETFE(), 2, false, 0, false) //
                + CodeGenUtil.bigDecimalToEbcdic(this.getAE_AEIEDIFE(), 2, false, 0, false) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEISLLTY(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEIEFEET(), 2) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAE_AEICIRCU_EDI(), 12) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 214) //
                ;
    }

    public String makeMessageAscii() {
        return "" //
                + StringUtils.rightPad(this.getIMS_TRANS(), 8, " ") //
                + StringUtils.rightPad(this.getSYSCODE(), 4, " ") //
                + StringUtils.rightPad(this.getSYS_DATETIME(), 14, " ") //
                + StringUtils.rightPad(this.getFEP_EJNO(), 8, " ") //
                + StringUtils.rightPad(this.getTXN_FLOW(), 1, " ") //
                + StringUtils.rightPad(this.getMSG_CAT(), 2, " ") //
                + StringUtils.rightPad(this.getSOURCE_CHANNEL(), 3, " ") //
                + StringUtils.rightPad(this.getPCODE(), 4, " ") //
                + StringUtils.rightPad(this.getFSCODE(), 2, " ") //
                + StringUtils.rightPad(this.getPROCESS_TYPE(), 4, " ") //
                + StringUtils.rightPad(this.getBUSINESS_DATE(), 7, " ") //
                + StringUtils.rightPad(this.getACQUIRER_BANK(), 3, " ") //
                + StringUtils.rightPad(this.getTXNSTAN(), 7, " ") //
                + StringUtils.rightPad(this.getTERMINALID(), 8, " ") //
                + StringUtils.rightPad(this.getTERMINAL_TYPE(), 4, " ") //
                + StringUtils.rightPad(this.getCARDISSUE_BANK(), 3, " ") //
                + StringUtils.rightPad(this.getCARDTYPE(), 1, " ") //
                + StringUtils.rightPad(this.getRESPONSE_CODE(), 4, " ") //
                + StringUtils.rightPad(this.getATMTRANSEQ(), 4, " ") //
                + StringUtils.rightPad(this.getHRVS(), 25, " ") //
                + StringUtils.rightPad(this.getCBSMAC(), 4, " ") //
                + StringUtils.rightPad(this.getICCHIPSTAN(), 8, " ") //
                + StringUtils.rightPad(this.getTERM_CHECKNO(), 8, " ") //
                + StringUtils.rightPad(this.getTERMTXN_DATETIME(), 14, " ") //
                + this.getICMEMO()  //
                + this.getTXNICCTAC()  //
                + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNAMT(), 11, false, 2, true) //
                + StringUtils.rightPad(this.getFROMACT(), 16, " ") //
                + StringUtils.rightPad(this.getTRIN_BANKNO(), 7, " ") //
                + StringUtils.rightPad(this.getTROUT_BANKNO(), 7, " ") //
                + StringUtils.rightPad(this.getTOACT(), 16, " ") //
                + StringUtils.rightPad(this.getTR_SPECIAL_FLAG(), 2, " ") //
                + StringUtils.rightPad(this.getTXMEMO(), 40, " ") //
                + StringUtils.rightPad(this.getI_ACT(), 12, " ") //
                + CodeGenUtil.bigDecimalToAsciiCBS(this.getCHANNEL_CHARGE(), 2, false, 0, false) //
                + StringUtils.rightPad(this.getEATM_RESERVE_FLAG(), 1, " ") //
                + StringUtils.rightPad(this.getORIGINAL_TX_DAYTIME(), 14, " ") //
                + StringUtils.rightPad(this.getTO_ACT_MEMO(), 16, " ") //
                + StringUtils.rightPad(this.getFROM_ACT_MEMO(), 16, " ") //
                + StringUtils.rightPad(this.getAE_AEIFSTBH(), 4, " ") //
                + StringUtils.rightPad(this.getAE_AEICIRCU(), 1, " ") //
                + StringUtils.rightPad(this.getAE_AEIDSTYP(), 1, " ") //
                + StringUtils.rightPad(this.getAE_AEIDSBRH(), 4, " ") //
                + CodeGenUtil.bigDecimalToAsciiCBS(this.getAE_AEIDSCRG(), 2, false, 0, false) //
                + CodeGenUtil.bigDecimalToAsciiCBS(this.getAE_AEIFIXFE(), 2, false, 0, false) //
                + CodeGenUtil.bigDecimalToAsciiCBS(this.getAE_AEINETFE(), 2, false, 0, false) //
                + CodeGenUtil.bigDecimalToAsciiCBS(this.getAE_AEIEDIFE(), 2, false, 0, false) //
                + StringUtils.rightPad(this.getAE_AEISLLTY(), 1, " ") //
                + StringUtils.rightPad(this.getAE_AEIEFEET(), 2, " ") //
                + StringUtils.rightPad(this.getAE_AEICIRCU_EDI(), 12, " ") //
                + StringUtils.rightPad(this.getDRVS(), 214, " ") //
                ;
    }
}
