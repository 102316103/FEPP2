package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.SmsMapper;
import com.syscom.fep.mybatis.model.Sms;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Resource
public interface SmsExtMapper extends SmsMapper {
    List<Sms> queryAll();

    int updateFEPMonitorServiceWhenTerminate(@Param("smsServicename") String smsServicename, @Param("smsServiceip") String smsServiceip, @Param("smsServicestate") String smsServicestate, @Param("smsStoptime") Date smsStoptime);

    List<Sms> queryFEPMonitorService();

    List<Sms> queryExcludeServiceName(@Param("smsServicenames") List<String> serviceNameList);

    int deleteAll();

    List<Sms> selectByServiceName(@Param("smsServicename") String smsServicename);
}
