package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomrolemembersMapper;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomrolemembersExtMapper extends SyscomrolemembersMapper {

    int deleteAllByRoleId(@Param("roleid") Integer roleid);

    List<SyscomrolemembersAndCulture> querySelectedByRoleId(@Param("roleid") Integer roleid, @Param("culture") String culture);

    List<SyscomrolemembersAndCulture> queryUNSelectMembers(@Param("roleid") Integer roleid, @Param("culture") String culture);

    List<SyscomrolemembersAndCulture> queryParentRolesByUserId(@Param("userid") Integer userid, @Param("culture") String culture);

    List<SyscomrolemembersAndCulture> queryRoleUsersByRoleId(@Param("roleid") Integer roleid);
}