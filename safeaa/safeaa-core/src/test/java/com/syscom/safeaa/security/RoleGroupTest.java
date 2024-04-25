package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.model.Syscomrolegroup;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;

import java.util.List;

import javax.annotation.Resource;

public class RoleGroupTest extends BaseTest{
	@Resource
	RoleGroup roleGroup;

	@Test
	public void testGetSelectedGroupsByRoleId() throws Exception {
		String culture = "zh-TW";
		List<SyscomrolegroupAndCulture> result = roleGroup.getSelectedGroupsByRoleId(195, culture);
		assertNotNull(result);
	}

	@Test
	public void testGetUNSelectedGroupsByRoleId() throws Exception {
		String culture = "zh-TW";
		List<SyscomrolegroupAndCulture> result = roleGroup.getUNSelectedGroupsByRoleId(195, culture);
		assertNotNull(result);
	}

	@Test
	public void testGetExecutableGroupsByUserId() throws Exception {
		int userid = 25;
		List<Syscomrolegroup> result = roleGroup.getExecutableGroupsByUserId(userid);
		assertNotNull(result);
	}
	
	
}
