package com.syscom.safeaa.mybatis.extmapper;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.safeaa.mybatis.mapper.SyscomresourceMapper;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;

@Resource
public interface SyscomresourceExtMapper extends SyscomresourceMapper {

	public Integer queryResourceIdByNo(@Param("resourceno") String resourceno);

	public List<SyscomresourceAndCulture> queryAllResources(SyscomresourceAndCulture sc);
}