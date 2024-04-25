package com.syscom.safeaa.security;

import java.util.List;

import com.syscom.safeaa.mybatis.model.Syscomroledeny;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;


public interface RoleDeny {

	/**
	 * Create one new SyscomRoleDeny record.
	 * 
	 * <p>增加角角色功能權限限制資料(傳入角角色功能權限限制物件)</p>
	 * @param oDefRoleDeny
	 * @return The count of successful insert records,if return -1 Then represent insert fail or parameter passing error. 
	 * @throws Exception
	 */
	int addRoleDeny(final Syscomroledeny oDefRoleDeny) throws Exception;
	
	
	int addRoleDeny(final String roleNo, final String childNo, final String childType) throws Exception;
	
	boolean checkChildId(final int childId, final String childType) throws Exception;
	
	boolean checkChildNo(final String childNo, final String childType) throws Exception;
	
	int removeRoleDeny(final int roleId, final int childId, final String childType) throws Exception;
	
	int removeRoleDenysByRoleId(final int roleId) throws Exception;

	int removeRoleDenysByChildId(final int childId,final String childType ) throws Exception;
	
	List<SyscomrolegroupAndCulture> getSelectedRoleDenyByRoleId(final int roleId,final String culture ) throws Exception;

	List getUNSelectRoleDenyByRoleId(final int roleId,final String culture ) throws Exception;
	
	List<Syscomrolemembers> getDenyGroupsByUserId(final int userId) throws Exception;
}
