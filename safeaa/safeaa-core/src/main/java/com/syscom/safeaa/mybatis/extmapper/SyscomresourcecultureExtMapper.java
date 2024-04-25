package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomresourcecultureMapper;
import com.syscom.safeaa.mybatis.model.Syscomresourceculture;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomresourcecultureExtMapper extends SyscomresourcecultureMapper {

	public List<Syscomresourceculture> queryAllByResourceId(@Param("resourceid") Integer resourceid);

	public int deleteAllByResourceId(@Param("resourceid") Integer resourceid);
}