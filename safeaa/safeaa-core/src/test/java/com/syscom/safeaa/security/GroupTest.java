package com.syscom.safeaa.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.model.Syscomgroup;
import com.syscom.safeaa.mybatis.model.Syscomgroupculture;
import com.syscom.safeaa.mybatis.model.Syscomgroupmembers;
import com.syscom.safeaa.mybatis.model.Syscomuser;
import com.syscom.safeaa.mybatis.vo.SyscomgroupAndCulture;
import com.syscom.safeaa.mybatis.vo.SyscomgroupmembersAndGroupLevel;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
public class GroupTest extends BaseTest{
	
	@Resource 
	Group group;
	
	@SuppressWarnings("deprecation")
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
//		System.out.println("******AuthenticationTest******<開始測試>");

		group.setCurrentUserId(1);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition posExpireddate = new ParsePosition(0);
		ParsePosition posEffectdate = new ParsePosition(0);
		Syscomuser currentUser = new Syscomuser();
		currentUser.setUserid(1);
		currentUser.setIdno("1");
		currentUser.setBirthday("2020-01-07");
		currentUser.setLogonid("1");
		currentUser.setExpireddate(sdf.parse("2030-01-07", posExpireddate));
		currentUser.setEffectdate(sdf.parse("2020-01-07", posEffectdate));
		group.setCurrentUser(currentUser);
	}

	@Ignore("TEST OK")
	@Test
	public void createGroupExceptionTest() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組代碼不得空白");
		int rst = group.createGroup(new Syscomgroup());
		assertEquals(-1, rst);
	}


	@Test
	public void createGroupTest() throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition posExpireddate = new ParsePosition(0);
		ParsePosition posEffectdate = new ParsePosition(0);

		Syscomgroup syscomgroup = new Syscomgroup();
		syscomgroup.setGroupno("Group");
		syscomgroup.setExpireddate(sdf.parse("2030-01-07", posExpireddate));
		syscomgroup.setEffectdate(sdf.parse("2010-01-07", posEffectdate));
		syscomgroup.setUpdatetime(new Date());
		syscomgroup.setUpdateuserid(1);
		int rst = group.createGroup(syscomgroup);

		assertEquals(1, rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void updateGroupException1Test() throws Exception {
		Syscomgroup syscomgroup = new Syscomgroup();
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組代碼不得空白");
		int rst = group.updateGroup(syscomgroup);
		assertEquals(-1, rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void updateGroupException2Test() throws Exception {
		Syscomgroup syscomgroup = new Syscomgroup();
		syscomgroup.setGroupno("Group");
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("無此功能群組資料");
		int rst = group.updateGroup(syscomgroup);
		assertEquals(-1, rst);
	}
	
//	@Ignore("TEST OK")
	@Test
	public void updateGroupTest() throws Exception {
		Syscomgroup syscomgroup = new Syscomgroup();
		syscomgroup.setGroupid(1);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");		
		syscomgroup.setEffectdate(simpleDateFormat.parse("2018-12-17"));
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		ParsePosition posExpireddate = new ParsePosition(0);
//		ParsePosition posEffectdate = new ParsePosition(0);
//		syscomgroup.setExpireddate(sdf.parse("2030-01-07", posExpireddate));
//		syscomgroup.setEffectdate(sdf.parse("2010-01-07", posEffectdate));
//		syscomgroup.setGroupurl("");
		int rst = group.updateGroup(syscomgroup);
		assertEquals(1, rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void deleteGroupException1Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		boolean rst = group.deleteGroup(null);
		assertFalse(rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void deleteGroupException2Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("刪除主檔失敗");
		boolean rst = group.deleteGroup(999999);
		assertFalse(rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupIdByNoException1Test() throws Exception{
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組代碼不得空白");
		long result = group.getGroupIdByNo("");
		assertEquals(-1,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupIdByNoException2Test() throws Exception{
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("查無資料");
		long result = group.getGroupIdByNo("adfdsfasd");
		assertEquals(-1,result);
	}

	@Ignore("TEST OK")
	@Test
	public void getGroupIdByNoTest() throws Exception{
		long result = group.getGroupIdByNo("SYSCOM");
		assertEquals(result, 1);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupByIdException1Test() throws Exception {
		Syscomgroup syscomgroup = new Syscomgroup();
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		Syscomgroup result = group.getGroupById(syscomgroup);
		assertNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupByIdException2Test() throws Exception {
		Syscomgroup syscomgroup = new Syscomgroup();
		syscomgroup.setGroupid(1111);
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("查無資料");
		Syscomgroup result = group.getGroupById(syscomgroup);
		assertNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupByIdTest() throws Exception{
		Syscomgroup syscomgroup = new Syscomgroup();
		syscomgroup.setGroupid(1);
		Syscomgroup rSyscomgroup = group.getGroupById(syscomgroup);
		assertEquals(rSyscomgroup.getGroupno(), "SYSCOM");
	}
	
	@Ignore("待處理")
	@Test
	public void getGroupById2Exception1Test() throws Exception {
		
	}
	
	@Ignore("待處理")
	@Test
	public void getAllGroupsTest() throws Exception {
		
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByIdException1Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		SyscomgroupAndCulture result = group.getGroupDataById(null,"");
		assertNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByIdException2Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("查無資料");
		SyscomgroupAndCulture result = group.getGroupDataById(666,"zh-TW");
		assertNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByIdTest() throws Exception {
		SyscomgroupAndCulture result = group.getGroupDataById(1,"zh-TW");
		assertEquals(result.getGroupno(), "SYSCOM");
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByNameExceptionTest() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組名稱不得空白");
		List<SyscomgroupAndCulture> result = group.getGroupDataByName(null,"");
		assertNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByName1Test() throws Exception {
		List<SyscomgroupAndCulture> result = group.getGroupDataByName("SYSCOM","FC");
		assertNotNull(result);
		assertEquals(0,result.size());
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByName2Test() throws Exception {
		List<SyscomgroupAndCulture> result = group.getGroupDataByName("SYSCOM","");
		assertNotNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByNoException1Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組代碼不得空");
		List<SyscomgroupAndCulture> result = group.getGroupDataByNo(null,"");
		assertNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByNoException2Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("查無資料");
		group.getGroupDataByNo("fkajfla","");

	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByNoException3Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("Cant find any record");
		group.getGroupDataByNo("SYSCOM","en-US");
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByNo1Test() throws Exception {
		List<SyscomgroupAndCulture> result = group.getGroupDataByNo("16","");
		assertNotNull(result);
		assertEquals("跨行匯款作業中心管理-櫃員",result.get(0).getGroupname());
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupDataByNo2Test() throws Exception {
		List<SyscomgroupAndCulture> result = group.getGroupDataByNo("16","zh-TW");
		assertNotNull(result);
		assertEquals("跨行匯款作業中心管理-櫃員",result.get(0).getGroupname());
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupMembersException1Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		int result = group.removeGroupMembers(null,null,"");
		assertEquals(-1,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupMembersException2Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("下層序號未傳入");
		int result = group.removeGroupMembers(253,null,"");
		assertEquals(-1,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupMembersException3Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("成員類別不得空白");
		int result = group.removeGroupMembers(253,253,"");
		assertEquals(-1,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupMembers1Test() throws Exception {
		int result = group.removeGroupMembers(999,999,"R");
		assertEquals(0,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupMembers2Test() throws Exception {
		int result = group.removeGroupMembers(253,253,"R");
		assertEquals(1,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeAllGroupMembersException1Test() throws Exception {
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		int result = group.removeAllGroupMembers(null);
		assertEquals(-1,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeAllGroupMembers1Test() throws Exception {
		int result = group.removeAllGroupMembers(999);
		assertEquals(0,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeAllGroupMembers2Test() throws Exception {
		int result = group.removeAllGroupMembers(256);
		assertEquals(1,result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getUNSelectedMembersByIdException1Test() throws Exception{
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		List<SyscomgroupmembersAndGroupLevel> result = group.getUNSelectedMembersById(null,"zh-TW");
		assertNotNull(result);
	}

	@Ignore("TEST OK")
	@Test
	public void getUNSelectedMembersById1Test() throws Exception{
		List<SyscomgroupmembersAndGroupLevel> result = group.getUNSelectedMembersById(1,"zh-TW");		
		assertNotNull(result);
		assertThat (result.size(), greaterThan(10));	
	}

	@Ignore("TEST OK")
	@Test
	public void getSelectedMembersByIdException1Test() throws Exception{		
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		List<SyscomgroupmembersAndGroupLevel> result = group.getSelectedMembersById(null,"zh-TW");
		assertNotNull(result);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getSelectedMembersById1Test() throws Exception{
		List<SyscomgroupmembersAndGroupLevel> result = group.getSelectedMembersById(1,"zh-TW");		
		assertNotNull(result);
		assertEquals (result.size(), 14);	
	}
	
	@Ignore("TEST OK")
	@Test
	public void getNestedMembersByIdException1Test() throws Exception{		
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");
		group.getNestedMembersById(null,"zh-TW");
	}
	
	@Ignore("TEST OK")
	@Test
	public void getNestedMembersById1Test() throws Exception{
		String culture ="";
		List<SyscomgroupmembersAndGroupLevel> result = group.getNestedMembersById(1,culture);				
		assertNotNull(result);
		assertThat (result.size(), greaterThan(10));
		assertEquals("",culture);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getNestedMembersById2Test() throws Exception{
		String culture =null;
		List<SyscomgroupmembersAndGroupLevel> result = group.getNestedMembersById(1,culture);				
		assertNotNull(result);
		assertThat (result.size(), greaterThan(10));
//		assertNull(culture);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getNestedMembersById3Test() throws Exception{
		List<SyscomgroupmembersAndGroupLevel> result = group.getNestedMembersById(1,"zh-TW");		
		assertNotNull(result);
		assertThat (result.size(), greaterThan(10));		
	}

	@Ignore("TEST OK")
	@Test
	public void addGroupCultureException1Test() throws Exception{
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");		
		group.addGroupCulture(oDefGroupCulture);
	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupCultureException2Test() throws Exception{
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		oDefGroupCulture.setGroupid(888888);
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("新增語系資料失敗");
		group.addGroupCulture(oDefGroupCulture);

	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupCultureTest() throws Exception{
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		oDefGroupCulture.setGroupid(888888);
		oDefGroupCulture.setGroupname("test");
		int rst = group.addGroupCulture(oDefGroupCulture);
		assertEquals(1,rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupCultureException11Test() throws Exception{
		String groupNo = null; 
		String culture = "zh-TW";
		String groupName = "test";
		String remark = "test";
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組代碼不得空白");
		group.addGroupCulture(groupNo,culture,groupName,remark);
	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupCultureException12Test() throws Exception{
		String groupNo = "fffff"; 
		String culture = "zh-TW";
		String groupName = "test";
		String remark = "test";
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("無此功能群組資料");
		group.addGroupCulture(groupNo,culture,groupName,remark);
	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupCulture11Test() throws Exception{
		String groupNo = "GroupTest"; 
		String culture = "zh-TW";
		String groupName = "test";
		String remark = "test";
		int rst = group.addGroupCulture(groupNo,culture,groupName,remark);
		assertEquals(1,rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupCultureException13Test() throws Exception{
		String groupNo = "GroupTest"; 
		String culture = "zh-TW";
		String groupName = "test";
		String remark = "test";
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("新增失敗");
		group.addGroupCulture(groupNo,culture,groupName,remark);
	}
	
	
	@Ignore("TEST OK")
	@Test
	public void updateGroupCultureException1Test() throws Exception{
		
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");		
		group.updateGroupCulture(oDefGroupCulture);

	}
	
	@Ignore("TEST OK")
	@Test
	public void updateGroupCultureException2Test() throws Exception{
		
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		oDefGroupCulture.setGroupid(777);
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("資料不存在無法修改");		
		group.updateGroupCulture(oDefGroupCulture);

	}
	

	@Test
	public void updateGroupCulture1Test() throws Exception{
		
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		oDefGroupCulture.setGroupid(32);
		oDefGroupCulture.setRemark("remark");
		int rst = group.updateGroupCulture(oDefGroupCulture);
		assertEquals(1,rst);

	}
	
	@Ignore("TEST OK")
	@Test
	public void updateGroupCultureException11Test() throws Exception{		
		String groupNo = ""; 
		String culture = "zh-TW";
		String groupName = "test";
		String remark = "test";
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組代碼不得空白");		
		group.updateGroupCulture(groupNo, culture, groupName, remark);

	}
	
	@Ignore("TEST OK")
	@Test
	public void updateGroupCultureException12Test() throws Exception{
		
		String groupNo = "7777"; 
		String culture = "zh-TW";
		String groupName = "test";
		String remark = "test";
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("資料不存在無法修改");		
		group.updateGroupCulture(groupNo, culture, groupName, remark);

	}
	
	@Ignore("TEST OK")
	@Test
	public void updateGroupCulture11Test() throws Exception{	
		String groupNo = "GroupTest"; 
		String culture = "zh-TW";
		String groupName = "test";
		String remark = "test updateGroupCulture11Test";
		int rst = group.updateGroupCulture(groupNo, culture, groupName, remark);
		assertEquals(1,rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupCultureException1Test() throws Exception{	
		Integer groupId = null;
		String culture = "zh-TW";
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");		
		group.removeGroupCulture(groupId, culture);

	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupCultureException2Test() throws Exception{	
		Integer groupId = 777;
		String culture = "zh-TW";
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("資料不存在無法刪除");		
		group.removeGroupCulture(groupId, culture);

	}
	
	@Ignore("TEST OK")
	@Test
	public void removeGroupCulture1Test() throws Exception{	
		Integer groupId = 253;
		String culture = "zh-TW";		
		int rst = group.removeGroupCulture(groupId, culture);
		assertEquals(1,rst);
	}

	@Ignore("TEST OK")
	@Test
	public void removeAllGroupCulturesException1Test() throws Exception{	
		Integer groupId = null;
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");		
		group.removeAllGroupCultures(groupId);

	}
	
	@Ignore("TEST OK")
	@Test
	public void removeAllGroupCulturesException2Test() throws Exception{	
		Integer groupId = 789999;
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("資料不存在無法刪除");		
		group.removeAllGroupCultures(groupId);

	}
	
	@Ignore("TEST OK")
	@Test
	public void removeAllGroupCultures1Test() throws Exception{	
		Integer groupId = 888888;		
		int rst = group.removeAllGroupCultures(groupId);
		assertThat (rst, greaterThan(0));
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupCultureByIdException1Test() throws Exception{	
		Syscomgroupculture oDefGroupCulture = null;
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");		
		group.getGroupCultureById(oDefGroupCulture);

	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupCultureByIdException2Test() throws Exception{	
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");		
		group.getGroupCultureById(oDefGroupCulture);

	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupCultureById1Test() throws Exception{	
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		oDefGroupCulture.setGroupid(9876549);
		Syscomgroupculture rst = group.getGroupCultureById(oDefGroupCulture);
		assertNull(rst);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupCultureById2Test() throws Exception{	
		Syscomgroupculture oDefGroupCulture = new Syscomgroupculture();
		oDefGroupCulture.setGroupid(999999);
		Syscomgroupculture rst = group.getGroupCultureById(oDefGroupCulture);
		assertNotNull(rst);
		assertEquals("test",rst.getGroupname());
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupCultureByIdException11Test() throws Exception{	
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");	
		group.getGroupCulturesById(null);
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupCultureById11Test() throws Exception{			
		List<Syscomgroupculture> rst = group.getGroupCulturesById(777777);
		assertNotNull(rst);
		assertEquals(0,rst.size());
	}
	
	@Ignore("TEST OK")
	@Test
	public void getGroupCultureById12Test() throws Exception{			
		List<Syscomgroupculture> rst = group.getGroupCulturesById(1);
		assertNotNull(rst);
		assertThat (rst.size(), greaterThan(0));
		assertEquals("SYSCOM",rst.get(0).getGroupname());
	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupMembersException1Test() throws Exception{	
		Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");	
		group.addGroupMembers(oDefGroupMembers);

	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupMembersException2Test() throws Exception{	
		Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
		oDefGroupMembers.setGroupid(777777);
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("下層序號未傳入");	
		group.addGroupMembers(oDefGroupMembers);

	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupMembersException3Test() throws Exception{	
		Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
		oDefGroupMembers.setGroupid(777777);
		oDefGroupMembers.setChildid(1);
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("成員類別不得空白");	
		group.addGroupMembers(oDefGroupMembers);

	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupMembersException4Test() throws Exception{	
		Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
		oDefGroupMembers.setGroupid(236);
		oDefGroupMembers.setChildid(227);
		oDefGroupMembers.setChildtype("R");
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("新增成員資料失敗");	
		group.addGroupMembers(oDefGroupMembers);

	}
	
	@Ignore("TEST OK")
	@Test
	public void addGroupMembersException5Test() throws Exception{	
		Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
		oDefGroupMembers.setGroupid(236);
		oDefGroupMembers.setChildid(228);
		oDefGroupMembers.setChildtype("R");
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("新增成員資料失敗");	
		group.addGroupMembers(oDefGroupMembers);

	}
	
	
	@Test
	public void addGroupMembersTest() throws Exception{	
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition posExpireddate = new ParsePosition(0);
		ParsePosition posEffectdate = new ParsePosition(0);
		
		Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
		oDefGroupMembers.setGroupid(236);
		oDefGroupMembers.setChildid(228);
		oDefGroupMembers.setChildtype("R");
		oDefGroupMembers.setLocationno(16);
		oDefGroupMembers.setEffectdate(sdf.parse("2030-01-07", posExpireddate));
		oDefGroupMembers.setExpireddate(sdf.parse("2020-01-07", posEffectdate));
		oDefGroupMembers.setUpdateuserid(1);
		oDefGroupMembers.setUpdatetime(new Date());

		int rst = group.addGroupMembers(oDefGroupMembers);
		assertEquals(1,rst);

	}
	
	
	@Test
	public void updateGroupMembersException1Test() throws Exception{	
		Syscomgroupmembers oDefGroupMembers = new Syscomgroupmembers();
		expectedEx.expect(SafeaaException.class);
		expectedEx.expectMessage("功能群組序號未傳入");	
		group.addGroupMembers(oDefGroupMembers);

	}
	
}
