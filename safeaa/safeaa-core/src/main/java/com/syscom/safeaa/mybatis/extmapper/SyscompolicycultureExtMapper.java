package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscompolicycultureMapper;
import com.syscom.safeaa.mybatis.model.Syscompolicyculture;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscompolicycultureExtMapper extends SyscompolicycultureMapper {

    int deleteAllByPolicyId(@Param("policyId") Integer policyId);

    List<Syscompolicyculture> queryAllByPolicyId(@Param("policyId") Integer policyId);

}