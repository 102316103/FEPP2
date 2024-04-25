package com.syscom.fep.mybatis.ext.mapper;

import com.google.gson.Gson;
import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Sms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SmsExtMapperTest extends MybatisBaseTest {
    @Autowired
    private SmsExtMapper mapper;
    private Sms sms;

    @BeforeEach
    public void setUp() {
        Map<String, String> others = new HashMap<>();
        others.put("name", "Richard");
        others.put("employee no", "B00161");
        sms = new Sms();
        sms.setLogAuditTrail(true);
        sms.setSmsCpu(10);
        sms.setSmsCpuThreshold(20);
        sms.setSmsHostname("fepap1");
        sms.setSmsOthers(new Gson().toJson(others));
        sms.setSmsPid(30);
        sms.setSmsRam(40);
        sms.setSmsRamThreshold(50);
        sms.setSmsServiceip("192.168.30.29");
        sms.setSmsServicename("LA-RICHARD-WIN11");
        sms.setSmsServicestate("0");
        sms.setSmsStarttime(Calendar.getInstance().getTime());
        sms.setSmsStoptime(Calendar.getInstance().getTime());
        sms.setSmsThreads(60);
        sms.setSmsThreadsActive(70);
        sms.setSmsThreadsThreshold(80);
        sms.setSmsUpdatetime(Calendar.getInstance().getTime());
        sms.setUpdateUser(0L);
    }

    @Test
    public void insert() {
        mapper.insert(sms);
    }

    @Test
    public void select() {
        mapper.selectByPrimaryKey(sms.getSmsServicename(), sms.getSmsServiceip());
    }

    @Test
    public void update() {
        mapper.updateByPrimaryKey(sms);
    }

    @Test
    public void delete() {
        mapper.deleteByPrimaryKey(sms);
    }

    @Test
    public void selectAll(){
        mapper.queryFEPMonitorService();
    }
}
