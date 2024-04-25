package com.syscom.fep.mybatis.ext.mapper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Batch;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BatchExtMapperTest extends MybatisBaseTest {
    @Autowired
    private BatchExtMapper mapper;

    private Batch record;

    @BeforeEach
    public void setup() {
        record = new Batch();
        record.setBatchAction("");
        record.setBatchCheckbusinessdate((short) 0);
        record.setBatchCurrentid("");
        record.setBatchDescription("UT Test");
        record.setBatchEditgroup("");
        record.setBatchEnable((short) 1);
        record.setBatchLastruntime(Calendar.getInstance().getTime());
        record.setBatchName("UT Test");
        record.setBatchNextruntime(Calendar.getInstance().getTime());
        record.setBatchNotifymail("myfifa2005@qq.com");
        record.setBatchNotifytype((short) 1);
        record.setBatchResult("1");
        record.setBatchSchedule((short) 1);
//		record.setBatchScheduleDayinterval((short) 1);
        record.setBatchScheduleMonthdays("1,2,3,4,5,6,7,8,9");
        record.setBatchScheduleMonths("FEB");
        record.setUpdateUser(123);
    }

    @Test
    public void testInsertSelective() {
        mapper.insertSelective(record);
    }

    @Test
    public void testGetSingleBATCHByDef() {
        mapper.getSingleBATCHByDef("UT Test");
    }

    @Test
    public void testQueryBatchByName() {
        mapper.queryBatchByName("UT Test");
    }

    @Test
    public void testGetAllBatch() {
        mapper.getAllBatch("UT Test", Arrays.asList("1", "2"));
    }

    @Test
    public void testGetBatchFirstTaskById() {
        mapper.getBatchFirstTaskById(3);
    }

    @Test
    public void testGetBatchContext() {
        List<Map<String, Object>> result = mapper.getBatchContext(3, 1, 123, 6);
        assertTrue(CollectionUtils.isNotEmpty(result));
    }

    @Test
    public void testQueryScheduledBatchByNameAndSubsys() {
        List<Batch> result = mapper.queryScheduledBatchByNameAndSubsys("UT Test", Arrays.asList("1", "2"));
        assertTrue(CollectionUtils.isNotEmpty(result));
    }
}
