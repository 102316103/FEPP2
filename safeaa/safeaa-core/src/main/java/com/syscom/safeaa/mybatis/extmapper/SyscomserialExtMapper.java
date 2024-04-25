package com.syscom.safeaa.mybatis.extmapper;

import javax.annotation.Resource;

import com.syscom.safeaa.mybatis.mapper.SyscomserialMapper;

@Resource
public interface SyscomserialExtMapper extends SyscomserialMapper {

	public int resetIdBySerialName(String SerialName);

}