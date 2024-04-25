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

public class Send2160ControllerTest extends ServerBaseTest {
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() throws Exception {
        SpringBeanFactoryUtil.registerController(Send2160Controller.class);
    }

    @AfterAll
    public void tearDown() throws Exception {
        SpringBeanFactoryUtil.unregisterBean(Send2160Controller.class);
    }

    @Test
    public void testVAA2566ForVAIssueRequestA() throws Exception {
        String messageXml = "21602023111516354";
        this.testProcessRequestDataXml(messageXml);
    }

    @Test
    public void testEFT226X_2261() throws Exception {
        String messageXml =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                        "<FEP><RqHeader><MsgID>EFT226X</MsgID><MsgType>G</MsgType><ChlName>NETBANK</ChlName><ChlEJNo>0ea8cbb6a4314f308705903c17245e37</ChlEJNo><ChlSendTime>2017-12-06T16:45:52</ChlSendTime><TxnID>FEPPayBill</TxnID><BranchID>TW8070769</BranchID><TermID>71</TermID></RqHeader><SvcRq><Rq><BKNO>461</BKNO><TXACT>2222222222222222</TXACT><BKNO_D>807</BKNO_D><ACT_D>0006682962555009</ACT_D><TXAMT>8</TXAMT><IDNO>B231053008</IDNO><PAYCNO>4696560100006604</PAYCNO><VPID>10003400</VPID><CLASS>00035</CLASS><PAYID>1102</PAYID><PTYPE>3</PTYPE><DUEDATE>0</DUEDATE><MODE>1</MODE><PSBMEMO_D>信用卡費</PSBMEMO_D><PSBMEMO_C>信用卡費</PSBMEMO_C><PSBREM_S_D>560100006604</PSBREM_S_D><PSBREM_S_C>9656010000660</PSBREM_S_C><PSBREM_F_D>4696560100006604</PSBREM_F_D><PSBREM_F_C>4696560100006604</PSBREM_F_C></Rq></SvcRq></FEP>";
        this.testProcessRequestDataXml(messageXml);
    }

    @Test
    public void testEFT226X_2262() throws Exception {
        String messageXml =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                        "<FEP><RqHeader><MsgID>EFT226X</MsgID><MsgType>G</MsgType><ChlName>NETBANK</ChlName><ChlEJNo>0ea8cbb6a4314f308705903c17245e37</ChlEJNo><ChlSendTime>2017-12-06T16:45:52</ChlSendTime><TxnID>FEPPayBill</TxnID><BranchID>TW8070769</BranchID><TermID>71</TermID></RqHeader><SvcRq><Rq><BKNO>461</BKNO><TXACT>2222222222222222</TXACT><BKNO_D>807</BKNO_D><ACT_D>0006682962555009</ACT_D><TXAMT>8</TXAMT><IDNO>B231053008</IDNO><PAYCNO>4696560100006604</PAYCNO><VPID>10003400</VPID><CLASS>00035</CLASS><PAYID>1102</PAYID><PTYPE>3</PTYPE><DUEDATE>0</DUEDATE><MODE>1</MODE><PSBMEMO_D>信用卡費</PSBMEMO_D><PSBMEMO_C>信用卡費</PSBMEMO_C><PSBREM_S_D>560100006604</PSBREM_S_D><PSBREM_S_C>9656010000660</PSBREM_S_C><PSBREM_F_D>4696560100006604</PSBREM_F_D><PSBREM_F_C>4696560100006604</PSBREM_F_C></Rq></SvcRq></FEP>";
        this.testProcessRequestDataXml(messageXml);
    }

    @Test
    public void testEFT226X_2264() throws Exception {
        String messageXml =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                        "<FEP><RqHeader><MsgID>EFT226X</MsgID><MsgType>G</MsgType><ChlName>NETBANK</ChlName><ChlEJNo>6d6845cdc00147bb9175f59cd1873d4b</ChlEJNo><ChlSendTime>2018-01-11T11:59:06</ChlSendTime><TxnID>FEPPayBill</TxnID><BranchID>TW8070769</BranchID><TermID>71</TermID></RqHeader><SvcRq><Rq><BKNO>461</BKNO><TXACT>1234567890123456</TXACT><BKNO_D /><ACT_D>8421017147088964</ACT_D><TXAMT>750</TXAMT><IDNO>A123456789</IDNO><PAYCNO>8421017147088964</PAYCNO><VPID>10000002</VPID><CLASS>40005</CLASS><PAYID>8072</PAYID><PTYPE>4</PTYPE><DUEDATE>20181231</DUEDATE><UNIT /><IDENTITY>211176</IDENTITY><MENO>827220181231211176</MENO><MODE>1</MODE><PSBMEMO_D /><PSBMEMO_C /><PSBREM_S_D /><PSBREM_S_C /><PSBREM_F_D /><PSBREM_F_C /></Rq></SvcRq></FEP>";
        this.testProcessRequestDataXml(messageXml);
    }

    // 2021/11/11台北新增測試電文 START ADD BY WJ
    /**
     * 晶片卡自行繳稅
     * SelfIssueRequestA
     *
     * @throws Exception
     */
    @Test
    public void testIPY2532() throws Exception {
        String messageXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
                + "<FEP>\r\n"
                + "  <RqHeader>\r\n"
                + "    <MsgID>IPY2532</MsgID>\r\n"
                + "    <MsgType>G</MsgType>\r\n"
                + "    <ChlName>NETBANK</ChlName>\r\n"
                + "    <ChlEJNo>MMANTFXPK-TAX-8420589651166122290</ChlEJNo>\r\n"
                + "    <ChlSendTime>2021-07-02T11:24:32</ChlSendTime>\r\n"
                + "    <TxnID>FEPIPY</TxnID>\r\n"
                + "    <BranchID>TW8070769</BranchID>\r\n"
                + "    <TermID>71</TermID>\r\n"
                + "  </RqHeader>\r\n"
                + "  <SvcRq>\r\n"
                + "    <Rq><BKNO>807</BKNO><TXACT>14703100628267</TXACT><TXAMT>750</TXAMT><CLASS>15001</CLASS><PAYCNO>0000000000000000</PAYCNO><DUEDATE>1091231</DUEDATE><UNIT>028</UNIT><IDNO>10123456789</IDNO><MODE>1</MODE><PSBMEMO_D>網銀繳稅</PSBMEMO_D><PSBMEMO_C>網銀繳稅</PSBMEMO_C><PSBREM_S_D>綜所稅</PSBREM_S_D><PSBREM_S_C></PSBREM_S_C><PSBREM_F_D>綜所稅</PSBREM_F_D><PSBREM_F_C></PSBREM_F_C><IPADDR>10.1.108.169</IPADDR></Rq>\r\n"
                + "  </SvcRq>\r\n"
                + "</FEP>";
        this.testProcessRequestDataXml(messageXml);
    }

    /**
     * 晶片卡轉帳
     * SelfIssueRequestA
     *
     * @throws Exception
     */
    @Test
    public void testNETBANKIFT2521() throws Exception {
        String messageXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
                + "<FEP>\r\n"
                + "  <RqHeader>\r\n"
                + "    <MsgID>IFT2521</MsgID>\r\n"
                + "    <MsgType>G</MsgType>\r\n"
                + "    <ChlName>NETBANK</ChlName>\r\n"
                + "    <ChlEJNo>MMA-PK-8395036453139219575</ChlEJNo>\r\n"
                + "    <ChlSendTime>2020-10-07T17:17:54</ChlSendTime>\r\n"
                + "    <TxnID>FEPFT</TxnID>\r\n"
                + "    <BranchID>TW8070769</BranchID>\r\n"
                + "    <TermID>71</TermID>\r\n"
                + "  </RqHeader>\r\n"
                + "  <SvcRq>\r\n"
                + "    <Rq><BKNO>807</BKNO><TXACT>17300400606434</TXACT><BKNO_D>012</BKNO_D><ACT_D>0000686168321022</ACT_D><TXAMT>2000</TXAMT><MODE>1</MODE><REG_TFR_TYPE></REG_TFR_TYPE><PSBMEMO_D>網銀轉帳</PSBMEMO_D><PSBMEMO_C>網銀轉帳</PSBMEMO_C><PSBREM_S_D>012168321022</PSBREM_S_D><PSBREM_S_C>100300000551</PSBREM_S_C><PSBREM_F_D>0120000686168321022~</PSBREM_F_D><PSBREM_F_C>00100300000551</PSBREM_F_C><IPADDR>10.11.166.159</IPADDR><CHREM></CHREM></Rq>\r\n"
                + "  </SvcRq>\r\n"
                + "</FEP>";
        this.testProcessRequestDataXml(messageXml);
    }

    /**
     * 晶片卡轉帳
     * SelfIssueRequestA
     *
     * @throws Exception
     */
    @Test
    public void testMOBILBANKIFT2521() throws Exception {
        String messageXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
                + "<FEP>\r\n"
                + "  <RqHeader>\r\n"
                + "    <MsgID>IFT2521</MsgID>\r\n"
                + "    <MsgType>G</MsgType>\r\n"
                + "    <ChlName>MOBILBANK</ChlName>\r\n"
                + "    <ChlEJNo>MMA-PKSMS8404774374245726384</ChlEJNo>\r\n"
                + "    <ChlSendTime>2020-12-31T10:17:12</ChlSendTime>\r\n"
                + "    <TxnID>FEPFT</TxnID>\r\n"
                + "    <BranchID>TW8070769</BranchID>\r\n"
                + "    <TermID>71</TermID>\r\n"
                + "  </RqHeader>\r\n"
                + "  <SvcRq>\r\n"
                + "    <Rq><BKNO>807</BKNO><TXACT>12100400728197</TXACT><BKNO_D>461</BKNO_D><ACT_D>0958123456</ACT_D><TXAMT>100</TXAMT><MODE>1</MODE><REG_TFR_TYPE></REG_TFR_TYPE><PSBMEMO_D>門號轉帳</PSBMEMO_D><PSBMEMO_C>門號轉帳</PSBMEMO_C><PSBREM_S_D>0958123456</PSBREM_S_D><PSBREM_S_C>12100400728197</PSBREM_S_C><PSBREM_F_D>4610958123456~郭○銘</PSBREM_F_D><PSBREM_F_C>12100400728197</PSBREM_F_C><IPADDR>10.11.73.115</IPADDR><CHREM>Ｔｅｓｔ</CHREM><MTP>Y</MTP></Rq>\r\n"
                + "  </SvcRq>\r\n"
                + "</FEP>";
        this.testProcessRequestDataXml(messageXml);
    }

    /**
     * 晶片卡轉帳
     * SelfIssueRequestA
     *
     * @throws Exception
     */
    @Test
    public void testMMAB2CIFT2521() throws Exception {
        String messageXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
                + "<FEP>\r\n"
                + "  <RqHeader>\r\n"
                + "    <MsgID>IFT2521</MsgID>\r\n"
                + "    <MsgType>G</MsgType>\r\n"
                + "    <ChlName>MMAB2C</ChlName>\r\n"
                + "    <ChlEJNo>M-PK-8398556448198961160</ChlEJNo>\r\n"
                + "    <ChlSendTime>2020-10-20T11:04:29</ChlSendTime>\r\n"
                + "    <TxnID>FEPFT</TxnID>\r\n"
                + "    <BranchID>TW8070769</BranchID>\r\n"
                + "    <TermID>71</TermID>\r\n"
                + "  </RqHeader>\r\n"
                + "  <SvcRq>\r\n"
                + "    <Rq><BKNO>807</BKNO><TXACT>15800400207601</TXACT><BKNO_D>009</BKNO_D><ACT_D>0050500100156000</ACT_D><TXAMT>1080</TXAMT><MODE>1</MODE><REG_TFR_TYPE></REG_TFR_TYPE><PSBMEMO_D>網銀轉帳</PSBMEMO_D><PSBMEMO_C>網銀轉帳</PSBMEMO_C><PSBREM_S_D>測試</PSBREM_S_D><PSBREM_S_C>800400207601</PSBREM_S_C><PSBREM_F_D>0090050500100156000~測試</PSBREM_F_D><PSBREM_F_C>15800400207601</PSBREM_F_C><IPADDR>10.11.75.40</IPADDR><CHREM>測試</CHREM></Rq>\r\n"
                + "  </SvcRq>\r\n"
                + "</FEP>";
        this.testProcessRequestDataXml(messageXml);
    }

    // 2021/11/11台北新增測試電文 END ADD BY WJ
    private void testProcessRequestDataXml(String messageXml) throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders
                .post("/recv/send2160")
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
