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

public class ATMMonControllerTest extends ServerBaseTest {
    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public void setup() throws Exception {
        SpringBeanFactoryUtil.registerController(ATMMonController.class);
    }

    @AfterAll
    public void tearDown() throws Exception {
        SpringBeanFactoryUtil.unregisterBean(ATMMonController.class);
    }

    @Test
    public void testATMMon() throws Exception {
        String messageIn = "{" +
            "\"MsgRq\": [" +
              "{" +
                "\"ATMNo\": \"00123\"," +
                "\"ConnectStatus\": 1," +
                "\"ServiceStatus\": 1," +
                "\"Enable\": 1" +
                "}," +
                "{" +
                    "\"ATMNo\": \"00125\"," +
                    "\"ConnectStatus\": 0," +
                    "\"ServiceStatus\": 0," +
                    "\"Enable\": 0" +
                    "}" +
                    "]" +
                    "}";
        this.testProcessRequestData(messageIn);
    }

    

    private String testProcessRequestData(String message) throws Exception {
        
        RequestBuilder builder = MockMvcRequestBuilders
                .post("/fep/ATMStatusChange")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(message);

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
}
