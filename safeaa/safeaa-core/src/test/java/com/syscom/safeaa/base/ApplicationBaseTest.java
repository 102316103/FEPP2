/**
 * 
 */
package com.syscom.safeaa.base;

import java.util.Date;

import com.syscom.safeaa.mybatis.model.Syscomrole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author JenniferYin
 *
 */
class ApplicationBaseTest {
//	private String testXml ="";
//	private String testJson ="";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		testSerializeToXml();
		testSerializeToJson();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}
	
	public class ApplicationBaseEx extends ApplicationBase{
		
	}

	@Test
	void testSerializeToXml()  {
		Syscomrole role = new Syscomrole();
		role.setRoleid(123456789);
		role.setRoleno("administrator");
		role.setRoletype("permanate");
		role.setEffectdate(new Date(System.currentTimeMillis()-100000));
		role.setExpireddate(new Date(System.currentTimeMillis()+ 20000000L));
		role.setUpdateuserid(321321);
		

	}

	@Test
	void testDeserializeFromXml()  {

//		ApplicationBase base = new ApplicationBaseEx();
//		Syscomrole role  = (Syscomrole)base.deserializeFromXml(testXml, Syscomrole.class);
//		
//		System.out.println(role.toString());
//		System.out.println(role.getClass());
	}
	
	@Test
	void testSerializeToJson()  {
		Syscomrole role = new Syscomrole();
		role.setRoleid(123456789);
		role.setRoleno("administrator");
		role.setRoletype("permanate");
		role.setEffectdate(new Date(System.currentTimeMillis()-100000));
		role.setExpireddate(new Date(System.currentTimeMillis()+ 20000000L));
		role.setUpdateuserid(321321);
		
//		ApplicationBase base = new ApplicationBaseEx();
//		String xml = base.serializeToJson(role);
//		testJson = new String (xml);
//		System.out.println(testJson);
	}
	
	@Test
	void testDeserializeFromJson()  {

		ApplicationBase base = new ApplicationBaseEx();
//		Syscomrole role  = (Syscomrole)base.deserializeFromJson(testJson, Syscomrole.class);
//		
//		System.out.println(role.toString());
//		System.out.println(role.getClass());
	}	
}
