package com.syscom.fep.vo.text.unisys.mo.response;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.unisys.mo.MOGeneral;
import com.syscom.fep.vo.text.unisys.mo.MOUnisysTextBase;


public class MOErrResponse extends MOUnisysTextBase {
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

    //MSGID
    @Field(length = 8)
    private String _MSGID = "";

    //訊息長度
    @Field(length = 6)

    private String _MSGLNG = "";

    //餘額
    @Field(length = 28, optional = true)
    private String _BAL = "";

    //
    @Field(length = 1176, optional = true)
    private String _FILLER1 = "";

    private static final int _TotalLength = 624;

    /**
     輸入行機台編號

     <remark></remark>
     */
    public String getTRMNO()
    {
        return _TRMNO;
    }
    public void setTRMNO(String value)
    {
        _TRMNO = value;
    }

    /**
     交易傳輸編號

     <remark></remark>
     */
    public String getTXTNO()
    {
        return _TXTNO;
    }
    public void setTXTNO(String value)
    {
        _TXTNO = value;
    }

    /**
     端末TASK ID

     <remark></remark>
     */
    public String getTTSKID()
    {
        return _TTSKID;
    }
    public void setTTSKID(String value)
    {
        _TTSKID = value;
    }

    /**
     櫃台機種類

     <remark>2</remark>
     */
    public String getTRMTYP()
    {
        return _TRMTYP;
    }
    public void setTRMTYP(String value)
    {
        _TRMTYP = value;
    }

    /**
     TXTSK

     <remark></remark>
     */
    public String getTXTSK()
    {
        return _TXTSK;
    }
    public void setTXTSK(String value)
    {
        _TXTSK = value;
    }

    /**
     是否還有TOTA

     <remark>1</remark>
     */
    public String getMSGEND()
    {
        return _MSGEND;
    }
    public void setMSGEND(String value)
    {
        _MSGEND = value;
    }

    /**
     MSGID

     <remark></remark>
     */
    public String getMSGID()
    {
        return _MSGID;
    }
    public void setMSGID(String value)
    {
        _MSGID = value;
    }

    /**
     訊息長度

     <remark></remark>
     */
    public String getMSGLNG()
    {
        return _MSGLNG;
    }
    public void setMSGLNG(String value)
    {
        _MSGLNG = value;
    }

    /**
     餘額

     <remark></remark>
     */
    public String getBAL()
    {
        return _BAL;
    }
    public void setBAL(String value)
    {
        _BAL = value;
    }

    /**


     <remark></remark>
     */
    public String getFILLER1()
    {
        return _FILLER1;
    }
    public void setFILLER1(String value)
    {
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
