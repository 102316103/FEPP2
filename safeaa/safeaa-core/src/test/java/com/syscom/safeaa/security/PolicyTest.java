package com.syscom.safeaa.security;

import com.syscom.safeaa.core.BaseTest;


public class PolicyTest extends BaseTest{
//	@Resource
//	Policy policy;
//
//	/**
//	 * Create one new SyscomPolicy record.
//	 * The count of successful insert records,
//	 * if return -1 Then represent insert fail or parameter passing error.
//	 * 建立一筆管理原則資料
//	 * @return
//	 */
//	@Test
//	public void testCreatePolicy() throws Exception{
//		Syscompolicy syscompolicy = new Syscompolicy();
//		syscompolicy.setPolicyid(99);
//		syscompolicy.setPolicyno("test");
//		syscompolicy.setAllowlocal((short) 1);
//		syscompolicy.setGetvaluetype("4");
//		syscompolicy.setDefaultpolicyvalue("1111");
//		syscompolicy.setInvalidmessageid("0096");
//		syscompolicy.setEffectdate(new Date());
//		syscompolicy.setExpireddate(new Date());
//		syscompolicy.setUpdateuserid(1);
//		syscompolicy.setUpdatetime(new Date());
//
//		int result = policy.createPolicy(syscompolicy);
//		System.out.println(result);
//	}
//
//	/**
//	 * Modify one SyscomPolicy record.
//	 * The count of successful updated records,
//	 * if return -1 Then represent update fail or parameter passing error.
//	 * If PolicyId has no value, Then find PolicyId by PolicyNo first.<br/>
//	 * If PolicyId has value, Then update table directly.
//	 * 修改一筆管理原則資料
//	 * 若PolicyId有值則直接Update，若沒有則判斷PolicyNo是否有值
//	 * 並以PolicyNo取得PolicyId再以UpdateByPrimaryKey更新
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testUpdatePolicy() throws Exception{
//		Syscompolicy syscompolicy = new Syscompolicy();
//		syscompolicy.setPolicyid(99);
//		syscompolicy.setPolicyno("test");
//		syscompolicy.setAllowlocal((short) 1);
//		syscompolicy.setGetvaluetype("4");
//		syscompolicy.setDefaultpolicyvalue("2222");
//		syscompolicy.setInvalidmessageid("0096");
//		syscompolicy.setEffectdate(new Date());
//		syscompolicy.setExpireddate(new Date());
//		syscompolicy.setUpdateuserid(1);
//		syscompolicy.setUpdatetime(new Date());
//
//		int result = policy.updatePolicy(syscompolicy);
//		System.out.println(result);
//	}
//
//	/**
//	 * Delete one SyscomPolicy record and its relative culture data and member data.
//	 * policy sequence no
//	 * True:delete successful False: delete fail
//	 * Update db need transaction process because delete culture data simulate.
//	 * 以管理原則序號刪除一筆管理原則及其相關的語系資料及成員資料
//	 * ͬ同時刪除語系資料所以要包Transaction
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testDeletePolicy() throws Exception{
//		int policyId = 99;
//
//		boolean result = policy.deletePolicy(policyId);
//		System.out.println(result);
//	}
//
//	/**
//	 * Delete one SyscomPolicy record and its relative culture data and member data.
//	 * policy sequence no
//	 * True:delete successful False: delete fail
//	 * Update db need transaction process because delete culture data simulate.
//	 * 以管理原則序號刪除一筆管理原則及其相關的語系資料及成員資料
//	 * ͬ同時刪除語系資料所以要包Transaction
//	 *
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetPolicyIdByNo() throws Exception{
//		String policyNo = "test";
//
//		List<Syscompolicy> result = policy.getPolicyIdByNo(policyNo);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get one policy record by using PolicyId.
//	 * if return -1 Then represent query fail or parameter passing error.
//	 * 以管理原則代碼(PolicyNo)查出該筆管理原則的管理原則序號
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetPolicyById() throws Exception{
//		Syscompolicy syscompolicy = new Syscompolicy();
//		syscompolicy.setPolicyid(99);
//
//		Syscompolicy result = policy.getPolicyById(syscompolicy);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get one policy record by using PolicyId.
//	 * if return -1 Then represent query fail or parameter passing error.
//	 * 以管理原則序號(PolicyId)查出一筆管理原則資料
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetPolicyByIdII() throws Exception{
//		Syscompolicy syscompolicy = new Syscompolicy();
//		syscompolicy.setPolicyid(99);
//		Syscompolicyculture syscompolicyculture = new Syscompolicyculture();
//		syscompolicyculture.setCulture("zh-TW");
//		syscompolicyculture.setPolicyid(1);
//
//		Syscompolicy result = policy.getPolicyById(syscompolicy, syscompolicyculture);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get all policys and relative culture data by using culture code.
//	 * 1. Do not check valid date from EffectDate and ExpireDate.
//	 * 2. If It have no culture code pass then system configuration culture data will return.
//	 * 以傳入語系查出所有管理原則資料(含語系資料)
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetAllPolicys() throws Exception{
//		String culture = "zh-tw";
//
//		List<SyscomQueryAllPolicysVo> result = policy.getAllPolicys(culture);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get one policy and relative culture data by using PolicyId.
//	 * 以管理原則序號(PolicyId)查出一筆管理原則資料(含語系資料)
//	 *
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetPolicyDataById() throws Exception{
//		String culture = "zh-tw";
//		Integer policyId = 1;
//
//		List<SyscomQueryAllPolicysVo> result = policy.getPolicyDataById(culture, policyId);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get all policys and relative culture data by using policy name.
//	 * all policy data and relative culture data.
//	 * 以管理原則名稱查出管理原則資料(含語系資料)
//	 * 1. 不判斷有效日期(含未生效及停用資料)
//	 * 2. 若不傳入語系代碼，則回傳系統預設語系資料
//	 * 3. 以Like方式查詢
//	 *
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetPolicyDataByName() throws Exception{
//		String culture = "zh-tw";
//		String policyName = "帳戶鎖定期間";
//
//		List<SyscomQueryAllPolicysVo> result = policy.getPolicyDataByName(culture, policyName);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get one policy and relative culture data by using PolicyNo.
//	 *  以管理原則代碼(PolicyNo)查出一筆管理原則資料(含語系資料)
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetPolicyDataByNo() throws Exception{
//		String culture = "zh-tw";
//		String policyNo = "AccountLockoutDuration";
//
//		List<SyscomQueryAllPolicysVo> result = policy.getPolicyDataByNo(culture, policyNo);
//		System.out.println(result);
//	}
//
//	/**
//	 * Create one new SyscomPolicyCulture record.
//	 * 建立一筆管理原則語系資料(傳入語系物件)
//	 * 語系若不傳入會以系統預設語系處理
//	 *
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testAddPolicyCulture() throws Exception{
//		Syscompolicyculture syscompolicyculture = new Syscompolicyculture();
//		syscompolicyculture.setPolicyid(999);
//		syscompolicyculture.setCulture("zh-tw");
//		syscompolicyculture.setPolicyname("test");
//		syscompolicyculture.setRemark("test");
//
//		int result = policy.addPolicyCulture(syscompolicyculture);
//		System.out.println(result);
//	}
//
//	/**
//	 * Create one new SyscomPolicyCulture record.
//	 * 建立一筆管理原則語系資料(傳入語系資料)
//	 * 語系若不傳入會以系統預設語系處理
//	 *
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testAddPolicyCultureII() throws Exception{
//		String policyNo = "test";
//		String culture = "zh-tw";
//		String policyName = "test";
//		String remark = "test";
//
//		int result = policy.addPolicyCulture(policyNo, culture, policyName, remark);
//		System.out.println(result);
//	}
//
//	/**
//	 * Update one SyscomPolicyCulture record.
//	 * 修改一筆管理原則語系資料(傳入語系資料)
//	 * 語系若不傳入會以系統預設語系處理
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testUpdatePolicyCulture() throws Exception{
//		Syscompolicyculture syscompolicyculture = new Syscompolicyculture();
//		syscompolicyculture.setPolicyid(999);
//		syscompolicyculture.setCulture("zh-tw");
//		syscompolicyculture.setPolicyname("test_1");
//		syscompolicyculture.setRemark("test_2");
//
//		int result = policy.updatePolicyCulture(syscompolicyculture);
//		System.out.println(result);
//	}
//
//	/**
//	 * Update one SyscomPolicyCulture record.
//	 * 修改一筆管理原則語系資料(傳入語系資料)
//	 * 1. 先取得PolicyId再更新資料
//	 * 2. 語系若不傳入會以系統預設語系處理
//	 */
//	@Test
//	public void testUpdatePolicyCultureII() throws Exception{
//		String policyNo = "test";
//		String culture = "zh-tw";
//		String policyName = "test";
//		String remark = "test";
//
//		int result = policy.updatePolicyCulture(policyNo, culture, policyName, remark);
//		System.out.println(result);
//	}
//
//	/**
//	 * Delete one designate SyscomPolicyCulture record.
//	 * 以管理原則序號刪除一筆管理原則語系資料(指定語系)
//	 * 語系若不傳入會以系統預設語系處理
//	 */
//	@Test
//	public void testRemovePolicyCulture() throws Exception{
//		int policyId = 999;
//		String culture = "zh-tw";
//
//		int result = policy.removePolicyCulture(policyId, culture);
//		System.out.println(result);
//	}
//
//	/**
//	 * To delete all SyscomPolicyCulture records by using PolicyId .
//	 * 以管理原則序號刪除該管理原則的所有語系資料
//	 */
//	@Test
//	public void testRemoveAllPolicyCultures() throws Exception{
//		Integer policyId = 999;
//
//		int result = policy.removeAllPolicyCultures(policyId);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get one SyscomPolicyCulture record by using PolicyId and culture code.
//	 * 以管理原則序號(PolicyId)及語系查出一筆管理原則語系資料
//	 * @return
//	 * @throws SafeaaException
//	 */
//	@Test
//	public void testGetPolicyCultureById() throws Exception{
//		int policyId = 999;
//		String culture = "zh-tw";
//
//		Syscompolicyculture syscompolicyculture = new Syscompolicyculture();
//		syscompolicyculture.setPolicyid(policyId);
//		syscompolicyculture.setCulture(culture);
//		Syscompolicyculture result = policy.getPolicyCultureById(syscompolicyculture);
//		System.out.println(result);
//	}
//
//	/**
//	 * To get all SyscomPolicyCulture records by using PolicyId.
//	 * 以管理原則序號(PolicyId)查出該筆管理原則的所有語系資料
//	 *
//	 */
//	@Test
//	public void testGetPolicyCulturesById() throws Exception{
//		Integer policyId = 999;
//
//		List<Syscompolicyculture> result = policy.getPolicyCulturesById(policyId);
//		System.out.println(result);
//	}
}
