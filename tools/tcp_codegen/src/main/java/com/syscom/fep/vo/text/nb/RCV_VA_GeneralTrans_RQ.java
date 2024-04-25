package com.syscom.fep.vo.text.nb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("SOAP-ENV:Envelope")
public class RCV_VA_GeneralTrans_RQ {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:SOAP-ENV")
    private String xmlnsSoapEnv;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("SOAP-ENV:Header")
    private RCV_VA_GeneralTrans_RQ_Header header;
    @XStreamAlias("SOAP-ENV:Body")
    private RCV_VA_GeneralTrans_RQ_Body body;

    public String getXmlnsSoapEnv() {
        return xmlnsSoapEnv;
    }
    public void setXmlnsSoapEnv(String xmlnsSoapEnv) {
        this.xmlnsSoapEnv = xmlnsSoapEnv;
    }
    public String getXmlnsXsi() {
        return xmlnsXsi;
    }
    public void setXmlnsXsi(String xmlnsXsi) {
        this.xmlnsXsi = xmlnsXsi;
    }
    public RCV_VA_GeneralTrans_RQ_Header getHeader() {
        return header;
    }
    public void setHeader(RCV_VA_GeneralTrans_RQ_Header header) {
        this.header = header;
    }
    public RCV_VA_GeneralTrans_RQ_Body getBody() {
        return body;
    }
    public void setBody(RCV_VA_GeneralTrans_RQ_Body body) {
        this.body = body;
    }
    @XStreamAlias("SOAP-ENV:Header")
    public static class RCV_VA_GeneralTrans_RQ_Header {
    }
    @XStreamAlias("SOAP-ENV:Body")
    public static class RCV_VA_GeneralTrans_RQ_Body {
        @XStreamAlias("esb:MsgRq")
        private RCV_VA_GeneralTrans_RQ_Body_MsgRq rq;
        public RCV_VA_GeneralTrans_RQ_Body_MsgRq getRq() {
            return rq;
        }
        public void setRq(RCV_VA_GeneralTrans_RQ_Body_MsgRq rq) {
            this.rq = rq;
        }
		@Override
		public String toString() {
			return "RCV_VA_GeneralTrans_RQ_Body [rq=" + rq + "]";
		}

    }
    @XStreamAlias("esb:MsgRq")
    public static class RCV_VA_GeneralTrans_RQ_Body_MsgRq {
        @XStreamAlias("Header")
        private RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header;
        @XStreamAlias("SvcRq")
        private RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq;
        public RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header getHeader() {
            return header;
        }
        public void setHeader(RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header header) {
            this.header = header;
        }
        public RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq getSvcRq() {
            return svcRq;
        }
        public void setSvcRq(RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq) {
            this.svcRq = svcRq;
        }
		@Override
		public String toString() {
			return "RCV_VA_GeneralTrans_RQ_Body_MsgRq [header=" + header + ", svcRq=" + svcRq + "]";
		}

    }

    @XStreamAlias("Header")
    public static class RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header {
        private String CLIENTTRACEID;
        private String CHANNEL;
        private String MSGID;
        private String MSGKIND;
        private String TXNID;
        private String BRANCHID;
        private String TERMID;
        private String CLIENTDT;

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
        public String getMSGKIND(){
            return MSGKIND;
        }
        public void setMSGKIND(String value) {
            this.MSGKIND = value;
        }
        public String getTXNID(){
            return TXNID;
        }
        public void setTXNID(String value) {
            this.TXNID = value;
        }
        public String getBRANCHID(){
            return BRANCHID;
        }
        public void setBRANCHID(String value) {
            this.BRANCHID = value;
        }
        public String getTERMID(){
            return TERMID;
        }
        public void setTERMID(String value) {
            this.TERMID = value;
        }
        public String getCLIENTDT(){
            return CLIENTDT;
        }
        public void setCLIENTDT(String value) {
            this.CLIENTDT = value;
        }
		@Override
		public String toString() {
			return "RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header [CLIENTTRACEID=" + CLIENTTRACEID + ", CHANNEL=" + CHANNEL
					+ ", MSGID=" + MSGID + ", MSGKIND=" + MSGKIND + ", TXNID=" + TXNID + ", BRANCHID=" + BRANCHID
					+ ", TERMID=" + TERMID + ", CLIENTDT=" + CLIENTDT + "]";
		}

    }

    @XStreamAlias("SvcRq")
    public static class RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq {
        @XStreamAlias("SENDDATA")
        private RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq_SendData senddata;
        public RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq_SendData getSendData() {
            return senddata;
        }
        public void setSendData(RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq_SendData senddata) {
            this.senddata = senddata;
        }
        private String INDATE;
        private String INTIME;
        private String IPADDR;
        private String TXNTYPE;
        private String TERMINALID;
        private String TERMINAL_TYPE;
        private String TERMINAL_CHECKNO;
        private String FSCODE;
        private String PCODE;
        private String TRANSAMT;
        private String VACATE;
        private String AEIPYTP;
        private String AEIPYBK;
        private String AEIPYBH;
        private String AEIPCRBK;
        private String AEIPYAC;
        private String TAXIDNO;
        private String MOBILENO;
        private String SSLTYPE;
        private String AEIPCRAC;
        private String IC_SEQNO;
        private String ICMARK;
        private String IC_TAC_LEN;
        private String IC_TAC;
        private String AEICDAY;
        private String AEICTIME;

        public String getINDATE(){
            return INDATE;
        }
        public void setINDATE(String value) {
            this.INDATE = value;
        }
        public String getINTIME(){
            return INTIME;
        }
        public void setINTIME(String value) {
            this.INTIME = value;
        }
        public String getIPADDR(){
            return IPADDR;
        }
        public void setIPADDR(String value) {
            this.IPADDR = value;
        }
        public String getTXNTYPE(){
            return TXNTYPE;
        }
        public void setTXNTYPE(String value) {
            this.TXNTYPE = value;
        }
        public String getTERMINALID(){
            return TERMINALID;
        }
        public void setTERMINALID(String value) {
            this.TERMINALID = value;
        }
        public String getTERMINAL_TYPE(){
            return TERMINAL_TYPE;
        }
        public void setTERMINAL_TYPE(String value) {
            this.TERMINAL_TYPE = value;
        }
        public String getTERMINAL_CHECKNO(){
            return TERMINAL_CHECKNO;
        }
        public void setTERMINAL_CHECKNO(String value) {
            this.TERMINAL_CHECKNO = value;
        }
        public String getFSCODE(){
            return FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getPCODE(){
            return PCODE;
        }
        public void setPCODE(String value) {
            this.PCODE = value;
        }
        public String getTRANSAMT(){
            return TRANSAMT;
        }
        public void setTRANSAMT(String value) {
            this.TRANSAMT = value;
        }
        public String getVACATE(){
            return VACATE;
        }
        public void setVACATE(String value) {
            this.VACATE = value;
        }
        public String getAEIPYTP(){
            return AEIPYTP;
        }
        public void setAEIPYTP(String value) {
            this.AEIPYTP = value;
        }
        public String getAEIPYBK(){
            return AEIPYBK;
        }
        public void setAEIPYBK(String value) {
            this.AEIPYBK = value;
        }
        public String getAEIPYBH(){
            return AEIPYBH;
        }
        public void setAEIPYBH(String value) {
            this.AEIPYBH = value;
        }
        public String getAEIPCRBK(){
            return AEIPCRBK;
        }
        public void setAEIPCRBK(String value) {
            this.AEIPCRBK = value;
        }
        public String getAEIPYAC(){
            return AEIPYAC;
        }
        public void setAEIPYAC(String value) {
            this.AEIPYAC = value;
        }
        public String getTAXIDNO(){
            return TAXIDNO;
        }
        public void setTAXIDNO(String value) {
            this.TAXIDNO = value;
        }
        public String getMOBILENO(){
            return MOBILENO;
        }
        public void setMOBILENO(String value) {
            this.MOBILENO = value;
        }
        public String getSSLTYPE(){
            return SSLTYPE;
        }
        public void setSSLTYPE(String value) {
            this.SSLTYPE = value;
        }
        public String getAEIPCRAC(){
            return AEIPCRAC;
        }
        public void setAEIPCRAC(String value) {
            this.AEIPCRAC = value;
        }
        public String getIC_SEQNO(){
            return IC_SEQNO;
        }
        public void setIC_SEQNO(String value) {
            this.IC_SEQNO = value;
        }
        public String getICMARK(){
            return ICMARK;
        }
        public void setICMARK(String value) {
            this.ICMARK = value;
        }
        public String getIC_TAC_LEN(){
            return IC_TAC_LEN;
        }
        public void setIC_TAC_LEN(String value) {
            this.IC_TAC_LEN = value;
        }
        public String getIC_TAC(){
            return IC_TAC;
        }
        public void setIC_TAC(String value) {
            this.IC_TAC = value;
        }
        public String getAEICDAY(){
            return AEICDAY;
        }
        public void setAEICDAY(String value) {
            this.AEICDAY = value;
        }
        public String getAEICTIME(){
            return AEICTIME;
        }
        public void setAEICTIME(String value) {
            this.AEICTIME = value;
        }
		@Override
		public String toString() {
			return "RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq [senddata=" + senddata + ", INDATE=" + INDATE + ", INTIME="
					+ INTIME + ", IPADDR=" + IPADDR + ", TXNTYPE=" + TXNTYPE + ", TERMINALID=" + TERMINALID
					+ ", TERMINAL_TYPE=" + TERMINAL_TYPE + ", TERMINAL_CHECKNO=" + TERMINAL_CHECKNO + ", FSCODE="
					+ FSCODE + ", PCODE=" + PCODE + ", TRANSAMT=" + TRANSAMT + ", VACATE=" + VACATE + ", AEIPYTP="
					+ AEIPYTP + ", AEIPYBK=" + AEIPYBK + ", AEIPYBH=" + AEIPYBH + ", AEIPCRBK=" + AEIPCRBK
					+ ", AEIPYAC=" + AEIPYAC + ", TAXIDNO=" + TAXIDNO + ", MOBILENO=" + MOBILENO + ", SSLTYPE="
					+ SSLTYPE + ", AEIPCRAC=" + AEIPCRAC + ", IC_SEQNO=" + IC_SEQNO + ", ICMARK=" + ICMARK
					+ ", IC_TAC_LEN=" + IC_TAC_LEN + ", IC_TAC=" + IC_TAC + ", AEICDAY=" + AEICDAY + ", AEICTIME="
					+ AEICTIME + "]";
		}

    }

    @XStreamAlias("SENDDATA")
    public static class RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq_SendData {
        private String PAYUNTNO;
        private String TAXTYPE;
        private String PAYFEENO;
        private String CLCPYCI;
        private String AEIPYAC2;
        private String AELFTP;
        private String BIRTHDAY;
        private String TELHOME;
        private String AEIPYUES;
        private String FILLER2;

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
        public String getCLCPYCI(){
            return CLCPYCI;
        }
        public void setCLCPYCI(String value) {
            this.CLCPYCI = value;
        }
        public String getAEIPYAC2(){
            return AEIPYAC2;
        }
        public void setAEIPYAC2(String value) {
            this.AEIPYAC2 = value;
        }
        public String getAELFTP(){
            return AELFTP;
        }
        public void setAELFTP(String value) {
            this.AELFTP = value;
        }
        public String getBIRTHDAY(){
            return BIRTHDAY;
        }
        public void setBIRTHDAY(String value) {
            this.BIRTHDAY = value;
        }
        public String getTELHOME(){
            return TELHOME;
        }
        public void setTELHOME(String value) {
            this.TELHOME = value;
        }
        public String getAEIPYUES(){
            return AEIPYUES;
        }
        public void setAEIPYUES(String value) {
            this.AEIPYUES = value;
        }
        public String getFILLER2(){
            return FILLER2;
        }
        public void setFILLER2(String value) {
            this.FILLER2 = value;
        }
		@Override
		public String toString() {
			return "RCV_VA_GeneralTrans_RQ_Body_MsgRq_SvcRq_SendData [PAYUNTNO=" + PAYUNTNO + ", TAXTYPE=" + TAXTYPE
					+ ", PAYFEENO=" + PAYFEENO + ", CLCPYCI=" + CLCPYCI + ", AEIPYAC2=" + AEIPYAC2 + ", AELFTP="
					+ AELFTP + ", BIRTHDAY=" + BIRTHDAY + ", TELHOME=" + TELHOME + ", AEIPYUES=" + AEIPYUES
					+ ", FILLER2=" + FILLER2 + "]";
		}

    }

	@Override
	public String toString() {
		return "RCV_VA_GeneralTrans_RQ [xmlnsSoapEnv=" + xmlnsSoapEnv + ", xmlnsXsi=" + xmlnsXsi + ", header=" + header
				+ ", body=" + body + "]";
	}

}
