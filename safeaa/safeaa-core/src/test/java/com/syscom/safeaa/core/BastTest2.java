package com.syscom.safeaa.core;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.syscom.safeaa.mybatis.extmapper.SyscompolicyExtMapper;
import com.syscom.safeaa.mybatis.model.SyscomQueryParentRolesByUserId;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.security.User;




public class BastTest2  extends BaseTest{
	@Resource
    User syscomuserService;
	@Resource
	SyscompolicyExtMapper syscommypolicyService;

	@Test
    public  void testISyscomuser(){
		List<Syscomuser> user = syscomuserService.selectUserByUserName("3");
		assertNotNull(user);

//		System.out.println(user.get(0).getUsername());
    }

	/**
	 * PROCEDURE "QUERYPOLICYBYUSERID"
	 * 測試存儲過程queryPolicyByUserId
	 */
	@Test
	public  void testISyscommypolicy(){
		List<SyscomQueryParentRolesByUserId> list = syscommypolicyService.queryPolicyByUserId(1);
		assertNotNull(list);

//		System.out.println("查詢完成，總計查出：" + list.size() + "筆資料");
	}
}
