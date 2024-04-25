package com.syscom.safeaa.core.test;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.syscom.safeaa.security.ResourceTest;
import com.syscom.safeaa.security.RoleResourceTest;


/**
 * SecuritySuitTest
 * 
 * @author syscom
 * @date 2021/08/13
 */
@SuiteClasses({
	ResourceTest.class,
	RoleResourceTest.class
})
@RunWith(Suite.class)
public class SecuritySuitTest {

}
