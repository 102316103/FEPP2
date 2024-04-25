package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Alarm;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlarmExtMapperTest extends MybatisBaseTest {
    @Autowired
    private AlarmExtMapper mapper;

    @Test
    public void testGetAlarmByPKLike() {
        String alarmNo = "D' OR '1%' = '1";
        List<Alarm> list = mapper.getAlarmByPKLike(alarmNo);
        assertTrue(CollectionUtils.isEmpty(list));
        alarmNo = "D'; DELETE FROM BATCH_LOCKS;";
        list = mapper.getAlarmByPKLike(alarmNo);
        assertTrue(CollectionUtils.isEmpty(list));
        alarmNo = "D";
        list = mapper.getAlarmByPKLike(alarmNo);
        for (Alarm alarm : list) {
            UnitTestLogger.info(alarm);
        }
    }
}
