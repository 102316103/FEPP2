package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.model.Syscomrolemembers;
import com.syscom.safeaa.mybatis.vo.SyscomrolegroupAndCulture;

import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RoleDenyTest extends BaseTest {
	@Resource
	RoleDeny roleDeny;

	@Test
	public void testGetSelectedRoleDenyByRoleId() throws Exception{
		List<SyscomrolegroupAndCulture> result = roleDeny.getSelectedRoleDenyByRoleId(195, "zh-TW");
		
		assertNotNull(result);
	}

	@Test
	public void testGetDenyGroupsByUserId() throws Exception{
		List<Syscomrolemembers> result = roleDeny.getDenyGroupsByUserId(187);
		
		assertNotNull(result);
	}
	
}
