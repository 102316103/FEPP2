package com.syscom.fep.server.controller.restful;

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

import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.server.ServerBaseTest;

public class HCEControllerTest extends ServerBaseTest {
	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	public void setup() throws Exception {
		SpringBeanFactoryUtil.registerController(HCEController.class);
	}

	@AfterAll
	public void tearDown() throws Exception {
		SpringBeanFactoryUtil.unregisterBean(HCEController.class);
	}

	@Test  // HCEOtherIssueRequestA I12500 他行
	public void I12500() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb/\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE20231123000000102</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>I12500</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID>HCE_GeneralTrans</TXNID>\n" +
				"\t\t\t\t<BRANCHID/>\n" +
				"\t\t\t\t<TERMID/>\n" +
				"\t\t\t\t<CLIENTDT>2023-11-22T16:59:30.517</CLIENTDT>\n" +
				"\t\t\t</Header>\n" +
				"\t\t\t<SvcRq>\n" +
				"\t\t\t\t<INDATE>20231122</INDATE>\n" +
				"\t\t\t\t<INTIME>165930</INTIME>\n" +
				"\t\t\t\t<IPADDR/>\n" +
				"\t\t\t\t<TXNTYPE>RQ</TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>TB006HCE</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>1E12EA43</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>I1</FSCODE>\n" +
				"\t\t\t\t<PCODE>2500</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>00000000000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>8060000</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>1100600000008825</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000001</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>39393938373635313030393533303152635022041815A96B01DEFA9865AE</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>387705F6956342E9000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK/>\n" +
				"\t\t\t\t<TRNSFRINACCNT/>\n" +
				"\t\t\t\t<TRANSTYPEFLAG/>\n" +
				"\t\t\t\t<TEXTMARK/>\n" +
				"\t\t\t\t<ORITXSTAN/>\n" +
				"\t\t\t\t<IC_TAC_DATE/>\n" +
				"\t\t\t\t<IC_TAC_TIME/>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>";
		this.testProcessRequestData(messageXml);
	}

	@Test //  HCEIQSelfIssue I125000 自行
	public void I125000() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb/\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE20230831000000104</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>I12500</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID/>\n" +
				"\t\t\t\t<BRANCHID/>\n" +
				"\t\t\t\t<TERMID/>\n" +
				"\t\t\t\t<CLIENTDT>2023-08-30T16:59:30.517</CLIENTDT>\n" +
				"\t\t\t</Header>\n" +
				"\t\t\t<SvcRq>\n" +
				"\t\t\t\t<INDATE>20230830</INDATE>\n" +
				"\t\t\t\t<INTIME>165930</INTIME>\n" +
				"\t\t\t\t<IPADDR/>\n" +
				"\t\t\t\t<TXNTYPE>RQ</TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>TB006HCE</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>1E12EA43</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>I1</FSCODE>\n" +
				"\t\t\t\t<PCODE>2500</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>0</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0060000</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>1100600003298324</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000001</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>39393938373635313030393533303152635022041815A96B01DEFA9865AE</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>387705F6956342E9000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK/>\n" +
				"\t\t\t\t<TRNSFRINACCNT/>\n" +
				"\t\t\t\t<TRANSTYPEFLAG/>\n" +
				"\t\t\t\t<TEXTMARK/>\n" +
				"\t\t\t\t<ORITXSTAN/>\n" +
				"\t\t\t\t<IC_TAC_DATE/>\n" +
				"\t\t\t\t<IC_TAC_TIME/>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>";
		this.testProcessRequestData(messageXml);
	}

	@Test // HCESelfIssueRequestA T42521
	public void T42521() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE20230830000000303</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>T42521</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID/>\n" +
				"\t\t\t\t<BRANCHID/>\n" +
				"\t\t\t\t<TERMID/>\n" +
				"\t\t\t\t<CLIENTDT>2023-08-30T17:05:00.604</CLIENTDT>\n" +
				"\t\t\t</Header>\n" +
				"\t\t\t<SvcRq>\n" +
				"\t\t\t\t<INDATE>20230830</INDATE>\n" +
				"\t\t\t\t<INTIME>170500</INTIME>\n" +
				"\t\t\t\t<IPADDR/>\n" +
				"\t\t\t\t<TXNTYPE>RQ</TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>EAI00000</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>TEST</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>T4</FSCODE>\n" +
				"\t\t\t\t<PCODE>2521</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>130000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>0560765032011</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000006</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>3035363036393931333230323630315263502302260734B428F003D7F6FD</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>C8207821CA35EAF2000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK>0080000</TRNSFRINBANK>\n" +
				"\t\t\t\t<TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
				"\t\t\t\t<TRANSTYPEFLAG/>\n" +
				"\t\t\t\t<TEXTMARK/>\n" +
				"\t\t\t\t<ORITXSTAN/>\n" +
				"\t\t\t\t<IC_TAC_DATE/>\n" +
				"\t\t\t\t<IC_TAC_TIME/>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>";
		this.testProcessRequestData(messageXml);
	}

	@Test // HCEOtherIssueRequestA TW2522
	public void TW2522() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE20230920000000104</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>TW2522</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID></TXNID>\n" +
				"\t\t\t\t<BRANCHID></BRANCHID>\n" +
				"\t\t\t\t<TERMID></TERMID>\n" +
				"\t\t\t\t<CLIENTDT>2024-01-16T09:00:01.000</CLIENTDT>\n" +
				"\t\t\t</Header>    \n" +
				"\t\t\t<SvcRq >\n" +
				"\t\t\t\t<INDATE>20240116</INDATE>\n" +
				"\t\t\t\t<INTIME>090001</INTIME>\n" +
				"\t\t\t\t<IPADDR></IPADDR>\n" +
				"\t\t\t\t<TXNTYPE>RQ </TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>EAI00000</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>TEST</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>TW</FSCODE>\n" +
				"\t\t\t\t<PCODE>2522</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>10000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0080000</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>0000100100000021</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000006</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>3035363036393931333230323630315263502302260734B428F003D7F6FD</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>C8207821CA35EAF2000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK>0060560</TRNSFRINBANK>\n" +
				"\t\t\t\t<TRNSFRINACCNT>0560765032011</TRNSFRINACCNT>\n" +
				"\t\t\t\t<TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"\t\t\t\t<TEXTMARK>></TEXTMARK>\n" +
				"\t\t\t\t<ORITXSTAN></ORITXSTAN>\n" +
				"\t\t\t\t<IC_TAC_DATE></IC_TAC_DATE>\n" +
				"\t\t\t\t<IC_TAC_TIME></IC_TAC_TIME>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>\n";
		this.testProcessRequestData(messageXml);
	}
	@Test // HCEOtherIssueRequestA TA2523
	public void TA2523() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE20230921000000103</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>TA2523</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID></TXNID>\n" +
				"\t\t\t\t<BRANCHID></BRANCHID>\n" +
				"\t\t\t\t<TERMID></TERMID>\n" +
				"\t\t\t\t<CLIENTDT>2024-01-16T09:00:01.000</CLIENTDT>\n" +
				"\t\t\t</Header>    \n" +
				"\t\t\t<SvcRq >\n" +
				"\t\t\t\t<INDATE>20240116</INDATE>\n" +
				"\t\t\t\t<INTIME>090001</INTIME>\n" +
				"\t\t\t\t<IPADDR></IPADDR>\n" +
				"\t\t\t\t<TXNTYPE>RQ </TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>EAI00000</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>TEST</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>TA</FSCODE>\n" +
				"\t\t\t\t<PCODE>2523</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>10000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0080000</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>0000100100000021</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000006</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>3035363036393931333230323630315263502302260734B428F003D7F6FD</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>C8207821CA35EAF2000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK>0080000</TRNSFRINBANK>\n" +
				"\t\t\t\t<TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
				"\t\t\t\t<TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"\t\t\t\t<TEXTMARK>></TEXTMARK>\n" +
				"\t\t\t\t<ORITXSTAN></ORITXSTAN>\n" +
				"\t\t\t\t<IC_TAC_DATE></IC_TAC_DATE>\n" +
				"\t\t\t\t<IC_TAC_TIME></IC_TAC_TIME>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>\n";
		this.testProcessRequestData(messageXml);
	}

	@Test // HCETRSelfIssue T225230 自行
	public void T225230() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE202308280000303</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>T22523</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID/>\n" +
				"\t\t\t\t<BRANCHID/>\n" +
				"\t\t\t\t<TERMID/>\n" +
				"\t\t\t\t<CLIENTDT>2023-08-28T14:20:04.604</CLIENTDT>\n" +
				"\t\t\t</Header>\n" +
				"\t\t\t<SvcRq>\n" +
				"\t\t\t\t<INDATE>20230828</INDATE>\n" +
				"\t\t\t\t<INTIME>142004</INTIME>\n" +
				"\t\t\t\t<IPADDR/>\n" +
				"\t\t\t\t<TXNTYPE>RQ</TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>EAI00000</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>993FCDE2</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>T2</FSCODE>\n" +
				"\t\t\t\t<PCODE>2523</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>10000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>1100600003216938</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000028</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>303536303938383030333431323031526350230227113062FD9A1B31376A</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>0CE2AD83DB06CC2C000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK>0060000</TRNSFRINBANK>\n" +
				"\t\t\t\t<TRNSFRINACCNT>0000560699132026</TRNSFRINACCNT>\n" +
				"\t\t\t\t<TRANSTYPEFLAG/>\n" +
				"\t\t\t\t<TEXTMARK/>\n" +
				"\t\t\t\t<ORITXSTAN/>\n" +
				"\t\t\t\t<IC_TAC_DATE>20221128</IC_TAC_DATE>\n" +
				"\t\t\t\t<IC_TAC_TIME>142001</IC_TAC_TIME>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>";
		this.testProcessRequestData(messageXml);
	}

	@Test // HCEOtherIssueRequestA TR2524
	public void TR2524() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE20230922000000105</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>TR2524</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID></TXNID>\n" +
				"\t\t\t\t<BRANCHID></BRANCHID>\n" +
				"\t\t\t\t<TERMID></TERMID>\n" +
				"\t\t\t\t<CLIENTDT>2024-01-16T09:00:01.000</CLIENTDT>\n" +
				"\t\t\t</Header>    \n" +
				"\t\t\t<SvcRq >\n" +
				"\t\t\t\t<INDATE>20240116</INDATE>\n" +
				"\t\t\t\t<INTIME>090001</INTIME>\n" +
				"\t\t\t\t<IPADDR></IPADDR>\n" +
				"\t\t\t\t<TXNTYPE>RQ </TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>EAI00000</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>TEST</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>TR</FSCODE>\n" +
				"\t\t\t\t<PCODE>2524</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>10000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0080000</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>0560765032011</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000006</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>3035363036393931333230323630315263502302260734B428F003D7F6FD</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>C8207821CA35EAF2000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK>0080000</TRNSFRINBANK>\n" +
				"\t\t\t\t<TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
				"\t\t\t\t<TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
				"\t\t\t\t<TEXTMARK>></TEXTMARK>\n" +
				"\t\t\t\t<ORITXSTAN></ORITXSTAN>\n" +
				"\t\t\t\t<IC_TAC_DATE></IC_TAC_DATE>\n" +
				"\t\t\t\t<IC_TAC_TIME></IC_TAC_TIME>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>\n";
		this.testProcessRequestData(messageXml);
	}

	@Test // HCEInq 查詢交易
	public void HCEInq() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>HCE202308280000303</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>T22523</MSGID>\n" +
				"\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
				"\t\t\t\t<TXNID/>\n" +
				"\t\t\t\t<BRANCHID/>\n" +
				"\t\t\t\t<TERMID/>\n" +
				"\t\t\t\t<CLIENTDT>2023-08-28T14:20:04.604</CLIENTDT>\n" +
				"\t\t\t</Header>\n" +
				"\t\t\t<SvcRq>\n" +
				"\t\t\t\t<INDATE>20230828</INDATE>\n" +
				"\t\t\t\t<INTIME>142004</INTIME>\n" +
				"\t\t\t\t<IPADDR/>\n" +
				"\t\t\t\t<TXNTYPE>IQ</TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>EAI00000</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>993FCDE2</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>T2</FSCODE>\n" +
				"\t\t\t\t<PCODE>2523</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>10000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>1100600003216938</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000028</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>303536303938383030333431323031526350230227113062FD9A1B31376A</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>0CE2AD83DB06CC2C000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK>0060000</TRNSFRINBANK>\n" +
				"\t\t\t\t<TRNSFRINACCNT>0000560699132026</TRNSFRINACCNT>\n" +
				"\t\t\t\t<TRANSTYPEFLAG/>\n" +
				"\t\t\t\t<TEXTMARK/>\n" +
				"\t\t\t\t<ORITXSTAN/>\n" +
				"\t\t\t\t<IC_TAC_DATE>20221128</IC_TAC_DATE>\n" +
				"\t\t\t\t<IC_TAC_TIME>142001</IC_TAC_TIME>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>";
		this.testProcessRequestData(messageXml);
	}

	@Test // 轉帳 T22523 HCETRSelfIssue
	public void TEST() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header/><soapenv:Body><esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\"><Header><CLIENTTRACEID>202403611562421705408331</CLIENTTRACEID><CHANNEL>HCA</CHANNEL><MSGID>T22523</MSGID><MSGKIND>G</MSGKIND><TXNID>TransferTransactions</TXNID><BRANCHID></BRANCHID><TERMID></TERMID><CLIENTDT>2024-02-05T11:56:48.331</CLIENTDT></Header><SvcRq xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"TransferTransactionsSvcRqType\"><INDATE>20240205</INDATE><INTIME>115648</INTIME><IPADDR></IPADDR><TXNTYPE>RQ</TXNTYPE><TERMINALID>TB006HCE</TERMINALID><TERMINAL_TYPE>A518</TERMINAL_TYPE><TERMINAL_CHECKNO>A8311EEB</TERMINAL_CHECKNO><FSCODE>T2</FSCODE><PCODE>2523</PCODE><TRANSAMT>+000000000000100</TRANSAMT><TRNSFROUTBANK>0060000</TRNSFROUTBANK><TRNSFROUTACCNT>1100600000008825</TRNSFROUTACCNT><IC_SEQNO>00000405</IC_SEQNO><ICMARK>393939383736353130303935333031526350240205127A62BD3176FD5ABB</ICMARK><IC_TAC_LEN>000A</IC_TAC_LEN><IC_TAC>DA6A63267FF5CADA000000000000000000000000</IC_TAC><TRNSFRINBANK>006</TRNSFRINBANK><TRNSFRINACCNT>0009997965777574</TRNSFRINACCNT><TRANSTYPEFLAG> </TRANSTYPEFLAG><TEXTMARK></TEXTMARK><ORITXSTAN></ORITXSTAN><IC_TAC_DATE>20240205</IC_TAC_DATE><IC_TAC_TIME>115624</IC_TAC_TIME></SvcRq></esb:MsgRq></soapenv:Body></soapenv:Envelope>";
		this.testProcessRequestData(messageXml);
	}

	@Test // 合庫餘額查詢 I12500 HCEIQSelfIssue
	public void TEST1() throws Exception {
		String messageXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
				"\t<soapenv:Header/>\n" +
				"\t<soapenv:Body>\n" +
				"\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
				"\t\t\t<Header>\n" +
				"\t\t\t\t<CLIENTTRACEID>202405209233892578619671</CLIENTTRACEID>\n" +
				"\t\t\t\t<CHANNEL>HCA</CHANNEL>\n" +
				"\t\t\t\t<MSGID>I12500</MSGID>\n" +
				"\t\t\t\t<MSGKIND>I</MSGKIND>\n" +
				"\t\t\t\t<TXNID>BalanceTransactionInquiry</TXNID>\n" +
				"\t\t\t\t<BRANCHID/>\n" +
				"\t\t\t\t<TERMID/>\n" +
				"\t\t\t\t<CLIENTDT>2024-02-21T09:23:40.949</CLIENTDT>\n" +
				"\t\t\t</Header>\n" +
				"\t\t\t<SvcRq xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"BalanceTransactionInquirySvcRqType\">\n" +
				"\t\t\t\t<INDATE>20240221</INDATE>\n" +
				"\t\t\t\t<INTIME>092340</INTIME>\n" +
				"\t\t\t\t<IPADDR/>\n" +
				"\t\t\t\t<TXNTYPE>IQ</TXNTYPE>\n" +
				"\t\t\t\t<TERMINALID>TB006HCE</TERMINALID>\n" +
				"\t\t\t\t<TERMINAL_TYPE>A518</TERMINAL_TYPE>\n" +
				"\t\t\t\t<TERMINAL_CHECKNO>54868877</TERMINAL_CHECKNO>\n" +
				"\t\t\t\t<FSCODE>I1</FSCODE>\n" +
				"\t\t\t\t<PCODE>2500</PCODE>\n" +
				"\t\t\t\t<TRANSAMT>+000000000000000</TRANSAMT>\n" +
				"\t\t\t\t<TRNSFROUTBANK>0060000</TRNSFROUTBANK>\n" +
				"\t\t\t\t<TRNSFROUTACCNT>1100600000008825</TRNSFROUTACCNT>\n" +
				"\t\t\t\t<IC_SEQNO>00000457</IC_SEQNO>\n" +
				"\t\t\t\t<ICMARK>39393938373635313030393533303152635024022110A8087FBF7A3601AD</ICMARK>\n" +
				"\t\t\t\t<IC_TAC_LEN>000A</IC_TAC_LEN>\n" +
				"\t\t\t\t<IC_TAC>5C1043C5AD74979D000000000000000000000000</IC_TAC>\n" +
				"\t\t\t\t<TRNSFRINBANK/>\n" +
				"\t\t\t\t<TRNSFRINACCNT>0000000000000000</TRNSFRINACCNT>\n" +
				"\t\t\t\t<TRANSTYPEFLAG/>\n" +
				"\t\t\t\t<TEXTMARK/>\n" +
				"\t\t\t\t<ORITXSTAN/>\n" +
				"\t\t\t\t<IC_TAC_DATE/>\n" +
				"\t\t\t\t<IC_TAC_TIME/>\n" +
				"\t\t\t</SvcRq>\n" +
				"\t\t</esb:MsgRq>\n" +
				"\t</soapenv:Body>\n" +
				"</soapenv:Envelope>";
		this.testProcessRequestData(messageXml);
	}

	private void testProcessRequestData(String messageXml) throws Exception {
		RequestBuilder builder = MockMvcRequestBuilders
				.post("/recv/hce")
				.accept(MediaType.APPLICATION_XML)
				.contentType(MediaType.APPLICATION_XML)
				.content(messageXml);
		// 執行請求
		ResultActions action = mockMvc.perform(builder);
		// 分析結果
		MvcResult result = action.andExpect(MockMvcResultMatchers.status().isOk()) // 執行狀態
				.andReturn();
		String resultStr = result.getResponse().getContentAsString();
//		UnitTestLogger.info("resultStr = [", resultStr, "]");
	}
}
