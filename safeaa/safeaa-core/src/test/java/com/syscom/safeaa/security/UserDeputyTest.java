package com.syscom.safeaa.security;

import com.syscom.safeaa.core.BaseTest;



public class UserDeputyTest extends BaseTest{
//	@Resource
//	UserDeputy syscomUserDeputyService;
//
//	/**
//	 * Create one new SyscomUserDeputy record.
//	 * The count of successful insert records,
//	 * 增加使用者代理人資料(傳入使用者代理人物件)
//	 *
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testAddUserDeputy() throws Exception{
//		Syscomuserdeputy syscomuserdeputy = new Syscomuserdeputy();
//		syscomuserdeputy.setUserid(111);
//		syscomuserdeputy.setRoleid(112);
//		syscomuserdeputy.setDeputyuserid(113);
//		syscomuserdeputy.setEffectdate(new Date());
//		syscomuserdeputy.setExpireddate(new Date());
//		syscomuserdeputy.setUpdateuserid(1);
//		syscomuserdeputy.setUpdatetime(new Date());
//
//		int result = syscomUserDeputyService.addUserDeputy(syscomuserdeputy);
//		System.out.println(result);
//	}
//
//	/**
//	 * Delete one SyscomUserDeputy record.
//	 * The count of successful insert records,
//	 * if return -1 Then represent insert fail or parameter passing error.
//	 * 以角色序號,成員序號及代理人序號刪除一筆使用者代理人資料
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testRemoveUserDeputy() throws Exception{
//		int userId = 111;
//		int roleId = 112;
//		int deputyUserId = 113;
//
//		int result = syscomUserDeputyService.removeUserDeputy(userId, roleId, deputyUserId);
//		System.out.println(result);
//	}
//
//	/**
//	 * Delete SyscomUserDeputy records of one user.
//	 * The count of successful insert records,
//	 * if return -1 Then represent insert fail or parameter passing error.
//	 * 以使用者序號移除一個使用者的所有代理人資料
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testRemoveUserDeputysByUserId() throws Exception{
//		int userId = 111;
//
//		int result = syscomUserDeputyService.removeUserDeputysByUserId(userId);
//		System.out.println(result);
//	}
//
//	/**
//	 * Delete SyscomUserDeputy records of one role.
//	 * The count of successful insert records,
//	 * if return -1 Then represent insert fail or parameter passing error.
//	 * 以角色序號移除一個角色的所有使用者代理人資料
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testRemoveUserDeputysByRoleId() throws Exception{
//		int roleId = 112;
//
//		int result = syscomUserDeputyService.removeUserDeputysByRoleId(roleId);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get all selected SyscomUserDeputy records by using UserId.
//	 * 以使用者序號查出已加入至某使用者代理人的使用者清單
//	 *
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetSelectedDeputyByUserId() throws Exception{
//		int roleId = 112;
//		String orderBy = null;
//
//		List<SyscomQueryAllUserDeputyVo> result = syscomUserDeputyService.getSelectedDeputyByUserId(roleId, orderBy);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get all unselected SyscomUserDeputy records by using UserId and RoleId.
//	 * 查出尚未加入至某使用者角色代理人的使用者清單
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetUNSelectRolesByUserId() throws Exception{
//		int userId = 999;
//		int roleId = 1;
//
//		List<Syscomuser> list = syscomUserDeputyService.getUNSelectRolesByUserId(userId, roleId);
//		System.out.println("Total Size:" + syscomUserDeputyService.getUNSelectRolesByUserId(userId, roleId).size());
//	}
//
//	/**
//	 * To get one SyscomUserDeputy record by using UserId and culture code.
//	 * 以使用者序號查出已使用者代理人的代理狀態
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetDeputyMatrixByUserId() throws Exception{
//		int userId = 1;
//		String culture = "zh-TW";
//
//		List<SyscomDeputyUserMatrixVo> list = syscomUserDeputyService.getDeputyMatrixByUserId(userId, culture);
//
//	}
}
