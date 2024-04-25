package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomgroupcultureMapper;
import com.syscom.safeaa.mybatis.model.Syscomgroupculture;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomgroupcultureExtMapper extends SyscomgroupcultureMapper {

    int deleteAllByGroupId(@Param("groupid") Integer groupid);

    List<Syscomgroupculture> queryAllByGroupId(@Param("groupid") Integer groupid);
}