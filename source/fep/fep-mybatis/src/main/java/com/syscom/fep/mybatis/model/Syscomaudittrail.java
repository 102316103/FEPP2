package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Syscomaudittrail extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMAUDITTRAIL.LOGID
     *
     * @mbg.generated
     */
    private String logid = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMAUDITTRAIL.USERID
     *
     * @mbg.generated
     */
    private Integer userid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMAUDITTRAIL.UPDATETIME
     *
     * @mbg.generated
     */
    private Date updatetime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMAUDITTRAIL.ACCESSTYPE
     *
     * @mbg.generated
     */
    private String accesstype = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMAUDITTRAIL.UPDATETABLE
     *
     * @mbg.generated
     */
    private String updatetable = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMAUDITTRAIL.UPDATEPRIMARYKEY
     *
     * @mbg.generated
     */
    private String updateprimarykey;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMAUDITTRAIL.DATAIMAGE
     *
     * @mbg.generated
     */
    private String dataimage;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    public Syscomaudittrail(String logid, Integer userid, Date updatetime, String accesstype, String updatetable, String updateprimarykey) {
        this.logid = logid;
        this.userid = userid;
        this.updatetime = updatetime;
        this.accesstype = accesstype;
        this.updatetable = updatetable;
        this.updateprimarykey = updateprimarykey;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    public Syscomaudittrail(String logid, Integer userid, Date updatetime, String accesstype, String updatetable, String updateprimarykey, String dataimage) {
        this.logid = logid;
        this.userid = userid;
        this.updatetime = updatetime;
        this.accesstype = accesstype;
        this.updatetable = updatetable;
        this.updateprimarykey = updateprimarykey;
        this.dataimage = dataimage;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    public Syscomaudittrail() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMAUDITTRAIL.LOGID
     *
     * @return the value of SYSCOMAUDITTRAIL.LOGID
     *
     * @mbg.generated
     */
    public String getLogid() {
        return logid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMAUDITTRAIL.LOGID
     *
     * @param logid the value for SYSCOMAUDITTRAIL.LOGID
     *
     * @mbg.generated
     */
    public void setLogid(String logid) {
        this.logid = logid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMAUDITTRAIL.USERID
     *
     * @return the value of SYSCOMAUDITTRAIL.USERID
     *
     * @mbg.generated
     */
    public Integer getUserid() {
        return userid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMAUDITTRAIL.USERID
     *
     * @param userid the value for SYSCOMAUDITTRAIL.USERID
     *
     * @mbg.generated
     */
    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMAUDITTRAIL.UPDATETIME
     *
     * @return the value of SYSCOMAUDITTRAIL.UPDATETIME
     *
     * @mbg.generated
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMAUDITTRAIL.UPDATETIME
     *
     * @param updatetime the value for SYSCOMAUDITTRAIL.UPDATETIME
     *
     * @mbg.generated
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMAUDITTRAIL.ACCESSTYPE
     *
     * @return the value of SYSCOMAUDITTRAIL.ACCESSTYPE
     *
     * @mbg.generated
     */
    public String getAccesstype() {
        return accesstype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMAUDITTRAIL.ACCESSTYPE
     *
     * @param accesstype the value for SYSCOMAUDITTRAIL.ACCESSTYPE
     *
     * @mbg.generated
     */
    public void setAccesstype(String accesstype) {
        this.accesstype = accesstype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMAUDITTRAIL.UPDATETABLE
     *
     * @return the value of SYSCOMAUDITTRAIL.UPDATETABLE
     *
     * @mbg.generated
     */
    public String getUpdatetable() {
        return updatetable;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMAUDITTRAIL.UPDATETABLE
     *
     * @param updatetable the value for SYSCOMAUDITTRAIL.UPDATETABLE
     *
     * @mbg.generated
     */
    public void setUpdatetable(String updatetable) {
        this.updatetable = updatetable;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMAUDITTRAIL.UPDATEPRIMARYKEY
     *
     * @return the value of SYSCOMAUDITTRAIL.UPDATEPRIMARYKEY
     *
     * @mbg.generated
     */
    public String getUpdateprimarykey() {
        return updateprimarykey;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMAUDITTRAIL.UPDATEPRIMARYKEY
     *
     * @param updateprimarykey the value for SYSCOMAUDITTRAIL.UPDATEPRIMARYKEY
     *
     * @mbg.generated
     */
    public void setUpdateprimarykey(String updateprimarykey) {
        this.updateprimarykey = updateprimarykey;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMAUDITTRAIL.DATAIMAGE
     *
     * @return the value of SYSCOMAUDITTRAIL.DATAIMAGE
     *
     * @mbg.generated
     */
    public String getDataimage() {
        return dataimage;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMAUDITTRAIL.DATAIMAGE
     *
     * @param dataimage the value for SYSCOMAUDITTRAIL.DATAIMAGE
     *
     * @mbg.generated
     */
    public void setDataimage(String dataimage) {
        this.dataimage = dataimage;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", logid=").append(logid);
        sb.append(", userid=").append(userid);
        sb.append(", updatetime=").append(updatetime);
        sb.append(", accesstype=").append(accesstype);
        sb.append(", updatetable=").append(updatetable);
        sb.append(", updateprimarykey=").append(updateprimarykey);
        sb.append(", dataimage=").append(dataimage);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
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
        Syscomaudittrail other = (Syscomaudittrail) that;
        return (this.getLogid() == null ? other.getLogid() == null : this.getLogid().equals(other.getLogid()))
            && (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()))
            && (this.getAccesstype() == null ? other.getAccesstype() == null : this.getAccesstype().equals(other.getAccesstype()))
            && (this.getUpdatetable() == null ? other.getUpdatetable() == null : this.getUpdatetable().equals(other.getUpdatetable()))
            && (this.getUpdateprimarykey() == null ? other.getUpdateprimarykey() == null : this.getUpdateprimarykey().equals(other.getUpdateprimarykey()))
            && (this.getDataimage() == null ? other.getDataimage() == null : this.getDataimage().equals(other.getDataimage()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getLogid() == null) ? 0 : getLogid().hashCode());
        result = 31 * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
        result = 31 * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        result = 31 * result + ((getAccesstype() == null) ? 0 : getAccesstype().hashCode());
        result = 31 * result + ((getUpdatetable() == null) ? 0 : getUpdatetable().hashCode());
        result = 31 * result + ((getUpdateprimarykey() == null) ? 0 : getUpdateprimarykey().hashCode());
        result = 31 * result + ((getDataimage() == null) ? 0 : getDataimage().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMAUDITTRAIL
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<LOGID>").append(this.logid).append("</LOGID>");
        sb.append("<USERID>").append(this.userid).append("</USERID>");
        sb.append("<UPDATETIME>").append(this.updatetime).append("</UPDATETIME>");
        sb.append("<ACCESSTYPE>").append(this.accesstype).append("</ACCESSTYPE>");
        sb.append("<UPDATETABLE>").append(this.updatetable).append("</UPDATETABLE>");
        sb.append("<UPDATEPRIMARYKEY>").append(this.updateprimarykey).append("</UPDATEPRIMARYKEY>");
        sb.append("<DATAIMAGE>").append(this.dataimage).append("</DATAIMAGE>");
        return sb.toString();
    }
}