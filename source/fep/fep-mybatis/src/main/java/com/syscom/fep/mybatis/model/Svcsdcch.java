package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Svcsdcch extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.SEQNO
     *
     * @mbg.generated
     */
    private Long seqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.CALDATE
     *
     * @mbg.generated
     */
    private String caldate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.FILENAME
     *
     * @mbg.generated
     */
    private String filename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.FILEHEADER
     *
     * @mbg.generated
     */
    private String fileheader;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    private String dataattribute;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.EASYNO
     *
     * @mbg.generated
     */
    private String easyno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.TXDATE
     *
     * @mbg.generated
     */
    private String txdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.TXTIME
     *
     * @mbg.generated
     */
    private String txtime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.TXAMOUNT
     *
     * @mbg.generated
     */
    private Long txamount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.EASYBALANCE
     *
     * @mbg.generated
     */
    private Long easybalance;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.HASH
     *
     * @mbg.generated
     */
    private String hash;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.ERRCODE
     *
     * @mbg.generated
     */
    private String errcode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.FILEDATA
     *
     * @mbg.generated
     */
    private String filedata;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.UPDATEUSERID
     *
     * @mbg.generated
     */
    private Integer updateuserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCCH.UPDATETIME
     *
     * @mbg.generated
     */
    private Date updatetime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table SVCSDCCH
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCH
     *
     * @mbg.generated
     */
    public Svcsdcch(Long seqno, String caldate, String filename, String fileheader, String dataattribute, String easyno, String txdate, String txtime, Long txamount, Long easybalance, String hash, String errcode, String filedata, Integer updateuserid, Date updatetime) {
        this.seqno = seqno;
        this.caldate = caldate;
        this.filename = filename;
        this.fileheader = fileheader;
        this.dataattribute = dataattribute;
        this.easyno = easyno;
        this.txdate = txdate;
        this.txtime = txtime;
        this.txamount = txamount;
        this.easybalance = easybalance;
        this.hash = hash;
        this.errcode = errcode;
        this.filedata = filedata;
        this.updateuserid = updateuserid;
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCH
     *
     * @mbg.generated
     */
    public Svcsdcch() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.SEQNO
     *
     * @return the value of SVCSDCCH.SEQNO
     *
     * @mbg.generated
     */
    public Long getSeqno() {
        return seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.SEQNO
     *
     * @param seqno the value for SVCSDCCH.SEQNO
     *
     * @mbg.generated
     */
    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.CALDATE
     *
     * @return the value of SVCSDCCH.CALDATE
     *
     * @mbg.generated
     */
    public String getCaldate() {
        return caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.CALDATE
     *
     * @param caldate the value for SVCSDCCH.CALDATE
     *
     * @mbg.generated
     */
    public void setCaldate(String caldate) {
        this.caldate = caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.FILENAME
     *
     * @return the value of SVCSDCCH.FILENAME
     *
     * @mbg.generated
     */
    public String getFilename() {
        return filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.FILENAME
     *
     * @param filename the value for SVCSDCCH.FILENAME
     *
     * @mbg.generated
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.FILEHEADER
     *
     * @return the value of SVCSDCCH.FILEHEADER
     *
     * @mbg.generated
     */
    public String getFileheader() {
        return fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.FILEHEADER
     *
     * @param fileheader the value for SVCSDCCH.FILEHEADER
     *
     * @mbg.generated
     */
    public void setFileheader(String fileheader) {
        this.fileheader = fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.DATAATTRIBUTE
     *
     * @return the value of SVCSDCCH.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public String getDataattribute() {
        return dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.DATAATTRIBUTE
     *
     * @param dataattribute the value for SVCSDCCH.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public void setDataattribute(String dataattribute) {
        this.dataattribute = dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.EASYNO
     *
     * @return the value of SVCSDCCH.EASYNO
     *
     * @mbg.generated
     */
    public String getEasyno() {
        return easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.EASYNO
     *
     * @param easyno the value for SVCSDCCH.EASYNO
     *
     * @mbg.generated
     */
    public void setEasyno(String easyno) {
        this.easyno = easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.TXDATE
     *
     * @return the value of SVCSDCCH.TXDATE
     *
     * @mbg.generated
     */
    public String getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.TXDATE
     *
     * @param txdate the value for SVCSDCCH.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.TXTIME
     *
     * @return the value of SVCSDCCH.TXTIME
     *
     * @mbg.generated
     */
    public String getTxtime() {
        return txtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.TXTIME
     *
     * @param txtime the value for SVCSDCCH.TXTIME
     *
     * @mbg.generated
     */
    public void setTxtime(String txtime) {
        this.txtime = txtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.TXAMOUNT
     *
     * @return the value of SVCSDCCH.TXAMOUNT
     *
     * @mbg.generated
     */
    public Long getTxamount() {
        return txamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.TXAMOUNT
     *
     * @param txamount the value for SVCSDCCH.TXAMOUNT
     *
     * @mbg.generated
     */
    public void setTxamount(Long txamount) {
        this.txamount = txamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.EASYBALANCE
     *
     * @return the value of SVCSDCCH.EASYBALANCE
     *
     * @mbg.generated
     */
    public Long getEasybalance() {
        return easybalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.EASYBALANCE
     *
     * @param easybalance the value for SVCSDCCH.EASYBALANCE
     *
     * @mbg.generated
     */
    public void setEasybalance(Long easybalance) {
        this.easybalance = easybalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.HASH
     *
     * @return the value of SVCSDCCH.HASH
     *
     * @mbg.generated
     */
    public String getHash() {
        return hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.HASH
     *
     * @param hash the value for SVCSDCCH.HASH
     *
     * @mbg.generated
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.ERRCODE
     *
     * @return the value of SVCSDCCH.ERRCODE
     *
     * @mbg.generated
     */
    public String getErrcode() {
        return errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.ERRCODE
     *
     * @param errcode the value for SVCSDCCH.ERRCODE
     *
     * @mbg.generated
     */
    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.FILEDATA
     *
     * @return the value of SVCSDCCH.FILEDATA
     *
     * @mbg.generated
     */
    public String getFiledata() {
        return filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.FILEDATA
     *
     * @param filedata the value for SVCSDCCH.FILEDATA
     *
     * @mbg.generated
     */
    public void setFiledata(String filedata) {
        this.filedata = filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.UPDATEUSERID
     *
     * @return the value of SVCSDCCH.UPDATEUSERID
     *
     * @mbg.generated
     */
    public Integer getUpdateuserid() {
        return updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.UPDATEUSERID
     *
     * @param updateuserid the value for SVCSDCCH.UPDATEUSERID
     *
     * @mbg.generated
     */
    public void setUpdateuserid(Integer updateuserid) {
        this.updateuserid = updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCCH.UPDATETIME
     *
     * @return the value of SVCSDCCH.UPDATETIME
     *
     * @mbg.generated
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCCH.UPDATETIME
     *
     * @param updatetime the value for SVCSDCCH.UPDATETIME
     *
     * @mbg.generated
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCH
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
        sb.append(", txdate=").append(txdate);
        sb.append(", txtime=").append(txtime);
        sb.append(", txamount=").append(txamount);
        sb.append(", easybalance=").append(easybalance);
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
     * This method corresponds to the database table SVCSDCCH
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
        Svcsdcch other = (Svcsdcch) that;
        return (this.getSeqno() == null ? other.getSeqno() == null : this.getSeqno().equals(other.getSeqno()))
            && (this.getCaldate() == null ? other.getCaldate() == null : this.getCaldate().equals(other.getCaldate()))
            && (this.getFilename() == null ? other.getFilename() == null : this.getFilename().equals(other.getFilename()))
            && (this.getFileheader() == null ? other.getFileheader() == null : this.getFileheader().equals(other.getFileheader()))
            && (this.getDataattribute() == null ? other.getDataattribute() == null : this.getDataattribute().equals(other.getDataattribute()))
            && (this.getEasyno() == null ? other.getEasyno() == null : this.getEasyno().equals(other.getEasyno()))
            && (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getTxtime() == null ? other.getTxtime() == null : this.getTxtime().equals(other.getTxtime()))
            && (this.getTxamount() == null ? other.getTxamount() == null : this.getTxamount().equals(other.getTxamount()))
            && (this.getEasybalance() == null ? other.getEasybalance() == null : this.getEasybalance().equals(other.getEasybalance()))
            && (this.getHash() == null ? other.getHash() == null : this.getHash().equals(other.getHash()))
            && (this.getErrcode() == null ? other.getErrcode() == null : this.getErrcode().equals(other.getErrcode()))
            && (this.getFiledata() == null ? other.getFiledata() == null : this.getFiledata().equals(other.getFiledata()))
            && (this.getUpdateuserid() == null ? other.getUpdateuserid() == null : this.getUpdateuserid().equals(other.getUpdateuserid()))
            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCH
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
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getTxtime() == null) ? 0 : getTxtime().hashCode());
        result = 31 * result + ((getTxamount() == null) ? 0 : getTxamount().hashCode());
        result = 31 * result + ((getEasybalance() == null) ? 0 : getEasybalance().hashCode());
        result = 31 * result + ((getHash() == null) ? 0 : getHash().hashCode());
        result = 31 * result + ((getErrcode() == null) ? 0 : getErrcode().hashCode());
        result = 31 * result + ((getFiledata() == null) ? 0 : getFiledata().hashCode());
        result = 31 * result + ((getUpdateuserid() == null) ? 0 : getUpdateuserid().hashCode());
        result = 31 * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCCH
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
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<TXTIME>").append(this.txtime).append("</TXTIME>");
        sb.append("<TXAMOUNT>").append(this.txamount).append("</TXAMOUNT>");
        sb.append("<EASYBALANCE>").append(this.easybalance).append("</EASYBALANCE>");
        sb.append("<HASH>").append(this.hash).append("</HASH>");
        sb.append("<ERRCODE>").append(this.errcode).append("</ERRCODE>");
        sb.append("<FILEDATA>").append(this.filedata).append("</FILEDATA>");
        sb.append("<UPDATEUSERID>").append(this.updateuserid).append("</UPDATEUSERID>");
        sb.append("<UPDATETIME>").append(this.updatetime).append("</UPDATETIME>");
        return sb.toString();
    }
}