package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class TestdropObjects extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTDROP_OBJECTS.ID
     *
     * @mbg.generated
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTDROP_OBJECTS.OBJECT_NAME
     *
     * @mbg.generated
     */
    private String objectName = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTDROP_OBJECTS.STATUS
     *
     * @mbg.generated
     */
    private Integer status;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTDROP_OBJECTS.NOTE
     *
     * @mbg.generated
     */
    private String note;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TESTDROP_OBJECTS.ADDDATE
     *
     * @mbg.generated
     */
    private Date adddate;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table TESTDROP_OBJECTS
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
     *
     * @mbg.generated
     */
    public TestdropObjects(Integer id, String objectName, Integer status, String note, Date adddate) {
        this.id = id;
        this.objectName = objectName;
        this.status = status;
        this.note = note;
        this.adddate = adddate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
     *
     * @mbg.generated
     */
    public TestdropObjects() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTDROP_OBJECTS.ID
     *
     * @return the value of TESTDROP_OBJECTS.ID
     *
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTDROP_OBJECTS.ID
     *
     * @param id the value for TESTDROP_OBJECTS.ID
     *
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTDROP_OBJECTS.OBJECT_NAME
     *
     * @return the value of TESTDROP_OBJECTS.OBJECT_NAME
     *
     * @mbg.generated
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTDROP_OBJECTS.OBJECT_NAME
     *
     * @param objectName the value for TESTDROP_OBJECTS.OBJECT_NAME
     *
     * @mbg.generated
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTDROP_OBJECTS.STATUS
     *
     * @return the value of TESTDROP_OBJECTS.STATUS
     *
     * @mbg.generated
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTDROP_OBJECTS.STATUS
     *
     * @param status the value for TESTDROP_OBJECTS.STATUS
     *
     * @mbg.generated
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTDROP_OBJECTS.NOTE
     *
     * @return the value of TESTDROP_OBJECTS.NOTE
     *
     * @mbg.generated
     */
    public String getNote() {
        return note;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTDROP_OBJECTS.NOTE
     *
     * @param note the value for TESTDROP_OBJECTS.NOTE
     *
     * @mbg.generated
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TESTDROP_OBJECTS.ADDDATE
     *
     * @return the value of TESTDROP_OBJECTS.ADDDATE
     *
     * @mbg.generated
     */
    public Date getAdddate() {
        return adddate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TESTDROP_OBJECTS.ADDDATE
     *
     * @param adddate the value for TESTDROP_OBJECTS.ADDDATE
     *
     * @mbg.generated
     */
    public void setAdddate(Date adddate) {
        this.adddate = adddate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", objectName=").append(objectName);
        sb.append(", status=").append(status);
        sb.append(", note=").append(note);
        sb.append(", adddate=").append(adddate);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
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
        TestdropObjects other = (TestdropObjects) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getObjectName() == null ? other.getObjectName() == null : this.getObjectName().equals(other.getObjectName()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getNote() == null ? other.getNote() == null : this.getNote().equals(other.getNote()))
            && (this.getAdddate() == null ? other.getAdddate() == null : this.getAdddate().equals(other.getAdddate()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getId() == null) ? 0 : getId().hashCode());
        result = 31 * result + ((getObjectName() == null) ? 0 : getObjectName().hashCode());
        result = 31 * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = 31 * result + ((getNote() == null) ? 0 : getNote().hashCode());
        result = 31 * result + ((getAdddate() == null) ? 0 : getAdddate().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TESTDROP_OBJECTS
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ID>").append(this.id).append("</ID>");
        sb.append("<OBJECT_NAME>").append(this.objectName).append("</OBJECT_NAME>");
        sb.append("<STATUS>").append(this.status).append("</STATUS>");
        sb.append("<NOTE>").append(this.note).append("</NOTE>");
        sb.append("<ADDDATE>").append(this.adddate).append("</ADDDATE>");
        return sb.toString();
    }
}