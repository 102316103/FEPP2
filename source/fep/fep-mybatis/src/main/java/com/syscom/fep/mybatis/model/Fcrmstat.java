package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Fcrmstat extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_CURRENCY
     *
     * @mbg.generated
     */
    private String fcrmstatCurrency = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_CURRENCY_MEMO
     *
     * @mbg.generated
     */
    private String fcrmstatCurrencyMemo;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_FISCI_FLAG1
     *
     * @mbg.generated
     */
    private String fcrmstatFisciFlag1;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_FISCI_FLAG4
     *
     * @mbg.generated
     */
    private String fcrmstatFisciFlag4;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_FISCO_FLAG1
     *
     * @mbg.generated
     */
    private String fcrmstatFiscoFlag1;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_FISCO_FLAG4
     *
     * @mbg.generated
     */
    private String fcrmstatFiscoFlag4;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_AOCTRM
     *
     * @mbg.generated
     */
    private String fcrmstatAoctrm;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_AOCTRM1
     *
     * @mbg.generated
     */
    private String fcrmstatAoctrm1;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_AOCTRM4
     *
     * @mbg.generated
     */
    private String fcrmstatAoctrm4;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_MBACTRM
     *
     * @mbg.generated
     */
    private String fcrmstatMbactrm;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_MBACTRM1
     *
     * @mbg.generated
     */
    private String fcrmstatMbactrm1;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_MBACTRM4
     *
     * @mbg.generated
     */
    private String fcrmstatMbactrm4;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_CLRACTNO
     *
     * @mbg.generated
     */
    private String fcrmstatClractno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_CLRBKNO
     *
     * @mbg.generated
     */
    private String fcrmstatClrbkno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_FISC_CHARGE
     *
     * @mbg.generated
     */
    private Integer fcrmstatFiscCharge;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_INBANK_CHARGE
     *
     * @mbg.generated
     */
    private Integer fcrmstatInbankCharge;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.FCRMSTAT_CLRBANK_CHARGE
     *
     * @mbg.generated
     */
    private Integer fcrmstatClrbankCharge;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table FCRMSTAT
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSTAT
     *
     * @mbg.generated
     */
    public Fcrmstat(String fcrmstatCurrency, String fcrmstatCurrencyMemo, String fcrmstatFisciFlag1, String fcrmstatFisciFlag4, String fcrmstatFiscoFlag1, String fcrmstatFiscoFlag4, String fcrmstatAoctrm, String fcrmstatAoctrm1, String fcrmstatAoctrm4, String fcrmstatMbactrm, String fcrmstatMbactrm1, String fcrmstatMbactrm4, String fcrmstatClractno, String fcrmstatClrbkno, Integer fcrmstatFiscCharge, Integer fcrmstatInbankCharge, Integer fcrmstatClrbankCharge, Integer updateUserid, Date updateTime) {
        this.fcrmstatCurrency = fcrmstatCurrency;
        this.fcrmstatCurrencyMemo = fcrmstatCurrencyMemo;
        this.fcrmstatFisciFlag1 = fcrmstatFisciFlag1;
        this.fcrmstatFisciFlag4 = fcrmstatFisciFlag4;
        this.fcrmstatFiscoFlag1 = fcrmstatFiscoFlag1;
        this.fcrmstatFiscoFlag4 = fcrmstatFiscoFlag4;
        this.fcrmstatAoctrm = fcrmstatAoctrm;
        this.fcrmstatAoctrm1 = fcrmstatAoctrm1;
        this.fcrmstatAoctrm4 = fcrmstatAoctrm4;
        this.fcrmstatMbactrm = fcrmstatMbactrm;
        this.fcrmstatMbactrm1 = fcrmstatMbactrm1;
        this.fcrmstatMbactrm4 = fcrmstatMbactrm4;
        this.fcrmstatClractno = fcrmstatClractno;
        this.fcrmstatClrbkno = fcrmstatClrbkno;
        this.fcrmstatFiscCharge = fcrmstatFiscCharge;
        this.fcrmstatInbankCharge = fcrmstatInbankCharge;
        this.fcrmstatClrbankCharge = fcrmstatClrbankCharge;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSTAT
     *
     * @mbg.generated
     */
    public Fcrmstat() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_CURRENCY
     *
     * @return the value of FCRMSTAT.FCRMSTAT_CURRENCY
     *
     * @mbg.generated
     */
    public String getFcrmstatCurrency() {
        return fcrmstatCurrency;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_CURRENCY
     *
     * @param fcrmstatCurrency the value for FCRMSTAT.FCRMSTAT_CURRENCY
     *
     * @mbg.generated
     */
    public void setFcrmstatCurrency(String fcrmstatCurrency) {
        this.fcrmstatCurrency = fcrmstatCurrency;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_CURRENCY_MEMO
     *
     * @return the value of FCRMSTAT.FCRMSTAT_CURRENCY_MEMO
     *
     * @mbg.generated
     */
    public String getFcrmstatCurrencyMemo() {
        return fcrmstatCurrencyMemo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_CURRENCY_MEMO
     *
     * @param fcrmstatCurrencyMemo the value for FCRMSTAT.FCRMSTAT_CURRENCY_MEMO
     *
     * @mbg.generated
     */
    public void setFcrmstatCurrencyMemo(String fcrmstatCurrencyMemo) {
        this.fcrmstatCurrencyMemo = fcrmstatCurrencyMemo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_FISCI_FLAG1
     *
     * @return the value of FCRMSTAT.FCRMSTAT_FISCI_FLAG1
     *
     * @mbg.generated
     */
    public String getFcrmstatFisciFlag1() {
        return fcrmstatFisciFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_FISCI_FLAG1
     *
     * @param fcrmstatFisciFlag1 the value for FCRMSTAT.FCRMSTAT_FISCI_FLAG1
     *
     * @mbg.generated
     */
    public void setFcrmstatFisciFlag1(String fcrmstatFisciFlag1) {
        this.fcrmstatFisciFlag1 = fcrmstatFisciFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_FISCI_FLAG4
     *
     * @return the value of FCRMSTAT.FCRMSTAT_FISCI_FLAG4
     *
     * @mbg.generated
     */
    public String getFcrmstatFisciFlag4() {
        return fcrmstatFisciFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_FISCI_FLAG4
     *
     * @param fcrmstatFisciFlag4 the value for FCRMSTAT.FCRMSTAT_FISCI_FLAG4
     *
     * @mbg.generated
     */
    public void setFcrmstatFisciFlag4(String fcrmstatFisciFlag4) {
        this.fcrmstatFisciFlag4 = fcrmstatFisciFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_FISCO_FLAG1
     *
     * @return the value of FCRMSTAT.FCRMSTAT_FISCO_FLAG1
     *
     * @mbg.generated
     */
    public String getFcrmstatFiscoFlag1() {
        return fcrmstatFiscoFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_FISCO_FLAG1
     *
     * @param fcrmstatFiscoFlag1 the value for FCRMSTAT.FCRMSTAT_FISCO_FLAG1
     *
     * @mbg.generated
     */
    public void setFcrmstatFiscoFlag1(String fcrmstatFiscoFlag1) {
        this.fcrmstatFiscoFlag1 = fcrmstatFiscoFlag1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_FISCO_FLAG4
     *
     * @return the value of FCRMSTAT.FCRMSTAT_FISCO_FLAG4
     *
     * @mbg.generated
     */
    public String getFcrmstatFiscoFlag4() {
        return fcrmstatFiscoFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_FISCO_FLAG4
     *
     * @param fcrmstatFiscoFlag4 the value for FCRMSTAT.FCRMSTAT_FISCO_FLAG4
     *
     * @mbg.generated
     */
    public void setFcrmstatFiscoFlag4(String fcrmstatFiscoFlag4) {
        this.fcrmstatFiscoFlag4 = fcrmstatFiscoFlag4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_AOCTRM
     *
     * @return the value of FCRMSTAT.FCRMSTAT_AOCTRM
     *
     * @mbg.generated
     */
    public String getFcrmstatAoctrm() {
        return fcrmstatAoctrm;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_AOCTRM
     *
     * @param fcrmstatAoctrm the value for FCRMSTAT.FCRMSTAT_AOCTRM
     *
     * @mbg.generated
     */
    public void setFcrmstatAoctrm(String fcrmstatAoctrm) {
        this.fcrmstatAoctrm = fcrmstatAoctrm;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_AOCTRM1
     *
     * @return the value of FCRMSTAT.FCRMSTAT_AOCTRM1
     *
     * @mbg.generated
     */
    public String getFcrmstatAoctrm1() {
        return fcrmstatAoctrm1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_AOCTRM1
     *
     * @param fcrmstatAoctrm1 the value for FCRMSTAT.FCRMSTAT_AOCTRM1
     *
     * @mbg.generated
     */
    public void setFcrmstatAoctrm1(String fcrmstatAoctrm1) {
        this.fcrmstatAoctrm1 = fcrmstatAoctrm1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_AOCTRM4
     *
     * @return the value of FCRMSTAT.FCRMSTAT_AOCTRM4
     *
     * @mbg.generated
     */
    public String getFcrmstatAoctrm4() {
        return fcrmstatAoctrm4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_AOCTRM4
     *
     * @param fcrmstatAoctrm4 the value for FCRMSTAT.FCRMSTAT_AOCTRM4
     *
     * @mbg.generated
     */
    public void setFcrmstatAoctrm4(String fcrmstatAoctrm4) {
        this.fcrmstatAoctrm4 = fcrmstatAoctrm4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_MBACTRM
     *
     * @return the value of FCRMSTAT.FCRMSTAT_MBACTRM
     *
     * @mbg.generated
     */
    public String getFcrmstatMbactrm() {
        return fcrmstatMbactrm;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_MBACTRM
     *
     * @param fcrmstatMbactrm the value for FCRMSTAT.FCRMSTAT_MBACTRM
     *
     * @mbg.generated
     */
    public void setFcrmstatMbactrm(String fcrmstatMbactrm) {
        this.fcrmstatMbactrm = fcrmstatMbactrm;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_MBACTRM1
     *
     * @return the value of FCRMSTAT.FCRMSTAT_MBACTRM1
     *
     * @mbg.generated
     */
    public String getFcrmstatMbactrm1() {
        return fcrmstatMbactrm1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_MBACTRM1
     *
     * @param fcrmstatMbactrm1 the value for FCRMSTAT.FCRMSTAT_MBACTRM1
     *
     * @mbg.generated
     */
    public void setFcrmstatMbactrm1(String fcrmstatMbactrm1) {
        this.fcrmstatMbactrm1 = fcrmstatMbactrm1;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_MBACTRM4
     *
     * @return the value of FCRMSTAT.FCRMSTAT_MBACTRM4
     *
     * @mbg.generated
     */
    public String getFcrmstatMbactrm4() {
        return fcrmstatMbactrm4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_MBACTRM4
     *
     * @param fcrmstatMbactrm4 the value for FCRMSTAT.FCRMSTAT_MBACTRM4
     *
     * @mbg.generated
     */
    public void setFcrmstatMbactrm4(String fcrmstatMbactrm4) {
        this.fcrmstatMbactrm4 = fcrmstatMbactrm4;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_CLRACTNO
     *
     * @return the value of FCRMSTAT.FCRMSTAT_CLRACTNO
     *
     * @mbg.generated
     */
    public String getFcrmstatClractno() {
        return fcrmstatClractno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_CLRACTNO
     *
     * @param fcrmstatClractno the value for FCRMSTAT.FCRMSTAT_CLRACTNO
     *
     * @mbg.generated
     */
    public void setFcrmstatClractno(String fcrmstatClractno) {
        this.fcrmstatClractno = fcrmstatClractno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_CLRBKNO
     *
     * @return the value of FCRMSTAT.FCRMSTAT_CLRBKNO
     *
     * @mbg.generated
     */
    public String getFcrmstatClrbkno() {
        return fcrmstatClrbkno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_CLRBKNO
     *
     * @param fcrmstatClrbkno the value for FCRMSTAT.FCRMSTAT_CLRBKNO
     *
     * @mbg.generated
     */
    public void setFcrmstatClrbkno(String fcrmstatClrbkno) {
        this.fcrmstatClrbkno = fcrmstatClrbkno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_FISC_CHARGE
     *
     * @return the value of FCRMSTAT.FCRMSTAT_FISC_CHARGE
     *
     * @mbg.generated
     */
    public Integer getFcrmstatFiscCharge() {
        return fcrmstatFiscCharge;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_FISC_CHARGE
     *
     * @param fcrmstatFiscCharge the value for FCRMSTAT.FCRMSTAT_FISC_CHARGE
     *
     * @mbg.generated
     */
    public void setFcrmstatFiscCharge(Integer fcrmstatFiscCharge) {
        this.fcrmstatFiscCharge = fcrmstatFiscCharge;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_INBANK_CHARGE
     *
     * @return the value of FCRMSTAT.FCRMSTAT_INBANK_CHARGE
     *
     * @mbg.generated
     */
    public Integer getFcrmstatInbankCharge() {
        return fcrmstatInbankCharge;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_INBANK_CHARGE
     *
     * @param fcrmstatInbankCharge the value for FCRMSTAT.FCRMSTAT_INBANK_CHARGE
     *
     * @mbg.generated
     */
    public void setFcrmstatInbankCharge(Integer fcrmstatInbankCharge) {
        this.fcrmstatInbankCharge = fcrmstatInbankCharge;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.FCRMSTAT_CLRBANK_CHARGE
     *
     * @return the value of FCRMSTAT.FCRMSTAT_CLRBANK_CHARGE
     *
     * @mbg.generated
     */
    public Integer getFcrmstatClrbankCharge() {
        return fcrmstatClrbankCharge;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.FCRMSTAT_CLRBANK_CHARGE
     *
     * @param fcrmstatClrbankCharge the value for FCRMSTAT.FCRMSTAT_CLRBANK_CHARGE
     *
     * @mbg.generated
     */
    public void setFcrmstatClrbankCharge(Integer fcrmstatClrbankCharge) {
        this.fcrmstatClrbankCharge = fcrmstatClrbankCharge;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.UPDATE_USERID
     *
     * @return the value of FCRMSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.UPDATE_USERID
     *
     * @param updateUserid the value for FCRMSTAT.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMSTAT.UPDATE_TIME
     *
     * @return the value of FCRMSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMSTAT.UPDATE_TIME
     *
     * @param updateTime the value for FCRMSTAT.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSTAT
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
        Fcrmstat other = (Fcrmstat) that;
        return (this.getFcrmstatCurrency() == null ? other.getFcrmstatCurrency() == null : this.getFcrmstatCurrency().equals(other.getFcrmstatCurrency()))
            && (this.getFcrmstatCurrencyMemo() == null ? other.getFcrmstatCurrencyMemo() == null : this.getFcrmstatCurrencyMemo().equals(other.getFcrmstatCurrencyMemo()))
            && (this.getFcrmstatFisciFlag1() == null ? other.getFcrmstatFisciFlag1() == null : this.getFcrmstatFisciFlag1().equals(other.getFcrmstatFisciFlag1()))
            && (this.getFcrmstatFisciFlag4() == null ? other.getFcrmstatFisciFlag4() == null : this.getFcrmstatFisciFlag4().equals(other.getFcrmstatFisciFlag4()))
            && (this.getFcrmstatFiscoFlag1() == null ? other.getFcrmstatFiscoFlag1() == null : this.getFcrmstatFiscoFlag1().equals(other.getFcrmstatFiscoFlag1()))
            && (this.getFcrmstatFiscoFlag4() == null ? other.getFcrmstatFiscoFlag4() == null : this.getFcrmstatFiscoFlag4().equals(other.getFcrmstatFiscoFlag4()))
            && (this.getFcrmstatAoctrm() == null ? other.getFcrmstatAoctrm() == null : this.getFcrmstatAoctrm().equals(other.getFcrmstatAoctrm()))
            && (this.getFcrmstatAoctrm1() == null ? other.getFcrmstatAoctrm1() == null : this.getFcrmstatAoctrm1().equals(other.getFcrmstatAoctrm1()))
            && (this.getFcrmstatAoctrm4() == null ? other.getFcrmstatAoctrm4() == null : this.getFcrmstatAoctrm4().equals(other.getFcrmstatAoctrm4()))
            && (this.getFcrmstatMbactrm() == null ? other.getFcrmstatMbactrm() == null : this.getFcrmstatMbactrm().equals(other.getFcrmstatMbactrm()))
            && (this.getFcrmstatMbactrm1() == null ? other.getFcrmstatMbactrm1() == null : this.getFcrmstatMbactrm1().equals(other.getFcrmstatMbactrm1()))
            && (this.getFcrmstatMbactrm4() == null ? other.getFcrmstatMbactrm4() == null : this.getFcrmstatMbactrm4().equals(other.getFcrmstatMbactrm4()))
            && (this.getFcrmstatClractno() == null ? other.getFcrmstatClractno() == null : this.getFcrmstatClractno().equals(other.getFcrmstatClractno()))
            && (this.getFcrmstatClrbkno() == null ? other.getFcrmstatClrbkno() == null : this.getFcrmstatClrbkno().equals(other.getFcrmstatClrbkno()))
            && (this.getFcrmstatFiscCharge() == null ? other.getFcrmstatFiscCharge() == null : this.getFcrmstatFiscCharge().equals(other.getFcrmstatFiscCharge()))
            && (this.getFcrmstatInbankCharge() == null ? other.getFcrmstatInbankCharge() == null : this.getFcrmstatInbankCharge().equals(other.getFcrmstatInbankCharge()))
            && (this.getFcrmstatClrbankCharge() == null ? other.getFcrmstatClrbankCharge() == null : this.getFcrmstatClrbankCharge().equals(other.getFcrmstatClrbankCharge()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSTAT
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFcrmstatCurrency() == null) ? 0 : getFcrmstatCurrency().hashCode());
        result = prime * result + ((getFcrmstatCurrencyMemo() == null) ? 0 : getFcrmstatCurrencyMemo().hashCode());
        result = prime * result + ((getFcrmstatFisciFlag1() == null) ? 0 : getFcrmstatFisciFlag1().hashCode());
        result = prime * result + ((getFcrmstatFisciFlag4() == null) ? 0 : getFcrmstatFisciFlag4().hashCode());
        result = prime * result + ((getFcrmstatFiscoFlag1() == null) ? 0 : getFcrmstatFiscoFlag1().hashCode());
        result = prime * result + ((getFcrmstatFiscoFlag4() == null) ? 0 : getFcrmstatFiscoFlag4().hashCode());
        result = prime * result + ((getFcrmstatAoctrm() == null) ? 0 : getFcrmstatAoctrm().hashCode());
        result = prime * result + ((getFcrmstatAoctrm1() == null) ? 0 : getFcrmstatAoctrm1().hashCode());
        result = prime * result + ((getFcrmstatAoctrm4() == null) ? 0 : getFcrmstatAoctrm4().hashCode());
        result = prime * result + ((getFcrmstatMbactrm() == null) ? 0 : getFcrmstatMbactrm().hashCode());
        result = prime * result + ((getFcrmstatMbactrm1() == null) ? 0 : getFcrmstatMbactrm1().hashCode());
        result = prime * result + ((getFcrmstatMbactrm4() == null) ? 0 : getFcrmstatMbactrm4().hashCode());
        result = prime * result + ((getFcrmstatClractno() == null) ? 0 : getFcrmstatClractno().hashCode());
        result = prime * result + ((getFcrmstatClrbkno() == null) ? 0 : getFcrmstatClrbkno().hashCode());
        result = prime * result + ((getFcrmstatFiscCharge() == null) ? 0 : getFcrmstatFiscCharge().hashCode());
        result = prime * result + ((getFcrmstatInbankCharge() == null) ? 0 : getFcrmstatInbankCharge().hashCode());
        result = prime * result + ((getFcrmstatClrbankCharge() == null) ? 0 : getFcrmstatClrbankCharge().hashCode());
        result = prime * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSTAT
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", fcrmstatCurrency=").append(fcrmstatCurrency);
        sb.append(", fcrmstatCurrencyMemo=").append(fcrmstatCurrencyMemo);
        sb.append(", fcrmstatFisciFlag1=").append(fcrmstatFisciFlag1);
        sb.append(", fcrmstatFisciFlag4=").append(fcrmstatFisciFlag4);
        sb.append(", fcrmstatFiscoFlag1=").append(fcrmstatFiscoFlag1);
        sb.append(", fcrmstatFiscoFlag4=").append(fcrmstatFiscoFlag4);
        sb.append(", fcrmstatAoctrm=").append(fcrmstatAoctrm);
        sb.append(", fcrmstatAoctrm1=").append(fcrmstatAoctrm1);
        sb.append(", fcrmstatAoctrm4=").append(fcrmstatAoctrm4);
        sb.append(", fcrmstatMbactrm=").append(fcrmstatMbactrm);
        sb.append(", fcrmstatMbactrm1=").append(fcrmstatMbactrm1);
        sb.append(", fcrmstatMbactrm4=").append(fcrmstatMbactrm4);
        sb.append(", fcrmstatClractno=").append(fcrmstatClractno);
        sb.append(", fcrmstatClrbkno=").append(fcrmstatClrbkno);
        sb.append(", fcrmstatFiscCharge=").append(fcrmstatFiscCharge);
        sb.append(", fcrmstatInbankCharge=").append(fcrmstatInbankCharge);
        sb.append(", fcrmstatClrbankCharge=").append(fcrmstatClrbankCharge);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMSTAT
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<FCRMSTAT_CURRENCY>").append(this.fcrmstatCurrency).append("</FCRMSTAT_CURRENCY>");
        sb.append("<FCRMSTAT_CURRENCY_MEMO>").append(this.fcrmstatCurrencyMemo).append("</FCRMSTAT_CURRENCY_MEMO>");
        sb.append("<FCRMSTAT_FISCI_FLAG1>").append(this.fcrmstatFisciFlag1).append("</FCRMSTAT_FISCI_FLAG1>");
        sb.append("<FCRMSTAT_FISCI_FLAG4>").append(this.fcrmstatFisciFlag4).append("</FCRMSTAT_FISCI_FLAG4>");
        sb.append("<FCRMSTAT_FISCO_FLAG1>").append(this.fcrmstatFiscoFlag1).append("</FCRMSTAT_FISCO_FLAG1>");
        sb.append("<FCRMSTAT_FISCO_FLAG4>").append(this.fcrmstatFiscoFlag4).append("</FCRMSTAT_FISCO_FLAG4>");
        sb.append("<FCRMSTAT_AOCTRM>").append(this.fcrmstatAoctrm).append("</FCRMSTAT_AOCTRM>");
        sb.append("<FCRMSTAT_AOCTRM1>").append(this.fcrmstatAoctrm1).append("</FCRMSTAT_AOCTRM1>");
        sb.append("<FCRMSTAT_AOCTRM4>").append(this.fcrmstatAoctrm4).append("</FCRMSTAT_AOCTRM4>");
        sb.append("<FCRMSTAT_MBACTRM>").append(this.fcrmstatMbactrm).append("</FCRMSTAT_MBACTRM>");
        sb.append("<FCRMSTAT_MBACTRM1>").append(this.fcrmstatMbactrm1).append("</FCRMSTAT_MBACTRM1>");
        sb.append("<FCRMSTAT_MBACTRM4>").append(this.fcrmstatMbactrm4).append("</FCRMSTAT_MBACTRM4>");
        sb.append("<FCRMSTAT_CLRACTNO>").append(this.fcrmstatClractno).append("</FCRMSTAT_CLRACTNO>");
        sb.append("<FCRMSTAT_CLRBKNO>").append(this.fcrmstatClrbkno).append("</FCRMSTAT_CLRBKNO>");
        sb.append("<FCRMSTAT_FISC_CHARGE>").append(this.fcrmstatFiscCharge).append("</FCRMSTAT_FISC_CHARGE>");
        sb.append("<FCRMSTAT_INBANK_CHARGE>").append(this.fcrmstatInbankCharge).append("</FCRMSTAT_INBANK_CHARGE>");
        sb.append("<FCRMSTAT_CLRBANK_CHARGE>").append(this.fcrmstatClrbankCharge).append("</FCRMSTAT_CLRBANK_CHARGE>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}