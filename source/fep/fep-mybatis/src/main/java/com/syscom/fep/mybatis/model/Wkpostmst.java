package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Wkpostmst extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WKPOSTMST.SYSCODE
     *
     * @mbg.generated
     */
    private String syscode = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WKPOSTMST.TXDATE
     *
     * @mbg.generated
     */
    private Date txdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WKPOSTMST.TXRECCOUNT
     *
     * @mbg.generated
     */
    private BigDecimal txreccount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WKPOSTMST.TXDRAMT
     *
     * @mbg.generated
     */
    private BigDecimal txdramt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WKPOSTMST.TXCRAMT
     *
     * @mbg.generated
     */
    private BigDecimal txcramt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WKPOSTMST.UPDUSERID
     *
     * @mbg.generated
     */
    private String upduserid = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column WKPOSTMST.UPDDATETIME
     *
     * @mbg.generated
     */
    private Date upddatetime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table WKPOSTMST
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WKPOSTMST
     *
     * @mbg.generated
     */
    public Wkpostmst(String syscode, Date txdate, BigDecimal txreccount, BigDecimal txdramt, BigDecimal txcramt, String upduserid, Date upddatetime) {
        this.syscode = syscode;
        this.txdate = txdate;
        this.txreccount = txreccount;
        this.txdramt = txdramt;
        this.txcramt = txcramt;
        this.upduserid = upduserid;
        this.upddatetime = upddatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WKPOSTMST
     *
     * @mbg.generated
     */
    public Wkpostmst() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WKPOSTMST.SYSCODE
     *
     * @return the value of WKPOSTMST.SYSCODE
     *
     * @mbg.generated
     */
    public String getSyscode() {
        return syscode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WKPOSTMST.SYSCODE
     *
     * @param syscode the value for WKPOSTMST.SYSCODE
     *
     * @mbg.generated
     */
    public void setSyscode(String syscode) {
        this.syscode = syscode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WKPOSTMST.TXDATE
     *
     * @return the value of WKPOSTMST.TXDATE
     *
     * @mbg.generated
     */
    public Date getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WKPOSTMST.TXDATE
     *
     * @param txdate the value for WKPOSTMST.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(Date txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WKPOSTMST.TXRECCOUNT
     *
     * @return the value of WKPOSTMST.TXRECCOUNT
     *
     * @mbg.generated
     */
    public BigDecimal getTxreccount() {
        return txreccount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WKPOSTMST.TXRECCOUNT
     *
     * @param txreccount the value for WKPOSTMST.TXRECCOUNT
     *
     * @mbg.generated
     */
    public void setTxreccount(BigDecimal txreccount) {
        this.txreccount = txreccount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WKPOSTMST.TXDRAMT
     *
     * @return the value of WKPOSTMST.TXDRAMT
     *
     * @mbg.generated
     */
    public BigDecimal getTxdramt() {
        return txdramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WKPOSTMST.TXDRAMT
     *
     * @param txdramt the value for WKPOSTMST.TXDRAMT
     *
     * @mbg.generated
     */
    public void setTxdramt(BigDecimal txdramt) {
        this.txdramt = txdramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WKPOSTMST.TXCRAMT
     *
     * @return the value of WKPOSTMST.TXCRAMT
     *
     * @mbg.generated
     */
    public BigDecimal getTxcramt() {
        return txcramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WKPOSTMST.TXCRAMT
     *
     * @param txcramt the value for WKPOSTMST.TXCRAMT
     *
     * @mbg.generated
     */
    public void setTxcramt(BigDecimal txcramt) {
        this.txcramt = txcramt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WKPOSTMST.UPDUSERID
     *
     * @return the value of WKPOSTMST.UPDUSERID
     *
     * @mbg.generated
     */
    public String getUpduserid() {
        return upduserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WKPOSTMST.UPDUSERID
     *
     * @param upduserid the value for WKPOSTMST.UPDUSERID
     *
     * @mbg.generated
     */
    public void setUpduserid(String upduserid) {
        this.upduserid = upduserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column WKPOSTMST.UPDDATETIME
     *
     * @return the value of WKPOSTMST.UPDDATETIME
     *
     * @mbg.generated
     */
    public Date getUpddatetime() {
        return upddatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column WKPOSTMST.UPDDATETIME
     *
     * @param upddatetime the value for WKPOSTMST.UPDDATETIME
     *
     * @mbg.generated
     */
    public void setUpddatetime(Date upddatetime) {
        this.upddatetime = upddatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WKPOSTMST
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", syscode=").append(syscode);
        sb.append(", txdate=").append(txdate);
        sb.append(", txreccount=").append(txreccount);
        sb.append(", txdramt=").append(txdramt);
        sb.append(", txcramt=").append(txcramt);
        sb.append(", upduserid=").append(upduserid);
        sb.append(", upddatetime=").append(upddatetime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WKPOSTMST
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
        Wkpostmst other = (Wkpostmst) that;
        return (this.getSyscode() == null ? other.getSyscode() == null : this.getSyscode().equals(other.getSyscode()))
            && (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getTxreccount() == null ? other.getTxreccount() == null : this.getTxreccount().equals(other.getTxreccount()))
            && (this.getTxdramt() == null ? other.getTxdramt() == null : this.getTxdramt().equals(other.getTxdramt()))
            && (this.getTxcramt() == null ? other.getTxcramt() == null : this.getTxcramt().equals(other.getTxcramt()))
            && (this.getUpduserid() == null ? other.getUpduserid() == null : this.getUpduserid().equals(other.getUpduserid()))
            && (this.getUpddatetime() == null ? other.getUpddatetime() == null : this.getUpddatetime().equals(other.getUpddatetime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WKPOSTMST
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getSyscode() == null) ? 0 : getSyscode().hashCode());
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getTxreccount() == null) ? 0 : getTxreccount().hashCode());
        result = 31 * result + ((getTxdramt() == null) ? 0 : getTxdramt().hashCode());
        result = 31 * result + ((getTxcramt() == null) ? 0 : getTxcramt().hashCode());
        result = 31 * result + ((getUpduserid() == null) ? 0 : getUpduserid().hashCode());
        result = 31 * result + ((getUpddatetime() == null) ? 0 : getUpddatetime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table WKPOSTMST
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<SYSCODE>").append(this.syscode).append("</SYSCODE>");
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<TXRECCOUNT>").append(this.txreccount).append("</TXRECCOUNT>");
        sb.append("<TXDRAMT>").append(this.txdramt).append("</TXDRAMT>");
        sb.append("<TXCRAMT>").append(this.txcramt).append("</TXCRAMT>");
        sb.append("<UPDUSERID>").append(this.upduserid).append("</UPDUSERID>");
        sb.append("<UPDDATETIME>").append(this.upddatetime).append("</UPDDATETIME>");
        return sb.toString();
    }
}