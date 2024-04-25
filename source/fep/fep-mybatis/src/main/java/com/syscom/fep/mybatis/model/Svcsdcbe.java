package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Svcsdcbe extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.SEQNO
     *
     * @mbg.generated
     */
    private Long seqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.CALDATE
     *
     * @mbg.generated
     */
    private String caldate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.FILENAME
     *
     * @mbg.generated
     */
    private String filename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.FILEHEADER
     *
     * @mbg.generated
     */
    private String fileheader;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    private String dataattribute;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.EASYNO
     *
     * @mbg.generated
     */
    private String easyno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.NOTICEDATE
     *
     * @mbg.generated
     */
    private String noticedate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.TXDATE
     *
     * @mbg.generated
     */
    private String txdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.TXTIME
     *
     * @mbg.generated
     */
    private String txtime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.TXAMOUNT
     *
     * @mbg.generated
     */
    private Long txamount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.TXCORPNAME
     *
     * @mbg.generated
     */
    private String txcorpname;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.TXLOCATIONNAME
     *
     * @mbg.generated
     */
    private String txlocationname;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.HASH
     *
     * @mbg.generated
     */
    private String hash;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.ERRCODE
     *
     * @mbg.generated
     */
    private String errcode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.FILEDATA
     *
     * @mbg.generated
     */
    private String filedata;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.UPDATEUSERID
     *
     * @mbg.generated
     */
    private Integer updateuserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCBE.UPDATETIME
     *
     * @mbg.generated
     */
    private Date updatetime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table SVCSDCBE
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBE
     *
     * @mbg.generated
     */
    public Svcsdcbe(Long seqno, String caldate, String filename, String fileheader, String dataattribute, String easyno, String noticedate, String txdate, String txtime, Long txamount, String txcorpname, String txlocationname, String hash, String errcode, String filedata, Integer updateuserid, Date updatetime) {
        this.seqno = seqno;
        this.caldate = caldate;
        this.filename = filename;
        this.fileheader = fileheader;
        this.dataattribute = dataattribute;
        this.easyno = easyno;
        this.noticedate = noticedate;
        this.txdate = txdate;
        this.txtime = txtime;
        this.txamount = txamount;
        this.txcorpname = txcorpname;
        this.txlocationname = txlocationname;
        this.hash = hash;
        this.errcode = errcode;
        this.filedata = filedata;
        this.updateuserid = updateuserid;
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBE
     *
     * @mbg.generated
     */
    public Svcsdcbe() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.SEQNO
     *
     * @return the value of SVCSDCBE.SEQNO
     *
     * @mbg.generated
     */
    public Long getSeqno() {
        return seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.SEQNO
     *
     * @param seqno the value for SVCSDCBE.SEQNO
     *
     * @mbg.generated
     */
    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.CALDATE
     *
     * @return the value of SVCSDCBE.CALDATE
     *
     * @mbg.generated
     */
    public String getCaldate() {
        return caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.CALDATE
     *
     * @param caldate the value for SVCSDCBE.CALDATE
     *
     * @mbg.generated
     */
    public void setCaldate(String caldate) {
        this.caldate = caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.FILENAME
     *
     * @return the value of SVCSDCBE.FILENAME
     *
     * @mbg.generated
     */
    public String getFilename() {
        return filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.FILENAME
     *
     * @param filename the value for SVCSDCBE.FILENAME
     *
     * @mbg.generated
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.FILEHEADER
     *
     * @return the value of SVCSDCBE.FILEHEADER
     *
     * @mbg.generated
     */
    public String getFileheader() {
        return fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.FILEHEADER
     *
     * @param fileheader the value for SVCSDCBE.FILEHEADER
     *
     * @mbg.generated
     */
    public void setFileheader(String fileheader) {
        this.fileheader = fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.DATAATTRIBUTE
     *
     * @return the value of SVCSDCBE.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public String getDataattribute() {
        return dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.DATAATTRIBUTE
     *
     * @param dataattribute the value for SVCSDCBE.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public void setDataattribute(String dataattribute) {
        this.dataattribute = dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.EASYNO
     *
     * @return the value of SVCSDCBE.EASYNO
     *
     * @mbg.generated
     */
    public String getEasyno() {
        return easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.EASYNO
     *
     * @param easyno the value for SVCSDCBE.EASYNO
     *
     * @mbg.generated
     */
    public void setEasyno(String easyno) {
        this.easyno = easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.NOTICEDATE
     *
     * @return the value of SVCSDCBE.NOTICEDATE
     *
     * @mbg.generated
     */
    public String getNoticedate() {
        return noticedate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.NOTICEDATE
     *
     * @param noticedate the value for SVCSDCBE.NOTICEDATE
     *
     * @mbg.generated
     */
    public void setNoticedate(String noticedate) {
        this.noticedate = noticedate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.TXDATE
     *
     * @return the value of SVCSDCBE.TXDATE
     *
     * @mbg.generated
     */
    public String getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.TXDATE
     *
     * @param txdate the value for SVCSDCBE.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.TXTIME
     *
     * @return the value of SVCSDCBE.TXTIME
     *
     * @mbg.generated
     */
    public String getTxtime() {
        return txtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.TXTIME
     *
     * @param txtime the value for SVCSDCBE.TXTIME
     *
     * @mbg.generated
     */
    public void setTxtime(String txtime) {
        this.txtime = txtime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.TXAMOUNT
     *
     * @return the value of SVCSDCBE.TXAMOUNT
     *
     * @mbg.generated
     */
    public Long getTxamount() {
        return txamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.TXAMOUNT
     *
     * @param txamount the value for SVCSDCBE.TXAMOUNT
     *
     * @mbg.generated
     */
    public void setTxamount(Long txamount) {
        this.txamount = txamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.TXCORPNAME
     *
     * @return the value of SVCSDCBE.TXCORPNAME
     *
     * @mbg.generated
     */
    public String getTxcorpname() {
        return txcorpname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.TXCORPNAME
     *
     * @param txcorpname the value for SVCSDCBE.TXCORPNAME
     *
     * @mbg.generated
     */
    public void setTxcorpname(String txcorpname) {
        this.txcorpname = txcorpname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.TXLOCATIONNAME
     *
     * @return the value of SVCSDCBE.TXLOCATIONNAME
     *
     * @mbg.generated
     */
    public String getTxlocationname() {
        return txlocationname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.TXLOCATIONNAME
     *
     * @param txlocationname the value for SVCSDCBE.TXLOCATIONNAME
     *
     * @mbg.generated
     */
    public void setTxlocationname(String txlocationname) {
        this.txlocationname = txlocationname;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.HASH
     *
     * @return the value of SVCSDCBE.HASH
     *
     * @mbg.generated
     */
    public String getHash() {
        return hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.HASH
     *
     * @param hash the value for SVCSDCBE.HASH
     *
     * @mbg.generated
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.ERRCODE
     *
     * @return the value of SVCSDCBE.ERRCODE
     *
     * @mbg.generated
     */
    public String getErrcode() {
        return errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.ERRCODE
     *
     * @param errcode the value for SVCSDCBE.ERRCODE
     *
     * @mbg.generated
     */
    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.FILEDATA
     *
     * @return the value of SVCSDCBE.FILEDATA
     *
     * @mbg.generated
     */
    public String getFiledata() {
        return filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.FILEDATA
     *
     * @param filedata the value for SVCSDCBE.FILEDATA
     *
     * @mbg.generated
     */
    public void setFiledata(String filedata) {
        this.filedata = filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.UPDATEUSERID
     *
     * @return the value of SVCSDCBE.UPDATEUSERID
     *
     * @mbg.generated
     */
    public Integer getUpdateuserid() {
        return updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.UPDATEUSERID
     *
     * @param updateuserid the value for SVCSDCBE.UPDATEUSERID
     *
     * @mbg.generated
     */
    public void setUpdateuserid(Integer updateuserid) {
        this.updateuserid = updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCBE.UPDATETIME
     *
     * @return the value of SVCSDCBE.UPDATETIME
     *
     * @mbg.generated
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCBE.UPDATETIME
     *
     * @param updatetime the value for SVCSDCBE.UPDATETIME
     *
     * @mbg.generated
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBE
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
        sb.append(", noticedate=").append(noticedate);
        sb.append(", txdate=").append(txdate);
        sb.append(", txtime=").append(txtime);
        sb.append(", txamount=").append(txamount);
        sb.append(", txcorpname=").append(txcorpname);
        sb.append(", txlocationname=").append(txlocationname);
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
     * This method corresponds to the database table SVCSDCBE
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
        Svcsdcbe other = (Svcsdcbe) that;
        return (this.getSeqno() == null ? other.getSeqno() == null : this.getSeqno().equals(other.getSeqno()))
            && (this.getCaldate() == null ? other.getCaldate() == null : this.getCaldate().equals(other.getCaldate()))
            && (this.getFilename() == null ? other.getFilename() == null : this.getFilename().equals(other.getFilename()))
            && (this.getFileheader() == null ? other.getFileheader() == null : this.getFileheader().equals(other.getFileheader()))
            && (this.getDataattribute() == null ? other.getDataattribute() == null : this.getDataattribute().equals(other.getDataattribute()))
            && (this.getEasyno() == null ? other.getEasyno() == null : this.getEasyno().equals(other.getEasyno()))
            && (this.getNoticedate() == null ? other.getNoticedate() == null : this.getNoticedate().equals(other.getNoticedate()))
            && (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getTxtime() == null ? other.getTxtime() == null : this.getTxtime().equals(other.getTxtime()))
            && (this.getTxamount() == null ? other.getTxamount() == null : this.getTxamount().equals(other.getTxamount()))
            && (this.getTxcorpname() == null ? other.getTxcorpname() == null : this.getTxcorpname().equals(other.getTxcorpname()))
            && (this.getTxlocationname() == null ? other.getTxlocationname() == null : this.getTxlocationname().equals(other.getTxlocationname()))
            && (this.getHash() == null ? other.getHash() == null : this.getHash().equals(other.getHash()))
            && (this.getErrcode() == null ? other.getErrcode() == null : this.getErrcode().equals(other.getErrcode()))
            && (this.getFiledata() == null ? other.getFiledata() == null : this.getFiledata().equals(other.getFiledata()))
            && (this.getUpdateuserid() == null ? other.getUpdateuserid() == null : this.getUpdateuserid().equals(other.getUpdateuserid()))
            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBE
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
        result = 31 * result + ((getNoticedate() == null) ? 0 : getNoticedate().hashCode());
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getTxtime() == null) ? 0 : getTxtime().hashCode());
        result = 31 * result + ((getTxamount() == null) ? 0 : getTxamount().hashCode());
        result = 31 * result + ((getTxcorpname() == null) ? 0 : getTxcorpname().hashCode());
        result = 31 * result + ((getTxlocationname() == null) ? 0 : getTxlocationname().hashCode());
        result = 31 * result + ((getHash() == null) ? 0 : getHash().hashCode());
        result = 31 * result + ((getErrcode() == null) ? 0 : getErrcode().hashCode());
        result = 31 * result + ((getFiledata() == null) ? 0 : getFiledata().hashCode());
        result = 31 * result + ((getUpdateuserid() == null) ? 0 : getUpdateuserid().hashCode());
        result = 31 * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCBE
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
        sb.append("<NOTICEDATE>").append(this.noticedate).append("</NOTICEDATE>");
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<TXTIME>").append(this.txtime).append("</TXTIME>");
        sb.append("<TXAMOUNT>").append(this.txamount).append("</TXAMOUNT>");
        sb.append("<TXCORPNAME>").append(this.txcorpname).append("</TXCORPNAME>");
        sb.append("<TXLOCATIONNAME>").append(this.txlocationname).append("</TXLOCATIONNAME>");
        sb.append("<HASH>").append(this.hash).append("</HASH>");
        sb.append("<ERRCODE>").append(this.errcode).append("</ERRCODE>");
        sb.append("<FILEDATA>").append(this.filedata).append("</FILEDATA>");
        sb.append("<UPDATEUSERID>").append(this.updateuserid).append("</UPDATEUSERID>");
        sb.append("<UPDATETIME>").append(this.updatetime).append("</UPDATETIME>");
        return sb.toString();
    }
}