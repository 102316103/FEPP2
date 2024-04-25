package com.syscom.safeaa.mybatis.extmapper;

import com.syscom.safeaa.mybatis.mapper.SyscomgroupmembersMapper;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;
import java.util.List;

@Resource
public interface SyscomgroupmembersExtMapper extends SyscomgroupmembersMapper {
    /**
     *  <p>刪除一筆功能群組的所有相關的成員資料</p>
     * @param groupid 功能群組序號
     * @return
     */
    int deleteAllByGroupId(@Param("groupid") Integer groupid);
    
    /**
     *  <p>刪除一筆功能群組的所有相關的成員資料</p>
     * @param resourceid 資源代號
     * @return
     */
    int deleteAllByResourceId(@Param("resourceid") Integer resourceid);

    /**
     *  <p>查出尚未加入至某功能群組的成員清單(含語系資料)</p>
     *  <li> remark:  須判斷有效日期
     * @param groupid 功能群組序號
     * @param culture  語系
     * @return 成員清單
     */
    List<SyscomgroupmembersAndGroupLevel> queryUNSelectMembers(@Param("groupid") Integer groupid, @Param("culture") String culture);


    List<SyscomgroupmembersAndGroupLevel> queryNestedMembersByGroupId(@Param("groupid") Integer groupid, @Param("culture") String culture);

    /**
     *  <p> 以功能群組序號(GroupId)查出直接關連的成員資料(含語系資料)</p
     *  <li> remark:須判斷有效日期
     * @param groupid 功能群組序號
     * @param culture 語系
     * @return 直接關連的成員資料
     * @throws 異常時丟出此Data Access Exception
     */
    List<SyscomgroupmembersAndGroupLevel> querySelectedByGroupId(@Param("groupid") Integer groupid, @Param("culture") String culture);

    Integer getMaxLocationno(@Param("groupid") Integer groupid);

}