package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class IB_UHT1_O001 extends IMSTextBase {
    @Field(length = 8)
        private String IMS_PYTAXANS = StringUtils.EMPTY;

    @Field(length = 4)
        private String SYSCODE = StringUtils.EMPTY;

    @Field(length = 14)
        private String SYS_DATETIME = StringUtils.EMPTY;

    @Field(length = 8)
        private String FEP_EJNO = StringUtils.EMPTY;

    @Field(length = 2)
        private String FSCODE = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMSRC4_FISC = StringUtils.EMPTY;

    @Field(length = 3)
        private String IMSRC_TCB = StringUtils.EMPTY;

    @Field(length = 7)
        private String IMSBUSINESS_DATE = StringUtils.EMPTY;

    @Field(length = 1)
        private String IMSACCT_FLAG = StringUtils.EMPTY;

    @Field(length = 1)
        private String IMSRVS_FLAG = StringUtils.EMPTY;

    @Field(length = 6)
        private String IMS_TXN_TIME = StringUtils.EMPTY;

    @Field(length = 10)
        private String IMS_OUT_STAN = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMS_FMMBR = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMS_TMMBR = StringUtils.EMPTY;

    @Field(length = 20)
        private String HRVS = StringUtils.EMPTY;

    @Field(length = 4)
        private String IMS_MAC = StringUtils.EMPTY;

    @Field(length = 1)
        private String NOTICE_TYPE = StringUtils.EMPTY;

    @Field(length = 3)
        private String NOTICE_NUMBER = StringUtils.EMPTY;

    @Field(length = 10)
        private String NOTICE_CUSID = StringUtils.EMPTY;

    @Field(length = 10)
        private String NOTICE_MOBILENO = StringUtils.EMPTY;

    @Field(length = 50)
        private String NOTICE_EMAIL = StringUtils.EMPTY;

    @Field(length = 1)
        private String SEND_FISC2160 = StringUtils.EMPTY;

    @Field(length = 2)
        private String O_TXN_TYPE_CODE = StringUtils.EMPTY;

    @Field(length = 14)
        private BigDecimal TXNAMT;

    @Field(length = 4)
        private BigDecimal TXNCHARGE;

    @Field(length = 3)
        private String O_FROM_BANK = StringUtils.EMPTY;

    @Field(length = 16)
        private String O_FROM_ACT = StringUtils.EMPTY;

    @Field(length = 3)
        private String O_TO_BANK = StringUtils.EMPTY;

    @Field(length = 16)
        private String O_TO_ACT = StringUtils.EMPTY;

    @Field(length = 14)
        private BigDecimal ACT_BALANCE;

    @Field(length = 16)
        private String LUCKYNO = StringUtils.EMPTY;

    @Field(length = 12)
        private String O_FILLER_TR = StringUtils.EMPTY;

    @Field(length = 14)
        private String AVAILABLE_BALANCE = StringUtils.EMPTY;

    @Field(length = 1)
        private String O_Redeem_State = StringUtils.EMPTY;

    @Field(length = 10)
        private String O_Add_Point = StringUtils.EMPTY;

    @Field(length = 10)
        private String O_Deduct_Point = StringUtils.EMPTY;

    @Field(length = 1)
        private String O_Extra_amt_type = StringUtils.EMPTY;

    @Field(length = 10)
        private String O_Extra_Amount = StringUtils.EMPTY;

    @Field(length = 3)
        private String O_Extra_Curr_code = StringUtils.EMPTY;

    @Field(length = 13)
        private String O_Final_TX_amt = StringUtils.EMPTY;

    @Field(length = 3)
        private String O_Final_TX_Curr_code = StringUtils.EMPTY;

    @Field(length = 49)
        private String O_BIT46_POT_RFU = StringUtils.EMPTY;

    @Field(length = 211)
        private String DRVS = StringUtils.EMPTY;

        public String getIMS_PYTAXANS(){
        return this.IMS_PYTAXANS;
        }
        public void setIMS_PYTAXANS(String IMS_PYTAXANS){
        this.IMS_PYTAXANS = IMS_PYTAXANS;
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
        public String getFSCODE(){
        return this.FSCODE;
        }
        public void setFSCODE(String FSCODE){
        this.FSCODE = FSCODE;
        }
        public String getIMSRC4_FISC(){
        return this.IMSRC4_FISC;
        }
        public void setIMSRC4_FISC(String IMSRC4_FISC){
        this.IMSRC4_FISC = IMSRC4_FISC;
        }
        public String getIMSRC_TCB(){
        return this.IMSRC_TCB;
        }
        public void setIMSRC_TCB(String IMSRC_TCB){
        this.IMSRC_TCB = IMSRC_TCB;
        }
        public String getIMSBUSINESS_DATE(){
        return this.IMSBUSINESS_DATE;
        }
        public void setIMSBUSINESS_DATE(String IMSBUSINESS_DATE){
        this.IMSBUSINESS_DATE = IMSBUSINESS_DATE;
        }
        public String getIMSACCT_FLAG(){
        return this.IMSACCT_FLAG;
        }
        public void setIMSACCT_FLAG(String IMSACCT_FLAG){
        this.IMSACCT_FLAG = IMSACCT_FLAG;
        }
        public String getIMSRVS_FLAG(){
        return this.IMSRVS_FLAG;
        }
        public void setIMSRVS_FLAG(String IMSRVS_FLAG){
        this.IMSRVS_FLAG = IMSRVS_FLAG;
        }
        public String getIMS_TXN_TIME(){
        return this.IMS_TXN_TIME;
        }
        public void setIMS_TXN_TIME(String IMS_TXN_TIME){
        this.IMS_TXN_TIME = IMS_TXN_TIME;
        }
        public String getIMS_OUT_STAN(){
        return this.IMS_OUT_STAN;
        }
        public void setIMS_OUT_STAN(String IMS_OUT_STAN){
        this.IMS_OUT_STAN = IMS_OUT_STAN;
        }
        public String getIMS_FMMBR(){
        return this.IMS_FMMBR;
        }
        public void setIMS_FMMBR(String IMS_FMMBR){
        this.IMS_FMMBR = IMS_FMMBR;
        }
        public String getIMS_TMMBR(){
        return this.IMS_TMMBR;
        }
        public void setIMS_TMMBR(String IMS_TMMBR){
        this.IMS_TMMBR = IMS_TMMBR;
        }
        public String getHRVS(){
        return this.HRVS;
        }
        public void setHRVS(String HRVS){
        this.HRVS = HRVS;
        }
        public String getIMS_MAC(){
        return this.IMS_MAC;
        }
        public void setIMS_MAC(String IMS_MAC){
        this.IMS_MAC = IMS_MAC;
        }
        public String getNOTICE_TYPE(){
        return this.NOTICE_TYPE;
        }
        public void setNOTICE_TYPE(String NOTICE_TYPE){
        this.NOTICE_TYPE = NOTICE_TYPE;
        }
        public String getNOTICE_NUMBER(){
        return this.NOTICE_NUMBER;
        }
        public void setNOTICE_NUMBER(String NOTICE_NUMBER){
        this.NOTICE_NUMBER = NOTICE_NUMBER;
        }
        public String getNOTICE_CUSID(){
        return this.NOTICE_CUSID;
        }
        public void setNOTICE_CUSID(String NOTICE_CUSID){
        this.NOTICE_CUSID = NOTICE_CUSID;
        }
        public String getNOTICE_MOBILENO(){
        return this.NOTICE_MOBILENO;
        }
        public void setNOTICE_MOBILENO(String NOTICE_MOBILENO){
        this.NOTICE_MOBILENO = NOTICE_MOBILENO;
        }
        public String getNOTICE_EMAIL(){
        return this.NOTICE_EMAIL;
        }
        public void setNOTICE_EMAIL(String NOTICE_EMAIL){
        this.NOTICE_EMAIL = NOTICE_EMAIL;
        }
        public String getSEND_FISC2160(){
        return this.SEND_FISC2160;
        }
        public void setSEND_FISC2160(String SEND_FISC2160){
        this.SEND_FISC2160 = SEND_FISC2160;
        }
        public String getO_TXN_TYPE_CODE(){
        return this.O_TXN_TYPE_CODE;
        }
        public void setO_TXN_TYPE_CODE(String O_TXN_TYPE_CODE){
        this.O_TXN_TYPE_CODE = O_TXN_TYPE_CODE;
        }
        public BigDecimal getTXNAMT(){
        return this.TXNAMT;
        }
        public void setTXNAMT(BigDecimal TXNAMT){
        this.TXNAMT = TXNAMT;
        }
        public BigDecimal getTXNCHARGE(){
        return this.TXNCHARGE;
        }
        public void setTXNCHARGE(BigDecimal TXNCHARGE){
        this.TXNCHARGE = TXNCHARGE;
        }
        public String getO_FROM_BANK(){
        return this.O_FROM_BANK;
        }
        public void setO_FROM_BANK(String O_FROM_BANK){
        this.O_FROM_BANK = O_FROM_BANK;
        }
        public String getO_FROM_ACT(){
        return this.O_FROM_ACT;
        }
        public void setO_FROM_ACT(String O_FROM_ACT){
        this.O_FROM_ACT = O_FROM_ACT;
        }
        public String getO_TO_BANK(){
        return this.O_TO_BANK;
        }
        public void setO_TO_BANK(String O_TO_BANK){
        this.O_TO_BANK = O_TO_BANK;
        }
        public String getO_TO_ACT(){
        return this.O_TO_ACT;
        }
        public void setO_TO_ACT(String O_TO_ACT){
        this.O_TO_ACT = O_TO_ACT;
        }
        public BigDecimal getACT_BALANCE(){
        return this.ACT_BALANCE;
        }
        public void setACT_BALANCE(BigDecimal ACT_BALANCE){
        this.ACT_BALANCE = ACT_BALANCE;
        }
        public String getLUCKYNO(){
        return this.LUCKYNO;
        }
        public void setLUCKYNO(String LUCKYNO){
        this.LUCKYNO = LUCKYNO;
        }
        public String getO_FILLER_TR(){
        return this.O_FILLER_TR;
        }
        public void setO_FILLER_TR(String O_FILLER_TR){
        this.O_FILLER_TR = O_FILLER_TR;
        }
        public String getAVAILABLE_BALANCE(){
        return this.AVAILABLE_BALANCE;
        }
        public void setAVAILABLE_BALANCE(String AVAILABLE_BALANCE){
        this.AVAILABLE_BALANCE = AVAILABLE_BALANCE;
        }
        public String getO_Redeem_State(){
        return this.O_Redeem_State;
        }
        public void setO_Redeem_State(String O_Redeem_State){
        this.O_Redeem_State = O_Redeem_State;
        }
        public String getO_Add_Point(){
        return this.O_Add_Point;
        }
        public void setO_Add_Point(String O_Add_Point){
        this.O_Add_Point = O_Add_Point;
        }
        public String getO_Deduct_Point(){
        return this.O_Deduct_Point;
        }
        public void setO_Deduct_Point(String O_Deduct_Point){
        this.O_Deduct_Point = O_Deduct_Point;
        }
        public String getO_Extra_amt_type(){
        return this.O_Extra_amt_type;
        }
        public void setO_Extra_amt_type(String O_Extra_amt_type){
        this.O_Extra_amt_type = O_Extra_amt_type;
        }
        public String getO_Extra_Amount(){
        return this.O_Extra_Amount;
        }
        public void setO_Extra_Amount(String O_Extra_Amount){
        this.O_Extra_Amount = O_Extra_Amount;
        }
        public String getO_Extra_Curr_code(){
        return this.O_Extra_Curr_code;
        }
        public void setO_Extra_Curr_code(String O_Extra_Curr_code){
        this.O_Extra_Curr_code = O_Extra_Curr_code;
        }
        public String getO_Final_TX_amt(){
        return this.O_Final_TX_amt;
        }
        public void setO_Final_TX_amt(String O_Final_TX_amt){
        this.O_Final_TX_amt = O_Final_TX_amt;
        }
        public String getO_Final_TX_Curr_code(){
        return this.O_Final_TX_Curr_code;
        }
        public void setO_Final_TX_Curr_code(String O_Final_TX_Curr_code){
        this.O_Final_TX_Curr_code = O_Final_TX_Curr_code;
        }
        public String getO_BIT46_POT_RFU(){
        return this.O_BIT46_POT_RFU;
        }
        public void setO_BIT46_POT_RFU(String O_BIT46_POT_RFU){
        this.O_BIT46_POT_RFU = O_BIT46_POT_RFU;
        }
        public String getDRVS(){
        return this.DRVS;
        }
        public void setDRVS(String DRVS){
        this.DRVS = DRVS;
        }

public void parseCbsTele(String tita) throws ParseException{
        this.setIMS_PYTAXANS(EbcdicConverter.fromHex(CCSID.English,tita.substring(0, 16)));
        this.setSYSCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(16, 24)));
        this.setSYS_DATETIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(24, 52)));
        this.setFEP_EJNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(52, 68)));
        this.setFSCODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(68, 72)));
        this.setIMSRC4_FISC(EbcdicConverter.fromHex(CCSID.English,tita.substring(72, 80)));
        this.setIMSRC_TCB(EbcdicConverter.fromHex(CCSID.English,tita.substring(80, 86)));
        this.setIMSBUSINESS_DATE(EbcdicConverter.fromHex(CCSID.English,tita.substring(86, 100)));
        this.setIMSACCT_FLAG(EbcdicConverter.fromHex(CCSID.English,tita.substring(100, 102)));
        this.setIMSRVS_FLAG(EbcdicConverter.fromHex(CCSID.English,tita.substring(102, 104)));
        this.setIMS_TXN_TIME(EbcdicConverter.fromHex(CCSID.English,tita.substring(104, 116)));
        this.setIMS_OUT_STAN(EbcdicConverter.fromHex(CCSID.English,tita.substring(116, 136)));
        this.setIMS_FMMBR(EbcdicConverter.fromHex(CCSID.English,tita.substring(136, 144)));
        this.setIMS_TMMBR(EbcdicConverter.fromHex(CCSID.English,tita.substring(144, 152)));
        this.setHRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(152, 192)));
        this.setIMS_MAC(EbcdicConverter.fromHex(CCSID.English,tita.substring(192, 200)));
        this.setNOTICE_TYPE(EbcdicConverter.fromHex(CCSID.English,tita.substring(200, 202)));
        this.setNOTICE_NUMBER(EbcdicConverter.fromHex(CCSID.English,tita.substring(202, 208)));
        this.setNOTICE_CUSID(EbcdicConverter.fromHex(CCSID.English,tita.substring(208, 228)));
        this.setNOTICE_MOBILENO(EbcdicConverter.fromHex(CCSID.English,tita.substring(228, 248)));
        this.setNOTICE_EMAIL(EbcdicConverter.fromHex(CCSID.English,tita.substring(248, 348)));
        this.setSEND_FISC2160(EbcdicConverter.fromHex(CCSID.English,tita.substring(348, 350)));
        this.setO_TXN_TYPE_CODE(EbcdicConverter.fromHex(CCSID.English,tita.substring(350, 354)));
        this.setTXNAMT(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(354, 382).trim()), false, 2));
        this.setTXNCHARGE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(382, 390).trim()), false, 0));
        this.setO_FROM_BANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(390, 396)));
        this.setO_FROM_ACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(396, 428)));
        this.setO_TO_BANK(EbcdicConverter.fromHex(CCSID.English,tita.substring(428, 434)));
        this.setO_TO_ACT(EbcdicConverter.fromHex(CCSID.English,tita.substring(434, 466)));
        this.setACT_BALANCE(CodeGenUtil.asciiToBigDecimal(EbcdicConverter.fromHex(CCSID.English,tita.substring(466, 494).trim()), false, 2));
        this.setLUCKYNO(EbcdicConverter.fromHex(CCSID.English,tita.substring(494, 526)));
        this.setO_FILLER_TR(EbcdicConverter.fromHex(CCSID.English,tita.substring(526, 550)));
        this.setAVAILABLE_BALANCE(EbcdicConverter.fromHex(CCSID.English,tita.substring(550, 578)));
        this.setO_Redeem_State(EbcdicConverter.fromHex(CCSID.English,tita.substring(578, 580)));
        this.setO_Add_Point(EbcdicConverter.fromHex(CCSID.English,tita.substring(580, 600)));
        this.setO_Deduct_Point(EbcdicConverter.fromHex(CCSID.English,tita.substring(600, 620)));
        this.setO_Extra_amt_type(EbcdicConverter.fromHex(CCSID.English,tita.substring(620, 622)));
        this.setO_Extra_Amount(EbcdicConverter.fromHex(CCSID.English,tita.substring(622, 642)));
        this.setO_Extra_Curr_code(EbcdicConverter.fromHex(CCSID.English,tita.substring(642, 648)));
        this.setO_Final_TX_amt(EbcdicConverter.fromHex(CCSID.English,tita.substring(648, 674)));
        this.setO_Final_TX_Curr_code(EbcdicConverter.fromHex(CCSID.English,tita.substring(674, 680)));
        this.setO_BIT46_POT_RFU(EbcdicConverter.fromHex(CCSID.English,tita.substring(680, 778)));
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(778, 1200)));
}

public String makeMessage() {
return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_PYTAXANS(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYSCODE(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSYS_DATETIME(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFEP_EJNO(), 8) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(), 2) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRC4_FISC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRC_TCB(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSBUSINESS_DATE(), 7) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSACCT_FLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRVS_FLAG(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TXN_TIME(), 6) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_OUT_STAN(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_FMMBR(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TMMBR(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getHRVS(), 20) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_MAC(), 4) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getNOTICE_TYPE(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getNOTICE_NUMBER(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getNOTICE_CUSID(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getNOTICE_MOBILENO(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getNOTICE_EMAIL(), 50) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSEND_FISC2160(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_TXN_TYPE_CODE(), 2) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNAMT(), 11, false, 2, true) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getTXNCHARGE(), 4, false, 0, false) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_FROM_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_FROM_ACT(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_TO_BANK(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_TO_ACT(), 16) //
            + CodeGenUtil.bigDecimalToEbcdic(this.getACT_BALANCE(), 11, false, 2, true) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getLUCKYNO(), 16) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_FILLER_TR(), 12) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getAVAILABLE_BALANCE(), 14) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Redeem_State(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Add_Point(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Deduct_Point(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Extra_amt_type(), 1) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Extra_Amount(), 10) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Extra_Curr_code(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Final_TX_amt(), 13) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_Final_TX_Curr_code(), 3) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getO_BIT46_POT_RFU(), 49) //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 211) //
;
}

public String makeMessageAscii() {
return "" //
            + StringUtils.leftPad(this.getIMS_PYTAXANS(), 8," ") //
            + StringUtils.leftPad(this.getSYSCODE(), 4," ") //
            + StringUtils.leftPad(this.getSYS_DATETIME(), 14," ") //
            + StringUtils.leftPad(this.getFEP_EJNO(), 8," ") //
            + StringUtils.leftPad(this.getFSCODE(), 2," ") //
            + StringUtils.leftPad(this.getIMSRC4_FISC(), 4," ") //
            + StringUtils.leftPad(this.getIMSRC_TCB(), 3," ") //
            + StringUtils.leftPad(this.getIMSBUSINESS_DATE(), 7," ") //
            + StringUtils.leftPad(this.getIMSACCT_FLAG(), 1," ") //
            + StringUtils.leftPad(this.getIMSRVS_FLAG(), 1," ") //
            + StringUtils.leftPad(this.getIMS_TXN_TIME(), 6," ") //
            + StringUtils.leftPad(this.getIMS_OUT_STAN(), 10," ") //
            + StringUtils.leftPad(this.getIMS_FMMBR(), 4," ") //
            + StringUtils.leftPad(this.getIMS_TMMBR(), 4," ") //
            + StringUtils.leftPad(this.getHRVS(), 20," ") //
            + StringUtils.leftPad(this.getIMS_MAC(), 4," ") //
            + StringUtils.leftPad(this.getNOTICE_TYPE(), 1," ") //
            + StringUtils.leftPad(this.getNOTICE_NUMBER(), 3," ") //
            + StringUtils.leftPad(this.getNOTICE_CUSID(), 10," ") //
            + StringUtils.leftPad(this.getNOTICE_MOBILENO(), 10," ") //
            + StringUtils.leftPad(this.getNOTICE_EMAIL(), 50," ") //
            + StringUtils.leftPad(this.getSEND_FISC2160(), 1," ") //
            + StringUtils.leftPad(this.getO_TXN_TYPE_CODE(), 2," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNAMT(), 11, false, 2, true) //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getTXNCHARGE(), 4, false, 0, false) //
            + StringUtils.leftPad(this.getO_FROM_BANK(), 3," ") //
            + StringUtils.leftPad(this.getO_FROM_ACT(), 16," ") //
            + StringUtils.leftPad(this.getO_TO_BANK(), 3," ") //
            + StringUtils.leftPad(this.getO_TO_ACT(), 16," ") //
            + CodeGenUtil.bigDecimalToAsciiCBS(this.getACT_BALANCE(), 11, false, 2, true) //
            + StringUtils.leftPad(this.getLUCKYNO(), 16," ") //
            + StringUtils.leftPad(this.getO_FILLER_TR(), 12," ") //
            + StringUtils.leftPad(this.getAVAILABLE_BALANCE(), 14," ") //
            + StringUtils.leftPad(this.getO_Redeem_State(), 1," ") //
            + StringUtils.leftPad(this.getO_Add_Point(), 10," ") //
            + StringUtils.leftPad(this.getO_Deduct_Point(), 10," ") //
            + StringUtils.leftPad(this.getO_Extra_amt_type(), 1," ") //
            + StringUtils.leftPad(this.getO_Extra_Amount(), 10," ") //
            + StringUtils.leftPad(this.getO_Extra_Curr_code(), 3," ") //
            + StringUtils.leftPad(this.getO_Final_TX_amt(), 13," ") //
            + StringUtils.leftPad(this.getO_Final_TX_Curr_code(), 3," ") //
            + StringUtils.leftPad(this.getO_BIT46_POT_RFU(), 49," ") //
            + StringUtils.leftPad(this.getDRVS(), 211," ") //
;
}
}
