package com.syscom.safeaa.security;

import java.sql.Date;
import java.util.List;
import com.syscom.safeaa.core.BaseInterface;
import com.syscom.safeaa.mybatis.model.Syscomgroup;
import com.syscom.safeaa.mybatis.model.Syscomgroupculture;
import com.syscom.safeaa.mybatis.model.Syscomgroupmembers;
import com.syscom.safeaa.mybatis.vo.SyscomgroupAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomgroupInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;

/**
 * Class of processing function group and culture data (SyscomGroup, SyscomGroupCulture and SyscomGroupMember).<br/>
 * Designer: David Tai.<br/>
 * Version: 2.0.0.0.<br/>
 * Declaration: Copyright 2021 SYSCOM Computer Engineering Corporation. All rights reserved.<br/>
 * <p>
 * The data process of function group object (include program group/menu...)
 * Group definition : collection of resource object.<br/>
 * </p>
 * 
 * <p> 處理功能群組與功能群組語系的類別。</p>
 * <p> 針對功能群組物件(包含程式群組/選單...)的資料處理。</p>
 * @author JenniferYin
 *
 */
public interface Group extends BaseInterface {
	
	/**
	 * Create one new SyscomGroup record.
	 * 
	  *  建立一筆功能群組資料
	 * @param oDefGroup function group object
	 * @return The count of successful insert records, -1 represent insert fail or parameter passing error.
	 */
	int createGroup(final Syscomgroup oDefGroup) throws Exception;
	
	
	/**
	 * Modify one SyscomGroup record.
	 * 
	 * If GroupId has no value, Then find GroupId by GroupNo first.
	 * If GroupId has value, Then update table directly.
	 * 
	 * <p>修改一筆功能群組資料</p>
	 * <li>若GroupId有值則直接Update，若沒有則判斷GroupNo是否有值
	 * <li>並以GroupNo取得GroupId再以UpdateByPrimaryKey更新
	 *  
	 * @param oDefGroup function group object
	 * @return The count of successful updated records, -1 Then represent update fail or parameter passing error.
	 */
	
	int updateGroup(final Syscomgroup oDefGroup) throws Exception;
	
	/**
	 * Delete one SyscomGroup record and its relative culture data and member data.
	 * 
	  *  <p> 以功能群組序號刪除一筆功能群組及其相關的語系資料及成員資料</p>
	  *  <p> 同時刪除語系資料所以要包Transaction </p>
	 *   
	 * @param groupId function group sequence no
	 * @return true:delete successful, false: delete fail
	 */
	boolean deleteGroup(final Integer groupId) throws Exception;
	
	/**
	 * To get GroupId of function group by using GroupNo.
	 * 
	 * <p>以功能群組代碼(GroupNo)查出該筆功能群組的功能群組序號</p>
	 * 
	 * @param groupNo group no
	 * @return  groupId : -1 meaning the groupNo is not exist in SyscomGroup.
	 */
	Integer getGroupIdByNo(final String groupNo) throws Exception;
	
	/**
	 *  To get one function group record by using GroupId.
	 *  
	 *  <p>以功能群組序號(GroupId)查出一筆功能群組資料</p>
	 * @param oDefGroup
	 * @return
	 * @throws Exception
	 */
	Syscomgroup getGroupById(final Syscomgroup oDefGroup ) throws Exception;
	
	
	//Syscomgroupculture getGroupById(final Syscomgroup oDefGroup,final Syscomgroupculture oDefGroupCulture ) throws Exception;
	
	List<SyscomgroupAndCulture> getAllGroups(final boolean includeRoot, String culture) throws Exception;
	
	/**
	 * To get one function group and relative culture data by using GroupId.
	 * 
	 * remark:
	 * <li>1. Do not check valid date from EffectDate and ExpireDate.
	 * <li>2. If It have no culture code pass then system configuration culture data will return.
	 * 
	 * <local summary>
	 * <p>以功能群組序號(GroupId)查出一筆功能群組資料(含語系資料)</p>
	 * </localsummary>
	 * <li> 1. 不判斷有效日期(含未生效及停用資料)
	 * <li> 2. 若不傳入語系代碼，則回傳系統預設語系資料
	 * @param groupId function GroupId
	 * @param culture system culture code
	 * @return one function group data and relative culture data.
	 * @throws Exception
	 */
	SyscomgroupAndCulture getGroupDataById(final Integer groupId, String culture) throws Exception;
	
	
	/**
	 * To get all function group sand relative culture data by using group name.
	 * 
	 * <remarks>
	 * 1. Do not check valid date from EffectDate and ExpireDate.
	 * 2. If It have no culture code pass then system configuration culture data will return.
	 * 3. Using SQL 'Like' clause 
	 * </remarks>
	 * 
	 * <localsummary>
	  * <p>以功能群組名稱查出功能群組資料(含語系資料)</p>
	 * </localsummary>
	 * <localremarks>
	 * <li>1. 不判斷有效日期(含未生效及停用資料)
	 * <li>. 若不傳入語系代碼，則回傳系統預設語系資料
	 * <li>3. 以Like方式查詢
	 * </localremarks>	 * 
	 * @param groupName function group name
	 * @param culture system culture code
	 * @return  all function group data and relative culture data.
	 * @throws Exception
	 */
	List<SyscomgroupAndCulture> getGroupDataByName(final String groupName, String culture ) throws Exception;
	
	
	
	/**
	 * To get one function group and relative culture data by using GroupNo.
	 *
	 * <remarks>
	 * 1. Do not check valid date from EffectDate and ExpireDate.
	 * 2. If It have no culture code pass then system configuration culture data will return.
	 * </remarks>
	 * <localsummary>
	 * <p>以功能群組代碼(GroupNo)查出一筆功能群組資料(含語系資料)</p>
	 * </localsummary>
	 * <localremarks>
	 * <li>1. 不判斷有效日期(含未生效及停用資料)
	 * <li>2. 若不傳入語系代碼，則回傳系統預設語系資料
	 * </localremarks>	 * 
	 * @param groupNo function group no
	 * @param culture system culture code
	 * @return one function group data and relative culture data.
	 * @throws Exception 
	 */
	List<SyscomgroupAndCulture> getGroupDataByNo(final String groupNo, String culture) throws Exception;
	
	
	int addGroupCulture(Syscomgroupculture oDefGroupCulture) throws Exception;
	
	int addGroupCulture(final String groupNo, String culture, final String groupName, final String remark) throws Exception;
	
	int updateGroupCulture(Syscomgroupculture oDefGroupCulture) throws Exception;
	
	int updateGroupCulture(final String groupNo, String culture, final String groupName, final String remark) throws Exception;
	
	int removeGroupCulture(final Integer groupId, String culture) throws Exception;
	
	int removeAllGroupCultures( final Integer groupId) throws Exception;
	
	Syscomgroupculture getGroupCultureById(Syscomgroupculture oDefGroupCulture) throws Exception;
	
	List<Syscomgroupculture> getGroupCulturesById( final Integer groupId) throws Exception;
	
	
	int addGroupMembers(Syscomgroupmembers oDefGroupMembers ) throws Exception;

	int addGroupMembers(final String groupNo, String memberNo, String childType, int locationNo, 
			Date effectDate, Date expiredDate) throws Exception;
	
	int updateGroupMembers(Syscomgroupmembers oDefGroupMembers ) throws Exception;
	int updateGroupMembers(final String groupNo, String memberNo, String childType, int locationNo, 
			Date effectDate, Date expiredDate) throws Exception;
	
	/**
	 * Remove one SyscomGroupMembers record.
	 * 
	 * <p>以功能群組序號,成員序號及成員類別刪除一筆功能群組成員資料</p>
	 * @param groupId function group sequence no
	 * @param childId child member sequence no
	 * @param childType child type
	 * @return The count of successful delete records, if return -1 Then represent delete fail or parameter passing error.
	 */
	int removeGroupMembers(final Integer groupId, final Integer childId, final String  childType) throws Exception;
	
	
	/**
	 * Remove all SyscomGroupMembers records of designate group.
	 * 
	 * <p>以功能群組序號刪除該功能群組的所有成員資料</p>
	 * @param groupId function group sequence no
	 * @return The count of successful delete records,if return -1 Then represent delete fail or parameter passing error. 
	 */
	int removeAllGroupMembers(final Integer groupId) throws Exception;
	
	
	/**
	 * To get all unselected SyscomGroupMembers records by using GroupId and culture code.
	 * 
	 * <p>If It have no culture code pass then system configuration culture code will be placed.
	 * 
	 * <p>查出尚未加入至某功能群組(序號)的成員清單(指定語系) </p>
	 * <p>語系若不傳入會以系統預設語系處理</p>
	 * 
	 * @param groupId function group sequence no
	 * @param culture Culture code
	 * @return All group and resource list.
	 */
	List<SyscomgroupmembersAndGroupLevel>  getUNSelectedMembersById(Integer groupId, String culture) throws Exception;	
	
	/**
	 *  To get all selected SyscomGroupMembers records by using GroupId and culture code.
	 * <p>If It have no culture code pass then system configuration culture code will be placed.
	 * 
	 * <p>查出已加入至某功能群組(序號)的成員清單(指定語系) </p>
	 * <p>語系若不傳入會以系統預設語系處理</p>
	 * 
	 * @param groupId function group sequence no
	 * @param culture Culture code
	 * @return All group and resource list.
	 */
	List<SyscomgroupmembersAndGroupLevel>  getSelectedMembersById(Integer groupId, String culture) throws Exception;
	

	
	/**
	 * To get all members(including groups and resources) data of GroupId by using GroupId and culture code.
	 * 
	 * <p>以功能群組序號(GroupId)查出所屬的成員資料(含功能群組及資源) </p>
	 * <p>語系若不傳入會以系統預設語系處理</p>
	 * @param groupId function group sequence no
	 * @param culture Culture code
	 * @return All group and resource list.
	 */
	List<SyscomgroupmembersAndGroupLevel>  getNestedMembersById(Integer groupId, String culture ) throws Exception; 
	
	
	public SyscomgroupAndCulture getGroupById(Syscomgroup oDefGroup,Syscomgroupculture oDefGroupCulture) throws Exception;
	
	public List<SyscomgroupInfoVo> getSyscomgroupInfoVoAll() throws Exception; 
	
}
