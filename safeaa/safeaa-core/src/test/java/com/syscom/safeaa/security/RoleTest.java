package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.model.Syscomrole;
import com.syscom.safeaa.mybatis.model.Syscomroleculture;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomroleResourceVo;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;


public class RoleTest extends BaseTest{
	@Resource
    User syscomuserService;

	@Resource
	Role role;

	@Test
	public void testUpdateRole() throws Exception{
		Syscomrole defRole = new Syscomrole();
		defRole.setRoleid(195);
		defRole.setRoleno("T001");
		defRole.setRoletype("3");
//		System.out.println("update role");
		role.updateRole(defRole);
	}

	@Test
	public void testGetRoleById() throws Exception{
		Syscomrole defRole = new Syscomrole();
		defRole.setRoleid(195);
		Syscomrole result = role.getRoleById(defRole);
		assertEquals(result.getRoleno(), "T001");
	}

	@Test
	public void testGetRoleByIdWithCulture() throws Exception{

		Syscomrole defRole = new Syscomrole();
		defRole.setRoleid(1);
		Syscomroleculture  roleculture = new Syscomroleculture();
		roleculture.setCulture("zh-TW");
		SyscomroleAndCulture result = role.getRoleById(defRole,roleculture);
		assertEquals(result.getRolename(), "SAFE");
	}

	@Test
	public void testGetAllRoles() throws Exception {

		List<SyscomroleAndCulture> result = role.getAllRoles("zh-TW");
		assertEquals(result.size(), 7);
	}

	@Test
	public void testGetRoleDataById() throws Exception {
		int roleid = 1;
		SyscomroleAndCulture result = role.getRoleDataById(roleid, "zh-TW");
		assertEquals (result.getRoleno(), "SYSCOM");
	}

	@Test
	public void testGetRoleDataByName() throws Exception{
		String roleName= "S";
		List<SyscomroleAndCulture> result = role.getRoleDataByName(roleName, "zh-TW");
		assertEquals (result.size(), 1);
	}

	@Test
	public void testGetRoleDataByNo() throws Exception{
		String roleNo= "T001";
		SyscomroleAndCulture result = role.getRoleDataByNo(roleNo, "zh-TW");
		assertEquals(result.getRoleid().intValue(), 195);
	}

	@Test
    public  void testISyscomuser(){
		List<Syscomuser> user = syscomuserService.selectUserByUserName("3");
		assertNotNull(user);

//		System.out.println(user.get(0).getUsername());
    }

	@Test
	public void testGetUNSelectMembersById() throws Exception{
		List<SyscomrolemembersAndCulture>  result = role.getUNSelectMembersById(1, "");
		assertNotNull(result);
	}

    @Test
    public void testQueryMenuListByRoles() throws Exception{
        String[] roles = {"FEPAdmin","TesterGroup2"};
        List<SyscomroleResourceVo>  result = role.queryMenuListByRoles(roles);
        assertNotNull(result);
    }
}
