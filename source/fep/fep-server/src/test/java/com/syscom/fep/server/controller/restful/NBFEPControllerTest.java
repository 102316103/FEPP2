package com.syscom.fep.server.controller.restful;

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.ServerBaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;

public class NBFEPControllerTest extends ServerBaseTest {
	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	public void setup() throws Exception {
		SpringBeanFactoryUtil.registerController(NBFEPController.class);
	}

	@AfterAll
	public void tearDown() throws Exception {
		SpringBeanFactoryUtil.unregisterBean(NBFEPController.class);
	}

	@Test //NBPYOtherRequestA EW2262 ABPYEAO01
	public void testNBPYOtherRequestA_EW2262() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20230902002262302</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>EW2262</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2023-09-05T11:05:01.604</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20230905</INDATE>\n" +
				"                <INTIME>110501</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>EW</FSCODE>\n" +
				"                <PCODE>2262</PCODE>\n" +
				"                <TRNSFLAG>2</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>T</BUSINESSTYPE>\n" +
				"                <TRANSAMT>00003138000</TRANSAMT>\n" +
				"                <TRANBRANCH>0560</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>B234513136</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>4440000</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>0004444008819195</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>                       \n" +
				"                <TRNSFRINBANK>0060000</TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT>0000000000000000</TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE></FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE></CUSTPAYFEE>\n" +
				"                <FISCFEE></FISCFEE>                \n" +
				"                <CHAFEE_BRANCH></CHAFEE_BRANCH>\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE></CHAFEE_TYPE>\n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE/>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                    \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME></TRNSFROUTNAME>                \n" +
				"                <SSLTYPE>N</SSLTYPE>\n" +
				"                <LIMITTYPE></LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA>\n" +
				"                  <NPOPID>10004995</NPOPID>\n" +
				"                  <NPPAYTYPE>00279</NPPAYTYPE>\n" +
				"                  <NPFEENO>6101</NPFEENO>\n" +
				"                  <NPID>B234513136 </NPID>\n" +
				"                  <NPPAYNO>1417060060008163</NPPAYNO>\n" +
				"                  <NPPAYENDDATE>99991231</NPPAYENDDATE>\n" +
				"                  <NPBRANCH>1416</NPBRANCH>\n" +
				"                  <IDENTIFIER></IDENTIFIER>\n" +
				"                </PAYDATA>  \n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test // NBPYSelfRequestA ED2261
	public void testNBPYSelfRequestA_ED2261() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20230901002261001</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>ED2261</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2023-09-06T11:05:01.604</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20230906</INDATE>\n" +
				"                <INTIME>110501</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>ED</FSCODE>\n" +
				"                <PCODE>2261</PCODE>\n" +
				"                <TRNSFLAG>2</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>T</BUSINESSTYPE>\n" +
				"                <TRANSAMT>00003138000</TRANSAMT>\n" +
				"                <TRANBRANCH>0560</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>B234513136</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>0560765032011</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>                       \n" +
				"                <TRNSFRINBANK>4440000</TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT>0004444008819195</TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE></FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE>10</CUSTPAYFEE>\n" +
				"                <FISCFEE></FISCFEE>                \n" +
				"                <CHAFEE_BRANCH></CHAFEE_BRANCH>\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE></CHAFEE_TYPE>\n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE/>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                    \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME></TRNSFROUTNAME>                \n" +
				"                <SSLTYPE>N</SSLTYPE>\n" +
				"                <LIMITTYPE></LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA>\n" +
				"                  <NPOPID>10004995</NPOPID>\n" +
				"                  <NPPAYTYPE>00279</NPPAYTYPE>\n" +
				"                  <NPFEENO>6101</NPFEENO>\n" +
				"                  <NPID>B234513136 </NPID>\n" +
				"                  <NPPAYNO>1417060060008163</NPPAYNO>\n" +
				"                  <NPPAYENDDATE>99991231</NPPAYENDDATE>\n" +
				"                  <NPBRANCH>1416</NPBRANCH>\n" +
				"                  <IDENTIFIER></IDENTIFIER>\n" +
				"                </PAYDATA>  \n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test // NBPYSELFISSUE EA2263  ABPYEAOEA
	public void testNBPYSELFISSUE_EA2263() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20231214000000003</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>EA2263</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2023-12-14T11:01:01.604</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20231214</INDATE>\n" +
				"                <INTIME>110101</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>EA</FSCODE>\n" +
				"                <PCODE>2263</PCODE>\n" +
				"                <TRNSFLAG>2</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>T</BUSINESSTYPE>\n" +
				"                <TRANSAMT>100000</TRANSAMT>\n" +
				"                <TRANBRANCH>0560</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>D174869911</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>0000560765032011</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>    \t\t\t\t\n" +
				"                <TRNSFRINBANK>0060000</TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT></TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE></FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE></CUSTPAYFEE>\n" +
				"                <FISCFEE></FISCFEE>                           \n" +
				"                <CHAFEE_BRANCH></CHAFEE_BRANCH>\t\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE></CHAFEE_TYPE>\n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE/>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                       \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME>貶幅俠</TRNSFROUTNAME>                \n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <LIMITTYPE>52</LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA>\t\t\t\t\n" +
				"                  <NPOPID>18888888</NPOPID>\n" +
				"                  <NPPAYTYPE>59999</NPPAYTYPE>\n" +
				"                  <NPFEENO>9999</NPFEENO>\n" +
				"                  <NPID>D174869911 </NPID>\n" +
				"                  <NPPAYNO>0009998765123163</NPPAYNO>\n" +
				"                  <NPPAYENDDATE>99991231</NPPAYENDDATE>\n" +
				"                  <NPBRANCH></NPBRANCH>\n" +
				"                  <IDENTIFIER></IDENTIFIER>\n" +
				"                </PAYDATA>  \t\t\t\t\n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test //NBSelfIssueRequestA TS2521    CBSTESTDATA:1.ABACCO01   2.ABTREAOTS
	public void testNBSelfIssueRequestA_TS2521() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20231128000002</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>TS2521</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-11-28T11:01:01.604</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221128</INDATE>\n" +
				"                <INTIME>110101</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>TEST</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>TS</FSCODE>\n" +
				"                <PCODE>2521</PCODE>\n" +
				"                <TRNSFLAG>2</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>T</BUSINESSTYPE>\n" +
				"                <TRANSAMT>80000</TRANSAMT>\n" +
				"                <TRANBRANCH>0560</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>D174869911</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>0560765032011</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>                   \n" +
				"                <TRNSFRINBANK>0080000</TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE>15</FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE>06</CUSTPAYFEE>\n" +
				"                <FISCFEE>10</FISCFEE>                \n" +
				"                <CHAFEE_BRANCH>9997</CHAFEE_BRANCH>\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE>U</CHAFEE_TYPE>\n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE/>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME>貶幅俠</TRNSFROUTNAME>           \n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <LIMITTYPE>52</LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA></PAYDATA>\n" +
				"                <TEXTMARK>貶幅俠</TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test // NBSelfIssueRequestA TY2532    CBSTESTDATA:1.ABACCO01  2.ABTAXEAO01  BusinessBase.2450值需改
	public void testNBSelfIssueRequestA_TY2532() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBQ20231201000002</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBQ</CHANNEL>\n" +
				"                <MSGID>TY2532</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-12-05T11:55:11.092</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221205</INDATE>\n" +
				"                <INTIME>115510</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>6514</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>10002378</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>TY</FSCODE>\n" +
				"                <PCODE>2532</PCODE>\n" +
				"                <TRNSFLAG>1</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>B</BUSINESSTYPE>\n" +
				"                <TRANSAMT>81600</TRANSAMT>\n" +
				"                <TRANBRANCH>5012</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>03779904</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0065012</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>5012717000001</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>                          \n" +
				"                <TRNSFRINBANK></TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT></TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE>15</FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE>0</CUSTPAYFEE>\n" +
				"                <FISCFEE></FISCFEE>                \n" +
				"                <CHAFEE_BRANCH></CHAFEE_BRANCH>\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE>U</CHAFEE_TYPE>                \n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE>TAX11201</TRNSFROUTNOTE>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME></TRNSFROUTNAME>                \n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <LIMITTYPE>03</LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA>\n" +
				"                  <PAYCATEGORY>11201</PAYCATEGORY>\n" +
				"                  <PAYNO>4102900018160101</PAYNO>\n" +
				"                  <PAYENDDATE>20221231</PAYENDDATE>\n" +
				"                  <ORGAN></ORGAN>\n" +
				"                  <CID></CID>\n" +
				"                  <FILLER></FILLER>\n" +
				"                </PAYDATA>  \n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test // NBTGSelfRequestA TG2521    CBSTESTDATA:ABTREAOTG
	public void testNBTGSelfRequestA_TG2521() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>MBQ20230515000010</CLIENTTRACEID>\n" +
				"                <CHANNEL>MBQ</CHANNEL>\n" +
				"                <MSGID>TG2521</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2023-05-12T11:10:01.604</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20230511</INDATE>\n" +
				"                <INTIME>111001</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>TG</FSCODE>\n" +
				"                <PCODE>2521</PCODE>\n" +
				"                <TRNSFLAG>2</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>S</BUSINESSTYPE>\n" +
				"                <TRANSAMT>8000000</TRANSAMT>\n" +
				"                <TRANBRANCH>0560</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>22099131</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>5012717000001</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO>F121374529</TRNSFRINIDNO>                   \n" +
				"                <TRNSFRINBANK>0080000</TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE>15</FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE>10</CUSTPAYFEE>\n" +
				"                <FISCFEE>00</FISCFEE>                \n" +
				"                <CHAFEE_BRANCH>0560</CHAFEE_BRANCH>\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE>1</CHAFEE_TYPE>\n" +
				"                <TRNSFRINNOTE>202305薪資</TRNSFRINNOTE>\n" +
				"                <TRNSFROUTNOTE/>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>\n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME>台積電</TRNSFROUTNAME>                 \n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <LIMITTYPE>52</LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA></PAYDATA>\n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test  //LEIssueRequestA  LE2566    CBSTESTDATA:ABVAO001
	public void testLEIssueRequestA_LE2566() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20221210000101</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBQ</CHANNEL>\n" +
				"                <MSGID>LE2566</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-12-10T12:30:58.013</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221210</INDATE>\n" +
				"                <INTIME>123058</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>6514</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>10002378</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>LE</FSCODE>\n" +
				"                <PCODE>2566</PCODE>\n" +
				"                <TRANSAMT>00000000000</TRANSAMT>\n" +
				"                <VACATE>02</VACATE>\n" +
				"                <AEIPYTP>03</AEIPYTP>\n" +
				"                <AEIPYBK>006</AEIPYBK>\n" +
				"                <AEIPYBH>0000</AEIPYBH>\n" +
				"                <AEIPCRBK>013</AEIPCRBK>\n" +
				"                <AEIPYAC>0000062505012289</AEIPYAC>\n" +
				"                <TAXIDNO>U120062139</TAXIDNO>\n" +
				"                <MOBILENO>0928104588</MOBILENO>\n" +
				"                <SENDDATA>\n" +
				"                \t<!--授權、線上約定 -B -->\n" +
				"                \t<PAYUNTNO>10002638</PAYUNTNO>\n" +
				"                \t<TAXTYPE>00001</TAXTYPE>\n" +
				"                \t<PAYFEENO>0000</PAYFEENO>\n" +
				"                \t<CLCPYCI>89394750</CLCPYCI>\n" +
				"                \t<AEIPYAC2>0000062505012289</AEIPYAC2>\n" +
				"                \t<!--授權、線上約定 -E -->\n" +
				"                </SENDDATA>\n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <AEIPCRAC>0000062505012289</AEIPCRAC>\n" +
				"                <IC_SEQNO>00000003</IC_SEQNO>\n" +
				"                <ICMARK>000230000224028406250501228920221203010000000000EAF9DDB31F56</ICMARK>\n" +
				"                <IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"                <IC_TAC>E7C2F633263F2284</IC_TAC>\n" +
				"                <AEICDAY>1111210</AEICDAY>\n" +
				"                <AEICTIME>123058</AEICTIME>  \n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test  //LFIssueRequestA  LF2566    CBSTESTDATA:ABVAO0LF
	public void testLFIssueRequestA_LF2566() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20231010000108</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>LF2566</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-12-09T12:30:58.013</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221209</INDATE>\n" +
				"                <INTIME>123057</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>6514</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>10002378</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>LF</FSCODE>\n" +
				"                <PCODE>2566</PCODE>\n" +
				"                <TRANSAMT>00000000000</TRANSAMT>\n" +
				"                <VACATE>10</VACATE>\n" +
				"                <AEIPYTP>00</AEIPYTP>\n" +
				"                <AEIPYBK>006</AEIPYBK>\n" +
				"                <AEIPYBH>0000</AEIPYBH>\n" +
				"                <AEIPCRBK>013</AEIPCRBK>\n" +
				"                <AEIPYAC>0009997705073012</AEIPYAC>\n" +
				"                <TAXIDNO>U120062139</TAXIDNO>\n" +
				"                <MOBILENO>0928104588</MOBILENO>\n" +
				"                <SENDDATA>\n" +
				"                \t<AELFTP>01</AELFTP>\n" +
				"                \t<BIRTHDAY>19861011</BIRTHDAY>\n" +
				"                \t<TELHOME></TELHOME>\n" +
				"                \t<AEIPYUES></AEIPYUES>\n" +
				"                \t<FILLER2></FILLER2>\n" +
				"                </SENDDATA>\n" +
				"                <SSLTYPE>N</SSLTYPE>\n" +
				"                <AEIPCRAC>0000062505012289</AEIPCRAC>\n" +
				"                <IC_SEQNO>00000003</IC_SEQNO>\n" +
				"                <ICMARK>000230000224028406250501228920221203010000000000EAF9DDB31F56</ICMARK>\n" +
				"                <IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"                <IC_TAC>E7C2F633263F2284</IC_TAC>\n" +
				"                <AEICDAY>1111209</AEICDAY>\n" +
				"                <AEICTIME>120947</AEICTIME>  \n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test  //LESelfIssue  LE2566  ABVAO001
	public void testLESelfIssue_LE2566() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20221211000108</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBQ</CHANNEL>\n" +
				"                <MSGID>LE2566</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-12-10T12:30:58.013</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221210</INDATE>\n" +
				"                <INTIME>123058</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>6514</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>10002378</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>LE</FSCODE>\n" +
				"                <PCODE>2566</PCODE>\n" +
				"                <TRANSAMT>00000000000</TRANSAMT>\n" +
				"                <VACATE>02</VACATE>\n" +
				"                <AEIPYTP>03</AEIPYTP>\n" +
				"                <AEIPYBK>006</AEIPYBK>\n" +
				"                <AEIPYBH>0000</AEIPYBH>\n" +
				"                <AEIPCRBK>006</AEIPCRBK>\n" +
				"                <AEIPYAC>0000062505012289</AEIPYAC>\n" +
				"                <TAXIDNO>U120062139</TAXIDNO>\n" +
				"                <MOBILENO>0928104588</MOBILENO>\n" +
				"                <SENDDATA>\n" +
				"                \t<!--授權、線上約定 -B -->\n" +
				"                \t<PAYUNTNO>10000161</PAYUNTNO>\n" +
				"                \t<TAXTYPE>00058</TAXTYPE>\n" +
				"                \t<PAYFEENO>0001</PAYFEENO>\n" +
				"                \t<CLCPYCI>89394750</CLCPYCI>\n" +
				"                \t<AEIPYAC2>0000062505012289</AEIPYAC2>\n" +
				"                \t<!--授權、線上約定 -E -->\n" +
				"                </SENDDATA>\n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <AEIPCRAC>0000062505012289</AEIPCRAC>\n" +
				"                <IC_SEQNO>00000003</IC_SEQNO>\n" +
				"                <ICMARK>000230000224028406250501228920221203010000000000EAF9DDB31F56</ICMARK>\n" +
				"                <IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"                <IC_TAC>E7C2F633263F2284</IC_TAC>\n" +
				"                <AEICDAY>1111210</AEICDAY>\n" +
				"                <AEICTIME>123058</AEICTIME>  \n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test  //LFSelfIssue  LF2566  ABVAO0LF
	public void testLFSelfIssue_LF2566() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20221211000102</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>LF2566</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-12-09T12:30:58.013</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221209</INDATE>\n" +
				"                <INTIME>123057</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>6514</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>10002378</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>LF</FSCODE>\n" +
				"                <PCODE>2566</PCODE>\n" +
				"                <TRANSAMT>00000000000</TRANSAMT>\n" +
				"                <VACATE>10</VACATE>\n" +
				"                <AEIPYTP>00</AEIPYTP>\n" +
				"                <AEIPYBK>006</AEIPYBK>\n" +
				"                <AEIPYBH>0000</AEIPYBH>\n" +
				"                <AEIPCRBK>006</AEIPCRBK>\n" +
				"                <AEIPYAC>0009997705073012</AEIPYAC>\n" +
				"                <TAXIDNO>U120062139</TAXIDNO>\n" +
				"                <MOBILENO>0928104588</MOBILENO>\n" +
				"                <SENDDATA>\n" +
				"                \t<AELFTP>01</AELFTP>\n" +
				"                \t<BIRTHDAY>19861011</BIRTHDAY>\n" +
				"                \t<TELHOME></TELHOME>\n" +
				"                \t<AEIPYUES></AEIPYUES>\n" +
				"                \t<FILLER2></FILLER2>\n" +
				"                </SENDDATA>\n" +
				"                <SSLTYPE>N</SSLTYPE>\n" +
				"                <AEIPCRAC>0000062505012289</AEIPCRAC>\n" +
				"                <IC_SEQNO>00000003</IC_SEQNO>\n" +
				"                <ICMARK>000230000224028406250501228920221203010000000000EAF9DDB31F56</ICMARK>\n" +
				"                <IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"                <IC_TAC>E7C2F633263F2284</IC_TAC>\n" +
				"                <AEICDAY>1111209</AEICDAY>\n" +
				"                <AEICTIME>120947</AEICTIME>  \n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test  // VAInq LF2566
	public void LF2566_VAInq() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20221211000102</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>LF2566</MSGID>\n" +
				"                <MSGKIND>I</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-12-09T12:30:58.013</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221209</INDATE>\n" +
				"                <INTIME>123057</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>IQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>6514</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>10002378</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>LF</FSCODE>\n" +
				"                <PCODE>2566</PCODE>\n" +
				"                <TRANSAMT>00000000000</TRANSAMT>\n" +
				"                <VACATE>10</VACATE>\n" +
				"                <AEIPYTP>00</AEIPYTP>\n" +
				"                <AEIPYBK>006</AEIPYBK>\n" +
				"                <AEIPYBH>0000</AEIPYBH>\n" +
				"                <AEIPCRBK>006</AEIPCRBK>\n" +
				"                <AEIPYAC>0009997705073012</AEIPYAC>\n" +
				"                <TAXIDNO>U120062139</TAXIDNO>\n" +
				"                <MOBILENO>0928104588</MOBILENO>\n" +
				"                <SENDDATA>\n" +
				"                \t<AELFTP>01</AELFTP>\n" +
				"                \t<BIRTHDAY>19861011</BIRTHDAY>\n" +
				"                \t<TELHOME></TELHOME>\n" +
				"                \t<AEIPYUES></AEIPYUES>\n" +
				"                \t<FILLER2></FILLER2>\n" +
				"                </SENDDATA>\n" +
				"                <SSLTYPE>N</SSLTYPE>\n" +
				"                <AEIPCRAC>0000062505012289</AEIPCRAC>\n" +
				"                <IC_SEQNO>00000003</IC_SEQNO>\n" +
				"                <ICMARK>000230000224028406250501228920221203010000000000EAF9DDB31F56</ICMARK>\n" +
				"                <IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"                <IC_TAC>E7C2F633263F2284</IC_TAC>\n" +
				"                <AEICDAY>1111209</AEICDAY>\n" +
				"                <AEICTIME>120947</AEICTIME>  \n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test // NBSelfIssueRequestA EF2561  第一道 : ABACCO01 ABACCO02  第二道 : ABPYEAOEF
	public void EF2561() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20221216000109</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>EF2561</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-11-28T11:02:01.604</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221215</INDATE>\n" +
				"                <INTIME>110201</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ </TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>EF</FSCODE>\n" +
				"                <PCODE>2561</PCODE>\n" +
				"                <TRNSFLAG>2</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>A</BUSINESSTYPE>\n" +
				"                <TRANSAMT>570000</TRANSAMT>\n" +
				"                <TRANBRANCH>0560</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>F121374529</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>0009997705073012</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>                   \n" +
				"                <TRNSFRINBANK>0120000</TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT>0000000000000000</TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE></FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE></CUSTPAYFEE>\n" +
				"                <FISCFEE></FISCFEE>                \n" +
				"                <CHAFEE_BRANCH></CHAFEE_BRANCH>\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE></CHAFEE_TYPE>\n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE/>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME></TRNSFROUTNAME>                \n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <LIMITTYPE>52</LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA>\n" +
				"                  <NPOPID>10000002</NPOPID>\n" +
				"                  <NPPAYTYPE>40005</NPPAYTYPE>\n" +
				"                  <NPFEENO>0062</NPFEENO>\n" +
				"                  <NPID></NPID>\n" +
				"                  <NPPAYNO>7420230022835010</NPPAYNO>\n" +
				"                  <NPPAYENDDATE>20220731</NPPAYENDDATE>\n" +
				"                  <NPBRANCH>9997</NPBRANCH>\n" +
				"                  <IDENTIFIER></IDENTIFIER>\n" +
				"                </PAYDATA>  \n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test // NBInq  EA2263  EAIQTXO01 EAIQTXO02
	public void EA2263() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBP20231217000000010</CLIENTTRACEID>\n" +
				"                <CHANNEL>NBP</CHANNEL>\n" +
				"                <MSGID>EA2263</MSGID>\n" +
				"                <MSGKIND>I</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2023-12-14T11:01:01.604</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20231216</INDATE>\n" +
				"                <INTIME>110101</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>IQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>EA</FSCODE>\n" +
				"                <PCODE>2263</PCODE>\n" +
				"                <TRNSFLAG>2</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>T</BUSINESSTYPE>\n" +
				"                <TRANSAMT>100000</TRANSAMT>\n" +
				"                <TRANBRANCH>0560</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>D174869911</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>0000560765032011</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>    \t\t\t\t\n" +
				"                <TRNSFRINBANK>0060000</TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT></TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE></FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE></CUSTPAYFEE>\n" +
				"                <FISCFEE></FISCFEE>                           \n" +
				"                <CHAFEE_BRANCH></CHAFEE_BRANCH>\t\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE></CHAFEE_TYPE>\n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE/>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                       \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME>貶幅俠</TRNSFROUTNAME>                \n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <LIMITTYPE>52</LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA>\t\t\t\t\n" +
				"                  <NPOPID>18888888</NPOPID>\n" +
				"                  <NPPAYTYPE>59999</NPPAYTYPE>\n" +
				"                  <NPFEENO>9999</NPFEENO>\n" +
				"                  <NPID>D174869911</NPID>\n" +
				"                  <NPPAYNO>0009998765123163</NPPAYNO>\n" +
				"                  <NPPAYENDDATE>99991231</NPPAYENDDATE>\n" +
				"                  <NPBRANCH></NPBRANCH>\n" +
				"                  <IDENTIFIER></IDENTIFIER>\n" +
				"                </PAYDATA>  \t\t\t\t\n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	@Test // NBSelfIssueRequestA  EOI_TY2532  CBSTESTDATA:1.ABACCO01  2.ABTAXEAO01
	public void EOI_TY2532() throws Exception {
		String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"    <SOAP-ENV:Header/>\n" +
				"    <SOAP-ENV:Body>\n" +
				"        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"            <Header>\n" +
				"                <CLIENTTRACEID>NBQ20240101000001</CLIENTTRACEID>\n" +
				"                <CHANNEL>EOI</CHANNEL>\n" +
				"                <MSGID>TY2532</MSGID>\n" +
				"                <MSGKIND>G</MSGKIND>\n" +
				"                <TXNID></TXNID>\n" +
				"                <BRANCHID></BRANCHID>\n" +
				"                <TERMID></TERMID>\n" +
				"                <CLIENTDT>2022-12-05T11:55:11.092</CLIENTDT>\n" +
				"            </Header>\n" +
				"            <SvcRq>\n" +
				"                <INDATE>20221205</INDATE>\n" +
				"                <INTIME>115510</INTIME>\n" +
				"                <IPADDR></IPADDR>\n" +
				"                <TXNTYPE>RQ</TXNTYPE>\n" +
				"                <TERMINALID>EAI00000</TERMINALID>\n" +
				"                <TERMINAL_TYPE>6514</TERMINAL_TYPE>\n" +
				"                <TERMINAL_CHECKNO>10002378</TERMINAL_CHECKNO>\n" +
				"                <FSCODE>TY</FSCODE>\n" +
				"                <PCODE>2532</PCODE>\n" +
				"                <TRNSFLAG>1</TRNSFLAG>\n" +
				"                <BUSINESSTYPE>B</BUSINESSTYPE>\n" +
				"                <TRANSAMT>81600</TRANSAMT>\n" +
				"                <TRANBRANCH>5012</TRANBRANCH>\n" +
				"                <TRNSFROUTIDNO>03779904</TRNSFROUTIDNO>\n" +
				"                <TRNSFROUTBANK>0065012</TRNSFROUTBANK>\n" +
				"                <TRNSFROUTACCNT>5012717000001</TRNSFROUTACCNT>\n" +
				"                <TRNSFRINIDNO></TRNSFRINIDNO>                          \n" +
				"                <TRNSFRINBANK></TRNSFRINBANK>\n" +
				"                <TRNSFRINACCNT></TRNSFRINACCNT>\n" +
				"                <FEEPAYMENTTYPE>15</FEEPAYMENTTYPE>\n" +
				"                <CUSTPAYFEE>0</CUSTPAYFEE>\n" +
				"                <FISCFEE></FISCFEE>                \n" +
				"                <CHAFEE_BRANCH></CHAFEE_BRANCH>\n" +
				"                <CHAFEE_AMT>0</CHAFEE_AMT>\n" +
				"                <FAXFEE>0</FAXFEE>\n" +
				"                <TRANSFEE>0</TRANSFEE>\n" +
				"                <OTHERBANKFEE>0</OTHERBANKFEE>\n" +
				"                <CHAFEE_TYPE>U</CHAFEE_TYPE>                \n" +
				"                <TRNSFRINNOTE/>\n" +
				"                <TRNSFROUTNOTE>TAX11201</TRNSFROUTNOTE>\n" +
				"                <ORITXSTAN></ORITXSTAN>\n" +
				"                <AFFAIRSCODE></AFFAIRSCODE>                \n" +
				"                <CUSTCODE></CUSTCODE>\n" +
				"                <TRNSFROUTNAME></TRNSFROUTNAME>                \n" +
				"                <SSLTYPE>S</SSLTYPE>\n" +
				"                <LIMITTYPE>03</LIMITTYPE>\n" +
				"                <TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"                <PAYDATA>\n" +
				"                  <PAYCATEGORY>11201</PAYCATEGORY>\n" +
				"                  <PAYNO>4102900018160101</PAYNO>\n" +
				"                  <PAYENDDATE>20221231</PAYENDDATE>\n" +
				"                  <ORGAN></ORGAN>\n" +
				"                  <CID></CID>\n" +
				"                  <FILLER></FILLER>\n" +
				"                </PAYDATA>  \n" +
				"                <TEXTMARK></TEXTMARK>\n" +
				"            </SvcRq>\n" +
				"        </esb:MsgRq>\n" +
				"    </SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>\n";
		this.testProcessRequestDataXml(messageXml);
	}

	// 2021/11/11台北新增測試電文 END ADD BY WJ
	private void testProcessRequestDataXml(String messageXml) throws Exception {
		RequestBuilder builder = MockMvcRequestBuilders
				.post("/recv/nbfep")
				.accept(MediaType.APPLICATION_XML)
				.contentType(new MediaType("application", "xml", StandardCharsets.UTF_8))
				.content(messageXml);

		// 執行請求
		ResultActions action = mockMvc.perform(builder);
		// 分析結果
		MvcResult result = action.andExpect(MockMvcResultMatchers.status().isOk()) // 執行狀態
				// .andExpect(MockMvcResultMatchers.jsonPath("message").value(logData.getMessage())) // 期望值
				// .andExpect(MockMvcResultMatchers.jsonPath("messageId").value(logData.getMessageId())) // 期望值
				// .andDo(MockMvcResultHandlers.print()) // 打印
				.andReturn();
		String resultStr = result.getResponse().getContentAsString();
		UnitTestLogger.info("resultStr = [", resultStr, "]");
	}
}

