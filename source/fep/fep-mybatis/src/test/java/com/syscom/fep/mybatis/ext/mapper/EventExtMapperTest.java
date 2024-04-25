package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EventExtMapperTest extends MybatisBaseTest {
    @Autowired
    private EventExtMapper mapper;

    @Test
    public void testCheckEVENTForAlarmDelete() {
        String alarmNo = "D114";
        List<Event> list = mapper.CheckEVENTForAlarmDelete(alarmNo);
        for (Event event : list) {
            UnitTestLogger.info(event);
        }
    }
}
