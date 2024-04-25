package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomuserstatusMapper;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUsers;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomuserstatusExtMapper extends SyscomuserstatusMapper {
    /**
     * QUERYALLUSERS
     * @param UserId
     */
    List<SyscomQueryAllUsers> syscomQueryAllUsers(@Param("UserId") Integer UserId,
                                                  @Param("UserName") String UserName,
                                                  @Param("LogOnId") String LogOnId,
                                                  @Param("userMail") String userMail);
}