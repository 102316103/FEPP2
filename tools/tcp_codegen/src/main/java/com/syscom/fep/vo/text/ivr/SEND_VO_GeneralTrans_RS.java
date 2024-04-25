package com.syscom.fep.vo.text.ivr;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.CodeGenUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("soap:Envelope")
public class SEND_VO_GeneralTrans_RS extends VoXmlBase {
    @XStreamAsAttribute
    @XStreamAlias("xmlns:soap")
    private String xmlnsSoap = "http://schemas.xmlsoap.org/soap/envelope/";
    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    private String xmlnsXsi;
    @XStreamAlias("soap:Header")
    private String header = StringUtils.EMPTY;
    @XStreamAlias("soap:Body")
    private SEND_VO_GeneralTrans_RS_Body body;

    public SEND_VO_GeneralTrans_RS_Body getBody() {
        return body;
    }
    public void setBody(SEND_VO_GeneralTrans_RS_Body body) {
        this.body = body;
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
		@Override
		public String toString() {
			return "SEND_VO_GeneralTrans_RS_Body [rs=" + rs + "]";
		}

    }
    @XStreamAlias("NS1:MsgRs")
    public static class SEND_VO_GeneralTrans_RS_Body_MsgRs {
        @XStreamAsAttribute
        @XStreamAlias("xmlns:NS1")
        private String xmlnsNs1="http://www.ibm.com.tw/esb";
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
        public void setSvcRs(SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs svcRs) {
            this.svcRs = svcRs;
        }
		@Override
		public String toString() {
			return "SEND_VO_GeneralTrans_RS_Body_MsgRs [xmlnsNs1=" + xmlnsNs1 + ", header=" + header + ", svcRs="
					+ svcRs + "]";
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
        public String getCLIENTDT(){
            return this.CLIENTDT;
        }
        public void setCLIENTDT(String value) {
            this.CLIENTDT = value;
        }
        public String getSYSTEMID(){
            return this.SYSTEMID;
        }
        public void setSYSTEMID(String value) {
            this.SYSTEMID = value;
        }
        public String getSTATUSCODE(){
            return this.STATUSCODE;
        }
        public void setSTATUSCODE(String value) {
            this.STATUSCODE = value;
        }
        public String getSEVERITY(){
            return this.SEVERITY;
        }
        public void setSEVERITY(String value) {
            this.SEVERITY = value;
        }
        public String getSTATUSDESC(){
            return this.STATUSDESC;
        }
        public void setSTATUSDESC(String value) {
            this.STATUSDESC = value;
        }
		@Override
		public String toString() {
			return "SEND_VO_GeneralTrans_RS_Body_MsgRs_Header [CLIENTTRACEID=" + CLIENTTRACEID + ", CHANNEL=" + CHANNEL
					+ ", MSGID=" + MSGID + ", CLIENTDT=" + CLIENTDT + ", SYSTEMID=" + SYSTEMID + ", STATUSCODE="
					+ STATUSCODE + ", SEVERITY=" + SEVERITY + ", STATUSDESC=" + STATUSDESC + "]";
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
            return this.OUTDATE;
        }
        public void setOUTDATE(String value) {
            this.OUTDATE = value;
        }
        public String getOUTTIME(){
            return this.OUTTIME;
        }
        public void setOUTTIME(String value) {
            this.OUTTIME = value;
        }
        public String getFEP_EJNO(){
            return this.FEP_EJNO;
        }
        public void setFEP_EJNO(String value) {
            this.FEP_EJNO = value;
        }
        public String getCUSTOMERID(){
            return this.CUSTOMERID;
        }
        public void setCUSTOMERID(String value) {
            this.CUSTOMERID = value;
        }
        public String getTXNTYPE(){
            return this.TXNTYPE;
        }
        public void setTXNTYPE(String value) {
            this.TXNTYPE = value;
        }
        public String getHOSTACC_FLAG(){
            return this.HOSTACC_FLAG;
        }
        public void setHOSTACC_FLAG(String value) {
            this.HOSTACC_FLAG = value;
        }
        public String getHOSTRVS_FLAG(){
            return this.HOSTRVS_FLAG;
        }
        public void setHOSTRVS_FLAG(String value) {
            this.HOSTRVS_FLAG = value;
        }
        public String getFSCODE(){
            return this.FSCODE;
        }
        public void setFSCODE(String value) {
            this.FSCODE = value;
        }
        public String getOVTXC(){
            return this.OVTXC;
        }
        public void setOVTXC(String value) {
            this.OVTXC = value;
        }
        public String getOVBHI(){
            return this.OVBHI;
        }
        public void setOVBHI(String value) {
            this.OVBHI = value;
        }
        public String getOVTLI(){
            return this.OVTLI;
        }
        public void setOVTLI(String value) {
            this.OVTLI = value;
        }
        public String getOVSEQ(){
            return this.OVSEQ;
        }
        public void setOVSEQ(String value) {
            this.OVSEQ = value;
        }
        public String getOVTRMNO(){
            return this.OVTRMNO;
        }
        public void setOVTRMNO(String value) {
            this.OVTRMNO = value;
        }
        public String getOVOPR(){
            return this.OVOPR;
        }
        public void setOVOPR(String value) {
            this.OVOPR = value;
        }
        public String getOVSTAN(){
            return this.OVSTAN;
        }
        public void setOVSTAN(String value) {
            this.OVSTAN = value;
        }
        public String getOVRTNC(){
            return this.OVRTNC;
        }
        public void setOVRTNC(String value) {
            this.OVRTNC = value;
        }
        public String getOVCTL(){
            return this.OVCTL;
        }
        public void setOVCTL(String value) {
            this.OVCTL = value;
        }
        public String getOVRES(){
            return this.OVRES;
        }
        public void setOVRES(String value) {
            this.OVRES = value;
        }
        public BigDecimal getOVCGFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.OVCGFEE, false, 0);
        }
        public void setOVCGFEE(BigDecimal value) {
            this.OVCGFEE = CodeGenUtil.bigDecimalToAscii(value, 2, false, 0, false);
        }
        public BigDecimal getOVFEE(){
            return CodeGenUtil.asciiToBigDecimal(this.OVFEE, false, 0);
        }
        public void setOVFEE(BigDecimal value) {
            this.OVFEE = CodeGenUtil.bigDecimalToAscii(value, 3, false, 0, false);
        }
        public String getOVFAXNO(){
            return this.OVFAXNO;
        }
        public void setOVFAXNO(String value) {
            this.OVFAXNO = value;
        }
        public String getOVTXD(){
            return this.OVTXD;
        }
        public void setOVTXD(String value) {
            this.OVTXD = value;
        }
        public BigDecimal getOVBAL(){
            return CodeGenUtil.asciiToBigDecimal(this.OVBAL, true, 2);
        }
        public void setOVBAL(BigDecimal value) {
            this.OVBAL = CodeGenUtil.bigDecimalToAscii(value, 14, true, 2, true);
        }
        public BigDecimal getOVAMT(){
            return CodeGenUtil.asciiToBigDecimal(this.OVAMT, true, 2);
        }
        public void setOVAMT(BigDecimal value) {
            this.OVAMT = CodeGenUtil.bigDecimalToAscii(value, 14, true, 2, true);
        }
        public String getOVNAME(){
            return this.OVNAME;
        }
        public void setOVNAME(String value) {
            this.OVNAME = value;
        }
        public String getOVSEXF(){
            return this.OVSEXF;
        }
        public void setOVSEXF(String value) {
            this.OVSEXF = value;
        }
        public String getOVTPT(){
            return this.OVTPT;
        }
        public void setOVTPT(String value) {
            this.OVTPT = value;
        }
        public String getOVFEETY(){
            return this.OVFEETY;
        }
        public void setOVFEETY(String value) {
            this.OVFEETY = value;
        }
        public String getOVCIRCU(){
            return this.OVCIRCU;
        }
        public void setOVCIRCU(String value) {
            this.OVCIRCU = value;
        }
        public String getOVEND(){
            return this.OVEND;
        }
        public void setOVEND(String value) {
            this.OVEND = value;
        }
		@Override
		public String toString() {
			return "SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs [OUTDATE=" + OUTDATE + ", OUTTIME=" + OUTTIME
					+ ", FEP_EJNO=" + FEP_EJNO + ", CUSTOMERID=" + CUSTOMERID + ", TXNTYPE=" + TXNTYPE
					+ ", HOSTACC_FLAG=" + HOSTACC_FLAG + ", HOSTRVS_FLAG=" + HOSTRVS_FLAG + ", FSCODE=" + FSCODE
					+ ", OVTXC=" + OVTXC + ", OVBHI=" + OVBHI + ", OVTLI=" + OVTLI + ", OVSEQ=" + OVSEQ + ", OVTRMNO="
					+ OVTRMNO + ", OVOPR=" + OVOPR + ", OVSTAN=" + OVSTAN + ", OVRTNC=" + OVRTNC + ", OVCTL=" + OVCTL
					+ ", OVRES=" + OVRES + ", OVCGFEE=" + OVCGFEE + ", OVFEE=" + OVFEE + ", OVFAXNO=" + OVFAXNO
					+ ", OVTXD=" + OVTXD + ", OVBAL=" + OVBAL + ", OVAMT=" + OVAMT + ", OVNAME=" + OVNAME + ", OVSEXF="
					+ OVSEXF + ", OVTPT=" + OVTPT + ", OVFEETY=" + OVFEETY + ", OVCIRCU=" + OVCIRCU + ", OVEND=" + OVEND
					+ "]";
		}

    }

	@Override
	public String toString() {
		return "SEND_VO_GeneralTrans_RS [xmlnsSoap=" + xmlnsSoap + ", xmlnsXsi=" + xmlnsXsi + ", header=" + header
				+ ", body=" + body + "]";
	}

}
