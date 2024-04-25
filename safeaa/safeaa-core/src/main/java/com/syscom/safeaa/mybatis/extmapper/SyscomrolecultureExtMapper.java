package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomrolecultureMapper;
import com.syscom.safeaa.mybatis.model.Syscomroleculture;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomrolecultureExtMapper extends SyscomrolecultureMapper {

    int deleteAllByRoleId(@Param("roleid") Integer roleid);

    List<Syscomroleculture> queryAllByRoleId(@Param("roleid") Integer roleid);
}