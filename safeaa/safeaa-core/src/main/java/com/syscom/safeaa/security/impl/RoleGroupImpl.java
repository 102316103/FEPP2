package com.syscom.safeaa.security.impl;

import java.util.List;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.constant.CommonConstants;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.security.RoleGroup;
import com.syscom.safeaa.utils.SyscomConfig;
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomresourceExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleExtMapper;
import com.syscom.safeaa.mybatis.extmapper.SyscomrolegroupExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomgroup;
import com.syscom.safeaa.mybatis.model.Syscomresource;
import com.syscom.safeaa.mybatis.model.Syscomrole;
import com.syscom.safeaa.mybatis.model.Syscomrolegroup;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleGroupImpl extends ApplicationBase implements RoleGroup{
	
	@Autowired
	private SyscomConfig safeSettings;
	
	@Autowired
	private SyscomrolegroupExtMapper mDBRoleGroupMapper;
	
	@Autowired
	private SyscomroleExtMapper mDBRoleMapper;
	
	@Autowired
	private SyscomgroupExtMapper mDBGroupMapper;
	
	@Autowired
	private SyscomresourceExtMapper mDBResourceMapper;

	public RoleGroupImpl() {}
	
	@Override
	public int addRoleGroup(Syscomrolegroup oDefRoleGroup) throws SafeaaException {
		try {
			if (oDefRoleGroup.getRoleid() <=0)  {
				//角色序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId); 
			}

			if (oDefRoleGroup.getGroupid() <=0)  {
				//功能群組序號未傳入
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostGroupId); 
			}	

			if (StringUtils.isBlank(oDefRoleGroup.getChildtype())) {				
				//成員類別不得空白
				this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType); 
			}	
			boolean flg = checkChildId(oDefRoleGroup.getGroupid(), oDefRoleGroup.getChildtype());
			if (flg) {
				return mDBRoleGroupMapper.insert(oDefRoleGroup);
			}
			
		} catch (Exception e) {
			throw e;
		}
		return -1;

	}

	@Override
	public int addRoleGroup(String roleNo, String groupNo, String childType) throws SafeaaException{
		
		if (StringUtils.isBlank(roleNo))  {
			//角色代碼不得空白
			this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyRoleNo); 
		}
		
		if (StringUtils.isBlank(groupNo))  {
			//功能群組代碼不得空白
			this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyGroupNo); 
		}
		
		if (StringUtils.isBlank(childType)) {
			//成員類別不得空白
			this.addError(safeSettings.getCulture(), SAFEMessageId.EmptyChildType); 
		}
		
		try {
			Syscomrole mDefRole = new Syscomrole();
			mDefRole.setRoleno(roleNo);
			Syscomrolegroup mDefRoleGroup = new Syscomrolegroup();
			mDefRoleGroup.setChildtype(childType);
			Integer roleId = mDBRoleMapper.queryRoleIdByNo(roleNo);
			
			if (roleId == null || roleId <=0) {
				//無此角色資料
				this.addError(safeSettings.getCulture(), SAFEMessageId.WithoutRoleData); 
			}
			mDefRoleGroup.setRoleid(roleId);
	
			if (childType.equals(CommonConstants.ChildTypeResource)) {
				//TODO int groupId = mDBResource
				int groupId =1;
				mDefRoleGroup.setGroupid( groupId);
				mDefRoleGroup.setChildtype(childType);
				return mDBRoleGroupMapper.insert(mDefRoleGroup);
			}else {
				
			}
			//mDefRoleGroup.setGroupId();
			if (this.checkChildNo(groupNo, childType)) {
				return mDBRoleGroupMapper.insert(mDefRoleGroup);
			}				

	    }catch(Exception e) {
			//該登入帳號尚未啟用
			this.addError(safeSettings.getCulture(), SAFEMessageId.LogOnIdNotEffect);
		}
		return -1;
	}

	@Override
	public Integer getChildIdByNo(String childNo, String childType) {		
		return 0;
	}
	
	@Override
	public boolean checkChildId(Integer childId, String childType) throws SafeaaException {
		if(childType.equals("R")) {
			Syscomresource syscomresource = mDBResourceMapper.selectByPrimaryKey(childId);
			if(syscomresource==null) {
				this.addError(safeSettings.getCulture(), SAFEMessageId.WithoutResourceData); 
				return false;
			}
			return true;
		}else if(childType.equals("G")) {
			Syscomgroup syscomgroup = mDBGroupMapper.selectByPrimaryKey(childId);
			if(syscomgroup==null) {
				this.addError(safeSettings.getCulture(), SAFEMessageId.WithoutResourceData); 
				return false;
			}
			return true;
		}else {
			this.addError(safeSettings.getCulture(), SAFEMessageId.ErrorChildType); 
			return false;
		}
	}

	@Override
	public boolean checkChildNo(String childNo, String childType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int removeRoleGroup(int roleId, int groupId, String childType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removeRoleGroupsByRoleId(int roleId) throws SafeaaException{
		try {

			if ( roleId <= 0) {
				this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
				return -1;
			}

			int rst = mDBRoleGroupMapper.deleteAllByRoleId(roleId);
			if (rst < 0) {
				addError(safeSettings.getCulture(), SAFEMessageId.DeleteNoRecord);
			}
			
			return rst;
			
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public int removeRoleGroupsByGroupId(int groupId, String childType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SyscomrolegroupAndCulture> getSelectedGroupsByRoleId(int roleId, String culture) throws SafeaaException {
		
		if ( roleId <= 0) {
			this.addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture(); 
		}
		try {
			return mDBRoleGroupMapper.querySelectedGroupsByRoleId(roleId, culture);
			
		}catch(Exception e) {
			e.printStackTrace();
			//TODO, define new messageId
			this.addError(safeSettings.getCulture(), SAFEMessageId.LogOnFail);
		}
		return null;
	}

	@Override
	public List<SyscomrolegroupAndCulture> getUNSelectedGroupsByRoleId(int roleId, String culture) throws SafeaaException{
		if ( roleId <= 0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}
		if (StringUtils.isBlank(culture)) {
			culture = safeSettings.getCulture();
		}
		try {
			return mDBRoleGroupMapper.queryUnSelectedGroupsByRoleId(roleId, culture);
			
		}catch(Exception e) {
			e.printStackTrace();
			this.addError(safeSettings.getCulture(), SAFEMessageId.LogOnFail);
		}
		return null;
	}

	@Override
	public List<Syscomrolegroup> getExecutableGroupsByUserId(int userId) throws Exception {
		if ( userId <= 0) {
			addError(safeSettings.getCulture(), SAFEMessageId.LostRoleId);
		}

		try {
			return mDBRoleGroupMapper.queryExecutableGroupsByUserId(userId);
			
		}catch(Exception e) {
			e.printStackTrace();
			//TODO, define new messageId
			this.addError(safeSettings.getCulture(), SAFEMessageId.LogOnFail);
		}
		return null;
	}



}
