package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Alert;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"integration", "mybatis", "jms", "taipei"})
public class AlertExtMapperTest extends MybatisBaseTest {

    @Autowired
    private AlertExtMapper mapper;

    private Alert record;

    @BeforeEach
    public void setup() {
        record = new Alert();
        record.setArApplication("FEP");
        record.setArDatetime(Calendar.getInstance().getTime());
        record.setArEj("0000");
        record.setArErcode("4001");
        record.setArErdescription("Exception Occur");
        record.setArHostip("127.0.0.1");
        record.setArHostname("centos8.host");
        record.setArLevel("ERROR");
        record.setArLine("888");
        record.setArMessage("Hello, you are wrong");
        record.setArNo(123);
        record.setArSubsys("RM");
        record.setArSys("FEP");
        record.setAtmno("998");
        record.setLogAuditTrail(true);
        record.setRcvtime(Calendar.getInstance().getTime());
        record.setStatus((short) 0);
        record.setUpdateUser(888);
    }

    @Test
    public void testGetDataTableByPrimaryKey() {
        record.setArNo(9);
        List<Alert> list = mapper.getDataTableByPrimaryKey(record);
        assertTrue(CollectionUtils.isNotEmpty(list));
    }

    @Test
    public void testQueryAlert() {
        record.setArNo(9);
        List<HashMap<String, Object>> list = mapper.queryAlert(record, "2022-05-05 17:20:45", "2022-05-19 17:20:45");
        assertTrue(CollectionUtils.isNotEmpty(list));
    }

    @Test
    public void testGetAlertCounts() {
        Alert alert = new Alert();
        alert.setArErcode("NullPointerException");
        alert.setArHostname(null);
        alert.setArDatetime(CalendarUtil.parseDateValue(19991231).getTime());
        UnitTestLogger.info("result = " + mapper.getAlertCounts(alert));
    }

    @Test
    public void testInsert() {
        mapper.insert(record);
    }

    @Test
    public void testInsertSelective() {
        mapper.insertSelective(record);
    }

    @Test
    public void testDelete() {
        mapper.insert(record);
        mapper.deleteByPrimaryKey(record);
    }

    @Test
    public void testSelectByPrimaryKey() {
        mapper.insert(record);
        mapper.selectByPrimaryKey(record.getArNo());
    }

    @Test
    public void testUpdateByPrimaryKey() {
        mapper.insert(record);
        record.setArHostname("centos8.host");
        mapper.updateByPrimaryKey(record);
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        mapper.insert(record);
        record.setArHostname("centos8.host");
        mapper.updateByPrimaryKeySelective(record);
    }
}
