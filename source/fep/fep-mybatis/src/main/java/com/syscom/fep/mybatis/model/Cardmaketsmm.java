package com.syscom.fep.mybatis.model;

import com.syscom.fep.mybatis.vo.BaseModel;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class Cardmaketsmm extends BaseModel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.TXDATE
     *
     * @mbg.generated
     */
    private String txdate = StringUtils.SPACE;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.BATCHSEQ
     *
     * @mbg.generated
     */
    private Integer batchseq;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.PAYMENTSCHEME
     *
     * @mbg.generated
     */
    private String paymentscheme;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.FILEVERSION
     *
     * @mbg.generated
     */
    private String fileversion;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.INPUTFILENAME
     *
     * @mbg.generated
     */
    private String inputfilename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.OUTPUTFILENAME
     *
     * @mbg.generated
     */
    private String outputfilename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.FILETOTALRECORD
     *
     * @mbg.generated
     */
    private Integer filetotalrecord;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.DIVERSIFYKEYFLAG
     *
     * @mbg.generated
     */
    private String diversifykeyflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.TESTINGPRODUCTIONFLAG
     *
     * @mbg.generated
     */
    private String testingproductionflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.EMVIVCVC3FLAG
     *
     * @mbg.generated
     */
    private String emvivcvc3flag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.EFFECTIVEDATEFLAG
     *
     * @mbg.generated
     */
    private String effectivedateflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.FISCEF1003FLAG
     *
     * @mbg.generated
     */
    private String fiscef1003flag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.FISCSTOREVALUEPURSEFLAG
     *
     * @mbg.generated
     */
    private String fiscstorevaluepurseflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.FISCDIVERSIFYINPUTFLAG
     *
     * @mbg.generated
     */
    private String fiscdiversifyinputflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.FISCOTPFLAG
     *
     * @mbg.generated
     */
    private String fiscotpflag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.OUTPUTDATE
     *
     * @mbg.generated
     */
    private String outputdate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.STATUSCODE
     *
     * @mbg.generated
     */
    private String statuscode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.SUCCESSRECORDS
     *
     * @mbg.generated
     */
    private Integer successrecords;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.FAILRECORDS
     *
     * @mbg.generated
     */
    private Integer failrecords;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.UPDATE_USERID
     *
     * @mbg.generated
     */
    private Integer updateUserid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column CARDMAKETSMM.UPDATE_TIME
     *
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table CARDMAKETSMM
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMM
     *
     * @mbg.generated
     */
    public Cardmaketsmm(String txdate, Integer batchseq, String paymentscheme, String fileversion, String inputfilename, String outputfilename, Integer filetotalrecord, String diversifykeyflag, String testingproductionflag, String emvivcvc3flag, String effectivedateflag, String fiscef1003flag, String fiscstorevaluepurseflag, String fiscdiversifyinputflag, String fiscotpflag, String outputdate, String statuscode, Integer successrecords, Integer failrecords, Integer updateUserid, Date updateTime) {
        this.txdate = txdate;
        this.batchseq = batchseq;
        this.paymentscheme = paymentscheme;
        this.fileversion = fileversion;
        this.inputfilename = inputfilename;
        this.outputfilename = outputfilename;
        this.filetotalrecord = filetotalrecord;
        this.diversifykeyflag = diversifykeyflag;
        this.testingproductionflag = testingproductionflag;
        this.emvivcvc3flag = emvivcvc3flag;
        this.effectivedateflag = effectivedateflag;
        this.fiscef1003flag = fiscef1003flag;
        this.fiscstorevaluepurseflag = fiscstorevaluepurseflag;
        this.fiscdiversifyinputflag = fiscdiversifyinputflag;
        this.fiscotpflag = fiscotpflag;
        this.outputdate = outputdate;
        this.statuscode = statuscode;
        this.successrecords = successrecords;
        this.failrecords = failrecords;
        this.updateUserid = updateUserid;
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMM
     *
     * @mbg.generated
     */
    public Cardmaketsmm() {
        super();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.TXDATE
     *
     * @return the value of CARDMAKETSMM.TXDATE
     *
     * @mbg.generated
     */
    public String getTxdate() {
        return txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.TXDATE
     *
     * @param txdate the value for CARDMAKETSMM.TXDATE
     *
     * @mbg.generated
     */
    public void setTxdate(String txdate) {
        this.txdate = txdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.BATCHSEQ
     *
     * @return the value of CARDMAKETSMM.BATCHSEQ
     *
     * @mbg.generated
     */
    public Integer getBatchseq() {
        return batchseq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.BATCHSEQ
     *
     * @param batchseq the value for CARDMAKETSMM.BATCHSEQ
     *
     * @mbg.generated
     */
    public void setBatchseq(Integer batchseq) {
        this.batchseq = batchseq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.PAYMENTSCHEME
     *
     * @return the value of CARDMAKETSMM.PAYMENTSCHEME
     *
     * @mbg.generated
     */
    public String getPaymentscheme() {
        return paymentscheme;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.PAYMENTSCHEME
     *
     * @param paymentscheme the value for CARDMAKETSMM.PAYMENTSCHEME
     *
     * @mbg.generated
     */
    public void setPaymentscheme(String paymentscheme) {
        this.paymentscheme = paymentscheme;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.FILEVERSION
     *
     * @return the value of CARDMAKETSMM.FILEVERSION
     *
     * @mbg.generated
     */
    public String getFileversion() {
        return fileversion;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.FILEVERSION
     *
     * @param fileversion the value for CARDMAKETSMM.FILEVERSION
     *
     * @mbg.generated
     */
    public void setFileversion(String fileversion) {
        this.fileversion = fileversion;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.INPUTFILENAME
     *
     * @return the value of CARDMAKETSMM.INPUTFILENAME
     *
     * @mbg.generated
     */
    public String getInputfilename() {
        return inputfilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.INPUTFILENAME
     *
     * @param inputfilename the value for CARDMAKETSMM.INPUTFILENAME
     *
     * @mbg.generated
     */
    public void setInputfilename(String inputfilename) {
        this.inputfilename = inputfilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.OUTPUTFILENAME
     *
     * @return the value of CARDMAKETSMM.OUTPUTFILENAME
     *
     * @mbg.generated
     */
    public String getOutputfilename() {
        return outputfilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.OUTPUTFILENAME
     *
     * @param outputfilename the value for CARDMAKETSMM.OUTPUTFILENAME
     *
     * @mbg.generated
     */
    public void setOutputfilename(String outputfilename) {
        this.outputfilename = outputfilename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.FILETOTALRECORD
     *
     * @return the value of CARDMAKETSMM.FILETOTALRECORD
     *
     * @mbg.generated
     */
    public Integer getFiletotalrecord() {
        return filetotalrecord;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.FILETOTALRECORD
     *
     * @param filetotalrecord the value for CARDMAKETSMM.FILETOTALRECORD
     *
     * @mbg.generated
     */
    public void setFiletotalrecord(Integer filetotalrecord) {
        this.filetotalrecord = filetotalrecord;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.DIVERSIFYKEYFLAG
     *
     * @return the value of CARDMAKETSMM.DIVERSIFYKEYFLAG
     *
     * @mbg.generated
     */
    public String getDiversifykeyflag() {
        return diversifykeyflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.DIVERSIFYKEYFLAG
     *
     * @param diversifykeyflag the value for CARDMAKETSMM.DIVERSIFYKEYFLAG
     *
     * @mbg.generated
     */
    public void setDiversifykeyflag(String diversifykeyflag) {
        this.diversifykeyflag = diversifykeyflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.TESTINGPRODUCTIONFLAG
     *
     * @return the value of CARDMAKETSMM.TESTINGPRODUCTIONFLAG
     *
     * @mbg.generated
     */
    public String getTestingproductionflag() {
        return testingproductionflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.TESTINGPRODUCTIONFLAG
     *
     * @param testingproductionflag the value for CARDMAKETSMM.TESTINGPRODUCTIONFLAG
     *
     * @mbg.generated
     */
    public void setTestingproductionflag(String testingproductionflag) {
        this.testingproductionflag = testingproductionflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.EMVIVCVC3FLAG
     *
     * @return the value of CARDMAKETSMM.EMVIVCVC3FLAG
     *
     * @mbg.generated
     */
    public String getEmvivcvc3flag() {
        return emvivcvc3flag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.EMVIVCVC3FLAG
     *
     * @param emvivcvc3flag the value for CARDMAKETSMM.EMVIVCVC3FLAG
     *
     * @mbg.generated
     */
    public void setEmvivcvc3flag(String emvivcvc3flag) {
        this.emvivcvc3flag = emvivcvc3flag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.EFFECTIVEDATEFLAG
     *
     * @return the value of CARDMAKETSMM.EFFECTIVEDATEFLAG
     *
     * @mbg.generated
     */
    public String getEffectivedateflag() {
        return effectivedateflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.EFFECTIVEDATEFLAG
     *
     * @param effectivedateflag the value for CARDMAKETSMM.EFFECTIVEDATEFLAG
     *
     * @mbg.generated
     */
    public void setEffectivedateflag(String effectivedateflag) {
        this.effectivedateflag = effectivedateflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.FISCEF1003FLAG
     *
     * @return the value of CARDMAKETSMM.FISCEF1003FLAG
     *
     * @mbg.generated
     */
    public String getFiscef1003flag() {
        return fiscef1003flag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.FISCEF1003FLAG
     *
     * @param fiscef1003flag the value for CARDMAKETSMM.FISCEF1003FLAG
     *
     * @mbg.generated
     */
    public void setFiscef1003flag(String fiscef1003flag) {
        this.fiscef1003flag = fiscef1003flag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.FISCSTOREVALUEPURSEFLAG
     *
     * @return the value of CARDMAKETSMM.FISCSTOREVALUEPURSEFLAG
     *
     * @mbg.generated
     */
    public String getFiscstorevaluepurseflag() {
        return fiscstorevaluepurseflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.FISCSTOREVALUEPURSEFLAG
     *
     * @param fiscstorevaluepurseflag the value for CARDMAKETSMM.FISCSTOREVALUEPURSEFLAG
     *
     * @mbg.generated
     */
    public void setFiscstorevaluepurseflag(String fiscstorevaluepurseflag) {
        this.fiscstorevaluepurseflag = fiscstorevaluepurseflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.FISCDIVERSIFYINPUTFLAG
     *
     * @return the value of CARDMAKETSMM.FISCDIVERSIFYINPUTFLAG
     *
     * @mbg.generated
     */
    public String getFiscdiversifyinputflag() {
        return fiscdiversifyinputflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.FISCDIVERSIFYINPUTFLAG
     *
     * @param fiscdiversifyinputflag the value for CARDMAKETSMM.FISCDIVERSIFYINPUTFLAG
     *
     * @mbg.generated
     */
    public void setFiscdiversifyinputflag(String fiscdiversifyinputflag) {
        this.fiscdiversifyinputflag = fiscdiversifyinputflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.FISCOTPFLAG
     *
     * @return the value of CARDMAKETSMM.FISCOTPFLAG
     *
     * @mbg.generated
     */
    public String getFiscotpflag() {
        return fiscotpflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.FISCOTPFLAG
     *
     * @param fiscotpflag the value for CARDMAKETSMM.FISCOTPFLAG
     *
     * @mbg.generated
     */
    public void setFiscotpflag(String fiscotpflag) {
        this.fiscotpflag = fiscotpflag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.OUTPUTDATE
     *
     * @return the value of CARDMAKETSMM.OUTPUTDATE
     *
     * @mbg.generated
     */
    public String getOutputdate() {
        return outputdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.OUTPUTDATE
     *
     * @param outputdate the value for CARDMAKETSMM.OUTPUTDATE
     *
     * @mbg.generated
     */
    public void setOutputdate(String outputdate) {
        this.outputdate = outputdate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.STATUSCODE
     *
     * @return the value of CARDMAKETSMM.STATUSCODE
     *
     * @mbg.generated
     */
    public String getStatuscode() {
        return statuscode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.STATUSCODE
     *
     * @param statuscode the value for CARDMAKETSMM.STATUSCODE
     *
     * @mbg.generated
     */
    public void setStatuscode(String statuscode) {
        this.statuscode = statuscode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.SUCCESSRECORDS
     *
     * @return the value of CARDMAKETSMM.SUCCESSRECORDS
     *
     * @mbg.generated
     */
    public Integer getSuccessrecords() {
        return successrecords;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.SUCCESSRECORDS
     *
     * @param successrecords the value for CARDMAKETSMM.SUCCESSRECORDS
     *
     * @mbg.generated
     */
    public void setSuccessrecords(Integer successrecords) {
        this.successrecords = successrecords;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.FAILRECORDS
     *
     * @return the value of CARDMAKETSMM.FAILRECORDS
     *
     * @mbg.generated
     */
    public Integer getFailrecords() {
        return failrecords;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.FAILRECORDS
     *
     * @param failrecords the value for CARDMAKETSMM.FAILRECORDS
     *
     * @mbg.generated
     */
    public void setFailrecords(Integer failrecords) {
        this.failrecords = failrecords;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.UPDATE_USERID
     *
     * @return the value of CARDMAKETSMM.UPDATE_USERID
     *
     * @mbg.generated
     */
    public Integer getUpdateUserid() {
        return updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.UPDATE_USERID
     *
     * @param updateUserid the value for CARDMAKETSMM.UPDATE_USERID
     *
     * @mbg.generated
     */
    public void setUpdateUserid(Integer updateUserid) {
        this.updateUserid = updateUserid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column CARDMAKETSMM.UPDATE_TIME
     *
     * @return the value of CARDMAKETSMM.UPDATE_TIME
     *
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column CARDMAKETSMM.UPDATE_TIME
     *
     * @param updateTime the value for CARDMAKETSMM.UPDATE_TIME
     *
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMM
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", txdate=").append(txdate);
        sb.append(", batchseq=").append(batchseq);
        sb.append(", paymentscheme=").append(paymentscheme);
        sb.append(", fileversion=").append(fileversion);
        sb.append(", inputfilename=").append(inputfilename);
        sb.append(", outputfilename=").append(outputfilename);
        sb.append(", filetotalrecord=").append(filetotalrecord);
        sb.append(", diversifykeyflag=").append(diversifykeyflag);
        sb.append(", testingproductionflag=").append(testingproductionflag);
        sb.append(", emvivcvc3flag=").append(emvivcvc3flag);
        sb.append(", effectivedateflag=").append(effectivedateflag);
        sb.append(", fiscef1003flag=").append(fiscef1003flag);
        sb.append(", fiscstorevaluepurseflag=").append(fiscstorevaluepurseflag);
        sb.append(", fiscdiversifyinputflag=").append(fiscdiversifyinputflag);
        sb.append(", fiscotpflag=").append(fiscotpflag);
        sb.append(", outputdate=").append(outputdate);
        sb.append(", statuscode=").append(statuscode);
        sb.append(", successrecords=").append(successrecords);
        sb.append(", failrecords=").append(failrecords);
        sb.append(", updateUserid=").append(updateUserid);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMM
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
        Cardmaketsmm other = (Cardmaketsmm) that;
        return (this.getTxdate() == null ? other.getTxdate() == null : this.getTxdate().equals(other.getTxdate()))
            && (this.getBatchseq() == null ? other.getBatchseq() == null : this.getBatchseq().equals(other.getBatchseq()))
            && (this.getPaymentscheme() == null ? other.getPaymentscheme() == null : this.getPaymentscheme().equals(other.getPaymentscheme()))
            && (this.getFileversion() == null ? other.getFileversion() == null : this.getFileversion().equals(other.getFileversion()))
            && (this.getInputfilename() == null ? other.getInputfilename() == null : this.getInputfilename().equals(other.getInputfilename()))
            && (this.getOutputfilename() == null ? other.getOutputfilename() == null : this.getOutputfilename().equals(other.getOutputfilename()))
            && (this.getFiletotalrecord() == null ? other.getFiletotalrecord() == null : this.getFiletotalrecord().equals(other.getFiletotalrecord()))
            && (this.getDiversifykeyflag() == null ? other.getDiversifykeyflag() == null : this.getDiversifykeyflag().equals(other.getDiversifykeyflag()))
            && (this.getTestingproductionflag() == null ? other.getTestingproductionflag() == null : this.getTestingproductionflag().equals(other.getTestingproductionflag()))
            && (this.getEmvivcvc3flag() == null ? other.getEmvivcvc3flag() == null : this.getEmvivcvc3flag().equals(other.getEmvivcvc3flag()))
            && (this.getEffectivedateflag() == null ? other.getEffectivedateflag() == null : this.getEffectivedateflag().equals(other.getEffectivedateflag()))
            && (this.getFiscef1003flag() == null ? other.getFiscef1003flag() == null : this.getFiscef1003flag().equals(other.getFiscef1003flag()))
            && (this.getFiscstorevaluepurseflag() == null ? other.getFiscstorevaluepurseflag() == null : this.getFiscstorevaluepurseflag().equals(other.getFiscstorevaluepurseflag()))
            && (this.getFiscdiversifyinputflag() == null ? other.getFiscdiversifyinputflag() == null : this.getFiscdiversifyinputflag().equals(other.getFiscdiversifyinputflag()))
            && (this.getFiscotpflag() == null ? other.getFiscotpflag() == null : this.getFiscotpflag().equals(other.getFiscotpflag()))
            && (this.getOutputdate() == null ? other.getOutputdate() == null : this.getOutputdate().equals(other.getOutputdate()))
            && (this.getStatuscode() == null ? other.getStatuscode() == null : this.getStatuscode().equals(other.getStatuscode()))
            && (this.getSuccessrecords() == null ? other.getSuccessrecords() == null : this.getSuccessrecords().equals(other.getSuccessrecords()))
            && (this.getFailrecords() == null ? other.getFailrecords() == null : this.getFailrecords().equals(other.getFailrecords()))
            && (this.getUpdateUserid() == null ? other.getUpdateUserid() == null : this.getUpdateUserid().equals(other.getUpdateUserid()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMM
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((getTxdate() == null) ? 0 : getTxdate().hashCode());
        result = 31 * result + ((getBatchseq() == null) ? 0 : getBatchseq().hashCode());
        result = 31 * result + ((getPaymentscheme() == null) ? 0 : getPaymentscheme().hashCode());
        result = 31 * result + ((getFileversion() == null) ? 0 : getFileversion().hashCode());
        result = 31 * result + ((getInputfilename() == null) ? 0 : getInputfilename().hashCode());
        result = 31 * result + ((getOutputfilename() == null) ? 0 : getOutputfilename().hashCode());
        result = 31 * result + ((getFiletotalrecord() == null) ? 0 : getFiletotalrecord().hashCode());
        result = 31 * result + ((getDiversifykeyflag() == null) ? 0 : getDiversifykeyflag().hashCode());
        result = 31 * result + ((getTestingproductionflag() == null) ? 0 : getTestingproductionflag().hashCode());
        result = 31 * result + ((getEmvivcvc3flag() == null) ? 0 : getEmvivcvc3flag().hashCode());
        result = 31 * result + ((getEffectivedateflag() == null) ? 0 : getEffectivedateflag().hashCode());
        result = 31 * result + ((getFiscef1003flag() == null) ? 0 : getFiscef1003flag().hashCode());
        result = 31 * result + ((getFiscstorevaluepurseflag() == null) ? 0 : getFiscstorevaluepurseflag().hashCode());
        result = 31 * result + ((getFiscdiversifyinputflag() == null) ? 0 : getFiscdiversifyinputflag().hashCode());
        result = 31 * result + ((getFiscotpflag() == null) ? 0 : getFiscotpflag().hashCode());
        result = 31 * result + ((getOutputdate() == null) ? 0 : getOutputdate().hashCode());
        result = 31 * result + ((getStatuscode() == null) ? 0 : getStatuscode().hashCode());
        result = 31 * result + ((getSuccessrecords() == null) ? 0 : getSuccessrecords().hashCode());
        result = 31 * result + ((getFailrecords() == null) ? 0 : getFailrecords().hashCode());
        result = 31 * result + ((getUpdateUserid() == null) ? 0 : getUpdateUserid().hashCode());
        result = 31 * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table CARDMAKETSMM
     * 
     * @mbg.generated
     */
    @Override
    public String fieldsToXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<TXDATE>").append(this.txdate).append("</TXDATE>");
        sb.append("<BATCHSEQ>").append(this.batchseq).append("</BATCHSEQ>");
        sb.append("<PAYMENTSCHEME>").append(this.paymentscheme).append("</PAYMENTSCHEME>");
        sb.append("<FILEVERSION>").append(this.fileversion).append("</FILEVERSION>");
        sb.append("<INPUTFILENAME>").append(this.inputfilename).append("</INPUTFILENAME>");
        sb.append("<OUTPUTFILENAME>").append(this.outputfilename).append("</OUTPUTFILENAME>");
        sb.append("<FILETOTALRECORD>").append(this.filetotalrecord).append("</FILETOTALRECORD>");
        sb.append("<DIVERSIFYKEYFLAG>").append(this.diversifykeyflag).append("</DIVERSIFYKEYFLAG>");
        sb.append("<TESTINGPRODUCTIONFLAG>").append(this.testingproductionflag).append("</TESTINGPRODUCTIONFLAG>");
        sb.append("<EMVIVCVC3FLAG>").append(this.emvivcvc3flag).append("</EMVIVCVC3FLAG>");
        sb.append("<EFFECTIVEDATEFLAG>").append(this.effectivedateflag).append("</EFFECTIVEDATEFLAG>");
        sb.append("<FISCEF1003FLAG>").append(this.fiscef1003flag).append("</FISCEF1003FLAG>");
        sb.append("<FISCSTOREVALUEPURSEFLAG>").append(this.fiscstorevaluepurseflag).append("</FISCSTOREVALUEPURSEFLAG>");
        sb.append("<FISCDIVERSIFYINPUTFLAG>").append(this.fiscdiversifyinputflag).append("</FISCDIVERSIFYINPUTFLAG>");
        sb.append("<FISCOTPFLAG>").append(this.fiscotpflag).append("</FISCOTPFLAG>");
        sb.append("<OUTPUTDATE>").append(this.outputdate).append("</OUTPUTDATE>");
        sb.append("<STATUSCODE>").append(this.statuscode).append("</STATUSCODE>");
        sb.append("<SUCCESSRECORDS>").append(this.successrecords).append("</SUCCESSRECORDS>");
        sb.append("<FAILRECORDS>").append(this.failrecords).append("</FAILRECORDS>");
        sb.append("<UPDATE_USERID>").append(this.updateUserid).append("</UPDATE_USERID>");
        sb.append("<UPDATE_TIME>").append(this.updateTime).append("</UPDATE_TIME>");
        return sb.toString();
    }
}