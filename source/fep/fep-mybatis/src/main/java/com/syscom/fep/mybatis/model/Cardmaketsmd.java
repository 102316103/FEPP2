package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Cardmaketsmd extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.TXDATE
     *
     * @mbg.generated
     */
    private String txdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.BATCHSEQ
     *
     * @mbg.generated
     */
    private Integer batchseq;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.SEQNO
     *
     * @mbg.generated
     */
    private Integer seqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.ACTNOCARDNO
     *
     * @mbg.generated
     */
    private String actnocardno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.SEID
     *
     * @mbg.generated
     */
    private String seid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.PAYMENTSCHEME
     *
     * @mbg.generated
     */
    private String paymentscheme;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.MSISDN
     *
     * @mbg.generated
     */
    private String msisdn;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.SETYPE
     *
     * @mbg.generated
     */
    private String setype;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.MNOID
     *
     * @mbg.generated
     */
    private String mnoid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.ACTIVATIONCODE
     *
     * @mbg.generated
     */
    private String activationcode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.C2PIN
     *
     * @mbg.generated
     */
    private String c2pin;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.CONTACTLESSCARDHOLDERAUTHFLAG
     *
     * @mbg.generated
     */
    private String contactlesscardholderauthflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.IDNO
     *
     * @mbg.generated
     */
    private String idno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.CARDISSUSER
     *
     * @mbg.generated
     */
    private String cardissuser;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.ICREMARK1001
     *
     * @mbg.generated
     */
    private String icremark1001;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.ICREMARK1003
     *
     * @mbg.generated
     */
    private String icremark1003;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.DISPLAYACTNOCARDNO
     *
     * @mbg.generated
     */
    private String displayactnocardno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.STOREVALUEPURSEID
     *
     * @mbg.generated
     */
    private String storevaluepurseid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.CHIPID
     *
     * @mbg.generated
     */
    private String chipid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.RESPONSECODE
     *
     * @mbg.generated
     */
    private String responsecode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.SIR
     *
     * @mbg.generated
     */
    private String sir;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.DIVERSIFYDATA
     *
     * @mbg.generated
     */
    private String diversifydata;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMD.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    public Cardmaketsmd(String txdate, Integer batchseq, Integer seqno, String actnocardno, String seid, String paymentscheme, String msisdn, String setype, String mnoid, String activationcode, String c2pin, String contactlesscardholderauthflag, String idno, String cardissuser, String icremark1001, String icremark1003, String displayactnocardno, String storevaluepurseid, String chipid, String responsecode, String sir, String diversifydata, Integer updateUserid, Date updateTime) {
        this.txdate = txdate;
        this.batchseq = batchseq;
        this.seqno = seqno;
        this.actnocardno = actnocardno;
        this.seid = seid;
        this.paymentscheme = paymentscheme;
        this.msisdn = msisdn;
        this.setype = setype;
        this.mnoid = mnoid;
        this.activationcode = activationcode;
        this.c2pin = c2pin;
        this.contactlesscardholderauthflag = contactlesscardholderauthflag;
        this.idno = idno;
        this.cardissuser = cardissuser;
        this.icremark1001 = icremark1001;
        this.icremark1003 = icremark1003;
        this.displayactnocardno = displayactnocardno;
        this.storevaluepurseid = storevaluepurseid;
        this.chipid = chipid;
        this.responsecode = responsecode;
        this.sir = sir;
        this.diversifydata = diversifydata;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    public Cardmaketsmd() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.TXDATE
     *
     * @return the value of CARDMAKETSMD.TXDATE
     *
     * @mbg.generated
     */
    public String getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.TXDATE
     *
     * @param txdate the value for CARDMAKETSMD.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.BATCHSEQ
     *
     * @return the value of CARDMAKETSMD.BATCHSEQ
     *
     * @mbg.generated
     */
    public Integer getBatchseq() {
        return batchseq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.BATCHSEQ
     *
     * @param batchseq the value for CARDMAKETSMD.BATCHSEQ
     *
     * @mbg.generated
     */
    public void setBatchseq(Integer batchseq) {
        this.batchseq = batchseq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.SEQNO
     *
     * @return the value of CARDMAKETSMD.SEQNO
     *
     * @mbg.generated
     */
    public Integer getSeqno() {
        return seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.SEQNO
     *
     * @param seqno the value for CARDMAKETSMD.SEQNO
     *
     * @mbg.generated
     */
    public void setSeqno(Integer seqno) {
        this.seqno = seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.ACTNOCARDNO
     *
     * @return the value of CARDMAKETSMD.ACTNOCARDNO
     *
     * @mbg.generated
     */
    public String getActnocardno() {
        return actnocardno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.ACTNOCARDNO
     *
     * @param actnocardno the value for CARDMAKETSMD.ACTNOCARDNO
     *
     * @mbg.generated
     */
    public void setActnocardno(String actnocardno) {
        this.actnocardno = actnocardno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.SEID
     *
     * @return the value of CARDMAKETSMD.SEID
     *
     * @mbg.generated
     */
    public String getSeid() {
        return seid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.SEID
     *
     * @param seid the value for CARDMAKETSMD.SEID
     *
     * @mbg.generated
     */
    public void setSeid(String seid) {
        this.seid = seid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.PAYMENTSCHEME
     *
     * @return the value of CARDMAKETSMD.PAYMENTSCHEME
     *
     * @mbg.generated
     */
    public String getPaymentscheme() {
        return paymentscheme;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.PAYMENTSCHEME
     *
     * @param paymentscheme the value for CARDMAKETSMD.PAYMENTSCHEME
     *
     * @mbg.generated
     */
    public void setPaymentscheme(String paymentscheme) {
        this.paymentscheme = paymentscheme;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.MSISDN
     *
     * @return the value of CARDMAKETSMD.MSISDN
     *
     * @mbg.generated
     */
    public String getMsisdn() {
        return msisdn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.MSISDN
     *
     * @param msisdn the value for CARDMAKETSMD.MSISDN
     *
     * @mbg.generated
     */
    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.SETYPE
     *
     * @return the value of CARDMAKETSMD.SETYPE
     *
     * @mbg.generated
     */
    public String getSetype() {
        return setype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.SETYPE
     *
     * @param setype the value for CARDMAKETSMD.SETYPE
     *
     * @mbg.generated
     */
    public void setSetype(String setype) {
        this.setype = setype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.MNOID
     *
     * @return the value of CARDMAKETSMD.MNOID
     *
     * @mbg.generated
     */
    public String getMnoid() {
        return mnoid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.MNOID
     *
     * @param mnoid the value for CARDMAKETSMD.MNOID
     *
     * @mbg.generated
     */
    public void setMnoid(String mnoid) {
        this.mnoid = mnoid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.ACTIVATIONCODE
     *
     * @return the value of CARDMAKETSMD.ACTIVATIONCODE
     *
     * @mbg.generated
     */
    public String getActivationcode() {
        return activationcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.ACTIVATIONCODE
     *
     * @param activationcode the value for CARDMAKETSMD.ACTIVATIONCODE
     *
     * @mbg.generated
     */
    public void setActivationcode(String activationcode) {
        this.activationcode = activationcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.C2PIN
     *
     * @return the value of CARDMAKETSMD.C2PIN
     *
     * @mbg.generated
     */
    public String getC2pin() {
        return c2pin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.C2PIN
     *
     * @param c2pin the value for CARDMAKETSMD.C2PIN
     *
     * @mbg.generated
     */
    public void setC2pin(String c2pin) {
        this.c2pin = c2pin;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.CONTACTLESSCARDHOLDERAUTHFLAG
     *
     * @return the value of CARDMAKETSMD.CONTACTLESSCARDHOLDERAUTHFLAG
     *
     * @mbg.generated
     */
    public String getContactlesscardholderauthflag() {
        return contactlesscardholderauthflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.CONTACTLESSCARDHOLDERAUTHFLAG
     *
     * @param contactlesscardholderauthflag the value for CARDMAKETSMD.CONTACTLESSCARDHOLDERAUTHFLAG
     *
     * @mbg.generated
     */
    public void setContactlesscardholderauthflag(String contactlesscardholderauthflag) {
        this.contactlesscardholderauthflag = contactlesscardholderauthflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.IDNO
     *
     * @return the value of CARDMAKETSMD.IDNO
     *
     * @mbg.generated
     */
    public String getIdno() {
        return idno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.IDNO
     *
     * @param idno the value for CARDMAKETSMD.IDNO
     *
     * @mbg.generated
     */
    public void setIdno(String idno) {
        this.idno = idno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.CARDISSUSER
     *
     * @return the value of CARDMAKETSMD.CARDISSUSER
     *
     * @mbg.generated
     */
    public String getCardissuser() {
        return cardissuser;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.CARDISSUSER
     *
     * @param cardissuser the value for CARDMAKETSMD.CARDISSUSER
     *
     * @mbg.generated
     */
    public void setCardissuser(String cardissuser) {
        this.cardissuser = cardissuser;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.ICREMARK1001
     *
     * @return the value of CARDMAKETSMD.ICREMARK1001
     *
     * @mbg.generated
     */
    public String getIcremark1001() {
        return icremark1001;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.ICREMARK1001
     *
     * @param icremark1001 the value for CARDMAKETSMD.ICREMARK1001
     *
     * @mbg.generated
     */
    public void setIcremark1001(String icremark1001) {
        this.icremark1001 = icremark1001;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.ICREMARK1003
     *
     * @return the value of CARDMAKETSMD.ICREMARK1003
     *
     * @mbg.generated
     */
    public String getIcremark1003() {
        return icremark1003;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.ICREMARK1003
     *
     * @param icremark1003 the value for CARDMAKETSMD.ICREMARK1003
     *
     * @mbg.generated
     */
    public void setIcremark1003(String icremark1003) {
        this.icremark1003 = icremark1003;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.DISPLAYACTNOCARDNO
     *
     * @return the value of CARDMAKETSMD.DISPLAYACTNOCARDNO
     *
     * @mbg.generated
     */
    public String getDisplayactnocardno() {
        return displayactnocardno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.DISPLAYACTNOCARDNO
     *
     * @param displayactnocardno the value for CARDMAKETSMD.DISPLAYACTNOCARDNO
     *
     * @mbg.generated
     */
    public void setDisplayactnocardno(String displayactnocardno) {
        this.displayactnocardno = displayactnocardno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.STOREVALUEPURSEID
     *
     * @return the value of CARDMAKETSMD.STOREVALUEPURSEID
     *
     * @mbg.generated
     */
    public String getStorevaluepurseid() {
        return storevaluepurseid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.STOREVALUEPURSEID
     *
     * @param storevaluepurseid the value for CARDMAKETSMD.STOREVALUEPURSEID
     *
     * @mbg.generated
     */
    public void setStorevaluepurseid(String storevaluepurseid) {
        this.storevaluepurseid = storevaluepurseid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.CHIPID
     *
     * @return the value of CARDMAKETSMD.CHIPID
     *
     * @mbg.generated
     */
    public String getChipid() {
        return chipid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.CHIPID
     *
     * @param chipid the value for CARDMAKETSMD.CHIPID
     *
     * @mbg.generated
     */
    public void setChipid(String chipid) {
        this.chipid = chipid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.RESPONSECODE
     *
     * @return the value of CARDMAKETSMD.RESPONSECODE
     *
     * @mbg.generated
     */
    public String getResponsecode() {
        return responsecode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.RESPONSECODE
     *
     * @param responsecode the value for CARDMAKETSMD.RESPONSECODE
     *
     * @mbg.generated
     */
    public void setResponsecode(String responsecode) {
        this.responsecode = responsecode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.SIR
     *
     * @return the value of CARDMAKETSMD.SIR
     *
     * @mbg.generated
     */
    public String getSir() {
        return sir;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.SIR
     *
     * @param sir the value for CARDMAKETSMD.SIR
     *
     * @mbg.generated
     */
    public void setSir(String sir) {
        this.sir = sir;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.DIVERSIFYDATA
     *
     * @return the value of CARDMAKETSMD.DIVERSIFYDATA
     *
     * @mbg.generated
     */
    public String getDiversifydata() {
        return diversifydata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.DIVERSIFYDATA
     *
     * @param diversifydata the value for CARDMAKETSMD.DIVERSIFYDATA
     *
     * @mbg.generated
     */
    public void setDiversifydata(String diversifydata) {
        this.diversifydata = diversifydata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.UPDATE_USERID
     *
     * @return the value of CARDMAKETSMD.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.UPDATE_USERID
     *
     * @param updateUserid the value for CARDMAKETSMD.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMD.UPDATE_TIME
     *
     * @return the value of CARDMAKETSMD.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMD.UPDATE_TIME
     *
     * @param updateTime the value for CARDMAKETSMD.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", txdate=").append(txdate);
        sb.append(", batchseq=").append(batchseq);
        sb.append(", seqno=").append(seqno);
        sb.append(", actnocardno=").append(actnocardno);
        sb.append(", seid=").append(seid);
        sb.append(", paymentscheme=").append(paymentscheme);
        sb.append(", msisdn=").append(msisdn);
        sb.append(", setype=").append(setype);
        sb.append(", mnoid=").append(mnoid);
        sb.append(", activationcode=").append(activationcode);
        sb.append(", c2pin=").append(c2pin);
        sb.append(", contactlesscardholderauthflag=").append(contactlesscardholderauthflag);
        sb.append(", idno=").append(idno);
        sb.append(", cardissuser=").append(cardissuser);
        sb.append(", icremark1001=").append(icremark1001);
        sb.append(", icremark1003=").append(icremark1003);
        sb.append(", displayactnocardno=").append(displayactnocardno);
        sb.append(", storevaluepurseid=").append(storevaluepurseid);
        sb.append(", chipid=").append(chipid);
        sb.append(", responsecode=").append(responsecode);
        sb.append(", sir=").append(sir);
        sb.append(", diversifydata=").append(diversifydata);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Cardmaketsmd other = (Cardmaketsmd) that;
        return (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getBatchseq() == null ? other.getBatchseq() == null : this.getBatchseq().equals(other.getBatchseq()))
            && (this.getSeqno() == null ? other.getSeqno() == null : this.getSeqno().equals(other.getSeqno()))
            && (this.getActnocardno() == null ? other.getActnocardno() == null : this.getActnocardno().equals(other.getActnocardno()))
            && (this.getSeid() == null ? other.getSeid() == null : this.getSeid().equals(other.getSeid()))
            && (this.getPaymentscheme() == null ? other.getPaymentscheme() == null : this.getPaymentscheme().equals(other.getPaymentscheme()))
            && (this.getMsisdn() == null ? other.getMsisdn() == null : this.getMsisdn().equals(other.getMsisdn()))
            && (this.getSetype() == null ? other.getSetype() == null : this.getSetype().equals(other.getSetype()))
            && (this.getMnoid() == null ? other.getMnoid() == null : this.getMnoid().equals(other.getMnoid()))
            && (this.getActivationcode() == null ? other.getActivationcode() == null : this.getActivationcode().equals(other.getActivationcode()))
            && (this.getC2pin() == null ? other.getC2pin() == null : this.getC2pin().equals(other.getC2pin()))
            && (this.getContactlesscardholderauthflag() == null ? other.getContactlesscardholderauthflag() == null : this.getContactlesscardholderauthflag().equals(other.getContactlesscardholderauthflag()))
            && (this.getIdno() == null ? other.getIdno() == null : this.getIdno().equals(other.getIdno()))
            && (this.getCardissuser() == null ? other.getCardissuser() == null : this.getCardissuser().equals(other.getCardissuser()))
            && (this.getIcremark1001() == null ? other.getIcremark1001() == null : this.getIcremark1001().equals(other.getIcremark1001()))
            && (this.getIcremark1003() == null ? other.getIcremark1003() == null : this.getIcremark1003().equals(other.getIcremark1003()))
            && (this.getDisplayactnocardno() == null ? other.getDisplayactnocardno() == null : this.getDisplayactnocardno().equals(other.getDisplayactnocardno()))
            && (this.getStorevaluepurseid() == null ? other.getStorevaluepurseid() == null : this.getStorevaluepurseid().equals(other.getStorevaluepurseid()))
            && (this.getChipid() == null ? other.getChipid() == null : this.getChipid().equals(other.getChipid()))
            && (this.getResponsecode() == null ? other.getResponsecode() == null : this.getResponsecode().equals(other.getResponsecode()))
            && (this.getSir() == null ? other.getSir() == null : this.getSir().equals(other.getSir()))
            && (this.getDiversifydata() == null ? other.getDiversifydata() == null : this.getDiversifydata().equals(other.getDiversifydata()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getBatchseq() == null) ? 0 : getBatchseq().hashCode());
        result = 31 * result + ((getSeqno() == null) ? 0 : getSeqno().hashCode());
        result = 31 * result + ((getActnocardno() == null) ? 0 : getActnocardno().hashCode());
        result = 31 * result + ((getSeid() == null) ? 0 : getSeid().hashCode());
        result = 31 * result + ((getPaymentscheme() == null) ? 0 : getPaymentscheme().hashCode());
        result = 31 * result + ((getMsisdn() == null) ? 0 : getMsisdn().hashCode());
        result = 31 * result + ((getSetype() == null) ? 0 : getSetype().hashCode());
        result = 31 * result + ((getMnoid() == null) ? 0 : getMnoid().hashCode());
        result = 31 * result + ((getActivationcode() == null) ? 0 : getActivationcode().hashCode());
        result = 31 * result + ((getC2pin() == null) ? 0 : getC2pin().hashCode());
        result = 31 * result + ((getContactlesscardholderauthflag() == null) ? 0 : getContactlesscardholderauthflag().hashCode());
        result = 31 * result + ((getIdno() == null) ? 0 : getIdno().hashCode());
        result = 31 * result + ((getCardissuser() == null) ? 0 : getCardissuser().hashCode());
        result = 31 * result + ((getIcremark1001() == null) ? 0 : getIcremark1001().hashCode());
        result = 31 * result + ((getIcremark1003() == null) ? 0 : getIcremark1003().hashCode());
        result = 31 * result + ((getDisplayactnocardno() == null) ? 0 : getDisplayactnocardno().hashCode());
        result = 31 * result + ((getStorevaluepurseid() == null) ? 0 : getStorevaluepurseid().hashCode());
        result = 31 * result + ((getChipid() == null) ? 0 : getChipid().hashCode());
        result = 31 * result + ((getResponsecode() == null) ? 0 : getResponsecode().hashCode());
        result = 31 * result + ((getSir() == null) ? 0 : getSir().hashCode());
        result = 31 * result + ((getDiversifydata() == null) ? 0 : getDiversifydata().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMD
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<BATCHSEQ>").append(this.batchseq).append("</BATCHSEQ>");
        sb.append("<SEQNO>").append(this.seqno).append("</SEQNO>");
        sb.append("<ACTNOCARDNO>").append(this.actnocardno).append("</ACTNOCARDNO>");
        sb.append("<SEID>").append(this.seid).append("</SEID>");
        sb.append("<PAYMENTSCHEME>").append(this.paymentscheme).append("</PAYMENTSCHEME>");
        sb.append("<MSISDN>").append(this.msisdn).append("</MSISDN>");
        sb.append("<SETYPE>").append(this.setype).append("</SETYPE>");
        sb.append("<MNOID>").append(this.mnoid).append("</MNOID>");
        sb.append("<ACTIVATIONCODE>").append(this.activationcode).append("</ACTIVATIONCODE>");
        sb.append("<C2PIN>").append(this.c2pin).append("</C2PIN>");
        sb.append("<CONTACTLESSCARDHOLDERAUTHFLAG>").append(this.contactlesscardholderauthflag).append("</CONTACTLESSCARDHOLDERAUTHFLAG>");
        sb.append("<IDNO>").append(this.idno).append("</IDNO>");
        sb.append("<CARDISSUSER>").append(this.cardissuser).append("</CARDISSUSER>");
        sb.append("<ICREMARK1001>").append(this.icremark1001).append("</ICREMARK1001>");
        sb.append("<ICREMARK1003>").append(this.icremark1003).append("</ICREMARK1003>");
        sb.append("<DISPLAYACTNOCARDNO>").append(this.displayactnocardno).append("</DISPLAYACTNOCARDNO>");
        sb.append("<STOREVALUEPURSEID>").append(this.storevaluepurseid).append("</STOREVALUEPURSEID>");
        sb.append("<CHIPID>").append(this.chipid).append("</CHIPID>");
        sb.append("<RESPONSECODE>").append(this.responsecode).append("</RESPONSECODE>");
        sb.append("<SIR>").append(this.sir).append("</SIR>");
        sb.append("<DIVERSIFYDATA>").append(this.diversifydata).append("</DIVERSIFYDATA>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}