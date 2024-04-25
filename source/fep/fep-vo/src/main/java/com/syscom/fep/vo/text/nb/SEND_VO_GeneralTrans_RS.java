package com.syscom.fep.vo.text.nb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("soap:Envelope")
public class SEND_VO_GeneralTrans_RS extends NbXmlBase {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:soap")
    private String xmlnsSoap;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("soap:Header")
    private SEND_VO_GeneralTrans_RS_Header header;
    @XStreamAlias("soap:Body")
    private SEND_VO_GeneralTrans_RS_Body body;

    public String getXmlnsSoap() {
        return xmlnsSoap;
    }
    public void setXmlnsSoap(String xmlnsSoap) {
        this.xmlnsSoap = xmlnsSoap;
    }
    public String getXmlnsXsi() {
        return xmlnsXsi;
    }
    public void setXmlnsXsi(String xmlnsXsi) {
        this.xmlnsXsi = xmlnsXsi;
    }
    public SEND_VO_GeneralTrans_RS_Header getHeader() {
        return header;
    }
    public void setHeader(SEND_VO_GeneralTrans_RS_Header header) {
        this.header = header;
    }
    public SEND_VO_GeneralTrans_RS_Body getBody() {
        return body;
    }
    public void setBody(SEND_VO_GeneralTrans_RS_Body body) {
        this.body = body;
    }
    @XStreamAlias("soap:Header")
    public static class SEND_VO_GeneralTrans_RS_Header {
    }
    @XStreamAlias("soap:Body")
    public static class SEND_VO_GeneralTrans_RS_Body {
        @XStreamAlias("NS1:MsgRs")
        private SEND_VO_GeneralTrans_RS_Body_MsgRs rs;
        public SEND_VO_GeneralTrans_RS_Body_MsgRs getRs() {
            return rs;
        }
        public void setRs(SEND_VO_GeneralTrans_RS_Body_MsgRs rs) {
            this.rs = rs;
        }
    }
    @XStreamAlias("NS1:MsgRs")
    public static class SEND_VO_GeneralTrans_RS_Body_MsgRs {
        @XStreamAlias("Header")
        private SEND_VO_GeneralTrans_RS_Body_MsgRs_Header header;
        @XStreamAlias("SvcRs")
        private SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs svcRs;
        public SEND_VO_GeneralTrans_RS_Body_MsgRs_Header getHeader() {
            return header;
        }
        public void setHeader(SEND_VO_GeneralTrans_RS_Body_MsgRs_Header header) {
            this.header = header;
        }
        public SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs getSvcRs() {
            return svcRs;
        }
        public void setSvcRq(SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs svcRs) {
            this.svcRs = svcRs;
        }
    }

    @XStreamAlias("Header")
    public static class SEND_VO_GeneralTrans_RS_Body_MsgRs_Header {
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
    public static class SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs {
        private String OUTDATE;
        private String OUTTIME;
        private String FEP_EJNO;
        private String CUSTOMERID;
        private String TXNTYPE;
        private String HOSTACC_FLAG;
        private String HOSTRVS_FLAG;
        private String FSCODE;
        private String OVTXC;
        private String OVBHI;
        private String OVTLI;
        private String OVSEQ;
        private String OVTRMNO;
        private String OVOPR;
        private String OVSTAN;
        private String OVRTNC;
        private String OVCTL;
        private String OVRES;
        private String OVCGFEE;
        private String OVFEE;
        private String OVFAXNO;
        private String OVTXD;
        private String OVBAL;
        private String OVAMT;
        private String OVNAME;
        private String OVSEXF;
        private String OVTPT;
        private String OVFEETY;
        private String OVCIRCU;
        private String OVEND;

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
        public String getHOSTACC_FLAG(){
            return HOSTACC_FLAG;
        }
        public void setHOSTACC_FLAG(String value) {
            this.HOSTACC_FLAG = value;
        }
        public String getHOSTRVS_FLAG(){
            return HOSTRVS_FLAG;
        }
        public void setHOSTRVS_FLAG(String value) {
            this.HOSTRVS_FLAG = value;
        }
        public String getFSCODE(){
            return FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getOVTXC(){
            return OVTXC;
        }
        public void setOVTXC(String value) {
            this.OVTXC = value;
        }
        public String getOVBHI(){
            return OVBHI;
        }
        public void setOVBHI(String value) {
            this.OVBHI = value;
        }
        public String getOVTLI(){
            return OVTLI;
        }
        public void setOVTLI(String value) {
            this.OVTLI = value;
        }
        public String getOVSEQ(){
            return OVSEQ;
        }
        public void setOVSEQ(String value) {
            this.OVSEQ = value;
        }
        public String getOVTRMNO(){
            return OVTRMNO;
        }
        public void setOVTRMNO(String value) {
            this.OVTRMNO = value;
        }
        public String getOVOPR(){
            return OVOPR;
        }
        public void setOVOPR(String value) {
            this.OVOPR = value;
        }
        public String getOVSTAN(){
            return OVSTAN;
        }
        public void setOVSTAN(String value) {
            this.OVSTAN = value;
        }
        public String getOVRTNC(){
            return OVRTNC;
        }
        public void setOVRTNC(String value) {
            this.OVRTNC = value;
        }
        public String getOVCTL(){
            return OVCTL;
        }
        public void setOVCTL(String value) {
            this.OVCTL = value;
        }
        public String getOVRES(){
            return OVRES;
        }
        public void setOVRES(String value) {
            this.OVRES = value;
        }
        public String getOVCGFEE(){
            return OVCGFEE;
        }
        public void setOVCGFEE(String value) {
            this.OVCGFEE = value;
        }
        public String getOVFEE(){
            return OVFEE;
        }
        public void setOVFEE(String value) {
            this.OVFEE = value;
        }
        public String getOVFAXNO(){
            return OVFAXNO;
        }
        public void setOVFAXNO(String value) {
            this.OVFAXNO = value;
        }
        public String getOVTXD(){
            return OVTXD;
        }
        public void setOVTXD(String value) {
            this.OVTXD = value;
        }
        public String getOVBAL(){
            return OVBAL;
        }
        public void setOVBAL(String value) {
            this.OVBAL = value;
        }
        public String getOVAMT(){
            return OVAMT;
        }
        public void setOVAMT(String value) {
            this.OVAMT = value;
        }
        public String getOVNAME(){
            return OVNAME;
        }
        public void setOVNAME(String value) {
            this.OVNAME = value;
        }
        public String getOVSEXF(){
            return OVSEXF;
        }
        public void setOVSEXF(String value) {
            this.OVSEXF = value;
        }
        public String getOVTPT(){
            return OVTPT;
        }
        public void setOVTPT(String value) {
            this.OVTPT = value;
        }
        public String getOVFEETY(){
            return OVFEETY;
        }
        public void setOVFEETY(String value) {
            this.OVFEETY = value;
        }
        public String getOVCIRCU(){
            return OVCIRCU;
        }
        public void setOVCIRCU(String value) {
            this.OVCIRCU = value;
        }
        public String getOVEND(){
            return OVEND;
        }
        public void setOVEND(String value) {
            this.OVEND = value;
        }
    }
}