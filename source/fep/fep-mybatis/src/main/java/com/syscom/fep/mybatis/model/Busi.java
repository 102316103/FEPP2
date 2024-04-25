package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Busi extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BUSI.BUSI_IDNO
     *
     * @mbg.generated
     */
    private String busiIdno = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BUSI.BUSI_IDERR
     *
     * @mbg.generated
     */
    private String busiIderr = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BUSI.BUSI_DEPID
     *
     * @mbg.generated
     */
    private String busiDepid = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BUSI.BUSI_COMPANY
     *
     * @mbg.generated
     */
    private String busiCompany;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BUSI.BUSI_DEPNAME
     *
     * @mbg.generated
     */
    private String busiDepname;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BUSI.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column BUSI.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table BUSI
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BUSI
     *
     * @mbg.generated
     */
    public Busi(String busiIdno, String busiIderr, String busiDepid, String busiCompany, String busiDepname, Integer updateUserid, Date updateTime) {
        this.busiIdno = busiIdno;
        this.busiIderr = busiIderr;
        this.busiDepid = busiDepid;
        this.busiCompany = busiCompany;
        this.busiDepname = busiDepname;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BUSI
     *
     * @mbg.generated
     */
    public Busi() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BUSI.BUSI_IDNO
     *
     * @return the value of BUSI.BUSI_IDNO
     *
     * @mbg.generated
     */
    public String getBusiIdno() {
        return busiIdno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BUSI.BUSI_IDNO
     *
     * @param busiIdno the value for BUSI.BUSI_IDNO
     *
     * @mbg.generated
     */
    public void setBusiIdno(String busiIdno) {
        this.busiIdno = busiIdno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BUSI.BUSI_IDERR
     *
     * @return the value of BUSI.BUSI_IDERR
     *
     * @mbg.generated
     */
    public String getBusiIderr() {
        return busiIderr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BUSI.BUSI_IDERR
     *
     * @param busiIderr the value for BUSI.BUSI_IDERR
     *
     * @mbg.generated
     */
    public void setBusiIderr(String busiIderr) {
        this.busiIderr = busiIderr;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BUSI.BUSI_DEPID
     *
     * @return the value of BUSI.BUSI_DEPID
     *
     * @mbg.generated
     */
    public String getBusiDepid() {
        return busiDepid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BUSI.BUSI_DEPID
     *
     * @param busiDepid the value for BUSI.BUSI_DEPID
     *
     * @mbg.generated
     */
    public void setBusiDepid(String busiDepid) {
        this.busiDepid = busiDepid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BUSI.BUSI_COMPANY
     *
     * @return the value of BUSI.BUSI_COMPANY
     *
     * @mbg.generated
     */
    public String getBusiCompany() {
        return busiCompany;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BUSI.BUSI_COMPANY
     *
     * @param busiCompany the value for BUSI.BUSI_COMPANY
     *
     * @mbg.generated
     */
    public void setBusiCompany(String busiCompany) {
        this.busiCompany = busiCompany;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BUSI.BUSI_DEPNAME
     *
     * @return the value of BUSI.BUSI_DEPNAME
     *
     * @mbg.generated
     */
    public String getBusiDepname() {
        return busiDepname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BUSI.BUSI_DEPNAME
     *
     * @param busiDepname the value for BUSI.BUSI_DEPNAME
     *
     * @mbg.generated
     */
    public void setBusiDepname(String busiDepname) {
        this.busiDepname = busiDepname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BUSI.UPDATE_USERID
     *
     * @return the value of BUSI.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BUSI.UPDATE_USERID
     *
     * @param updateUserid the value for BUSI.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column BUSI.UPDATE_TIME
     *
     * @return the value of BUSI.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column BUSI.UPDATE_TIME
     *
     * @param updateTime the value for BUSI.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BUSI
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", busiIdno=").append(busiIdno);
        sb.append(", busiIderr=").append(busiIderr);
        sb.append(", busiDepid=").append(busiDepid);
        sb.append(", busiCompany=").append(busiCompany);
        sb.append(", busiDepname=").append(busiDepname);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BUSI
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
        Busi other = (Busi) that;
        return (this.getBusiIdno() == null ? other.getBusiIdno() == null : this.getBusiIdno().equals(other.getBusiIdno()))
            && (this.getBusiIderr() == null ? other.getBusiIderr() == null : this.getBusiIderr().equals(other.getBusiIderr()))
            && (this.getBusiDepid() == null ? other.getBusiDepid() == null : this.getBusiDepid().equals(other.getBusiDepid()))
            && (this.getBusiCompany() == null ? other.getBusiCompany() == null : this.getBusiCompany().equals(other.getBusiCompany()))
            && (this.getBusiDepname() == null ? other.getBusiDepname() == null : this.getBusiDepname().equals(other.getBusiDepname()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BUSI
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getBusiIdno() == null) ? 0 : getBusiIdno().hashCode());
        result = 31 * result + ((getBusiIderr() == null) ? 0 : getBusiIderr().hashCode());
        result = 31 * result + ((getBusiDepid() == null) ? 0 : getBusiDepid().hashCode());
        result = 31 * result + ((getBusiCompany() == null) ? 0 : getBusiCompany().hashCode());
        result = 31 * result + ((getBusiDepname() == null) ? 0 : getBusiDepname().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table BUSI
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<BUSI_IDNO>").append(this.busiIdno).append("</BUSI_IDNO>");
        sb.append("<BUSI_IDERR>").append(this.busiIderr).append("</BUSI_IDERR>");
        sb.append("<BUSI_DEPID>").append(this.busiDepid).append("</BUSI_DEPID>");
        sb.append("<BUSI_COMPANY>").append(this.busiCompany).append("</BUSI_COMPANY>");
        sb.append("<BUSI_DEPNAME>").append(this.busiDepname).append("</BUSI_DEPNAME>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}