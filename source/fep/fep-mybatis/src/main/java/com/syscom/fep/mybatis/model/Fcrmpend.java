package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Fcrmpend extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_DATE
     *
     * @mbg.generated
     */
    private String fcrmpendDate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_TIME
     *
     * @mbg.generated
     */
    private String fcrmpendTime = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_STAN
     *
     * @mbg.generated
     */
    private String fcrmpendStan = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_PCODE
     *
     * @mbg.generated
     */
    private String fcrmpendPcode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_SENDER_BANK
     *
     * @mbg.generated
     */
    private String fcrmpendSenderBank;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_RECEIVER_BANK
     *
     * @mbg.generated
     */
    private String fcrmpendReceiverBank;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_BANK_NO
     *
     * @mbg.generated
     */
    private String fcrmpendBankNo;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_TXAMT
     *
     * @mbg.generated
     */
    private BigDecimal fcrmpendTxamt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_IN_ACC_ID_NO
     *
     * @mbg.generated
     */
    private String fcrmpendInAccIdNo;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_OUT_NAME
     *
     * @mbg.generated
     */
    private String fcrmpendOutName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_ADDRESS
     *
     * @mbg.generated
     */
    private String fcrmpendAddress;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_IN_NAME
     *
     * @mbg.generated
     */
    private String fcrmpendInName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_KIND
     *
     * @mbg.generated
     */
    private String fcrmpendKind;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_ORGKIND
     *
     * @mbg.generated
     */
    private String fcrmpendOrgkind;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMOEND_CURRENCY
     *
     * @mbg.generated
     */
    private String fcrmoendCurrency;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMOEND_MEMO
     *
     * @mbg.generated
     */
    private String fcrmoendMemo;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.FCRMPEND_MARK
     *
     * @mbg.generated
     */
    private String fcrmpendMark;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column FCRMPEND.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    public Fcrmpend(String fcrmpendDate, String fcrmpendTime, String fcrmpendStan, String fcrmpendPcode, String fcrmpendSenderBank, String fcrmpendReceiverBank, String fcrmpendBankNo, BigDecimal fcrmpendTxamt, String fcrmpendInAccIdNo, String fcrmpendOutName, String fcrmpendAddress, String fcrmpendInName, String fcrmpendKind, String fcrmpendOrgkind, String fcrmoendCurrency, String fcrmoendMemo, String fcrmpendMark, Integer updateUserid, Date updateTime) {
        this.fcrmpendDate = fcrmpendDate;
        this.fcrmpendTime = fcrmpendTime;
        this.fcrmpendStan = fcrmpendStan;
        this.fcrmpendPcode = fcrmpendPcode;
        this.fcrmpendSenderBank = fcrmpendSenderBank;
        this.fcrmpendReceiverBank = fcrmpendReceiverBank;
        this.fcrmpendBankNo = fcrmpendBankNo;
        this.fcrmpendTxamt = fcrmpendTxamt;
        this.fcrmpendInAccIdNo = fcrmpendInAccIdNo;
        this.fcrmpendOutName = fcrmpendOutName;
        this.fcrmpendAddress = fcrmpendAddress;
        this.fcrmpendInName = fcrmpendInName;
        this.fcrmpendKind = fcrmpendKind;
        this.fcrmpendOrgkind = fcrmpendOrgkind;
        this.fcrmoendCurrency = fcrmoendCurrency;
        this.fcrmoendMemo = fcrmoendMemo;
        this.fcrmpendMark = fcrmpendMark;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    public Fcrmpend() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_DATE
     *
     * @return the value of FCRMPEND.FCRMPEND_DATE
     *
     * @mbg.generated
     */
    public String getFcrmpendDate() {
        return fcrmpendDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_DATE
     *
     * @param fcrmpendDate the value for FCRMPEND.FCRMPEND_DATE
     *
     * @mbg.generated
     */
    public void setFcrmpendDate(String fcrmpendDate) {
        this.fcrmpendDate = fcrmpendDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_TIME
     *
     * @return the value of FCRMPEND.FCRMPEND_TIME
     *
     * @mbg.generated
     */
    public String getFcrmpendTime() {
        return fcrmpendTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_TIME
     *
     * @param fcrmpendTime the value for FCRMPEND.FCRMPEND_TIME
     *
     * @mbg.generated
     */
    public void setFcrmpendTime(String fcrmpendTime) {
        this.fcrmpendTime = fcrmpendTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_STAN
     *
     * @return the value of FCRMPEND.FCRMPEND_STAN
     *
     * @mbg.generated
     */
    public String getFcrmpendStan() {
        return fcrmpendStan;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_STAN
     *
     * @param fcrmpendStan the value for FCRMPEND.FCRMPEND_STAN
     *
     * @mbg.generated
     */
    public void setFcrmpendStan(String fcrmpendStan) {
        this.fcrmpendStan = fcrmpendStan;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_PCODE
     *
     * @return the value of FCRMPEND.FCRMPEND_PCODE
     *
     * @mbg.generated
     */
    public String getFcrmpendPcode() {
        return fcrmpendPcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_PCODE
     *
     * @param fcrmpendPcode the value for FCRMPEND.FCRMPEND_PCODE
     *
     * @mbg.generated
     */
    public void setFcrmpendPcode(String fcrmpendPcode) {
        this.fcrmpendPcode = fcrmpendPcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_SENDER_BANK
     *
     * @return the value of FCRMPEND.FCRMPEND_SENDER_BANK
     *
     * @mbg.generated
     */
    public String getFcrmpendSenderBank() {
        return fcrmpendSenderBank;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_SENDER_BANK
     *
     * @param fcrmpendSenderBank the value for FCRMPEND.FCRMPEND_SENDER_BANK
     *
     * @mbg.generated
     */
    public void setFcrmpendSenderBank(String fcrmpendSenderBank) {
        this.fcrmpendSenderBank = fcrmpendSenderBank;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_RECEIVER_BANK
     *
     * @return the value of FCRMPEND.FCRMPEND_RECEIVER_BANK
     *
     * @mbg.generated
     */
    public String getFcrmpendReceiverBank() {
        return fcrmpendReceiverBank;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_RECEIVER_BANK
     *
     * @param fcrmpendReceiverBank the value for FCRMPEND.FCRMPEND_RECEIVER_BANK
     *
     * @mbg.generated
     */
    public void setFcrmpendReceiverBank(String fcrmpendReceiverBank) {
        this.fcrmpendReceiverBank = fcrmpendReceiverBank;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_BANK_NO
     *
     * @return the value of FCRMPEND.FCRMPEND_BANK_NO
     *
     * @mbg.generated
     */
    public String getFcrmpendBankNo() {
        return fcrmpendBankNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_BANK_NO
     *
     * @param fcrmpendBankNo the value for FCRMPEND.FCRMPEND_BANK_NO
     *
     * @mbg.generated
     */
    public void setFcrmpendBankNo(String fcrmpendBankNo) {
        this.fcrmpendBankNo = fcrmpendBankNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_TXAMT
     *
     * @return the value of FCRMPEND.FCRMPEND_TXAMT
     *
     * @mbg.generated
     */
    public BigDecimal getFcrmpendTxamt() {
        return fcrmpendTxamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_TXAMT
     *
     * @param fcrmpendTxamt the value for FCRMPEND.FCRMPEND_TXAMT
     *
     * @mbg.generated
     */
    public void setFcrmpendTxamt(BigDecimal fcrmpendTxamt) {
        this.fcrmpendTxamt = fcrmpendTxamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_IN_ACC_ID_NO
     *
     * @return the value of FCRMPEND.FCRMPEND_IN_ACC_ID_NO
     *
     * @mbg.generated
     */
    public String getFcrmpendInAccIdNo() {
        return fcrmpendInAccIdNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_IN_ACC_ID_NO
     *
     * @param fcrmpendInAccIdNo the value for FCRMPEND.FCRMPEND_IN_ACC_ID_NO
     *
     * @mbg.generated
     */
    public void setFcrmpendInAccIdNo(String fcrmpendInAccIdNo) {
        this.fcrmpendInAccIdNo = fcrmpendInAccIdNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_OUT_NAME
     *
     * @return the value of FCRMPEND.FCRMPEND_OUT_NAME
     *
     * @mbg.generated
     */
    public String getFcrmpendOutName() {
        return fcrmpendOutName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_OUT_NAME
     *
     * @param fcrmpendOutName the value for FCRMPEND.FCRMPEND_OUT_NAME
     *
     * @mbg.generated
     */
    public void setFcrmpendOutName(String fcrmpendOutName) {
        this.fcrmpendOutName = fcrmpendOutName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_ADDRESS
     *
     * @return the value of FCRMPEND.FCRMPEND_ADDRESS
     *
     * @mbg.generated
     */
    public String getFcrmpendAddress() {
        return fcrmpendAddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_ADDRESS
     *
     * @param fcrmpendAddress the value for FCRMPEND.FCRMPEND_ADDRESS
     *
     * @mbg.generated
     */
    public void setFcrmpendAddress(String fcrmpendAddress) {
        this.fcrmpendAddress = fcrmpendAddress;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_IN_NAME
     *
     * @return the value of FCRMPEND.FCRMPEND_IN_NAME
     *
     * @mbg.generated
     */
    public String getFcrmpendInName() {
        return fcrmpendInName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_IN_NAME
     *
     * @param fcrmpendInName the value for FCRMPEND.FCRMPEND_IN_NAME
     *
     * @mbg.generated
     */
    public void setFcrmpendInName(String fcrmpendInName) {
        this.fcrmpendInName = fcrmpendInName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_KIND
     *
     * @return the value of FCRMPEND.FCRMPEND_KIND
     *
     * @mbg.generated
     */
    public String getFcrmpendKind() {
        return fcrmpendKind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_KIND
     *
     * @param fcrmpendKind the value for FCRMPEND.FCRMPEND_KIND
     *
     * @mbg.generated
     */
    public void setFcrmpendKind(String fcrmpendKind) {
        this.fcrmpendKind = fcrmpendKind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_ORGKIND
     *
     * @return the value of FCRMPEND.FCRMPEND_ORGKIND
     *
     * @mbg.generated
     */
    public String getFcrmpendOrgkind() {
        return fcrmpendOrgkind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_ORGKIND
     *
     * @param fcrmpendOrgkind the value for FCRMPEND.FCRMPEND_ORGKIND
     *
     * @mbg.generated
     */
    public void setFcrmpendOrgkind(String fcrmpendOrgkind) {
        this.fcrmpendOrgkind = fcrmpendOrgkind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMOEND_CURRENCY
     *
     * @return the value of FCRMPEND.FCRMOEND_CURRENCY
     *
     * @mbg.generated
     */
    public String getFcrmoendCurrency() {
        return fcrmoendCurrency;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMOEND_CURRENCY
     *
     * @param fcrmoendCurrency the value for FCRMPEND.FCRMOEND_CURRENCY
     *
     * @mbg.generated
     */
    public void setFcrmoendCurrency(String fcrmoendCurrency) {
        this.fcrmoendCurrency = fcrmoendCurrency;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMOEND_MEMO
     *
     * @return the value of FCRMPEND.FCRMOEND_MEMO
     *
     * @mbg.generated
     */
    public String getFcrmoendMemo() {
        return fcrmoendMemo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMOEND_MEMO
     *
     * @param fcrmoendMemo the value for FCRMPEND.FCRMOEND_MEMO
     *
     * @mbg.generated
     */
    public void setFcrmoendMemo(String fcrmoendMemo) {
        this.fcrmoendMemo = fcrmoendMemo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.FCRMPEND_MARK
     *
     * @return the value of FCRMPEND.FCRMPEND_MARK
     *
     * @mbg.generated
     */
    public String getFcrmpendMark() {
        return fcrmpendMark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.FCRMPEND_MARK
     *
     * @param fcrmpendMark the value for FCRMPEND.FCRMPEND_MARK
     *
     * @mbg.generated
     */
    public void setFcrmpendMark(String fcrmpendMark) {
        this.fcrmpendMark = fcrmpendMark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.UPDATE_USERID
     *
     * @return the value of FCRMPEND.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.UPDATE_USERID
     *
     * @param updateUserid the value for FCRMPEND.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column FCRMPEND.UPDATE_TIME
     *
     * @return the value of FCRMPEND.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column FCRMPEND.UPDATE_TIME
     *
     * @param updateTime the value for FCRMPEND.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
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
        Fcrmpend other = (Fcrmpend) that;
        return (this.getFcrmpendDate() == null ? other.getFcrmpendDate() == null : this.getFcrmpendDate().equals(other.getFcrmpendDate()))
            && (this.getFcrmpendTime() == null ? other.getFcrmpendTime() == null : this.getFcrmpendTime().equals(other.getFcrmpendTime()))
            && (this.getFcrmpendStan() == null ? other.getFcrmpendStan() == null : this.getFcrmpendStan().equals(other.getFcrmpendStan()))
            && (this.getFcrmpendPcode() == null ? other.getFcrmpendPcode() == null : this.getFcrmpendPcode().equals(other.getFcrmpendPcode()))
            && (this.getFcrmpendSenderBank() == null ? other.getFcrmpendSenderBank() == null : this.getFcrmpendSenderBank().equals(other.getFcrmpendSenderBank()))
            && (this.getFcrmpendReceiverBank() == null ? other.getFcrmpendReceiverBank() == null : this.getFcrmpendReceiverBank().equals(other.getFcrmpendReceiverBank()))
            && (this.getFcrmpendBankNo() == null ? other.getFcrmpendBankNo() == null : this.getFcrmpendBankNo().equals(other.getFcrmpendBankNo()))
            && (this.getFcrmpendTxamt() == null ? other.getFcrmpendTxamt() == null : this.getFcrmpendTxamt().equals(other.getFcrmpendTxamt()))
            && (this.getFcrmpendInAccIdNo() == null ? other.getFcrmpendInAccIdNo() == null : this.getFcrmpendInAccIdNo().equals(other.getFcrmpendInAccIdNo()))
            && (this.getFcrmpendOutName() == null ? other.getFcrmpendOutName() == null : this.getFcrmpendOutName().equals(other.getFcrmpendOutName()))
            && (this.getFcrmpendAddress() == null ? other.getFcrmpendAddress() == null : this.getFcrmpendAddress().equals(other.getFcrmpendAddress()))
            && (this.getFcrmpendInName() == null ? other.getFcrmpendInName() == null : this.getFcrmpendInName().equals(other.getFcrmpendInName()))
            && (this.getFcrmpendKind() == null ? other.getFcrmpendKind() == null : this.getFcrmpendKind().equals(other.getFcrmpendKind()))
            && (this.getFcrmpendOrgkind() == null ? other.getFcrmpendOrgkind() == null : this.getFcrmpendOrgkind().equals(other.getFcrmpendOrgkind()))
            && (this.getFcrmoendCurrency() == null ? other.getFcrmoendCurrency() == null : this.getFcrmoendCurrency().equals(other.getFcrmoendCurrency()))
            && (this.getFcrmoendMemo() == null ? other.getFcrmoendMemo() == null : this.getFcrmoendMemo().equals(other.getFcrmoendMemo()))
            && (this.getFcrmpendMark() == null ? other.getFcrmpendMark() == null : this.getFcrmpendMark().equals(other.getFcrmpendMark()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFcrmpendDate() == null) ? 0 : getFcrmpendDate().hashCode());
        result = prime * result + ((getFcrmpendTime() == null) ? 0 : getFcrmpendTime().hashCode());
        result = prime * result + ((getFcrmpendStan() == null) ? 0 : getFcrmpendStan().hashCode());
        result = prime * result + ((getFcrmpendPcode() == null) ? 0 : getFcrmpendPcode().hashCode());
        result = prime * result + ((getFcrmpendSenderBank() == null) ? 0 : getFcrmpendSenderBank().hashCode());
        result = prime * result + ((getFcrmpendReceiverBank() == null) ? 0 : getFcrmpendReceiverBank().hashCode());
        result = prime * result + ((getFcrmpendBankNo() == null) ? 0 : getFcrmpendBankNo().hashCode());
        result = prime * result + ((getFcrmpendTxamt() == null) ? 0 : getFcrmpendTxamt().hashCode());
        result = prime * result + ((getFcrmpendInAccIdNo() == null) ? 0 : getFcrmpendInAccIdNo().hashCode());
        result = prime * result + ((getFcrmpendOutName() == null) ? 0 : getFcrmpendOutName().hashCode());
        result = prime * result + ((getFcrmpendAddress() == null) ? 0 : getFcrmpendAddress().hashCode());
        result = prime * result + ((getFcrmpendInName() == null) ? 0 : getFcrmpendInName().hashCode());
        result = prime * result + ((getFcrmpendKind() == null) ? 0 : getFcrmpendKind().hashCode());
        result = prime * result + ((getFcrmpendOrgkind() == null) ? 0 : getFcrmpendOrgkind().hashCode());
        result = prime * result + ((getFcrmoendCurrency() == null) ? 0 : getFcrmoendCurrency().hashCode());
        result = prime * result + ((getFcrmoendMemo() == null) ? 0 : getFcrmoendMemo().hashCode());
        result = prime * result + ((getFcrmpendMark() == null) ? 0 : getFcrmpendMark().hashCode());
        result = prime * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", fcrmpendDate=").append(fcrmpendDate);
        sb.append(", fcrmpendTime=").append(fcrmpendTime);
        sb.append(", fcrmpendStan=").append(fcrmpendStan);
        sb.append(", fcrmpendPcode=").append(fcrmpendPcode);
        sb.append(", fcrmpendSenderBank=").append(fcrmpendSenderBank);
        sb.append(", fcrmpendReceiverBank=").append(fcrmpendReceiverBank);
        sb.append(", fcrmpendBankNo=").append(fcrmpendBankNo);
        sb.append(", fcrmpendTxamt=").append(fcrmpendTxamt);
        sb.append(", fcrmpendInAccIdNo=").append(fcrmpendInAccIdNo);
        sb.append(", fcrmpendOutName=").append(fcrmpendOutName);
        sb.append(", fcrmpendAddress=").append(fcrmpendAddress);
        sb.append(", fcrmpendInName=").append(fcrmpendInName);
        sb.append(", fcrmpendKind=").append(fcrmpendKind);
        sb.append(", fcrmpendOrgkind=").append(fcrmpendOrgkind);
        sb.append(", fcrmoendCurrency=").append(fcrmoendCurrency);
        sb.append(", fcrmoendMemo=").append(fcrmoendMemo);
        sb.append(", fcrmpendMark=").append(fcrmpendMark);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table FCRMPEND
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<FCRMPEND_DATE>").append(this.fcrmpendDate).append("</FCRMPEND_DATE>");
        sb.append("<FCRMPEND_TIME>").append(this.fcrmpendTime).append("</FCRMPEND_TIME>");
        sb.append("<FCRMPEND_STAN>").append(this.fcrmpendStan).append("</FCRMPEND_STAN>");
        sb.append("<FCRMPEND_PCODE>").append(this.fcrmpendPcode).append("</FCRMPEND_PCODE>");
        sb.append("<FCRMPEND_SENDER_BANK>").append(this.fcrmpendSenderBank).append("</FCRMPEND_SENDER_BANK>");
        sb.append("<FCRMPEND_RECEIVER_BANK>").append(this.fcrmpendReceiverBank).append("</FCRMPEND_RECEIVER_BANK>");
        sb.append("<FCRMPEND_BANK_NO>").append(this.fcrmpendBankNo).append("</FCRMPEND_BANK_NO>");
        sb.append("<FCRMPEND_TXAMT>").append(this.fcrmpendTxamt).append("</FCRMPEND_TXAMT>");
        sb.append("<FCRMPEND_IN_ACC_ID_NO>").append(this.fcrmpendInAccIdNo).append("</FCRMPEND_IN_ACC_ID_NO>");
        sb.append("<FCRMPEND_OUT_NAME>").append(this.fcrmpendOutName).append("</FCRMPEND_OUT_NAME>");
        sb.append("<FCRMPEND_ADDRESS>").append(this.fcrmpendAddress).append("</FCRMPEND_ADDRESS>");
        sb.append("<FCRMPEND_IN_NAME>").append(this.fcrmpendInName).append("</FCRMPEND_IN_NAME>");
        sb.append("<FCRMPEND_KIND>").append(this.fcrmpendKind).append("</FCRMPEND_KIND>");
        sb.append("<FCRMPEND_ORGKIND>").append(this.fcrmpendOrgkind).append("</FCRMPEND_ORGKIND>");
        sb.append("<FCRMOEND_CURRENCY>").append(this.fcrmoendCurrency).append("</FCRMOEND_CURRENCY>");
        sb.append("<FCRMOEND_MEMO>").append(this.fcrmoendMemo).append("</FCRMOEND_MEMO>");
        sb.append("<FCRMPEND_MARK>").append(this.fcrmpendMark).append("</FCRMPEND_MARK>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}