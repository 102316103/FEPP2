package com.syscom.fep.vo.text.ims;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.vo.CodeGenUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;

public class CB_IQTX_O001 extends IMSTextBase {
    @Field(length = 24)
    private String OUTMSGID = StringUtils.EMPTY;

    @Field(length = 1)
    private String OUTMSGF = StringUtils.EMPTY;

    @Field(length = 10)
    private String OUTSTAN = StringUtils.EMPTY;

    @Field(length = 8)
    private String OUTDATE = StringUtils.EMPTY;

    @Field(length = 6)
    private String OUTTIME = StringUtils.EMPTY;

    @Field(length = 4)
    private String OUTSERV = StringUtils.EMPTY;

    @Field(length = 8)
    private String OUTTD = StringUtils.EMPTY;

    @Field(length = 5)
    private String OUTAP = StringUtils.EMPTY;

    @Field(length = 10)
    private String OUTID = StringUtils.EMPTY;

    @Field(length = 1)
    private String OUTFF = StringUtils.EMPTY;

    @Field(length = 3)
    private String OUTPGNO = StringUtils.EMPTY;

    @Field(length = 4)
    private String OUTFIX = StringUtils.EMPTY;

    @Field(length = 4)
    private String OUTV1CT = StringUtils.EMPTY;

    @Field(length = 4)
    private String OUTRTC = StringUtils.EMPTY;

    @Field(length = 25)
    private String OUTRSV = StringUtils.EMPTY;

    @Field(length = 3)
    private String ACQ_BID = StringUtils.EMPTY;

    @Field(length = 7)
    private String TXSTAN = StringUtils.EMPTY;

    @Field(length = 7)
    private String TXDATE = StringUtils.EMPTY;

    @Field(length = 7)
    private String FG_TXDATE = StringUtils.EMPTY;

    @Field(length = 4)
    private String PCODE = StringUtils.EMPTY;

    @Field(length = 2)
    private String FSCODE = StringUtils.EMPTY;

    @Field(length = 1)
    private String TX_ACCT_FLAG = StringUtils.EMPTY;

    @Field(length = 1)
    private String TX_RVS_FLAG = StringUtils.EMPTY;

    @Field(length = 4)
    private String IMSRC4_FISC = StringUtils.EMPTY;

    @Field(length = 3)
    private String IMS_RC3_TCB = StringUtils.EMPTY;

    @Field(length = 161)
    private String FILLER = StringUtils.EMPTY;

    public String getOUTMSGID() {
        return this.OUTMSGID;
    }

    public void setOUTMSGID(String OUTMSGID) {
        this.OUTMSGID = OUTMSGID;
    }

    public String getOUTMSGF() {
        return this.OUTMSGF;
    }

    public void setOUTMSGF(String OUTMSGF) {
        this.OUTMSGF = OUTMSGF;
    }

    public String getOUTSTAN() {
        return this.OUTSTAN;
    }

    public void setOUTSTAN(String OUTSTAN) {
        this.OUTSTAN = OUTSTAN;
    }

    public String getOUTDATE() {
        return this.OUTDATE;
    }

    public void setOUTDATE(String OUTDATE) {
        this.OUTDATE = OUTDATE;
    }

    public String getOUTTIME() {
        return this.OUTTIME;
    }

    public void setOUTTIME(String OUTTIME) {
        this.OUTTIME = OUTTIME;
    }

    public String getOUTSERV() {
        return this.OUTSERV;
    }

    public void setOUTSERV(String OUTSERV) {
        this.OUTSERV = OUTSERV;
    }

    public String getOUTTD() {
        return this.OUTTD;
    }

    public void setOUTTD(String OUTTD) {
        this.OUTTD = OUTTD;
    }

    public String getOUTAP() {
        return this.OUTAP;
    }

    public void setOUTAP(String OUTAP) {
        this.OUTAP = OUTAP;
    }

    public String getOUTID() {
        return this.OUTID;
    }

    public void setOUTID(String OUTID) {
        this.OUTID = OUTID;
    }

    public String getOUTFF() {
        return this.OUTFF;
    }

    public void setOUTFF(String OUTFF) {
        this.OUTFF = OUTFF;
    }

    public String getOUTPGNO() {
        return this.OUTPGNO;
    }

    public void setOUTPGNO(String OUTPGNO) {
        this.OUTPGNO = OUTPGNO;
    }

    public String getOUTFIX() {
        return this.OUTFIX;
    }

    public void setOUTFIX(String OUTFIX) {
        this.OUTFIX = OUTFIX;
    }

    public String getOUTV1CT() {
        return this.OUTV1CT;
    }

    public void setOUTV1CT(String OUTV1CT) {
        this.OUTV1CT = OUTV1CT;
    }

    public String getOUTRTC() {
        return this.OUTRTC;
    }

    public void setOUTRTC(String OUTRTC) {
        this.OUTRTC = OUTRTC;
    }

    public String getOUTRSV() {
        return this.OUTRSV;
    }

    public void setOUTRSV(String OUTRSV) {
        this.OUTRSV = OUTRSV;
    }

    public String getACQ_BID() {
        return this.ACQ_BID;
    }

    public void setACQ_BID(String ACQ_BID) {
        this.ACQ_BID = ACQ_BID;
    }

    public String getTXSTAN() {
        return this.TXSTAN;
    }

    public void setTXSTAN(String TXSTAN) {
        this.TXSTAN = TXSTAN;
    }

    public String getTXDATE() {
        return this.TXDATE;
    }

    public void setTXDATE(String TXDATE) {
        this.TXDATE = TXDATE;
    }

    public String getFG_TXDATE() {
        return this.FG_TXDATE;
    }

    public void setFG_TXDATE(String FG_TXDATE) {
        this.FG_TXDATE = FG_TXDATE;
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

    public String getTX_ACCT_FLAG() {
        return this.TX_ACCT_FLAG;
    }

    public void setTX_ACCT_FLAG(String TX_ACCT_FLAG) {
        this.TX_ACCT_FLAG = TX_ACCT_FLAG;
    }

    public String getTX_RVS_FLAG() {
        return this.TX_RVS_FLAG;
    }

    public void setTX_RVS_FLAG(String TX_RVS_FLAG) {
        this.TX_RVS_FLAG = TX_RVS_FLAG;
    }

    public String getIMSRC4_FISC() {
        return this.IMSRC4_FISC;
    }

    public void setIMSRC4_FISC(String IMSRC4_FISC) {
        this.IMSRC4_FISC = IMSRC4_FISC;
    }

    public String getIMS_RC3_TCB() {
        return this.IMS_RC3_TCB;
    }

    public void setIMS_RC3_TCB(String IMS_RC3_TCB) {
        this.IMS_RC3_TCB = IMS_RC3_TCB;
    }

    public String getFILLER() {
        return this.FILLER;
    }

    public void setFILLER(String FILLER) {
        this.FILLER = FILLER;
    }


    public void parseCbsTele(String tita) throws ParseException {
        this.setOUTMSGID(EbcdicConverter.fromHex(CCSID.English, tita.substring(0, 48)));
        this.setOUTMSGF(EbcdicConverter.fromHex(CCSID.English, tita.substring(48, 50)));
        this.setOUTSTAN(EbcdicConverter.fromHex(CCSID.English, tita.substring(50, 70)));
        this.setOUTDATE(EbcdicConverter.fromHex(CCSID.English, tita.substring(70, 86)));
        this.setOUTTIME(EbcdicConverter.fromHex(CCSID.English, tita.substring(86, 98)));
        this.setOUTSERV(EbcdicConverter.fromHex(CCSID.English, tita.substring(98, 106)));
        this.setOUTTD(EbcdicConverter.fromHex(CCSID.English, tita.substring(106, 122)));
        this.setOUTAP(EbcdicConverter.fromHex(CCSID.English, tita.substring(122, 132)));
        this.setOUTID(EbcdicConverter.fromHex(CCSID.English, tita.substring(132, 152)));
        this.setOUTFF(EbcdicConverter.fromHex(CCSID.English, tita.substring(152, 154)));
        this.setOUTPGNO(EbcdicConverter.fromHex(CCSID.English, tita.substring(154, 160)));
        this.setOUTFIX(EbcdicConverter.fromHex(CCSID.English, tita.substring(160, 168)));
        this.setOUTV1CT(EbcdicConverter.fromHex(CCSID.English, tita.substring(168, 176)));
        this.setOUTRTC(EbcdicConverter.fromHex(CCSID.English, tita.substring(176, 184)));
        this.setOUTRSV(EbcdicConverter.fromHex(CCSID.English, tita.substring(184, 234)));
        this.setACQ_BID(EbcdicConverter.fromHex(CCSID.English, tita.substring(234, 240)));
        this.setTXSTAN(EbcdicConverter.fromHex(CCSID.English, tita.substring(240, 254)));
        this.setTXDATE(EbcdicConverter.fromHex(CCSID.English, tita.substring(254, 268)));
        this.setFG_TXDATE(EbcdicConverter.fromHex(CCSID.English, tita.substring(268, 282)));
        this.setPCODE(EbcdicConverter.fromHex(CCSID.English, tita.substring(282, 290)));
        this.setFSCODE(EbcdicConverter.fromHex(CCSID.English, tita.substring(290, 294)));
        this.setTX_ACCT_FLAG(EbcdicConverter.fromHex(CCSID.English, tita.substring(294, 296)));
        this.setTX_RVS_FLAG(EbcdicConverter.fromHex(CCSID.English, tita.substring(296, 298)));
        this.setIMSRC4_FISC(EbcdicConverter.fromHex(CCSID.English, tita.substring(298, 306)));
        this.setIMS_RC3_TCB(EbcdicConverter.fromHex(CCSID.English, tita.substring(306, 312)));
        this.setFILLER(EbcdicConverter.fromHex(CCSID.English, tita.substring(312, 634)));
    }

    public String makeMessage() {
        return "" //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTMSGID(), 24) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTMSGF(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTSTAN(), 10) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTDATE(), 8) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTTIME(), 6) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTSERV(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTTD(), 8) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTAP(), 5) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTID(), 10) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTFF(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTPGNO(), 3) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTFIX(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTV1CT(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTRTC(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getOUTRSV(), 25) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACQ_BID(), 3) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXSTAN(), 7) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTXDATE(), 7) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFG_TXDATE(), 7) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPCODE(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(), 2) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTX_ACCT_FLAG(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTX_RVS_FLAG(), 1) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMSRC4_FISC(), 4) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getIMS_RC3_TCB(), 3) //
                + CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFILLER(), 161) //
                ;
    }

    public String makeMessageAscii() {
        return "" //
                + StringUtils.leftPad(this.getOUTMSGID(), 24, " ") //
                + StringUtils.leftPad(this.getOUTMSGF(), 1, " ") //
                + StringUtils.leftPad(this.getOUTSTAN(), 10, " ") //
                + StringUtils.leftPad(this.getOUTDATE(), 8, " ") //
                + StringUtils.leftPad(this.getOUTTIME(), 6, " ") //
                + StringUtils.leftPad(this.getOUTSERV(), 4, " ") //
                + StringUtils.leftPad(this.getOUTTD(), 8, " ") //
                + StringUtils.leftPad(this.getOUTAP(), 5, " ") //
                + StringUtils.leftPad(this.getOUTID(), 10, " ") //
                + StringUtils.leftPad(this.getOUTFF(), 1, " ") //
                + StringUtils.leftPad(this.getOUTPGNO(), 3, " ") //
                + StringUtils.leftPad(this.getOUTFIX(), 4, " ") //
                + StringUtils.leftPad(this.getOUTV1CT(), 4, " ") //
                + StringUtils.leftPad(this.getOUTRTC(), 4, " ") //
                + StringUtils.leftPad(this.getOUTRSV(), 25, " ") //
                + StringUtils.leftPad(this.getACQ_BID(), 3, " ") //
                + StringUtils.leftPad(this.getTXSTAN(), 7, " ") //
                + StringUtils.leftPad(this.getTXDATE(), 7, " ") //
                + StringUtils.leftPad(this.getFG_TXDATE(), 7, " ") //
                + StringUtils.leftPad(this.getPCODE(), 4, " ") //
                + StringUtils.leftPad(this.getFSCODE(), 2, " ") //
                + StringUtils.leftPad(this.getTX_ACCT_FLAG(), 1, " ") //
                + StringUtils.leftPad(this.getTX_RVS_FLAG(), 1, " ") //
                + StringUtils.leftPad(this.getIMSRC4_FISC(), 4, " ") //
                + StringUtils.leftPad(this.getIMS_RC3_TCB(), 3, " ") //
                + StringUtils.leftPad(this.getFILLER(), 161, " ") //
                ;
    }
}
