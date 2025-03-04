package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;

public class Listratelog extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_ID
     *
     * @mbg.generated
     */
    private Integer listratelogId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_ZONE_CODE
     *
     * @mbg.generated
     */
    private String listratelogZoneCode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_PAIRNAME
     *
     * @mbg.generated
     */
    private String listratelogPairname;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_COSTBUY
     *
     * @mbg.generated
     */
    private BigDecimal listratelogCostbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_COSTSELL
     *
     * @mbg.generated
     */
    private BigDecimal listratelogCostsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_LISTBUY
     *
     * @mbg.generated
     */
    private BigDecimal listratelogListbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_LISTSELL
     *
     * @mbg.generated
     */
    private BigDecimal listratelogListsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_NOTEBUY
     *
     * @mbg.generated
     */
    private BigDecimal listratelogNotebuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_NOTESELL
     *
     * @mbg.generated
     */
    private BigDecimal listratelogNotesell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_EMPBUY
     *
     * @mbg.generated
     */
    private BigDecimal listratelogEmpbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_EMPSELL
     *
     * @mbg.generated
     */
    private BigDecimal listratelogEmpsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_NOTEMPBUY
     *
     * @mbg.generated
     */
    private BigDecimal listratelogNotempbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_NOTEMPSELL
     *
     * @mbg.generated
     */
    private BigDecimal listratelogNotempsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.LISTRATELOG_MDFLAG
     *
     * @mbg.generated
     */
    private String listratelogMdflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATELOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table LISTRATELOG
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATELOG
     *
     * @mbg.generated
     */
    public Listratelog(Integer listratelogId, String listratelogZoneCode, String listratelogPairname, BigDecimal listratelogCostbuy, BigDecimal listratelogCostsell, BigDecimal listratelogListbuy, BigDecimal listratelogListsell, BigDecimal listratelogNotebuy, BigDecimal listratelogNotesell, BigDecimal listratelogEmpbuy, BigDecimal listratelogEmpsell, BigDecimal listratelogNotempbuy, BigDecimal listratelogNotempsell, String listratelogMdflag, Integer updateUserid, Date updateTime) {
        this.listratelogId = listratelogId;
        this.listratelogZoneCode = listratelogZoneCode;
        this.listratelogPairname = listratelogPairname;
        this.listratelogCostbuy = listratelogCostbuy;
        this.listratelogCostsell = listratelogCostsell;
        this.listratelogListbuy = listratelogListbuy;
        this.listratelogListsell = listratelogListsell;
        this.listratelogNotebuy = listratelogNotebuy;
        this.listratelogNotesell = listratelogNotesell;
        this.listratelogEmpbuy = listratelogEmpbuy;
        this.listratelogEmpsell = listratelogEmpsell;
        this.listratelogNotempbuy = listratelogNotempbuy;
        this.listratelogNotempsell = listratelogNotempsell;
        this.listratelogMdflag = listratelogMdflag;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATELOG
     *
     * @mbg.generated
     */
    public Listratelog() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_ID
     *
     * @return the value of LISTRATELOG.LISTRATELOG_ID
     *
     * @mbg.generated
     */
    public Integer getListratelogId() {
        return listratelogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_ID
     *
     * @param listratelogId the value for LISTRATELOG.LISTRATELOG_ID
     *
     * @mbg.generated
     */
    public void setListratelogId(Integer listratelogId) {
        this.listratelogId = listratelogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_ZONE_CODE
     *
     * @return the value of LISTRATELOG.LISTRATELOG_ZONE_CODE
     *
     * @mbg.generated
     */
    public String getListratelogZoneCode() {
        return listratelogZoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_ZONE_CODE
     *
     * @param listratelogZoneCode the value for LISTRATELOG.LISTRATELOG_ZONE_CODE
     *
     * @mbg.generated
     */
    public void setListratelogZoneCode(String listratelogZoneCode) {
        this.listratelogZoneCode = listratelogZoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_PAIRNAME
     *
     * @return the value of LISTRATELOG.LISTRATELOG_PAIRNAME
     *
     * @mbg.generated
     */
    public String getListratelogPairname() {
        return listratelogPairname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_PAIRNAME
     *
     * @param listratelogPairname the value for LISTRATELOG.LISTRATELOG_PAIRNAME
     *
     * @mbg.generated
     */
    public void setListratelogPairname(String listratelogPairname) {
        this.listratelogPairname = listratelogPairname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_COSTBUY
     *
     * @return the value of LISTRATELOG.LISTRATELOG_COSTBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogCostbuy() {
        return listratelogCostbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_COSTBUY
     *
     * @param listratelogCostbuy the value for LISTRATELOG.LISTRATELOG_COSTBUY
     *
     * @mbg.generated
     */
    public void setListratelogCostbuy(BigDecimal listratelogCostbuy) {
        this.listratelogCostbuy = listratelogCostbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_COSTSELL
     *
     * @return the value of LISTRATELOG.LISTRATELOG_COSTSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogCostsell() {
        return listratelogCostsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_COSTSELL
     *
     * @param listratelogCostsell the value for LISTRATELOG.LISTRATELOG_COSTSELL
     *
     * @mbg.generated
     */
    public void setListratelogCostsell(BigDecimal listratelogCostsell) {
        this.listratelogCostsell = listratelogCostsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_LISTBUY
     *
     * @return the value of LISTRATELOG.LISTRATELOG_LISTBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogListbuy() {
        return listratelogListbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_LISTBUY
     *
     * @param listratelogListbuy the value for LISTRATELOG.LISTRATELOG_LISTBUY
     *
     * @mbg.generated
     */
    public void setListratelogListbuy(BigDecimal listratelogListbuy) {
        this.listratelogListbuy = listratelogListbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_LISTSELL
     *
     * @return the value of LISTRATELOG.LISTRATELOG_LISTSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogListsell() {
        return listratelogListsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_LISTSELL
     *
     * @param listratelogListsell the value for LISTRATELOG.LISTRATELOG_LISTSELL
     *
     * @mbg.generated
     */
    public void setListratelogListsell(BigDecimal listratelogListsell) {
        this.listratelogListsell = listratelogListsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_NOTEBUY
     *
     * @return the value of LISTRATELOG.LISTRATELOG_NOTEBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogNotebuy() {
        return listratelogNotebuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_NOTEBUY
     *
     * @param listratelogNotebuy the value for LISTRATELOG.LISTRATELOG_NOTEBUY
     *
     * @mbg.generated
     */
    public void setListratelogNotebuy(BigDecimal listratelogNotebuy) {
        this.listratelogNotebuy = listratelogNotebuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_NOTESELL
     *
     * @return the value of LISTRATELOG.LISTRATELOG_NOTESELL
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogNotesell() {
        return listratelogNotesell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_NOTESELL
     *
     * @param listratelogNotesell the value for LISTRATELOG.LISTRATELOG_NOTESELL
     *
     * @mbg.generated
     */
    public void setListratelogNotesell(BigDecimal listratelogNotesell) {
        this.listratelogNotesell = listratelogNotesell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_EMPBUY
     *
     * @return the value of LISTRATELOG.LISTRATELOG_EMPBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogEmpbuy() {
        return listratelogEmpbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_EMPBUY
     *
     * @param listratelogEmpbuy the value for LISTRATELOG.LISTRATELOG_EMPBUY
     *
     * @mbg.generated
     */
    public void setListratelogEmpbuy(BigDecimal listratelogEmpbuy) {
        this.listratelogEmpbuy = listratelogEmpbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_EMPSELL
     *
     * @return the value of LISTRATELOG.LISTRATELOG_EMPSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogEmpsell() {
        return listratelogEmpsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_EMPSELL
     *
     * @param listratelogEmpsell the value for LISTRATELOG.LISTRATELOG_EMPSELL
     *
     * @mbg.generated
     */
    public void setListratelogEmpsell(BigDecimal listratelogEmpsell) {
        this.listratelogEmpsell = listratelogEmpsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_NOTEMPBUY
     *
     * @return the value of LISTRATELOG.LISTRATELOG_NOTEMPBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogNotempbuy() {
        return listratelogNotempbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_NOTEMPBUY
     *
     * @param listratelogNotempbuy the value for LISTRATELOG.LISTRATELOG_NOTEMPBUY
     *
     * @mbg.generated
     */
    public void setListratelogNotempbuy(BigDecimal listratelogNotempbuy) {
        this.listratelogNotempbuy = listratelogNotempbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_NOTEMPSELL
     *
     * @return the value of LISTRATELOG.LISTRATELOG_NOTEMPSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListratelogNotempsell() {
        return listratelogNotempsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_NOTEMPSELL
     *
     * @param listratelogNotempsell the value for LISTRATELOG.LISTRATELOG_NOTEMPSELL
     *
     * @mbg.generated
     */
    public void setListratelogNotempsell(BigDecimal listratelogNotempsell) {
        this.listratelogNotempsell = listratelogNotempsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.LISTRATELOG_MDFLAG
     *
     * @return the value of LISTRATELOG.LISTRATELOG_MDFLAG
     *
     * @mbg.generated
     */
    public String getListratelogMdflag() {
        return listratelogMdflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.LISTRATELOG_MDFLAG
     *
     * @param listratelogMdflag the value for LISTRATELOG.LISTRATELOG_MDFLAG
     *
     * @mbg.generated
     */
    public void setListratelogMdflag(String listratelogMdflag) {
        this.listratelogMdflag = listratelogMdflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.UPDATE_USERID
     *
     * @return the value of LISTRATELOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.UPDATE_USERID
     *
     * @param updateUserid the value for LISTRATELOG.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATELOG.UPDATE_TIME
     *
     * @return the value of LISTRATELOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATELOG.UPDATE_TIME
     *
     * @param updateTime the value for LISTRATELOG.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATELOG
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", listratelogId=").append(listratelogId);
        sb.append(", listratelogZoneCode=").append(listratelogZoneCode);
        sb.append(", listratelogPairname=").append(listratelogPairname);
        sb.append(", listratelogCostbuy=").append(listratelogCostbuy);
        sb.append(", listratelogCostsell=").append(listratelogCostsell);
        sb.append(", listratelogListbuy=").append(listratelogListbuy);
        sb.append(", listratelogListsell=").append(listratelogListsell);
        sb.append(", listratelogNotebuy=").append(listratelogNotebuy);
        sb.append(", listratelogNotesell=").append(listratelogNotesell);
        sb.append(", listratelogEmpbuy=").append(listratelogEmpbuy);
        sb.append(", listratelogEmpsell=").append(listratelogEmpsell);
        sb.append(", listratelogNotempbuy=").append(listratelogNotempbuy);
        sb.append(", listratelogNotempsell=").append(listratelogNotempsell);
        sb.append(", listratelogMdflag=").append(listratelogMdflag);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATELOG
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
        Listratelog other = (Listratelog) that;
        return (this.getListratelogId() == null ? other.getListratelogId() == null : this.getListratelogId().equals(other.getListratelogId()))
            && (this.getListratelogZoneCode() == null ? other.getListratelogZoneCode() == null : this.getListratelogZoneCode().equals(other.getListratelogZoneCode()))
            && (this.getListratelogPairname() == null ? other.getListratelogPairname() == null : this.getListratelogPairname().equals(other.getListratelogPairname()))
            && (this.getListratelogCostbuy() == null ? other.getListratelogCostbuy() == null : this.getListratelogCostbuy().equals(other.getListratelogCostbuy()))
            && (this.getListratelogCostsell() == null ? other.getListratelogCostsell() == null : this.getListratelogCostsell().equals(other.getListratelogCostsell()))
            && (this.getListratelogListbuy() == null ? other.getListratelogListbuy() == null : this.getListratelogListbuy().equals(other.getListratelogListbuy()))
            && (this.getListratelogListsell() == null ? other.getListratelogListsell() == null : this.getListratelogListsell().equals(other.getListratelogListsell()))
            && (this.getListratelogNotebuy() == null ? other.getListratelogNotebuy() == null : this.getListratelogNotebuy().equals(other.getListratelogNotebuy()))
            && (this.getListratelogNotesell() == null ? other.getListratelogNotesell() == null : this.getListratelogNotesell().equals(other.getListratelogNotesell()))
            && (this.getListratelogEmpbuy() == null ? other.getListratelogEmpbuy() == null : this.getListratelogEmpbuy().equals(other.getListratelogEmpbuy()))
            && (this.getListratelogEmpsell() == null ? other.getListratelogEmpsell() == null : this.getListratelogEmpsell().equals(other.getListratelogEmpsell()))
            && (this.getListratelogNotempbuy() == null ? other.getListratelogNotempbuy() == null : this.getListratelogNotempbuy().equals(other.getListratelogNotempbuy()))
            && (this.getListratelogNotempsell() == null ? other.getListratelogNotempsell() == null : this.getListratelogNotempsell().equals(other.getListratelogNotempsell()))
            && (this.getListratelogMdflag() == null ? other.getListratelogMdflag() == null : this.getListratelogMdflag().equals(other.getListratelogMdflag()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATELOG
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getListratelogId() == null) ? 0 : getListratelogId().hashCode());
        result = 31 * result + ((getListratelogZoneCode() == null) ? 0 : getListratelogZoneCode().hashCode());
        result = 31 * result + ((getListratelogPairname() == null) ? 0 : getListratelogPairname().hashCode());
        result = 31 * result + ((getListratelogCostbuy() == null) ? 0 : getListratelogCostbuy().hashCode());
        result = 31 * result + ((getListratelogCostsell() == null) ? 0 : getListratelogCostsell().hashCode());
        result = 31 * result + ((getListratelogListbuy() == null) ? 0 : getListratelogListbuy().hashCode());
        result = 31 * result + ((getListratelogListsell() == null) ? 0 : getListratelogListsell().hashCode());
        result = 31 * result + ((getListratelogNotebuy() == null) ? 0 : getListratelogNotebuy().hashCode());
        result = 31 * result + ((getListratelogNotesell() == null) ? 0 : getListratelogNotesell().hashCode());
        result = 31 * result + ((getListratelogEmpbuy() == null) ? 0 : getListratelogEmpbuy().hashCode());
        result = 31 * result + ((getListratelogEmpsell() == null) ? 0 : getListratelogEmpsell().hashCode());
        result = 31 * result + ((getListratelogNotempbuy() == null) ? 0 : getListratelogNotempbuy().hashCode());
        result = 31 * result + ((getListratelogNotempsell() == null) ? 0 : getListratelogNotempsell().hashCode());
        result = 31 * result + ((getListratelogMdflag() == null) ? 0 : getListratelogMdflag().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATELOG
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<LISTRATELOG_ID>").append(this.listratelogId).append("</LISTRATELOG_ID>");
        sb.append("<LISTRATELOG_ZONE_CODE>").append(this.listratelogZoneCode).append("</LISTRATELOG_ZONE_CODE>");
        sb.append("<LISTRATELOG_PAIRNAME>").append(this.listratelogPairname).append("</LISTRATELOG_PAIRNAME>");
        sb.append("<LISTRATELOG_COSTBUY>").append(this.listratelogCostbuy).append("</LISTRATELOG_COSTBUY>");
        sb.append("<LISTRATELOG_COSTSELL>").append(this.listratelogCostsell).append("</LISTRATELOG_COSTSELL>");
        sb.append("<LISTRATELOG_LISTBUY>").append(this.listratelogListbuy).append("</LISTRATELOG_LISTBUY>");
        sb.append("<LISTRATELOG_LISTSELL>").append(this.listratelogListsell).append("</LISTRATELOG_LISTSELL>");
        sb.append("<LISTRATELOG_NOTEBUY>").append(this.listratelogNotebuy).append("</LISTRATELOG_NOTEBUY>");
        sb.append("<LISTRATELOG_NOTESELL>").append(this.listratelogNotesell).append("</LISTRATELOG_NOTESELL>");
        sb.append("<LISTRATELOG_EMPBUY>").append(this.listratelogEmpbuy).append("</LISTRATELOG_EMPBUY>");
        sb.append("<LISTRATELOG_EMPSELL>").append(this.listratelogEmpsell).append("</LISTRATELOG_EMPSELL>");
        sb.append("<LISTRATELOG_NOTEMPBUY>").append(this.listratelogNotempbuy).append("</LISTRATELOG_NOTEMPBUY>");
        sb.append("<LISTRATELOG_NOTEMPSELL>").append(this.listratelogNotempsell).append("</LISTRATELOG_NOTEMPSELL>");
        sb.append("<LISTRATELOG_MDFLAG>").append(this.listratelogMdflag).append("</LISTRATELOG_MDFLAG>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}