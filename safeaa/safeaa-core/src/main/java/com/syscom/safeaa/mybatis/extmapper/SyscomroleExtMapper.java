package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomroleMapper;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomroleInfoVo;

import com.syscom.safeaa.mybatis.vo.SyscomroleResourceVo;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomroleExtMapper extends SyscomroleMapper {
    /**
     * queryRoleIdByNo
     */
	Integer queryRoleIdByNo(@Param("roleno") String roleno);

    /**
     * queryAllRoles
     * @param  roleCulture SyscomroleAndCulture object
     * @return List<SyscomroleAndCulture>
     */
    List<SyscomroleAndCulture> queryAllRoles(SyscomroleAndCulture roleCulture);
    
    List<SyscomroleInfoVo> getSyscomroleInfoVoAll();

    List<SyscomroleResourceVo> queryMenuListByRoles(@Param("roles") String[] roles)throws Exception;
}