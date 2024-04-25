package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomrolegroupMapper;
import com.syscom.safeaa.mybatis.model.Syscomrolegroup;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomrolegroupExtMapper extends SyscomrolegroupMapper {

    List<SyscomrolegroupAndCulture> querySelectedGroupsByRoleId(@Param("roleid") Integer roleid, @Param("culture") String culture );

    List<SyscomrolegroupAndCulture> queryUnSelectedGroupsByRoleId(@Param("roleid") Integer roleid, @Param("culture") String culture );

    List<Syscomrolegroup> queryExecutableGroupsByUserId(@Param("userid") Integer userid);

    int deleteAllByRoleId(@Param("roleid") Integer roleid);
    
    Integer queryGroupIdByNo(@Param("groupno") String groupno);

    int deleteAllByGroupId(@Param("groupid") Integer groupid, @Param("childtype") String childtype);
}