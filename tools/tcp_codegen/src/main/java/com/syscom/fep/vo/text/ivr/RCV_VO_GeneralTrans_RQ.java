package com.syscom.fep.vo.text.ivr;

import java.math.BigDecimal;

import com.syscom.fep.vo.CodeGenUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("SOAP-ENV:Envelope")
public class RCV_VO_GeneralTrans_RQ extends VoXmlBase {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:SOAP-ENV")
    private String xmlnsSoapEnv;
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("SOAP-ENV:Header")
    private RCV_VO_GeneralTrans_RQ_Header header;
    @XStreamAlias("SOAP-ENV:Body")
    private RCV_VO_GeneralTrans_RQ_Body body;

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
    public RCV_VO_GeneralTrans_RQ_Header getHeader() {
        return header;
    }
    public void setHeader(RCV_VO_GeneralTrans_RQ_Header header) {
        this.header = header;
    }
    public RCV_VO_GeneralTrans_RQ_Body getBody() {
        return body;
    }
    public void setBody(RCV_VO_GeneralTrans_RQ_Body body) {
        this.body = body;
    }
    @XStreamAlias("SOAP-ENV:Header")
    public static class RCV_VO_GeneralTrans_RQ_Header {
    }
    @XStreamAlias("SOAP-ENV:Body")
    public static class RCV_VO_GeneralTrans_RQ_Body {
        @XStreamAlias("esb:MsgRq")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq rq;
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq getRq() {
            return rq;
        }
        public void setRq(RCV_VO_GeneralTrans_RQ_Body_MsgRq rq) {
            this.rq = rq;
        }
		@Override
		public String toString() {
			return "RCV_VO_GeneralTrans_RQ_Body [rq=" + rq + "]";
		}

    }
    @XStreamAlias("esb:MsgRq")
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq {
        @XStreamAlias("Header")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header header;
        @XStreamAlias("SvcRq")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq;
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header getHeader() {
            return header;
        }
        public void setHeader(RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header header) {
            this.header = header;
        }
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq getSvcRq() {
            return svcRq;
        }
        public void setSvcRq(RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq) {
            this.svcRq = svcRq;
        }
		@Override
		public String toString() {
			return "RCV_VO_GeneralTrans_RQ_Body_MsgRq [header=" + header + ", svcRq=" + svcRq + "]";
		}

    }

    @XStreamAlias("Header")
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header {
        private String CLIENTTRACEID;
        private String CHANNEL;
        private String MSGID;
        private String MSGKIND;
        private String TXNID;
        private String BRANCHID;
        private String TERMID;
        private String CLIENTDT;

        public String getCLIENTTRACEID(){
            return this.CLIENTTRACEID;
        }
        public void setCLIENTTRACEID(String value) {
            this.CLIENTTRACEID = value;
        }
        public String getCHANNEL(){
            return this.CHANNEL;
        }
        public void setCHANNEL(String value) {
            this.CHANNEL = value;
        }
        public String getMSGID(){
            return this.MSGID;
        }
        public void setMSGID(String value) {
            this.MSGID = value;
        }
        public String getMSGKIND(){
            return this.MSGKIND;
        }
        public void setMSGKIND(String value) {
            this.MSGKIND = value;
        }
        public String getTXNID(){
            return this.TXNID;
        }
        public void setTXNID(String value) {
            this.TXNID = value;
        }
        public String getBRANCHID(){
            return this.BRANCHID;
        }
        public void setBRANCHID(String value) {
            this.BRANCHID = value;
        }
        public String getTERMID(){
            return this.TERMID;
        }
        public void setTERMID(String value) {
            this.TERMID = value;
        }
        public String getCLIENTDT(){
            return this.CLIENTDT;
        }
        public void setCLIENTDT(String value) {
            this.CLIENTDT = value;
        }
		@Override
		public String toString() {
			return "RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header [CLIENTTRACEID=" + CLIENTTRACEID + ", CHANNEL=" + CHANNEL
					+ ", MSGID=" + MSGID + ", MSGKIND=" + MSGKIND + ", TXNID=" + TXNID + ", BRANCHID=" + BRANCHID
					+ ", TERMID=" + TERMID + ", CLIENTDT=" + CLIENTDT + "]";
		}

    }

    @XStreamAlias("SvcRq")
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq {
        @XStreamAlias("PAYDATA")
        private RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA PAYDATA;
        public RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA getPAYDATA() {
            return PAYDATA;
        }
        public void setPAYDATA(RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA PAYDATA) {
            this.PAYDATA = PAYDATA;
        }

        private String INDATE;
        private String INTIME;
        private String TXNTYPE;
        private String TERMINALID;
        private String TERMINAL_TYPE;
        private String TERMINAL_CHECKNO;
        private String FSCODE;
        private String PCODE;
        private String IVTXC;
        private String IVBHI;
        private String IVTLI;
        private String IVOPR;
        private String IVSEQ;
        private String IVTRMNO;
        private String IVMAC;
        private String IVSNO;
        private String IVPPSYN;
        private String IVPINBLK;
        private String IVMACSYN;
        private String IVFACCT;
        private String IVAMT;
        private String IVSENDF;

        public String getINDATE(){
            return this.INDATE;
        }
        public void setINDATE(String value) {
            this.INDATE = value;
        }
        public String getINTIME(){
            return this.INTIME;
        }
        public void setINTIME(String value) {
            this.INTIME = value;
        }
        public String getTXNTYPE(){
            return this.TXNTYPE;
        }
        public void setTXNTYPE(String value) {
            this.TXNTYPE = value;
        }
        public String getTERMINALID(){
            return this.TERMINALID;
        }
        public void setTERMINALID(String value) {
            this.TERMINALID = value;
        }
        public String getTERMINAL_TYPE(){
            return this.TERMINAL_TYPE;
        }
        public void setTERMINAL_TYPE(String value) {
            this.TERMINAL_TYPE = value;
        }
        public String getTERMINAL_CHECKNO(){
            return this.TERMINAL_CHECKNO;
        }
        public void setTERMINAL_CHECKNO(String value) {
            this.TERMINAL_CHECKNO = value;
        }
        public String getFSCODE(){
            return this.FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getPCODE(){
            return this.PCODE;
        }
        public void setPCODE(String value) {
            this.PCODE = value;
        }
        public String getIVTXC(){
            return this.IVTXC;
        }
        public void setIVTXC(String value) {
            this.IVTXC = value;
        }
        public String getIVBHI(){
            return this.IVBHI;
        }
        public void setIVBHI(String value) {
            this.IVBHI = value;
        }
        public String getIVTLI(){
            return this.IVTLI;
        }
        public void setIVTLI(String value) {
            this.IVTLI = value;
        }
        public String getIVOPR(){
            return this.IVOPR;
        }
        public void setIVOPR(String value) {
            this.IVOPR = value;
        }
        public String getIVSEQ(){
            return this.IVSEQ;
        }
        public void setIVSEQ(String value) {
            this.IVSEQ = value;
        }
        public String getIVTRMNO(){
            return this.IVTRMNO;
        }
        public void setIVTRMNO(String value) {
            this.IVTRMNO = value;
        }
        public String getIVMAC(){
            return this.IVMAC;
        }
        public void setIVMAC(String value) {
            this.IVMAC = value;
        }
        public String getIVSNO(){
            return this.IVSNO;
        }
        public void setIVSNO(String value) {
            this.IVSNO = value;
        }
        public String getIVPPSYN(){
            return this.IVPPSYN;
        }
        public void setIVPPSYN(String value) {
            this.IVPPSYN = value;
        }
        public String getIVPINBLK(){
            return this.IVPINBLK;
        }
        public void setIVPINBLK(String value) {
            this.IVPINBLK = value;
        }
        public String getIVMACSYN(){
            return this.IVMACSYN;
        }
        public void setIVMACSYN(String value) {
            this.IVMACSYN = value;
        }
        public String getIVFACCT(){
            return this.IVFACCT;
        }
        public void setIVFACCT(String value) {
            this.IVFACCT = value;
        }
        public BigDecimal getIVAMT(){
            return CodeGenUtil.asciiToBigDecimal(this.IVAMT, false, 0);
        }
        public void setIVAMT(BigDecimal value) {
            this.IVAMT = CodeGenUtil.bigDecimalToAscii(value, 11, false, 0, false);
        }
        public String getIVSENDF(){
            return this.IVSENDF;
        }
        public void setIVSENDF(String value) {
            this.IVSENDF = value;
        }
		@Override
		public String toString() {
			return "RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq [PAYDATA=" + PAYDATA + ", INDATE=" + INDATE + ", INTIME="
					+ INTIME + ", TXNTYPE=" + TXNTYPE + ", TERMINALID=" + TERMINALID + ", TERMINAL_TYPE="
					+ TERMINAL_TYPE + ", TERMINAL_CHECKNO=" + TERMINAL_CHECKNO + ", FSCODE=" + FSCODE + ", PCODE="
					+ PCODE + ", IVTXC=" + IVTXC + ", IVBHI=" + IVBHI + ", IVTLI=" + IVTLI + ", IVOPR=" + IVOPR
					+ ", IVSEQ=" + IVSEQ + ", IVTRMNO=" + IVTRMNO + ", IVMAC=" + IVMAC + ", IVSNO=" + IVSNO
					+ ", IVPPSYN=" + IVPPSYN + ", IVPINBLK=" + IVPINBLK + ", IVMACSYN=" + IVMACSYN + ", IVFACCT="
					+ IVFACCT + ", IVAMT=" + IVAMT + ", IVSENDF=" + IVSENDF + "]";
		}

    }

    @XStreamAlias("PAYDATA")
    public static class RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA {
        private String IVTBANK;
        private String IVTACCN;
        private String FILLER1;
        private String IVTAXTYP;
        private String IVTAXORG;
        private String IVTAXIDN;
        private String IVTAXBLN;
        private String IVTAXDAT;
        private String FILLER2;

        public String getIVTBANK(){
            return this.IVTBANK;
        }
        public void setIVTBANK(String value) {
            this.IVTBANK = value;
        }
        public String getIVTACCN(){
            return this.IVTACCN;
        }
        public void setIVTACCN(String value) {
            this.IVTACCN = value;
        }
        public String getFILLER1(){
            return this.FILLER1;
        }
        public void setFILLER1(String value) {
            this.FILLER1 = value;
        }
        public String getIVTAXTYP(){
            return this.IVTAXTYP;
        }
        public void setIVTAXTYP(String value) {
            this.IVTAXTYP = value;
        }
        public String getIVTAXORG(){
            return this.IVTAXORG;
        }
        public void setIVTAXORG(String value) {
            this.IVTAXORG = value;
        }
        public String getIVTAXIDN(){
            return this.IVTAXIDN;
        }
        public void setIVTAXIDN(String value) {
            this.IVTAXIDN = value;
        }
        public String getIVTAXBLN(){
            return this.IVTAXBLN;
        }
        public void setIVTAXBLN(String value) {
            this.IVTAXBLN = value;
        }
        public String getIVTAXDAT(){
            return this.IVTAXDAT;
        }
        public void setIVTAXDAT(String value) {
            this.IVTAXDAT = value;
        }
        public String getFILLER2(){
            return this.FILLER2;
        }
        public void setFILLER2(String value) {
            this.FILLER2 = value;
        }
		@Override
		public String toString() {
			return "RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA [IVTBANK=" + IVTBANK + ", IVTACCN=" + IVTACCN
					+ ", FILLER1=" + FILLER1 + ", IVTAXTYP=" + IVTAXTYP + ", IVTAXORG=" + IVTAXORG + ", IVTAXIDN="
					+ IVTAXIDN + ", IVTAXBLN=" + IVTAXBLN + ", IVTAXDAT=" + IVTAXDAT + ", FILLER2=" + FILLER2 + "]";
		}

    }

	@Override
	public String toString() {
		return "RCV_VO_GeneralTrans_RQ [xmlnsSoapEnv=" + xmlnsSoapEnv + ", xmlnsXsi=" + xmlnsXsi + ", header=" + header
				+ ", body=" + body + "]";
	}

}
