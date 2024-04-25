package com.syscom.fep.vo.text.nb;

import org.apache.commons.lang3.StringUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("soap:Envelope")
public class SEND_VA_GeneralTrans_RS {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:soap")
    private String xmlnsSoap = "http://schemas.xmlsoap.org/soap/envelope/";
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("soap:Header")
    private String header = StringUtils.EMPTY;
    @XStreamAlias("soap:Body")
    private SEND_VA_GeneralTrans_RS_Body body;

    public SEND_VA_GeneralTrans_RS_Body getBody() {
        return body;
    }
    public void setBody(SEND_VA_GeneralTrans_RS_Body body) {
        this.body = body;
    }
    @XStreamAlias("soap:Body")
    public static class SEND_VA_GeneralTrans_RS_Body {
        @XStreamAlias("NS1:MsgRs")
        private SEND_VA_GeneralTrans_RS_Body_MsgRs rs;
        public SEND_VA_GeneralTrans_RS_Body_MsgRs getRs() {
            return rs;
        }
        public void setRs(SEND_VA_GeneralTrans_RS_Body_MsgRs rs) {
            this.rs = rs;
        }
    }
    @XStreamAlias("NS1:MsgRs")
    public static class SEND_VA_GeneralTrans_RS_Body_MsgRs {
        @XStreamAsAttribute
        @XStreamAlias("xmlns:NS1")
        private String xmlnsNs1="http://www.ibm.com.tw/esb";
        @XStreamAlias("Header")
        private SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header;
        @XStreamAlias("SvcRs")
        private SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs svcRs;
        public SEND_VA_GeneralTrans_RS_Body_MsgRs_Header getHeader() {
            return header;
        }
        public void setHeader(SEND_VA_GeneralTrans_RS_Body_MsgRs_Header header) {
            this.header = header;
        }
        public SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs getSvcRs() {
            return svcRs;
        }
        public void setSvcRs(SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs svcRs) {
            this.svcRs = svcRs;
        }
    }

    @XStreamAlias("Header")
    public static class SEND_VA_GeneralTrans_RS_Body_MsgRs_Header {
        private String CLIENTTRACEID;
        private String CHANNEL;
        private String MSGID;
        private String CLIENTDT;
        private String SYSTEMID;
        private String STATUSCODE;
        private String SEVERITY;
        private String STATUSDESC;

        public String getCLIENTTRACEID(){
            return CLIENTTRACEID;
        }
        public void setCLIENTTRACEID(String value) {
            this.CLIENTTRACEID = value;
        }
        public String getCHANNEL(){
            return CHANNEL;
        }
        public void setCHANNEL(String value) {
            this.CHANNEL = value;
        }
        public String getMSGID(){
            return MSGID;
        }
        public void setMSGID(String value) {
            this.MSGID = value;
        }
        public String getCLIENTDT(){
            return CLIENTDT;
        }
        public void setCLIENTDT(String value) {
            this.CLIENTDT = value;
        }
        public String getSYSTEMID(){
            return SYSTEMID;
        }
        public void setSYSTEMID(String value) {
            this.SYSTEMID = value;
        }
        public String getSTATUSCODE(){
            return STATUSCODE;
        }
        public void setSTATUSCODE(String value) {
            this.STATUSCODE = value;
        }
        public String getSEVERITY(){
            return SEVERITY;
        }
        public void setSEVERITY(String value) {
            this.SEVERITY = value;
        }
        public String getSTATUSDESC(){
            return STATUSDESC;
        }
        public void setSTATUSDESC(String value) {
            this.STATUSDESC = value;
        }
    }

    @XStreamAlias("SvcRs")
    public static class SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs {
        @XStreamAlias("REPLYDATA")
        private SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs_ReplyData replydata;
        public SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs_ReplyData getReplyData() {
            return replydata;
        }
        public void setReplyData(SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs_ReplyData replydata) {
            this.replydata = replydata;
        }
        private String OUTDATE;
        private String OUTTIME;
        private String FEP_EJNO;
        private String TXNSTAN;
        private String CUSTOMERID;
        private String TXNTYPE;
        private String FSCODE;
        private String TRANSAMT;
        private String AEIPYTP;
        private String TAXIDNO;
        private String AEIPCRBK;
        private String CLACTNO;
        private String RSPRESULT;

        public String getOUTDATE(){
            return OUTDATE;
        }
        public void setOUTDATE(String value) {
            this.OUTDATE = value;
        }
        public String getOUTTIME(){
            return OUTTIME;
        }
        public void setOUTTIME(String value) {
            this.OUTTIME = value;
        }
        public String getFEP_EJNO(){
            return FEP_EJNO;
        }
        public void setFEP_EJNO(String value) {
            this.FEP_EJNO = value;
        }
        public String getTXNSTAN(){
            return TXNSTAN;
        }
        public void setTXNSTAN(String value) {
            this.TXNSTAN = value;
        }
        public String getCUSTOMERID(){
            return CUSTOMERID;
        }
        public void setCUSTOMERID(String value) {
            this.CUSTOMERID = value;
        }
        public String getTXNTYPE(){
            return TXNTYPE;
        }
        public void setTXNTYPE(String value) {
            this.TXNTYPE = value;
        }
        public String getFSCODE(){
            return FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getTRANSAMT(){
            return TRANSAMT;
        }
        public void setTRANSAMT(String value) {
            this.TRANSAMT = value;
        }
        public String getAEIPYTP(){
            return AEIPYTP;
        }
        public void setAEIPYTP(String value) {
            this.AEIPYTP = value;
        }
        public String getTAXIDNO(){
            return TAXIDNO;
        }
        public void setTAXIDNO(String value) {
            this.TAXIDNO = value;
        }
        public String getAEIPCRBK(){
            return AEIPCRBK;
        }
        public void setAEIPCRBK(String value) {
            this.AEIPCRBK = value;
        }
        public String getCLACTNO(){
            return CLACTNO;
        }
        public void setCLACTNO(String value) {
            this.CLACTNO = value;
        }
        public String getRSPRESULT(){
            return RSPRESULT;
        }
        public void setRSPRESULT(String value) {
            this.RSPRESULT = value;
        }
    }

    @XStreamAlias("REPLYDATA")
    public static class SEND_VA_GeneralTrans_RS_Body_MsgRs_SvcRs_ReplyData {
        private String PAYUNTNO;
        private String TAXTYPE;
        private String PAYFEENO;
        private String FILLER1;
        private String AELFTP;
        private String RESULT;
        private String ACRESULT;
        private String ACSTAT;
        private String AEIPYUES;
        private String CHKCELL;
        private String FILLER;

        public String getPAYUNTNO(){
            return PAYUNTNO;
        }
        public void setPAYUNTNO(String value) {
            this.PAYUNTNO = value;
        }
        public String getTAXTYPE(){
            return TAXTYPE;
        }
        public void setTAXTYPE(String value) {
            this.TAXTYPE = value;
        }
        public String getPAYFEENO(){
            return PAYFEENO;
        }
        public void setPAYFEENO(String value) {
            this.PAYFEENO = value;
        }
        public String getFILLER1(){
            return FILLER1;
        }
        public void setFILLER1(String value) {
            this.FILLER1 = value;
        }
        public String getAELFTP(){
            return AELFTP;
        }
        public void setAELFTP(String value) {
            this.AELFTP = value;
        }
        public String getRESULT(){
            return RESULT;
        }
        public void setRESULT(String value) {
            this.RESULT = value;
        }
        public String getACRESULT(){
            return ACRESULT;
        }
        public void setACRESULT(String value) {
            this.ACRESULT = value;
        }
        public String getACSTAT(){
            return ACSTAT;
        }
        public void setACSTAT(String value) {
            this.ACSTAT = value;
        }
        public String getAEIPYUES(){
            return AEIPYUES;
        }
        public void setAEIPYUES(String value) {
            this.AEIPYUES = value;
        }
        public String getCHKCELL(){
            return CHKCELL;
        }
        public void setCHKCELL(String value) {
            this.CHKCELL = value;
        }
        public String getFILLER(){
            return FILLER;
        }
        public void setFILLER(String value) {
            this.FILLER = value;
        }
    }
}
