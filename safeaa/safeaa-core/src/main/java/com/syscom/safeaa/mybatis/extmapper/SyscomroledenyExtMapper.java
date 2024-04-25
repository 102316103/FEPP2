package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomroledenyMapper;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;

import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomroledenyExtMapper extends SyscomroledenyMapper {

    List<Syscomrolemembers> queryDenyGroupsByUserId(@Param ("userid") Integer userid);

    List<SyscomrolegroupAndCulture> querySelectedRoleDenyByRoleId(@Param("roleid") Integer roleid, @Param("childtype") String childtype, @Param("culture") String culture);

    List<SyscomrolegroupAndCulture> queryUNSelectRoleDenyByRoleId(@Param("roleid") Integer roleid, @Param("childtype") String childtype, @Param("culture") String culture);

}