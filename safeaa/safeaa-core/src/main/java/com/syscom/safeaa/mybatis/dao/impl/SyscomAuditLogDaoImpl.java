package com.syscom.safeaa.mybatis.dao.impl;

import com.syscom.safeaa.enums.EnumAuditType;
import com.syscom.safeaa.mybatis.dao.SyscomAuditLogDao;
import com.syscom.safeaa.mybatis.extmapper.SyscomauditlogExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomauditlog;
import com.syscom.safeaa.utils.DbHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SyscomAuditLogDaoImpl implements SyscomAuditLogDao {

    @Autowired
    private SyscomauditlogExtMapper syscomauditlogExtMapper;

    /**
     * To get audit log of change password event
     * @param userId
     * @param topCount
     * @return
     */
    @Override
    public List<Syscomauditlog> queryChangePasswordLog(int userId, int topCount) {
        try{
            int auditType = EnumAuditType.ChangePassword.getValue();
            return syscomauditlogExtMapper.queryAllDataBy(userId, auditType, DbHelper.toShort(true), topCount, "UpdateTime DESC");
        }catch (Exception e){
            throw e;
        }
    }
}
