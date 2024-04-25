package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomauditlogMapper;
import com.syscom.safeaa.mybatis.model.Syscomauditlog;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomauditlogExtMapper extends SyscomauditlogMapper {
    /**
     * To get audit log of change password event
     * 以使用者序號查出變更密碼稽核紀錄
     * @return
     */
    List<Syscomauditlog> queryAllDataBy(@Param("userId") int userId,
                                        @Param("auditType") int auditType,
                                        @Param("result") short result,
                                        @Param("topCount") int topCount,
                                        @Param("orderBy") String orderBy);

}