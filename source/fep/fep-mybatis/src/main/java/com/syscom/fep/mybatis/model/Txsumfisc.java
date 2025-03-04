package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import org.apache.commons.lang3.StringUtils;

public class Txsumfisc extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.TXDATE
     *
     * @mbg.generated
     */
    private String txdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.STAN
     *
     * @mbg.generated
     */
    private String stan = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.EJ
     *
     * @mbg.generated
     */
    private Integer ej;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.TXTIME
     *
     * @mbg.generated
     */
    private String txtime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.TXCD
     *
     * @mbg.generated
     */
    private String txcd;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.TOTALTIME
     *
     * @mbg.generated
     */
    private Integer totaltime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.GWTIME
     *
     * @mbg.generated
     */
    private Integer gwtime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.GWQUEUETIME
     *
     * @mbg.generated
     */
    private Integer gwqueuetime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.SERVICETIME
     *
     * @mbg.generated
     */
    private Integer servicetime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.HANDLERTIME
     *
     * @mbg.generated
     */
    private Integer handlertime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.AATIME
     *
     * @mbg.generated
     */
    private Integer aatime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.T24TIME
     *
     * @mbg.generated
     */
    private Integer t24time;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.CREDITTIME
     *
     * @mbg.generated
     */
    private Integer credittime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.UNISYSTIME
     *
     * @mbg.generated
     */
    private Integer unisystime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.TOTALDESTIME
     *
     * @mbg.generated
     */
    private Integer totaldestime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TXSUMFISC.TOTALDESCOUNT
     *
     * @mbg.generated
     */
    private Integer totaldescount;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table TXSUMFISC
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXSUMFISC
     *
     * @mbg.generated
     */
    public Txsumfisc(String txdate, String stan, Integer ej, String txtime, String txcd, Integer totaltime, Integer gwtime, Integer gwqueuetime, Integer servicetime, Integer handlertime, Integer aatime, Integer t24time, Integer credittime, Integer unisystime, Integer totaldestime, Integer totaldescount) {
        this.txdate = txdate;
        this.stan = stan;
        this.ej = ej;
        this.txtime = txtime;
        this.txcd = txcd;
        this.totaltime = totaltime;
        this.gwtime = gwtime;
        this.gwqueuetime = gwqueuetime;
        this.servicetime = servicetime;
        this.handlertime = handlertime;
        this.aatime = aatime;
        this.t24time = t24time;
        this.credittime = credittime;
        this.unisystime = unisystime;
        this.totaldestime = totaldestime;
        this.totaldescount = totaldescount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXSUMFISC
     *
     * @mbg.generated
     */
    public Txsumfisc() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.TXDATE
     *
     * @return the value of TXSUMFISC.TXDATE
     *
     * @mbg.generated
     */
    public String getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.TXDATE
     *
     * @param txdate the value for TXSUMFISC.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.STAN
     *
     * @return the value of TXSUMFISC.STAN
     *
     * @mbg.generated
     */
    public String getStan() {
        return stan;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.STAN
     *
     * @param stan the value for TXSUMFISC.STAN
     *
     * @mbg.generated
     */
    public void setStan(String stan) {
        this.stan = stan;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.EJ
     *
     * @return the value of TXSUMFISC.EJ
     *
     * @mbg.generated
     */
    public Integer getEj() {
        return ej;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.EJ
     *
     * @param ej the value for TXSUMFISC.EJ
     *
     * @mbg.generated
     */
    public void setEj(Integer ej) {
        this.ej = ej;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.TXTIME
     *
     * @return the value of TXSUMFISC.TXTIME
     *
     * @mbg.generated
     */
    public String getTxtime() {
        return txtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.TXTIME
     *
     * @param txtime the value for TXSUMFISC.TXTIME
     *
     * @mbg.generated
     */
    public void setTxtime(String txtime) {
        this.txtime = txtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.TXCD
     *
     * @return the value of TXSUMFISC.TXCD
     *
     * @mbg.generated
     */
    public String getTxcd() {
        return txcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.TXCD
     *
     * @param txcd the value for TXSUMFISC.TXCD
     *
     * @mbg.generated
     */
    public void setTxcd(String txcd) {
        this.txcd = txcd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.TOTALTIME
     *
     * @return the value of TXSUMFISC.TOTALTIME
     *
     * @mbg.generated
     */
    public Integer getTotaltime() {
        return totaltime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.TOTALTIME
     *
     * @param totaltime the value for TXSUMFISC.TOTALTIME
     *
     * @mbg.generated
     */
    public void setTotaltime(Integer totaltime) {
        this.totaltime = totaltime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.GWTIME
     *
     * @return the value of TXSUMFISC.GWTIME
     *
     * @mbg.generated
     */
    public Integer getGwtime() {
        return gwtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.GWTIME
     *
     * @param gwtime the value for TXSUMFISC.GWTIME
     *
     * @mbg.generated
     */
    public void setGwtime(Integer gwtime) {
        this.gwtime = gwtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.GWQUEUETIME
     *
     * @return the value of TXSUMFISC.GWQUEUETIME
     *
     * @mbg.generated
     */
    public Integer getGwqueuetime() {
        return gwqueuetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.GWQUEUETIME
     *
     * @param gwqueuetime the value for TXSUMFISC.GWQUEUETIME
     *
     * @mbg.generated
     */
    public void setGwqueuetime(Integer gwqueuetime) {
        this.gwqueuetime = gwqueuetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.SERVICETIME
     *
     * @return the value of TXSUMFISC.SERVICETIME
     *
     * @mbg.generated
     */
    public Integer getServicetime() {
        return servicetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.SERVICETIME
     *
     * @param servicetime the value for TXSUMFISC.SERVICETIME
     *
     * @mbg.generated
     */
    public void setServicetime(Integer servicetime) {
        this.servicetime = servicetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.HANDLERTIME
     *
     * @return the value of TXSUMFISC.HANDLERTIME
     *
     * @mbg.generated
     */
    public Integer getHandlertime() {
        return handlertime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.HANDLERTIME
     *
     * @param handlertime the value for TXSUMFISC.HANDLERTIME
     *
     * @mbg.generated
     */
    public void setHandlertime(Integer handlertime) {
        this.handlertime = handlertime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.AATIME
     *
     * @return the value of TXSUMFISC.AATIME
     *
     * @mbg.generated
     */
    public Integer getAatime() {
        return aatime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.AATIME
     *
     * @param aatime the value for TXSUMFISC.AATIME
     *
     * @mbg.generated
     */
    public void setAatime(Integer aatime) {
        this.aatime = aatime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.T24TIME
     *
     * @return the value of TXSUMFISC.T24TIME
     *
     * @mbg.generated
     */
    public Integer getT24time() {
        return t24time;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.T24TIME
     *
     * @param t24time the value for TXSUMFISC.T24TIME
     *
     * @mbg.generated
     */
    public void setT24time(Integer t24time) {
        this.t24time = t24time;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.CREDITTIME
     *
     * @return the value of TXSUMFISC.CREDITTIME
     *
     * @mbg.generated
     */
    public Integer getCredittime() {
        return credittime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.CREDITTIME
     *
     * @param credittime the value for TXSUMFISC.CREDITTIME
     *
     * @mbg.generated
     */
    public void setCredittime(Integer credittime) {
        this.credittime = credittime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.UNISYSTIME
     *
     * @return the value of TXSUMFISC.UNISYSTIME
     *
     * @mbg.generated
     */
    public Integer getUnisystime() {
        return unisystime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.UNISYSTIME
     *
     * @param unisystime the value for TXSUMFISC.UNISYSTIME
     *
     * @mbg.generated
     */
    public void setUnisystime(Integer unisystime) {
        this.unisystime = unisystime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.TOTALDESTIME
     *
     * @return the value of TXSUMFISC.TOTALDESTIME
     *
     * @mbg.generated
     */
    public Integer getTotaldestime() {
        return totaldestime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.TOTALDESTIME
     *
     * @param totaldestime the value for TXSUMFISC.TOTALDESTIME
     *
     * @mbg.generated
     */
    public void setTotaldestime(Integer totaldestime) {
        this.totaldestime = totaldestime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TXSUMFISC.TOTALDESCOUNT
     *
     * @return the value of TXSUMFISC.TOTALDESCOUNT
     *
     * @mbg.generated
     */
    public Integer getTotaldescount() {
        return totaldescount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TXSUMFISC.TOTALDESCOUNT
     *
     * @param totaldescount the value for TXSUMFISC.TOTALDESCOUNT
     *
     * @mbg.generated
     */
    public void setTotaldescount(Integer totaldescount) {
        this.totaldescount = totaldescount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXSUMFISC
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
        sb.append(", stan=").append(stan);
        sb.append(", ej=").append(ej);
        sb.append(", txtime=").append(txtime);
        sb.append(", txcd=").append(txcd);
        sb.append(", totaltime=").append(totaltime);
        sb.append(", gwtime=").append(gwtime);
        sb.append(", gwqueuetime=").append(gwqueuetime);
        sb.append(", servicetime=").append(servicetime);
        sb.append(", handlertime=").append(handlertime);
        sb.append(", aatime=").append(aatime);
        sb.append(", t24time=").append(t24time);
        sb.append(", credittime=").append(credittime);
        sb.append(", unisystime=").append(unisystime);
        sb.append(", totaldestime=").append(totaldestime);
        sb.append(", totaldescount=").append(totaldescount);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXSUMFISC
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
        Txsumfisc other = (Txsumfisc) that;
        return (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getStan() == null ? other.getStan() == null : this.getStan().equals(other.getStan()))
            && (this.getEj() == null ? other.getEj() == null : this.getEj().equals(other.getEj()))
            && (this.getTxtime() == null ? other.getTxtime() == null : this.getTxtime().equals(other.getTxtime()))
            && (this.getTxcd() == null ? other.getTxcd() == null : this.getTxcd().equals(other.getTxcd()))
            && (this.getTotaltime() == null ? other.getTotaltime() == null : this.getTotaltime().equals(other.getTotaltime()))
            && (this.getGwtime() == null ? other.getGwtime() == null : this.getGwtime().equals(other.getGwtime()))
            && (this.getGwqueuetime() == null ? other.getGwqueuetime() == null : this.getGwqueuetime().equals(other.getGwqueuetime()))
            && (this.getServicetime() == null ? other.getServicetime() == null : this.getServicetime().equals(other.getServicetime()))
            && (this.getHandlertime() == null ? other.getHandlertime() == null : this.getHandlertime().equals(other.getHandlertime()))
            && (this.getAatime() == null ? other.getAatime() == null : this.getAatime().equals(other.getAatime()))
            && (this.getT24time() == null ? other.getT24time() == null : this.getT24time().equals(other.getT24time()))
            && (this.getCredittime() == null ? other.getCredittime() == null : this.getCredittime().equals(other.getCredittime()))
            && (this.getUnisystime() == null ? other.getUnisystime() == null : this.getUnisystime().equals(other.getUnisystime()))
            && (this.getTotaldestime() == null ? other.getTotaldestime() == null : this.getTotaldestime().equals(other.getTotaldestime()))
            && (this.getTotaldescount() == null ? other.getTotaldescount() == null : this.getTotaldescount().equals(other.getTotaldescount()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXSUMFISC
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getStan() == null) ? 0 : getStan().hashCode());
        result = 31 * result + ((getEj() == null) ? 0 : getEj().hashCode());
        result = 31 * result + ((getTxtime() == null) ? 0 : getTxtime().hashCode());
        result = 31 * result + ((getTxcd() == null) ? 0 : getTxcd().hashCode());
        result = 31 * result + ((getTotaltime() == null) ? 0 : getTotaltime().hashCode());
        result = 31 * result + ((getGwtime() == null) ? 0 : getGwtime().hashCode());
        result = 31 * result + ((getGwqueuetime() == null) ? 0 : getGwqueuetime().hashCode());
        result = 31 * result + ((getServicetime() == null) ? 0 : getServicetime().hashCode());
        result = 31 * result + ((getHandlertime() == null) ? 0 : getHandlertime().hashCode());
        result = 31 * result + ((getAatime() == null) ? 0 : getAatime().hashCode());
        result = 31 * result + ((getT24time() == null) ? 0 : getT24time().hashCode());
        result = 31 * result + ((getCredittime() == null) ? 0 : getCredittime().hashCode());
        result = 31 * result + ((getUnisystime() == null) ? 0 : getUnisystime().hashCode());
        result = 31 * result + ((getTotaldestime() == null) ? 0 : getTotaldestime().hashCode());
        result = 31 * result + ((getTotaldescount() == null) ? 0 : getTotaldescount().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TXSUMFISC
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<STAN>").append(this.stan).append("</STAN>");
        sb.append("<EJ>").append(this.ej).append("</EJ>");
        sb.append("<TXTIME>").append(this.txtime).append("</TXTIME>");
        sb.append("<TXCD>").append(this.txcd).append("</TXCD>");
        sb.append("<TOTALTIME>").append(this.totaltime).append("</TOTALTIME>");
        sb.append("<GWTIME>").append(this.gwtime).append("</GWTIME>");
        sb.append("<GWQUEUETIME>").append(this.gwqueuetime).append("</GWQUEUETIME>");
        sb.append("<SERVICETIME>").append(this.servicetime).append("</SERVICETIME>");
        sb.append("<HANDLERTIME>").append(this.handlertime).append("</HANDLERTIME>");
        sb.append("<AATIME>").append(this.aatime).append("</AATIME>");
        sb.append("<T24TIME>").append(this.t24time).append("</T24TIME>");
        sb.append("<CREDITTIME>").append(this.credittime).append("</CREDITTIME>");
        sb.append("<UNISYSTIME>").append(this.unisystime).append("</UNISYSTIME>");
        sb.append("<TOTALDESTIME>").append(this.totaldestime).append("</TOTALDESTIME>");
        sb.append("<TOTALDESCOUNT>").append(this.totaldescount).append("</TOTALDESCOUNT>");
        return sb.toString();
    }
}