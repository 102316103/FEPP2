package com.syscom.safeaa.security;

import java.sql.Date;
import java.util.List;
import com.syscom.safeaa.mybatis.model.Syscomrole;
import com.syscom.safeaa.mybatis.model.Syscomroleculture;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomroleInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomroleResourceVo;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;

public interface Role {

	public int createRole(Syscomrole oDefRole) throws Exception;
	
	public int updateRole(Syscomrole oDefRole) throws Exception;
	
	public boolean deleteRole(int roleId) throws Exception;
	
	/**
	 * To get RoleId of role by using RoleNo.
	 * 
 	 *  <p>以角色代碼(RoleNo)查出該筆角色的角色序號</p>
     * 
	 * @param roleNo
	 * @return RoleId : -1 meaning the roleNo is not exist in SyscomRole.
	 * @throws Exception
	 */
	public Integer getRoleIdByNo(String roleNo) throws Exception;
	
	/**
	 * To get one role record by using RoleId.
	 * 
	 *  <p>以角色序號(RoleId)查出一筆角色資料</p>
	 *  
	 * @param role
	 * @return if return null Then represent query fail or parameter passing error.
	 * @throws Exception
	 */
	public Syscomrole getRoleById(Syscomrole role) throws Exception;
	
	/**
	 * To get one role record and relative culture data by using RoleId.
	 * <p> 以角色序號(RoleId)查出一筆角色資料(含語系資料)
   	  * 若角色語系物件中語系欄位未傳入則以系統預設語系查詢
   	  * </p>
	 * @param role
	 * @param culture
	 * @return
	 * @throws Exception
	 */
	
	public SyscomroleAndCulture getRoleById(Syscomrole role, Syscomroleculture culture ) throws Exception;
	
	/**
	 * To get all roles and relative culture data by using culture code.
	 * 
	 * <p>以傳入語系查出所有角色資料(含語系資料)
	 * <li> 1. 不判斷有效日期(含未生效及停用資料)
	 * <li> 2. 若不傳入語系代碼，則回傳系統預設語系資料
	 * </p>
	 * @param culture  system culture code<
	 * @return all role data and relative culture data
	 * @throws Exception
	 */
	public List<SyscomroleAndCulture> getAllRoles(String culture) throws Exception;
	
	/**
	 * To get one role and relative culture data by using RoleId.
	 * 
	 * <p> 
	 * <li>1. Do not check valid date from EffectDate and ExpireDate.
	 * <li>2. If It have no culture code pass then system configuration culture data will return.
	 * </p>
	 * 
	 * @param roleId function roleId
	 * @param culture system culture code
	 * @return one role data and relative culture data.
	 * @throws Exception
	 */
	
	public SyscomroleAndCulture getRoleDataById(int roleId, String culture) throws Exception;
	
	
	/**
	 * To get all role sand relative culture data by using role name.
	 * 
	 * remark:
	 * <li>1. Do not check valid date from EffectDate and ExpireDate.
	 * <li>2. If It have no culture code pass then system configuration culture data will return.
	 * <li>3. Using SQL 'Like' clause 
	 * 
	 * <p>以角色名稱查出所有角色資料(含語系資料)
	 * <li> 1. 不判斷有效日期(含未生效及停用資料)
	 * <li> 2. 若不傳入語系代碼，則回傳系統預設語系資料
	 * </p>
	 * @param rolename role name
	 * @param culture  system culture code
	 * @return all role data and relative culture data.
	 * @throws Exception
	 */
	public List<SyscomroleAndCulture> getRoleDataByName(String roleName, String culture) throws Exception;

	/**
	 * To get one role record by using RoleNo.
	 * 
	 * remark:
	 * <li>1. Do not check valid date from EffectDate and ExpireDate
	 * <li>2. If It have no culture code pass then system configuration culture data will return.
	 * 
	 *  <p>以角色代碼(RoleNo)查出一筆角色資料</p>
	 * <li> 1. 不判斷有效日期(含未生效及停用資料)
	 * <li> 2. 若不傳入語系代碼，則回傳系統預設語系資料
	 *  	  
	 * @param roleNo role no
	 * @param culture system culture code
	 * @return one role data and relative culture data
	 * @throws Exception
	 */
	public SyscomroleAndCulture getRoleDataByNo(String roleNo, String culture) throws Exception;
	
	int addRoleCulture( Syscomroleculture oDefRoleCulture) throws Exception;
	
	int addRoleCulture(final String roleNo, String culture, String roleName, String remark ) throws Exception;
	
	
	int updateRoleCulture( Syscomroleculture oDefRoleCulture) throws Exception;
	
	int updateRoleCulture(final String roleNo, String culture, String roleName, String remark ) throws Exception;
	
	int removeRoleCulture(final int roleId, String culture) throws Exception;
	
	int removeAllRoleCultures(final int roleId) throws Exception;
	
	Syscomroleculture getRoleCultureById(Syscomroleculture oDefRoleCulture) throws Exception;
	
	List getRoleCulturesById( final int roleId) throws Exception;

	int addRoleMembers(Syscomrolemembers oDefRoleMembers) throws Exception;
	
	int addRoleMembers(final String roleNo, final String memberNo, String childType, int locationNo, 
			Date effectDate, Date expiredDate ) throws Exception;
	
	
	int updateRoleMembes(Syscomrolemembers oDefRoleMembers) throws Exception;
	
	int updateRoleMembes(final String roleNo, final String memberNo, String childType, 
			Date effectDate, Date expiredDate ) throws Exception;
	
	int removeRoleMembers(final int roleId, final int childId, String childType) throws Exception;
	int removeAllRoleMembers(final int roleId) throws Exception;
	int removeAllRoleUsers(final int roleId) throws Exception;
	
	List<SyscomrolemembersAndCulture> getUNSelectMembersById(final int roleId, String culture) throws Exception;
	
	List<SyscomrolemembersAndCulture> getSelectedMembersById(final int roleId, String culture) throws Exception;
	
	
	List<SyscomrolemembersAndCulture> getParentRolesByLogOnId(String logOnId, String culture) throws Exception;

	
	List<SyscomrolemembersAndCulture> getRoleUsersByRoleId(final int roleId) throws Exception;
	
	List<SyscomroleInfoVo> getSyscomroleInfoVoAll() throws Exception;

	List<SyscomroleResourceVo> queryMenuListByRoles(String[] roles)throws Exception;
}
