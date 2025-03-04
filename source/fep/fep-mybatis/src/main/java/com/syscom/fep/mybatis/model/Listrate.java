package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Listrate extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_ZONE_CODE
     *
     * @mbg.generated
     */
    private String listrateZoneCode = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_PAIRNAME
     *
     * @mbg.generated
     */
    private String listratePairname = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_COSTBUY
     *
     * @mbg.generated
     */
    private BigDecimal listrateCostbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_COSTSELL
     *
     * @mbg.generated
     */
    private BigDecimal listrateCostsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_LISTBUY
     *
     * @mbg.generated
     */
    private BigDecimal listrateListbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_LISTSELL
     *
     * @mbg.generated
     */
    private BigDecimal listrateListsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_NOTEBUY
     *
     * @mbg.generated
     */
    private BigDecimal listrateNotebuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_NOTESELL
     *
     * @mbg.generated
     */
    private BigDecimal listrateNotesell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_EMPBUY
     *
     * @mbg.generated
     */
    private BigDecimal listrateEmpbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_EMPSELL
     *
     * @mbg.generated
     */
    private BigDecimal listrateEmpsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_NOTEMPBUY
     *
     * @mbg.generated
     */
    private BigDecimal listrateNotempbuy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_NOTEMPSELL
     *
     * @mbg.generated
     */
    private BigDecimal listrateNotempsell;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.LISTRATE_MDFLAG
     *
     * @mbg.generated
     */
    private String listrateMdflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column LISTRATE.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    public Listrate(String listrateZoneCode, String listratePairname, BigDecimal listrateCostbuy, BigDecimal listrateCostsell, BigDecimal listrateListbuy, BigDecimal listrateListsell, BigDecimal listrateNotebuy, BigDecimal listrateNotesell, BigDecimal listrateEmpbuy, BigDecimal listrateEmpsell, BigDecimal listrateNotempbuy, BigDecimal listrateNotempsell, String listrateMdflag, Integer updateUserid, Date updateTime) {
        this.listrateZoneCode = listrateZoneCode;
        this.listratePairname = listratePairname;
        this.listrateCostbuy = listrateCostbuy;
        this.listrateCostsell = listrateCostsell;
        this.listrateListbuy = listrateListbuy;
        this.listrateListsell = listrateListsell;
        this.listrateNotebuy = listrateNotebuy;
        this.listrateNotesell = listrateNotesell;
        this.listrateEmpbuy = listrateEmpbuy;
        this.listrateEmpsell = listrateEmpsell;
        this.listrateNotempbuy = listrateNotempbuy;
        this.listrateNotempsell = listrateNotempsell;
        this.listrateMdflag = listrateMdflag;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    public Listrate() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_ZONE_CODE
     *
     * @return the value of LISTRATE.LISTRATE_ZONE_CODE
     *
     * @mbg.generated
     */
    public String getListrateZoneCode() {
        return listrateZoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_ZONE_CODE
     *
     * @param listrateZoneCode the value for LISTRATE.LISTRATE_ZONE_CODE
     *
     * @mbg.generated
     */
    public void setListrateZoneCode(String listrateZoneCode) {
        this.listrateZoneCode = listrateZoneCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_PAIRNAME
     *
     * @return the value of LISTRATE.LISTRATE_PAIRNAME
     *
     * @mbg.generated
     */
    public String getListratePairname() {
        return listratePairname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_PAIRNAME
     *
     * @param listratePairname the value for LISTRATE.LISTRATE_PAIRNAME
     *
     * @mbg.generated
     */
    public void setListratePairname(String listratePairname) {
        this.listratePairname = listratePairname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_COSTBUY
     *
     * @return the value of LISTRATE.LISTRATE_COSTBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListrateCostbuy() {
        return listrateCostbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_COSTBUY
     *
     * @param listrateCostbuy the value for LISTRATE.LISTRATE_COSTBUY
     *
     * @mbg.generated
     */
    public void setListrateCostbuy(BigDecimal listrateCostbuy) {
        this.listrateCostbuy = listrateCostbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_COSTSELL
     *
     * @return the value of LISTRATE.LISTRATE_COSTSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListrateCostsell() {
        return listrateCostsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_COSTSELL
     *
     * @param listrateCostsell the value for LISTRATE.LISTRATE_COSTSELL
     *
     * @mbg.generated
     */
    public void setListrateCostsell(BigDecimal listrateCostsell) {
        this.listrateCostsell = listrateCostsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_LISTBUY
     *
     * @return the value of LISTRATE.LISTRATE_LISTBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListrateListbuy() {
        return listrateListbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_LISTBUY
     *
     * @param listrateListbuy the value for LISTRATE.LISTRATE_LISTBUY
     *
     * @mbg.generated
     */
    public void setListrateListbuy(BigDecimal listrateListbuy) {
        this.listrateListbuy = listrateListbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_LISTSELL
     *
     * @return the value of LISTRATE.LISTRATE_LISTSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListrateListsell() {
        return listrateListsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_LISTSELL
     *
     * @param listrateListsell the value for LISTRATE.LISTRATE_LISTSELL
     *
     * @mbg.generated
     */
    public void setListrateListsell(BigDecimal listrateListsell) {
        this.listrateListsell = listrateListsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_NOTEBUY
     *
     * @return the value of LISTRATE.LISTRATE_NOTEBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListrateNotebuy() {
        return listrateNotebuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_NOTEBUY
     *
     * @param listrateNotebuy the value for LISTRATE.LISTRATE_NOTEBUY
     *
     * @mbg.generated
     */
    public void setListrateNotebuy(BigDecimal listrateNotebuy) {
        this.listrateNotebuy = listrateNotebuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_NOTESELL
     *
     * @return the value of LISTRATE.LISTRATE_NOTESELL
     *
     * @mbg.generated
     */
    public BigDecimal getListrateNotesell() {
        return listrateNotesell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_NOTESELL
     *
     * @param listrateNotesell the value for LISTRATE.LISTRATE_NOTESELL
     *
     * @mbg.generated
     */
    public void setListrateNotesell(BigDecimal listrateNotesell) {
        this.listrateNotesell = listrateNotesell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_EMPBUY
     *
     * @return the value of LISTRATE.LISTRATE_EMPBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListrateEmpbuy() {
        return listrateEmpbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_EMPBUY
     *
     * @param listrateEmpbuy the value for LISTRATE.LISTRATE_EMPBUY
     *
     * @mbg.generated
     */
    public void setListrateEmpbuy(BigDecimal listrateEmpbuy) {
        this.listrateEmpbuy = listrateEmpbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_EMPSELL
     *
     * @return the value of LISTRATE.LISTRATE_EMPSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListrateEmpsell() {
        return listrateEmpsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_EMPSELL
     *
     * @param listrateEmpsell the value for LISTRATE.LISTRATE_EMPSELL
     *
     * @mbg.generated
     */
    public void setListrateEmpsell(BigDecimal listrateEmpsell) {
        this.listrateEmpsell = listrateEmpsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_NOTEMPBUY
     *
     * @return the value of LISTRATE.LISTRATE_NOTEMPBUY
     *
     * @mbg.generated
     */
    public BigDecimal getListrateNotempbuy() {
        return listrateNotempbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_NOTEMPBUY
     *
     * @param listrateNotempbuy the value for LISTRATE.LISTRATE_NOTEMPBUY
     *
     * @mbg.generated
     */
    public void setListrateNotempbuy(BigDecimal listrateNotempbuy) {
        this.listrateNotempbuy = listrateNotempbuy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_NOTEMPSELL
     *
     * @return the value of LISTRATE.LISTRATE_NOTEMPSELL
     *
     * @mbg.generated
     */
    public BigDecimal getListrateNotempsell() {
        return listrateNotempsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_NOTEMPSELL
     *
     * @param listrateNotempsell the value for LISTRATE.LISTRATE_NOTEMPSELL
     *
     * @mbg.generated
     */
    public void setListrateNotempsell(BigDecimal listrateNotempsell) {
        this.listrateNotempsell = listrateNotempsell;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.LISTRATE_MDFLAG
     *
     * @return the value of LISTRATE.LISTRATE_MDFLAG
     *
     * @mbg.generated
     */
    public String getListrateMdflag() {
        return listrateMdflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.LISTRATE_MDFLAG
     *
     * @param listrateMdflag the value for LISTRATE.LISTRATE_MDFLAG
     *
     * @mbg.generated
     */
    public void setListrateMdflag(String listrateMdflag) {
        this.listrateMdflag = listrateMdflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.UPDATE_USERID
     *
     * @return the value of LISTRATE.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.UPDATE_USERID
     *
     * @param updateUserid the value for LISTRATE.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column LISTRATE.UPDATE_TIME
     *
     * @return the value of LISTRATE.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column LISTRATE.UPDATE_TIME
     *
     * @param updateTime the value for LISTRATE.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", listrateZoneCode=").append(listrateZoneCode);
        sb.append(", listratePairname=").append(listratePairname);
        sb.append(", listrateCostbuy=").append(listrateCostbuy);
        sb.append(", listrateCostsell=").append(listrateCostsell);
        sb.append(", listrateListbuy=").append(listrateListbuy);
        sb.append(", listrateListsell=").append(listrateListsell);
        sb.append(", listrateNotebuy=").append(listrateNotebuy);
        sb.append(", listrateNotesell=").append(listrateNotesell);
        sb.append(", listrateEmpbuy=").append(listrateEmpbuy);
        sb.append(", listrateEmpsell=").append(listrateEmpsell);
        sb.append(", listrateNotempbuy=").append(listrateNotempbuy);
        sb.append(", listrateNotempsell=").append(listrateNotempsell);
        sb.append(", listrateMdflag=").append(listrateMdflag);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
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
        Listrate other = (Listrate) that;
        return (this.getListrateZoneCode() == null ? other.getListrateZoneCode() == null : this.getListrateZoneCode().equals(other.getListrateZoneCode()))
            && (this.getListratePairname() == null ? other.getListratePairname() == null : this.getListratePairname().equals(other.getListratePairname()))
            && (this.getListrateCostbuy() == null ? other.getListrateCostbuy() == null : this.getListrateCostbuy().equals(other.getListrateCostbuy()))
            && (this.getListrateCostsell() == null ? other.getListrateCostsell() == null : this.getListrateCostsell().equals(other.getListrateCostsell()))
            && (this.getListrateListbuy() == null ? other.getListrateListbuy() == null : this.getListrateListbuy().equals(other.getListrateListbuy()))
            && (this.getListrateListsell() == null ? other.getListrateListsell() == null : this.getListrateListsell().equals(other.getListrateListsell()))
            && (this.getListrateNotebuy() == null ? other.getListrateNotebuy() == null : this.getListrateNotebuy().equals(other.getListrateNotebuy()))
            && (this.getListrateNotesell() == null ? other.getListrateNotesell() == null : this.getListrateNotesell().equals(other.getListrateNotesell()))
            && (this.getListrateEmpbuy() == null ? other.getListrateEmpbuy() == null : this.getListrateEmpbuy().equals(other.getListrateEmpbuy()))
            && (this.getListrateEmpsell() == null ? other.getListrateEmpsell() == null : this.getListrateEmpsell().equals(other.getListrateEmpsell()))
            && (this.getListrateNotempbuy() == null ? other.getListrateNotempbuy() == null : this.getListrateNotempbuy().equals(other.getListrateNotempbuy()))
            && (this.getListrateNotempsell() == null ? other.getListrateNotempsell() == null : this.getListrateNotempsell().equals(other.getListrateNotempsell()))
            && (this.getListrateMdflag() == null ? other.getListrateMdflag() == null : this.getListrateMdflag().equals(other.getListrateMdflag()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getListrateZoneCode() == null) ? 0 : getListrateZoneCode().hashCode());
        result = 31 * result + ((getListratePairname() == null) ? 0 : getListratePairname().hashCode());
        result = 31 * result + ((getListrateCostbuy() == null) ? 0 : getListrateCostbuy().hashCode());
        result = 31 * result + ((getListrateCostsell() == null) ? 0 : getListrateCostsell().hashCode());
        result = 31 * result + ((getListrateListbuy() == null) ? 0 : getListrateListbuy().hashCode());
        result = 31 * result + ((getListrateListsell() == null) ? 0 : getListrateListsell().hashCode());
        result = 31 * result + ((getListrateNotebuy() == null) ? 0 : getListrateNotebuy().hashCode());
        result = 31 * result + ((getListrateNotesell() == null) ? 0 : getListrateNotesell().hashCode());
        result = 31 * result + ((getListrateEmpbuy() == null) ? 0 : getListrateEmpbuy().hashCode());
        result = 31 * result + ((getListrateEmpsell() == null) ? 0 : getListrateEmpsell().hashCode());
        result = 31 * result + ((getListrateNotempbuy() == null) ? 0 : getListrateNotempbuy().hashCode());
        result = 31 * result + ((getListrateNotempsell() == null) ? 0 : getListrateNotempsell().hashCode());
        result = 31 * result + ((getListrateMdflag() == null) ? 0 : getListrateMdflag().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LISTRATE
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<LISTRATE_ZONE_CODE>").append(this.listrateZoneCode).append("</LISTRATE_ZONE_CODE>");
        sb.append("<LISTRATE_PAIRNAME>").append(this.listratePairname).append("</LISTRATE_PAIRNAME>");
        sb.append("<LISTRATE_COSTBUY>").append(this.listrateCostbuy).append("</LISTRATE_COSTBUY>");
        sb.append("<LISTRATE_COSTSELL>").append(this.listrateCostsell).append("</LISTRATE_COSTSELL>");
        sb.append("<LISTRATE_LISTBUY>").append(this.listrateListbuy).append("</LISTRATE_LISTBUY>");
        sb.append("<LISTRATE_LISTSELL>").append(this.listrateListsell).append("</LISTRATE_LISTSELL>");
        sb.append("<LISTRATE_NOTEBUY>").append(this.listrateNotebuy).append("</LISTRATE_NOTEBUY>");
        sb.append("<LISTRATE_NOTESELL>").append(this.listrateNotesell).append("</LISTRATE_NOTESELL>");
        sb.append("<LISTRATE_EMPBUY>").append(this.listrateEmpbuy).append("</LISTRATE_EMPBUY>");
        sb.append("<LISTRATE_EMPSELL>").append(this.listrateEmpsell).append("</LISTRATE_EMPSELL>");
        sb.append("<LISTRATE_NOTEMPBUY>").append(this.listrateNotempbuy).append("</LISTRATE_NOTEMPBUY>");
        sb.append("<LISTRATE_NOTEMPSELL>").append(this.listrateNotempsell).append("</LISTRATE_NOTEMPSELL>");
        sb.append("<LISTRATE_MDFLAG>").append(this.listrateMdflag).append("</LISTRATE_MDFLAG>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}