package com.syscom.safeaa.security.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.syscom.safeaa.base.ApplicationBase;
import com.syscom.safeaa.enums.EnumActionType;
import com.syscom.safeaa.enums.SAFEMessageId;
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomgroup;
import com.syscom.safeaa.mybatis.model.Syscomrolegroup;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;
import com.syscom.safeaa.security.Authorization;
import com.syscom.safeaa.security.Group;
import com.syscom.safeaa.security.MenuTree;
import com.syscom.safeaa.security.RoleDeny;
import com.syscom.safeaa.security.RoleGroup;

/**
 * 
 * @author JenniferYin
 *
 */
@Component
public class AuthorizationImpl extends ApplicationBase implements Authorization {

	public final static int ActionTypeCount = EnumActionType.values().length;

	
	@Autowired
    SyscomgroupExtMapper groupExtMapper;
	
	@Autowired
	Group group;
	
	@Autowired
	RoleDeny roleDeny;
	
	@Autowired
	RoleGroup roleGroup;
	
	
	public AuthorizationImpl() {
	}
	
	public boolean checkButtonEnable(final String functionList, final EnumActionType actionType ) {
		
		if (StringUtils.isBlank(functionList) ||functionList.length() < ActionTypeCount ) {
			return false;
		}
		
		try {
			return Boolean.parseBoolean( functionList.substring(actionType.getValue(), 1+actionType.getValue() ));
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	/**
	 * Generate XML of menu control for user login
	 * 
	 * <p>產生MENU CONTROL所需的XM</p>
	 * @param userId  user sequence no
	 * @param culture culture code
	 * @param rootGroup root node for search tree
	 * @return  XML for menu control
	 */ 
	public String getMenuXml(final long userId, final String culture, final String rootGroup) {
		
		String cacheKey = "menu_" + culture.toLowerCase();
		//TODO cache data
		_log.debug("load menu from db");
		
		String cur_culture = culture;
		try {
			
			Integer groupId = groupExtMapper.queryGroupIdByNo(rootGroup);
			if (groupId == null || groupId <=0 ) {
				addError("", SAFEMessageId.WithoutGroupData);
			}
			Syscomgroup mDefGroup = groupExtMapper.selectByPrimaryKey(groupId);
			if (null== mDefGroup || mDefGroup.getGroupid()<=0) {
				addError("", SAFEMessageId.WithoutGroupData); //無功能群組資料
			}
			
			if (StringUtils.isBlank(culture)) {
				cur_culture = "zh-TW";
			}

			MenuTree tree = new MenuTree(new SyscomgroupmembersAndGroupLevel(mDefGroup)); 
			
			List<SyscomgroupmembersAndGroupLevel> dtGroup = group.getNestedMembersById(groupId, cur_culture);
			
			tree.generateMenuTree(dtGroup);
			
			List<Syscomrolegroup> dtRoleGroup = roleGroup.getExecutableGroupsByUserId(1);
			tree.addRoleGroupResource(dtRoleGroup);
			
			List<Syscomrolemembers> dtRoleDeny = roleDeny.getDenyGroupsByUserId(187);
			
			tree.denyRoleGroupResource(dtRoleDeny);			
			_log.debug(tree);
			return tree.toString();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
		
}
