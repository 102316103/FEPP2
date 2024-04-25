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

public class IVRControllerTest extends ServerBaseTest {
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() throws Exception {
        SpringBeanFactoryUtil.registerController(IVRController.class);
    }

    @AfterAll
    public void tearDown() throws Exception {
        SpringBeanFactoryUtil.unregisterBean(IVRController.class);
    }

    @Test // IVRTRSelfRequestA TD2521  第一道:ABTRVOO01  第二道:ABTRVOO02
    public void TD2521() throws Exception {
        String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t  <SOAP-ENV:Header/>\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
                "        \t  <Header>\n" +
                "                <CLIENTTRACEID>VO20231128000000105</CLIENTTRACEID>\n" +
                "                <CHANNEL>VO</CHANNEL>\n" +
                "                <MSGID>TD2521</MSGID>\n" +
                "                <MSGKIND>G</MSGKIND>\n" +
                "                <TXNID>00000001</TXNID>\n" +
                "                <BRANCHID></BRANCHID>\n" +
                "                <TERMID></TERMID>\n" +
                "                <CLIENTDT>2023-11-28T14:20:04.604</CLIENTDT>\n" +
                "            </Header>    \n" +
                "            <SvcRq>\n" +
                "                <INDATE>20231128</INDATE>\n" +
                "                <INTIME>142004</INTIME>\n" +
                "                <TXNTYPE>RQ</TXNTYPE>\n" +
                "                <TERMINALID>VO</TERMINALID>\n" +
                "                <TERMINAL_TYPE>V997</TERMINAL_TYPE>\n" +
                "                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
                "                <FSCODE>TD</FSCODE>\n" +
                "                <PCODE>2521</PCODE>\n" +
                "                <IVTXC>00000001</IVTXC>\n" +
                "                <IVBHI>V997</IVBHI>\n" +
                "                <IVTLI>09</IVTLI>\n" +
                "                <IVOPR>09</IVOPR>\n" +
                "                <IVSEQ>997911</IVSEQ>\n" +
                "                <IVTRMNO>0526470</IVTRMNO>\n" +
                "                <IVMAC>61E725A9</IVMAC>\n" +
                "                <IVSNO>9A02</IVSNO>\n" +
                "                <IVPPSYN>544F8E21</IVPPSYN>\n" +
                "                <IVPINBLK>31C4BCC7E407036958A987F635C88DDF</IVPINBLK>\n" +
                "                <IVMACSYN>E64375A3</IVMACSYN>                \n" +
                "                <IVFACCT>9998717876541</IVFACCT>\n" +
                "                <IVAMT>99</IVAMT>\n" +
                "                <IVSENDF>2</IVSENDF>\n" +
                "                <PAYDATA>\n" +
                "                \t<IVTBANK>008</IVTBANK>\n" +
                "                \t<IVTACCN>0000100200220118</IVTACCN>\n" +
                "                \t<FILLER1></FILLER1>\n" +
                "                </PAYDATA>\t\n" +
                "            </SvcRq>\n" +
                "        </esb:MsgRq>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>\n";
        this.testProcessRequestData(messageXml);
    }

    @Test // IVRTXSelfRequestA T52523  第一道:ABTAXVOO01  第二道:ABTAXVOO02
    public void T52523() throws Exception {
        String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t  <SOAP-ENV:Header/>\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
                "        \t  <Header>\n" +
                "                <CLIENTTRACEID>VO20231128000000203</CLIENTTRACEID>\n" +
                "                <CHANNEL>VO</CHANNEL>\n" +
                "                <MSGID>T52532</MSGID>\n" +
                "                <MSGKIND>G</MSGKIND>\n" +
                "                <TXNID>00000002</TXNID>\n" +
                "                <BRANCHID></BRANCHID>\n" +
                "                <TERMID></TERMID>\n" +
                "                <CLIENTDT>2023-11-28T14:20:04.604</CLIENTDT>\n" +
                "            </Header>    \n" +
                "            <SvcRq>\n" +
                "                <INDATE>20231128</INDATE>\n" +
                "                <INTIME>142004</INTIME>\n" +
                "                <TXNTYPE>RQ</TXNTYPE>\n" +
                "                <TERMINALID>VO</TERMINALID>\n" +
                "                <TERMINAL_TYPE>V997</TERMINAL_TYPE>\n" +
                "                <TERMINAL_CHECKNO>00001704</TERMINAL_CHECKNO>\n" +
                "                <FSCODE>T5</FSCODE>\n" +
                "                <PCODE>2532</PCODE>\n" +
                "                <IVTXC>00000002</IVTXC>\n" +
                "                <IVBHI>V997</IVBHI>\n" +
                "                <IVTLI>01</IVTLI>\n" +
                "                <IVOPR>04</IVOPR>\n" +
                "                <IVSEQ>997930</IVSEQ>\n" +
                "                <IVTRMNO>0526474</IVTRMNO>\n" +
                "                <IVMAC>4685401A</IVMAC>\n" +
                "                <IVSNO>9A02</IVSNO>\n" +
                "                <IVPPSYN>544F8E21</IVPPSYN>\n" +
                "                <IVPINBLK>31C4BCC7E407036958A987F635C88DDF</IVPINBLK>\n" +
                "                <IVMACSYN>E64375A3</IVMACSYN>                \n" +
                "                <IVFACCT>9998717876541</IVFACCT>\n" +
                "                <IVAMT>99</IVAMT>\n" +
                "                <IVSENDF>1</IVSENDF>\n" +
                "                <PAYDATA>\n" +
                "                \t<IVTAXTYP>15001</IVTAXTYP>\n" +
                "                \t<IVTAXORG>123</IVTAXORG>\n" +
                "                \t<IVTAXIDN>A123456789 </IVTAXIDN>\n" +
                "                \t<IVTAXBLN></IVTAXBLN>\n" +
                "                \t<IVTAXDAT></IVTAXDAT>\n" +
                "                \t<FILLER2></FILLER2>\n" +
                "                </PAYDATA>\t\n" +
                "            </SvcRq>\n" +
                "        </esb:MsgRq>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>\n";
        this.testProcessRequestData(messageXml);
    }

    @Test // TD2521 IVRTRSelfRequestA
    public void Heku2521() throws Exception {
        String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<SOAP-ENV:Header/>\n" +
                "\t<SOAP-ENV:Body>\n" +
                "\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
                "\t\t\t<Header>\n" +
                "\t\t\t\t<CLIENTTRACEID>20240215000000374</CLIENTTRACEID>\n" +
                "\t\t\t\t<CHANNEL>VO</CHANNEL>\n" +
                "\t\t\t\t<MSGID>TD2521</MSGID>\n" +
                "\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
                "\t\t\t\t<TXNID>00000295</TXNID>\n" +
                "\t\t\t\t<BRANCHID/>\n" +
                "\t\t\t\t<TERMID/>\n" +
                "\t\t\t\t<CLIENTDT>2024-02-15T15:39:38.000</CLIENTDT>\n" +
                "\t\t\t</Header>\n" +
                "\t\t\t<SvcRq>\n" +
                "\t\t\t\t<INDATE>20240215</INDATE>\n" +
                "\t\t\t\t<INTIME>153938</INTIME>\n" +
                "\t\t\t\t<IPADDR>10.0.6.3</IPADDR>\n" +
                "\t\t\t\t<TXNTYPE>RQ </TXNTYPE>\n" +
                "\t\t\t\t<TERMINALID>VO</TERMINALID>\n" +
                "\t\t\t\t<TERMINAL_TYPE>V997</TERMINAL_TYPE>\n" +
                "\t\t\t\t<TERMINAL_CHECKNO>V997</TERMINAL_CHECKNO>\n" +
                "\t\t\t\t<FSCODE>TD</FSCODE>\n" +
                "\t\t\t\t<PCODE>2521</PCODE>\n" +
                "\t\t\t\t<TRNSFLAG>2</TRNSFLAG>\n" +
                "\t\t\t\t<BUSINESSTYPE>T</BUSINESSTYPE>\n" +
                "\t\t\t\t<TRANSAMT>150</TRANSAMT>\n" +
                "\t\t\t\t<TRANBRANCH>0560</TRANBRANCH>\n" +
                "\t\t\t\t<TRNSFROUTIDNO>D174869911</TRNSFROUTIDNO>\n" +
                "\t\t\t\t<TRNSFROUTNAME/>\n" +
                "\t\t\t\t<TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
                "\t\t\t\t<TRNSFROUTACCNT>0560765032011</TRNSFROUTACCNT>\n" +
                "\t\t\t\t<TRNSFRINIDNO/>\n" +
                "\t\t\t\t<TRNSFRINBANK>008</TRNSFRINBANK>\n" +
                "\t\t\t\t<TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
                "\t\t\t\t<FEEPAYMENTTYPE>15</FEEPAYMENTTYPE>\n" +
                "\t\t\t\t<CUSTPAYFEE>06</CUSTPAYFEE>\n" +
                "\t\t\t\t<FISCFEE>10</FISCFEE>\n" +
                "\t\t\t\t<CHAFEE_BRANCH>9997</CHAFEE_BRANCH>\n" +
                "\t\t\t\t<CHAFEE_AMT>0</CHAFEE_AMT>\n" +
                "\t\t\t\t<FAXFEE>0</FAXFEE>\n" +
                "\t\t\t\t<TRANSFEE>0</TRANSFEE>\n" +
                "\t\t\t\t<OTHERBANKFEE>0</OTHERBANKFEE>\n" +
                "\t\t\t\t<CHAFEE_TYPE>U</CHAFEE_TYPE>\n" +
                "\t\t\t\t<TRNSFRINNOTE/>\n" +
                "\t\t\t\t<TRNSFROUTNOTE/>\n" +
                "\t\t\t\t<ORITXSTAN/>\n" +
                "\t\t\t\t<AFFAIRSCODE/>\n" +
                "\t\t\t\t<CUSTCODE/>\n" +
                "\t\t\t\t<SSLTYPE>S</SSLTYPE>\n" +
                "\t\t\t\t<LIMITTYPE>52</LIMITTYPE>\n" +
                "\t\t\t\t<TRANSTYPEFLAG/>\n" +
                "\t\t\t\t<PAYDATA>\n" +
                "\t\t\t\t\t<!--繳稅 -B -->\n" +
                "\t\t\t\t\t<PAYCATEGORY/>\n" +
                "\t\t\t\t\t<PAYNO/>\n" +
                "\t\t\t\t\t<PAYENDDATE/>\n" +
                "\t\t\t\t\t<ORGAN/>\n" +
                "\t\t\t\t\t<CID/>\n" +
                "\t\t\t\t\t<FILLER/>\n" +
                "\t\t\t\t\t<!--繳稅 -B -->\n" +
                "\t\t\t\t\t<!--汽燃料費、全國繳費 -B -->\n" +
                "\t\t\t\t\t<NPOPID>18888888</NPOPID>\n" +
                "\t\t\t\t\t<NPPAYTYPE>59999</NPPAYTYPE>\n" +
                "\t\t\t\t\t<NPFEENO>9999</NPFEENO>\n" +
                "\t\t\t\t\t<NPID>D174869911 </NPID>\n" +
                "\t\t\t\t\t<NPPAYNO>0009998765123163</NPPAYNO>\n" +
                "\t\t\t\t\t<NPPAYENDDATE>99991231</NPPAYENDDATE>\n" +
                "\t\t\t\t\t<NPBRANCH/>\n" +
                "\t\t\t\t\t<IDENTIFIER/>\n" +
                "\t\t\t\t\t<!--汽燃料費、全國繳費 -E -->\n" +
                "\t\t\t\t</PAYDATA>\n" +
                "\t\t\t\t<TEXTMARK/>\n" +
                "\t\t\t</SvcRq>\n" +
                "\t\t</esb:MsgRq>\n" +
                "\t</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>\n";
        this.testProcessRequestData(messageXml);
    }

    @Test // T52532 IVRTXSelfRequestA  ABTRVOO01
    public void Heku2532() throws Exception {
        String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<SOAP-ENV:Header/>\n" +
                "\t\t<SOAP-ENV:Body>\n" +
                "\t\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
                "\t\t\t\t<Header>\n" +
                "\t\t\t\t\t<CLIENTTRACEID>20240215000000400</CLIENTTRACEID>\n" +
                "\t\t\t\t\t<CHANNEL>VO</CHANNEL>\n" +
                "\t\t\t\t\t<MSGID>T52532</MSGID>\n" +
                "\t\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
                "\t\t\t\t\t<TXNID>00000295</TXNID>\n" +
                "\t\t\t\t\t<BRANCHID></BRANCHID>\n" +
                "\t\t\t\t\t<TERMID></TERMID>\n" +
                "\t\t\t\t\t<CLIENTDT>2024-02-15T15:39:38.000</CLIENTDT>\n" +
                "\t\t\t\t</Header>    \n" +
                "\t\t\t\t<SvcRq>\n" +
                "\t\t\t\t\t<INDATE>20240215</INDATE>\n" +
                "\t\t\t\t\t<INTIME>153938</INTIME>\n" +
                "\t\t\t\t\t<IPADDR>10.0.6.3</IPADDR>\n" +
                "\t\t\t\t\t<TXNTYPE>RQ</TXNTYPE>\n" +
                "\t\t\t\t\t<TERMINALID>VO</TERMINALID>\n" +
                "\t\t\t\t\t<TERMINAL_TYPE>V997</TERMINAL_TYPE>\n" +
                "\t\t\t\t\t<TERMINAL_CHECKNO>V997</TERMINAL_CHECKNO>\n" +
                "\t\t\t\t\t<FSCODE>T5</FSCODE>\n" +
                "\t\t\t\t\t<PCODE>2532</PCODE>\n" +
                "\t\t\t\t\t<TRNSFLAG>2</TRNSFLAG>\n" +
                "\t\t\t\t\t<BUSINESSTYPE>B</BUSINESSTYPE>\n" +
                "\t\t\t\t\t<TRANSAMT>150</TRANSAMT>\n" +
                "\t\t\t\t\t<TRANBRANCH>0560</TRANBRANCH>\n" +
                "\t\t\t\t\t<TRNSFROUTIDNO>D174869911</TRNSFROUTIDNO>\n" +
                "\t\t\t\t\t<TRNSFROUTNAME></TRNSFROUTNAME>\n" +
                "\t\t\t\t\t<TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
                "\t\t\t\t\t<TRNSFROUTACCNT>0560765032011</TRNSFROUTACCNT>\n" +
                "\t\t\t\t\t<TRNSFRINIDNO></TRNSFRINIDNO>                \n" +
                "\t\t\t\t\t<TRNSFRINBANK>008</TRNSFRINBANK>\n" +
                "\t\t\t\t\t<TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
                "\t\t\t\t\t<FEEPAYMENTTYPE>15</FEEPAYMENTTYPE>\n" +
                "\t\t\t\t\t<CUSTPAYFEE>15</CUSTPAYFEE>\n" +
                "\t\t\t\t\t<FISCFEE>10</FISCFEE>\n" +
                "\t\t\t\t\t<CHAFEE_BRANCH>9997</CHAFEE_BRANCH>\n" +
                "\t\t\t\t\t<CHAFEE_AMT>0</CHAFEE_AMT>\n" +
                "\t\t\t\t\t<FAXFEE>0</FAXFEE>\n" +
                "\t\t\t\t\t<TRANSFEE>0</TRANSFEE>\n" +
                "\t\t\t\t\t<OTHERBANKFEE>0</OTHERBANKFEE>\n" +
                "\t\t\t\t\t<CHAFEE_TYPE>U</CHAFEE_TYPE>\n" +
                "\t\t\t\t\t<TRNSFRINNOTE/>\n" +
                "\t\t\t\t\t<TRNSFROUTNOTE/>\n" +
                "\t\t\t\t\t<ORITXSTAN></ORITXSTAN>\n" +
                "\t\t\t\t\t<CUSTCODE></CUSTCODE>\n" +
                "\t\t\t\t\t<SSLTYPE>S</SSLTYPE>\n" +
                "\t\t\t\t\t<LIMITTYPE>52</LIMITTYPE>\n" +
                "\t\t\t\t\t<TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
                "\t\t\t\t\t<PAYDATA>\n" +
                "\t\t\t\t\t\t<PAYCATEGORY>59999</PAYCATEGORY>\n" +
                "\t\t\t\t\t\t<PAYNO>0009998765123163</PAYNO>\n" +
                "\t\t\t\t\t\t<PAYENDDATE>99991231</PAYENDDATE>\n" +
                "\t\t\t\t\t\t<ORGAN></ORGAN>\n" +
                "\t\t\t\t\t\t<CID>D174869911</CID>\n" +
                "\t\t\t\t\t\t<FILLER>9999</FILLER>\n" +
                "\t\t\t\t\t\t<NPOPID></NPOPID>\n" +
                "\t\t\t\t\t\t<NPPAYTYPE></NPPAYTYPE>\n" +
                "\t\t\t\t\t\t<NPFEENO></NPFEENO>\n" +
                "\t\t\t\t\t\t<NPID></NPID>\n" +
                "\t\t\t\t\t\t<NPPAYNO></NPPAYNO>\n" +
                "\t\t\t\t\t\t<NPPAYENDDATE></NPPAYENDDATE>\n" +
                "\t\t\t\t\t\t<NPBRANCH></NPBRANCH>\n" +
                "\t\t\t\t\t\t<IDENTIFIER></IDENTIFIER>\n" +
                "\t\t\t\t\t</PAYDATA>\n" +
                "\t\t\t\t<TEXTMARK></TEXTMARK>                \n" +
                "\t\t\t</SvcRq>\n" +
                "\t\t</esb:MsgRq>\n" +
                "\t</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>\n";
        this.testProcessRequestData(messageXml);
    }

    @Test // 合庫 TD2521 IVRTRSelfRequestA ABTRVOO01
    public void Hekutest() throws Exception {
        String messageXml = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "\t<SOAP-ENV:Header/>\n" +
                "\t\t<SOAP-ENV:Body>\n" +
                "\t\t\t<esb:MsgRq xmlns:esb=\"http://www.ibm.com.tw/esb\">\n" +
                "\t\t\t\t<Header>\n" +
                "\t\t\t\t\t<CLIENTTRACEID>20240215000000400</CLIENTTRACEID>\n" +
                "\t\t\t\t\t<CHANNEL>VO</CHANNEL>\n" +
                "\t\t\t\t\t<MSGID>T52532</MSGID>\n" +
                "\t\t\t\t\t<MSGKIND>G</MSGKIND>\n" +
                "\t\t\t\t\t<TXNID>00000295</TXNID>\n" +
                "\t\t\t\t\t<BRANCHID></BRANCHID>\n" +
                "\t\t\t\t\t<TERMID></TERMID>\n" +
                "\t\t\t\t\t<CLIENTDT>2024-02-15T15:39:38.000</CLIENTDT>\n" +
                "\t\t\t\t</Header>    \n" +
                "\t\t\t\t<SvcRq>\n" +
                "\t\t\t\t\t<INDATE>20240215</INDATE>\n" +
                "\t\t\t\t\t<INTIME>153938</INTIME>\n" +
                "\t\t\t\t\t<IPADDR>10.0.6.3</IPADDR>\n" +
                "\t\t\t\t\t<TXNTYPE>RQ</TXNTYPE>\n" +
                "\t\t\t\t\t<TERMINALID>VO</TERMINALID>\n" +
                "\t\t\t\t\t<TERMINAL_TYPE>V997</TERMINAL_TYPE>\n" +
                "\t\t\t\t\t<TERMINAL_CHECKNO>V997</TERMINAL_CHECKNO>\n" +
                "\t\t\t\t\t<FSCODE>T5</FSCODE>\n" +
                "\t\t\t\t\t<PCODE>2532</PCODE>\n" +
                "\t\t\t\t\t<TRNSFLAG>2</TRNSFLAG>\n" +
                "\t\t\t\t\t<BUSINESSTYPE>B</BUSINESSTYPE>\n" +
                "\t\t\t\t\t<TRANSAMT>150</TRANSAMT>\n" +
                "\t\t\t\t\t<TRANBRANCH>0560</TRANBRANCH>\n" +
                "\t\t\t\t\t<TRNSFROUTIDNO>D174869911</TRNSFROUTIDNO>\n" +
                "\t\t\t\t\t<TRNSFROUTNAME></TRNSFROUTNAME>\n" +
                "\t\t\t\t\t<TRNSFROUTBANK>0060560</TRNSFROUTBANK>\n" +
                "\t\t\t\t\t<TRNSFROUTACCNT>0560765032011</TRNSFROUTACCNT>\n" +
                "\t\t\t\t\t<TRNSFRINIDNO></TRNSFRINIDNO>                \n" +
                "\t\t\t\t\t<TRNSFRINBANK>008</TRNSFRINBANK>\n" +
                "\t\t\t\t\t<TRNSFRINACCNT>0000100100000021</TRNSFRINACCNT>\n" +
                "\t\t\t\t\t<FEEPAYMENTTYPE>15</FEEPAYMENTTYPE>\n" +
                "\t\t\t\t\t<CUSTPAYFEE>15</CUSTPAYFEE>\n" +
                "\t\t\t\t\t<FISCFEE>10</FISCFEE>\n" +
                "\t\t\t\t\t<CHAFEE_BRANCH>9997</CHAFEE_BRANCH>\n" +
                "\t\t\t\t\t<CHAFEE_AMT>0</CHAFEE_AMT>\n" +
                "\t\t\t\t\t<FAXFEE>0</FAXFEE>\n" +
                "\t\t\t\t\t<TRANSFEE>0</TRANSFEE>\n" +
                "\t\t\t\t\t<OTHERBANKFEE>0</OTHERBANKFEE>\n" +
                "\t\t\t\t\t<CHAFEE_TYPE>U</CHAFEE_TYPE>\n" +
                "\t\t\t\t\t<TRNSFRINNOTE/>\n" +
                "\t\t\t\t\t<TRNSFROUTNOTE/>\n" +
                "\t\t\t\t\t<ORITXSTAN></ORITXSTAN>\n" +
                "\t\t\t\t\t<CUSTCODE></CUSTCODE>\n" +
                "\t\t\t\t\t<SSLTYPE>S</SSLTYPE>\n" +
                "\t\t\t\t\t<LIMITTYPE>52</LIMITTYPE>\n" +
                "\t\t\t\t\t<TRANSTYPEFLAG></TRANSTYPEFLAG>\n" +
                "\t\t\t\t\t<PAYDATA>\n" +
                "\t\t\t\t\t\t<PAYCATEGORY>59999</PAYCATEGORY>\n" +
                "\t\t\t\t\t\t<PAYNO>0009998765123163</PAYNO>\n" +
                "\t\t\t\t\t\t<PAYENDDATE>99991231</PAYENDDATE>\n" +
                "\t\t\t\t\t\t<ORGAN></ORGAN>\n" +
                "\t\t\t\t\t\t<CID>D174869911</CID>\n" +
                "\t\t\t\t\t\t<FILLER>9999</FILLER>\n" +
                "\t\t\t\t\t\t<NPOPID></NPOPID>\n" +
                "\t\t\t\t\t\t<NPPAYTYPE></NPPAYTYPE>\n" +
                "\t\t\t\t\t\t<NPFEENO></NPFEENO>\n" +
                "\t\t\t\t\t\t<NPID></NPID>\n" +
                "\t\t\t\t\t\t<NPPAYNO></NPPAYNO>\n" +
                "\t\t\t\t\t\t<NPPAYENDDATE></NPPAYENDDATE>\n" +
                "\t\t\t\t\t\t<NPBRANCH></NPBRANCH>\n" +
                "\t\t\t\t\t\t<IDENTIFIER></IDENTIFIER>\n" +
                "\t\t\t\t\t</PAYDATA>\n" +
                "\t\t\t\t<TEXTMARK></TEXTMARK>                \n" +
                "\t\t\t</SvcRq>\n" +
                "\t\t</esb:MsgRq>\n" +
                "\t</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>\n";
        this.testProcessRequestData(messageXml);
    }


    private void testProcessRequestData(String messageXml) throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders
                .post("/recv/ivr")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
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
