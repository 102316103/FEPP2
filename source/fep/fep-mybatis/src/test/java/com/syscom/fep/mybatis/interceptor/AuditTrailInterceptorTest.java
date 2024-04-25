package com.syscom.fep.mybatis.interceptor;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.ext.mapper.AlarmExtMapper;
import com.syscom.fep.mybatis.ext.mapper.AtmfeeExtMapper;
import com.syscom.fep.mybatis.model.Alarm;
import com.syscom.fep.mybatis.model.Atmfee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Calendar;

@ActiveProfiles({"integration", "mybatis", "taipei"})
@Rollback(false)
public class AuditTrailInterceptorTest extends MybatisBaseTest {
    @Autowired
    private AtmfeeExtMapper atmfeeMapper;
    @Autowired
    private AlarmExtMapper alarmMapper;
    private Atmfee atmfee;
    private Alarm alarm;

    @BeforeEach
    public void setup() {
        atmfee = new Atmfee();
        atmfee.setAtmfeeTxMm("123456");
        atmfee.setAtmfeeSeqNo("0");
        atmfee.setAtmfeeName("AtmfeeName");
        atmfee.setAtmfeeCur("CNY");
        atmfee.setAtmfeeFee(new BigDecimal("99.99"));
        atmfee.setAtmfeeFiscFlag("0");
        atmfee.setAtmfeePcode("2510");
        atmfee.setUpdateUserid(1);
        atmfee.setUpdateTime(Calendar.getInstance().getTime());
        alarm = new Alarm();
        alarm.setAlarmNo("0000");
        alarm.setAlarmName("AlarmName");
        alarm.setAlarmNameS("AlarmNameS");
        alarm.setAlarmIcon("AlarmIcon");
        alarm.setAlarmSendems((short) 0);
        alarm.setAlarmLog((short) 1);
        alarm.setAlarmAutostop((short) 2);
        alarm.setAlarmConsole((short) 3);
        alarm.setAlarmNotifyEmail("AlarmNotifyEmail");
        alarm.setAlarmNotifyTimes((short) 999);
        alarm.setAlarmRemark("AlarmRemark");
        alarm.setUpdateUserid(1);
        alarm.setUpdateTime(Calendar.getInstance().getTime());
    }

    @Test
    public void testAtmfee() {
        atmfeeMapper.deleteByPrimaryKey(atmfee);
        atmfeeMapper.insert(atmfee);
        atmfee.setAtmfeeName("AtmfeeName1");
        atmfee.setAtmfeeCur("NYD");
        atmfee.setAtmfeeFee(new BigDecimal("100.00"));
        atmfee.setAtmfeeFiscFlag("1");
        atmfee.setAtmfeePcode("2511");
        atmfee.setUpdateTime(Calendar.getInstance().getTime());
        atmfee.setLogAuditTrail(true);
        atmfee.setUpdateUser(1);
        atmfeeMapper.updateByPrimaryKey(atmfee);
        atmfeeMapper.deleteByPrimaryKey(atmfee);
    }

    @Test
    public void testAlarm() {
        alarmMapper.deleteByPrimaryKey(alarm);
        alarmMapper.insert(alarm);
        alarm.setAlarmName("AlarmNam1");
        alarm.setAlarmNameS("AlarmNamS1");
        alarm.setAlarmIcon("AlarmIco1");
        alarm.setAlarmSendems((short) 10);
        alarm.setAlarmLog((short) 11);
        alarm.setAlarmAutostop((short) 12);
        alarm.setAlarmConsole((short) 13);
        alarm.setAlarmNotifyEmail("AlarmNotifyEmail1");
        alarm.setAlarmNotifyTimes((short) 100);
        alarm.setAlarmRemark("AlarmRemark1");
        alarm.setUpdateUserid(1);
        alarm.setUpdateTime(Calendar.getInstance().getTime());
        alarm.setLogAuditTrail(true);
        alarm.setUpdateUser(1);
        alarmMapper.updateByPrimaryKey(alarm);
        alarmMapper.deleteByPrimaryKey(alarm);
    }

    @Test
    public void testAlarm2() {
        alarm.setLogAuditTrail(true);
        alarm.setUpdateUser(1);
        alarmMapper.deleteByPrimaryKey(alarm);
        alarmMapper.insert(alarm);
        alarmMapper.selectByPrimaryKey(alarm.getAlarmNo());
        alarmMapper.deleteByPrimaryKey(alarm);
    }
}
