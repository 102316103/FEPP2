package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;

public class Svcsdcrp extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.SEQNO
     *
     * @mbg.generated
     */
    private Long seqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.CALDATE
     *
     * @mbg.generated
     */
    private String caldate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.FILENAME
     *
     * @mbg.generated
     */
    private String filename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.FILEHEADER
     *
     * @mbg.generated
     */
    private String fileheader;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    private String dataattribute;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.TXTYPE
     *
     * @mbg.generated
     */
    private String txtype;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.EASYNO
     *
     * @mbg.generated
     */
    private String easyno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.EASYENDYYMM
     *
     * @mbg.generated
     */
    private String easyendyymm;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.ICSEQNO
     *
     * @mbg.generated
     */
    private String icseqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.EASYNOR
     *
     * @mbg.generated
     */
    private String easynor;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.TXREASON
     *
     * @mbg.generated
     */
    private String txreason;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.ORIGINALEASYNO
     *
     * @mbg.generated
     */
    private String originaleasyno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.CARDSTATUS
     *
     * @mbg.generated
     */
    private String cardstatus;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.MAKECARDSTATUS
     *
     * @mbg.generated
     */
    private String makecardstatus;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.ISSUEBANKID
     *
     * @mbg.generated
     */
    private String issuebankid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.MAKEDATE
     *
     * @mbg.generated
     */
    private String makedate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.EASYBALANCE
     *
     * @mbg.generated
     */
    private Integer easybalance;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.EASYDEPOSIT
     *
     * @mbg.generated
     */
    private Integer easydeposit;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.CARDSUPPLIERNO
     *
     * @mbg.generated
     */
    private String cardsupplierno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.ISAMICSEQNO
     *
     * @mbg.generated
     */
    private String isamicseqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.ISAMBATCHNO
     *
     * @mbg.generated
     */
    private String isambatchno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.ISAMCARDSEQNO
     *
     * @mbg.generated
     */
    private Integer isamcardseqno;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.AUTOLOADAMOUNT
     *
     * @mbg.generated
     */
    private Integer autoloadamount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.HASH
     *
     * @mbg.generated
     */
    private String hash;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.ERRCODE
     *
     * @mbg.generated
     */
    private String errcode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.FILEDATA
     *
     * @mbg.generated
     */
    private String filedata;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.UPDATEUSERID
     *
     * @mbg.generated
     */
    private Integer updateuserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column SVCSDCRP.UPDATETIME
     *
     * @mbg.generated
     */
    private Date updatetime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table SVCSDCRP
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRP
     *
     * @mbg.generated
     */
    public Svcsdcrp(Long seqno, String caldate, String filename, String fileheader, String dataattribute, String txtype, String easyno, String easyendyymm, String icseqno, String easynor, String txreason, String originaleasyno, String cardstatus, String makecardstatus, String issuebankid, String makedate, Integer easybalance, Integer easydeposit, String cardsupplierno, String isamicseqno, String isambatchno, Integer isamcardseqno, Integer autoloadamount, String hash, String errcode, String filedata, Integer updateuserid, Date updatetime) {
        this.seqno = seqno;
        this.caldate = caldate;
        this.filename = filename;
        this.fileheader = fileheader;
        this.dataattribute = dataattribute;
        this.txtype = txtype;
        this.easyno = easyno;
        this.easyendyymm = easyendyymm;
        this.icseqno = icseqno;
        this.easynor = easynor;
        this.txreason = txreason;
        this.originaleasyno = originaleasyno;
        this.cardstatus = cardstatus;
        this.makecardstatus = makecardstatus;
        this.issuebankid = issuebankid;
        this.makedate = makedate;
        this.easybalance = easybalance;
        this.easydeposit = easydeposit;
        this.cardsupplierno = cardsupplierno;
        this.isamicseqno = isamicseqno;
        this.isambatchno = isambatchno;
        this.isamcardseqno = isamcardseqno;
        this.autoloadamount = autoloadamount;
        this.hash = hash;
        this.errcode = errcode;
        this.filedata = filedata;
        this.updateuserid = updateuserid;
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRP
     *
     * @mbg.generated
     */
    public Svcsdcrp() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.SEQNO
     *
     * @return the value of SVCSDCRP.SEQNO
     *
     * @mbg.generated
     */
    public Long getSeqno() {
        return seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.SEQNO
     *
     * @param seqno the value for SVCSDCRP.SEQNO
     *
     * @mbg.generated
     */
    public void setSeqno(Long seqno) {
        this.seqno = seqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.CALDATE
     *
     * @return the value of SVCSDCRP.CALDATE
     *
     * @mbg.generated
     */
    public String getCaldate() {
        return caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.CALDATE
     *
     * @param caldate the value for SVCSDCRP.CALDATE
     *
     * @mbg.generated
     */
    public void setCaldate(String caldate) {
        this.caldate = caldate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.FILENAME
     *
     * @return the value of SVCSDCRP.FILENAME
     *
     * @mbg.generated
     */
    public String getFilename() {
        return filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.FILENAME
     *
     * @param filename the value for SVCSDCRP.FILENAME
     *
     * @mbg.generated
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.FILEHEADER
     *
     * @return the value of SVCSDCRP.FILEHEADER
     *
     * @mbg.generated
     */
    public String getFileheader() {
        return fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.FILEHEADER
     *
     * @param fileheader the value for SVCSDCRP.FILEHEADER
     *
     * @mbg.generated
     */
    public void setFileheader(String fileheader) {
        this.fileheader = fileheader;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.DATAATTRIBUTE
     *
     * @return the value of SVCSDCRP.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public String getDataattribute() {
        return dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.DATAATTRIBUTE
     *
     * @param dataattribute the value for SVCSDCRP.DATAATTRIBUTE
     *
     * @mbg.generated
     */
    public void setDataattribute(String dataattribute) {
        this.dataattribute = dataattribute;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.TXTYPE
     *
     * @return the value of SVCSDCRP.TXTYPE
     *
     * @mbg.generated
     */
    public String getTxtype() {
        return txtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.TXTYPE
     *
     * @param txtype the value for SVCSDCRP.TXTYPE
     *
     * @mbg.generated
     */
    public void setTxtype(String txtype) {
        this.txtype = txtype;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.EASYNO
     *
     * @return the value of SVCSDCRP.EASYNO
     *
     * @mbg.generated
     */
    public String getEasyno() {
        return easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.EASYNO
     *
     * @param easyno the value for SVCSDCRP.EASYNO
     *
     * @mbg.generated
     */
    public void setEasyno(String easyno) {
        this.easyno = easyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.EASYENDYYMM
     *
     * @return the value of SVCSDCRP.EASYENDYYMM
     *
     * @mbg.generated
     */
    public String getEasyendyymm() {
        return easyendyymm;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.EASYENDYYMM
     *
     * @param easyendyymm the value for SVCSDCRP.EASYENDYYMM
     *
     * @mbg.generated
     */
    public void setEasyendyymm(String easyendyymm) {
        this.easyendyymm = easyendyymm;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.ICSEQNO
     *
     * @return the value of SVCSDCRP.ICSEQNO
     *
     * @mbg.generated
     */
    public String getIcseqno() {
        return icseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.ICSEQNO
     *
     * @param icseqno the value for SVCSDCRP.ICSEQNO
     *
     * @mbg.generated
     */
    public void setIcseqno(String icseqno) {
        this.icseqno = icseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.EASYNOR
     *
     * @return the value of SVCSDCRP.EASYNOR
     *
     * @mbg.generated
     */
    public String getEasynor() {
        return easynor;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.EASYNOR
     *
     * @param easynor the value for SVCSDCRP.EASYNOR
     *
     * @mbg.generated
     */
    public void setEasynor(String easynor) {
        this.easynor = easynor;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.TXREASON
     *
     * @return the value of SVCSDCRP.TXREASON
     *
     * @mbg.generated
     */
    public String getTxreason() {
        return txreason;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.TXREASON
     *
     * @param txreason the value for SVCSDCRP.TXREASON
     *
     * @mbg.generated
     */
    public void setTxreason(String txreason) {
        this.txreason = txreason;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.ORIGINALEASYNO
     *
     * @return the value of SVCSDCRP.ORIGINALEASYNO
     *
     * @mbg.generated
     */
    public String getOriginaleasyno() {
        return originaleasyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.ORIGINALEASYNO
     *
     * @param originaleasyno the value for SVCSDCRP.ORIGINALEASYNO
     *
     * @mbg.generated
     */
    public void setOriginaleasyno(String originaleasyno) {
        this.originaleasyno = originaleasyno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.CARDSTATUS
     *
     * @return the value of SVCSDCRP.CARDSTATUS
     *
     * @mbg.generated
     */
    public String getCardstatus() {
        return cardstatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.CARDSTATUS
     *
     * @param cardstatus the value for SVCSDCRP.CARDSTATUS
     *
     * @mbg.generated
     */
    public void setCardstatus(String cardstatus) {
        this.cardstatus = cardstatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.MAKECARDSTATUS
     *
     * @return the value of SVCSDCRP.MAKECARDSTATUS
     *
     * @mbg.generated
     */
    public String getMakecardstatus() {
        return makecardstatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.MAKECARDSTATUS
     *
     * @param makecardstatus the value for SVCSDCRP.MAKECARDSTATUS
     *
     * @mbg.generated
     */
    public void setMakecardstatus(String makecardstatus) {
        this.makecardstatus = makecardstatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.ISSUEBANKID
     *
     * @return the value of SVCSDCRP.ISSUEBANKID
     *
     * @mbg.generated
     */
    public String getIssuebankid() {
        return issuebankid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.ISSUEBANKID
     *
     * @param issuebankid the value for SVCSDCRP.ISSUEBANKID
     *
     * @mbg.generated
     */
    public void setIssuebankid(String issuebankid) {
        this.issuebankid = issuebankid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.MAKEDATE
     *
     * @return the value of SVCSDCRP.MAKEDATE
     *
     * @mbg.generated
     */
    public String getMakedate() {
        return makedate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.MAKEDATE
     *
     * @param makedate the value for SVCSDCRP.MAKEDATE
     *
     * @mbg.generated
     */
    public void setMakedate(String makedate) {
        this.makedate = makedate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.EASYBALANCE
     *
     * @return the value of SVCSDCRP.EASYBALANCE
     *
     * @mbg.generated
     */
    public Integer getEasybalance() {
        return easybalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.EASYBALANCE
     *
     * @param easybalance the value for SVCSDCRP.EASYBALANCE
     *
     * @mbg.generated
     */
    public void setEasybalance(Integer easybalance) {
        this.easybalance = easybalance;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.EASYDEPOSIT
     *
     * @return the value of SVCSDCRP.EASYDEPOSIT
     *
     * @mbg.generated
     */
    public Integer getEasydeposit() {
        return easydeposit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.EASYDEPOSIT
     *
     * @param easydeposit the value for SVCSDCRP.EASYDEPOSIT
     *
     * @mbg.generated
     */
    public void setEasydeposit(Integer easydeposit) {
        this.easydeposit = easydeposit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.CARDSUPPLIERNO
     *
     * @return the value of SVCSDCRP.CARDSUPPLIERNO
     *
     * @mbg.generated
     */
    public String getCardsupplierno() {
        return cardsupplierno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.CARDSUPPLIERNO
     *
     * @param cardsupplierno the value for SVCSDCRP.CARDSUPPLIERNO
     *
     * @mbg.generated
     */
    public void setCardsupplierno(String cardsupplierno) {
        this.cardsupplierno = cardsupplierno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.ISAMICSEQNO
     *
     * @return the value of SVCSDCRP.ISAMICSEQNO
     *
     * @mbg.generated
     */
    public String getIsamicseqno() {
        return isamicseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.ISAMICSEQNO
     *
     * @param isamicseqno the value for SVCSDCRP.ISAMICSEQNO
     *
     * @mbg.generated
     */
    public void setIsamicseqno(String isamicseqno) {
        this.isamicseqno = isamicseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.ISAMBATCHNO
     *
     * @return the value of SVCSDCRP.ISAMBATCHNO
     *
     * @mbg.generated
     */
    public String getIsambatchno() {
        return isambatchno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.ISAMBATCHNO
     *
     * @param isambatchno the value for SVCSDCRP.ISAMBATCHNO
     *
     * @mbg.generated
     */
    public void setIsambatchno(String isambatchno) {
        this.isambatchno = isambatchno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.ISAMCARDSEQNO
     *
     * @return the value of SVCSDCRP.ISAMCARDSEQNO
     *
     * @mbg.generated
     */
    public Integer getIsamcardseqno() {
        return isamcardseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.ISAMCARDSEQNO
     *
     * @param isamcardseqno the value for SVCSDCRP.ISAMCARDSEQNO
     *
     * @mbg.generated
     */
    public void setIsamcardseqno(Integer isamcardseqno) {
        this.isamcardseqno = isamcardseqno;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.AUTOLOADAMOUNT
     *
     * @return the value of SVCSDCRP.AUTOLOADAMOUNT
     *
     * @mbg.generated
     */
    public Integer getAutoloadamount() {
        return autoloadamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.AUTOLOADAMOUNT
     *
     * @param autoloadamount the value for SVCSDCRP.AUTOLOADAMOUNT
     *
     * @mbg.generated
     */
    public void setAutoloadamount(Integer autoloadamount) {
        this.autoloadamount = autoloadamount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.HASH
     *
     * @return the value of SVCSDCRP.HASH
     *
     * @mbg.generated
     */
    public String getHash() {
        return hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.HASH
     *
     * @param hash the value for SVCSDCRP.HASH
     *
     * @mbg.generated
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.ERRCODE
     *
     * @return the value of SVCSDCRP.ERRCODE
     *
     * @mbg.generated
     */
    public String getErrcode() {
        return errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.ERRCODE
     *
     * @param errcode the value for SVCSDCRP.ERRCODE
     *
     * @mbg.generated
     */
    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.FILEDATA
     *
     * @return the value of SVCSDCRP.FILEDATA
     *
     * @mbg.generated
     */
    public String getFiledata() {
        return filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.FILEDATA
     *
     * @param filedata the value for SVCSDCRP.FILEDATA
     *
     * @mbg.generated
     */
    public void setFiledata(String filedata) {
        this.filedata = filedata;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.UPDATEUSERID
     *
     * @return the value of SVCSDCRP.UPDATEUSERID
     *
     * @mbg.generated
     */
    public Integer getUpdateuserid() {
        return updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.UPDATEUSERID
     *
     * @param updateuserid the value for SVCSDCRP.UPDATEUSERID
     *
     * @mbg.generated
     */
    public void setUpdateuserid(Integer updateuserid) {
        this.updateuserid = updateuserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column SVCSDCRP.UPDATETIME
     *
     * @return the value of SVCSDCRP.UPDATETIME
     *
     * @mbg.generated
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column SVCSDCRP.UPDATETIME
     *
     * @param updatetime the value for SVCSDCRP.UPDATETIME
     *
     * @mbg.generated
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRP
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
        sb.append(", txtype=").append(txtype);
        sb.append(", easyno=").append(easyno);
        sb.append(", easyendyymm=").append(easyendyymm);
        sb.append(", icseqno=").append(icseqno);
        sb.append(", easynor=").append(easynor);
        sb.append(", txreason=").append(txreason);
        sb.append(", originaleasyno=").append(originaleasyno);
        sb.append(", cardstatus=").append(cardstatus);
        sb.append(", makecardstatus=").append(makecardstatus);
        sb.append(", issuebankid=").append(issuebankid);
        sb.append(", makedate=").append(makedate);
        sb.append(", easybalance=").append(easybalance);
        sb.append(", easydeposit=").append(easydeposit);
        sb.append(", cardsupplierno=").append(cardsupplierno);
        sb.append(", isamicseqno=").append(isamicseqno);
        sb.append(", isambatchno=").append(isambatchno);
        sb.append(", isamcardseqno=").append(isamcardseqno);
        sb.append(", autoloadamount=").append(autoloadamount);
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
     * This method corresponds to the database table SVCSDCRP
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
        Svcsdcrp other = (Svcsdcrp) that;
        return (this.getSeqno() == null ? other.getSeqno() == null : this.getSeqno().equals(other.getSeqno()))
            && (this.getCaldate() == null ? other.getCaldate() == null : this.getCaldate().equals(other.getCaldate()))
            && (this.getFilename() == null ? other.getFilename() == null : this.getFilename().equals(other.getFilename()))
            && (this.getFileheader() == null ? other.getFileheader() == null : this.getFileheader().equals(other.getFileheader()))
            && (this.getDataattribute() == null ? other.getDataattribute() == null : this.getDataattribute().equals(other.getDataattribute()))
            && (this.getTxtype() == null ? other.getTxtype() == null : this.getTxtype().equals(other.getTxtype()))
            && (this.getEasyno() == null ? other.getEasyno() == null : this.getEasyno().equals(other.getEasyno()))
            && (this.getEasyendyymm() == null ? other.getEasyendyymm() == null : this.getEasyendyymm().equals(other.getEasyendyymm()))
            && (this.getIcseqno() == null ? other.getIcseqno() == null : this.getIcseqno().equals(other.getIcseqno()))
            && (this.getEasynor() == null ? other.getEasynor() == null : this.getEasynor().equals(other.getEasynor()))
            && (this.getTxreason() == null ? other.getTxreason() == null : this.getTxreason().equals(other.getTxreason()))
            && (this.getOriginaleasyno() == null ? other.getOriginaleasyno() == null : this.getOriginaleasyno().equals(other.getOriginaleasyno()))
            && (this.getCardstatus() == null ? other.getCardstatus() == null : this.getCardstatus().equals(other.getCardstatus()))
            && (this.getMakecardstatus() == null ? other.getMakecardstatus() == null : this.getMakecardstatus().equals(other.getMakecardstatus()))
            && (this.getIssuebankid() == null ? other.getIssuebankid() == null : this.getIssuebankid().equals(other.getIssuebankid()))
            && (this.getMakedate() == null ? other.getMakedate() == null : this.getMakedate().equals(other.getMakedate()))
            && (this.getEasybalance() == null ? other.getEasybalance() == null : this.getEasybalance().equals(other.getEasybalance()))
            && (this.getEasydeposit() == null ? other.getEasydeposit() == null : this.getEasydeposit().equals(other.getEasydeposit()))
            && (this.getCardsupplierno() == null ? other.getCardsupplierno() == null : this.getCardsupplierno().equals(other.getCardsupplierno()))
            && (this.getIsamicseqno() == null ? other.getIsamicseqno() == null : this.getIsamicseqno().equals(other.getIsamicseqno()))
            && (this.getIsambatchno() == null ? other.getIsambatchno() == null : this.getIsambatchno().equals(other.getIsambatchno()))
            && (this.getIsamcardseqno() == null ? other.getIsamcardseqno() == null : this.getIsamcardseqno().equals(other.getIsamcardseqno()))
            && (this.getAutoloadamount() == null ? other.getAutoloadamount() == null : this.getAutoloadamount().equals(other.getAutoloadamount()))
            && (this.getHash() == null ? other.getHash() == null : this.getHash().equals(other.getHash()))
            && (this.getErrcode() == null ? other.getErrcode() == null : this.getErrcode().equals(other.getErrcode()))
            && (this.getFiledata() == null ? other.getFiledata() == null : this.getFiledata().equals(other.getFiledata()))
            && (this.getUpdateuserid() == null ? other.getUpdateuserid() == null : this.getUpdateuserid().equals(other.getUpdateuserid()))
            && (this.getUpdatetime() == null ? other.getUpdatetime() == null : this.getUpdatetime().equals(other.getUpdatetime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRP
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
        result = 31 * result + ((getTxtype() == null) ? 0 : getTxtype().hashCode());
        result = 31 * result + ((getEasyno() == null) ? 0 : getEasyno().hashCode());
        result = 31 * result + ((getEasyendyymm() == null) ? 0 : getEasyendyymm().hashCode());
        result = 31 * result + ((getIcseqno() == null) ? 0 : getIcseqno().hashCode());
        result = 31 * result + ((getEasynor() == null) ? 0 : getEasynor().hashCode());
        result = 31 * result + ((getTxreason() == null) ? 0 : getTxreason().hashCode());
        result = 31 * result + ((getOriginaleasyno() == null) ? 0 : getOriginaleasyno().hashCode());
        result = 31 * result + ((getCardstatus() == null) ? 0 : getCardstatus().hashCode());
        result = 31 * result + ((getMakecardstatus() == null) ? 0 : getMakecardstatus().hashCode());
        result = 31 * result + ((getIssuebankid() == null) ? 0 : getIssuebankid().hashCode());
        result = 31 * result + ((getMakedate() == null) ? 0 : getMakedate().hashCode());
        result = 31 * result + ((getEasybalance() == null) ? 0 : getEasybalance().hashCode());
        result = 31 * result + ((getEasydeposit() == null) ? 0 : getEasydeposit().hashCode());
        result = 31 * result + ((getCardsupplierno() == null) ? 0 : getCardsupplierno().hashCode());
        result = 31 * result + ((getIsamicseqno() == null) ? 0 : getIsamicseqno().hashCode());
        result = 31 * result + ((getIsambatchno() == null) ? 0 : getIsambatchno().hashCode());
        result = 31 * result + ((getIsamcardseqno() == null) ? 0 : getIsamcardseqno().hashCode());
        result = 31 * result + ((getAutoloadamount() == null) ? 0 : getAutoloadamount().hashCode());
        result = 31 * result + ((getHash() == null) ? 0 : getHash().hashCode());
        result = 31 * result + ((getErrcode() == null) ? 0 : getErrcode().hashCode());
        result = 31 * result + ((getFiledata() == null) ? 0 : getFiledata().hashCode());
        result = 31 * result + ((getUpdateuserid() == null) ? 0 : getUpdateuserid().hashCode());
        result = 31 * result + ((getUpdatetime() == null) ? 0 : getUpdatetime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SVCSDCRP
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
        sb.append("<TXTYPE>").append(this.txtype).append("</TXTYPE>");
        sb.append("<EASYNO>").append(this.easyno).append("</EASYNO>");
        sb.append("<EASYENDYYMM>").append(this.easyendyymm).append("</EASYENDYYMM>");
        sb.append("<ICSEQNO>").append(this.icseqno).append("</ICSEQNO>");
        sb.append("<EASYNOR>").append(this.easynor).append("</EASYNOR>");
        sb.append("<TXREASON>").append(this.txreason).append("</TXREASON>");
        sb.append("<ORIGINALEASYNO>").append(this.originaleasyno).append("</ORIGINALEASYNO>");
        sb.append("<CARDSTATUS>").append(this.cardstatus).append("</CARDSTATUS>");
        sb.append("<MAKECARDSTATUS>").append(this.makecardstatus).append("</MAKECARDSTATUS>");
        sb.append("<ISSUEBANKID>").append(this.issuebankid).append("</ISSUEBANKID>");
        sb.append("<MAKEDATE>").append(this.makedate).append("</MAKEDATE>");
        sb.append("<EASYBALANCE>").append(this.easybalance).append("</EASYBALANCE>");
        sb.append("<EASYDEPOSIT>").append(this.easydeposit).append("</EASYDEPOSIT>");
        sb.append("<CARDSUPPLIERNO>").append(this.cardsupplierno).append("</CARDSUPPLIERNO>");
        sb.append("<ISAMICSEQNO>").append(this.isamicseqno).append("</ISAMICSEQNO>");
        sb.append("<ISAMBATCHNO>").append(this.isambatchno).append("</ISAMBATCHNO>");
        sb.append("<ISAMCARDSEQNO>").append(this.isamcardseqno).append("</ISAMCARDSEQNO>");
        sb.append("<AUTOLOADAMOUNT>").append(this.autoloadamount).append("</AUTOLOADAMOUNT>");
        sb.append("<HASH>").append(this.hash).append("</HASH>");
        sb.append("<ERRCODE>").append(this.errcode).append("</ERRCODE>");
        sb.append("<FILEDATA>").append(this.filedata).append("</FILEDATA>");
        sb.append("<UPDATEUSERID>").append(this.updateuserid).append("</UPDATEUSERID>");
        sb.append("<UPDATETIME>").append(this.updatetime).append("</UPDATETIME>");
        return sb.toString();
    }
}