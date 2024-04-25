package com.syscom.fep.vo.text.ims;

import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;

public class IB_LE_O002 extends IMSTextBase {
    @Field(length = 8)
        private String IMS_TRANS = StringUtils.EMPTY;

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

    @Field(length = 500)
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
        public String getDRVS(){
        return this.DRVS;
        }
        public void setDRVS(String DRVS){
        this.DRVS = DRVS;
        }

public void parseCbsTele(String tita) throws ParseException{
        this.setIMS_TRANS(EbcdicConverter.fromHex(CCSID.English,tita.substring(0, 16)));
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
        this.setDRVS(EbcdicConverter.fromHex(CCSID.English,tita.substring(200, 1200)));
}

public String makeMessage() {
return "" //
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_TRANS(), 8) //
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
            + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDRVS(), 500) //
;
}

public String makeMessageAscii() {
return "" //
            + StringUtils.leftPad(this.getIMS_TRANS(), 8," ") //
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
            + StringUtils.leftPad(this.getDRVS(), 500," ") //
;
}
}
