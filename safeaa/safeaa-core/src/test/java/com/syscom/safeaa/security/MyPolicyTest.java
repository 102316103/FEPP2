package com.syscom.safeaa.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.safeaa.core.BaseTest;

/**
 *
 * @author syscom
 *
 */
public class MyPolicyTest  extends BaseTest {

    @Autowired
    private MyPolicy myPolicy2;

    @Before
    public void setUp() throws Exception {
//        System.out.println("******RoleResourceTest******<開始測試>");
    }

    @Test
    public void getInitTest(){
        myPolicy2.init(1);
        assertNotNull(myPolicy2);

        assertTrue(myPolicy2.ismAllowMultiLogOn());
    }

    @After
    public void tearDown() throws Exception {
//        System.out.println("******ResourceTest******<測試結束>");
    }
}
