package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.model.Syscomroleresource;
import com.syscom.safeaa.mybatis.vo.SyscomSelectResourcesVo;

/**
 * 
 * @author syscom
 *
 */
public class RoleResourceTest extends BaseTest {

	@Autowired
	private RoleResource roleResourceService;
	
	@Before
	public void setUp() throws Exception {
//		System.out.println("******RoleResourceTest******<開始測試>");
		roleResourceService.setCurrentUserId(1);
	}
	
	@After
	public void tearDown() throws Exception {
//		System.out.println("******RoleResourceTest******<測試結束>");
	}
	
	@Ignore("TEST OK")
	@Test
	public void addRoleResourceTest() throws Exception{
		Syscomroleresource resource = new Syscomroleresource();
		resource.setRoleid(1);
		resource.setResourceid(246);
		resource.setSafedefinefunctionlist("");
		resource.setUserdefinefunctionlist("");
		resource.setUpdateuserid(roleResourceService.getCurrentUserId());
		resource.setUpdatetime(new Date());
		int rst = roleResourceService.addRoleResource(resource);
		assertEquals(1, rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void addRoleResourceTest2() throws Exception{
		String roleno = "SYSCOM";
		String resourceno = "1";
		int rst = roleResourceService.addRoleResource(roleno,resourceno);
		assertEquals(1, rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeRoleResourceTest() throws Exception{
		int roleid = 1;
		int resourceid = 1;
		int rst = roleResourceService.removeRoleResource(roleid,resourceid);
		assertEquals(1, rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeRoleResourcesByRoleIdTest() throws Exception{
		int roleid = 1;
		int rst = roleResourceService.removeRoleResourcesByRoleId(roleid);
		assertEquals(1, rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeRoleResourcesByResourceIdTest() throws Exception{
		int resourceid = 1;
		int rst = roleResourceService.removeRoleResourcesByResourceId(resourceid);
		assertEquals(1, rst);
	}
	
	@Test
	public void getSelectedResourcesByRoleIdTest() throws Exception{
		int roleid = 1;
		String culture = "zh-TW";
		List<SyscomSelectResourcesVo> list = roleResourceService.getSelectedResourcesByRoleId(roleid,culture);
		assertNotNull(list);
		assertEquals(0, list.size());
	}
	
	@Test
	public void getUnselectResourcesByRoleIdTest() throws Exception{
		int roleid = 1;
		String culture = "zh-TW";
		List<SyscomSelectResourcesVo> list = roleResourceService.getUnselectResourcesByRoleId(roleid,culture);
		assertNotNull(list);
		assertEquals(139, list.size());
	}
}
