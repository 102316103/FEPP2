package com.syscom.fep.vo.text.atm.response;

import java.math.BigDecimal;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.atm.ATMTextBase;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.frmcommon.annotation.Field;

public class ATM_FAA_CC1CPC extends ATMTextBase {
	@Field(length = 5)
	private String WSID = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String S1 = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String RECFMT = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String POAPUSE = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String MSGCAT = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String MSGTYP = StringUtils.EMPTY;
	
	@Field(length = 6)
	private String TRANDATE = StringUtils.EMPTY;
	
	@Field(length = 6)
	private String TRANTIME = StringUtils.EMPTY;
	
	@Field(length = 4)
	private String TRANSEQ = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String TDRSEG = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String PRCRDACT = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String DATATYPE = StringUtils.EMPTY;
	
	@Field(length = 3)
	private String DATALEN = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String ACKNOW = StringUtils.EMPTY;
	
	@Field(length = 3)
	private String PAGENO = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String DATATYPE1 = StringUtils.EMPTY;
	
	@Field(length = 3)
	private String DATALEN1 = StringUtils.EMPTY;
	
	@Field(length = 1)
	private String ACKNOW1 = StringUtils.EMPTY;
	
	@Field(length = 3)
	private String PAGENO1 = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String D1 = ";/";
	
	@Field(length = 2)
	private String FSCODE = StringUtils.EMPTY;
	
	@Field(length = 16)
	private String CARDACT = StringUtils.EMPTY;
	
	@Field(length = 20)
	private String TOACT = StringUtils.EMPTY;
	
	@Field(length = 20)
	private String ACTNAME = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String D2 = ";/";
	
	@Field(length = 7)
	private String STAN = StringUtils.EMPTY;
	
	@Field(length = 4)
	private String PRCCODE = StringUtils.EMPTY;
	
	@Field(length = 2)
	private String DEND1 = ";#";
	
	@Field(length = 8)
	private String MACCODE = StringUtils.EMPTY;
	
	private static final int _TotalLength = 131;

	public String getWSID(){
		return WSID;
	}
	
	public void setWSID(String WSID){
		this.WSID = WSID;
	}
    
	public String getS1(){
		return S1;
	}
	
	public void setS1(String S1){
		this.S1 = S1;
	}
    
	public String getRECFMT(){
		return RECFMT;
	}
	
	public void setRECFMT(String RECFMT){
		this.RECFMT = RECFMT;
	}
    
	public String getPOAPUSE(){
		return POAPUSE;
	}
	
	public void setPOAPUSE(String POAPUSE){
		this.POAPUSE = POAPUSE;
	}
    
	public String getMSGCAT(){
		return MSGCAT;
	}
	
	public void setMSGCAT(String MSGCAT){
		this.MSGCAT = MSGCAT;
	}
    
	public String getMSGTYP(){
		return MSGTYP;
	}
	
	public void setMSGTYP(String MSGTYP){
		this.MSGTYP = MSGTYP;
	}
    
	public String getTRANDATE(){
		return TRANDATE;
	}
	
	public void setTRANDATE(String TRANDATE){
		this.TRANDATE = TRANDATE;
	}
    
	public String getTRANTIME(){
		return TRANTIME;
	}
	
	public void setTRANTIME(String TRANTIME){
		this.TRANTIME = TRANTIME;
	}
    
	public String getTRANSEQ(){
		return TRANSEQ;
	}
	
	public void setTRANSEQ(String TRANSEQ){
		this.TRANSEQ = TRANSEQ;
	}
    
	public String getTDRSEG(){
		return TDRSEG;
	}
	
	public void setTDRSEG(String TDRSEG){
		this.TDRSEG = TDRSEG;
	}
    
	public String getPRCRDACT(){
		return PRCRDACT;
	}
	
	public void setPRCRDACT(String PRCRDACT){
		this.PRCRDACT = PRCRDACT;
	}
    
	public String getDATATYPE(){
		return DATATYPE;
	}
	
	public void setDATATYPE(String DATATYPE){
		this.DATATYPE = DATATYPE;
	}
    
	public String getDATALEN(){
		return DATALEN;
	}
	
	public void setDATALEN(String DATALEN){
		this.DATALEN = DATALEN;
	}
    
	public String getACKNOW(){
		return ACKNOW;
	}
	
	public void setACKNOW(String ACKNOW){
		this.ACKNOW = ACKNOW;
	}
    
	public String getPAGENO(){
		return PAGENO;
	}
	
	public void setPAGENO(String PAGENO){
		this.PAGENO = PAGENO;
	}
    
	public String getDATATYPE1(){
		return DATATYPE1;
	}
	
	public void setDATATYPE1(String DATATYPE1){
		this.DATATYPE1 = DATATYPE1;
	}
    
	public String getDATALEN1(){
		return DATALEN1;
	}
	
	public void setDATALEN1(String DATALEN1){
		this.DATALEN1 = DATALEN1;
	}
    
	public String getACKNOW1(){
		return ACKNOW1;
	}
	
	public void setACKNOW1(String ACKNOW1){
		this.ACKNOW1 = ACKNOW1;
	}
    
	public String getPAGENO1(){
		return PAGENO1;
	}
	
	public void setPAGENO1(String PAGENO1){
		this.PAGENO1 = PAGENO1;
	}
    
	public String getD1(){
		return D1;
	}
	
	public void setD1(String D1){
		this.D1 = D1;
	}
    
	public String getFSCODE(){
		return FSCODE;
	}
	
	public void setFSCODE(String FSCODE){
		this.FSCODE = FSCODE;
	}
    
	public String getCARDACT(){
		return CARDACT;
	}
	
	public void setCARDACT(String CARDACT){
		this.CARDACT = CARDACT;
	}
    
	public String getTOACT(){
		return TOACT;
	}
	
	public void setTOACT(String TOACT){
		this.TOACT = TOACT;
	}
    
	public String getACTNAME(){
		return ACTNAME;
	}
	
	public void setACTNAME(String ACTNAME){
		this.ACTNAME = ACTNAME;
	}
    
	public String getD2(){
		return D2;
	}
	
	public void setD2(String D2){
		this.D2 = D2;
	}
    
	public String getSTAN(){
		return STAN;
	}
	
	public void setSTAN(String STAN){
		this.STAN = STAN;
	}
    
	public String getPRCCODE(){
		return PRCCODE;
	}
	
	public void setPRCCODE(String PRCCODE){
		this.PRCCODE = PRCCODE;
	}
    
	public String getDEND1(){
		return DEND1;
	}
	
	public void setDEND1(String DEND1){
		this.DEND1 = DEND1;
	}
    
	public String getMACCODE(){
		return MACCODE;
	}
	
	public void setMACCODE(String MACCODE){
		this.MACCODE = MACCODE;
	}
    
	@Override
	public int getTotalLength() {
		return _TotalLength;
	}

	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return null;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) {
		return null;
	}

	public String makeMessage() {
		this.setWSID(StringUtils.rightPad(this.getWSID(),5,StringUtils.SPACE));
		this.setS1(StringUtils.rightPad(this.getS1(),1,StringUtils.SPACE));
		this.setRECFMT(StringUtils.rightPad(this.getRECFMT(),1,StringUtils.SPACE));
		this.setPOAPUSE(StringUtils.rightPad(this.getPOAPUSE(),1,StringUtils.SPACE));
		this.setMSGCAT(StringUtils.rightPad(this.getMSGCAT(),1,StringUtils.SPACE));
		this.setMSGTYP(StringUtils.rightPad(this.getMSGTYP(),2,StringUtils.SPACE));
		this.setTRANDATE(StringUtils.rightPad(this.getTRANDATE(),6,StringUtils.SPACE));
		this.setTRANTIME(StringUtils.rightPad(this.getTRANTIME(),6,StringUtils.SPACE));
		this.setTRANSEQ(StringUtils.rightPad(this.getTRANSEQ(),4,StringUtils.SPACE));
		this.setTDRSEG(StringUtils.rightPad(this.getTDRSEG(),2,StringUtils.SPACE));
		this.setPRCRDACT(StringUtils.rightPad(this.getPRCRDACT(),1,StringUtils.SPACE));
		this.setDATATYPE(StringUtils.rightPad(this.getDATATYPE(),2,StringUtils.SPACE));
		this.setDATALEN(StringUtils.rightPad(this.getDATALEN(),3,StringUtils.SPACE));
		this.setACKNOW(StringUtils.rightPad(this.getACKNOW(),1,StringUtils.SPACE));
		this.setPAGENO(StringUtils.rightPad(this.getPAGENO(),3,StringUtils.SPACE));
		this.setDATATYPE1(StringUtils.rightPad(this.getDATATYPE1(),2,StringUtils.SPACE));
		this.setDATALEN1(StringUtils.rightPad(this.getDATALEN1(),3,StringUtils.SPACE));
		this.setACKNOW1(StringUtils.rightPad(this.getACKNOW1(),1,StringUtils.SPACE));
		this.setPAGENO1(StringUtils.rightPad(this.getPAGENO1(),3,StringUtils.SPACE));
		this.setD1(StringUtils.rightPad(this.getD1(),2,StringUtils.SPACE));
		this.setFSCODE(StringUtils.rightPad(this.getFSCODE(),2,StringUtils.SPACE));
		this.setCARDACT(StringUtils.rightPad(this.getCARDACT(),16,StringUtils.SPACE));
		this.setTOACT(StringUtils.rightPad(this.getTOACT(),20,StringUtils.SPACE));
		this.setACTNAME(StringUtils.rightPad(this.getACTNAME(),20,StringUtils.SPACE));
		this.setD2(StringUtils.rightPad(this.getD2(),2,StringUtils.SPACE));
		this.setSTAN(StringUtils.rightPad(this.getSTAN(),7,StringUtils.SPACE));
		this.setPRCCODE(StringUtils.rightPad(this.getPRCCODE(),4,StringUtils.SPACE));
		this.setDEND1(StringUtils.rightPad(this.getDEND1(),2,StringUtils.SPACE));
		this.setMACCODE(StringUtils.rightPad(this.getMACCODE(),8,StringUtils.SPACE));

		return CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getWSID(),5) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getS1(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getRECFMT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPOAPUSE(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGCAT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMSGTYP(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANDATE(),6) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANTIME(),6) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTRANSEQ(),4) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTDRSEG(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPRCRDACT(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDATATYPE(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDATALEN(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACKNOW(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAGENO(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDATATYPE1(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDATALEN1(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACKNOW1(),1) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPAGENO1(),3) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getD1(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getFSCODE(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getCARDACT(),16) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getTOACT(),20) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getACTNAME(),20) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getD2(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getSTAN(),7) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getPRCCODE(),4) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getDEND1(),2) +
		      CodeGenUtil.asciiToEbcdicDefaultEmpty(this.getMACCODE(),8)
		      ;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
	}
}
