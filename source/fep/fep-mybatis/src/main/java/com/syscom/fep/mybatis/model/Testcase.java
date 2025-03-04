package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import org.apache.commons.lang3.StringUtils;

public class Testcase extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.SYSTEMID
     *
     * @mbg.generated
     */
    private String systemid = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.TXCD
     *
     * @mbg.generated
     */
    private String txcd = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.CASENO
     *
     * @mbg.generated
     */
    private String caseno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.SEQ
     *
     * @mbg.generated
     */
    private String seq = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.NECESSITY
     *
     * @mbg.generated
     */
    private Short necessity;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.CHANNEL
     *
     * @mbg.generated
     */
    private String channel;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.KIND
     *
     * @mbg.generated
     */
    private String kind;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.TRANFUNCTION
     *
     * @mbg.generated
     */
    private String tranfunction;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.CASEDESC
     *
     * @mbg.generated
     */
    private String casedesc;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.CASERESULT
     *
     * @mbg.generated
     */
    private String caseresult;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.EXPECTEDRESULT
     *
     * @mbg.generated
     */
    private String expectedresult;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.EXPECTEDFISCRC
     *
     * @mbg.generated
     */
    private String expectedfiscrc;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.EXPECTEDATMRC
     *
     * @mbg.generated
     */
    private String expectedatmrc;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.PREPAREITEM
     *
     * @mbg.generated
     */
    private String prepareitem;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.REMARK
     *
     * @mbg.generated
     */
    private String remark;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.TITA
     *
     * @mbg.generated
     */
    private String tita;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTCASE.TOTA
     *
     * @mbg.generated
     */
    private String tota;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table TESTCASE
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTCASE
     *
     * @mbg.generated
     */
    public Testcase(String systemid, String txcd, String caseno, String seq, Short necessity, String channel, String kind, String tranfunction, String casedesc, String caseresult, String expectedresult, String expectedfiscrc, String expectedatmrc) {
        this.systemid = systemid;
        this.txcd = txcd;
        this.caseno = caseno;
        this.seq = seq;
        this.necessity = necessity;
        this.channel = channel;
        this.kind = kind;
        this.tranfunction = tranfunction;
        this.casedesc = casedesc;
        this.caseresult = caseresult;
        this.expectedresult = expectedresult;
        this.expectedfiscrc = expectedfiscrc;
        this.expectedatmrc = expectedatmrc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTCASE
     *
     * @mbg.generated
     */
    public Testcase(String systemid, String txcd, String caseno, String seq, Short necessity, String channel, String kind, String tranfunction, String casedesc, String caseresult, String expectedresult, String expectedfiscrc, String expectedatmrc, String prepareitem, String remark, String tita, String tota) {
        this.systemid = systemid;
        this.txcd = txcd;
        this.caseno = caseno;
        this.seq = seq;
        this.necessity = necessity;
        this.channel = channel;
        this.kind = kind;
        this.tranfunction = tranfunction;
        this.casedesc = casedesc;
        this.caseresult = caseresult;
        this.expectedresult = expectedresult;
        this.expectedfiscrc = expectedfiscrc;
        this.expectedatmrc = expectedatmrc;
        this.prepareitem = prepareitem;
        this.remark = remark;
        this.tita = tita;
        this.tota = tota;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTCASE
     *
     * @mbg.generated
     */
    public Testcase() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.SYSTEMID
     *
     * @return the value of TESTCASE.SYSTEMID
     *
     * @mbg.generated
     */
    public String getSystemid() {
        return systemid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.SYSTEMID
     *
     * @param systemid the value for TESTCASE.SYSTEMID
     *
     * @mbg.generated
     */
    public void setSystemid(String systemid) {
        this.systemid = systemid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.TXCD
     *
     * @return the value of TESTCASE.TXCD
     *
     * @mbg.generated
     */
    public String getTxcd() {
        return txcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.TXCD
     *
     * @param txcd the value for TESTCASE.TXCD
     *
     * @mbg.generated
     */
    public void setTxcd(String txcd) {
        this.txcd = txcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.CASENO
     *
     * @return the value of TESTCASE.CASENO
     *
     * @mbg.generated
     */
    public String getCaseno() {
        return caseno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.CASENO
     *
     * @param caseno the value for TESTCASE.CASENO
     *
     * @mbg.generated
     */
    public void setCaseno(String caseno) {
        this.caseno = caseno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.SEQ
     *
     * @return the value of TESTCASE.SEQ
     *
     * @mbg.generated
     */
    public String getSeq() {
        return seq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.SEQ
     *
     * @param seq the value for TESTCASE.SEQ
     *
     * @mbg.generated
     */
    public void setSeq(String seq) {
        this.seq = seq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.NECESSITY
     *
     * @return the value of TESTCASE.NECESSITY
     *
     * @mbg.generated
     */
    public Short getNecessity() {
        return necessity;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.NECESSITY
     *
     * @param necessity the value for TESTCASE.NECESSITY
     *
     * @mbg.generated
     */
    public void setNecessity(Short necessity) {
        this.necessity = necessity;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.CHANNEL
     *
     * @return the value of TESTCASE.CHANNEL
     *
     * @mbg.generated
     */
    public String getChannel() {
        return channel;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.CHANNEL
     *
     * @param channel the value for TESTCASE.CHANNEL
     *
     * @mbg.generated
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.KIND
     *
     * @return the value of TESTCASE.KIND
     *
     * @mbg.generated
     */
    public String getKind() {
        return kind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.KIND
     *
     * @param kind the value for TESTCASE.KIND
     *
     * @mbg.generated
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.TRANFUNCTION
     *
     * @return the value of TESTCASE.TRANFUNCTION
     *
     * @mbg.generated
     */
    public String getTranfunction() {
        return tranfunction;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.TRANFUNCTION
     *
     * @param tranfunction the value for TESTCASE.TRANFUNCTION
     *
     * @mbg.generated
     */
    public void setTranfunction(String tranfunction) {
        this.tranfunction = tranfunction;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.CASEDESC
     *
     * @return the value of TESTCASE.CASEDESC
     *
     * @mbg.generated
     */
    public String getCasedesc() {
        return casedesc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.CASEDESC
     *
     * @param casedesc the value for TESTCASE.CASEDESC
     *
     * @mbg.generated
     */
    public void setCasedesc(String casedesc) {
        this.casedesc = casedesc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.CASERESULT
     *
     * @return the value of TESTCASE.CASERESULT
     *
     * @mbg.generated
     */
    public String getCaseresult() {
        return caseresult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.CASERESULT
     *
     * @param caseresult the value for TESTCASE.CASERESULT
     *
     * @mbg.generated
     */
    public void setCaseresult(String caseresult) {
        this.caseresult = caseresult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.EXPECTEDRESULT
     *
     * @return the value of TESTCASE.EXPECTEDRESULT
     *
     * @mbg.generated
     */
    public String getExpectedresult() {
        return expectedresult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.EXPECTEDRESULT
     *
     * @param expectedresult the value for TESTCASE.EXPECTEDRESULT
     *
     * @mbg.generated
     */
    public void setExpectedresult(String expectedresult) {
        this.expectedresult = expectedresult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.EXPECTEDFISCRC
     *
     * @return the value of TESTCASE.EXPECTEDFISCRC
     *
     * @mbg.generated
     */
    public String getExpectedfiscrc() {
        return expectedfiscrc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.EXPECTEDFISCRC
     *
     * @param expectedfiscrc the value for TESTCASE.EXPECTEDFISCRC
     *
     * @mbg.generated
     */
    public void setExpectedfiscrc(String expectedfiscrc) {
        this.expectedfiscrc = expectedfiscrc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.EXPECTEDATMRC
     *
     * @return the value of TESTCASE.EXPECTEDATMRC
     *
     * @mbg.generated
     */
    public String getExpectedatmrc() {
        return expectedatmrc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.EXPECTEDATMRC
     *
     * @param expectedatmrc the value for TESTCASE.EXPECTEDATMRC
     *
     * @mbg.generated
     */
    public void setExpectedatmrc(String expectedatmrc) {
        this.expectedatmrc = expectedatmrc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.PREPAREITEM
     *
     * @return the value of TESTCASE.PREPAREITEM
     *
     * @mbg.generated
     */
    public String getPrepareitem() {
        return prepareitem;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.PREPAREITEM
     *
     * @param prepareitem the value for TESTCASE.PREPAREITEM
     *
     * @mbg.generated
     */
    public void setPrepareitem(String prepareitem) {
        this.prepareitem = prepareitem;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.REMARK
     *
     * @return the value of TESTCASE.REMARK
     *
     * @mbg.generated
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.REMARK
     *
     * @param remark the value for TESTCASE.REMARK
     *
     * @mbg.generated
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.TITA
     *
     * @return the value of TESTCASE.TITA
     *
     * @mbg.generated
     */
    public String getTita() {
        return tita;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.TITA
     *
     * @param tita the value for TESTCASE.TITA
     *
     * @mbg.generated
     */
    public void setTita(String tita) {
        this.tita = tita;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTCASE.TOTA
     *
     * @return the value of TESTCASE.TOTA
     *
     * @mbg.generated
     */
    public String getTota() {
        return tota;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTCASE.TOTA
     *
     * @param tota the value for TESTCASE.TOTA
     *
     * @mbg.generated
     */
    public void setTota(String tota) {
        this.tota = tota;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTCASE
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", systemid=").append(systemid);
        sb.append(", txcd=").append(txcd);
        sb.append(", caseno=").append(caseno);
        sb.append(", seq=").append(seq);
        sb.append(", necessity=").append(necessity);
        sb.append(", channel=").append(channel);
        sb.append(", kind=").append(kind);
        sb.append(", tranfunction=").append(tranfunction);
        sb.append(", casedesc=").append(casedesc);
        sb.append(", caseresult=").append(caseresult);
        sb.append(", expectedresult=").append(expectedresult);
        sb.append(", expectedfiscrc=").append(expectedfiscrc);
        sb.append(", expectedatmrc=").append(expectedatmrc);
        sb.append(", prepareitem=").append(prepareitem);
        sb.append(", remark=").append(remark);
        sb.append(", tita=").append(tita);
        sb.append(", tota=").append(tota);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTCASE
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
        Testcase other = (Testcase) that;
        return (this.getSystemid() == null ? other.getSystemid() == null : this.getSystemid().equals(other.getSystemid()))
            && (this.getTxcd() == null ? other.getTxcd() == null : this.getTxcd().equals(other.getTxcd()))
            && (this.getCaseno() == null ? other.getCaseno() == null : this.getCaseno().equals(other.getCaseno()))
            && (this.getSeq() == null ? other.getSeq() == null : this.getSeq().equals(other.getSeq()))
            && (this.getNecessity() == null ? other.getNecessity() == null : this.getNecessity().equals(other.getNecessity()))
            && (this.getChannel() == null ? other.getChannel() == null : this.getChannel().equals(other.getChannel()))
            && (this.getKind() == null ? other.getKind() == null : this.getKind().equals(other.getKind()))
            && (this.getTranfunction() == null ? other.getTranfunction() == null : this.getTranfunction().equals(other.getTranfunction()))
            && (this.getCasedesc() == null ? other.getCasedesc() == null : this.getCasedesc().equals(other.getCasedesc()))
            && (this.getCaseresult() == null ? other.getCaseresult() == null : this.getCaseresult().equals(other.getCaseresult()))
            && (this.getExpectedresult() == null ? other.getExpectedresult() == null : this.getExpectedresult().equals(other.getExpectedresult()))
            && (this.getExpectedfiscrc() == null ? other.getExpectedfiscrc() == null : this.getExpectedfiscrc().equals(other.getExpectedfiscrc()))
            && (this.getExpectedatmrc() == null ? other.getExpectedatmrc() == null : this.getExpectedatmrc().equals(other.getExpectedatmrc()))
            && (this.getPrepareitem() == null ? other.getPrepareitem() == null : this.getPrepareitem().equals(other.getPrepareitem()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getTita() == null ? other.getTita() == null : this.getTita().equals(other.getTita()))
            && (this.getTota() == null ? other.getTota() == null : this.getTota().equals(other.getTota()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTCASE
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getSystemid() == null) ? 0 : getSystemid().hashCode());
        result = 31 * result + ((getTxcd() == null) ? 0 : getTxcd().hashCode());
        result = 31 * result + ((getCaseno() == null) ? 0 : getCaseno().hashCode());
        result = 31 * result + ((getSeq() == null) ? 0 : getSeq().hashCode());
        result = 31 * result + ((getNecessity() == null) ? 0 : getNecessity().hashCode());
        result = 31 * result + ((getChannel() == null) ? 0 : getChannel().hashCode());
        result = 31 * result + ((getKind() == null) ? 0 : getKind().hashCode());
        result = 31 * result + ((getTranfunction() == null) ? 0 : getTranfunction().hashCode());
        result = 31 * result + ((getCasedesc() == null) ? 0 : getCasedesc().hashCode());
        result = 31 * result + ((getCaseresult() == null) ? 0 : getCaseresult().hashCode());
        result = 31 * result + ((getExpectedresult() == null) ? 0 : getExpectedresult().hashCode());
        result = 31 * result + ((getExpectedfiscrc() == null) ? 0 : getExpectedfiscrc().hashCode());
        result = 31 * result + ((getExpectedatmrc() == null) ? 0 : getExpectedatmrc().hashCode());
        result = 31 * result + ((getPrepareitem() == null) ? 0 : getPrepareitem().hashCode());
        result = 31 * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = 31 * result + ((getTita() == null) ? 0 : getTita().hashCode());
        result = 31 * result + ((getTota() == null) ? 0 : getTota().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTCASE
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<SYSTEMID>").append(this.systemid).append("</SYSTEMID>");
        sb.append("<TXCD>").append(this.txcd).append("</TXCD>");
        sb.append("<CASENO>").append(this.caseno).append("</CASENO>");
        sb.append("<SEQ>").append(this.seq).append("</SEQ>");
        sb.append("<NECESSITY>").append(this.necessity).append("</NECESSITY>");
        sb.append("<CHANNEL>").append(this.channel).append("</CHANNEL>");
        sb.append("<KIND>").append(this.kind).append("</KIND>");
        sb.append("<TRANFUNCTION>").append(this.tranfunction).append("</TRANFUNCTION>");
        sb.append("<CASEDESC>").append(this.casedesc).append("</CASEDESC>");
        sb.append("<CASERESULT>").append(this.caseresult).append("</CASERESULT>");
        sb.append("<EXPECTEDRESULT>").append(this.expectedresult).append("</EXPECTEDRESULT>");
        sb.append("<EXPECTEDFISCRC>").append(this.expectedfiscrc).append("</EXPECTEDFISCRC>");
        sb.append("<EXPECTEDATMRC>").append(this.expectedatmrc).append("</EXPECTEDATMRC>");
        sb.append("<PREPAREITEM>").append(this.prepareitem).append("</PREPAREITEM>");
        sb.append("<REMARK>").append(this.remark).append("</REMARK>");
        sb.append("<TITA>").append(this.tita).append("</TITA>");
        sb.append("<TOTA>").append(this.tota).append("</TOTA>");
        return sb.toString();
    }
}