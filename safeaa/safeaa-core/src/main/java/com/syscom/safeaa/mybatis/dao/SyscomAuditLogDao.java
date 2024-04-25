package com.syscom.safeaa.mybatis.dao;

import com.syscom.safeaa.mybatis.model.Syscomauditlog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SyscomAuditLogDao {
    /**
     *
     * @param userId
     * @param topCount
     * @return
     */
    List<Syscomauditlog> queryChangePasswordLog(int userId, int topCount);

}
