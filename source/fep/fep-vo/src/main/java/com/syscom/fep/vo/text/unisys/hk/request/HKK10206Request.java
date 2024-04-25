package com.syscom.fep.vo.text.unisys.hk.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.unisys.hk.HKGeneral;
import com.syscom.fep.vo.text.unisys.hk.HKGeneralRequest;
import com.syscom.fep.vo.text.unisys.hk.HKUnisysTextBase;
import org.apache.commons.lang3.StringUtils;

public class HKK10206Request extends HKUnisysTextBase {
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

    //CKSRT
    @Field(length = 2, optional = true)
    private String _CKSRT = "";

    //票號
    @Field(length = 20, optional = true)
    private String _CKNO = "";

    //轉出帳號
    @Field(length = 28, optional = true)
    private String _CTACTNO = "";

    //序號
    @Field(length = 10, optional = true)
    private String _SEQNO = "";

    //營業日
    @Field(length = 16, optional = true)
    private String _BDTXD = "";

    //金額
    @Field(length = 26, optional = true)
    private String _FPBAMT = "";

    //轉入帳號
    @Field(length = 28, optional = true)
    private String _FACTNO = "";

    //標記
    @Field(length = 2, optional = true)
    private String _FFG = "";

    //原幣其他應付款
    @Field(length = 26, optional = true)
    private String _FOAMT1 = "";

    //基礎幣兌換金額
    @Field(length = 26, optional = true)
    private String _BCAMT = "";

    //應付基礎幣金額
    @Field(length = 26, optional = true)
    private String _BXAMT = "";

    //基礎幣承作匯率
    @Field(length = 20, optional = true)
    private String _BORATE = "";

    //基礎幣成本匯率
    @Field(length = 20, optional = true)
    private String _BCRATE = "";

    //現金
    @Field(length = 26, optional = true)
    private String _CASH = "";

    //BKCUR
    @Field(length = 4, optional = true)
    private String _BKCUR = "";

    //BKSRNO
    @Field(length = 6, optional = true)
    private String _BKSRNO = "";

    //金額
    @Field(length = 26, optional = true)
    private String _PBAMT = "";

    //轉入帳號
    @Field(length = 28, optional = true)
    private String _PACTNO = "";

    //標記
    @Field(length = 2, optional = true)
    private String _PFG = "";

    //金額
    @Field(length = 26, optional = true)
    private String _BOAMT1 = "";

    //幣別
    @Field(length = 1372, optional = true)
    private String _OCURCD = "";

    //金額
    @Field(length = 26, optional = true)
    private String _OCAMT = "";

    //OXAMT
    @Field(length = 26, optional = true)
    private String _OXAMT = "";

    //ATM吐鈔幣別
    @Field(length = 4, optional = true)
    private String _ATMCUR = "";

    //ATM吐鈔金額
    @Field(length = 26, optional = true)
    private String _ATMAMT = "";

    //帳務行基礎幣
    @Field(length = 4, optional = true)
    private String _BASCUR = "";

    //基礎幣扣帳手續費
    @Field(length = 8, optional = true)
    private String _A_BCUSPAY = "";

    //港幣對葡幣的比值
    @Field(length = 20, optional = true)
    private String _A_BCUSPRAT = "";

    //
    @Field(length = 4, optional = true)
    private String _FILLER1 = "";

    //OACTNO
    @Field(length = 28, optional = true)
    private String _OACTNO = "";

    //OFG
    @Field(length = 2, optional = true)
    private String _OFG = "";

    //OOAMT1
    @Field(length = 26, optional = true)
    private String _OOAMT1 = "";

    //原始交易資料
    @Field(length = 6, optional = true)
    private String _OTXCD = "";

    //ODSCPT
    @Field(length = 6, optional = true)
    private String _ODSCPT = "";

    //RBRNO
    @Field(length = 6, optional = true)
    private String _RBRNO = "";

    //STANBKNO
    @Field(length = 6, optional = true)
    private String _STANBKNO = "";

    //STANSSN
    @Field(length = 14, optional = true)
    private String _STANSSN = "";

    //SREMARK
    @Field(length = 12, optional = true)
    private String _SREMARK = "";

    //取款時當地幣別
    @Field(length = 6, optional = true)
    private String _A_SEACUR = "";

    //取款金額
    @Field(length = 1372, optional = true)
    private String _A_SEAAMT = "";

    //業務別
    @Field(length = 4, optional = true)
    private String _SECNO = "";

    //ID
    @Field(length = 42, optional = true)
    private String _UNINO = "";


    //ATM應收手續費
    @Field(length = 8, optional = true)
    private String _HCAMT = "";

    //ATM客戶負擔手續費
    @Field(length = 8, optional = true)
    private String _CUSPAY = "";

    //JETCO營業日
    @Field(length = 16, optional = true)
    private String _JTBSDY = "";

    //隔日交易
    @Field(length = 2, optional = true)
    private String _TPNFG = "";

    //MERRCD-PYM種類
    @Field(length = 8, optional = true)
    private String _MERRCD = "";

    //MODE
    @Field(length = 2, optional = true)
    private String _MODE = "";

    //TRANS KEY
    @Field(length = 16, optional = true)
    private String _TRDATE = "";

    //輸入行
    @Field(length = 6, optional = true)
    private String _TRBRNO = "";

    //櫃台機代號
    @Field(length = 4, optional = true)
    private String _TRWSNO = "";

    //交易傳輸編號
    @Field(length = 10, optional = true)
    private String _TRTXTNO = "";

    //ATMID
    @Field(length = 16, optional = true)
    private String _ATMID = "";

    //PYM應收手續費
    @Field(length = 10, optional = true)
    private String _PYMHC = "";

    //ADDITIONAL CHARGE(非HK地)
    @Field(length = 2, optional = true)
    private String _ADDCHGE = "";

    //ATM客戶減免手續費
    @Field(length = 8, optional = true)
    private String _CUSFREE = "";

    //ATM對方帳號所屬銀行
    @Field(length = 8, optional = true)
    private String _SECBCD = "";

    //ATM清算分行
    @Field(length = 6, optional = true)
    private String _LOCBRNO = "";

    //PLUS跨區提款清算幣別
    @Field(length = 4, optional = true)
    private String _A_PLUSCUR = "";

    //PLUS跨區提款清算金額
    @Field(length = 26, optional = true)
    private String _A_PLUSAMT = "";

    //
    @Field(length = 538, optional = true)
    private String _FILLER2 = "";

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
     CKSRT

     <remark></remark>
     */
    public String getCKSRT() {
        return _CKSRT;
    }
    public void setCKSRT(String value) {
        _CKSRT = value;
    }

    /**
     票號

     <remark></remark>
     */
    public String getCKNO() {
        return _CKNO;
    }
    public void setCKNO(String value) {
        _CKNO = value;
    }

    /**
     轉出帳號

     <remark></remark>
     */
    public String getCTACTNO() {
        return _CTACTNO;
    }
    public void setCTACTNO(String value) {
        _CTACTNO = value;
    }

    /**
     序號

     <remark></remark>
     */
    public String getSEQNO() {
        return _SEQNO;
    }
    public void setSEQNO(String value) {
        _SEQNO = value;
    }

    /**
     營業日

     <remark></remark>
     */
    public String getBDTXD() {
        return _BDTXD;
    }
    public void setBDTXD(String value) {
        _BDTXD = value;
    }

    /**
     金額

     <remark></remark>
     */
    public String getFPBAMT() {
        return _FPBAMT;
    }
    public void setFPBAMT(String value) {
        _FPBAMT = value;
    }

    /**
     轉入帳號

     <remark></remark>
     */
    public String getFACTNO() {
        return _FACTNO;
    }
    public void setFACTNO(String value) {
        _FACTNO = value;
    }

    /**
     標記

     <remark></remark>
     */
    public String getFFG() {
        return _FFG;
    }
    public void setFFG(String value) {
        _FFG = value;
    }

    /**
     原幣其他應付款

     <remark></remark>
     */
    public String getFOAMT1() {
        return _FOAMT1;
    }
    public void setFOAMT1(String value) {
        _FOAMT1 = value;
    }

    /**
     基礎幣兌換金額

     <remark></remark>
     */
    public String getBCAMT() {
        return _BCAMT;
    }
    public void setBCAMT(String value) {
        _BCAMT = value;
    }

    /**
     應付基礎幣金額

     <remark></remark>
     */
    public String getBXAMT() {
        return _BXAMT;
    }
    public void setBXAMT(String value) {
        _BXAMT = value;
    }

    /**
     基礎幣承作匯率

     <remark></remark>
     */
    public String getBORATE() {
        return _BORATE;
    }
    public void setBORATE(String value) {
        _BORATE = value;
    }

    /**
     基礎幣成本匯率

     <remark></remark>
     */
    public String getBCRATE() {
        return _BCRATE;
    }
    public void setBCRATE(String value) {
        _BCRATE = value;
    }

    /**
     現金

     <remark></remark>
     */
    public String getCASH() {
        return _CASH;
    }
    public void setCASH(String value) {
        _CASH = value;
    }

    /**
     BKCUR

     <remark></remark>
     */
    public String getBKCUR() {
        return _BKCUR;
    }
    public void setBKCUR(String value) {
        _BKCUR = value;
    }

    /**
     BKSRNO

     <remark></remark>
     */
    public String getBKSRNO() {
        return _BKSRNO;
    }
    public void setBKSRNO(String value) {
        _BKSRNO = value;
    }

    /**
     金額

     <remark></remark>
     */
    public String getPBAMT() {
        return _PBAMT;
    }
    public void setPBAMT(String value) {
        _PBAMT = value;
    }

    /**
     轉入帳號

     <remark></remark>
     */
    public String getPACTNO() {
        return _PACTNO;
    }
    public void setPACTNO(String value) {
        _PACTNO = value;
    }

    /**
     標記

     <remark></remark>
     */
    public String getPFG() {
        return _PFG;
    }
    public void setPFG(String value) {
        _PFG = value;
    }

    /**
     金額

     <remark></remark>
     */
    public String getBOAMT1() {
        return _BOAMT1;
    }
    public void setBOAMT1(String value) {
        _BOAMT1 = value;
    }

    /**
     幣別

     <remark></remark>
     */
    public String getOCURCD() {
        return _OCURCD;
    }
    public void setOCURCD(String value) {
        _OCURCD = value;
    }

    /**
     金額

     <remark></remark>
     */
    public String getOCAMT() {
        return _OCAMT;
    }
    public void setOCAMT(String value) {
        _OCAMT = value;
    }

    /**
     OXAMT

     <remark></remark>
     */
    public String getOXAMT() {
        return _OXAMT;
    }
    public void setOXAMT(String value) {
        _OXAMT = value;
    }

    /**
     ATM吐鈔幣別

     <remark></remark>
     */
    public final String getATMCUR()
    {
        return _ATMCUR;
    }
    public final void setATMCUR(String value)
    {
        _ATMCUR = value;
    }

    /**
     ATM吐鈔金額

     <remark></remark>
     */
    public final String getATMAMT()
    {
        return _ATMAMT;
    }
    public final void setATMAMT(String value)
    {
        _ATMAMT = value;
    }

    /**
     帳務行基礎幣

     <remark></remark>
     */
    public final String getBASCUR()
    {
        return _BASCUR;
    }
    public final void setBASCUR(String value)
    {
        _BASCUR = value;
    }

    /**
     基礎幣扣帳手續費

     <remark></remark>
     */
    public String getABcuspay() {
        return _A_BCUSPAY;
    }
    public void setABcuspay(String value) {
        _A_BCUSPAY = value;
    }

    /**
     港幣對葡幣的比值

     <remark></remark>
     */
    public String getABcusprat() {
        return _A_BCUSPRAT;
    }
    public void setABcusprat(String value) {
        _A_BCUSPRAT = value;
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


    /**
     OACTNO

     <remark></remark>
     */
    public String getOACTNO() {
        return _OACTNO;
    }
    public void setOACTNO(String value) {
        _OACTNO = value;
    }

    /**
     OFG

     <remark></remark>
     */
    public String getOFG() {
        return _OFG;
    }
    public void setOFG(String value) {
        _OFG = value;
    }

    /**
     OOAMT1

     <remark></remark>
     */
    public String getOOAMT1() {
        return _OOAMT1;
    }
    public void setOOAMT1(String value) {
        _OOAMT1 = value;
    }

    /**
     原始交易資料

     <remark></remark>
     */
    public String getOTXCD() {
        return _OTXCD;
    }
    public void setOTXCD(String value) {
        _OTXCD = value;
    }

    /**
     ODSCPT

     <remark></remark>
     */
    public String getODSCPT() {
        return _ODSCPT;
    }
    public void setODSCPT(String value) {
        _ODSCPT = value;
    }

    /**
     RBRNO

     <remark></remark>
     */
    public String getRBRNO() {
        return _RBRNO;
    }
    public void setRBRNO(String value) {
        _RBRNO = value;
    }

    /**
     STANBKNO

     <remark></remark>
     */
    public String getSTANBKNO() {
        return _STANBKNO;
    }
    public void setSTANBKNO(String value) {
        _STANBKNO = value;
    }

    /**
     STANSSN

     <remark></remark>
     */
    public String getSTANSSN() {
        return _STANSSN;
    }
    public void setSTANSSN(String value) {
        _STANSSN = value;
    }

    /**
     SREMARK

     <remark></remark>
     */
    public String getSREMARK() {
        return _SREMARK;
    }
    public void setSREMARK(String value) {
        _SREMARK = value;
    }

    /**
     取款時當地幣別

     <remark></remark>
     */
    public String getASeacur() {
        return _A_SEACUR;
    }
    public void setASeacur(String value) {
        _A_SEACUR = value;
    }

    /**
     取款金額

     <remark></remark>
     */
    public String getASeaamt() {
        return _A_SEAAMT;
    }
    public void setASeaamt(String value) {
        _A_SEAAMT = value;
    }

    /**
     業務別

     <remark></remark>
     */
    public String getSECNO() {
        return _SECNO;
    }
    public void setSECNO(String value) {
        _SECNO = value;
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
     ATM應收手續費

     <remark></remark>
     */
    public String getHCAMT() {
        return _HCAMT;
    }
    public void setHCAMT(String value) {
        _HCAMT = value;
    }

    /**
     ATM客戶負擔手續費

     <remark></remark>
     */
    public String getCUSPAY() {
        return _CUSPAY;
    }
    public void setCUSPAY(String value) {
        _CUSPAY = value;
    }

    /**
     JETCO營業日

     <remark></remark>
     */
    public String getJTBSDY() {
        return _JTBSDY;
    }
    public void setJTBSDY(String value) {
        _JTBSDY = value;
    }

    /**
     隔日交易

     <remark></remark>
     */
    public String getTPNFG() {
        return _TPNFG;
    }
    public void setTPNFG(String value) {
        _TPNFG = value;
    }

    /**
     MERRCD-PYM種類

     <remark></remark>
     */
    public String getMERRCD() {
        return _MERRCD;
    }
    public void setMERRCD(String value) {
        _MERRCD = value;
    }

    /**
     MODE

     <remark></remark>
     */
    public String getMODE() {
        return _MODE;
    }
    public void setMODE(String value) {
        _MODE = value;
    }

    /**
     TRANS KEY

     <remark></remark>
     */
    public String getTRDATE() {
        return _TRDATE;
    }
    public void setTRDATE(String value) {
        _TRDATE = value;
    }

    /**
     輸入行

     <remark></remark>
     */
    public String getTRBRNO() {
        return _TRBRNO;
    }
    public void setTRBRNO(String value) {
        _TRBRNO = value;
    }

    /**
     櫃台機代號

     <remark></remark>
     */
    public String getTRWSNO() {
        return _TRWSNO;
    }
    public void setTRWSNO(String value) {
        _TRWSNO = value;
    }

    /**
     交易傳輸編號

     <remark></remark>
     */
    public String getTRTXTNO() {
        return _TRTXTNO;
    }
    public void setTRTXTNO(String value) {
        _TRTXTNO = value;
    }

    /**
     ATMID

     <remark></remark>
     */
    public String getATMID() {
        return _ATMID;
    }
    public void setATMID(String value) {
        _ATMID = value;
    }

    /**
     PYM應收手續費

     <remark></remark>
     */
    public String getPYMHC() {
        return _PYMHC;
    }
    public void setPYMHC(String value) {
        _PYMHC = value;
    }

    /**
     ADDITIONAL CHARGE(非HK地)

     <remark></remark>
     */
    public String getADDCHGE() {
        return _ADDCHGE;
    }
    public void setADDCHGE(String value) {
        _ADDCHGE = value;
    }

    /**
     ATM客戶減免手續費

     <remark></remark>
     */
    public String getCUSFREE() {
        return _CUSFREE;
    }
    public void setCUSFREE(String value) {
        _CUSFREE = value;
    }

    /**
     ATM對方帳號所屬銀行

     <remark></remark>
     */
    public String getSECBCD() {
        return _SECBCD;
    }
    public void setSECBCD(String value) {
        _SECBCD = value;
    }

    /**
     ATM清算分行

     <remark></remark>
     */
    public String getLOCBRNO() {
        return _LOCBRNO;
    }
    public void setLOCBRNO(String value) {
        _LOCBRNO = value;
    }

    /**
     PLUS跨區提款清算幣別

     <remark></remark>
     */
    public String getAPluscur() {
        return _A_PLUSCUR;
    }
    public void setAPluscur(String value) {
        _A_PLUSCUR = value;
    }

    /**
     PLUS跨區提款清算金額

     <remark></remark>
     */
    public String getAPlusamt() {
        return _A_PLUSAMT;
    }
    public void setAPlusamt(String value) {
        _A_PLUSAMT = value;
    }

    /**


     <remark></remark>
     */
    public String getFILLER2() {
        return _FILLER2;
    }
    public void setFILLER2(String value) {
        _FILLER2 = value;
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
        _CKSRT = StringUtil.toHex(StringUtils.leftPad(tempVar.getCKSRT(),1,'0')); //CKSRT
        _CKNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getCKNO(),10,'0')); //票號
        _CTACTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getCTACTNO(),14,'0')); //轉出帳號
        _SEQNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getSEQNO(),5,'0')); //序號
        _BDTXD = StringUtil.toHex(StringUtils.leftPad(tempVar.getBDTXD(),8,'0')); //營業日
        _FPBAMT = this.toHex(tempVar.getFPBAMT(), 13, 2); //金額
        _FACTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getFACTNO(),14,'0')); //轉入帳號
        _FFG = StringUtil.toHex(StringUtils.leftPad(tempVar.getFFG(),1,'0')); //標記
        _FOAMT1 = this.toHex(tempVar.getFOAMT1(), 13, 2); //原幣其他應付款
        _BCAMT = this.toHex(tempVar.getBCAMT(), 13, 2); //基礎幣兌換金額
        _BXAMT = this.toHex(tempVar.getBXAMT(), 13, 2); //應付基礎幣金額
        _BORATE = this.toHex(tempVar.getBORATE(), 10, 5); //基礎幣承作匯率
        _BCRATE = this.toHex(tempVar.getBCRATE(), 10, 5); //基礎幣成本匯率
        _CASH = this.toHex(tempVar.getCASH(), 13, 2); //現金
        _BKCUR = StringUtil.toHex(StringUtils.leftPad(tempVar.getBKCUR(),2,'0')); //BKCUR
        _BKSRNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getBKSRNO(),3,'0')); //BKSRNO
        _PBAMT = this.toHex(tempVar.getPBAMT(), 13, 2); //金額
        _PACTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getPACTNO(),14,'0')); //轉入帳號
        _PFG = StringUtil.toHex(StringUtils.leftPad(tempVar.getPFG(),1,'0')); //標記
        _BOAMT1 = this.toHex(tempVar.getBOAMT1(), 13, 2); //金額
        _OCURCD = StringUtil.toHex(StringUtils.leftPad(tempVar.getOCURCD(),2,'0')); //幣別
        _OCAMT = this.toHex(tempVar.getOCAMT(), 13, 2); //金額
        _OXAMT = this.toHex(tempVar.getOXAMT(), 13, 2); //OXAMT
        _ATMCUR = StringUtil.toHex(StringUtils.leftPad(tempVar.getATMCUR(),2,'0')); //ATM吐鈔幣別
        _ATMAMT = this.toHex(tempVar.getATMAMT(), 13, 2); //ATM吐鈔金額
        _BASCUR = StringUtil.toHex(StringUtils.leftPad(tempVar.getBASCUR(),2,'0')); //帳務行基礎幣
        _A_BCUSPAY = this.toHex(tempVar.getABcuspay(), 4, 2); //基礎幣扣帳手續費
        _A_BCUSPRAT = this.toHex(tempVar.getABcusprat(), 10, 5); //港幣對葡幣的比值
        _FILLER1 = StringUtil.toHex(StringUtils.leftPad("", 2, ' '));
        _OACTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getOACTNO(),14,'0')); //OACTNO
        _OFG = StringUtil.toHex(StringUtils.leftPad(tempVar.getOFG(),1,'0')); //OFG
        _OOAMT1 = this.toHex(tempVar.getOOAMT1(), 13, 2); //OOAMT1
        _OTXCD = StringUtil.toHex(StringUtils.rightPad(tempVar.getOTXCD(),3,' ')); //原始交易資料
        _ODSCPT = StringUtil.toHex(StringUtils.leftPad(tempVar.getODSCPT(),3,'0')); //ODSCPT
        _RBRNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getRBRNO(),3,'0')); //RBRNO
        _STANBKNO = StringUtil.toHex(StringUtils.rightPad(tempVar.getSTANBKNO(),3,' ')); //STANBKNO
        _STANSSN = StringUtil.toHex(StringUtils.rightPad(tempVar.getSTANSSN(),7,' ')); //STANSSN
        _SREMARK = StringUtil.toHex(StringUtils.rightPad(tempVar.getSREMARK(),6,' ')); //SREMARK
        _A_SEACUR = StringUtil.toHex(StringUtils.rightPad(tempVar.getASeacur(),3,' ')); //取款時當地幣別
        _A_SEAAMT = this.toHex(tempVar.getASeaamt(), 13, 0); //取款金額
        _SECNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getSECNO(),2,'0')); //業務別
        _UNINO = StringUtil.toHex(StringUtils.rightPad(tempVar.getUNINO(),21,' ')); //ID
        _HCAMT = this.toHex(tempVar.getHCAMT(), 4, 2); //ATM應收手續費
        _CUSPAY = this.toHex(tempVar.getCUSPAY(), 4, 2); //ATM客戶負擔手續費
        _JTBSDY = StringUtil.toHex(StringUtils.leftPad(tempVar.getJTBSDY(),8,'0')); //JETCO營業日
        _TPNFG = StringUtil.toHex(StringUtils.leftPad(tempVar.getTPNFG(),1,'0')); //隔日交易
        _MERRCD = StringUtil.toHex(StringUtils.rightPad(tempVar.getMERRCD(),4,' ')); //MERRCD-PYM種類
        _MODE = StringUtil.toHex(StringUtils.leftPad(tempVar.getMODE(),1,'0')); //MODE
        _TRDATE = StringUtil.toHex(StringUtils.leftPad(tempVar.getTRDATE(),8,'0')); //TRANS KEY
        _TRBRNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getTRBRNO(),3,'0')); //輸入行
        _TRWSNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getTRWSNO(),2,'0')); //櫃台機代號
        _TRTXTNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getTRTXTNO(),5,'0')); //交易傳輸編號
        _ATMID = StringUtil.toHex(StringUtils.leftPad(tempVar.getATMID(),8,'0')); //ATMID
        _PYMHC = this.toHex(tempVar.getPYMHC(), 5, 3); //PYM應收手續費
        _ADDCHGE = StringUtil.toHex(StringUtils.leftPad(tempVar.getADDCHGE(),1,'0')); //ADDITIONAL CHARGE(非HK地)
        _CUSFREE = this.toHex(tempVar.getCUSFREE(), 4, 2); //ATM客戶減免手續費
        _SECBCD = StringUtil.toHex(StringUtils.rightPad(tempVar.getSECBCD(),4,' ')); //ATM對方帳號所屬銀行
        _LOCBRNO = StringUtil.toHex(StringUtils.leftPad(tempVar.getLOCBRNO(),3,'0')); //ATM清算分行
        _A_PLUSCUR = StringUtil.toHex(StringUtils.leftPad(tempVar.getAPluscur(),2,'0')); //PLUS跨區提款清算幣別
        _A_PLUSAMT = this.toHex(tempVar.getAPlusamt(), 13, 2); //PLUS跨區提款清算金額
        _FILLER2 = StringUtil.toHex(StringUtils.leftPad("", 269, ' '));

        return _TRMNO + _TXTNO + _TTSKID + _TRMTYP + _TXCD + _PTYPE + _DSCPT1 + _HCODE + _YCODE + _ACTNO + _TXTYPE + _CRDB + _SPCD + _NBCD + _TLRNO + _TRNMOD + _CURCD + _TXAMT + _FXABRN + _FXWSNO + _FXTXTNO + _FXFLAG + _CKSRT + _CKNO + _CTACTNO + _SEQNO + _BDTXD + _FPBAMT + _FACTNO + _FFG + _FOAMT1 + _BCAMT + _BXAMT + _BORATE + _BCRATE + _CASH + _BKCUR + _BKSRNO + _PBAMT + _PACTNO + _PFG + _BOAMT1 + _OCURCD + _OCAMT + _OXAMT + _ATMCUR + _ATMAMT + _BASCUR + _A_BCUSPAY + _A_BCUSPRAT + _FILLER1 + _OACTNO + _OFG + _OOAMT1 + _OTXCD + _ODSCPT + _RBRNO + _STANBKNO + _STANSSN + _SREMARK + _A_SEACUR + _A_SEAAMT + _SECNO + _UNINO + _HCAMT + _CUSPAY + _JTBSDY + _TPNFG + _MERRCD + _MODE + _TRDATE + _TRBRNO + _TRWSNO + _TRTXTNO + _ATMID + _PYMHC + _ADDCHGE + _CUSFREE + _SECBCD + _LOCBRNO + _A_PLUSCUR + _A_PLUSAMT + _FILLER2;
    }



    @Override
    public void toGeneral(HKGeneral general) throws Exception {

    }

    @Override
    public int getTotalLength() {
        return _TotalLength;
    }


}
