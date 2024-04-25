package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomroleresourceMapper;
import com.syscom.safeaa.mybatis.vo.SyscomSelectResourcesVo;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomroleresourceExtMapper extends SyscomroleresourceMapper {

    int deleteAllByRoleId(@Param("roleid") Integer roleid);

    int deleteAllByResourceId(@Param("resourceid") Integer resourceid);

    List<SyscomSelectResourcesVo> querySelectedResourcesByRoleId(@Param("roleid") Integer roleid, @Param("culture") String culture);

    List<SyscomSelectResourcesVo> queryUNSelectResourcesByRoleId(@Param("roleid") Integer roleid,@Param("culture") String culture);
}