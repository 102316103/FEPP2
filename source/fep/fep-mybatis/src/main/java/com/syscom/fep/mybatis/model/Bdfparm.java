package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Bdfparm extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BDFPARM.BDFPARM_PROJNO
     *
     * @mbg.generated
     */
    private String bdfparmProjno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BDFPARM.BDFPARM_BRNO
     *
     * @mbg.generated
     */
    private String bdfparmBrno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BDFPARM.BDFPARM_DBACT
     *
     * @mbg.generated
     */
    private String bdfparmDbact = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BDFPARM.BDFPARM_CRACT
     *
     * @mbg.generated
     */
    private String bdfparmCract = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BDFPARM.BDFPARM_FEE_CUSTPAY
     *
     * @mbg.generated
     */
    private BigDecimal bdfparmFeeCustpay;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BDFPARM.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BDFPARM.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table BDFPARM
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BDFPARM
     *
     * @mbg.generated
     */
    public Bdfparm(String bdfparmProjno, String bdfparmBrno, String bdfparmDbact, String bdfparmCract, BigDecimal bdfparmFeeCustpay, Integer updateUserid, Date updateTime) {
        this.bdfparmProjno = bdfparmProjno;
        this.bdfparmBrno = bdfparmBrno;
        this.bdfparmDbact = bdfparmDbact;
        this.bdfparmCract = bdfparmCract;
        this.bdfparmFeeCustpay = bdfparmFeeCustpay;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BDFPARM
     *
     * @mbg.generated
     */
    public Bdfparm() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BDFPARM.BDFPARM_PROJNO
     *
     * @return the value of BDFPARM.BDFPARM_PROJNO
     *
     * @mbg.generated
     */
    public String getBdfparmProjno() {
        return bdfparmProjno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BDFPARM.BDFPARM_PROJNO
     *
     * @param bdfparmProjno the value for BDFPARM.BDFPARM_PROJNO
     *
     * @mbg.generated
     */
    public void setBdfparmProjno(String bdfparmProjno) {
        this.bdfparmProjno = bdfparmProjno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BDFPARM.BDFPARM_BRNO
     *
     * @return the value of BDFPARM.BDFPARM_BRNO
     *
     * @mbg.generated
     */
    public String getBdfparmBrno() {
        return bdfparmBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BDFPARM.BDFPARM_BRNO
     *
     * @param bdfparmBrno the value for BDFPARM.BDFPARM_BRNO
     *
     * @mbg.generated
     */
    public void setBdfparmBrno(String bdfparmBrno) {
        this.bdfparmBrno = bdfparmBrno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BDFPARM.BDFPARM_DBACT
     *
     * @return the value of BDFPARM.BDFPARM_DBACT
     *
     * @mbg.generated
     */
    public String getBdfparmDbact() {
        return bdfparmDbact;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BDFPARM.BDFPARM_DBACT
     *
     * @param bdfparmDbact the value for BDFPARM.BDFPARM_DBACT
     *
     * @mbg.generated
     */
    public void setBdfparmDbact(String bdfparmDbact) {
        this.bdfparmDbact = bdfparmDbact;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BDFPARM.BDFPARM_CRACT
     *
     * @return the value of BDFPARM.BDFPARM_CRACT
     *
     * @mbg.generated
     */
    public String getBdfparmCract() {
        return bdfparmCract;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BDFPARM.BDFPARM_CRACT
     *
     * @param bdfparmCract the value for BDFPARM.BDFPARM_CRACT
     *
     * @mbg.generated
     */
    public void setBdfparmCract(String bdfparmCract) {
        this.bdfparmCract = bdfparmCract;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BDFPARM.BDFPARM_FEE_CUSTPAY
     *
     * @return the value of BDFPARM.BDFPARM_FEE_CUSTPAY
     *
     * @mbg.generated
     */
    public BigDecimal getBdfparmFeeCustpay() {
        return bdfparmFeeCustpay;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BDFPARM.BDFPARM_FEE_CUSTPAY
     *
     * @param bdfparmFeeCustpay the value for BDFPARM.BDFPARM_FEE_CUSTPAY
     *
     * @mbg.generated
     */
    public void setBdfparmFeeCustpay(BigDecimal bdfparmFeeCustpay) {
        this.bdfparmFeeCustpay = bdfparmFeeCustpay;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BDFPARM.UPDATE_USERID
     *
     * @return the value of BDFPARM.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BDFPARM.UPDATE_USERID
     *
     * @param updateUserid the value for BDFPARM.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BDFPARM.UPDATE_TIME
     *
     * @return the value of BDFPARM.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BDFPARM.UPDATE_TIME
     *
     * @param updateTime the value for BDFPARM.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BDFPARM
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", bdfparmProjno=").append(bdfparmProjno);
        sb.append(", bdfparmBrno=").append(bdfparmBrno);
        sb.append(", bdfparmDbact=").append(bdfparmDbact);
        sb.append(", bdfparmCract=").append(bdfparmCract);
        sb.append(", bdfparmFeeCustpay=").append(bdfparmFeeCustpay);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BDFPARM
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
        Bdfparm other = (Bdfparm) that;
        return (this.getBdfparmProjno() == null ? other.getBdfparmProjno() == null : this.getBdfparmProjno().equals(other.getBdfparmProjno()))
            && (this.getBdfparmBrno() == null ? other.getBdfparmBrno() == null : this.getBdfparmBrno().equals(other.getBdfparmBrno()))
            && (this.getBdfparmDbact() == null ? other.getBdfparmDbact() == null : this.getBdfparmDbact().equals(other.getBdfparmDbact()))
            && (this.getBdfparmCract() == null ? other.getBdfparmCract() == null : this.getBdfparmCract().equals(other.getBdfparmCract()))
            && (this.getBdfparmFeeCustpay() == null ? other.getBdfparmFeeCustpay() == null : this.getBdfparmFeeCustpay().equals(other.getBdfparmFeeCustpay()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BDFPARM
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getBdfparmProjno() == null) ? 0 : getBdfparmProjno().hashCode());
        result = 31 * result + ((getBdfparmBrno() == null) ? 0 : getBdfparmBrno().hashCode());
        result = 31 * result + ((getBdfparmDbact() == null) ? 0 : getBdfparmDbact().hashCode());
        result = 31 * result + ((getBdfparmCract() == null) ? 0 : getBdfparmCract().hashCode());
        result = 31 * result + ((getBdfparmFeeCustpay() == null) ? 0 : getBdfparmFeeCustpay().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BDFPARM
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<BDFPARM_PROJNO>").append(this.bdfparmProjno).append("</BDFPARM_PROJNO>");
        sb.append("<BDFPARM_BRNO>").append(this.bdfparmBrno).append("</BDFPARM_BRNO>");
        sb.append("<BDFPARM_DBACT>").append(this.bdfparmDbact).append("</BDFPARM_DBACT>");
        sb.append("<BDFPARM_CRACT>").append(this.bdfparmCract).append("</BDFPARM_CRACT>");
        sb.append("<BDFPARM_FEE_CUSTPAY>").append(this.bdfparmFeeCustpay).append("</BDFPARM_FEE_CUSTPAY>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}