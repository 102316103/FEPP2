package com.syscom.safeaa.security;

import java.util.List;

import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.model.Syscomrolegroup;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;


/**
 * Class of processing relation between role and group.<br/>
 * Designer: David Tai.<br/>
 * Declaration: Copyright 2021 SYSCOM Computer Engineering Corporation. All rights reserved.<br/>
 * 
 * The data process of relation between role and group. (include program role/menu...).
 * 
 * <p>處理角色功能群組權限類別。</p>
 * <p>針對角色功能群組權限物件(包含程式/選單/報表...)的資料處理。</p>
 * @author JenniferYin
 *
 */
public interface RoleGroup {

	/**
	 * Create one new SyscomRoleGroup record.
	 * 
	 * <p>增加角色功能群組權限資料(傳入角色功能群組權限物件)</p>
	 * @param oDefRoleGroup relation object between role and group
	 * @return The count of successful insert records, if return -1 Then represent insert fail or parameter passing error.
	 */
	int addRoleGroup(final Syscomrolegroup oDefRoleGroup) throws Exception;
	
	/**
	 * Create one new SyscomRoleGroup record.
	 * 
	 * <p>增加角色功能群組權限資料</p>
	 * @param roleNo role no
	 * @param groupNo function group no
	 * @param type of child member for role
	 * @return The count of successful insert records, if return -1 Then represent insert fail or parameter passing error.
	 */
	int addRoleGroup(final String roleNo, final String groupNo, final String childType) throws Exception;
	
	
	/**
	 * 
	 * @param childId
	 * @param childType
	 * @return
	 * @throws SafeaaException 
	 */
	boolean checkChildId( Integer childId, final String childType) throws SafeaaException;
	
	boolean checkChildNo( final String childNo, final String childType);
	
	Integer getChildIdByNo(final String childNo, final String childType) throws SafeaaException;
	
	/**
	 * Delete one SyscomRoleGroup record.
	 * 
	 * <p>以角色序號,成員序號及成員類別刪除一筆角色功能群組權限資料</p>
	 * @param roleId role sequence no
	 * @param groupId function group sequence no
	 * @param childType type of child member for role
	 * @return The count of successful insert records, if return -1 Then represent insert fail or parameter passing error.
	 */
	int removeRoleGroup(final int roleId, final int groupId, final String childType) throws Exception;
	
	/**
	 * Delete SyscomRoleGroup records of one role.
	 * 
	 * <p>以角色序號移除一個角色的所有角色功能群組權限資料</p>
	 * 
	 * @param roleId role sequence no
	 * @return The count of successful removed records, if return -1 Then represent insert fail or parameter passing error.
	 */
	int removeRoleGroupsByRoleId(final int roleId) throws Exception;
	
	/**
	 * Delete SyscomRoleGroup records of one group.
	 * 
	 * <p>以成員序號移除一個功能群組的所有角色功能群組權限資料</p>
	 * 
	 * @param groupId group sequence no
	 * @param childType type of child member for role
	 * @return The count of successful removed records, if return -1 Then represent insert fail or parameter passing error.
	 */
	int removeRoleGroupsByGroupId(final int groupId, final String childType) throws Exception;
	
	/**
	 * To get all selected SyscomRoleGroup records by using RoleId and culture code.
	 * If It have no culture code pass then system configuration culture code will be placed.
	 * 
	 * <p>以角色序號查出已加入至某角色的功能群組清單(指定語系)</p>
	 * <p>語系若不傳入會以系統預設語系處理</p>
	 * @param roleId role sequence no
	 * @param culture Culture code
	 * @return All group and resource list.
	 */
	List<SyscomrolegroupAndCulture> getSelectedGroupsByRoleId(final int roleId, final String culture) throws Exception;
	
	/**
	 * To get all unselected SyscomRoleGroup records by using RoleId and culture code.
	 * If It have no culture code pass then system configuration culture code will be placed.
	 * 
	 * <p>以角色序號查出尚未加入至某角色的功能群組清單(指定語系)</p>
	 * <p>語系若不傳入會以系統預設語系處理</p>
	 * @param roleId role sequence no
	 * @param culture Culture code
	 * @return All group and resource list.
	 */
	List<SyscomrolegroupAndCulture> getUNSelectedGroupsByRoleId(final int roleId, final String culture) throws Exception;
	
	/**
	 * To get all executable groups and resources by using UserId.
	 * 
	 * <p以使用者序號查出使用者能執行的所有功能群組清單(包含資源)</p>
	 * <p>R:資源 G:功能群組</p>
	 * @param userId user sequence no
	 * @return All group and resource list.
	 */
	List<Syscomrolegroup> getExecutableGroupsByUserId(final int userId) throws Exception;
}

	
	
