package com.syscom.fep.vo.text.unisys.mo.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.unisys.mo.MOGeneral;
import com.syscom.fep.vo.text.unisys.mo.MOUnisysTextBase;

public class MOK00001Response extends MOUnisysTextBase {
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

    //ID
    @Field(length = 42, optional = true)
    private String _UNINO = "";

    //電話1
    @Field(length = 40, optional = true)
    private String _TELNO1 = "";

    //姓名
    @Field(length = 84, optional = true)
    private String _NAME = "";

    //電話2
    @Field(length = 40, optional = true)
    private String _TELNO2 = "";

    //帳號
    @Field(length = 28, optional = true)
    private String _ACTNO1 = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD1 = "";

    //帳戶餘額
    @Field(length = 28, optional = true)
    private String _PRIBAL1 = "";

    //HWBAL1
    @Field(length = 26, optional = true)
    private String _HWBAL1 = "";

    //可用餘額
    @Field(length = 26, optional = true)
    private String _AMT1 = "";

    //STPBAL1
    @Field(length = 26, optional = true)
    private String _STPBAL1 = "";

    //帳號
    @Field(length = 28, optional = true)
    private String _ACTNO2 = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD2 = "";

    //帳戶餘額
    @Field(length = 28, optional = true)
    private String _PRIBAL2 = "";

    //HWBAL2
    @Field(length = 26, optional = true)
    private String _HWBAL2 = "";

    //可用餘額
    @Field(length = 26, optional = true)
    private String _AMT2 = "";

    //STPBAL2
    @Field(length = 26, optional = true)
    private String _STPBAL2 = "";

    //帳號
    @Field(length = 28, optional = true)
    private String _ACTNO3 = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD3 = "";

    //帳戶餘額
    @Field(length = 28, optional = true)
    private String _PRIBAL3 = "";

    //HWBAL3
    @Field(length = 26, optional = true)
    private String _HWBAL3 = "";

    //可用餘額
    @Field(length = 26, optional = true)
    private String _AMT3 = "";

    //STPBAL3
    @Field(length = 26, optional = true)
    private String _STPBAL3 = "";

    //帳號
    @Field(length = 28, optional = true)
    private String _ACTNO4 = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD4 = "";

    //帳戶餘額
    @Field(length = 28, optional = true)
    private String _PRIBAL4 = "";

    //HWBAL4
    @Field(length = 26, optional = true)
    private String _HWBAL4 = "";

    //可用餘額
    @Field(length = 26, optional = true)
    private String _AMT4 = "";

    //STPBAL4
    @Field(length = 26, optional = true)
    private String _STPBAL4 = "";

    //帳號
    @Field(length = 28, optional = true)
    private String _ACTNO5 = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD5 = "";

    //帳戶餘額
    @Field(length = 28, optional = true)
    private String _PRIBAL5 = "";

    //HWBAL5
    @Field(length = 26, optional = true)
    private String _HWBAL5 = "";

    //可用餘額
    @Field(length = 26, optional = true)
    private String _AMT5 = "";

    //STPBAL5
    @Field(length = 26, optional = true)
    private String _STPBAL5 = "";

    //帳號
    @Field(length = 28, optional = true)
    private String _ACTNO6 = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD6 = "";

    //帳戶餘額
    @Field(length = 28, optional = true)
    private String _PRIBAL6 = "";

    //HWBAL6
    @Field(length = 26, optional = true)
    private String _HWBAL6 = "";

    //可用餘額
    @Field(length = 26, optional = true)
    private String _AMT6 = "";

    //STPBAL6
    @Field(length = 26, optional = true)
    private String _STPBAL6 = "";

    //
    @Field(length = 170, optional = true)
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
     ID

     <remark></remark>
     */
    public String getUNINO() {
        return _UNINO;
    }
    public void setUNINO(String value) {
        _UNINO = value;
    }

    /**
     電話1

     <remark></remark>
     */
    public String getTELNO1() {
        return _TELNO1;
    }
    public void setTELNO1(String value) {
        _TELNO1 = value;
    }

    /**
     姓名

     <remark></remark>
     */
    public String getNAME() {
        return _NAME;
    }
    public void setNAME(String value) {
        _NAME = value;
    }

    /**
     電話2

     <remark></remark>
     */
    public String getTELNO2() {
        return _TELNO2;
    }
    public void setTELNO2(String value) {
        _TELNO2 = value;
    }

    /**
     帳號

     <remark></remark>
     */
    public String getACTNO1() {
        return _ACTNO1;
    }
    public void setACTNO1(String value) {
        _ACTNO1 = value;
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
     帳戶餘額

     <remark></remark>
     */
    public String getPRIBAL1() {
        return _PRIBAL1;
    }
    public void setPRIBAL1(String value) {
        _PRIBAL1 = value;
    }

    /**
     HWBAL1

     <remark></remark>
     */
    public String getHWBAL1() {
        return _HWBAL1;
    }
    public void setHWBAL1(String value) {
        _HWBAL1 = value;
    }

    /**
     可用餘額

     <remark></remark>
     */
    public String getAMT1() {
        return _AMT1;
    }
    public void setAMT1(String value) {
        _AMT1 = value;
    }

    /**
     STPBAL1

     <remark></remark>
     */
    public String getSTPBAL1() {
        return _STPBAL1;
    }
    public void setSTPBAL1(String value) {
        _STPBAL1 = value;
    }

    /**
     帳號

     <remark></remark>
     */
    public String getACTNO2() {
        return _ACTNO2;
    }
    public void setACTNO2(String value) {
        _ACTNO2 = value;
    }

    /**
     幣別

     <remark></remark>
     */
    public String getCURCD2() {
        return _CURCD2;
    }
    public void setCURCD2(String value) {
        _CURCD2 = value;
    }

    /**
     帳戶餘額

     <remark></remark>
     */
    public String getPRIBAL2() {
        return _PRIBAL2;
    }
    public void setPRIBAL2(String value) {
        _PRIBAL2 = value;
    }

    /**
     HWBAL2

     <remark></remark>
     */
    public String getHWBAL2() {
        return _HWBAL2;
    }
    public void setHWBAL2(String value) {
        _HWBAL2 = value;
    }

    /**
     可用餘額

     <remark></remark>
     */
    public String getAMT2() {
        return _AMT2;
    }
    public void setAMT2(String value) {
        _AMT2 = value;
    }

    /**
     STPBAL2

     <remark></remark>
     */
    public String getSTPBAL2() {
        return _STPBAL2;
    }
    public void setSTPBAL2(String value) {
        _STPBAL2 = value;
    }

    /**
     帳號

     <remark></remark>
     */
    public String getACTNO3() {
        return _ACTNO3;
    }
    public void setACTNO3(String value) {
        _ACTNO3 = value;
    }

    /**
     幣別

     <remark></remark>
     */
    public String getCURCD3() {
        return _CURCD3;
    }
    public void setCURCD3(String value) {
        _CURCD3 = value;
    }

    /**
     帳戶餘額

     <remark></remark>
     */
    public String getPRIBAL3() {
        return _PRIBAL3;
    }
    public void setPRIBAL3(String value) {
        _PRIBAL3 = value;
    }

    /**
     HWBAL3

     <remark></remark>
     */
    public String getHWBAL3() {
        return _HWBAL3;
    }
    public void setHWBAL3(String value) {
        _HWBAL3 = value;
    }

    /**
     可用餘額

     <remark></remark>
     */
    public String getAMT3() {
        return _AMT3;
    }
    public void setAMT3(String value) {
        _AMT3 = value;
    }

    /**
     STPBAL3

     <remark></remark>
     */
    public String getSTPBAL3() {
        return _STPBAL3;
    }
    public void setSTPBAL3(String value) {
        _STPBAL3 = value;
    }

    /**
     帳號

     <remark></remark>
     */
    public String getACTNO4() {
        return _ACTNO4;
    }
    public void setACTNO4(String value) {
        _ACTNO4 = value;
    }

    /**
     幣別

     <remark></remark>
     */
    public String getCURCD4() {
        return _CURCD4;
    }
    public void setCURCD4(String value) {
        _CURCD4 = value;
    }

    /**
     帳戶餘額

     <remark></remark>
     */
    public String getPRIBAL4() {
        return _PRIBAL4;
    }
    public void setPRIBAL4(String value) {
        _PRIBAL4 = value;
    }

    /**
     HWBAL4

     <remark></remark>
     */
    public String getHWBAL4() {
        return _HWBAL4;
    }
    public void setHWBAL4(String value) {
        _HWBAL4 = value;
    }

    /**
     可用餘額

     <remark></remark>
     */
    public String getAMT4() {
        return _AMT4;
    }
    public void setAMT4(String value) {
        _AMT4 = value;
    }

    /**
     STPBAL4

     <remark></remark>
     */
    public String getSTPBAL4() {
        return _STPBAL4;
    }
    public void setSTPBAL4(String value) {
        _STPBAL4 = value;
    }

    /**
     帳號

     <remark></remark>
     */
    public String getACTNO5() {
        return _ACTNO5;
    }
    public void setACTNO5(String value) {
        _ACTNO5 = value;
    }

    /**
     幣別

     <remark></remark>
     */
    public String getCURCD5() {
        return _CURCD5;
    }
    public void setCURCD5(String value) {
        _CURCD5 = value;
    }

    /**
     帳戶餘額

     <remark></remark>
     */
    public String getPRIBAL5() {
        return _PRIBAL5;
    }
    public void setPRIBAL5(String value) {
        _PRIBAL5 = value;
    }

    /**
     HWBAL5

     <remark></remark>
     */
    public String getHWBAL5() {
        return _HWBAL5;
    }
    public void setHWBAL5(String value) {
        _HWBAL5 = value;
    }

    /**
     可用餘額

     <remark></remark>
     */
    public String getAMT5() {
        return _AMT5;
    }
    public void setAMT5(String value) {
        _AMT5 = value;
    }

    /**
     STPBAL5

     <remark></remark>
     */
    public String getSTPBAL5() {
        return _STPBAL5;
    }
    public void setSTPBAL5(String value) {
        _STPBAL5 = value;
    }

    /**
     帳號

     <remark></remark>
     */
    public String getACTNO6() {
        return _ACTNO6;
    }
    public void setACTNO6(String value) {
        _ACTNO6 = value;
    }

    /**
     幣別

     <remark></remark>
     */
    public String getCURCD6() {
        return _CURCD6;
    }
    public void setCURCD6(String value) {
        _CURCD6 = value;
    }

    /**
     帳戶餘額

     <remark></remark>
     */
    public String getPRIBAL6() {
        return _PRIBAL6;
    }
    public void setPRIBAL6(String value) {
        _PRIBAL6 = value;
    }

    /**
     HWBAL6

     <remark></remark>
     */
    public String getHWBAL6() {
        return _HWBAL6;
    }
    public void setHWBAL6(String value) {
        _HWBAL6 = value;
    }

    /**
     可用餘額

     <remark></remark>
     */
    public String getAMT6() {
        return _AMT6;
    }
    public void setAMT6(String value) {
        _AMT6 = value;
    }

    /**
     STPBAL6

     <remark></remark>
     */
    public String getSTPBAL6() {
        return _STPBAL6;
    }
    public void setSTPBAL6(String value) {
        _STPBAL6 = value;
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
