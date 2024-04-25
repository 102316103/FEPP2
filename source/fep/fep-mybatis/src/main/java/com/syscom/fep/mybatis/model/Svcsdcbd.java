package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Svcsdcbd extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.SEQNO
     *
     * @mbg.generated
     */
    private Long seqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.CALDATE
     *
     * @mbg.generated
     */
    private String caldate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.FILENAME
     *
     * @mbg.generated
     */
    private String filename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.FILEHEADER
     *
     * @mbg.generated
     */
    private String fileheader;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    private String dataattribute;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.EASYNO
     *
     * @mbg.generated
     */
    private String easyno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.EASYBALANCE
     *
     * @mbg.generated
     */
    private Long easybalance;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.BACKCARDCHAGEFEE
     *
     * @mbg.generated
     */
    private Long backcardchagefee;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.TRANSFERAMOUNT
     *
     * @mbg.generated
     */
    private Long transferamount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.LOSTDATE
     *
     * @mbg.generated
     */
    private String lostdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.RC
     *
     * @mbg.generated
     */
    private String rc;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.LOSTBALANCE
     *
     * @mbg.generated
     */
    private Long lostbalance;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.HASH
     *
     * @mbg.generated
     */
    private String hash;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.ERRCODE
     *
     * @mbg.generated
     */
    private String errcode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.FILEDATA
     *
     * @mbg.generated
     */
    private String filedata;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.UPDATEUSERID
     *
     * @mbg.generated
     */
    private Integer updateuserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBD.UPDATETIME
     *
     * @mbg.generated
     */
    private Date updatetime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table SVCSDCBD
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBD
     *
     * @mbg.generated
     */
    public Svcsdcbd(Long seqno, String caldate, String filename, String fileheader, String dataattribute, String easyno, Long easybalance, Long backcardchagefee, Long transferamount, String lostdate, String rc, Long lostbalance, String hash, String errcode, String filedata, Integer updateuserid, Date updatetime) {
        this.seqno = seqno;
        this.caldate = caldate;
        this.filename = filename;
        this.fileheader = fileheader;
        this.dataattribute = dataattribute;
        this.easyno = easyno;
        this.easybalance = easybalance;
        this.backcardchagefee = backcardchagefee;
        this.transferamount = transferamount;
        this.lostdate = lostdate;
        this.rc = rc;
        this.lostbalance = lostbalance;
        this.hash = hash;
        this.errcode = errcode;
        this.filedata = filedata;
        this.updateuserid = updateuserid;
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBD
     *
     * @mbg.generated
     */
    public Svcsdcbd() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.SEQNO
     *
     * @return the value of SVCSDCBD.SEQNO
     *
     * @mbg.generated
     */
    public Long getSeqno() {
        return seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.SEQNO
     *
     * @param seqno the value for SVCSDCBD.SEQNO
     *
     * @mbg.generated
     */
    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.CALDATE
     *
     * @return the value of SVCSDCBD.CALDATE
     *
     * @mbg.generated
     */
    public String getCaldate() {
        return caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.CALDATE
     *
     * @param caldate the value for SVCSDCBD.CALDATE
     *
     * @mbg.generated
     */
    public void setCaldate(String caldate) {
        this.caldate = caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.FILENAME
     *
     * @return the value of SVCSDCBD.FILENAME
     *
     * @mbg.generated
     */
    public String getFilename() {
        return filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.FILENAME
     *
     * @param filename the value for SVCSDCBD.FILENAME
     *
     * @mbg.generated
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.FILEHEADER
     *
     * @return the value of SVCSDCBD.FILEHEADER
     *
     * @mbg.generated
     */
    public String getFileheader() {
        return fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.FILEHEADER
     *
     * @param fileheader the value for SVCSDCBD.FILEHEADER
     *
     * @mbg.generated
     */
    public void setFileheader(String fileheader) {
        this.fileheader = fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.DATAATTRIBUTE
     *
     * @return the value of SVCSDCBD.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public String getDataattribute() {
        return dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.DATAATTRIBUTE
     *
     * @param dataattribute the value for SVCSDCBD.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public void setDataattribute(String dataattribute) {
        this.dataattribute = dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.EASYNO
     *
     * @return the value of SVCSDCBD.EASYNO
     *
     * @mbg.generated
     */
    public String getEasyno() {
        return easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.EASYNO
     *
     * @param easyno the value for SVCSDCBD.EASYNO
     *
     * @mbg.generated
     */
    public void setEasyno(String easyno) {
        this.easyno = easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.EASYBALANCE
     *
     * @return the value of SVCSDCBD.EASYBALANCE
     *
     * @mbg.generated
     */
    public Long getEasybalance() {
        return easybalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.EASYBALANCE
     *
     * @param easybalance the value for SVCSDCBD.EASYBALANCE
     *
     * @mbg.generated
     */
    public void setEasybalance(Long easybalance) {
        this.easybalance = easybalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.BACKCARDCHAGEFEE
     *
     * @return the value of SVCSDCBD.BACKCARDCHAGEFEE
     *
     * @mbg.generated
     */
    public Long getBackcardchagefee() {
        return backcardchagefee;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.BACKCARDCHAGEFEE
     *
     * @param backcardchagefee the value for SVCSDCBD.BACKCARDCHAGEFEE
     *
     * @mbg.generated
     */
    public void setBackcardchagefee(Long backcardchagefee) {
        this.backcardchagefee = backcardchagefee;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.TRANSFERAMOUNT
     *
     * @return the value of SVCSDCBD.TRANSFERAMOUNT
     *
     * @mbg.generated
     */
    public Long getTransferamount() {
        return transferamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.TRANSFERAMOUNT
     *
     * @param transferamount the value for SVCSDCBD.TRANSFERAMOUNT
     *
     * @mbg.generated
     */
    public void setTransferamount(Long transferamount) {
        this.transferamount = transferamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.LOSTDATE
     *
     * @return the value of SVCSDCBD.LOSTDATE
     *
     * @mbg.generated
     */
    public String getLostdate() {
        return lostdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.LOSTDATE
     *
     * @param lostdate the value for SVCSDCBD.LOSTDATE
     *
     * @mbg.generated
     */
    public void setLostdate(String lostdate) {
        this.lostdate = lostdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.RC
     *
     * @return the value of SVCSDCBD.RC
     *
     * @mbg.generated
     */
    public String getRc() {
        return rc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.RC
     *
     * @param rc the value for SVCSDCBD.RC
     *
     * @mbg.generated
     */
    public void setRc(String rc) {
        this.rc = rc;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.LOSTBALANCE
     *
     * @return the value of SVCSDCBD.LOSTBALANCE
     *
     * @mbg.generated
     */
    public Long getLostbalance() {
        return lostbalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.LOSTBALANCE
     *
     * @param lostbalance the value for SVCSDCBD.LOSTBALANCE
     *
     * @mbg.generated
     */
    public void setLostbalance(Long lostbalance) {
        this.lostbalance = lostbalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.HASH
     *
     * @return the value of SVCSDCBD.HASH
     *
     * @mbg.generated
     */
    public String getHash() {
        return hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.HASH
     *
     * @param hash the value for SVCSDCBD.HASH
     *
     * @mbg.generated
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.ERRCODE
     *
     * @return the value of SVCSDCBD.ERRCODE
     *
     * @mbg.generated
     */
    public String getErrcode() {
        return errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.ERRCODE
     *
     * @param errcode the value for SVCSDCBD.ERRCODE
     *
     * @mbg.generated
     */
    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.FILEDATA
     *
     * @return the value of SVCSDCBD.FILEDATA
     *
     * @mbg.generated
     */
    public String getFiledata() {
        return filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.FILEDATA
     *
     * @param filedata the value for SVCSDCBD.FILEDATA
     *
     * @mbg.generated
     */
    public void setFiledata(String filedata) {
        this.filedata = filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.UPDATEUSERID
     *
     * @return the value of SVCSDCBD.UPDATEUSERID
     *
     * @mbg.generated
     */
    public Integer getUpdateuserid() {
        return updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.UPDATEUSERID
     *
     * @param updateuserid the value for SVCSDCBD.UPDATEUSERID
     *
     * @mbg.generated
     */
    public void setUpdateuserid(Integer updateuserid) {
        this.updateuserid = updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBD.UPDATETIME
     *
     * @return the value of SVCSDCBD.UPDATETIME
     *
     * @mbg.generated
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBD.UPDATETIME
     *
     * @param updatetime the value for SVCSDCBD.UPDATETIME
     *
     * @mbg.generated
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBD
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", seqno=").append(seqno);
        sb.append(", caldate=").append(caldate);
        sb.append(", filename=").append(filename);
        sb.append(", fileheader=").append(fileheader);
        sb.append(", dataattribute=").append(dataattribute);
        sb.append(", easyno=").append(easyno);
        sb.append(", easybalance=").append(easybalance);
        sb.append(", backcardchagefee=").append(backcardchagefee);
        sb.append(", transferamount=").append(transferamount);
        sb.append(", lostdate=").append(lostdate);
        sb.append(", rc=").append(rc);
        sb.append(", lostbalance=").append(lostbalance);
        sb.append(", hash=").append(hash);
        sb.append(", errcode=").append(errcode);
        sb.append(", filedata=").append(filedata);
        sb.append(", updateuserid=").append(updateuserid);
        sb.append(", updatetime=").append(updatetime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBD
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
        Svcsdcbd other = (Svcsdcbd) that;
        return (this.getSeqno() == null ? other.getSeqno() == null : this.getSeqno().equals(other.getSeqno()))
            && (this.getCaldate() == null ? other.getCaldate() == null : this.getCaldate().equals(other.getCaldate()))
            && (this.getFilename() == null ? other.getFilename() == null : this.getFilename().equals(other.getFilename()))
            && (this.getFileheader() == null ? other.getFileheader() == null : this.getFileheader().equals(other.getFileheader()))
            && (this.getDataattribute() == null ? other.getDataattribute() == null : this.getDataattribute().equals(other.getDataattribute()))
            && (this.getEasyno() == null ? other.getEasyno() == null : this.getEasyno().equals(other.getEasyno()))
            && (this.getEasybalance() == null ? other.getEasybalance() == null : this.getEasybalance().equals(other.getEasybalance()))
            && (this.getBackcardchagefee() == null ? other.getBackcardchagefee() == null : this.getBackcardchagefee().equals(other.getBackcardchagefee()))
            && (this.getTransferamount() == null ? other.getTransferamount() == null : this.getTransferamount().equals(other.getTransferamount()))
            && (this.getLostdate() == null ? other.getLostdate() == null : this.getLostdate().equals(other.getLostdate()))
            && (this.getRc() == null ? other.getRc() == null : this.getRc().equals(other.getRc()))
            && (this.getLostbalance() == null ? other.getLostbalance() == null : this.getLostbalance().equals(other.getLostbalance()))
            && (this.getHash() == null ? other.getHash() == null : this.getHash().equals(other.getHash()))
            && (this.getErrcode() == null ? other.getErrcode() == null : this.getErrcode().equals(other.getErrcode()))
            && (this.getFiledata() == null ? other.getFiledata() == null : this.getFiledata().equals(other.getFiledata()))
            && (this.getUpdateuserid() == null ? other.getUpdateuserid() == null : this.getUpdateuserid().equals(other.getUpdateuserid()))
            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBD
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getSeqno() == null) ? 0 : getSeqno().hashCode());
        result = 31 * result + ((getCaldate() == null) ? 0 : getCaldate().hashCode());
        result = 31 * result + ((getFilename() == null) ? 0 : getFilename().hashCode());
        result = 31 * result + ((getFileheader() == null) ? 0 : getFileheader().hashCode());
        result = 31 * result + ((getDataattribute() == null) ? 0 : getDataattribute().hashCode());
        result = 31 * result + ((getEasyno() == null) ? 0 : getEasyno().hashCode());
        result = 31 * result + ((getEasybalance() == null) ? 0 : getEasybalance().hashCode());
        result = 31 * result + ((getBackcardchagefee() == null) ? 0 : getBackcardchagefee().hashCode());
        result = 31 * result + ((getTransferamount() == null) ? 0 : getTransferamount().hashCode());
        result = 31 * result + ((getLostdate() == null) ? 0 : getLostdate().hashCode());
        result = 31 * result + ((getRc() == null) ? 0 : getRc().hashCode());
        result = 31 * result + ((getLostbalance() == null) ? 0 : getLostbalance().hashCode());
        result = 31 * result + ((getHash() == null) ? 0 : getHash().hashCode());
        result = 31 * result + ((getErrcode() == null) ? 0 : getErrcode().hashCode());
        result = 31 * result + ((getFiledata() == null) ? 0 : getFiledata().hashCode());
        result = 31 * result + ((getUpdateuserid() == null) ? 0 : getUpdateuserid().hashCode());
        result = 31 * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBD
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<SEQNO>").append(this.seqno).append("</SEQNO>");
        sb.append("<CALDATE>").append(this.caldate).append("</CALDATE>");
        sb.append("<FILENAME>").append(this.filename).append("</FILENAME>");
        sb.append("<FILEHEADER>").append(this.fileheader).append("</FILEHEADER>");
        sb.append("<DATAATTRIBUTE>").append(this.dataattribute).append("</DATAATTRIBUTE>");
        sb.append("<EASYNO>").append(this.easyno).append("</EASYNO>");
        sb.append("<EASYBALANCE>").append(this.easybalance).append("</EASYBALANCE>");
        sb.append("<BACKCARDCHAGEFEE>").append(this.backcardchagefee).append("</BACKCARDCHAGEFEE>");
        sb.append("<TRANSFERAMOUNT>").append(this.transferamount).append("</TRANSFERAMOUNT>");
        sb.append("<LOSTDATE>").append(this.lostdate).append("</LOSTDATE>");
        sb.append("<RC>").append(this.rc).append("</RC>");
        sb.append("<LOSTBALANCE>").append(this.lostbalance).append("</LOSTBALANCE>");
        sb.append("<HASH>").append(this.hash).append("</HASH>");
        sb.append("<ERRCODE>").append(this.errcode).append("</ERRCODE>");
        sb.append("<FILEDATA>").append(this.filedata).append("</FILEDATA>");
        sb.append("<UPDATEUSERID>").append(this.updateuserid).append("</UPDATEUSERID>");
        sb.append("<UPDATETIME>").append(this.updatetime).append("</UPDATETIME>");
        return sb.toString();
    }
}