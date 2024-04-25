package com.syscom.fep.vo.text.unisys.hk.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.unisys.hk.HKGeneral;
import com.syscom.fep.vo.text.unisys.hk.HKGeneralRequest;
import com.syscom.fep.vo.text.unisys.hk.HKUnisysTextBase;
import org.apache.commons.lang3.StringUtils;

public class HKK00001Request extends HKUnisysTextBase {
    //ATM代號
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

    //交易代碼
    @Field(length = 6)
    private String _TXCD = "";

    //處理型態
    @Field(length = 2)
    private String _PTYPE = "";

    //摘要
    @Field(length = 32)
    private String _DSCPT1 = "";

    //更正記號
    @Field(length = 2)
    private String _HCODE = "";

    //補送記號
    @Field(length = 2)
    private String _YCODE = "";

    //
    @Field(length = 28)
    private String _ACTNO = "";

    //帳務別
    @Field(length = 4)
    private String _TXTYPE = "";

    //借貸別
    @Field(length = 2)
    private String _CRDB = "";

    //主管記號
    @Field(length = 4)
    private String _SPCD = "";

    //無摺記號
    @Field(length = 2)
    private String _NBCD = "";

    //櫃員編號
    @Field(length = 4)
    private String _TLRNO = "";

    //訓練／代登記號
    @Field(length = 2)
    private String _TRNMOD = "";

    //幣別
    @Field(length = 4)
    private String _CURCD = "";

    //交易金額
    @Field(length = 26)
    private String _TXAMT = "";

    //更正時原交易輸入行
    @Field(length = 6)
    private String _FXABRN = "";

    //更正時原交易櫃台機號
    @Field(length = 4)
    private String _FXWSNO = "";

    //更正時原交易傳輸編號
    @Field(length = 10)
    private String _FXTXTNO = "";

    //更正時原交易註記
    @Field(length = 4)
    private String _FXFLAG = "";

    //主機帳務記號
    @Field(length = 2, optional = true)
    private String _MODE = "";

    //
    @Field(length = 1372, optional = true)
    private String _FILLER1 = "";

    private static final int _TotalLength = 758;

    /**
     ATM代號

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
     交易代碼

     <remark></remark>
     */
    public String getTXCD() {
        return _TXCD;
    }
    public void setTXCD(String value) {
        _TXCD = value;
    }

    /**
     處理型態

     <remark>0</remark>
     */
    public String getPTYPE() {
        return _PTYPE;
    }
    public void setPTYPE(String value) {
        _PTYPE = value;
    }

    /**
     摘要

     <remark></remark>
     */
    public String getDSCPT1() {
        return _DSCPT1;
    }
    public void setDSCPT1(String value) {
        _DSCPT1 = value;
    }

    /**
     更正記號

     <remark></remark>
     */
    public String getHCODE() {
        return _HCODE;
    }
    public void setHCODE(String value) {
        _HCODE = value;
    }

    /**
     補送記號

     <remark></remark>
     */
    public String getYCODE() {
        return _YCODE;
    }
    public void setYCODE(String value) {
        _YCODE = value;
    }

    /**


     <remark></remark>
     */
    public String getACTNO() {
        return _ACTNO;
    }
    public void setACTNO(String value) {
        _ACTNO = value;
    }

    /**
     帳務別

     <remark></remark>
     */
    public String getTXTYPE() {
        return _TXTYPE;
    }
    public void setTXTYPE(String value) {
        _TXTYPE = value;
    }

    /**
     借貸別

     <remark>0</remark>
     */
    public String getCRDB() {
        return _CRDB;
    }
    public void setCRDB(String value) {
        _CRDB = value;
    }

    /**
     主管記號

     <remark>0</remark>
     */
    public String getSPCD() {
        return _SPCD;
    }
    public void setSPCD(String value) {
        _SPCD = value;
    }

    /**
     無摺記號

     <remark>1</remark>
     */
    public String getNBCD() {
        return _NBCD;
    }
    public void setNBCD(String value) {
        _NBCD = value;
    }

    /**
     櫃員編號

     <remark>97</remark>
     */
    public String getTLRNO() {
        return _TLRNO;
    }
    public void setTLRNO(String value) {
        _TLRNO = value;
    }

    /**
     訓練／代登記號

     <remark>0</remark>
     */
    public String getTRNMOD() {
        return _TRNMOD;
    }
    public void setTRNMOD(String value) {
        _TRNMOD = value;
    }

    /**
     幣別

     <remark>3</remark>
     */
    public String getCURCD() {
        return _CURCD;
    }
    public void setCURCD(String value) {
        _CURCD = value;
    }

    /**
     交易金額

     <remark>0</remark>
     */
    public String getTXAMT() {
        return _TXAMT;
    }
    public void setTXAMT(String value) {
        _TXAMT = value;
    }

    /**
     更正時原交易輸入行

     <remark>0</remark>
     */
    public String getFXABRN() {
        return _FXABRN;
    }
    public void setFXABRN(String value) {
        _FXABRN = value;
    }

    /**
     更正時原交易櫃台機號

     <remark>0</remark>
     */
    public String getFXWSNO() {
        return _FXWSNO;
    }
    public void setFXWSNO(String value) {
        _FXWSNO = value;
    }

    /**
     更正時原交易傳輸編號

     <remark>0</remark>
     */
    public String getFXTXTNO() {
        return _FXTXTNO;
    }
    public void setFXTXTNO(String value) {
        _FXTXTNO = value;
    }

    /**
     更正時原交易註記

     <remark>0</remark>
     */
    public String getFXFLAG() {
        return _FXFLAG;
    }
    public void setFXFLAG(String value) {
        _FXFLAG = value;
    }

    /**
     主機帳務記號

     <remark></remark>
     */
    public String getMODE() {
        return _MODE;
    }
    public void setMODE(String value) {
        _MODE = value;
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
    public HKGeneral parseFlatfile(String flatfile) throws Exception {
        return null;
    }

    @Override
    public String makeMessageFromGeneral(HKGeneral general) throws Exception {
        HKGeneralRequest tempVar = general.getmRequest();
        _TRMNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getTRMNO(),5,'0')); //ATM代號
        _TXTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getTXTNO(),5,'0')); //交易傳輸編號
        _TTSKID = StringUtil.toHex(StringUtils.rightPad(tempVar.getTTSKID(),2,' ')); //端末TASK ID
        _TRMTYP = StringUtil.toHex(StringUtils.rightPad(tempVar.getTRMTYP(),1,' ')); //櫃台機種類
        _TXCD = StringUtil.toHex(StringUtils.rightPad(tempVar.getTXCD(),3,' ')); //交易代碼
        _PTYPE = StringUtil.toHex(StringUtils.rightPad(tempVar.getPTYPE(),1,' ')); //處理型態
        _DSCPT1 = StringUtil.toHex(StringUtils.leftPad(tempVar.getDSCPT1(),3,'0')); //摘要
        _HCODE = StringUtil.toHex(StringUtils.rightPad(tempVar.getHCODE(),1,' ')); //更正記號
        _YCODE = StringUtil.toHex(StringUtils.rightPad(tempVar.getYCODE(),1,' ')); //補送記號
        _ACTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getACTNO(),14,'0'));
        _TXTYPE = StringUtil.toHex(StringUtils.rightPad(tempVar.getTXTYPE(),2,' ')); //帳務別
        _CRDB = StringUtil.toHex(StringUtils.rightPad(tempVar.getCRDB(),1,' ')); //借貸別
        _SPCD = StringUtil.toHex(StringUtils.leftPad(tempVar.getSPCD(),2,'0')); //主管記號
        _NBCD = StringUtil.toHex(StringUtils.rightPad(tempVar.getNBCD(),1,' ')); //無摺記號
        _TLRNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getTLRNO(),2,'0')); //櫃員編號
        _TRNMOD = StringUtil.toHex(StringUtils.rightPad(tempVar.getTRNMOD(),1,' ')); //訓練／代登記號
        _CURCD = StringUtil.toHex(StringUtils.leftPad(tempVar.getCURCD(),2,'0')); //幣別
        _TXAMT = this.toHex(tempVar.getTXAMT(), 13, 2); //交易金額
        _FXABRN = StringUtil.toHex(StringUtils.leftPad(tempVar.getFXABRN(),3,'0')); //更正時原交易輸入行
        _FXWSNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getFXWSNO(),2,'0')); //更正時原交易櫃台機號
        _FXTXTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getFXTXTNO(),5,'0')); //更正時原交易傳輸編號
        _FXFLAG = StringUtil.toHex(StringUtils.leftPad(tempVar.getFXFLAG(),2,'0')); //更正時原交易註記
        _MODE = StringUtil.toHex(StringUtils.leftPad(tempVar.getMODE(),1,'0')); //主機帳務記號
        _FILLER1 = StringUtil.toHex(StringUtils.leftPad("", 685, ' '));

        return _TRMNO + _TXTNO + _TTSKID + _TRMTYP + _TXCD + _PTYPE + _DSCPT1 + _HCODE + _YCODE + _ACTNO + _TXTYPE + _CRDB + _SPCD + _NBCD + _TLRNO + _TRNMOD + _CURCD + _TXAMT + _FXABRN + _FXWSNO + _FXTXTNO + _FXFLAG + _MODE + _FILLER1;

    }

    @Override
    public void toGeneral(HKGeneral general) throws Exception {

    }

    @Override
    public int getTotalLength() {
        return _TotalLength;
    }
}
