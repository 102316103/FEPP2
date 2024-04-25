package com.syscom.safeaa.core.test;

import com.syscom.safeaa.mybatis.dao.SyscomuserDao;
import com.syscom.safeaa.utils.SafeaaSpringBeanFactoryUtil;

public class Daotest {
    // dao test
    private int daoTest(){
        SyscomuserDao dao = SafeaaSpringBeanFactoryUtil.getBean("syscomuserDao");
        long userid = (long)0;
        return dao.deleteByPrimaryKey(userid);
    }
}

