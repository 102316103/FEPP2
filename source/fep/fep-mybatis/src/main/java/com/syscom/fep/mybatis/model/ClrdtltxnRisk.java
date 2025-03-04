package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;

public class ClrdtltxnRisk extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN_RISK.TXDATE
     *
     * @mbg.generated
     */
    private String txdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN_RISK.TIME
     *
     * @mbg.generated
     */
    private String time;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN_RISK.TXAMT
     *
     * @mbg.generated
     */
    private BigDecimal txamt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN_RISK.USE_BAL
     *
     * @mbg.generated
     */
    private BigDecimal useBal;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CLRDTLTXN_RISK.REMARK
     *
     * @mbg.generated
     */
    private String remark;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table CLRDTLTXN_RISK
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN_RISK
     *
     * @mbg.generated
     */
    public ClrdtltxnRisk(String txdate, String time, BigDecimal txamt, BigDecimal useBal, String remark) {
        this.txdate = txdate;
        this.time = time;
        this.txamt = txamt;
        this.useBal = useBal;
        this.remark = remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN_RISK
     *
     * @mbg.generated
     */
    public ClrdtltxnRisk() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN_RISK.TXDATE
     *
     * @return the value of CLRDTLTXN_RISK.TXDATE
     *
     * @mbg.generated
     */
    public String getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN_RISK.TXDATE
     *
     * @param txdate the value for CLRDTLTXN_RISK.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN_RISK.TIME
     *
     * @return the value of CLRDTLTXN_RISK.TIME
     *
     * @mbg.generated
     */
    public String getTime() {
        return time;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN_RISK.TIME
     *
     * @param time the value for CLRDTLTXN_RISK.TIME
     *
     * @mbg.generated
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN_RISK.TXAMT
     *
     * @return the value of CLRDTLTXN_RISK.TXAMT
     *
     * @mbg.generated
     */
    public BigDecimal getTxamt() {
        return txamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN_RISK.TXAMT
     *
     * @param txamt the value for CLRDTLTXN_RISK.TXAMT
     *
     * @mbg.generated
     */
    public void setTxamt(BigDecimal txamt) {
        this.txamt = txamt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN_RISK.USE_BAL
     *
     * @return the value of CLRDTLTXN_RISK.USE_BAL
     *
     * @mbg.generated
     */
    public BigDecimal getUseBal() {
        return useBal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN_RISK.USE_BAL
     *
     * @param useBal the value for CLRDTLTXN_RISK.USE_BAL
     *
     * @mbg.generated
     */
    public void setUseBal(BigDecimal useBal) {
        this.useBal = useBal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CLRDTLTXN_RISK.REMARK
     *
     * @return the value of CLRDTLTXN_RISK.REMARK
     *
     * @mbg.generated
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CLRDTLTXN_RISK.REMARK
     *
     * @param remark the value for CLRDTLTXN_RISK.REMARK
     *
     * @mbg.generated
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN_RISK
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
        sb.append(", time=").append(time);
        sb.append(", txamt=").append(txamt);
        sb.append(", useBal=").append(useBal);
        sb.append(", remark=").append(remark);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN_RISK
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
        ClrdtltxnRisk other = (ClrdtltxnRisk) that;
        return (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getTime() == null ? other.getTime() == null : this.getTime().equals(other.getTime()))
            && (this.getTxamt() == null ? other.getTxamt() == null : this.getTxamt().equals(other.getTxamt()))
            && (this.getUseBal() == null ? other.getUseBal() == null : this.getUseBal().equals(other.getUseBal()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN_RISK
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getTime() == null) ? 0 : getTime().hashCode());
        result = 31 * result + ((getTxamt() == null) ? 0 : getTxamt().hashCode());
        result = 31 * result + ((getUseBal() == null) ? 0 : getUseBal().hashCode());
        result = 31 * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CLRDTLTXN_RISK
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<TIME>").append(this.time).append("</TIME>");
        sb.append("<TXAMT>").append(this.txamt).append("</TXAMT>");
        sb.append("<USE_BAL>").append(this.useBal).append("</USE_BAL>");
        sb.append("<REMARK>").append(this.remark).append("</REMARK>");
        return sb.toString();
    }
}