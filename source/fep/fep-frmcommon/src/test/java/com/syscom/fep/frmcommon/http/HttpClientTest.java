package com.syscom.fep.frmcommon.http;

import com.syscom.fep.frmcommon.FrmcommonBaseTest;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;

public class HttpClientTest extends FrmcommonBaseTest {

    @Test
    public void testGet2() {
        HttpClient httpClient = new HttpClient();
        try {
            String response = null;
            response = httpClient.doGet(
                    "http://centos8.host:9090/api/v1/query?query=node_uname_info{param}",
                    new HashMap<String, Object>() {{
                        put("param", "{instance=\"centos8.host:9100\"}");
                    }});
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPost() {
        HttpClient httpClient = new HttpClient();
        try {
            UnitTestLogger.info(httpClient.doPost("http://localhost:8089/recv", MediaType.APPLICATION_JSON, "{\"data\":\"Richard\"}"));
        } catch (Exception e) {
            UnitTestLogger.error(e, e.getMessage());
        }
    }
}
