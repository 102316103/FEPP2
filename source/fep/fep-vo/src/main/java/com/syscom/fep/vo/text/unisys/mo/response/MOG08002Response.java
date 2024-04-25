package com.syscom.fep.vo.text.unisys.mo.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.unisys.mo.MOGeneral;
import com.syscom.fep.vo.text.unisys.mo.MOUnisysTextBase;

public class MOG08002Response extends MOUnisysTextBase {
    //輸入行機台編號
    @Field(length = 10)
    private String _TRMNO = "";

    //交易傳輸編號
    @Field(length = 10)
    private String _TXTNO = "";

    //端末TASK ID
    @Field(length = 4)
    private String _TTSKID = "";

    //櫃台機種類
    @Field(length = 2)
    private String _TRMTYP = "";

    //TXTSK
    @Field(length = 2)
    private String _TXTSK = "";

    //是否還有TOTA
    @Field(length = 2)
    private String _MSGEND = "";

    //訊息編號
    @Field(length = 8)
    private String _MSGID = "";

    //訊息長度
    @Field(length = 6)
    private String _MSGLNG = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD1 = "";

    //CCSRATE
    @Field(length = 20, optional = true)
    private String _CCSRATE1 = "";

    //現鈔賣出匯率
    @Field(length = 20, optional = true)
    private String _CSRATE1 = "";

    //終價匯率
    @Field(length = 20, optional = true)
    private String _ACRAT1 = "";

    //EQUS
    @Field(length = 20, optional = true)
    private String _EQUS1 = "";

    //MDFLG
    @Field(length = 2, optional = true)
    private String _MDFLG1 = "";

    //DISRAT
    @Field(length = 20, optional = true)
    private String _DISRAT1 = "";

    //掛牌賣出匯率
    @Field(length = 20, optional = true)
    private String _SCASH1 = "";

    //幣別2
    @Field(length = 4, optional = true)
    private String _CURCD2 = "";

    //CCSRATE2
    @Field(length = 20, optional = true)
    private String _CCSRATE2 = "";

    //現鈔賣出匯率
    @Field(length = 20, optional = true)
    private String _CSRATE2 = "";

    //終價匯率2
    @Field(length = 20, optional = true)
    private String _ACRAT2 = "";

    //EQUS2
    @Field(length = 20, optional = true)
    private String _EQUS2 = "";

    //MDFLG2
    @Field(length = 2, optional = true)
    private String _MDFLG2 = "";

    //DISRAT2
    @Field(length = 20, optional = true)
    private String _DISRAT2 = "";

    //掛牌賣出匯率
    @Field(length = 20, optional = true)
    private String _SCASH2 = "";

    //幣別3
    @Field(length = 4, optional = true)
    private String _CURCD3 = "";

    //CCSRATE3
    @Field(length = 20, optional = true)
    private String _CCSRATE3 = "";

    //現鈔賣出匯率
    @Field(length = 20, optional = true)
    private String _CSRATE3 = "";

    //終價匯率3
    @Field(length = 20, optional = true)
    private String _ACRAT3 = "";

    //EQUS3
    @Field(length = 20, optional = true)
    private String _EQUS3 = "";

    //MDFLG3
    @Field(length = 2, optional = true)
    private String _MDFLG3 = "";

    //DISRAT3
    @Field(length = 20, optional = true)
    private String _DISRAT3 = "";

    //掛牌賣出匯率
    @Field(length = 20, optional = true)
    private String _SCASH3 = "";

    //TCODE
    @Field(length = 2, optional = true)
    private String _TCODE = "";

    //
    @Field(length = 824, optional = true)
    private String _FILLER1 = "";

    private static final int _TotalLength = 624;

    /**
     輸入行機台編號

     <remark></remark>
     */
    public String getTRMNO() {
        return _TRMNO;
    }
    public void setTRMNO(String value) {
        _TRMNO = value;
    }

    /**
     交易傳輸編號

     <remark></remark>
     */
    public String getTXTNO() {
        return _TXTNO;
    }
    public void setTXTNO(String value) {
        _TXTNO = value;
    }

    /**
     端末TASK ID

     <remark></remark>
     */
    public String getTTSKID() {
        return _TTSKID;
    }
    public void setTTSKID(String value) {
        _TTSKID = value;
    }

    /**
     櫃台機種類

     <remark>2</remark>
     */
    public String getTRMTYP() {
        return _TRMTYP;
    }
    public void setTRMTYP(String value) {
        _TRMTYP = value;
    }

    /**
     TXTSK

     <remark></remark>
     */
    public String getTXTSK() {
        return _TXTSK;
    }
    public void setTXTSK(String value) {
        _TXTSK = value;
    }

    /**
     是否還有TOTA

     <remark>1</remark>
     */
    public String getMSGEND() {
        return _MSGEND;
    }
    public void setMSGEND(String value) {
        _MSGEND = value;
    }

    /**
     訊息編號

     <remark></remark>
     */
    public String getMSGID() {
        return _MSGID;
    }
    public void setMSGID(String value) {
        _MSGID = value;
    }

    /**
     訊息長度

     <remark></remark>
     */
    public String getMSGLNG() {
        return _MSGLNG;
    }
    public void setMSGLNG(String value) {
        _MSGLNG = value;
    }

    /**
     幣別

     <remark></remark>
     */
    public String getCURCD1() {
        return _CURCD1;
    }
    public void setCURCD1(String value) {
        _CURCD1 = value;
    }

    /**
     CCSRATE

     <remark></remark>
     */
    public String getCCSRATE1() {
        return _CCSRATE1;
    }
    public void setCCSRATE1(String value) {
        _CCSRATE1 = value;
    }

    /**
     現鈔賣出匯率

     <remark></remark>
     */
    public String getCSRATE1() {
        return _CSRATE1;
    }
    public void setCSRATE1(String value) {
        _CSRATE1 = value;
    }

    /**
     終價匯率

     <remark></remark>
     */
    public String getACRAT1() {
        return _ACRAT1;
    }
    public void setACRAT1(String value) {
        _ACRAT1 = value;
    }

    /**
     EQUS

     <remark></remark>
     */
    public String getEQUS1() {
        return _EQUS1;
    }
    public void setEQUS1(String value) {
        _EQUS1 = value;
    }

    /**
     MDFLG

     <remark></remark>
     */
    public String getMDFLG1() {
        return _MDFLG1;
    }
    public void setMDFLG1(String value) {
        _MDFLG1 = value;
    }

    /**
     DISRAT

     <remark></remark>
     */
    public String getDISRAT1() {
        return _DISRAT1;
    }
    public void setDISRAT1(String value) {
        _DISRAT1 = value;
    }

    /**
     掛牌賣出匯率

     <remark></remark>
     */
    public String getSCASH1() {
        return _SCASH1;
    }
    public void setSCASH1(String value) {
        _SCASH1 = value;
    }

    /**
     幣別2

     <remark></remark>
     */
    public String getCURCD2() {
        return _CURCD2;
    }
    public void setCURCD2(String value) {
        _CURCD2 = value;
    }

    /**
     CCSRATE2

     <remark></remark>
     */
    public String getCCSRATE2() {
        return _CCSRATE2;
    }
    public void setCCSRATE2(String value) {
        _CCSRATE2 = value;
    }

    /**
     現鈔賣出匯率

     <remark></remark>
     */
    public String getCSRATE2() {
        return _CSRATE2;
    }
    public void setCSRATE2(String value) {
        _CSRATE2 = value;
    }

    /**
     終價匯率2

     <remark></remark>
     */
    public String getACRAT2() {
        return _ACRAT2;
    }
    public void setACRAT2(String value) {
        _ACRAT2 = value;
    }

    /**
     EQUS2

     <remark></remark>
     */
    public String getEQUS2() {
        return _EQUS2;
    }
    public void setEQUS2(String value) {
        _EQUS2 = value;
    }

    /**
     MDFLG2

     <remark></remark>
     */
    public String getMDFLG2() {
        return _MDFLG2;
    }
    public void setMDFLG2(String value) {
        _MDFLG2 = value;
    }

    /**
     DISRAT2

     <remark></remark>
     */
    public String getDISRAT2() {
        return _DISRAT2;
    }
    public void setDISRAT2(String value) {
        _DISRAT2 = value;
    }

    /**
     掛牌賣出匯率

     <remark></remark>
     */
    public String getSCASH2() {
        return _SCASH2;
    }
    public void setSCASH2(String value) {
        _SCASH2 = value;
    }

    /**
     幣別3

     <remark></remark>
     */
    public String getCURCD3() {
        return _CURCD3;
    }
    public void setCURCD3(String value) {
        _CURCD3 = value;
    }

    /**
     CCSRATE3

     <remark></remark>
     */
    public String getCCSRATE3() {
        return _CCSRATE3;
    }
    public void setCCSRATE3(String value) {
        _CCSRATE3 = value;
    }

    /**
     現鈔賣出匯率

     <remark></remark>
     */
    public String getCSRATE3() {
        return _CSRATE3;
    }
    public void setCSRATE3(String value) {
        _CSRATE3 = value;
    }

    /**
     終價匯率3

     <remark></remark>
     */
    public String getACRAT3() {
        return _ACRAT3;
    }
    public void setACRAT3(String value) {
        _ACRAT3 = value;
    }

    /**
     EQUS3

     <remark></remark>
     */
    public String getEQUS3() {
        return _EQUS3;
    }
    public void setEQUS3(String value) {
        _EQUS3 = value;
    }

    /**
     MDFLG3

     <remark></remark>
     */
    public String getMDFLG3() {
        return _MDFLG3;
    }
    public void setMDFLG3(String value) {
        _MDFLG3 = value;
    }

    /**
     DISRAT3

     <remark></remark>
     */
    public String getDISRAT3() {
        return _DISRAT3;
    }
    public void setDISRAT3(String value) {
        _DISRAT3 = value;
    }

    /**
     掛牌賣出匯率

     <remark></remark>
     */
    public String getSCASH3() {
        return _SCASH3;
    }
    public void setSCASH3(String value) {
        _SCASH3 = value;
    }

    /**
     TCODE

     <remark></remark>
     */
    public String getTCODE() {
        return _TCODE;
    }
    public void setTCODE(String value) {
        _TCODE = value;
    }

    /**


     <remark></remark>
     */
    public String getFILLER1() {
        return _FILLER1;
    }
    public void setFILLER1(String value) {
        _FILLER1 = value;
    }



    @Override
    public MOGeneral parseFlatfile(String flatfile) throws Exception {
        return this.parseFlatfile(this.getClass(), flatfile);
    }

    @Override
    public String makeMessageFromGeneral(MOGeneral general) throws Exception {
        return null;
    }

    @Override
    public void toGeneral(MOGeneral general) throws Exception {

    }

    @Override
    public int getTotalLength() {
        return _TotalLength;
    }

}
