package com.syscom.safeaa.security;

import com.syscom.safeaa.core.BaseTest;


public class UserTest extends BaseTest{
//	@Resource
//    User syscomuserService;
//
//	@Resource
//	Role role;
//
//	/**
//	 * 根據條件分頁查詢角色資料
//	 *
//	 * @param
//	 * @return 角色資料集合信息
//	 */
//	@Test
//	public void testSelectUserList() throws Exception{
//		Syscomuser syscomuser = new Syscomuser();
//		syscomuser.setUserid(1);
//		syscomuser.setLogonid("admin");
//		List<Syscomuser> list = syscomuserService.selectUserList(syscomuser);
//		System.out.println("Total Size:" + list.size());
//	}
//
//	/**
//	 * 通過使用者名查詢使用者
//	 *
//	 * @param
//	 * @return 使用者對象信息
//	 */
//	@Test
//	public void testSelectUserByUserName() throws Exception{
//		String name = "系統管理員";
//		List<Syscomuser> syscomuser = syscomuserService.selectUserByUserName(name);
//		System.out.println(syscomuser);
//	}
//
//	/**
//	 * Create one new SyscomUser record and relative SyscomUserStatus record.
//	 *
//	 */
//	@Test
//	public void testCreateUser() throws Exception{
//		Syscomuser syscomuser = new Syscomuser();
//		syscomuser.setLogonid("001");
//		syscomuser.setUpdateuserid(1);
//		syscomuser.setEffectdate(new Date());
//		syscomuser.setExpireddate(new Date());
//		syscomuser.setUpdatetime(new Date());
//		int passwordFormat = 0;
//		String sscodeHintQuestion = "Yes or Not?";
//		String sscodeHintAnswer = "Yes";
//		String sscode = "123456";
//		int success = syscomuserService.createUser(syscomuser,passwordFormat,sscodeHintQuestion,sscodeHintAnswer,sscode);
//		System.out.println(success);
//		assertEquals(1, success);
//	}
//
//	/**
//	 *
//	 * 修改一筆使用者資料
//	 * 若UserId有值則直接Update，若沒有則判斷LogOnId是否有值
//	 * 並以LogOnId取得UserId再以UpdateByPrimaryKey更新
//	 */
//	@Test
//	public void testUpdateUser() throws Exception {
//		Syscomuser sysUser = new Syscomuser();
//		sysUser.setUserid(999);
//		sysUser.setLogonid("001");
//		sysUser.setBirthday("19910226");
//
//		int result = syscomuserService.updateUser(sysUser);
//		System.out.println(result);
//	}
//
//	/**
//	 *
//	 * 以使用者序號刪除一筆使用者及其相關的狀態資料及成員資料
//	 * 同時刪除狀態資料所以要包Transaction
//	 */
//	@Test
//	public void testDeleteUser() throws Exception {
//		int userId = 999;
//		boolean result = syscomuserService.deleteUser(userId);
//		System.out.println(result);
//	}
//
//	/**
//	 *
//	 * 以登入帳號(LogOnId)查出該筆使用者的使用者序號
//	 *
//	 */
//	@Test
//	public void testGetUserIdByNo() throws Exception{
//		String logOnId= "001";
//		List<Syscomuser> result = syscomuserService.getUserIdByNo(logOnId);
//		System.out.println(result);
//	}
//
//	/**
//	 *
//	 * 以使用者序號(UserId)查出一筆使用者資料
//	 *
//	 */
//	@Test
//	public void testGetUserById() throws Exception{
//		Syscomuser syscomuser = new Syscomuser();
//		syscomuser.setUserid(999);
//
//		Syscomuser result= syscomuserService.getUserById(syscomuser);
//		System.out.println(result);
//	}
//
//
//	/**
//	 * 以使用者序號(UserId)查出一筆使用者資料(含狀態資料)
//	 *
//	 */
//	@Test
//	public void testGetUserById_II() throws Exception{
//		Syscomuser syscomuser = new Syscomuser();
//		syscomuser.setUserid(999);
//
//		Syscomuserstatus syscomuserstatus = new Syscomuserstatus();
//		syscomuserstatus.setUserid(999);
//
//		int  result = syscomuserService.getUserById(syscomuser, syscomuserstatus);
//		System.out.println(result);
//	}
//
//	/**
//	 * all user data and relative status data.
//	 */
//	@Test
//	public void testGetAllUsers() throws Exception{
//		String whereClause = "";
//
//		List<SyscomQueryAllUsers>  result = syscomuserService.getAllUsers(whereClause);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get one user and relative status data by using UserId.
//	 * 1. Do not check valid date from EffectDate and ExpireDate.
//	 * 2. If It have no culture code pass then system configuration status data will return.
//	 * 以使用者序號(UserId)查出一筆使用者資料(含狀態資料)
//	 * 1. 不判斷有效日期(含未生效及停用資料)
//	 */
//	@Test
//	public void testGetUserDataById() throws Exception{
//		int userId = 999;
//
//		List<SyscomQueryAllUsers>  result = syscomuserService.getUserDataById(userId);
//		System.out.println(result);
//	}
//
//	/**
//	 *
//	 * 以使用者名稱查出使用者資料(含狀態資料)
//	 * 1. 不判斷有效日期(含未生效及停用資料)
//	 * 2. 以Like方式查詢
//	 */
//	@Test
//	public void testGetUserDataByName() throws Exception{
//		String userName = "Qine";
//
//		List<SyscomQueryAllUsers>  result = syscomuserService.getUserDataByName(userName);
//		System.out.println(result);
//	}
//
//	/**
//	 *
//	 * 以登入帳號(LogOnId)查出一筆使用者資料(含狀態資料)
//	 * 1. 不判斷有效日期(含未生效及停用資料)
//	 *
//	 */
//	@Test
//	public void testGetUserDataByNo() throws Exception{
//		String logOnId = "001";
//
//		List<SyscomQueryAllUsers>  result = syscomuserService.getUserDataByNo(logOnId);
//		System.out.println(result);
//	}
//
//	/**
//	 * <param name="userMail">user no mail address</param>
//	 * one user data and relative status data.
//	 * 以使用者EMail查出使用者資料(含狀態資料)
//	 * 1. 不判斷有效日期(含未生效及停用資料)
//	 *
//	 */
//	@Test
//	public void testGetUserDataByMail() throws Exception{
//		String userMail = "100@qq.com";
//
//		List<SyscomQueryAllUsers>  result = syscomuserService.getUserDataByMail(userMail);
//		System.out.println(result);
//	}
//
//	/**
//	 * Create one new SyscomUserStatus record.
//	 * The count of successful insert records,
//	 * 建立一筆使用者狀態資料
//	 *
//	 */
//	@Test
//	public void testCreateUserStatus() throws Exception{
//		Syscomuserstatus syscomuserstatus = new Syscomuserstatus();
//		syscomuserstatus.setUserid(998);
//		syscomuserstatus.setSscodeformat(0);
//		syscomuserstatus.setIslockout((short) 0);
//
//		int result = syscomuserService.createUserStatus(syscomuserstatus);
//		System.out.println(result);
//	}
//
//	/**
//	 * Create one new SyscomUserStatus record.
//	 * The count of successful insert records,
//	 * 修改一筆使用者狀態資料
//	 *
//	 */
//	@Test
//	public void testUpdateUserStatus() throws Exception{
//		Syscomuserstatus syscomuserstatus = new Syscomuserstatus();
//		syscomuserstatus.setUserid(998);
//		syscomuserstatus.setSscodehintquestion("true or false");
//		syscomuserstatus.setSscodehintanswer("true");
//
//		int result = syscomuserService.updateUserStatus(syscomuserstatus);
//		System.out.println(result);
//	}
//
//	/**
//	 * Delete one designate SyscomUserStatus record.
//	 * The count of successful deleted records,
//	 * 以使用者序號刪除一筆使用者狀態資料
//	 *
//	 */
//	@Test
//	public void testDeleteUserStatus() throws Exception{
//		int userId = 998;
//
//		int result = syscomuserService.deleteUserStatus(userId);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get SyscomUserStatus record by using UserId.
//	 * Status data of designate user.
//	 * 以使用者序號(UserId)查出該筆使用者的狀態資料
//	 *
//	 */
//	@Test
//	public void testGetUserStatusById() throws Exception{
//		Syscomuserstatus syscomuserstatus = new Syscomuserstatus();
//		syscomuserstatus.setUserid(999);
//
//		Syscomuserstatus result = syscomuserService.getUserStatusById(syscomuserstatus);
//		System.out.println(result);
//	}
//
//	/**
//	 *
//	 * Force to unlock one user status record.
//	 * 強迫帳戶解鎖
//	 *
//	 */
//	@Test
//	public void testForceAccountLockOff() throws Exception{
//		int userId = 999;
//		int updateUserId = 0;
//
//		int result = syscomuserService.forceAccountLockOff(userId, updateUserId);
//		System.out.println(result);
//	}
//
//	/**
//	 * Validate the new LogOnId of user.
//	 *
//	 * @Param new user logon Id
//	 * <returns>
//	 * True : LogOnId is valid, False : LogOnId is not valid
//	 * </returns>
//	 * Check rules are depending on system policy.
//	 * 檢核新登入帳號
//	 * 先取得該系統Default管理原則
//	 */
//	@Test
//	public void testValidateNewLogOnId() throws Exception{
//		String newLogOnId = "123L56";
//
//		boolean result = syscomuserService.validateNewLogOnId(newLogOnId);
//		System.out.println(result);
//	}
//
//	/**
//	 * Validate the new password of user.
//	 * True : password is valid, False : password is not valid
//	 *
//	 * @return
//	 * @throws Exception 檢核新密碼
//	 *                   先取得該系統Default管理原則
//	 */
//	@Test
//	public void testValidateNewPassword() throws Exception{
//		int userId = 999;
//		String newSscode = "Qil890";
//		String idno = "610527199102661823";
//		String birthday = "19910226";
//		String logOnId = "001";
//		int passwordEncryption = 0;
//
//		boolean result = syscomuserService.validateNewPassword(userId, newSscode, idno, birthday, logOnId, passwordEncryption);
//		System.out.println(result);
//	}
//
//	@Test
//	public void testGetSyscomuserInfo() throws Exception{
//		String logOnId = "admin";
//
//		SyscomuserInfoVo vo = syscomuserService.getSyscomuserInfo(logOnId);
//		Assert.assertNotNull(vo);
//		Assert.assertEquals("系統管理員", vo.getUsername());
//		Assert.assertEquals(Long.valueOf(1L).intValue(),vo.getIslogon().intValue());
//	}
}
