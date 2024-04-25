package com.syscom.safeaa.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.safeaa.core.BaseTest;
import com.syscom.safeaa.mybatis.model.Syscomresource;
import com.syscom.safeaa.mybatis.model.Syscomresourceculture;
import com.syscom.safeaa.mybatis.vo.SyscomresourceAndCulture;


/**
 *
 * @author syscom
 * @date 2021/08/13
 */
public class ResourceTest extends BaseTest {

	@Autowired
	private Resource resourceService;

	@Before
	public void setUp() throws Exception {
//		System.out.println("******ResourceTest******<開始測試>");
		resourceService.setCurrentUserId(1);
	}

	@Ignore("TEST OK")
	@Test
	public void createResourceTest() throws Exception {
		Syscomresource resource = new Syscomresource();
		resource.setResourceno("1");
		resource.setEffectdate(new Date());
		resource.setExpireddate(new Date());
		resource.setUpdatetime(new Date());
		int rst = resourceService.createResource(resource);
		assertEquals(1, rst);

	}

	@Ignore("TEST OK")
	@Test
	public void updateResourceTest() throws Exception {
		Syscomresource resource = new Syscomresource();
		resource.setResourceid(246);
		resource.setResourceno("1");
		resource.setResourceurl("xxxx");
		resource.setEffectdate(new Date());
		resource.setExpireddate(new Date());
		resource.setUpdatetime(new Date());
		int rst = resourceService.updateResource(resource);
		assertEquals(1, rst);
	}

	@Ignore("TEST OK")
	@Test
	public void updateResourceTest2() throws Exception {
		Syscomresource resource = new Syscomresource();
		resource.setResourceno("1");
		resource.setResourceurl("yyyy");
		resource.setEffectdate(new Date());
		resource.setExpireddate(new Date());
		resource.setUpdatetime(new Date());
		int rst = resourceService.updateResource(resource);
		assertEquals(1, rst);
	}

	@Ignore("TEST OK")
	@Test
	public void deleteResourceTest() throws Exception {
		int resourceid = 246 ;
		boolean rst = resourceService.deleteResource(resourceid);
		assertTrue(rst);
	}

	@Ignore("TEST OK")
	@Test
	public void getResourceIdByNoTest() throws Exception {
		String resourceNo = "1";
		Integer resourceid = resourceService.getResourceIdByNo(resourceNo);
		assertNotNull(resourceid);
	}

	@Ignore("TEST OK")
	@Test
	public void getResourceByIdTest() throws Exception {
		int resourceid = 245;
		Syscomresource syscomresource = resourceService.getResourceById(resourceid);
		assertNotNull(syscomresource);
	}

	@Ignore("Not Ready to Run")
	@Test
	public void getResourceByIdTest2() throws Exception {
		Syscomresource syscomresource = new Syscomresource();
		syscomresource.setResourceid(44);
		Syscomresourceculture syscomresourceculture = new Syscomresourceculture();
		syscomresourceculture.setResourceid(44);
		syscomresourceculture.setCulture("zh-TW");
		int rst = resourceService.getResourceById(syscomresource,syscomresourceculture);
		assertEquals(1,rst);
		assertEquals("變更押碼基碼-0102",syscomresourceculture.getResourcename());
	}

	@Ignore("TEST OK")
	@Test
	public void getAllResourcesTest()  throws Exception {
		List<SyscomresourceAndCulture> list = resourceService.getAllResources("zh-TW");
		assertNotNull(list);

		List<SyscomresourceAndCulture> list2 = resourceService.getAllResources("zh-TW2");
		assertEquals(0, list2.size());
	}

	@Ignore("TEST OK")
	@Test
	public void getResourceDataByIdTest()  throws Exception {
		List<SyscomresourceAndCulture> list = resourceService.getResourceDataById(44,"zh-TW");
		assertNotNull(list);

		List<SyscomresourceAndCulture> list2 = resourceService.getResourceDataById(44,"zh-TW2");
		assertEquals(0, list2.size());
	}

	@Ignore("TEST OK")
	@Test
	public void getResourceDataByNameTest()  throws Exception {
		List<SyscomresourceAndCulture> list = resourceService.getResourceDataByName("變更押碼基碼-0102","zh-TW");
		assertNotNull(list);

		List<SyscomresourceAndCulture> list2 = resourceService.getResourceDataByName("變更押碼基碼-0102","zh-TW2");
		assertEquals(0, list2.size());
	}

	@Ignore("TEST OK")
	@Test
	public void getResourceDataByNoTest()  throws Exception {
		List<SyscomresourceAndCulture> list = resourceService.getResourceDataByNo("010102","zh-TW");
		assertNotNull(list);

		List<SyscomresourceAndCulture> list2 = resourceService.getResourceDataByNo("010102","zh-TW2");
		assertEquals(0, list2.size());
	}

	@Ignore("TEST OK")
	@Test
	public void addResourceCultureTest()  throws Exception {
		Syscomresourceculture oDefResourceCulture1 = new Syscomresourceculture();
		oDefResourceCulture1.setResourceid(245);
		oDefResourceCulture1.setResourcename("xxxxxxx");
		int rst1 =resourceService.addResourceCulture(oDefResourceCulture1);
		assertEquals(1, rst1);

		Syscomresourceculture oDefResourceCulture2 = new Syscomresourceculture();
		oDefResourceCulture2.setResourceid(246);
		oDefResourceCulture2.setCulture("zh-TW");
		oDefResourceCulture2.setResourcename("xxxxxxx");
		int rst2 =resourceService.addResourceCulture(oDefResourceCulture2);
		assertEquals(1, rst2);
	}

	@Test
	public void addResourceCultureTest2()  throws Exception {

		String resourceNo1 = "1";
		String culture1 = "zh-TW";
		String resourceName1 = "xxxxxxx";
		String remark1 = "xxxxxxx";

		int rst1 =resourceService.addResourceCulture(resourceNo1,culture1,resourceName1,remark1);
		assertEquals(1, rst1);

		String resourceNo2 = "2";
		String culture2 = "";
		String resourceName2 = "xxxxxxx";
		String remark2 = "xxxxxxx";

		int rst2=resourceService.addResourceCulture(resourceNo2,culture2,resourceName2,remark2);
		assertEquals(1, rst2);
	}

	@Ignore("TEST OK")
	@Test
	public void updateResourceCultureTest()  throws Exception {

		Syscomresourceculture oDefResourceCulture = new Syscomresourceculture();
		oDefResourceCulture.setResourceid(246);
		oDefResourceCulture.setCulture("zh-TW");
		oDefResourceCulture.setResourcename("yyyyyyyy");
		int rst = resourceService.updateResourceCulture(oDefResourceCulture);
		assertEquals(1, rst);
	}


	@Test
	public void updateResourceCultureTest2()  throws Exception {

		String resourceNo1 = "1";
		String culture1 = "zh-TW";
		String resourceName1 = "fffff";
		String remark1 = "ffff";

		int rst1 = resourceService.updateResourceCulture(resourceNo1,culture1,resourceName1,remark1);
		assertEquals(1, rst1);

		String resourceNo2 = "2";
		String culture2 = null;
		String resourceName2 = "fff";
		String remark2 = "xxxffxxxx";

		int rst2 = resourceService.updateResourceCulture(resourceNo2,culture2,resourceName2,remark2);
		assertEquals(1, rst2);

		String resourceNo3 = "2";
		String culture3 = null;
		String resourceName3 = "fff";
		String remark3 = "";

		int rst3 = resourceService.updateResourceCulture(resourceNo3,culture3,resourceName3,remark3);
		assertEquals(1, rst3);
	}


	@Test
	public void removeResourceCultureTest()  throws Exception {
		int resourceId1 = 245;
		String culture1 = "zh-TW";
		int rst1 = resourceService.removeResourceCulture(resourceId1,culture1);
		assertEquals(1, rst1);

		int resourceId2 = 246;
		String culture2 = "";
		int rst2 = resourceService.removeResourceCulture(resourceId2,culture2);
		assertEquals(1, rst2);
	}

	@Ignore("TEST OK")
	@Test
	public void removeAllResourceCulturesTest()  throws Exception {
		int resourceId = 245;
		int rst1 = resourceService.removeAllResourceCultures(resourceId);
		assertEquals(1, rst1);
	}

	@Ignore("TEST OK")
	@Test
	public void getResourceCultureByIdTest()  throws Exception {
		int resourceId1 = 246;
		String culture1 = "zh-TW";
		Syscomresourceculture sc1 = resourceService.getResourceCultureById(resourceId1,culture1);
		assertNotNull(sc1);

		int resourceId2 = 246;
		String culture2 = "";
		Syscomresourceculture sc2 = resourceService.getResourceCultureById(resourceId2,culture2);
		assertNotNull(sc2);
	}

	@Ignore("TEST OK")
	@Test
	public void getResourceCulturesByIdTest()  throws Exception {
		int resourceId1 = 246;
		List<Syscomresourceculture> list = resourceService.getResourceCulturesById(resourceId1);
		assertNotNull(list);
		assertEquals(1, list.size());
	}

	@After
	public void tearDown() throws Exception {
//		System.out.println("******ResourceTest******<測試結束>");
	}
}
