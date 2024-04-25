package com.syscom.safeaa.mybatis.model;

import com.syscom.safeaa.mybatis.vo.BaseModel;
import java.util.Date;

public class Syscomrolegroup extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMROLEGROUP.ROLEID
     *
     * @mbg.generated
     */
    private Integer roleid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMROLEGROUP.GROUPID
     *
     * @mbg.generated
     */
    private Integer groupid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMROLEGROUP.CHILDTYPE
     *
     * @mbg.generated
     */
    private String childtype;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMROLEGROUP.SAFEDEFINEFUNCTIONLIST
     *
     * @mbg.generated
     */
    private String safedefinefunctionlist;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMROLEGROUP.USERDEFINEFUNCTIONLIST
     *
     * @mbg.generated
     */
    private String userdefinefunctionlist;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMROLEGROUP.UPDATEUSERID
     *
     * @mbg.generated
     */
    private Integer updateuserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SYSCOMROLEGROUP.UPDATETIME
     *
     * @mbg.generated
     */
    private Date updatetime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table SYSCOMROLEGROUP
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEGROUP
     *
     * @mbg.generated
     */
    public Syscomrolegroup(Integer roleid, Integer groupid, String childtype, String safedefinefunctionlist, String userdefinefunctionlist, Integer updateuserid, Date updatetime) {
        this.roleid = roleid;
        this.groupid = groupid;
        this.childtype = childtype;
        this.safedefinefunctionlist = safedefinefunctionlist;
        this.userdefinefunctionlist = userdefinefunctionlist;
        this.updateuserid = updateuserid;
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEGROUP
     *
     * @mbg.generated
     */
    public Syscomrolegroup() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMROLEGROUP.ROLEID
     *
     * @return the value of SYSCOMROLEGROUP.ROLEID
     *
     * @mbg.generated
     */
    public Integer getRoleid() {
        return roleid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMROLEGROUP.ROLEID
     *
     * @param roleid the value for SYSCOMROLEGROUP.ROLEID
     *
     * @mbg.generated
     */
    public void setRoleid(Integer roleid) {
        this.roleid = roleid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMROLEGROUP.GROUPID
     *
     * @return the value of SYSCOMROLEGROUP.GROUPID
     *
     * @mbg.generated
     */
    public Integer getGroupid() {
        return groupid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMROLEGROUP.GROUPID
     *
     * @param groupid the value for SYSCOMROLEGROUP.GROUPID
     *
     * @mbg.generated
     */
    public void setGroupid(Integer groupid) {
        this.groupid = groupid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMROLEGROUP.CHILDTYPE
     *
     * @return the value of SYSCOMROLEGROUP.CHILDTYPE
     *
     * @mbg.generated
     */
    public String getChildtype() {
        return childtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMROLEGROUP.CHILDTYPE
     *
     * @param childtype the value for SYSCOMROLEGROUP.CHILDTYPE
     *
     * @mbg.generated
     */
    public void setChildtype(String childtype) {
        this.childtype = childtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMROLEGROUP.SAFEDEFINEFUNCTIONLIST
     *
     * @return the value of SYSCOMROLEGROUP.SAFEDEFINEFUNCTIONLIST
     *
     * @mbg.generated
     */
    public String getSafedefinefunctionlist() {
        return safedefinefunctionlist;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMROLEGROUP.SAFEDEFINEFUNCTIONLIST
     *
     * @param safedefinefunctionlist the value for SYSCOMROLEGROUP.SAFEDEFINEFUNCTIONLIST
     *
     * @mbg.generated
     */
    public void setSafedefinefunctionlist(String safedefinefunctionlist) {
        this.safedefinefunctionlist = safedefinefunctionlist;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMROLEGROUP.USERDEFINEFUNCTIONLIST
     *
     * @return the value of SYSCOMROLEGROUP.USERDEFINEFUNCTIONLIST
     *
     * @mbg.generated
     */
    public String getUserdefinefunctionlist() {
        return userdefinefunctionlist;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMROLEGROUP.USERDEFINEFUNCTIONLIST
     *
     * @param userdefinefunctionlist the value for SYSCOMROLEGROUP.USERDEFINEFUNCTIONLIST
     *
     * @mbg.generated
     */
    public void setUserdefinefunctionlist(String userdefinefunctionlist) {
        this.userdefinefunctionlist = userdefinefunctionlist;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMROLEGROUP.UPDATEUSERID
     *
     * @return the value of SYSCOMROLEGROUP.UPDATEUSERID
     *
     * @mbg.generated
     */
    public Integer getUpdateuserid() {
        return updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMROLEGROUP.UPDATEUSERID
     *
     * @param updateuserid the value for SYSCOMROLEGROUP.UPDATEUSERID
     *
     * @mbg.generated
     */
    public void setUpdateuserid(Integer updateuserid) {
        this.updateuserid = updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SYSCOMROLEGROUP.UPDATETIME
     *
     * @return the value of SYSCOMROLEGROUP.UPDATETIME
     *
     * @mbg.generated
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SYSCOMROLEGROUP.UPDATETIME
     *
     * @param updatetime the value for SYSCOMROLEGROUP.UPDATETIME
     *
     * @mbg.generated
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEGROUP
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
        Syscomrolegroup other = (Syscomrolegroup) that;
        return (this.getRoleid() == null ? other.getRoleid() == null : this.getRoleid().equals(other.getRoleid()))
            && (this.getGroupid() == null ? other.getGroupid() == null : this.getGroupid().equals(other.getGroupid()))
            && (this.getChildtype() == null ? other.getChildtype() == null : this.getChildtype().equals(other.getChildtype()))
            && (this.getSafedefinefunctionlist() == null ? other.getSafedefinefunctionlist() == null : this.getSafedefinefunctionlist().equals(other.getSafedefinefunctionlist()))
            && (this.getUserdefinefunctionlist() == null ? other.getUserdefinefunctionlist() == null : this.getUserdefinefunctionlist().equals(other.getUserdefinefunctionlist()))
            && (this.getUpdateuserid() == null ? other.getUpdateuserid() == null : this.getUpdateuserid().equals(other.getUpdateuserid()))
            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEGROUP
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRoleid() == null) ? 0 : getRoleid().hashCode());
        result = prime * result + ((getGroupid() == null) ? 0 : getGroupid().hashCode());
        result = prime * result + ((getChildtype() == null) ? 0 : getChildtype().hashCode());
        result = prime * result + ((getSafedefinefunctionlist() == null) ? 0 : getSafedefinefunctionlist().hashCode());
        result = prime * result + ((getUserdefinefunctionlist() == null) ? 0 : getUserdefinefunctionlist().hashCode());
        result = prime * result + ((getUpdateuserid() == null) ? 0 : getUpdateuserid().hashCode());
        result = prime * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEGROUP
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", roleid=").append(roleid);
        sb.append(", groupid=").append(groupid);
        sb.append(", childtype=").append(childtype);
        sb.append(", safedefinefunctionlist=").append(safedefinefunctionlist);
        sb.append(", userdefinefunctionlist=").append(userdefinefunctionlist);
        sb.append(", updateuserid=").append(updateuserid);
        sb.append(", updatetime=").append(updatetime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SYSCOMROLEGROUP
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ROLEID>").append(this.roleid).append("</ROLEID>");
        sb.append("<GROUPID>").append(this.groupid).append("</GROUPID>");
        sb.append("<CHILDTYPE>").append(this.childtype).append("</CHILDTYPE>");
        sb.append("<SAFEDEFINEFUNCTIONLIST>").append(this.safedefinefunctionlist).append("</SAFEDEFINEFUNCTIONLIST>");
        sb.append("<USERDEFINEFUNCTIONLIST>").append(this.userdefinefunctionlist).append("</USERDEFINEFUNCTIONLIST>");
        sb.append("<UPDATEUSERID>").append(this.updateuserid).append("</UPDATEUSERID>");
        sb.append("<UPDATETIME>").append(this.updatetime).append("</UPDATETIME>");
        return sb.toString();
    }
}