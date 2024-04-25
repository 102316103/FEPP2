package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomuserdeputyMapper;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomQueryAllUserDeputyVo;
import com.syscom.safeaa.mybatis.vo.SyscomRoleMembersVo;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomuserdeputyExtMapper extends SyscomuserdeputyMapper {

    int deleteAllByUserId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);

    List<SyscomQueryAllUserDeputyVo> queryAllUserDeputy(@Param("userId") Integer userId, @Param("roleId") Integer roleId, @Param("orderBy") String orderBy);

    List<SyscomRoleMembersVo> queryParentRolesByUserId(@Param("userId") Integer userId, @Param("culture") String culture);

    List<Syscomuser> queryUNSelectUserDeputyByUserId(@Param("userId") int userId, @Param("roleId") int roleId);
}