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
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.util.StanGenerator;
import com.syscom.fep.server.ServerBaseTest;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.communication.ToFEPFISCCommu;

//@ActiveProfiles({"mybatis", "jms-localhost", "integration", "taipei"})
public class IMSControllerTest extends ServerBaseTest {
    @Autowired
    private StanGenerator generator;
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() throws Exception {
        SpringBeanFactoryUtil.registerController(IMSController.class);
    }

    @AfterAll
    public void tearDown() throws Exception {
        SpringBeanFactoryUtil.unregisterBean(IMSController.class);
    }

    /**
     * 
     *
     * @throws Exception
     */
    @Test
    public void testCommonCBSRequestI() throws Exception {
    	//2521
		String message = 
				"00000030323030323532313030313039343330303830303030303036303030303132303931393137353532393030303050572F1D24AC2010000032012B3030303030303030383030303045414930303030303030303134303633544553542020202030303830303030303036303030303230323330393139313735353239413531383030303031303031303030303030323130353630373635303332303131202020000000000000000000000000000000                              FFFFFFFF";
        this.testProcessRequestData(message);
    }
    
    @Test
    public void testCommonCBSConfirmI() throws Exception {
    	//2521
        String message =
                "00000030323032323532313030313039343330303830303030303036303030303132303931393137353532393430303150572F1D24000000000000012B303030303030303038303030304541493030303030FFFFFFFF";
        this.testProcessRequestData(message);
    }
    
    @Test
    public void testCommonCBSRequestI_2562() throws Exception {
    	//2562
		String message = 
				"00000030323030323536323030313231383630313630303030303036303030303131303632393135323531303030303050572F1D24EC2010010113012B30303030303030313030303030543939393853303230303030343933383138383838383838393939393030303031373639303036303030303031363030303032303232303632393135323531303630313135393939393939393930353430353230303030303137313039393939393132333120202020202020202020202030303030303933323130303033313038930001020000000000000000000000                              0104200A43323145453745453034313043353642323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230323032303230FFFFFFFF                              FFFFFFFF";
        this.testProcessRequestData(message);
    }
    
    @Test
    public void testCommonCBSConfirmI_2562() throws Exception {
    	//2562
        String message = 
        		"00000030323032323536323030313231383630313630303030303036303030303131303632393135323531303430303150572F1D24000000000000012B303030303030303130303030305439393938533032FFFFFFFF";
        this.testProcessRequestData(message);
    }

    private String testProcessRequestData(String message) throws Exception {
        ToFEPFISCCommu request = new ToFEPFISCCommu();
        request.setEj(TxHelper.generateEj());
        request.setMessage(message);
        request.setStan(this.generateStan());
        request.setStep(0);
        request.setSync(true); // 2021-06-09 Richard add for UT only
        String messageIn = XmlUtil.toXML(request);
        return this.testProcessRequestDataWithXML(messageIn);
    }

    private String testProcessRequestDataWithXML(String messageIn) throws Exception {
        RequestBuilder builder = MockMvcRequestBuilders
                .post("/recv/ims")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .content(messageIn);

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
        return resultStr;
    }

    private final String generateStan() {
        return generator.generate();
    }
}
