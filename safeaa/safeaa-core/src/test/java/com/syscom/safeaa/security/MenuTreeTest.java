package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.extmapper.SyscomgroupExtMapper;
import com.syscom.safeaa.mybatis.model.Syscomgroup;
import com.syscom.safeaa.mybatis.model.Syscomrolegroup;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;

public class MenuTreeTest extends BaseTest {

    private MenuTree tree;
	@Autowired
	RoleDeny roleDeny;

	@Autowired
	Group groupEt;
	@Autowired
    SyscomgroupExtMapper groupMapper;
	
	@Autowired
	RoleGroup roleGroup;
	
	@Test
	public void testMenuTreeLevel() throws Exception{
//		SyscomgroupmembersAndGroupLevel group = new SyscomgroupmembersAndGroupLevel();
//		group.setGroupid(1L);
//		group.setChildid(1L);
//		group.setChildno("32");
//		group.setChildname("gadafwd");
//		
//		group.setGrouplevel(0L);
//		group.setLocationno(15L);
//		group.setUrl("");
//		
//		//tree = new MenuTree(group);
//		System.out.println(tree.toString());
//		
//		long groupId = groupMapper.queryGroupIdByNo("SYSCOM");
//		Syscomgroup mDefGroup = groupMapper.selectByPrimaryKey(groupId);
//		
//		tree = new MenuTree(new SyscomgroupmembersAndGroupLevel(mDefGroup)); 
//		
//		String culture = "zh-TW";
//		List<SyscomgroupmembersAndGroupLevel> dtGroup = groupEt.getNestedMembersById(groupId, culture);
//		
//		tree.generateMenuTree(dtGroup);
//		
//		List<Syscomrolegroup> dtRoleGroup = roleGroup.getExecutableGroupsByUserId(1L);
//		tree.addRoleGroupResource(dtRoleGroup);
//		
//		List<Syscomrolemembers> dtRoleDeny = roleDeny.getDenyGroupsByUserId(187L);
//		System.out.println( "dtRoleDeny.size()=" + dtRoleDeny.size());
//		
//		tree.denyRoleGroupResource(dtRoleDeny);
//		System.out.println(tree);
	}

	@Test
	public void testGenerateMenuTree() {
		
	}

	@Test
	public void testAddNode() {
	}

	@Test
	public void testAddSingleNode() {
	}

}
