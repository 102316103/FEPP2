package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Hkcif extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.ACTNO
     *
     * @mbg.generated
     */
    private String actno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.CARD_SEQ
     *
     * @mbg.generated
     */
    private Short cardSeq;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.AOCODE
     *
     * @mbg.generated
     */
    private String aocode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.ACTCLS
     *
     * @mbg.generated
     */
    private String actcls;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.ACTSLP
     *
     * @mbg.generated
     */
    private String actslp;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.COUNTRYCODE
     *
     * @mbg.generated
     */
    private String countrycode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.STATMNT
     *
     * @mbg.generated
     */
    private String statmnt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.MAILFG
     *
     * @mbg.generated
     */
    private String mailfg;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.NAME
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.MAILADDRESS
     *
     * @mbg.generated
     */
    private String mailaddress;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.TEL1
     *
     * @mbg.generated
     */
    private String tel1;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.TEL2
     *
     * @mbg.generated
     */
    private String tel2;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.DORMANT
     *
     * @mbg.generated
     */
    private String dormant;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column HKCIF.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table HKCIF
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
     *
     * @mbg.generated
     */
    public Hkcif(String actno, Short cardSeq, String aocode, String actcls, String actslp, String countrycode, String statmnt, String mailfg, String name, String mailaddress, String tel1, String tel2, String dormant, Integer updateUserid, Date updateTime) {
        this.actno = actno;
        this.cardSeq = cardSeq;
        this.aocode = aocode;
        this.actcls = actcls;
        this.actslp = actslp;
        this.countrycode = countrycode;
        this.statmnt = statmnt;
        this.mailfg = mailfg;
        this.name = name;
        this.mailaddress = mailaddress;
        this.tel1 = tel1;
        this.tel2 = tel2;
        this.dormant = dormant;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
     *
     * @mbg.generated
     */
    public Hkcif() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.ACTNO
     *
     * @return the value of HKCIF.ACTNO
     *
     * @mbg.generated
     */
    public String getActno() {
        return actno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.ACTNO
     *
     * @param actno the value for HKCIF.ACTNO
     *
     * @mbg.generated
     */
    public void setActno(String actno) {
        this.actno = actno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.CARD_SEQ
     *
     * @return the value of HKCIF.CARD_SEQ
     *
     * @mbg.generated
     */
    public Short getCardSeq() {
        return cardSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.CARD_SEQ
     *
     * @param cardSeq the value for HKCIF.CARD_SEQ
     *
     * @mbg.generated
     */
    public void setCardSeq(Short cardSeq) {
        this.cardSeq = cardSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.AOCODE
     *
     * @return the value of HKCIF.AOCODE
     *
     * @mbg.generated
     */
    public String getAocode() {
        return aocode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.AOCODE
     *
     * @param aocode the value for HKCIF.AOCODE
     *
     * @mbg.generated
     */
    public void setAocode(String aocode) {
        this.aocode = aocode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.ACTCLS
     *
     * @return the value of HKCIF.ACTCLS
     *
     * @mbg.generated
     */
    public String getActcls() {
        return actcls;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.ACTCLS
     *
     * @param actcls the value for HKCIF.ACTCLS
     *
     * @mbg.generated
     */
    public void setActcls(String actcls) {
        this.actcls = actcls;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.ACTSLP
     *
     * @return the value of HKCIF.ACTSLP
     *
     * @mbg.generated
     */
    public String getActslp() {
        return actslp;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.ACTSLP
     *
     * @param actslp the value for HKCIF.ACTSLP
     *
     * @mbg.generated
     */
    public void setActslp(String actslp) {
        this.actslp = actslp;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.COUNTRYCODE
     *
     * @return the value of HKCIF.COUNTRYCODE
     *
     * @mbg.generated
     */
    public String getCountrycode() {
        return countrycode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.COUNTRYCODE
     *
     * @param countrycode the value for HKCIF.COUNTRYCODE
     *
     * @mbg.generated
     */
    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.STATMNT
     *
     * @return the value of HKCIF.STATMNT
     *
     * @mbg.generated
     */
    public String getStatmnt() {
        return statmnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.STATMNT
     *
     * @param statmnt the value for HKCIF.STATMNT
     *
     * @mbg.generated
     */
    public void setStatmnt(String statmnt) {
        this.statmnt = statmnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.MAILFG
     *
     * @return the value of HKCIF.MAILFG
     *
     * @mbg.generated
     */
    public String getMailfg() {
        return mailfg;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.MAILFG
     *
     * @param mailfg the value for HKCIF.MAILFG
     *
     * @mbg.generated
     */
    public void setMailfg(String mailfg) {
        this.mailfg = mailfg;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.NAME
     *
     * @return the value of HKCIF.NAME
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.NAME
     *
     * @param name the value for HKCIF.NAME
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.MAILADDRESS
     *
     * @return the value of HKCIF.MAILADDRESS
     *
     * @mbg.generated
     */
    public String getMailaddress() {
        return mailaddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.MAILADDRESS
     *
     * @param mailaddress the value for HKCIF.MAILADDRESS
     *
     * @mbg.generated
     */
    public void setMailaddress(String mailaddress) {
        this.mailaddress = mailaddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.TEL1
     *
     * @return the value of HKCIF.TEL1
     *
     * @mbg.generated
     */
    public String getTel1() {
        return tel1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.TEL1
     *
     * @param tel1 the value for HKCIF.TEL1
     *
     * @mbg.generated
     */
    public void setTel1(String tel1) {
        this.tel1 = tel1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.TEL2
     *
     * @return the value of HKCIF.TEL2
     *
     * @mbg.generated
     */
    public String getTel2() {
        return tel2;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.TEL2
     *
     * @param tel2 the value for HKCIF.TEL2
     *
     * @mbg.generated
     */
    public void setTel2(String tel2) {
        this.tel2 = tel2;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.DORMANT
     *
     * @return the value of HKCIF.DORMANT
     *
     * @mbg.generated
     */
    public String getDormant() {
        return dormant;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.DORMANT
     *
     * @param dormant the value for HKCIF.DORMANT
     *
     * @mbg.generated
     */
    public void setDormant(String dormant) {
        this.dormant = dormant;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.UPDATE_USERID
     *
     * @return the value of HKCIF.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.UPDATE_USERID
     *
     * @param updateUserid the value for HKCIF.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column HKCIF.UPDATE_TIME
     *
     * @return the value of HKCIF.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column HKCIF.UPDATE_TIME
     *
     * @param updateTime the value for HKCIF.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", actno=").append(actno);
        sb.append(", cardSeq=").append(cardSeq);
        sb.append(", aocode=").append(aocode);
        sb.append(", actcls=").append(actcls);
        sb.append(", actslp=").append(actslp);
        sb.append(", countrycode=").append(countrycode);
        sb.append(", statmnt=").append(statmnt);
        sb.append(", mailfg=").append(mailfg);
        sb.append(", name=").append(name);
        sb.append(", mailaddress=").append(mailaddress);
        sb.append(", tel1=").append(tel1);
        sb.append(", tel2=").append(tel2);
        sb.append(", dormant=").append(dormant);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
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
        Hkcif other = (Hkcif) that;
        return (this.getActno() == null ? other.getActno() == null : this.getActno().equals(other.getActno()))
            && (this.getCardSeq() == null ? other.getCardSeq() == null : this.getCardSeq().equals(other.getCardSeq()))
            && (this.getAocode() == null ? other.getAocode() == null : this.getAocode().equals(other.getAocode()))
            && (this.getActcls() == null ? other.getActcls() == null : this.getActcls().equals(other.getActcls()))
            && (this.getActslp() == null ? other.getActslp() == null : this.getActslp().equals(other.getActslp()))
            && (this.getCountrycode() == null ? other.getCountrycode() == null : this.getCountrycode().equals(other.getCountrycode()))
            && (this.getStatmnt() == null ? other.getStatmnt() == null : this.getStatmnt().equals(other.getStatmnt()))
            && (this.getMailfg() == null ? other.getMailfg() == null : this.getMailfg().equals(other.getMailfg()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getMailaddress() == null ? other.getMailaddress() == null : this.getMailaddress().equals(other.getMailaddress()))
            && (this.getTel1() == null ? other.getTel1() == null : this.getTel1().equals(other.getTel1()))
            && (this.getTel2() == null ? other.getTel2() == null : this.getTel2().equals(other.getTel2()))
            && (this.getDormant() == null ? other.getDormant() == null : this.getDormant().equals(other.getDormant()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getActno() == null) ? 0 : getActno().hashCode());
        result = 31 * result + ((getCardSeq() == null) ? 0 : getCardSeq().hashCode());
        result = 31 * result + ((getAocode() == null) ? 0 : getAocode().hashCode());
        result = 31 * result + ((getActcls() == null) ? 0 : getActcls().hashCode());
        result = 31 * result + ((getActslp() == null) ? 0 : getActslp().hashCode());
        result = 31 * result + ((getCountrycode() == null) ? 0 : getCountrycode().hashCode());
        result = 31 * result + ((getStatmnt() == null) ? 0 : getStatmnt().hashCode());
        result = 31 * result + ((getMailfg() == null) ? 0 : getMailfg().hashCode());
        result = 31 * result + ((getName() == null) ? 0 : getName().hashCode());
        result = 31 * result + ((getMailaddress() == null) ? 0 : getMailaddress().hashCode());
        result = 31 * result + ((getTel1() == null) ? 0 : getTel1().hashCode());
        result = 31 * result + ((getTel2() == null) ? 0 : getTel2().hashCode());
        result = 31 * result + ((getDormant() == null) ? 0 : getDormant().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table HKCIF
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ACTNO>").append(this.actno).append("</ACTNO>");
        sb.append("<CARD_SEQ>").append(this.cardSeq).append("</CARD_SEQ>");
        sb.append("<AOCODE>").append(this.aocode).append("</AOCODE>");
        sb.append("<ACTCLS>").append(this.actcls).append("</ACTCLS>");
        sb.append("<ACTSLP>").append(this.actslp).append("</ACTSLP>");
        sb.append("<COUNTRYCODE>").append(this.countrycode).append("</COUNTRYCODE>");
        sb.append("<STATMNT>").append(this.statmnt).append("</STATMNT>");
        sb.append("<MAILFG>").append(this.mailfg).append("</MAILFG>");
        sb.append("<NAME>").append(this.name).append("</NAME>");
        sb.append("<MAILADDRESS>").append(this.mailaddress).append("</MAILADDRESS>");
        sb.append("<TEL1>").append(this.tel1).append("</TEL1>");
        sb.append("<TEL2>").append(this.tel2).append("</TEL2>");
        sb.append("<DORMANT>").append(this.dormant).append("</DORMANT>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}